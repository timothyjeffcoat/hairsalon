package com.yourhairsalon.booking.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.internet.MimeMessage;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
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
import com.yourhairsalon.booking.domain.Appointment;
import com.yourhairsalon.booking.domain.AppointmentDeep;
import com.yourhairsalon.booking.domain.Audit;
import com.yourhairsalon.booking.domain.BaseService;
import com.yourhairsalon.booking.domain.ClientGroup;
import com.yourhairsalon.booking.domain.Clients;
import com.yourhairsalon.booking.domain.Communications;
import com.yourhairsalon.booking.domain.PaymentsService;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.Addresses;
import com.yourhairsalon.booking.domain.Staff;
import com.yourhairsalon.booking.form.EmailAddressValidator;
import com.yourhairsalon.booking.form.FullClientForm;
import com.yourhairsalon.booking.form.SelectClientForm;
import com.yourhairsalon.booking.form.UserPreference;
import com.yourhairsalon.booking.reference.CommType;
import com.yourhairsalon.booking.reference.RegistrationTypes;

import flexjson.JSONSerializer;
import java.util.Comparator;

@RequestMapping("/clients")
@Controller
public class MyClientsController {
	private static final Log log = LogFactory.getLog(MyClientsController.class);
	private Shop shop;
    @Autowired
    private UserPreference preferences;
	@Autowired
	private transient MailSender mailTemplate;
	@Autowired
	private transient JavaMailSenderImpl mailHTMLTemplate;	
	private Audit audit = new Audit();

    private void setShop(Shop shop){
    	getPreferences().setShop(shop);
    	this.shop = shop;
    }
	private Shop getShop(){
		log.debug("ENTERED getShop IndexController");
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
    	log.debug("EXITING getShop IndexController");
		return getPreferences().getShop();
	}
	
	private void saveAuditMessage(String msg,Staff staff,String type ){
		getAudit().setShop(getShop());
		getAudit().setDescription(msg);
		getAudit().setStaff(staff);
		getAudit().setTs(new Date());
		getAudit().setType(type);
		getAudit().merge();
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
	
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
    	log.debug("ENTERED Clients list method");
    	ModelAndView mav = new ModelAndView();
    	log.debug("STEP 1");
        mav.setViewName("clients");
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
        	TypedQuery<Clients> client = Clients.findClientsesByShop(getShop());
        	log.debug("STEP 10");
            uiModel.addAttribute("clients",client.getResultList() );
            log.debug("STEP 11");
//        }
    	log.debug("STEP 12");
    	uiModel.addAttribute("addresses",new Addresses());
    	log.debug("STEP 13");
    	//uiModel.addAttribute("delete", false);
    	uiModel.addAttribute("typeName", "client");
    	log.debug("STEP 14");
    	uiModel.addAttribute("path", "/clients?id");
    	log.debug("STEP 15");
    	uiModel.addAttribute("labelPlural","Clients");
    	log.debug("STEP 16");
    	
        addDateTimeFormatPatterns(uiModel);
        log.debug("STEP 17");
        log.debug("EXITING Clients list method");
        return mav;
    }
    
    @RequestMapping(value="/list", method = RequestMethod.GET)
    public @ResponseBody  String clientlist() {
    	log.debug("ENTERED clientlist method");
    	log.debug("STEP 1");
		Collection<Clients> client = Clients.findClientsesByShop(getShop()).getResultList();

		Iterator itr = client.iterator();
		while (itr.hasNext()) {
			Clients clients = (Clients) itr.next();
			String name = clients.getFirstName();
			clients.setFirstName(name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase());
			String lname = clients.getLastName();
			clients.setLastName(lname.substring(0, 1).toUpperCase() + lname.substring(1).toLowerCase());
		}
		
		String response = Clients.toJsonArray2(client);
		log.debug("EXITING clientlist method");
        return response;
    }
    @RequestMapping(params = "display", method = RequestMethod.POST)
    public ModelAndView listPost(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
    	log.debug("Entered Clients listPost method");
    	ModelAndView mav = new ModelAndView();
        mav.setViewName("clients");
    	if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            //uiModel.addAttribute("clients", Clients.findClientsEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            uiModel.addAttribute("clients", Clients.findClientsesByShop(getShop()));
            float nrOfPages = (float) Clients.countClientses() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
        	TypedQuery<Clients> client = Clients.findClientsesByShop(getShop());
            uiModel.addAttribute("clients",client.getResultList() );
        }
    	
    	uiModel.addAttribute("addresses",new Addresses());
    	//uiModel.addAttribute("delete", false);
    	uiModel.addAttribute("typeName", "client");
    	uiModel.addAttribute("path", "/clients?id");
    	uiModel.addAttribute("labelPlural","Clients");
    	
