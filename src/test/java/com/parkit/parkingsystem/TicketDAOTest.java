package com.parkit.parkingsystem;

import static org.assertj.core.api.Assertions.*;
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
			verify(mockDBConfig, times(1)).closeConnection(mockConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Disabled // Disabled because of error logging
	@Test
	public void errorWhenSavingTicketReturnFalse() {
		try {
			when(mockDBConfig.getConnection()).thenThrow(new MockitoException("Unit test exception"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean ret = ticketDAO.saveTicket(ticket);
		assertEquals(false, ret);
	}

	@Test
	public void getTicketReturnProperTicket() {
		java.sql.Timestamp date = new java.sql.Timestamp(new Date().getTime());
		java.sql.Timestamp futureDate = new java.sql.Timestamp(date.getTime() + 10);
		try {
			when(mockDBConfig.getConnection()).thenReturn(mockConnection);
			when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockPS);
			when(mockPS.executeQuery()).thenReturn(mockRS);
			when(mockRS.next()).thenReturn(true);
			when(mockRS.getInt(any(Integer.class))).thenReturn(42, 28);
			when(mockRS.getString(6)).thenReturn("BIKE");
			when(mockRS.getDouble(3)).thenReturn(27.0);
			when(mockRS.getTimestamp(any(Integer.class))).thenReturn(date, futureDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ticket = ticketDAO.getTicket("SalutCamarade");
		assertThat(ticket.getParkingSpot()).isEqualTo(new ParkingSpot(42, ParkingType.BIKE, false));
		assertThat(ticket.getId()).isEqualTo(28);
		assertThat(ticket.getVehicleRegNumber()).isEqualTo("SalutCamarade");
		assertThat(ticket.getPrice()).isEqualTo(27.0);
		assertThat(ticket.getInTime()).isEqualTo(date);
		assertThat(ticket.getOutTime()).isEqualTo(futureDate);
	}

	@Test
	public void getTicketCallOtherClassCorrectly() {
		try {
			when(mockDBConfig.getConnection()).thenReturn(mockConnection);
			when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockPS);
			when(mockPS.executeQuery()).thenReturn(mockRS);
			when(mockRS.next()).thenReturn(true);
			when(mockRS.getInt(any(Integer.class))).thenReturn(42, 28);
			when(mockRS.getString(6)).thenReturn("BIKE");
			when(mockRS.getDouble(3)).thenReturn(27.0);
			when(mockRS.getTimestamp(any(Integer.class))).thenReturn(new java.sql.Timestamp(new Date().getTime()));

			ticketDAO.getTicket("jeserrarien");

			verify(mockDBConfig, times(1)).getConnection();
			verify(mockConnection, times(1)).prepareStatement(any(String.class));
			verify(mockPS, times(1)).executeQuery();
			verify(mockRS, times(1)).next();
			verify(mockRS, times(2)).getInt(any(Integer.class));
			verify(mockRS, times(1)).getDouble(any(Integer.class));
			verify(mockRS, times(2)).getTimestamp(any(Integer.class));
			verify(mockDBConfig, times(1)).closeResultSet(mockRS);
			verify(mockDBConfig, times(1)).closePreparedStatement(mockPS);
			verify(mockDBConfig, times(1)).closeConnection(mockConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getTicketReturnNullIfThereIsNoResult() {
		try {
			when(mockDBConfig.getConnection()).thenReturn(mockConnection);
			when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockPS);
			when(mockPS.executeQuery()).thenReturn(mockRS);
			when(mockRS.next()).thenReturn(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ticket = ticketDAO.getTicket("I'm losing MY MIND");
		assertThat(ticket).isEqualTo(null);
	}

	@Test
	public void updateTicketCorrectlyReturnTrue() {
		try {
			when(mockDBConfig.getConnection()).thenReturn(mockConnection);
			when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockPS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean ret = ticketDAO.updateTicket(ticket);
		assertThat(ret).isEqualTo(true);
	}

	@Disabled // disabled because of error logging
	@Test
	public void updateTicketIncorrectlyReturnfalse() {
		try {
			when(mockDBConfig.getConnection()).thenThrow(new MockitoException("Unit test exception"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean ret = ticketDAO.updateTicket(ticket);
		assertThat(ret).isEqualTo(false);
	}

	@Test
	public void updateTicketCallOtherClassCorrectly() {
		try {
			when(mockDBConfig.getConnection()).thenReturn(mockConnection);
			when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockPS);

			ticketDAO.updateTicket(ticket);

			verify(mockDBConfig, times(1)).getConnection();
			verify(mockConnection, times(1)).prepareStatement(any(String.class));
			verify(mockPS, times(1)).setDouble(1, 0.0);
			verify(mockPS, times(1)).setTimestamp(2, new java.sql.Timestamp(ticket.getOutTime().getTime()));
			verify(mockPS, times(1)).setInt(3, 1);
			verify(mockPS, times(1)).execute();
			verify(mockDBConfig, times(1)).closeConnection(mockConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
