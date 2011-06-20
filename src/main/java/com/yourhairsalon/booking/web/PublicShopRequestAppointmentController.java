package com.yourhairsalon.booking.web;

import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.yourhairsalon.booking.domain.Appointment;
import com.yourhairsalon.booking.domain.AppointmentDeep;
import com.yourhairsalon.booking.domain.BaseService;
import com.yourhairsalon.booking.domain.Clients;
import com.yourhairsalon.booking.domain.Communications;
import com.yourhairsalon.booking.domain.CustomService;
import com.yourhairsalon.booking.domain.Settings;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.ShopSettings;
import com.yourhairsalon.booking.domain.Staff;
import com.yourhairsalon.booking.form.AppointmentNotification;
import com.yourhairsalon.booking.form.RegisterForm;
import com.yourhairsalon.booking.reference.CommType;
import com.yourhairsalon.booking.reference.ScheduleStatus;
import com.yourhairsalon.booking.task.RegistrationNotificationHelper;

import flexjson.JSONSerializer;

@PreAuthorize("hasRole('ROLE_CUSTOMERSCUSTOMER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_CUSTOMERADMIN')")
@RequestMapping("/public/shop/{shopname}/requestappointment/**")
@Controller
public class PublicShopRequestAppointmentController {
	
	private static final Log log = LogFactory.getLog(PublicShopRequestAppointmentController.class);
	private Shop newshop;
	@Autowired
    private transient RegistrationNotificationHelper registrationNotificationHelper;

	/**
	 * 
	 * send message of notification of appointment request to both the customer and shop owner
	 * 
	 * @param regform
	 * @return
	 */
	private boolean sendMessage(RegisterForm regform,String shopname){
		try{
			String username = regform.getUsername();
			AppointmentNotification an = new AppointmentNotification();
			an.setAppointmentdate(new Date());
			an.setClientname(regform.getFirstname() + " " + regform.getLastname());
			an.setDescription("Notification of request for appointment "); 
			an.setEmail(regform.getEmail());// email address to be used to send to
			an.setFromshop(shopname);
			an.setShop_email_address("donotreply@scheduleem.com");
			an.setShop_email_message("Welcome to "+shopname+". You may login to your new account at http://www.scheduleem.com/shop/public/shop/"+shopname+"");
			an.setShop_email_signature("Sincerely, "+shopname);
			an.setShop_email_subject("Notification of registration at "+shopname);
			System.setProperty("file.encoding", "UTF-8");
			getRegistrationNotificationHelper().sendMessage(an);
		}catch(Exception e){
			log.error(e.getMessage());
			return false;
		}
		return true;
	}
	private boolean sendClientMessage(Date appointmentdate, String firstname, String lastname, String username, String shopname, String email,String msg){
		try{
			AppointmentNotification an = new AppointmentNotification();
			try{
				an.setAppointmentdate(appointmentdate);
			}catch(Exception e){
				log.error(e);
			}
			
			an.setClientname(firstname + " " + lastname);
			an.setDescription("Notification of request for appointment "); 
			an.setEmail(email);// email address to be used to send to
			an.setFromshop(shopname);
			an.setShop_email_address("donotreply@scheduleem.com");
			an.setShop_email_message(msg);
			an.setShop_email_signature("Sincerely, "+shopname);
			an.setShop_email_subject("Notification of request for appointment at "+shopname);
			System.setProperty("file.encoding", "UTF-8");
			getRegistrationNotificationHelper().sendMessage(an);
		}catch(Exception e){
			log.error(e.getMessage());
			return false;
		}
		return true;
	}
	private boolean sendShopNotificationMessage(Date appointmentdate, String firstname, String lastname, String username, String shopname, String email,String msg){
		try{
			AppointmentNotification an = new AppointmentNotification();
			try{
				an.setAppointmentdate(appointmentdate);
			}catch(Exception e){
				log.error(e);
			}
			an.setClientname(firstname + " " + lastname);
			an.setDescription("Notification of request for appointment ");

			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			
			Settings settings = new Settings(shopsetting);
			if(!settings.getEmail_address().equalsIgnoreCase("")){
				an.setEmail(settings.getEmail_address());// email address to be used to send to shop
				an.setFromshop(shopname);
				an.setShop_email_address("donotreply@scheduleem.com");
				an.setShop_email_message(msg);
				an.setShop_email_signature("Sincerely, "+shopname);
				an.setShop_email_subject("Notification of request for appointment at "+shopname);
				System.setProperty("file.encoding", "UTF-8");
				getRegistrationNotificationHelper().sendMessage(an);
			}
		}catch(Exception e){
			log.error(e.getMessage());
			return false;
		}
		return true;
	}
	
