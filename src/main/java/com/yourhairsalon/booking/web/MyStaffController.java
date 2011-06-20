package com.yourhairsalon.booking.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.internet.MimeMessage;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
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

import com.yourhairsalon.booking.domain.AbstractGroup;
import com.yourhairsalon.booking.domain.AbstractPerson;
import com.yourhairsalon.booking.domain.Addresses;
import com.yourhairsalon.booking.domain.Appointment;
import com.yourhairsalon.booking.domain.AppointmentDeep;
import com.yourhairsalon.booking.domain.Audit;
import com.yourhairsalon.booking.domain.ClientGroup;
import com.yourhairsalon.booking.domain.Clients;
import com.yourhairsalon.booking.domain.Communications;
import com.yourhairsalon.booking.domain.GCal;
import com.yourhairsalon.booking.domain.Person;
import com.yourhairsalon.booking.domain.PersonDaoImpl;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.ShopSettings;
import com.yourhairsalon.booking.domain.Staff;
import com.yourhairsalon.booking.form.EmailAddressValidator;
import com.yourhairsalon.booking.form.FullClientForm;
import com.yourhairsalon.booking.form.GCalForm;
import com.yourhairsalon.booking.form.RegisterForm;
import com.yourhairsalon.booking.form.SelectClientForm;
import com.yourhairsalon.booking.form.UserPreference;
import com.yourhairsalon.booking.reference.CommType;
import com.yourhairsalon.booking.reference.RegistrationTypes;

import flexjson.JSONSerializer;

@RequestMapping("/mystaff/**")
@Controller
public class MyStaffController {
	private static final Log log = LogFactory.getLog(MyStaffController.class);
	private Audit audit = new Audit();
	private Shop shop;
	private String individualusername;
    @Autowired
    private UserPreference preferences;
	@Autowired
	private transient MailSender mailTemplate;
	@Autowired
	private transient JavaMailSenderImpl mailHTMLTemplate;	
	@Autowired
	private PersonDaoImpl personDao;

	
	private void saveAuditMessage(String msg,Staff staff,String type ){
		getAudit().setShop(getShop());
		getAudit().setDescription(msg);
		getAudit().setStaff(staff);
		getAudit().setTs(new Date());
		getAudit().setType(type);
		getAudit().merge();
	}

    private void setShop(Shop shop){
    	getPreferences().setShop(shop);
    	this.shop = shop;
    }
	private Shop getShop(){
		log.debug("ENTERED getShop MyStaffController");
		Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		log.debug("obj.getClass().toString(): "+obj.getClass().toString());
    	if(obj instanceof InetOrgPerson) {
			log.debug("The principal object is InetOrgPerson");
			String parentshop = ((InetOrgPerson) obj).getO();
			setIndividualusername(((InetOrgPerson) obj).getUid());
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
    	log.debug("EXITING getShop IndexController");
		return getPreferences().getShop();
	}
	
	/**
	 * used to add new staff to ldap 
	 * 
	 * @param regform
	 * @param shopname
	 * @return
	 */
	private boolean persistLdap(RegisterForm regform,String shopname){
		boolean rtn = true;
		log.debug("ENTERED persistLdap");
		log.debug("shopname : " + shopname);
		log.debug("regform : " + regform);
		Person person = new Person();
		person.setUid(regform.getUsername());
		person.setUserPassword(regform.getPassword());
		log.debug("person : " + person.toString());
		try{
			personDao.createCustomerStaff(person,shopname);
		}catch(Exception e){
			log.error("EXCEPTION TRYING TO CREATE CUSTOMER STAFF");
			log.error(e);
			rtn = false;
		}
		log.debug("EXITING persistLdap");
		return rtn; 
	}

	/**
	 * 
	 * @param regform
	 * @param shopname
	 * @return
	 */
	private boolean updateLdapPw(String staffid, String password){
		boolean rtn = true;
		log.debug("ENTERED updateLdapPw");
		log.debug("staffid: "+staffid);
		
		if(staffid == null || staffid.equalsIgnoreCase("")) return false;
		
		Person person = new Person();
		person.setUid(staffid);
		person.setUserPassword(password);
		log.debug("staffid : " + staffid);
		try{
			personDao.changePassword(person);
		}catch(Exception e){
			log.error("EXCEPTION TRYING TO CREATE CUSTOMER CUSTOMER");
			log.error(e);
			rtn = false;
		}
		log.debug("EXITING updateLdapPw");
		return rtn; 
	}
	

    /**
     * view to allow staff member to manage their own google calendar account
     * 
     * @param uiModel
     * @return
     */
	@RequestMapping(value="lonestaff",method = RequestMethod.GET)
    public ModelAndView displayLoneStaff(
    		Model uiModel) {
		log.debug("ENTERED displayLoneStaff");
		ModelAndView mav = new ModelAndView();

		String uid = getIndividualusername();
		TypedQuery<Staff> staff = Staff.findStaffsByUsernameEquals(uid);
		if(staff.getResultList().size()>0){
			Staff lonestaff = staff.getResultList().get(0);
			Long id = lonestaff.getId();
			FullClientForm fcf = setStaffToFCF(lonestaff);
			uiModel.addAttribute("lonestaff", fcf);
			
			
			TypedQuery<GCal> orginal_gals = GCal.findGCalsByShopEqualsAndStaffEquals(getShop(), lonestaff);
			GCal orig_gal = null; 
			if(orginal_gals.getResultList().size() > 0){
				GCalForm gal = new GCalForm();
				orig_gal = orginal_gals.getResultList().get(0);
				String cal_name = orig_gal.getCalendarnameid();
				String cal_username = orig_gal.getUsername();
				gal.setGcal_id(orig_gal.getCalendarnameid());
				gal.setGcal_title(orig_gal.getGcal_title());
				
				uiModel.addAttribute("staffid", id);
				uiModel.addAttribute("google_cals", gal);
				uiModel.addAttribute("google_username", cal_username);
			}
			
			mav.addObject("lonestaff", fcf);
		}	
		log.debug("Exiting displayLoneStaff");
		mav.setViewName("mystaff/lonestaff");
		return mav;
	}	
	/**
	 * sets staff's google username, password, and calendar to be synched with.
	 * 
	 * @param uid
	 * @param username
	 * @param password
	 * @param calendarname
	 * @param orig_calendarname
	 * @param calendartitle
	 * @return
	 */
	@RequestMapping(value="/gcal", method=RequestMethod.GET)
	public @ResponseBody  String updateGCal(
			@RequestParam(value="uid") String uid,
			@RequestParam(value="ur") String username,
			@RequestParam(value="p") String password,
			@RequestParam(value="c") String calendarname,
			@RequestParam(value="ogc") String orig_calendarname,
			@RequestParam(value="t") String calendartitle
		) {
		log.debug("ENTERED updateGCal");
		log.debug("calendarname: " + calendarname);
		log.debug("orig_calendarname: " + orig_calendarname);
		String RESULTS = "SUCCESS";
		try{
			log.debug("getIndividualusername(): "+ uid);
			TypedQuery<Staff> staff = Staff.findStaffsByUsernameEquals(uid);
			
			if(staff.getResultList().size()>0  && username != null && password != null && calendarname != null && !username.equalsIgnoreCase("") && !password.equalsIgnoreCase("") && !calendarname.equalsIgnoreCase("")){
				Staff lonestaff = staff.getResultList().get(0);
				if(orig_calendarname != null && !orig_calendarname.equalsIgnoreCase("") ){
					log.debug("lonestaff.getUsername(): "+lonestaff.getUsername());
					TypedQuery<GCal> orginal_gals = GCal.findGCalsByShopEqualsAndStaffEqualsAndCalendarnameidEquals(getShop(), lonestaff, orig_calendarname);
					GCal orig_gal = null; 
					if(orginal_gals.getResultList().size() > 0){
						log.debug("GOING TO EDIT");
						orig_gal = orginal_gals.getResultList().get(0);
							log.debug("gals.getResultList().size() > 0");	
							if(orig_gal != null){
								log.debug("ABOUT TO UPDATE EXISTING GCAL");
								orig_gal.setUsername(username);
								orig_gal.setPassword(password);
								orig_gal.setCalendarnameid(calendarname);
								orig_gal.setGcal_title(calendartitle);
								orig_gal.merge();
							}
					}else{
						log.debug("ABOUT TO CREATE NEW GCAL");
						GCal ga = new GCal();
						ga.setShop(getShop());
						ga.setStaff(lonestaff);
						ga.setUsername(username);
						ga.setPassword(password);
						ga.setCalendarnameid(calendarname);
						ga.setGcal_title(calendartitle);
						ga.merge();
					}				
				}else{
					log.debug("ABOUT TO CREATE NEW GCAL 2");
					GCal ga = new GCal();
					ga.setShop(getShop());
					ga.setStaff(lonestaff);
					ga.setUsername(username);
					ga.setPassword(password);
					ga.setCalendarnameid(calendarname);
					ga.setGcal_title(calendartitle);
					ga.merge();
				}
				// adding audit log
				saveAuditMessage("Updated settings Allow_staff_gcalendar to ",null,"GENERAL");
			}
			
		}catch(Exception e){
			log.error(e);
			RESULTS = "FAILURE";
		}

		log.debug("EXITING updateGCal");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}	


	public Date convertStringToDate(String stringdate) {
		log.debug("ENTERED convertStringToDate");
		log.debug("stringdate: "+stringdate);
		Date newdate = new Date();
		if(stringdate.contains("/")){
			log.debug("contains slashes");
		}
		if(stringdate.contains("-")){
			log.debug("contains dashes");
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
		}
		
		log.debug("EXITING convertStringToDate");
		return newdate;
	}
	
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
    	log.debug("ENTERED Staff list method");
    	ModelAndView mav = new ModelAndView();
    	log.debug("STEP 1");
        mav.setViewName("mystaff/index");
        log.debug("STEP 2");
//    	if (page != null || size != null) {
//    		log.debug("STEP 3");
//            int sizeNo = size == null ? 10 : size.intValue();
//            log.debug("STEP 4");
//            //uiModel.addAttribute("clients", Clients.findClientsEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
//            log.debug("STEP 5");
//            TypedQuery<Clients> client = Clients.findClientsesByShop(getShop());
//            uiModel.addAttribute("clients", client);
//            log.debug("STEP 6");
//            float nrOfPages = (float) Clients.countClientses() / sizeNo;
//            log.debug("STEP 7");
//            //uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
//            log.debug("STEP 8");
//        } else {
        	log.debug("STEP 9");
        	TypedQuery<Staff> staff = Staff.findStaffsByShop(getShop());
        	log.debug("STEP 10");
            uiModel.addAttribute("staff",staff.getResultList() );
            log.debug("STEP 11");
//        }
    	log.debug("STEP 12");
    	uiModel.addAttribute("addresses",new Addresses());
    	log.debug("STEP 13");
    	//uiModel.addAttribute("delete", false);
    	uiModel.addAttribute("typeName", "staff");
    	log.debug("STEP 14");
    	uiModel.addAttribute("path", "/staff?id");
    	log.debug("STEP 15");
    	uiModel.addAttribute("labelPlural","Staffs");
    	log.debug("STEP 16");
    	
        addDateTimeFormatPatterns(uiModel);
        log.debug("STEP 17");
        log.debug("EXITING Staff list method");
        return mav;
    }
    
