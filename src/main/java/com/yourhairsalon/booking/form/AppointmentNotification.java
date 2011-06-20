package com.yourhairsalon.booking.form;

import java.io.Serializable;
import java.util.*;

public class AppointmentNotification implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String clientname;
	private String email;
	private String description;
	private Date appointmentdate;
	private long appointmentid;
	private Date begintime;
	private Date endtime;
	private String time;
	private String fromshop;
	
	private String shop_phone;
	
	private String shop_sms_to;// phone number to send it to
	private String shop_sms_message; // body of message of text message
	
	private String shop_email_address;
	private String shop_email_subject;
	private String shop_email_message;
	private String shop_email_signature;
	
	private String google_cal_name;
	private String google_cal_username;
	private String google_cal_password;
	private String google_cal_message;
	private String google_cal_title;

	private String google_cal_staff_name;
	private String google_cal_staff_username;
	private String google_cal_staff_password;
	private String google_cal_staff_message;
	private String google_cal_staff_title;
	
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
	public long getAppointmentid() {
		return appointmentid;
	}
	public void setAppointmentid(long appointmentid) {
		this.appointmentid = appointmentid;
	}
	public String getGoogle_cal_name() {
		return google_cal_name;
	}
	public void setGoogle_cal_name(String google_cal_name) {
		this.google_cal_name = google_cal_name;
	}
	public String getGoogle_cal_username() {
		return google_cal_username;
	}
	public void setGoogle_cal_username(String google_cal_username) {
		this.google_cal_username = google_cal_username;
	}
	public String getGoogle_cal_password() {
		return google_cal_password;
	}
	public void setGoogle_cal_password(String google_cal_password) {
		this.google_cal_password = google_cal_password;
	}
	public String getGoogle_cal_message() {
		return google_cal_message;
	}
	public void setGoogle_cal_message(String google_cal_message) {
		this.google_cal_message = google_cal_message;
	}
	public String getGoogle_cal_title() {
		return google_cal_title;
	}
	public void setGoogle_cal_title(String google_cal_title) {
		this.google_cal_title = google_cal_title;
	}
	public String getGoogle_cal_staff_name() {
		return google_cal_staff_name;
	}
	public void setGoogle_cal_staff_name(String google_cal_staff_name) {
		this.google_cal_staff_name = google_cal_staff_name;
	}
	public String getGoogle_cal_staff_username() {
		return google_cal_staff_username;
	}
	public void setGoogle_cal_staff_username(String google_cal_staff_username) {
		this.google_cal_staff_username = google_cal_staff_username;
	}
	public String getGoogle_cal_staff_password() {
		return google_cal_staff_password;
	}
	public void setGoogle_cal_staff_password(String google_cal_staff_password) {
		this.google_cal_staff_password = google_cal_staff_password;
	}
	public String getGoogle_cal_staff_message() {
		return google_cal_staff_message;
	}
	public void setGoogle_cal_staff_message(String google_cal_staff_message) {
		this.google_cal_staff_message = google_cal_staff_message;
	}
	public String getGoogle_cal_staff_title() {
		return google_cal_staff_title;
	}
	public void setGoogle_cal_staff_title(String google_cal_staff_title) {
		this.google_cal_staff_title = google_cal_staff_title;
	}
	public String getShop_sms_to() {
		return shop_sms_to;
	}
	public void setShop_sms_to(String shop_sms_to) {
		this.shop_sms_to = shop_sms_to;
	}
	public String getShop_sms_message() {
		return shop_sms_message;
	}
	public void setShop_sms_message(String shop_sms_message) {
		this.shop_sms_message = shop_sms_message;
	}
	public String getShop_phone() {
		return shop_phone;
	}
	public void setShop_phone(String shop_phone) {
		this.shop_phone = shop_phone;
	}
}
