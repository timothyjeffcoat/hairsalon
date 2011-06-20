package com.yourhairsalon.booking.web;

import java.util.Date;

import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.ShopSettings;
import com.yourhairsalon.booking.form.AppointmentNotification;
import com.yourhairsalon.booking.form.RegisterForm;
import com.yourhairsalon.booking.task.RegistrationNotificationHelper;

@RequestMapping("/public/username")
@Controller
public class UsernameController {
	private static final Log log = LogFactory.getLog(UsernameController.class);
	@Autowired
    private transient RegistrationNotificationHelper registrationNotificationHelper;

    @RequestMapping(method = RequestMethod.POST, value = "{id}")
    public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    }

	
    @RequestMapping
    public ModelAndView index(Model uiModel) {
    	log.debug("ENTERED INDEX OF UsernameController");
    	ModelAndView mav = new ModelAndView();
    	mav.setViewName("public/username/index");
    	mav.addObject("register", new RegisterForm());
    	mav.addObject("successmessage","");
    	log.debug("EXITING INDEX OF UsernameController");
        return mav;
    }
	@RequestMapping(method = RequestMethod.POST)
    public ModelAndView recoverUsername(@Valid RegisterForm register,  BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
    	log.debug("ENTERED recoverUsername");
    	
    	ModelAndView mav = new ModelAndView();
    	mav.setViewName("public/username/index");
    	if (bindingResult.hasErrors()) {
    		log.error("THERE IS AN ERROR WITH THE REGISTER FORM: ");
    		if(register != null) log.error("REGISTERFORM: " + register.toString()); 
            uiModel.addAttribute("register", register);
            return mav;
        }
    	if(register != null) log.debug("REGISTERFORM: " + register.toString());
    	// send emails to person trying to recover username
    	System.setProperty("file.encoding", "UTF-8");
    	boolean success = sendMessage(register);
    	
    	RegisterForm regform = new RegisterForm();
    	uiModel.addAttribute("register", regform);
    	if(success){
    		mav.addObject("successmessage","The e-mail message has been sent. Check your e-mail for your username.");
    	}else{
    		mav.addObject("successmessage","The e-mail message was not sent with a username .");
    	}
    	log.debug("EXITING recoverUsername");
        return mav;
    }
	private boolean sendMessage(RegisterForm regform){
		try{
			String email = regform.getEmail();
			AppointmentNotification an = new AppointmentNotification();
			an.setAppointmentdate(new Date());
			an.setClientname("");
			an.setDescription("Username recovery");
			// assuming the username is the uid for the shop use this
			if (!email.equalsIgnoreCase("anonymous") && !email.equalsIgnoreCase("")) {
				log.info("email: " + email);
				TypedQuery<ShopSettings> shop = ShopSettings.findShopSettingsesByEmail_address(email);
				log.debug("the returned results for looking for "+ email + " is a size of: " + shop.getResultList().size());
				if(shop.getResultList().size() == 1){
					ShopSettings thisshop = shop.getResultList().get(0);
					String username = thisshop.getShop().getShopuuid();
					
					if(username != null && !username.equalsIgnoreCase("")){
						if(email != null){
							an.setEmail(email);
							an.setFromshop("Shop For " + username);
							an.setShop_email_address("usernamerecovery@scheduleem.com");
							an.setShop_email_message("Username recovery at scheduleem.com. Your username is: "+username);
							an.setShop_email_signature("Sincerely, Scheduleem.com");
							an.setShop_email_subject("Username recovery at scheduleem.com");
							System.setProperty("file.encoding", "UTF-8");
							getRegistrationNotificationHelper().sendMessage(an);
							log.debug("email message sent to the queue with username "+username);
						}else{
							// TODO : inform that email is bad
							log.debug("The shop email address is null");
						}
					}else{
						log.debug("The shop username is null");
					}
				}else{
					// TODO: since username is not a shop , check to see if it belongs to a Staff.
					// THIS LOGIC IS GOING TO REQUIRE THINKING ABOUT HOW PERSONS NOT THE ADMIN OF THE SHOP ARE TO BE
					// HANDLED FOR LOGIN
					log.debug("There is more than one shop that has this email address!!!");
				}
			}
		}catch(Exception e){
			log.error(e.getMessage());
			return false;
		}
		return true;
	}

	public RegistrationNotificationHelper getRegistrationNotificationHelper() {
		return registrationNotificationHelper;
	}

	public void setRegistrationNotificationHelper(
			RegistrationNotificationHelper registrationNotificationHelper) {
		this.registrationNotificationHelper = registrationNotificationHelper;
	}
}
