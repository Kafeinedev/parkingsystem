package com.parkit.parkingsystem;

import static org.mockito.Mockito.*;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.constants.ReductionFactor;
import com.parkit.parkingsystem.dao.ReductionDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class FareCalculatorServiceTest {

	private static FareCalculatorService fareCalculatorService;
	private Ticket ticket;

	@Mock
	private ReductionDAO mockReductionDAO;

	@BeforeEach
	private void setUpPerTest() {
		fareCalculatorService = new FareCalculatorService(mockReductionDAO);
		ticket = new Ticket();
		ticket.setVehicleRegNumber("ABCD");
		ticket.setInTime(Date.from(Instant.EPOCH));
	}

	@Test
	public void calculateFare_whenACarStayOneHour_setTheProperTicketPrice() {
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		when(mockReductionDAO.isRecurrent(any(String.class))).thenReturn(false);
		ticket.setOutTime(Date.from(Instant.ofEpochSecond(3600)));
		ticket.setParkingSpot(parkingSpot);

		fareCalculatorService.calculateFare(ticket);
		assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
	}

	@Test
	public void calculateFare_whenABikeStayOneHour_setTheProperTicketPrice() {
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
		when(mockReductionDAO.isRecurrent(any(String.class))).thenReturn(false);
		ticket.setOutTime(Date.from(Instant.ofEpochSecond(3600)));
		ticket.setParkingSpot(parkingSpot);

		fareCalculatorService.calculateFare(ticket);
		assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
	}

	@Test
	public void calculateFare_whenAnUnknownTypeIsGiven_throw() {
		ParkingSpot parkingSpot = new ParkingSpot(1, null, false);
		ticket.setOutTime(Date.from(Instant.ofEpochSecond(3600)));
		ticket.setParkingSpot(parkingSpot);

		assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	public void calculateFare_whenImpossibleTimeIsGiven_throw() {
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
		ticket.setOutTime(ticket.getInTime());
		ticket.setInTime(Date.from(Instant.ofEpochSecond(3600)));
		ticket.setParkingSpot(parkingSpot);

		assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	public void calculateFare_whenABikeStay45mins_setTheProperTicketPrice() {
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
		when(mockReductionDAO.isRecurrent(any(String.class))).thenReturn(false);
		ticket.setOutTime(Date.from(Instant.ofEpochSecond(2700)));
		ticket.setParkingSpot(parkingSpot);

		fareCalculatorService.calculateFare(ticket);
		assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFare_whenACarStay45mins_setTheProperTicketPrice() {
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		when(mockReductionDAO.isRecurrent(any(String.class))).thenReturn(false);
		ticket.setOutTime(Date.from(Instant.ofEpochSecond(2700)));
		ticket.setParkingSpot(parkingSpot);

		fareCalculatorService.calculateFare(ticket);
		assertEquals((0.75 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFare_whenACarStayOneDay_setTheProperTicketPrice() {
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		when(mockReductionDAO.isRecurrent(any(String.class))).thenReturn(false);
		ticket.setOutTime(Date.from(Instant.ofEpochSecond(3600 * 24)));
		ticket.setParkingSpot(parkingSpot);

		fareCalculatorService.calculateFare(ticket);
		assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFare_whenAReccurrentBikeStayOneHour_setTheProperTicketPrice() {
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
		when(mockReductionDAO.isRecurrent(any(String.class))).thenReturn(true);
		ticket.setOutTime(Date.from(Instant.ofEpochSecond(3600)));
		ticket.setParkingSpot(parkingSpot);

		fareCalculatorService.calculateFare(ticket);
		assertEquals((Fare.BIKE_RATE_PER_HOUR * (1.0 - ReductionFactor.RECURRENT_USER)), ticket.getPrice());
	}

	@Test
	public void calculateFare_whenACarStayLessThan30mins_setTheProperTicketPrice() {
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		when(mockReductionDAO.isRecurrent(any(String.class))).thenReturn(false);

		ticket.setOutTime(Date.from(Instant.ofEpochSecond(1799)));
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals(0.0, ticket.getPrice());
	}

}
