package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

public class TicketDAO {

	private static final Logger logger = LogManager.getLogger("TicketDAO");

	public DataBaseConfig dataBaseConfig = new DataBaseConfig();

	public boolean saveTicket(Ticket ticket) {
		Connection con = null;
		PreparedStatement ps = null;
		boolean ret = false;
		try {
			con = dataBaseConfig.getConnection();
			ps = con.prepareStatement(DBConstants.SAVE_TICKET);
			ps.setInt(1, ticket.getParkingSpot().getNumber());
			ps.setString(2, ticket.getVehicleRegNumber());
			ps.setDouble(3, ticket.getPrice());
			ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
			ps.setTimestamp(5, (ticket.getOutTime() == null) ? null : (new Timestamp(ticket.getOutTime().getTime())));
			ret = ps.execute();
		} catch (Exception ex) {
			logger.error("Error saving ticket", ex);
		} finally {
			close(con, ps, null);
		}
		return ret;
	}

	public Ticket getTicket(String vehicleRegNumber) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Ticket ticket = null;
		try {
			con = dataBaseConfig.getConnection();
			ps = con.prepareStatement(DBConstants.GET_TICKET);
			ps.setString(1, vehicleRegNumber);
			rs = ps.executeQuery();
			if (rs.next()) {
				ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)), false);
				Timestamp outTime = rs.getTimestamp(5);
				ticket = new Ticket(rs.getInt(2), parkingSpot, vehicleRegNumber, rs.getDouble(3),
						new Date(rs.getTimestamp(4).getTime()), (outTime != null ? new Date(outTime.getTime()) : null));
			}
		} catch (Exception ex) {
			logger.error("Error retrieving ticket", ex);
		} finally {
			close(con, ps, rs);
		}
		return ticket;
	}

	public boolean updateTicket(Ticket ticket) {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = dataBaseConfig.getConnection();
			ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
			ps.setDouble(1, ticket.getPrice());
			ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
			ps.setInt(3, ticket.getId());
			ps.execute();
			return true;
		} catch (Exception ex) {
			logger.error("Error updating ticket info", ex);
		} finally {
			close(con, ps, null);
		}
		return false;
	}

	private void close(Connection con, PreparedStatement ps, ResultSet rs) {
		dataBaseConfig.closeResultSet(rs);
		dataBaseConfig.closePreparedStatement(ps);
		dataBaseConfig.closeConnection(con);
	}
}
