package com.yourhairsalon.booking.web;

import com.yourhairsalon.booking.domain.Staff;
import com.yourhairsalon.booking.domain.CustomService;

import org.joda.time.format.DateTimeFormat;
import org.springframework.ui.Model;

import com.yourhairsalon.booking.domain.Appointment;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.yourhairsalon.booking.domain.Addresses;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.yourhairsalon.booking.domain.AbstractService;
import com.yourhairsalon.booking.domain.AppointmentDeep;
import com.yourhairsalon.booking.domain.Clients;
import com.yourhairsalon.booking.domain.Communications;
import com.yourhairsalon.booking.domain.Registration;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.ShopSettings;
import com.yourhairsalon.booking.reference.ClientDisplay;
import com.yourhairsalon.booking.reference.CommType;
import com.yourhairsalon.booking.reference.RegistrationTypes;
import com.yourhairsalon.booking.reference.ScheduleStatus;

import javax.persistence.TypedQuery;

import com.yourhairsalon.booking.domain.BaseService;
import com.yourhairsalon.booking.form.UserPreference;

import flexjson.JSONSerializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/")
@Controller
public class IndexController {
    private static final Log log = LogFactory.getLog(IndexController.class);
    private String shopname_url = "";
    private Shop thisshop;
    
    @Autowired
    private UserPreference preferences;
    
