package com.evolveum.polygon.rest.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="users")
public class CoupaUserList {
	
	private List<CoupaUser> users;

	public List<CoupaUser> getUsers() {
		return users;
	}
	//@XmlElementWrapper(name = "users") 
	@XmlElement(name = "user")  
	public void setUsers(List<CoupaUser> users) {
		this.users = users;
	}

}
