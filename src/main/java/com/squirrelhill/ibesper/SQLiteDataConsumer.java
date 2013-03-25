package com.squirrelhill.ibesper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.apache.log4j.Logger;
import org.joda.time.field.OffsetDateTimeField;

import com.squirrelhill.ibesper.event.MarketDataBarEvent;

public class SQLiteDataConsumer implements IbDataConsumer {
	private static Logger log = Logger.getLogger(SQLiteDataConsumer.class);
	
	private Connection connection;
	
	public SQLiteDataConsumer(String jdbcUrl) {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			log.error("Could not load SQLite JDBC driver. " +
					"Cannot operate Consumer", e);
			return;
		}
		
		try {
			connection = DriverManager.getConnection(jdbcUrl);
			
			Statement createBarTypeTable = connection.createStatement();
			createBarTypeTable.execute("CREATE TABLE IF NOT EXISTS bar_type " +
					"(id integer, name text)");
			createBarTypeTable.close();
			
			Statement createHistoricalBarsTable = connection.createStatement();
			createHistoricalBarsTable.execute("CREATE TABLE IF NOT EXISTS hist_bars " +
					"(symbol text, " +
					"datetime datetime, " +
					"bar_type_id integer, " +
					"open_price numeric, " +
					"high_price numeric, " +
					"low_price numeric, " +
					"close_price numeric, " +
					"volume integer, " +
					"count integer, " +
					"vwap numeric, " +
					"has_gaps boolean)");
			createHistoricalBarsTable.close();
			
			Statement populateBarTypeTable = connection.createStatement();
			populateBarTypeTable.execute("INSERT OR IGNORE INTO bar_type (id, name) VALUES " +
					"(1, 'TRADE'), " +
					"(2, 'MIDPOINT'), " +
					"(3, 'BID'), " +
					"(4, 'ASK'), " +
					"(5, 'BID_ASK'), " +
					"(6, 'HISTORICAL_VOLATILITY'), " +
					"(7, 'IMPLIED_VOLATILITY')");
			populateBarTypeTable.close();
		} catch (SQLException e) {
			log.error("Could not establish connection to mktdata.db. " +
					"Cannot operate Consumer", e);
			return;
		}
	}

	@Override
	public void consumeHistoricalMarketDataBar(MarketDataBarEvent barEvent) {
		if (barEvent == null || barEvent.getContract() == null) {
			log.error("Tried to insert either a null barEvent or barEvent " +
					"with a null contract. This is invalid");
			return;
		}
		
		try {
			PreparedStatement ps = connection.prepareStatement("INSERT INTO hist_bars (symbol, datetime, " +
					"bar_type_id, open_price, high_price, low_price, close_price, volume, count, vwap, has_gaps) " +
					"VALUES (?, ?, (SELECT id FROM bar_type where name = ?), ?, ?, ?, ?, ?, ?, ?, ?)");
			
			ps.setString(1, barEvent.getContract().m_localSymbol);
			
			if (barEvent.getDateTime() != null)
				ps.setTimestamp(2, new java.sql.Timestamp(barEvent.getDateTime().getTime()));
			else 
				ps.setNull(2, Types.TIMESTAMP);
			
			if (barEvent.getBarType() != null)
				ps.setString(3, barEvent.getBarType().name());
			else
				ps.setNull(3, Types.VARCHAR);
			
			ps.setDouble(4, barEvent.getOpenPrice());
			
			ps.setDouble(5, barEvent.getHighPrice());
			
			ps.setDouble(6, barEvent.getLowPrice());
			
			ps.setDouble(7, barEvent.getClosePrice());
			
			ps.setInt(8, barEvent.getVolume());
			
			ps.setInt(9, barEvent.getCount());
			
			ps.setDouble(10, barEvent.getVwap());
			
			ps.setBoolean(11, barEvent.isHasGaps());
			
			ps.execute();
		} catch (SQLException e) {
			log.error("Could not record " + barEvent.toString(), e);
		}
	}
}
