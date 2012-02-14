package com.yourhairsalon.booking.messaging;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.mail.internet.MimeUtility;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;

import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarFeed;
import com.yourhairsalon.booking.domain.Appointment;
import com.yourhairsalon.booking.domain.Audit;
import com.yourhairsalon.booking.domain.GCal;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.ShopSettings;
import com.yourhairsalon.booking.domain.Staff;
import com.yourhairsalon.booking.form.AppointmentNotification;
import com.yourhairsalon.booking.form.GcalAppointmentNotification;
import com.yourhairsalon.booking.reference.ScheduleStatus;

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

public class JmsGCalSMSNotificationListQueueListener {
	private static final Log log = LogFactory.getLog(JmsGCalSMSNotificationListQueueListener.class);

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
		
    public void onMessage(Object message) {
    	// Ths is where the message from the queue is sent out via smtp
    	log.debug("ENTERED JmsGCalNotificationListQueueListener onMessage");
    	AppointmentNotification appt = (AppointmentNotification)message;
    	
		log.debug("JMS message received id:" + appt.getAppointmentid()+ " description: " + appt.getDescription());
		long apptid = appt.getAppointmentid();
		if(apptid > 0L){
			log.debug("LOOK AT ME 2");
			Appointment appointment = Appointment.findAppointment(apptid);
			if(appointment != null){
				log.debug("LOOK AT ME 4");
		    	String google_cal = "";
		    	String google_username = "";
		    	boolean sendtoque = false;
				try{
					TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(appointment.getShop());
					ShopSettings shopsetting = shopsettings.getResultList().get(0);
					boolean use_gcal = shopsetting.isUse_gcalendar_for_shop();
					boolean allow_staff_gcal = shopsetting.isAllow_staff_gcalendar();
					if(use_gcal || allow_staff_gcal){
						if(use_gcal){
							log.debug("LOOK AT ME 9");
							// SHOP RELATED
							TypedQuery<GCal> gals = GCal.findGCalsByShopEqualsAndStaffIsNull(appointment.getShop());
							if(gals != null && gals.getResultList().size() > 0){
								log.debug("LOOK AT ME 11");
//								GCal gal = gals.getResultList().get(0);
								google_cal = appt.getGoogle_cal_name();
								google_username = appt.getGoogle_cal_username();
								String google_password = appt.getGoogle_cal_password();
								// SEND TO QUE ONLY IF CREDENTIALS ARE NON NULL AND NON EMPTY
								if(google_cal != null && !google_cal.equalsIgnoreCase("") && google_username != null && !google_username.equalsIgnoreCase("") && google_password != null && !google_password.equalsIgnoreCase("")){
									sendtoque = true;
									
								    CalendarService myService = new CalendarService("gcal-jms-2");
								    String userName = google_username;
								    String userPassword = google_password;
								    // Create the necessary URL objects.
								    try {
								      metafeedUrl = new URL("https://www.google.com/calendar/feeds/default/private/full");
								      eventFeedUrl = new URL("https://www.google.com/calendar/feeds/"+google_cal+"/private/full");
								    } catch (MalformedURLException e) {
								      // Bad URL
								      log.error("Uh oh - you've got an invalid URL.");
								      log.error(e);
								    }
								    try {
										myService.setUserCredentials(userName,userPassword);
										CalendarEventEntry myEntry = new CalendarEventEntry();
										myEntry.setTitle(new PlainTextConstruct("Staff: "+appointment.getStaff().getFirstName()+" "+appointment.getStaff().getLastName()+" Client: "+appointment.getClient().getFirstName() + " " + appointment.getClient().getLastName()));
										myEntry.setContent(new PlainTextConstruct(appt.getGoogle_cal_message()));
										myEntry.setQuickAdd(false);
										myEntry.setWebContent(null);
										Calendar begin_calendar = appointment.getBeginDateTime();
										Calendar calendar = new GregorianCalendar();
										calendar.setTime(appointment.getAppointmentDate());
										calendar.set(calendar.HOUR_OF_DAY, begin_calendar.getTime().getHours());
										calendar.set(calendar.MINUTE,begin_calendar.getTime().getMinutes());
								        DateTime startTime = new DateTime(calendar.getTime(), TimeZone.getDefault());
								        Calendar end_calendar = appointment.getEndDateTime();
										Calendar calendar2 = new GregorianCalendar();
										calendar2.setTime(end_calendar.getTime());
										calendar2.set(calendar.HOUR_OF_DAY, end_calendar.getTime().getHours());
										calendar2.set(calendar.MINUTE,end_calendar.getTime().getMinutes());
										
								        DateTime endTime = new DateTime(calendar2.getTime(),TimeZone.getDefault());
								        When eventTimes = new When();
								        eventTimes.setStartTime(startTime);
								        eventTimes.setEndTime(endTime);
								        myEntry.addTime(eventTimes);
								        
								        // only create new event if appointment.getGcalid() is null
								        String appointment_gcalid = appointment.getGcalid();
								        log.debug("appointment_gcalid: "+appointment_gcalid);
								        if(appointment_gcalid == null){
								        	log.debug("TRYING TO CREATE NEW EVENT");
								        	myEntry.setId(google_cal);
											CalendarEventEntry calevententry = myService.insert(eventFeedUrl, myEntry);
											String calid = calevententry.getEditLink().getHref();
											log.debug("calid: " + calid);
											if(calid != null && !calid.equalsIgnoreCase("")){
												try{
													appointment.setGcalid(calid);
													appointment.merge();
													log.debug(" FUTURAMA 10");
												}catch(Exception ex){
													log.error(ex);
												}
											}
											saveAuditMessage(appointment.getShop(),"Added appointment to your google calendar for your shop",appointment.getStaff());
								        }else if(appointment.getStatus() == ScheduleStatus.CANCELED || appointment.getStatus() == ScheduleStatus.DELETED){
								        	log.debug("TRYING TO DELETE EVENT");
								        	try{
									        	URL editUrl = new URL(appointment_gcalid);
									        	CalendarEventEntry fetchedEntry = myService.getEntry(editUrl, CalendarEventEntry.class);
								        		log.debug("fetchedEntry: " + fetchedEntry.getTitle());
								        		fetchedEntry.delete();
												appointment.setGcalid(null);
												appointment.merge();
												log.debug(" FUTURAMA 11");
												saveAuditMessage(appointment.getShop(),"Deleted appointment to your google calendar for your shop",appointment.getStaff());
								        	}catch(Exception er){
								        		log.debug("trying fetchedEntry.delete();");
								        		log.error(er);
								        	}
								        	
								        }else{
									        // else either edit/update or delete calendar event
								        	log.debug("TRYING TO UPDATE EVENT");
								        	try{
									        	URL editUrl = new URL(appointment_gcalid);
									        	CalendarEventEntry fetchedEntry = myService.getEntry(editUrl, CalendarEventEntry.class);
								        		log.debug("fetchedEntry: " + fetchedEntry.getTitle());
								        		fetchedEntry.delete();
									        	CalendarEventEntry calevententry = myService.insert(eventFeedUrl, myEntry);
									        	String calid = calevententry.getEditLink().getHref();
												log.debug("calid: " + calid);
												if(calid != null && !calid.equalsIgnoreCase("")){
													try{
														appointment.setGcalid(calid);
														appointment.merge();
														log.debug(" FUTURAMA 12");
													}catch(Exception ex){
														log.error(ex);
													}
												}
												saveAuditMessage(appointment.getShop(),"Updated appointment in your google calendar for your shop",appointment.getStaff());
								        	}catch(Exception er){
								        		log.debug("trying fetchedEntry.delete(); 2");
								        		log.error(er);
								        	}

								        }

								        
								    }catch(Exception e){
								    	log.debug("NOT trying fetchedEntry.delete();");
								    	log.error(e);
								    }
									
									
									
									
									
								}
								
							}
						}
						if(allow_staff_gcal){
							log.debug("JmsGCalNotificationListQueueListener INSIDE ALLOW STAFF TO GCAL");
							// STAFF RELATED
							log.debug("JmsGCalNotificationListQueueListener ALLOW STAFF TO GCAL: " + appointment.getStaff().isUse_gcalendar());
							if(appointment.getStaff().isUse_gcalendar()){
								TypedQuery<GCal> orginal_gals = GCal.findGCalsByShopEqualsAndStaffEquals(appointment.getShop(), appointment.getStaff());
//								log.debug("orginal_gals.getResultList().size(): " + orginal_gals.getResultList().size());
//								GCal orig_gal = null; 
								if(orginal_gals != null && orginal_gals.getResultList().size() > 0){
//									orig_gal = orginal_gals.getResultList().get(0);
									google_cal = appt.getGoogle_cal_staff_name();
									google_username = appt.getGoogle_cal_staff_username();
									String google_password = appt.getGoogle_cal_staff_password();
									if(google_cal != null && !google_cal.equalsIgnoreCase("") && google_username != null && !google_username.equalsIgnoreCase("") && google_password != null && !google_password.equalsIgnoreCase("")){
										log.debug("JmsGCalNotificationListQueueListener ALLOW GCAL FOR STAFF IS SET TO TRUE");
										sendtoque = true;
										
									    CalendarService myService = new CalendarService("gcal-jms-STAFF");
										
									    String userName = google_username;
									    String userPassword = google_password;
									    // Create the necessary URL objects.
									    try {
									      metafeedUrl = new URL("https://www.google.com/calendar/feeds/default/private/full");
									      eventFeedUrl = new URL("https://www.google.com/calendar/feeds/"+google_cal+"/private/full");
									    } catch (MalformedURLException e) {
									      // Bad URL
									      log.error("JmsGCalNotificationListQueueListener STAFF Uh oh - you've got an invalid URL.");
									      log.error(e);
									    }
									    try {
											myService.setUserCredentials(userName,userPassword);
											CalendarEventEntry myEntry = new CalendarEventEntry();
											myEntry.setTitle(new PlainTextConstruct(appointment.getClient().getFirstName() + " " + appointment.getClient().getLastName()));
											myEntry.setContent(new PlainTextConstruct(appt.getGoogle_cal_staff_message()));
											myEntry.setQuickAdd(false);
											myEntry.setWebContent(null);
											Calendar begin_calendar = appointment.getBeginDateTime();
											Calendar calendar = new GregorianCalendar();
											calendar.setTime(appointment.getAppointmentDate());
											calendar.set(calendar.HOUR_OF_DAY, begin_calendar.getTime().getHours());
											calendar.set(calendar.MINUTE,begin_calendar.getTime().getMinutes());
									        DateTime startTime = new DateTime(calendar.getTime(), TimeZone.getDefault());
									        Calendar end_calendar = appointment.getEndDateTime();
											Calendar calendar2 = new GregorianCalendar();
											calendar2.setTime(end_calendar.getTime());
											calendar2.set(calendar.HOUR_OF_DAY, end_calendar.getTime().getHours());
											calendar2.set(calendar.MINUTE,end_calendar.getTime().getMinutes());
											
									        DateTime endTime = new DateTime(calendar2.getTime(),TimeZone.getDefault());
									        When eventTimes = new When();
									        eventTimes.setStartTime(startTime);
									        eventTimes.setEndTime(endTime);
									        myEntry.addTime(eventTimes);
									        
									        // only create new event if appointment.getGcalid() is null
									        String appointment_gcalid = appointment.getStaff_gcalid();
									        log.debug("JmsGCalNotificationListQueueListener STAFF appointment_gcalid: "+appointment_gcalid);
									        if(appointment_gcalid == null){
									        	log.debug("TRYING TO CREATE NEW EVENT STAFF");
												CalendarEventEntry calevententry = myService.insert(eventFeedUrl, myEntry);
												String calid = calevententry.getEditLink().getHref();
												log.debug(" STAFF calid: " + calid);
												if(calid != null && !calid.equalsIgnoreCase("")){
													try{
														appointment.setGcalid(calid);
														appointment.merge();
														log.debug(" STAFF FUTURAMA 10");
													}catch(Exception ex){
														log.error(ex);
													}
												}
												saveAuditMessage(appointment.getShop(),"Added appointment to your google calendar for staff",appointment.getStaff());
									        }else if(appointment.getStatus() == ScheduleStatus.CANCELED || appointment.getStatus() == ScheduleStatus.DELETED){
									        	log.debug("JmsGCalNotificationListQueueListener TRYING TO DELETE EVENT STAFF");
									        	try{
										        	URL editUrl = new URL(appointment_gcalid);
										        	CalendarEventEntry fetchedEntry = myService.getEntry(editUrl, CalendarEventEntry.class);
									        		log.debug(" STAFF fetchedEntry: " + fetchedEntry.getTitle());
									        		fetchedEntry.delete();
													appointment.setGcalid(null);
													appointment.merge();
													log.debug(" STAFF FUTURAMA 11");
													saveAuditMessage(appointment.getShop(),"Deleted appointment to your google calendar for staff",appointment.getStaff());
									        	}catch(Exception er){
									        		log.debug("trying fetchedEntry.delete();");
									        		log.error(er);
									        	}
									        	
									        }else{
										        // else either edit/update or delete calendar event
									        	log.debug("JmsGCalNotificationListQueueListener TRYING TO UPDATE EVENT STAFF");
									        	try{
										        	URL editUrl = new URL(appointment_gcalid);
										        	CalendarEventEntry fetchedEntry = myService.getEntry(editUrl, CalendarEventEntry.class);
									        		log.debug("JmsGCalNotificationListQueueListener STAFF fetchedEntry: " + fetchedEntry.getTitle());
									        		fetchedEntry.delete();
										        	CalendarEventEntry calevententry = myService.insert(eventFeedUrl, myEntry);
										        	String calid = calevententry.getEditLink().getHref();
													log.debug("calid: " + calid);
													if(calid != null && !calid.equalsIgnoreCase("")){
														try{
															appointment.setGcalid(calid);
															appointment.merge();
															log.debug("JmsGCalNotificationListQueueListener STAFF FUTURAMA 12");
														}catch(Exception ex){
															log.error(ex);
														}
													}
													saveAuditMessage(appointment.getShop(),"Updated appointment in your google calendar for staff",appointment.getStaff());
									        	}catch(Exception er){
									        		log.debug("JmsGCalNotificationListQueueListener STAFF trying fetchedEntry.delete(); 2");
									        		log.error(er);
									        	}

									        }

									        
									    }catch(Exception e){
									    	log.debug("JmsGCalNotificationListQueueListener NOT trying fetchedEntry.delete();");
									    	log.error(e);
									    }
										
										
									}
								}
							}
						}
					}
				}catch(Exception e){
					log.error(e);
				}
			}
		}
			
        log.debug("EXITING JmsGCalNotificationListQueueListener onMessage");
    }
    
