package com.evolveum.polygon.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="default-address")
public class CoupaDefaultAddress {
	
	private String locationCode;

	public String getLocationCode() {
		return locationCode;
	}
	@XmlElement(name="location-code")
	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}
	
	

}
