package com.squirrelhill.ibesper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.espertech.esper.client.EPServiceProvider;
import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.TickType;
import com.ib.client.UnderComp;
import com.squirrelhill.ibesper.event.MarketDataBarEvent;
import com.squirrelhill.ibesper.event.TickPriceEvent;
import com.squirrelhill.ibesper.ibmodel.MarketDataBarType;
import com.squirrelhill.ibesper.ibmodel.NegativeTimeException;

public class IbDataProducer implements EWrapper {
	private static Logger log = Logger.getLogger(IbDataProducer.class);
	
	private static SimpleDateFormat endDateFormat = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
	
	private static final long SIX_MONTHS_IN_MILLIS = 15775800000L;

	private EClientSocket ibSocket;
	private EPServiceProvider epService;
	private IntObjectOpenHashMap<Contract> requestIdContractMap;
	private IntObjectOpenHashMap<AsyncCallback<MarketDataBarEvent>> requestIdBarCallbackMap;
	private AtomicInteger latestRequestId;

	public IbDataProducer() {
		latestRequestId = new AtomicInteger(0);
		ibSocket = new EClientSocket(this);
		ibSocket.eConnect("127.0.0.1", 7496, 1);
		
		requestIdContractMap = new IntObjectOpenHashMap<Contract>();
		requestIdBarCallbackMap = new IntObjectOpenHashMap<AsyncCallback<MarketDataBarEvent>>();
	}

	@Override
	public void connectionClosed() {
		log.warn("Connection closed by TWS");
	}

	@Override
	public void error(Exception e) {
		log.error("Received an exception from EClientSocket", e);
	}

	@Override
	public void error(String str) {
		log.error("Received an error from EClientSocket: \n" + str);
	}

	@Override
	public void error(int tickerId, int errorCode, String errorString) {
		// TODO Provide translations for error codes
		log.error("Received an error(" + errorCode + ") from EClientSocket: \n" + errorString);
	}