    private String gcal(String username, String password){
		String response = " ";
	    CalendarService myService = new CalendarService("gcal-jms-2");
	
	    String userName = username;
	    String userPassword = password;
	
	    // Create the necessary URL objects.
	    try {
	      metafeedUrl = new URL(METAFEED_OWN_URL_BASE );
	      eventFeedUrl = new URL("https://www.google.com/calendar/feeds/timjeffcoat@gmail.com/private/full");
	    } catch (MalformedURLException e) {
	      // Bad URL
	      log.error("Uh oh - you've got an invalid URL.");
	      log.error(e);
	      response = "Uh oh - you've got an invalid URL.";
	    }
	    ArrayList map = new ArrayList();
	    try {
	      myService.setUserCredentials(userName, userPassword);
	
	      // Demonstrate retrieving a list of the user's calendars.
	      // Send the request and receive the response:
	      CalendarFeed resultFeed = myService.getFeed(metafeedUrl, CalendarFeed.class);
	
	      log.debug("Your calendars:");
	      log.debug("");
	      
	      for (int i = 0; i < resultFeed.getEntries().size(); i++) {
	        CalendarEntry entry = resultFeed.getEntries().get(i);
	        String newresponse = entry.getTitle().getPlainText();
			map.add(new String(newresponse));
	        
	        log.debug("<br/>" + entry.getTitle().getPlainText());
	      }
	      log.debug("");
	    }catch(Exception e){
	    	log.error(e);
	    }
	    return response;
    }
    
    

