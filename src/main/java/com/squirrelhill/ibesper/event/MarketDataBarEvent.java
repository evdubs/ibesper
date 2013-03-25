package com.squirrelhill.ibesper.event;

import java.util.Date;

import com.ib.client.Contract;
import com.squirrelhill.ibesper.ibmodel.MarketDataBarType;

public class MarketDataBarEvent {
	private Contract contract;
	private Date dateTime;
	private MarketDataBarType barType;
	private double openPrice;
	private double highPrice;
	private double lowPrice;
	private double closePrice;
	private int volume;
	private int count;
	private double vwap;
	private boolean hasGaps;
	
	public MarketDataBarEvent() {
		
	}
	
	public Contract getContract() {
		return contract;
	}
	
	public void setContract(Contract contract) {
		this.contract = contract;
	}
	
	public Date getDateTime() {
		return dateTime;
	}
	
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}
	
	public MarketDataBarType getBarType() {
		return barType;
	}

	public void setBarType(MarketDataBarType barType) {
		this.barType = barType;
	}

	public double getOpenPrice() {
		return openPrice;
	}
	
	public void setOpenPrice(double openPrice) {
		this.openPrice = openPrice;
	}
	
	public double getHighPrice() {
		return highPrice;
	}
	
	public void setHighPrice(double highPrice) {
		this.highPrice = highPrice;
	}
	
	public double getLowPrice() {
		return lowPrice;
	}
	
	public void setLowPrice(double lowPrice) {
		this.lowPrice = lowPrice;
	}
	
	public double getClosePrice() {
		return closePrice;
	}
	
	public void setClosePrice(double closePrice) {
		this.closePrice = closePrice;
	}
	
	public int getVolume() {
		return volume;
	}
	
	public void setVolume(int volume) {
		this.volume = volume;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public double getVwap() {
		return vwap;
	}
	
	public void setVwap(double vwap) {
		this.vwap = vwap;
	}
	
	public boolean isHasGaps() {
		return hasGaps;
	}
	
	public void setHasGaps(boolean hasGaps) {
		this.hasGaps = hasGaps;
	}

	@Override
	public String toString() {
		return "MarketDataBarEvent [contract=" + contract + ", dateTime="
				+ dateTime + ", barType=" + barType + ", openPrice="
				+ openPrice + ", highPrice=" + highPrice + ", lowPrice="
				+ lowPrice + ", closePrice=" + closePrice + ", volume="
				+ volume + ", count=" + count + ", vwap=" + vwap + ", hasGaps="
				+ hasGaps + "]";
	}
}