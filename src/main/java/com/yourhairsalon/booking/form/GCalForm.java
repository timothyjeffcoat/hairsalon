package com.yourhairsalon.booking.form;

import java.util.Date;

import com.yourhairsalon.booking.reference.CommType;

public class GCalForm {
	private String username;
	private boolean use_gcal_for_shop;
	private boolean allow_gcal_for_staff;
	private String gcal_id;
	private String gcal_title;
	private String id;
	public String getGcal_id() {
		return gcal_id;
	}
	public void setGcal_id(String gcal_id) {
		this.gcal_id = gcal_id;
	}
	public String getGcal_title() {
		return gcal_title;
	}
	public void setGcal_title(String gcal_title) {
		this.gcal_title = gcal_title;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public boolean isAllow_gcal_for_staff() {
		return allow_gcal_for_staff;
	}
	public void setAllow_gcal_for_staff(boolean allow_gcal_for_staff) {
		this.allow_gcal_for_staff = allow_gcal_for_staff;
	}
	public boolean isUse_gcal_for_shop() {
		return use_gcal_for_shop;
	}
	public void setUse_gcal_for_shop(boolean use_gcal_for_shop) {
		this.use_gcal_for_shop = use_gcal_for_shop;
	}
	}
