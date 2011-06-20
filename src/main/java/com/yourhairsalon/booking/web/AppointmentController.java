package com.yourhairsalon.booking.web;

import com.yourhairsalon.booking.domain.Appointment;
import com.yourhairsalon.booking.domain.BaseService;
import com.yourhairsalon.booking.domain.Clients;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.Staff;
import com.yourhairsalon.booking.reference.ScheduleStatus;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
//import org.joda.time.format.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat;
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

@RooWebScaffold(path = "appointments", formBackingObject = Appointment.class)
@RequestMapping("/appointments")
@Controller
public class AppointmentController {



	@RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> showJson(@PathVariable("id") Long id) {
        Appointment appointment = Appointment.findAppointment(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        if (appointment == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(appointment.toJson(), headers, HttpStatus.OK);
    }

	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAllAppointments()), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json) {
        Appointment.fromJsonToAppointment(json).persist();
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJsonArray(@RequestBody String json) {
        for (Appointment appointment: Appointment.fromJsonArrayToAppointments(json)) {
            appointment.persist();
        }
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJson(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (Appointment.fromJsonToAppointment(json).merge() == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJsonArray(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        for (Appointment appointment: Appointment.fromJsonArrayToAppointments(json)) {
            if (appointment.merge() == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Long id) {
        Appointment appointment = Appointment.findAppointment(id);
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (appointment == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        appointment.remove();
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByAppointmentDateBetween", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByAppointmentDateBetween(@RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByAppointmentDateBetween(minAppointmentDate, maxAppointmentDate).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByBeginDateTimeGreaterThanAndEndDateTimeLessThan", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByBeginDateTimeGreaterThanAndEndDateTimeLessThan(@RequestParam("beginDateTime") @DateTimeFormat(style = "MS") Calendar beginDateTime, @RequestParam("endDateTime") @DateTimeFormat(style = "MS") Calendar endDateTime) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByBeginDateTimeGreaterThanAndEndDateTimeLessThan(beginDateTime, endDateTime).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndAppointmentDateBetween", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndAppointmentDateBetween(@RequestParam("shop") Shop shop, @RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndAppointmentDateBetween(shop, minAppointmentDate, maxAppointmentDate).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndAppointmentDateEquals", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndAppointmentDateEquals(@RequestParam("shop") Shop shop, @RequestParam("appointmentDate") @DateTimeFormat(style = "S-") Date appointmentDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndAppointmentDateEquals(shop, appointmentDate).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanAndEndDateTimeLessThan", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanAndEndDateTimeLessThan(@RequestParam("shop") Shop shop, @RequestParam("appointmentDate") @DateTimeFormat(style = "S-") Date appointmentDate, @RequestParam("beginDateTime") @DateTimeFormat(style = "MS") Calendar beginDateTime, @RequestParam("endDateTime") @DateTimeFormat(style = "MS") Calendar endDateTime) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanAndEndDateTimeLessThan(shop, appointmentDate, beginDateTime, endDateTime).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanEqualsAndEndDateTimeLessThanEquals", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanEqualsAndEndDateTimeLessThanEquals(@RequestParam("shop") Shop shop, @RequestParam("appointmentDate") @DateTimeFormat(style = "S-") Date appointmentDate, @RequestParam("beginDateTime") @DateTimeFormat(style = "MS") Calendar beginDateTime, @RequestParam("endDateTime") @DateTimeFormat(style = "MS") Calendar endDateTime) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanEqualsAndEndDateTimeLessThanEquals(shop, appointmentDate, beginDateTime, endDateTime).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndAppointmentDateGreaterThanAndRecur_parentEquals", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndAppointmentDateGreaterThanAndRecur_parentEquals(@RequestParam("shop") Shop shop, @RequestParam("appointmentDate") @DateTimeFormat(style = "S-") Date appointmentDate, @RequestParam("recur_parent") Long recur_parent) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndAppointmentDateGreaterThanAndRecur_parentEquals(shop, appointmentDate, recur_parent).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndBeginDateTimeGreaterThanAndEndDateTimeLessThan", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndBeginDateTimeGreaterThanAndEndDateTimeLessThan(@RequestParam("shop") Shop shop, @RequestParam("beginDateTime") @DateTimeFormat(style = "MS") Calendar beginDateTime, @RequestParam("endDateTime") @DateTimeFormat(style = "MS") Calendar endDateTime) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndBeginDateTimeGreaterThanAndEndDateTimeLessThan(shop, beginDateTime, endDateTime).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndCancelledNot", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndCancelledNot(@RequestParam("shop") Shop shop, @RequestParam(value = "cancelled", required = false) Boolean cancelled) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndCancelledNot(shop, cancelled == null ? new Boolean(false) : cancelled).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndCancelledNotAndAppointmentDateBetween", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndCancelledNotAndAppointmentDateBetween(@RequestParam("shop") Shop shop, @RequestParam(value = "cancelled", required = false) Boolean cancelled, @RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndCancelledNotAndAppointmentDateBetween(shop, cancelled == null ? new Boolean(false) : cancelled, minAppointmentDate, maxAppointmentDate).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndClient", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndClient(@RequestParam("shop") Shop shop, @RequestParam("client") Clients client) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndClient(shop, client).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndStaff", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndStaff(@RequestParam("shop") Shop shop, @RequestParam("staff") Staff staff) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndStaff(shop, staff).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndStaffEquals", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndStaffEquals(@RequestParam("shop") Shop shop, @RequestParam("staff") Staff staff) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndStaffEquals(shop, staff).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndStatus", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndStatus(@RequestParam("shop") Shop shop, @RequestParam("status") ScheduleStatus status) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndStatus(shop, status).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndStatusAndAppointmentDateBetween", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndStatusAndAppointmentDateBetween(@RequestParam("shop") Shop shop, @RequestParam("status") ScheduleStatus status, @RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndStatusAndAppointmentDateBetween(shop, status, minAppointmentDate, maxAppointmentDate).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndStatusAndAppointmentDateGreaterThanEquals", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndStatusAndAppointmentDateGreaterThanEquals(@RequestParam("shop") Shop shop, @RequestParam("status") ScheduleStatus status, @RequestParam("appointmentDate") @DateTimeFormat(style = "S-") Date appointmentDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndStatusAndAppointmentDateGreaterThanEquals(shop, status, appointmentDate).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndStatusAndClientIsNotNullAndAppointmentDateBetween", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndStatusAndClientIsNotNullAndAppointmentDateBetween(@RequestParam("shop") Shop shop, @RequestParam("status") ScheduleStatus status, @RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndStatusAndClientIsNotNullAndAppointmentDateBetween(shop, status, minAppointmentDate, maxAppointmentDate).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndStatusAndClientIsNotNullAndAppointmentDateBetweenAndStaffEquals", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndStatusAndClientIsNotNullAndAppointmentDateBetweenAndStaffEquals(@RequestParam("shop") Shop shop, @RequestParam("status") ScheduleStatus status, @RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate, @RequestParam("staff") Staff staff) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndStatusAndClientIsNotNullAndAppointmentDateBetweenAndStaffEquals(shop, status, minAppointmentDate, maxAppointmentDate, staff).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndStatusAndClientNotAndAppointmentDateBetween", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndStatusAndClientNotAndAppointmentDateBetween(@RequestParam("shop") Shop shop, @RequestParam("status") ScheduleStatus status, @RequestParam("client") Clients client, @RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndStatusAndClientNotAndAppointmentDateBetween(shop, status, client, minAppointmentDate, maxAppointmentDate).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndStatusNotAndAppointmentDateBetween", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndStatusNotAndAppointmentDateBetween(@RequestParam("shop") Shop shop, @RequestParam("status") ScheduleStatus status, @RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndStatusNotAndAppointmentDateBetween(shop, status, minAppointmentDate, maxAppointmentDate).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndStatusNotAndAppointmentDateBetweenAndStaffEquals", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndStatusNotAndAppointmentDateBetweenAndStaffEquals(@RequestParam("shop") Shop shop, @RequestParam("status") ScheduleStatus status, @RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate, @RequestParam("staff") Staff staff) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndStatusNotAndAppointmentDateBetweenAndStaffEquals(shop, status, minAppointmentDate, maxAppointmentDate, staff).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndStatusNotAndCancelledNotAndAppointmentDateBetween", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindAppointmentsByShopAndStatusNotAndCancelledNotAndAppointmentDateBetween(@RequestParam("shop") Shop shop, @RequestParam("status") ScheduleStatus status, @RequestParam(value = "cancelled", required = false) Boolean cancelled, @RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Appointment.toJsonArray(Appointment.findAppointmentsByShopAndStatusNotAndCancelledNotAndAppointmentDateBetween(shop, status, cancelled == null ? new Boolean(false) : cancelled, minAppointmentDate, maxAppointmentDate).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = { "find=ByAppointmentDateBetween", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByAppointmentDateBetweenForm(Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        return "appointments/findAppointmentsByAppointmentDateBetween";
    }

	@RequestMapping(params = "find=ByAppointmentDateBetween", method = RequestMethod.GET)
    public String findAppointmentsByAppointmentDateBetween(@RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByAppointmentDateBetween(minAppointmentDate, maxAppointmentDate).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByBeginDateTimeGreaterThanAndEndDateTimeLessThan", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByBeginDateTimeGreaterThanAndEndDateTimeLessThanForm(Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        return "appointments/findAppointmentsByBeginDateTimeGreaterThanAndEndDateTimeLessThan";
    }

	@RequestMapping(params = "find=ByBeginDateTimeGreaterThanAndEndDateTimeLessThan", method = RequestMethod.GET)
    public String findAppointmentsByBeginDateTimeGreaterThanAndEndDateTimeLessThan(@RequestParam("beginDateTime") @DateTimeFormat(style = "MS") Calendar beginDateTime, @RequestParam("endDateTime") @DateTimeFormat(style = "MS") Calendar endDateTime, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByBeginDateTimeGreaterThanAndEndDateTimeLessThan(beginDateTime, endDateTime).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndAppointmentDateBetween", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndAppointmentDateBetweenForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/findAppointmentsByShopAndAppointmentDateBetween";
    }

	@RequestMapping(params = "find=ByShopAndAppointmentDateBetween", method = RequestMethod.GET)
    public String findAppointmentsByShopAndAppointmentDateBetween(@RequestParam("shop") Shop shop, @RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndAppointmentDateBetween(shop, minAppointmentDate, maxAppointmentDate).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndAppointmentDateEquals", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndAppointmentDateEqualsForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/findAppointmentsByShopAndAppointmentDateEquals";
    }

	@RequestMapping(params = "find=ByShopAndAppointmentDateEquals", method = RequestMethod.GET)
    public String findAppointmentsByShopAndAppointmentDateEquals(@RequestParam("shop") Shop shop, @RequestParam("appointmentDate") @DateTimeFormat(style = "S-") Date appointmentDate, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndAppointmentDateEquals(shop, appointmentDate).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanAndEndDateTimeLessThan", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanAndEndDateTimeLessThanForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/findAppointmentsByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanAndEndDateTimeLessThan";
    }

	@RequestMapping(params = "find=ByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanAndEndDateTimeLessThan", method = RequestMethod.GET)
    public String findAppointmentsByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanAndEndDateTimeLessThan(@RequestParam("shop") Shop shop, @RequestParam("appointmentDate") @DateTimeFormat(style = "S-") Date appointmentDate, @RequestParam("beginDateTime") @DateTimeFormat(style = "MS") Calendar beginDateTime, @RequestParam("endDateTime") @DateTimeFormat(style = "MS") Calendar endDateTime, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanAndEndDateTimeLessThan(shop, appointmentDate, beginDateTime, endDateTime).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanEqualsAndEndDateTimeLessThanEquals", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanEqualsAndEndDateTimeLessThanEqualsForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/findAppointmentsByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanEqualsAndEndDateTimeLessThanEquals";
    }

	@RequestMapping(params = "find=ByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanEqualsAndEndDateTimeLessThanEquals", method = RequestMethod.GET)
    public String findAppointmentsByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanEqualsAndEndDateTimeLessThanEquals(@RequestParam("shop") Shop shop, @RequestParam("appointmentDate") @DateTimeFormat(style = "S-") Date appointmentDate, @RequestParam("beginDateTime") @DateTimeFormat(style = "MS") Calendar beginDateTime, @RequestParam("endDateTime") @DateTimeFormat(style = "MS") Calendar endDateTime, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanEqualsAndEndDateTimeLessThanEquals(shop, appointmentDate, beginDateTime, endDateTime).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndAppointmentDateGreaterThanAndRecur_parentEquals", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndAppointmentDateGreaterThanAndRecur_parentEqualsForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/findAppointmentsByShopAndAppointmentDateGreaterThanAndRecur_parentEquals";
    }

	@RequestMapping(params = "find=ByShopAndAppointmentDateGreaterThanAndRecur_parentEquals", method = RequestMethod.GET)
    public String findAppointmentsByShopAndAppointmentDateGreaterThanAndRecur_parentEquals(@RequestParam("shop") Shop shop, @RequestParam("appointmentDate") @DateTimeFormat(style = "S-") Date appointmentDate, @RequestParam("recur_parent") Long recur_parent, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndAppointmentDateGreaterThanAndRecur_parentEquals(shop, appointmentDate, recur_parent).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndBeginDateTimeGreaterThanAndEndDateTimeLessThan", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndBeginDateTimeGreaterThanAndEndDateTimeLessThanForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/findAppointmentsByShopAndBeginDateTimeGreaterThanAndEndDateTimeLessThan";
    }

	@RequestMapping(params = "find=ByShopAndBeginDateTimeGreaterThanAndEndDateTimeLessThan", method = RequestMethod.GET)
    public String findAppointmentsByShopAndBeginDateTimeGreaterThanAndEndDateTimeLessThan(@RequestParam("shop") Shop shop, @RequestParam("beginDateTime") @DateTimeFormat(style = "MS") Calendar beginDateTime, @RequestParam("endDateTime") @DateTimeFormat(style = "MS") Calendar endDateTime, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndBeginDateTimeGreaterThanAndEndDateTimeLessThan(shop, beginDateTime, endDateTime).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndCancelledNot", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndCancelledNotForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        return "appointments/findAppointmentsByShopAndCancelledNot";
    }

	@RequestMapping(params = "find=ByShopAndCancelledNot", method = RequestMethod.GET)
    public String findAppointmentsByShopAndCancelledNot(@RequestParam("shop") Shop shop, @RequestParam(value = "cancelled", required = false) Boolean cancelled, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndCancelledNot(shop, cancelled == null ? new Boolean(false) : cancelled).getResultList());
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndCancelledNotAndAppointmentDateBetween", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndCancelledNotAndAppointmentDateBetweenForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/findAppointmentsByShopAndCancelledNotAndAppointmentDateBetween";
    }

	@RequestMapping(params = "find=ByShopAndCancelledNotAndAppointmentDateBetween", method = RequestMethod.GET)
    public String findAppointmentsByShopAndCancelledNotAndAppointmentDateBetween(@RequestParam("shop") Shop shop, @RequestParam(value = "cancelled", required = false) Boolean cancelled, @RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndCancelledNotAndAppointmentDateBetween(shop, cancelled == null ? new Boolean(false) : cancelled, minAppointmentDate, maxAppointmentDate).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndClient", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndClientForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        uiModel.addAttribute("clientses", Clients.findAllClientses());
        return "appointments/findAppointmentsByShopAndClient";
    }

	@RequestMapping(params = "find=ByShopAndClient", method = RequestMethod.GET)
    public String findAppointmentsByShopAndClient(@RequestParam("shop") Shop shop, @RequestParam("client") Clients client, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndClient(shop, client).getResultList());
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndStaff", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndStaffForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        uiModel.addAttribute("staffs", Staff.findAllStaffs());
        return "appointments/findAppointmentsByShopAndStaff";
    }

	@RequestMapping(params = "find=ByShopAndStaff", method = RequestMethod.GET)
    public String findAppointmentsByShopAndStaff(@RequestParam("shop") Shop shop, @RequestParam("staff") Staff staff, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndStaff(shop, staff).getResultList());
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndStaffEquals", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndStaffEqualsForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        uiModel.addAttribute("staffs", Staff.findAllStaffs());
        return "appointments/findAppointmentsByShopAndStaffEquals";
    }

	@RequestMapping(params = "find=ByShopAndStaffEquals", method = RequestMethod.GET)
    public String findAppointmentsByShopAndStaffEquals(@RequestParam("shop") Shop shop, @RequestParam("staff") Staff staff, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndStaffEquals(shop, staff).getResultList());
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndStatus", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndStatusForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        uiModel.addAttribute("schedulestatuses", java.util.Arrays.asList(ScheduleStatus.class.getEnumConstants()));
        return "appointments/findAppointmentsByShopAndStatus";
    }

	@RequestMapping(params = "find=ByShopAndStatus", method = RequestMethod.GET)
    public String findAppointmentsByShopAndStatus(@RequestParam("shop") Shop shop, @RequestParam("status") ScheduleStatus status, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndStatus(shop, status).getResultList());
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndStatusAndAppointmentDateBetween", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndStatusAndAppointmentDateBetweenForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        uiModel.addAttribute("schedulestatuses", java.util.Arrays.asList(ScheduleStatus.class.getEnumConstants()));
        addDateTimeFormatPatterns(uiModel);
        return "appointments/findAppointmentsByShopAndStatusAndAppointmentDateBetween";
    }

	@RequestMapping(params = "find=ByShopAndStatusAndAppointmentDateBetween", method = RequestMethod.GET)
    public String findAppointmentsByShopAndStatusAndAppointmentDateBetween(@RequestParam("shop") Shop shop, @RequestParam("status") ScheduleStatus status, @RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndStatusAndAppointmentDateBetween(shop, status, minAppointmentDate, maxAppointmentDate).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndStatusAndAppointmentDateGreaterThanEquals", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndStatusAndAppointmentDateGreaterThanEqualsForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        uiModel.addAttribute("schedulestatuses", java.util.Arrays.asList(ScheduleStatus.class.getEnumConstants()));
        addDateTimeFormatPatterns(uiModel);
        return "appointments/findAppointmentsByShopAndStatusAndAppointmentDateGreaterThanEquals";
    }

	@RequestMapping(params = "find=ByShopAndStatusAndAppointmentDateGreaterThanEquals", method = RequestMethod.GET)
    public String findAppointmentsByShopAndStatusAndAppointmentDateGreaterThanEquals(@RequestParam("shop") Shop shop, @RequestParam("status") ScheduleStatus status, @RequestParam("appointmentDate") @DateTimeFormat(style = "S-") Date appointmentDate, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndStatusAndAppointmentDateGreaterThanEquals(shop, status, appointmentDate).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndStatusAndClientIsNotNullAndAppointmentDateBetween", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndStatusAndClientIsNotNullAndAppointmentDateBetweenForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        uiModel.addAttribute("schedulestatuses", java.util.Arrays.asList(ScheduleStatus.class.getEnumConstants()));
        addDateTimeFormatPatterns(uiModel);
        return "appointments/findAppointmentsByShopAndStatusAndClientIsNotNullAndAppointmentDateBetween";
    }

	@RequestMapping(params = "find=ByShopAndStatusAndClientIsNotNullAndAppointmentDateBetween", method = RequestMethod.GET)
    public String findAppointmentsByShopAndStatusAndClientIsNotNullAndAppointmentDateBetween(@RequestParam("shop") Shop shop, @RequestParam("status") ScheduleStatus status, @RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndStatusAndClientIsNotNullAndAppointmentDateBetween(shop, status, minAppointmentDate, maxAppointmentDate).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndStatusAndClientIsNotNullAndAppointmentDateBetweenAndStaffEquals", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndStatusAndClientIsNotNullAndAppointmentDateBetweenAndStaffEqualsForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        uiModel.addAttribute("schedulestatuses", java.util.Arrays.asList(ScheduleStatus.class.getEnumConstants()));
        uiModel.addAttribute("staffs", Staff.findAllStaffs());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/findAppointmentsByShopAndStatusAndClientIsNotNullAndAppointmentDateBetweenAndStaffEquals";
    }

	@RequestMapping(params = "find=ByShopAndStatusAndClientIsNotNullAndAppointmentDateBetweenAndStaffEquals", method = RequestMethod.GET)
    public String findAppointmentsByShopAndStatusAndClientIsNotNullAndAppointmentDateBetweenAndStaffEquals(@RequestParam("shop") Shop shop, @RequestParam("status") ScheduleStatus status, @RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate, @RequestParam("staff") Staff staff, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndStatusAndClientIsNotNullAndAppointmentDateBetweenAndStaffEquals(shop, status, minAppointmentDate, maxAppointmentDate, staff).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndStatusAndClientNotAndAppointmentDateBetween", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndStatusAndClientNotAndAppointmentDateBetweenForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        uiModel.addAttribute("schedulestatuses", java.util.Arrays.asList(ScheduleStatus.class.getEnumConstants()));
        uiModel.addAttribute("clientses", Clients.findAllClientses());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/findAppointmentsByShopAndStatusAndClientNotAndAppointmentDateBetween";
    }

	@RequestMapping(params = "find=ByShopAndStatusAndClientNotAndAppointmentDateBetween", method = RequestMethod.GET)
    public String findAppointmentsByShopAndStatusAndClientNotAndAppointmentDateBetween(@RequestParam("shop") Shop shop, @RequestParam("status") ScheduleStatus status, @RequestParam("client") Clients client, @RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndStatusAndClientNotAndAppointmentDateBetween(shop, status, client, minAppointmentDate, maxAppointmentDate).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndStatusNotAndAppointmentDateBetween", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndStatusNotAndAppointmentDateBetweenForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        uiModel.addAttribute("schedulestatuses", java.util.Arrays.asList(ScheduleStatus.class.getEnumConstants()));
        addDateTimeFormatPatterns(uiModel);
        return "appointments/findAppointmentsByShopAndStatusNotAndAppointmentDateBetween";
    }

	@RequestMapping(params = "find=ByShopAndStatusNotAndAppointmentDateBetween", method = RequestMethod.GET)
    public String findAppointmentsByShopAndStatusNotAndAppointmentDateBetween(@RequestParam("shop") Shop shop, @RequestParam("status") ScheduleStatus status, @RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndStatusNotAndAppointmentDateBetween(shop, status, minAppointmentDate, maxAppointmentDate).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndStatusNotAndAppointmentDateBetweenAndStaffEquals", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndStatusNotAndAppointmentDateBetweenAndStaffEqualsForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        uiModel.addAttribute("schedulestatuses", java.util.Arrays.asList(ScheduleStatus.class.getEnumConstants()));
        uiModel.addAttribute("staffs", Staff.findAllStaffs());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/findAppointmentsByShopAndStatusNotAndAppointmentDateBetweenAndStaffEquals";
    }

	@RequestMapping(params = "find=ByShopAndStatusNotAndAppointmentDateBetweenAndStaffEquals", method = RequestMethod.GET)
    public String findAppointmentsByShopAndStatusNotAndAppointmentDateBetweenAndStaffEquals(@RequestParam("shop") Shop shop, @RequestParam("status") ScheduleStatus status, @RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate, @RequestParam("staff") Staff staff, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndStatusNotAndAppointmentDateBetweenAndStaffEquals(shop, status, minAppointmentDate, maxAppointmentDate, staff).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/list";
    }

	@RequestMapping(params = { "find=ByShopAndStatusNotAndCancelledNotAndAppointmentDateBetween", "form" }, method = RequestMethod.GET)
    public String findAppointmentsByShopAndStatusNotAndCancelledNotAndAppointmentDateBetweenForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        uiModel.addAttribute("schedulestatuses", java.util.Arrays.asList(ScheduleStatus.class.getEnumConstants()));
        addDateTimeFormatPatterns(uiModel);
        return "appointments/findAppointmentsByShopAndStatusNotAndCancelledNotAndAppointmentDateBetween";
    }

	@RequestMapping(params = "find=ByShopAndStatusNotAndCancelledNotAndAppointmentDateBetween", method = RequestMethod.GET)
    public String findAppointmentsByShopAndStatusNotAndCancelledNotAndAppointmentDateBetween(@RequestParam("shop") Shop shop, @RequestParam("status") ScheduleStatus status, @RequestParam(value = "cancelled", required = false) Boolean cancelled, @RequestParam("minAppointmentDate") @DateTimeFormat(style = "S-") Date minAppointmentDate, @RequestParam("maxAppointmentDate") @DateTimeFormat(style = "S-") Date maxAppointmentDate, Model uiModel) {
        uiModel.addAttribute("appointments", Appointment.findAppointmentsByShopAndStatusNotAndCancelledNotAndAppointmentDateBetween(shop, status, cancelled == null ? new Boolean(false) : cancelled, minAppointmentDate, maxAppointmentDate).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "appointments/list";
    }

	@RequestMapping(method = RequestMethod.POST)
    public String create(@Valid Appointment appointment, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("appointment", appointment);
            addDateTimeFormatPatterns(uiModel);
            return "appointments/create";
        }
        uiModel.asMap().clear();
        appointment.persist();
        return "redirect:/appointments/" + encodeUrlPathSegment(appointment.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model uiModel) {
        uiModel.addAttribute("appointment", new Appointment());
        addDateTimeFormatPatterns(uiModel);
        List dependencies = new ArrayList();
        if (Staff.countStaffs() == 0) {
            dependencies.add(new String[]{"staff", "staffs"});
        }
        if (Shop.countShops() == 0) {
            dependencies.add(new String[]{"shop", "shops"});
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "appointments/create";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("appointment", Appointment.findAppointment(id));
        uiModel.addAttribute("itemId", id);
        return "appointments/show";
    }

	@RequestMapping(method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            uiModel.addAttribute("appointments", Appointment.findAppointmentEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) Appointment.countAppointments() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("appointments", Appointment.findAllAppointments());
        }
        addDateTimeFormatPatterns(uiModel);
        return "appointments/list";
    }

	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Appointment appointment, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("appointment", appointment);
            addDateTimeFormatPatterns(uiModel);
            return "appointments/update";
        }
        uiModel.asMap().clear();
        appointment.merge();
        return "redirect:/appointments/" + encodeUrlPathSegment(appointment.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("appointment", Appointment.findAppointment(id));
        addDateTimeFormatPatterns(uiModel);
        return "appointments/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        Appointment.findAppointment(id).remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/appointments";
    }

	@ModelAttribute("appointments")
    public Collection<Appointment> populateAppointments() {
        return Appointment.findAllAppointments();
    }

	@ModelAttribute("baseservices")
    public Collection<BaseService> populateBaseServices() {
        return BaseService.findAllBaseServices();
    }

	@ModelAttribute("clientses")
    public Collection<Clients> populateClientses() {
        return Clients.findAllClientses();
    }

	@ModelAttribute("shops")
    public Collection<Shop> populateShops() {
        return Shop.findAllShops();
    }

	@ModelAttribute("staffs")
    public Collection<Staff> populateStaffs() {
        return Staff.findAllStaffs();
    }

	@ModelAttribute("schedulestatuses")
    public Collection<ScheduleStatus> populateScheduleStatuses() {
        return Arrays.asList(ScheduleStatus.class.getEnumConstants());
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("appointment_reoccur_start_date_date_format", org.joda.time.format.DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("appointment_appointmentdate_date_format", org.joda.time.format.DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("appointment_minappointmentdate_date_format", org.joda.time.format.DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("appointment_maxappointmentdate_date_format", org.joda.time.format.DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("appointment_checkintime_date_format", org.joda.time.format.DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("appointment_checkouttime_date_format", org.joda.time.format.DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("appointment_confirmeddate_date_format", org.joda.time.format.DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("appointment_createddate_date_format", org.joda.time.format.DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("appointment_enddatetime_date_format", org.joda.time.format.DateTimeFormat.patternForStyle("MS", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("appointment_begindatetime_date_format", org.joda.time.format.DateTimeFormat.patternForStyle("MS", LocaleContextHolder.getLocale()));
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
