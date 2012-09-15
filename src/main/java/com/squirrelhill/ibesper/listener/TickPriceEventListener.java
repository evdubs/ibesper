package com.squirrelhill.ibesper.listener;

import org.apache.log4j.Logger;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class TickPriceEventListener implements UpdateListener {
	private static Logger log = Logger.getLogger(TickPriceEventListener.class);
	
	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		EventBean eventBean = newEvents[0];
		log.debug("Found event " + eventBean);
	}

}
