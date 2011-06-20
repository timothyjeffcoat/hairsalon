package com.yourhairsalon.booking.form;

import java.util.Collection;
import java.util.Date;
import com.yourhairsalon.booking.domain.*;

public class AddAppointmentForm {
	private Date selectDate;
	private Clients client;
	private BaseService service1;
	private BaseService service2;
	private BaseService service3;
	private BaseService service4;
	private String notes;
	private String hour;
	private String minute;
	private String ampm;
	
	public void setSelectDate(Date selectDate) {
		this.selectDate = selectDate;
	}

	public Date getSelectDate() {
		return selectDate;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getNotes() {
		return notes;
	}

	public void setClient(Clients client) {
		this.client = client;
	}

	public Clients getClient() {
		return client;
	}

	public void setService1(BaseService service1) {
		this.service1 = service1;
	}

	public BaseService getService1() {
		return service1;
	}

	public void setService2(BaseService service2) {
		this.service2 = service2;
	}

	public BaseService getService2() {
		return service2;
	}

	public void setService3(BaseService service3) {
		this.service3 = service3;
	}

	public BaseService getService3() {
		return service3;
	}

	public void setService4(BaseService service4) {
		this.service4 = service4;
	}

	public BaseService getService4() {
		return service4;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public String getHour() {
		return hour;
	}

	public void setMinute(String minute) {
		this.minute = minute;
	}

	public String getMinute() {
		return minute;
	}

	public void setAmpm(String ampm) {
		this.ampm = ampm;
	}

	public String getAmpm() {
		return ampm;
	}
}
