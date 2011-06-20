package com.yourhairsalon.booking.web;

import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.yourhairsalon.booking.domain.Shop;

@RequestMapping("/public/shop")
@Controller
public class PublicShopController {
	private static final Log log = LogFactory.getLog(PublicShopController.class);

    @RequestMapping(method = RequestMethod.POST, value = "{id}")
    public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    }

    @RequestMapping
    public String index() {
        return "public/shop/index";
    }

    @RequestMapping(value="/{shopurl}", method = RequestMethod.GET)
    public ModelAndView getForShopname(
    		@PathVariable String shopurl,Model model) {
    	log.debug("ENTERED getForShopname");
    	TypedQuery<Shop> shop = Shop.findShopsByShop_urlEquals(shopurl);
    	ModelAndView mav = new ModelAndView();
		log.debug("ENTERED standar");
		mav.setViewName("public/shop/index");
    	mav.addObject("shopurl",shopurl);
    	model.addAttribute("shopurl",shopurl);
    	log.debug("shopurl "+shopurl);
    	if(shop.getResultList().size() > 0){
    		log.debug("!!!!! FOUND SHOP !!!");
    		String shopname = shop.getResultList().get(0).getShop_name();
    		mav.addObject("shopname",shopname);
    		model.addAttribute("shopname",shopname);
    		log.debug("shopname "+shopname);
    	}
    	log.debug("EXITING getForShopname");
    	return mav;
    }


    @RequestMapping(value="/{shopurl}/login", method = RequestMethod.GET)
    public ModelAndView login(
    		@PathVariable String shopurl,Model model) {
    	log.debug("ENTERED login");
    	TypedQuery<Shop> shop = Shop.findShopsByShop_urlEquals(shopurl);
    	ModelAndView mav = new ModelAndView();
		mav.setViewName("public/shop/login");
    	mav.addObject("storeurl",shopurl);
    	model.addAttribute("shopurl",shopurl);
    	mav.addObject("login","trying to login");
    	log.debug("shopurl "+shopurl);
    	if(shop.getResultList().size() > 0){
    		log.debug("the resultlist is greater than 0 ");
    		String shopname = shop.getResultList().get(0).getShop_name();
    		mav.addObject("shopname",shopname);
    		model.addAttribute("shopname",shopname);
    		log.debug("shopname "+shopname);
    	}
    	log.debug("EXITING login");
    	return mav;
    }
    
}
