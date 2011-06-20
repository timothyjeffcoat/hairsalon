package com.yourhairsalon.booking.web;

import com.yourhairsalon.booking.domain.Appointment;
import com.yourhairsalon.booking.domain.Audit;

import hirondelle.date4j.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Provider.Service;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import javax.mail.internet.MimeUtility;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.record.formula.functions.Weekday;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import com.yourhairsalon.booking.domain.Appointment;
import com.yourhairsalon.booking.domain.AppointmentDeep;
import com.yourhairsalon.booking.domain.AppointmentUpdate;
import com.yourhairsalon.booking.domain.BaseService;
import com.yourhairsalon.booking.domain.Clients;
import com.yourhairsalon.booking.domain.Communications;
import com.yourhairsalon.booking.domain.CustomPrice;
import com.yourhairsalon.booking.domain.CustomService;
import com.yourhairsalon.booking.domain.GCal;
import com.yourhairsalon.booking.domain.Payments;
import com.yourhairsalon.booking.domain.PaymentsService;
import com.yourhairsalon.booking.domain.PaymentsType;
import com.yourhairsalon.booking.domain.ServiceGroup;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.ShopSettings;
import com.yourhairsalon.booking.domain.Staff;
import com.yourhairsalon.booking.form.AddAppointmentForm;
import com.yourhairsalon.booking.form.AppointmentNotification;
import com.yourhairsalon.booking.form.ApptSvcJson;
import com.yourhairsalon.booking.form.CategoryServiceForm;
import com.yourhairsalon.booking.form.GcalAppointmentNotification;
import com.yourhairsalon.booking.form.RegisterForm;
import com.yourhairsalon.booking.form.UserPreference;
import com.yourhairsalon.booking.reference.CommType;
import com.yourhairsalon.booking.reference.PaymentTypes;
import com.yourhairsalon.booking.reference.ScheduleStatus;

import flexjson.JSONSerializer;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

import com.yourhairsalon.booking.reference.DateRange;


import com.yourhairsalon.booking.task.GCalNotificationHelper;
import com.yourhairsalon.booking.task.GeneralNotificationHelper;
import com.yourhairsalon.booking.task.GCalSMSNotificationHelper;
import com.yourhairsalon.booking.task.GeneralSMSNotificationHelper;





//ical4j
//import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.data.*;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.*;
//ical4j end

@RequestMapping("/myschedule")
@Controller
public class MyScheduleController {
	private static final Log log = LogFactory.getLog(MyScheduleController.class);

	private Shop shop;
	
	private Audit audit = new Audit();
	
    @Autowired
    private UserPreference preferences;
	
	@Autowired
    private transient GeneralNotificationHelper generalNotificationHelper;

	@Autowired
    private transient GCalNotificationHelper gcalNotificationHelper;

	@Autowired
    private transient GeneralSMSNotificationHelper generalSMSNotificationHelper;

	@Autowired
    private transient GCalSMSNotificationHelper gcalSMSNotificationHelper;
	
	private void setShop(Shop shop){
    	getPreferences().setShop(shop);
    	this.shop = shop;
    }
	private Shop getShop(){
		log.debug("ENTERED getShop MyScheduleController");
		Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	if(obj instanceof InetOrgPerson) {
			log.debug("The principal object is InetOrgPerson");
			String parentshop = ((InetOrgPerson) obj).getO();
			String username = ((UserDetails) obj).getUsername();
			log.info("parentshop of LOGGED IN USER: " + parentshop);
			log.info("username of LOGGED IN USER: " + username);
			if(getPreferences().getShop() == null){
				TypedQuery<Shop> shop = null;
				if(parentshop == null){
					parentshop = username;
				}
				shop = Shop.findShopsByShopuuid(parentshop);
				log.debug("the returned results for looking for "+ parentshop + " is a size of: " + shop.getResultList().size());
				if(shop.getResultList().size() > 0){
					getPreferences().setShop(shop.getResultList().get(0));
					
				}
			}			
		}else if (obj instanceof UserDetails) {
			log.debug("The principal object is UserDetails");
			String username = ((UserDetails) obj).getUsername();
			log.debug("username: "+username);
			log.debug("preferences: "+getPreferences());
			if (getPreferences().getShop()==null) {
				log.info("LOGGED IN USER: " + username);
				TypedQuery<Shop> shop = Shop.findShopsByShopuuid(username);
				log.debug("the returned results for looking for "+ username + " is a size of: " + shop.getResultList().size());
				if(shop.getResultList().size() > 0){
					getPreferences().setShop(shop.getResultList().get(0));
					
				}else{
					// see if you can find the username in staff and then get shop from that
					log.debug("TRYING TO ASSOCIATE A SHOP WITH THE LOGGED IN PERSON");
					TypedQuery<Staff> staf = Staff.findStaffsByUsername(username);
					if(staf.getResultList().size() > 0){
						Staff staff = staf.getResultList().get(0);
						Shop sh = staff.getShop();
						shop = Shop.findShopsByShopuuid(sh.getShopuuid());
						log.debug("shopuuid: "+ sh.getShopuuid());
						if(shop.getResultList().size() > 0){
							getPreferences().setShop(sh);
							
						}
						
					}
				}
			}
		}			
    	log.debug("EXITING getShop MyScheduleController");
		return getPreferences().getShop();
	}

	
	/**
	 * using this to centralize the saving , merging, or persisting of all occurrences
	 * of Appointment in this class
	 * 
	 * @param appt
	 * @return
	 */
	private Appointment saveAppointment(Appointment appt){
		log.debug("ENTERED saveAppointment VERSION 3");

		appt.persist();
		Appointment returned_appt = appt.merge();
		
		log.debug("returned_appt.getStatus() : "+returned_appt.getStatus());
		/* NOW TEST TO SEE IF THIS APPOINTMENT NEEDS TO BE SENT TO GOOGLE CALENDAR TO BE CRUD
		THIS DEPENDS ON GCAL SETTINGS FOR THIS SHOP AND STAFF
		*/
    	String google_cal = "";
    	String google_username = "";
    	boolean sendtoque = false;
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);

			boolean use_gcal = shopsetting.isUse_gcalendar_for_shop();
			boolean allow_staff_gcal = shopsetting.isAllow_staff_gcalendar();
			if(use_gcal || allow_staff_gcal){
				if(use_gcal){
					// SHOP RELATED
					TypedQuery<GCal> gals = GCal.findGCalsByShopEqualsAndStaffIsNull(getShop());
					if(gals.getResultList().size() > 0){
						GCal gal = gals.getResultList().get(0);
						google_cal = gal.getCalendarnameid();
						google_username = gal.getUsername();
						String google_password = gal.getPassword();
						// SEND TO QUE ONLY IF CREDENTIALS ARE NON NULL AND NON EMPTY
						if(google_cal != null && !google_cal.equalsIgnoreCase("") && google_username != null && !google_username.equalsIgnoreCase("") && google_password != null && !google_password.equalsIgnoreCase("") ){
							log.debug("ALLOW GCAL FOR SHOP IS SET TO TRUE");
							sendtoque = true;
							if(sendtoque){
								AppointmentNotification gcala = new AppointmentNotification();
								gcala.setDescription("TESTING OF GCAL");
								if(returned_appt.getId() != null){
									gcala.setAppointmentid(returned_appt.getId());
									gcala.setGoogle_cal_name(google_cal);
									gcala.setGoogle_cal_username(google_username);
									gcala.setGoogle_cal_password(google_password);
									Set<BaseService> firstservice= appt.getServices();
									int asz = firstservice.size();
									if(asz == 1){
										log.debug("the number of services is one");
										BaseService svc = firstservice.iterator().next();

										String client_email = "";
										String client_cell = "";
										String client_home = "";
										String client_work = "";
										List<Communications> comm = Communications.findCommunicationsesByPerson(appt.getClient()).getResultList();
										int sz = comm.size();
										log.debug("the size of number of comms is: "+ sz);
										for(int x=0;x<sz;x++){
											log.debug("looping to update comms");
											Communications com = comm.get(x);
											String comval = com.getCommunication_value();
											
											if(com.getCommunication_type().name().equalsIgnoreCase(CommType.EMAIL.name())){
												log.debug("email");
												client_email  = com.getCommunication_value();
											}
											if(com.getCommunication_type().name().equalsIgnoreCase(CommType.CELL_PHONE.name())){
												log.debug("cell");
												client_cell  = com.getCommunication_value();
											}
											if(com.getCommunication_type().name().equalsIgnoreCase(CommType.HOME_PHONE.name())){
												log.debug("cell");
												client_home  = com.getCommunication_value();
											}
											if(com.getCommunication_type().name().equalsIgnoreCase(CommType.WORK_PHONE.name())){
												log.debug("cell");
												client_work  = com.getCommunication_value();
											}
										}
										
										
										gcala.setGoogle_cal_message("service: "+svc.getDescription()+" email: " + client_email +" cell: " + client_cell + " home: " + client_home + " work: " + client_work);
									}
									
								}
								log.debug("ATTEMPTING TO SEND APPT TO QUE");
								if(returned_appt.getStatus() != ScheduleStatus.CHECKED_OUT){
									try {
										getGcalNotificationHelper().sendMessage(gcala);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}	
								}
								
								log.debug("AFTER ATTEMPTING TO SEND APPT TO QUE");		
							}
							
						}
						
					}
				}
				if(allow_staff_gcal){
					log.debug("MyScheduleController INSIDE ALLOW STAFF TO GCAL");
					// STAFF RELATED
					log.debug("MyScheduleController ALLOW STAFF TO GCAL: " + appt.getStaff().isUse_gcalendar());
					if(appt.getStaff().isUse_gcalendar()){
						TypedQuery<GCal> orginal_gals = GCal.findGCalsByShopEqualsAndStaffEquals(getShop(), appt.getStaff());
						log.debug("MyScheduleController orginal_gals.getResultList().size(): " + orginal_gals.getResultList().size());
						GCal orig_gal = null; 
						if(orginal_gals.getResultList().size() > 0){
							orig_gal = orginal_gals.getResultList().get(0);
							String cal_name = orig_gal.getCalendarnameid();
							String cal_username = orig_gal.getUsername();
							String google_password = orig_gal.getPassword();
							if(cal_name != null && !cal_name.equalsIgnoreCase("") && cal_username != null && !cal_username.equalsIgnoreCase("") && google_password != null && !google_password.equalsIgnoreCase("") ){
								log.debug("MyScheduleController ALLOW GCAL FOR STAFF IS SET TO TRUE");
								sendtoque = true;	
								if(sendtoque){
									AppointmentNotification gcala = new AppointmentNotification();
									gcala.setDescription("TESTING OF GCAL");
									if(returned_appt.getId() != null){
										gcala.setAppointmentid(returned_appt.getId());
										gcala.setGoogle_cal_staff_name(cal_name);
										gcala.setGoogle_cal_staff_username(cal_username);
										gcala.setGoogle_cal_staff_password(google_password);
										Set<BaseService> firstservice= appt.getServices();
										int asz = firstservice.size();
										if(asz == 1){
											log.debug("MyScheduleController the number of services is one");
											BaseService svc = firstservice.iterator().next();

											String client_email = "";
											String client_cell = "";
											String client_home = "";
											String client_work = "";
											List<Communications> comm = Communications.findCommunicationsesByPerson(appt.getClient()).getResultList();
											int sz = comm.size();
											log.debug("MyScheduleController the size of number of comms is: "+ sz);
											for(int x=0;x<sz;x++){
												log.debug("looping to update comms");
												Communications com = comm.get(x);
												String comval = com.getCommunication_value();
												
												if(com.getCommunication_type().name().equalsIgnoreCase(CommType.EMAIL.name())){
													log.debug("email");
													client_email  = com.getCommunication_value();
												}
												if(com.getCommunication_type().name().equalsIgnoreCase(CommType.CELL_PHONE.name())){
													log.debug("cell");
													client_cell  = com.getCommunication_value();
												}
												if(com.getCommunication_type().name().equalsIgnoreCase(CommType.HOME_PHONE.name())){
													log.debug("cell");
													client_home  = com.getCommunication_value();
												}
												if(com.getCommunication_type().name().equalsIgnoreCase(CommType.WORK_PHONE.name())){
													log.debug("cell");
													client_work  = com.getCommunication_value();
												}
											}
											
											
											gcala.setGoogle_cal_message("service: "+svc.getDescription()+" email: " + client_email +" cell: " + client_cell + " home: " + client_home + " work: " + client_work);
										}										
									}
									log.debug("MyScheduleController ATTEMPTING TO SEND APPT TO QUE");
									if(returned_appt.getStatus() != ScheduleStatus.CHECKED_OUT){
										try {
											getGcalNotificationHelper().sendMessage(gcala);
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									log.debug("MyScheduleController AFTER ATTEMPTING TO SEND APPT TO QUE");		
								}
								
							}
						}
					}
				}
			}
		}catch(Exception e){
			log.error(e);
		}
		log.debug("EXITING saveAppointment");
		return returned_appt;
	}
	
    private DateList getDates(java.util.Date startRange, java.util.Date endRange, java.util.Date eventStart, Recur recur) { 
        log.debug("Entered private DateList getDates");
        log.debug("startRange: "+startRange.toString());
        log.debug("endRange: "+endRange.toString());
        log.debug("eventStart: "+eventStart.toString());
        log.debug("recur: "+recur.toString());
        net.fortuna.ical4j.model.Date start = new net.fortuna.ical4j.model.Date(startRange); 
        net.fortuna.ical4j.model.Date end = new net.fortuna.ical4j.model.Date(endRange); 
        net.fortuna.ical4j.model.Date seed = new net.fortuna.ical4j.model.Date(eventStart); 
        log.debug("start: "+start.toString());
        log.debug("end: "+end.toString());
        log.debug("seed: "+seed.toString());
        DateList dates = recur.getDates(seed, start, end, Value.DATE);
        log.debug("size of dates : "+dates.size());
        for (int i=0; i<dates.size(); i++) { 
            log.info("date_" + i + " = " + dates.get(i).toString()); 
        }
        log.debug("Exiting private DateList getDates");
        return dates;
    }
	
    /**
     * daily: every day for the date range and number of days in between known as interval
     * if interval=1 then it is every day,
     * if interval=2 then it is every other day, etc...
     */
    private DateList getDaily(java.util.Date eventStart,java.util.Date rangeStart,java.util.Date rangeEnd,int interval){
    	log.debug("BEGIN getDaily");
        net.fortuna.ical4j.model.Date end = new net.fortuna.ical4j.model.Date(rangeEnd);
        Recur recur = new Recur(Recur.DAILY, end);
        recur.setInterval(interval);
        DateList dailylist = getDates(rangeStart, rangeEnd, eventStart, recur);
        log.debug("END getDaily");
    	return dailylist;
    }

    /**
     * daily: every day for the occurrence indicated and number of days in between known as interval
     * if interval=1 then it is every day,
     * if interval=2 then it is every other day, etc...
     */
    private DateList getDailyByOcurrence(java.util.Date eventStart,java.util.Date rangeStart,int occur,int interval){
    	log.debug("BEGIN getDailyByOcurrence");
    	java.util.Date rangeEnd = (java.util.Date)eventStart.clone();
    	rangeEnd.setDate(rangeEnd.getDate()+occur);
    	
        Recur recur = new Recur(Recur.DAILY, occur);
        recur.setInterval(interval);
        DateList dailylist = getDates(rangeStart, rangeEnd, eventStart, recur);
        log.debug("END getDailyByOcurrence");
    	return dailylist;
    }
        