	private Clients getClient(){
		Clients thisclient = null;
    	Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (obj instanceof UserDetails) {
			String username = ((UserDetails) obj).getUsername();
			if (!username.equalsIgnoreCase("anonymous")) {
				log.info("LOGGED IN USER: " + username);
				TypedQuery<Clients> testclients = Clients.findClientsesByUsernameEquals(username);
				if(testclients.getResultList().size() > 0){
					log.debug("client "+username+" found");
					Clients client = testclients.getResultList().get(0);
					thisclient = client;
				}
			}
		}
		return thisclient;
	}
	
	private Shop getShop(){
		Shop thisshop = null;
    	Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	if(obj instanceof InetOrgPerson) {
			log.debug("The principal object is InetOrgPerson");
			String parentshop = ((InetOrgPerson) obj).getO();
			String username = ((UserDetails) obj).getUsername();
			log.info("parentshop of LOGGED IN USER: " + parentshop);
			log.info("username of LOGGED IN USER: " + username);
			if (!username.equalsIgnoreCase("anonymous")) {
				log.info("LOGGED IN USER: " + username);
				if(parentshop == null){
					parentshop = username;
				}
				TypedQuery<Clients> testclients = Clients.findClientsesByUsernameEquals(username);
				if(testclients.getResultList().size() > 0){
					log.debug("client "+username+" found");
					Clients client = testclients.getResultList().get(0);
					thisshop = client.getShop();
				}
			}
		}else if (obj instanceof UserDetails) {
			String username = ((UserDetails) obj).getUsername();
			if (!username.equalsIgnoreCase("anonymous")) {
				log.info("LOGGED IN USER: " + username);
				TypedQuery<Clients> testclients = Clients.findClientsesByUsernameEquals(username);
				if(testclients.getResultList().size() > 0){
					log.debug("client "+username+" found");
					Clients client = testclients.getResultList().get(0);
					thisshop = client.getShop();
				}
			}
		}
		return thisshop;
	}
	
