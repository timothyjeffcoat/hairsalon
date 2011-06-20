package com.yourhairsalon.booking.web;

import javax.persistence.TypedQuery;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.id.IdentityGenerator.GetGeneratedKeysDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.yourhairsalon.booking.domain.AbstractGroup;
import com.yourhairsalon.booking.domain.Addresses;
import com.yourhairsalon.booking.domain.Audit;
import com.yourhairsalon.booking.domain.BaseService;
import com.yourhairsalon.booking.domain.ClientGroup;
import com.yourhairsalon.booking.domain.Clients;
import com.yourhairsalon.booking.domain.Communications;
import com.yourhairsalon.booking.domain.CustomService;
import com.yourhairsalon.booking.domain.ServiceGroup;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.ShopSettings;
import com.yourhairsalon.booking.domain.Staff;
import com.yourhairsalon.booking.form.FullClientForm;
import com.yourhairsalon.booking.form.ServiceForm;
import com.yourhairsalon.booking.form.UserPreference;
import com.yourhairsalon.booking.reference.CommType;

import flexjson.JSONSerializer;
@RequestMapping("/services")
@Controller
public class ServicesController {
	private static final Log log = LogFactory.getLog(ServicesController.class);
	private Audit audit = new Audit();
	@Autowired
    private UserPreference preferences;

	private Shop shop;

    public void setShop(Shop shop){
    	getPreferences().setShop(shop);
    	this.shop = shop;
    }
	
	private void saveAuditMessage(String msg,Staff staff,String type ){
		getAudit().setShop(getShop());
		getAudit().setDescription(msg);
		getAudit().setStaff(staff);
		getAudit().setTs(new Date());
		getAudit().setType(type);
		getAudit().merge();
	}
    
