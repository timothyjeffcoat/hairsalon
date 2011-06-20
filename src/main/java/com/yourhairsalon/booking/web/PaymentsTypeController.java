package com.yourhairsalon.booking.web;

import com.yourhairsalon.booking.domain.Payments;
import com.yourhairsalon.booking.domain.PaymentsType;
import com.yourhairsalon.booking.reference.PaymentTypes;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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

@RooWebScaffold(path = "paymentstypes", formBackingObject = PaymentsType.class)
@RequestMapping("/paymentstypes")
@Controller
public class PaymentsTypeController {

	@RequestMapping(method = RequestMethod.POST)
    public String create(@Valid PaymentsType paymentsType, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("paymentsType", paymentsType);
            return "paymentstypes/create";
        }
        uiModel.asMap().clear();
        paymentsType.persist();
        return "redirect:/paymentstypes/" + encodeUrlPathSegment(paymentsType.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model uiModel) {
        uiModel.addAttribute("paymentsType", new PaymentsType());
        return "paymentstypes/create";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("paymentstype", PaymentsType.findPaymentsType(id));
        uiModel.addAttribute("itemId", id);
        return "paymentstypes/show";
    }

	@RequestMapping(method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            uiModel.addAttribute("paymentstypes", PaymentsType.findPaymentsTypeEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) PaymentsType.countPaymentsTypes() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("paymentstypes", PaymentsType.findAllPaymentsTypes());
        }
        return "paymentstypes/list";
    }

	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid PaymentsType paymentsType, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("paymentsType", paymentsType);
            return "paymentstypes/update";
        }
        uiModel.asMap().clear();
        paymentsType.merge();
        return "redirect:/paymentstypes/" + encodeUrlPathSegment(paymentsType.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("paymentsType", PaymentsType.findPaymentsType(id));
        return "paymentstypes/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        PaymentsType.findPaymentsType(id).remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/paymentstypes";
    }

	@ModelAttribute("paymentses")
    public Collection<Payments> populatePaymentses() {
        return Payments.findAllPaymentses();
    }

	@ModelAttribute("paymentstypes")
    public Collection<PaymentsType> populatePaymentsTypes() {
        return PaymentsType.findAllPaymentsTypes();
    }

	@ModelAttribute("paymenttypeses")
    public Collection<PaymentTypes> populatePaymentTypeses() {
        return Arrays.asList(PaymentTypes.class.getEnumConstants());
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
        PaymentsType paymentstype = PaymentsType.findPaymentsType(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        if (paymentstype == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(paymentstype.toJson(), headers, HttpStatus.OK);
    }

	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(PaymentsType.toJsonArray(PaymentsType.findAllPaymentsTypes()), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json) {
        PaymentsType.fromJsonToPaymentsType(json).persist();
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJsonArray(@RequestBody String json) {
        for (PaymentsType paymentsType: PaymentsType.fromJsonArrayToPaymentsTypes(json)) {
            paymentsType.persist();
        }
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJson(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (PaymentsType.fromJsonToPaymentsType(json).merge() == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJsonArray(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        for (PaymentsType paymentsType: PaymentsType.fromJsonArrayToPaymentsTypes(json)) {
            if (paymentsType.merge() == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Long id) {
        PaymentsType paymentstype = PaymentsType.findPaymentsType(id);
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (paymentstype == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        paymentstype.remove();
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }
}