    @RequestMapping(method = RequestMethod.POST, value = "{id}")
    public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    }


    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView index(
    		@PathVariable String shopname,
    		Model uiModel) {
    	log.debug("ENTERED Public Shop Request Appointmnent Index ");
		ModelAndView mav = new ModelAndView();
		log.debug("trying to display standard");
		mav.setViewName("public/shop/requestappointment/index");
		log.debug("EXITING Public Shop Request Appointmnent Index");
        return mav;
    }
	@ModelAttribute("services")
    public Map<String,String> populateServices() {
		Map map = new HashMap();
		try {
			List<BaseService> list = BaseService.findBaseServicesByShop(
					getShop()).getResultList();

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
					if (((CustomService) base).getClient().getId() == getClient()
							.getId()) {
						log.debug("NOT REMOVING CUSTOM SERVICE");
						map.put(new String(sid), new String(desc + " "
								+ amttime + " minutes"));
					} else {
						log.debug("REMOVING CUSTOM SERVICE");
						i.remove();
					}
				} else {
					log.debug("NOT CUSTOM SERVICE");
					map.put(new String(sid), new String(desc + " " + amttime
							+ " minutes"));
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		return map;
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
    		@RequestParam(value="eampm",required=true) String endampm,
    		@RequestParam(value="sid",required=true) String sid
    		) {
		// list only BaseService for the Shop, not Custom
		log.debug("Entering checkDateTime");
		/* 
		 * the end times are dummy values.
		 * calculate what they should actually be. This will require the service id to be a parameter 
		*/
		String amountoftime = getServiceTime(Integer.parseInt(sid));
		
		String conflict = "false";
		
        int inthour = Integer.parseInt(hour);
        int intminute = Integer.parseInt(minute);
        if(ampm.equalsIgnoreCase("am")){
        	inthour = Integer.parseInt(hour);
        }else{
        	inthour = 12 + Integer.parseInt(hour);
        }
        int intehour = 0;
        int inteminute = 0;
        if(endhour != null && !endhour.equalsIgnoreCase("select") && endampm != null && !endampm.equalsIgnoreCase("select") && !endminute.equalsIgnoreCase("select")){
            intehour = Integer.parseInt(endhour);
            inteminute = Integer.parseInt(endminute);
            if(endampm.equalsIgnoreCase("am")){
            	intehour = Integer.parseInt(endhour);
            }else{
            	intehour = 12 + Integer.parseInt(endhour);
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
		enddatetime.setHours(begindatetime.getHours());
		// begin time minutes plus amountoftime
		enddatetime.setMinutes(begindatetime.getMinutes() + Integer.parseInt(amountoftime));// adjusting for lack of precision to evaluate microseconds otherwise query will not work
		enddatetime.setSeconds(0);
		endtime.setTime(enddatetime);
		
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
    		@RequestParam(value="clientid",required=true) String clientid, 
    		@RequestParam(value="svcid",required=true) String svcid, 
    		@RequestParam(value="hour",required=true) String hour, 
    		@RequestParam(value="minute",required=true) String minute,
    		@RequestParam(value="ampm",required=true) String ampm, 
    		@RequestParam(value="notes",required=true) String notes,
    		@RequestParam(value="ehour",required=true) String endhour, 
    		@RequestParam(value="eminute",required=true) String endminute,
    		@RequestParam(value="eampm",required=true) String endampm 
    		
    		) {
    	String successstatus = "success";
    	log.debug("Entered createApptJson method");

    	log.debug("the selected date: "+hSelectDate);
        log.debug("service 1: "+svcid);
        log.debug("hour: "+hour);
        log.debug("minute: "+minute);
        log.debug("ampm: "+ampm);
        clientid = ""+getClient().getId();
        log.debug("client id: "+clientid);
        log.debug("note: "+notes);
        log.debug("end hour: "+endhour);
        log.debug("end minute: "+endminute);
        log.debug("end ampm: "+endampm);
        
        String amountoftime = getServiceTime(Integer.parseInt(svcid));
        
        int inthour = Integer.parseInt(hour);
        int intminute = Integer.parseInt(minute);
        if(ampm.equalsIgnoreCase("am")){
        	inthour = Integer.parseInt(hour);
        }else{
        	inthour = 12 + Integer.parseInt(hour);
        }
        int intehour = 0;
        int inteminute = 0;
        if(endhour != null && !endhour.equalsIgnoreCase("select") && endampm != null && !endampm.equalsIgnoreCase("select") && !endminute.equalsIgnoreCase("select")){
            intehour = Integer.parseInt(endhour);
            inteminute = Integer.parseInt(endminute);
            if(endampm.equalsIgnoreCase("am")){
            	intehour = Integer.parseInt(endhour);
            }else{
            	intehour = 12 + Integer.parseInt(endhour);
            }
        	
        }
        // save addappointment data
		if(getNewshop() == null){
			setNewshop(getShop());
		}
		TypedQuery<Staff> staffs = Staff.findStaffsByShop(getNewshop());
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
				// add an appointment
			    String emailmsg = "";
				Appointment appt = new Appointment();
				appt.setShop(getShop());
				Clients client = Clients.findClients(Long.parseLong(clientid));
				appt.setClient(client);
				emailmsg += "Pending appointment for " +client.getFirstName() + " " + client.getLastName();
				// get email for client
				Set<Communications> comm = client.getCommunication();
				String client_email = "";
				String client_cellphone = "";
				String client_homephone = "";
				String client_workphone = "";
				for (Iterator i = comm.iterator(); i.hasNext();) {
					Communications base = (Communications) i.next();
					if(base.getCommunication_type().equals(CommType.EMAIL)){
						client_email = base.getCommunication_value();
						emailmsg += " Client email address: "+ client_email;
					}
					if(base.getCommunication_type().equals(CommType.CELL_PHONE)){
						client_cellphone = base.getCommunication_value();
						emailmsg += " Client cell phone: "+ client_cellphone;
					}
					if(base.getCommunication_type().equals(CommType.HOME_PHONE)){
						client_homephone = base.getCommunication_value();
						emailmsg += " Client home phone: "+ client_homephone;
					}
					if(base.getCommunication_type().equals(CommType.WORK_PHONE)){
						client_workphone = base.getCommunication_value();
						emailmsg += " Client work phone: "+ client_workphone;
					}
				}
				
				appt.setStaff(staffs.getResultList().get(0));
				TypedQuery<BaseService>  tqservice = BaseService.findBaseServicesByShop(getShop());
				Set<BaseService> set = new HashSet<BaseService>();
				appt.setAppointmentDate(apptDate);
				emailmsg += " "+ apptDate;
				appt.setCreateddate(new Date());
				appt.setDescription("Appointment "+apptDate);
				appt.setStarttime(0);
				appt.setCancelled(false);
				appt.setEndtime(0);
				appt.setNotes(notes);
				appt.setStatus(ScheduleStatus.PENDING);
				BaseService svc1 = BaseService.findBaseService(Long.parseLong(svcid));
				int time = 0;
				if(svc1.getAmounttime() >= svc1.getLength_time()){
					time = svc1.getAmounttime();
				}else{
					time = svc1.getLength_time();
				}
				emailmsg += " Service: " + svc1.getDescription();
				// BEGIN TIME
				Calendar begintime = Calendar.getInstance();
				Date begindatetime = (Date)apptDate.clone();
				begindatetime.setHours(inthour);
				begindatetime.setMinutes(intminute);
				begintime.setTime(begindatetime);
				emailmsg += " Begin Time: " + begindatetime;
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
					endtime.setTime(enddatetime);
					
					
					appt.setEndDateTime(endtime);
					set.add(svc1);
				}else{
					// end time selected so use it					
					Date enddatetime = (Date)apptDate.clone();
					Calendar endtime = Calendar.getInstance();
	
					enddatetime.setHours(begindatetime.getHours());
					enddatetime.setMinutes(begindatetime.getMinutes() + Integer.parseInt(amountoftime));
					endtime.setTime(enddatetime);
					
					
					appt.setEndDateTime(endtime);
					set.add(svc1);
				}
				emailmsg += " End Time: " + appt.getEndDateTime();
				appt.setServices(set);
				log.debug("about to try to persist the appointment "+appt.toString());
				if(!client_email.equalsIgnoreCase("")){
						sendClientMessage(appt.getAppointmentDate(),client.getFirstName(), client.getLastName(), client.getUsername(),getShop().getShop_name(),client_email,emailmsg);
						sendShopNotificationMessage(appt.getAppointmentDate(),client.getFirstName(), client.getLastName(), client.getUsername(),getShop().getShop_name(),client_email,emailmsg);
				}
				appt.persist();
				log.debug("persisted appointment");
			}catch(Exception ed){
				log.error(ed);
				successstatus += " failure ";
			}
		}					
		List<AppointmentDeep> apptdeep = getTodaysAppointments(hSelectDate);
		
        log.debug("Exited createApptJson method");
        return successstatus;
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
		log.debug("the value of beginofday: "+beginofday);
		
		endofday.setHours(23);
		endofday.setMinutes(59);
		log.debug("the value of endofday: "+endofday);
		log.debug("the shop is: "+getNewshop().toString());
		TypedQuery<Appointment> appointments = Appointment.findAppointmentsByShopAndStatusNotAndAppointmentDateBetween(getNewshop(), ScheduleStatus.DELETED, beginofday, endofday);
		log.debug("getTodaysAppointments -- the number of appointments is: "+appointments.getResultList().size());
		List<AppointmentDeep> apptdeep = new ArrayList<AppointmentDeep>();
		if(appointments.getResultList().size()>0){
			for(int x=0;x<appointments.getResultList().size();x++){
				Appointment appt = appointments.getResultList().get(x);
				log.debug("The toString of appt: "+ appt.toString());
				AppointmentDeep deep = new AppointmentDeep(appt);
				log.debug("The toString of DEEP appt: "+ deep.toString());
				apptdeep.add(deep);
			}
			
		}
		log.debug("EXITING getTodaysAppointments ");
		return apptdeep;
	}
    public String getServiceTime(long id) {
		// list only BaseService for the Shop, not Custom
		log.debug("Entering getServiceTime");
		BaseService list = BaseService.findBaseService(id);
		String time = ""+list.getAmounttime();
		

		log.debug("Exiting getServiceTime");
        return time;
    }

	public Shop getNewshop() {
		return newshop;
	}

	public void setNewshop(Shop newshop) {
		this.newshop = newshop;
	}

	public RegistrationNotificationHelper getRegistrationNotificationHelper() {
		return registrationNotificationHelper;
	}

	public void setRegistrationNotificationHelper(
			RegistrationNotificationHelper registrationNotificationHelper) {
		this.registrationNotificationHelper = registrationNotificationHelper;
	}    
}
