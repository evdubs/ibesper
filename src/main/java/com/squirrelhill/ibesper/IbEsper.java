package com.squirrelhill.ibesper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.squirrelhill.ibesper.event.MarketDataBarEvent;
import com.squirrelhill.ibesper.ibmodel.ContractFactory;
import com.squirrelhill.ibesper.ibmodel.InvalidContractException;
import com.squirrelhill.ibesper.ibmodel.MarketDataBarType;
import com.squirrelhill.ibesper.ibmodel.NegativeTimeException;
import com.squirrelhill.ibesper.listener.TickPriceEventListener;

/**
 * 
 */
public class IbEsper 
{	
    public static void main( String[] args )
    {   
    	SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    	
        PropertyConfigurator.configure(System.getProperty("user.dir") + "/log4j.properties");
        
        Logger log = Logger.getLogger(IbEsper.class);
        
        EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider();
        String expressionString = "select avg(price) from com.squirrelhill.ibesper.event.TickPriceEvent.win:time(30 sec)";
        EPStatement statement = epService.getEPAdministrator().createEPL(expressionString);
        statement.addListener(new TickPriceEventListener());

        IbDataProducer dataProvider = new IbDataProducer();
        final SQLiteDataConsumer dataConsumer = new SQLiteDataConsumer("jdbc:sqlite:mktdata.db");
        
        try {
        	Date startDate = dateTimeFormat.parse("20130314 13:30:00");
        	Date endDate = dateTimeFormat.parse("20130314 14:00:00");
			dataProvider.requestHistoricalMarketData(ContractFactory.createEquity("VXX"), startDate, endDate, 
					MarketDataBarType.TRADE, new AsyncCallback<MarketDataBarEvent>() {
				@Override
				public void onSuccess(MarketDataBarEvent t) {
					dataConsumer.consumeHistoricalMarketDataBar(t);
				}
			});
		} catch (InvalidContractException ex) {
			log.error("Could not instantiate VXX", ex);
		} catch (ParseException e) {
			log.error("Could not convert dates", e);
		} catch (NegativeTimeException e) {
			log.error("Check your dates to make sure endDate > startDate", e);
		}
        
        while (true) {
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				log.error("Main sleep thread interrupted", ex);
			}
        }
    }
}
