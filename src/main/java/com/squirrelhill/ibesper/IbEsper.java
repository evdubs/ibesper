package com.squirrelhill.ibesper;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.squirrelhill.ibesper.ibmodel.ContractFactory;
import com.squirrelhill.ibesper.ibmodel.InvalidContractException;
import com.squirrelhill.ibesper.listener.TickPriceEventListener;

/**
 * 
 */
public class IbEsper 
{	
    public static void main( String[] args )
    {   
        PropertyConfigurator.configure(System.getProperty("user.dir") + "/log4j.properties");
        
        Logger log = Logger.getLogger(IbEsper.class);
        
        EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider();
        String expressionString = "select avg(price) from com.squirrelhill.ibesper.event.TickPriceEvent.win:time(30 sec)";
        EPStatement statement = epService.getEPAdministrator().createEPL(expressionString);
        statement.addListener(new TickPriceEventListener());

        IbDataProvider dataProvider = new IbDataProvider(epService);
        
        try {
			dataProvider.requestLiveMarketData(ContractFactory.createEquity("VXX"), false);
			
		} catch (InvalidContractException ex) {
			log.error("Could not instantiate VXX", ex);
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
