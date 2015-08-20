package org.codehaus.enunciate.examples.objc.schema.vehicles;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * @author Martin Magakian
 * http://doduck.com
 */
public class Seat {

	private Integer number;
	
	public Integer getNumber() {
		return number;
	}
	
	public void setNumber(Integer number) {
		this.number = number;
	}

}