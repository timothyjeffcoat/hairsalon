package com.yourhairsalon.booking.task;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

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
import com.yourhairsalon.booking.util.GenerateAccessToken;
import com.yourhairsalon.booking.util.ResultPrinter;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.base.rest.PayPalResource;

@Service
public class PayPalServiceImpl implements PayPalService{
	
	private static final Log log = LogFactory.getLog(PayPalServiceImpl.class);
	private static final long serialVersionUID = 1L;
	
	Map<String, String> map = new HashMap<String, String>();

	public void init() {
		// ##Load Configuration
		// Load SDK configuration for
		// the resource. This intialization code can be
		// done as Init Servlet.
		InputStream is = PayPalServiceImpl.class.getResourceAsStream("/sdk_config.properties");
		try {
			PayPalResource.initConfig(is);
		} catch (PayPalRESTException e) {
			log.error(e.getMessage());
		}

	}
	
	public Payment createPayment(
			String payerid,
			String guid,
			String returnURL,
			String cancelURL,
			HttpServletResponse resp
			) {
		log.debug("Entered createPayment");
		Payment createdPayment = null;
		// ###AccessToken
		// Retrieve the access token from
		// OAuthTokenCredential by passing in
		// ClientID and ClientSecret
		APIContext apiContext = null;
		String accessToken = null;
		try {
			log.debug("trace paypal 0");
			accessToken = GenerateAccessToken.getAccessToken();
			log.debug("trace paypal 1");
			// ### Api Context
			// Pass in a `ApiContext` object to authenticate
			// the call and to send a unique request id
			// (that ensures idempotency). The SDK generates
			// a request id if you do not pass one explicitly.
			apiContext = new APIContext(accessToken);
			log.debug("trace paypal 2");
			// Use this variant if you want to pass in a request id
			// that is meaningful in your application, ideally
			// a order id.
			/*
			 * String requestId = Long.toString(System.nanoTime(); APIContext
			 * apiContext = new APIContext(accessToken, requestId ));
			 */
		} catch (PayPalRESTException e) {
			log.debug("trace paypal 3");
//			req.setAttribute("error", e.getMessage());
		}
		if (payerid != null) {
			Payment payment = new Payment();
			if (guid != null) {
				log.debug("trace paypal 4");
				payment.setId(map.get(guid));
			}
			log.debug("trace paypal 5");
			PaymentExecution paymentExecution = new PaymentExecution();
			log.debug("trace paypal 6");
			paymentExecution.setPayerId(payerid);
			log.debug("trace paypal 7");
			try {
				log.debug("trace paypal 8");
				createdPayment = payment.execute(apiContext, paymentExecution);
				log.debug("trace paypal 9");
//				ResultPrinter.addResult(req, resp, "Executed The Payment", Payment.getLastRequest(), Payment.getLastResponse(), null);
			} catch (PayPalRESTException e) {
				log.debug("trace paypal 10");
//				ResultPrinter.addResult(req, resp, "Executed The Payment", Payment.getLastRequest(), null, e.getMessage());
			}
		} else {

			// ###Details
			// Let's you specify details of a payment amount.
			log.debug("trace paypal 11");
			Details details = new Details();
			details.setShipping("1");
			details.setSubtotal("5");
			details.setTax("1");
			log.debug("trace paypal 12");
			// ###Amount
			// Let's you specify a payment amount.
			Amount amount = new Amount();
			amount.setCurrency("USD");
			// Total must be equal to sum of shipping, tax and subtotal.
			amount.setTotal("7");
			amount.setDetails(details);
			log.debug("trace paypal 13");
			// ###Transaction
			// A transaction defines the contract of a
			// payment - what is the payment for and who
			// is fulfilling it. Transaction is created with
			// a `Payee` and `Amount` types
			Transaction transaction = new Transaction();
			transaction.setAmount(amount);
			transaction
					.setDescription("This is the payment transaction description.");
			log.debug("trace paypal 14");
			// ### Items
			Item item = new Item();
			item.setName("Ground Coffee 40 oz").setQuantity("1").setCurrency("USD").setPrice("5");
			ItemList itemList = new ItemList();
			List<Item> items = new ArrayList<Item>();
			items.add(item);
			itemList.setItems(items);
			
			transaction.setItemList(itemList);
			log.debug("trace paypal 15");
			
			// The Payment creation API requires a list of
			// Transaction; add the created `Transaction`
			// to a List
			List<Transaction> transactions = new ArrayList<Transaction>();
			transactions.add(transaction);
			log.debug("trace paypal 16");
			// ###Payer
			// A resource representing a Payer that funds a payment
			// Payment Method
			// as 'paypal'
			Payer payer = new Payer();
			payer.setPaymentMethod("paypal");
			log.debug("trace paypal 17");
			// ###Payment
			// A Payment Resource; create one using
			// the above types and intent as 'sale'
			Payment payment = new Payment();
			payment.setIntent("sale");
			payment.setPayer(payer);
			payment.setTransactions(transactions);
			log.debug("trace paypal 18");
			// ###Redirect URLs
			// BEGIN TODO: FIX THIS SO THAT THE CORRECT URLS ARE RETURNED FOR THIS WEB APP INSTEAD OF THE PAYPAL SAMPLE APP
			// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			RedirectUrls redirectUrls = new RedirectUrls();
			guid = UUID.randomUUID().toString().replaceAll("-", "");

			
//			redirectUrls.setCancelUrl(req.getScheme() + "://"
//					+ req.getServerName() + ":" + req.getServerPort()
//					+ req.getContextPath() + "/paymentwithpaypal?guid=" + guid);
//			redirectUrls.setReturnUrl(req.getScheme() + "://"
//					+ req.getServerName() + ":" + req.getServerPort()
//					+ req.getContextPath() + "/paymentwithpaypal?guid=" + guid);

			
			redirectUrls.setCancelUrl(cancelURL+ "/paymentwithpaypal?guid=" + guid);
			redirectUrls.setReturnUrl(returnURL + "/paymentwithpaypal?guid=" + guid);
			payment.setRedirectUrls(redirectUrls);

			log.debug("trace paypal 19");
			// Create a payment by posting to the APIService
			// using a valid AccessToken
			// The return object contains the status;
			try {
				createdPayment = payment.create(apiContext);
				log.info("Created payment with id = "
						+ createdPayment.getId() + " and status = "
						+ createdPayment.getState());
				log.debug("trace paypal 20");
				// ###Payment Approval Url
				String paypalUrl = "";
				Iterator<Links> links = createdPayment.getLinks().iterator();
				while (links.hasNext()) {
					Links link = links.next();
					if (link.getRel().equalsIgnoreCase("approval_url")) {
						// i believe i need to execute this link to take us to the paypal page
						paypalUrl = link.getHref();
//						req.setAttribute("redirectURL", link.getHref());
					}
				}
				log.debug("trace paypal 21");
//				ResultPrinter.addResult(req, resp, "Payment with PayPal", Payment.getLastRequest(), Payment.getLastResponse(), null);
				map.put(guid, createdPayment.getId());//this is being used like we expect it to beused again on the return. what to do now?
				log.debug("trace paypal 22");
				resp.setHeader("Location", paypalUrl);
				// END TODO: FIX THIS SO THAT THE CORRECT URLS ARE RETURNED FOR THIS WEB APP INSTEAD OF THE PAYPAL SAMPLE APP
			} catch (PayPalRESTException e) {
				log.debug("trace paypal 23");
//				ResultPrinter.addResult(req, resp, "Payment with PayPal", Payment.getLastRequest(), null, e.getMessage());
			}
		}
		log.debug("Exiting createPayment");
		return createdPayment;
	}
}