    /** 
     * daily: week day ( Monday thru Friday ) for given time range and interval
     * or
     * weekly , every x number of weeks(s) on list of days
     * 
     * set interval = 1 for every week 
     * set interval = 2 for every other week, etc...
     * 
     */
    private DateList getDailyWeekDays(java.util.Date eventStart,java.util.Date rangeStart,java.util.Date rangeEnd,int interval){
        // week day
    	log.debug("BEGIN getDailyWeekDays");
        net.fortuna.ical4j.model.Date end = new net.fortuna.ical4j.model.Date(rangeEnd);
        Recur recur = new Recur(Recur.DAILY, end);
        recur.setInterval(interval);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.TU);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.TH);
        recur.getDayList().add(WeekDay.FR);
        DateList weekdaylist = getDates(rangeStart, rangeEnd, eventStart, recur);
        log.debug("END getDailyWeekDays");
    	return weekdaylist;
    }

    /** 
     * daily: week day ( Monday thru Friday ) for given occurrence(s) and interval
     * or
     * weekly , every x number of weeks(s) on list of days
     * 
     * set interval = 1 for every week 
     * set interval = 2 for every other week, etc...
     * 
     */
    private DateList getDailyWeekDaysByOccurrence(java.util.Date eventStart,java.util.Date rangeStart,int occur,int interval){
    	log.debug("BEGIN getDailyWeekDaysByOccurrence");
    	
    	java.util.Date rangeEnd = (java.util.Date)eventStart.clone();
    	log.debug("1 rangeEnd: "+rangeEnd.toString());
    	rangeEnd.setDate(rangeEnd.getDate()+(7*occur));// occurrence times 7 days for weekdays
    	log.debug("2 rangeEnd: "+rangeEnd.toString());
    	
        net.fortuna.ical4j.model.Date end = new net.fortuna.ical4j.model.Date(rangeEnd);
        
        log.debug("end: "+end.toString());
        
        Recur recur = new Recur(Recur.DAILY, 7*occur);
        recur.setInterval(interval);
        recur.getDayList().add(WeekDay.MO);
        recur.getDayList().add(WeekDay.TU);
        recur.getDayList().add(WeekDay.WE);
        recur.getDayList().add(WeekDay.TH);
        recur.getDayList().add(WeekDay.FR);
        DateList weekdaylist = getDates(rangeStart, rangeEnd, eventStart, recur);
        log.debug("END getDailyWeekDaysByOccurrence");
    	return weekdaylist;
    }
    
    
    /**
     * weekly: for the date range and number of days in between known as interval
     * if interval=1 then it is every day,
     * if interval=2 then it is every other day, etc...
     */
    private DateList getWeeklyWithDaily(java.util.Date eventStart,java.util.Date rangeStart,java.util.Date rangeEnd,int interval,Collection<WeekDay> listofweekdays){
    	log.debug("BEGIN getWeeklyWithDaily");
        net.fortuna.ical4j.model.Date end = new net.fortuna.ical4j.model.Date(rangeEnd);
        Recur recur = new Recur(Recur.WEEKLY, end);
        recur.setInterval(interval);
        
        
		Iterator itr = listofweekdays.iterator();
		while (itr.hasNext()) {
			WeekDay weekday = (WeekDay) itr.next();
			recur.getDayList().add(weekday);
		}        
        
        DateList dailylist = getDates(rangeStart, rangeEnd, eventStart, recur);
        log.debug("END getWeeklyWithDaily");
    	return dailylist;
    }
    
    /**
     * weekly: for the occurrence and number of days in between known as interval
     * if interval=1 then it is every day,
     * if interval=2 then it is every other day, etc...
     */
    private DateList getWeeklyWithDailyByOccurrence(java.util.Date eventStart,java.util.Date rangeStart,int occur,int interval,Collection<WeekDay> listofweekdays){
    	log.debug("BEGIN getWeeklyWithDailyByOccurrence");
    	java.util.Date rangeEnd = (java.util.Date)eventStart.clone();
    	log.debug("1 rangeEnd: "+rangeEnd.toString());
    	if(interval==1){
    		rangeEnd.setDate(rangeEnd.getDate()+(7*occur));
    	}else{
    		rangeEnd.setDate(rangeEnd.getDate()+(7*(interval*occur-1)));
    	}
    	log.debug("2 rangeEnd: "+rangeEnd.toString());
    	
        net.fortuna.ical4j.model.Date end = new net.fortuna.ical4j.model.Date(rangeEnd);
        log.debug("end: "+end.toString());
        log.debug("occur: "+occur);
        Recur recur = new Recur(Recur.WEEKLY, occur);
        recur.setInterval(interval);
        
        
		Iterator itr = listofweekdays.iterator();
		while (itr.hasNext()) {
			WeekDay weekday = (WeekDay) itr.next();
			log.debug("adding day of week to list: "+weekday.toString());
			recur.getDayList().add(weekday);
		}        
        
        DateList dailylist = getDates(rangeStart, rangeEnd, eventStart, recur);
        log.debug("END getWeeklyWithDailyByOccurrence size: "+dailylist.size());
    	return dailylist;
    }
    
    /**
     *  monthly
     * 
     * @param eventStart
     * @param rangeStart
     * @param rangeEnd
     * @param interval
     * @return
     */
    private DateList getMonthlyDays(java.util.Date eventStart,java.util.Date rangeStart,java.util.Date rangeEnd,int interval){
    	log.debug("BEGIN getMonthlyDays");
        net.fortuna.ical4j.model.Date end = new net.fortuna.ical4j.model.Date(rangeEnd);
        Recur recur = new Recur(Recur.MONTHLY, end);
        recur.setInterval(interval);

        DateList monthlist = getDates(rangeStart, rangeEnd, eventStart, recur);
        log.debug("END getMonthlyDays");
    	return monthlist;
    }

    /**
     *  monthly, by occurrence
     * 
     * @param eventStart
     * @param rangeStart
     * @param rangeEnd
     * @param interval
     * @return
     */
    private DateList getMonthlyDaysByOccurrence(java.util.Date eventStart,java.util.Date rangeStart,int occur,int interval){
    	log.debug("BEGIN getMonthlyDays");
    	java.util.Date rangeEnd = (java.util.Date)eventStart.clone();
    	rangeEnd.setMonth(rangeEnd.getMonth()+occur);
    	
        net.fortuna.ical4j.model.Date end = new net.fortuna.ical4j.model.Date(rangeEnd);
        Recur recur = new Recur(Recur.MONTHLY, occur);
        recur.setInterval(interval);

        DateList monthlist = getDates(rangeStart, rangeEnd, eventStart, recur);
        log.debug("END getMonthlyDays");
    	return monthlist;
    }
    
    /**
     *  monthly, on the xth of every x numbere (interval) of months
     * 
     * @param eventStart
     * @param rangeStart
     * @param rangeEnd
     * @param interval
     * @return
     */
    private DateList getMonthlyDaysXthOfEveryYMonths(java.util.Date eventStart,java.util.Date rangeStart, java.util.Date rangeEnd, int xthday, int interval){
    	log.debug("BEGIN getMonthlyDaysXthOfEveryYMonths");
    	
    	net.fortuna.ical4j.model.Date end = new net.fortuna.ical4j.model.Date(rangeEnd);
    	
        // FREQ=MONTHLY;INTERVAL=1;BYMONTHDAY=2
        Recur recur = new Recur(Recur.MONTHLY, end);
        recur.setInterval(interval);
        recur.getMonthDayList().add(new Integer(xthday));
        
        DateList monthlist = getDates(rangeStart, rangeEnd, eventStart, recur);
        log.debug("END getMonthlyDaysXthOfEveryYMonths");
    	return monthlist;
    }
    
    /**
     *  monthly, on the xth of every x numbere (interval) of months
     * 
     * @param eventStart
     * @param rangeStart
     * @param rangeEnd
     * @param interval
     * @return
     */
    private DateList getMonthlyDaysXthOfEveryYMonthsByOccurrence(java.util.Date eventStart,java.util.Date rangeStart, int occur, int xthday, int interval){
    	log.debug("BEGIN getMonthlyDaysXthOfEveryYMonthsByOccurrence");
    	java.util.Date rangeEnd = (java.util.Date)eventStart.clone();
    	rangeEnd.setMonth(rangeEnd.getMonth()+occur);
    	
    	net.fortuna.ical4j.model.Date end = new net.fortuna.ical4j.model.Date(rangeEnd);
    	
        // FREQ=MONTHLY;INTERVAL=1;BYMONTHDAY=2
        Recur recur = new Recur(Recur.MONTHLY, occur);
        recur.setInterval(interval);
        recur.getMonthDayList().add(new Integer(xthday));
        
        DateList monthlist = getDates(rangeStart, rangeEnd, eventStart, recur);
        log.debug("END getMonthlyDaysXthOfEveryYMonthsByOccurrence");
    	return monthlist;
    }
    
    /**
     *  monthly , the yth of every xsomething of every z month(s) by date range
     *  
     *  WeekDay something == similar to WeekDay.MO
     *  
     * @param eventStart
     * @param rangeStart
     * @param rangeEnd
     * @param xthday
     * @param something : WeekDay.MO, etc..
     * @param interval
     * @return
     */
    private DateList getMonthlyDaysXthOfSomething(java.util.Date eventStart,java.util.Date rangeStart, java.util.Date rangeEnd, int xthday , WeekDay something, int interval){
    	log.debug("BEGIN getMonthlyDaysXthOfSomething");
    	
    	net.fortuna.ical4j.model.Date end = new net.fortuna.ical4j.model.Date(rangeEnd);
    	
        // FREQ=MONTHLY;INTERVAL=2;COUNT=4;BYDAY=2MO
        Recur recur = new Recur(Recur.MONTHLY, end);
        recur.setInterval(interval);
        recur.getDayList().add(new WeekDay(something, xthday));
        
        DateList monthlist = getDates(rangeStart, rangeEnd, eventStart, recur);
        log.debug("END getMonthlyDaysXthOfSomething");
    	return monthlist;
    }

    /**
     *  monthly , the yth of every xsomething of every z month(s) by occurrence
     *  
     * @param eventStart
     * @param rangeStart
     * @param occur
     * @param xthday
     * @param something
     * @param interval
     * @return
     */
    private DateList getMonthlyDaysXthOfSomethingByOccurrence(java.util.Date eventStart,java.util.Date rangeStart, int occur, int xthday, WeekDay something, int interval){
    	log.debug("BEGIN getMonthlyDaysXthOfSomethingByOccurrence");
    	java.util.Date rangeEnd = (java.util.Date)eventStart.clone();
    	log.debug("1 rangeEnd: "+rangeEnd.toString());
    	rangeEnd.setMonth(rangeEnd.getMonth()+occur);
    	log.debug("2 rangeEnd: "+rangeEnd.toString());
    	net.fortuna.ical4j.model.Date end = new net.fortuna.ical4j.model.Date(rangeEnd);
    	log.debug("end: "+end.toString());
    	
        // FREQ=MONTHLY;INTERVAL=2;COUNT=4;BYDAY=2MO
        Recur recur = new Recur(Recur.MONTHLY, occur);
        recur.setInterval(interval);
        recur.getDayList().add(new WeekDay(something, xthday));
        
        DateList monthlist = getDates(rangeStart, rangeEnd, eventStart, recur);
        log.debug("END getMonthlyDaysXthOfSomethingByOccurrence");
    	return monthlist;
    }
    
    
    
    @RequestMapping(value="/testdates", method=RequestMethod.GET)    
	public String recurring(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 2);
        cal.set(Calendar.MONTH, 7);//remember that months are zero based
        cal.set(Calendar.YEAR, 2012);
        java.util.Date eventStart = cal.getTime();
        
        cal.set(Calendar.DAY_OF_MONTH, 2);
        java.util.Date rangeStart = cal.getTime();
        
        cal.set(Calendar.MONTH, 1);
        cal.set(Calendar.YEAR, 2013);
        java.util.Date rangeEnd = cal.getTime();
        
        //TEST: First thursday each month starting august 2, 2012 ends february 2, 2013. However in this case since occurrence is 3 then it only goes 3 times
        DateList list = getMonthlyDaysXthOfSomethingByOccurrence(eventStart,rangeStart,3,1,WeekDay.TH,1);
        
        //TEST: First thursday each month starting august 2, 2012 ends january 2013
        list = getMonthlyDaysXthOfSomething(eventStart,rangeStart,rangeEnd,1,WeekDay.TH,1);
        
        //TEST: starting august 2, 2012 ends 4th occurrence, every month of 2nd interval 1
        list = getMonthlyDaysXthOfEveryYMonthsByOccurrence(eventStart,rangeStart,4,2,1);

        //TEST: starting august 2, 2012 ends january 2013, every month of 2nd interval 1        
        list = getMonthlyDaysXthOfEveryYMonths(eventStart,rangeStart,rangeEnd,2,1);

        //TEST: starting august 2, 2012 ends 4th occurrence, every month interval 1        
        list = getMonthlyDaysByOccurrence(eventStart,rangeStart,4,1);
        
        //TEST: starting august 2, 2012 ends january 2013 interval 1        
        list = getMonthlyDays(eventStart,rangeStart,rangeEnd,1);

        // TEST:  1 occurences, interval of 1, with list of weekday to use , in this case thursday
        ArrayList<WeekDay> weekdays = new ArrayList<WeekDay>();
        weekdays.add(WeekDay.TH);
        list = getWeeklyWithDailyByOccurrence(eventStart,rangeStart,10,1,weekdays);

        // TEST:  weekly: rangeEnd 2013-01-31 , interval of 1, with list of weekday to use , in this case thursday        
        list = getWeeklyWithDaily(eventStart,rangeStart,rangeEnd,1,weekdays);

        // TEST:  daily: two occurences should be two weeks every week day                
        list = getDailyWeekDaysByOccurrence(eventStart,rangeStart,2,1);

        // TEST:  daily: two occurences should be two weeks every week day                
        list = getDailyWeekDays(eventStart,rangeStart,rangeEnd,1);

        // TEST:  daily: 5 occurences or 5 days
        list = getDailyByOcurrence(eventStart,rangeStart,5,1);
        
        // TEST:  daily: rangeEnd
        list = getDaily(eventStart,rangeStart,rangeEnd,1);
        
        return "forward:/";
	}
    /**
     * Grab the client's cell phone number
     * 
     * @param client
     * @return
     */
    private String getCellPhoneFromClient( Clients client){
    	log.debug("Entered getCellPhoneFromClient");
    	String phonenumber = "";
    	
		Set<Communications> comm = client.getCommunication();
		int sz = comm.size();
		
		log.debug("the size of number of comms is: "+ sz);
		
		for (Iterator it=comm.iterator(); it.hasNext(); ) {
			Communications element = (Communications)it.next();
			String comval = element.getCommunication_value();
			if(comval!=null){
				if(element.getCommunication_type().name().equalsIgnoreCase(CommType.CELL_PHONE.name())){
					log.debug("client's cell phone: " + comval);
					phonenumber = comval;
					break;
				}
			}
		}
		log.debug("Exiting getCellPhoneFromClient");
    	return phonenumber;
    }
	/**
	 * 
	 * EMAIL SENT WHEN APPOINTMENT IS CREATED OR UPDATED
	 *  
	 * @param regform
	 * @return
	 */
	private boolean sendMessage(Appointment appt){
		log.debug("ENTERED sendMessage");
		try{
			TypedQuery<ShopSettings> inital_shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings inital_shopsetting = inital_shopsettings.getResultList().get(0);
			String initial_subject = inital_shopsetting.getIemail_subject();
			String initial_message = inital_shopsetting.getIemail_message();
			String initial_email_address = inital_shopsetting.getIemail_address();
			String initial_signature  = inital_shopsetting.getIemail_signature();
			
			String shop_phonenumber = "";
			
			if(initial_message == null || initial_message.equalsIgnoreCase("")){
				Properties properties = new Properties();
				
				initial_message ="<p>	Dear ${clientfullname},</p><p>	Your appointment for a ${apptservicename}</p><p>	is on ${apptdate} @ ${appttime}</p><p>	Please give 24 hours notice if you need to change or cancel this appointment.</p><p>	Sincerely,</p><p>	${stafffullname}</p><p>	p.s. Thank you for referring your friends and family.&nbsp; We appreciate your help!</p>";
				initial_subject="Your appointment has been scheduled";
				initial_signature="Thank you for scheduling your appointent for a ${apptservicename} on ${apptdate} @ ${appttime}";
				String receipt_email="<p>	Dear ${clientfullname},</p><p>	Here is your receipt for</p><p>	your ${apptservicename}</p><p>	${apptdate} @ ${appttime}</p><p>	${apptserviceprice}</p><p>	I appreciate your business!</p><p>	${stafffullname}</p>";
				String receipt_subject="Appointment receipt";
				String receipt_signature="p.s. I would love to work for your friends and family too. Thank you for telling them about me!";
				
				String notification_email="<p>	Dear ${clientfullname},</p><p>	This is a reminder that your appointment for ${apptservicename}</p><p>	Is on date ${apptdate} time ${appttime}</p><p>	&nbsp;</p><p>	Sincerely,</p><p>	${stafffullname}</p>";
				String notification_subject="Upcoming appointment notification";
				String notification_signature="p.s. thank you for being on time.";
				try {
				    properties.load(this.getClass().getClassLoader().getResourceAsStream("emailtemplates.properties"));
				    
				    initial_message = properties.getProperty("initial");
				    initial_signature = properties.getProperty("initial_signature");
				    initial_subject = properties.getProperty("initial_subject");
	
				    receipt_email = properties.getProperty("receipt");
				    receipt_signature = properties.getProperty("receipt_signature");
				    receipt_subject = properties.getProperty("receipt_subject");
				    
				    notification_email = properties.getProperty("notification");
				    notification_signature = properties.getProperty("notification_signature");
				    notification_subject = properties.getProperty("notification_subject");
				} catch (IOException e) {
					log.error(e);
				}			
				
				ShopSettings shopSettings = new ShopSettings();
				//notification
				if(inital_shopsetting.getEmail_message()==null ||inital_shopsetting.getEmail_message().equalsIgnoreCase("")){
					shopSettings.setEmail_subject(notification_subject);
					shopSettings.setEmail_message(notification_email);
					shopSettings.setEmail_signature(notification_signature);
				}
				// initial 
				shopSettings.setIemail_subject(initial_subject);
				shopSettings.setIemail_message(initial_message);
				shopSettings.setIemail_signature(initial_signature);
				shopSettings.setIemail_address(initial_email_address);
				// receipt
				if(inital_shopsetting.getRemail_message()==null ||inital_shopsetting.getRemail_message().equalsIgnoreCase("")){ 
					shopSettings.setRemail_subject(receipt_subject);
					shopSettings.setRemail_message(receipt_email);
					shopSettings.setRemail_signature(receipt_signature);
					shopSettings.setRemail_address(initial_email_address);
				}
				shopSettings.merge();
			}
			//DO TAG REPLACEMENTS VIA STRING SEARCH AND REPLACE FOR THE FOLLOWING TAGS
			log.debug("initial_message: "+initial_message);
			String client_firstname = "";
			String client_lastname = "";
			String client_fullname = "";
			String staff_firstname = "";
			String staff_lastname = "";
			String staff_fullname = "";
			
			boolean do_sms= false;
			
			if(appt.getClient() != null){
				client_firstname = appt.getClient().getFirstName();
				client_lastname = appt.getClient().getLastName();
				
				do_sms= appt.getClient().getAccepts_sms_initial();
				
				client_fullname = client_firstname+" "+client_lastname;
				initial_message = replaceClientInfo(initial_message,
						client_firstname, client_lastname, client_fullname);

				initial_signature = replaceClientInfo(initial_signature,
						client_firstname, client_lastname, client_fullname);
			
			}
			if(appt.getStaff() != null){
				staff_firstname = appt.getStaff().getFirstName();
				staff_lastname = appt.getStaff().getLastName();
				staff_fullname = staff_firstname+" "+staff_lastname;
				initial_message = replaceStaffInfo(staff_fullname,
						initial_message, staff_firstname, staff_lastname);

				initial_signature = replaceStaffInfo(staff_fullname,
						initial_signature, staff_firstname, staff_lastname);
			
			}
			if(appt.getServices() != null){
				Set<BaseService> setsvcs = appt.getServices();
				for (Iterator it=setsvcs.iterator(); it.hasNext(); ) {
					BaseService bservice = (BaseService)it.next();
					String servicename = bservice.getDescription();
					String serviceprice = ""+bservice.getCost();
					
					double money = bservice.getCost();
					NumberFormat formatter = NumberFormat.getCurrencyInstance();
					String moneyString = Matcher.quoteReplacement(formatter.format(money));
					log.debug("moneyString: " + moneyString);
					initial_message = replaceServiceInfo(initial_message,
							servicename, moneyString);
					
					initial_signature = replaceServiceInfo(initial_signature,
							servicename, moneyString);
							
				}				
				
				
			}
			
			/*
			 * 
				${clientfullname}
				
				${clientfirstname}
				
				${clientlastname}
				
				${stafffullname}
				
				${stafffirstname}
				
				${stafflastname}
				
				${apptdate}
				
				${appttime}
				
				${apptservicename}
				
				${apptserviceprice}
			 */
			String initial_from_email = inital_shopsetting.getIemail_address();
			
			AppointmentNotification an = new AppointmentNotification();
			an.setAppointmentdate(appt.getAppointmentDate());
			an.setAppointmentid(appt.getId());
			if(appt.getClient() == null) return false;
			
			String clientname = appt.getClient().getFirstName() + " " + appt.getClient().getLastName();
			an.setClientname(clientname);
			
			String cell_phone = getCellPhoneFromClient(appt.getClient());
			log.debug("cell phone of client: "+cell_phone);
			an.setShop_sms_to(cell_phone);
			
			if(initial_subject != null && !initial_subject.equalsIgnoreCase("")){
				an.setDescription(initial_subject);
			}else{
				an.setDescription("Notification of appointment creation or update ");
			}
			String client_email = "";
			String client_cellphone = "";
			List<Communications> comm = Communications.findCommunicationsesByPerson(appt.getClient()).getResultList();
			int sz = comm.size();
			log.debug("the size of number of comms is: "+ sz);
			for(int x=0;x<sz;x++){
				log.debug("looping to update comms");
				Communications com = comm.get(x);
				String comval = com.getCommunication_value();
				
				if(com.getCommunication_type().name().equalsIgnoreCase(CommType.EMAIL.name())){
					log.debug("email");
					client_email  = com.getCommunication_value();
				}
				if(com.getCommunication_type().name().equalsIgnoreCase(CommType.CELL_PHONE.name())){
					client_cellphone  = com.getCommunication_value();
					log.debug("client_cellphone " + client_cellphone);
				}
			}
			
			if(client_email != null && !client_email.equalsIgnoreCase("")){
				an.setEmail(client_email);
				String shopname = getShop().getShop_name();
				 
				TypedQuery<ShopSettings> settings = ShopSettings.findShopSettingsesByShop(getShop());
				String email_shop = "registration@scheduleem.com";
				if(settings.getResultList().get(0).getEmail_address() != null){
					email_shop = settings.getResultList().get(0).getEmail_address();
				}
				an.setFromshop(shopname);
				if(initial_from_email != null && !initial_from_email.equalsIgnoreCase("")){
					an.setShop_email_address(initial_from_email);
				}else{
					an.setShop_email_address("registration@scheduleem.com");
				}
				
				// ********************************************
				String today = "";
				String shoptimezone = "";
				try{
					TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
					ShopSettings shopsetting = shopsettings.getResultList().get(0);
					shoptimezone = shopsetting.getTimezone();
					shop_phonenumber = shopsetting.getStore_phone();
					if(shoptimezone == null){
						shoptimezone = "MST";
						shopsetting.setTimezone(shoptimezone);
						shopsetting.merge();
					}

				}catch(Exception e){
					log.error(e);
				}
				TimeZone tz = TimeZone.getTimeZone(shoptimezone);
				Date dateValue = appt.getAppointmentDate();
				Calendar calValue = Calendar.getInstance(tz);
				calValue.setTime(dateValue);
				
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
				String dateString = df.format(calValue.getTime());
				today = dateString;
				
				String appointmentbegintime = "";
				SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm a");
				appointmentbegintime = dateformatter.format(appt.getBeginDateTime().getTime());
				
				// ********************************************
				System.setProperty("file.encoding", "UTF-8");
				
				String email_message = "An appointment has been made for you "+today+" at "+appointmentbegintime+".  Kindly give at least 24 hours notice of cancellation or reschedule.  Thank you for sending in your friends and family! I appreciate your business!";
				
				initial_message = replaceAppointmentInfo(initial_message,
						today, appointmentbegintime);
				
				
				log.debug("initial_message right before assigning to email: "+ initial_message);
				email_message = initial_message;//.replaceAll("[^\\x20-\\x7e]", " ");
				
//				email_message = MimeUtility.encodeText(email_message);
				
				an.setShop_email_message(email_message);
				if(initial_signature != null && !initial_signature.equalsIgnoreCase("")){
					initial_signature = replaceAppointmentInfo(
							initial_signature, today, appointmentbegintime);

					an.setShop_email_signature(initial_signature);
				}else{
					an.setShop_email_signature("Sincerely, "+shopname);
				}
				if(initial_subject != null && !initial_subject.equalsIgnoreCase("")){
					an.setShop_email_subject(initial_subject);
				}else{
					an.setShop_email_subject("Notification of appointment creation or update at "+shopname);
				}
				log.debug("appt.getStatus(): " + appt.getStatus());
				if(appt.getStatus() != ScheduleStatus.CHECKED_OUT){
					getGeneralNotificationHelper().sendMessage(an);

				}
				
			}else{
				log.debug("AN EMAIL MESSAGE CANNOT BE DELIVERED TO "+clientname+" BECAUSE THEY HAVE NO EMAIL ADDRESS!!");
			}
			
			
			if(client_cellphone != null && !client_cellphone.equalsIgnoreCase("")){
				an.setEmail(client_email);
				String shopname = getShop().getShop_name();
				 
				TypedQuery<ShopSettings> settings = ShopSettings.findShopSettingsesByShop(getShop());
				String email_shop = "registration@scheduleem.com";
				if(settings.getResultList().get(0).getEmail_address() != null){
					email_shop = settings.getResultList().get(0).getEmail_address();
				}
				an.setFromshop(shopname);
				if(initial_from_email != null && !initial_from_email.equalsIgnoreCase("")){
					an.setShop_email_address(initial_from_email);
				}else{
					an.setShop_email_address("registration@scheduleem.com");
				}
				
				// ********************************************
				String today = "";
				String shoptimezone = "";
				try{
					TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
					ShopSettings shopsetting = shopsettings.getResultList().get(0);
					shoptimezone = shopsetting.getTimezone();
					shop_phonenumber = shopsetting.getStore_phone();
					if(shoptimezone == null){
						shoptimezone = "MST";
						shopsetting.setTimezone(shoptimezone);
						shopsetting.merge();
					}

				}catch(Exception e){
					log.error(e);
				}
				TimeZone tz = TimeZone.getTimeZone(shoptimezone);
				Date dateValue = appt.getAppointmentDate();
				Calendar calValue = Calendar.getInstance(tz);
				calValue.setTime(dateValue);
				
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
				String dateString = df.format(calValue.getTime());
				today = dateString;
				
				String appointmentbegintime = "";
				SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm a");
				appointmentbegintime = dateformatter.format(appt.getBeginDateTime().getTime());
				
				// ********************************************
				System.setProperty("file.encoding", "UTF-8");
				
				String email_message = "An appointment has been made for you "+today+" at "+appointmentbegintime+".  Kindly give at least 24 hours notice of cancellation or reschedule.  Thank you for sending in your friends and family! I appreciate your business!";
				
				initial_message = replaceAppointmentInfo(initial_message,
						today, appointmentbegintime);
				
				
				log.debug("initial_message right before assigning to email: "+ initial_message);
				email_message = initial_message;//.replaceAll("[^\\x20-\\x7e]", " ");
				
//				email_message = MimeUtility.encodeText(email_message);
				
				an.setShop_email_message(email_message);
				if(initial_signature != null && !initial_signature.equalsIgnoreCase("")){
					initial_signature = replaceAppointmentInfo(
							initial_signature, today, appointmentbegintime);

					an.setShop_email_signature(initial_signature);
				}else{
					an.setShop_email_signature("Sincerely, "+shopname);
				}
				if(initial_subject != null && !initial_subject.equalsIgnoreCase("")){
					an.setShop_email_subject(initial_subject);
				}else{
					an.setShop_email_subject("Notification of appointment creation or update at "+shopname);
				}
				log.debug("appt.getStatus(): " + appt.getStatus());
				if(appt.getStatus() != ScheduleStatus.CHECKED_OUT){
					log.debug("about to text a notification for appointment creation");
					log.debug("appointment notification object: "+an.toString());
					log.debug("sms to: "+ an.getShop_sms_to());
					an.setBegintime(appt.getBeginDateTime().getTime());
					log.debug("next 1");
					String notificication_receipt = "notification";
					log.debug("next 2");
					log.debug("shop: " + shop);
					log.debug("next 31");
					Long number_of_sms_available = getShop().getNumber_sms_purchased();
					log.debug("next 4");
					if(number_of_sms_available != null && number_of_sms_available > 0L){
						log.debug("before sending to sendSMSTextMessage shop_name: "+getShop().getShop_name());
						sendSMSTextMessage(notificication_receipt, appt, shop_phonenumber, staff_fullname,do_sms, an);
						log.debug("after sending to sendSMSTextMessage");
					}else{
						saveAuditMessage("Text messaging is not available for appointment for "+ today+" time: "+ appointmentbegintime+" client: "+appt.getClient().getFirstName()+" "+appt.getClient().getLastName()+" by staff " +staff_fullname,null,"TEXT MESSAGE");						
					}
					
				}
				
			}else{
				log.debug("AN sms MESSAGE CANNOT BE DELIVERED TO "+clientname+" BECAUSE THEY HAVE NO cell phone number!!");
			}			
		}catch(Exception e){
			log.error(e.getMessage());
			return false;
		}
		log.debug("EXITING sendMessage");
		return true;
	}
	private void sendSMSTextMessage(String notificication_receipt, Appointment appt, String shop_phonenumber,
			String staff_fullname, boolean do_sms, AppointmentNotification an) {
		log.debug("Entered sendSMSTextMessage");
		log.debug("notificication_receipt: "+notificication_receipt);
		log.debug("appt: "+appt);
		log.debug("shop_phonenumber: "+shop_phonenumber);
		log.debug("staff_fullname: "+staff_fullname);
		log.debug("do_sms: "+do_sms);
		log.debug("an: "+an);
		String initial_sms_message = "";
		String receipt_sms_message = "";
		
		if(do_sms == true){
			log.debug("inside of do_sms");
			String shop_sms_message = "";
			
			an.setShop_sms_message(shop_sms_message);
			log.debug("inside of do_sms 2");
			
			String text_message = "";
			
			String shoptimezone = "";
			ShopSettings shopsetting = null;
			try{
				log.debug("inside of do_sms 3");
				TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
				log.debug("inside of do_sms 4");
				shopsetting = shopsettings.getResultList().get(0);
				log.debug("inside of do_sms 5");
				shoptimezone = shopsetting.getTimezone();
				if(shoptimezone == null){
					shoptimezone = "MST";
					shopsetting.setTimezone(shoptimezone);
					shopsetting.merge();
				}

			}catch(Exception e){
				log.error("Error trying to set the timezone from shop settings");
				log.error(e);
			}
			log.debug("inside of do_sms 6");
			TimeZone tz = TimeZone.getTimeZone(shoptimezone);
			log.debug("inside of do_sms 7");
			Date dateValue = appt.getAppointmentDate();
			log.debug("inside of do_sms 8");
			Calendar calValue = Calendar.getInstance(tz);
			log.debug("inside of do_sms 9");
			calValue.setTime(dateValue);
			log.debug("inside of do_sms 10");
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
			log.debug("inside of do_sms 11");
			String dateString = df.format(calValue.getTime());
			log.debug("inside of do_sms 12");
			String today = dateString;
			log.debug("inside of do_sms 13");
			
			String appointmentbegintime = "";
			SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm a");
			log.debug("inside of do_sms 15");
			appointmentbegintime = dateformatter.format(appt.getBeginDateTime().getTime());
			log.debug("inside of do_sms 16");
			// get sms from the Settings for sms messaging.
			if(shopsetting != null){
				log.debug("if(shopsetting != null){");
				initial_sms_message = shopsetting.getIsms_message();
				log.debug("initial_sms_message "+initial_sms_message);
				receipt_sms_message = shopsetting.getRsms_message();
				log.debug("receipt_sms_message "+receipt_sms_message);
				if(receipt_sms_message == null || receipt_sms_message.equalsIgnoreCase("") || initial_sms_message == null || initial_sms_message.equalsIgnoreCase("")){
					log.debug("grabbing values from properties");
					Properties properties = new Properties();
					try {
					    properties.load(this.getClass().getClassLoader().getResourceAsStream("smstemplate.properties"));
					    
					    if(initial_sms_message == null || initial_sms_message.equalsIgnoreCase("")){
					    	initial_sms_message = properties.getProperty("isms_message");
					    	shopsetting.setIsms_message(initial_sms_message);
					    }
					    if(receipt_sms_message == null || receipt_sms_message.equalsIgnoreCase("")){
					    	receipt_sms_message = properties.getProperty("rsms_message");
					    	shopsetting.setRsms_message(receipt_sms_message);
					    }
					    
					    shopsetting.merge();
					} catch (IOException e) {
						log.error("EXCEPTION trying to load the initial sms message from smstemplate.properties");
						log.error(e);
					}			
					
				}				
			}
		    
		    // client
			String client_firstname = appt.getClient().getFirstName();
			String client_lastname = appt.getClient().getLastName();
			String client_fullname = client_firstname+" "+client_lastname;
			
		    initial_sms_message = replaceClientInfo(
					initial_sms_message, client_firstname,
					client_lastname, client_fullname);
		    log.debug("initial_sms_message: "+initial_sms_message);
		    receipt_sms_message  = replaceClientInfo(
		    		receipt_sms_message, client_firstname,
					client_lastname, client_fullname);
		    log.debug("receipt_sms_message: "+receipt_sms_message);
		    // staff
			String staff_firstname = appt.getStaff().getFirstName();
			String staff_lastname = appt.getStaff().getLastName();
			staff_fullname = staff_firstname+" "+staff_lastname;
		    
		    initial_sms_message = replaceStaffInfo(staff_fullname,
					initial_sms_message, staff_firstname,
					staff_lastname);
		    
		    receipt_sms_message = replaceStaffInfo(staff_fullname,
		    		receipt_sms_message, staff_firstname,
					staff_lastname);

		    // service detail
			if(appt.getServices() != null){
				Set<BaseService> setsvcs = appt.getServices();
				for (Iterator it=setsvcs.iterator(); it.hasNext(); ) {
					BaseService bservice = (BaseService)it.next();
					String servicename = bservice.getDescription();
					String serviceprice = ""+bservice.getCost();
					
					double money = bservice.getCost();
					NumberFormat formatter = NumberFormat.getCurrencyInstance();
					String moneyString = Matcher.quoteReplacement(formatter.format(money));
					log.debug("moneyString: " + moneyString);
				    initial_sms_message = replaceServiceInfo(
							initial_sms_message, servicename,
							moneyString);
							
				    receipt_sms_message = replaceServiceInfo(
				    		receipt_sms_message, servicename,
							moneyString);

				}				
				
				
			}
			
			// shop namee and shop phone
			String shopphone = shopsetting.getStore_phone();
			String shopname = getShop().getShop_name();
		    
			initial_sms_message = replaceShopInfo(
					initial_sms_message, shopphone, shopname);
		    
			receipt_sms_message = replaceShopInfo(
					receipt_sms_message, shopphone, shopname);

			// appointment date and time
			
			// ********************************************
			today = "";
			shoptimezone = "";
			try{
				shoptimezone = shopsetting.getTimezone();
				shop_phonenumber = shopsetting.getStore_phone();
				if(shoptimezone == null){
					shoptimezone = "MST";
					shopsetting.setTimezone(shoptimezone);
					shopsetting.merge();
				}

			}catch(Exception e){
				log.error(e);
			}
			tz = TimeZone.getTimeZone(shoptimezone);
			dateValue = appt.getAppointmentDate();
			calValue = Calendar.getInstance(tz);
			calValue.setTime(dateValue);
			
			df = new SimpleDateFormat("MM/dd/yyyy");
			dateString = df.format(calValue.getTime());
			today = dateString;
			
			appointmentbegintime = "";
			dateformatter = new SimpleDateFormat("hh:mm a");
			appointmentbegintime = dateformatter.format(appt.getBeginDateTime().getTime());
			
			// ********************************************
			System.setProperty("file.encoding", "UTF-8");
			
			initial_sms_message = replaceAppointmentInfo(
					initial_sms_message, today,
					appointmentbegintime);
									
			receipt_sms_message = replaceAppointmentInfo(
					receipt_sms_message, today,
					appointmentbegintime);
			// determine if appointment creation message or receipt message
			if(notificication_receipt.equalsIgnoreCase("notification")){
				text_message = initial_sms_message;
			}else{
				text_message = receipt_sms_message;
			}

			log.error("trace 9");
			an.setShop_sms_message(text_message);
			
			log.debug("sending sms message");
			getGeneralSMSNotificationHelper().sendMessage(an);
			log.debug("sms message sent");
		}
		log.debug("Exiting sendSMSTextMessage");
	}
	private String replaceAppointmentInfo(String initial_sms_message,
			String today, String appointmentbegintime) {
		initial_sms_message = initial_sms_message.replaceAll(Pattern.quote("${apptdate}"), today.toLowerCase());
		initial_sms_message = initial_sms_message.replaceAll(Pattern.quote("${appttime}"), appointmentbegintime.toLowerCase());
		return initial_sms_message;
	}
	private String replaceShopInfo(String initial_sms_message,
			String shopphone, String shopname) {
		initial_sms_message = initial_sms_message.replaceAll(Pattern.quote("${shopname}"), shopname);
		initial_sms_message = initial_sms_message.replaceAll(Pattern.quote("${shopphone}"), shopphone);
		return initial_sms_message;
	}
	private String replaceServiceInfo(String initial_sms_message,
			String servicename, String moneyString) {
		initial_sms_message = initial_sms_message.replaceAll(Pattern.quote("${apptservicename}"), servicename);
		initial_sms_message = initial_sms_message.replaceAll(Pattern.quote("${apptserviceprice}"), moneyString);
		return initial_sms_message;
	}
	private String replaceStaffInfo(String staff_fullname,
			String initial_sms_message, String staff_firstname,
			String staff_lastname) {
		initial_sms_message = initial_sms_message.replaceAll(Pattern.quote("${stafffullname}"), staff_fullname);
		initial_sms_message = initial_sms_message.replaceAll(Pattern.quote("${stafffirstname}"), staff_firstname);
		initial_sms_message = initial_sms_message.replaceAll(Pattern.quote("${stafflastname}"), staff_lastname);
		return initial_sms_message;
	}
	private String replaceClientInfo(String initial_sms_message,
			String client_firstname, String client_lastname,
			String client_fullname) {
		log.debug("Entered replaceClientInfo");
		log.debug("client_firstname: " + client_firstname);
		log.debug("client_lastname: " + client_lastname);
		log.debug("client_fullname: " + client_fullname);
		log.debug("initial_sms_message: " + initial_sms_message);
		initial_sms_message = initial_sms_message.replaceAll(Pattern.quote("${clientfullname}"), client_fullname);
		initial_sms_message = initial_sms_message.replaceAll(Pattern.quote("${clientfirstname}"), client_firstname);
		initial_sms_message = initial_sms_message.replaceAll(Pattern.quote("${clientlastname}"), client_lastname);
		log.debug("Exiting replaceClientInfo");
		return initial_sms_message;
	}
	
	/**
	 * when appointment is checkout send this message
	 * @param appt
	 * @return
	 */
	private boolean sendReceiptMessage(Appointment appt){
		try{
			log.debug("ENTERED sendReceiptMessage");
			TypedQuery<ShopSettings> receipt_shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings receipt_shopsetting = receipt_shopsettings.getResultList().get(0);
			log.debug("receipt_shopsetting: " + receipt_shopsetting);
			String receipt_subject = receipt_shopsetting.getRemail_subject();
			String receipt_message = receipt_shopsetting.getRemail_message();
			String receipt_from_email = receipt_shopsetting.getRemail_address();
			String receipt_signature = receipt_shopsetting.getRemail_signature();
			String email_address = receipt_shopsetting.getEmail_address();
			
			log.debug("receipt_message: " + receipt_message);
			if(receipt_message == null || receipt_message.equalsIgnoreCase("")){
				Properties properties = new Properties();
				String initial_message ="<p>	Dear ${clientfullname},</p><p>	Your appointment for a ${apptservicename}</p><p>	is on ${apptdate} @ ${appttime}</p><p>	Please give 24 hours notice if you need to change or cancel this appointment.</p><p>	Sincerely,</p><p>	${stafffullname}</p><p>	p.s. Thank you for referring your friends and family.&nbsp; We appreciate your help!</p>";
				String initial_subject="Your appointment has been scheduled";
				String initial_signature="Thank you for scheduling your appointent for a ${apptservicename} on ${apptdate} @ ${appttime}";
				receipt_message="<p>	Dear ${clientfullname},</p><p>	Here is your receipt for</p><p>	your ${apptservicename}</p><p>	${apptdate} @ ${appttime}</p><p>	${apptserviceprice}</p><p>	I appreciate your business!</p><p>	${stafffullname}</p>";
				receipt_subject="Appointment receipt";
				receipt_signature="p.s. I would love to work for your friends and family too. Thank you for telling them about me!";
				String notification_email="<p>	Dear ${clientfullname},</p><p>	This is a reminder that your appointment for ${apptservicename}</p><p>	Is on date ${apptdate} time ${appttime}</p><p>	&nbsp;</p><p>	Sincerely,</p><p>	${stafffullname}</p>";
				String notification_subject="Upcoming appointment notification";
				String notification_signature="p.s. thank you for being on time.";
				try {
				    properties.load(this.getClass().getClassLoader().getResourceAsStream("emailtemplates.properties"));
				    log.debug("emailtemplates.properties loaded");
				    initial_message = properties.getProperty("initial");
				    initial_signature = properties.getProperty("initial_signature");
				    initial_subject = properties.getProperty("initial_subject");
				    log.debug("initial_message: " + initial_message);
				    receipt_message = properties.getProperty("receipt");
				    receipt_signature = properties.getProperty("receipt_signature");
				    receipt_subject = properties.getProperty("receipt_subject");
				    log.debug("receipt_message: " + receipt_message);
				    
				    notification_email = properties.getProperty("notification");
				    notification_signature = properties.getProperty("notification_signature");
				    notification_subject = properties.getProperty("notification_subject");
				    log.debug("notification_email: " + notification_email);
				} catch (IOException e) {
					log.error(e);
				}			
				
				//notification
				if(receipt_shopsetting.getEmail_message()==null){
					receipt_shopsetting.setEmail_subject(notification_subject);
					receipt_shopsetting.setEmail_message(notification_email);
					receipt_shopsetting.setEmail_signature(notification_signature);
				}
				// initial
				if(receipt_shopsetting.getIemail_message()==null){
					receipt_shopsetting.setIemail_subject(initial_subject);
					receipt_shopsetting.setIemail_message(initial_message);
					receipt_shopsetting.setIemail_signature(initial_signature);
					receipt_shopsetting.setIemail_address(email_address);
				}
				// receipt
				if(receipt_shopsetting.getRemail_message()==null){ 
					receipt_shopsetting.setRemail_subject(receipt_subject);
					receipt_shopsetting.setRemail_message(receipt_message);
					receipt_shopsetting.setRemail_signature(receipt_signature);
					receipt_shopsetting.setRemail_address(email_address);
				}
				receipt_shopsetting.merge();
			}			
//			DO TAG REPLACEMENTS VIA STRING SEARCH AND REPLACE FOR THE FOLLOWING TAGS
			log.debug("receipt_message: "+receipt_message);
			String client_firstname = "";
			String client_lastname = "";
			String client_fullname = "";
			String staff_firstname = "";
			String staff_lastname = "";
			String staff_fullname = "";
			
			boolean do_sms= false;
			
			if(appt.getClient() != null){
				
				do_sms = appt.getClient().getAccepts_sms_receipts();
				
				client_firstname = appt.getClient().getFirstName();
				client_lastname = appt.getClient().getLastName();
				
				client_fullname = client_firstname+" "+client_lastname;
				receipt_message = replaceClientInfo(receipt_message,
						client_firstname, client_lastname, client_fullname);

				receipt_signature = replaceClientInfo(receipt_signature,
						client_firstname, client_lastname, client_fullname);
			
			}
			if(appt.getStaff() != null){
				staff_firstname = appt.getStaff().getFirstName();
				staff_lastname = appt.getStaff().getLastName();
				staff_fullname = staff_firstname+" "+staff_lastname;
				receipt_message = replaceStaffInfo(staff_fullname,
						receipt_message, staff_firstname, staff_lastname);
				
				receipt_signature = replaceStaffInfo(staff_fullname,
						receipt_signature, staff_firstname, staff_lastname);
			}
			if(appt.getServices() != null){
				Set<BaseService> setsvcs = appt.getServices();
				for (Iterator it=setsvcs.iterator(); it.hasNext(); ) {
					BaseService bservice = (BaseService)it.next();
					String servicename = bservice.getDescription();
					String serviceprice = ""+bservice.getCost();
					
					double money = bservice.getCost();
					NumberFormat formatter = NumberFormat.getCurrencyInstance();
					String moneyString = Matcher.quoteReplacement(formatter.format(money));
					log.debug("moneyString: " + moneyString);
					receipt_message = replaceServiceInfo(receipt_message,
							servicename, moneyString);
					
					receipt_signature = replaceServiceInfo(receipt_signature,
							servicename, moneyString);
					
					
					
				}				
				
				
			}			
			AppointmentNotification an = new AppointmentNotification();
			an.setAppointmentdate(appt.getAppointmentDate());
			an.setAppointmentid(appt.getId());
			if(appt.getClient() == null) return false;
			
			String clientname = appt.getClient().getFirstName() + " " + appt.getClient().getLastName(); 
			an.setClientname(clientname);
			
			String cell_phone = getCellPhoneFromClient(appt.getClient());
			log.debug("cell phone of client to send the receipt to: " + cell_phone);
			an.setShop_sms_to(cell_phone);
			
			
			if(receipt_subject != null && !receipt_subject.equalsIgnoreCase("")){
				an.setDescription(receipt_subject);
			}else{
				an.setDescription("Appointment receipt ");
			}
			
			String client_email = "";
			List<Communications> comm = Communications.findCommunicationsesByPerson(appt.getClient()).getResultList();
			int sz = comm.size();
			log.debug("the size of number of comms is: "+ sz);
			for(int x=0;x<sz;x++){
				log.debug("looping to update comms");
				Communications com = comm.get(x);
				String comval = com.getCommunication_value();
				
				if(com.getCommunication_type().name().equalsIgnoreCase(CommType.EMAIL.name())){
					log.debug("email");
					client_email  = com.getCommunication_value();
				}
			}
			
			if(client_email != null && !client_email.equalsIgnoreCase("")){
				an.setEmail(client_email);
				String shopname = getShop().getShop_name();
	
				
				TypedQuery<ShopSettings> settings = ShopSettings.findShopSettingsesByShop(getShop());
				String email_shop = "registration@scheduleem.com";
				if(settings.getResultList().get(0).getEmail_address() != null){
					email_shop = settings.getResultList().get(0).getEmail_address();
				}
				an.setFromshop(shopname);
				if(receipt_from_email != null && !receipt_from_email.equalsIgnoreCase("")){
					an.setShop_email_address(receipt_from_email);
				}else{
					an.setShop_email_address("registration@scheduleem.com");
				}
				
				// ********************************************
				String today = "";
				String shoptimezone = "";
				try{
					TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
					ShopSettings shopsetting = shopsettings.getResultList().get(0);
					shoptimezone = shopsetting.getTimezone();
					if(shoptimezone == null){
						shoptimezone = "MST";
						shopsetting.setTimezone(shoptimezone);
						shopsetting.merge();
					}

				}catch(Exception e){
					log.error(e);
				}
				TimeZone tz = TimeZone.getTimeZone(shoptimezone);
				Date dateValue = appt.getAppointmentDate();
				Calendar calValue = Calendar.getInstance(tz);
				calValue.setTime(dateValue);
				
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
				String dateString = df.format(calValue.getTime());
				today = dateString;
				
				String appointmentbegintime = "";
				SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm a");
				appointmentbegintime = dateformatter.format(appt.getBeginDateTime().getTime());
				
				// ********************************************
				System.setProperty("file.encoding", "UTF-8");
				
				String email_message = "For appointment "+today+" at "+appointmentbegintime+".  This is a receipt.  Thank you for sending in your friends and family! I appreciate your business!";
				receipt_message = receipt_message.replaceAll(Pattern.quote("${apptdate}"), today);
				receipt_message = receipt_message.replaceAll(Pattern.quote("${appttime}"), appointmentbegintime.toLowerCase());
				
				email_message = receipt_message;//.replaceAll("[^\\x20-\\x7e]", " ");
				
//				email_message = MimeUtility.encodeText(email_message);
				
				an.setShop_email_message(email_message);
				
				if(receipt_signature != null && !receipt_signature.equalsIgnoreCase("")){
					an.setShop_email_signature(receipt_signature);
				}else{
					an.setShop_email_signature("Sincerely, "+shopname);
				}
				if(receipt_subject != null && !receipt_subject.equalsIgnoreCase("")){
					an.setShop_email_subject(receipt_subject);
				}else{
					an.setShop_email_subject("Receipt for appointment at "+shopname);
				}
				
				getGeneralNotificationHelper().sendMessage(an);
			}else{
				log.debug("AN RECEIPT EMAIL MESSAGE CANNOT BE DELIVERED TO "+clientname+" BECAUSE THEY HAVE NO EMAIL ADDRESS!!");
			}
		}catch(Exception e){
			log.error(e.getMessage());
			return false;
		}
		log.debug("EXITING sendReceiptMessage");
		return true;
	}	
	private void saveAuditMessage(String msg,Staff staff,String type ){
		log.debug("Entered saveAuditMessage");
		log.debug("msg : " + msg);
		log.debug("getAudit(): "+getAudit());
		this.audit = new Audit();
		if(getAudit()!=null){
			getAudit().setShop(getShop());
			getAudit().setDescription(msg);
			getAudit().setStaff(staff);
			getAudit().setTs(new Date());
			getAudit().setType(type);
			getAudit().persist();
		}
		log.debug("Exiting saveAuditMessage");
	}
	@ModelAttribute("mobilestaffs")
    public List<Staff> populateStaffsMobile() {
    	log.debug("ENTERED populateStaffs");
    	log.debug("STEP 1");
		List<Staff> staff = Staff.findStaffsByShop(getShop()).getResultList();
		Comparator comparator = new Comparator<Staff>() {
			@Override
			public int compare(Staff e1, Staff e2) {
				int res = e1.getFirstName().compareToIgnoreCase(
						e2.getFirstName());
				if (res != 0)
					return res;
				return e1.getLastName().compareToIgnoreCase(
						e2.getLastName());

			}
		};
		Collections.sort(staff, comparator); // use the comparator as much as u want
		
		log.debug("EXITING populateStaffs");
        return staff;
    }

	@ModelAttribute("staffs")
    public Map<String,String> populateStaffs() {
    	log.debug("ENTERED populateStaffs");
    	log.debug("STEP 1");
		List<Staff> staff = Staff.findStaffsByShop(getShop()).getResultList();
		Map map = new HashMap();
		Iterator itr = staff.iterator();
		while (itr.hasNext()) {
			Staff staffs = (Staff) itr.next();
			log.debug("staff: "+staffs.toString());
			String name = staffs.getFirstName();
			staffs.setFirstName(name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase());
			String lname = staffs.getLastName();
			staffs.setLastName(lname.substring(0, 1).toUpperCase() + lname.substring(1).toLowerCase());
			String sid = ""+staffs.getId();
			String desc = name + " " + lname;
			map.put(new String(sid), new String(desc));
		}
		
		log.debug("EXITING populateStaffs");
        return map;
    }

	@ModelAttribute("clients")
    public Map<String,String> populateClients() {
		log.debug("Entering populateClients");
		Map map = new HashMap();
		List<Clients> list = null;
		try {
			
			list = Clients.findClientsesByShop(getShop()).getResultList();
		} catch (Exception e) {
			log.error(e);
		}

		try {
			
			//SORT THE LIST BY FIRST LAST NAME THEN FIRST NAME			
			Collections.sort(list,new Comparator<Clients>() {
								
								@Override
								public int compare(Clients e1, Clients e2) {
									 int res =  e1.getFirstName().compareToIgnoreCase(e2.getFirstName());
						                if (res != 0)
						                    return res;
						                return e1.getLastName().compareToIgnoreCase(e2.getLastName());									
									
								}
							}				
				);
			map.put(new String(""), new String(""));
			for (Iterator i = list.iterator(); i.hasNext();) {
				Clients base = (Clients) i.next();
				String sid = new String();
				String desc = new String();
				sid = "" + base.getId();
				desc = base.getFirstName() + " " + base.getLastName();
				log.debug("MAP : " + sid + " " + desc);
				map.put(new String(sid), new String(desc));
			}
			
		} catch (Exception e) {
			log.error(e);
		}		
        return map;
    }
	

	
	@ModelAttribute("mobileclients")
    public List<Clients> populateClientsMobile() {
		log.debug("Entering populateClients");
//		Map map = new HashMap();
		List<Clients> list = null;
		try {
			
			list = Clients.findClientsesByShop(getShop()).getResultList();
// SORT THE LIST BY FIRST LAST NAME THEN FIRST NAME			
			Collections.sort(list,new Comparator<Clients>() {
								
								@Override
								public int compare(Clients e1, Clients e2) {
									 int res =  e1.getFirstName().compareToIgnoreCase(e2.getFirstName());
						                if (res != 0)
						                    return res;
						                return e1.getLastName().compareToIgnoreCase(e2.getLastName());									
									
								}
							}				
				);
//			map.put(new String(""), new String(""));
//			for (Iterator i = list.iterator(); i.hasNext();) {
//				Clients base = (Clients) i.next();
//				String sid = new String();
//				String desc = new String();
//				sid = "" + base.getId();
//				desc = base.getFirstName() + " " + base.getLastName();
//				log.debug("MAP : " + sid + " " + desc);
//				map.put(new String(sid), new String(desc));
//			}
		} catch (Exception e) {
			log.error(e);
		}
		
        return list;
    }
	@RequestMapping(value="/isalive", method=RequestMethod.GET)
    public @ResponseBody  String isAlive() {
		log.debug("Entering isAlive");
		String jsonarray = "true";
		String jsonarray2 = new JSONSerializer().exclude("*.class").serialize(jsonarray);
		log.debug("DISPLAY THE JSONIZED VERSION OF CLIENTS 2");
		log.debug(jsonarray2);
		log.debug("DISPLAY THE JSONIZED VERSION OF CLIENTS");
		log.debug(jsonarray);
		log.debug("Exiting isAlive");
        return jsonarray2;
    }
	
	@RequestMapping(value="/isalive", method=RequestMethod.POST)
    public @ResponseBody  String isAlivePost() {
		log.debug("Entering isAlive");
		String jsonarray = "true";
		String jsonarray2 = new JSONSerializer().exclude("*.class").serialize(jsonarray);
		log.debug("DISPLAY THE JSONIZED VERSION OF CLIENTS 2");
		log.debug(jsonarray2);
		log.debug("DISPLAY THE JSONIZED VERSION OF CLIENTS");
		log.debug(jsonarray);
		log.debug("Exiting isAlive");
        return jsonarray2;
    }
	
	@RequestMapping(value="/jsonclients", method=RequestMethod.GET)
    public @ResponseBody  String populateClientsJSON() {
		log.debug("Entering populateClientsJSON");
		List<Clients> list = Clients.findClientsesByShop(getShop()).getResultList();
		String jsonarray = "{ items: [{'id':141,'title':'My Title'},{'id':142,'title':'Title 2'}]}";
		Map map = new HashMap();
		
		for (Iterator i = list.iterator(); i.hasNext();) {
			Clients base = (Clients) i.next();
			String sid = new String();
			String firstname = new String();
			String lastname = new String();
			sid = ""+base.getId();
			firstname = base.getFirstName();
			map.put(new String("id"), new String(sid));
			map.put(new String("firstname"), new String(firstname));
			map.put(new String("lastname"), new String(lastname));
		}
		
		jsonarray = new JSONSerializer().exclude("*.class").serialize(map);
		String jsonarray2 = new JSONSerializer().exclude("*.class").serialize(list);
		log.debug("DISPLAY THE JSONIZED VERSION OF CLIENTS 2");
		log.debug(jsonarray2);
		log.debug("DISPLAY THE JSONIZED VERSION OF CLIENTS");
		log.debug(jsonarray);
		log.debug("Exiting populateClientsJSON");
        return jsonarray2;
    }
	@RequestMapping(value="/svctime", method=RequestMethod.POST)
    public @ResponseBody  String getServiceTime(@RequestParam(value="id",required=false) long id) {
		// list only BaseService for the Shop, not Custom
		log.debug("Entering getServiceTime");
		BaseService list = BaseService.findBaseService(id);

		String jsonarray2 = new JSONSerializer().exclude("*.class").serialize(list);
		log.debug("DISPLAY THE JSONIZED VERSION OF SERVICES 2");
		log.debug(jsonarray2);
		log.debug("Exiting getServiceTime");
        return jsonarray2;
    }
	/**
	 * determines if the input date time conflicts with any existing appointments
	 * 
	 * 
	 * @return
	 */
	@RequestMapping(value="/checkdatetime", method=RequestMethod.POST)
    public @ResponseBody  String checkDateTime(
    		@RequestParam(value="hSelectDate",required=true) String hSelectDate,
    		@RequestParam(value="hour",required=true) String hour, 
    		@RequestParam(value="minute",required=true) String minute,
    		@RequestParam(value="ampm",required=true) String ampm, 
    		@RequestParam(value="ehour",required=true) String endhour, 
    		@RequestParam(value="eminute",required=true) String endminute,
    		@RequestParam(value="eampm",required=true) String endampm 
    		) {
		// list only BaseService for the Shop, not Custom
		log.debug("Entering checkDateTime");
		
		String conflict = "false";
		
        int inthour = Integer.parseInt(hour);
        int intminute = Integer.parseInt(minute);
        if(ampm.equalsIgnoreCase("am")){
        	inthour = Integer.parseInt(hour);
        }else{
        	if(inthour != 12){
    			inthour = 12 + Integer.parseInt(hour);
        	}
        }
        int intehour = 0;
        int inteminute = 0;
        if(endhour != null && !endhour.equalsIgnoreCase("select") && endampm != null && !endampm.equalsIgnoreCase("select") && !endminute.equalsIgnoreCase("select")){
            intehour = Integer.parseInt(endhour);
            inteminute = Integer.parseInt(endminute);
            if(endampm.equalsIgnoreCase("am")){
            	intehour = Integer.parseInt(endhour);
            }else{
            	if(intehour != 12){
        			intehour = 12 + Integer.parseInt(endhour);
            	}
            }
        	
        }
		String dateString = hSelectDate; 
		log.debug("dateString: "+dateString);
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    Date apptDate = null; 
	    try {
	    	log.debug("ABOUT TO TRANSFORM DATE");
			if(hSelectDate == null || hSelectDate.equalsIgnoreCase("")){
				dateString = dateFormat.format(new Date());
				apptDate = dateFormat.parse(dateString);
			}else{
				apptDate = dateFormat.parse(dateString);	
			}
	    	
	    	
	    	log.debug("the transformed datestring into the apptDate is: "+apptDate.toString());
		} catch (ParseException e) {
			log.error(e);
			e.printStackTrace();
			conflict = "true";
		}
	    Calendar calapptdate = Calendar.getInstance();
	    calapptdate.setTime(apptDate);
		// BEGIN TIME
		Calendar begintime = (Calendar)calapptdate.clone();
		Date begindatetime = (Date)apptDate.clone();
		begindatetime.setDate(apptDate.getDate()); 
		begindatetime.setHours(inthour);
		begindatetime.setMinutes(intminute);// adjusting for lack of precision to evaluate microseconds otherwise query will not work
		begindatetime.setSeconds(0);
		begintime.setTime(begindatetime);
		begintime.set(Calendar.MILLISECOND, 0);
		
		String strdate2 = null;
		SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
		 if (begintime != null) {
			 strdate2 = sdf2.format(begintime.getTime());
		 }		

		log.debug("Calender begintime is: "+strdate2);
		
		// END TIME
		Date enddatetime = (Date)apptDate.clone();
		Calendar endtime = (Calendar)calapptdate.clone();
		enddatetime.setDate(apptDate.getDate());
		enddatetime.setHours(intehour);
		enddatetime.setMinutes(inteminute);// adjusting for lack of precision to evaluate microseconds otherwise query will not work
		enddatetime.setSeconds(0);
		endtime.setTime(enddatetime);
		endtime.set(Calendar.MILLISECOND, 0);
		
		String strdate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
		 if (endtime != null) {
			 strdate = sdf.format(endtime.getTime());
		 }		
		log.debug("Calender endtime is: "+strdate);
		log.debug("begindatetime is: "+begindatetime.toString() + " enddatetime : " + enddatetime.toString());
		
		List<Appointment> list = Appointment.appointmentConflict(getShop(), apptDate, begintime,endtime).getResultList();
		log.debug("RETURNED RESULTS OF FINDING APPOINTMENTS BETWEEN THESE DATES IS: "+list.size());
		if(list.size()>0){
			log.debug("THERE IS A CONFLICT CAUSE THE RETURNED RESULTS OF FINDING APPOINTMENTS BETWEEN THESE DATES IS: "+list.size());
			conflict = "true";
		}
		
		
		String jsonarray2 = new JSONSerializer().exclude("*.class").serialize(conflict);
		log.debug("DISPLAY THE JSONIZED VERSION OF SERVICES 2");
		log.debug(jsonarray2);
		log.debug("Exiting checkDateTime");
        return jsonarray2;
    }
	
	/**
	 * determines if the input date time conflicts with any existing appointments
	 * 
	 * 
	 * @return
	 */
	@RequestMapping(value="/checkdatetimestaff", method=RequestMethod.POST)
    public @ResponseBody  String checkDateTimeStaff(
    		@RequestParam(value="staff",required=true) String staff,
    		@RequestParam(value="hSelectDate",required=true) String hSelectDate,
    		@RequestParam(value="hour",required=true) String hour, 
    		@RequestParam(value="minute",required=true) String minute,
    		@RequestParam(value="ampm",required=true) String ampm, 
    		@RequestParam(value="ehour",required=true) String endhour, 
    		@RequestParam(value="eminute",required=true) String endminute,
    		@RequestParam(value="eampm",required=true) String endampm 
    		) {
		// list only BaseService for the Shop, not Custom
		log.debug("Entering checkDateTime");
		
		String conflict = "false";
		
        int inthour = Integer.parseInt(hour);
        int intminute = Integer.parseInt(minute);
        if(ampm.equalsIgnoreCase("am")){
        	inthour = Integer.parseInt(hour);
        }else{
        	if(inthour != 12){
        		inthour = 12 + Integer.parseInt(hour);
        	}
        }
        int intehour = 0;
        int inteminute = 0;
        if(endhour != null && !endhour.equalsIgnoreCase("select") && endampm != null && !endampm.equalsIgnoreCase("select") && !endminute.equalsIgnoreCase("select")){
            intehour = Integer.parseInt(endhour);
            inteminute = Integer.parseInt(endminute);
            if(endampm.equalsIgnoreCase("am")){
            	intehour = Integer.parseInt(endhour);
            }else{
            	if(intehour != 12){
            		intehour = 12 + Integer.parseInt(endhour);
            	}
            }
        	
        }
		String dateString = hSelectDate; 
		log.debug("dateString: "+dateString);
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    Date apptDate = null; 
	    try {
	    	log.debug("ABOUT TO TRANSFORM DATE");
			if(hSelectDate == null || hSelectDate.equalsIgnoreCase("")){
				dateString = dateFormat.format(new Date());
				apptDate = dateFormat.parse(dateString);
			}else{
				apptDate = dateFormat.parse(dateString);	
			}
	    	
	    	
	    	log.debug("the transformed datestring into the apptDate is: "+apptDate.toString());
		} catch (ParseException e) {
			log.error(e);
			e.printStackTrace();
			conflict = "true";
		}
	    Calendar calapptdate = Calendar.getInstance();
	    calapptdate.setTime(apptDate);
		// BEGIN TIME
		Calendar begintime = (Calendar)calapptdate.clone();
		Date begindatetime = (Date)apptDate.clone();
		begindatetime.setDate(apptDate.getDate()); 
		begindatetime.setHours(inthour);
		begindatetime.setMinutes(intminute);// adjusting for lack of precision to evaluate microseconds otherwise query will not work
		begindatetime.setSeconds(0);
		begintime.setTime(begindatetime);
		begintime.set(Calendar.MILLISECOND, 0);
		
		String strdate2 = null;
		SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
		 if (begintime != null) {
			 strdate2 = sdf2.format(begintime.getTime());
		 }		

		log.debug("Calender begintime is: "+strdate2);
		
		// END TIME
		Date enddatetime = (Date)apptDate.clone();
		Calendar endtime = (Calendar)calapptdate.clone();
		enddatetime.setDate(apptDate.getDate());
		enddatetime.setHours(intehour);
		enddatetime.setMinutes(inteminute);// adjusting for lack of precision to evaluate microseconds otherwise query will not work
		enddatetime.setSeconds(0);
		endtime.setTime(enddatetime);
		endtime.set(Calendar.MILLISECOND, 0);
		
		String strdate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
		 if (endtime != null) {
			 strdate = sdf.format(endtime.getTime());
		 }		
		log.debug("Calender endtime is: "+strdate);
		log.debug("begindatetime is: "+begindatetime.toString() + " enddatetime : " + enddatetime.toString());
		Staff staf = Staff.findStaff(Long.parseLong(staff));
		List<Appointment> list = Appointment.appointmentConflictStaffStatusList(staf, getShop(), apptDate, begintime,endtime).getResultList();
		log.debug("RETURNED RESULTS OF FINDING APPOINTMENTS BETWEEN THESE DATES IS: "+list.size());
		if(list.size()>0){
			for(int d = 0; d<list.size(); d++){
				Appointment dappt = list.get(d);
				log.debug("appointment id: "+dappt.getId());
				log.debug("STATUS: "+dappt.getStatus());
				log.debug("EndDateTime: "+dappt.getEndDateTime().getTime().toString());
				log.debug("BeginDateTime: "+dappt.getBeginDateTime().getTime().toString());
				log.debug("DESCRIPTION: "+dappt.getDescription());
				log.debug("Staff: "+dappt.getStaff().getFirstName());
				if(dappt.getClient() != null){
					log.debug("Client: "+dappt.getClient().getFirstName());
				}
			}
			log.debug("THERE IS A CONFLICT CAUSE THE RETURNED RESULTS OF FINDING APPOINTMENTS BETWEEN THESE DATES IS: "+list.size());
			conflict = "true";
		}
		
		
		String jsonarray2 = new JSONSerializer().exclude("*.class").serialize(conflict);
		log.debug("DISPLAY THE JSONIZED VERSION OF SERVICES 2");
		log.debug(jsonarray2);
		log.debug("Exiting checkDateTime");
        return jsonarray2;
    }	
	@RequestMapping(value="/jsonsvc", method=RequestMethod.POST)
    public @ResponseBody  String populateService1JSON(@RequestParam(value="id",required=false) long id) {
		// list only BaseService for the Shop, not Custom
		log.debug("Entering populateService1JSON");
		List<BaseService> list = BaseService.findBaseServicesByShop(getShop()).getResultList();
		String jsonarray = "{ items: [{'id':141,'title':'My Title'},{'id':142,'title':'Title 2'}]}";
		Appointment appointment = Appointment.findAppointment(id);
		
		Clients client = appointment.getClient();
		Map map = new HashMap();
		
		for (Iterator i = list.iterator(); i.hasNext();) {
			BaseService base = (BaseService) i.next();
			String sid = new String();
			String desc = new String();
			sid = ""+base.getId();
			desc = base.getDescription();
			map.put(new String("id"), new String(sid));
			map.put(new String("value"), new String(desc));
			log.debug("MAP : "+sid + " "+desc);
			log.debug("The baseservice is: "+base.toString());
			if (base instanceof CustomService) {
				if(((CustomService) base).getClient().getId()==client.getId()){
					log.debug("NOT REMOVING CUSTOM SERVICE");
				}else{
					log.debug("REMOVING CUSTOM SERVICE");
					i.remove();
				}
			}
		}
		
		jsonarray = new JSONSerializer().exclude("*.class").serialize(map);
		String jsonarray2 = new JSONSerializer().exclude("*.class").serialize(list);
		log.debug("DISPLAY THE JSONIZED VERSION OF SERVICES 2");
		log.debug(jsonarray2);
		log.debug("DISPLAY THE JSONIZED VERSION OF SERVICES");
		log.debug(jsonarray);
		log.debug("Exiting populateService1JSON");
        return jsonarray2;
    }
	@RequestMapping(value="/jsonsvcs", method=RequestMethod.POST)
    public @ResponseBody  String populateServicesJSON() {
		// list only BaseService for the Shop, not Custom
		log.debug("Entering populateService1JSON");
		List<BaseService> list = BaseService.findBaseServicesByShop(getShop()).getResultList();
		String jsonarray = "{ items: [{'id':141,'title':'My Title'},{'id':142,'title':'Title 2'}]}";
		Map map = new HashMap();
		
		for (Iterator i = list.iterator(); i.hasNext();) {
			BaseService base = (BaseService) i.next();
			String sid = new String();
			String desc = new String();
			sid = ""+base.getId();
			desc = base.getDescription();
			map.put(new String("id"), new String(sid));
			map.put(new String("value"), new String(desc));
			log.debug("MAP : "+sid + " "+desc);
			log.debug("The baseservice is: "+base.toString());
			if (base instanceof CustomService) {
				log.debug("REMOVING CUSTOM SERVICE");
				i.remove();
			}
		}
		
		jsonarray = new JSONSerializer().exclude("*.class").serialize(map);
		String jsonarray2 = new JSONSerializer().exclude("*.class").serialize(list);
		log.debug("DISPLAY THE JSONIZED VERSION OF SERVICES");
		log.debug(jsonarray2);
		log.debug("DISPLAY THE JSONIZED VERSION OF SERVICES");
		log.debug(jsonarray);
		log.debug("Exiting populateService1JSON");
        return jsonarray2;
    }
	@RequestMapping(value="/jsonsvcsg", method=RequestMethod.GET)
    public @ResponseBody  String populateServicesgJSON() {
		// list only BaseService for the Shop, not Custom
		log.debug("Entering populateService1JSON");
		List<BaseService> list = BaseService.findBaseServicesByShop(getShop()).getResultList();
		String jsonarray = "{ items: [{'id':141,'title':'My Title'},{'id':142,'title':'Title 2'}]}";
		Map map = new HashMap();
		
		for (Iterator i = list.iterator(); i.hasNext();) {
			BaseService base = (BaseService) i.next();
			String sid = new String();
			String desc = new String();
			sid = ""+base.getId();
			desc = base.getDescription();
			String amttime = "" + base.getAmounttime();
			map.put(new String("id"), new String(sid));
			map.put(new String("value"), new String(desc));
			map.put(new String("amounttime"), new String(amttime));
			log.debug("MAP : "+sid + " "+desc);
			log.debug("The baseservice is: "+base.toString());
			if (base instanceof CustomService) {
				log.debug("REMOVING CUSTOM SERVICE");
				i.remove();
			}
		}
		
		jsonarray = new JSONSerializer().exclude("*.class").serialize(map);
		String jsonarray2 = new JSONSerializer().exclude("*.class").serialize(list);
		log.debug("DISPLAY THE JSONIZED VERSION OF SERVICES");
		log.debug(jsonarray2);
		log.debug("DISPLAY THE JSONIZED VERSION OF SERVICES");
		log.debug(jsonarray);
		log.debug("Exiting populateService1JSON");
        return jsonarray2;
    }	
	
	@RequestMapping(value="/jsonsvc", method=RequestMethod.GET)
	public @ResponseBody  String getService1JSON(@RequestParam(value="id",required=false) long id) {
		// list only BaseService for the Shop, not Custom
		log.debug("Entering populateService1JSON");
		List<BaseService> list = BaseService.findBaseServicesByShop(getShop()).getResultList();
		String jsonarray = "{ items: [{'id':141,'title':'My Title'},{'id':142,'title':'Title 2'}]}";
		Appointment appointment = Appointment.findAppointment(id);
		
		Clients client = appointment.getClient();
		Map map = new HashMap();
		
		for (Iterator i = list.iterator(); i.hasNext();) {
			BaseService base = (BaseService) i.next();
			String sid = new String();
			String desc = new String();
			sid = ""+base.getId();
			desc = base.getDescription();
			map.put(new String("id"), new String(sid));
			map.put(new String("value"), new String(desc));
			log.debug("MAP : "+sid + " "+desc);
			log.debug("The baseservice is: "+base.toString());
			if (base instanceof CustomService) {
				if(((CustomService) base).getClient().getId()==client.getId()){
					log.debug("NOT REMOVING CUSTOM SERVICE");
				}else{
					log.debug("REMOVING CUSTOM SERVICE");
					i.remove();
				}
			}
		}
		
		jsonarray = new JSONSerializer().exclude("*.class").serialize(map);
		String jsonarray2 = new JSONSerializer().exclude("*.class").serialize(list);
		log.debug("DISPLAY THE JSONIZED VERSION OF SERVICES 2");
		log.debug(jsonarray2);
		log.debug("DISPLAY THE JSONIZED VERSION OF SERVICES");
		log.debug(jsonarray);
		log.debug("Exiting populateService1JSON");
        return jsonarray2;
    }

	/**
	 * same as @RequestMapping(value="/jsonsvc", method=RequestMethod.GET) but 
	 * uses the appointment id to get the svcid then returns results 
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/jsonapptsvc", method=RequestMethod.GET)
	public @ResponseBody  String getService1JSONViaAppt(@RequestParam(value="apptid",required=false) long apptid) {
		// list only BaseService for the Shop, not Custom
		log.debug("Entering getService1JSONViaAppt");
		List<BaseService> list = BaseService.findBaseServicesByShop(getShop()).getResultList();
		String jsonarray = "{ items: [{'id':141,'title':'My Title'},{'id':142,'title':'Title 2'}]}";
		Appointment appointment = Appointment.findAppointment(apptid);
		
		Clients client = appointment.getClient();
		Map map = new HashMap();
		
		for (Iterator i = list.iterator(); i.hasNext();) {
			BaseService base = (BaseService) i.next();
			String sid = new String();
			String desc = new String();
			sid = ""+base.getId();
			desc = base.getDescription();
			map.put(new String("id"), new String(sid));
			map.put(new String("value"), new String(desc));
			log.debug("MAP : "+sid + " "+desc);
			log.debug("The baseservice is: "+base.toString());
			if (base instanceof CustomService) {
				if(((CustomService) base).getClient().getId()==client.getId()){
					log.debug("NOT REMOVING CUSTOM SERVICE");
				}else{
					log.debug("REMOVING CUSTOM SERVICE");
					i.remove();
				}
			}
		}
		
		jsonarray = new JSONSerializer().exclude("*.class").serialize(map);
		String jsonarray2 = new JSONSerializer().exclude("*.class").serialize(list);
		log.debug("DISPLAY THE JSONIZED VERSION OF SERVICES 2");
		log.debug(jsonarray2);
		log.debug("DISPLAY THE JSONIZED VERSION OF SERVICES");
		log.debug(jsonarray);
		log.debug("Exiting getService1JSONViaAppt");
        return jsonarray2;
    }
	
	@ModelAttribute("services")
    public Map<String,String> populateServices() {
		Map map = new HashMap();
		List<BaseService> list = null;
		try {
			list = BaseService.findBaseServicesByShop(getShop()).getResultList();
			
			map.put(new String(""), new String(""));
			for (Iterator i = list.iterator(); i.hasNext();) {
				BaseService base = (BaseService) i.next();
				String sid = new String();
				String desc = new String();
				sid = "" + base.getId();
				desc = base.getDescription();
				String amttime = "" + base.getAmounttime();
				log.debug("MAP : " + sid + " " + desc);
				log.debug("The baseservice is: " + base.toString());
				if (base instanceof CustomService) {
					log.debug("REMOVING CUSTOM SERVICE");
					i.remove();
				} else {
					log.debug("NOT CUSTOM SERVICE");
					map.put(new String(sid), new String(desc + " (" + amttime
							+ ")"));
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		return map;
    }
	
	@ModelAttribute("mobileservices")
    public List<BaseService> populateServicesMobile() {
//		Map map = new HashMap();
		List<BaseService> list = null;
		try {
			list = BaseService.findBaseServicesByShop(getShop()).getResultList();
			BaseService blank = new BaseService();
//			blank.setAmounttime(new Integer(0));
			blank.setDescription("");
			list.add(blank);
			Collections.sort(list, new Comparator<BaseService>() {

				@Override
				public int compare(BaseService e1, BaseService e2) {
//					int res = e1.getDescription().compareToIgnoreCase(
//							e2.getDescription());
//					if (res != 0)
//						return res;
					return e1.getDescription().compareToIgnoreCase(
							e2.getDescription());

				}
			});
			
//			map.put(new String(""), new String(""));
//			for (Iterator i = list.iterator(); i.hasNext();) {
//				BaseService base = (BaseService) i.next();
//				String sid = new String();
//				String desc = new String();
//				sid = "" + base.getId();
//				desc = base.getDescription();
//				String amttime = "" + base.getAmounttime();
//				log.debug("MAP : " + sid + " " + desc);
//				log.debug("The baseservice is: " + base.toString());
//				if (base instanceof CustomService) {
//					log.debug("REMOVING CUSTOM SERVICE");
//					i.remove();
//				} else {
//					log.debug("NOT CUSTOM SERVICE");
//					map.put(new String(sid), new String(desc + " (" + amttime
//							+ ")"));
//				}
//			}
		} catch (Exception e) {
			log.error(e);
		}
		return list;
    }
	
	/**
	 * categories of services
	 * 
	 * @return
	 */
	@ModelAttribute("categories")
    public Map<String,String> populateGroupServices() {
		Map map = new HashMap();
		try {
			List<ServiceGroup> list = ServiceGroup.findServiceGroupsByShop(getShop()).getResultList();
			map.put(new String("0"), new String("All"));
			for (Iterator i = list.iterator(); i.hasNext();) {
				ServiceGroup base = (ServiceGroup) i.next();
				String sid = new String();
				String desc = new String();
				desc = "" + base.getGroup_name();
				sid = "" + base.getId();
				log.debug("MAP : " + sid + " " + desc);
				log.debug("The service group is: " + base.toString());
				map.put(new String(sid), new String(desc));
			}
		} catch (Exception e) {
			log.error(e);
		}
		return map;
    }

	/**
	 * list of categories matched with services
	 * 
	 * @return
	 */
	@ModelAttribute("categoryservices")
    public List<CategoryServiceForm> populateCategoryServices() {
		List<CategoryServiceForm> catsvclist = new ArrayList<CategoryServiceForm>();
		try {
			List<ServiceGroup> list = ServiceGroup.findServiceGroupsByShop(getShop()).getResultList();
			
			for (Iterator i = list.iterator(); i.hasNext();) {
				ServiceGroup base = (ServiceGroup) i.next();
				String sid = new String();
				String desc = new String();
				desc = "" + base.getGroup_name();
				sid = "" + base.getId();
				log.debug("MAP : " + sid + " " + desc);
				log.debug("The service group is: " + base.toString());
				Set<BaseService> setsvcs = base.getServices();
				for (Iterator it=setsvcs.iterator(); it.hasNext(); ) {
					BaseService bservice = (BaseService)it.next();
					CategoryServiceForm csf = new CategoryServiceForm();
					String amttime = "" + bservice.getAmounttime();
					csf.setCatid(sid);
					csf.setSvcamounttime(amttime);
					csf.setServicedescription(bservice.getDescription());
					csf.setSvcid(""+bservice.getId());
					csf.setSvcprice(""+bservice.getCost());
					catsvclist.add(csf);
				}				
				
			}
		} catch (Exception e) {
			log.error(e);
		}
		return catsvclist;
    }

	
	@ModelAttribute("servicesprices")
    public Map<String,String> populateServicesPrices() {
		Map map = new HashMap();
		try {
			List<BaseService> list = BaseService.findBaseServicesByShop(getShop()).getResultList();
			map.put(new String(""), new String(""));
			for (Iterator i = list.iterator(); i.hasNext();) {
				BaseService base = (BaseService) i.next();
				String sid = new String();
				String price = new String();
				sid = "" + base.getId();
				price = ""+base.getCost();
				log.debug("MAP : " + sid + " " + price);
				log.debug("The baseservice is: " + base.toString());
				if (base instanceof CustomService) {
					log.debug("REMOVING CUSTOM SERVICE");
					i.remove();
				} else {
					log.debug("NOT CUSTOM SERVICE");
					map.put(new String(sid), new String(price));
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		return map;
    }
	
	@ModelAttribute("service1")
    public Collection<BaseService> populateService1() {
		// list only BaseService for the Shop, not Custom
		log.debug("ENTERING populateService1");
		List<BaseService> list = BaseService.findBaseServicesByShop(getShop()).getResultList();
		for (Iterator i = list.iterator(); i.hasNext();) {
			BaseService base = (BaseService) i.next();
			log.debug("the base service1 is: "+base.toString());
			if (base instanceof CustomService) {
				i.remove();
			}
		}
		log.debug("EXITING populateService1");
        return list;
    }
	@ModelAttribute("service2")
    public Collection<BaseService> populateService2() {
		log.debug("Entering populateService2");
        return BaseService.findBaseServicesByShop(getShop()).getResultList();
    }
	@ModelAttribute("service3")
    public Collection<BaseService> populateService3() {
		log.debug("Entering populateService3");
        return BaseService.findBaseServicesByShop(getShop()).getResultList();
    }
	@ModelAttribute("service4")
    public Collection<BaseService> populateService4() {
		log.debug("Entering populateService4");
        return BaseService.findBaseServicesByShop(getShop()).getResultList();
    }
	
	/**
	 * called by the Edit Appointment dialog.
	 * 
	 * Allows the price to be changed.
	 * 
	 * The appointment to be cancelled.
	 * 
	 * A note to be added
	 * 
	 * @param apptid
	 * @return
	 */
	@RequestMapping(value="/update", method=RequestMethod.GET)
	public @ResponseBody  String updateAppt(
			@RequestParam(value="aid") String apptid,
			@RequestParam(value="c",required = false) Boolean cancel,
			@RequestParam(value="sid") long serviceid,
			@RequestParam(value="st") String servicetype,
			@RequestParam(value="sp") Float serviceprice,
			@RequestParam(value="n",required = false) String notes) {
		log.debug("ENTERED THE UPDATE IN MYSCHEDULE FOR A AJAX CALL");
		log.debug("apptid  : "+apptid);
		log.debug("cancel : "+cancel);
		log.debug("serviceid : "+serviceid);
		log.debug("servicetype : "+servicetype);
		log.debug("serviceprice : "+serviceprice);
		log.debug("notes  : "+notes);
		log.debug("notes decoded : "+URLDecoder.decode(notes));
		
		boolean updateappt = false;
		// get the appointment by id
		Appointment appointment = Appointment.findAppointment(Long.parseLong(apptid));
		
		// update price, cancel, and notes
		if(cancel != null && appointment.getCancelled() != cancel){
			appointment.setCancelled(cancel);
			updateappt = true;
			log.debug("update the cancelled");
			// adding audit log
			String appointmentbegintime = "";
			SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm:ss a");
			appointmentbegintime = dateformatter.format(appointment.getBeginDateTime().getTime());
			
			SimpleDateFormat dateformatterappt = new SimpleDateFormat("MM/dd/yyyy");
			String originaldate = dateformatterappt.format(appointment.getAppointmentDate());
			String staffname = "";
			if(appointment.getStaff() != null){
				staffname = appointment.getStaff().getFirstName() + " " + appointment.getStaff().getLastName();
			}
			saveAuditMessage("Appointment for "+ originaldate+" "+ appointmentbegintime+" Client: "+appointment.getClient().getFirstName()+" "+appointment.getClient().getLastName()+" SET CANCELLED "+cancel+" by staff " +staffname,null,"GENERAL");
			
		}else if(cancel == null){
			// same as if it is false
			log.debug("update the cancelled even though it was null");
			appointment.setCancelled(false);
			updateappt = true;
			// adding audit log
			String appointmentbegintime = "";
			SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm:ss a");
			appointmentbegintime = dateformatter.format(appointment.getBeginDateTime().getTime());
			
			SimpleDateFormat dateformatterappt = new SimpleDateFormat("MM/dd/yyyy");
			String originaldate = dateformatterappt.format(appointment.getAppointmentDate());
			String staffname = "";
			if(appointment.getStaff() != null){
				staffname = appointment.getStaff().getFirstName() + " " + appointment.getStaff().getLastName();
			}
			saveAuditMessage("Appointment for "+ originaldate+" "+ appointmentbegintime+" Client: "+appointment.getClient().getFirstName()+" "+appointment.getClient().getLastName()+" SET CANCELLED TO FALSE by staff " +staffname,null,"GENERAL");
			
		}
		if(notes != null && appointment.getNotes() != notes){
			String tmp = appointment.getNotes();
			if(tmp != null){
				tmp.concat(" "+notes);
			}else{
				tmp = notes;
			}
			log.debug("update the notes");
			appointment.setNotes(tmp);
			updateappt = true;
			// adding audit log
			String appointmentbegintime = "";
			SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm:ss a");
			appointmentbegintime = dateformatter.format(appointment.getBeginDateTime().getTime());
			
			SimpleDateFormat dateformatterappt = new SimpleDateFormat("MM/dd/yyyy");
			String originaldate = dateformatterappt.format(appointment.getAppointmentDate());
			String staffname = "";
			if(appointment.getStaff() != null){
				staffname = appointment.getStaff().getFirstName() + " " + appointment.getStaff().getLastName();
			}
			saveAuditMessage("Appointment for "+ originaldate+" "+ appointmentbegintime+" Client: "+appointment.getClient().getFirstName()+" "+appointment.getClient().getLastName()+" UPDATED NOTES by staff " +staffname,null,"GENERAL");
			
		}
		Set<BaseService> firstservice= appointment.getServices();
		// TODO figure out how to update the correct service. might need to 
		// grab the id when first setting the AppointmentDeep
		int asz = firstservice.size();
		// if there is only one service then no problem
		if(asz == 1){
			log.debug("the number of services is one");
			BaseService svc = firstservice.iterator().next();
			if(servicetype.equalsIgnoreCase("custom") && svc instanceof CustomService){
				log.debug("the value of the service cost: "+svc.getCost());
				if(serviceprice != null && svc.getCost() != serviceprice){
					svc.setCost(serviceprice);
					svc.merge();
					log.debug("update the customservice");
				}
			}else{
				// this is a basicservice type that needs to be customized
				
				// since there is only one service for the appointment we do not need to 
				// check to see if an existing customservice is available
				if(serviceprice != null && svc.getCost() != serviceprice && cancel != null && cancel != true ){
					CustomService custom = new CustomService();
					custom.setSendReminders(svc.isSendReminders());
					custom.setProcesstime(svc.getProcesstime());
					custom.setFinishtime(svc.getFinishtime());
					custom.setMinsetup(svc.getMinsetup());
					custom.setAmounttime(svc.getAmounttime());
					custom.setLength_time(svc.getLength_time());
					custom.setDescription("Custom "+svc.getDescription());
					custom.setInfo_note(svc.getInfo_note());
					custom.setShop(svc.getShop());
					custom.setOriginalid(svc.getId());
					custom.setClient(appointment.getClient());
					custom.setCost(serviceprice);
					log.debug("persisting customservice");
					//custom.merge();
					log.debug("the id of the newly persisted customservice is: "+custom.getId());
					// remove the basicservice and replace with this customservice
					firstservice.remove(svc);
					firstservice.add(custom);
					log.debug("update the service");
				}
			}
		}
		// TODO: otherwise search thru list of services
		if(asz > 1){
			
		}
		if(updateappt == true){
			appointment = saveAppointment(appointment);
			
			System.setProperty("file.encoding", "UTF-8");
			log.debug("appointment.getStatus(): "+appointment.getStatus());
			if(appointment.getStatus() != ScheduleStatus.CHECKED_OUT){
				sendMessage(appointment);
			}
			// adding audit log
			String appointmentbegintime = "";
			SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm:ss a");
			appointmentbegintime = dateformatter.format(appointment.getBeginDateTime().getTime());
			
			SimpleDateFormat dateformatterappt = new SimpleDateFormat("MM/dd/yyyy");
			String originaldate = dateformatterappt.format(appointment.getAppointmentDate());
			String staffname = "";
			if(appointment.getStaff() != null){
				staffname = appointment.getStaff().getFirstName() + " " + appointment.getStaff().getLastName();
			}
			saveAuditMessage("Appointment for "+ originaldate+" "+ appointmentbegintime+" Client: "+appointment.getClient().getFirstName()+" "+appointment.getClient().getLastName()+" UPDATED by staff " +staffname,null,"GENERAL");
			
			log.debug("merged or updated the appointment");
		}
		List<AppointmentDeep> appt = new ArrayList<AppointmentDeep>();
		AppointmentDeep deep = new AppointmentDeep(appointment);
		//TODO: AT THIS POINT LOOK UP CUSTOMPRICE
		BaseService b = BaseService.findBaseService(deep.getService1id());
		if(b != null){
			TypedQuery<CustomPrice> tcp = CustomPrice.findCustomPricesByShopAndAppointmentAndServiceAndClient(getShop(), appointment, b, deep.getClient());
			List<CustomPrice> lcp = tcp.getResultList();
			if(lcp.size() > 0){
				log.debug("Found custom price for this appointment: "+lcp.get(0).getCost());
				deep.setService1cost(""+lcp.get(0).getCost());
			}
		}
		
		appt.add(deep);
		String jsonarray = AppointmentDeep.toJsonArray(appt);
		return jsonarray;
	}

	public Date convertStringToDateDynamic(String stringdate, String format) {
		log.debug("ENTERED convertStringToDateDynamic");
		Date newdate = new Date();
		try {
			String str_date = stringdate;
			DateFormat formatter;
			Date date;
			formatter = new SimpleDateFormat(format);
			newdate = (Date) formatter.parse(str_date);
			log.debug("string date being converted is " + newdate);
		} catch (ParseException e) {
			log.error("Exception :" + e);
		}
		log.debug("EXITING convertStringToDateDynamic");
		return newdate;
	}
	
	public Date convertStringToDate(String stringdate) {
		log.debug("ENTERED convertStringToDate");
		Date newdate = new Date();
		try {
			String str_date = stringdate;
			DateFormat formatter;
			Date date;
			formatter = new SimpleDateFormat("yyyy-MM-dd");
			newdate = (Date) formatter.parse(str_date);
			log.debug("string date being converted is " + newdate);
		} catch (ParseException e) {
			log.error("Exception :" + e);
		}
		log.debug("EXITING convertStringToDate");
		return newdate;
	}

	public Date convertStringMMddyyy(String stringdate) {
		log.debug("ENTERED convertStringToDate");
		Date newdate = new Date();
		try {
			String str_date = stringdate;
			DateFormat formatter;
			Date date;
			formatter = new SimpleDateFormat("MM/dd/yyyy");
			newdate = (Date) formatter.parse(str_date);
			log.debug("string date being converted is " + newdate);
		} catch (ParseException e) {
			log.error("Exception :" + e);
		}
		log.debug("EXITING convertStringToDate");
		return newdate;
	}
	
	private Date convertStringToDateWithSlashes(String stringdate) {
		log.debug("ENTERED convertStringToDateWithSlashes");
		Date newdate = new Date();
		try {
			String str_date = stringdate;
			DateFormat formatter;
			Date date;
			formatter = new SimpleDateFormat("MM/dd/yyyy");
			newdate = (Date) formatter.parse(str_date);
			log.debug("string date being converted is " + newdate);
		} catch (ParseException e) {
			log.error("Exception :" + e);
		}
		log.debug("EXITING convertStringToDateWithSlashes");
		return newdate;
	}

	@RequestMapping(value="/updateedit", method=RequestMethod.GET)
	public @ResponseBody  String updateEditAppt(
			@RequestParam(value="staffid") String staffid,
			@RequestParam(value="aid") String apptid,
			@RequestParam(value="c",required = false) Boolean cancel,
			@RequestParam(value="sid") long serviceid,
			@RequestParam(value="st") String servicetype,
			@RequestParam(value="sp") Float serviceprice,
			@RequestParam(value="n",required = false) String notes,
			@RequestParam(value="hour",required = false) String hour,
			@RequestParam(value="min",required = false) String minute,
			@RequestParam(value="ap",required = false) String ampm,
			@RequestParam(value="d",required = false) String newdate,
			@RequestParam(value="ehour",required = false) String endhour,
			@RequestParam(value="emin",required = false) String endminute,
			@RequestParam(value="eap",required = false) String endampm,
			@RequestParam(value="uf",required = false) String updatefuture,
			@RequestParam(value="ri",required = false) String request_indicator,
			@RequestParam(value="ischeckout",required = false) boolean ischeckout
			) {
		log.debug("ENTERED THE updateEditAppt IN MYSCHEDULE FOR A AJAX CALL");
		log.debug("apptid  : "+apptid);
		log.debug("cancel : "+cancel);
		log.debug("serviceid : "+serviceid);
		log.debug("servicetype : "+servicetype);
		log.debug("serviceprice : "+serviceprice);
		log.debug("notes  : "+notes);
		log.debug("notes decoded : "+URLDecoder.decode(notes));
		log.debug("hour : "+hour);
		log.debug("minute : "+minute);
		log.debug("ampm : "+ampm);
		log.debug("newdate : "+newdate);
		log.debug("end hour : "+endhour);
		log.debug("end minute : "+endminute);
		log.debug("end ampm : "+endampm);
		log.debug("ischeckout: "+ischeckout);
		boolean updateappt = false;
		// get the appointment by id
		Appointment appointment = Appointment.findAppointment(Long.parseLong(apptid));
		if(newdate != null) {
			String originaldate = appointment.getAppointmentDate().getYear()+"-"+ appointment.getAppointmentDate().getMonth()+"-"+ appointment.getAppointmentDate().getDay();
			log.debug("getYear: "+ appointment.getAppointmentDate().getYear());
			log.debug("getMonth: "+ appointment.getAppointmentDate().getMonth());
			log.debug("getDay: "+ appointment.getAppointmentDate().getDay());
			log.debug("originaldate: "+ originaldate);
			if(!originaldate.equalsIgnoreCase(newdate)){
				Date converteddate = convertStringToDate(newdate);
				log.debug("The converted date is: "+converteddate.toString());
				appointment.setAppointmentDate(converteddate);
				log.debug("changed date "+ appointment.getAppointmentDate().getTime());
				updateappt = true;
			}
		}
		// 0 is 12am , 12 is 12pm
		int newendhour = 0;
		if(endampm != null) {
			if(endampm.equalsIgnoreCase("PM")){
				// 	pm
				if(!endhour.equalsIgnoreCase("12")){
					endhour = ""+(Integer.parseInt(endhour) + 12);
					log.debug("endhour adjusted for pm: "+endhour);
				}
			}
		}		
		if(endhour != null && !endhour.equalsIgnoreCase("select") 
				&& !endhour.equalsIgnoreCase("") && !endhour.equalsIgnoreCase("select") 
				&& endminute != null && !endminute.equalsIgnoreCase("") && !endminute.equalsIgnoreCase("select")
				) {
			String original_endhour = ""+appointment.getEndDateTime().getTime().getHours();
			log.debug("original_endhour: "+original_endhour);
			//if(!original_endhour.equalsIgnoreCase(endhour)){
				if(endampm != null) {
					newendhour = Integer.parseInt(endhour);
					DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String copynewdate = newdate;
					if(newendhour<10){
						copynewdate = copynewdate + " 0"+newendhour+":"+endminute+":00";
					}else{
						copynewdate = copynewdate + " "+newendhour+":"+endminute+":00";
					}
					try {
						log.debug("copynewdate: "+copynewdate);
						Date a = dfm.parse(copynewdate);
						Calendar cal = Calendar.getInstance();
						cal.setTime(a);
						appointment.setEndDateTime(cal);
					} catch (ParseException e) {
						log.error(""+e.getMessage());
					}
					
					
					log.debug("changed end hour "+ appointment.getEndDateTime().getTime());
					updateappt = true;
				}
			//}
		}
		int newhour = 0;
		if(ampm != null) {
			if(ampm.equalsIgnoreCase("PM")){
				// 	pm
				if(!hour.equalsIgnoreCase("12")){
					hour = ""+ (Integer.parseInt(hour) + 12);
					log.debug("hour adjusted for pm: "+hour);
				}
			}else{
				// am
			}
		}
		
		if(hour != null && !hour.equalsIgnoreCase("") && minute != null && !minute.equalsIgnoreCase("")) {
//			String original_hour = ""+appointment.getBeginDateTime().getTime().getHours();
//			log.debug("original_hour: "+original_hour);
//			if(!original_hour.equalsIgnoreCase(hour)){
				newhour = Integer.parseInt(hour);
				
				
				DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				if(newhour < 10){
					newdate = newdate + " 0"+newhour+":"+minute+":00";
				}else{
					newdate = newdate + " "+newhour+":"+minute+":00";
				}
				try {
					log.debug("newdate: "+newdate);
					Date a = dfm.parse(newdate);
					Calendar cal = Calendar.getInstance();
					cal.setTime(a);
					appointment.setBeginDateTime(cal);
					if(endhour == null || endhour.equalsIgnoreCase("select") 
							|| endhour.equalsIgnoreCase("") || endhour.equalsIgnoreCase("select") 
							|| endminute == null || endminute.equalsIgnoreCase("") || endminute.equalsIgnoreCase("select")
							) {
						
						// figure out what the end time should be by the length_time and amounttime for the service 
						Set<BaseService> firstservice= appointment.getServices();
						// figure out how to update the correct service. might need to 
						// grab the id when first setting the AppointmentDeep
						int asz = firstservice.size();
						// if there is only one service then no problem
						if(asz > 0){
							log.debug("the number of services is greater than 0");
							BaseService svc = firstservice.iterator().next();
							Calendar cal_end = Calendar.getInstance();
							int time = svc.getAmounttime();
							Date enddatetime = new Date();
							enddatetime.setHours(cal.getTime().getHours());
							enddatetime.setMinutes(cal.getTime().getMinutes()+time);
							enddatetime.setSeconds(0);
							cal_end.setTime(enddatetime);
							cal_end.set(Calendar.MILLISECOND, 0);
							
							appointment.setEndDateTime(cal_end);
							
							
						}else{
							log.debug("the number of services is zero");
							BaseService svc = BaseService.findBaseService(serviceid);
							Set<BaseService> bs = new HashSet<BaseService>();
							bs.add(svc);
							appointment.setServices(bs);
							
							svc.setShop(getShop());
							Calendar cal_end = Calendar.getInstance();
							int time = svc.getAmounttime();
							Date enddatetime = new Date();
							enddatetime.setHours(cal.getTime().getHours());
							enddatetime.setMinutes(cal.getTime().getMinutes()+time);
							enddatetime.setSeconds(0);
							cal_end.setTime(enddatetime);
							cal_end.set(Calendar.MILLISECOND, 0);
							
							appointment.setEndDateTime(cal_end);
							
						}
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					log.error(""+e.getMessage());
				}
				
				
				log.debug("changed hour "+ appointment.getBeginDateTime().getTime());
				updateappt = true;
			//}
		}
		
		// update price, cancel, and notes
		if(cancel != null && appointment.getCancelled() != cancel){
			appointment.setCancelled(cancel);
			updateappt = true;
			log.debug("update the cancelled");
		}else if(cancel == null){
			// same as if it is false
			log.debug("update the cancelled even though it was null");
			appointment.setCancelled(false);
			updateappt = true;
		}
		if(notes != null && appointment.getNotes() != notes){
			String tmp = appointment.getNotes();
			tmp = notes;
			log.debug("update the notes");
			appointment.setNotes(tmp);
			updateappt = true;
		}
		Set<BaseService> firstservice= appointment.getServices();
		// TODO figure out how to update the correct service. might need to 
		// grab the id when first setting the AppointmentDeep
		int asz = firstservice.size();
		// if there is only one service then no problem
		if(asz > 0){
			log.debug("the number of services is one");
			BaseService svc = firstservice.iterator().next();
			if(servicetype.equalsIgnoreCase("custom") && svc instanceof CustomService){
				log.debug("this is a custom service");
				log.debug("determine status of service");
				log.debug("svc.getId(): "+svc.getId());
				log.debug("serviceid: "+serviceid);
				// USE OF CUSTOMSERVICE DEPRECATED. 
				CustomService customerservice = (CustomService)svc;
				log.debug("cast to customerservice");
				// GRAB RELEVANT DATA FROM CUSTOMSERVICE
				Long originalid = customerservice.getOriginalid();
				BaseService originalbase = BaseService.findBaseService(originalid);
				log.debug("found baseservice");
				CustomPrice customprice = null;
				// CREATE A CUSTOMPRICE
				customprice = new CustomPrice();
				customprice.setCost(serviceprice);
				customprice.setService(originalbase);
				customprice.setAppointment(appointment);
				customprice.setShop(svc.getShop());
				customprice.setClient(appointment.getClient());
				// UPDATE CUSTOMPRICE
				customprice.merge();
				log.debug("updated customprice");
				// REMOVE CUSTOMSERVICE
				try{
					customerservice.remove();
				} catch (Exception e) {
					log.error(e);
				}
				
				log.debug("removed customservice");
			}else{
				// check to see if this is a basicservice type that needs to be customized
				log.debug("determine status of this basic service");
				log.debug("svc.getId(): "+svc.getId());
				log.debug("serviceid: "+serviceid);
				// or perhaps it is a different service that needs to be set?
				if(svc.getId()!=serviceid &&  svc.getId()!=serviceid){
					//set the service id to the new one
					log.debug("A new service has been selected");
					BaseService newservice = BaseService.findBaseService(serviceid);
					if(serviceprice != null && svc.getCost() != serviceprice){
						// TODO: since the price is different than create a new custom service or update this custom service?
						//newservice.setCost(serviceprice);
						//log.debug("changing price of service to "+serviceprice);
					}
					firstservice.remove(svc);
					firstservice.add(newservice);
					log.debug("Finished removing old service and adding new service");
				}
				// since there is only one service for the appointment we do not need to 
				// check to see if an existing customservice is available
				log.debug("TRYING TO UPDATE SERVICE PRICE");
				log.debug("svc.getId() "+svc.getId());
				log.debug("serviceid: "+serviceid);
				log.debug("serviceprice "+serviceprice);
				log.debug("svc.getCost() "+svc.getCost());
				log.debug("cancel "+cancel);
				log.debug("does getCost equals serviceprice? "+(Math.abs(svc.getCost() - serviceprice) < 0.00001));
				boolean pricecostsame = (Math.abs(svc.getCost() - serviceprice) < 0.00001);
				if(svc.getId()==serviceid && serviceprice != null && pricecostsame == false && (cancel != null && cancel != true) ){
					// INSTEAD OF CREATING A CUSTOMSERVICE CRUD CUSTOMPRICE
					log.debug("entered customprice creation");
					TypedQuery<CustomPrice> howmanycp = CustomPrice.findCustomPricesByShopAndAppointmentAndServiceAndClient(svc.getShop(),appointment,svc,appointment.getClient());
					int numcp = howmanycp.getResultList().size();
					CustomPrice customprice = null;
					if(numcp > 0){
						customprice = howmanycp.getResultList().get(0);
						customprice.setCost(serviceprice);
					}else{
						customprice = new CustomPrice();
						customprice.setCost(serviceprice);
						customprice.setService(svc);
						customprice.setAppointment(appointment);
						customprice.setShop(svc.getShop());
						customprice.setClient(appointment.getClient());
					}
					customprice.merge();
					log.debug("exiting customprice creation");
				}else{
					log.debug("entered customprice removal");
					TypedQuery<CustomPrice> howmanycp = CustomPrice.findCustomPricesByShopAndAppointmentAndServiceAndClient(svc.getShop(),appointment,svc,appointment.getClient());
					int numcp = howmanycp.getResultList().size();
					CustomPrice customprice = null;
					if(numcp > 0){
						customprice = howmanycp.getResultList().get(0);
						try{
							customprice.remove();
						} catch (Exception e) {
							log.error(e);
						}
						
					}
					
					log.debug("exiting customprice removal");
					
				}
			}
		}else{
			BaseService svc = BaseService.findBaseService(serviceid);
			Set<BaseService> bs = new HashSet<BaseService>();
			bs.add(svc);
			appointment.setServices(bs);			
		}
		// TODO: otherwise search thru list of services
		if(asz > 1){
			
		}
		if(appointment.getStatus() == ScheduleStatus.PENDING){
			// TODO: send an email to this client informing requested appointment has been accepted
			log.debug("SEND EMAIL TO CLIENT TO INFORM REQUESTED APPOINTMENT HAS BEEN ACCEPTED.");
			// change status to indicate acceptance
			appointment.setStatus(ScheduleStatus.ACTIVE); 
		}
		if(appointment.getStaff() != null){
			// update staff if necessary
			Long staff_id = Long.parseLong(staffid);
			Staff comparestaff = Staff.findStaff(staff_id);
			if(!appointment.getStaff().equals(comparestaff)){
				updateappt = true;
			}
		}
		if(updateappt == true){
			if(appointment.getStaff() != null){
				// update staff if necessary
				Long staff_id = Long.parseLong(staffid);
				Staff comparestaff = Staff.findStaff(staff_id);
				if(!appointment.getStaff().equals(comparestaff)){
					appointment.setStaff(comparestaff);
				}
			}
			if(request_indicator!=null){
				appointment.setRequested_image_name(request_indicator);
				appointment.setRequested_image_path(request_indicator);
			}
			// save changes
			appointment.merge();
			
			Appointment appointment2 = saveAppointment(appointment);
			Set<BaseService> bases = appointment2.getServices();
			
			// adding audit log
			String appointmentbegintime = "";
			SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm:ss a");
			appointmentbegintime = dateformatter.format(appointment.getBeginDateTime().getTime());
			
			SimpleDateFormat dateformatterappt = new SimpleDateFormat("MM/dd/yyyy");
			String originaldate = dateformatterappt.format(appointment.getAppointmentDate());
			String staffname = "";
			if(appointment.getStaff() != null){
				staffname = appointment.getStaff().getFirstName() + " " + appointment.getStaff().getLastName();
			}
			saveAuditMessage("Appointment for "+ originaldate+" "+ appointmentbegintime+" Client: "+appointment.getClient().getFirstName()+" "+appointment.getClient().getLastName()+" UPDATED by staff " +staffname,null,"GENERAL");
			// update future recurring appointments , minus the notes
			if(updatefuture.equalsIgnoreCase("true")){
				log.debug("update future appointments is set to true");
				TypedQuery<Appointment>  listtobedeleted = Appointment.findAppointmentsByShopAndAppointmentDateGreaterThanAndRecur_parentEquals(getShop(), appointment2.getAppointmentDate(), appointment2.getRecur_parent());
				if(listtobedeleted.getResultList().size()>0){
					BaseService svc = BaseService.findBaseService(serviceid);
					Set<BaseService> bs = new HashSet<BaseService>();
					bs.add(svc);

					for (Iterator it=listtobedeleted.getResultList().iterator(); it.hasNext(); ) {
						// looping to update future appointments
						log.debug("LOOPING TO UPDATE FUTURE APPOINTMENTS AS PART OF THIS RECURRING EVENT");
						Appointment element = (Appointment)it.next();
						// edit change to element. Staff, Begin time, end time, and services
						element.setStaff(appointment2.getStaff());
						element.setBeginDateTime(appointment2.getBeginDateTime());
						element.setEndDateTime(appointment2.getEndDateTime());
						log.debug("the appointment id that is to be updated is: "+element.getId());
						element.setServices(bs);
						Appointment dothis = element.merge();
						log.debug("saved again");
						Appointment element2 = saveAppointment(dothis);
						log.debug("saved appointment");
						// save audit message
						originaldate = dateformatterappt.format(element2.getAppointmentDate());
						appointmentbegintime = dateformatter.format(element2.getBeginDateTime().getTime());
						
						
						saveAuditMessage("Appointment for "+ originaldate+" "+ appointmentbegintime+" Client: "+element2.getClient().getFirstName()+" "+element2.getClient().getLastName()+" DELETED  by staff " +staffname,null,"GENERAL");
					}			 
				}else{
					log.debug("no future appointments were found even though this was set to true for updating recurring appointments");
				}
			}
			log.debug("merged or updated the appointment");
		}
		log.debug("the appointment date is: "+appointment.getAppointmentDate().toString());
		List<AppointmentDeep> appt = new ArrayList<AppointmentDeep>();
		AppointmentDeep deep = new AppointmentDeep(appointment);
		if(cancel != null && cancel == true){
			
		}else{
			//TODO: AT THIS POINT LOOK UP CUSTOMPRICE
			BaseService b = BaseService.findBaseService(deep.getService1id());
			if(b != null){
				TypedQuery<CustomPrice> tcp = CustomPrice.findCustomPricesByShopAndAppointmentAndServiceAndClient(getShop(), appointment, b, deep.getClient());
				List<CustomPrice> lcp = tcp.getResultList();
				if(lcp.size() > 0){
					log.debug("Found custom price for this appointment: "+lcp.get(0).getCost());
					deep.setService1cost(""+lcp.get(0).getCost());
				}
			}
			
			appt.add(deep);
		}
		//send out email reminder only if the client has indicated they want to receive the email
		log.debug("CAN I SEND A TEXT MESSAGE WITHOUT HAVING TO HAVE THE EMAIL NOTIFICATION ENABLED???? ");
		log.debug("appointment.getClient().getAccepts_sms_initial(): " + appointment.getClient().getAccepts_sms_initial());
		log.debug("appointment.getClient().getAccepts_initial(): " + appointment.getClient().getAccepts_initial());
		log.debug("appointment.getStatus(): " + appointment.getStatus());
		log.debug("ischeckout: " +ischeckout);
		if((appointment.getClient().getAccepts_sms_initial() == true || appointment.getClient().getAccepts_initial() == true) && appointment.getStatus() != ScheduleStatus.CHECKED_OUT && ischeckout == false){
			log.debug("!!!!! FIRE IN THE HOLE.  SENDING A MESSAGE !!!!!!!! ");
			// prevents two messages being sent out on a checkout
			sendMessage(appointment);
		}
		
		String jsonarray = AppointmentDeep.toJsonArray(appt);
		log.debug("EXITING THE updateEditAppt IN MYSCHEDULE FOR A AJAX CALL");
		return jsonarray;
	}
	/**
	 * When click the Agenda the appointmentid is passed to the parameter
	 * and this looks up that appointment and returns it to be displayed
	 * in the Edit Appointment dialog
	 * 
	 * @param apptid
	 * @return
	 */
	@RequestMapping(value="/appt", method=RequestMethod.GET)
	public @ResponseBody  String getAppt(@RequestParam(value="apptid") String apptid) {
		// get the appointment by id
		log.debug("ENTERED getAppt GET");
		Appointment appointment = Appointment.findAppointment(Long.parseLong(apptid));
		log.debug("the appointment date is: "+appointment.getAppointmentDate().toString());
		List<AppointmentDeep> appt = new ArrayList<AppointmentDeep>();
		AppointmentDeep deep = new AppointmentDeep(appointment);
		//TODO: AT THIS POINT LOOK UP CUSTOMPRICE
		BaseService b = BaseService.findBaseService(deep.getService1id());
		if(b != null){
			TypedQuery<CustomPrice> tcp = CustomPrice.findCustomPricesByShopAndAppointmentAndServiceAndClient(getShop(), appointment, b, deep.getClient());
			List<CustomPrice> lcp = tcp.getResultList();
			if(lcp.size() > 0){
				log.debug("Found custom price for this appointment: "+lcp.get(0).getCost());
				deep.setService1cost(""+lcp.get(0).getCost());
			}
		}
		
		appt.add(deep);
		String jsonarray = AppointmentDeep.toJsonArray(appt);
		log.debug("EXITING getAppt GET");
		return jsonarray;
	}
	/**
	 * Set the status of appointment to CANCELED
	 * 
	 * @param apptid
	 * @return
	 */
	@RequestMapping(value="/cancel", method=RequestMethod.GET)
	public @ResponseBody  String cancelAppt(@RequestParam(value="apptid") String apptid) {
		// get the appointment by id
		log.debug("ENTERED cancelAppt GET");
		Appointment appointment = Appointment.findAppointment(Long.parseLong(apptid));
		appointment.setStatus(ScheduleStatus.CANCELED);
		appointment = saveAppointment(appointment);
		
		log.debug("the appointment has been cancelled: ");
		String jsonarray = "SUCCESS";
		String originaldate = "";
		
		String appointmentbegintime = "";
		SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm:ss a");
		appointmentbegintime = dateformatter.format(appointment.getBeginDateTime().getTime());
		
		SimpleDateFormat dateformatterappt = new SimpleDateFormat("MM/dd/yyyy");
		originaldate = dateformatterappt.format(appointment.getAppointmentDate());
		String staffname = "";
		if(appointment.getStaff() != null){
			staffname = appointment.getStaff().getFirstName() + " " + appointment.getStaff().getLastName();
		}
		saveAuditMessage("Appointment for "+ originaldate+" "+ appointmentbegintime+" Client: "+appointment.getClient().getFirstName()+" "+appointment.getClient().getLastName()+" CANCELLED by staff " +staffname,null,"GENERAL");
		log.debug("EXITING cancelAppt GET");
		return jsonarray;
	}

	@RequestMapping(value="/cancelmobile", method=RequestMethod.GET)
	public ModelAndView cancelApptMobile(
    		Model uiModel,
			@RequestParam(value="appointmentid") String apptid,
			HttpServletRequest httpServletRequest
			) {
		// get the appointment by id
		log.debug("ENTERED cancelApptMobile");
		ModelAndView mav = new ModelAndView();
		Appointment appointment = Appointment.findAppointment(Long.parseLong(apptid));
		appointment.setStatus(ScheduleStatus.CANCELED);
		appointment = saveAppointment(appointment);
		
		// adding audit log
		String appointmentbegintime = "";
		SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm:ss a");
		appointmentbegintime = dateformatter.format(appointment.getBeginDateTime().getTime());
		
		SimpleDateFormat dateformatterappt = new SimpleDateFormat("MM/dd/yyyy");
		String originaldate = dateformatterappt.format(appointment.getAppointmentDate());
		String staffname = "";
		if(appointment.getStaff() != null){
			staffname = appointment.getStaff().getFirstName() + " " + appointment.getStaff().getLastName();
		}
		saveAuditMessage("Appointment for "+ originaldate+" "+ appointmentbegintime+" Client: "+appointment.getClient().getFirstName()+" "+appointment.getClient().getLastName()+" CANCELLED  by staff " +staffname,null,"GENERAL");
		
		log.debug("EXITING cancelApptMobile");
		return mav;
	}	
	/**
	 * Set the status of appointment to NO_SHOW
	 * @param apptid
	 * @return
	 */
	@RequestMapping(value="/noshow", method=RequestMethod.GET)
	public @ResponseBody  String noShowAppt(@RequestParam(value="apptid") String apptid) {
		// get the appointment by id
		log.debug("ENTERED noShowAppt GET");
		Appointment appointment = Appointment.findAppointment(Long.parseLong(apptid));
		appointment.setStatus(ScheduleStatus.NO_SHOW);
		appointment = saveAppointment(appointment);
		// adding audit log
		String appointmentbegintime = "";
		SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm:ss a");
		appointmentbegintime = dateformatter.format(appointment.getBeginDateTime().getTime());
		
		SimpleDateFormat dateformatterappt = new SimpleDateFormat("MM/dd/yyyy");
		String originaldate = dateformatterappt.format(appointment.getAppointmentDate());
		String staffname = "";
		if(appointment.getStaff() != null){
			staffname = appointment.getStaff().getFirstName() + " " + appointment.getStaff().getLastName();
		}
		saveAuditMessage("Appointment for "+ originaldate+" "+ appointmentbegintime+" Client: "+appointment.getClient().getFirstName()+" "+appointment.getClient().getLastName()+" NOSHOW  by staff " +staffname,null,"GENERAL");
		
		log.debug("the appointment has been noshow ");
		String jsonarray = "SUCCESS";
		log.debug("EXITING noShowAppt GET");
		return jsonarray;
	}

	/**
	 * Set the status of appointment to DELETED
	 * @param apptid
	 * @return
	 */
	@RequestMapping(value="/delete", method=RequestMethod.GET)
	public @ResponseBody  String deleteAppt(
			@RequestParam(value="apptid") String apptid,
			@RequestParam(value="df" ,required=false) String deletefuture
			) {
		// get the appointment by id
		log.debug("ENTERED deleteAppt GET");
		log.debug("apptid: " + apptid);
		String jsonarray = "";
		Appointment appointment = Appointment.findAppointment(Long.parseLong(apptid));
		if(appointment != null){
			Date getdate = appointment.getAppointmentDate();
			long getparent = 0L;
			if(appointment.getRecur_parent() != null){
				getparent = appointment.getRecur_parent();	
			}
			
			appointment.setStatus(ScheduleStatus.DELETED);
			appointment = saveAppointment(appointment);
			// adding audit log
			String appointmentbegintime = "";
			SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm:ss a");
			appointmentbegintime = dateformatter.format(appointment.getBeginDateTime().getTime());
			
			SimpleDateFormat dateformatterappt = new SimpleDateFormat("MM/dd/yyyy");
			String originaldate = dateformatterappt.format(appointment.getAppointmentDate());
			String staffname = "";
			if(appointment.getStaff() != null){
				staffname = appointment.getStaff().getFirstName() + " " + appointment.getStaff().getLastName();
			}
			saveAuditMessage("Appointment for "+ originaldate+" "+ appointmentbegintime+" Client: "+appointment.getClient().getFirstName()+" "+appointment.getClient().getLastName()+" DELETED  by staff " +staffname,null,"GENERAL");
			
	
			if(appointment.getRecur_parent() != null && deletefuture.equalsIgnoreCase("true")){
				// get list of all future appointments and delete them too
				 TypedQuery<Appointment>  listtobedeleted = Appointment.findAppointmentsByShopAndAppointmentDateGreaterThanAndRecur_parentEquals(getShop(), getdate, getparent);
				 if(listtobedeleted.getResultList().size()>0){
					for (Iterator it=listtobedeleted.getResultList().iterator(); it.hasNext(); ) {
						Appointment element = (Appointment)it.next();
						element.setStatus(ScheduleStatus.DELETED);
						originaldate = dateformatterappt.format(element.getAppointmentDate());
						appointmentbegintime = dateformatter.format(element.getBeginDateTime().getTime());
						Appointment element2 = saveAppointment(element);
						
						saveAuditMessage("Appointment for "+ originaldate+" "+ appointmentbegintime+" Client: "+element2.getClient().getFirstName()+" "+element2.getClient().getLastName()+" DELETED  by staff " +staffname,null,"GENERAL");
					}			 
				 }
			}
			
			log.debug("the appointment has been deleteAppt: ");
			jsonarray = "SUCCESS";
		}else{
			log.debug("the appointment has NOT been deleteAppt");
			jsonarray = "FAILURE";
		}
		log.debug("EXITING deleteAppt GET");
		return jsonarray;
	}
	
	/**
	 * This is where the appointment is checked out and payment is processed. Very like the 
	 * update Edit method but with the addition of Payment.
	 * 
	 * @param apptid
	 * @return
	 */
	@RequestMapping(value="/checkout", method=RequestMethod.GET)
	public @ResponseBody  String checkOutAppt(@RequestParam(value="apptid") String apptid) {
		// get the appointment by id
		log.debug("ENTERED checkOutAppt GET");
		Appointment appointment = Appointment.findAppointment(Long.parseLong(apptid));
		Float fullamount = 0.00F;
		
		Set<BaseService> services = appointment.getServices();
		Set<PaymentsService> payservices = new HashSet<PaymentsService>();
		log.debug("computing full amount");
		for (Iterator it=services.iterator(); it.hasNext(); ) {
			
			BaseService element = (BaseService)it.next();
			if(element != null && element.getCost() != null){
				TypedQuery<CustomPrice> howmanycp = CustomPrice.findCustomPricesByShopAndAppointmentAndService(getShop(), appointment, element);
				int numcp = howmanycp.getResultList().size();
				float customcost = 0.0F;
				if (numcp > 0) {
					CustomPrice  customprice = howmanycp.getResultList().get(0);
					customcost = customprice.getCost();
				}else{
					customcost = element.getCost();
				}
				
				PaymentsService payservice = new PaymentsService();
				payservice.setDescription(element.getDescription());
				payservice.setAmount(customcost);
				payservices.add(payservice);
				
				fullamount = fullamount + customcost;
				
			}
		}		
		log.debug("full amount computed: "+fullamount);
		
		Payments makepay = new Payments();
		makepay.setTax(0.00F);
		makepay.setNote("checkout appointment");
		makepay.setGratuity(0.00F);
		makepay.setAmount(fullamount);
		if(appointment != null){
			log.debug("appointment was not null");
			makepay.setAppointment(appointment);
		}
		if(appointment.getClient()!= null){
			log.debug("client was not null");
			makepay.setClient(appointment.getClient());
		}
		if(getShop() != null){
			log.debug("getShop() was not null");
			makepay.setShop(getShop());
		}
		if(payservices != null){
			log.debug("payservices was not null");
			makepay.setPaymentsservice(payservices);
		}else{
			log.debug("!!!! payservices WAS NULL !!!!!!!");
		}
		
		Set<PaymentsType> paytypes = new HashSet<PaymentsType>();
		PaymentsType pt = new PaymentsType();
		pt.setAmount(fullamount);
		pt.setType(PaymentTypes.CASH);
		paytypes.add(pt);
		
		makepay.setPaymentstype(paytypes);
		TypedQuery<Staff> staffs = Staff.findStaffsByShop(getShop());
		if(staffs.getResultList().size() > 0){
			Staff staff = staffs.getResultList().get(0);
			if(staff != null){
				log.debug("staff was not null");
				makepay.setStaff(staff);
			}
		}
		makepay.setDatecreated(new Date());
		makepay.merge();
		log.debug("payment saved ");
		
		appointment.setStatus(ScheduleStatus.CHECKED_OUT);
		try{
			log.debug("check out status is: "+appointment.getStatus());
			appointment.merge();
			appointment = saveAppointment(appointment);
			// adding audit log
			String appointmentbegintime = "";
			SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm:ss a");
			appointmentbegintime = dateformatter.format(appointment.getBeginDateTime().getTime());
			
			SimpleDateFormat dateformatterappt = new SimpleDateFormat("MM/dd/yyyy");
			String originaldate = dateformatterappt.format(appointment.getAppointmentDate());
			String staffname = "";
			if(appointment.getStaff() != null){
				staffname = appointment.getStaff().getFirstName() + " " + appointment.getStaff().getLastName();
			}
			saveAuditMessage("Appointment for "+ originaldate+" "+ appointmentbegintime+" Client: "+appointment.getClient().getFirstName()+" "+appointment.getClient().getLastName()+" CHECKOUT by staff " +staffname,null,"GENERAL");
			// SEND RECEIPT TO CLIENT
			if(appointment.getClient().getAccepts_receipts()==true){
				sendReceiptMessage(appointment);
			}
			if(appointment.getClient().getAccepts_sms_receipts()){
				log.debug("About to send a text message receipt");
				Long number_of_sms_available = getShop().getNumber_sms_purchased();
				if(number_of_sms_available != null && number_of_sms_available > 0L){
					log.debug("before sending to sendSMSReceiptMessage shop_name: "+getShop().getShop_name());
					sendSMSReceiptMessage(appointment);
					log.debug("after sending to sendSMSReceiptMessage");
				}else{
					saveAuditMessage("Text messaging receipt is not available for appointment for "+ originaldate+" time: "+ appointmentbegintime+" client: "+appointment.getClient().getFirstName()+" "+appointment.getClient().getLastName()+" by staff " +staffname,null,"TEXT MESSAGE");					
				}
				
			}
		}catch(Exception e){
			log.debug("EXCEPTION: "+e.getMessage());
			makepay.remove();	
		}
		log.debug("the appointment has been checkout ");
		String jsonarray = "SUCCESS";
		log.debug("EXITING checkOutAppt GET");
		return jsonarray;
	}
	/**
	 * precursor to actually sending out the receipt sms message to the que
	 * 
	 * @param appt
	 */
	private void sendSMSReceiptMessage(Appointment appt){
		String shop_phonenumber = "";
		try{
			log.debug("ENTERED sendSMSReceiptMessage");
			TypedQuery<ShopSettings> receipt_shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings receipt_shopsetting = receipt_shopsettings.getResultList().get(0);
			
//			DO TAG REPLACEMENTS VIA STRING SEARCH AND REPLACE FOR THE FOLLOWING TAGS
			String client_firstname = "";
			String client_lastname = "";
			String client_fullname = "";
			String staff_firstname = "";
			String staff_lastname = "";
			String staff_fullname = "";
			
			boolean do_sms= false;
			
			if(appt.getClient() != null){
				
				do_sms = appt.getClient().getAccepts_sms_receipts();
				
				client_firstname = appt.getClient().getFirstName();
				client_lastname = appt.getClient().getLastName();
				
				client_fullname = client_firstname+" "+client_lastname;
			
			}
			if(appt.getStaff() != null){
				staff_firstname = appt.getStaff().getFirstName();
				staff_lastname = appt.getStaff().getLastName();
				staff_fullname = staff_firstname+" "+staff_lastname;
			}
			if(appt.getServices() != null){
				Set<BaseService> setsvcs = appt.getServices();
				for (Iterator it=setsvcs.iterator(); it.hasNext(); ) {
					BaseService bservice = (BaseService)it.next();
					String servicename = bservice.getDescription();
					String serviceprice = ""+bservice.getCost();
					
					double money = bservice.getCost();
					NumberFormat formatter = NumberFormat.getCurrencyInstance();
					String moneyString = Matcher.quoteReplacement(formatter.format(money));
				}				
				
				
			}			
			AppointmentNotification an = new AppointmentNotification();
			an.setAppointmentid(appt.getId());
			an.setAppointmentdate(appt.getAppointmentDate());
			
			String clientname = appt.getClient().getFirstName() + " " + appt.getClient().getLastName(); 
			an.setClientname(clientname);
			
			String cell_phone = getCellPhoneFromClient(appt.getClient());
			log.debug("cell phone of client to send the receipt to: " + cell_phone);
			an.setShop_sms_to(cell_phone);
			
			
			String client_email = "";
			List<Communications> comm = Communications.findCommunicationsesByPerson(appt.getClient()).getResultList();
			int sz = comm.size();
			log.debug("the size of number of comms is: "+ sz);
			for(int x=0;x<sz;x++){
				log.debug("looping to update comms");
				Communications com = comm.get(x);
				String comval = com.getCommunication_value();
				
				if(com.getCommunication_type().name().equalsIgnoreCase(CommType.EMAIL.name())){
					log.debug("email");
					client_email  = com.getCommunication_value();
				}
			}
			
//			if(client_email != null && !client_email.equalsIgnoreCase("")){
				an.setEmail(client_email);
				String shopname = getShop().getShop_name();
	
				
				TypedQuery<ShopSettings> settings = ShopSettings.findShopSettingsesByShop(getShop());
				String email_shop = "registration@scheduleem.com";
				if(settings.getResultList().get(0).getEmail_address() != null){
					email_shop = settings.getResultList().get(0).getEmail_address();
				}
				an.setFromshop(shopname);
				
				// ********************************************
				String today = "";
				String shoptimezone = "";
				try{
					TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
					ShopSettings shopsetting = shopsettings.getResultList().get(0);
					shoptimezone = shopsetting.getTimezone();
					shop_phonenumber = shopsetting.getStore_phone();
					if(shoptimezone == null){
						shoptimezone = "MST";
						shopsetting.setTimezone(shoptimezone);
						shopsetting.merge();
					}

				}catch(Exception e){
					log.error(e);
				}
				TimeZone tz = TimeZone.getTimeZone(shoptimezone);
				Date dateValue = appt.getAppointmentDate();
				Calendar calValue = Calendar.getInstance(tz);
				calValue.setTime(dateValue);
				
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
				String dateString = df.format(calValue.getTime());
				today = dateString;
				
				String appointmentbegintime = "";
				SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm a");
				appointmentbegintime = dateformatter.format(appt.getBeginDateTime().getTime());
				
				// ********************************************

				String notificication_receipt = "receipt";
				Long number_of_sms_available = getShop().getNumber_sms_purchased();
				if(number_of_sms_available != null && number_of_sms_available > 0L){
					log.debug("before sending to sendSMSToQue shop_name: "+getShop().getShop_name());
					sendSMSTextMessage(notificication_receipt,appt, shop_phonenumber, staff_fullname,do_sms, an);
					log.debug("after sending to sendSMSToQue");
				}else{
					saveAuditMessage("Text messaging receipt is not available for appointment for "+ today+" time: "+ appointmentbegintime+" client: "+appt.getClient().getFirstName()+" "+appt.getClient().getLastName()+" by staff " +staff_fullname,null,"TEXT MESSAGE");					
				}
				
//			}else{
//				log.debug("A RECEIPT EMAIL MESSAGE CANNOT BE DELIVERED TO "+clientname+" BECAUSE THEY HAVE NO EMAIL ADDRESS!!");
//			}
		}catch(Exception e){
			log.error(e.getMessage());
			
		}	
		log.debug("Exiting sendSMSReceiptMessage");
	}

	/**
	 * Determine if a CustomService exists and if so then remove it and create a CustomPrice instead
	 * 
	 * @param apptdeep
	 * @return
	 */
	private List<AppointmentDeep> setCustomPrice(List<AppointmentDeep> apptdeep) {
		log.debug("ENTERED setCustomPrice");
		int sz = apptdeep.size();
		log.debug("NUMBER OF APPOINTMENTS: " + sz);
		// now process the rest of the available custom services for each appointment
		for (int z = 0; z < sz; z++) {
			log.debug("ENTERED GENERAL SEARCH");
			AppointmentDeep deep2 = (AppointmentDeep) apptdeep.get(z);
			log.debug("This appointment description is: "+deep2.getDescription());
			log.debug("This appointment id is: "+deep2.getId());
			log.debug("This appointment Service1cost is: "+deep2.getService1cost());
			Appointment appointment = Appointment.findAppointment(Long.parseLong(deep2.getId()));
			Set<BaseService> list = appointment.getServices();
			
			Clients client = appointment.getClient();
			
			for (Iterator i = list.iterator(); i.hasNext();) {
				BaseService base = (BaseService) i.next();
				float serviceprice = base.getCost();
				
				if(appointment.getClient() == null) continue;
				
				TypedQuery<CustomPrice> howmanycp = CustomPrice.findCustomPricesByShopAndAppointmentAndClient(getShop(), appointment, appointment.getClient());
				int numcp = howmanycp.getResultList().size();
				if (numcp > 0) {
					CustomPrice  customprice = howmanycp.getResultList().get(0);
					deep2.setService1cost(""+customprice.getCost());
					log.debug("ASSIGNED A PRICE TO Service1cost FROM customprice.getCost(): "+customprice.getCost());
				}
			}
		}		
		log.debug("EXITING setCustomPrice");
		return apptdeep;
	}

	/**
	 * When the day on the Calendar is selected  that date is passed here
	 * so that the Day's Agenda can be returned.
	 *  
	 * @param selecteddate
	 * @return
	 */
	@RequestMapping(value="/agenda", method=RequestMethod.GET)
	public @ResponseBody  String getAgenda(@RequestParam(value="date") String selecteddate) {
		// get all of the appointments for today
		log.debug("ENTERED getAgenda GET");
		log.debug("the selected date for todays appointments is: "+selecteddate);
		List<AppointmentDeep> apptdeep = getTodaysAppointments(selecteddate);
		Collections.sort(apptdeep,
								 new Comparator<AppointmentDeep>() {
								
											@Override
											public int compare(AppointmentDeep e1, AppointmentDeep e2) {
												// the wizardy with Calendars below is to make
												// sure the Dates are the same for Month, day, year
												log.debug("comparing times");
												Calendar cal1 = e1.getBeginDateTime();
												log.debug("cal1 date: "+cal1.get(Calendar.DATE));
												log.debug("cal1 month: "+cal1.get(Calendar.MONTH));
												log.debug("cal1 year: "+cal1.get(Calendar.YEAR));
												Calendar cal2 = e2.getBeginDateTime();
												log.debug("cal2 date: "+cal2.get(Calendar.DATE));
												log.debug("cal2 month: "+cal2.get(Calendar.MONTH));
												log.debug("cal2 year: "+cal2.get(Calendar.YEAR));
												
												Calendar tmpcal = Calendar.getInstance();
												log.debug("tmpcal date: "+tmpcal.get(Calendar.DATE));
												log.debug("tmpcal month: "+tmpcal.get(Calendar.MONTH));
												log.debug("tmpcal year: "+tmpcal.get(Calendar.YEAR));
												
												tmpcal.setTime(e1.getAppointmentDate());
												
												
												cal1.set(Calendar.DATE,tmpcal.get(Calendar.DATE));
												cal1.set(Calendar.MONTH,tmpcal.get(Calendar.MONTH));
												cal1.set(Calendar.YEAR,tmpcal.get(Calendar.YEAR));
												e1.setBeginDateTime(cal1);
												
												cal2.set(Calendar.DATE,tmpcal.get(Calendar.DATE));
												cal2.set(Calendar.MONTH,tmpcal.get(Calendar.MONTH));
												cal2.set(Calendar.YEAR,tmpcal.get(Calendar.YEAR));
												
												e2.setBeginDateTime(cal2);
												log.debug("revised cal1 date: "+cal1.get(Calendar.DATE));
												log.debug("revised cal1 month: "+cal1.get(Calendar.MONTH));
												log.debug("revised cal1 year: "+cal1.get(Calendar.YEAR));
												log.debug("revised cal2 date: "+cal2.get(Calendar.DATE));
												log.debug("revised cal2 month: "+cal2.get(Calendar.MONTH));
												log.debug("revised cal2 year: "+cal2.get(Calendar.YEAR));
												return (int) (e1.getBeginDateTime().compareTo(e2.getBeginDateTime()));
											}
										}				
				);
		List<AppointmentDeep> toodeep = setCustomPrice(apptdeep);
		String jsonarray = AppointmentDeep.toJsonArray(toodeep);
		log.debug("EXITING getAgenda GET");
		return jsonarray;
	}
	
	@RequestMapping(value="/calendar", method=RequestMethod.GET)
	public @ResponseBody  String getCalendarAppointments(@RequestParam(value="date") String selecteddate,@RequestParam(value="enddate") String enddate) {
		// get all of the appointments for today
		log.debug("ENTERED getCalendarAppointments GET");
		log.debug("selecteddate: "+selecteddate);
		log.debug("enddate: "+enddate);
		List<AppointmentDeep> apptdeep = getAppointments(selecteddate,enddate);
		Iterator itr = apptdeep.iterator();
		
		while (itr.hasNext()) {
			AppointmentDeep a = (AppointmentDeep) itr.next();
			if(a.getClient() != null){
				Clients c = Clients.findClients(a.getClient().getId());
				Set<Communications> comm = c.getCommunication();
				int sz = comm.size();
				log.debug("the size of number of comms is: "+ sz);
				for (Iterator it=comm.iterator(); it.hasNext(); ) {
					Communications element = (Communications)it.next();
					String comval = element.getCommunication_value();
					if(comval!=null){
						if(element.getCommunication_type().name().equalsIgnoreCase(CommType.CELL_PHONE.name())){
							log.debug("cell phone" + comval);
							a.setClientworkphonenumber(comval);
						}
						if(element.getCommunication_type().name().equalsIgnoreCase(CommType.WORK_PHONE.name())){
							log.debug("work phone" + comval);
							a.setClientcellphonenumber(comval);
						}
						if(element.getCommunication_type().name().equalsIgnoreCase(CommType.HOME_PHONE.name())){
							log.debug("home phone" + comval);
							a.setClientphonenumber(comval);
						}
					}
				}		
			}
			
		}        
		
		Collections.sort(apptdeep);
		String jsonarray = AppointmentDeep.toJsonArray(apptdeep);
		log.debug("EXITING getCalendarAppointments GET");
		return jsonarray;
	}

	@RequestMapping(value="/calendarstaff", method=RequestMethod.GET)
	public @ResponseBody  String getCalendarAppointmentsStaff(
				@RequestParam(value="staff") String staff,
				@RequestParam(value="date") String selecteddate,
				@RequestParam(value="enddate") String enddate
				) {
		// get all of the appointments for today
		log.debug("ENTERED getCalendarAppointments GET");
		log.debug("selecteddate: "+selecteddate);
		log.debug("enddate: "+enddate);
		List<AppointmentDeep> apptdeep = getAppointmentsStaff(staff,selecteddate,enddate);
		Collections.sort(apptdeep);
		String jsonarray = AppointmentDeep.toJsonArray(apptdeep);
		log.debug("EXITING getCalendarAppointments GET");
		return jsonarray;
	}
	
	private List getTodaysAppointments( String selecteddate){
		log.debug("ENTERED getTodaysAppointments ");
		// get all of the appointments for today
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		//df.setTimeZone(TimeZone.getTimeZone("MST"));
		Date beginofday = null;
		Date endofday = null;
		
		try {
			if(selecteddate == null || selecteddate.equalsIgnoreCase("")){
				String dateString = df.format(new Date());
				endofday = df.parse(dateString);
				beginofday = df.parse(dateString);
			}else{
				endofday = df.parse(selecteddate);
				beginofday = df.parse(selecteddate);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		beginofday.setHours(0);
		beginofday.setMinutes(0);
		beginofday.setSeconds(0);
		log.debug("the value of beginofday: "+beginofday);
		
		endofday.setHours(23);
		endofday.setMinutes(59);
		endofday.setSeconds(0);
		log.debug("the value of endofday: "+endofday);
		log.debug("the shop is: "+getShop().toString());
		//TypedQuery<Appointment> appointments = Appointment.findAppointmentsByShopAndAppointmentDateBetween(getShop(), beginofday, endofday);
		//TypedQuery<Appointment> appointments = Appointment.findAppointmentsByShopAndCancelledNotAndAppointmentDateBetween(getShop(),true, beginofday, endofday);
		TypedQuery<Appointment> appointments = Appointment.findAppointmentsByShopAndStatusNotAndAppointmentDateBetween(getShop(), ScheduleStatus.DELETED, beginofday, endofday);
		log.debug("getTodaysAppointments -- the number of appointments is: "+appointments.getResultList().size());
		List<AppointmentDeep> apptdeep = new ArrayList<AppointmentDeep>();
		if(appointments.getResultList().size()>0){
			for(int x=0;x<appointments.getResultList().size();x++){
				Appointment appt = appointments.getResultList().get(x);
				log.debug("APPOINTMENT id: "+appt.getId());
				//log.debug("APPOINTMENT CLIENT NAME: "+appt.getClient().getFirstName()+ " " +appt.getClient().getLastName());
				if(appt.getStatus() != ScheduleStatus.CANCELED){
					log.debug("The toString of appt: "+ appt.toString());
					AppointmentDeep deep = new AppointmentDeep(appt);
					log.debug("The toString of DEEP appt: "+ deep.toString());
					//TODO: AT THIS POINT LOOK UP CUSTOMPRICE
					BaseService b = BaseService.findBaseService(deep.getService1id());
					if(b != null){
						if(deep.getClient() == null) continue;
						TypedQuery<CustomPrice> tcp = CustomPrice.findCustomPricesByShopAndAppointmentAndServiceAndClient(getShop(), appt, b, deep.getClient());
						List<CustomPrice> lcp = tcp.getResultList();
						if(lcp.size() > 0){
							log.debug("Found custom price for this appointment: "+lcp.get(0).getCost());
							deep.setService1cost(""+lcp.get(0).getCost());
						}
					}
	
					apptdeep.add(deep);
				}else{
					log.debug("CANCELLED APPOINTMENT ");
				}
			}
			
		}
		log.debug("EXITING getTodaysAppointments ");
		return apptdeep;
	}
	
	private List getAppointments( String selecteddate, String enddate){
		// get all of the appointments for today
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date beginofday = null;
		Date endofday = null;
		
		try {
			if(selecteddate == null || selecteddate.equalsIgnoreCase("")){
				String dateString = df.format(new Date());
				endofday = df.parse(dateString);
				beginofday = df.parse(dateString);
			}else{
				endofday = df.parse(enddate);
				beginofday = df.parse(selecteddate);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		beginofday.setHours(0);
		beginofday.setMinutes(0);
		beginofday.setSeconds(0);
		log.debug("the value of beginofday: "+beginofday);
		
		endofday.setHours(23);
		endofday.setMinutes(59);
		endofday.setSeconds(0);
		log.debug("the value of endofday: "+endofday);
		
		
		
		
		
		log.debug("the shop is: "+getShop().toString());
//		TypedQuery<Appointment> appointments = Appointment.findAppointmentsByShopAndAppointmentDateBetween(getShop(), beginofday, endofday);
		//TypedQuery<Appointment> appointments = Appointment.findAppointmentsByShopAndCancelledNotAndAppointmentDateBetween(getShop(),true, beginofday, endofday);
		TypedQuery<Appointment> appointments = Appointment.findAppointmentsByShopAndStatusNotAndAppointmentDateBetween(getShop(), ScheduleStatus.DELETED , beginofday, endofday);
		log.debug("getTodaysAppointments -- the number of appointments is: "+appointments.getResultList().size());
		List<AppointmentDeep> apptdeep = new ArrayList<AppointmentDeep>();
		if(appointments.getResultList().size()>0){
			for(int x=0;x<appointments.getResultList().size();x++){
				Appointment appt = appointments.getResultList().get(x);
				log.debug("APPOINTMENT ID: "+appt.getId());
//				log.debug("APPOINTMENT CLIENT NAME: "+appt.getClient().getFirstName()+ " " +appt.getClient().getLastName());
				if(appt.getStatus() != ScheduleStatus.CANCELED){
					log.debug("BEFORE CONVERTING TO APPOINTMENTDEEP appt.getBeginDateTime(): "+appt.getBeginDateTime().getTime().toString()+" appointment date: "+appt.getAppointmentDate().toString());
					AppointmentDeep deep = new AppointmentDeep(appt);
					//TODO: AT THIS POINT LOOK UP CUSTOMPRICE
					BaseService b = BaseService.findBaseService(deep.getService1id());
					if(b != null){
						if(deep.getClient() == null) continue;
						TypedQuery<CustomPrice> tcp = CustomPrice.findCustomPricesByShopAndAppointmentAndServiceAndClient(getShop(), appt, b, deep.getClient());
						List<CustomPrice> lcp = tcp.getResultList();
						if(lcp.size() > 0){
							log.debug("Found custom price for this appointment: "+lcp.get(0).getCost());
							deep.setService1cost(""+lcp.get(0).getCost());
						}
					}
	
					log.debug("AFTER CONVERTING TO APPOINTMENTDEEP deep.getBeginDateTime()"+deep.getBeginDateTime());
					apptdeep.add(deep);
				}else{
					log.debug("IGNORED A CANCELLED APPOINTMENT");
				}
			}
			
		}
		return apptdeep;
	}
	
	private List getAppointmentsDynamic( String selecteddate, String enddate, String formatstring){
		// get all of the appointments for today
		DateFormat df = new SimpleDateFormat(formatstring);
		Date beginofday = null;
		Date endofday = null;
		
		try {
			if(selecteddate == null || selecteddate.equalsIgnoreCase("")){
				String dateString = df.format(new Date());
				endofday = df.parse(dateString);
				beginofday = df.parse(dateString);
			}else{
				endofday = df.parse(enddate);
				beginofday = df.parse(selecteddate);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		beginofday.setHours(0);
		beginofday.setMinutes(0);
		beginofday.setSeconds(0);
		log.debug("the value of beginofday: "+beginofday);
		
		endofday.setHours(23);
		endofday.setMinutes(59);
		endofday.setSeconds(0);
		log.debug("the value of endofday: "+endofday);
		
		
		
		
		
		log.debug("the shop is: "+getShop().toString());
//		TypedQuery<Appointment> appointments = Appointment.findAppointmentsByShopAndAppointmentDateBetween(getShop(), beginofday, endofday);
		//TypedQuery<Appointment> appointments = Appointment.findAppointmentsByShopAndCancelledNotAndAppointmentDateBetween(getShop(),true, beginofday, endofday);
		TypedQuery<Appointment> appointments = Appointment.findAppointmentsByShopAndStatusNotAndAppointmentDateBetween(getShop(), ScheduleStatus.DELETED , beginofday, endofday);
		log.debug("getTodaysAppointments -- the number of appointments is: "+appointments.getResultList().size());
		List<AppointmentDeep> apptdeep = new ArrayList<AppointmentDeep>();
		if(appointments.getResultList().size()>0){
			for(int x=0;x<appointments.getResultList().size();x++){
				Appointment appt = appointments.getResultList().get(x);
				log.debug("APPOINTMENT ID: "+appt.getId());
//				log.debug("APPOINTMENT CLIENT NAME: "+appt.getClient().getFirstName()+ " " +appt.getClient().getLastName());
				if(appt.getStatus() != ScheduleStatus.CANCELED){
					log.debug("BEFORE CONVERTING TO APPOINTMENTDEEP appt.getBeginDateTime(): "+appt.getBeginDateTime().getTime().toString()+" appointment date: "+appt.getAppointmentDate().toString());
					AppointmentDeep deep = new AppointmentDeep(appt);
					//TODO: AT THIS POINT LOOK UP CUSTOMPRICE
					BaseService b = BaseService.findBaseService(deep.getService1id());
					if(b != null){
						if(deep.getClient() == null) continue;
						TypedQuery<CustomPrice> tcp = CustomPrice.findCustomPricesByShopAndAppointmentAndServiceAndClient(getShop(), appt, b, deep.getClient());
						List<CustomPrice> lcp = tcp.getResultList();
						if(lcp.size() > 0){
							log.debug("Found custom price for this appointment: "+lcp.get(0).getCost());
							deep.setService1cost(""+lcp.get(0).getCost());
						}
					}
	
					log.debug("AFTER CONVERTING TO APPOINTMENTDEEP deep.getBeginDateTime()"+deep.getBeginDateTime());
					apptdeep.add(deep);
				}else{
					log.debug("IGNORED A CANCELLED APPOINTMENT");
				}
			}
			
		}
		return apptdeep;
	}	
	private List getAppointmentsStaff( String staff, String selecteddate, String enddate){
		// get all of the appointments for today
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date beginofday = null;
		Date endofday = null;
		
		try {
			if(selecteddate == null || selecteddate.equalsIgnoreCase("")){
				String dateString = df.format(new Date());
				endofday = df.parse(dateString);
				beginofday = df.parse(dateString);
			}else{
				endofday = df.parse(enddate);
				beginofday = df.parse(selecteddate);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		beginofday.setHours(0);
		beginofday.setMinutes(0);
		beginofday.setSeconds(0);
		log.debug("the value of beginofday: "+beginofday);
		
		endofday.setHours(23);
		endofday.setMinutes(59);
		endofday.setSeconds(0);
		log.debug("the value of endofday: "+endofday);
		
		
		
		
		
		log.debug("the shop is: "+getShop().toString());
		Staff staf = Staff.findStaff(Long.parseLong(staff));
		TypedQuery<Appointment> appointments = Appointment.findAppointmentsByShopAndStatusNotAndAppointmentDateBetweenAndStaffEquals(getShop(), ScheduleStatus.DELETED, beginofday, endofday,staf);
		log.debug("getTodaysAppointments -- the number of appointments is: "+appointments.getResultList().size());
		List<AppointmentDeep> apptdeep = new ArrayList<AppointmentDeep>();
		if(appointments.getResultList().size()>0){
			for(int x=0;x<appointments.getResultList().size();x++){
				Appointment appt = appointments.getResultList().get(x);
				log.debug("BEFORE CONVERTING TO APPOINTMENTDEEP appt.getBeginDateTime()"+appt.getBeginDateTime());
				AppointmentDeep deep = new AppointmentDeep(appt);
				log.debug("AFTER CONVERTING TO APPOINTMENTDEEP deep.getBeginDateTime()"+deep.getBeginDateTime());
				//TODO: AT THIS POINT LOOK UP CUSTOMPRICE
				BaseService b = BaseService.findBaseService(deep.getService1id());
				if(b != null){
					if(deep.getClient() == null) continue;
					TypedQuery<CustomPrice> tcp = CustomPrice.findCustomPricesByShopAndAppointmentAndServiceAndClient(getShop(), appt, b, deep.getClient());
					List<CustomPrice> lcp = tcp.getResultList();
					if(lcp.size() > 0){
						log.debug("Found custom price for this appointment: "+lcp.get(0).getCost());
						deep.setService1cost(""+lcp.get(0).getCost());
					}
				}
				log.debug("AFTER CONVERTING TO APPOINTMENTDEEP deep.getBeginDateTime()"+deep.getBeginDateTime());
				
				apptdeep.add(deep);
			}
			
		}
		return apptdeep;
	}	


	/**
	 * load all events for the month
	 * 
	 * @return
	 */
	@RequestMapping(value="/calappts", method=RequestMethod.GET)
	public @ResponseBody  String loadCalViewAppts(
			){
		log.debug("ENTERED loadCalViewAppts");
		List<AppointmentDeep> apptdeep = new ArrayList<AppointmentDeep>();
		
		
		String selecteddate = "";
		String enddate = "";
		// compute what the begin and end days are for this month
		String shoptimezone = "";
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shoptimezone = shopsetting.getTimezone();
			if(shoptimezone == null){
				shoptimezone = "MST";
				shopsetting.setTimezone(shoptimezone);
				shopsetting.merge();
			}
		}catch(Exception e){
			log.error(e);
		}
		TimeZone tz = TimeZone.getTimeZone(shoptimezone);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		// Get calendar set to current date and time
		Calendar c = Calendar.getInstance(tz);
		c.set(Calendar.DATE, 1);
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		selecteddate = df.format(c.getTime());
		//last day of month
		Calendar calValue = (Calendar)c.clone();
		calValue.add(Calendar.MONTH, 1);
		//calValue.set(Calendar.DAY_OF_MONTH, 1);
		calValue.add(Calendar.DATE, -1);
		enddate = df.format(calValue.getTime());
		// retrieve list of appointments
		apptdeep = getAppointments(selecteddate,enddate);
		log.debug("LIST OF APPOINTMENTS PRIOR TO MOBILIZE "+apptdeep.toString());
		
//		apptdeep = getTodaysAppointments("");
//		mav.addObject("todaysagenda",apptdeep);
//		uiModel.addAttribute("todaysagenda",apptdeep);
	/*
		 [{"appointmentDate":1380175200000,"beginDateTime":{"firstDayOfWeek":1,"gregorianChange":-12219292800000,"lenient":true,"minimalDaysInFirstWeek":1,"time":1348668000000,"timeInMillis":1348668000000,"timeZone":{"DSTSavings":0,"ID":"MST","dirty":false,"displayName":"Mountain Standard Time","lastRuleInstance":null,"rawOffset":-25200000}},"beginDateTimeDate":1348668000000,"cancelled":false,"checkintime":null,"checkouttime":null,"client":{"accepts_initial":true,"accepts_notifications":true,"accepts_receipts":true,"birthDay":null,"client_group":null,"firstName":"George","id":5178,"lastName":"Jones","reportable":true,"shop":{"expiration_date":1372013860393,"id":4656,"reportable":true,"shop_name":"knaylor","shop_url":"knaylor","shopuuid":"knaylor","ts":null,"type":"PAID","version":12},"username":null,"version":1},"clientcellphonenumber":null,"clientphonenumber":null,"clientworkphonenumber":null,"confirmed":null,"confirmeddate":null,"createddate":1380034668700,"day_ApptDate":"26","description":"Appointment Wed Sep 26 00:00:00 MDT 2012","duration_month":null,"endDateTime":{"firstDayOfWeek":1,"gregorianChange":-12219292800000,"lenient":true,"minimalDaysInFirstWeek":1,"time":1348674300000,"timeInMillis":1348674300000,"timeZone":{"DSTSavings":0,"ID":"MST","dirty":false,"displayName":"Mountain Standard Time","lastRuleInstance":null,"rawOffset":-25200000}},"endDateTimeDate":1348674300000,"end_date":"2012-09-26 08:00:00","endtime":0,"fc_beginDateTime":"8:00","fc_beginHour":"8","fc_beginMinute":"00","fc_endDateTime":"9:45","fc_endHour":"9","fc_endMinute":"45","frequency_week":null,"id":"5562","month_ApptDate":"09","notes":"","personallabel":null,"recur_parent":null,"reoccur_start_date":null,"reoccurring":null,"reoccurring_email_all":null,"requested_image_name":null,"requested_image_path":null,"s_beginDateTime":"8:00 AM","s_endDateTime":"9:45 AM","service1cost":"40.0","service1id":851968,"service1type":"base","service2cost":null,"service2id":0,"service2type":null,"service3cost":null,"service3id":0,"service3type":null,"service4cost":null,"service4id":0,"service4type":null,"servicename1":"Brazilian Wax","servicename2":null,"servicename3":null,"servicename4":null,"shop":{"expiration_date":1372013860393,"id":4656,"reportable":true,"shop_name":"knaylor","shop_url":"knaylor","shopuuid":"knaylor","ts":null,"type":"PAID","version":12},"staff":{"birthDay":1358060400000,"firstName":"Henry","id":4825,"lastName":"Ford","reportable":true,"shop":{"expiration_date":1372013860393,"id":4656,"reportable":true,"shop_name":"knaylor","shop_url":"knaylor","shopuuid":"knaylor","ts":null,"type":"PAID","version":12},"use_gcalendar":false,"username":null,"version":1},"start_date":"2012-09-26 09:45:00","starttime":0,"status":"ACTIVE","text":"Brazilian Wax","timezone":"MST","year_ApptDate":"2013"}]
	*/
		String jsonarray = AppointmentDeep.toJsonArrayMobile(apptdeep);
		//String jsonarray = "[{ id:1, start_date:'2013-09-26 00:00:00', end_date:'2013-09-26 01:00:00', text:'French Open', details:'Philippe-Chatrier Court Paris, FRA' },		 { id:2, start_date:'2013-09-10 00:00:00', end_date:'2013-09-13 00:00:00', text:'Aegon Championship', details:'The Queens Club London, ENG'}]";
		//String jsonarray = "[         { 'id':'111','start_date': '2013-10-01 09:00', 'end_date': '2013-10-01 12:00', 'text':'English lesson', 'subject': 'english' },         { 'id':'222','start_date': '2013-10-01 10:00', 'end_date': '2013-10-01 16:00', 'text':'Math exam', 'subject': 'math' },         { 'id':'333','start_date': '2013-10-01 10:00', 'end_date': '2013-10-01 14:00', 'text':'Science lesson', 'subject': 'science' },         { 'id':'444','start_date': '2013-10-01 16:00', 'end_date': '2013-10-01 17:00', 'text':'English lesson', 'subject': 'english' },         { 'id':'555','start_date': '2013-10-01 09:00', 'end_date': '2013-10-01 17:00', 'text':'Usual event' }     ]";
		//String jsonarray = " [	{		'subject':'english',		'details':'TEST deta',		'end_date':'2013-10-01 08:30:00',		'id':'5594',		'service1id':786432,		'servicename1':'Haircut',		'start_date':'2013-10-01 08:00:00',		'status':'ACTIVE',		'text':'Haircut'	},	{		'subject':'math',		'details':'Rest data',		'end_date':'2013-10-01 09:45:00',		'id':'5604',		'service1id':819200,		'servicename1':'Red Nails',		'start_date':'2013-10-01 09:00:00',		'status':'ACTIVE',		'text':'Red Nails'	}]";
		log.debug("returning jsonarray: "+jsonarray);
		log.debug("EXITING loadCalViewAppts");
		return jsonarray;
	}

	@RequestMapping(value="/calapptschange", method=RequestMethod.GET)
	public @ResponseBody  String loadCalViewApptsOnChange(
			@RequestParam(value="newDate",required=true) String newdate
			){
		log.debug("ENTERED loadCalViewApptsOnChange");
		log.debug("newdate: " + newdate);
		Date apptDate = convertStringToDateDynamic(newdate,"MM/dd/yyyy");
		
		List<AppointmentDeep> apptdeep = new ArrayList<AppointmentDeep>();
		
		
		String selecteddate = "";
		String enddate = "";
		// compute what the begin and end days are for this month
		String shoptimezone = "";
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shoptimezone = shopsetting.getTimezone();
			if(shoptimezone == null){
				shoptimezone = "MST";
				shopsetting.setTimezone(shoptimezone);
				shopsetting.merge();
			}
		}catch(Exception e){
			log.error(e);
		}
		TimeZone tz = TimeZone.getTimeZone(shoptimezone);
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		// Get calendar set to current date and time
		Calendar c = Calendar.getInstance(tz);
		c.setTime(apptDate);
		log.debug("c: "+c.getTime().toString());
		selecteddate = df.format(c.getTime());
//		c.set(Calendar.DATE, 1);
//		c.set(Calendar.HOUR, 0);
//		c.set(Calendar.MINUTE, 0);
//		c.set(Calendar.SECOND, 0);
		log.debug("2 c: "+c.getTime().toString());
//		selecteddate = df.format(c.getTime());
//		log.debug("selecteddate: "+selecteddate.toString());
		//last day of month
		Calendar calValue = (Calendar)c.clone();
		log.debug("past 1");
		calValue.add(Calendar.MONTH, 1);
		log.debug("past 2");
		//calValue.set(Calendar.DAY_OF_MONTH, 1);
		calValue.add(Calendar.DATE, -1);
		log.debug("past 3");
		enddate = df.format(calValue.getTime());
		log.debug("past 4");
		// retrieve list of appointments
		log.debug("selecteddate: "+ selecteddate);
		log.debug("enddate: "+ enddate);
		apptdeep = getAppointmentsDynamic(selecteddate,enddate,"MM/dd/yyyy");
		
		String jsonarray = AppointmentDeep.toJsonArrayMobile(apptdeep);
		log.debug("returning jsonarray: "+jsonarray);
		log.debug("EXITING loadCalViewApptsOnChange");
		return jsonarray;
	}



	private DateRange getDateRange(Calendar calrange) {
		Calendar begining, end;
	    DateRange daterange = new DateRange();
	    {
	        Calendar calendar = calrange.getInstance();
	        calendar.set(Calendar.DAY_OF_MONTH,
	                calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
	        setTimeToBeginningOfDay(calendar);
	        begining = calendar;
	    }

	    {
	        Calendar calendar = calrange.getInstance();
	        calendar.set(Calendar.DAY_OF_MONTH,
	                calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
	        setTimeToEndofDay(calendar);
	        end = calendar;
	    }
	    daterange.setBeginDate(begining);
	    daterange.setEndDate(end);
	    
	    return daterange;
	}

	private Calendar getCalendarFor(Date gotodate) {
	    Calendar calendar = GregorianCalendar.getInstance();
	    calendar.setTime(new Date());
	    return calendar;
	}

	private void setTimeToBeginningOfDay(Calendar calendar) {
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MILLISECOND, 0);
	}

	private static void setTimeToEndofDay(Calendar calendar) {
	    calendar.set(Calendar.HOUR_OF_DAY, 23);
	    calendar.set(Calendar.MINUTE, 59);
	    calendar.set(Calendar.SECOND, 59);
	    calendar.set(Calendar.MILLISECOND, 999);
	}	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



	@RequestMapping(method = RequestMethod.GET)
    public ModelAndView getCreateForm(Model uiModel) {
		log.debug("ENTERED getCreateForm");
		ModelAndView mav = new ModelAndView();
		if(getShop() != null){
			// then we have the shop and we are securely logged in
			//AddAppointmentForm aaf = new AddAppointmentForm();
			//aaf.setSelectDate(new Date());
			//uiModel.addAttribute("addappointment", aaf);
			//mav.addObject("addappointment",aaf);
			mav.addObject("selectdate","");
			// get all of the appointments for today
			Date beginofday = new Date();
			beginofday.setHours(0);
			beginofday.setMinutes(0);
			beginofday.setSeconds(0);
			log.debug("the value of beginofday: "+beginofday);
			Date endofday = new Date();
			endofday.setHours(23);
			endofday.setMinutes(59);
			endofday.setSeconds(0);
			log.debug("the value of endofday: "+endofday);
			log.debug("the shop is: "+getShop().toString());
			//TypedQuery<Appointment> appointments = Appointment.findAppointmentsByShopAndAppointmentDateBetween(getShop(), beginofday, endofday);
			//TypedQuery<Appointment> appointments = Appointment.findAppointmentsByShopAndCancelledNotAndAppointmentDateBetween(getShop(),true, beginofday, endofday);
			TypedQuery<Appointment> appointments = Appointment.findAppointmentsByShopAndStatusNotAndAppointmentDateBetween(getShop(), ScheduleStatus.DELETED,beginofday, endofday);
			
			log.debug("getCreateForm -- the number of appointments is: "+appointments.getResultList().size());
			//TypedQuery<Appointment> appointments2 = Appointment.findAppointmentsByShopAndCancelledNotAndAppointmentDateBetween(getShop(),false, beginofday, endofday);
			TypedQuery<Appointment> appointments2 = Appointment.findAppointmentsByShopAndStatusNotAndAppointmentDateBetween(getShop(), ScheduleStatus.DELETED,beginofday, endofday);
			log.debug("getCreateForm -- the number of appointments2 is: "+appointments2.getResultList().size());
			List<AppointmentDeep> apptdeep = new ArrayList<AppointmentDeep>();
			if(appointments.getResultList().size()>0){
				for(int x=0;x<appointments.getResultList().size();x++){
					Appointment appt = appointments.getResultList().get(x);
					AppointmentDeep deep = new AppointmentDeep(appt);
					//TODO: AT THIS POINT LOOK UP CUSTOMPRICE
					BaseService b = BaseService.findBaseService(deep.getService1id());
					if(b != null){
						if(deep.getClient() == null) continue;
						TypedQuery<CustomPrice> tcp = CustomPrice.findCustomPricesByShopAndAppointmentAndServiceAndClient(getShop(), appt, b, deep.getClient());
						List<CustomPrice> lcp = tcp.getResultList();
						if(lcp.size() > 0){
							log.debug("Found custom price for this appointment: "+lcp.get(0).getCost());
							deep.setService1cost(""+lcp.get(0).getCost());
						}
					}
					log.debug("AFTER CONVERTING TO APPOINTMENTDEEP deep.getBeginDateTime()"+deep.getBeginDateTime());
					
					apptdeep.add(deep);
				}
				
			}
			apptdeep = getTodaysAppointments("");
			
			Collections.sort(apptdeep,
					 new Comparator<AppointmentDeep>() {
					
								@Override
								public int compare(AppointmentDeep e1, AppointmentDeep e2) {
									// the wizardy with Calendars below is to make
									// sure the Dates are the same for Month, day, year
									log.debug("comparing times");
									Calendar cal1 = e1.getBeginDateTime();
									log.debug("cal1 date: "+cal1.get(Calendar.DATE));
									log.debug("cal1 month: "+cal1.get(Calendar.MONTH));
									log.debug("cal1 year: "+cal1.get(Calendar.YEAR));
									Calendar cal2 = e2.getBeginDateTime();
									log.debug("cal2 date: "+cal2.get(Calendar.DATE));
									log.debug("cal2 month: "+cal2.get(Calendar.MONTH));
									log.debug("cal2 year: "+cal2.get(Calendar.YEAR));
									
									Calendar tmpcal = Calendar.getInstance();
									log.debug("tmpcal date: "+tmpcal.get(Calendar.DATE));
									log.debug("tmpcal month: "+tmpcal.get(Calendar.MONTH));
									log.debug("tmpcal year: "+tmpcal.get(Calendar.YEAR));
									
									tmpcal.setTime(e1.getAppointmentDate());
									
									
									cal1.set(Calendar.DATE,tmpcal.get(Calendar.DATE));
									cal1.set(Calendar.MONTH,tmpcal.get(Calendar.MONTH));
									cal1.set(Calendar.YEAR,tmpcal.get(Calendar.YEAR));
									e1.setBeginDateTime(cal1);
									
									cal2.set(Calendar.DATE,tmpcal.get(Calendar.DATE));
									cal2.set(Calendar.MONTH,tmpcal.get(Calendar.MONTH));
									cal2.set(Calendar.YEAR,tmpcal.get(Calendar.YEAR));
									
									e2.setBeginDateTime(cal2);
									log.debug("revised cal1 date: "+cal1.get(Calendar.DATE));
									log.debug("revised cal1 month: "+cal1.get(Calendar.MONTH));
									log.debug("revised cal1 year: "+cal1.get(Calendar.YEAR));
									log.debug("revised cal2 date: "+cal2.get(Calendar.DATE));
									log.debug("revised cal2 month: "+cal2.get(Calendar.MONTH));
									log.debug("revised cal2 year: "+cal2.get(Calendar.YEAR));
									return (int) (e1.getBeginDateTime().compareTo(e2.getBeginDateTime()));
								}
							}				
	);				
			
			mav.addObject("todaysagenda",apptdeep);
			uiModel.addAttribute("todaysagenda",apptdeep);
			uiModel.addAttribute("addappt_selectdate_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
		}
		log.debug("EXITING getCreateForm VIA myschedule");
		mav.setViewName("myschedule");
		return mav;
    }

	@RequestMapping(value = "/updated", method = RequestMethod.GET)
    public ModelAndView showViewViaPost(Model uiModel) {
		log.debug("ENTERED showViewViaPost");
		ModelAndView mav = new ModelAndView();
		if(getShop() != null){
			// then we have the shop and we are securely logged in
			//AddAppointmentForm aaf = new AddAppointmentForm();
			//aaf.setSelectDate(new Date());
			//uiModel.addAttribute("addappointment", aaf);
			//mav.addObject("addappointment",aaf);
			mav.addObject("selectdate","");
			// get all of the appointments for today
			Date beginofday = new Date();
			beginofday.setHours(0);
			beginofday.setMinutes(0);
			beginofday.setSeconds(0);
			log.debug("the value of beginofday: "+beginofday);
			Date endofday = new Date();
			endofday.setHours(23);
			endofday.setMinutes(59);
			endofday.setSeconds(0);
			log.debug("the value of endofday: "+endofday);
			log.debug("the shop is: "+getShop().toString());
			//TypedQuery<Appointment> appointments = Appointment.findAppointmentsByShopAndAppointmentDateBetween(getShop(), beginofday, endofday);
			//TypedQuery<Appointment> appointments = Appointment.findAppointmentsByShopAndCancelledNotAndAppointmentDateBetween(getShop(),true, beginofday, endofday);
			TypedQuery<Appointment> appointments = Appointment.findAppointmentsByShopAndStatusNotAndAppointmentDateBetween(getShop(), ScheduleStatus.DELETED,beginofday, endofday);
			log.debug("showViewViaPost -- the number of appointments is: "+appointments.getResultList().size());
			List<AppointmentDeep> apptdeep = new ArrayList<AppointmentDeep>();
			if(appointments.getResultList().size()>0){
				for(int x=0;x<appointments.getResultList().size();x++){
					Appointment appt = appointments.getResultList().get(x);
					AppointmentDeep deep = new AppointmentDeep(appt);
					//TODO: AT THIS POINT LOOK UP CUSTOMPRICE
					BaseService b = BaseService.findBaseService(deep.getService1id());
					if(b != null){
						if(deep.getClient() == null) continue;
						TypedQuery<CustomPrice> tcp = CustomPrice.findCustomPricesByShopAndAppointmentAndServiceAndClient(getShop(), appt, b, deep.getClient());
						List<CustomPrice> lcp = tcp.getResultList();
						if(lcp.size() > 0){
							log.debug("Found custom price for this appointment: "+lcp.get(0).getCost());
							deep.setService1cost(""+lcp.get(0).getCost());
						}
					}
					log.debug("AFTER CONVERTING TO APPOINTMENTDEEP deep.getBeginDateTime()"+deep.getBeginDateTime());
					
					apptdeep.add(deep);
				}
				
			}
			apptdeep = getTodaysAppointments("");
			
			
			
			mav.addObject("todaysagenda",apptdeep);
			uiModel.addAttribute("todaysagenda",apptdeep);
			uiModel.addAttribute("addappt_selectdate_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
		}
		mav.setViewName("myschedule");
		log.debug("EXITING showViewViaPost");
		return mav;
        
    }
	   
    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView create(String hSelectDate,@Valid AddAppointmentForm addappointment,  BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
    	ModelAndView mav = new ModelAndView();
    	mav.setView(new RedirectView("myschedule"));
    	log.debug("Entered MyScheduleController create method");

    	if (bindingResult.hasErrors()) {
            uiModel.addAttribute("appointment", addappointment);
            addDateTimeFormatPatterns(uiModel);
            return mav;
        }
    	
        log.debug("the selected date: "+hSelectDate);
        log.debug("service 1: "+addappointment.getService1().toString());
        log.debug("hour: "+addappointment.getHour());
        log.debug("minute: "+addappointment.getMinute());
        log.debug("ampm: "+addappointment.getAmpm());
        log.debug("client fname: "+addappointment.getClient().getFirstName());
        log.debug("note: "+addappointment.getNotes());
        int hour = 0;
        int minute = Integer.parseInt(addappointment.getMinute());
        if(addappointment.getAmpm().equalsIgnoreCase("am")){
        	hour = Integer.parseInt(addappointment.getHour());
        }else{
        	if(hour != 12){
        		hour = 12 + Integer.parseInt(addappointment.getHour());
        	}
        }
        uiModel.asMap().clear();
        // save addappointment data
		if(getShop() == null){
			setShop(getShop());
		}
		TypedQuery<Staff> staffs = Staff.findStaffsByShop(getShop());
		if(staffs.getResultList().size() > 0){
			log.debug("found at least one service to assing to an appointment");
			try{
				// format date
				String dateString = hSelectDate; 
				log.debug("dateString: "+dateString);
			    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			    Date apptDate = null; 
			    try {
			    	log.debug("ABOUT TO TRANSFORM DATE");
					if(hSelectDate == null || hSelectDate.equalsIgnoreCase("")){
						dateString = dateFormat.format(new Date());
						apptDate = dateFormat.parse(dateString);
					}else{
						apptDate = dateFormat.parse(dateString);	
					}
			    	
			    	
			    	log.debug("the transformed datestring into the apptDate is: "+apptDate.toString());
				} catch (ParseException e) {
					log.error(e);
					e.printStackTrace();
				}			
				// add an appointment
				Appointment appt = new Appointment();
				appt.setShop(getShop());
				appt.setClient(addappointment.getClient());
				appt.setStaff(staffs.getResultList().get(0));
				TypedQuery<BaseService>  tqservice = BaseService.findBaseServicesByShop(getShop());
				Set<BaseService> set = new HashSet<BaseService>();
				appt.setAppointmentDate(apptDate);
				appt.setCreateddate(new Date());
				appt.setDescription("Appointment "+apptDate);
				appt.setStarttime(0);
				appt.setCancelled(false);
				appt.setEndtime(0);
				appt.setStatus(ScheduleStatus.ACTIVE);
				BaseService svc1 = addappointment.getService1();
				int time = 0;
				if(svc1.getAmounttime() >= svc1.getLength_time()){
					time = svc1.getAmounttime();
				}else{
					time = svc1.getLength_time();
				}
				
				Calendar begintime = Calendar.getInstance();
				Date begindatetime = new Date();
				begindatetime.setHours(hour);
				begindatetime.setMinutes(minute);
				begindatetime.setSeconds(0);
				begintime.setTime(begindatetime);
				begintime.set(Calendar.MILLISECOND, 0);
				
				Date enddatetime = new Date();
				Calendar endtime = Calendar.getInstance();
				appt.setBeginDateTime(begintime);
				
				enddatetime.setHours(hour);
				enddatetime.setMinutes(minute+time);
				enddatetime.setSeconds(0);
				endtime.setTime(enddatetime);
				endtime.set(Calendar.MILLISECOND, 0);
				
				appt.setEndDateTime(endtime);
				set.add(addappointment.getService1());
				
				appt.setServices(set);
				log.debug("about to try to persist the appointment "+appt.toString());
				appt.persist();
				// adding audit log
				String appointmentbegintime = "";
				SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm:ss a");
				appointmentbegintime = dateformatter.format(appt.getBeginDateTime().getTime());
				
				SimpleDateFormat dateformatterappt = new SimpleDateFormat("MM/dd/yyyy");
				String originaldate = dateformatterappt.format(appt.getAppointmentDate());
				String staffname = "";
				if(appt.getStaff() != null){
					staffname = appt.getStaff().getFirstName() + " " + appt.getStaff().getLastName();
				}
				saveAuditMessage("Appointment for "+ originaldate+" "+ appointmentbegintime+" Client: "+appt.getClient().getFirstName()+" "+appt.getClient().getLastName()+" CREATED. Staff " +staffname,null,"GENERAL");
				
				log.debug("persisted appointment");
			}catch(Exception ed){
				log.error(ed);
			}
		}					
		List<AppointmentDeep> apptdeep = getTodaysAppointments(hSelectDate);
		
		
		mav.addObject("todaysagenda",apptdeep);
		uiModel.addAttribute("todaysagenda",apptdeep);
		
        log.debug("Exited MyScheduleController create method");
        return mav;
    }
    /**
     * 
     * @param hSelectDate
     * @param clientid
     * @param svcid
     * @param hour // begin hour
     * @param minute // begin minute
     * @param ampm // begin ampm
     * @param notes
     * @param endhour // end hour
     * @param endminute // end  minute
     * @param endampm // end am or pm
     * @return
     */
    @RequestMapping(value="/createappt", method=RequestMethod.POST)
    public @ResponseBody  String createApptJson(
    		@RequestParam(value="hSelectDate",required=true) String hSelectDate,
    		@RequestParam(value="staffid",required=true) String staffid,
    		@RequestParam(value="clientid",required=true) String clientid, 
    		@RequestParam(value="svcid",required=true) String svcid, 
    		@RequestParam(value="hour",required=true) String hour, 
    		@RequestParam(value="minute",required=true) String minute,
    		@RequestParam(value="ampm",required=true) String ampm, 
    		@RequestParam(value="notes",required=true) String notes,
    		@RequestParam(value="ehour",required=true) String endhour, 
    		@RequestParam(value="eminute",required=true) String endminute,
    		@RequestParam(value="eampm",required=true) String endampm, 
    		@RequestParam(value="enable_recur",required=false) String enable_recur,
    		@RequestParam(value="weekly",required=false) String weekly,
    		@RequestParam(value="weeklyevery",required=false) String weeklyevery,
    		@RequestParam(value="weeklyeverytext",required=false) String weeklyeverytext,
    		@RequestParam(value="everyweekdaysu",required=false) String everyweekdaysu,
    		@RequestParam(value="everyweekdaymo",required=false) String everyweekdaymo,
    		@RequestParam(value="everyweekdaytu",required=false) String everyweekdaytu,
    		@RequestParam(value="everyweekdaywe",required=false) String everyweekdaywe,
    		@RequestParam(value="everyweekdaythu",required=false) String everyweekdaythu,
    		@RequestParam(value="everyweekdayfri",required=false) String everyweekdayfri,
    		@RequestParam(value="everyweekdaysat",required=false) String everyweekdaysat,
    		@RequestParam(value="daily",required=false) String daily,
    		@RequestParam(value="every",required=false) String every,
    		@RequestParam(value="everytext",required=false) String everytext,
    		@RequestParam(value="everyweekday",required=false) String everyweekday,
    		@RequestParam(value="monthly",required=false) String monthly,
    		@RequestParam(value="everyday",required=false) String everyday,
    		@RequestParam(value="everymonthdaytext",required=false) String everymonthdaytext,
    		@RequestParam(value="everymonthtext",required=false) String everymonthtext,
    		@RequestParam(value="everyregex",required=false) String everyregex,
    		@RequestParam(value="everynthdaytext",required=false) String everynthdaytext,
    		@RequestParam(value="everynthmonthtext",required=false) String everynthmonthtext,
    		@RequestParam(value="everyhowmanymonthtext",required=false) String everyhowmanymonthtext,
    		@RequestParam(value="endafter",required=false) String endafter,
    		@RequestParam(value="endafterxoccur",required=false) String endafterxoccur,
    		@RequestParam(value="everydate",required=false) String everydate,
    		@RequestParam(value="rangerecurEndDate",required=false) String rangerecurEndDate,
    		@RequestParam(value="ri",required=false) String requested_indicator
    		) {
    	String successstatus = "success";
    	log.debug("Entered createApptJson method");

    	log.debug("the selected date: "+hSelectDate);
        log.debug("service 1: "+svcid);
        log.debug("hour: "+hour);
        log.debug("minute: "+minute);
        log.debug("ampm: "+ampm);
        log.debug("client fname: "+clientid);
        log.debug("note: "+notes);
        log.debug("end hour: "+endhour);
        log.debug("end minute: "+endminute);
        log.debug("end ampm: "+endampm);
        
	 	log.debug("enable_recur : "+enable_recur);
		log.debug("weekly : "+weekly);
    	log.debug("weeklyevery : "+weeklyevery);
    	log.debug("weeklyeverytext : "+weeklyeverytext);
    	log.debug("everyweekdaysu : "+everyweekdaysu);
    	log.debug("everyweekdaymo : "+everyweekdaymo);
    	log.debug("everyweekdaytu : "+everyweekdaytu);
    	log.debug("everyweekdaywe : "+everyweekdaywe);
    	log.debug("everyweekdaythu : "+everyweekdaythu);
    	log.debug("everyweekdayfri : "+everyweekdayfri);
    	log.debug("everyweekdaysat : "+everyweekdaysat);
    	log.debug("daily : "+daily);
    	log.debug("every : "+every);
    	log.debug("everytext : "+everytext);
    	log.debug("everyweekday : "+everyweekday);
    	log.debug("monthly : "+monthly);
    	log.debug("everyday : "+everyday);
    	log.debug("everymonthdaytext : "+everymonthdaytext);
    	log.debug("everymonthtext : "+everymonthtext);
    	log.debug("everyregex : "+everyregex);
    	log.debug("everynthdaytext : "+everynthdaytext);
    	log.debug("everynthmonthtext : "+everynthmonthtext);
    	log.debug("everyhowmanymonthtext : "+everyhowmanymonthtext);
    	log.debug("endafter : "+endafter);
    	log.debug("endafterxoccur : "+endafterxoccur);
    	log.debug("everydate : "+everydate);
    	log.debug("rangerecurEndDate : "+rangerecurEndDate);        
    	
        int inthour = Integer.parseInt(hour);
        int intminute = Integer.parseInt(minute);
        if(ampm.equalsIgnoreCase("am")){
        	inthour = Integer.parseInt(hour);
        }else{
        	if(inthour != 12){
    			inthour = 12 + Integer.parseInt(hour);
        	}
        }
        int intehour = 0;
        int inteminute = 0;
        if(endhour != null && !endhour.equalsIgnoreCase("select") && endampm != null && !endampm.equalsIgnoreCase("select") && !endminute.equalsIgnoreCase("select")){
            intehour = Integer.parseInt(endhour);
            inteminute = Integer.parseInt(endminute);
            if(endampm.equalsIgnoreCase("am")){
            	if(endhour.equalsIgnoreCase("12")){
            		endhour = "0";
            	}
            	intehour = Integer.parseInt(endhour);
            }else{
            	if(endhour.equalsIgnoreCase("12")){
            		intehour = Integer.parseInt(endhour);
            	}else{
            		if(intehour != 12){
            			intehour = 12 + Integer.parseInt(endhour);
            		}
            	}
            }
        	
        }
        // save addappointment data
		if(getShop() == null){
			setShop(getShop());
		}
		TypedQuery<Staff> staffs = Staff.findStaffsByShop(getShop());
		if(staffs.getResultList().size() > 0){
			log.debug("found at least one service to assing to an appointment");
			try{
				// format date
				String dateString = hSelectDate; 
				log.debug("dateString: "+dateString);
			    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			    Date apptDate = null; 
			    Date er_enddate = null;
			    try {
			    	log.debug("ABOUT TO TRANSFORM DATE");
					if(hSelectDate == null || hSelectDate.equalsIgnoreCase("")){
						dateString = dateFormat.format(new Date());
						apptDate = dateFormat.parse(dateString);
					}else{
						apptDate = dateFormat.parse(dateString);	
					}
					if(rangerecurEndDate != null && !rangerecurEndDate.equalsIgnoreCase("")){
						log.debug("trying to transform er_enddate ");
						er_enddate = dateFormat.parse(rangerecurEndDate);
						log.debug("the transformed er_enddate is: "+er_enddate.toString());
					}
			    	log.debug("the transformed datestring into the apptDate is: "+apptDate.toString());
				} catch (ParseException e) {
					log.error(e);
					e.printStackTrace();
					successstatus = " failure to transform date ";
				}		
			    // if recurring get list of dates
			    DateList list = null;
			    // DAILY
			    if(daily.equalsIgnoreCase("true")){
			    	if(everydate.equalsIgnoreCase("true") && er_enddate != null){
			    		list = getDaily(apptDate,apptDate,er_enddate,1);
			    	}
			    	if(endafter.equalsIgnoreCase("true") && !endafterxoccur.equalsIgnoreCase("")){
			    		try{
			    			Integer occurr = Integer.parseInt(endafterxoccur);
			    			list = getDailyByOcurrence(apptDate,apptDate,occurr,1);
			    		}catch(Exception e){
			    			log.error(e);
			    		}
			    	}
			    }
			    if(every.equalsIgnoreCase("true")){
			    	if(everydate.equalsIgnoreCase("true") && er_enddate != null){
			    		try{
			    			Integer everyinterval = Integer.parseInt(everytext);
			    			list = getDaily(apptDate,apptDate,er_enddate,everyinterval);
			    		}catch(Exception e){
			    			log.error(e);
			    		}
			    	}
			    	if(endafter.equalsIgnoreCase("true") && !endafterxoccur.equalsIgnoreCase("")){
			    		try{
			    			Integer occurr = Integer.parseInt(endafterxoccur);
			    			Integer everyinterval = Integer.parseInt(everytext);
			    			list = getDailyByOcurrence(apptDate,apptDate,occurr,everyinterval);
			    		}catch(Exception e){
			    			log.error(e);
			    		}
			    	}
			    }
			    if(everyweekday.equalsIgnoreCase("true")){
			    	if(everydate.equalsIgnoreCase("true") && er_enddate != null){
		    			list = getDailyWeekDays(apptDate,apptDate,er_enddate,1);
			    	}
			    	if(endafter.equalsIgnoreCase("true") && !endafterxoccur.equalsIgnoreCase("")){
			    		try{
			    			Integer occurr = Integer.parseInt(endafterxoccur);
			    			Integer everyinterval = Integer.parseInt(everytext);
			    			list = getDailyWeekDaysByOccurrence(apptDate,apptDate,occurr,everyinterval);
			    		}catch(Exception e){
			    			log.error(e);
			    		}
			    	}
			    }
			    // WEEKLY
		    	
			    ArrayList<WeekDay> weekdays = new ArrayList<WeekDay>();
	        	if(everyweekdaysu.equalsIgnoreCase("true")){
	        		weekdays.add(WeekDay.SU);
	        	}
	        	if(everyweekdaymo.equalsIgnoreCase("true")){
	        		weekdays.add(WeekDay.MO);
	        	}
	        	if(everyweekdaytu.equalsIgnoreCase("true")){
	        		weekdays.add(WeekDay.TU);
	        	}
	        	if(everyweekdaywe.equalsIgnoreCase("true")){
	        		weekdays.add(WeekDay.WE);
	        	}
	        	if(everyweekdaythu.equalsIgnoreCase("true")){
	        		weekdays.add(WeekDay.TH);
	        	}
	        	if(everyweekdayfri.equalsIgnoreCase("true")){
	        		weekdays.add(WeekDay.FR);
	        	}
	        	if(everyweekdaysat.equalsIgnoreCase("true")){
	        		weekdays.add(WeekDay.SA);
	        	}
	        	
			    if(weekly.equalsIgnoreCase("true")){
			    	if(everydate.equalsIgnoreCase("true") && er_enddate != null){
			    		list = getWeeklyWithDaily(apptDate,apptDate,er_enddate,1,weekdays);
			    	}
			    	if(endafter.equalsIgnoreCase("true") && !endafterxoccur.equalsIgnoreCase("")){
			    		try{
			    			Integer occurr = Integer.parseInt(endafterxoccur);
			    			list = getWeeklyWithDailyByOccurrence(apptDate,apptDate,occurr,1,weekdays);
			    		}catch(Exception e){
			    			log.error(e);
			    		}
			    	}
			    }
			    if(weeklyevery.equalsIgnoreCase("true")){
			    	try{
				    	Integer everyweeklyinterval = Integer.parseInt(weeklyeverytext);
				    	
				    	if(everydate.equalsIgnoreCase("true") && er_enddate != null){
				    		list = getWeeklyWithDaily(apptDate,apptDate,er_enddate,everyweeklyinterval,weekdays);
				    		log.debug("weeklyevery everydate list size: "+list.size());
				    	}
				    	if(endafter.equalsIgnoreCase("true") && !endafterxoccur.equalsIgnoreCase("")){
				    		try{
				    			Integer occurr = Integer.parseInt(endafterxoccur);
				    			list = getWeeklyWithDailyByOccurrence(apptDate,apptDate,occurr,everyweeklyinterval,weekdays);
				    			log.debug("weeklyevery endafter list size: "+list.size());
				    		}catch(Exception e){
				    			log.error(e);
				    		}
				    	}
			    	}catch(Exception e){
			    		log.error(e);
			    	}
			    }			    
			    // MONTHLY
			    if(monthly.equalsIgnoreCase("true")){
			    	if(everydate.equalsIgnoreCase("true") && er_enddate != null){
			    		list = getMonthlyDays(apptDate,apptDate,er_enddate,1);
			    	}
			    	if(endafter.equalsIgnoreCase("true") && !endafterxoccur.equalsIgnoreCase("")){
			    		try{
			    			Integer occurr = Integer.parseInt(endafterxoccur);
			    			list = getMonthlyDaysByOccurrence(apptDate,apptDate,occurr,1);
			    			log.debug("monthly list size: "+list.size());
			    		}catch(Exception e){
			    			log.error(e);
			    		}
			    	}
			    }
			    if(everyday.equalsIgnoreCase("true")){
		    		try{
			    		Integer interval = Integer.parseInt(everymonthdaytext);
			    		Integer everymonthtextinterval = Integer.parseInt(everymonthtext);
			    		
				    	if(everydate.equalsIgnoreCase("true") && er_enddate != null){
					    		list = getMonthlyDaysXthOfEveryYMonths(apptDate,apptDate,er_enddate,interval,everymonthtextinterval);
					    		log.debug("everyday everydate list size: "+list.size());
				    	}
				    	
				    	if(endafter.equalsIgnoreCase("true") && !endafterxoccur.equalsIgnoreCase("")){
				    		try{
				    			Integer occurr = Integer.parseInt(endafterxoccur);
				    			list = getMonthlyDaysXthOfEveryYMonthsByOccurrence(apptDate,apptDate,occurr,interval,everymonthtextinterval);
				    			log.debug("everyday endafter list size: "+list.size());
				    		}catch(Exception e){
				    			log.error(e);
				    		}
				    	}
		    		}catch(Exception e){
		    			log.error(e);
		    		}
			    }
			    // SOMETHING
			    if(everyregex.equalsIgnoreCase("true")){
		    		try{
			    		Integer interval = Integer.parseInt(everynthdaytext);
			    		Integer everyhowmanymonthtextinterval = Integer.parseInt(everyhowmanymonthtext);
			    		WeekDay weekday = null;
			        	if(everynthmonthtext.equalsIgnoreCase("1")){
			        		weekday = WeekDay.SU;
			        	}
			        	if(everynthmonthtext.equalsIgnoreCase("2")){
			        		weekday = WeekDay.MO;
			        	}
			        	if(everynthmonthtext.equalsIgnoreCase("3")){
			        		weekday = WeekDay.TU;
			        	}
			        	if(everynthmonthtext.equalsIgnoreCase("4")){
			        		weekday = WeekDay.WE;
			        	}
			        	if(everynthmonthtext.equalsIgnoreCase("5")){
			        		weekday = WeekDay.TH;
			        	}
			        	if(everynthmonthtext.equalsIgnoreCase("6")){
			        		weekday = WeekDay.FR;
			        	}
			        	if(everynthmonthtext.equalsIgnoreCase("7")){
			        		weekday = WeekDay.SA;
			        	}
			    		
				    	if(everydate.equalsIgnoreCase("true") && er_enddate != null){
					    		list = getMonthlyDaysXthOfSomething(apptDate,apptDate,er_enddate,interval,weekday,everyhowmanymonthtextinterval);
					    		log.debug("everyregex everydate list size: "+list.size());
				    	}
				    	
				    	if(endafter.equalsIgnoreCase("true") && !endafterxoccur.equalsIgnoreCase("")){
				    		try{
				    			Integer occurr = Integer.parseInt(endafterxoccur);
				    			list = getMonthlyDaysXthOfSomethingByOccurrence(apptDate,apptDate,occurr,interval,weekday,everyhowmanymonthtextinterval);
				    			log.debug("everyregex endafter list size: "+list.size());
				    		}catch(Exception e){
				    			log.error(e);
				    		}
				    	}
		    		}catch(Exception e){
		    			log.error(e);
		    		}
			    }
			    
			    if(list == null){
			    	log.debug("list == null");
			    	list = new DateList();
			    	list.add(new net.fortuna.ical4j.model.Date(apptDate));
			    	log.debug("list size : "+list.size());
			    }
			    long parent = 0L;
			    log.debug("datelist: list.size(): "+list.size());
		        for (int i=0; i<list.size(); i++) {
		        	Date date_from_list = (Date)list.get(i);
		        	log.debug("date_from_list: "+date_from_list.toString());
					// add an appointment
					Appointment appt = new Appointment();
					appt.setShop(getShop());
					Clients client = Clients.findClients(Long.parseLong(clientid));
					appt.setClient(client);
					try{
						log.debug("The staffid: "+staffid);
						Staff apptstaff = Staff.findStaff(Long.parseLong(staffid));
						if(apptstaff != null){
							appt.setStaff(apptstaff);	
						}else{
							appt.setStaff(staffs.getResultList().get(0));
						}
					}catch(Exception staffException){
						log.error(staffException);
						staffException.printStackTrace();
					}
					TypedQuery<BaseService>  tqservice = BaseService.findBaseServicesByShop(getShop());
					Set<BaseService> set = new HashSet<BaseService>();
					appt.setAppointmentDate(date_from_list);
					appt.setCreateddate(new Date());
					appt.setDescription("Appointment "+apptDate);
					appt.setStarttime(0);
					appt.setCancelled(false);
					appt.setEndtime(0);
					appt.setNotes(notes);
					appt.setStatus(ScheduleStatus.ACTIVE);
					BaseService svc1 = BaseService.findBaseService(Long.parseLong(svcid));
					int time = 0;
					if(svc1.getAmounttime() >= svc1.getLength_time()){
						time = svc1.getAmounttime();
					}else{
						time = svc1.getLength_time();
					}
					// BEGIN TIME
					Calendar begintime = Calendar.getInstance();
					Date begindatetime = (Date)apptDate.clone();
					begindatetime.setHours(inthour);
					begindatetime.setMinutes(intminute);
					begindatetime.setSeconds(0);
					begintime.setTime(begindatetime);
					begintime.set(Calendar.MILLISECOND, 0);
					
					appt.setBeginDateTime(begintime);
					// END TIME
					if(endhour == null || endhour.equalsIgnoreCase("select") 
							|| endhour.equalsIgnoreCase("") || endhour.equalsIgnoreCase("select") 
							|| endminute == null || endminute.equalsIgnoreCase("") || endminute.equalsIgnoreCase("select")
							) {
						// end time not selected so use service time plus begin time
						Date enddatetime = (Date)apptDate.clone();
						Calendar endtime = Calendar.getInstance();
		
						enddatetime.setHours(inthour);
						enddatetime.setMinutes(intminute+time);
						enddatetime.setSeconds(0);
						endtime.setTime(enddatetime);
						endtime.set(Calendar.MILLISECOND, 0);
						log.debug("1 END TIME HOUR: "+intehour);
						log.debug("1 END TIME MINUTE: "+inteminute);					
						log.debug("1 END TIME: "+endtime.toString());
						appt.setEndDateTime(endtime);
						set.add(svc1);
					}else{
						// end time selected so use it					
						Date enddatetime = (Date)apptDate.clone();
						Calendar endtime = Calendar.getInstance();
		
						enddatetime.setHours(intehour);
						enddatetime.setMinutes(inteminute);
						enddatetime.setSeconds(0);
						endtime.setTime(enddatetime);
						endtime.set(Calendar.MILLISECOND, 0);
						log.debug("2 END TIME HOUR: "+intehour);
						log.debug("2 END TIME MINUTE: "+inteminute);
						log.debug("2 END TIME: "+endtime.toString());
						appt.setEndDateTime(endtime);
						set.add(svc1);
					}
					appt.setServices(set);
					log.debug("about to try to persist the appointment "+appt.toString());
					if(parent > 0L){
						appt.setRecur_parent(parent);
					}
					appt.setRequested_image_name(requested_indicator);
					appt.setRequested_image_path(requested_indicator);
					Appointment parent_appt = saveAppointment(appt);
					successstatus = ""+parent;
					log.debug("parent_appt: "+parent_appt);
					if(parent_appt != null && i==0){
						log.debug("This is i==0. Grabbing id of appointment to be used for recurring appointments");
						parent = parent_appt.getId();
						log.debug("parent: "+parent);
						// only send email if client accepts

						log.debug("appt "+ appt);
						if(appt != null) log.debug("appt.getClient() "+ appt.getClient());
						
						if(appt != null && appt.getClient()!=null){
							log.debug("appt.getClient().getAccepts_initial() "+ appt.getClient().getAccepts_initial());
							log.debug("appt.getClient().getAccepts_sms_initial(): "+appt.getClient().getAccepts_sms_initial());
						}
						if(appt != null && appt.getClient() != null && ((appt.getClient().getAccepts_initial() != null && appt.getClient().getAccepts_initial()==true) || (appt.getClient().getAccepts_sms_initial() != null && appt.getClient().getAccepts_sms_initial()==true))){
							try{
								log.debug("about to sendMessage");
								
								log.debug("going to set the System.setProperty for file encoding to utf-8");
								System.setProperty("file.encoding", "UTF-8");
								log.debug("succeeded setting the System.setProperty for file encoding to utf-8");
								sendMessage(appt);
								log.debug("sent the sendMessage");
							}catch(Exception eddy){
								log.error("problem sending notification email after creating the appointment in the database");
								log.error(eddy);
							}
						}
					}
					log.debug("about to add an entry in the audit log");
					// adding audit log
					String appointmentbegintime = "";
					SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm:ss a");
					appointmentbegintime = dateformatter.format(appt.getBeginDateTime().getTime());
					
					SimpleDateFormat dateformatterappt = new SimpleDateFormat("MM/dd/yyyy");
					if(appt !=null && appt.getAppointmentDate()!=null){
						String originaldate = dateformatterappt.format(appt.getAppointmentDate());
						String staffname = "";
						if(appt.getStaff() != null){
							staffname = appt.getStaff().getFirstName() + " " + appt.getStaff().getLastName();
						}
						try{
							saveAuditMessage("Appointment for "+ originaldate+" "+ appointmentbegintime+" Client: "+appt.getClient().getFirstName()+" "+appt.getClient().getLastName()+" CREATED. Staff " +staffname,null,"GENERAL");
						}catch(Exception deary){
							log.error("Error trying to save an audit message.");
							log.error(deary);;
						}
					}
					log.debug("persisted appointment");
		        }
				
			}catch(Exception ed){
				log.error(ed);
				successstatus += " failure ";
			}
		}					
		List<AppointmentDeep> apptdeep = getTodaysAppointments(hSelectDate);
		
        log.debug("Exited createApptJson method");
        return successstatus;
    }
    @RequestMapping(value="/createpersonal", method=RequestMethod.POST)
    public @ResponseBody  String createPersonalApptJson(
    		@RequestParam(value="hSelectDate",required=true) String hSelectDate,
    		@RequestParam(value="staff",required=true) String staff,
    		@RequestParam(value="hour",required=true) String hour, 
    		@RequestParam(value="minute",required=true) String minute,
    		@RequestParam(value="ampm",required=true) String ampm, 
    		@RequestParam(value="notes",required=true) String notes,
    		@RequestParam(value="label",required=true) String label,
    		@RequestParam(value="ehour",required=true) String endhour, 
    		@RequestParam(value="eminute",required=true) String endminute,
    		@RequestParam(value="eampm",required=true) String endampm 
    		
    		) {
    	String successstatus = "success";
    	log.debug("Entered createPersonalApptJson method");

    	log.debug("the selected date: "+hSelectDate);
        log.debug("hour: "+hour);
        log.debug("minute: "+minute);
        log.debug("ampm: "+ampm);
        log.debug("note: "+notes);
        log.debug("end hour: "+endhour);
        log.debug("end minute: "+endminute);
        log.debug("end ampm: "+endampm);
        
        int inthour = Integer.parseInt(hour);
        int intminute = Integer.parseInt(minute);
        if(ampm.equalsIgnoreCase("am")){
        	inthour = Integer.parseInt(hour);
        }else{
        	if(inthour != 12){
				inthour = 12 + Integer.parseInt(hour);
        	}
        }
        int intehour = 0;
        int inteminute = 0;
        if(endhour != null && !endhour.equalsIgnoreCase("select") && endampm != null && !endampm.equalsIgnoreCase("select") && !endminute.equalsIgnoreCase("select")){
            intehour = Integer.parseInt(endhour);
            inteminute = Integer.parseInt(endminute);
            if(endampm.equalsIgnoreCase("am")){
            	intehour = Integer.parseInt(endhour);
            }else{
            	if(intehour != 12){
            		intehour = 12 + Integer.parseInt(endhour);
            	}
            }
        	
        }
        // save addappointment data
		if(getShop() == null){
			setShop(getShop());
		}
		TypedQuery<Staff> staffs = Staff.findStaffsByShop(getShop());
		Long long_staff_id = Long.parseLong(staff);
		Staff found_staff = Staff.findStaff(long_staff_id);
		if(found_staff != null){
			log.debug("found at least one service to assing to an appointment");
			try{
				// format date
				String dateString = hSelectDate; 
				log.debug("dateString: "+dateString);
			    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			    Date apptDate = null; 
			    try {
			    	log.debug("ABOUT TO TRANSFORM DATE");
					if(hSelectDate == null || hSelectDate.equalsIgnoreCase("")){
						dateString = dateFormat.format(new Date());
						apptDate = dateFormat.parse(dateString);
					}else{
						apptDate = dateFormat.parse(dateString);	
					}
			    	
			    	
			    	log.debug("the transformed datestring into the apptDate is: "+apptDate.toString());
				} catch (ParseException e) {
					log.error(e);
					e.printStackTrace();
					successstatus = " failure to transform date ";
				}			
				// add an appointment
				Appointment appt = new Appointment();
				appt.setShop(getShop());
				appt.setStaff(found_staff);
				TypedQuery<BaseService>  tqservice = BaseService.findBaseServicesByShop(getShop());
				Set<BaseService> set = new HashSet<BaseService>();
				appt.setAppointmentDate(apptDate);
				appt.setCreateddate(new Date());
				appt.setDescription("Appointment "+apptDate);
				appt.setStarttime(0);
				appt.setCancelled(false);
				appt.setEndtime(0);
				appt.setPersonallabel(label);
				appt.setNotes(notes);
				appt.setStatus(ScheduleStatus.ACTIVE);
				int time = 0;
				// BEGIN TIME
				Calendar begintime = Calendar.getInstance();
				Date begindatetime = new Date();
				begindatetime.setHours(inthour);
				begindatetime.setMinutes(intminute);
				begindatetime.setSeconds(0);
				begintime.setTime(begindatetime);
				begintime.set(Calendar.MILLISECOND, 0);
				
				appt.setBeginDateTime(begintime);
				// END TIME
				if(endhour == null || endhour.equalsIgnoreCase("select") 
						|| endhour.equalsIgnoreCase("") || endhour.equalsIgnoreCase("select") 
						|| endminute == null || endminute.equalsIgnoreCase("") || endminute.equalsIgnoreCase("select")
						) {
					// end time not selected so use service time plus begin time
					Date enddatetime = new Date();
					Calendar endtime = Calendar.getInstance();
	
					enddatetime.setHours(inthour);
					enddatetime.setMinutes(intminute+time);
					enddatetime.setSeconds(0);
					endtime.setTime(enddatetime);
					endtime.set(Calendar.MILLISECOND, 0);
					
					appt.setEndDateTime(endtime);
				}else{
					// end time selected so use it					
					Date enddatetime = new Date();
					Calendar endtime = Calendar.getInstance();
	
					enddatetime.setHours(intehour);
					enddatetime.setMinutes(inteminute);
					enddatetime.setSeconds(0);
					endtime.setTime(enddatetime);
					endtime.set(Calendar.MILLISECOND, 0);
					
					appt.setEndDateTime(endtime);
				}
				appt.setServices(set);
				log.debug("about to try to persist the appointment "+appt.toString());
				appt.persist();
				// adding audit log
				String appointmentbegintime = "";
				SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm:ss a");
				appointmentbegintime = dateformatter.format(appt.getBeginDateTime().getTime());
				
				SimpleDateFormat dateformatterappt = new SimpleDateFormat("MM/dd/yyyy");
				String originaldate = dateformatterappt.format(appt.getAppointmentDate());
				String staffname = "";
				if(appt.getStaff() != null){
					staffname = appt.getStaff().getFirstName() + " " + appt.getStaff().getLastName();
				}
				saveAuditMessage("PERSONAL TIME CREATED:  "+ originaldate+" Begin time: "+ appointmentbegintime+" by staff " +staffname,appt.getStaff(),"GENERAL");
				
				log.debug("persisted appointment");
			}catch(Exception ed){
				log.error(ed);
				successstatus += " failure ";
			}
		}					
		List<AppointmentDeep> apptdeep = getTodaysAppointments(hSelectDate);
		
        log.debug("Exited createPersonalApptJson method");
        return successstatus;
    }   
    @RequestMapping(value="/updatepersonal", method=RequestMethod.POST)
    public @ResponseBody  String updatePersonalApptJson(
    		@RequestParam(value="appointmentid",required=true) String apptid,
    		@RequestParam(value="staff",required=true) String staff,
    		@RequestParam(value="hSelectDate",required=true) String hSelectDate,
    		@RequestParam(value="hour",required=true) String hour, 
    		@RequestParam(value="minute",required=true) String minute,
    		@RequestParam(value="ampm",required=true) String ampm, 
    		@RequestParam(value="notes",required=true) String notes,
    		@RequestParam(value="label",required=true) String label,
    		@RequestParam(value="ehour",required=true) String endhour, 
    		@RequestParam(value="eminute",required=true) String endminute,
    		@RequestParam(value="eampm",required=true) String endampm 
    		
    		) {
    	String successstatus = "success";
    	log.debug("Entered updatePersonalApptJson method");

    	log.debug("the selected date: "+hSelectDate);
        log.debug("hour: "+hour);
        log.debug("minute: "+minute);
        log.debug("ampm: "+ampm);
        log.debug("note: "+notes);
        log.debug("end hour: "+endhour);
        log.debug("end minute: "+endminute);
        log.debug("end ampm: "+endampm);
        
		Long long_staff_id = Long.parseLong(staff);
		Staff found_staff = Staff.findStaff(long_staff_id);
        
        Appointment appt = Appointment.findAppointment(Long.parseLong(apptid));
        
        int inthour = Integer.parseInt(hour);
        int intminute = Integer.parseInt(minute);
        if(ampm.equalsIgnoreCase("am")){
        	inthour = Integer.parseInt(hour);
        }else{
        	if(inthour != 12){
    			inthour = 12 + Integer.parseInt(hour);
        	}
        }
        int intehour = 0;
        int inteminute = 0;
        if(endhour != null && !endhour.equalsIgnoreCase("select") && endampm != null && !endampm.equalsIgnoreCase("select") && !endminute.equalsIgnoreCase("select")){
            intehour = Integer.parseInt(endhour);
            inteminute = Integer.parseInt(endminute);
            if(endampm.equalsIgnoreCase("am")){
            	intehour = Integer.parseInt(endhour);
            }else{
            	if(intehour != 12){
            		intehour = 12 + Integer.parseInt(endhour);
            	}
            }
        	
        }
        // save addappointment data
		if(getShop() == null){
			setShop(getShop());
		}
		TypedQuery<Staff> staffs = Staff.findStaffsByShop(getShop());
		if(staffs.getResultList().size() > 0){
			log.debug("found at least one service to assing to an appointment");
			try{
				// format date
				String dateString = hSelectDate; 
				log.debug("dateString: "+dateString);
			    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			    Date apptDate = null; 
			    try {
			    	log.debug("ABOUT TO TRANSFORM DATE");
					if(hSelectDate == null || hSelectDate.equalsIgnoreCase("")){
						dateString = dateFormat.format(new Date());
						apptDate = dateFormat.parse(dateString);
					}else{
						apptDate = dateFormat.parse(dateString);	
					}
			    	
			    	
			    	log.debug("the transformed datestring into the apptDate is: "+apptDate.toString());
				} catch (ParseException e) {
					log.error(e);
					e.printStackTrace();
					successstatus = " failure to transform date ";
				}			
				TypedQuery<BaseService>  tqservice = BaseService.findBaseServicesByShop(getShop());
				Set<BaseService> set = new HashSet<BaseService>();
				appt.setStaff(found_staff);
				appt.setAppointmentDate(apptDate);
				appt.setCreateddate(new Date());
				appt.setDescription("Appointment "+apptDate);
				appt.setStarttime(0);
				appt.setCancelled(false);
				appt.setEndtime(0);
				appt.setPersonallabel(label);
				appt.setStatus(ScheduleStatus.ACTIVE);
				int time = 0;
				// BEGIN TIME
				Calendar begintime = Calendar.getInstance();
				Date begindatetime = new Date();
				begindatetime.setHours(inthour);
				begindatetime.setMinutes(intminute);
				begindatetime.setSeconds(0);
				begintime.setTime(begindatetime);
				begintime.set(Calendar.MILLISECOND, 0);
				
				appt.setBeginDateTime(begintime);
				// END TIME
				if(endhour == null || endhour.equalsIgnoreCase("select") 
						|| endhour.equalsIgnoreCase("") || endhour.equalsIgnoreCase("select") 
						|| endminute == null || endminute.equalsIgnoreCase("") || endminute.equalsIgnoreCase("select")
						) {
					// end time not selected so use service time plus begin time
					Date enddatetime = new Date();
					Calendar endtime = Calendar.getInstance();
	
					enddatetime.setHours(inthour);
					enddatetime.setMinutes(intminute+time);
					enddatetime.setSeconds(0);
					endtime.setTime(enddatetime);
					endtime.set(Calendar.MILLISECOND, 0);
					
					appt.setEndDateTime(endtime);
				}else{
					// end time selected so use it					
					Date enddatetime = new Date();
					Calendar endtime = Calendar.getInstance();
	
					enddatetime.setHours(intehour);
					enddatetime.setMinutes(inteminute);
					enddatetime.setSeconds(0);
					endtime.setTime(enddatetime);
					endtime.set(Calendar.MILLISECOND, 0);
					
					appt.setEndDateTime(endtime);
				}
				appt.setServices(set);
				log.debug("about to try to persist the appointment "+appt.toString());
				appt.persist();
				log.debug("FINISHED CREATING PERSONAL TIME. NOW ABOUT TO PERSIST AUDIT MESSAGE");
				// adding audit log
				try{
					log.debug("TRYING TO AUDIT MESSAGE");
					String appointmentbegintime = "";
					SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm:ss a");
					appointmentbegintime = dateformatter.format(appt.getBeginDateTime().getTime());
					SimpleDateFormat dateformatterappt = new SimpleDateFormat("MM/dd/yyyy");
					String originaldate = dateformatterappt.format(appt.getAppointmentDate());
					String staffname = "";
					if(appt.getStaff() != null){
						staffname = appt.getStaff().getFirstName() + " " + appt.getStaff().getLastName();
					}
					log.debug("tracing  8");
					String msg = "PERSONAL TIME UPDATED : "+ originaldate+" Begin time "+ appointmentbegintime+" by staff " +staffname;
					log.debug("AUDIT MESSAGE: "+msg);
					saveAuditMessage(msg,appt.getStaff(),"GENERAL");
					log.debug("PAST AUDITING MESSAGE");
				}catch(Exception auditE){
					log.error(auditE);
				}
				log.debug("persisted appointment");
			}catch(Exception ed){
				log.error(ed);
				successstatus += " failure ";
			}
		}					
		List<AppointmentDeep> apptdeep = getTodaysAppointments(hSelectDate);
		
        log.debug("Exited updatePersonalApptJson method");
        return successstatus;
    }       
    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(AddAppointmentForm addappointment,Model uiModel) {
    	log.debug("Entered MyScheduleController createForm method");
        uiModel.addAttribute("appointment", new Appointment());
        addDateTimeFormatPatterns(uiModel);
        List dependencies = new ArrayList();
        if (Staff.countStaffs() == 0) {
            dependencies.add(new String[]{"staff", "staffs"});
        }
        if (Clients.countClientses() == 0) {
            dependencies.add(new String[]{"clients", "clientses"});
        }
        if (Shop.countShops() == 0) {
            dependencies.add(new String[]{"shop", "shops"});
        }
        uiModel.addAttribute("dependencies", dependencies);
        log.debug("Exit MyScheduleController createForm method");
        return "myschedule";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("appointment", Appointment.findAppointment(id));
        uiModel.addAttribute("itemId", id);
        return "myschedule/show";
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Appointment appointment, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
    	log.debug("Entered MyScheduleController update method");
    	if (bindingResult.hasErrors()) {
            uiModel.addAttribute("appointment", appointment);
            addDateTimeFormatPatterns(uiModel);
            return "myschedule/update";
        }
        uiModel.asMap().clear();
        appointment = saveAppointment(appointment);
		// adding audit log
		String appointmentbegintime = "";
		SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm:ss a");
		appointmentbegintime = dateformatter.format(appointment.getBeginDateTime().getTime());
		
		SimpleDateFormat dateformatterappt = new SimpleDateFormat("MM/dd/yyyy");
		String originaldate = dateformatterappt.format(appointment.getAppointmentDate());
		String staffname = "";
		if(appointment.getStaff() != null){
			staffname = appointment.getStaff().getFirstName() + " " + appointment.getStaff().getLastName();
		}
		saveAuditMessage("Appointment for "+ originaldate+" "+ appointmentbegintime+" Client: "+appointment.getClient().getFirstName()+" "+appointment.getClient().getLastName()+" UPDATED by staff " +staffname,appointment.getStaff(),"GENERAL");
        
        log.debug("Exiting MyScheduleController update method");
        return "redirect:/myschedule/" + encodeUrlPathSegment(appointment.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("appointment", Appointment.findAppointment(id));
        addDateTimeFormatPatterns(uiModel);
        return "myschedule/update";
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        Appointment.findAppointment(id).remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/myschedule";
    }
    
    void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("appointment_reoccur_start_date_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("appointment_appointmentdate_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("appointment_checkintime_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("appointment_checkouttime_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("appointment_confirmeddate_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("appointment_createddate_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
    }
    
    String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        }
        catch (UnsupportedEncodingException uee) {}
        return pathSegment;
    }

	public Audit getAudit() {
		return audit;
	}

	public void setAudit(Audit audit) {
		this.audit = audit;
	}

	public GeneralNotificationHelper getGeneralNotificationHelper() {
		return generalNotificationHelper;
	}
	public void setGeneralNotificationHelper(GeneralNotificationHelper generalNotificationHelper) {

		this.generalNotificationHelper = generalNotificationHelper;
	}

	public UserPreference getPreferences() {
		return preferences;
	}

	public void setPreferences(UserPreference preferences) {
		this.preferences = preferences;
	}
	public GCalNotificationHelper getGcalNotificationHelper() {
		return gcalNotificationHelper;
	}
	public void setGcalNotificationHelper(GCalNotificationHelper gcalNotificationHelper) {
		this.gcalNotificationHelper = gcalNotificationHelper;
	}
	public GeneralSMSNotificationHelper getGeneralSMSNotificationHelper() {
		return generalSMSNotificationHelper;
	}
	public void setGeneralSMSNotificationHelper(
			GeneralSMSNotificationHelper generalSMSNotificationHelper) {
		this.generalSMSNotificationHelper = generalSMSNotificationHelper;
	}
	public GCalSMSNotificationHelper getGcalSMSNotificationHelper() {
		return gcalSMSNotificationHelper;
	}
	public void setGcalSMSNotificationHelper(GCalSMSNotificationHelper gcalSMSNotificationHelper) {
		this.gcalSMSNotificationHelper = gcalSMSNotificationHelper;
	}

}
