package com.squirrelhill.ibesper.event;

import java.util.Date;

import com.ib.client.Contract;

public class TickPriceEvent {
	private Contract contract;
	private String tickType;
	private double price;
	private Date timestamp;
	
	public TickPriceEvent() {
	}

	public Contract getContract() {
		return contract;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}

	public String getTickType() {
		return tickType;
	}

	public void setTickType(String tickType) {
		this.tickType = tickType;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
}
