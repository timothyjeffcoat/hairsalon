package com.yourhairsalon.booking.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.yourhairsalon.booking.form.RegisterForm;

@RequestMapping("/public/recovery")
@Controller
public class RecoveryController {
	private static final Log log = LogFactory.getLog(RecoveryController.class);

    @RequestMapping(method = RequestMethod.POST, value = "{id}")
    public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    }

    @RequestMapping
    public ModelAndView index(
    		Model uiModel) {
    	log.debug("ENTERED INDEX OF RecoveryController");
		ModelAndView mav = new ModelAndView();
		log.debug("trying to display standard");
		mav.setViewName("public/recovery/index");
    	log.debug("EXITING INDEX OF RecoveryController");
        return mav;
    }

}
