package com.webcohesion.enunciate.examples.objc_client.schema.vehicles;

import java.util.List;

/**
 * @author Martin Magakian
 * http://doduck.com
 */
public class SeatRow {
	
	private String rowName;
	private List<Seat> seats;

	public SeatRow() {
	}
	
	public List<Seat> getSeats() {
		return seats;
	}

	public void setSeats(List<Seat> seats) {
		this.seats = seats;
	}

	public String getRowName() {
		return rowName;
	}

	public void setRowName(String rowName) {
		this.rowName = rowName;
	}
	
}