    @RequestMapping(value="/list", method = RequestMethod.GET)
    public @ResponseBody  String stafflist() {
    	log.debug("ENTERED stafflist method");
    	log.debug("STEP 1");
		Collection<Staff> staff = Staff.findStaffsByShop(getShop()).getResultList();

		Iterator itr = staff.iterator();
		while (itr.hasNext()) {
			Staff staffs = (Staff) itr.next();
			String name = staffs.getFirstName();
			staffs.setFirstName(name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase());
			String lname = staffs.getLastName();
			staffs.setLastName(lname.substring(0, 1).toUpperCase() + lname.substring(1).toLowerCase());
		}
		
		String response = Staff.toJsonArray(staff);
		log.debug("EXITING stafflist method");
        return response;
    }
    @RequestMapping(params = "display", method = RequestMethod.POST)
    public ModelAndView listPost(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
    	log.debug("Entered Staff listPost method");
    	ModelAndView mav = new ModelAndView();
        mav.setViewName("mystaff");
    	if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            //uiModel.addAttribute("clients", Clients.findClientsEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            uiModel.addAttribute("staff", Staff.findStaffsByShop(getShop()));
            float nrOfPages = (float) Staff.countStaffs() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
        	TypedQuery<Staff> staff = Staff.findStaffsByShop(getShop());
            uiModel.addAttribute("staff",staff.getResultList() );
        }
    	
    	uiModel.addAttribute("addresses",new Addresses());
    	//uiModel.addAttribute("delete", false);
    	uiModel.addAttribute("typeName", "staff");
    	uiModel.addAttribute("path", "/staff?id");
    	uiModel.addAttribute("labelPlural","Staffs");
    	