    /**
     * Helper method to create either single-instance or recurring events. For
     * simplicity, some values that might normally be passed as parameters (such
     * as author name, email, etc.) are hard-coded.
     * 
     * @param service An authenticated CalendarService object.
     * @param eventTitle Title of the event to create.
     * @param eventContent Text content of the event to create.
     * @param recurData Recurrence value for the event, or null for
     *        single-instance events.
     * @param isQuickAdd True if eventContent should be interpreted as the text of
     *        a quick add event.
     * @param wc A WebContent object, or null if this is not a web content event.
     * @return The newly-created CalendarEventEntry.
     * @throws ServiceException If the service is unable to handle the request.
     * @throws IOException Error communicating with the server.
     */
    private static CalendarEventEntry createEvent(CalendarService service,
        String eventTitle, String eventContent, String recurData,
        boolean isQuickAdd, WebContent wc) throws ServiceException, IOException {
      CalendarEventEntry myEntry = new CalendarEventEntry();

      myEntry.setTitle(new PlainTextConstruct(eventTitle));
      myEntry.setContent(new PlainTextConstruct(eventContent));
      myEntry.setQuickAdd(isQuickAdd);
      myEntry.setWebContent(wc);

      // If a recurrence was requested, add it. Otherwise, set the
      // time (the current date and time) and duration (30 minutes)
      // of the event.
      if (recurData == null) {
        Calendar calendar = new GregorianCalendar();
        DateTime startTime = new DateTime(calendar.getTime(), TimeZone
            .getDefault());

        calendar.add(Calendar.MINUTE, 30);
        DateTime endTime = new DateTime(calendar.getTime(), 
            TimeZone.getDefault());

        When eventTimes = new When();
        eventTimes.setStartTime(startTime);
        eventTimes.setEndTime(endTime);
        myEntry.addTime(eventTimes);
      } else {
        Recurrence recur = new Recurrence();
        recur.setValue(recurData);
        myEntry.setRecurrence(recur);
      }

      // Send the request and receive the response:
      return service.insert(eventFeedUrl, myEntry);
    }

