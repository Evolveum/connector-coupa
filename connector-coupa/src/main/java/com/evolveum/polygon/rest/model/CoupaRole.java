package com.evolveum.polygon.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="role")
public class CoupaRole {
	
	private String id;
	private String name;
	private String description;
	private String omnipotent;
	private String systemRole;
	
	public String getId() {
		return id;
	}
	@XmlElement(name = "id")
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	@XmlElement(name = "name")
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	@XmlElement(name = "description")
	public void setDescription(String description) {
		this.description = description;
	}
	public String getOmnipotent() {
		return omnipotent;
	}
	@XmlElement(name = "omnipotent")
	public void setOmnipotent(String omnipotent) {
		this.omnipotent = omnipotent;
	}
	public String getSystemRole() {
		return systemRole;
	}
	@XmlElement(name = "system-role")
	public void setSystemRole(String systemRole) {
		this.systemRole = systemRole;
	}
	
	
	public String getShadowArrayValue(){
		return name;
	}

}
