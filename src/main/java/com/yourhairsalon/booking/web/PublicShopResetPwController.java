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
import org.apache.commons.codec.binary.Hex;

@RequestMapping("/public/shop/{shopname}/resetpw")
@Controller
public class PublicShopResetPwController {
	private static final Log log = LogFactory.getLog(PublicShopResetPwController.class);
	private String encPass =  "Scheduleem,llc.84606";
	@Autowired
	private PersonDaoImpl personDao;
	
    @RequestMapping(method = RequestMethod.POST, value = "{id}")
    public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    }

    @RequestMapping
    public ModelAndView index(
    		@PathVariable String shopname,
    		@RequestParam(value="p",required=true) String newpassword,
    		@RequestParam(value="u",required=true) String username,
    		Model uiModel) {
    	log.debug("ENTERED PublicShopResetPwController ResetPw");
    	
    	// view public shop login page
		ModelAndView mav = new ModelAndView();
		mav.setView(new RedirectView("../"+shopname+"/login"));
    	if(username != null && !username.equalsIgnoreCase("") && newpassword != null && !newpassword.equalsIgnoreCase("")){
			BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
			textEncryptor.setPassword(encPass);
			try{
				byte[] theByteArray = username.getBytes();
				String bytes = new String(theByteArray);
				byte[] charname = Hex.decodeHex(bytes.toCharArray());
				String DecryptUsername = new String(charname);

				byte[] passtheByteArray = newpassword.getBytes();
				String passbytes = new String(passtheByteArray);
				byte[] charpass = Hex.decodeHex(passbytes.toCharArray());
				String Decryptpassword = new String(charpass);
				
				String password = textEncryptor.decrypt(Decryptpassword);
				String user = textEncryptor.decrypt(DecryptUsername);
				Person person = new Person();
				log.debug("username decrypted: "+user);
				log.debug("password reset initiated for user");
				person.setUid(user);
				person.setUserPassword(password);
				try{
					getPersonDao().changePassword(person);
					log.debug("past change password");
				}catch(Exception e){
					log.error(e);
					// TODO: DISPLAY FAILURE MESSAGE					
				}
			}catch(Exception e2){
				log.error(e2);
				// TODO: DISPLAY FAILURE MESSAGE
			}
    	}
    	log.debug("EXITING PublicShopResetPwController ResetPw");
        return mav;
    }


	private PersonDaoImpl getPersonDao() {
		return personDao;
	}

	private void setPersonDao(PersonDaoImpl personDao) {
		this.personDao = personDao;
	}
}