        addDateTimeFormatPatterns(uiModel);
        log.debug("EXITING Clients listPost method");
        return mav;
    }
    
    void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("clients_birthday_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
    }

    /**
     * Saves the data from the form
     */
    @RequestMapping(method = RequestMethod.POST)
    public String create(@Valid FullClientForm clients, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
    	log.debug("Entered Clients create POST method");
    	if (registeredForMoreClients() == false) {
    		return "clients/create"; 
    	}
    	if (bindingResult.hasErrors()) {
    		log.debug("binding has errors so redisplay create form");
    		log.debug("error count " + bindingResult.getErrorCount());
    		log.debug("errors " + bindingResult.getFieldError());
            uiModel.addAttribute("clients", clients);
            addDateTimeFormatPatterns(uiModel);
            return "clients/create";
        }
        uiModel.asMap().clear();
        Clients client = new Clients();
        client.setShop(getShop());
        client.setFirstName(clients.getFirstName());
        client.setLastName(clients.getLastName());
        if(clients.getBirthDay() == null || clients.getBirthDay().equalsIgnoreCase("null")){
        	client.setBirthDay(null);
        }else{
        	log.debug("clients.getBirthDay(): "+clients.getBirthDay());
        	client.setBirthDay(convertStringToDate(clients.getBirthDay()));
        }
        log.debug("the client trying to be persisted is: "+client.toString());
        AbstractPerson per =  client.merge();
        client = Clients.findClients(per.getId());
        
        Addresses addresses = new Addresses();
        String addr1 = clients.getAddress1();
        log.debug("addr1 "+addr1);
        String addr2 = clients.getAddress2();
        log.debug("addr2 "+addr2);
        String city = clients.getCitycode();
        log.debug("city "+city);
        String state = clients.getStatecode();
        log.debug("state "+state);
        String zip = clients.getZipcode();
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
			log.debug("person / client object "+client);
			addresses.setPerson(client);
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
		String hphone = clients.getHome_phone();
		if(hphone == null || hphone.equalsIgnoreCase("")) hphone = "555-555-5555";
		com.setCommunication_value(hphone);
		com.setPerson(client);
		com.setShop(newshop);
		log.debug("com1 to string: "+com.toString());
		log.debug("Persisting com1");
		com.persist();

		Communications com2 = new Communications();
		com2.setCommunication_type(CommType.WORK_PHONE);
		String wphone = clients.getWork_phone();
		if(wphone == null || wphone.equalsIgnoreCase("") ) wphone = "555-555-5555";
		com2.setCommunication_value(wphone);
		com2.setPerson(client);
		com2.setShop(newshop);
		log.debug("Persisting com2");
		com2.persist();

		Communications com3 = new Communications();
		com3.setCommunication_type(CommType.CELL_PHONE);
		String cphone = clients.getCell_phone();
		if(cphone == null || cphone.equalsIgnoreCase("") ) cphone = "555-555-5555";
		com3.setCommunication_value(cphone);
		com3.setPerson(client);
		com3.setShop(newshop);
		log.debug("Persisting com3");
		com3.persist();

		Communications com4 = new Communications();
		String email = clients.getEmail();
		if(email == null || email.equalsIgnoreCase("") ) email = "placeholder@placedholder.com";
		com4.setCommunication_type(CommType.EMAIL);
		com4.setCommunication_value(email);
		com4.setPerson(client);
		com4.setShop(newshop);
		log.debug("Persisting com4");
		com4.persist();
		// adding audit log
		saveAuditMessage("Client "+ client.getFirstName() + " " + client.getLastName() +" added ",null,"GENERAL");
        
        log.debug("after trying to persist client");
        log.debug("EXITING Clients create POST method");
        return "redirect:/clients/" + encodeUrlPathSegment(client.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(params = "form", method = RequestMethod.GET)
    public ModelAndView createForm(Model uiModel) {
    	log.debug("Entered Clients createForm method");
    	ModelAndView mav = new ModelAndView();
    	Clients client = new Clients();
    	client.setShop(getShop());
        
    	FullClientForm fcf = new FullClientForm();
        uiModel.addAttribute("clients", fcf);
        
        //uiModel.addAttribute("clients", client);
        mav.setViewName("clients/create");
        mav.addObject("clients", fcf);
        
        addDateTimeFormatPatterns(uiModel);
        log.debug("Exiting Clients createForm method");
        return mav;
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
    	log.debug("Entered Clients show method");
        addDateTimeFormatPatterns(uiModel);
        Clients client = Clients.findClients(id);
        FullClientForm fcf = new FullClientForm();
        fcf.setFirstName(client.getFirstName());
        fcf.setLastName(client.getLastName());
        fcf.setId(client.getId());
        
        if(client.getBirthDay() == null){
        	fcf.setBirthDay(null);
        }else{
        	log.debug("clients.getBirthDay(): "+client.getBirthDay());
        	fcf.setBirthDay(""+client.getBirthDay());
        }
        
        uiModel.addAttribute("clients", fcf);
		List<Addresses> addr = Addresses.findAddressesesByPerson(client).getResultList();
		if(addr.size()>0){
			fcf.setAddress1(addr.get(0).getAddress1());
			fcf.setAddress2(addr.get(0).getAddress2());
			fcf.setCitycode(addr.get(0).getCitycode());
			fcf.setStatecode(addr.get(0).getStatecode());
			fcf.setZipcode(addr.get(0).getZipcode());
		}
		List<Communications> comm = Communications.findCommunicationsesByPerson(client).getResultList();
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
        // NOW GET LIST OF APPOINTMENTS FOR THIS CLIENT
        TypedQuery<Appointment> appt_history = Appointment.findAppointmentsByShopAndClient(getShop(), client);
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
        uiModel.addAttribute("clienthistory",apptdeep);
        return "clients/show";
    }
    /**
     * Ajax/json version of grabbing the client info
     */
    @RequestMapping(value = "/viewclient", method = RequestMethod.GET)
    public @ResponseBody String viewClient(@RequestParam("id") Long id) {
    	log.debug("Entered viewClient");
        
        Clients client = Clients.findClients(id);
        FullClientForm fcf = new FullClientForm();
        fcf.setFirstName(client.getFirstName());
        fcf.setLastName(client.getLastName());
        fcf.setId(client.getId());
        
        if(client.getAccepts_initial() != null){
        	fcf.setAccepts_inital_emails(client.getAccepts_initial());
        }
        if(client.getAccepts_notifications() != null){
        	fcf.setAccepts_notifications_emails(client.getAccepts_notifications());	
        }
        if(client.getAccepts_receipts() != null){
        	fcf.setAccepts_receipts_emails(client.getAccepts_receipts());	
        }
        

        if(client.getAccepts_sms_initial() != null){
        	fcf.setAccepts_inital_sms(client.getAccepts_sms_initial());
        }
        if(client.getAccepts_sms_notifications() != null){
        	fcf.setAccepts_notifications_sms(client.getAccepts_sms_notifications());	
        }
        if(client.getAccepts_sms_receipts() != null){
        	fcf.setAccepts_receipts_sms(client.getAccepts_sms_receipts());	
        }
        
        if(client.getBirthDay() == null){
        	fcf.setBirthDay(null);
        }else{
        	log.debug("clients.getBirthDay(): "+client.getBirthDay());
        	fcf.setBirthDay(""+client.getBirthDay());
        }
        
		List<Addresses> addr = Addresses.findAddressesesByPerson(client).getResultList();
		if(addr.size()>0){
			fcf.setAddress1(addr.get(0).getAddress1());
			fcf.setAddress2(addr.get(0).getAddress2());
			fcf.setCitycode(addr.get(0).getCitycode());
			fcf.setStatecode(addr.get(0).getStatecode());
			fcf.setZipcode(addr.get(0).getZipcode());
		}
		List<Communications> comm = Communications.findCommunicationsesByPerson(client).getResultList();
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
        
		ArrayList<FullClientForm> map = new ArrayList<FullClientForm>();
		map.add(fcf);
        String response = FullClientForm.toJsonArray(map);
        log.debug("Exiting viewClient");
        return response;
    }  
    /**
     * Ajax/json version of grabbing the client appointment history
     */
    @RequestMapping(value = "/appthistory", method = RequestMethod.GET)
    public @ResponseBody String clientApptHistory(@RequestParam("id") Long id) {
    	log.debug("Entered clientApptHistory");
        
        Clients client = Clients.findClients(id);

        // NOW GET LIST OF APPOINTMENTS FOR THIS CLIENT
        TypedQuery<Appointment> appt_history = Appointment.findAppointmentsByShopAndClient(getShop(), client);
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
        log.debug("Exiting clientApptHistory");
        return response;
    }      
    /**
     * Updates the Client
     * @param itemId is the client id
     * @param clients
     * @param bindingResult
     * @param uiModel
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT)
    public String update(Long itemId,@Valid FullClientForm clients, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
    	log.debug("Entered Clients update method");
    	String hphone = clients.getHome_phone();
    	if(hphone == null ) hphone = "";
    	String wphone = clients.getWork_phone();
    	if(wphone == null ) wphone = "";
    	String cphone  = clients.getCell_phone();
    	if(cphone == null ) cphone = "";
    	String email  = clients.getEmail();
    	if(email == null ) email = "";
    	
    	if (bindingResult.hasErrors()) {
            uiModel.addAttribute("clients", clients);
            addDateTimeFormatPatterns(uiModel);
            return "clients/update";
        }
    	
    	
        String addr1 = clients.getAddress1();
        log.debug("addr1 "+addr1);
        String addr2 = clients.getAddress2();
        log.debug("addr2 "+addr2);
        String city = clients.getCitycode();
        log.debug("city "+city);
        String state = clients.getStatecode();
        log.debug("state "+state);
        String zip = clients.getZipcode();
        log.debug("zip "+zip);
        if(addr1 == null) addr1 = "1";
        if(addr2 == null) addr2 = "2";
        if(city == null) city = "city";
        if(state == null) state = "st";
        if(zip == null) zip = "12345";

        
        uiModel.asMap().clear();
        Clients client = Clients.findClients(itemId);
        if(client != null){
	        client.setFirstName(clients.getFirstName());
	        client.setLastName(clients.getLastName());
	        if(clients.getBirthDay() == null || clients.getBirthDay().equalsIgnoreCase("null")){
	        	client.setBirthDay(null);
	        }else{
	        	log.debug("clients.getBirthDay(): "+clients.getBirthDay());
	        	client.setBirthDay(convertStringToDate(clients.getBirthDay()));
	        }
	        
	        client.merge();
	        
        	List<Addresses> addr = Addresses.findAddressesesByPerson(client).getResultList();
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
			List<Communications> comm = Communications.findCommunicationsesByPerson(client).getResultList();
			int sz = comm.size();
			log.debug("the size of number of comms is: "+ sz);
			if(sz == 0){
				log.debug("trying to create new comms for client");
				// create a new set of communications
				Shop newshop = getShop();
				Communications com = new Communications();
				com.setCommunication_type(CommType.HOME_PHONE);
				com.setCommunication_value(hphone);
				com.setPerson(client);
				com.setShop(newshop);
				com.persist();
				log.debug("just tried to add a new home phone");
				Communications com2 = new Communications();
				com2.setCommunication_type(CommType.WORK_PHONE);
				com2.setCommunication_value(wphone);
				com2.setPerson(client);
				com2.setShop(newshop);
				com2.persist();
	
				Communications com3 = new Communications();
				com3.setCommunication_type(CommType.CELL_PHONE);
				com3.setCommunication_value(cphone);
				com3.setPerson(client);
				com3.setShop(newshop);
				com3.persist();
	
				Communications com4 = new Communications();
				com4.setCommunication_type(CommType.EMAIL);
				com4.setCommunication_value(email);
				com4.setPerson(client);
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
		saveAuditMessage("Client "+ client.getFirstName() + " " + client.getLastName() +" UPDATED ",null,"GENERAL");
        
        return "redirect:/clients/" + encodeUrlPathSegment(clients.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public ModelAndView updateForm(@PathVariable("id") Long id, Model uiModel) {
    	log.debug("Entered Clients updateForm method");
    	
        addDateTimeFormatPatterns(uiModel);
        Clients client = Clients.findClients(id);
        FullClientForm fcf = new FullClientForm();
        fcf.setVersion(client.getVersion());
        fcf.setFirstName(client.getFirstName());
        fcf.setLastName(client.getLastName());
        fcf.setId(client.getId());
        if(client.getBirthDay() == null){
        	fcf.setBirthDay(null);
        }else{
        	log.debug("clients.getBirthDay(): "+client.getBirthDay());
        	fcf.setBirthDay(""+client.getBirthDay());
        }
        
        uiModel.addAttribute("clients", fcf);
		List<Addresses> addr = Addresses.findAddressesesByPerson(client).getResultList();
		if(addr.size()>0){
			fcf.setAddress1(addr.get(0).getAddress1());
			fcf.setAddress2(addr.get(0).getAddress2());
			fcf.setCitycode(addr.get(0).getCitycode());
			fcf.setStatecode(addr.get(0).getStatecode());
			fcf.setZipcode(addr.get(0).getZipcode());
		}
		List<Communications> comm = Communications.findCommunicationsesByPerson(client).getResultList();
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

    	
		mav.setViewName("clients/update");
    	
        addDateTimeFormatPatterns(uiModel);
        return mav;
    }
	@RequestMapping(value="/deleteclient", method=RequestMethod.GET)
	public @ResponseBody  String deleteClient(
			@RequestParam(value="id") Long id
		) {
		log.debug("ENTERED deleteClient");
		log.debug("trying to  delete client id: " + id);
		String status = "SUCCESS";
		Clients client = null;
		try{
			client = Clients.findClients(id);
			client.remove();
			// adding audit log
			saveAuditMessage("Client "+ client.getFirstName() + " " + client.getLastName() +" added ",null,"GENERAL");
		}catch(Exception e){
			log.error(e.getMessage());
			status = "FAILURE";
		}
		
		log.debug("EXITING deleteClient");
		return new JSONSerializer().exclude("*.class").serialize(status);
	}
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
    	log.debug("ENTERED Clients delete method");
    	Clients client = Clients.findClients(id);
    	client.remove();
		// adding audit log
		saveAuditMessage("Client "+ client.getFirstName() + " " + client.getLastName() +" added ",null,"GENERAL");
    	log.debug("REMOVED CLIENT");
        uiModel.asMap().clear();
        log.debug("CLEARED MAP");
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        log.debug("SET FIRST ATTRIBUTE");
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        log.debug("SET SECOND ATTRIBUTE");
        log.debug("EXITING Clients delete method");
        return "redirect:/clients?display";
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
		log.debug("ENTERED getCreate OF CLIENTS TO SEND MAIL");
		System.setProperty("file.encoding", "UTF-8");
		sendMessage(getShop().getShopuuid()+"@scheduleem.com", subject,to, message);
		
		log.debug("EXITING getCreate OF CLIENTS TO SEND MAIL");
		return "SUCCESS";
	}

	public String sendMessageHTML(
			String mailFrom, 
			String subject, 
			String mailTo,
			String message) {
		log.debug("ENTERED sendMessageHTML");
		String returnmessage= "success";
		org.springframework.mail.SimpleMailMessage simpleMailMessage = new org.springframework.mail.SimpleMailMessage();
		log.debug("mailFrom: "+mailFrom);
		log.debug("subject: "+subject);
		log.debug("mailTo: "+mailTo);
		log.debug("message: "+message);
		try{
			MimeMessage mime_message = getMailHTMLTemplate().createMimeMessage();
			// use the true flag to indicate you need a multipart message
			MimeMessageHelper helper = new MimeMessageHelper(mime_message, true);
			helper.setFrom(mailFrom);
			helper.setSubject(subject);
			helper.setTo(mailTo);
			helper.setText(message, true);// the true signifies to set this as an html email

			getMailHTMLTemplate().send(mime_message);
			
			// adding audit log
			saveAuditMessage("HTML Client Email sent to que for sending | From : "+mailFrom + " | subject : " +subject + " | to : " +mailTo,null,"GENERAL");
			
		}catch(Exception e){
			log.error(e.getMessage());
			returnmessage = e.getMessage();
		}
		log.debug("EXITING sendMessageHTML");
		return returnmessage;
	}	
	
	/**
	 * add group
	 * @param name
	 * @param notes
	 * @return
	 */
	@RequestMapping(value="/addgrp", method=RequestMethod.GET)
	public @ResponseBody  String addClientGroup(
			@RequestParam(value="n") String name,
			@RequestParam(value="nt") String notes
		) {
		log.debug("ENTERED addClientGroup");
		ClientGroup cg = new ClientGroup();
		cg.setGroup_name(name);
		cg.setGroup_notes(notes);
		cg.setShop(getShop());
		cg.setCreateddate(new Date());
		AbstractGroup cg2 = cg.merge();
		// adding audit log
		saveAuditMessage("Added client group " + name,null,"GENERAL");
		
		log.debug("EXITING addClientGroup");
		return ""+cg2.getId();
	}

	/**
	 * add a client to the database
	 */
	@RequestMapping(value="/addclient", method=RequestMethod.GET)
	public @ResponseBody  String addClient(
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
			@RequestParam(value="e") String cemail
		) {
		log.debug("ENTERED addClient");
		String RESULTS = "SUCCESS";
		boolean allow = registeredForMoreClients();
		log.debug("RESULTS FROM registeredForMoreClients(): "+allow);
		if (allow == true) {
			log.debug("ENTERED allow more clients portion of code");
			TypedQuery<Clients>	existingclient = Clients.findClientsesByShopAndFirstNameEqualsAndLastNameEquals(getShop(),firstname,lastname);
			if(existingclient.getResultList().size() > 0){
				RESULTS = "FAILURE_EXISTS_ALREADY";
			}else{
				try {
					Clients client = new Clients();
					client.setShop(getShop());
					client.setFirstName(firstname);
					client.setLastName(lastname);
					try {
						if (dob == null || dob.equalsIgnoreCase("null")) {
							client.setBirthDay(null);
						} else {
							log.debug("clients.getBirthDay(): " + dob);
							client.setBirthDay(convertStringToDate(dob));
						}
					} catch (Exception e) {
						log.error("EXCEPTION TRYING TO SAVE DOB: " + e);
					}
					log.debug("the client trying to be persisted is: "
							+ client.toString());
					AbstractPerson per = client.merge();
					client = Clients.findClients(per.getId());
	
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
						log.debug("person / client object " + client);
						addresses.setPerson(client);
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
					com.setPerson(client);
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
					com2.setPerson(client);
					com2.setShop(newshop);
					log.debug("Persisting com2");
					com2.persist();
	
					Communications com3 = new Communications();
					com3.setCommunication_type(CommType.CELL_PHONE);
					String cphone = cellphone;
					if (cphone == null || cphone.equalsIgnoreCase("null"))
						cphone = null;
					com3.setCommunication_value(cphone);
					com3.setPerson(client);
					com3.setShop(newshop);
					log.debug("Persisting com3");
					com3.persist();
	
					Communications com4 = new Communications();
					String email = cemail;
					if (email == null || email.equalsIgnoreCase("null"))
						email = null;
					com4.setCommunication_type(CommType.EMAIL);
					com4.setCommunication_value(email);
					com4.setPerson(client);
					com4.setShop(newshop);
					log.debug("Persisting com4");
					com4.persist();
				} catch (Exception de) {
					RESULTS = "FAILURE";
				}
				// adding audit log
				saveAuditMessage("Added client " + firstname + " " + lastname,null,"GENERAL");
				
			}
		}else{
			// not allowed, too many clients for TRIAL
			// adding audit log
			saveAuditMessage("Fail to add client " + firstname + " " + lastname,null,"GENERAL");
			log.debug("ENTERED NOT allow more clients portion of code");
			RESULTS = "FAILURE";
		}
		log.debug("RESULTS: "+RESULTS);
		log.debug("EXITING addClient");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}
	/**
	 * add a client to the database
	 */
	@RequestMapping(value="/updateclient", method=RequestMethod.GET)
	public @ResponseBody  String updateClient(
			@RequestParam(value="i") String clientid,
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
			@RequestParam(value="n") String notifications,
			@RequestParam(value="r") String receipts,
			@RequestParam(value="init") String inital,			
			@RequestParam(value="ns") String notifications_sms,
			@RequestParam(value="rs") String receipts_sms,
			@RequestParam(value="inits") String inital_sms
			
		) {
		log.debug("ENTERED updateClient");
		log.debug("notifications: "+notifications);
		log.debug("receipts: "+receipts);
		log.debug("inital: "+inital);
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

        long itemId = Long.parseLong(clientid); 
        Clients client = Clients.findClients(itemId);
        if(client != null){
        	
        	if(inital.equalsIgnoreCase("true")){
        		client.setAccepts_initial(true);	
        	}else{
        		client.setAccepts_initial(false);
        	}
        	if(receipts.equalsIgnoreCase("true")){
        		client.setAccepts_receipts(true);	
        	}else{
        		client.setAccepts_receipts(false);
        	}
        	if(notifications.equalsIgnoreCase("true")){
        		client.setAccepts_notifications(true);	
        	}else{
        		client.setAccepts_notifications(false);
        	}
        	
        	if(inital_sms.equalsIgnoreCase("true")){
        		client.setAccepts_sms_initial(true);	
        	}else{
        		client.setAccepts_sms_initial(false);
        	}
        	if(receipts_sms.equalsIgnoreCase("true")){
        		client.setAccepts_sms_receipts(true);	
        	}else{
        		client.setAccepts_sms_receipts(false);
        	}
        	if(notifications_sms.equalsIgnoreCase("true")){
        		client.setAccepts_sms_notifications(true);	
        	}else{
        		client.setAccepts_sms_notifications(false);
        	}
        	
	        client.setFirstName(firstname);
	        client.setLastName(lastname);
	        if(dob == null || dob.equalsIgnoreCase("null")){
	        	client.setBirthDay(null);
	        }else{
	        	log.debug("clients.getBirthDay(): "+dob);
	        	client.setBirthDay(convertStringToDate(dob));
	        }
	        
	        client.merge();
			// adding audit log
			saveAuditMessage("Updated client " + firstname + " " + lastname,null,"GENERAL");
	        
        	List<Addresses> addr = Addresses.findAddressesesByPerson(client).getResultList();
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
				addrs.setPerson(client);
    			
    			addrs.merge();    			
    		}
	        
	        // UPDATE THE COMMUNICATION
			List<Communications> comm = Communications.findCommunicationsesByPerson(client).getResultList();
			int sz = comm.size();
			log.debug("the size of number of comms is: "+ sz);
			if(sz == 0){
				log.debug("trying to create new comms for client");
				// create a new set of communications
				Shop newshop = getShop();
				Communications com = new Communications();
				com.setCommunication_type(CommType.HOME_PHONE);
				com.setCommunication_value(hphone);
				com.setPerson(client);
				com.setShop(newshop);
				com.persist();
				log.debug("just tried to add a new home phone");
				Communications com2 = new Communications();
				com2.setCommunication_type(CommType.WORK_PHONE);
				com2.setCommunication_value(wphone);
				com2.setPerson(client);
				com2.setShop(newshop);
				com2.persist();
	
				Communications com3 = new Communications();
				com3.setCommunication_type(CommType.CELL_PHONE);
				com3.setCommunication_value(cphone);
				com3.setPerson(client);
				com3.setShop(newshop);
				com3.persist();
	
				Communications com4 = new Communications();
				com4.setCommunication_type(CommType.EMAIL);
				com4.setCommunication_value(email);
				com4.setPerson(client);
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
		log.debug("EXITING updateClient");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}
	/**
	 * delete group 
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/delgrp", method=RequestMethod.GET)
	public @ResponseBody  String deleteClientGroup(
			@RequestParam(value="id") Long id
		) {
		log.debug("ENTERED deleteClientGroup");
		String status = "SUCCESS";
		try{
			ClientGroup cg = ClientGroup.findClientGroup(id);
			cg.remove();
			// adding audit log
			saveAuditMessage("deleted client group " + cg.getGroup_name(),null,"GENERAL");
			
		}catch(Exception e){
			log.error(e.getMessage());
			status = "FALSE";
		}
		log.debug("EXITING deleteClientGroup");
		return status;
	}
	/**
	 * get clientgroup for id
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/cgrp", method=RequestMethod.GET)
	public @ResponseBody  String getClientGroup(
			@RequestParam(value="id") Long id
		) {
		log.debug("ENTERED getClientGroup");
		String status = "SUCCESS";
		try{
			ClientGroup cg = ClientGroup.findClientGroup(id);
			ArrayList<ClientGroup> map = new ArrayList<ClientGroup>();
			map.add(cg);
			status = ClientGroup.toJsonArray2(map);
		}catch(Exception e){
			log.error(e.getMessage());
			status = "FALSE";
		}
		log.debug("EXITING getClientGroup");
		return status;
	}
	
	/**
	 * get list of clients from client group
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/clist", method=RequestMethod.GET)
	public @ResponseBody  String getClientList(
			@RequestParam(value="id") Long id
		) {
		log.debug("ENTERED getClientList");
		String status = "SUCCESS";
		try{
			ClientGroup cg = ClientGroup.findClientGroup(id);
			Set<Clients> stg = cg.getClients();
			ArrayList<Clients> map = new ArrayList<Clients>();
			for (Iterator it=stg.iterator(); it.hasNext(); ) {
				Clients element = (Clients)it.next();
				if(element != null){
					map.add(element);
				}
			}		
			status = Clients.toJsonArray2(map);
		}catch(Exception e){
			log.error(e.getMessage());
			status = "FALSE";
		}
		log.debug("EXITING getClientList");
		return status;
	}
	/**
	 * based on the client group id retrieve a list of clients that are not 
	 * already in the list of clients for the client group.
	 * 
	 * filtered client list
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/fclist", method=RequestMethod.GET)
	public @ResponseBody  String getFilteredClientList(
			@RequestParam(value="cgid") Long id
		) {
		log.debug("ENTERED getFilteredClientList");
		String status = "SUCCESS";
		try{
			ClientGroup cg = ClientGroup.findClientGroup(id);
			Collection<Clients> clientlist = Clients.findClientsesByShop(getShop()).getResultList();
			Set<Clients> stg = cg.getClients();
			ArrayList<Clients> map = new ArrayList<Clients>();
			clientlist.removeAll(stg);
			map.addAll(clientlist);
			status = Clients.toJsonArray2(map);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		log.debug("EXITING getFilteredClientList");
		return status;
	}
	
	/**
	 * add a client based on id to the clientgroup based on id
	 * 
	 * @param clientid
	 * @param clintgrouptid
	 * @return
	 */
	@RequestMapping(value="/addc", method=RequestMethod.GET)
	public @ResponseBody  String addClient(
			@RequestParam(value="cid") Long clientid,
			@RequestParam(value="gid") Long clientgroupid
		) {
		log.debug("ENTERED addClient");
		String status = "SUCCESS";
		try{
			ClientGroup cg = ClientGroup.findClientGroup(clientgroupid);
			Clients addclient = Clients.findClients(clientid);
			Set<Clients> stg = cg.getClients();
			stg.add(addclient);
			cg.merge();
			// adding audit log
			saveAuditMessage("Added client " + addclient.getFirstName() + " " + addclient.getLastName()+ " to client group " + cg.getGroup_name(),null,"GENERAL");
			
		}catch(Exception e){
			log.error(e.getMessage());
			status = "FALSE";
		}
		log.debug("EXITING addClient");
		return status;
	}
	
	@RequestMapping(value="/removec", method=RequestMethod.GET)
	public @ResponseBody  String removeClient(
			@RequestParam(value="cid") Long clientid,
			@RequestParam(value="gid") Long clientgroupid
		) {
		log.debug("ENTERED removeClient");
		String status = "SUCCESS";
		try{
			ClientGroup cg = ClientGroup.findClientGroup(clientgroupid);
			Clients removeclient = Clients.findClients(clientid);
			Set<Clients> stg = cg.getClients();
			stg.remove(removeclient);
			cg.merge();
			// adding audit log
			saveAuditMessage("Removed client " + removeclient.getFirstName() + " " + removeclient.getLastName()+ " to client group " + cg.getGroup_name(),null,"GENERAL");
		}catch(Exception e){
			log.error(e.getMessage());
			status = "FALSE";
		}
		log.debug("EXITING removeClient");
		return status;
	}
	
	/**
	 * retrieve list of ClientGroup
	 * @return
	 */
	@RequestMapping(value="/cg", method=RequestMethod.GET)
	public @ResponseBody  String clientGroups(){
		
		log.debug("ENTERED clientGroups");
		TypedQuery<ClientGroup> tqcg = ClientGroup.findClientGroupsByShop(getShop());
		String jsonarray = ClientGroup.toJsonArray2(tqcg.getResultList());
		
		log.debug("EXITING clientGroups");
		return jsonarray;
	}
	
	@RequestMapping(value="/mail", method=RequestMethod.POST)
	public @ResponseBody  String create(
			@RequestParam(value="t") String to,
			@RequestParam(value="s") String subject,
			@RequestParam(value="m",required=false) String message
			) {
		log.debug("ENTERED CREATE OF CLIENTS TO SEND MAIL");
		System.setProperty("file.encoding", "UTF-8");
		sendMessage(getShop().getShopuuid()+"@scheduleem.com", subject,to, message);
		log.debug("EXITING CREATE OF CLIENTS TO SEND MAIL");
		return "SUCCESS";
	}

	@ModelAttribute("selectclients")
    public Collection<SelectClientForm> populateClients() {
		log.debug("Entering populateClients");
		ArrayList<SelectClientForm> map = new ArrayList<SelectClientForm>();
		Collection<Clients> clientlist = Clients.findClientsesByShop(getShop()).getResultList();
		Iterator itr = clientlist.iterator(); 
		 while(itr.hasNext()) {
			 Clients client = (Clients)itr.next();
			 String name = client.getFirstName() + " " + client.getLastName();
			 Set<Communications> comm = client.getCommunication();
			 Iterator etr = comm.iterator();;
			 while(etr.hasNext()) {
				 Communications com = (Communications)etr.next();
				 if(com.getCommunication_type()==CommType.EMAIL){
					 if(EmailAddressValidator.isValidEmailAddress(com.getCommunication_value())){
						 SelectClientForm nc = new SelectClientForm();
						 nc.setEmail(com.getCommunication_value());
						 nc.setFirstName(client.getFirstName());
						 nc.setLastName(client.getLastName());
						 log.debug(com.getCommunication_value()+" " + name);
						 map.add(nc);     
					 }
					 break;
				 }
			 }
		 } 		
		log.debug("Exiting populateClients");
        return map;
    }
	@ModelAttribute("clients")
    public @ResponseBody  String clientGrid() {
    	log.debug("ENTERED clientlist method");
    	log.debug("STEP 1");
		Collection<Clients> client = Clients.findClientsesByShop(getShop()).getResultList();

		Iterator itr = client.iterator();
		while (itr.hasNext()) {
			Clients clients = (Clients) itr.next();
			String name = clients.getFirstName();
			clients.setFirstName(name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase());
			String lname = clients.getLastName();
			clients.setLastName(lname.substring(0, 1).toUpperCase() + lname.substring(1).toLowerCase());
		}
		
		String response = Clients.toJsonArray2(client);
		log.debug("EXITING clientlist method");
        return response;
    }
	
	/**
	 * determine if this shop is PAID 
	 * 
	 * if not PAID then only allow 30 clients
	 * 
	 * if results are true then conditions are such that we allow more clients
	 * 
	 * if results are false then conditions says not to allow more clients
	 * 
	 * @return boolean
	 */
    @ModelAttribute("allowaddclient")
    public boolean registeredForMoreClients() {
    	log.debug("ENTERING registeredForMoreClients()");
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
    		String trialnumberclients = properties.getProperty("number.trial.clients.limit");
    		int numofclients = Clients.findClientsesByShop(getShop()).getResultList().size();
    		log.debug("numofclients: "+numofclients);
    		int limitclient = 15;
    		if(trialnumberclients != null && trialnumberclients.equalsIgnoreCase("")){
    			limitclient = Integer.parseInt(trialnumberclients);
    		}
    		if( numofclients < limitclient){
    			allow = true;
    			log.debug("the number of clients is less than or equal to " + limitclient);
    		}else{
    			log.debug("the number of clients is greater than  "+limitclient);
    			allow = false;
    		}
    		
    	}
    	log.debug("EXITING registeredForMoreClients()");		
        return allow;
    }
    
    @RequestMapping(value = "/allowadd", method = RequestMethod.GET)
    public @ResponseBody String allowAdd() {
    	boolean allow = registeredForMoreClients();
    	return ""+allow;
    }

	public Audit getAudit() {
		return audit;
	}

	public void setAudit(Audit audit) {
		this.audit = audit;
	}

	public UserPreference getPreferences() {
		return preferences;
	}

	public void setPreferences(UserPreference preferences) {
		this.preferences = preferences;
	}
	public JavaMailSenderImpl getMailHTMLTemplate() {
		return mailHTMLTemplate;
	}
	public void setMailHTMLTemplate(JavaMailSenderImpl mailHTMLTemplate) {
		this.mailHTMLTemplate = mailHTMLTemplate;
	}

    	
}
