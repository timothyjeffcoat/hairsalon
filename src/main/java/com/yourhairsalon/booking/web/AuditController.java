package com.yourhairsalon.booking.web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.yourhairsalon.booking.domain.Appointment;
import com.yourhairsalon.booking.domain.AppointmentDeep;
import com.yourhairsalon.booking.domain.Audit;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.ShopSettings;
import com.yourhairsalon.booking.domain.Staff;
import com.yourhairsalon.booking.form.UserPreference;
import com.yourhairsalon.booking.reference.ScheduleStatus;

@RequestMapping("/audit")
@Controller
public class AuditController {
	private static final Log log = LogFactory.getLog(AuditController.class);
	private Shop shop;
	
    @Autowired
    private UserPreference preferences;
	
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
	
    @RequestMapping(method = RequestMethod.POST, value = "{id}")
    public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    }

    @RequestMapping
    public String index(HttpServletRequest request) {
		log.debug("displaying normal site");
		return "audit/index";	
    }
    
	@ModelAttribute("history")
	public List getAudits(Model model){
		log.debug("ENTERED getAudits");
		List<Audit> auditlist = null;
		String response = "";
		try{
			if(getShop() != null){
				TypedQuery<Audit> audits = Audit.findAuditsByShop(getShop());
				auditlist = audits.getResultList().subList(0, 5);
				log.debug("getAudits -- the number of Audit is: "+auditlist.size());
				model.addAttribute("pending_date_format", DateTimeFormat.patternForStyle("L-", LocaleContextHolder.getLocale()));
				
				Audit.toJsonArray(auditlist);
			}
		}catch(Exception e){
			log.error(e);
		}
		log.debug("EXITING getAudits");
		return auditlist;
	}
	
	@ModelAttribute("jsonhistory")
	public String getJsonAudits(Model model){
		log.debug("ENTERED getJsonAudits");
		List<Audit> auditlist = null;
		String response = "";
		try{
			if(getShop() != null){
				TypedQuery<Audit> audits = Audit.findAuditsByShop(getShop());
				auditlist = audits.getResultList();
				//log.debug("getAudits -- the number of Audit is: "+audits.getResultList().size());
				//model.addAttribute("pending_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
				
				response = Audit.toJsonArray(auditlist);
			}
		}catch(Exception e){
			log.error(e);
		}
		log.debug("EXITING getJsonAudits");
		return response;
	}
	
	@RequestMapping(value ="/jsonsmshistorythismonth", method = RequestMethod.GET)
	public ModelAndView getJsonAuditsSMSThisMonth(
			ModelMap modelMap,
			HttpServletRequest request
			) {
		log.debug("ENTERED getJsonAuditsSMSThisMonth");
		///////////////////////////////////////////////////////////
		String shoptimezone = "";
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shoptimezone = shopsetting.getTimezone();
			if(shoptimezone == null){
				shoptimezone = "MST";
				shopsetting.setTimezone(shoptimezone);
				shopsetting.merge();
			}
		}catch(Exception e){
			log.error(e);
		}
		
		TimeZone tz = TimeZone.getTimeZone(shoptimezone);
		Date dateValue = new Date();
		Calendar firstDayofMonth = Calendar.getInstance(tz);
		firstDayofMonth.setTime(dateValue);
		firstDayofMonth.set(Calendar.DAY_OF_MONTH, 1);
		firstDayofMonth.getTime().setHours(0);
		firstDayofMonth.getTime().setMinutes(0);
		Date firstDay = firstDayofMonth.getTime();
		
		Calendar calValue = Calendar.getInstance(tz);
		calValue.setTime(dateValue);
		
		calValue.add(Calendar.MONTH, 1);  
		calValue.set(Calendar.DAY_OF_MONTH, 1);  
		calValue.add(Calendar.DATE, -1);  
  
        Date lastDayOfMonth = calValue.getTime();  
        lastDayOfMonth.setHours(23);
        lastDayOfMonth.setMinutes(59);
		///////////////////////////////////////////////////////////////		
		List<Audit> auditlist = null;
		String response = "";
		long sizeofresults = 0;
		try{
			if(getShop() != null){
				TypedQuery<Audit> audits = Audit.findAuditsByShopAndTypeAndTsBetweenAndDescriptionIsATextMessage(getShop(),"TEXT MESSAGE",firstDay,lastDayOfMonth);
				auditlist = audits.getResultList();
				//log.debug("getAudits -- the number of Audit is: "+audits.getResultList().size());
				//model.addAttribute("pending_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
				sizeofresults = auditlist.size();
				log.debug("auditlist.size: "+ sizeofresults);
				response = Audit.toJsonArray(auditlist);
			}
		}catch(Exception e){
			log.error(e);
		}
		ModelAndView mav = new ModelAndView();
		mav.setViewName("audit/index");
		log.debug("response: "+response);
		modelMap.put("sms_this_month", response);
		modelMap.put("total_sms_this_month", sizeofresults);
		log.debug("EXITING getJsonAuditsSMSThisMonth");
		return mav;
	}
	public UserPreference getPreferences() {
		return preferences;
	}

	public void setPreferences(UserPreference preferences) {
		this.preferences = preferences;
	}    
	
}
