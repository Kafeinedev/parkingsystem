package com.parkit.parkingsystem;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.parkit.parkingsystem.dao.ReductionDAO;

@ExtendWith(MockitoExtension.class)
class ReductionDAOTest {

	private static ReductionDAO reductionDAO;

	@Mock
	private static DataBaseConfig mockDBConfig;
	@Mock
	private static Connection mockConnection;
	@Mock
	private static PreparedStatement mockPS;
	@Mock
	private static ResultSet mockRS;

	@BeforeEach
	private void setUpPerTest() {
		reductionDAO = new ReductionDAO();
		reductionDAO.dataBaseConfig = mockDBConfig;
	}

	@Test
	public void isRecurringUserReturnProperValue() {
		try {
			when(mockDBConfig.getConnection()).thenReturn(mockConnection);
			when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockPS);
			when(mockPS.executeQuery()).thenReturn(mockRS);
			when(mockRS.next()).thenReturn(true);
			when(mockRS.getBoolean(1)).thenReturn(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean ret = reductionDAO.isRecurring("nawak");
		assertThat(ret).isEqualTo(true);
	}

	@Test
	public void isRecurringUserCallOtherClassesProperly() {
		try {
			when(mockDBConfig.getConnection()).thenReturn(mockConnection);
			when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockPS);
			when(mockPS.executeQuery()).thenReturn(mockRS);
			when(mockRS.next()).thenReturn(true);
			when(mockRS.getBoolean(1)).thenReturn(true);

			reductionDAO.isRecurring("nawak");

			verify(mockDBConfig, times(1)).getConnection();
			verify(mockConnection, times(1)).prepareStatement(any(String.class));
			verify(mockPS, times(1)).setString(1, "nawak");
			verify(mockPS, times(1)).executeQuery();
			verify(mockRS, times(1)).next();
			verify(mockRS, times(1)).getBoolean(1);
			verify(mockDBConfig, times(1)).closeResultSet(mockRS);
			verify(mockDBConfig, times(1)).closePreparedStatement(mockPS);
			verify(mockDBConfig, times(1)).closeConnection(mockConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void isRecurringUserReturnFalseIfError() {
		try {
			when(mockDBConfig.getConnection()).thenThrow(new MockitoException("Unit test exception"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean ret = reductionDAO.isRecurring("blabla");
		assertThat(ret).isEqualTo(false);
	}
}