        addDateTimeFormatPatterns(uiModel);
        log.debug("EXITING Staffs listPost method");
        return mav;
    }
    
    void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("staffs_birthday_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
    }

    /**
     * Saves the data from the form
     */
    @RequestMapping(method = RequestMethod.POST)
    public String create(@Valid FullClientForm staff, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
    	log.debug("Entered Staffs create POST method");
    	if (registeredForMoreStaff() == false) {
    		return "staff/create"; 
    	}
    	if (bindingResult.hasErrors()) {
    		log.debug("binding has errors so redisplay create form");
    		log.debug("error count " + bindingResult.getErrorCount());
    		log.debug("errors " + bindingResult.getFieldError());
            uiModel.addAttribute("staff", staff);
            addDateTimeFormatPatterns(uiModel);
            return "staff/create";
        }
        uiModel.asMap().clear();
        Staff staffs = new Staff();
        staffs.setShop(getShop());
        staffs.setFirstName(staff.getFirstName());
        staffs.setLastName(staff.getLastName());
        if(staff.getBirthDay() == null || staff.getBirthDay().equalsIgnoreCase("null")){
        	staffs.setBirthDay(null);
        }else{
        	log.debug("staff.getBirthDay(): "+staff.getBirthDay());
        	staffs.setBirthDay(convertStringToDate(staff.getBirthDay()));
        }
        log.debug("the client trying to be persisted is: "+staffs.toString());
        AbstractPerson per =  staffs.merge();
        staffs = Staff.findStaff(per.getId());
        
        Addresses addresses = new Addresses();
        String addr1 = staff.getAddress1();
        log.debug("addr1 "+addr1);
        String addr2 = staff.getAddress2();
        log.debug("addr2 "+addr2);
        String city = staff.getCitycode();
        log.debug("city "+city);
        String state = staff.getStatecode();
        log.debug("state "+state);
        String zip = staff.getZipcode();
        log.debug("zip "+zip);
        if(addr1 == null || addr1.equalsIgnoreCase("")) addr1 = "1";
        if(addr2 == null || addr2.equalsIgnoreCase("")) addr2 = "2";
        if(city == null || city.equalsIgnoreCase("")) city = "city";
        if(state == null || state.equalsIgnoreCase("")) state = "st";
        if(zip == null || zip.equalsIgnoreCase("")) zip = "12345";
        addresses.setAddress1(addr1);
        addresses.setAddress2(addr2);
        addresses.setCitycode(city);
        addresses.setStatecode(state);
        addresses.setZipcode(zip);
		if (addresses.getPerson() == null) {
			log.debug("person / staff object "+staffs);
			addresses.setPerson(staffs);
		}
		if (addresses.getShop() == null) {
			addresses.setShop(getShop());
		}
		log.debug("saving address");
		addresses.persist();
        
        
		// create a new set of communications
		Shop newshop = getShop();
		Communications com = new Communications();
		com.setCommunication_type(CommType.HOME_PHONE);
		String hphone = staff.getHome_phone();
		if(hphone == null || hphone.equalsIgnoreCase("")) hphone = "555-555-5555";
		com.setCommunication_value(hphone);
		com.setPerson(staffs);
		com.setShop(newshop);
		log.debug("com1 to string: "+com.toString());
		log.debug("Persisting com1");
		com.persist();

		Communications com2 = new Communications();
		com2.setCommunication_type(CommType.WORK_PHONE);
		String wphone = staff.getWork_phone();
		if(wphone == null || wphone.equalsIgnoreCase("") ) wphone = "555-555-5555";
		com2.setCommunication_value(wphone);
		com2.setPerson(staffs);
		com2.setShop(newshop);
		log.debug("Persisting com2");
		com2.persist();

		Communications com3 = new Communications();
		com3.setCommunication_type(CommType.CELL_PHONE);
		String cphone = staff.getCell_phone();
		if(cphone == null || cphone.equalsIgnoreCase("") ) cphone = "555-555-5555";
		com3.setCommunication_value(cphone);
		com3.setPerson(staffs);
		com3.setShop(newshop);
		log.debug("Persisting com3");
		com3.persist();

		Communications com4 = new Communications();
		String email = staff.getEmail();
		if(email == null || email.equalsIgnoreCase("") ) email = "placeholder@placedholder.com";
		com4.setCommunication_type(CommType.EMAIL);
		com4.setCommunication_value(email);
		com4.setPerson(staffs);
		com4.setShop(newshop);
		log.debug("Persisting com4");
		com4.persist();
		// adding audit log
		saveAuditMessage("Staff "+ staffs.getFirstName() + " " + staffs.getLastName() +" added ",null,"GENERAL");
        
        log.debug("after trying to persist staff");
        log.debug("EXITING Staffs create POST method");
        return "redirect:/mystaff/" + encodeUrlPathSegment(staffs.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(params = "form", method = RequestMethod.GET)
    public ModelAndView createForm(Model uiModel) {
    	log.debug("Entered Staff createForm method");
    	ModelAndView mav = new ModelAndView();
    	Clients client = new Clients();
    	client.setShop(getShop());
        
    	FullClientForm fcf = new FullClientForm();
        uiModel.addAttribute("staff", fcf);
        
        //uiModel.addAttribute("clients", client);
        mav.setViewName("mystaff/create");
        mav.addObject("staff", fcf);
        
        addDateTimeFormatPatterns(uiModel);
        log.debug("Exiting Staff createForm method");
        return mav;
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
    	log.debug("Entered Staff show method");
        addDateTimeFormatPatterns(uiModel);
        Staff staff = Staff.findStaff(id);
        FullClientForm fcf = new FullClientForm();
        fcf.setFirstName(staff.getFirstName());
        fcf.setLastName(staff.getLastName());
        fcf.setId(staff.getId());
        
        if(staff.getBirthDay() == null){
        	fcf.setBirthDay(null);
        }else{
        	log.debug("staff.getBirthDay(): "+staff.getBirthDay());
        	fcf.setBirthDay(""+staff.getBirthDay());
        }
        
        uiModel.addAttribute("staff", fcf);
		List<Addresses> addr = Addresses.findAddressesesByPerson(staff).getResultList();
		if(addr.size()>0){
			fcf.setAddress1(addr.get(0).getAddress1());
			fcf.setAddress2(addr.get(0).getAddress2());
			fcf.setCitycode(addr.get(0).getCitycode());
			fcf.setStatecode(addr.get(0).getStatecode());
			fcf.setZipcode(addr.get(0).getZipcode());
		}
		List<Communications> comm = Communications.findCommunicationsesByPerson(staff).getResultList();
		int sz = comm.size();

		for(int x=0;x<sz;x++){
			Communications com = comm.get(x);
			String comval = com.getCommunication_value();
			
			if(com.getCommunication_type().name().equalsIgnoreCase(CommType.HOME_PHONE.name())){
				fcf.setHome_phone(comval);
			}
			if(com.getCommunication_type().name().equalsIgnoreCase(CommType.WORK_PHONE.name())){
				fcf.setWork_phone(comval);
			}
			if(com.getCommunication_type().name().equalsIgnoreCase(CommType.CELL_PHONE.name())){
				fcf.setCell_phone(comval);
			}
			if(com.getCommunication_type().name().equalsIgnoreCase(CommType.EMAIL.name())){
				fcf.setEmail(comval);
			}
		}
        uiModel.addAttribute("itemId", id);
        // NOW GET LIST OF APPOINTMENTS FOR THIS Staff
        TypedQuery<Appointment> appt_history = Appointment.findAppointmentsByShopAndStaff(getShop(), staff);
        List<AppointmentDeep> apptdeep = new ArrayList<AppointmentDeep>();
        if(appt_history.getResultList().size() > 0){
			for(int x=0;x<appt_history.getResultList().size();x++){
				Appointment appt = appt_history.getResultList().get(x);
				log.debug("The toString of appt: "+ appt.toString());
				AppointmentDeep deep = new AppointmentDeep(appt);
				log.debug("The toString of DEEP appt: "+ deep.toString());
				apptdeep.add(deep);
			}
        }
        Collections.sort(apptdeep);
        uiModel.addAttribute("staffhistory",apptdeep);
        return "staff/show";
    }
    
    private FullClientForm setStaffToFCF(Staff staff){
        FullClientForm fcf = new FullClientForm();
        fcf.setUse_gcalendar(""+staff.isUse_gcalendar());
        fcf.setFirstName(staff.getFirstName());
        fcf.setLastName(staff.getLastName());
        fcf.setUsername(staff.getUsername());
        fcf.setId(staff.getId());
        
        if(staff.getBirthDay() == null){
        	fcf.setBirthDay(null);
        }else{
        	log.debug("staff.getBirthDay(): "+staff.getBirthDay());
        	fcf.setBirthDay(""+staff.getBirthDay());
        	fcf.setBirthDate(staff.getBirthDay());
        }
        
		List<Addresses> addr = Addresses.findAddressesesByPerson(staff).getResultList();
		if(addr.size()>0){
			fcf.setAddress1(addr.get(0).getAddress1());
			fcf.setAddress2(addr.get(0).getAddress2());
			fcf.setCitycode(addr.get(0).getCitycode());
			fcf.setStatecode(addr.get(0).getStatecode());
			fcf.setZipcode(addr.get(0).getZipcode());
		}
		List<Communications> comm = Communications.findCommunicationsesByPerson(staff).getResultList();
		int sz = comm.size();

		for(int x=0;x<sz;x++){
			Communications com = comm.get(x);
			String comval = com.getCommunication_value();
			
			if(com.getCommunication_type().name().equalsIgnoreCase(CommType.HOME_PHONE.name())){
				fcf.setHome_phone(comval);
			}
			if(com.getCommunication_type().name().equalsIgnoreCase(CommType.WORK_PHONE.name())){
				fcf.setWork_phone(comval);
			}
			if(com.getCommunication_type().name().equalsIgnoreCase(CommType.CELL_PHONE.name())){
				fcf.setCell_phone(comval);
			}
			if(com.getCommunication_type().name().equalsIgnoreCase(CommType.EMAIL.name())){
				fcf.setEmail(comval);
			}
		}    	
		return fcf;
    }
    /**
     * Ajax/json version of grabbing the client info
     */
    @RequestMapping(value = "/viewstaff", method = RequestMethod.GET)
    public @ResponseBody String viewStaff(@RequestParam("id") Long id) {
    	log.debug("Entered viewStaff");
        
    	Staff staff = Staff.findStaff(id);
        FullClientForm fcf = setStaffToFCF(staff);

		ArrayList<FullClientForm> map = new ArrayList<FullClientForm>();
		map.add(fcf);
        String response = FullClientForm.toJsonArray(map);
        log.debug("Exiting viewStaff");
        return response;
    }  
    /**
     * Ajax/json version of grabbing the client appointment history
     */
    @RequestMapping(value = "/appthistory", method = RequestMethod.GET)
    public @ResponseBody String staffApptHistory(@RequestParam("id") Long id) {
    	log.debug("Entered staffApptHistory");
        
    	Staff staff = Staff.findStaff(id);

        // NOW GET LIST OF APPOINTMENTS FOR THIS CLIENT
        TypedQuery<Appointment> appt_history = Appointment.findAppointmentsByShopAndStaff(getShop(), staff);
        List<AppointmentDeep> apptdeep = new ArrayList<AppointmentDeep>();
        if(appt_history.getResultList().size() > 0){
			for(int x=0;x<appt_history.getResultList().size();x++){
				Appointment appt = appt_history.getResultList().get(x);
				log.debug("The toString of appt: "+ appt.toString());
				AppointmentDeep deep = new AppointmentDeep(appt);
				log.debug("The toString of DEEP appt: "+ deep.toString());
				apptdeep.add(deep);
			}
        }
        Collections.sort(apptdeep);

        String response = AppointmentDeep.toJsonArray(apptdeep);
        log.debug("Exiting staffApptHistory");
        return response;
    }      
    /**
     * Updates the Staff
     * @param itemId is the staff id
     * @param staff
     * @param bindingResult
     * @param uiModel
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT)
    public String update(Long itemId,@Valid FullClientForm staffs, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
    	log.debug("Entered Staffs update method");
    	String hphone = staffs.getHome_phone();
    	if(hphone == null ) hphone = "";
    	String wphone = staffs.getWork_phone();
    	if(wphone == null ) wphone = "";
    	String cphone  = staffs.getCell_phone();
    	if(cphone == null ) cphone = "";
    	String email  = staffs.getEmail();
    	if(email == null ) email = "";
    	
    	if (bindingResult.hasErrors()) {
            uiModel.addAttribute("staff", staffs);
            addDateTimeFormatPatterns(uiModel);
            return "staff/update";
        }
    	
    	
        String addr1 = staffs.getAddress1();
        log.debug("addr1 "+addr1);
        String addr2 = staffs.getAddress2();
        log.debug("addr2 "+addr2);
        String city = staffs.getCitycode();
        log.debug("city "+city);
        String state = staffs.getStatecode();
        log.debug("state "+state);
        String zip = staffs.getZipcode();
        log.debug("zip "+zip);
        if(addr1 == null) addr1 = "1";
        if(addr2 == null) addr2 = "2";
        if(city == null) city = "city";
        if(state == null) state = "st";
        if(zip == null) zip = "12345";

        
        uiModel.asMap().clear();
        Staff staff = Staff.findStaff(itemId);
        if(staff != null){
	        staff.setFirstName(staffs.getFirstName());
	        staff.setLastName(staffs.getLastName());
	        if(staffs.getBirthDay() == null || staffs.getBirthDay().equalsIgnoreCase("null")){
	        	staff.setBirthDay(null);
	        }else{
	        	log.debug("staff.getBirthDay(): "+staffs.getBirthDay());
	        	staff.setBirthDay(convertStringToDate(staffs.getBirthDay()));
	        }
	        
	        staff.merge();
	        
        	List<Addresses> addr = Addresses.findAddressesesByPerson(staff).getResultList();
    		if(addr.size()>0){
    			addr.get(0);
    			// update values
    			addr.get(0).setAddress1(addr1);
    			addr.get(0).setAddress2(addr2);
    			addr.get(0).setCitycode(city);
    			addr.get(0).setStatecode(state);
    			addr.get(0).setZipcode(zip);
    			// save/update record
    			addr.get(0).merge();
    		}        	
	        
	        // UPDATE THE COMMUNICATION
			List<Communications> comm = Communications.findCommunicationsesByPerson(staff).getResultList();
			int sz = comm.size();
			log.debug("the size of number of comms is: "+ sz);
			if(sz == 0){
				log.debug("trying to create new comms for client");
				// create a new set of communications
				Shop newshop = getShop();
				Communications com = new Communications();
				com.setCommunication_type(CommType.HOME_PHONE);
				com.setCommunication_value(hphone);
				com.setPerson(staff);
				com.setShop(newshop);
				com.persist();
				log.debug("just tried to add a new home phone");
				Communications com2 = new Communications();
				com2.setCommunication_type(CommType.WORK_PHONE);
				com2.setCommunication_value(wphone);
				com2.setPerson(staff);
				com2.setShop(newshop);
				com2.persist();
	
				Communications com3 = new Communications();
				com3.setCommunication_type(CommType.CELL_PHONE);
				com3.setCommunication_value(cphone);
				com3.setPerson(staff);
				com3.setShop(newshop);
				com3.persist();
	
				Communications com4 = new Communications();
				com4.setCommunication_type(CommType.EMAIL);
				com4.setCommunication_value(email);
				com4.setPerson(staff);
				com4.setShop(newshop);
				com4.persist();
				
				
			}else{
				for(int x=0;x<sz;x++){
					log.debug("looping to update comms");
					Communications com = comm.get(x);
					String comval = com.getCommunication_value();
					
					if(com.getCommunication_type().name().equalsIgnoreCase(CommType.HOME_PHONE.name())){
						log.debug("home phone");
						com.setCommunication_value(hphone);
						com.persist();
					}
					if(com.getCommunication_type().name().equalsIgnoreCase(CommType.WORK_PHONE.name())){
						log.debug("work phone");
						com.setCommunication_value(wphone);
						com.persist();
					}
					if(com.getCommunication_type().name().equalsIgnoreCase(CommType.CELL_PHONE.name())){
						log.debug("cell phone");
						com.setCommunication_value(cphone);
						com.persist();
					}
					if(com.getCommunication_type().name().equalsIgnoreCase(CommType.EMAIL.name())){
						log.debug("email");
						com.setCommunication_value(email);
						com.persist();
					}
				}
			}        
        }
		// adding audit log
		saveAuditMessage("Staff "+ staff.getFirstName() + " " + staff.getLastName() +" UPDATED ",null,"GENERAL");
        
        return "redirect:/mystaff/" + encodeUrlPathSegment(staffs.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public ModelAndView updateForm(@PathVariable("id") Long id, Model uiModel) {
    	log.debug("Entered Staff updateForm method");
    	
        addDateTimeFormatPatterns(uiModel);
        Staff staff = Staff.findStaff(id);
        FullClientForm fcf = new FullClientForm();
        fcf.setVersion(staff.getVersion());
        fcf.setFirstName(staff.getFirstName());
        fcf.setLastName(staff.getLastName());
        fcf.setId(staff.getId());
        if(staff.getBirthDay() == null){
        	fcf.setBirthDay(null);
        }else{
        	log.debug("staff.getBirthDay(): "+staff.getBirthDay());
        	fcf.setBirthDay(""+staff.getBirthDay());
        }
        
        uiModel.addAttribute("staff", fcf);
		List<Addresses> addr = Addresses.findAddressesesByPerson(staff).getResultList();
		if(addr.size()>0){
			fcf.setAddress1(addr.get(0).getAddress1());
			fcf.setAddress2(addr.get(0).getAddress2());
			fcf.setCitycode(addr.get(0).getCitycode());
			fcf.setStatecode(addr.get(0).getStatecode());
			fcf.setZipcode(addr.get(0).getZipcode());
		}
		List<Communications> comm = Communications.findCommunicationsesByPerson(staff).getResultList();
		int sz = comm.size();

		for(int x=0;x<sz;x++){
			Communications com = comm.get(x);
			String comval = com.getCommunication_value();
			
			if(com.getCommunication_type().name().equalsIgnoreCase(CommType.HOME_PHONE.name())){
				fcf.setHome_phone(comval);
			}
			if(com.getCommunication_type().name().equalsIgnoreCase(CommType.WORK_PHONE.name())){
				fcf.setWork_phone(comval);
			}
			if(com.getCommunication_type().name().equalsIgnoreCase(CommType.CELL_PHONE.name())){
				fcf.setCell_phone(comval);
			}
			if(com.getCommunication_type().name().equalsIgnoreCase(CommType.EMAIL.name())){
				fcf.setEmail(comval);
			}
		}
        uiModel.addAttribute("itemId", id);
        
    	ModelAndView mav = new ModelAndView();

    	
		mav.setViewName("mystaff/update");
    	
        addDateTimeFormatPatterns(uiModel);
        return mav;
    }



	
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
    	log.debug("ENTERED Staff delete method");
    	Clients client = Clients.findClients(id);
    	client.remove();
		// adding audit log
		saveAuditMessage("Staff "+ client.getFirstName() + " " + client.getLastName() +" added ",null,"GENERAL");
    	log.debug("REMOVED Staff");
        uiModel.asMap().clear();
        log.debug("CLEARED MAP");
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        log.debug("SET FIRST ATTRIBUTE");
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        log.debug("SET SECOND ATTRIBUTE");
        log.debug("EXITING Staff delete method");
        return "redirect:/mystaff?display";
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
	
	public void sendMessage(String mailFrom, String subject, String mailTo,
			String message) {
		log.debug("ENTERED sendMessage");
		org.springframework.mail.SimpleMailMessage simpleMailMessage = new org.springframework.mail.SimpleMailMessage();
		simpleMailMessage.setFrom(mailFrom);
		log.debug("mailFrom: "+mailFrom);
		simpleMailMessage.setSubject(subject);
		log.debug("subject: "+subject);
		simpleMailMessage.setTo(mailTo);
		log.debug("mailTo: "+mailTo);
		simpleMailMessage.setText(message);
		log.debug("message: "+message);
		try{
			mailTemplate.send(simpleMailMessage);
			// adding audit log
			saveAuditMessage("Email sent to que for sending | From : "+mailFrom + " | subject : " +subject + " | to : " +mailTo,null,"GENERAL");
			
		}catch(Exception e){
			log.error(e.getMessage());
		}
		log.debug("EXITING sendMessage");
	}

	public String sendMessageHTML(
			String mailFrom, 
			String subject, 
			String mailTo,
			String message) {
		log.debug("ENTERED sendMessageHTML");
		String returnmessage= "success";
		org.springframework.mail.SimpleMailMessage simpleMailMessage = new org.springframework.mail.SimpleMailMessage();
//		simpleMailMessage.setFrom(mailFrom);
		log.debug("mailFrom: "+mailFrom);
//		simpleMailMessage.setSubject(subject);
		log.debug("subject: "+subject);
//		simpleMailMessage.setTo(mailTo);
		log.debug("mailTo: "+mailTo);
//		simpleMailMessage.setText(message);
		log.debug("message: "+message);
		try{
//			mailHTMLTemplate.send(simpleMailMessage);
			
			MimeMessage mime_message = getMailHTMLTemplate().createMimeMessage();
			// use the true flag to indicate you need a multipart message
			MimeMessageHelper helper = new MimeMessageHelper(mime_message, true);
			helper.setFrom(mailFrom);
			helper.setSubject(subject);
			helper.setTo(mailTo);
			helper.setText(message, true);// the true signifies to set this as an html email

			getMailHTMLTemplate().send(mime_message);
			
			// adding audit log
			saveAuditMessage("HTML Email sent to que for sending | From : "+mailFrom + " | subject : " +subject + " | to : " +mailTo,null,"GENERAL");
			
		}catch(Exception e){
			log.error(e.getMessage());
			returnmessage = e.getMessage();
		}
		log.debug("EXITING sendMessageHTML");
		return returnmessage;
	}
	
	/**
	 * send mail
	 * 
	 * @param to
	 * @param subject
	 * @param message
	 * @return
	 */
	@RequestMapping(value="/sendmail", method=RequestMethod.GET)
	public @ResponseBody  String getCreate(
			@RequestParam(value="t") String to,
			@RequestParam(value="s") String subject,
			@RequestParam(value="m",required=false) String message
		) {
		log.debug("ENTERED getCreate OF Staff TO SEND MAIL");
		System.setProperty("file.encoding", "UTF-8");
		sendMessage(getShop().getShopuuid()+"@scheduleem.com", subject,to, message);
		
		log.debug("EXITING getCreate OF Staff TO SEND MAIL");
		return "SUCCESS";
	}
	
	/**
	 * add a staff to the database
	 */
	@RequestMapping(value="/addstaff", method=RequestMethod.GET)
	public @ResponseBody  String addStaff(
			@RequestParam(value="f") String firstname,
			@RequestParam(value="l") String lastname,
			@RequestParam(value="dob") String dob,
			@RequestParam(value="a1") String address1,
			@RequestParam(value="a2") String address2,
			@RequestParam(value="c") String ccity,
			@RequestParam(value="s") String cstate,
			@RequestParam(value="z") String czip,
			@RequestParam(value="hp") String homephone,
			@RequestParam(value="wp") String workphone,
			@RequestParam(value="cp") String cellphone,
			@RequestParam(value="e") String cemail,
			@RequestParam(value="u",required=false) String username,
			@RequestParam(value="p",required=false) String password
		) {
		log.debug("ENTERED addStaff");
		String RESULTS = "SUCCESS";
		boolean allow = registeredForMoreStaff();
		log.debug("RESULTS FROM registeredForMoreStaff(): "+allow);
		if (allow == true) {
			log.debug("ENTERED allow more staff portion of code");
			TypedQuery<Staff>	existingstaff = Staff.findStaffsByShopAndFirstNameEqualsAndLastNameEquals(getShop(),firstname,lastname);
			if(existingstaff.getResultList().size() > 0){
				RESULTS = "FAILURE_EXISTS_ALREADY";
			}else{
				try {
					Staff staff = new Staff();
					staff.setShop(getShop());
					staff.setFirstName(firstname);
					staff.setLastName(lastname);
					staff.setUsername(username);
					try {
						if (dob == null || dob.equalsIgnoreCase("null")) {
							staff.setBirthDay(null);
						} else {
							log.debug("staff.getBirthDay(): " + dob);
							staff.setBirthDay(convertStringToDate(dob));
						}
					} catch (Exception e) {
						log.error("EXCEPTION TRYING TO SAVE DOB: " + e);
					}
					log.debug("the staff trying to be persisted is: "
							+ staff.toString());
					AbstractPerson per = staff.merge();
					staff = Staff.findStaff(per.getId());
					
					Addresses addresses = new Addresses();
					String addr1 = address1;
					log.debug("addr1 " + addr1);
					String addr2 = address2;
					log.debug("addr2 " + addr2);
					String city = ccity;
					log.debug("city " + city);
					String state = cstate;
					log.debug("state " + state);
					String zip = czip;
					log.debug("zip " + zip);
					if (addr1 == null || addr1.equalsIgnoreCase("null"))
						addr1 = null;
					if (addr2 == null || addr2.equalsIgnoreCase("null"))
						addr2 = null;
					if (city == null || city.equalsIgnoreCase("null"))
						city = null;
					if (state == null || state.equalsIgnoreCase("null"))
						state = null;
					if (zip == null || zip.equalsIgnoreCase("null"))
						zip = null;
					addresses.setAddress1(addr1);
					addresses.setAddress2(addr2);
					addresses.setCitycode(city);
					addresses.setStatecode(state);
					addresses.setZipcode(zip);
					if (addresses.getPerson() == null) {
						log.debug("person / client object " + staff);
						addresses.setPerson(staff);
					}
					if (addresses.getShop() == null) {
						addresses.setShop(getShop());
					}
					log.debug("saving address");
					addresses.persist();
	
					// create a new set of communications
					Shop newshop = getShop();
					Communications com = new Communications();
					com.setCommunication_type(CommType.HOME_PHONE);
					String hphone = homephone;
					if (hphone == null || hphone.equalsIgnoreCase("null"))
						hphone = null;
					com.setCommunication_value(hphone);
					com.setPerson(staff);
					com.setShop(newshop);
					log.debug("com1 to string: " + com.toString());
					log.debug("Persisting com1");
					com.persist();
	
					Communications com2 = new Communications();
					com2.setCommunication_type(CommType.WORK_PHONE);
					String wphone = workphone;
					if (wphone == null || wphone.equalsIgnoreCase("null"))
						wphone = null;
					com2.setCommunication_value(wphone);
					com2.setPerson(staff);
					com2.setShop(newshop);
					log.debug("Persisting com2");
					com2.persist();
	
					Communications com3 = new Communications();
					com3.setCommunication_type(CommType.CELL_PHONE);
					String cphone = cellphone;
					if (cphone == null || cphone.equalsIgnoreCase("null"))
						cphone = null;
					com3.setCommunication_value(cphone);
					com3.setPerson(staff);
					com3.setShop(newshop);
					log.debug("Persisting com3");
					com3.persist();
	
					Communications com4 = new Communications();
					String email = cemail;
					if (email == null || email.equalsIgnoreCase("null"))
						email = null;
					com4.setCommunication_type(CommType.EMAIL);
					com4.setCommunication_value(email);
					com4.setPerson(staff);
					com4.setShop(newshop);
					log.debug("Persisting com4");
					com4.persist();
				} catch (Exception de) {
					RESULTS = "FAILURE";
				}
				//now use persistLdap to save to ldap
				log.debug("firstname: "+firstname);
				log.debug("lastname: "+lastname);
				log.debug("username: "+username);
				log.debug("cemail: "+cemail);
				log.debug("shopname: "+getShop().getShop_name());
				RegisterForm regform = new RegisterForm();
				regform.setFirstname(firstname);
				regform.setLastname(lastname);
				regform.setUsername(username);
				regform.setPassword(password);
				regform.setEmail(cemail);
				persistLdap(regform,getShop().getShop_name());
				// adding audit log
				saveAuditMessage("Added staff " + firstname + " " + lastname,null,"GENERAL");
				
			}
		}else{
			// not allowed, too many clients for TRIAL
			// adding audit log
			saveAuditMessage("Fail to add staff " + firstname + " " + lastname,null,"GENERAL");
			log.debug("ENTERED NOT allow more staff portion of code");
			RESULTS = "FAILURE";
		}
		log.debug("RESULTS: "+RESULTS);
		log.debug("EXITING addStaff");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}
	
	
	@RequestMapping(value="/changestaffpw", method=RequestMethod.GET)
	public @ResponseBody  String changeStaffPassword(
			@RequestParam(value="s",required=true) String staffid,
			@RequestParam(value="p",required=true) String password,
			@RequestParam(value="u",required=true) String editusername
		) {
		log.debug("ENTERED changeStaffPassword");
		log.debug("staffid: " + staffid);
		log.debug("editusername: " + editusername);
		String RESULTS = "SUCCESS";
		boolean allow = registeredForMoreStaff();
		log.debug("RESULTS FROM registeredForMoreStaff(): "+allow);
		
		if (allow == true) {
			log.debug("ENTERED allow more staff portion of code");
			TypedQuery<Staff>	existingstaff = Staff.findStaffsById(Long.parseLong(staffid));
			
			if(existingstaff.getResultList().size() > 0){
				log.debug("existing staff greater than 0");
				String username = "";
				try {
					Staff staff = existingstaff.getResultList().get(0);
					username = staff.getUsername();
					log.debug("editusername: "+editusername+" username: " + username);
					if(!editusername.equalsIgnoreCase(username)){
						// create a new record with this or update existing one in ldap
						log.debug("username: " + username);
						log.debug("editusername: " + editusername);
						log.debug("editusername and username not equal");
						if(username == null || username.equalsIgnoreCase("")){
							log.debug("username does not exists for staff");
							String email = "";
							Set<Communications> comm = staff.getCommunication();
							Iterator etr = comm.iterator();;
							while(etr.hasNext()) {
								Communications com = (Communications)etr.next();
								if(com.getCommunication_type()==CommType.EMAIL){
								email = com.getCommunication_value();
								break;
								}
							}
							
							
							// create a new one
							String firstname = staff.getFirstName();
							String lastname = staff.getLastName();
							RegisterForm regform = new RegisterForm();
							regform.setFirstname(firstname);
							regform.setLastname(lastname);
							regform.setUsername(editusername);
							regform.setPassword(password);
							regform.setEmail(email);
							log.debug("trying to create new instance of "+editusername+" in ldap");
							persistLdap(regform,getShop().getShop_name());
							log.debug("succeeded in trying to create new instance of "+editusername+" in ldap");
							
							staff.setUsername(editusername);
							staff.merge();
							// adding audit log
							saveAuditMessage("Added staff " + firstname + " " + lastname + " to ldap with username " + editusername,null,"GENERAL");
							
						}else{
							// check to see if this exists in ldap
							try{
								boolean alreadyexists = personDao.uidExists(username);
								log.debug("alreadyexists: " + alreadyexists);
								if(alreadyexists){
									log.debug(username + " already exists in ldap");
									// change the password
									log.debug(" changing password for "+ username+ "in ldap");
									updateLdapPw(username, password);
									log.debug(" changed password for "+ username+ "in ldap");
									// adding audit log
									saveAuditMessage("changed password for staff " + username,null,"GENERAL");
								}
								
							}catch(Exception e){
								log.error("EXCEPTION TRYING TO CREATE CUSTOMER CUSTOMER");
								log.error(e);
							}
						}
						
					}else{
						if(username != null || !username.equalsIgnoreCase("")){
							updateLdapPw(username, password);	
							// adding audit log
							saveAuditMessage("changed password for staff " + username,null,"GENERAL");
						}
					}
				} catch (Exception de) {
					log.error(de);;
					RESULTS = "FAILURE";
				}
			}
		}else{
			// not allowed, too many staff for TRIAL
			// adding audit log
			saveAuditMessage("Fail to change password and or add staff id: " + staffid,null,"GENERAL");
			log.debug("ENTERED NOT allow more staff portion of code");
			RESULTS = "FAILURE";
		}
		log.debug("RESULTS: "+RESULTS);
		log.debug("EXITING changeStaffPassword");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}
	
	/**
	 * update staff in database
	 */
	@RequestMapping(value="/updatestaff", method=RequestMethod.GET)
	public @ResponseBody  String updateStaff(
			@RequestParam(value="i") String staffid,
			@RequestParam(value="f") String firstname,
			@RequestParam(value="l") String lastname,
			@RequestParam(value="dob") String dob,
			@RequestParam(value="a1") String address1,
			@RequestParam(value="a2") String address2,
			@RequestParam(value="c") String ccity,
			@RequestParam(value="s") String cstate,
			@RequestParam(value="z") String czip,
			@RequestParam(value="hp") String homephone,
			@RequestParam(value="wp") String workphone,
			@RequestParam(value="cp") String cellphone,
			@RequestParam(value="e") String cemail,
			@RequestParam(value="ag") String allowgcal
		) {
		log.debug("ENTERED updateStaff");
		log.debug("allowgcal: "+allowgcal);
		String RESULTS = "SUCCESS";
    	String hphone = homephone;
    	if(hphone == null || hphone.equalsIgnoreCase("null")) hphone = null;
    	String wphone = workphone;
    	if(wphone == null || wphone.equalsIgnoreCase("null") ) wphone = null;
    	String cphone  = cellphone;
    	if(cphone == null  || cphone.equalsIgnoreCase("null")) cphone = null;
    	String email  = cemail;
    	if(email == null || email.equalsIgnoreCase("null") ) email = null;
    	
        String addr1 = address1;
        log.debug("addr1 "+addr1);
        String addr2 = address2;
        log.debug("addr2 "+addr2);
        String city = ccity;
        log.debug("city "+city);
        String state = cstate;
        log.debug("state "+state);
        String zip = czip;
        log.debug("zip "+zip);
        if(addr1 == null || addr1.equalsIgnoreCase("null") ) addr1 = null;
        if(addr2 == null || addr2.equalsIgnoreCase("null") ) addr2 = null;
        if(city == null || city.equalsIgnoreCase("null") ) city = null;
        if(state == null || state.equalsIgnoreCase("null") ) state = null;
        if(zip == null || zip.equalsIgnoreCase("null") ) zip = null;

        long itemId = Long.parseLong(staffid); 
        Staff staff = Staff.findStaff(itemId);
        if(staff != null){
        	if(allowgcal.equalsIgnoreCase("on")){
        		staff.setUse_gcalendar(true);	
        	}else{
        		staff.setUse_gcalendar(false);
        	}
        	
	        staff.setFirstName(firstname);
	        staff.setLastName(lastname);
	        if(dob == null || dob.equalsIgnoreCase("null")){
	        	staff.setBirthDay(null);
	        }else{
	        	log.debug("staff.getBirthDay(): "+dob);
	        	staff.setBirthDay(convertStringToDate(dob));
	        }
	        
	        staff.merge();
			// adding audit log
			saveAuditMessage("Updated staff " + firstname + " " + lastname,null,"GENERAL");
	        
        	List<Addresses> addr = Addresses.findAddressesesByPerson(staff).getResultList();
    		if(addr.size()>0){
    			addr.get(0);
    			// update values
    			addr.get(0).setAddress1(addr1);
    			addr.get(0).setAddress2(addr2);
    			addr.get(0).setCitycode(city);
    			addr.get(0).setStatecode(state);
    			addr.get(0).setZipcode(zip);
    			// save/update record
    			addr.get(0).merge();
    		}else{
    			// create the address
    			log.debug("NEED TO CREATE NEW ADDRESS");
    			Addresses addrs = new Addresses();
    			addrs.setAddress1(addr1);
    			addrs.setAddress2(addr2);
    			addrs.setCitycode(city);
    			addrs.setStatecode(state);
    			addrs.setZipcode(zip);

    			addrs.setShop(getShop());
				addrs.setPerson(staff);
    			
    			addrs.merge();    			
    		}
	        
	        // UPDATE THE COMMUNICATION
			List<Communications> comm = Communications.findCommunicationsesByPerson(staff).getResultList();
			int sz = comm.size();
			log.debug("the size of number of comms is: "+ sz);
			if(sz == 0){
				log.debug("trying to create new comms for client");
				// create a new set of communications
				Shop newshop = getShop();
				Communications com = new Communications();
				com.setCommunication_type(CommType.HOME_PHONE);
				com.setCommunication_value(hphone);
				com.setPerson(staff);
				com.setShop(newshop);
				com.persist();
				log.debug("just tried to add a new home phone");
				Communications com2 = new Communications();
				com2.setCommunication_type(CommType.WORK_PHONE);
				com2.setCommunication_value(wphone);
				com2.setPerson(staff);
				com2.setShop(newshop);
				com2.persist();
	
				Communications com3 = new Communications();
				com3.setCommunication_type(CommType.CELL_PHONE);
				com3.setCommunication_value(cphone);
				com3.setPerson(staff);
				com3.setShop(newshop);
				com3.persist();
	
				Communications com4 = new Communications();
				com4.setCommunication_type(CommType.EMAIL);
				com4.setCommunication_value(email);
				com4.setPerson(staff);
				com4.setShop(newshop);
				com4.persist();
				
				
			}else{
				for(int x=0;x<sz;x++){
					log.debug("looping to update comms");
					Communications com = comm.get(x);
					String comval = com.getCommunication_value();
					
					if(com.getCommunication_type().name().equalsIgnoreCase(CommType.HOME_PHONE.name())){
						log.debug("home phone");
						com.setCommunication_value(hphone);
						com.persist();
					}
					if(com.getCommunication_type().name().equalsIgnoreCase(CommType.WORK_PHONE.name())){
						log.debug("work phone");
						com.setCommunication_value(wphone);
						com.persist();
					}
					if(com.getCommunication_type().name().equalsIgnoreCase(CommType.CELL_PHONE.name())){
						log.debug("cell phone");
						com.setCommunication_value(cphone);
						com.persist();
					}
					if(com.getCommunication_type().name().equalsIgnoreCase(CommType.EMAIL.name())){
						log.debug("email");
						com.setCommunication_value(email);
						com.persist();
					}
				}
			}        
        }
		log.debug("EXITING updateStaff");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}

	
	@RequestMapping(value="/mail", method=RequestMethod.POST)
	public @ResponseBody  String create(
			@RequestParam(value="t") String to,
			@RequestParam(value="s") String subject,
			@RequestParam(value="m",required=false) String message
			) {
		log.debug("ENTERED CREATE OF Staff TO SEND MAIL");
		System.setProperty("file.encoding", "UTF-8");
		sendMessage(getShop().getShopuuid()+"@scheduleem.com", subject,to, message);
		log.debug("EXITING CREATE OF Staff TO SEND MAIL");
		return "SUCCESS";
	}

	@ModelAttribute("selectstaff")
    public Collection<SelectClientForm> populateStaff() {
		log.debug("Entering populateStaff");
		ArrayList<SelectClientForm> map = new ArrayList<SelectClientForm>();
		Collection<Staff> stafflist = Staff.findStaffsByShop(getShop()).getResultList();
		Iterator itr = stafflist.iterator(); 
		 while(itr.hasNext()) {
			 Staff staff = (Staff)itr.next();
			 String name = staff.getFirstName() + " " + staff.getLastName();
			 Set<Communications> comm = staff.getCommunication();
			 Iterator etr = comm.iterator();;
			 while(etr.hasNext()) {
				 Communications com = (Communications)etr.next();
				 if(com.getCommunication_type()==CommType.EMAIL){
					 if(EmailAddressValidator.isValidEmailAddress(com.getCommunication_value())){
						 SelectClientForm nc = new SelectClientForm();
						 nc.setEmail(com.getCommunication_value());
						 nc.setFirstName(staff.getFirstName());
						 nc.setLastName(staff.getLastName());
						 log.debug(com.getCommunication_value()+" " + name);
						 map.add(nc);     
					 }
					 break;
				 }
			 }
		 } 		
		log.debug("Exiting populateStaff");
        return map;
    }
	@ModelAttribute("mstafflist")
    public Collection<SelectClientForm> mstaffList() {
		log.debug("Entering mstaffList");
		ArrayList<SelectClientForm> map = new ArrayList<SelectClientForm>();
		Collection<Staff> stafflist = Staff.findStaffsByShop(getShop()).getResultList();
		Iterator itr = stafflist.iterator(); 
		 while(itr.hasNext()) {
			 Staff staff = (Staff)itr.next();
			 String name = staff.getFirstName() + " " + staff.getLastName();
			 log.debug("name: " + name);
			 Set<Communications> comm = staff.getCommunication();
			 Iterator etr = comm.iterator();;
			 while(etr.hasNext()) {
				 Communications com = (Communications)etr.next();
				 if(com.getCommunication_type()==CommType.EMAIL){
					 if(EmailAddressValidator.isValidEmailAddress(com.getCommunication_value())){
						 SelectClientForm nc = new SelectClientForm();
						 nc.setEmail(com.getCommunication_value());
						 nc.setFirstName(staff.getFirstName());
						 nc.setLastName(staff.getLastName());
						 log.debug(com.getCommunication_value()+" " + name);
						 map.add(nc);     
					 }
				 }
			 }
		 } 		
		log.debug("Exiting mstaffList");
        return map;
    }
	
	@ModelAttribute("staffs")
    public @ResponseBody  String staffGrid() {
    	log.debug("ENTERED stafflist method");
    	log.debug("STEP 1");
		Collection<Staff> staff = Staff.findStaffsByShop(getShop()).getResultList();

		Iterator itr = staff.iterator();
		while (itr.hasNext()) {
			Staff staffs = (Staff) itr.next();
			String name = staffs.getFirstName();
			staffs.setFirstName(name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase());
			String lname = staffs.getLastName();
			staffs.setLastName(lname.substring(0, 1).toUpperCase() + lname.substring(1).toLowerCase());
		}
		
		String response = Staff.toJsonArray(staff);
		log.debug("EXITING stafflist method");
        return response;
    }

	@ModelAttribute("loggedinuser")
    public @ResponseBody  String loggedinUser() {
    	log.debug("ENTERED loggedinUser");
    	String response = "";
		Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	if(obj instanceof InetOrgPerson) {
			log.debug("The principal object is InetOrgPerson");
			String parentshop = ((InetOrgPerson) obj).getO();
			setIndividualusername(((InetOrgPerson) obj).getUid());
			String username = ((UserDetails) obj).getUsername();
			log.info("parentshop of LOGGED IN USER: " + parentshop);
			log.info("username of LOGGED IN USER: " + username);
			response = username;
		}else if (obj instanceof UserDetails) {
			log.debug("The principal object is UserDetails");
			String username = ((UserDetails) obj).getUsername();
			response = username;
			log.debug("username: "+username);
		}			

		log.debug("EXITING loggedinUser");
        return response;
    }
	
	/**
	 * determine if this shop is PAID 
	 * 
	 * if not PAID then only allow 30 staff
	 * 
	 * if results are true then conditions are such that we allow more staff
	 * 
	 * if results are false then conditions says not to allow more staff
	 * 
	 * @return boolean
	 */
    @ModelAttribute("allowaddstaff")
    public boolean registeredForMoreStaff() {
    	log.debug("ENTERING registeredForMoreStaff()");
    	boolean allow = false;
    	
    	if( getShop().getType() == RegistrationTypes.PAID && getShop().getExpiration_date() != null){
    		log.debug("entering PAID");
    		int results = getShop().getExpiration_date().compareTo(new Date());
    		log.debug("results that determine if allow is changed from false to true "+results);
    	    if(results > 0)
    	    	allow = true;
    	    else if (results < 0)
    	    	allow = false;
    	    else
    	    	allow = true;
    		
    	}
    	if( allow == false || getShop().getType() == RegistrationTypes.TRIAL){
    		log.debug("entering TRIAL");
    		Properties properties = new Properties();
    		try {
    		    properties.load(this.getClass().getClassLoader().getResourceAsStream("access.properties"));
    		    
    		} catch (IOException e) {
    			log.error(e);
    		}			
    		String trialnumberstaff = properties.getProperty("number.trial.staff.limit");
    		int numofstaff = Staff.findStaffsByShop(getShop()).getResultList().size();
    		log.debug("numofstaff: "+numofstaff);
    		int limitstaff = 10;
    		if(trialnumberstaff != null && !trialnumberstaff.equalsIgnoreCase("")){
    			limitstaff = Integer.parseInt(trialnumberstaff);
    		}
    		if( numofstaff < limitstaff){
    			allow = true;
    			log.debug("the number of staff is less than or equal to " + limitstaff);
    		}else{
    			log.debug("the number of staff is greater than  "+limitstaff);
    			allow = false;
    		}
    		
    	}
    	log.debug("EXITING registeredForMoreStaff()");		
        return allow;
    }
    
    @RequestMapping(value = "/allowadd", method = RequestMethod.GET)
    public @ResponseBody String allowAdd() {
    	boolean allow = registeredForMoreStaff();
    	return ""+allow;
    }

	public Audit getAudit() {
		return audit;
	}

	public void setAudit(Audit audit) {
		this.audit = audit;
	}

	public MailSender getMailTemplate() {
		return mailTemplate;
	}

	public void setMailTemplate(MailSender mailTemplate) {
		this.mailTemplate = mailTemplate;
	}


	public UserPreference getPreferences() {
		return preferences;
	}


	public void setPreferences(UserPreference preferences) {
		this.preferences = preferences;
	}

	public String getIndividualusername() {
		return individualusername;
	}

	public void setIndividualusername(String individualusername) {
		this.individualusername = individualusername;
	}

	public JavaMailSenderImpl getMailHTMLTemplate() {
		return mailHTMLTemplate;
	}

	public void setMailHTMLTemplate(JavaMailSenderImpl mailHTMLTemplate) {
		this.mailHTMLTemplate = mailHTMLTemplate;
	}
}
