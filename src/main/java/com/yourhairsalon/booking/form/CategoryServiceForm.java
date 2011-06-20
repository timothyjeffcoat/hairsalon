package com.yourhairsalon.booking.form;

public class CategoryServiceForm {
	private String svcid;
	private String catid;
	private String svcamounttime;
	private String servicedescription;
	private String svcprice;
	public String getSvcid() {
		return svcid;
	}
	public void setSvcid(String svcid) {
		this.svcid = svcid;
	}
	public String getCatid() {
		return catid;
	}
	public void setCatid(String catid) {
		this.catid = catid;
	}
	public String getServicedescription() {
		return servicedescription;
	}
	public void setServicedescription(String servicedescription) {
		this.servicedescription = servicedescription;
	}
	public String getSvcprice() {
		return svcprice;
	}
	public void setSvcprice(String svcprice) {
		this.svcprice = svcprice;
	}
	public String getSvcamounttime() {
		return svcamounttime;
	}
	public void setSvcamounttime(String svcamounttime) {
		this.svcamounttime = svcamounttime;
	}

}
