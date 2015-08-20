package org.codehaus.enunciate.examples.objc.schema.vehicles;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

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