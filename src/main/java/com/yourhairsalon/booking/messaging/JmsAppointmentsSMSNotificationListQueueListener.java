package com.yourhairsalon.booking.messaging;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.yourhairsalon.booking.domain.Appointment;
import com.yourhairsalon.booking.domain.Audit;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.Staff;
import com.yourhairsalon.booking.form.AppointmentNotification;
import com.nexmo.messaging.sdk.NexmoSmsClient;
import com.nexmo.messaging.sdk.NexmoSmsClientSSL;
import com.nexmo.messaging.sdk.SmsSubmissionResult;
import com.nexmo.messaging.sdk.messages.TextMessage;

public class JmsAppointmentsSMSNotificationListQueueListener {

	private static final Log log = LogFactory.getLog(JmsAppointmentsSMSNotificationListQueueListener.class);
	
	public static final String SMS_FROM = "12132633577";
	
    public static final String API_KEY = "5492cec3";
    public static final String API_SECRET = "700c0516";
    
    @Autowired
    private Audit audit;
    
    /**
     * creates an audit trail of outgoing messages
     * 
     * @param msg
     * @param staff
     * @param shop
     */
	private void saveAuditMessage(String msg,Staff staff,Shop shop,String type ){
		log.debug("Entered saveAuditMessage");
		log.debug("msg : " + msg);
		log.debug("getAudit(): "+getAudit());
		this.audit = new Audit();
		if(getAudit()!=null){
			getAudit().setShop(shop);
			getAudit().setDescription(msg);
			getAudit().setStaff(staff);
			getAudit().setTs(new Date());
			getAudit().setType(type);
			getAudit().persist();
		}
		log.debug("Exiting saveAuditMessage");
	}
    
    /**
     * send text messages via nexmo.com
     * 
     * @param SMS_FROM
     * @param SMS_TO
     * @param SMS_TEXT
     */
    private void sendTextMessage(String SMS_TO, String SMS_TEXT){
    	log.debug("Entered sendTextMessage");
    	log.debug("in class JmsAppointmentsSMSNotificationListQueListener");
        // Create a client for submitting to Nexmo

        NexmoSmsClient client = null;
        try {
            client = new NexmoSmsClientSSL(API_KEY, API_SECRET);
        } catch (Exception e) {
            log.error("Failed to instanciate a Nexmo Client");
            log.error(e);
//            throw new RuntimeException("Failed to instanciate a Nexmo Client");
            return;
        }

        // Create a Text SMS Message request object ...

        TextMessage message = new TextMessage(SMS_FROM, SMS_TO, SMS_TEXT);

        // Use the Nexmo client to submit the Text Message ...

        SmsSubmissionResult[] results = null;
        try {
            results = client.submitMessage(message);
        } catch (Exception e) {
            log.error("Failed to communicate with the Nexmo Client");
            log.error(e);
            throw new RuntimeException("Failed to communicate with the Nexmo Client");
        }

        // Evaluate the results of the submission attempt ...
        log.debug("... Message submitted in [ " + results.length + " ] parts");
        for (int i=0;i<results.length;i++) {
            log.debug("--------- part [ " + (i + 1) + " ] ------------");
            log.debug("Status [ " + results[i].getStatus() + " ] ...");
            if (results[i].getStatus() == SmsSubmissionResult.STATUS_OK){
                log.debug("SUCCESS");
            }else if (results[i].getTemporaryError()){
                log.debug("TEMPORARY FAILURE - PLEASE RETRY");
            }else{
                log.debug("SUBMISSION FAILED!");
            }
            log.debug("Message-Id [ " + results[i].getMessageId() + " ] ...");
            log.debug("Error-Text [ " + results[i].getErrorText() + " ] ...");

            if (results[i].getMessagePrice() != null)
                log.debug("Message-Price [ " + results[i].getMessagePrice() + " ] ...");
            if (results[i].getRemainingBalance() != null)
                log.debug("Remaining-Balance [ " + results[i].getRemainingBalance() + " ] ...");
        }
        log.debug("Exiting sendTextMessage");
    }    	

