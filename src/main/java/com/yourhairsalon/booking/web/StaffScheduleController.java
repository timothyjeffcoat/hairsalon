package com.yourhairsalon.booking.web;

import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.StaffSchedule;
import com.yourhairsalon.booking.reference.ScheduleStatus;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
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

@RooWebScaffold(path = "staffschedules", formBackingObject = StaffSchedule.class)
@RequestMapping("/staffschedules")
@Controller
public class StaffScheduleController {

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> showJson(@PathVariable("id") Long id) {
        StaffSchedule staffschedule = StaffSchedule.findStaffSchedule(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        if (staffschedule == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(staffschedule.toJson(), headers, HttpStatus.OK);
    }

	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(StaffSchedule.toJsonArray(StaffSchedule.findAllStaffSchedules()), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json) {
        StaffSchedule.fromJsonToStaffSchedule(json).persist();
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJsonArray(@RequestBody String json) {
        for (StaffSchedule staffSchedule: StaffSchedule.fromJsonArrayToStaffSchedules(json)) {
            staffSchedule.persist();
        }
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJson(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (StaffSchedule.fromJsonToStaffSchedule(json).merge() == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJsonArray(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        for (StaffSchedule staffSchedule: StaffSchedule.fromJsonArrayToStaffSchedules(json)) {
            if (staffSchedule.merge() == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Long id) {
        StaffSchedule staffschedule = StaffSchedule.findStaffSchedule(id);
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (staffschedule == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        staffschedule.remove();
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ById", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindStaffSchedulesById(@RequestParam("id") Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(StaffSchedule.toJsonArray(StaffSchedule.findStaffSchedulesById(id).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShop", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindStaffSchedulesByShop(@RequestParam("shop") Shop shop) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(StaffSchedule.toJsonArray(StaffSchedule.findStaffSchedulesByShop(shop).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST)
    public String create(@Valid StaffSchedule staffSchedule, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("staffSchedule", staffSchedule);
            addDateTimeFormatPatterns(uiModel);
            return "staffschedules/create";
        }
        uiModel.asMap().clear();
        staffSchedule.persist();
        return "redirect:/staffschedules/" + encodeUrlPathSegment(staffSchedule.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model uiModel) {
        uiModel.addAttribute("staffSchedule", new StaffSchedule());
        addDateTimeFormatPatterns(uiModel);
        List dependencies = new ArrayList();
        if (Shop.countShops() == 0) {
            dependencies.add(new String[]{"shop", "shops"});
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "staffschedules/create";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("staffschedule", StaffSchedule.findStaffSchedule(id));
        uiModel.addAttribute("itemId", id);
        return "staffschedules/show";
    }

	@RequestMapping(method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            uiModel.addAttribute("staffschedules", StaffSchedule.findStaffScheduleEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) StaffSchedule.countStaffSchedules() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("staffschedules", StaffSchedule.findAllStaffSchedules());
        }
        addDateTimeFormatPatterns(uiModel);
        return "staffschedules/list";
    }

	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid StaffSchedule staffSchedule, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("staffSchedule", staffSchedule);
            addDateTimeFormatPatterns(uiModel);
            return "staffschedules/update";
        }
        uiModel.asMap().clear();
        staffSchedule.merge();
        return "redirect:/staffschedules/" + encodeUrlPathSegment(staffSchedule.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("staffSchedule", StaffSchedule.findStaffSchedule(id));
        addDateTimeFormatPatterns(uiModel);
        return "staffschedules/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        StaffSchedule.findStaffSchedule(id).remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/staffschedules";
    }

	@ModelAttribute("shops")
    public Collection<Shop> populateShops() {
        return Shop.findAllShops();
    }

	@ModelAttribute("staffschedules")
    public Collection<StaffSchedule> populateStaffSchedules() {
        return StaffSchedule.findAllStaffSchedules();
    }

	@ModelAttribute("schedulestatuses")
    public Collection<ScheduleStatus> populateScheduleStatuses() {
        return Arrays.asList(ScheduleStatus.class.getEnumConstants());
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("staffSchedule_schedule_date_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("staffSchedule_start_time_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("staffSchedule_end_time_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
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
    public String findStaffSchedulesByIdForm(Model uiModel) {
        return "staffschedules/findStaffSchedulesById";
    }

	@RequestMapping(params = "find=ById", method = RequestMethod.GET)
    public String findStaffSchedulesById(@RequestParam("id") Long id, Model uiModel) {
        uiModel.addAttribute("staffschedules", StaffSchedule.findStaffSchedulesById(id).getResultList());
        return "staffschedules/list";
    }

	@RequestMapping(params = { "find=ByShop", "form" }, method = RequestMethod.GET)
    public String findStaffSchedulesByShopForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        return "staffschedules/findStaffSchedulesByShop";
    }

	@RequestMapping(params = "find=ByShop", method = RequestMethod.GET)
    public String findStaffSchedulesByShop(@RequestParam("shop") Shop shop, Model uiModel) {
        uiModel.addAttribute("staffschedules", StaffSchedule.findStaffSchedulesByShop(shop).getResultList());
        return "staffschedules/list";
    }
}