    /**
     * Creates a single-occurrence event.
     * 
     * @param service An authenticated CalendarService object.
     * @param eventTitle Title of the event to create.
     * @param eventContent Text content of the event to create.
     * @return The newly-created CalendarEventEntry.
     * @throws ServiceException If the service is unable to handle the request.
     * @throws IOException Error communicating with the server.
     */
    private static CalendarEventEntry createSingleEvent(CalendarService service,
        String eventTitle, String eventContent) throws ServiceException,
        IOException {
      return createEvent(service, eventTitle, eventContent, null, false, null);
    }

    /**
     * Creates a quick add event.
     * 
     * @param service An authenticated CalendarService object.
     * @param quickAddContent The quick add text, including the event title, date
     *        and time.
     * @return The newly-created CalendarEventEntry.
     * @throws ServiceException If the service is unable to handle the request.
     * @throws IOException Error communicating with the server.
     */
    private static CalendarEventEntry createQuickAddEvent(
        CalendarService service, String quickAddContent) throws ServiceException,
        IOException {
      return createEvent(service, null, quickAddContent, null, true, null);
    }    
    
    

    /**
     * Updates the title of an existing calendar event.
     * 
     * @param entry The event to update.
     * @param newTitle The new title for this event.
     * @return The updated CalendarEventEntry object.
     * @throws ServiceException If the service is unable to handle the request.
     * @throws IOException Error communicating with the server.
     */
    private static CalendarEventEntry updateTitle(CalendarEventEntry entry,
        String newTitle) throws ServiceException, IOException {
      entry.setTitle(new PlainTextConstruct(newTitle));
      return entry.update();
    }

