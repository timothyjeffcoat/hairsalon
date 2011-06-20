package com.yourhairsalon.booking.web;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import javax.persistence.TypedQuery;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Details;
import com.paypal.api.payments.Item;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.base.rest.PayPalResource;
import com.paypal.sdk.core.nvp.NVPDecoder;
import com.paypal.sdk.core.nvp.NVPEncoder;
import com.paypal.sdk.profiles.APIProfile;
import com.paypal.sdk.profiles.ProfileFactory;
import com.paypal.sdk.services.NVPCallerServices;
import com.paypal.sdk.util.ResponseBuilder;
import com.paypal.sdk.util.Util;
import com.yourhairsalon.booking.domain.AppointmentDeep;
import com.yourhairsalon.booking.domain.Audit;
import com.yourhairsalon.booking.domain.BaseService;
import com.yourhairsalon.booking.domain.Clients;
import com.yourhairsalon.booking.domain.GCal;
import com.yourhairsalon.booking.domain.Person;
import com.yourhairsalon.booking.domain.PersonDaoImpl;
import com.yourhairsalon.booking.domain.Registration;
import com.yourhairsalon.booking.domain.Settings;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.Staff;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;

import  com.yourhairsalon.booking.domain.ShopSettings;
import com.yourhairsalon.booking.form.FullClientForm;
import com.yourhairsalon.booking.form.GCalForm;
import com.yourhairsalon.booking.form.UserPreference;
import com.yourhairsalon.booking.reference.ClientDisplay;
import com.yourhairsalon.booking.reference.RegistrationTypes;

import flexjson.JSONException;
import flexjson.JSONSerializer;

import org.springframework.web.bind.annotation.ModelAttribute;

import com.yourhairsalon.booking.util.*;
import com.google.gdata.client.Query;
import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Link;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.calendar.CalendarFeed;
import com.google.gdata.data.calendar.WebContent;
import com.google.gdata.data.extensions.ExtendedProperty;
import com.google.gdata.data.extensions.Recurrence;
import com.google.gdata.data.extensions.Reminder;
import com.google.gdata.data.extensions.Reminder.Method;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.util.*;

@RequestMapping("/settings")
@Controller
public class SettingsController {
	private static final Log log = LogFactory.getLog(SettingsController.class);
	private NVPCallerServices caller = null;
	private NVPEncoder encoder = new NVPEncoder();
	private NVPDecoder decoder = new NVPDecoder();
	private Audit audit = new Audit();
	private String individualusername;
	
	Map<String, String> map = new HashMap<String, String>();
	
	
	@Autowired
	private PersonDaoImpl personDao;
    
	@Autowired
    private UserPreference preferences;
	
	private Shop shop;

	// The base URL for a user's calendar metafeed (needs a username appended).
	private static final String METAFEED_URL_BASE = "https://www.google.com/calendar/feeds/";
	
	private static final String METAFEED_OWN_URL_BASE = "https://www.google.com/calendar/feeds/default/owncalendars/full";
		
	// The string to add to the user's metafeedUrl to access the event feed for
	// their primary calendar.
	private static final String EVENT_FEED_URL_SUFFIX = "/private/full";

	// The URL for the metafeed of the specified user.
	// (e.g. http://www.google.com/feeds/calendar/jdoe@gmail.com)
	private static URL metafeedUrl = null;

	// The URL for the event feed of the specified user's primary calendar.
	// (e.g. http://www.googe.com/feeds/calendar/jdoe@gmail.com/private/full)
	private static URL eventFeedUrl = null;
	
	
	
	public void init(ServletConfig servletConfig) throws ServletException {
		// ##Load Configuration
		// Load SDK configuration for
		// the resource. This intialization code can be
		// done as Init Servlet.
		InputStream is = SettingsController.class
				.getResourceAsStream("/sdk_config.properties");
		try {
			PayPalResource.initConfig(is);
		} catch (PayPalRESTException e) {
			log.error(e.getMessage());
		}

	}
	
    private void setShop(Shop shop){
    	getPreferences().setShop(shop);
    	this.shop = shop;
    }
	private Shop getShop(){
		log.debug("ENTERED getShop IndexController");
		Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	if(obj instanceof InetOrgPerson) {
			log.debug("The principal object is InetOrgPerson");
			String parentshop = ((InetOrgPerson) obj).getO();
			String username = ((UserDetails) obj).getUsername();
			setIndividualusername(((InetOrgPerson) obj).getUid());
			log.info("parentshop of LOGGED IN USER: " + parentshop);
			log.info("username of LOGGED IN USER: " + username);
//			if(getPreferences().getShop() == null){
				TypedQuery<Shop> shop = null;
				if(parentshop == null){
					parentshop = username;
				}
				shop = Shop.findShopsByShopuuid(parentshop);
				log.debug("the returned results for looking for "+ parentshop + " is a size of: " + shop.getResultList().size());
				if(shop.getResultList().size() > 0){
					getPreferences().setShop(shop.getResultList().get(0));
					
				}
//			}			
		}else if (obj instanceof UserDetails) {
			log.debug("The principal object is UserDetails");
			String username = ((UserDetails) obj).getUsername();
			log.debug("username: "+username);
			log.debug("preferences: "+getPreferences());
//			if (getPreferences().getShop()==null) {
				log.info("LOGGED IN USER: " + username);
				TypedQuery<Shop> shop = Shop.findShopsByShopuuid(username);
				log.debug("the returned results for looking for "+ username + " is a size of: " + shop.getResultList().size());
				if(shop.getResultList().size() > 0){
					getPreferences().setShop(shop.getResultList().get(0));
					
				}else{
					// see if you can find the username in staff and then get shop from that
					log.debug("TRYING TO ASSOCIATE A SHOP WITH THE LOGGED IN PERSON");
					TypedQuery<Staff> staf = Staff.findStaffsByUsername(username);
					if(staf.getResultList().size() > 0){
						Staff staff = staf.getResultList().get(0);
						Shop sh = staff.getShop();
						shop = Shop.findShopsByShopuuid(sh.getShopuuid());
						log.debug("shopuuid: "+ sh.getShopuuid());
						if(shop.getResultList().size() > 0){
							getPreferences().setShop(sh);
							
						}
						
					}
				}
//			}
		}			
    	log.debug("EXITING getShop IndexController");
		return getPreferences().getShop();
	}	
	private void saveAuditMessage(String msg,Staff staff,String type ){
		getAudit().setShop(getShop());
		getAudit().setDescription(msg);
		getAudit().setStaff(staff);
		getAudit().setTs(new Date());
		getAudit().setType(type);
		getAudit().merge();
	}