	@Override
	public void accountDownloadEnd(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void bondContractDetails(int arg0, ContractDetails arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contractDetails(int arg0, ContractDetails arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void contractDetailsEnd(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void currentTime(long arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deltaNeutralValidation(int arg0, UnderComp arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void execDetails(int arg0, Contract arg1, Execution arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void execDetailsEnd(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fundamentalData(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Convert the given datetimes into the format "N S" where N is the
	 * amount of seconds between the range.
	 * 
	 * @param startDate Starting time
	 * @param endDate Ending time
	 * @return "N S" formatted string
	 * @throws NegativeTimeException startDate > endDate
	 */
	private String generateSecondDuration(Date startDate, Date endDate) 
			throws NegativeTimeException {
		if (endDate.getTime() - startDate.getTime() < 0)
			throw new NegativeTimeException("Cannot generate second duration " +
					"between (" + startDate + ", " + endDate + ")");
		long millis = endDate.getTime() - startDate.getTime();
		
		return (millis / 1000) + " S";
	}
	
	/**
	 * Request historical market data. This method
	 * requests the highest resolution available (see 
	 * <a href="http://www.interactivebrokers.com/en/software/api/apiguide/api/historical_data_limitations.htm">here</a> 
	 * for more info).
	 * 
	 * @param contract Contract to request market data for
	 * @param startDate Starting time
	 * @param endDate Ending time
	 * @throws NegativeTimeException startDate > endDate
	 */
	public void requestHistoricalMarketData(Contract contract, Date startDate, 
			Date endDate, MarketDataBarType barType, AsyncCallback<MarketDataBarEvent> cb) 
					throws NegativeTimeException {
		String barPeriod = "1 secs";
		
		if (endDate.getTime() - startDate.getTime() > SIX_MONTHS_IN_MILLIS)
			barPeriod = "1 min";
		
		if (ibSocket.isConnected()) {
			String duration = generateSecondDuration(startDate, endDate);
			
			int requestId = latestRequestId.incrementAndGet();
			
			synchronized (requestIdContractMap) {
				requestIdContractMap.put(requestId, contract);
			}
			
			synchronized (requestIdBarCallbackMap) {
				requestIdBarCallbackMap.put(requestId, cb);
			}
			
			ibSocket.reqHistoricalData(requestId, contract, 
					endDateFormat.format(endDate), duration, barPeriod, 
					MarketDataBarType.getIbString(barType), 0, 2);
		} else {
			log.warn("Tried to request market data while not connected");
		}
	}

	@Override
	public void historicalData(int requestId, String formattedDate, double openPrice, double highPrice,
			double lowPrice, double closePrice, int volume, int tradeCount, double weightedAverage,
			boolean hasGaps) {
		Contract c = null;
		
		synchronized (requestIdContractMap) {
			c = requestIdContractMap.get(requestId);
		}
		
		MarketDataBarEvent barEvent = new MarketDataBarEvent();
		barEvent.setContract(c);
		
		Date dateTime = new Date(Long.parseLong(formattedDate));
		barEvent.setDateTime(dateTime);
		
		barEvent.setOpenPrice(openPrice);
		barEvent.setHighPrice(highPrice);
		barEvent.setLowPrice(lowPrice);
		barEvent.setClosePrice(closePrice);
		barEvent.setVolume(volume);
		barEvent.setVwap(weightedAverage);
		barEvent.setHasGaps(hasGaps);
		
		AsyncCallback<MarketDataBarEvent> cb = null;
		
		synchronized (requestIdBarCallbackMap) {
			cb = requestIdBarCallbackMap.get(requestId);
		}
		
		if (cb != null)
			cb.onSuccess(barEvent);
	}

	@Override
	public void managedAccounts(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void marketDataType(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void nextValidId(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void openOrder(int arg0, Contract arg1, Order arg2, OrderState arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void openOrderEnd() {
		// TODO Auto-generated method stub

	}

	@Override
	public void orderStatus(int arg0, String arg1, int arg2, int arg3,
			double arg4, int arg5, int arg6, double arg7, int arg8, String arg9) {
		// TODO Auto-generated method stub

	}

	@Override
	public void realtimeBar(int arg0, long arg1, double arg2, double arg3,
			double arg4, double arg5, long arg6, double arg7, int arg8) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveFA(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scannerData(int arg0, int arg1, ContractDetails arg2,
			String arg3, String arg4, String arg5, String arg6) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scannerDataEnd(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scannerParameters(String arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * Request simple market data. 
	 * 
	 * @param contract Contract to request market data for
	 * @param snapshot True for just one snapshot; False for streaming data
	 */
	public void requestLiveMarketData(Contract contract, boolean snapshot) {
		if (ibSocket.isConnected()) {
			int requestId = latestRequestId.incrementAndGet();
			
			synchronized (requestIdContractMap) {
				requestIdContractMap.put(requestId, contract);
			}
			
			requestIdContractMap.put(requestId, contract);
			ibSocket.reqMktData(requestId, contract, null, snapshot);
		} else {
			log.warn("Tried requesting market data while not connected");
		}
	}

	@Override
	public void tickEFP(int arg0, int arg1, double arg2, String arg3,
			double arg4, int arg5, String arg6, double arg7, double arg8) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickGeneric(int arg0, int arg1, double arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickOptionComputation(int arg0, int arg1, double arg2,
			double arg3, double arg4, double arg5, double arg6, double arg7,
			double arg8, double arg9) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickPrice(int requestId, int tickTypeId, double price, int canAutoExecute) {
		log.debug("Received tick price for " + requestId + " " + tickTypeId + " " + price
				+ " " + canAutoExecute);
		TickPriceEvent tickPriceEvent = new TickPriceEvent();
		tickPriceEvent.setContract(requestIdContractMap.get(requestId));
		tickPriceEvent.setTickType(TickType.getField(tickTypeId));
		tickPriceEvent.setPrice(price);
		tickPriceEvent.setTimestamp(new Date());
		
		epService.getEPRuntime().sendEvent(tickPriceEvent);
	}

	@Override
	public void tickSize(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickSnapshotEnd(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickString(int arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateAccountTime(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateAccountValue(String arg0, String arg1, String arg2,
			String arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateMktDepth(int arg0, int arg1, int arg2, int arg3,
			double arg4, int arg5) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateMktDepthL2(int arg0, int arg1, String arg2, int arg3,
			int arg4, double arg5, int arg6) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateNewsBulletin(int arg0, int arg1, String arg2, String arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updatePortfolio(Contract arg0, int arg1, double arg2,
			double arg3, double arg4, double arg5, double arg6, String arg7) {
		// TODO Auto-generated method stub

	}

	@Override
	public void commissionReport(CommissionReport arg0) {
		// TODO Auto-generated method stub
		
	}

}
