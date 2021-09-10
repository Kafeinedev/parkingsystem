package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.constants.ParkingType;
import org.mockito.Mock;
import org.mockito.exceptions.base.MockitoException;

@ExtendWith(MockitoExtension.class)
public class TicketDAOTest {

	private static TicketDAO ticketDAO;
	private static Ticket ticket;
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
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = mockDBConfig;
		ticket = new Ticket();
		ticket.setId(1);
		ticket.setParkingSpot(new ParkingSpot(35, ParkingType.CAR, false));
		ticket.setVehicleRegNumber("ABCDEF");
		ticket.setPrice(0.0);
		ticket.setInTime(new Date());
		ticket.setOutTime(new Date());
	}

	@Test
	public void savingTicketCorrectlyReturnTrue() {
		try {
			when(mockDBConfig.getConnection()).thenReturn(mockConnection);
			when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockPS);
			when(mockPS.execute()).thenReturn(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean ret = ticketDAO.saveTicket(ticket);
		assertEquals(true, ret);
	}

	@Test
	public void savingTicketCallOtherClassCorrectly() {
		try {
			when(mockDBConfig.getConnection()).thenReturn(mockConnection);
			when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockPS);
			ticketDAO.saveTicket(ticket);
			verify(mockDBConfig, times(1)).getConnection();
			verify(mockConnection, times(1)).prepareStatement(any(String.class));
			verify(mockPS, times(1)).setInt(1, 35);
			verify(mockPS, times(1)).setString(2, "ABCDEF");
			verify(mockPS, times(1)).setDouble(3, 0.0);
			verify(mockPS, times(2)).setTimestamp(any(Integer.class), any(java.sql.Timestamp.class));
			verify(mockPS, times(1)).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void errorWhenSavingTicketReturnFalse() {
		try {
			when(mockDBConfig.getConnection()).thenThrow(new MockitoException("Unit test exception"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean ret = ticketDAO.saveTicket(ticket);
		assertEquals(false, ret);
	}

}
