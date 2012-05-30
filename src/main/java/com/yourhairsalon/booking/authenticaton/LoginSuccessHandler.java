package com.yourhairsalon.booking.authenticaton;


import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;


public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
	private final Log log = LogFactory.getLog(getClass());
	
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
    	log.debug("ENTERED LoginSuccessHandler onAuthenticationSuccess");
    	String lang = (String)request.getSession().getAttribute("lang");
    	log.debug("lang: " + lang);
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
    	Properties p = new Properties();
    	String gotopage = "index";
    	String role = "";

    	try {
			SecurityContextHolder.getContext().setAuthentication(authentication);
			log.debug("PRINCIPAL: "+authentication.getPrincipal().toString());
			request.setAttribute("authentication", authentication);
			log.debug("set the Authentication attribute");
		} catch (Exception e1) {
			log.error(e1.getMessage());
		}
		log.debug("EXITING InprivaSuccessHandler onAuthenticationSuccess");
//		response.sendRedirect(request.getContextPath()+"/"+gotopage+"?lang="+lang+"&locale="+lang);
		
		   setDefaultTargetUrl(request.getContextPath()+"/"+gotopage+"?lang="+lang+"&locale="+lang);
//		   setDefaultTargetUrl("/public/shop/megamoney/"+gotopage+"?lang="+lang+"&locale="+lang);
           super.onAuthenticationSuccess(request, response, authentication);		
    }    
}