package com.yourhairsalon.booking.form;

import java.io.Serializable;
import java.util.*;

import com.yourhairsalon.booking.domain.Appointment;

public class GcalAppointmentNotification implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Appointment appointment;
	private String clientname;
	private String email;
	private String description;
	private Date appointmentdate;
	private Date begintime;
	private Date endtime;
	private String time;
	private String fromshop;
	private String shop_email_address;
	private String shop_email_subject;
	private String shop_email_message;
	private String shop_email_signature;
	
	public void setClientname(String clientname) {
		this.clientname = clientname;
	}
	public String getClientname() {
		return clientname;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEmail() {
		return email;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getTime() {
		return time;
	}
	public void setFromshop(String fromshop) {
		this.fromshop = fromshop;
	}
	public String getFromshop() {
		return fromshop;
	}
	public void setShop_email_subject(String shop_email_subject) {
		this.shop_email_subject = shop_email_subject;
	}
	public String getShop_email_subject() {
		return shop_email_subject;
	}
	public void setShop_email_message(String shop_email_message) {
		this.shop_email_message = shop_email_message;
	}
	public String getShop_email_message() {
		return shop_email_message;
	}
	public void setShop_email_signature(String shop_email_signature) {
		this.shop_email_signature = shop_email_signature;
	}
	public String getShop_email_signature() {
		return shop_email_signature;
	}
	public void setShop_email_address(String shop_email_address) {
		this.shop_email_address = shop_email_address;
	}
	public String getShop_email_address() {
		return shop_email_address;
	}
	public Date getAppointmentdate() {
		return appointmentdate;
	}
	public void setAppointmentdate(Date appointmentdate) {
		this.appointmentdate = appointmentdate;
	}
	public Date getBegintime() {
		return begintime;
	}
	public void setBegintime(Date begintime) {
		this.begintime = begintime;
	}
	public Date getEndtime() {
		return endtime;
	}
	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}
	public Appointment getAppointment() {
		return appointment;
	}
	public void setAppointment(Appointment appointment) {
		this.appointment = appointment;
	}
}
