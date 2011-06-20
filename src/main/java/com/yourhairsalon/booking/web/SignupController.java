package com.yourhairsalon.booking.web;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.yourhairsalon.booking.domain.AbstractService;
import com.yourhairsalon.booking.domain.Addresses;
import com.yourhairsalon.booking.domain.Appointment;
import com.yourhairsalon.booking.domain.BaseService;
import com.yourhairsalon.booking.domain.Clients;
import com.yourhairsalon.booking.domain.Communications;
import com.yourhairsalon.booking.domain.CustomService;
import com.yourhairsalon.booking.domain.Person;
import com.yourhairsalon.booking.domain.PersonDaoImpl;
import com.yourhairsalon.booking.domain.Registration;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.ShopSettings;
import com.yourhairsalon.booking.domain.Staff;
import com.yourhairsalon.booking.form.AddAppointmentForm;
import com.yourhairsalon.booking.form.AppointmentNotification;
import com.yourhairsalon.booking.form.RegisterForm;
import com.yourhairsalon.booking.reference.ClientDisplay;
import com.yourhairsalon.booking.reference.CommType;
import com.yourhairsalon.booking.reference.RegistrationTypes;
import com.yourhairsalon.booking.reference.ScheduleStatus;
import com.yourhairsalon.booking.task.RegistrationNotificationHelper;

import flexjson.JSONSerializer;

import org.springframework.beans.factory.annotation.Autowired;

@RequestMapping("/public/signup")
@Controller
public class SignupController {
	private static final Log log = LogFactory.getLog(SignupController.class);
    
	@Autowired
    private transient RegistrationNotificationHelper registrationNotificationHelper;
    
	@Autowired
	private PersonDaoImpl personDao;

	@RequestMapping(method = RequestMethod.POST)
    public ModelAndView registerForm(@Valid RegisterForm register,  BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
    	log.debug("ENTERED registerForm");
    	ModelAndView mav = new ModelAndView();
    	mav.setView(new RedirectView("public/signup/index"));
    	if (bindingResult.hasErrors()) {
    		log.error("THERE IS AN ERROR WITH THE REGISTER FORM: ");
    		if(register != null) log.error("REGISTERFORM: " + register.toString()); 
            uiModel.addAttribute("register", register);
            return mav;
        }
    	if(!register.getTest().equalsIgnoreCase("reggae")){
    		log.error("THERE IS AN ERROR WITH THE REGISTER TEST POSSIBLY A SPAM BOT ATTEMPT: ");
    		if(register != null) log.error("REGISTERFORM: " + register.toString()); 
            uiModel.addAttribute("register", register);
            return mav;
    	}
    	if(register != null) log.debug("REGISTERFORM: " + register.toString());
    	// persist ldap
    	persistLdap(register,register.getUsername());
    	// persist jpa
    	persistJpa(register);
    	// send emails to person registering
    	System.setProperty("file.encoding", "UTF-8");
    	log.debug("ABOUT TO SEND MESSAGE");
    	sendMessage(register);
    	// view login page
    	mav.setView(new RedirectView("../"));
    	
    	RegisterForm regform = new RegisterForm();
    	uiModel.addAttribute("register", regform);
    	log.debug("EXITING registerForm");
        return mav;
    }
	
