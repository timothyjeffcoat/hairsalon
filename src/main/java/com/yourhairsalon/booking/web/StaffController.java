package com.yourhairsalon.booking.web;

import com.yourhairsalon.booking.domain.Addresses;
import com.yourhairsalon.booking.domain.Communications;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.Staff;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

@RooWebScaffold(path = "staffs", formBackingObject = Staff.class)
@RequestMapping("/staffs")
@Controller
public class StaffController {

	@RequestMapping(method = RequestMethod.POST)
    public String create(@Valid Staff staff, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("staff", staff);
            addDateTimeFormatPatterns(uiModel);
            return "staffs/create";
        }
        uiModel.asMap().clear();
        staff.persist();
        return "redirect:/staffs/" + encodeUrlPathSegment(staff.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model uiModel) {
        uiModel.addAttribute("staff", new Staff());
        addDateTimeFormatPatterns(uiModel);
        List dependencies = new ArrayList();
        if (Shop.countShops() == 0) {
            dependencies.add(new String[]{"shop", "shops"});
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "staffs/create";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("staff", Staff.findStaff(id));
        uiModel.addAttribute("itemId", id);
        return "staffs/show";
    }

	@RequestMapping(method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            uiModel.addAttribute("staffs", Staff.findStaffEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) Staff.countStaffs() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("staffs", Staff.findAllStaffs());
        }
        addDateTimeFormatPatterns(uiModel);
        return "staffs/list";
    }

	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Staff staff, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("staff", staff);
            addDateTimeFormatPatterns(uiModel);
            return "staffs/update";
        }
        uiModel.asMap().clear();
        staff.merge();
        return "redirect:/staffs/" + encodeUrlPathSegment(staff.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("staff", Staff.findStaff(id));
        addDateTimeFormatPatterns(uiModel);
        return "staffs/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        Staff.findStaff(id).remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/staffs";
    }

	@ModelAttribute("addresseses")
    public Collection<Addresses> populateAddresseses() {
        return Addresses.findAllAddresseses();
    }

	@ModelAttribute("communicationses")
    public Collection<Communications> populateCommunicationses() {
        return Communications.findAllCommunicationses();
    }

	@ModelAttribute("shops")
    public Collection<Shop> populateShops() {
        return Shop.findAllShops();
    }

	@ModelAttribute("staffs")
    public Collection<Staff> populateStaffs() {
        return Staff.findAllStaffs();
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("staff_birthday_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
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

	@RequestMapping(params = { "find=ById", "form" }, method = RequestMethod.GET)
    public String findStaffsByIdForm(Model uiModel) {
        return "staffs/findStaffsById";
    }

	@RequestMapping(params = "find=ById", method = RequestMethod.GET)
    public String findStaffsById(@RequestParam("id") Long id, Model uiModel) {
        uiModel.addAttribute("staffs", Staff.findStaffsById(id).getResultList());
        return "staffs/list";
    }

	@RequestMapping(params = { "find=ByShop", "form" }, method = RequestMethod.GET)
    public String findStaffsByShopForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        return "staffs/findStaffsByShop";
    }

	@RequestMapping(params = "find=ByShop", method = RequestMethod.GET)
    public String findStaffsByShop(@RequestParam("shop") Shop shop, Model uiModel) {
        uiModel.addAttribute("staffs", Staff.findStaffsByShop(shop).getResultList());
        return "staffs/list";
    }

	@RequestMapping(params = { "find=ByShopAndFirstNameEqualsAndLastNameEquals", "form" }, method = RequestMethod.GET)
    public String findStaffsByShopAndFirstNameEqualsAndLastNameEqualsForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        return "staffs/findStaffsByShopAndFirstNameEqualsAndLastNameEquals";
    }

	@RequestMapping(params = "find=ByShopAndFirstNameEqualsAndLastNameEquals", method = RequestMethod.GET)
    public String findStaffsByShopAndFirstNameEqualsAndLastNameEquals(@RequestParam("shop") Shop shop, @RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName, Model uiModel) {
        uiModel.addAttribute("staffs", Staff.findStaffsByShopAndFirstNameEqualsAndLastNameEquals(shop, firstName, lastName).getResultList());
        return "staffs/list";
    }

	@RequestMapping(params = { "find=ByUsername", "form" }, method = RequestMethod.GET)
    public String findStaffsByUsernameForm(Model uiModel) {
        return "staffs/findStaffsByUsername";
    }

	@RequestMapping(params = "find=ByUsername", method = RequestMethod.GET)
    public String findStaffsByUsername(@RequestParam("username") String username, Model uiModel) {
        uiModel.addAttribute("staffs", Staff.findStaffsByUsername(username).getResultList());
        return "staffs/list";
    }

	@RequestMapping(params = { "find=ByUsernameEquals", "form" }, method = RequestMethod.GET)
    public String findStaffsByUsernameEqualsForm(Model uiModel) {
        return "staffs/findStaffsByUsernameEquals";
    }

	@RequestMapping(params = "find=ByUsernameEquals", method = RequestMethod.GET)
    public String findStaffsByUsernameEquals(@RequestParam("username") String username, Model uiModel) {
        uiModel.addAttribute("staffs", Staff.findStaffsByUsernameEquals(username).getResultList());
        return "staffs/list";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> showJson(@PathVariable("id") Long id) {
        Staff staff = Staff.findStaff(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        if (staff == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(staff.toJson(), headers, HttpStatus.OK);
    }

	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Staff.toJsonArray(Staff.findAllStaffs()), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json) {
        Staff.fromJsonToStaff(json).persist();
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJsonArray(@RequestBody String json) {
        for (Staff staff: Staff.fromJsonArrayToStaffs(json)) {
            staff.persist();
        }
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJson(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (Staff.fromJsonToStaff(json).merge() == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJsonArray(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        for (Staff staff: Staff.fromJsonArrayToStaffs(json)) {
            if (staff.merge() == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Long id) {
        Staff staff = Staff.findStaff(id);
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (staff == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        staff.remove();
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ById", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindStaffsById(@RequestParam("id") Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Staff.toJsonArray(Staff.findStaffsById(id).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShop", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindStaffsByShop(@RequestParam("shop") Shop shop) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Staff.toJsonArray(Staff.findStaffsByShop(shop).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndFirstNameEqualsAndLastNameEquals", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindStaffsByShopAndFirstNameEqualsAndLastNameEquals(@RequestParam("shop") Shop shop, @RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Staff.toJsonArray(Staff.findStaffsByShopAndFirstNameEqualsAndLastNameEquals(shop, firstName, lastName).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByUsername", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindStaffsByUsername(@RequestParam("username") String username) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Staff.toJsonArray(Staff.findStaffsByUsername(username).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByUsernameEquals", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindStaffsByUsernameEquals(@RequestParam("username") String username) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Staff.toJsonArray(Staff.findStaffsByUsernameEquals(username).getResultList()), headers, HttpStatus.OK);
    }
}