	/**
	 * the url for a paypal cancel request.
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/paypalcancel", method = RequestMethod.GET)
	public ModelAndView cancel(Model model) {
		log.debug("ENTERED cancel");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("settings");
		model.addAttribute("cancelmessage", "paypal cancel");
		log.debug("EXITING cancel");
		return mav;
	}

	/**
	 * 3rd step in the process. approval of purchase via paypal.
	 * 
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/DoExpressCheckoutPayment", method = RequestMethod.GET)
	public ModelAndView DoExpressCheckoutPayment(
			@RequestParam(value="token") String token,
					@RequestParam(value="PayerID") String PayerID,
					@RequestParam(value="TotalAmount") String TotalAmount,
					Model model,HttpServletRequest request) {
		log.debug("ENTERED DoExpressCheckoutPayment");
		ModelAndView mav = new ModelAndView();
		
		mav.setViewName("settings/receipt");	
		
		model.addAttribute("paypalsuccess", "paypal success");

		try {
			// NVPEncoder object is created and all the name value pairs are
			// loaded into it.
			NVPEncoder encoder = new NVPEncoder();
			encoder.add("METHOD", "DoExpressCheckoutPayment");
			encoder.add("TOKEN", token);
			encoder.add("PAYERID", PayerID);
			String paymentType = "sale";
			encoder.add("PAYMENTACTION", paymentType);
			encoder.add("AMT", TotalAmount);
			String currencyCodeType = "USD";
			encoder.add("CURRENCYCODE", currencyCodeType);
			
			Float float_amount = 0.00F;
			try{
				float_amount = Float.parseFloat(TotalAmount);
			}catch(Exception e){
				log.error(e);
			}
			// encode method will encode the name and value and form NVP string
			// for the request
			String strNVPString = encoder.encode();

			// call method will send the request to the server and return the
			// response NVPString
			String strNVPResponse = (String) caller.call(strNVPString);

			// NVPDecoder object is created
			NVPDecoder decoder = new NVPDecoder();

			// decode method of NVPDecoder will parse the request and decode the
			// name and value pair
			decoder.decode(strNVPResponse);

			ResponseBuilder rb=new ResponseBuilder();
			String header1 = "DoExpressCheckoutPayment";
			String header2 = "Thank you for your payment!";
			String resp=rb.BuildResponse(decoder,header1,header2);

			// checks for Acknowledgement and redirects accordingly to display
			// error messages
			String strAck = decoder.get("ACK");
			if (strAck != null
					&& !(strAck.equals("Success") || strAck
							.equals("SuccessWithWarning"))) {
				// session.setAttribute("response",decoder);
				// response.sendRedirect("APIError.jsp");
				log.error("PAYPAL ERROR: "+decoder);
				model.addAttribute("DoExpressCheckoutPaymentError", decoder);
			}else{
			
				Properties payments_properties = new Properties();
				try {
					payments_properties.load(this.getClass().getClassLoader().getResourceAsStream("payments.properties"));
				    
				} catch (IOException e) {
					log.error(e);
				}			
				String ONE_MONTH = payments_properties.getProperty("one.month");
				log.debug("ONE_MONTH: "+ONE_MONTH);
				String THREE_MONTHS = payments_properties.getProperty("three.months");
				String SIX_MONTHS = payments_properties.getProperty("six.months");
				String TWELVE_MONTHS = payments_properties.getProperty("twelve.months");
				String TWENTYFOUR_MONTHS = payments_properties.getProperty("twentyfour.months");
				String THIRTYSIX_MONTHS = payments_properties.getProperty("thirtysix.months");
				String SMS_ONE = payments_properties.getProperty("sms.one");
				String SMS_TWO = payments_properties.getProperty("sms.two");
				
				String months_msg = "";
				String DATE_FORMAT = "yyyy-MM-dd";
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
						DATE_FORMAT);
				Calendar c1 = Calendar.getInstance();
				java.util.Date tmpdate = getShop().getExpiration_date();
				if(tmpdate != null){
					log.debug("tmpdate YEAR: "+tmpdate.toString());
					c1.setTime(tmpdate); 
				}
				log.debug("TotalAmount: " + TotalAmount);
				if(TotalAmount.equalsIgnoreCase(ONE_MONTH)){
					// 1 month
					log.debug("trying to update shop with new expiration date");
					months_msg = "ONE MONTH";
					c1.add(Calendar.MONTH, 1);
					log.debug("c1 date: "+c1.getTime().toString());
					getShop().setType(RegistrationTypes.PAID);
					getShop().setExpiration_date(c1.getTime());
					getShop().merge();
					log.debug("FINISHED UPDATING SHOP");
				}else if(TotalAmount.equalsIgnoreCase(THREE_MONTHS)){
					// 3 months
					months_msg = "THREE MONTHS";
					c1.add(Calendar.MONTH, 3);
					log.debug("c1 date: "+c1.getTime().toString());
					getShop().setType(RegistrationTypes.PAID);
					getShop().setExpiration_date(c1.getTime());
					getShop().merge();
				}else if(TotalAmount.equalsIgnoreCase(SIX_MONTHS)){
					// 6 months
					months_msg = "SIX MONTHS";
					c1.add(Calendar.MONTH, 6);
					log.debug("c1 date: "+c1.getTime().toString());
					getShop().setType(RegistrationTypes.PAID);
					getShop().setExpiration_date(c1.getTime());
					getShop().merge();
				}else if(TotalAmount.equalsIgnoreCase(TWELVE_MONTHS)){
					// 12 months
					months_msg = "TWELVE MONTHS";
					c1.add(Calendar.MONTH, 12);
					log.debug("c1 date: "+c1.getTime().toString());
					getShop().setExpiration_date(c1.getTime());
					getShop().setType(RegistrationTypes.PAID);
					getShop().merge();
					
				}else if(TotalAmount.equalsIgnoreCase(TWENTYFOUR_MONTHS)){
					// 24 months
					months_msg = "TWENTY-FOUR MONTHS";
					c1.add(Calendar.MONTH, 24);
					log.debug("c1 date: "+c1.getTime().toString());
					getShop().setExpiration_date(c1.getTime());
					getShop().setType(RegistrationTypes.PAID);
					getShop().merge();
					
				}else if(TotalAmount.equalsIgnoreCase(THIRTYSIX_MONTHS)){
					// 36 months
					months_msg = "THIRTY-SIX MONTHS";
					c1.add(Calendar.MONTH, 36);
					log.debug("c1 date: "+c1.getTime().toString());
					getShop().setExpiration_date(c1.getTime());
					getShop().setType(RegistrationTypes.PAID);
					getShop().merge();
					
				}else if(TotalAmount.equalsIgnoreCase(SMS_ONE)){
					// sms 1000
					months_msg = "1000 TEXT MESSAGES";
					log.debug("c1 date: "+c1.getTime().toString());
					getShop().setSms_last_purchase_date(c1.getTime());
					Long tmp_purchased = getShop().getNumber_sms_purchased();
					getShop().setNumber_sms_purchased(tmp_purchased+1000);
					getShop().merge();
					
				}else if(TotalAmount.equalsIgnoreCase(SMS_TWO)){
					// sms 2000
					months_msg = "2000 TEXT MESSAGES";
					log.debug("c1 date: "+c1.getTime().toString());
					getShop().setSms_last_purchase_date(c1.getTime());
					Long tmp_purchased = getShop().getNumber_sms_purchased();
					getShop().setNumber_sms_purchased(tmp_purchased+2000);
					getShop().merge();
					
				}
				// CREATE A RECORD IN REGISTRATION THAT RECORDS THIS TRANSACTION
				try{
					log.debug("HEY I AM TRYING TO UPDATE THE REGISTRATION DATA");
					Registration reg = new Registration();
					reg.setPayerid(PayerID);
					reg.setToken(token);
					reg.setPaymentamount(float_amount);
					reg.setDatecreated(new Date());
					reg.setNote(months_msg);
					reg.setShop(getShop());
					reg.setType(RegistrationTypes.PAYMENT);
					reg.merge();
					log.debug("SUCCEEDED UPDATING THE REGISTRATION DATA");
				}catch(Exception payexception){
					log.error("HEY SOMETHING BAD HAPPENED HERE TOO!!!");
					log.error(payexception);
				}
				log.debug("DoExpressCheckoutPaymentSUCCESS resp: "+resp);
				log.debug("DoExpressCheckoutPaymentSUCCESS decoder: "+decoder);
				model.addAttribute("DoExpressCheckoutPaymentSuccess", resp);
			}
			
		} catch (Exception e) {
			// session.setAttribute("exception", e);
			// response.sendRedirect("Error.jsp");
			// return;
			log.error("HEY SOMETHING BAD HAPPENED HERE!!!");
			log.error(e);
		}

		log.debug("EXITING DoExpressCheckoutPayment");
		return mav;
	}
	@RequestMapping(value = "/receipt", method = RequestMethod.POST)
	public ModelAndView paypalReceipt(
			Model model,
			HttpServletRequest request, HttpServletResponse response) {
		log.debug("ENTERED paypalReceipt");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("settings/receipt");
		
		model.addAttribute("paypalreceipt", "paypal success");
		
		log.debug("EXITING paypalReceipt");
		return mav;
	}
	/**
	 * 2nd step using paypal.
	 * 
	 * paypal will invoke or call this page in response to a successful login and approval of purchase
	 * 
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/GetExpressCheckoutDetails", method = RequestMethod.GET)
	public ModelAndView GetExpressCheckoutDetails(
			Model model,
			HttpServletRequest request, HttpServletResponse response) {
		log.debug("ENTERED GetExpressCheckoutDetails");
		ModelAndView mav = new ModelAndView();
		
		model.addAttribute("paypalsuccess", "paypal success");

		// NVPCallerServiced object is taken from the session

		try {
			// NVPEncoder object is created and all the name value pairs are
			// loaded into it.
			NVPEncoder encoder = new NVPEncoder();
			encoder.add("METHOD", "GetExpressCheckoutDetails");
			encoder.add("TOKEN", request.getParameter("token"));

			// encode method will encode the name and value and form NVP string
			// for the request
			String strNVPString = encoder.encode();

			// call method will send the request to the server and return the
			// response NVPString
			String strNVPResponse = (String) caller.call(strNVPString);

			// NVPDecoder object is created
			NVPDecoder decoder = new NVPDecoder();

			// decode method of NVPDecoder will parse the request and decode the
			// name and value pair
			decoder.decode(strNVPResponse);

			// ResponseBuilder rb=new ResponseBuilder();
			// String header1 = "Review Order";
			// String header2 = "Step 3: DoExpressCheckoutPayment, Order Review Page";
			// String resp=rb.BuildResponse(decoder,header1,header2);

			// checks for Acknowledgement and redirects accordingly to display
			// error messages
			String strAck = decoder.get("ACK");
			if (strAck != null
					&& !(strAck.equals("Success") || strAck
							.equals("SuccessWithWarning"))) {
				// session.setAttribute("response",decoder);
				// response.sendRedirect("APIError.jsp");
				log.error("GetExpressCheckoutDetailsERROR: "+decoder);

			}else{
			
				// make a call to DoExpressCheckoutPayment ?
				model.addAttribute("approvalmessage", "approve this purchase");
				String token = decoder.get("TOKEN");
				String payerid = decoder.get("PAYERID");
				String totalamt = decoder.get("AMT");
				log.error("GetExpressCheckoutDetailsSUCCESS: "+decoder);
				log.debug("token "+token);
				log.debug("payerid "+payerid);
				log.debug("totalamt: "+totalamt);
				
				model.addAttribute("token",token);
				model.addAttribute("PayerID",payerid);
				model.addAttribute("TotalAmount",totalamt);
				DoExpressCheckoutPayment(token,payerid,totalamt,model,request);
			}
			
		} catch (Exception e) {
			log.error(e);
		}
		// check status
		mav.setViewName("settings");	
		
		
		log.debug("EXITING GetExpressCheckoutDetails");
		return mav;
	}
	
	@RequestMapping(value = "/paymentwithpaypal", method = RequestMethod.POST)
	public String createPayment(HttpServletRequest req, HttpServletResponse resp) {
		log.debug("Entered createPayment");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("settings/paypalresponse");
		
		Payment createdPayment = null;
		// ###AccessToken
		// Retrieve the access token from
		// OAuthTokenCredential by passing in
		// ClientID and ClientSecret
		APIContext apiContext = null;
		String accessToken = null;
		try {
			accessToken = GenerateAccessToken.getAccessToken();

			// ### Api Context
			// Pass in a `ApiContext` object to authenticate
			// the call and to send a unique request id
			// (that ensures idempotency). The SDK generates
			// a request id if you do not pass one explicitly.
			apiContext = new APIContext(accessToken);
			// Use this variant if you want to pass in a request id
			// that is meaningful in your application, ideally
			// a order id.
			/*
			 * String requestId = Long.toString(System.nanoTime(); APIContext
			 * apiContext = new APIContext(accessToken, requestId ));
			 */
		} catch (PayPalRESTException e) {
			req.setAttribute("error", e.getMessage());
		}
		if (req.getParameter("PayerID") != null) {
			// THIS SECTION IS EXECUTED ON THE RETURN TRIP FROM PAYPAL AFTER PAYMENT IS MADE
			log.debug("Entered PayerID : " + req.getParameter("PayerID"));
			Payment payment = new Payment();
			if (req.getParameter("guid") != null) {
				log.debug("Entered guid");
				payment.setId(map.get(req.getParameter("guid")));
				log.debug("guid : "+req.getParameter("guid"));
			}

			PaymentExecution paymentExecution = new PaymentExecution();
			paymentExecution.setPayerId(req.getParameter("PayerID"));
			try {
				createdPayment = payment.execute(apiContext, paymentExecution);
				ResultPrinter.addResult(req, resp, "Executed The Payment", Payment.getLastRequest(), Payment.getLastResponse(), null);
			} catch (PayPalRESTException e) {
				ResultPrinter.addResult(req, resp, "Executed The Payment", Payment.getLastRequest(), null, e.getMessage());
			}
//			 resp.setHeader("Location", "settings/paypalresponse");
			String gotoresponse = req.getScheme() + "://"+ req.getServerName() + ":" + req.getServerPort()+ req.getContextPath() + "/settings/paypalresponse";
			log.debug("Exiting createPayment to go to the paypalresponse page : " + gotoresponse);
			 return "redirect:" + gotoresponse;			 
		} else {
			// THIS SECTION IS BEGINS THE PAYPAL PROCESS SENDING USER TO THE PAYPAL LOGIN PAGE TO MAKE PAYMENT
			String typeOfTransaction = req.getParameter("process");
			String shipping_cost = "0";
			String shipping_subtotal = "0";
			String shipping_tax = "0";
			String currency = "USD";
			String total = "";
			String transaction_description = "";
			String item_name = "";
			String item_currency = currency;
			String item_price = "";
			String item_quanity = "1";
			
			log.debug("typeOfTransaction: "+typeOfTransaction);
			Properties payments_properties = new Properties();
			try {
				payments_properties.load(this.getClass().getClassLoader().getResourceAsStream("payments.properties"));
				log.debug("payments_properties: " + payments_properties);
			} catch (IOException e) {
				log.error(e);
			}			
			String paymentAmount = "";
			if(typeOfTransaction.equalsIgnoreCase("Pay 1 Month")){
				paymentAmount = payments_properties.getProperty("one.month");
				transaction_description = payments_properties.getProperty("one.description");;
				item_name = "Pay 1 Month";
				item_price = paymentAmount;
			}else if(typeOfTransaction.equalsIgnoreCase("Pay 3 Months")){
				paymentAmount = payments_properties.getProperty("three.months");
				transaction_description = payments_properties.getProperty("three.description");;
				item_name = "Pay 3 Months";
				item_price = paymentAmount;
			}else if(typeOfTransaction.equalsIgnoreCase("Pay 6 Months")){
				paymentAmount = payments_properties.getProperty("six.months");
				transaction_description = payments_properties.getProperty("six.description");;
				item_name = "Pay 6 Months";
				item_price = paymentAmount;
			}else if(typeOfTransaction.equalsIgnoreCase("Pay 12 Months")){
				paymentAmount = payments_properties.getProperty("twelve.months");
				transaction_description = payments_properties.getProperty("twelve.description");;
				item_name = "Pay 12 Months";
				item_price = paymentAmount;
			}else if(typeOfTransaction.equalsIgnoreCase("Pay 24 Months")){
				paymentAmount = payments_properties.getProperty("twentyfour.months");
				transaction_description = payments_properties.getProperty("twentyfour.description");;
				item_name = "Pay 24 Months";
				item_price = paymentAmount;
			}else if(typeOfTransaction.equalsIgnoreCase("Pay 36 Months")){
				paymentAmount = payments_properties.getProperty("thirtysix.months");
				transaction_description = payments_properties.getProperty("thirtysix.description");;
				item_name = "Pay 36 Months";
				item_price = paymentAmount;
			}else if(typeOfTransaction.equalsIgnoreCase("1000 messages")){
				transaction_description = payments_properties.getProperty("sms.one.descripton");
				item_name = "1000 messages";
				item_price = payments_properties.getProperty("sms.one");
			}else if(typeOfTransaction.equalsIgnoreCase("2000 messages")){
				transaction_description = payments_properties.getProperty("sms.two.descripton");
				item_name = "2000 messages";
				item_price = payments_properties.getProperty("sms.two");
			}
			total = item_price;
			shipping_subtotal = total;
			
			// ###Details
			// Let's you specify details of a payment amount.
			Details details = new Details();
			details.setShipping(shipping_cost);
			details.setSubtotal(shipping_subtotal);
			details.setTax(shipping_tax);

			// ###Amount
			// Let's you specify a payment amount.
			Amount amount = new Amount();
			amount.setCurrency(currency);
			// Total must be equal to sum of shipping, tax and subtotal.
			amount.setTotal(total);
			amount.setDetails(details);

			// ###Transaction
			// A transaction defines the contract of a
			// payment - what is the payment for and who
			// is fulfilling it. Transaction is created with
			// a `Payee` and `Amount` types
			Transaction transaction = new Transaction();
			transaction.setAmount(amount);
			transaction.setDescription(transaction_description);

			// ### Items
			Item item = new Item();
			item.setName(item_name).setQuantity(item_quanity).setCurrency(item_currency).setPrice(item_price);
			ItemList itemList = new ItemList();
			List<Item> items = new ArrayList<Item>();
			items.add(item);
			itemList.setItems(items);
			
			transaction.setItemList(itemList);
			
			
			// The Payment creation API requires a list of
			// Transaction; add the created `Transaction`
			// to a List
			List<Transaction> transactions = new ArrayList<Transaction>();
			transactions.add(transaction);

			// ###Payer
			// A resource representing a Payer that funds a payment
			// Payment Method
			// as 'paypal'
			Payer payer = new Payer();
			payer.setPaymentMethod("paypal");

			// ###Payment
			// A Payment Resource; create one using
			// the above types and intent as 'sale'
			Payment payment = new Payment();
			payment.setIntent("sale");
			payment.setPayer(payer);
			payment.setTransactions(transactions);

			// ###Redirect URLs
			RedirectUrls redirectUrls = new RedirectUrls();
			String guid = UUID.randomUUID().toString().replaceAll("-", "");
			redirectUrls.setCancelUrl(req.getScheme() + "://"
					+ req.getServerName() + ":" + req.getServerPort()
					+ req.getContextPath() + "/settings/responsewithpaypal?guid=" + guid);
			redirectUrls.setReturnUrl(req.getScheme() + "://"
					+ req.getServerName() + ":" + req.getServerPort()
					+ req.getContextPath() + "/settings/responsewithpaypal?guid=" + guid);
			payment.setRedirectUrls(redirectUrls);

			
			// Create a payment by posting to the APIService
			// using a valid AccessToken
			// The return object contains the status;
			try {
				createdPayment = payment.create(apiContext);
				log.info("Created payment with id = "
						+ createdPayment.getId() + " and status = "
						+ createdPayment.getState());
				// ###Payment Approval Url
				Iterator<Links> links = createdPayment.getLinks().iterator();
				String goToPaypal = "";
				while (links.hasNext()) {
					Links link = links.next();
					if (link.getRel().equalsIgnoreCase("approval_url")) {
						req.setAttribute("redirectURL", link.getHref());
						goToPaypal = link.getHref();	
					}
				}
				ResultPrinter.addResult(req, resp, "Payment with PayPal", Payment.getLastRequest(), Payment.getLastResponse(), null);
				map.put(guid, createdPayment.getId());
				log.debug("goToPaypal: " + goToPaypal);
//				resp.setHeader("Location", goToPaypal);
				return "redirect:" + goToPaypal;
			} catch (PayPalRESTException e) {
				ResultPrinter.addResult(req, resp, "Payment with PayPal", Payment.getLastRequest(), null, e.getMessage());
			}
		}
		log.debug("Exiting createPayment");
