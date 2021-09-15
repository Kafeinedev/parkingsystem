package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ReductionFactor;
import com.parkit.parkingsystem.dao.ReductionDAO;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	private ReductionDAO reductionDAO;

	public FareCalculatorService(ReductionDAO reductionDAO) {
		this.reductionDAO = reductionDAO;
	}

	private double calculateFactor(String vehiculeRegNumber) {
		double ret = 1.0;
		if (reductionDAO.isRecurring(vehiculeRegNumber)) {
			ret -= ReductionFactor.RECURRING_USER;
		}
		return ret;
	}

	public void calculateFare(Ticket ticket) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		long inTime = ticket.getInTime().getTime();
		long outTime = ticket.getOutTime().getTime();

		// duration in hours converted from ms (1000ms in 1s, 3600s in 1h)
		double duration = (outTime - inTime) / 3600000.0;

		switch (ticket.getParkingSpot().getParkingType()) {
		case CAR: {
			ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR * calculateFactor(ticket.getVehicleRegNumber()));
			break;
		}
		case BIKE: {
			ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR * calculateFactor(ticket.getVehicleRegNumber()));
			break;
		}
		default:
			throw new IllegalArgumentException("Unkown Parking Type");
		}
	}
}