    /**
     * Adds a reminder to a calendar event.
     * 
     * @param entry The event to update.
     * @param numMinutes Reminder time, in minutes.
     * @param methodType Method of notification (e.g. email, alert, sms).
     * @return The updated EventEntry object.
     * @throws ServiceException If the service is unable to handle the request.
     * @throws IOException Error communicating with the server.
     */
    private static CalendarEventEntry addReminder(CalendarEventEntry entry,
        int numMinutes, Method methodType) throws ServiceException, IOException {
      Reminder reminder = new Reminder();
      reminder.setMinutes(numMinutes);
      reminder.setMethod(methodType);
      entry.getReminder().add(reminder);
     
      return entry.update();
    }

    /**
     * Adds an extended property to a calendar event.
     * 
     * @param entry The event to update.
     * @return The updated EventEntry object.
     * @throws ServiceException If the service is unable to handle the request.
     * @throws IOException Error communicating with the server.
     */
    private static CalendarEventEntry addExtendedProperty(
        CalendarEventEntry entry) throws ServiceException, IOException {
      // Add an extended property "id" with value 1234 to the EventEntry entry.
      // We specify the complete schema URL to avoid namespace collisions with
      // other applications that use the same property name.
      ExtendedProperty property = new ExtendedProperty();
      property.setName("http://www.example.com/schemas/2005#mycal.id");
      property.setValue("1234");

      entry.addExtension(property);

      return entry.update();
    }

