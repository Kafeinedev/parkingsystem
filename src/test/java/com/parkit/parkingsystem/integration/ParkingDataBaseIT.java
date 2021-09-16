package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.constants.ReductionFactor;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.ReductionDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static ReductionDAO reductionDAO;
	private static DataBasePrepareService dataBasePrepareService;
	private static Clock timeMachine;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		reductionDAO = new ReductionDAO();
		reductionDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	@BeforeEach
	private void setUpPerTest() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		dataBasePrepareService.clearDataBaseEntries();
		timeMachine = Clock.fixed(Instant.EPOCH, TimeZone.getDefault().toZoneId());
	}

	@AfterAll
	private static void tearDown() {
	}

	@Test
	public void testParkingACar() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, reductionDAO);
		parkingService.setClock(timeMachine);
		parkingService.processIncomingVehicle();

		Ticket ticket = ticketDAO.getTicket("ABCDEF");
		assertThat(ticket.getId()).isEqualTo(1);
		assertThat(ticket.getInTime()).isEqualTo(Date.from(Instant.EPOCH));
		assertEquals(true, (ticket.getOutTime() == null));
		assertThat(ticket.getParkingSpot().getNumber()).isEqualTo(1);
		assertThat(ticket.getPrice()).isEqualTo(0.0);
		assertThat(ticket.getVehicleRegNumber()).isEqualTo("ABCDEF");

		int nextSpot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

		assertThat(nextSpot).isEqualTo(2);
		// TODO: check that a ticket is actually saved in DB and Parking table is
		// updated
		// with availability
	}

	@Test
	public void testParkingLotExit() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, reductionDAO);
		parkingService.setClock(timeMachine);
		parkingService.processIncomingVehicle();

		timeMachine = Clock.fixed(Instant.ofEpochSecond(3600), TimeZone.getDefault().toZoneId());
		parkingService.setClock(timeMachine);
		parkingService.processExitingVehicle();

		Ticket ticket = ticketDAO.getTicket("ABCDEF");
		assertThat(ticket.getId()).isEqualTo(1);
		assertThat(ticket.getInTime()).isEqualTo(Date.from(Instant.EPOCH));
		assertThat(ticket.getOutTime()).isEqualTo(Date.from(Instant.ofEpochSecond(3600)));
		assertThat(ticket.getParkingSpot().getNumber()).isEqualTo(1);
		assertThat(ticket.getPrice()).isEqualTo(Fare.CAR_RATE_PER_HOUR * 1);
		assertThat(ticket.getVehicleRegNumber()).isEqualTo("ABCDEF");

		// TODO:check that the fare generated and out time are populated correctly in
		// the database
	}

	@Test
	public void testParkingRecurrentUser() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, reductionDAO);
		parkingService.setClock(timeMachine);
		parkingService.processIncomingVehicle();
		timeMachine = Clock.fixed(Instant.ofEpochSecond(3600), TimeZone.getDefault().toZoneId());
		parkingService.setClock(timeMachine);
		parkingService.processExitingVehicle();

		timeMachine = Clock.fixed(Instant.ofEpochSecond(7200), TimeZone.getDefault().toZoneId());
		parkingService.setClock(timeMachine);
		parkingService.processIncomingVehicle();

		timeMachine = Clock.fixed(Instant.ofEpochSecond(10800), TimeZone.getDefault().toZoneId());
		parkingService.setClock(timeMachine);
		parkingService.processExitingVehicle();

		Ticket ticket = ticketDAO.getTicket("ABCDEF");
		assertThat(ticket.getId()).isEqualTo(1);
		assertThat(ticket.getInTime()).isEqualTo(Date.from(Instant.ofEpochSecond(7200)));
		assertThat(ticket.getOutTime()).isEqualTo(Date.from(Instant.ofEpochSecond(14600)));
		assertThat(ticket.getParkingSpot().getNumber()).isEqualTo(1);
		assertThat(ticket.getPrice()).isEqualTo(Fare.CAR_RATE_PER_HOUR * 1 * (1.0 - ReductionFactor.RECURRENT_USER));
		assertThat(ticket.getVehicleRegNumber()).isEqualTo("ABCDEF");
	}

}
