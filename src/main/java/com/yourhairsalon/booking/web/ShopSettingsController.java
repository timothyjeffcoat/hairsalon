package com.yourhairsalon.booking.web;

import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.ShopSettings;
import com.yourhairsalon.booking.reference.ClientDisplay;
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

@RooWebScaffold(path = "shopsettingses", formBackingObject = ShopSettings.class)
@RequestMapping("/shopsettingses")
@Controller
public class ShopSettingsController {



	@RequestMapping(params = { "find=ByEmail_address", "form" }, method = RequestMethod.GET)
    public String findShopSettingsesByEmail_addressForm(Model uiModel) {
        return "shopsettingses/findShopSettingsesByEmail_address";
    }

	@RequestMapping(params = "find=ByEmail_address", method = RequestMethod.GET)
    public String findShopSettingsesByEmail_address(@RequestParam("email_address") String email_address, Model uiModel) {
        uiModel.addAttribute("shopsettingses", ShopSettings.findShopSettingsesByEmail_address(email_address).getResultList());
        return "shopsettingses/list";
    }

	@RequestMapping(params = { "find=ById", "form" }, method = RequestMethod.GET)
    public String findShopSettingsesByIdForm(Model uiModel) {
        return "shopsettingses/findShopSettingsesById";
    }

	@RequestMapping(params = "find=ById", method = RequestMethod.GET)
    public String findShopSettingsesById(@RequestParam("id") Long id, Model uiModel) {
        uiModel.addAttribute("shopsettingses", ShopSettings.findShopSettingsesById(id).getResultList());
        return "shopsettingses/list";
    }

	@RequestMapping(params = { "find=ByShop", "form" }, method = RequestMethod.GET)
    public String findShopSettingsesByShopForm(Model uiModel) {
        uiModel.addAttribute("shops", Shop.findAllShops());
        return "shopsettingses/findShopSettingsesByShop";
    }

	@RequestMapping(params = "find=ByShop", method = RequestMethod.GET)
    public String findShopSettingsesByShop(@RequestParam("shop") Shop shop, Model uiModel) {
        uiModel.addAttribute("shopsettingses", ShopSettings.findShopSettingsesByShop(shop).getResultList());
        return "shopsettingses/list";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> showJson(@PathVariable("id") Long id) {
        ShopSettings shopsettings = ShopSettings.findShopSettings(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        if (shopsettings == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(shopsettings.toJson(), headers, HttpStatus.OK);
    }

	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(ShopSettings.toJsonArray(ShopSettings.findAllShopSettingses()), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json) {
        ShopSettings.fromJsonToShopSettings(json).persist();
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJsonArray(@RequestBody String json) {
        for (ShopSettings shopSettings: ShopSettings.fromJsonArrayToShopSettingses(json)) {
            shopSettings.persist();
        }
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJson(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (ShopSettings.fromJsonToShopSettings(json).merge() == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJsonArray(@RequestBody String json) {
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        for (ShopSettings shopSettings: ShopSettings.fromJsonArrayToShopSettingses(json)) {
            if (shopSettings.merge() == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Long id) {
        ShopSettings shopsettings = ShopSettings.findShopSettings(id);
        HttpHeaders headers= new HttpHeaders();
        headers.add("Content-Type", "application/text");
        if (shopsettings == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        shopsettings.remove();
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByEmail_address", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindShopSettingsesByEmail_address(@RequestParam("email_address") String email_address) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(ShopSettings.toJsonArray(ShopSettings.findShopSettingsesByEmail_address(email_address).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ById", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindShopSettingsesById(@RequestParam("id") Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(ShopSettings.toJsonArray(ShopSettings.findShopSettingsesById(id).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(params = "find=ByShop", method = RequestMethod.GET, headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> jsonFindShopSettingsesByShop(@RequestParam("shop") Shop shop) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(ShopSettings.toJsonArray(ShopSettings.findShopSettingsesByShop(shop).getResultList()), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST)
    public String create(@Valid ShopSettings shopSettings, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("shopSettings", shopSettings);
            return "shopsettingses/create";
        }
        uiModel.asMap().clear();
        shopSettings.persist();
        return "redirect:/shopsettingses/" + encodeUrlPathSegment(shopSettings.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model uiModel) {
        uiModel.addAttribute("shopSettings", new ShopSettings());
        List dependencies = new ArrayList();
        if (Shop.countShops() == 0) {
            dependencies.add(new String[]{"shop", "shops"});
        }
        uiModel.addAttribute("dependencies", dependencies);
        return "shopsettingses/create";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("shopsettings", ShopSettings.findShopSettings(id));
        uiModel.addAttribute("itemId", id);
        return "shopsettingses/show";
    }

	@RequestMapping(method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            uiModel.addAttribute("shopsettingses", ShopSettings.findShopSettingsEntries(page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) ShopSettings.countShopSettingses() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("shopsettingses", ShopSettings.findAllShopSettingses());
        }
        return "shopsettingses/list";
    }

	@RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid ShopSettings shopSettings, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("shopSettings", shopSettings);
            return "shopsettingses/update";
        }
        uiModel.asMap().clear();
        shopSettings.merge();
        return "redirect:/shopsettingses/" + encodeUrlPathSegment(shopSettings.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("shopSettings", ShopSettings.findShopSettings(id));
        return "shopsettingses/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        ShopSettings.findShopSettings(id).remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/shopsettingses";
    }

	@ModelAttribute("shops")
    public Collection<Shop> populateShops() {
        return Shop.findAllShops();
    }

	@ModelAttribute("shopsettingses")
    public Collection<ShopSettings> populateShopSettingses() {
        return ShopSettings.findAllShopSettingses();
    }

	@ModelAttribute("clientdisplays")
    public Collection<ClientDisplay> populateClientDisplays() {
        return Arrays.asList(ClientDisplay.class.getEnumConstants());
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
