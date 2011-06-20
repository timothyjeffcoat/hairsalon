package com.yourhairsalon.booking.web;

import com.yourhairsalon.booking.domain.AbstractPerson;
import com.yourhairsalon.booking.domain.Communications;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.reference.CommType;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
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

@RooWebScaffold(path = "communicationses", formBackingObject = Communications.class)
@RequestMapping("/communicationses")
@Controller
public class CommunicationsController {

	@RequestMapping(params = { "find=ByCommunication_valueEquals", "form" }, method = RequestMethod.GET)
    public String findCommunicationsesByCommunication_valueEqualsForm(Model uiModel) {
        return "communicationses/findCommunicationsesByCommunication_valueEquals";
    }

	@RequestMapping(params = "find=ByCommunication_valueEquals", method = RequestMethod.GET)
    public String findCommunicationsesByCommunication_valueEquals(@RequestParam("communication_value") String communication_value, Model uiModel) {
        uiModel.addAttribute("communicationses", Communications.findCommunicationsesByCommunication_valueEquals(communication_value).getResultList());
        return "communicationses/list";
    }

	@RequestMapping(params = { "find=ByPerson", "form" }, method = RequestMethod.GET)
    public String findCommunicationsesByPersonForm(Model uiModel) {
        return "communicationses/findCommunicationsesByPerson";
    }

	@RequestMapping(params = "find=ByPerson", method = RequestMethod.GET)
    public String findCommunicationsesByPerson(@RequestParam("person") AbstractPerson person, Model uiModel) {
        uiModel.addAttribute("communicationses", Communications.findCommunicationsesByPerson(person).getResultList());
        return "communicationses/list";
    }

	@RequestMapping(params = { "find=ByShop", "form" }, method = RequestMethod.GET)
    public String findCommunicationsesByShopForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        return "communicationses/findCommunicationsesByShop";
    }

	@RequestMapping(params = "find=ByShop", method = RequestMethod.GET)
    public String findCommunicationsesByShop(@RequestParam("shop") Shop shop, Model uiModel) {
        uiModel.addAttribute("communicationses", Communications.findCommunicationsesByShop(shop).getResultList());
        return "communicationses/list";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> showJson(@PathVariable("id") Long id) {
        Communications communications = Communications.findCommunications(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        if (communications == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(communications.toJson(), headers, HttpStatus.OK);
    }

	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Communications.toJsonArray(Communications.findAllCommunicationses()), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json) {
        Communications.fromJsonToCommunications(json).persist();
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJsonArray(@RequestBody String json) {
        for (Communications communications: Communications.fromJsonArrayToCommunicationses(json)) {
            communications.persist();
        }
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJson(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (Communications.fromJsonToCommunications(json).merge() == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJsonArray(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        for (Communications communications: Communications.fromJsonArrayToCommunicationses(json)) {
            if (communications.merge() == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Long id) {
        Communications communications = Communications.findCommunications(id);
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (communications == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        communications.remove();
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByCommunication_valueEquals", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindCommunicationsesByCommunication_valueEquals(@RequestParam("communication_value") String communication_value) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Communications.toJsonArray(Communications.findCommunicationsesByCommunication_valueEquals(communication_value).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByPerson", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindCommunicationsesByPerson(@RequestParam("person") AbstractPerson person) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Communications.toJsonArray(Communications.findCommunicationsesByPerson(person).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShop", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindCommunicationsesByShop(@RequestParam("shop") Shop shop) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(Communications.toJsonArray(Communications.findCommunicationsesByShop(shop).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST)
    public String create(@Valid Communications communications, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("communications", communications);
            return "communicationses/create";
        }
        uiModel.asMap().clear();
        communications.persist();
        return "redirect:/communicationses/" + encodeUrlPathSegment(communications.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model uiModel) {
        uiModel.addAttribute("communications", new Communications());
        List dependencies = new ArrayList();
        if (Shop.countShops() == 0) {
            dependencies.add(new String[]{"shop", "shops"});
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "communicationses/create";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("communications", Communications.findCommunications(id));
        uiModel.addAttribute("itemId", id);
        return "communicationses/show";
    }

	@RequestMapping(method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            uiModel.addAttribute("communicationses", Communications.findCommunicationsEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) Communications.countCommunicationses() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("communicationses", Communications.findAllCommunicationses());
        }
        return "communicationses/list";
    }

	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Communications communications, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("communications", communications);
            return "communicationses/update";
        }
        uiModel.asMap().clear();
        communications.merge();
        return "redirect:/communicationses/" + encodeUrlPathSegment(communications.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("communications", Communications.findCommunications(id));
        return "communicationses/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        Communications.findCommunications(id).remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/communicationses";
    }

	@ModelAttribute("abstractpeople")
    public Collection<AbstractPerson> populateAbstractpeople() {
        return AbstractPerson.findAllAbstractpeople();
    }

	@ModelAttribute("communicationses")
    public Collection<Communications> populateCommunicationses() {
        return Communications.findAllCommunicationses();
    }

	@ModelAttribute("shops")
    public Collection<Shop> populateShops() {
        return Shop.findAllShops();
    }

	@ModelAttribute("commtypes")
    public Collection<CommType> populateCommTypes() {
        return Arrays.asList(CommType.class.getEnumConstants());
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