	private boolean sendMessage(RegisterForm regform){
		log.debug("ENTERED sendMessage FOR REGISTRATION");
		try{
			Properties properties = new Properties();
			String bcc = "tim@jeffcoat.net";
			String message = "";
			String parent_owner_name = "";
			String registration_from_email = "";
			String signature = "";
			String subject = "";
			log.debug("trace 1");
			try {
			    properties.load(this.getClass().getClassLoader().getResourceAsStream("application_reg.properties"));
			    bcc = properties.getProperty("registration_bcc");
			    parent_owner_name = properties.getProperty("registration_shop");
			    message = properties.getProperty("registration_message");
			    registration_from_email = properties.getProperty("registration_from_email");
			    signature = properties.getProperty("registration_signature");
			    subject = properties.getProperty("registration_subject");
			} catch (IOException e) {
				log.error(e);
			}			
			
			String username = regform.getUsername();
			AppointmentNotification an = new AppointmentNotification();
			an.setAppointmentdate(new Date());
			String client_firstname = regform.getFirstname();
			String client_lastname = regform.getLastname();
			String client_fullname = client_firstname + " " + client_lastname;
			an.setClientname(client_fullname);
			an.setDescription("Notification of registration");
			an.setEmail(regform.getEmail());
			an.setFromshop("Shop For " + username);
			log.debug("trace 16");
			if(registration_from_email != null && registration_from_email.equalsIgnoreCase("")){
				log.debug("trace 17");
				an.setShop_email_address(registration_from_email);
				log.debug("trace 18");
			}else{
				log.debug("trace 19");
				an.setShop_email_address("registration@scheduleem.com");
				log.debug("trace 20");
			}
			log.debug("trace 21");
			if(message != null && message.equalsIgnoreCase("")){
				message = message.replaceAll(Pattern.quote("${clientfullname}"), client_fullname);
				message = message.replaceAll(Pattern.quote("${clientfirstname}"), client_firstname);
				message = message.replaceAll(Pattern.quote("${clientlastname}"), client_lastname);
				log.debug("trace 22");
				an.setShop_email_message(message);
				log.debug("trace 23");
			}else{
				log.debug("trace 24");
				an.setShop_email_message("Welcome to scheduleem.com. You may login to your new account at http://www.scheduleem.com/shop");
				log.debug("trace 25");
			}
			log.debug("trace 26");
			if(signature != null && signature.equalsIgnoreCase("")){
				log.debug("trace 27");
				an.setShop_email_signature(signature);
				log.debug("trace 28");
			}else{
				log.debug("trace 29");
				an.setShop_email_signature("Sincerely, Scheduleem.com");
				log.debug("trace 30");
			}
			log.debug("trace 31");
			if(subject != null && subject.equalsIgnoreCase("")){
				log.debug("trace 32");
				an.setShop_email_subject(subject);
				log.debug("trace 33");
			}else{
				log.debug("trace 34");
				an.setShop_email_subject("Notification of registration at scheduleem.com");
				log.debug("trace 35");
			}
			log.debug("trace 36");
			System.setProperty("file.encoding", "UTF-8");
			log.debug("SENDING MESSAGE TO QUE");
			getRegistrationNotificationHelper().sendMessage(an);
			log.debug("JUST SENT MESSAGE TO QUE");
		}catch(Exception e){
			log.error(e.getMessage());
			return false;
		}
		log.debug("EXITING sendMessage FOR REGISTRATION");
		return true;
	}
	
    private boolean isNameAvailable(String name) {
		log.debug("Entering isNameAvailable");
		
		TypedQuery<Shop> comparename = Shop.findShopsByShop_urlEquals(name);
		int num = comparename.getResultList().size();
		log.debug("comparename: "+comparename);
		log.debug("name: "+name);
		boolean isavail = false;
		if(num == 0){
			log.debug("entered first. this name is available");
			isavail = true;
		}else{
			log.debug("entered second. the number of matching shop names is: " + num);
			isavail = false;
		}
		log.debug("Exiting isNameAvailable");
        return isavail;
    }
	
