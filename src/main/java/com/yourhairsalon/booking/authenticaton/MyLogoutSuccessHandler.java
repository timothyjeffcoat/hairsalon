package com.yourhairsalon.booking.authenticaton;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@Component
public class MyLogoutSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler implements LogoutSuccessHandler {
	private static final Log log = LogFactory.getLog(MyLogoutSuccessHandler.class);
	  @Override
	    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
	        // maybe do some other things ...
		  log.debug("ENTERED onLogoutSuccess");
//		  String mobile = request.getParameter("mobile");
//		  authentication.setAuthenticated(false);
//		  authentication.setAuthenticated(false);
//		  log.debug("authentication: "+authentication.toString());
//		  authentication = null;
//		  request.getSession().invalidate();
//		  super.setDefaultTargetUrl("/index");
//		  if(mobile != null){
//			  super.setDefaultTargetUrl("/m_index");
//		  }else{
//			  log.debug("mobile param was null");
//		  }
	        super.handle(request, response, authentication);
	    }
}