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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.ShopSettings;
import com.yourhairsalon.booking.form.AppointmentNotification;
import com.yourhairsalon.booking.form.RegisterForm;
import com.yourhairsalon.booking.task.RegistrationNotificationHelper;

@RequestMapping("/public/forgotpw")
@Controller
public class ForgotPwController {
	private static final Log log = LogFactory.getLog(ForgotPwController.class);
	@Autowired
    private transient RegistrationNotificationHelper registrationNotificationHelper;
	
	private String encPass =  "Scheduleem,llc.84606";
	
    @RequestMapping(method = RequestMethod.POST, value = "{id}")
    public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    }


    @RequestMapping
    public ModelAndView index(Model uiModel) {
    	log.debug("ENTERED INDEX OF ForgotPwController");
    	ModelAndView mav = new ModelAndView();
    	mav.setViewName("public/forgotpw/index");
    	mav.addObject("register", new RegisterForm());
    	mav.addObject("successmessage","");
    	log.debug("EXITING INDEX OF ForgotPwController");
        return mav;
    }
	@RequestMapping(method = RequestMethod.POST)
    public ModelAndView recoverPassword(@Valid RegisterForm register,  BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
    	log.debug("ENTERED recoverPassword");
    	
    	ModelAndView mav = new ModelAndView();
    	mav.setViewName("public/forgotpw/index");
    	if (bindingResult.hasErrors()) {
    		log.error("THERE IS AN ERROR WITH THE REGISTER FORM: ");
    		if(register != null) log.error("REGISTERFORM: " + register.toString()); 
            uiModel.addAttribute("register", register);
            return mav;
        }
    	if(register != null) log.debug("REGISTERFORM: " + register.toString());
    	// send emails to person registering
    	System.setProperty("file.encoding", "UTF-8");
    	boolean success = sendMessage(register);
    	
    	RegisterForm regform = new RegisterForm();
    	uiModel.addAttribute("register", regform);
    	if(success){
    		mav.addObject("successmessage","The e-mail message has been sent. Check your e-mail for link to activate new password.");
    	}else{
    		mav.addObject("successmessage","The e-mail message was not sent. Your password will not be updated.");
    	}
    	log.debug("EXITING recoverPassword");
        return mav;
    }
	private boolean sendMessage(RegisterForm regform){
		try{
			String username = regform.getUsername();
			String email = "";
			AppointmentNotification an = new AppointmentNotification();
			an.setAppointmentdate(new Date());
			an.setClientname("Schedule'em Customer ");
			an.setDescription("Password recovery");
			// assuming the username is the uid for the shop use this
			if (!username.equalsIgnoreCase("anonymous") && !username.equalsIgnoreCase("")) {
				log.info("USER: " + username);
				TypedQuery<Shop> shop = Shop.findShopsByShopuuid(username);
				log.debug("the returned results for looking for "+ username + " is a size of: " + shop.getResultList().size());
				if(shop.getResultList().size() > 0){
					Shop thisshop = shop.getResultList().get(0);
					TypedQuery<ShopSettings> shopset = ShopSettings.findShopSettingsesByShop(thisshop);
					if(shopset.getResultList().size() > 0){
						ShopSettings thisshopset = shopset.getResultList().get(0);
						email = thisshopset.getEmail_address();
						if(email != null){
							an.setEmail(email);
							an.setFromshop("Shop For " + username);
							// use new password and encrypt it
							String pass = regform.getPassword();
					    	BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
					    	textEncryptor.setPassword(getEncPass());
					    	//the following is what is going to be appended to the url 
					    	// https://www.scheduleem.com/shop/public/resetpw?encSecret
					    	// this will include the new password and the username, 
					    	String encSecret = textEncryptor.encrypt(pass);
					    	String encUser = textEncryptor.encrypt(username);
							String base = "https://www.scheduleem.com/shop/public/resetpw?p="+encSecret+"&u="+encUser;
							an.setShop_email_address("passwordrecovery@scheduleem.com");
							an.setShop_email_message("Password recovery at scheduleem.com. You may reset your password by going to the following link.  "+base);
							an.setShop_email_signature("Sincerely, Scheduleem.com");
							an.setShop_email_subject("Password recovery at scheduleem.com");
							System.setProperty("file.encoding", "UTF-8");
							getRegistrationNotificationHelper().sendMessage(an);
							log.debug("email message sent to the queue with base "+base);
						}else{
							// TODO : inform that email is bad
							log.debug("The shop email address is null");
						}
						
					}					
				}else{
					// TODO: since username is not a shop , check to see if it belongs to a Staff.
					// THIS LOGIC IS GOING TO REQUIRE THINKING ABOUT HOW PERSONS NOT THE ADMIN OF THE SHOP ARE TO BE
					// HANDLED FOR LOGIN
					log.debug("The username does not match a shop");
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

	private String getEncPass() {
		return encPass;
	}

	private void setEncPass(String encPass) {
		this.encPass = encPass;
	}	
}
