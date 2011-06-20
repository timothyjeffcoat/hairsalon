package com.yourhairsalon.booking.web;

import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.reference.RegistrationTypes;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.format.DateTimeFormat;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;

@RooWebScaffold(path = "shops", formBackingObject = Shop.class)
@RequestMapping("/shops/**")
@Controller
public class ShopController {
	private static final Log log = LogFactory.getLog(ShopController.class);
	
    @RequestMapping(value = "/shops/user",method = RequestMethod.GET)
    public String shops(Model model) {
    	log.debug("SHOPS CONTROLLER DISPLAYING SHOPS MODEL AND VIEW");
    	Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    			
    				if (obj instanceof UserDetails) {
    					  String username = ((UserDetails)obj).getUsername();			
    					  if (!username.equalsIgnoreCase("anonymous")) {
							  log.info("LOGGED IN USER: "+username);
    					  }
    				}
    				return "shops/list";
        
    }

	@RequestMapping(params = { "find=ById", "form" }, method = RequestMethod.GET)
    public String findShopsByIdForm(Model uiModel) {
        return "shops/findShopsById";
    }

	@RequestMapping(params = "find=ById", method = RequestMethod.GET)
    public String findShopsById(@RequestParam("id") Long id, Model uiModel) {
        uiModel.addAttribute("shops", Shop.findShopsById(id).getResultList());
        return "shops/list";
    }

	@RequestMapping(params = { "find=ByShop_name", "form" }, method = RequestMethod.GET)
    public String findShopsByShop_nameForm(Model uiModel) {
        return "shops/findShopsByShop_name";
    }

	@RequestMapping(params = "find=ByShop_name", method = RequestMethod.GET)
    public String findShopsByShop_name(@RequestParam("shop_name") String shop_name, Model uiModel) {
        uiModel.addAttribute("shops", Shop.findShopsByShop_name(shop_name).getResultList());
        return "shops/list";
    }

	@RequestMapping(params = { "find=ByShop_nameEquals", "form" }, method = RequestMethod.GET)
    public String findShopsByShop_nameEqualsForm(Model uiModel) {
        return "shops/findShopsByShop_nameEquals";
    }

	@RequestMapping(params = "find=ByShop_nameEquals", method = RequestMethod.GET)
    public String findShopsByShop_nameEquals(@RequestParam("shop_name") String shop_name, Model uiModel) {
        uiModel.addAttribute("shops", Shop.findShopsByShop_nameEquals(shop_name).getResultList());
        return "shops/list";
    }

	@RequestMapping(params = { "find=ByShop_nameIsNotNull", "form" }, method = RequestMethod.GET)
    public String findShopsByShop_nameIsNotNullForm() {
        return "shops/findShopsByShop_nameIsNotNull";
    }

