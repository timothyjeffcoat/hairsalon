package com.yourhairsalon.booking.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.yourhairsalon.booking.domain.AppointmentDeep;
import com.yourhairsalon.booking.reference.CommType;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class FullClientForm {
	private Long id;
	private Integer version;
	private String firstName;
	private String lastName;
	private String birthDay;
	private Date birthDate;
	private String address1;
	private String address2;
	private String citycode;
	private String statecode;
	private String zipcode;
	private String home_phone;
	private String work_phone;
	private String cell_phone;
	private String email;
	private String username;
	private String use_gcalendar;
	private String gcalendar_username;
	private String gcalendar_name;
	private boolean accepts_notifications_emails;
	private boolean accepts_receipts_emails;
	private boolean accepts_inital_emails;

	private boolean accepts_notifications_sms;
	private boolean accepts_receipts_sms;
	private boolean accepts_inital_sms;
	
	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }
    
    public static FullClientForm fromJsonToFullClientForm(String json) {
        return new JSONDeserializer<FullClientForm>().use(null, FullClientForm.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<FullClientForm> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }
    
    public static Collection<FullClientForm> fromJsonArrayToFullClientForm(String json) {
        return new JSONDeserializer<List<FullClientForm>>().use(null, ArrayList.class).use("values", FullClientForm.class).deserialize(json);
    }
	
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
	public void setAddress1(String address1) {
		this.address1 = address1;
	}
	public String getAddress1() {
		return address1;
	}
	public void setAddress2(String address2) {
		this.address2 = address2;
	}
	public String getAddress2() {
		return address2;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public String getCitycode() {
		return citycode;
	}
	public void setStatecode(String statecode) {
		this.statecode = statecode;
	}
	public String getStatecode() {
		return statecode;
	}
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
	public String getZipcode() {
		return zipcode;
	}
	public void setHome_phone(String home_phone) {
		this.home_phone = home_phone;
	}
	public String getHome_phone() {
		return home_phone;
	}
	public void setWork_phone(String work_phone) {
		this.work_phone = work_phone;
	}
	public String getWork_phone() {
		return work_phone;
	}
	public void setCell_phone(String cell_phone) {
		this.cell_phone = cell_phone;
	}
	public String getCell_phone() {
		return cell_phone;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEmail() {
		return email;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getId() {
		return id;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public Integer getVersion() {
		return version;
	}
	public void setBirthDay(String birthDay) {
		this.birthDay = birthDay;
	}
	public String getBirthDay() {
		return birthDay;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUse_gcalendar() {
		return use_gcalendar;
	}

	public void setUse_gcalendar(String use_gcalendar) {
		this.use_gcalendar = use_gcalendar;
	}

	public String getGcalendar_name() {
		return gcalendar_name;
	}

	public void setGcalendar_name(String gcalendar_name) {
		this.gcalendar_name = gcalendar_name;
	}

	public String getGcalendar_username() {
		return gcalendar_username;
	}

	public void setGcalendar_username(String gcalendar_username) {
		this.gcalendar_username = gcalendar_username;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public boolean isAccepts_notifications_emails() {
		return accepts_notifications_emails;
	}

	public void setAccepts_notifications_emails(boolean accepts_notifications_emails) {
		this.accepts_notifications_emails = accepts_notifications_emails;
	}

	public boolean isAccepts_receipts_emails() {
		return accepts_receipts_emails;
	}

	public void setAccepts_receipts_emails(boolean accepts_receipts_emails) {
		this.accepts_receipts_emails = accepts_receipts_emails;
	}

	public boolean isAccepts_inital_emails() {
		return accepts_inital_emails;
	}

	public void setAccepts_inital_emails(boolean accepts_inital_emails) {
		this.accepts_inital_emails = accepts_inital_emails;
	}

	public boolean isAccepts_notifications_sms() {
		return accepts_notifications_sms;
	}

	public void setAccepts_notifications_sms(boolean accepts_notifications_sms) {
		this.accepts_notifications_sms = accepts_notifications_sms;
	}

	public boolean isAccepts_receipts_sms() {
		return accepts_receipts_sms;
	}

	public void setAccepts_receipts_sms(boolean accepts_receipts_sms) {
		this.accepts_receipts_sms = accepts_receipts_sms;
	}

	public boolean isAccepts_inital_sms() {
		return accepts_inital_sms;
	}

	public void setAccepts_inital_sms(boolean accepts_inital_sms) {
		this.accepts_inital_sms = accepts_inital_sms;
	}

}
