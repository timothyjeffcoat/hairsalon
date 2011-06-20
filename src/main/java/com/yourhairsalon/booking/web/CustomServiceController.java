package com.yourhairsalon.booking.web;

import com.yourhairsalon.booking.domain.Clients;
import com.yourhairsalon.booking.domain.CustomService;
import com.yourhairsalon.booking.domain.Shop;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

@RooWebScaffold(path = "customservices", formBackingObject = CustomService.class)
@RequestMapping("/customservices")
@Controller
public class CustomServiceController {

	@RequestMapping(params = { "find=ByClient", "form" }, method = RequestMethod.GET)
    public String findCustomServicesByClientForm(Model uiModel) {
        uiModel.addAttribute("clientses", Clients.findAllClientses());
        return "customservices/findCustomServicesByClient";
    }

	@RequestMapping(params = "find=ByClient", method = RequestMethod.GET)
    public String findCustomServicesByClient(@RequestParam("client") Clients client, Model uiModel) {
        uiModel.addAttribute("customservices", CustomService.findCustomServicesByClient(client).getResultList());
        return "customservices/list";
    }

	@RequestMapping(params = { "find=ByShop", "form" }, method = RequestMethod.GET)
    public String findCustomServicesByShopForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        return "customservices/findCustomServicesByShop";
    }

	@RequestMapping(params = "find=ByShop", method = RequestMethod.GET)
    public String findCustomServicesByShop(@RequestParam("shop") Shop shop, Model uiModel) {
        uiModel.addAttribute("customservices", CustomService.findCustomServicesByShop(shop).getResultList());
        return "customservices/list";
    }

	@RequestMapping(params = { "find=ByShopAndClient", "form" }, method = RequestMethod.GET)
    public String findCustomServicesByShopAndClientForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        uiModel.addAttribute("clientses", Clients.findAllClientses());
        return "customservices/findCustomServicesByShopAndClient";
    }

	@RequestMapping(params = "find=ByShopAndClient", method = RequestMethod.GET)
    public String findCustomServicesByShopAndClient(@RequestParam("shop") Shop shop, @RequestParam("client") Clients client, Model uiModel) {
        uiModel.addAttribute("customservices", CustomService.findCustomServicesByShopAndClient(shop, client).getResultList());
        return "customservices/list";
    }

	@RequestMapping(params = { "find=ByShopAndClientAndOriginalid", "form" }, method = RequestMethod.GET)
    public String findCustomServicesByShopAndClientAndOriginalidForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        uiModel.addAttribute("clientses", Clients.findAllClientses());
        return "customservices/findCustomServicesByShopAndClientAndOriginalid";
    }

	@RequestMapping(params = "find=ByShopAndClientAndOriginalid", method = RequestMethod.GET)
    public String findCustomServicesByShopAndClientAndOriginalid(@RequestParam("shop") Shop shop, @RequestParam("client") Clients client, @RequestParam("originalid") Long originalid, Model uiModel) {
        uiModel.addAttribute("customservices", CustomService.findCustomServicesByShopAndClientAndOriginalid(shop, client, originalid).getResultList());
        return "customservices/list";
    }

	@RequestMapping(method = RequestMethod.POST)
    public String create(@Valid CustomService customService, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("customService", customService);
            return "customservices/create";
        }
        uiModel.asMap().clear();
        customService.persist();
        return "redirect:/customservices/" + encodeUrlPathSegment(customService.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model uiModel) {
        uiModel.addAttribute("customService", new CustomService());
        List dependencies = new ArrayList();
        if (Shop.countShops() == 0) {
            dependencies.add(new String[]{"shop", "shops"});
        }
        if (Clients.countClientses() == 0) {
            dependencies.add(new String[]{"clients", "clientses"});
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "customservices/create";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("customservice", CustomService.findCustomService(id));
        uiModel.addAttribute("itemId", id);
        return "customservices/show";
    }

	@RequestMapping(method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            uiModel.addAttribute("customservices", CustomService.findCustomServiceEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) CustomService.countCustomServices() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("customservices", CustomService.findAllCustomServices());
        }
        return "customservices/list";
    }

	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid CustomService customService, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("customService", customService);
            return "customservices/update";
        }
        uiModel.asMap().clear();
        customService.merge();
        return "redirect:/customservices/" + encodeUrlPathSegment(customService.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("customService", CustomService.findCustomService(id));
        return "customservices/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        CustomService.findCustomService(id).remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/customservices";
    }

	@ModelAttribute("clientses")
    public Collection<Clients> populateClientses() {
        return Clients.findAllClientses();
    }

	@ModelAttribute("customservices")
    public Collection<CustomService> populateCustomServices() {
        return CustomService.findAllCustomServices();
    }

	@ModelAttribute("shops")
    public Collection<Shop> populateShops() {
        return Shop.findAllShops();
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
        CustomService customservice = CustomService.findCustomService(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        if (customservice == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(customservice.toJson(), headers, HttpStatus.OK);
    }

	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(CustomService.toJsonArray(CustomService.findAllCustomServices()), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json) {
        CustomService.fromJsonToCustomService(json).persist();
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJsonArray(@RequestBody String json) {
        for (CustomService customService: CustomService.fromJsonArrayToCustomServices(json)) {
            customService.persist();
        }
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJson(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (CustomService.fromJsonToCustomService(json).merge() == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJsonArray(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        for (CustomService customService: CustomService.fromJsonArrayToCustomServices(json)) {
            if (customService.merge() == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Long id) {
        CustomService customservice = CustomService.findCustomService(id);
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (customservice == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        customservice.remove();
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByClient", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindCustomServicesByClient(@RequestParam("client") Clients client) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(CustomService.toJsonArray(CustomService.findCustomServicesByClient(client).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShop", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindCustomServicesByShop(@RequestParam("shop") Shop shop) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(CustomService.toJsonArray(CustomService.findCustomServicesByShop(shop).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndClient", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindCustomServicesByShopAndClient(@RequestParam("shop") Shop shop, @RequestParam("client") Clients client) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(CustomService.toJsonArray(CustomService.findCustomServicesByShopAndClient(shop, client).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShopAndClientAndOriginalid", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindCustomServicesByShopAndClientAndOriginalid(@RequestParam("shop") Shop shop, @RequestParam("client") Clients client, @RequestParam("originalid") Long originalid) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(CustomService.toJsonArray(CustomService.findCustomServicesByShopAndClientAndOriginalid(shop, client, originalid).getResultList()), headers, HttpStatus.OK);
    }
}
