package com.evolveum.polygon.rest.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="roles")
public class CoupaRoleList {
	
	private List<CoupaRole> roles;

	public List<CoupaRole> getRoles() {
		return roles;
	}
	@XmlElement(name = "role")  
	public void setRoles(List<CoupaRole> roles) {
		this.roles = roles;
	}

}
