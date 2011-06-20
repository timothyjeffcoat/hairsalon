package com.yourhairsalon.booking.web;

import com.yourhairsalon.booking.domain.Appointment;
import com.yourhairsalon.booking.domain.Clients;
import com.yourhairsalon.booking.domain.Payments;
import com.yourhairsalon.booking.domain.PaymentsService;
import com.yourhairsalon.booking.domain.PaymentsType;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.Staff;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
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

@RooWebScaffold(path = "paymentses", formBackingObject = Payments.class)
@RequestMapping("/paymentses")
@Controller
public class PaymentsController {

	@RequestMapping(params = { "find=ById", "form" }, method = RequestMethod.GET)
    public String findPaymentsesByIdForm(Model uiModel) {
        return "paymentses/findPaymentsesById";
    }

	@RequestMapping(params = "find=ById", method = RequestMethod.GET)
    public String findPaymentsesById(@RequestParam("id") Long id, Model uiModel) {
        uiModel.addAttribute("paymentses", Payments.findPaymentsesById(id).getResultList());
        return "paymentses/list";
    }

	@RequestMapping(params = { "find=ByShopAndDatecreatedBetween", "form" }, method = RequestMethod.GET)
    public String findPaymentsesByShopAndDatecreatedBetweenForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        addDateTimeFormatPatterns(uiModel);
        return "paymentses/findPaymentsesByShopAndDatecreatedBetween";
    }

	@RequestMapping(params = "find=ByShopAndDatecreatedBetween", method = RequestMethod.GET)
    public String findPaymentsesByShopAndDatecreatedBetween(@RequestParam("shop") Shop shop, @RequestParam("minDatecreated") @DateTimeFormat(style = "S-") Date minDatecreated, @RequestParam("maxDatecreated") @DateTimeFormat(style = "S-") Date maxDatecreated, Model uiModel) {
        uiModel.addAttribute("paymentses", Payments.findPaymentsesByShopAndDatecreatedBetween(shop, minDatecreated, maxDatecreated).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "paymentses/list";
    }

	@RequestMapping(params = { "find=ByShopAndDatecreatedBetweenAndStaffEquals", "form" }, method = RequestMethod.GET)
    public String findPaymentsesByShopAndDatecreatedBetweenAndStaffEqualsForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        uiModel.addAttribute("staffs", Staff.findAllStaffs());
        addDateTimeFormatPatterns(uiModel);
        return "paymentses/findPaymentsesByShopAndDatecreatedBetweenAndStaffEquals";
    }

	@RequestMapping(params = "find=ByShopAndDatecreatedBetweenAndStaffEquals", method = RequestMethod.GET)
    public String findPaymentsesByShopAndDatecreatedBetweenAndStaffEquals(@RequestParam("shop") Shop shop, @RequestParam("minDatecreated") @DateTimeFormat(style = "S-") Date minDatecreated, @RequestParam("maxDatecreated") @DateTimeFormat(style = "S-") Date maxDatecreated, @RequestParam("staff") Staff staff, Model uiModel) {
        uiModel.addAttribute("paymentses", Payments.findPaymentsesByShopAndDatecreatedBetweenAndStaffEquals(shop, minDatecreated, maxDatecreated, staff).getResultList());
        addDateTimeFormatPatterns(uiModel);
        return "paymentses/list";
    }

	@RequestMapping(method = RequestMethod.POST)
    public String create(@Valid Payments payments, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("payments", payments);
            addDateTimeFormatPatterns(uiModel);
            return "paymentses/create";
        }
        uiModel.asMap().clear();
        payments.persist();
        return "redirect:/paymentses/" + encodeUrlPathSegment(payments.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model uiModel) {
        uiModel.addAttribute("payments", new Payments());
        addDateTimeFormatPatterns(uiModel);
        List dependencies = new ArrayList();
        if (Clients.countClientses() == 0) {
            dependencies.add(new String[]{"clients", "clientses"});
        }
        if (Staff.countStaffs() == 0) {
            dependencies.add(new String[]{"staff", "staffs"});
        }
        if (Shop.countShops() == 0) {
            dependencies.add(new String[]{"shop", "shops"});
        }
        if (Appointment.countAppointments() == 0) {
            dependencies.add(new String[]{"appointment", "appointments"});
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "paymentses/create";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("payments", Payments.findPayments(id));
        uiModel.addAttribute("itemId", id);
        return "paymentses/show";
    }

	@RequestMapping(method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            uiModel.addAttribute("paymentses", Payments.findPaymentsEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) Payments.countPaymentses() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("paymentses", Payments.findAllPaymentses());
        }
        addDateTimeFormatPatterns(uiModel);
        return "paymentses/list";
    }

	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Payments payments, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("payments", payments);
            addDateTimeFormatPatterns(uiModel);
            return "paymentses/update";
        }
        uiModel.asMap().clear();
        payments.merge();
        return "redirect:/paymentses/" + encodeUrlPathSegment(payments.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("payments", Payments.findPayments(id));
        addDateTimeFormatPatterns(uiModel);
        return "paymentses/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        Payments.findPayments(id).remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/paymentses";
    }

	@ModelAttribute("appointments")
    public Collection<Appointment> populateAppointments() {
        return Appointment.findAllAppointments();
    }

	@ModelAttribute("clientses")
    public Collection<Clients> populateClientses() {
        return Clients.findAllClientses();
    }

	@ModelAttribute("paymentses")
    public Collection<Payments> populatePaymentses() {
        return Payments.findAllPaymentses();
    }

	@ModelAttribute("paymentsservices")
    public Collection<PaymentsService> populatePaymentsServices() {
        return PaymentsService.findAllPaymentsServices();
    }

	@ModelAttribute("paymentstypes")
    public Collection<PaymentsType> populatePaymentsTypes() {
        return PaymentsType.findAllPaymentsTypes();
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
        uiModel.addAttribute("payments_datecreated_date_format", org.joda.time.format.DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("payments_mindatecreated_date_format", org.joda.time.format.DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("payments_maxdatecreated_date_format", org.joda.time.format.DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
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
        Payments payments = Payments.findPayments(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        if (payments == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(payments.toJson(), headers, HttpStatus.OK);
    }

	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Payments.toJsonArray(Payments.findAllPaymentses()), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json) {
        Payments.fromJsonToPayments(json).persist();
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJsonArray(@RequestBody String json) {
        for (Payments payments: Payments.fromJsonArrayToPaymentses(json)) {
            payments.persist();
        }
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJson(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (Payments.fromJsonToPayments(json).merge() == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJsonArray(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        for (Payments payments: Payments.fromJsonArrayToPaymentses(json)) {
            if (payments.merge() == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Long id) {
        Payments payments = Payments.findPayments(id);
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (payments == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        payments.remove();
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ById", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindPaymentsesById(@RequestParam("id") Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Payments.toJsonArray(Payments.findPaymentsesById(id).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndDatecreatedBetween", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindPaymentsesByShopAndDatecreatedBetween(@RequestParam("shop") Shop shop, @RequestParam("minDatecreated") @DateTimeFormat(style = "S-") Date minDatecreated, @RequestParam("maxDatecreated") @DateTimeFormat(style = "S-") Date maxDatecreated) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Payments.toJsonArray(Payments.findPaymentsesByShopAndDatecreatedBetween(shop, minDatecreated, maxDatecreated).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndDatecreatedBetweenAndStaffEquals", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindPaymentsesByShopAndDatecreatedBetweenAndStaffEquals(@RequestParam("shop") Shop shop, @RequestParam("minDatecreated") @DateTimeFormat(style = "S-") Date minDatecreated, @RequestParam("maxDatecreated") @DateTimeFormat(style = "S-") Date maxDatecreated, @RequestParam("staff") Staff staff) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Payments.toJsonArray(Payments.findPaymentsesByShopAndDatecreatedBetweenAndStaffEquals(shop, minDatecreated, maxDatecreated, staff).getResultList()), headers, HttpStatus.OK);
    }
}