	private boolean persistJpa(RegisterForm regform){
		boolean rtn = true;
		String username = regform.getUsername();
		// create new shop
		Shop newshop = new Shop();
		String shopname = "";
		shopname = username;
		newshop.setShop_name(shopname);
		String resultString = shopname.replaceAll("[^\\p{L}\\p{N}]", "");
		boolean isavailable = isNameAvailable(resultString.toLowerCase());
		if(isavailable){
			// now set the url
			newshop.setShop_url(resultString.toLowerCase());
		}else{
			// TODO: what to do when shop url is not available?
		}
		newshop.setShopuuid(username);
		newshop.setType(RegistrationTypes.TRIAL);
		newshop = newshop.merge();
    	// persist a record of transaction in the Registration entity
    	Registration reg = new Registration();
    	reg.setDatecreated(new Date());
    	reg.setType(RegistrationTypes.INITIAL);
    	reg.setShop(newshop);
    	reg.merge();
		
		// create a sample client
		Clients clients = new Clients();
		clients.setShop(newshop);
		clients.setFirstName("Jane");
		clients.setLastName("Doe");
		clients.setBirthDay(new Date());
		clients.persist();
			// add address
		Addresses addr = new Addresses();
		addr.setAddress1("address 1");
		addr.setAddress2("address 2");
		addr.setCitycode("SLC");
		addr.setStatecode("UT");
		addr.setZipcode("84555");
		addr.setShop(newshop);
		addr.setPerson(clients);
		addr.persist();
			// add communication
		Communications communications = new Communications();
		communications.setPerson(clients);
		communications.setShop(newshop);
		communications.setCommunication_type(CommType.CELL_PHONE);
		communications.setCommunication_value("801-555-5555");
		communications.persist();
		Communications communications2 = new Communications();
		communications2.setPerson(clients);
		communications2.setShop(newshop);
		communications2.setCommunication_type(CommType.EMAIL);
		communications2.setCommunication_value("jame@doe.com");
		communications2.persist();
		Communications communications3 = new Communications();
		communications3.setPerson(clients);
		communications3.setShop(newshop);
		communications3.setCommunication_type(CommType.HOME_PHONE);
		communications3.setCommunication_value("456");
		communications3.persist();
		Communications communications4 = new Communications();
		communications4.setPerson(clients);
		communications4.setShop(newshop);
		communications4.setCommunication_type(CommType.WORK_PHONE);
		communications4.setCommunication_value("123");
		communications4.persist();
		// add new shop settings
		log.debug("starting shopsettings");
		Properties properties = new Properties();
		String initial_email="<p>	Dear ${clientfullname},</p><p>	Your appointment for a ${apptservicename}</p><p>	is on ${apptdate} @ ${appttime}</p><p>	Please give 24 hours notice if you need to change or cancel this appointment.</p><p>	Sincerely,</p><p>	${stafffullname}</p><p>	p.s. Thank you for referring your friends and family.&nbsp; We appreciate your help!</p>";
		String initial_subject="Your appointment has been scheduled";
		String initial_signature="p.s. s. Thank you for scheduling your appointment";
		String receipt_email="<p>	Dear ${clientfullname},</p><p>	Here is your receipt for</p><p>	your ${apptservicename}</p><p>	${apptdate} @ ${appttime}</p><p>	${apptserviceprice}</p><p>	I appreciate your business!</p><p>	${stafffullname}</p>";
		String receipt_subject="Appointment receipt";
		String receipt_signature="p.s. I would love to work for your friends and family too. Thank you for telling them about me!";
		String notification_email="<p>	Dear ${clientfullname},</p><p>	This is a reminder that your appointment for ${apptservicename}</p><p>	Is on date ${apptdate} time ${appttime}</p><p>	&nbsp;</p><p>	Sincerely,</p><p>	${stafffullname}</p>";
		String notification_subject="Upcoming appointment notification";
		String notification_signature="p.s. thank you for being on time.";
		try {
		    properties.load(this.getClass().getClassLoader().getResourceAsStream("emailtemplates.properties"));
		    
		    initial_email = properties.getProperty("initial");
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
		shopSettings.setEmail_address(regform.getEmail());
		//notification
		shopSettings.setEmail_subject(notification_subject);
		shopSettings.setEmail_message(notification_email);
		shopSettings.setEmail_signature(notification_signature);
		// initial 
		shopSettings.setIemail_address(regform.getEmail());
		shopSettings.setIemail_subject(initial_subject);
		shopSettings.setIemail_message(initial_email);
		shopSettings.setIemail_signature(initial_signature);
		// receipt
		shopSettings.setRemail_address(regform.getEmail());
		shopSettings.setRemail_subject(receipt_subject);
		shopSettings.setRemail_message(receipt_email);
		shopSettings.setRemail_signature(receipt_signature);
		
		shopSettings.setUsername(username);
		shopSettings.setStore_phone("555-555-5555");
		shopSettings.setDefault_appt_length("1");
		shopSettings.setReceiveConfirmations(true);
		shopSettings.setClientdisplay(ClientDisplay.FIRSTNAME);
		shopSettings.setTimezone("MST");
		log.debug("about to set shopsetting logo");
        java.lang.String image_file_logo = "image_file_log_" + 0;
        if (image_file_logo.length() > 16) {
            image_file_logo  = image_file_logo.substring(0, 16);
        }
        shopSettings.setImage_file_logo(image_file_logo);
		
        log.debug("setting the shop for shopsettings");
		shopSettings.setShop(newshop);
		log.debug("persisting shopsettings");
		shopSettings.persist();
		log.debug("persisted shopsettings");
		// add sample base service
		BaseService baseservice = new BaseService();
		baseservice.setSendReminders(true);
		baseservice.setProcesstime(0);
		baseservice.setFinishtime(0);
		baseservice.setMinsetup(0);
		baseservice.setCost(5.00F);
		baseservice.setAmounttime(30);
		baseservice.setLength_time(30);
		baseservice.setDescription("Haircut");
		baseservice.setInfo_note("Sample service. Edit and save your own.");
		baseservice.setShop(newshop);
		log.debug("persisting baseservice");
		AbstractService ab  = baseservice.merge();
		log.debug("persisted baseservice");
		// create a staff
		Staff staff = new Staff();
		staff.setFirstName(regform.getFirstname());
		staff.setLastName(regform.getLastname());
		staff.setShop(newshop);
		staff.setBirthDay(new Date());
		staff.persist();
		log.debug("persisted staff");
		TypedQuery<Staff> staffs = staff.findStaffsByShop(newshop);
		if(staffs.getResultList().size() > 0){
			log.debug("found at least one service to assing to an appointment");
			// add an appointment
	        int hour = 10;
	        int minute = 30;
			try{
		        //   Allocates a Date object and initializes it so that it represents the time
		        // at which it was allocated, measured to the nearest millisecond.
		        Date dateNow = new Date ();
		 
		        SimpleDateFormat dateformatYYYYMMDD = new SimpleDateFormat("yyyy-mm-dd");
		        StringBuilder nowYYYYMMDD = new StringBuilder( dateformatYYYYMMDD.format( dateNow ) );
		        System.out.println( "DEBUG: Today in YYYYMMDD: '" + nowYYYYMMDD + "'");
		        
				// format date
			    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
			    Date apptDate = null; 
			    try {
			    	apptDate = dateFormat.parse(nowYYYYMMDD.toString());
				} catch (ParseException e) {
					e.printStackTrace();
				}			
				
				// add an appointment
				Appointment appt2 = new Appointment();
				appt2.setShop(newshop);
				appt2.setClient(clients);
				appt2.setStaff(staffs.getResultList().get(0));
				TypedQuery<BaseService>  tqservice2 = BaseService.findBaseServicesByShop(newshop);
				Set set2 = new HashSet();
				appt2.setAppointmentDate(apptDate);
				appt2.setCreateddate(new Date());
				appt2.setDescription("Appointment "+apptDate);
				appt2.setStarttime(0);
				appt2.setCancelled(true);
				appt2.setStatus(ScheduleStatus.ACTIVE);
				appt2.setEndtime(0);
				Calendar begintime2 = Calendar.getInstance();
				Date begindatetime2 = new Date();
				begindatetime2.setHours(hour);
				begindatetime2.setMinutes(minute);
				begintime2.setTime(begindatetime2);
				Calendar endtime2 = Calendar.getInstance();
				appt2.setBeginDateTime(begintime2);
				appt2.setEndDateTime(endtime2);
				BaseService thisservice = tqservice2.getResultList().get(0);
				
				log.debug("to string for second service: "+thisservice.toString());
				set2.add(thisservice);
				
				appt2.setServices(set2);
				appt2.persist();
			}catch(Exception exd){
				log.error(exd);
			}
			
		}					
		
		log.debug("success persisting baseservice");
		return rtn;
	}
	private boolean persistLdap(RegisterForm regform,String shopname){
		boolean rtn = true;
		
		Person person = new Person();
		person.setUid(regform.getUsername());
		person.setUserPassword(regform.getPassword());
		try{
			personDao.createCustomer(person,shopname);
		}catch(Exception e){
			log.error("EXCEPTION TRYING TO CREATE CUSTOMER ADMIN");
			log.error(e);
			rtn = false;
		}
		return rtn; 
	}
    @RequestMapping
    public ModelAndView index(Model uiModel) {
    	log.debug("ENTERED INDEX OF SignupController");
    	ModelAndView mav = new ModelAndView();
    	mav.setViewName("public/signup/index");
    	mav.addObject("register", new RegisterForm());
    	uiModel.addAttribute("register", new RegisterForm());
    	log.debug("EXITING INDEX OF SignupController");
        return mav;
    }
	@RequestMapping(value="/isnamedavailable", method=RequestMethod.GET)
    public @ResponseBody  String isNameAvailableJSON(@RequestParam(value="name",required=true) String name) {
		log.debug("Entering isUidAvailableJSON");
		boolean person = personDao.uidExists(name);
		if(person == true){
			person = false;
		}else{
			person = true;
			person = isNameAvailable(name);
		}
		
		String jsonarray2 = new JSONSerializer().exclude("*.class").serialize(person);
		log.debug("Exiting isUidAvailableJSON");
        return jsonarray2;
    }
	
	@RequestMapping(value="/register", method=RequestMethod.GET)
    public @ResponseBody  String register(@RequestParam(value="name",required=true) String name) {
		log.debug("Entering register");
    	log.debug("the value of personDao : "+personDao);
    	if(personDao != null){
    		Person person = new Person();
    		person.setCountry("Sweden");
    		person.setCompany("company1");
    		person.setFullName("Some Person4");
    		person.setLastName("Person4");
    		person.setDescription("Sweden, Company1, Some Person4");
    		person.setPhone("+46 555-123456");
    		person.setUid("thirdtest");
    		person.setUserPassword("thirdtest");
    		person.setPassword("thirdtest");
    		try{
    			personDao.createCustomer(person,person.getUid());
    		}catch(Exception e){
    			log.error("EXCEPTION TRYING TO CREATE CUSTOMER ADMIN");
    			log.error(e);
    		}
    	}
		String jsonarray2 = new JSONSerializer().exclude("*.class").serialize("");
		
		log.debug("Exiting register");
        return jsonarray2;
    }

	public RegistrationNotificationHelper getRegistrationNotificationHelper() {
		return registrationNotificationHelper;
	}

	public void setRegistrationNotificationHelper(
			RegistrationNotificationHelper registrationNotificationHelper) {
		this.registrationNotificationHelper = registrationNotificationHelper;
	}
    
}
