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

	public boolean isRecurring(String vehicleRegNumber) {
		Connection con = null;
		boolean ret = false;
		try {
			con = dataBaseConfig.getConnection();
			PreparedStatement ps = con.prepareStatement(DBConstants.IS_RECURRING);
			ps.setString(1, vehicleRegNumber);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ret = rs.getBoolean(1);
			}
			dataBaseConfig.closeResultSet(rs);
			dataBaseConfig.closePreparedStatement(ps);
		} catch (Exception ex) {
			logger.error("Error getting recurring user", ex);
		} finally {
			dataBaseConfig.closeConnection(con);
		}
		return ret;
	}

	public boolean addRecurringUser(String string) {
		// TODO Auto-generated method stub
		return false;
	}
}
