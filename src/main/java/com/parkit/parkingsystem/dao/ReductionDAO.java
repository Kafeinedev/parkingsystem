package com.parkit.parkingsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;

public class ReductionDAO {
	private static final Logger logger = LogManager.getLogger("ReductionDAO");

	public DataBaseConfig dataBaseConfig = new DataBaseConfig();

	public boolean isRecurrent(String vehicleRegNumber) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean ret = false;
		try {
			con = dataBaseConfig.getConnection();
			ps = con.prepareStatement(DBConstants.IS_RECURRENT);
			ps.setString(1, vehicleRegNumber);
			rs = ps.executeQuery();
			if (rs.next()) {
				ret = rs.getBoolean(1);
			}
		} catch (Exception ex) {
			logger.error("Error getting recurrent user", ex);
		} finally {
			close(con, ps, rs);
		}
		return ret;
	}

	public boolean addRecurrentUser(String vehicleRegNumber) {
		Connection con = null;
		PreparedStatement ps = null;
		boolean ret = false;
		try {
			con = dataBaseConfig.getConnection();
			ps = con.prepareStatement(DBConstants.ADD_RECURRENT);
			ps.setString(1, vehicleRegNumber);
			ps.setBoolean(2, true);
			ret = ps.execute();
		} catch (Exception ex) {
			logger.error("Error adding recurrent user", ex);
		} finally {
			close(con, ps, null);
		}
		return ret;
	}

	private void close(Connection con, PreparedStatement ps, ResultSet rs) {
		dataBaseConfig.closeResultSet(rs);
		dataBaseConfig.closePreparedStatement(ps);
		dataBaseConfig.closeConnection(con);
	}
}
