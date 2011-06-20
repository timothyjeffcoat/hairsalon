package com.yourhairsalon.booking.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.yourhairsalon.booking.domain.Person;
import com.yourhairsalon.booking.domain.PersonDaoImpl;

@RequestMapping("/public/resetpw")
@Controller
public class ResetPwController {
	private static final Log log = LogFactory.getLog(ResetPwController.class);
	private String encPass =  "Scheduleem,llc.84606";
	@Autowired
	private PersonDaoImpl personDao;
	
    @RequestMapping(method = RequestMethod.POST, value = "{id}")
    public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    }

    @RequestMapping
    public ModelAndView index(
    		@RequestParam(value="p",required=true) String newpassword,
    		@RequestParam(value="u",required=true) String username,
    		Model uiModel) {
    	log.debug("ENTERED ResetPw");
    	ModelAndView mav = new ModelAndView();
    	// view login page
    	mav.setView(new RedirectView("../"));
    	if(username != null && !username.equalsIgnoreCase("") && newpassword != null && !newpassword.equalsIgnoreCase("")){
			BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
			textEncryptor.setPassword(encPass);
			try{
				String password = textEncryptor.decrypt(newpassword);
				String user = textEncryptor.decrypt(username);
				Person person = new Person();
				person.setUid(user);
				person.setUserPassword(password);
				try{
					getPersonDao().changePassword(person);
				}catch(Exception e){
					log.error(e);
					// TODO: DISPLAY FAILURE MESSAGE					
				}
			}catch(Exception e2){
				log.error(e2);
				// TODO: DISPLAY FAILURE MESSAGE
			}
    	}
    	log.debug("EXITING ResetPw");
        return mav;
    }


	private PersonDaoImpl getPersonDao() {
		return personDao;
	}

	private void setPersonDao(PersonDaoImpl personDao) {
		this.personDao = personDao;
	}

}