    /**
     * Makes a batch request to delete all the events in the given list. If any of
     * the operations fails, the errors returned from the server are displayed.
     * The CalendarEntry objects in the list given as a parameters must be entries
     * returned from the server that contain valid edit links (for optimistic
     * concurrency to work). Note: You can add entries to a batch request for the
     * other operation types (INSERT, QUERY, and UPDATE) in the same manner as
     * shown below for DELETE operations.
     * 
     * @param service An authenticated CalendarService object.
     * @param eventsToDelete A list of CalendarEventEntry objects to delete.
     * @throws ServiceException If the service is unable to handle the request.
     * @throws IOException Error communicating with the server.
     */
    private static void deleteEvents(CalendarService service,
        List<CalendarEventEntry> eventsToDelete) throws ServiceException,
        IOException {

      // Add each item in eventsToDelete to the batch request.
      CalendarEventFeed batchRequest = new CalendarEventFeed();
      for (int i = 0; i < eventsToDelete.size(); i++) {
        CalendarEventEntry toDelete = eventsToDelete.get(i);
        // Modify the entry toDelete with batch ID and operation type.
        BatchUtils.setBatchId(toDelete, String.valueOf(i));
        BatchUtils.setBatchOperationType(toDelete, BatchOperationType.DELETE);
        batchRequest.getEntries().add(toDelete);
      }

      // Get the URL to make batch requests to
      CalendarEventFeed feed = service.getFeed(eventFeedUrl,
          CalendarEventFeed.class);
      Link batchLink = feed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
      URL batchUrl = new URL(batchLink.getHref());

      // Submit the batch request
      CalendarEventFeed batchResponse = service.batch(batchUrl, batchRequest);

      // Ensure that all the operations were successful.
      boolean isSuccess = true;
      for (CalendarEventEntry entry : batchResponse.getEntries()) {
        String batchId = BatchUtils.getBatchId(entry);
        if (!BatchUtils.isSuccess(entry)) {
          isSuccess = false;
          BatchStatus status = BatchUtils.getBatchStatus(entry);
          System.out.println("\n" + batchId + " failed (" + status.getReason()
              + ") " + status.getContent());
        }
      }
      if (isSuccess) {
        System.out.println("Successfully deleted all events via batch request.");
      }
    }    
    
	private void saveAuditMessage(Shop shop, String msg,Staff staff ){
		Audit audit = new Audit();
		audit.setShop(shop);
		audit.setDescription(msg);
		audit.setStaff(staff);
		audit.setTs(new Date());
		audit.merge();
	}

}