    private void setShop(Shop shop){
    	getPreferences().setShop(shop);
    	this.thisshop = shop;
    }
	private Shop getShop(){
		log.debug("ENTERED getShop IndexController");
		Object obj = null;
		try{
			obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();	
		}catch(Exception e){
			log.error(""+e.getMessage());
		}
		
    	if(obj != null && obj instanceof InetOrgPerson) {
			log.debug("The principal object is InetOrgPerson");
			String parentshop = ((InetOrgPerson) obj).getO();
			String username = ((UserDetails) obj).getUsername();
			log.info("parentshop of LOGGED IN USER: " + parentshop);
			log.info("username of LOGGED IN USER: " + username);
//			if(getPreferences().getShop() == null){
				TypedQuery<Shop> shop = null;
				if(parentshop == null){
					parentshop = username;
				}
				shop = Shop.findShopsByShopuuid(parentshop);
				log.debug("the returned results for looking for "+ parentshop + " is a size of: " + shop.getResultList().size());
				if(shop.getResultList().size() > 0){
					getPreferences().setShop(shop.getResultList().get(0));
					
				}
//			}			
		}else if (obj != null && obj instanceof UserDetails) {
			log.debug("The principal object is UserDetails");
			String username = ((UserDetails) obj).getUsername();
			log.debug("username: "+username);
			log.debug("preferences: "+getPreferences());
//			if (getPreferences().getShop()==null) {
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
//			}
		}			
    	log.debug("EXITING getShop IndexController");
		return getPreferences().getShop();
	}
	@ModelAttribute("pendingappts")
	public List getTodaysPendingAppointments(Model model){
		log.debug("ENTERED getTodaysPendingAppointments");
		List<AppointmentDeep> apptdeep = null;
		try{
			if(getShop() != null){
				TypedQuery<Appointment> appointments = Appointment.findAppointmentsByShopAndStatus(getShop(), ScheduleStatus.PENDING);
				log.debug("getTodaysAppointments -- the number of appointments is: "+appointments.getResultList().size());
				apptdeep = new ArrayList<AppointmentDeep>();
				if(appointments.getResultList().size()>0){
					for(int x=0;x<appointments.getResultList().size();x++){
						Appointment appt = appointments.getResultList().get(x);
						log.debug("The toString of appt: "+ appt.toString());
						AppointmentDeep deep = new AppointmentDeep(appt);
						log.debug("The toString of DEEP appt: "+ deep.toString());
						apptdeep.add(deep);
					}
					
				}
				model.addAttribute("pending_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
			}
		}catch(Exception e){
			log.error(e);
		}
		log.debug("EXITING getTodaysPendingAppointments");
		return apptdeep;
	}
    
    
    public IndexController() {
        if (log.isDebugEnabled()) {
            log.debug("IndexController instantiated");
        }
    }
    private boolean isNameAvailable(String name) {
		log.debug("Entering isNameAvailable");
		
		TypedQuery<Shop> comparename = Shop.findShopsByShop_urlEquals(name);
		int num = comparename.getResultList().size();
		log.debug("comparename: "+comparename);
		log.debug("name: "+name);
		boolean isavail = false;
		if(num == 0){
			log.debug("entered first");
			isavail = true;
		}else{
			log.debug("entered second. the number of matching shop names is: " + num);
			isavail = false;
		}
		log.debug("Exiting isNameAvailable");
        return isavail;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView index(@RequestParam(value="locale", defaultValue = "en") String locale, @RequestParam(value="lang", defaultValue = "en") String lang,  Model model,HttpServletRequest request, HttpServletResponse response, HttpSession session) {
    	log.debug("Index CONTROLLER DISPLAYING Index MODEL AND VIEW");
    	String destination = "index";
    	ModelAndView mav = new ModelAndView();
		model.addAttribute("locale", locale);
		model.addAttribute("lang", lang);
		session.setAttribute("locale", locale);
		session.setAttribute("lang", lang);
		mav.addObject("locale", locale);
		mav.addObject("lang", lang);
    	
    	// check to see if this user has a shop already created
    		log.debug("ENTERED NORMAL SITE");
			mav.setViewName(destination);
    	
    	Object obj = null;
    	try {
			obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		} catch (Exception e1) {
			log.error(e1.getMessage());
			if(obj==null){
				log.debug("THE AUTHETICATION PRINCIPAL WAS NULL");
			}
			try {
				log.error("checking request to see if authentication attribute is not null ");
				if(request.getAttribute("authentication") != null){
					log.error("request authentication attribute is not null ");
					obj = request.getAttribute("authentication");
					SecurityContextHolder.getContext().setAuthentication((Authentication)obj);
					log.debug("set the Authentication from request");
				}
			} catch (Exception e) {
				log.error("failed setting the authentication");
				log.error(e.getMessage());
			}
			try {
				log.error("checking session to see if authentication attribute is not null ");
				if(session.getAttribute("authentication") != null){
					log.error("session authentication attribute is not null ");
					obj = session.getAttribute("authentication");
					SecurityContextHolder.getContext().setAuthentication((Authentication)obj);
					log.debug("set the Authentication from session");
				}
			} catch (Exception e) {
				log.error("failed setting the authentication");
				log.error(e.getMessage());
			}
		}
		if (obj != null && obj instanceof UserDetails) {
			log.debug("ENTERED SECURED OBJECT");
			String username = ((UserDetails) obj).getUsername();
			// first see if this is a customer customer user trying to login
			TypedQuery<Clients> testclients = Clients.findClientsesByUsernameEquals(username);
			log.debug("AFTER FINDING CLIENT. SIZE IS: "+testclients.getResultList().size());
			if(testclients.getResultList().size() > 0){
				log.debug("ENTERED CODE TO REDIRECT CLIENT TO SHOP ");
				log.debug("client "+username+" found");
				Clients client = testclients.getResultList().get(0);
				Shop thisshop = client.getShop();
				String shopurl = thisshop.getShop_url();
				destination = shopurl;
				log.debug("client shop url is: "+ destination);
				destination = "public/shop/" + destination;
				setShopname_url(destination);
				model.addAttribute("shopurl", shopurl);
				mav.addObject("shopurl", shopurl);
				mav.setView(new RedirectView("./"+destination));
				log.debug("EXITING IndexController via :" + destination);
				return mav;
			}else if (!username.equalsIgnoreCase("anonymous")) {
				log.info("LOGGED IN USER: " + username);
				// find shop with uid
				//TypedQuery<Shop> shop = Shop.findShopsByShopuuid(username);
				//log.debug("the returned results for looking for "+ username + " is a size of: " + shop.getResultList().size());
				if(getShop() == null){
					//TODO NEED TO LOGOUT THE USER AND INFORM HIM THAT HIS ACCOUNT IS NOT VALID. NEED TO CREATE
					log.debug("LOGGING OUT. USERNAME DOES NOT MATCH WITH ANY SHOP");
					mav.setView(new RedirectView("./static/j_spring_security_logout"));
					/*
					// create new shop
					Shop newshop = new Shop();
					String shopname = "";
					shopname = username;
					newshop.setShop_name(shopname);
					String resultString = shopname.replaceAll("[^\\p{L}\\p{N}]", "");
					// check to see if this shop url already exists
					boolean isavailable = isNameAvailable(resultString.toLowerCase());
					if(isavailable){
						// now set the url
						newshop.setShop_url(resultString.toLowerCase());
					}else{
						// what to do when shop url is not available ?
						
					}
					destination = newshop.getShop_url();
					destination = "public/shop/" + destination;
					setShopname_url(destination);
					mav.addObject("shopname_url", shopNameUrl());
					destination = "index";
					
					newshop.setShopuuid(username);
					newshop.setType(RegistrationTypes.TRIAL);
					newshop.persist();
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
					String initial_email ="<p>	Dear ${clientfullname},</p><p>	Your appointment for a ${apptservicename}</p><p>	is on ${apptdate} @ ${appttime}</p><p>	Please give 24 hours notice if you need to change or cancel this appointment.</p><p>	Sincerely,</p><p>	${stafffullname}</p><p>	p.s. Thank you for referring your friends and family.&nbsp; We appreciate your help!</p>";
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
					String email_address = username;// since this is a error in initializing the shop we don't know the email address
					//notification
					shopSettings.setEmail_address(email_address);
					shopSettings.setEmail_subject(notification_subject);
					shopSettings.setEmail_message(notification_email);
					shopSettings.setEmail_signature(notification_signature);
					// initial
					shopSettings.setIemail_address(email_address);
					shopSettings.setIemail_subject(initial_subject);
					shopSettings.setIemail_message(initial_email);
					shopSettings.setIemail_signature(initial_signature);
					// receipt
					shopSettings.setRemail_address(email_address);
					shopSettings.setRemail_subject(receipt_subject);
					shopSettings.setRemail_message(receipt_email);
					shopSettings.setRemail_signature(receipt_signature);

					shopSettings.setUsername(username);
					shopSettings.setStore_phone("555-555-5555");
					shopSettings.setDefault_appt_length("1");
					shopSettings.setReceiveConfirmations(true);
					shopSettings.setTimezone("MST");
					shopSettings.setClientdisplay(ClientDisplay.FIRSTNAME);
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
					staff.setFirstName("Kory");
					staff.setLastName("Malone");
					staff.setShop(newshop);
					staff.setBirthDay(new Date());
					staff.persist();
					log.debug("persisted staff");
					TypedQuery<Staff> staffs = staff.findStaffsByShop(newshop);
					if(staffs.getResultList().size() > 0){
						log.debug("found at least one service to assing to an appointment");
						// add an appointment
						Appointment appt = new Appointment();
						appt.setShop(newshop);
						appt.setClient(clients);
						appt.setStaff(staff);
						TypedQuery<BaseService>  tqservice = BaseService.findBaseServicesByShop(newshop);
						Set set = new HashSet();
						appt.setAppointmentDate(new Date());
						appt.setCreateddate(new Date());
						appt.setDescription("Test Appointment");
						appt.setCancelled(false);
						appt.setStarttime(15);
						appt.setStatus(ScheduleStatus.ACTIVE);
						appt.setEndtime(30);

						Set cset = new HashSet();
						if(tqservice.getResultList() != null && tqservice.getResultList().size() == 0){
							cset.addAll(tqservice.getResultList());
						}
							
				        int hour = 10;
				        int minute = 30;

						Calendar begintime = Calendar.getInstance();
						Date begindatetime = new Date();
						begindatetime.setHours(hour);
						begindatetime.setMinutes(minute);
						begintime.setTime(begindatetime);
						Calendar endtime = Calendar.getInstance();
						appt.setBeginDateTime(begintime);
						appt.setEndDateTime(endtime);

						appt.setServices(cset);
						appt.persist();
						log.debug("persisted appointment");
						
						// try another one
						try{
							// format date
							String dateString = "2012-01-20"; 
						     
						    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
						    Date apptDate = null; 
						    try {
						    	apptDate = dateFormat.parse(dateString);
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
					*/
					log.debug("success persisting baseservice");
				}else{
					// set shop name url
					log.debug("THE LAST ELSE STATEMENT");
					Shop tmpshop = getShop();
					log.debug("shop name: "+tmpshop.getShop_name()+" uuid: " +tmpshop.getShopuuid()+" url:"+tmpshop.getShop_url());
					destination = tmpshop.getShop_url();
					destination = "public/shop/" + destination;
					log.debug("DESTINATION: "+destination);
					setShopname_url(destination);
					mav.addObject("shopname_url", shopNameUrl());
					destination = "index";

				}
			}
		}
		model.addAttribute("locale", "en");
		model.addAttribute("shopname_url", shopNameUrl());
		log.debug("VIEW NAME: "+mav.getViewName());
    	log.debug("EXITING INDEX CONTROLLER INDEX");
    	return mav;
    }
    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView indexpost() {
    	log.debug("POST 2 Index CONTROLLER DISPLAYING Index MODEL AND VIEW");
    	return new ModelAndView("index");
        
    }

	public String shopNameUrl(){
    	log.debug("ENTERING shopNameUrl");
		Properties properties = new Properties();
		String baseurl = "https://www.scheduleem.com/shop/public/shop/";
		try {
		    properties.load(this.getClass().getClassLoader().getResourceAsStream("access.properties"));
		    baseurl = properties.getProperty("baseurl"); 
		} catch (IOException e) {
			log.error(e);
		}			
    	
    	String shopnameurl = "";
    	if(getShopname_url() != null){
    		String tmp = "";
    		if(getShop() != null && getShop().getShop_url() != null){
    			tmp = baseurl+getShop().getShop_url();
    		}else{
    			tmp = baseurl;
    		}
	    	log.debug("shop url replacing "+tmp);
	    	shopnameurl = tmp;
    	}
    	log.debug("EXITING shopNameUrl");
    	return shopnameurl;
    }
    
	private String getShopname_url() {
		log.debug("ENTERING getShopname_url");
		return shopname_url;
	}

	private void setShopname_url(String shopname_url) {
		log.debug("ENTERING setShopname_url");
		this.shopname_url = shopname_url;
	}
	public UserPreference getPreferences() {
		return preferences;
	}
	public void setPreferences(UserPreference preferences) {
		this.preferences = preferences;
	}


}
