package com.yourhairsalon.booking.web;

import com.yourhairsalon.booking.domain.ClientGroup;
import com.yourhairsalon.booking.domain.Clients;
import com.yourhairsalon.booking.domain.Shop;
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

@RooWebScaffold(path = "clientgroups", formBackingObject = ClientGroup.class)
@RequestMapping("/clientgroups")
@Controller
public class ClientGroupController {

	@RequestMapping(params = { "find=ByShop", "form" }, method = RequestMethod.GET)
    public String findClientGroupsByShopForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        return "clientgroups/findClientGroupsByShop";
    }

	@RequestMapping(params = "find=ByShop", method = RequestMethod.GET)
    public String findClientGroupsByShop(@RequestParam("shop") Shop shop, Model uiModel) {
        uiModel.addAttribute("clientgroups", ClientGroup.findClientGroupsByShop(shop).getResultList());
        return "clientgroups/list";
    }

	@RequestMapping(method = RequestMethod.POST)
    public String create(@Valid ClientGroup clientGroup, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("clientGroup", clientGroup);
            addDateTimeFormatPatterns(uiModel);
            return "clientgroups/create";
        }
        uiModel.asMap().clear();
        clientGroup.persist();
        return "redirect:/clientgroups/" + encodeUrlPathSegment(clientGroup.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model uiModel) {
        uiModel.addAttribute("clientGroup", new ClientGroup());
        addDateTimeFormatPatterns(uiModel);
        List dependencies = new ArrayList();
        if (Shop.countShops() == 0) {
            dependencies.add(new String[]{"shop", "shops"});
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "clientgroups/create";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("clientgroup", ClientGroup.findClientGroup(id));
        uiModel.addAttribute("itemId", id);
        return "clientgroups/show";
    }

	@RequestMapping(method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            uiModel.addAttribute("clientgroups", ClientGroup.findClientGroupEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) ClientGroup.countClientGroups() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("clientgroups", ClientGroup.findAllClientGroups());
        }
        addDateTimeFormatPatterns(uiModel);
        return "clientgroups/list";
    }

	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid ClientGroup clientGroup, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("clientGroup", clientGroup);
            addDateTimeFormatPatterns(uiModel);
            return "clientgroups/update";
        }
        uiModel.asMap().clear();
        clientGroup.merge();
        return "redirect:/clientgroups/" + encodeUrlPathSegment(clientGroup.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("clientGroup", ClientGroup.findClientGroup(id));
        addDateTimeFormatPatterns(uiModel);
        return "clientgroups/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        ClientGroup.findClientGroup(id).remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/clientgroups";
    }

	@ModelAttribute("clientgroups")
    public Collection<ClientGroup> populateClientGroups() {
        return ClientGroup.findAllClientGroups();
    }

	@ModelAttribute("clientses")
    public Collection<Clients> populateClientses() {
        return Clients.findAllClientses();
    }

	@ModelAttribute("shops")
    public Collection<Shop> populateShops() {
        return Shop.findAllShops();
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("clientGroup_createddate_date_format", DateTimeFormat.patternForStyle("S-", LocaleContextHolder.getLocale()));
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
        ClientGroup clientgroup = ClientGroup.findClientGroup(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        if (clientgroup == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(clientgroup.toJson(), headers, HttpStatus.OK);
    }

	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(ClientGroup.toJsonArray2(ClientGroup.findAllClientGroups()), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json) {
        ClientGroup.fromJsonToClientGroup(json).persist();
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJsonArray(@RequestBody String json) {
        for (ClientGroup clientGroup: ClientGroup.fromJsonArrayToClientGroups(json)) {
            clientGroup.persist();
        }
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJson(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (ClientGroup.fromJsonToClientGroup(json).merge() == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJsonArray(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        for (ClientGroup clientGroup: ClientGroup.fromJsonArrayToClientGroups(json)) {
            if (clientGroup.merge() == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Long id) {
        ClientGroup clientgroup = ClientGroup.findClientGroup(id);
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (clientgroup == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        clientgroup.remove();
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShop", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindClientGroupsByShop(@RequestParam("shop") Shop shop) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(ClientGroup.toJsonArray2(ClientGroup.findClientGroupsByShop(shop).getResultList()), headers, HttpStatus.OK);
    }
}