	@RequestMapping(params = "find=ByShop_nameIsNotNull", method = RequestMethod.GET)
    public String findShopsByShop_nameIsNotNull(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findShopsByShop_nameIsNotNull().getResultList());
        return "shops/list";
    }

	@RequestMapping(params = { "find=ByShop_nameIsNull", "form" }, method = RequestMethod.GET)
    public String findShopsByShop_nameIsNullForm() {
        return "shops/findShopsByShop_nameIsNull";
    }

	@RequestMapping(params = "find=ByShop_nameIsNull", method = RequestMethod.GET)
    public String findShopsByShop_nameIsNull(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findShopsByShop_nameIsNull().getResultList());
        return "shops/list";
    }

	@RequestMapping(params = { "find=ByShop_nameLike", "form" }, method = RequestMethod.GET)
    public String findShopsByShop_nameLikeForm(Model uiModel) {
        return "shops/findShopsByShop_nameLike";
    }

	@RequestMapping(params = "find=ByShop_nameLike", method = RequestMethod.GET)
    public String findShopsByShop_nameLike(@RequestParam("shop_name") String shop_name, Model uiModel) {
        uiModel.addAttribute("shops", Shop.findShopsByShop_nameLike(shop_name).getResultList());
        return "shops/list";
    }

	@RequestMapping(params = { "find=ByShop_nameNotEquals", "form" }, method = RequestMethod.GET)
    public String findShopsByShop_nameNotEqualsForm(Model uiModel) {
        return "shops/findShopsByShop_nameNotEquals";
    }

	@RequestMapping(params = "find=ByShop_nameNotEquals", method = RequestMethod.GET)
    public String findShopsByShop_nameNotEquals(@RequestParam("shop_name") String shop_name, Model uiModel) {
        uiModel.addAttribute("shops", Shop.findShopsByShop_nameNotEquals(shop_name).getResultList());
        return "shops/list";
    }

	@RequestMapping(params = { "find=ByShop_urlEquals", "form" }, method = RequestMethod.GET)
    public String findShopsByShop_urlEqualsForm(Model uiModel) {
        return "shops/findShopsByShop_urlEquals";
    }

	@RequestMapping(params = "find=ByShop_urlEquals", method = RequestMethod.GET)
    public String findShopsByShop_urlEquals(@RequestParam("shop_url") String shop_url, Model uiModel) {
        uiModel.addAttribute("shops", Shop.findShopsByShop_urlEquals(shop_url).getResultList());
        return "shops/list";
    }

	@RequestMapping(params = { "find=ByShopuuid", "form" }, method = RequestMethod.GET)
    public String findShopsByShopuuidForm(Model uiModel) {
        return "shops/findShopsByShopuuid";
    }

	@RequestMapping(params = "find=ByShopuuid", method = RequestMethod.GET)
    public String findShopsByShopuuid(@RequestParam("shopuuid") String shopuuid, Model uiModel) {
        uiModel.addAttribute("shops", Shop.findShopsByShopuuid(shopuuid).getResultList());
        return "shops/list";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> showJson(@PathVariable("id") Long id) {
        Shop shop = Shop.findShop(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        if (shop == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(shop.toJson(), headers, HttpStatus.OK);
    }

	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Shop.toJsonArray(Shop.findAllShops()), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json) {
        Shop.fromJsonToShop(json).persist();
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJsonArray(@RequestBody String json) {
        for (Shop shop: Shop.fromJsonArrayToShops(json)) {
            shop.persist();
        }
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJson(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (Shop.fromJsonToShop(json).merge() == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJsonArray(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        for (Shop shop: Shop.fromJsonArrayToShops(json)) {
            if (shop.merge() == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Long id) {
        Shop shop = Shop.findShop(id);
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (shop == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        shop.remove();
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ById", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindShopsById(@RequestParam("id") Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Shop.toJsonArray(Shop.findShopsById(id).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShop_name", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindShopsByShop_name(@RequestParam("shop_name") String shop_name) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Shop.toJsonArray(Shop.findShopsByShop_name(shop_name).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShop_nameEquals", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindShopsByShop_nameEquals(@RequestParam("shop_name") String shop_name) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Shop.toJsonArray(Shop.findShopsByShop_nameEquals(shop_name).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShop_nameIsNotNull", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindShopsByShop_nameIsNotNull() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Shop.toJsonArray(Shop.findShopsByShop_nameIsNotNull().getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShop_nameIsNull", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindShopsByShop_nameIsNull() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Shop.toJsonArray(Shop.findShopsByShop_nameIsNull().getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShop_nameLike", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindShopsByShop_nameLike(@RequestParam("shop_name") String shop_name) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Shop.toJsonArray(Shop.findShopsByShop_nameLike(shop_name).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShop_nameNotEquals", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindShopsByShop_nameNotEquals(@RequestParam("shop_name") String shop_name) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Shop.toJsonArray(Shop.findShopsByShop_nameNotEquals(shop_name).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShop_urlEquals", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindShopsByShop_urlEquals(@RequestParam("shop_url") String shop_url) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Shop.toJsonArray(Shop.findShopsByShop_urlEquals(shop_url).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopuuid", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindShopsByShopuuid(@RequestParam("shopuuid") String shopuuid) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Shop.toJsonArray(Shop.findShopsByShopuuid(shopuuid).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST)
    public String create(@Valid Shop shop, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("shop", shop);
            addDateTimeFormatPatterns(uiModel);
            return "shops/create";
        }
        uiModel.asMap().clear();
        shop.persist();
        return "redirect:/shops/" + encodeUrlPathSegment(shop.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model uiModel) {
        uiModel.addAttribute("shop", new Shop());
        addDateTimeFormatPatterns(uiModel);
        return "shops/create";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("shop", Shop.findShop(id));
        uiModel.addAttribute("itemId", id);
        return "shops/show";
    }

	@RequestMapping(method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            uiModel.addAttribute("shops", Shop.findShopEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) Shop.countShops() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("shops", Shop.findAllShops());
        }
        addDateTimeFormatPatterns(uiModel);
        return "shops/list";
    }

	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Shop shop, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("shop", shop);
            addDateTimeFormatPatterns(uiModel);
            return "shops/update";
        }
        uiModel.asMap().clear();
        shop.merge();
        return "redirect:/shops/" + encodeUrlPathSegment(shop.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("shop", Shop.findShop(id));
        addDateTimeFormatPatterns(uiModel);
        return "shops/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        Shop.findShop(id).remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/shops";
    }

	@ModelAttribute("shops")
    public Collection<Shop> populateShops() {
        return Shop.findAllShops();
    }

	@ModelAttribute("registrationtypeses")
    public Collection<RegistrationTypes> populateRegistrationTypeses() {
        return Arrays.asList(RegistrationTypes.class.getEnumConstants());
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("shop_expiration_date_date_format", DateTimeFormat.patternForStyle("M-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("shop_ts_date_format", DateTimeFormat.patternForStyle("M-", LocaleContextHolder.getLocale()));
    }

	String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        }
        catch (UnsupportedEncodingException uee) {}
        return pathSegment;
    }
}