    /**
     * Send the text message
     * @param mailFrom
     * @param subject
     * @param mailTo
     * @param message
     */
    public void onMessage(Object message) {
    	// Ths is where the message from the queue is sent out via smtp
    	log.debug("ENTERED JmsAppointmentsSMSNotificationListQueueListener onMessage");
    	System.setProperty("file.encoding", "UTF-8");
    	AppointmentNotification appt = (AppointmentNotification)message;
        log.debug("JMS message received for: " + appt.getClientname()+ " " + appt.getDescription());
        String msg = "";
        
        SimpleDateFormat dateformatter = new SimpleDateFormat("EEEE MM/dd");
        SimpleDateFormat betimes = new SimpleDateFormat("hh:mm a");
        String appointmenttime = "";
        String begintime = "";
        String endtime = "";
        log.debug("attempting to create a Date object using :"+appt.getAppointmentdate());
        
        @SuppressWarnings("deprecation")
		Date adate = appt.getAppointmentdate();
        
        log.debug("attempting to create a Date object using :"+appt.getBegintime());
        @SuppressWarnings("deprecation")
        Date bdate = appt.getBegintime();
        
        log.debug("attempting to create a Date object using :"+appt.getEndtime());
        @SuppressWarnings("deprecation")
        Date edate = appt.getEndtime();
        
        appointmenttime = dateformatter.format(adate.getTime());
        log.debug("appointmenttime: "+appointmenttime);
        
        begintime = betimes.format(bdate.getTime());
        log.debug("begintime: "+begintime);
        
        endtime = betimes.format(edate.getTime());
        log.debug("endtime: "+endtime);
        
		msg += appt.getShop_email_message();

        log.debug("msg: "+msg);
        
        String sms_message = appt.getShop_sms_message();
        
        Appointment appointment = Appointment.findAppointment(appt.getAppointmentid());
        
        Shop shop = appointment.getShop();
        
        saveAuditMessage("Attempting to send message: "+sms_message,appointment.getStaff(),shop,"TEXT MESSAGE");
        
        try{
            saveAuditMessage("Attempting to send message: "+appt.getShop_sms_message(),appointment.getStaff(),shop,"TEXT MESSAGE");        	
        	sendTextMessage(appt.getShop_sms_to(), sms_message);
        	// log success
        	saveAuditMessage("Success sending message: "+appt.getShop_sms_message(),appointment.getStaff(),shop,"TEXT MESSAGE");
            // Now reduce the shop Number_sms_purchased by one and increase the number_sms_sent by one
            try {
				Long sms_purchased = shop.getNumber_sms_purchased();
				log.debug("sms_purchased: " + sms_purchased);
				Long sms_sent = shop.getNumber_sms_sent();
				log.debug("sms_sent: " + sms_sent);
				boolean update_sms_status = false;
				log.debug("test of sms_purchased > 0L: "+ (sms_purchased > 0L));
				if(sms_purchased != null && sms_purchased > 0L){
					log.debug("about to decrement value for sms_purchased");
					shop.setNumber_sms_purchased(sms_purchased-1);
					update_sms_status = true;
				}
				if(sms_sent != null && sms_sent > 0L){
					log.debug("about to increment value for sms_sent");
					shop.setNumber_sms_sent(sms_sent+1);
					update_sms_status = true;
				}
				log.debug("update_sms_status: "+update_sms_status);
				if(update_sms_status){
					log.debug("trying to update the status of sms purchased and sent");
					shop.merge();
				}
			} catch (Exception e) {
				log.error("Exception trying to update the number of sms messages purchased and sent");
				log.error(e);
			}
        }catch(Exception e){
        	saveAuditMessage("Failed to send message: "+sms_message,appointment.getStaff(),shop,"TEXT MESSAGE");
        	log.error(e);
        }
        log.debug("EXITING JmsAppointmentsSMSNotificationListQueueListener onMessage");
    }

	public Audit getAudit() {
		return audit;
	}

	public void setAudit(Audit audit) {
		this.audit = audit;
	}


}
