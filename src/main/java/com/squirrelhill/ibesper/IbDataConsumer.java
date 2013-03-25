package com.squirrelhill.ibesper;

import com.squirrelhill.ibesper.event.MarketDataBarEvent;

public interface IbDataConsumer {
	public void consumeHistoricalMarketDataBar(MarketDataBarEvent barEvent);
}
