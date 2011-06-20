package com.yourhairsalon.booking.web;

import com.yourhairsalon.booking.domain.BaseService;
import com.yourhairsalon.booking.domain.ServiceGroup;
import com.yourhairsalon.booking.domain.Shop;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

@RooWebScaffold(path = "servicegroups", formBackingObject = ServiceGroup.class)
@RequestMapping("/servicegroups")
@Controller
public class ServiceGroupController {

	@RequestMapping(method = RequestMethod.POST)
    public String create(@Valid ServiceGroup serviceGroup, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("serviceGroup", serviceGroup);
            addDateTimeFormatPatterns(uiModel);
            return "servicegroups/create";
        }
        uiModel.asMap().clear();
        serviceGroup.persist();
        return "redirect:/servicegroups/" + encodeUrlPathSegment(serviceGroup.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model uiModel) {
        uiModel.addAttribute("serviceGroup", new ServiceGroup());
        addDateTimeFormatPatterns(uiModel);
        List dependencies = new ArrayList();
        if (Shop.countShops() == 0) {
            dependencies.add(new String[]{"shop", "shops"});
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "servicegroups/create";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("servicegroup", ServiceGroup.findServiceGroup(id));
        uiModel.addAttribute("itemId", id);
        return "servicegroups/show";
    }

	@RequestMapping(method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            uiModel.addAttribute("servicegroups", ServiceGroup.findServiceGroupEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) ServiceGroup.countServiceGroups() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("servicegroups", ServiceGroup.findAllServiceGroups());
        }
        addDateTimeFormatPatterns(uiModel);
        return "servicegroups/list";
    }

	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid ServiceGroup serviceGroup, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("serviceGroup", serviceGroup);
            addDateTimeFormatPatterns(uiModel);
            return "servicegroups/update";
        }
        uiModel.asMap().clear();
        serviceGroup.merge();
        return "redirect:/servicegroups/" + encodeUrlPathSegment(serviceGroup.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("serviceGroup", ServiceGroup.findServiceGroup(id));
        addDateTimeFormatPatterns(uiModel);
        return "servicegroups/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        ServiceGroup.findServiceGroup(id).remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/servicegroups";
    }

	@ModelAttribute("baseservices")
    public Collection<BaseService> populateBaseServices() {
        return BaseService.findAllBaseServices();
    }

	@ModelAttribute("servicegroups")
    public Collection<ServiceGroup> populateServiceGroups() {
        return ServiceGroup.findAllServiceGroups();
    }

	@ModelAttribute("shops")
    public Collection<Shop> populateShops() {
        return Shop.findAllShops();
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("serviceGroup_createddate_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
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

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> showJson(@PathVariable("id") Long id) {
        ServiceGroup servicegroup = ServiceGroup.findServiceGroup(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        if (servicegroup == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(servicegroup.toJson(), headers, HttpStatus.OK);
    }

	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(ServiceGroup.toJsonArray(ServiceGroup.findAllServiceGroups()), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json) {
        ServiceGroup.fromJsonToServiceGroup(json).persist();
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJsonArray(@RequestBody String json) {
        for (ServiceGroup serviceGroup: ServiceGroup.fromJsonArrayToServiceGroups(json)) {
            serviceGroup.persist();
        }
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJson(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (ServiceGroup.fromJsonToServiceGroup(json).merge() == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJsonArray(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        for (ServiceGroup serviceGroup: ServiceGroup.fromJsonArrayToServiceGroups(json)) {
            if (serviceGroup.merge() == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Long id) {
        ServiceGroup servicegroup = ServiceGroup.findServiceGroup(id);
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (servicegroup == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        servicegroup.remove();
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ById", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindServiceGroupsById(@RequestParam("id") Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(ServiceGroup.toJsonArray(ServiceGroup.findServiceGroupsById(id).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByServices", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindServiceGroupsByServices(@RequestParam("services") Set<BaseService> services) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(ServiceGroup.toJsonArray(ServiceGroup.findServiceGroupsByServices(services).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShop", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindServiceGroupsByShop(@RequestParam("shop") Shop shop) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(ServiceGroup.toJsonArray(ServiceGroup.findServiceGroupsByShop(shop).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = { "find=ById", "form" }, method = RequestMethod.GET)
    public String findServiceGroupsByIdForm(Model uiModel) {
        return "servicegroups/findServiceGroupsById";
    }

	@RequestMapping(params = "find=ById", method = RequestMethod.GET)
    public String findServiceGroupsById(@RequestParam("id") Long id, Model uiModel) {
        uiModel.addAttribute("servicegroups", ServiceGroup.findServiceGroupsById(id).getResultList());
        return "servicegroups/list";
    }

	@RequestMapping(params = { "find=ByServices", "form" }, method = RequestMethod.GET)
    public String findServiceGroupsByServicesForm(Model uiModel) {
        return "servicegroups/findServiceGroupsByServices";
    }

	@RequestMapping(params = "find=ByServices", method = RequestMethod.GET)
    public String findServiceGroupsByServices(@RequestParam("services") Set<BaseService> services, Model uiModel) {
        uiModel.addAttribute("servicegroups", ServiceGroup.findServiceGroupsByServices(services).getResultList());
        return "servicegroups/list";
    }

	@RequestMapping(params = { "find=ByShop", "form" }, method = RequestMethod.GET)
    public String findServiceGroupsByShopForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        return "servicegroups/findServiceGroupsByShop";
    }

	@RequestMapping(params = "find=ByShop", method = RequestMethod.GET)
    public String findServiceGroupsByShop(@RequestParam("shop") Shop shop, Model uiModel) {
        uiModel.addAttribute("servicegroups", ServiceGroup.findServiceGroupsByShop(shop).getResultList());
        return "servicegroups/list";
    }
}
