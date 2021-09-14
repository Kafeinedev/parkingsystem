package com.parkit.parkingsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;

public class ReductionDAO {
	public DataBaseConfig dataBaseConfig = new DataBaseConfig();
	private static final Logger logger = LogManager.getLogger("ReductionDAO");

	public boolean isRecurring(String vehicleRegNumber) {
		Connection con = null;
		try {
			con = dataBaseConfig.getConnection();
			PreparedStatement ps = con.prepareStatement(DBConstants.IS_RECURRING);
			ps.setString(1, vehicleRegNumber);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getBoolean(1);
			}
		} catch (Exception ex) {
			logger.error("Error getting recurring user", ex);
		} finally {
			dataBaseConfig.closeConnection(con);
		}
		return false;
	}
}
