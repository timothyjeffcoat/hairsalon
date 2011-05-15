package com.yourhairsalon.booking.domain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AppointmentUpdate {
	private static final Log log = LogFactory.getLog(AppointmentUpdate.class);
	
	private String id;
	private String service1cost;
	private String servicename1;
	private String servicetype;// BaseService or CustomService
	
	private Boolean cancelled;
	private String notes;
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public void setService1cost(String service1cost) {
		this.service1cost = service1cost;
	}
	public String getService1cost() {
		return service1cost;
	}
	public void setCancelled(Boolean cancelled) {
		this.cancelled = cancelled;
	}
	public Boolean getCancelled() {
		return cancelled;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public String getNotes() {
		return notes;
	}
	public void setServicename1(String servicename1) {
		this.servicename1 = servicename1;
	}
	public String getServicename1() {
		return servicename1;
	}
	public void setServicetype(String servicetype) {
		this.servicetype = servicetype;
	}
	public String getServicetype() {
		return servicetype;
	}
}
