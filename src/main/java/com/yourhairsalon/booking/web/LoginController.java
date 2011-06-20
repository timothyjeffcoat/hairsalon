package com.yourhairsalon.booking.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@RequestMapping("/login")
@Controller
public class LoginController {
    private static final Log log = LogFactory.getLog(LoginController.class);

    
    public LoginController() {
        if (log.isDebugEnabled()) {
            log.debug("LoginController instantiated");
        }
    }

    @RequestMapping(value ="/{locale}/{lang}",method = RequestMethod.GET)
    public ModelAndView login(
    		@PathVariable("locale") String locale, 
    		@PathVariable("lang") String lang,
    		HttpServletRequest request, 
    		Model model,
    		HttpSession session
    		) {
    	log.debug("LOGIN CONTROLLER DISPLAYING LOGIN MODEL AND VIEW");
    	ModelAndView mav = new ModelAndView();
    	SecurityContextHolder.clearContext();

		mav.addObject("locale", locale);
		mav.addObject("lang", lang);
		model.addAttribute("locale", locale);
		model.addAttribute("lang", lang);
		session.setAttribute("locale", locale);
		session.setAttribute("lang", lang);
		
		log.debug("displaying normal site");
		mav.setViewName("login");
		return mav;	
    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView loginnormal(
    		@RequestParam(value="locale", defaultValue = "en") String locale, 
    		@RequestParam(value="lang", defaultValue = "en") String lang,
    		HttpServletRequest request, 
    		Model model,
    		HttpSession session
    		) {
    	log.debug("NORMAL LOGIN CONTROLLER DISPLAYING LOGIN MODEL AND VIEW");
    	ModelAndView mav = new ModelAndView();
    	SecurityContextHolder.clearContext();

		mav.addObject("locale", locale);
		mav.addObject("lang", lang);
		model.addAttribute("locale", locale);
		model.addAttribute("lang", lang);
		session.setAttribute("locale", locale);
		session.setAttribute("lang", lang);
		
		log.debug("displaying normal site");
		mav.setViewName("login");
		return mav;	
    }    

    /**
     * Handle exceptions as gracefully as possible
     * @param ex
     * @param request
     * @return
     */
//    @ExceptionHandler(IOException.class)
//    public String handleIOException(IOException ex, HttpServletRequest request) {
//        return ClassUtils.getShortName(ex.getClass());
//    }

}