//		return createdPayment;
		return "redirect:./settings";
	}

	@RequestMapping(value = "/responsewithpaypal", method = RequestMethod.GET)
	public String responsePayment(HttpServletRequest req, HttpServletResponse resp) {
		log.debug("Entered responsePayment");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("settings/paypalresponse");

		Payment createdPayment = null;
		// ###AccessToken
		// Retrieve the access token from
		// OAuthTokenCredential by passing in
		// ClientID and ClientSecret
		APIContext apiContext = null;
		String accessToken = null;
		try {
			accessToken = GenerateAccessToken.getAccessToken();

			// ### Api Context
			// Pass in a `ApiContext` object to authenticate
			// the call and to send a unique request id
			// (that ensures idempotency). The SDK generates
			// a request id if you do not pass one explicitly.
			apiContext = new APIContext(accessToken);
			// Use this variant if you want to pass in a request id
			// that is meaningful in your application, ideally
			// a order id.
			/*
			 * String requestId = Long.toString(System.nanoTime(); APIContext
			 * apiContext = new APIContext(accessToken, requestId ));
			 */
		} catch (PayPalRESTException e) {
			req.setAttribute("error", e.getMessage());
		}
		if (req.getParameter("PayerID") != null) {
			log.debug("Entered PayerID : " + req.getParameter("PayerID"));
			Payment payment = new Payment();
			if (req.getParameter("guid") != null) {
				log.debug("Entered guid");
				payment.setId(map.get(req.getParameter("guid")));
				log.debug("guid : "+req.getParameter("guid"));
			}

			PaymentExecution paymentExecution = new PaymentExecution();
			paymentExecution.setPayerId(req.getParameter("PayerID"));
			try {
				createdPayment = payment.execute(apiContext, paymentExecution);

				/*
				 * sample response
				 * 
				 * {"id":"PAY-4AD58332V0996661SKUCF3XA","create_time":"2015-03-14T16:12:12Z","update_time":"2015-03-14T16:12:24Z","state":"approved","intent":"sale","payer":{"payment_method":"paypal","payer_info":{"email":"tim_1329347086_biz@scheduleem.com","first_name":"Scheduleem","last_name":"Shop","payer_id":"44FUF8HQC9SHY","shipping_address":{"line1":"1 Main St","city":"San Jose","state":"CA","postal_code":"95131","country_code":"US","recipient_name":"Scheduleem Shop's Test Store"}}},"transactions":[{"amount":{"total":"14.95","currency":"USD","details":{"subtotal":"14.95"}},"item_list":{"items":[{"name":"Pay 1 Month","price":"14.95","currency":"USD","quantity":"1"}],"shipping_address":{"recipient_name":"Scheduleem Shop's Test Store","line1":"1 Main St","city":"San Jose","state":"CA","postal_code":"95131","country_code":"US"}},"related_resources":[{"sale":{"id":"03316026LV962435D","create_time":"2015-03-14T16:12:12Z","update_time":"2015-03-14T16:12:24Z","amount":{"total":"14.95","currency":"USD"},"payment_mode":"INSTANT_TRANSFER","state":"completed","protection_eligibility":"ELIGIBLE","protection_eligibility_type":"ITEM_NOT_RECEIVED_ELIGIBLE,UNAUTHORIZED_PAYMENT_ELIGIBLE","parent_payment":"PAY-4AD58332V0996661SKUCF3XA","transaction_fee":{"value":"0.73","currency":"USD"},"links":[{"href":"https://api.sandbox.paypal.com/v1/payments/sale/03316026LV962435D","rel":"self","method":"GET"},{"href":"https://api.sandbox.paypal.com/v1/payments/sale/03316026LV962435D/refund","rel":"refund","method":"POST"},{"href":"https://api.sandbox.paypal.com/v1/payments/payment/PAY-4AD58332V0996661SKUCF3XA","rel":"parent_payment","method":"GET"}]}}]}],"links":[{"href":"https://api.sandbox.paypal.com/v1/payments/payment/PAY-4AD58332V0996661SKUCF3XA","rel":"self","method":"GET"}]}
				 * 
				 * 
				 * To see formatted response look in resources for paypal_example.json
				 */

				String json = Payment.getLastResponse();
				
				JsonParser parser = new JsonParser();
				JsonElement jsonElement = parser.parse(json);
				JsonObject obj = jsonElement.getAsJsonObject();

				// State of a created payment object is approved
				if(obj.get("state").getAsString().equalsIgnoreCase("approved")){
					log.debug("The transaction is approved");
					
					Set<Map.Entry<String, JsonElement>> map = obj.entrySet();
	                for(Map.Entry<String, JsonElement> entry : map){
	                    log.debug(entry.toString());
	                }
	                
	                JsonObject payerobj = obj.getAsJsonObject("payer");
	                log.debug("past payerobj: " + payerobj);
	                
	                JsonObject payerinfoobj = payerobj.getAsJsonObject("payer_info");
	                log.debug("past payerinfoobj: " + payerinfoobj);
	                
	                JsonElement jsonemailelement = payerinfoobj.get("email");
	                log.debug("past jsonemailelement: " + jsonemailelement);
	                
	                // important for identification with the database, the shop owner
	                String jsonemail = jsonemailelement.getAsString();
	                log.debug("jsonemail: "+jsonemail);
	                
	                JsonArray transactionsobj = obj.getAsJsonArray("transactions");
	                log.debug("transactionsobj: " + transactionsobj);
	                
	                JsonElement  itemsarray = transactionsobj.get(0);
	                log.debug("itemsarray: " + itemsarray);
	                
	                JsonObject itemslist = itemsarray.getAsJsonObject();
	                log.debug("itemslist: " + itemslist);
	                
	                JsonElement first_item = itemslist.get("item_list");
	                log.debug("first_item: " + first_item);
	                
	                JsonElement first_item_name = first_item.getAsJsonObject().get("items");
	                log.debug("first_item_name: " + first_item_name);

	                JsonArray firstitemsnamesarray = first_item_name.getAsJsonArray();
	                log.debug("firstitemsnamesarray: " + firstitemsnamesarray);
	                
	                JsonElement  firstitemsvalues = firstitemsnamesarray.get(0);
	                log.debug("firstitemsvalues: " + firstitemsvalues);
	                
	                
	                // important for identification about which product was purchased
	                String item_name = firstitemsvalues.getAsJsonObject().get("name").getAsString();
	                log.debug("item_name: " + item_name);

	                // important for identification about which product was purchased at what price	                
	                String item_price = firstitemsvalues.getAsJsonObject().get("price").getAsString();
	                log.debug("item_price: " + item_price);
	                
	                log.debug("Executed The Payment - last request: "+ Payment.getLastRequest() + " ||| last response: " + Payment.getLastResponse());
	                
	                JsonPrimitive tokenobject = obj.getAsJsonPrimitive("id");
	                log.debug("tokenobject: " + tokenobject);
	                
	                String token = tokenobject.getAsString();
	                log.debug("token: " + token);

	    			// persist the successful purchase to shop and shop settings
	                savePaypalResponse(req.getParameter("PayerID"), item_price, token, Float.parseFloat(item_price));
				}
				
				
			} catch (PayPalRESTException e) {
				ResultPrinter.addResult(req, resp, "Executed The Payment", Payment.getLastRequest(), null, e.getMessage());
				log.error(e);
			}catch(Exception ex){
				log.error(ex);
			}
			
			// now redirect to display the receipt
			String gotoresponse = req.getScheme() + "://"+ req.getServerName() + ":" + req.getServerPort()+ req.getContextPath() + "/settings";
			log.debug("Exiting responsePayment to go to the paypalresponse page : " + gotoresponse);
			 return "redirect:" + gotoresponse;			 
		}
		log.debug("Entered responsePayment");
//		return createdPayment;
		return "redirect:./settings";
	}
	
	/**
	 * updates the shop with values purchased via paypal for monthly subscription and purchase of sms text messages.
	 * 
	 * @param PayerID
	 * @param TotalAmount
	 * @param token
	 * @param float_amount
	 */
	private void savePaypalResponse(String PayerID, String TotalAmount, String token, Float float_amount){
		log.debug("Entered savePaypalResponse");
		Properties payments_properties = new Properties();
		try {
			payments_properties.load(this.getClass().getClassLoader().getResourceAsStream("payments.properties"));
		    
		} catch (IOException e) {
			log.error(e);
		}			
		String ONE_MONTH = payments_properties.getProperty("one.month");
		log.debug("ONE_MONTH: "+ONE_MONTH);
		String THREE_MONTHS = payments_properties.getProperty("three.months");
		String SIX_MONTHS = payments_properties.getProperty("six.months");
		String TWELVE_MONTHS = payments_properties.getProperty("twelve.months");
		String TWENTYFOUR_MONTHS = payments_properties.getProperty("twentyfour.months");
		String THIRTYSIX_MONTHS = payments_properties.getProperty("thirtysix.months");
		String SMS_ONE = payments_properties.getProperty("sms.one");
		String SMS_TWO = payments_properties.getProperty("sms.two");
		
		String months_msg = "";
		String DATE_FORMAT = "yyyy-MM-dd";
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
		Calendar c1 = Calendar.getInstance();
		java.util.Date tmpdate = getShop().getExpiration_date();
		if(tmpdate != null){
			log.debug("tmpdate YEAR: "+tmpdate.toString());
			c1.setTime(tmpdate); 
		}
		log.debug("TotalAmount: " + TotalAmount);
		if(TotalAmount.equalsIgnoreCase(ONE_MONTH)){
			// 1 month
			log.debug("trying to update shop with new expiration date");
			months_msg = "ONE MONTH";
			c1.add(Calendar.MONTH, 1);
			log.debug("c1 date: "+c1.getTime().toString());
			getShop().setType(RegistrationTypes.PAID);
			getShop().setExpiration_date(c1.getTime());
			getShop().merge();
			log.debug("FINISHED UPDATING SHOP");
		}else if(TotalAmount.equalsIgnoreCase(THREE_MONTHS)){
			// 3 months
			months_msg = "THREE MONTHS";
			c1.add(Calendar.MONTH, 3);
			log.debug("c1 date: "+c1.getTime().toString());
			getShop().setType(RegistrationTypes.PAID);
			getShop().setExpiration_date(c1.getTime());
			getShop().merge();
		}else if(TotalAmount.equalsIgnoreCase(SIX_MONTHS)){
			// 6 months
			months_msg = "SIX MONTHS";
			c1.add(Calendar.MONTH, 6);
			log.debug("c1 date: "+c1.getTime().toString());
			getShop().setType(RegistrationTypes.PAID);
			getShop().setExpiration_date(c1.getTime());
			getShop().merge();
		}else if(TotalAmount.equalsIgnoreCase(TWELVE_MONTHS)){
			// 12 months
			months_msg = "TWELVE MONTHS";
			c1.add(Calendar.MONTH, 12);
			log.debug("c1 date: "+c1.getTime().toString());
			getShop().setExpiration_date(c1.getTime());
			getShop().setType(RegistrationTypes.PAID);
			getShop().merge();
			
		}else if(TotalAmount.equalsIgnoreCase(TWENTYFOUR_MONTHS)){
			// 24 months
			months_msg = "TWENTY-FOUR MONTHS";
			c1.add(Calendar.MONTH, 24);
			log.debug("c1 date: "+c1.getTime().toString());
			getShop().setExpiration_date(c1.getTime());
			getShop().setType(RegistrationTypes.PAID);
			getShop().merge();
			
		}else if(TotalAmount.equalsIgnoreCase(THIRTYSIX_MONTHS)){
			// 36 months
			months_msg = "THIRTY-SIX MONTHS";
			c1.add(Calendar.MONTH, 36);
			log.debug("c1 date: "+c1.getTime().toString());
			getShop().setExpiration_date(c1.getTime());
			getShop().setType(RegistrationTypes.PAID);
			getShop().merge();
			
		}else if(TotalAmount.equalsIgnoreCase(SMS_ONE)){
			// sms 1000
			months_msg = "1000 TEXT MESSAGES";
			log.debug("c1 date: "+c1.getTime().toString());
			getShop().setSms_last_purchase_date(c1.getTime());
			Long tmp_purchased = getShop().getNumber_sms_purchased();
			getShop().setNumber_sms_purchased(tmp_purchased+1000);
			getShop().merge();
			
		}else if(TotalAmount.equalsIgnoreCase(SMS_TWO)){
			// sms 2000
			months_msg = "2000 TEXT MESSAGES";
			log.debug("c1 date: "+c1.getTime().toString());
			getShop().setSms_last_purchase_date(c1.getTime());
			Long tmp_purchased = getShop().getNumber_sms_purchased();
			getShop().setNumber_sms_purchased(tmp_purchased+2000);
			getShop().merge();
			
		}
		// CREATE A RECORD IN REGISTRATION THAT RECORDS THIS TRANSACTION
		try{
			log.debug("HEY I AM TRYING TO UPDATE THE REGISTRATION DATA");
			Registration reg = new Registration();
			reg.setPayerid(PayerID);
			reg.setToken(token);
			reg.setPaymentamount(float_amount);
			reg.setDatecreated(new Date());
			reg.setNote(months_msg);
			reg.setShop(getShop());
			reg.setType(RegistrationTypes.PAYMENT);
			reg.merge();
			log.debug("SUCCEEDED UPDATING THE REGISTRATION DATA");
		}catch(Exception payexception){
			log.error("HEY SOMETHING BAD HAPPENED HERE TOO!!!");
			log.error(payexception);
		}
		log.debug("Exiting savePaypalResponse");
	}
	
	@RequestMapping(value = "/paypalresponse", method = RequestMethod.GET)
	public ModelAndView redirectToResponse(HttpServletRequest request, HttpServletResponse response) {
		log.debug("Entered redirectToResponse");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("settings/paypalresponse");

		log.debug("Exiting redirectToResponse");
		return mav;
	}

	@RequestMapping(value = "/setexpresscheckout", method = RequestMethod.POST)
	public ModelAndView setexpress(
			@RequestParam(value="process") String process,
			@RequestParam(value="paymentType") String currency,
			Model model,
			HttpServletRequest request,
			HttpServletResponse resp
			) {
		log.debug("ENTERED setexpress");
		Properties properties = new Properties();
		try {
		    properties.load(this.getClass().getClassLoader().getResourceAsStream("paypal.properties"));
		    
		} catch (IOException e) {
			log.error(e);
		}			
		//String returnURL = url.toString() + "/settings/GetExpressCheckoutDetails";
		String returnURL = "";
		//String cancelURL = url.toString() + "/settings/paypalcancel";
		String cancelURL = "";
		log.debug("process: "+process);

		ModelAndView mav = new ModelAndView();
		mav.setViewName("settings");
		returnURL = properties.getProperty("paypal.return.url") + "/settings/GetExpressCheckoutDetails";
		cancelURL = properties.getProperty("paypal.cancel.url") + "/settings/paypalcancel";
		
		log.debug("THE RETURN URL : " + returnURL);
		log.debug("THE CANCEL URL : " + cancelURL);
		
		model.addAttribute("cancelmessage", "paypal cancel");
		// ************************************************************************
		String startDate = "";
		String endDate = "";
		String transactionID = "";

		StringBuffer url = new StringBuffer();
		url.append("http://");
		url.append(request.getServerName());
		url.append(":");
		url.append(request.getServerPort());
		url.append(request.getContextPath());
		log.debug("url: " + url.toString());

		String strNVPRequest = "";
		StringBuffer sbErrorMessages = new StringBuffer("");

		mav.setView(new RedirectView("settings"));	
		
		log.debug("trace 1");

		try {
			caller = new NVPCallerServices();
			log.debug("trace 2");
			APIProfile profile = ProfileFactory.createSignatureAPIProfile();
			log.debug("trace 3");
			/*
			 * WARNING: Do not embed plaintext credentials in your application
			 * code. Doing so is insecure and against best practices. Your API
			 * credentials must be handled securely. Please consider encrypting
			 * them for use in any production environment, and ensure that only
			 * authorized individuals may view or modify them.
			 */

			// Set up your API credentials, PayPal end point, API operation and
			// version.
			Properties payments_properties = new Properties();
			try {
				payments_properties.load(this.getClass().getClassLoader().getResourceAsStream("payments.properties"));
				log.debug("trace 4");
			} catch (IOException e) {
				log.error(e);
				log.debug("trace 5");
			}			
			log.debug("trace 6");
			profile.setAPIUsername(properties.getProperty("paypal.api.user.name"));
			log.debug("trace 7");
			profile.setAPIPassword(properties.getProperty("paypal.api.password"));
			profile.setSignature(properties.getProperty("paypal.signature"));
			profile.setEnvironment(properties.getProperty("paypal.environment"));
			profile.setSubject(properties.getProperty("paypal.subject"));
			log.debug("trace 8");
			caller.setAPIProfile(profile);
			log.debug("trace 9");
			encoder.add("METHOD", "SetExpressCheckout");
			log.debug("trace 10");
			String paymentAmount = "15";
			if(process.equalsIgnoreCase("Pay 1 Month")){
				paymentAmount = payments_properties.getProperty("one.month");
			}else if(process.equalsIgnoreCase("Pay 3 Months")){
				paymentAmount = payments_properties.getProperty("three.months");
			}else if(process.equalsIgnoreCase("Pay 6 Months")){
				paymentAmount = payments_properties.getProperty("six.months");
			}else if(process.equalsIgnoreCase("Pay 12 Months")){
				paymentAmount = payments_properties.getProperty("twelve.months");
			}else if(process.equalsIgnoreCase("Pay 24 Months")){
				paymentAmount = payments_properties.getProperty("twentyfour.months");
			}else if(process.equalsIgnoreCase("Pay 36 Months")){
				paymentAmount = payments_properties.getProperty("thirtysix.months");
			}
			String currencyCodeType = "USD";

			encoder.add("RETURNURL", returnURL + "?paymentAmount="
					+ paymentAmount + "&currencyCodeType=" + currencyCodeType);
			encoder.add("CANCELURL", cancelURL);

			// encoder.add("AMT",request.getParameter("paymentAmount"));
			String paymentType = "instant";
			encoder.add("PAYMENTACTION", paymentType);
			
			encoder.add("CURRENCYCODE",currencyCodeType);

			encoder.add("L_NAME0", "MONTHLY");
			encoder.add("L_NUMBER0", "1000");
			encoder.add("L_DESC0", "Month of service for scheduleem.com");
			encoder.add("L_AMT0", paymentAmount);
			encoder.add("L_QTY0", "1");
			encoder.add("ADDROVERRIDE", "1");

			float ft = Float.valueOf("1".trim())
					.floatValue()
					* Float.valueOf(paymentAmount.trim())
							.floatValue();

			encoder.add("ITEMAMT", String.valueOf(ft));
			float amt = Util.round(ft, 2);
			float maxamt = Util.round(amt + 25.00f, 2);
			encoder.add("AMT", String.valueOf(amt));
			encoder.add("MAXAMT", String.valueOf(maxamt));

			String testEnv = (String) properties.getProperty("paypal.environment");

			// encode method will encode the name and value and form NVP string
			// for the request
			strNVPRequest = encoder.encode();

			// call method will send the request to the server and return the
			// response NVPString
			String ppresponse = (String) caller.call(strNVPRequest);

			// decode method of NVPDecoder will parse the request and decode the
			// name and value pair
			decoder.decode(ppresponse);

			// checks for Acknowledgement and redirects accordingly to display
			// error messages
			String strAck = decoder.get("ACK");
			if (strAck != null
					&& !(strAck.equals("Success") || strAck
							.equals("SuccessWithWarning"))) {
				// display ERROR PAGE OR ERROR MESSAGE
			} else {
				// TODO: REDIRECT TO PAYPAL
				// https://www.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=EC-xxxtoken-herexxx&useraction=commit
				log.debug("response from initial query to get TOKEN with decoder to string: "+decoder.toString());
				mav.setView(new RedirectView("myschedule"));
				mav.setView(new RedirectView("settings"));	
				
				String paypalurl = "";
				if(testEnv.equalsIgnoreCase("sandbox")){
					paypalurl = "https://www."+testEnv+".paypal.com/cgi-bin/webscr?cmd=_express-checkout&token="+decoder.get("TOKEN")+"&useraction=commit";	
				}else{
					paypalurl = "https://www.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token="+decoder.get("TOKEN")+"&useraction=commit";
				}
				
				mav.setView(new RedirectView(paypalurl));

			}

		} catch (Exception ex) {
			ex.printStackTrace();
			log.error(ex);
			mav.setView(new RedirectView("settings"));			
		}
		log.debug(decoder.get("ACK"));

		log.debug("EXITING setExpressCheckout");		
		// *********************************************************************
		log.debug("EXITING setexpress");
		return mav;
	}
	@RequestMapping(value = "/smssetexpresscheckout", method = RequestMethod.POST)
	public ModelAndView setExpressSms(
			@RequestParam(value="smsprocess") String process,@RequestParam(value="paymentType") String currency,Model model,HttpServletRequest request) {
		log.debug("ENTERED setExpressSms");
		Properties properties = new Properties();
		try {
		    properties.load(this.getClass().getClassLoader().getResourceAsStream("paypal.properties"));
		    
		} catch (IOException e) {
			log.error(e);
		}			
		//String returnURL = url.toString() + "/settings/GetExpressCheckoutDetails";
		String returnURL = "";
		//String cancelURL = url.toString() + "/settings/paypalcancel";
		String cancelURL = "";
		log.debug("smsprocess: "+process);

		ModelAndView mav = new ModelAndView();
		mav.setViewName("settings");
		returnURL = properties.getProperty("paypal.return.url") + "/settings/GetExpressCheckoutDetails";
		cancelURL = properties.getProperty("paypal.cancel.url") + "/settings/paypalcancel";
		
		log.debug("THE RETURN URL : " + returnURL);
		log.debug("THE CANCEL URL : " + cancelURL);
		
		model.addAttribute("cancelmessage", "paypal cancel");
		// ************************************************************************
		String startDate = "";
		String endDate = "";
		String transactionID = "";

		StringBuffer url = new StringBuffer();
		url.append("http://");
		url.append(request.getServerName());
		url.append(":");
		url.append(request.getServerPort());
		url.append(request.getContextPath());
		

		String strNVPRequest = "";
		StringBuffer sbErrorMessages = new StringBuffer("");

		mav.setView(new RedirectView("settings"));	
		
		

		try {
			caller = new NVPCallerServices();
			APIProfile profile = ProfileFactory.createSignatureAPIProfile();
			/*
			 * WARNING: Do not embed plaintext credentials in your application
			 * code. Doing so is insecure and against best practices. Your API
			 * credentials must be handled securely. Please consider encrypting
			 * them for use in any production environment, and ensure that only
			 * authorized individuals may view or modify them.
			 */

			// Set up your API credentials, PayPal end point, API operation and
			// version.
			Properties payments_properties = new Properties();
			try {
				payments_properties.load(this.getClass().getClassLoader().getResourceAsStream("payments.properties"));
			    
			} catch (IOException e) {
				log.error(e);
			}			
			
			profile.setAPIUsername(properties.getProperty("paypal.api.user.name"));
			profile.setAPIPassword(properties.getProperty("paypal.api.password"));
			profile.setSignature(properties.getProperty("paypal.signature"));
			profile.setEnvironment(properties.getProperty("paypal.environment"));
			profile.setSubject(properties.getProperty("paypal.subject"));
			caller.setAPIProfile(profile);
			encoder.add("METHOD", "SetExpressCheckout");

			String paymentAmount = "15";
			if(process.equalsIgnoreCase("1000 messages")){
				paymentAmount = payments_properties.getProperty("sms.one");
			}else if(process.equalsIgnoreCase("2000 messages")){
				paymentAmount = payments_properties.getProperty("sms.two");
			}
			log.debug("paymentAmount: "+paymentAmount);
			String currencyCodeType = "USD";

			encoder.add("RETURNURL", returnURL + "?paymentAmount=" + paymentAmount + "&currencyCodeType=" + currencyCodeType);
			encoder.add("CANCELURL", cancelURL);

			// encoder.add("AMT",request.getParameter("paymentAmount"));
			String paymentType = "instant";
			encoder.add("PAYMENTACTION", paymentType);
			
			encoder.add("CURRENCYCODE",currencyCodeType);

			encoder.add("L_NAME0", "SMS_MESSAGES");
			encoder.add("L_NUMBER0", "5050");
			encoder.add("L_DESC0", "Batch of text messages used for scheduleem.com");
			encoder.add("L_AMT0", paymentAmount);
			encoder.add("L_QTY0", "1");
			encoder.add("ADDROVERRIDE", "1");

			float ft = Float.valueOf("1".trim())
					.floatValue()
					* Float.valueOf(paymentAmount.trim())
							.floatValue();

			encoder.add("ITEMAMT", String.valueOf(ft));
			float amt = Util.round(ft, 2);
			float maxamt = Util.round(amt + 25.00f, 2);
			encoder.add("AMT", String.valueOf(amt));
			encoder.add("MAXAMT", String.valueOf(maxamt));

			String testEnv = (String) properties.getProperty("paypal.environment");

			// encode method will encode the name and value and form NVP string
			// for the request
			strNVPRequest = encoder.encode();

			// call method will send the request to the server and return the
			// response NVPString
			String ppresponse = (String) caller.call(strNVPRequest);

			// decode method of NVPDecoder will parse the request and decode the
			// name and value pair
			decoder.decode(ppresponse);

			// checks for Acknowledgement and redirects accordingly to display
			// error messages
			String strAck = decoder.get("ACK");
			if (strAck != null
					&& !(strAck.equals("Success") || strAck
							.equals("SuccessWithWarning"))) {
				// display ERROR PAGE OR ERROR MESSAGE
			} else {
				// REDIRECT TO PAYPAL
				// https://www.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=EC-xxxtoken-herexxx&useraction=commit
				log.debug("response from initial query to get TOKEN with decoder to string: "+decoder.toString());
//				mav.setView(new RedirectView("myschedule"));
				mav.setView(new RedirectView("settings"));	
				
				String paypalurl = "";
				if(testEnv.equalsIgnoreCase("sandbox")){
					paypalurl = "https://www."+testEnv+".paypal.com/cgi-bin/webscr?cmd=_express-checkout&token="+decoder.get("TOKEN")+"&useraction=commit";	
				}else{
					paypalurl = "https://www.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token="+decoder.get("TOKEN")+"&useraction=commit";
				}
				
				mav.setView(new RedirectView(paypalurl));

			}

		} catch (Exception ex) {
			ex.printStackTrace();
			log.error(ex);
			mav.setView(new RedirectView("settings"));			
		}
		log.debug(decoder.get("ACK"));

		// *********************************************************************
		log.debug("EXITING setExpressSms");
		return mav;
	}
	@RequestMapping(method = RequestMethod.GET)
    public ModelAndView getCreateForm(Model model) {
		log.debug("MYSETTINGS CONTROLLER DISPLAYING MODEL AND VIEW");
		
		ModelAndView mav = new ModelAndView();
		mav.setViewName("settings");
		Long number_sms_messages = 666L;
		Long number_sms_sent = 555L;
		Date sms_last_purchased_date  = new Date();
		
    	Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (obj instanceof UserDetails) {
			String username = ((UserDetails) obj).getUsername();
			if (!username.equalsIgnoreCase("anonymous")) {
				log.info("LOGGED IN USER: " + username);
				// lookup the shopsetting based on uid
				TypedQuery<Shop> shop = Shop.findShopsByShopuuid(username);
				log.debug("the returned results for looking for "+ username + " is a size of: " + shop.getResultList().size());
				if(shop.getResultList().size() > 0){
					log.debug("entered greater than zero");
					Shop shop_this = shop.getResultList().get(0);
					if(shop_this.getNumber_sms_purchased() != null){
						number_sms_messages = shop_this.getNumber_sms_purchased();
					}else{
						log.debug("shop_this.getNumber_sms_purchased() is null");
					}
					if(shop_this.getNumber_sms_sent() != null){
						number_sms_sent = shop_this.getNumber_sms_sent();
					}else{
						log.debug("getNumber_sms_sent() is null");
					}
					if(shop_this.getSms_last_purchase_date() != null){
						sms_last_purchased_date = shop_this.getSms_last_purchase_date();
					}else{
						log.debug("shop_this.getNumber_sms_purchased() is null");
					}
					TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(shop_this);
					ShopSettings settings = shopsettings.getResultList().get(0);
					
					Settings setting = new Settings(settings);
					
					model.addAttribute("shopSettings", setting);
				}else{
					log.debug("entered not greater than zero");
					// add ATTRIBUTE SERVICES AFTER RETRIEVING FROM DATABASE
					ShopSettings settings = new ShopSettings();
					model.addAttribute("shopSettings", settings);
				}
			}
		}
		model.addAttribute("number_of_sms_messages_available",number_sms_messages);
		model.addAttribute("number_sms_sent",number_sms_sent);
		model.addAttribute("sms_last_purchased_date",sms_last_purchased_date);
		return mav;
        
    }
	/**
	 * Note if you get this error:
	 * 
	 * Errors/BindingResult argument declared without preceding model attribute. Check your handler method signature!
	 * 
	 * then according to this article:
	 * 
	 * http://loongest.com/spring-mvc/errorsbindingresult-argument-declared-without-preceding-model-attribute-check-your-handler-method-signature/
	 * 
	 *  You need to place the
	 *  
	 *  BindingResult bindingResult,
	 *  
	 *  immediately after
	 *  
	 *  @Valid @ModelAttribute ShopSettings
	 *  
	 *   
	 * @param requestedWith
	 * @param shopSettings
	 * @param bindingResult
	 * @param actionPath
	 * @param httpServletRequest
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/update", method = RequestMethod.PUT)
	public ModelAndView submitChange (
								@RequestHeader(value="X-Requested-With", required=false) String requestedWith,
								@Valid @ModelAttribute Settings shopSettings,
								BindingResult bindingResult,
						        @RequestParam(value="submitType", required=false) String actionPath,
						        @RequestParam(value="upload", required=false) String upload,
						        @RequestParam(value="appts", required=false) String appts,
						        @RequestParam(value="changephone", required=false) String changephone,
						        @RequestParam(value="pw", required=false) String pw,
						        @RequestParam(value="changeemail", required=false) String changeemail,
						        HttpServletRequest httpServletRequest,
						        Model model
						        ) {
		log.debug("the value of upload: "+upload);
		log.debug("the value of appts: "+appts);
		log.debug("the value of changephone: "+changephone);
		log.debug("the value of pw: "+pw);
		log.debug("the value of changeemail: "+changeemail);
		
		TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
		ShopSettings shopsetting = shopsettings.getResultList().get(0);
		
		Settings settings = new Settings(shopsetting);
		
		model.addAttribute("shopSettings", settings);
		

		ModelAndView mav = new ModelAndView();
		mav.setView(new RedirectView("../settings"));
        if (bindingResult.hasErrors()) {
            model.addAttribute("shopSettings", shopSettings);
            return mav;
        }
		
		log.debug("settings subject: " + shopSettings.getEmail_subject());
		log.debug("settings message: " + shopSettings.getEmail_message());
		log.debug("settings signature: " + shopSettings.getEmail_signature());
		log.debug("settings username: " + shopSettings.getUsername());
		log.debug("settings phone: " + shopSettings.getStore_phone());
		log.debug("settings appt length: " + shopSettings.getDefault_appt_length());
		log.debug("settings confirm: " + shopSettings.isReceiveConfirmations());
		log.debug("settings display: " + shopSettings.getClientdisplay());
		log.debug("settings logo: " + shopSettings.getImage_file_logo());
		log.debug("shopsettings tostring: " + shopSettings.toString());
		// email
		if(changeemail != null){
			shopsetting.setEmail_subject(shopSettings.getEmail_subject());
			shopsetting.setEmail_message(shopSettings.getEmail_message());
			shopsetting.setEmail_signature(shopSettings.getEmail_signature());
			// adding audit log
			saveAuditMessage("Updated settings email",null,"GENERAL");
			
		}
		// change password
		if(pw != null){
			shopsetting.setUser_password(shopSettings.getUser_password());
			// adding audit log
			saveAuditMessage("Updated settings password",null,"GENERAL");
			
		}
		//  change phone
		if(changephone != null){
			shopsetting.setStore_phone(shopSettings.getStore_phone());
			// adding audit log
			saveAuditMessage("Updated settings change phone",null,"GENERAL");
		}
		// appts
		if(appts != null){
			shopsetting.setDefault_appt_length(shopSettings.getDefault_appt_length());
			shopsetting.setReceiveConfirmations(shopSettings.isReceiveConfirmations());
			shopsetting.setClientdisplay(shopSettings.getClientdisplay());
			// adding audit log
			saveAuditMessage("Updated settings appointments",null,"GENERAL");
			
		}
		// upload
		if(upload != null){
			shopsetting.setImage_file_logo(shopSettings.getImage_file_logo());
			// adding audit log
			saveAuditMessage("Updated settings logo",null,"GENERAL");
		}
		
		shopsetting.persist();
		
		return mav;
	}
	// AJAX BELOW
	/**
	 * reminder email sent out x number of days prior
	 * 
	 * @param subject
	 * @param message
	 * @param signature
	 * @param shopemail
	 * @return
	 */
	@RequestMapping(value="/messages", method=RequestMethod.POST)
	public @ResponseBody  String updateMessages(
			@RequestParam(value="s") String subject,
			@RequestParam(value="m") String message,
			@RequestParam(value="sig") String signature,
			@RequestParam(value="se") String shopemail
		) {
		log.debug("ENTERED updateMessages");
		String RESULTS = "SUCCESS";
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shopsetting.setEmail_subject(subject);
			shopsetting.setEmail_big_message(message);
			shopsetting.setEmail_signature(signature);
			shopsetting.setEmail_address(shopemail);
			shopsetting.merge();
			// adding audit log
			saveAuditMessage("Updated settings messages",null,"GENERAL");
			
		}catch(Exception e){
			log.error(e);
			RESULTS = "FAILURE";
		}

		log.debug("EXITING updateMessages");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}	
	/**
	 * receipt email sent when checkout
	 * 
	 * @param subject
	 * @param message
	 * @param signature
	 * @param shopemail
	 * @return
	 */
	@RequestMapping(value="/rmessages", method=RequestMethod.POST)
	public @ResponseBody  String updateRMessages(
			@RequestParam(value="s") String subject,
			@RequestParam(value="m") String message,
			@RequestParam(value="sig") String signature,
			@RequestParam(value="se") String shopemail
		) {
		log.debug("ENTERED updateRMessages");
		String RESULTS = "SUCCESS";
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shopsetting.setRemail_subject(subject);
			shopsetting.setRemail_message(message);
			shopsetting.setRemail_signature(signature);
			shopsetting.setRemail_address(shopemail);
			shopsetting.merge();
			// adding audit log
			saveAuditMessage("Updated settings messages",null,"GENERAL");
			
		}catch(Exception e){
			log.error(e);
			RESULTS = "FAILURE";
		}

		log.debug("EXITING updateRMessages");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}	
	/**
	 * initial message to be sent when appointment is made
	 * 
	 * @param subject
	 * @param message
	 * @param signature
	 * @param shopemail
	 * @return
	 */
	@RequestMapping(value="/imessages", method=RequestMethod.POST)
	public @ResponseBody  String updateIMessages(
			@RequestParam(value="s") String subject,
			@RequestParam(value="m") String message,
			@RequestParam(value="sig") String signature,
			@RequestParam(value="se") String shopemail
		) {
		log.debug("ENTERED updateIMessages");
		String RESULTS = "SUCCESS";
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shopsetting.setIemail_subject(subject);
			shopsetting.setIemail_message(message);
			shopsetting.setIemail_signature(signature);
			shopsetting.setIemail_address(shopemail);
			shopsetting.merge();
			// adding audit log
			saveAuditMessage("Updated settings messages",null,"GENERAL");
			
		}catch(Exception e){
			log.error(e);
			RESULTS = "FAILURE";
		}

		log.debug("EXITING updateIMessages");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}	
	// SMS messages begin
	/**
	 * reminder sms sent out x number of days prior
	 * 
	 * @param message
	 * @return
	 */
	@RequestMapping(value="/smsmessages", method=RequestMethod.POST)
	public @ResponseBody  String updateSMSMessages(
			@RequestParam(value="m") String message
		) {
		log.debug("ENTERED updateSMSMessages");
		log.debug("message: " + message);
		String RESULTS = "SUCCESS";
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);

			// replace with sms message
			shopsetting.setSms_message(message);
			
			shopsetting.merge();
			// adding audit log
			saveAuditMessage("Updated reminder sms settings messages",null,"GENERAL");
			
		}catch(Exception e){
			log.error(e);
			RESULTS = "FAILURE";
		}

		log.debug("EXITING updateSMSMessages");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}	
	/**
	 * receipt email sent when checkout
	 * 
	 * @param subject
	 * @param message
	 * @param signature
	 * @param shopemail
	 * @return
	 */
	@RequestMapping(value="/rsmsmessages", method=RequestMethod.POST)
	public @ResponseBody  String updateSMSRMessages(
			@RequestParam(value="m") String message
		) {
		log.debug("ENTERED updateSMSRMessages");
		log.debug("message: " + message);
		String RESULTS = "SUCCESS";
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			
			//  replace with sms message
			shopsetting.setRsms_message(message);
			
			shopsetting.merge();
			// adding audit log
			saveAuditMessage("Updated receipt sms settings messages",null,"GENERAL");
			
		}catch(Exception e){
			log.error(e);
			RESULTS = "FAILURE";
		}

		log.debug("EXITING updateSMSRMessages");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}	
	/**
	 * initial sms message to be sent when appointment is made
	 * 
	 * @param subject
	 * @param message
	 * @param signature
	 * @param shopemail
	 * @return
	 */
	@RequestMapping(value="/ismsmessages", method=RequestMethod.POST)
	public @ResponseBody  String updateSMSIMessages(
			@RequestParam(value="m") String message
		) {
		log.debug("ENTERED updateSMSIMessages");
		log.debug("message: " + message);
		String RESULTS = "SUCCESS";
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			
			// replace with sms message
			shopsetting.setIsms_message(message);
			shopsetting.persist();
//			shopsetting.merge();
			// adding audit log
			saveAuditMessage("Updated initial sms settings messages",null,"GENERAL");
			
		}catch(Exception e){
			log.error(e);
			RESULTS = "FAILURE";
		}

		log.debug("EXITING updateSMSIMessages");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}		
	// SMS message end
	
	@RequestMapping(value="/gcallist", method=RequestMethod.GET)
	public @ResponseBody  String listGCal(
			@RequestParam(value="u") String username,
			@RequestParam(value="p") String password
			
			){
		log.debug("ENTERED listGCal");
		String response = " ";
		String jsonarray = "{ items: ";
	    CalendarService myService = new CalendarService("exampleCo-exampleApp-1");
	
//	    String userName = "timjeffcoat@gmail.com";
//	    String userPassword = "9google9";
	    String userName = username;
	    String userPassword = password;
	
	    // Create the necessary URL objects.
	    try {
	      metafeedUrl = new URL(METAFEED_OWN_URL_BASE );
	      eventFeedUrl = new URL(METAFEED_OWN_URL_BASE + userName
	          + EVENT_FEED_URL_SUFFIX);
	    } catch (MalformedURLException e) {
	      // Bad URL
	      log.error("Uh oh - you've got an invalid URL.");
	      log.error(e);
	      response = "Uh oh - you've got an invalid URL.";
	    }
	    JSONArray json_arr=new JSONArray();
	    List<GCalForm> gcallist = new ArrayList<GCalForm>();
	    try {
	      myService.setUserCredentials(userName, userPassword);
	
	      // Demonstrate retrieving a list of the user's calendars.
	      // Send the request and receive the response:
	      CalendarFeed resultFeed = myService.getFeed(metafeedUrl, CalendarFeed.class);
	
	      log.debug("Your calendars:");
	      log.debug("");
	      String totalpieces = "";
	      for (int i = 0; i < resultFeed.getEntries().size(); i++) {
	    	GCalForm form = new GCalForm();
	        CalendarEntry entry = resultFeed.getEntries().get(i);
	        String title = entry.getTitle().getPlainText();
	        String calid = entry.getId();
	        // TODO:  parse the calid to get the actual id on the far right of the http string that is returned here
	        int last_slash = calid.lastIndexOf("/");
	        calid = calid.substring(last_slash+1);
	        log.debug("i: " + i + " calid: " + calid + " title: "+ title);
	        form.setId(""+i);
	        form.setGcal_id(calid);
	        form.setGcal_title(title);
	        gcallist.add(form);
	      }

	      log.debug("json_arr: " + json_arr.toString());
	    }catch(Exception e){
	    	log.error(e);
	    }
	    String jsonarray2 = new JSONSerializer().exclude("*.class").serialize(gcallist);
	    log.debug("EXITING listGCal");
		return jsonarray2;
	}	

		
	/**
	 * ajax call for setting google calendar settings
	 * 			
	 * @param usegcal
	 * @param allowgcal
	 * @param username
	 * @param password
	 * @param calendarname
	 * @param calendartitle
	 * @return
	 */
	@RequestMapping(value="/gcal", method=RequestMethod.GET)
	public @ResponseBody  String updateGCal(
			@RequestParam(value="u") boolean usegcal,
			@RequestParam(value="a") boolean allowgcal,
			@RequestParam(value="ur") String username,
			@RequestParam(value="p") String password,
			@RequestParam(value="c") String calendarname,
			@RequestParam(value="t") String calendartitle
		) {
		log.debug("ENTERED updateGCal");
		String RESULTS = "SUCCESS";
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shopsetting.setUse_gcalendar_for_shop(usegcal);
			shopsetting.setAllow_staff_gcalendar(allowgcal);
			shopsetting.merge();
			if(usegcal && username != null && password != null && calendarname != null && !username.equalsIgnoreCase("") && !password.equalsIgnoreCase("") && !calendarname.equalsIgnoreCase("") ){
				// since google calendar is enabled save the username, password, and calendarname to GCal
				log.debug("calendarname: "+calendarname);
				log.debug("getShop().getShopuuid(): "+getShop().getShopuuid());
				TypedQuery<GCal> orginal_gals = GCal.findGCalsByShopEqualsAndStaffIsNull(getShop());
				GCal orig_gal = null; 
				if(orginal_gals.getResultList().size() > 0){
					orig_gal = orginal_gals.getResultList().get(0);
					String orig_calname = orig_gal.getCalendarnameid();
					TypedQuery<GCal> gals = GCal.findGCalsByShopEqualsAndStaffIsNullAndCalendarnameidEquals(getShop(), orig_calname);
					if(gals.getResultList().size() > 0){
						log.debug("gals.getResultList().size() > 0");	
						GCal gal = gals.getResultList().get(0);
						if(gal != null){
							log.debug("ABOUT TO UPDATE EXISTING GCAL");
							gal.setUsername(username);
							gal.setPassword(password);
							gal.setCalendarnameid(calendarname);
							gal.setGcal_title(calendartitle);
							gal.merge();
						}
					}					
				}else{
					log.debug("ABOUT TO CREATE NEW GCAL");
					GCal ga = new GCal();
					ga.setShop(getShop());
					ga.setUsername(username);
					ga.setPassword(password);
					ga.setCalendarnameid(calendarname);
					ga.setGcal_title(calendartitle);
					ga.merge();
				}
				// adding audit log
				saveAuditMessage("Updated settings Allow_staff_gcalendar to " + usegcal,null,"GENERAL");
			}
			
		}catch(Exception e){
			log.error(e);
			RESULTS = "FAILURE";
		}

		log.debug("EXITING updateGCal");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}	

	private boolean updateLdapPw(String staffid, String password){
		boolean rtn = true;
		log.debug("ENTERED updateLdapPw");
		log.debug("staffid: "+staffid);
		
		if(staffid == null || staffid.equalsIgnoreCase("")) return false;
		
		Person person = new Person();
		person.setUid(staffid);
		person.setUserPassword(password);
		log.debug("staffid : " + staffid);
		try{
			getPersonDao().changePassword(person);
		}catch(Exception e){
			log.error("EXCEPTION TRYING TO CREATE CUSTOMER CUSTOMER");
			log.error(e);
			rtn = false;
		}
		log.debug("EXITING updateLdapPw");
		return rtn; 
	}
	
	@RequestMapping(value="/password", method=RequestMethod.GET)
	public @ResponseBody  String updatePassword(
			@RequestParam(value="p") String password
		) {
		log.debug("ENTERED updatePassword");
		String RESULTS = "SUCCESS";
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			
			shopsetting.setUser_password(password);
			shopsetting.merge();
			// update the ldap with this new password
			updateLdapPw(getIndividualusername(),password);
			// adding audit log
			saveAuditMessage("Updated settings password",null,"GENERAL");
			
		}catch(Exception e){
			log.error(e);
			RESULTS = "FAILURE";
		}

		log.debug("EXITING updatePassword");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}	
	
	@RequestMapping(value="/storephone", method=RequestMethod.GET)
	public @ResponseBody  String updateStorephone(
			@RequestParam(value="p") String phone
		) {
		log.debug("ENTERED updateStorephone");
		String RESULTS = "SUCCESS";
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shopsetting.setStore_phone(phone);
			shopsetting.merge();
			// adding audit log
			saveAuditMessage("Updated settings store phone",null,"GENERAL");
			
		}catch(Exception e){
			log.error(e);
			RESULTS = "FAILURE";
		}
		log.debug("EXITING updateStorephone");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}	
	
	@RequestMapping(value="/isnamedavailable", method=RequestMethod.GET)
    public @ResponseBody  String isNameAvailableJSON(@RequestParam(value="name",required=true) String name) {
		log.debug("Entering isNameAvailableJSON");
		
		TypedQuery<Shop> comparename = Shop.findShopsByShop_name(name);
		int num = comparename.getResultList().size();
		log.debug("comparename: "+comparename);
		log.debug("name: "+name);
		boolean thesame = false;
		if(num == 0){
			log.debug("entered first");
			thesame = false;
		}else{
			log.debug("entered second. the number of matching shop names is: " + num);
			thesame = true;
			thesame = isNameAvailable(name);
		}
		// now check to see if the name is available for the shop url
		
		
		String jsonarray2 = new JSONSerializer().exclude("*.class").serialize(thesame);
		log.debug("Exiting isNameAvailableJSON");
        return jsonarray2;
    }
	
	@RequestMapping(value="/appointment", method=RequestMethod.GET)
	public @ResponseBody  String updateAppointment(
			@RequestParam(value="al") String length,
			@RequestParam(value="cd") String display,
			@RequestParam(value="rc") String confirmation,
			@RequestParam(value="dn") String daysnotify
		) {
		log.debug("ENTERED updateAppointment");
		String RESULTS = "SUCCESS";
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shopsetting.setDefault_appt_length(length);
			if(daysnotify != null && !daysnotify.equalsIgnoreCase("")){
				try{
					Integer numberdays=Integer.parseInt(daysnotify);
					shopsetting.setNumber_days_notify(numberdays);	
				}catch(Exception e){
					log.error(e);
				}
				
			}
			log.debug("display: "+display);
			log.debug("ClientDisplay.FIRSTNAME: "+ClientDisplay.FIRSTNAME);
			if(display.equalsIgnoreCase(""+ClientDisplay.FIRSTNAME)){
				shopsetting.setClientdisplay(ClientDisplay.FIRSTNAME);
			}else{
				shopsetting.setClientdisplay(ClientDisplay.LASTNAME);
			}
			log.debug("confirmation: "+confirmation);
			if(confirmation.equalsIgnoreCase("on")){
				shopsetting.setReceiveConfirmations(true);
			}else{
				shopsetting.setReceiveConfirmations(false);
			}
			shopsetting.merge();
			// adding audit log
			saveAuditMessage("Updated settings appointments",null,"GENERAL");
			
		}catch(Exception e){
			log.error(e);
			RESULTS = "FAILURE";
		}

		log.debug("EXITING updateAppointment");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}
	
	@RequestMapping(value="/isurlavailable", method=RequestMethod.GET)
    public @ResponseBody  String isURLAvailableJSON(@RequestParam(value="url",required=true) String url) {
		log.debug("Entering isURLAvailableJSON");
		
		TypedQuery<Shop> comparename = Shop.findShopsByShop_urlEquals(url);
		int num = comparename.getResultList().size();
		log.debug("number of results for the url: "+num);
		log.debug("url: "+url);
		boolean isavail = false;
		if(num == 0){
			log.debug("entered first");
			isavail = true;
		}else{
			log.debug("entered second. the number of matching shop names is: " + num);
			isavail = false;
		}
		
		
		String jsonarray2 = new JSONSerializer().exclude("*.class").serialize(isavail);
		log.debug("Exiting isURLAvailableJSON");
        return jsonarray2;
    }
	
    private boolean isNameAvailable(String name) {
		log.debug("Entering isNameAvailable");
		
		TypedQuery<Shop> comparename = Shop.findShopsByShop_urlEquals(name);
		int num = comparename.getResultList().size();
		log.debug("comparename: "+comparename);
		log.debug("name: "+name);
		boolean isavail = false;
		if(num == 0){
			log.debug("entered first");
			isavail = true;
		}else{
			log.debug("entered second. the number of matching shop names is: " + num);
			isavail = false;
		}
		log.debug("Exiting isNameAvailable");
        return isavail;
    }
	
	@RequestMapping(value="/shopname", method=RequestMethod.GET)
	public @ResponseBody  String updateshopname(
			@RequestParam(value="n") String shopname
		) {
		log.debug("ENTERED updateshopname");
		String RESULTS = "SUCCESS";
		
		TypedQuery<Shop> comparename = Shop.findShopsByShop_name(shopname);
		int num = comparename.getResultList().size();
		boolean thesame = false;
		if(num == 0){
			log.debug("entered first");
			thesame = false;
			try{
				getShop().setShop_name(shopname);
				getShop().merge();
			}catch(Exception e){
				log.error(e);
				RESULTS = "FAILURE";
			}
			
		}else{
			RESULTS = "FAILURE";
			log.debug("entered second. the number of matching shop names is: " + num);
			thesame = true;
		}
		

		log.debug("EXITING updateshopname");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}	

	@RequestMapping(value="/updateshopname", method=RequestMethod.POST)
	public ModelAndView updatemobileshopname(
			@RequestParam(value="shopname") String shopname
		) {
		log.debug("ENTERED updatemobileshopname");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("settings/msettings");
		TypedQuery<Shop> comparename = Shop.findShopsByShop_name(shopname);
		int num = comparename.getResultList().size();
		boolean thesame = false;
		if(num == 0){
			log.debug("entered first");
			thesame = false;
			try{
				getShop().setShop_name(shopname);
				getShop().merge();
				// adding audit log
				saveAuditMessage("Updated settings shop name",null,"GENERAL");
				
			}catch(Exception e){
				log.error(e);
			}
			
		}else{
			log.debug("entered second. the number of matching shop names is: " + num);
			thesame = true;
		}
		

		log.debug("EXITING updatemobileshopname");
		return mav;
	}	
	
	@RequestMapping(value="/updateshopurl", method=RequestMethod.GET)
	public @ResponseBody  String updateshopurl(
			@RequestParam(value="n") String shopurl
		) {
		log.debug("ENTERED updateshopurl");
		String RESULTS = "SUCCESS";
		
		TypedQuery<Shop> comparename = Shop.findShopsByShop_urlEquals(shopurl);
		int num = comparename.getResultList().size();
		boolean thesame = false;
		if(num == 0){
			log.debug("entered first");
			thesame = false;
			try{
				getShop().setShop_url(shopurl);
				getShop().merge();
				// adding audit log
				saveAuditMessage("Updated settings shop url",null,"GENERAL");
				
			}catch(Exception e){
				log.error(e);
				RESULTS = "FAILURE";
			}
			
		}else{
			RESULTS = "FAILURE";
			log.debug("entered second. the number of matching shop url is: " + num);
			thesame = true;
		}
		

		log.debug("EXITING updateshopurl");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}	

	@RequestMapping(value="/updateshopurl", method=RequestMethod.POST)
	public ModelAndView updatemobileshopurl(
			@RequestParam(value="shopurl") String shopurl
		) {
		log.debug("ENTERED updatemobileshopurl");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("settings/msettings");
		
		TypedQuery<Shop> comparename = Shop.findShopsByShop_urlEquals(shopurl);
		int num = comparename.getResultList().size();
		boolean thesame = false;
		if(num == 0){
			log.debug("entered first");
			thesame = false;
			try{
				getShop().setShop_url(shopurl);
				getShop().merge();
				// adding audit log
				saveAuditMessage("Updated settings shop url",null,"GENERAL");
				
			}catch(Exception e){
				log.error(e);
			}
			
		}else{
			log.debug("entered second. the number of matching shop url is: " + num);
			thesame = true;
		}
		

		log.debug("EXITING updatemobileshopurl");
		return mav;
	}	
	
	@RequestMapping(value="/updateshopphone", method=RequestMethod.POST)
	public ModelAndView updatemobileshopphone(
			@RequestParam(value="shopphone") String shopphone
		) {
		log.debug("ENTERED updatemobileshopphone");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("settings/msettings");
		
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shopsetting.setStore_phone(shopphone);
			shopsetting.merge();
			// adding audit log
			saveAuditMessage("Updated settings shop phone",null,"GENERAL");
			
		}catch(Exception e){
			log.error(e);
		}
		

		log.debug("EXITING updatemobileshopphone");
		return mav;
	}	

	
	@RequestMapping(value="/updatetz", method=RequestMethod.GET)
	public @ResponseBody  String updateshoptz(
			@RequestParam(value="tz") String shoptz
		) {
		log.debug("ENTERED updateshoptz");
		String RESULTS = "SUCCESS";
		
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shopsetting.setTimezone(shoptz);
			shopsetting.merge();
			// adding audit log
			saveAuditMessage("Updated settings shop time zone",null,"GENERAL");
			
		}catch(Exception e){
			log.error(e);
			RESULTS = "FAILURE";
		}
			

		log.debug("EXITING updateshoptz");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}	

	@RequestMapping(value="/updatetz", method=RequestMethod.POST)
	public ModelAndView updatemobileshoptz(
			@RequestParam(value="shoptimezone") String shoptz
		) {
		log.debug("ENTERED updatemobileshoptz");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("settings/msettings");
		
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shopsetting.setTimezone(shoptz);
			shopsetting.merge();
			// adding audit log
			saveAuditMessage("Updated settings shop time zone",null,"GENERAL");
		}catch(Exception e){
			log.error(e);
		}
			

		log.debug("EXITING updatemobileshoptz");
		return mav;
	}	

	@RequestMapping(value="/updateappsettings", method=RequestMethod.POST)
	public ModelAndView updatemobileApptSettings(
			@RequestParam(value="default_appt_length") String default_appt_length,
			@RequestParam(value="default_number_of_days_prior") String default_number_of_days_prior,
			@RequestParam(value="receiveConfirmations") boolean receiveConfirmations,
			@RequestParam(value="clientdisplay") String clientdisplay
		) {
		log.debug("ENTERED updatemobileApptSettings");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("settings/msettings");
		log.debug("default_appt_length: " + default_appt_length);
		log.debug("default_number_of_days_prior: " + default_number_of_days_prior);
		log.debug("receiveConfirmations: " + receiveConfirmations);
		log.debug("clientdisplay: " + clientdisplay);
		
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shopsetting.setDefault_appt_length(default_appt_length);
			
			shopsetting.setNumber_days_notify(Integer.parseInt(default_number_of_days_prior));
			shopsetting.setReceiveConfirmations(receiveConfirmations);
			
			shopsetting.setClientdisplay( ClientDisplay.valueOf(clientdisplay));
			shopsetting.merge();
			// adding audit log
			saveAuditMessage("Updated settings shop appointments",null,"GENERAL");
		}catch(Exception e){
			log.error(e);
		}
			

		log.debug("EXITING updatemobileApptSettings");
		return mav;
	}	

	@RequestMapping(value="/updateemailtemplate", method=RequestMethod.POST)
	public ModelAndView updateMobileEmailTemplate(
			@RequestParam(value="emailaddress") String emailaddress,
			@RequestParam(value="email_subject") String email_subject,
			@RequestParam(value="email_message") String email_message,
			@RequestParam(value="email_signature") String email_signature
		) {
		log.debug("ENTERED updateMobileEmailTemplate");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("settings/msettings");
		
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shopsetting.setEmail_address(emailaddress);
			shopsetting.setEmail_subject(email_subject);
			shopsetting.setEmail_message(email_message);
			shopsetting.setEmail_signature(email_signature);
			shopsetting.merge();
			// adding audit log
			saveAuditMessage("Updated settings shop email template",null,"GENERAL");
		}catch(Exception e){
			log.error(e);
		}
			

		log.debug("EXITING updateMobileEmailTemplate");
		return mav;
	}	
	
	/**
	 * mobile receipt email template
	 * 
	 * @param emailaddress
	 * @param email_subject
	 * @param email_message
	 * @param email_signature
	 * @return
	 */
	@RequestMapping(value="/updateremailtemplate", method=RequestMethod.POST)
	public ModelAndView updateMobileREmailTemplate(
			@RequestParam(value="remailaddress") String emailaddress,
			@RequestParam(value="remail_subject") String email_subject,
			@RequestParam(value="remail_message") String email_message,
			@RequestParam(value="remail_signature") String email_signature
		) {
		log.debug("ENTERED updateMobileREmailTemplate");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("settings/msettings");
		
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shopsetting.setRemail_address(emailaddress);
			shopsetting.setRemail_subject(email_subject);
			shopsetting.setRemail_message(email_message);
			shopsetting.setRemail_signature(email_signature);
			shopsetting.merge();
			// adding audit log
			saveAuditMessage("Updated settings shop email template",null,"GENERAL");
		}catch(Exception e){
			log.error(e);
		}
			

		log.debug("EXITING updateMobileREmailTemplate");
		return mav;
	}		

	/**
	 * mobile initial email template
	 * 
	 * @param emailaddress
	 * @param email_subject
	 * @param email_message
	 * @param email_signature
	 * @return
	 */
	@RequestMapping(value="/updateiemailtemplate", method=RequestMethod.POST)
	public ModelAndView updateMobileIEmailTemplate(
			@RequestParam(value="iemailaddress") String emailaddress,
			@RequestParam(value="iemail_subject") String email_subject,
			@RequestParam(value="iemail_message") String email_message,
			@RequestParam(value="iemail_signature") String email_signature
		) {
		log.debug("ENTERED updateMobileREmailTemplate");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("settings/msettings");
		
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shopsetting.setIemail_address(emailaddress);
			shopsetting.setIemail_subject(email_subject);
			shopsetting.setIemail_message(email_message);
			shopsetting.setIemail_signature(email_signature);
			shopsetting.merge();
			// adding audit log
			saveAuditMessage("Updated settings shop email template",null,"GENERAL");
		}catch(Exception e){
			log.error(e);
		}
			

		log.debug("EXITING updateMobileIEmailTemplate");
		return mav;
	}		

	@RequestMapping(value="/logo", method=RequestMethod.GET)
	public @ResponseBody  String updateLogo(
			@RequestParam(value="f") String logofile
		) {
		log.debug("ENTERED updateLogo");
		String RESULTS = "SUCCESS";
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shopsetting.setImage_file_logo(logofile);
			shopsetting.merge();
			// adding audit log
			saveAuditMessage("Updated settings shop logo",null,"GENERAL");
		}catch(Exception e){
			log.error(e);
			RESULTS = "FAILURE";
		}

		log.debug("EXITING updateLogo");
		return new JSONSerializer().exclude("*.class").serialize(RESULTS);
	}
	
	@ModelAttribute("shopsettings")
    public ShopSettings populateSettings() {
		log.debug("Entering populateSettings");
		TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
		ShopSettings shopsetting = shopsettings.getResultList().get(0);
		
		String receip_subject = shopsetting.getRemail_subject();
		String receip_message = shopsetting.getRemail_message();
		String receip_signature = shopsetting.getRemail_signature();
		String email_address = shopsetting.getEmail_address();
		
		String init_subject = shopsetting.getIemail_subject();
		String init_message = shopsetting.getIemail_message();
		String init_signature = shopsetting.getIemail_signature();

		String noti_subject = shopsetting.getEmail_subject();
		String noti_message = shopsetting.getEmail_message();
		String noti_signature = shopsetting.getEmail_signature();
		
		
		String sms_receip_message = shopsetting.getRsms_message();
		String sms_noti_message = shopsetting.getSms_message();
		String sms_init_message = shopsetting.getIsms_message();
		
		log.debug("receipt_message: " + receip_message);
		String initial_message = "";
		if(init_message==null){
			initial_message ="<p>	Dear ${clientfullname},</p><p>	Your appointment for a ${apptservicename}</p><p>	is on ${apptdate} @ ${appttime}</p><p>	Please give 24 hours notice if you need to change or cancel this appointment.</p><p>	Sincerely,</p><p>	${stafffullname}</p><p>	p.s. Thank you for referring your friends and family.&nbsp; We appreciate your help!</p>";
		}
		String initial_subject="";
		if(init_subject==null){
			initial_subject="Your appointment has been scheduled";
		}
		String initial_signature="";
		if(init_signature==null){
			initial_signature="Thank you for scheduling your appointment";
		}
		String receipt_message = "";
		if(receip_message==null){
			receipt_message = "<p>	Dear ${clientfullname},</p><p>	Here is your receipt for</p><p>	your ${apptservicename}</p><p>	${apptdate} @ ${appttime}</p><p>	${apptserviceprice}</p><p>	I appreciate your business!</p><p>	${stafffullname}</p>";
		}
		String receipt_subject = "";
		if(receip_subject==null){
			receipt_subject = "Appointment receipt";
		}
		String receipt_signature = "";
		if(receip_signature==null){
			receipt_signature = "p.s. I would love to work for your friends and family too. Thank you for telling them about me!";
		}
		String notification_email="";
		if(noti_message==null){
			notification_email="<p>	Dear ${clientfullname},</p><p>	This is a reminder that your appointment for ${apptservicename}</p><p>	Is on date ${apptdate} time ${appttime}</p><p>	&nbsp;</p><p>	Sincerely,</p><p>	${stafffullname}</p>";
		}
		String notification_subject="";
		if(noti_subject==null){
			notification_subject="Upcoming appointment notification";
		}
		String notification_signature="";
		if(noti_signature==null){
			notification_signature="Thank you for being on time.";
		}
		Properties properties = new Properties();
		try {
		    properties.load(this.getClass().getClassLoader().getResourceAsStream("emailtemplates.properties"));
		    log.debug("emailtemplates.properties loaded "+ properties.toString());
		    if(init_message==null){
		    	initial_message = properties.getProperty("initial");
		    }
		    if(init_signature==null){
		    	initial_signature = properties.getProperty("initial_signature");
		    }
		    if(init_subject==null){
		    	initial_subject = properties.getProperty("initial_subject");
		    }
		    log.debug("initial_message: " + initial_message);
		    if(receip_message==null){
		    	receipt_message = properties.getProperty("receipt");
		    }
		    if(receip_signature==null){
		    	receipt_signature = properties.getProperty("receipt_signature");
		    }
		    if(receip_subject==null){
		    	receipt_subject = properties.getProperty("receipt_subject");
		    }
		    log.debug("receipt_message: " + receipt_message);
		    if(noti_message==null){
		    	notification_email = properties.getProperty("notification");
		    }
		    if(noti_signature==null){
		    	notification_signature = properties.getProperty("notification_signature");
		    }
		    if(noti_subject==null){
		    	notification_subject = properties.getProperty("notification_subject");
		    }
		    log.debug("notification_email: " + notification_email);
		} catch (IOException e) {
			log.error(e);
		}		
		Properties sms_properties = new Properties();
		try {
			sms_properties.load(this.getClass().getClassLoader().getResourceAsStream("smstemplate.properties"));
		    log.debug("smstemplate.properties loaded "+ sms_properties.toString());
		    if(sms_init_message==null){
		    	sms_init_message = properties.getProperty("isms_message");
		    }
		    if(sms_noti_message==null){
		    	sms_noti_message = properties.getProperty("sms_message");
		    }
		    if(sms_receip_message==null){
		    	sms_receip_message = properties.getProperty("rsms_message");
		    }
		    
		} catch (Exception e) {
			log.error("Exception accessing sms template properties file ");
			log.error(e);
		}		
		
		//notification
		if(shopsetting.getEmail_message()==null ||shopsetting.getEmail_message().equalsIgnoreCase("")){
			shopsetting.setEmail_subject(notification_subject);
			shopsetting.setEmail_message(notification_email);
			shopsetting.setEmail_signature(notification_signature);
		}
		// initial
		if(shopsetting.getIemail_message()==null ||shopsetting.getIemail_message().equalsIgnoreCase("")){
			shopsetting.setIemail_subject(initial_subject);
			shopsetting.setIemail_message(initial_message);
			shopsetting.setIemail_signature(initial_signature);
			shopsetting.setIemail_address(email_address);
		}
		// receipt
		if(receip_message == null || receip_message.equalsIgnoreCase("")){ 
			shopsetting.setRemail_subject(receipt_subject);
			shopsetting.setRemail_message(receipt_message);
			shopsetting.setRemail_signature(receipt_signature);
			shopsetting.setRemail_address(email_address);
		}

		//sms notification
		if(shopsetting.getSms_message()==null ||shopsetting.getSms_message().equalsIgnoreCase("")){
			shopsetting.setSms_message(sms_noti_message);
		}
		// sms initial
		if(shopsetting.getIsms_message()==null ||shopsetting.getIsms_message().equalsIgnoreCase("")){
			shopsetting.setIsms_message(sms_init_message);
		}
		// sms receipt
		if(sms_receip_message == null || sms_receip_message.equalsIgnoreCase("")){ 
			shopsetting.setRsms_message(sms_receip_message);
		}

		
		shopsetting.merge();
		
		
		
		if(shopsetting.getNumber_days_notify() == null){
			shopsetting.setNumber_days_notify(0);
		}
        return shopsetting;
    }
	@ModelAttribute("shop")
    public Shop shop() {
		log.debug("Entering shop");
		log.debug("Exiting shop");
        return getShop();
    }
	@ModelAttribute("username")
    public String username() {
		log.debug("Entering username");
		String username = "";
    	Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (obj instanceof UserDetails) {
			username = ((UserDetails) obj).getUsername();
		}
		log.debug("Exiting username");
        return username;
    }
    @ModelAttribute("shopname")
	public String shopName(){
    	log.debug("ENTERING shopName");
    	String shopname = "";
    	shopname = getShop().getShop_name();
    		
	    log.debug("shopname: "+shopname);
    	log.debug("EXITING shopName");
    	return shopname;
    }
    @ModelAttribute("shopurl")
	public String shopUrl(){
    	log.debug("ENTERING shopUrl");
    	String shopurl = "";
    	shopurl = getShop().getShop_url();
    		
	    log.debug("shopurl: "+shopurl);
    	log.debug("EXITING shopUrl");
    	return shopurl;
    }
    
    
    @ModelAttribute("google_cals")
	public GCalForm shopGoogle_cals(){
    	GCalForm gal = new GCalForm();
    	log.debug("ENTERING shopGoogle_cals");
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			boolean use_gcal = shopsetting.isUse_gcalendar_for_shop();
			boolean use_staff_gcal = shopsetting.isAllow_staff_gcalendar();
			if(use_gcal){
				TypedQuery<GCal> gals = GCal.findGCalsByShopEqualsAndStaffIsNull(getShop());
				if(gals.getResultList().size() > 0){
					GCal gcal = gals.getResultList().get(0);
					gal.setAllow_gcal_for_staff(use_staff_gcal);
					gal.setUse_gcal_for_shop(use_gcal);
					gal.setGcal_id(gcal.getCalendarnameid());
					gal.setGcal_title(gcal.getGcal_title());
					gal.setUsername(gcal.getUsername());
					log.debug("google_cals: " + gal.toString());
				}
				
			}
		}catch(Exception e){
			log.error(e);
		}

    	log.debug("EXITING shopGoogle_cals");
    	return gal;
    }
    @ModelAttribute("google_username")
	public String shopGoogle_username(){
    	String google_username = "";
    	log.debug("ENTERING shopGoogle_username");
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			boolean use_gcal = shopsetting.isUse_gcalendar_for_shop();
			if(use_gcal){
				TypedQuery<GCal> gals = GCal.findGCalsByShopEqualsAndStaffIsNull(getShop());
				if(gals.getResultList().size() > 0){
					GCal gal = gals.getResultList().get(0);
					google_username = gal.getUsername();
				}
			}			
		}catch(Exception e){
			log.error(e);
		}

    	log.debug("EXITING shopGoogle_username");
    	return google_username;
    }
    
    @ModelAttribute("shoptimezone")
	public String shopTimeZone(){
    	String shoptimezone = "";
    	log.debug("ENTERING shopTimeZone");
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(getShop());
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shoptimezone = shopsetting.getTimezone();
			
		}catch(Exception e){
			log.error(e);
		}

    	log.debug("EXITING shopTimeZone");
    	return shoptimezone;
    }

	public Audit getAudit() {
		return audit;
	}

	public void setAudit(Audit audit) {
		this.audit = audit;
	}

	public UserPreference getPreferences() {
		return preferences;
	}

	public void setPreferences(UserPreference preferences) {
		this.preferences = preferences;
	}
	public PersonDaoImpl getPersonDao() {
		return personDao;
	}
	public void setPersonDao(PersonDaoImpl personDao) {
		this.personDao = personDao;
	}
	private String getIndividualusername() {
		return individualusername;
	}
	private void setIndividualusername(String individualusername) {
		this.individualusername = individualusername;
	}
	
}
