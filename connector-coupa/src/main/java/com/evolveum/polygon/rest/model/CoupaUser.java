/**
 * Copyright (c) 2017 AMI Praha a.s.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.polygon.rest.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="user")
public class CoupaUser {
	
	private String id;
	private String login;
	private String active;
	private String purchasingUser;//purchasing-user;
	private String authenticationMethod;//authentication-method;
	private String ssoIdentifier;//sso-identifier;
	private String email;
	private String firstname;
	private String lastname;
	private List<CoupaRole> roles;
	private String defLocale;//default-locale;
	private CoupaDefaultAddress defAddress;//default-address_location-code;
	
	
	public String getId() {
		return id;
	}
	@XmlElement
	public void setId(String id) {
		this.id = id;
	}
	
	public String getLogin() {
		return login;
	}
	@XmlElement
	public void setLogin(String login) {
		this.login = login;
	}
	
	public String getActive() {
		return active;
	}
	@XmlElement
	public void setActive(String active) {
		this.active = active;
	}
	
	public String getPurchasingUser() {
		return purchasingUser;
	}
	@XmlElement(name="purchasing-user")
	public void setPurchasingUser(String purchasingUser) {
		this.purchasingUser = purchasingUser;
	}
	
	public String getAuthenticationMethod() {
		return authenticationMethod;
	}
	@XmlElement(name="authentication-method")
	public void setAuthenticationMethod(String authenticationMethod) {
		this.authenticationMethod = authenticationMethod;
	}
	
	public String getSsoIdentifier() {
		return ssoIdentifier;
	}
	@XmlElement(name="sso-identifier")
	public void setSsoIdentifier(String ssoIdentifier) {
		this.ssoIdentifier = ssoIdentifier;
	}
	
	public String getEmail() {
		return email;
	}
	@XmlElement
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getFirstname() {
		return firstname;
	}
	@XmlElement
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	
	public String getLastname() {
		return lastname;
	}
	@XmlElement
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	
	public List<CoupaRole> getRoles() {
		return roles;
	}
	@XmlElementWrapper(name = "roles")
	@XmlElement(name = "role")  
	public void setRoles(List<CoupaRole> roles) {
		this.roles = roles;
	}
	
	public String getDefLocale() {
		return defLocale;
	}
	@XmlElement(name="default-locale")
	public void setDefLocale(String defLocale) {
		this.defLocale = defLocale;
	}
	
	public CoupaDefaultAddress getDefAddress() {
		return defAddress;
	}
	@XmlElement(name="default-address")
	public void setDefAddress(CoupaDefaultAddress defAddress) {
		this.defAddress = defAddress;
	}
	
	public String[] getRolesArray(){
		if(roles == null || roles.isEmpty()){
			return null;
		}
		String[] result = new String[roles.size()];
		int i = 0;
		for(CoupaRole role : roles){
			result[i] = role.getShadowArrayValue(); 
			i++;
		}
		return result;
	}
	
}
