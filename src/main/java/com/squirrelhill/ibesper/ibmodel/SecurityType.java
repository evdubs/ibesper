package com.squirrelhill.ibesper.ibmodel;

public enum SecurityType {
	STOCK {
		public String toString() {
			return "STK";
		}
	}, 
	OPTION {
		public String toString() {
			return "OPT";
		}
	}, 
	FUTURE {
		public String toString() {
			return "FUT";
		}
	}, 
	INDEX {
		public String toString() {
			return "IND";
		}
	}, 
	FUTURE_OPTION {
		public String toString() {
			return "FOP";
		}
	}, 
	CASH {
		public String toString() {
			return "CASH";
		}
	}, 
	BAG {
		public String toString() {
			return "BAG";
		}
	}
}
