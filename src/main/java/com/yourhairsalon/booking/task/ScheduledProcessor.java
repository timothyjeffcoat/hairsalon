package com.yourhairsalon.booking.task;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.mail.MailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yourhairsalon.booking.domain.Appointment;
import com.yourhairsalon.booking.domain.BaseService;
import com.yourhairsalon.booking.domain.Clients;
import com.yourhairsalon.booking.domain.Communications;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.ShopSettings;
import com.yourhairsalon.booking.form.AppointmentNotification;
import com.yourhairsalon.booking.reference.CommType;
import com.yourhairsalon.booking.reference.ScheduleStatus;
import com.yourhairsalon.booking.web.ServicesController;

@Transactional
public class ScheduledProcessor implements Processor {
	private static final Log log = LogFactory.getLog(ScheduledProcessor.class);
	private final AtomicInteger counter = new AtomicInteger();
	
	@Autowired
	private transient AppointmentsNotificationHelper appointmentNotificationHelper;
	
	@Autowired
	private transient AppointmentsSMSNotificationHelper appointmentSMSNotificationHelper;

	private Appointment appointment;
	private Clients client;
	private ShopSettings settings;

	/**
	 * Find Appointments that are scheduled within the next X number of days and
	 * send out a message to the queu that will then be read and an email sent
	 * out.
	 */
	public void process() {
		/*
		 * way this works is: 1. get list of shops 2. for each shop get
		 * number_days_notify 3. find list of appointments that fall within
		 * number_days_notify for each shop 4. send out notifications
		 */
		log.debug("ENTERED PROCESS TO SEND OUT NOTIFICATIONS EMAILS FOR UPCOMING APPOINTMENTS");
		System.setProperty("file.encoding", "UTF-8");

		List<Shop> shops = Shop.findAllShops();
		String sms_message = "";
			
		for (Iterator ayes = shops.iterator(); ayes.hasNext();) {
			log.debug("ENTERED LIST OF SHOPS");
			Shop shop = (Shop) ayes.next();
			String shop_name = shop.getShop_name();
			String shop_phone = "";
			log.debug("SHOP NAME: "+shop_name);
			
			TypedQuery<ShopSettings> shopsettinglist = ShopSettings.findShopSettingsesByShop(shop);
			if (shopsettinglist.getResultList().size() > 0) {
				log.debug("GETTING SHOP SETTINGS");
				ShopSettings shopsetting = shopsettinglist.getResultList().get(0);

				shop_phone = shopsetting.getStore_phone();

				sms_message = shopsetting.getSms_message();
				Properties sms_properties = new Properties();
				try {
					if(sms_message == null || sms_message .equalsIgnoreCase("")){
						sms_properties.load(this.getClass().getClassLoader().getResourceAsStream("smstemplate.properties"));
				    
				    	sms_message  = sms_properties.getProperty("sms_message");
				    	shopsetting.setSms_message(sms_message );
				    }
				    
				    shopsetting.merge();
				}catch(Exception exsms){
					log.error(exsms);
					
				}
				log.debug("trace 1");
				int numberofdaysnotify = 0;
				if (shopsetting.getNumber_days_notify() == null) {
					log.debug("trace 2");
				} else {
					log.debug("trace 3");
					numberofdaysnotify = shopsetting.getNumber_days_notify();
					
				}
				
				log.debug("numberofdaysnotify: " + numberofdaysnotify);
				log.debug("trace 4");
				if (numberofdaysnotify > 0) {
					log.debug("numberofdaysnotify: " + numberofdaysnotify);
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					df.setTimeZone(TimeZone.getTimeZone("MST"));
					Date beginofday = null;
					Date endofday = null;
					Calendar cal = Calendar.getInstance();
					beginofday = cal.getTime();
					log.debug("Today : " + cal.getTime());

					// Subtract 30 days from the calendar
					cal.add(Calendar.DATE, +numberofdaysnotify);
					log.debug("3 days from now: " + cal.getTime());
					endofday = cal.getTime();

					beginofday.setHours(0);
					beginofday.setMinutes(0);
					log.debug("the value of beginofday: " + beginofday);

					endofday.setHours(23);
					endofday.setMinutes(59);
					log.debug("processing next 10 at " + new Date());
					try {
						log.debug("retrieving appointments");
						log.debug("beginofday: " + beginofday);
						log.debug("endofday: " + endofday);
						TypedQuery<Appointment> appointments = Appointment
								.findAppointmentsByShopAndAppointmentDateBetween(
										shop, beginofday, endofday);
						log.debug("retrieved appointments");
						log.debug("OUTER appts size: " + appointments.getResultList().size());
						if (appointments.getResultList().size() > 0) {
							log.debug("appts size: " + appointments.getResultList().size());

							Properties properties = new Properties();
							String initial_email = "<p>	Dear ${clientfullname},</p><p>	Your appointment for a ${apptservicename}</p><p>	is on ${apptdate} @ ${appttime}</p><p>	Please give 24 hours notice if you need to change or cancel this appointment.</p><p>	Sincerely,</p><p>	${stafffullname}</p><p>	p.s. Thank you for referring your friends and family.&nbsp; We appreciate your help!</p>";
							String initial_subject = "Your appointment has been scheduled";
							String initial_signature = "p.s. s. Thank you for scheduling your appointment";
							
							String receipt_email = "<p>	Dear ${clientfullname},</p><p>	Here is your receipt for</p><p>	your ${apptservicename}</p><p>	${apptdate} @ ${appttime}</p><p>	${apptserviceprice}</p><p>	I appreciate your business!</p><p>	${stafffullname}</p>";
							String receipt_subject = "Appointment receipt";
							String receipt_signature = "p.s. I would love to work for your friends and family too. Thank you for telling them about me!";

							String notification_email = "<p>	Dear ${clientfullname},</p><p>	This is a reminder that your appointment for ${apptservicename}</p><p>	Is on date ${apptdate} time ${appttime}</p><p>	&nbsp;</p><p>	Sincerely,</p><p>	${stafffullname}</p>";
							String notification_subject = "Upcoming appointment notification";
							String notification_signature = "p.s. thank you for being on time.";
							try {
								properties.load(this
										.getClass()
										.getClassLoader()
										.getResourceAsStream(
												"emailtemplates.properties"));

								initial_email = properties
										.getProperty("initial");
								initial_signature = properties
										.getProperty("initial_signature");
								initial_subject = properties
										.getProperty("initial_subject");

								receipt_email = properties
										.getProperty("receipt");
								receipt_signature = properties
										.getProperty("receipt_signature");
								receipt_subject = properties
										.getProperty("receipt_subject");

								notification_email = properties
										.getProperty("notification");
								notification_signature = properties
										.getProperty("notification_signature");
								notification_subject = properties
										.getProperty("notification_subject");
							} catch (IOException e) {
								log.error(e);
							}

							// notification
							if (shopsetting.getEmail_message() == null
									|| shopsetting.getEmail_message()
											.equalsIgnoreCase("")) {
								shopsetting
										.setEmail_subject(notification_subject);
								shopsetting
										.setEmail_message(notification_email);
								shopsetting
										.setEmail_signature(notification_signature);
							}
							// initial
							if (shopsetting.getIemail_message() == null
									|| shopsetting.getIemail_message()
											.equalsIgnoreCase("")) {
								shopsetting.setIemail_subject(initial_subject);
								shopsetting.setIemail_message(initial_email);
								shopsetting
										.setIemail_signature(initial_signature);
							}
							// receipt
							if (shopsetting.getRemail_message() == null
									|| shopsetting.getRemail_message()
											.equalsIgnoreCase("")) {
								shopsetting.setRemail_subject(receipt_subject);
								shopsetting.setRemail_message(receipt_email);
								shopsetting
										.setRemail_signature(receipt_signature);
							}
							if (shopsetting.getEmail_message() == null
									|| shopsetting.getEmail_message()
											.equalsIgnoreCase("")
									|| shopsetting.getIemail_message() == null
									|| shopsetting.getIemail_message()
											.equalsIgnoreCase("")
									|| shopsetting.getRemail_message() == null
									|| shopsetting.getRemail_message()
											.equalsIgnoreCase("")) {
								shopsetting.merge();
							}

							for (int x = 0; x < appointments.getResultList()
									.size(); x++) {
								AppointmentNotification an = new AppointmentNotification();
								log.debug("setting appointment notification");
								Appointment appt = appointments.getResultList().get(x);
								an.setAppointmentid(appt.getId());
								if(appt.getStatus()!=ScheduleStatus.ACTIVE){
									log.debug("THIS APPOINTMENT WAS NOT ACTIVE!!");
									continue;
								}
								if(appt.getClient().getAccepts_sms_notifications()==false){
									
								}else{
									try {
										Thread.sleep(2000);
										Long number_of_sms_available = shop.getNumber_sms_purchased();
										if(number_of_sms_available != null && number_of_sms_available > 0L){
											log.debug("before sending to sendSMSToQue shop_phone: "+shop_phone);
											log.debug("before sending to sendSMSToQue shop_name: "+shop_name);
											sendSMSToQue(shopsetting, an, appt,shop_phone,shop_name);
											log.debug("after sending to sendSMSToQue");
										}else{
											log.debug("");
										}
									} catch (Exception e) {
										log.error(e);
									}
								}
								if(appt.getClient().getAccepts_notifications()==false){
//									continue;
								}else{
									
									sendEmailToQue(shopsetting, an, appt);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						log.error(e.getMessage());
					}
				}
			}else{
				log.debug("FAILED TO GET SHOP SETTINGS");
			}
		}

	}
	/**
	 * send filled out sms message to jms que
	 * 
	 * @param shopsetting
	 * @param an
	 * @param appt
	 */
	private void sendSMSToQue(ShopSettings shopsetting,AppointmentNotification an, Appointment appt,String shop_phone, String shop_name) {
		
		log.debug("Entered sendSMSToQue ");
		
		String staff_firstname = "";
		String staff_lastname = "";
		String staff_fullname = "";
		
		String shop_phonenumber = shop_phone;
		
		an.setAppointmentdate(appt.getAppointmentDate());
		log.debug("2 ");
		an.setBegintime(appt.getBeginDateTime().getTime());
		log.debug("3 ");
		an.setEndtime(appt.getEndDateTime().getTime());
		log.debug("4 ");
		Clients cl = appt.getClient();
		log.debug("5 ");
		if (cl != null) {
			an.setClientname(cl.getFirstName() + " " + cl.getLastName());
		}
		log.debug("6 ");
		an.setDescription(appt.getDescription());
		log.debug("7 ");
		boolean sendtext=false;
		if (cl != null) {
			Set<Communications> comm = cl.getCommunication();
			log.debug("8 ");
			String client_cellphone = "";
			log.debug("9 ");
			for (Iterator i = comm.iterator(); i.hasNext();) {
				Communications base = (Communications) i.next();
				if (base.getCommunication_type().equals(CommType.CELL_PHONE)) {
					client_cellphone = base.getCommunication_value();
					if(client_cellphone == null || client_cellphone.isEmpty()){
						sendtext = false; 
					}else{
						sendtext = true;
					}
					break;
				}
			}
			log.debug("10 ");
			an.setShop_sms_to(client_cellphone);
		}
		if (appt.getStaff() != null) {
			
			staff_firstname = appt.getStaff().getFirstName();
			staff_lastname = appt.getStaff().getLastName();
			staff_fullname = staff_firstname + " "+ staff_lastname;
			log.debug("staff for this appointment "+staff_fullname);
		}
		
		log.debug("11 ");
		
		an.setFromshop(appt.getShop().getShop_name());
		String text_message = "";
		text_message = shopsetting.getSms_message();
		// get sms message from shop settings
		// REPLACE FOR THE FOLLOWING TAGS
		String message = text_message;
		log.debug("message: " + message);
		String client_firstname = "";
		String client_lastname = "";
		String client_fullname = "";

		if (appt.getClient() != null) {
			client_firstname = appt.getClient().getFirstName();
			client_lastname = appt.getClient().getLastName();
			client_fullname = client_firstname + " " + client_lastname;
			message = replaceClientInfo(message, client_firstname,
					client_lastname, client_fullname);
		}
		if (appt.getStaff() != null) {
			staff_firstname = appt.getStaff().getFirstName();
			staff_lastname = appt.getStaff().getLastName();
			staff_fullname = staff_firstname + " "+ staff_lastname;
			message = replaceStaffInfo(message, staff_firstname,
					staff_lastname, staff_fullname);
		}
		if (appt.getServices() != null) {
			Set<BaseService> setsvcs = appt.getServices();
			for (Iterator it = setsvcs.iterator(); it.hasNext();) {
				BaseService bservice = (BaseService) it.next();
				String servicename = bservice.getDescription();
				String serviceprice = "" + bservice.getCost();
				message = replaceServiceInfo(message, servicename, serviceprice);
			}

		}
		// ********************************************
		String today = "";
		String shoptimezone = "";
		try {
			shoptimezone = shopsetting.getTimezone();
			if (shoptimezone == null) {
				shoptimezone = "MST";
				shopsetting.setTimezone(shoptimezone);
				shopsetting.merge();
			}

		} catch (Exception e) {
			log.error(e);
		}
		TimeZone tz = TimeZone.getTimeZone(shoptimezone);
		Date dateValue = appt.getAppointmentDate();
		Calendar calValue = Calendar.getInstance(tz);
		calValue.setTime(dateValue);

		DateFormat df2 = new SimpleDateFormat("MM/dd/yyyy");
		String dateString = df2.format(calValue.getTime());
		today = dateString;

		String appointmentbegintime = "";
		SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm a");
		appointmentbegintime = dateformatter.format(appt.getBeginDateTime().getTime());

		message = replaceAppointmentInfo(message, today, appointmentbegintime);

		log.debug("sendSMSToQue shop_phonenumber: " + shop_phonenumber);
		log.debug("sendSMSToQue shop name: " + shop_name);
		log.debug("sendSMSToQue message: " + message);
		message = replaceShopInfo(message, shop_phonenumber, shop_name);
		
		an.setShop_sms_message(message);
		
		if(sendtext == true){
			getAppointmentSMSNotificationHelper().sendMessage(an);
		}
		log.debug("Exiting sendSMSToQue ");
		
	}

	private void sendEmailToQue(ShopSettings shopsetting,AppointmentNotification an, Appointment appt) {
		log.debug("Entered sendEmailToQue ");
		an.setAppointmentdate(appt.getAppointmentDate());
		log.debug("2 ");
		an.setBegintime(appt.getBeginDateTime().getTime());
		log.debug("3 ");
		an.setEndtime(appt.getEndDateTime().getTime());
		log.debug("4 ");
		Clients cl = appt.getClient();
		log.debug("5 ");
		if (cl != null) {
			an.setClientname(cl.getFirstName() + " " + cl.getLastName());
		}
		log.debug("6 ");
		an.setDescription(appt.getDescription());
		log.debug("7 ");
		if (cl != null) {
			Set<Communications> comm = cl.getCommunication();
			log.debug("8 ");
			String client_email = "";
			log.debug("9 ");
			for (Iterator i = comm.iterator(); i.hasNext();) {
				Communications base = (Communications) i.next();
				if (base.getCommunication_type().equals(CommType.EMAIL)) {
					client_email = base.getCommunication_value();
				}
			}
			log.debug("10 ");
			an.setEmail(client_email);
		}
		log.debug("11 ");
		an.setFromshop(appt.getShop().getShop_name());
		log.debug("12 ");
		an.setShop_email_address(shopsetting.getEmail_address());
		log.debug("13 ");
		// DO TAG REPLACEMENTS VIA STRING SEARCH AND
		// REPLACE FOR THE FOLLOWING TAGS
		String message = shopsetting.getEmail_message();
		String big_message = shopsetting.getEmail_big_message();
		if (big_message == null || big_message.equalsIgnoreCase("")) {
			big_message = message;
			shopsetting.setEmail_message(big_message);
			log.debug("TRYING TO UPDATE THE BIG MESSAGE FOR THIS SHOP");
			shopsetting.merge();
			log.debug("UPDATED THE BIG MESSAGE FOR THIS SHOP WITHOUT CRASHING");
		} else {
			message = big_message;
		}
		log.debug("message: " + message);
		String client_firstname = "";
		String client_lastname = "";
		String client_fullname = "";
		String staff_firstname = "";
		String staff_lastname = "";
		String staff_fullname = "";

		if (appt.getClient() != null) {
			client_firstname = appt.getClient().getFirstName();
			client_lastname = appt.getClient().getLastName();
			client_fullname = client_firstname + " " + client_lastname;
			message = replaceClientInfo(message, client_firstname,
					client_lastname, client_fullname);
		}
		if (appt.getStaff() != null) {
			staff_firstname = appt.getStaff().getFirstName();
			staff_lastname = appt.getStaff().getLastName();
			staff_fullname = staff_firstname + " "+ staff_lastname;
			message = replaceStaffInfo(message, staff_firstname,
					staff_lastname, staff_fullname);
		}
		if (appt.getServices() != null) {
			Set<BaseService> setsvcs = appt.getServices();
			for (Iterator it = setsvcs.iterator(); it.hasNext();) {
				BaseService bservice = (BaseService) it.next();
				String servicename = bservice.getDescription();
				String serviceprice = "" + bservice.getCost();
				message = replaceServiceInfo(message, servicename, serviceprice);
			}

		}
		// ********************************************
		String today = "";
		String shoptimezone = "";
		try {
			shoptimezone = shopsetting.getTimezone();
			if (shoptimezone == null) {
				shoptimezone = "MST";
				shopsetting.setTimezone(shoptimezone);
				shopsetting.merge();
			}

		} catch (Exception e) {
			log.error(e);
		}
		TimeZone tz = TimeZone.getTimeZone(shoptimezone);
		Date dateValue = appt.getAppointmentDate();
		Calendar calValue = Calendar.getInstance(tz);
		calValue.setTime(dateValue);

		DateFormat df2 = new SimpleDateFormat("MM/dd/yyyy");
		String dateString = df2.format(calValue.getTime());
		today = dateString;

		String appointmentbegintime = "";
		SimpleDateFormat dateformatter = new SimpleDateFormat("hh:mm a");
		appointmentbegintime = dateformatter.format(appt.getBeginDateTime().getTime());

		message = replaceAppointmentInfo(message, today, appointmentbegintime);

		an.setShop_email_message(message);
		log.debug("14 ");
		
		an.setShop_email_signature(shopsetting.getEmail_signature());
		
		log.debug("15 ");
		
		an.setShop_email_subject(shopsetting.getEmail_subject());
		
		log.debug("16 ");
		log.debug("an: " + an.toString());
		
		getAppointmentNotificationHelper().sendMessage(an);
		
		log.debug("appointmentnotification sent to queue: "+ an.toString());
		
		log.debug("Exiting sendEmailToQue ");
	}
	
	private String replaceShopInfo(String initial_sms_message,String shopphone, String shopname) {
		log.debug("Entered replaceShopInfo");
		log.debug("replaceShopInfo initial_sms_message : " + initial_sms_message);
		log.debug("replaceShopInfo shopphone: " + shopphone);
		log.debug("replaceShopInfo shopname: "+ shopname);
		
		initial_sms_message = initial_sms_message.replaceAll(Pattern.quote("${shopname}"), shopname);
		
		initial_sms_message = initial_sms_message.replaceAll(Pattern.quote("${shopphone}"), shopphone);
		log.debug("Exiting replaceShopInfo");
		return initial_sms_message;
	}
	
	private String replaceAppointmentInfo(String message, String today,
			String appointmentbegintime) {
		message = message.replaceAll(Pattern.quote("${apptdate}"), today);
		message = message.replaceAll(Pattern.quote("${appttime}"),appointmentbegintime);
		return message;
	}
	private String replaceServiceInfo(String message, String servicename,
			String serviceprice) {
		message = message.replaceAll(Pattern.quote("${apptservicename}"),servicename);
		message = message.replaceAll(Pattern.quote("${apptserviceprice}"),serviceprice);
		return message;
	}
	private String replaceStaffInfo(String message, String staff_firstname,
			String staff_lastname, String staff_fullname) {
		message = message.replaceAll(Pattern.quote("${stafffullname}"),staff_fullname);
		message = message.replaceAll(Pattern.quote("${stafffirstname}"),staff_firstname);
		message = message.replaceAll(Pattern.quote("${stafflastname}"),staff_lastname);
		return message;
	}
	private String replaceClientInfo(String message, String client_firstname,
			String client_lastname, String client_fullname) {
		message = message.replaceAll(Pattern.quote("${clientfullname}"),client_fullname);
		message = message.replaceAll(Pattern.quote("${clientfirstname}"),client_firstname);
		message = message.replaceAll(Pattern.quote("${clientlastname}"),client_lastname);
		return message;
	}

	public void setAppointmentNotificationHelper(
			AppointmentsNotificationHelper appointmentNotificationHelper) {
		this.appointmentNotificationHelper = appointmentNotificationHelper;
	}

	public AppointmentsNotificationHelper getAppointmentNotificationHelper() {
		return appointmentNotificationHelper;
	}

	public void setAppointment(Appointment appointment) {
		this.appointment = appointment;
	}

	public Appointment getAppointment() {
		return appointment;
	}

	public void setClient(Clients client) {
		this.client = client;
	}

	public Clients getClient() {
		return client;
	}

	public void setSettings(ShopSettings settings) {
		this.settings = settings;
	}

	public ShopSettings getSettings() {
		return settings;
	}

	public AppointmentsSMSNotificationHelper getAppointmentSMSNotificationHelper() {
		return appointmentSMSNotificationHelper;
	}

	public void setAppointmentSMSNotificationHelper(
			AppointmentsSMSNotificationHelper appointmentSMSNotificationHelper) {
		this.appointmentSMSNotificationHelper = appointmentSMSNotificationHelper;
	}
}
