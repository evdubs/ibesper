package com.squirrelhill.ibesper.ibmodel;

public enum MarketDataBarType {
	TRADE {
		public String toString() {
			return "Trade";
		}
	},
	MIDPOINT {
		public String toString() {
			return "Midpoint";
		}
	},
	BID {
		public String toString() {
			return "Bid";
		}
	},
	ASK {
		public String toString() {
			return "Ask";
		}
	},
	BID_ASK {
		public String toString() {
			return "Bid/Ask";
		}
	},
	HISTORICAL_VOLATILITY {
		public String toString() {
			return "Historical volatility";
		}
	},
	IMPLIED_VOLATILITY {
		public String toString() {
			return "Option implied volatility";
		}
	};
	
	public static String getIbString(MarketDataBarType barType) {
		if (barType == null)
			return null;
		
		switch(barType) {
		case TRADE:
			return "TRADES";
		case MIDPOINT:
			return "MIDPOINT";
		case BID:
			return "BID";
		case ASK:
			return "ASK";
		case BID_ASK:
			return "BID_ASK";
		case HISTORICAL_VOLATILITY:
			return "HISTORICAL_VOLATILITY";
		case IMPLIED_VOLATILITY:
			return "OPTION_IMPLIED_VOLATILITY";
		}
		
		return null;
	}
}
