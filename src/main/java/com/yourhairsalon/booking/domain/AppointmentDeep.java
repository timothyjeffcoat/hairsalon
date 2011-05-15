package com.yourhairsalon.booking.domain;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.validation.constraints.Size;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.yourhairsalon.booking.reference.ScheduleStatus;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class AppointmentDeep implements Comparable<AppointmentDeep>{
	private static final Log log = LogFactory.getLog(AppointmentDeep.class);
	
	private String id;
	
	private String apptid;
	
	private String start_date; //for dhtmlx scheduler the start date of the event in the format 'yyyy-mm-dd hh:mm:ss'.
	
	private String end_date; //for dhtmlx scheduler the end date of the event in the format 'yyyy-mm-dd hh:mm:ss'.
	
	private String text; //for dhtmlx scheduler the title of the event
	
	private String details; //for dhtmlx scheduler the title of the event
	
    private Long recur_parent;
    
	private String timezone = "MST";
	
	private String personallabel;
	
	private long service1id;
	
	private long service2id;
	
	private long service3id;
	
	private long service4id;
	
	private String clientphonenumber;
	
	private String clientcellphonenumber;
	
	private String clientworkphonenumber;
	
	private String service1type;
	
	private String service2type;
	
	private String service3type;
	
	private String service4type;
	
	private String servicename1;
	
	private String service1cost;
	
	private String servicename2;
	
	private String service2cost;

	private String servicename3;
	
	private String service3cost;
	
	private String servicename4;
	
	private String service4cost;
	
	private Boolean confirmed;

    private Boolean cancelled;

    private Boolean reoccurring;

    private Boolean reoccurring_email_all;

    private Integer frequency_week;

    private Integer duration_month;

    private Date reoccur_start_date;

    private String description;

    private Date appointmentDate;
    
    private String year_ApptDate;
    
    private String month_ApptDate;
 
    private String day_ApptDate;
    
    private Integer starttime;

    private Integer endtime;

    private Date checkintime;

    private Date checkouttime;

    private Date confirmeddate;

    private Date createddate;

    private Staff staff;

    private String staffid;
    private String stafflastname;
    private String stafffirstname;
    
    private Clients client;
    
    private String clientid;
    private String clientfirstname;
    private String clientlastname;
    
    private Shop shop;

    private Calendar endDateTime;
    
    private Date endDateTimeDate;
    
    private String s_endDateTime;
    
    private String fc_endDateTime;
    
    private String fc_endHour;
    
    private String fc_endMinute;
    
    private Calendar beginDateTime;
    
    private Date beginDateTimeDate;
    
    private String subject = "";

    private String s_beginDateTime;
    
    private String fc_beginDateTime;
    
    private String fc_beginHour;
    
    private String fc_beginMinute;
    
    private ScheduleStatus status;
    
    private List<BaseService> services = new ArrayList<BaseService>();

    private String notes;
    
    private String requested_image_path;

    private String requested_image_name;
    
    public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }
    
    public static AppointmentDeep fromJsonToAppointmentDeep(String json) {
        return new JSONDeserializer<AppointmentDeep>().use(null, AppointmentDeep.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<AppointmentDeep> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

    public static String toJsonArrayMobile(Collection<AppointmentDeep> collection) {
        return new JSONSerializer().include("apptid","notes","service1cost","subject","start_date","end_date","text","details","status","clientid","clientfirstname","clientlastname","staffid","stafffirstname","stafflastname","service1id","servicename1").exclude("*").serialize(collection);
    }
    
    public static Collection<AppointmentDeep> fromJsonArrayToAppointments(String json) {
        return new JSONDeserializer<List<AppointmentDeep>>().use(null, ArrayList.class).use("values", AppointmentDeep.class).deserialize(json);
    }
    
    

	public AppointmentDeep(){
		
	}
	
	/** 
	 * accept the Appointment and extracts values and assigns to this class
	 *  
	 * @param appointment
	 */
	public AppointmentDeep(Appointment appointment){
		setId(""+appointment.getId());
		setStatus(appointment.getStatus());
		try {
			if(appointment.getNotes() != null){
				setNotes(URLEncoder.encode(appointment.getNotes(), "UTF-8"));
			}
		} catch (UnsupportedEncodingException e) {
			log.debug(e);
		}
		setAppointmentDate(appointment.getAppointmentDate());
		log.debug("appointment.getAppointmentDate() : "+appointment.getAppointmentDate());
		log.debug("APPOINTMENTDEEP.getBeginDateTime(): "+appointment.getBeginDateTime());
		setRequested_image_path(appointment.getRequested_image_path());
		setRequested_image_name(appointment.getRequested_image_name());
		setBeginDateTime(appointment.getBeginDateTime());
		setBeginDateTimeDate(appointment.getBeginDateTime().getTime());
		setCancelled(appointment.getCancelled());
		setCheckintime(appointment.getCheckintime());
		setCheckouttime(appointment.getCheckouttime());
		setPersonallabel(appointment.getPersonallabel());
		setClient(appointment.getClient());
		setConfirmed(appointment.getConfirmed());
		setConfirmeddate(appointment.getConfirmeddate());
		setCreateddate(appointment.getCreateddate());
		setDescription(appointment.getDescription());
		setDuration_month(appointment.getDuration_month());
		setEndDateTime(appointment.getEndDateTime());
		setEndDateTimeDate(appointment.getEndDateTime().getTime());
		setEndtime(appointment.getEndtime());
		setFrequency_week(appointment.getFrequency_week());
		setReoccur_start_date(appointment.getReoccur_start_date());
		setReoccurring(appointment.getReoccurring());
		setReoccurring_email_all(appointment.getReoccurring_email_all());
		setRecur_parent(appointment.getRecur_parent());
		
		Set<BaseService> svc = appointment.getServices();
		Iterator<BaseService> svcIter = svc.iterator();
		List<BaseService> svclist = new ArrayList<BaseService>();
		int svccounter = 0;
		while (svcIter.hasNext()) {
			BaseService bs = svcIter.next();
			String servicetype = "base";
			if(bs instanceof CustomService){
				log.debug("This BaseService is a CustomService");
				servicetype = "custom";
			}
			log.debug("The id for the service "+svccounter+" is: "+bs.getId());
			
			if(svccounter==0){
				setServicename1(bs.getDescription());
				setSubject(getServicename1());
				setService1type(servicetype);
				setService1cost(""+bs.getCost());
				setService1id(bs.getId());
			}
			if(svccounter==1){
				setService2type(servicetype);
				setServicename2(bs.getDescription());
				setService2cost(""+bs.getCost());
				setService2id(bs.getId());
			}
			if(svccounter==2){
				setService3type(servicetype);
				setServicename3(bs.getDescription());
				setService3cost(""+bs.getCost());
				setService3id(bs.getId());
			}
			if(svccounter==3){
				setService4type(servicetype);
				setServicename4(bs.getDescription());
				setService4cost(""+bs.getCost());
				setService4id(bs.getId());
			}
			
			svclist.add(bs);
			svccounter++;
		}		
		setServices(svclist);
		
		setShop(appointment.getShop());
		setStaff(appointment.getStaff());
		setStarttime(appointment.getStarttime());
	}
	
    public void setConfirmed(Boolean confirmed) {
		this.confirmed = confirmed;
	}

	public Boolean getConfirmed() {
		return confirmed;
	}

	public void setCancelled(Boolean cancelled) {
		this.cancelled = cancelled;
	}

	public Boolean getCancelled() {
		return cancelled;
	}

	public void setReoccurring(Boolean reoccurring) {
		this.reoccurring = reoccurring;
	}

	public Boolean getReoccurring() {
		return reoccurring;
	}

	public void setReoccurring_email_all(Boolean reoccurring_email_all) {
		this.reoccurring_email_all = reoccurring_email_all;
	}

	public Boolean getReoccurring_email_all() {
		return reoccurring_email_all;
	}

	public void setFrequency_week(Integer frequency_week) {
		this.frequency_week = frequency_week;
	}

	public Integer getFrequency_week() {
		return frequency_week;
	}

	public void setDuration_month(Integer duration_month) {
		this.duration_month = duration_month;
	}

	public Integer getDuration_month() {
		return duration_month;
	}

	public void setReoccur_start_date(Date reoccur_start_date) {
		this.reoccur_start_date = reoccur_start_date;
	}

	public Date getReoccur_start_date() {
		return reoccur_start_date;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setAppointmentDate(Date appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

	public Date getAppointmentDate() {
		return appointmentDate;
	}

	public void setStarttime(Integer starttime) {
		this.starttime = starttime;
	}

	public Integer getStarttime() {
		return starttime;
	}

	public void setEndtime(Integer endtime) {
		this.endtime = endtime;
	}

	public Integer getEndtime() {
		return endtime;
	}

	public void setCheckintime(Date checkintime) {
		this.checkintime = checkintime;
	}

	public Date getCheckintime() {
		return checkintime;
	}

	public void setCheckouttime(Date checkouttime) {
		this.checkouttime = checkouttime;
	}

	public Date getCheckouttime() {
		return checkouttime;
	}

	public void setConfirmeddate(Date confirmeddate) {
		this.confirmeddate = confirmeddate;
	}

	public Date getConfirmeddate() {
		return confirmeddate;
	}

	public void setCreateddate(Date createddate) {
		this.createddate = createddate;
	}

	public Date getCreateddate() {
		return createddate;
	}

	public void setStaff(Staff staff) {
		this.staff = staff;
	}

	public Staff getStaff() {
		return staff;
	}

	public void setClient(Clients client) {
		this.client = client;
	}

	public Clients getClient() {
		return client;
	}

	public void setShop(Shop shop) {
		this.shop = shop;
	}

	public Shop getShop() {
		return shop;
	}

	public void setEndDateTime(Calendar endDateTime) {
		this.endDateTime = endDateTime;
		this.endDateTime.setTimeInMillis(endDateTime.getTimeInMillis());
		this.endDateTime.setTimeZone(TimeZone.getTimeZone(getTimezone()));
	}

	public Calendar getEndDateTime() {
		return endDateTime;
	}

	public void setBeginDateTime(Calendar beginDateTime) {
		log.debug("APPOINTMENTDEEP setBeginDateTime Calendar parameter value: "+beginDateTime);
		this.beginDateTime = beginDateTime;
		this.beginDateTime.setTimeInMillis(beginDateTime.getTimeInMillis());
		this.beginDateTime.setTimeZone(TimeZone.getTimeZone(getTimezone()));
	}

	public Calendar getBeginDateTime() {
		return beginDateTime;
	}

	public void setServices(List<BaseService> services) {
		this.services = services;
	}

	public List<BaseService> getServices() {
		return services;
	}

	public void setServicename1(String servicename1) {
		this.servicename1 = servicename1;
	}

	public String getServicename1() {
		return servicename1;
	}

	public void setService1cost(String service1cost) {
		this.service1cost = service1cost;
	}

	public String getService1cost() {
		return service1cost;
	}

	public void setServicename2(String servicename2) {
		this.servicename2 = servicename2;
	}

	public String getServicename2() {
		return servicename2;
	}

	public void setService2cost(String service2cost) {
		this.service2cost = service2cost;
	}

	public String getService2cost() {
		return service2cost;
	}

	public void setServicename3(String servicename3) {
		this.servicename3 = servicename3;
	}

	public String getServicename3() {
		return servicename3;
	}

	public void setService3cost(String service3cost) {
		this.service3cost = service3cost;
	}

	public String getService3cost() {
		return service3cost;
	}

	public void setServicename4(String servicename4) {
		this.servicename4 = servicename4;
	}

	public String getServicename4() {
		return servicename4;
	}

	public void setService4cost(String service4cost) {
		this.service4cost = service4cost;
	}

	public String getService4cost() {
		return service4cost;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setS_endDateTime(String s_endDateTime) {
		this.s_endDateTime = s_endDateTime;
	}

	public String getS_endDateTime() {
		String hr = "";
		int hh = endDateTime.getTime().getHours();
		String ampm = "AM";
		
		if (hh   > 11) { ampm = "PM";        }
		if (hh   > 12) { hh = hh - 12; }
		if (hh   == 0) { hh = 12;        }
		
		hr = ""+hh;
		String strmm = String.format("%02d", endDateTime.getTime().getMinutes());
		s_endDateTime = hr+":"+strmm+" "+ampm;

		return s_endDateTime;
	}

	public void setS_beginDateTime(String s_beginDateTime) {
		this.s_beginDateTime = s_beginDateTime;
	}

	public String getS_beginDateTime() {
		String hr = "";
		int hh = beginDateTime.getTime().getHours();
		String ampm = "AM";
		
		if (hh   > 11) { ampm = "PM";        }
		if (hh   > 12) { hh = hh - 12; }
		if (hh   == 0) { hh = 12;        }
		
		hr = ""+hh;
		String strmm = String.format("%02d", beginDateTime.getTime().getMinutes());
		s_beginDateTime = hr+":"+strmm+" "+ampm;
		
		return s_beginDateTime;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getNotes() {
		return notes;
	}

	public void setService1id(long service1id) {
		this.service1id = service1id;
	}

	public long getService1id() {
		return service1id;
	}

	public void setService2id(long service2id) {
		this.service2id = service2id;
	}

	public long getService2id() {
		return service2id;
	}

	public void setService3id(long service3id) {
		this.service3id = service3id;
	}

	public long getService3id() {
		return service3id;
	}

	public void setService4id(long service4id) {
		this.service4id = service4id;
	}

	public long getService4id() {
		return service4id;
	}

	public void setService1type(String service1type) {
		this.service1type = service1type;
	}

	public String getService1type() {
		return service1type;
	}

	public void setService2type(String service2type) {
		this.service2type = service2type;
	}

	public String getService2type() {
		return service2type;
	}

	public void setService3type(String service3type) {
		this.service3type = service3type;
	}

	public String getService3type() {
		return service3type;
	}

	public void setService4type(String service4type) {
		this.service4type = service4type;
	}

	public String getService4type() {
		return service4type;
	}

	public void setStatus(ScheduleStatus status) {
		this.status = status;
	}

	public ScheduleStatus getStatus() {
		return status;
	}
	
	public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("description: ").append(getDescription()).append(", ");
        sb.append("appointmentDate: ").append(getAppointmentDate()).append(", ");
        sb.append("begin time: ").append(getBeginDateTime()).append(", ");
        sb.append("endtime: ").append(getEndDateTime()).append(", ");
        return sb.toString();
	}

	public void setYear_ApptDate(String year_ApptDate) {
		this.year_ApptDate = year_ApptDate;
	}

	public String getYear_ApptDate() {
		SimpleDateFormat dateformatYYYY = new SimpleDateFormat("yyyy");
        StringBuilder nowYYYY = new StringBuilder( dateformatYYYY.format( appointmentDate ) );
        year_ApptDate = ""+nowYYYY;

        return year_ApptDate;
	}

	public void setMonth_ApptDate(String month_ApptDate) {
		this.month_ApptDate = month_ApptDate;
	}

	public String getMonth_ApptDate() {
		SimpleDateFormat dateformatMM = new SimpleDateFormat("MM");
        StringBuilder nowMM = new StringBuilder( dateformatMM.format( appointmentDate ) );
        month_ApptDate = ""+nowMM;
		return month_ApptDate;
	}

	public void setDay_ApptDate(String day_ApptDate) {
		this.day_ApptDate = day_ApptDate;
	}

	public String getDay_ApptDate() {
		SimpleDateFormat dateformatdd = new SimpleDateFormat("dd");
        StringBuilder nowdd = new StringBuilder( dateformatdd.format( appointmentDate ) );
        day_ApptDate = ""+nowdd;
		return day_ApptDate;
	}

	public void setFc_endDateTime(String fc_endDateTime) {
		this.fc_endDateTime = fc_endDateTime;
	}

	public String getFc_endDateTime() {
		String hr = "";
		int hh = endDateTime.getTime().getHours();
		
		hr = ""+hh;
		String strmm = String.format("%02d", endDateTime.getTime().getMinutes());
		fc_endDateTime = hr+":"+strmm;

		
		return fc_endDateTime;
	}

	public void setFc_beginDateTime(String fc_beginDateTime) {
		this.fc_beginDateTime = fc_beginDateTime;
	}

	public String getFc_beginDateTime() {
		String hr = "";
		int hh = beginDateTime.getTime().getHours();
		
		hr = ""+hh;
		String strmm = String.format("%02d", beginDateTime.getTime().getMinutes());
		fc_beginDateTime = hr+":"+strmm;

		return fc_beginDateTime;
	}

	public void setFc_endHour(String fc_endHour) {
		this.fc_endHour = fc_endHour;
	}

	public String getFc_endHour() {
		String hr = "";
		int hh = endDateTime.getTime().getHours();
		hr = ""+hh;
		
		fc_endHour = hr;

		return fc_endHour;
	}

	public void setFc_endMinute(String fc_endMinute) {
		this.fc_endMinute = fc_endMinute;
	}

	public String getFc_endMinute() {
		String strmm = String.format("%02d", endDateTime.getTime().getMinutes());
		fc_endMinute = strmm;
		return fc_endMinute;
	}

	public void setFc_beginHour(String fc_beginHour) {
		this.fc_beginHour = fc_beginHour;
	}

	public String getFc_beginHour() {
		String hr = "";
		int hh = beginDateTime.getTime().getHours();
		hr = ""+hh;
		
		fc_beginHour = hr;

		return fc_beginHour;
	}

	public void setFc_beginMinute(String fc_beginMinute) {
		this.fc_beginMinute = fc_beginMinute;
	}

	public String getFc_beginMinute() {
		String strmm = String.format("%02d", beginDateTime.getTime().getMinutes());
		fc_beginMinute = strmm;

		return fc_beginMinute;
	}

	@Override
	public int compareTo(AppointmentDeep x) {
		log.debug("ENTERED compareTo");
		int startComparison = compare(x.getAppointmentDate(), this.getAppointmentDate());
		
	    return startComparison != 0 ? startComparison
	                                : compare(x.getAppointmentDate(), this.getAppointmentDate());
	}

	private static int compare(Date a, Date b) {
		log.debug("ENTERED compare");
		return a.compareTo(b);
	}

	public String getPersonallabel() {
		return personallabel;
	}

	public void setPersonallabel(String personallabel) {
		this.personallabel = personallabel;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getClientphonenumber() {
		return clientphonenumber;
	}

	public void setClientphonenumber(String clientphonenumber) {
		this.clientphonenumber = clientphonenumber;
	}

	public String getClientcellphonenumber() {
		return clientcellphonenumber;
	}

	public void setClientcellphonenumber(String clientcellphonenumber) {
		this.clientcellphonenumber = clientcellphonenumber;
	}

	public String getClientworkphonenumber() {
		return clientworkphonenumber;
	}

	public void setClientworkphonenumber(String clientworkphonenumber) {
		this.clientworkphonenumber = clientworkphonenumber;
	}

	public Long getRecur_parent() {
		return recur_parent;
	}

	public void setRecur_parent(Long recur_parent) {
		this.recur_parent = recur_parent;
	}

	public String getRequested_image_path() {
		return requested_image_path;
	}

	public void setRequested_image_path(String requested_image_path) {
		this.requested_image_path = requested_image_path;
	}

	public String getRequested_image_name() {
		return requested_image_name;
	}

	public void setRequested_image_name(String requested_image_name) {
		this.requested_image_name = requested_image_name;
	}

	public Date getEndDateTimeDate() {
		return endDateTimeDate;
	}

	public void setEndDateTimeDate(Date endDateTimeDate) {
		this.endDateTimeDate = endDateTimeDate;
	}

	public Date getBeginDateTimeDate() {
		return beginDateTimeDate;
	}

	public void setBeginDateTimeDate(Date beginDateTimeDate) {
		this.beginDateTimeDate = beginDateTimeDate;
	}

	public String getEnd_date() {
		//yyyy-mm-dd hh:mm:ss
		Date endt = getEndDateTimeDate();
		// Format the output with leading zeros for days and month
	    SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	    start_date = date_format.format(endt);
		return start_date;
	}

	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}

	public String getStart_date() {
		//yyyy-mm-dd hh:mm:ss
		Calendar begint = getBeginDateTime();
		// Format the output with leading zeros for days and month
	    SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	    end_date = date_format.format(begint.getTime());
	    
		return end_date;
	}

	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}

	public String getText() {
		text = ""+service1id;
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getStaffid() {
		staffid = ""+staff.getId();
		return staffid;
	}

	public void setStaffid(String staffid) {
		this.staffid = staffid;
	}

	public String getClientid() {
		if(client != null && client.getId() != null){
			clientid = ""+client.getId();
		}
		return clientid;
	}

	public void setClientid(String clientid) {
		this.clientid = clientid;
	}

	public String getStafflastname() {
		stafflastname = staff.getLastName();
		return stafflastname;
	}

	public void setStafflastname(String stafflastname) {
		this.stafflastname = stafflastname;
	}

	public String getStafffirstname() {
		stafffirstname = staff.getFirstName();
		return stafffirstname;
	}

	public void setStafffirstname(String stafffirstname) {
		this.stafffirstname = stafffirstname;
	}

	public String getClientfirstname() {
		if(client != null && client.getFirstName() != null){
			clientfirstname = client.getFirstName();
		}
		return clientfirstname;
	}

	public void setClientfirstname(String clientfirstname) {
		this.clientfirstname = clientfirstname;
	}

	public String getClientlastname() {
		if(client != null && client.getLastName() != null){
			clientlastname = client.getLastName();
		}
		return clientlastname;
	}

	public void setClientlastname(String clientlastname) {
		this.clientlastname = clientlastname;
	}

	public String getApptid() {
		apptid = getId();
		return apptid;
	}

	public void setApptid(String apptid) {
		this.apptid = apptid;
	}


}
