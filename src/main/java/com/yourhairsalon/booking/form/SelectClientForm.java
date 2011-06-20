package com.yourhairsalon.booking.form;

import java.util.Date;

import com.yourhairsalon.booking.reference.CommType;

public class SelectClientForm {
	private String firstName;
	private String lastName;
	private String email;
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEmail() {
		return email;
	}
	public String toString(){
		String ret = "";
		ret = getFirstName() + " " + getLastName();
		return ret;
	}
}
