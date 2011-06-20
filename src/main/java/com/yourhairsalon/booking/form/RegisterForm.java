package com.yourhairsalon.booking.form;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class RegisterForm {
	
	private String test;
	
	private String dojox_form__NewPWBox_0;
	
	private String dojox_form__VerifyPWBox_0;
	
	private String fullName;

	private String lastname;
	
	private String firstname;

	private String description;

	private String country;

	private String company;

	private String phone;

	private String username;

	private String uid;

	private String oldPassword;

	private String userPassword;

	private String password;

	private String email;
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public int hashCode() {
		return new HashCodeBuilder()
			.append(test)
			.append(fullName)
			.append(lastname)
			.append(firstname)
			.append(description)
			.append(country)
			.append(company)
			.append(phone)
			.append(username)
			.append(uid)
			.append(email)
			.toHashCode();
	}

	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
			.append("test", test)
			.append("fullName", fullName)
			.append("lastname", lastname)
			.append("firstname", firstname)
			.append("description", description)
			.append("country", country)
			.append("company", company)
			.append("phone", phone)
			.append("username", username)
			.append("uid", uid)
			.append("email", email)
			.toString();
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUid() {
		return uid;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDojox_form__NewPWBox_0() {
		return dojox_form__NewPWBox_0;
	}

	public void setDojox_form__NewPWBox_0(String dojox_form__NewPWBox_0) {
		this.dojox_form__NewPWBox_0 = dojox_form__NewPWBox_0;
	}

	public String getDojox_form__VerifyPWBox_0() {
		return dojox_form__VerifyPWBox_0;
	}

	public void setDojox_form__VerifyPWBox_0(String dojox_form__VerifyPWBox_0) {
		this.dojox_form__VerifyPWBox_0 = dojox_form__VerifyPWBox_0;
	}

	public String getTest() {
		return test;
	}

	public void setTest(String test) {
		this.test = test;
	}

}
