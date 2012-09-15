package com.squirrelhill.ibesper.ibmodel;

import com.ib.client.Contract;

public class ContractFactory {
	public static Contract createEquity(String symbol) 
			throws InvalidContractException {
		return createContract(symbol, SecurityType.STOCK, Exchange.SMART, 
				Currency.USD, "", null, 0);
	}
	
	public static Contract createOption(String symbol, String expiry, 
			double strike, CallPut callPut) throws InvalidContractException {
		return createContract(symbol, SecurityType.OPTION, Exchange.SMART, 
				Currency.USD, expiry, callPut, strike);
	}
	
	public static Contract createFuture(String symbol, String expiry) 
			throws InvalidContractException {
		return createContract(symbol, SecurityType.FUTURE, Exchange.SMART, 
				Currency.USD, expiry, null, 0);
	}
	
	public static Contract createContract(String symbol, 
			SecurityType securityType, Exchange exchange, Currency currency, 
			String expiry, CallPut callPut, double strike) 
					throws InvalidContractException {
		Contract contract = new Contract();
		
		if (symbol != null)
			contract.m_symbol = symbol;
		else
			throw new InvalidContractException("No symbol specified");
		
		if (securityType != null)
			contract.m_secType = securityType.toString();
		else 
			throw new InvalidContractException("No security type specified");
		
		if (exchange != null)
			contract.m_exchange = exchange.toString();
		else
			throw new InvalidContractException("No exchange specified");
		
		if (currency != null)
			contract.m_currency = currency.toString();
		else
			throw new InvalidContractException("No currency specified for contract");
		contract.m_expiry = expiry;
		
		if (callPut != null)
			contract.m_right = callPut.toString();
		
		contract.m_strike = strike;
		
		return contract;
	}
}