	public Shop getShop(){
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
    

	@RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public ModelAndView updateForm(@PathVariable("id") Long id, Model uiModel) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("services/update");
        uiModel.addAttribute("service", BaseService.findBaseService(id));
        return mav;
    }
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ModelAndView show(@PathVariable("id") Long id, Model uiModel) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("services/show");
        uiModel.addAttribute("service", BaseService.findBaseService(id));
        uiModel.addAttribute("itemId", id);
        return mav;
    }
	
    @RequestMapping(params = "save", method = RequestMethod.POST)
    public ModelAndView saveForm(
			@RequestHeader(value="X-Requested-With", required=false) String requestedWith,
			@Valid @ModelAttribute BaseService services,
			BindingResult bindingResult,
	        HttpServletRequest httpServletRequest,
	        Model model
    		) {
    	log.debug("ENTERING saveForm Services");
    	ModelAndView mav = new ModelAndView();
    	mav.setView(new RedirectView("services/add"));
    	if (bindingResult.hasErrors()) {
    		log.error(bindingResult.getAllErrors().toString());
            return mav;
        }
    	
    	
        model.asMap().clear();
        BaseService s = new BaseService();
        s.setShop(getShop());
        model.addAttribute("service", s);
        mav.addObject("service",s);
        mav.setView(new RedirectView("services"));
        // Save the results from the Form
		BaseService baseservice = new BaseService();
		boolean sendreminders = true;
		log.debug("THE VALUE OF SEND REMINDERS: "+services.isSendReminders());
		sendreminders = services.isSendReminders(); 
		baseservice.setSendReminders(sendreminders);
		baseservice.setProcesstime(services.getProcesstime());
		baseservice.setFinishtime(services.getFinishtime());
		baseservice.setMinsetup(services.getMinsetup());
		baseservice.setCost(services.getCost());
		baseservice.setAmounttime(services.getAmounttime());
		baseservice.setLength_time(services.getLength_time());
		baseservice.setDescription(services.getDescription());
		String notes = "";
		if(services.getInfo_note() == null){
		}else{
			notes = services.getInfo_note();
		}
		baseservice.setInfo_note(notes);
		baseservice.setShop(getShop());
		
		log.debug("persisting baseservice");
		try{
			baseservice.persist();
			// adding audit log
			saveAuditMessage("Added service " + baseservice.getDescription(),null,"GENERAL");
			
		}catch(Exception e){
			log.error(e);
		}
        
        // ************************
		log.debug("EXITING saveForm Services");
        return mav;
    }

    @RequestMapping(params = "update", method = RequestMethod.PUT)
    public ModelAndView updateForm(
			@RequestHeader(value="X-Requested-With", required=false) String requestedWith,
			@Valid @ModelAttribute BaseService services,
			BindingResult bindingResult,
	        HttpServletRequest httpServletRequest,
	        Model model
    		) {
    	ModelAndView mav = new ModelAndView();
        model.asMap().clear();
        mav.setView(new RedirectView("services"));
        // Save the results from the Form
        services.merge();
		// adding audit log
		saveAuditMessage("Updated service " + services.getDescription(),null,"GENERAL");

        // ************************
        return mav;
    }
    
    @RequestMapping(params = "form", method = RequestMethod.POST)
    public ModelAndView addForm(Model uiModel, HttpServletRequest httpServletRequest) {
    	ModelAndView mav = new ModelAndView();
    	mav.setViewName("services/add");
        uiModel.asMap().clear();
        BaseService s = new BaseService();
        s.setShop(getShop());
        uiModel.addAttribute("service", s);
        mav.addObject("service",s);
        return mav;
    }
    @RequestMapping(params = "form", method = RequestMethod.GET)
    public ModelAndView createForm(Model uiModel) {
    	ModelAndView mav = new ModelAndView();
        BaseService s = new BaseService();
        s.setShop(getShop());
        uiModel.addAttribute("service", s);
        mav.setViewName("services/add");
        mav.addObject("service", s);
        return mav;
    }
	
	@RequestMapping(method = RequestMethod.GET)
    public ModelAndView getCreateForm(Model model) {
		log.debug("MYSERVICES CONTROLLER DISPLAYING MODEL AND VIEW");
		
		ModelAndView mav = new ModelAndView();
		mav.setViewName("services");
		
    	Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (obj instanceof UserDetails) {
			String username = ((UserDetails) obj).getUsername();
			if (!username.equalsIgnoreCase("anonymous")) {
				log.info("LOGGED IN USER: " + username);
				TypedQuery<Shop> shop = Shop.findShopsByShopuuid(username);
				log.debug("the returned results for looking for "+ username + " is a size of: " + shop.getResultList().size());
				if(shop.getResultList().size() > 0){
					TypedQuery<BaseService> baseService = BaseService.findBaseServicesByShop(shop.getResultList().get(0));
					List<BaseService> services = baseService.getResultList();
					for (Iterator i = services.iterator(); i.hasNext();) {
						BaseService base = (BaseService) i.next();
						if (base instanceof CustomService) {
							i.remove();
						}
					}
					
					model.addAttribute("baseService", services);
//					model.addAttribute("update", false);
					model.addAttribute("delete", false);
					model.addAttribute("typeName", "service");
					model.addAttribute("path", "/services?id");
//					model.addAttribute("label", "Service");
					model.addAttribute("labelPlural","Services");
				}else{				
					// add ATTRIBUTE SERVICES AFTER RETRIEVING FROM DATABASE
					List<BaseService> services = new ArrayList<BaseService>();
					mav.addObject("baseService", services);
				}
				ServiceForm serviceform = new ServiceForm();
				mav.addObject("serviceform",serviceform);
			}
		}
		return mav;
        
    }
	@RequestMapping(value="/addgrp", method=RequestMethod.GET)
	public @ResponseBody  String addServiceGroup(
			@RequestParam(value="n") String name,
			@RequestParam(value="nt") String notes
		) {
		log.debug("ENTERED addServiceGroup");
		ServiceGroup cg = new ServiceGroup();
		cg.setGroup_name(name);
		cg.setGroup_notes(notes);
		cg.setShop(getShop());
		cg.setCreateddate(new Date());
		AbstractGroup cg2 = cg.merge();
		// adding audit log
		saveAuditMessage("Added service group " + cg.getGroup_name(),null,"GENERAL");
		
		log.debug("EXITING addServiceGroup");
		return ""+cg2.getId();
	}

	/**
	 * delete group 
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/delgrp", method=RequestMethod.GET)
	public @ResponseBody  String deleteServiceGroup(
			@RequestParam(value="id") Long id
		) {
		log.debug("ENTERED deleteClientGroup");
		String status = "SUCCESS";
		try{
			ServiceGroup cg = ServiceGroup.findServiceGroup(id);
			cg.remove();
			// adding audit log
			saveAuditMessage("Deleted service group " + cg.getGroup_name(),null,"GENERAL");
			
		}catch(Exception e){
			log.error(e.getMessage());
			status = "FALSE";
		}
		log.debug("EXITING deleteServiceGroup");
		return status;
	}
	/**
	 * get ServiceGroup for id
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/sgrp", method=RequestMethod.GET)
	public @ResponseBody  String getServiceGroup(
			@RequestParam(value="id") Long id
		) {
		log.debug("ENTERED getServiceGroup");
		String status = "SUCCESS";
		try{
			ServiceGroup cg = ServiceGroup.findServiceGroup(id);
			ArrayList<ServiceGroup> map = new ArrayList<ServiceGroup>();
			map.add(cg);
			status = ServiceGroup.toJsonArray(map);
		}catch(Exception e){
			log.error(e.getMessage());
			status = "FALSE";
		}
		log.debug("EXITING getServiceGroup");
		return status;
	}
	
	/**
	 * get list of Services from Service group
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/slist", method=RequestMethod.GET)
	public @ResponseBody  String getServiceList(
			@RequestParam(value="id") Long id
		) {
		log.debug("ENTERED getServiceList");
		String status = "SUCCESS";
		status = new JSONSerializer().exclude("*.class").serialize("SUCCESS");
		try{
			ServiceGroup cg = ServiceGroup.findServiceGroup(id);
			Set<BaseService> stg = cg.getServices();
			ArrayList<BaseService> map = new ArrayList<BaseService>();
			for (Iterator it=stg.iterator(); it.hasNext(); ) {
				BaseService element = (BaseService)it.next();
				if(element != null){
					map.add(element);
				}
			}		
			status = BaseService.toJsonArray2(map);
		}catch(Exception e){
			log.error(e.getMessage());
			status = "FALSE";
			status = new JSONSerializer().exclude("*.class").serialize("FALSE");
		}
		log.debug("EXITING getServiceList");
		return status;
	}
	
	/**
	 * mobile version of display list of services for a given category
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/mdisplaycategoryserviceslist", method=RequestMethod.GET)
	public ModelAndView getCategoryServiceList(
			@RequestParam(value="id") Long id,
			Model uiModel
		) {
		log.debug("ENTERED getCategoryServiceList");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("services/displaycategoryservicelist");
		String status = "SUCCESS";
		status = new JSONSerializer().exclude("*.class").serialize("SUCCESS");
		try{
			ServiceGroup cg = ServiceGroup.findServiceGroup(id);
			Set<BaseService> stg = cg.getServices();
			ArrayList<BaseService> map = new ArrayList<BaseService>();
			for (Iterator it=stg.iterator(); it.hasNext(); ) {
				BaseService element = (BaseService)it.next();
				if(element != null){
					map.add(element);
				}
			}
			log.debug("size of list of cat svcs: "+map.size());
			log.debug("categoryid: "+id);
			mav.addObject("categoryid",""+id);
			uiModel.addAttribute("categoryid",""+id);
			uiModel.addAttribute("services",map);
			status = BaseService.toJsonArray2(map);
		}catch(Exception e){
			log.error(e.getMessage());
			status = "FALSE";
			status = new JSONSerializer().exclude("*.class").serialize("FALSE");
		}
		log.debug("EXITING getCategoryServiceList");
		return mav;
	}
	
	
    /**
     * Ajax/json version of grabbing the service info
     */
    @RequestMapping(value = "/viewservice", method = RequestMethod.GET)
    public @ResponseBody String viewService(@RequestParam("id") Long id) {
    	log.debug("Entered viewService");
        
        BaseService service = BaseService.findBaseService(id);

        ArrayList<BaseService> map = new ArrayList<BaseService>();
		map.add(service);
        String response = BaseService.toJsonArray2(map);
        
        log.debug("Exiting viewService");
        return response;
    }  	
	/**
	 * get list of services for this store
	 */
	@RequestMapping(value="/list", method=RequestMethod.GET)
	public @ResponseBody  String getServicesList() {
		log.debug("ENTERED getServicesList");
		String status = new JSONSerializer().exclude("*.class").serialize("SUCCESS");
		log.debug("step 1");
		try{
			List<BaseService> listbaseservices = BaseService.findBaseServicesByShop(getShop()).getResultList();
			log.debug("step 2");
			for (Iterator i = listbaseservices.iterator(); i.hasNext();) {
				log.debug("step 3");
				BaseService base = (BaseService) i.next();
				if (base instanceof CustomService) {
					log.debug("REMOVING CUSTOM SERVICE");
					i.remove();
				}
			}
			log.debug("step 4");
			status = BaseService.toJsonArray2(listbaseservices);
			log.debug("status: " + status.toString());
		}catch(Exception e){
			log.error(e.getMessage());
			status = new JSONSerializer().exclude("*.class").serialize("FAILURE");
		}
		log.debug("EXITING getServicesList");
		return status;
	}
	
	@RequestMapping(value="/deleteservice", method=RequestMethod.GET)
	public @ResponseBody  String deleteService(
			@RequestParam(value="id") Long id
		) {
		log.debug("ENTERED deleteService");
		String status = "SUCCESS";
		try{
			BaseService service = BaseService.findBaseService(id);
			service.remove();
			// adding audit log
			saveAuditMessage("Removed service " + service.getDescription(),null,"GENERAL");
			
		}catch(Exception e){
			log.error(e.getMessage());
			status = "FAILURE";
		}
		log.debug("EXITING deleteService");
		return new JSONSerializer().exclude("*.class").serialize(status);
	}
	
	/**
	 * update service to the database
	 */
	@RequestMapping(value="/updateservice", method=RequestMethod.GET)
	public @ResponseBody  String updateService(
			@RequestParam(value="id") String serviceid,
			@RequestParam(value="d") String description,
			@RequestParam(value="p") String processtime,
			@RequestParam(value="f") String finishtime,
			@RequestParam(value="m") String minsetup,
			@RequestParam(value="c") String cost,
			@RequestParam(value="a") String amounttime,
			@RequestParam(value="lt") String length_time,
			@RequestParam(value="n") String info_note
		) {
		log.debug("ENTERED updateService");
		String RESULTS = "SUCCESS";

        long itemId = Long.parseLong(serviceid); 
        BaseService service = BaseService.findBaseService(itemId);
        if(service != null){
        	service.setDescription(description);
        	service.setProcesstime(Integer.parseInt(processtime));
        	service.setFinishtime(Integer.parseInt(finishtime));
        	service.setMinsetup(Integer.parseInt(minsetup));
        	service.setCost(Float.parseFloat(cost));
        	service.setAmounttime(Integer.parseInt(amounttime));
        	service.setLength_time(Integer.parseInt(length_time));
        	service.setInfo_note(info_note);
        	service.merge();
			// adding audit log
			saveAuditMessage("Updated service " + service.getDescription(),null,"GENERAL");
        	
        }
		log.debug("EXITING updateService");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}

	@RequestMapping(value="/addservice", method=RequestMethod.GET)
	public @ResponseBody  String addService(
			@RequestParam(value="d") String description,
			@RequestParam(value="p") String processtime,
			@RequestParam(value="f") String finishtime,
			@RequestParam(value="m") String minsetup,
			@RequestParam(value="c") String cost,
			@RequestParam(value="a") String amounttime,
			@RequestParam(value="lt") String length_time,
			@RequestParam(value="n") String info_note
		) {
		log.debug("ENTERED addService");
		String RESULTS = "SUCCESS";

        BaseService service = new BaseService();
        if(service != null){
        	service.setShop(getShop());
        	log.debug("description "+description);
        	service.setDescription(description);
        	
        	log.debug("processtime "+processtime);
        	service.setProcesstime(Integer.parseInt(processtime));
        	
        	log.debug("finishtime "+finishtime);
        	service.setFinishtime(Integer.parseInt(finishtime));
        	
        	service.setMinsetup(Integer.parseInt(minsetup));
        	service.setCost(Float.parseFloat(cost));
        	service.setAmounttime(Integer.parseInt(amounttime));
        	service.setLength_time(Integer.parseInt(length_time));
        	service.setInfo_note(info_note);
        	try{
        		service.persist();
    			// adding audit log
    			saveAuditMessage("Added service " + service.getDescription(),null,"GENERAL");
        		
        	}catch(Exception e){
        		log.error("EXCEPTION TRYING TO SAVE NEW SERVICE: "+e);
        	}
        }
		log.debug("EXITING addService");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}	
	
	/**
	 * based on the Service group id retrieve a list of Services that are not 
	 * already in the list of Services for the Service group.
	 * 
	 * filtered Service list
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/fclist", method=RequestMethod.GET)
	public @ResponseBody  String getFilteredServiceList(
			@RequestParam(value="sgid") Long id
		) {
		log.debug("ENTERED getFilteredServiceList");
		String status = "SUCCESS";
		try{
			ServiceGroup cg = ServiceGroup.findServiceGroup(id);
			Collection<BaseService> servicelist = BaseService.findBaseServicesByShop(getShop()).getResultList();
			Set<BaseService> stg = cg.getServices();
			ArrayList<BaseService> map = new ArrayList<BaseService>();
			servicelist.removeAll(stg);
//			//TODO now remove all CustomService
//			
//			// now add list to map
			map.addAll(servicelist);
			status = BaseService.toJsonArray2(map);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		log.debug("EXITING getFilteredServiceList");
		return status;
	}
			
	
	/**
	 * add a Service based on id to the Servicegroup based on id
	 * 
	 * @param Serviceid
	 * @param slintgrouptid
	 * @return
	 */
	@RequestMapping(value="/adds", method=RequestMethod.GET)
	public @ResponseBody  String addService(
			@RequestParam(value="sid") Long serviceid,
			@RequestParam(value="gid") Long servicegroupid
		) {
		log.debug("ENTERED addService");
		String status = "SUCCESS";
		status = new JSONSerializer().exclude("*.class").serialize("SUCCESS");
		try{
			ServiceGroup cg = ServiceGroup.findServiceGroup(servicegroupid);
			BaseService addservice = BaseService.findBaseService(serviceid);
			Set<BaseService> stg = cg.getServices();
			stg.add(addservice);
			cg.merge();
			// adding audit log
			saveAuditMessage("Added service "+ addservice.getDescription()+" to service group " + cg.getGroup_name(),null,"GENERAL");
			
		}catch(Exception e){
			log.error(e.getMessage());
			status = new JSONSerializer().exclude("*.class").serialize("FAILURE");
		}
		log.debug("EXITING addService");
		return status;
	}
	
	@RequestMapping(value="/removes", method=RequestMethod.GET)
	public @ResponseBody  String removeService(
			@RequestParam(value="sid") Long serviceid,
			@RequestParam(value="gid") Long servicegroupid
		) {
		log.debug("ENTERED removeService");
		String status = "SUCCESS";
		try{
			ServiceGroup cg = ServiceGroup.findServiceGroup(servicegroupid);
			BaseService removeservice = BaseService.findBaseService(serviceid);
			Set<BaseService> stg = cg.getServices();
			stg.remove(removeservice);
			cg.merge();
//			// adding audit log
			saveAuditMessage("Removed service "+ removeservice.getDescription()+" to service group " + cg.getGroup_name(),null,"GENERAL");
		}catch(Exception e){
			log.error(e.getMessage());
			status = "FALSE";
		}
		log.debug("EXITING removeService");
		return status;
	}
	
	/**
	 * retrieve list of ServiceGroup
	 * @return
	 */
	@RequestMapping(value="/sg", method=RequestMethod.GET)
	public @ResponseBody  String serviceGroups(){
		
		log.debug("ENTERED serviceGroups");
		TypedQuery<ServiceGroup> tqcg = ServiceGroup.findServiceGroupsByShop(getShop());
		String jsonarray = ServiceGroup.toJsonArray(tqcg.getResultList());
		
		log.debug("EXITING serviceGroups");
		return jsonarray;
	}



	public Audit getAudit() {
		return audit;
	}



	public void setAudit(Audit audit) {
		this.audit = audit;
	}

	public UserPreference getPreferences() {
		return preferences;
	}

	public void setPreferences(UserPreference preferences) {
		this.preferences = preferences;
	}
}
