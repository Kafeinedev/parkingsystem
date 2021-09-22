package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.ReductionDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

	private static ParkingService parkingService;
	// necessary to redirect System.out
	private final PrintStream standardOut = System.out;
	private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

	private Ticket ticket;
	private ParkingSpot parkingSpot;

	@Mock
	private static InputReaderUtil mockInputReaderUtil;
	@Mock
	private static ParkingSpotDAO mockParkingSpotDAO;
	@Mock
	private static TicketDAO mockTicketDAO;
	@Mock
	private static ReductionDAO mockReductionDAO;
	@Mock
	private static FareCalculatorService mockFareCalculatorService;

	@BeforeEach
	private void setUpPerTest() {
		System.setOut(new PrintStream(outputStreamCaptor));

		try {
			parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
			ticket = new Ticket(0, parkingSpot, "ABCDEF", 0.0, new Date(), null);

			parkingService = new ParkingService(mockInputReaderUtil, mockParkingSpotDAO, mockTicketDAO,
					mockReductionDAO);
			parkingService.setCalculatorService(mockFareCalculatorService);

			when(mockInputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up test");
		}
	}

	@AfterEach
	private void restoreStream() {
		System.setOut(standardOut);
	}

	@Test
	public void processIncomingVehicle_whenCommunicatingWithDB_sendTheProperData() {
		when(mockInputReaderUtil.readSelection()).thenReturn(1);
		when(mockParkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
		when(mockParkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

		parkingService.processIncomingVehicle();

		verify(mockInputReaderUtil, times(1)).readSelection();
		verify(mockParkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
		try {
			verify(mockInputReaderUtil, times(1)).readVehicleRegistrationNumber();
		} catch (Exception e) {
			e.printStackTrace();
		}
		verify(mockParkingSpotDAO, times(1)).updateParking(parkingSpot);
		verify(mockTicketDAO, times(1)).saveTicket(any(Ticket.class));
		verify(mockReductionDAO, times(1)).isRecurrent("ABCDEF");
	}

	@Test
	public void processIncomingVehicle_whenCommunicatingwithUser_sendTheProperInfo() {
		when(mockInputReaderUtil.readSelection()).thenReturn(1);
		when(mockReductionDAO.isRecurrent(any(String.class))).thenReturn(false);
		when(mockParkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

		parkingService.processIncomingVehicle();

		assertThat(outputStreamCaptor.toString()).isEqualTo("Please select vehicle type from menu\n" + "1 CAR\n"
				+ "2 BIKE\n" + "Please type the vehicle registration number and press enter key\n"
				+ "Generated Ticket and saved in DB\n" + "Please park your vehicle in spot number:1\n"
				+ "Recorded in-time for vehicle number:ABCDEF is:" + ticket.getInTime() + '\n');
	}

	@Test
	public void processIncomingVehicle_whenCommunicatingwithReccurentUser_sendTheProperInfo() {
		when(mockInputReaderUtil.readSelection()).thenReturn(1);
		when(mockReductionDAO.isRecurrent(any(String.class))).thenReturn(true);
		when(mockParkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

		parkingService.processIncomingVehicle();

		assertThat(outputStreamCaptor.toString()).isEqualTo("Please select vehicle type from menu\n" + "1 CAR\n"
				+ "2 BIKE\n" + "Please type the vehicle registration number and press enter key\n"
				+ "Welcome back! As a recurring user of our parking lot, you'll benefit from a 5% discount.\n"
				+ "Generated Ticket and saved in DB\n" + "Please park your vehicle in spot number:1\n"
				+ "Recorded in-time for vehicle number:ABCDEF is:" + ticket.getInTime() + '\n');
	}

	@Test
	public void processExitingVehicle_whenCommunicatingWithDB_sendTheProperData() {
		when(mockTicketDAO.getTicket(any(String.class))).thenReturn(ticket);
		when(mockTicketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
		when(mockReductionDAO.isRecurrent(any(String.class))).thenReturn(false);

		parkingService.processExitingVehicle();

		try {
			verify(mockInputReaderUtil, times(1)).readVehicleRegistrationNumber();
		} catch (Exception e) {
			e.printStackTrace();
		}
		verify(mockTicketDAO, times(1)).getTicket("ABCDEF");
		verify(mockParkingSpotDAO, times(1)).updateParking(parkingSpot);
		verify(mockReductionDAO, times(1)).isRecurrent("ABCDEF");
		verify(mockReductionDAO, times(1)).addRecurrentUser("ABCDEF");
	}

	@Test
	public void processExitingVehicle_whenCommunicatingwithUser_sendTheProperInfo() {
		when(mockTicketDAO.getTicket(any(String.class))).thenReturn(ticket);
		when(mockTicketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
		when(mockReductionDAO.isRecurrent(any(String.class))).thenReturn(false);

		parkingService.processExitingVehicle();

		assertThat(outputStreamCaptor.toString())
				.isEqualTo("Please type the vehicle registration number and press enter key\n"
						+ "Please pay the parking fare:" + ticket.getPrice() + '\n'
						+ "Recorded out-time for vehicle number:ABCDEF is:" + ticket.getOutTime() + '\n');
	}
}
