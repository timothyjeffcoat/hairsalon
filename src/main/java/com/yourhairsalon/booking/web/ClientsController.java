package com.yourhairsalon.booking.web;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import com.yourhairsalon.booking.domain.Addresses;
import com.yourhairsalon.booking.domain.Appointment;
import com.yourhairsalon.booking.domain.BaseService;
import com.yourhairsalon.booking.domain.ClientGroup;
import com.yourhairsalon.booking.domain.Clients;
import com.yourhairsalon.booking.domain.Communications;
import com.yourhairsalon.booking.domain.CustomService;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.Staff;
import com.yourhairsalon.booking.form.UserPreference;
import flexjson.JSONSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSender;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

@RooWebScaffold(path = "clientses", formBackingObject = Clients.class)
@RequestMapping("/clientses")
@Controller
public class ClientsController {
	private static final Log log = LogFactory.getLog(ClientsController.class);
	@Autowired
	private transient MailSender mailTemplate;
    @Autowired
    private UserPreference preferences;
	
	private Shop shop;

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
	
	public void sendMessage(String mailFrom, String subject, String mailTo,
			String message) {
		log.debug("ENTERED SENDMAIL");
		org.springframework.mail.SimpleMailMessage simpleMailMessage = new org.springframework.mail.SimpleMailMessage();
		simpleMailMessage.setFrom(mailFrom);
		log.debug("mailFrom: "+mailFrom);
		simpleMailMessage.setSubject(subject);
		log.debug("subject: "+subject);
		simpleMailMessage.setTo(mailTo);
		log.debug("mailTo: "+mailTo);
		simpleMailMessage.setText(message);
		log.debug("message: "+message);
		try{
			mailTemplate.send(simpleMailMessage);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		log.debug("EXITING SENDMAIL");
	}

	@RequestMapping(value="/mail", method=RequestMethod.GET)
	public @ResponseBody  String getCreate(
			@RequestParam(value="t") String to,
			@RequestParam(value="s") String subject,
			@RequestParam(value="m",required=false) String message,
			BindingResult result, 
			Model model,
			HttpServletRequest request) {
		log.debug("ENTERED getCreate OF CLIENTS TO SEND MAIL");
//		if (result.hasErrors()) {
//			return "FAILURE";
//		}
		System.setProperty("file.encoding", "UTF-8");
		sendMessage(getShop().getShopuuid()+"@scheduleem.com", subject,to, message);
		log.debug("EXITING getCreate OF CLIENTS TO SEND MAIL");
		return "SUCCESS";
	}
	@RequestMapping(value="/mail", method=RequestMethod.POST)
	public @ResponseBody  String create(
			@RequestParam(value="t") String to,
			@RequestParam(value="s") String subject,
			@RequestParam(value="m",required=false) String message,
			BindingResult result, 
			Model model,
			HttpServletRequest request) {
		log.debug("ENTERED CREATE OF CLIENTS TO SEND MAIL");
//		if (result.hasErrors()) {
//			return "FAILURE";
//		}
		System.setProperty("file.encoding", "UTF-8");
		sendMessage(getShop().getShopuuid()+"@scheduleem.com", subject,to, message);
		log.debug("EXITING CREATE OF CLIENTS TO SEND MAIL");
		return "SUCCESS";
	}

	private String encodeUrlPathSegment(String pathSegment,
			HttpServletRequest request) {
		String enc = request.getCharacterEncoding();
		if (enc == null) {
			enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
		}
		try {
			pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
		} catch (UnsupportedEncodingException uee) {
		}
		return pathSegment;
	}

	private UserPreference getPreferences() {
		return preferences;
	}

	private void setPreferences(UserPreference preferences) {
		this.preferences = preferences;
	}	

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> showJson(@PathVariable("id") Long id) {
        Clients clients = Clients.findClients(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        if (clients == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(clients.toJson(), headers, HttpStatus.OK);
    }

	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Clients.toJsonArray2(Clients.findAllClientses()), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json) {
        Clients.fromJsonToClients(json).persist();
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJsonArray(@RequestBody String json) {
        for (Clients clients: Clients.fromJsonArrayToClientses(json)) {
            clients.persist();
        }
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJson(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (Clients.fromJsonToClients(json).merge() == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJsonArray(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        for (Clients clients: Clients.fromJsonArrayToClientses(json)) {
            if (clients.merge() == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Long id) {
        Clients clients = Clients.findClients(id);
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (clients == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        clients.remove();
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ById", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindClientsesById(@RequestParam("id") Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Clients.toJsonArray2(Clients.findClientsesById(id).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShop", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindClientsesByShop(@RequestParam("shop") Shop shop) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Clients.toJsonArray2(Clients.findClientsesByShop(shop).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndFirstNameEqualsAndLastNameEquals", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindClientsesByShopAndFirstNameEqualsAndLastNameEquals(@RequestParam("shop") Shop shop, @RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Clients.toJsonArray2(Clients.findClientsesByShopAndFirstNameEqualsAndLastNameEquals(shop, firstName, lastName).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByUsernameEquals", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindClientsesByUsernameEquals(@RequestParam("username") String username) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Clients.toJsonArray2(Clients.findClientsesByUsernameEquals(username).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model uiModel) {
        uiModel.addAttribute("clients", new Clients());
        addDateTimeFormatPatterns(uiModel);
        List dependencies = new ArrayList();
        if (Shop.countShops() == 0) {
            dependencies.add(new String[]{"shop", "shops"});
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "clientses/create";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("clients", Clients.findClients(id));
        uiModel.addAttribute("itemId", id);
        return "clientses/show";
    }

	@RequestMapping(method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            uiModel.addAttribute("clientses", Clients.findClientsEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) Clients.countClientses() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("clientses", Clients.findAllClientses());
        }
        addDateTimeFormatPatterns(uiModel);
        return "clientses/list";
    }

	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Clients clients, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("clients", clients);
            addDateTimeFormatPatterns(uiModel);
            return "clientses/update";
        }
        uiModel.asMap().clear();
        clients.merge();
        return "redirect:/clientses/" + encodeUrlPathSegment(clients.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("clients", Clients.findClients(id));
        addDateTimeFormatPatterns(uiModel);
        return "clientses/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        Clients.findClients(id).remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/clientses";
    }

	@ModelAttribute("addresseses")
    public Collection<Addresses> populateAddresseses() {
        return Addresses.findAllAddresseses();
    }

	@ModelAttribute("clientgroups")
    public Collection<ClientGroup> populateClientGroups() {
        return ClientGroup.findAllClientGroups();
    }

	@ModelAttribute("clientses")
    public Collection<Clients> populateClientses() {
        return Clients.findAllClientses();
    }

	@ModelAttribute("communicationses")
    public Collection<Communications> populateCommunicationses() {
        return Communications.findAllCommunicationses();
    }

	@ModelAttribute("shops")
    public Collection<Shop> populateShops() {
        return Shop.findAllShops();
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("clients_birthday_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
    }

	@RequestMapping(params = { "find=ById", "form" }, method = RequestMethod.GET)
    public String findClientsesByIdForm(Model uiModel) {
        return "clientses/findClientsesById";
    }

	@RequestMapping(params = "find=ById", method = RequestMethod.GET)
    public String findClientsesById(@RequestParam("id") Long id, Model uiModel) {
        uiModel.addAttribute("clientses", Clients.findClientsesById(id).getResultList());
        return "clientses/list";
    }

	@RequestMapping(params = { "find=ByShop", "form" }, method = RequestMethod.GET)
    public String findClientsesByShopForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        return "clientses/findClientsesByShop";
    }

	@RequestMapping(params = "find=ByShop", method = RequestMethod.GET)
    public String findClientsesByShop(@RequestParam("shop") Shop shop, Model uiModel) {
        uiModel.addAttribute("clientses", Clients.findClientsesByShop(shop).getResultList());
        return "clientses/list";
    }

	@RequestMapping(params = { "find=ByShopAndFirstNameEqualsAndLastNameEquals", "form" }, method = RequestMethod.GET)
    public String findClientsesByShopAndFirstNameEqualsAndLastNameEqualsForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        return "clientses/findClientsesByShopAndFirstNameEqualsAndLastNameEquals";
    }

	@RequestMapping(params = "find=ByShopAndFirstNameEqualsAndLastNameEquals", method = RequestMethod.GET)
    public String findClientsesByShopAndFirstNameEqualsAndLastNameEquals(@RequestParam("shop") Shop shop, @RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName, Model uiModel) {
        uiModel.addAttribute("clientses", Clients.findClientsesByShopAndFirstNameEqualsAndLastNameEquals(shop, firstName, lastName).getResultList());
        return "clientses/list";
    }

	@RequestMapping(params = { "find=ByUsernameEquals", "form" }, method = RequestMethod.GET)
    public String findClientsesByUsernameEqualsForm(Model uiModel) {
        return "clientses/findClientsesByUsernameEquals";
    }

	@RequestMapping(params = "find=ByUsernameEquals", method = RequestMethod.GET)
    public String findClientsesByUsernameEquals(@RequestParam("username") String username, Model uiModel) {
        uiModel.addAttribute("clientses", Clients.findClientsesByUsernameEquals(username).getResultList());
        return "clientses/list";
    }
}
