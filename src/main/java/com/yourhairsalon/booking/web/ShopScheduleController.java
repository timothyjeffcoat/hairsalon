package com.yourhairsalon.booking.web;

import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.ShopSchedule;
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

@RooWebScaffold(path = "shopschedules", formBackingObject = ShopSchedule.class)
@RequestMapping("/shopschedules")
@Controller
public class ShopScheduleController {

	@RequestMapping(method = RequestMethod.POST)
    public String create(@Valid ShopSchedule shopSchedule, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("shopSchedule", shopSchedule);
            addDateTimeFormatPatterns(uiModel);
            return "shopschedules/create";
        }
        uiModel.asMap().clear();
        shopSchedule.persist();
        return "redirect:/shopschedules/" + encodeUrlPathSegment(shopSchedule.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model uiModel) {
        uiModel.addAttribute("shopSchedule", new ShopSchedule());
        addDateTimeFormatPatterns(uiModel);
        List dependencies = new ArrayList();
        if (Shop.countShops() == 0) {
            dependencies.add(new String[]{"shop", "shops"});
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "shopschedules/create";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("shopschedule", ShopSchedule.findShopSchedule(id));
        uiModel.addAttribute("itemId", id);
        return "shopschedules/show";
    }

	@RequestMapping(method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            uiModel.addAttribute("shopschedules", ShopSchedule.findShopScheduleEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) ShopSchedule.countShopSchedules() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("shopschedules", ShopSchedule.findAllShopSchedules());
        }
        addDateTimeFormatPatterns(uiModel);
        return "shopschedules/list";
    }

	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid ShopSchedule shopSchedule, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("shopSchedule", shopSchedule);
            addDateTimeFormatPatterns(uiModel);
            return "shopschedules/update";
        }
        uiModel.asMap().clear();
        shopSchedule.merge();
        return "redirect:/shopschedules/" + encodeUrlPathSegment(shopSchedule.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("shopSchedule", ShopSchedule.findShopSchedule(id));
        addDateTimeFormatPatterns(uiModel);
        return "shopschedules/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        ShopSchedule.findShopSchedule(id).remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/shopschedules";
    }

	@ModelAttribute("shops")
    public Collection<Shop> populateShops() {
        return Shop.findAllShops();
    }

	@ModelAttribute("shopschedules")
    public Collection<ShopSchedule> populateShopSchedules() {
        return ShopSchedule.findAllShopSchedules();
    }

	@ModelAttribute("schedulestatuses")
    public Collection<ScheduleStatus> populateScheduleStatuses() {
        return Arrays.asList(ScheduleStatus.class.getEnumConstants());
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("shopSchedule_schedule_date_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("shopSchedule_start_time_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
        uiModel.addAttribute("shopSchedule_end_time_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
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
        ShopSchedule shopschedule = ShopSchedule.findShopSchedule(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        if (shopschedule == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(shopschedule.toJson(), headers, HttpStatus.OK);
    }

	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(ShopSchedule.toJsonArray(ShopSchedule.findAllShopSchedules()), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json) {
        ShopSchedule.fromJsonToShopSchedule(json).persist();
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJsonArray(@RequestBody String json) {
        for (ShopSchedule shopSchedule: ShopSchedule.fromJsonArrayToShopSchedules(json)) {
            shopSchedule.persist();
        }
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJson(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (ShopSchedule.fromJsonToShopSchedule(json).merge() == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJsonArray(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        for (ShopSchedule shopSchedule: ShopSchedule.fromJsonArrayToShopSchedules(json)) {
            if (shopSchedule.merge() == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Long id) {
        ShopSchedule shopschedule = ShopSchedule.findShopSchedule(id);
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (shopschedule == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        shopschedule.remove();
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ById", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindShopSchedulesById(@RequestParam("id") Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(ShopSchedule.toJsonArray(ShopSchedule.findShopSchedulesById(id).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShop", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindShopSchedulesByShop(@RequestParam("shop") Shop shop) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(ShopSchedule.toJsonArray(ShopSchedule.findShopSchedulesByShop(shop).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = { "find=ById", "form" }, method = RequestMethod.GET)
    public String findShopSchedulesByIdForm(Model uiModel) {
        return "shopschedules/findShopSchedulesById";
    }

	@RequestMapping(params = "find=ById", method = RequestMethod.GET)
    public String findShopSchedulesById(@RequestParam("id") Long id, Model uiModel) {
        uiModel.addAttribute("shopschedules", ShopSchedule.findShopSchedulesById(id).getResultList());
        return "shopschedules/list";
    }

	@RequestMapping(params = { "find=ByShop", "form" }, method = RequestMethod.GET)
    public String findShopSchedulesByShopForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        return "shopschedules/findShopSchedulesByShop";
    }

	@RequestMapping(params = "find=ByShop", method = RequestMethod.GET)
    public String findShopSchedulesByShop(@RequestParam("shop") Shop shop, Model uiModel) {
        uiModel.addAttribute("shopschedules", ShopSchedule.findShopSchedulesByShop(shop).getResultList());
        return "shopschedules/list";
    }
}
