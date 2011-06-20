package com.yourhairsalon.booking.web;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.yourhairsalon.booking.domain.Person;
import com.yourhairsalon.booking.domain.PersonDaoImpl;
import com.yourhairsalon.booking.domain.Registration;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.ShopSettings;
import com.yourhairsalon.booking.domain.Staff;
import com.yourhairsalon.booking.form.AppointmentNotification;
import com.yourhairsalon.booking.form.RegisterForm;
import com.yourhairsalon.booking.reference.ClientDisplay;
import com.yourhairsalon.booking.reference.CommType;
import com.yourhairsalon.booking.reference.RegistrationTypes;
import com.yourhairsalon.booking.reference.ScheduleStatus;
import com.yourhairsalon.booking.task.RegistrationNotificationHelper;

import flexjson.JSONSerializer;

@RequestMapping("/public/shop/{shopurl}/register")
@Controller
public class PublicShopRegisterController {
	private static final Log log = LogFactory.getLog(PublicShopRegisterController.class);
	
	@Autowired
    private transient RegistrationNotificationHelper registrationNotificationHelper;
    
	@Autowired
	private PersonDaoImpl personDao;

	@RequestMapping(method = RequestMethod.POST)
    public ModelAndView registerForm(@PathVariable String shopurl,@Valid RegisterForm register,  BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
    	log.debug("ENTERED registerForm");
    	ModelAndView mav = new ModelAndView();
    	mav.setView(new RedirectView("public/shop/register/index"));
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
    	persistLdap(register,shopurl);
    	// persist jpa
    	boolean good = persistJpa(register,shopurl);
    	
    	// send emails to person registering
    	System.setProperty("file.encoding", "UTF-8");
    	sendMessage(register,shopurl);
    	// direct to the ${shopname} login page
    	mav.setView(new RedirectView("../"+shopurl));
    	
    	RegisterForm regform = new RegisterForm();
    	uiModel.addAttribute("register", regform);
    	log.debug("EXITING registerForm");
        return mav;
    }
	
	/**
	 * 
	 * 
	 * @param regform
	 * @return
	 */
	private boolean sendMessage(RegisterForm regform,String shopurl){
		try{
			String username = regform.getUsername();
			AppointmentNotification an = new AppointmentNotification();
			an.setAppointmentdate(new Date());
			an.setClientname(regform.getFirstname() + " " + regform.getLastname());
			an.setDescription("Notification of registration "); 
			an.setEmail(regform.getEmail());
			String shopname = shopurl;
			TypedQuery<Shop> newshop = Shop.findShopsByShop_urlEquals(shopurl);
			if(newshop.getResultList().size() > 0){
				shopname = newshop.getResultList().get(0).getShop_name();
			}
			an.setFromshop(shopname);
			an.setShop_email_address("registration@scheduleem.com");
			an.setShop_email_message("Welcome to "+shopname+". You may login to your new account at http://www.scheduleem.com/shop/public/shop/"+shopname);
			an.setShop_email_signature("Sincerely, "+shopname);
			an.setShop_email_subject("Notification of registration at "+shopname);
			System.setProperty("file.encoding", "UTF-8");
			getRegistrationNotificationHelper().sendMessage(an);
			// send another one to me to notify that somebody registered
			an.setEmail("timjeffcoat@gmail.com");
			getRegistrationNotificationHelper().sendMessage(an);
		}catch(Exception e){
			log.error(e.getMessage());
			return false;
		}
		return true;
	}
	/**
	 * TODO: include shopname
	 * 
	 * @param regform
	 * @return
	 */
	private boolean persistJpa(RegisterForm regform,String shopurl){
		boolean rtn = false;
		String username = regform.getUsername();
		TypedQuery<Shop> newshop = Shop.findShopsByShop_urlEquals(shopurl);
		Shop shop = null;
    	if(newshop.getResultList().size() > 0){
    		shop = newshop.getResultList().get(0);
		
			// create client
			Clients clients = new Clients();
			clients.setShop(shop);
			clients.setFirstName(regform.getFirstname());
			clients.setUsername(username);
			clients.setLastName(regform.getLastname());
			clients.persist();
				// add address
			Addresses addr = new Addresses();
			addr.setAddress1("");
			addr.setAddress2("");
			addr.setCitycode("");
			addr.setStatecode("");
			addr.setZipcode("");
			addr.setShop(shop);
			addr.setPerson(clients);
			addr.persist();
				// add communication
			Communications communications = new Communications();
			communications.setPerson(clients);
			communications.setShop(shop);
			communications.setCommunication_type(CommType.CELL_PHONE);
			communications.setCommunication_value("");
			communications.persist();
			Communications communications2 = new Communications();
			communications2.setPerson(clients);
			communications2.setShop(shop);
			communications2.setCommunication_type(CommType.EMAIL);
			communications2.setCommunication_value(regform.getEmail());
			communications2.persist();
			Communications communications3 = new Communications();
			communications3.setPerson(clients);
			communications3.setShop(shop);
			communications3.setCommunication_type(CommType.HOME_PHONE);
			communications3.setCommunication_value("");
			communications3.persist();
			Communications communications4 = new Communications();
			communications4.setPerson(clients);
			communications4.setShop(shop);
			communications4.setCommunication_type(CommType.WORK_PHONE);
			communications4.setCommunication_value("");
			communications4.persist();
			rtn = true;
    	}
			
		
		return rtn;
	}
	private boolean persistLdap(RegisterForm regform,String shopname){
		boolean rtn = true;
		
		Person person = new Person();
		person.setUid(regform.getUsername());
		person.setUserPassword(regform.getPassword());
		try{
			personDao.createCustomerCustomer(person,shopname);
		}catch(Exception e){
			log.error("EXCEPTION TRYING TO CREATE CUSTOMER CUSTOMER");
			log.error(e);
			rtn = false;
		}
		return rtn; 
	}

	@RequestMapping(method = RequestMethod.GET)
    public ModelAndView index(
    		@PathVariable String shopurl,
    		Model uiModel
    		) {
    	log.debug("ENTERED INDEX OF PublicShopRegisterController");
    	ModelAndView mav = new ModelAndView();
		mav.setViewName("public/shop/register/index");
    	mav.addObject("register", new RegisterForm());
    	uiModel.addAttribute("register", new RegisterForm());
		String shopname = shopurl;
		TypedQuery<Shop> newshop = Shop.findShopsByShop_urlEquals(shopurl);
		if(newshop.getResultList().size() > 0){
			shopname = newshop.getResultList().get(0).getShop_name();
		}
		uiModel.addAttribute("shopname", shopname);
		mav.addObject("shopname",shopname);
    	log.debug("EXITING INDEX OF PublicShopRegisterController");
        return mav;
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
	
	@RequestMapping(value="/isnamedavailable", method=RequestMethod.GET)
    public @ResponseBody  String isNameAvailableJSON(@PathVariable String shopname,@RequestParam(value="name",required=true) String name) {
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
    public @ResponseBody  String register(@PathVariable String shopname, @RequestParam(value="name",required=true) String name) {
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
    			personDao.createCustomer(person,shopname);
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
