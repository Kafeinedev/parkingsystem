package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ReductionFactor;
import com.parkit.parkingsystem.dao.ReductionDAO;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	private ReductionDAO reductionDAO;
	// duration of stay in hour
	private double duration;

	public FareCalculatorService(ReductionDAO reductionDAO) {
		this.reductionDAO = reductionDAO;
	}

	private double calculateFactor(String vehiculeRegNumber) {
		double ret = 1.0;
		if (duration <= 0.5) {
			ret -= ReductionFactor.UNDER_30_MINS;
		}
		if (reductionDAO.isRecurrent(vehiculeRegNumber)) {
			ret -= ReductionFactor.RECURRENT_USER;
		}
		return ret >= 0.0 ? ret : 0.0;
	}

	public void calculateFare(Ticket ticket) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		duration = (ticket.getOutTime().getTime() - ticket.getInTime().getTime()) / 3600000.0;

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
			throw new IllegalArgumentException("Unknown Parking Type");
		}
	}
}