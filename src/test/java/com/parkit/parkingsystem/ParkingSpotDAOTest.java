package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.model.ParkingSpot;

@ExtendWith(MockitoExtension.class)
class ParkingSpotDAOTest {

	private static ParkingSpotDAO parkingSpotDAO;
	private static ParkingSpot parkingSpot;

	@Mock
	private static DataBaseConfig mockDBConfig;
	@Mock
	private static Connection mockConnection;
	@Mock
	private static PreparedStatement mockPS;
	@Mock
	private static ResultSet mockRS;

	@BeforeEach
	public void setUpPerTest() {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = mockDBConfig;
	}

	@Test
	public void getNextAvailableSpotReturnPositiveInteger() {
		try {
			when(mockDBConfig.getConnection()).thenReturn(mockConnection);
			when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockPS);
			when(mockPS.executeQuery()).thenReturn(mockRS);
			when(mockRS.next()).thenReturn(true);
			when(mockRS.getInt(1)).thenReturn(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int ret = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
		assertEquals(1, ret);
	}

	// @Disabled
	@Test
	public void getNextAvailableSpotReturnMinusOneIfError() {
		try {
			when(mockDBConfig.getConnection()).thenThrow(new MockitoException("Unit test exception"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		int ret = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
		assertEquals(-1, ret);
	}

	@Test
	public void getNextAvailableSpotCallOtherClassProperly() {
		try {
			when(mockDBConfig.getConnection()).thenReturn(mockConnection);
			when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockPS);
			when(mockPS.executeQuery()).thenReturn(mockRS);
			when(mockRS.next()).thenReturn(true);
			when(mockRS.getInt(1)).thenReturn(1);

			parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

			verify(mockDBConfig, times(1)).getConnection();
			verify(mockConnection, times(1)).prepareStatement(any(String.class));
			verify(mockPS, times(1)).setString(1, ParkingType.CAR.toString());
			verify(mockPS, times(1)).executeQuery();
			verify(mockRS, times(1)).next();
			verify(mockRS, times(1)).getInt(1);
			verify(mockDBConfig, times(1)).closeResultSet(mockRS);
			verify(mockDBConfig, times(1)).closePreparedStatement(mockPS);
			verify(mockDBConfig, times(1)).closeConnection(mockConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void updateParkingReturnTrueIfProperlyUpdated() {
		try {
			when(mockDBConfig.getConnection()).thenReturn(mockConnection);
			when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockPS);
			when(mockPS.executeUpdate()).thenReturn(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean ret = parkingSpotDAO.updateParking(parkingSpot);
		assertEquals(true, ret);
	}

	// @Disabled
	@Test
	public void updateParkingReturnFalseIfNotProperlyUpdated() {
		try {
			when(mockDBConfig.getConnection()).thenThrow(new MockitoException("Unit test exception"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean ret = parkingSpotDAO.updateParking(parkingSpot);
		assertEquals(false, ret);
	}

	@Test
	public void updateParkingCallOtherClassProperly() {
		parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
		try {
			when(mockDBConfig.getConnection()).thenReturn(mockConnection);
			when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockPS);
			when(mockPS.executeUpdate()).thenReturn(1);

			parkingSpotDAO.updateParking(parkingSpot);

			verify(mockDBConfig, times(1)).getConnection();
			verify(mockConnection, times(1)).prepareStatement(any(String.class));
			verify(mockPS, times(1)).setBoolean(1, true);
			verify(mockPS, times(1)).setInt(2, 1);
			verify(mockPS, times(1)).executeUpdate();
			verify(mockDBConfig, times(1)).closePreparedStatement(mockPS);
			verify(mockDBConfig, times(1)).closeConnection(mockConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
