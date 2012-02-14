package com.yourhairsalon.booking.messaging;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.yourhairsalon.booking.domain.Appointment;
import com.yourhairsalon.booking.form.AppointmentNotification;

import com.nexmo.messaging.sdk.NexmoSmsClient;
import com.nexmo.messaging.sdk.NexmoSmsClientSSL;
import com.nexmo.messaging.sdk.SmsSubmissionResult;
import com.nexmo.messaging.sdk.messages.TextMessage;

public class JmsAppointmentsNotificationListQueueListener {

	private static final Log log = LogFactory.getLog(JmsAppointmentsNotificationListQueueListener.class);
	
	
    public static final String API_KEY = "5492cec3";
    public static final String API_SECRET = "700c0516";

    public static final String SMS_FROM = "Schedule'em";
    public static final String SMS_TO = "8017872326";
    public static final String SMS_TEXT = "Don't forget your appointment";
	
//	@Autowired
//	private transient MailSender mailTemplate;
	@Autowired
	private transient JavaMailSenderImpl mailTemplate;
	/**
     * Send the email message
     * @param mailFrom
     * @param subject
     * @param mailTo
     * @param message
     */
	public void sendMessage(String mailFrom, String subject, String mailTo,
			String message) {
		log.debug("ENTERED SENDMAIL");
		
		log.debug("mailFrom: "+mailFrom);
		log.debug("subject: "+subject);
		log.debug("mailTo: "+mailTo);
		log.debug("message: "+message);
		try{
			MimeMessage mime_message = getMailTemplate().createMimeMessage();
			// use the true flag to indicate you need a multipart message
			MimeMessageHelper helper = new MimeMessageHelper(mime_message, true);
			helper.setFrom(mailFrom);
			helper.setSubject(subject);
			helper.setTo(mailTo);
			// use the true flag to indicate the text included is HTML
			helper.setText(message, true);

			getMailTemplate().send(mime_message);
		}catch(Exception e){
			log.error(e.getMessage());
			log.debug("FAILED FAILED FAILED OOOOH THE AGONY OF FAILURE ");
		}
		
		log.debug("EXITING SENDMAIL");
	}
	
    public void onMessage(Object message) {
    	// Ths is where the message from the queue is sent out via smtp
    	log.debug("ENTERED JmsAppointmentsNotificationListQueueListener onMessage");
    	AppointmentNotification appt = (AppointmentNotification)message;
        log.debug("JMS message received for: " + appt.getClientname()+ " " + appt.getDescription());
        String msg = "";
//        msg += appt.getClientname()+",";
//        msg += "\n";
        
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

        sendMessage(appt.getShop_email_address(), appt.getShop_email_subject(),appt.getEmail(), msg);
        log.debug("EXITING JmsAppointmentsNotificationListQueueListener onMessage");
    }

    private void sendTextMessage(){
    	
        // Create a client for submitting to Nexmo

        NexmoSmsClient client = null;
        try {
            client = new NexmoSmsClientSSL(API_KEY, API_SECRET);
        } catch (Exception e) {
            System.err.println("Failed to instanciate a Nexmo Client");
            e.printStackTrace();
            throw new RuntimeException("Failed to instanciate a Nexmo Client");
        }

        // Create a Text SMS Message request object ...

        TextMessage message = new TextMessage(SMS_FROM, SMS_TO, SMS_TEXT);

        // Use the Nexmo client to submit the Text Message ...

        SmsSubmissionResult[] results = null;
        try {
            results = client.submitMessage(message);
        } catch (Exception e) {
            System.err.println("Failed to communicate with the Nexmo Client");
            e.printStackTrace();
            throw new RuntimeException("Failed to communicate with the Nexmo Client");
        }

        // Evaluate the results of the submission attempt ...
        log.debug("... Message submitted in [ " + results.length + " ] parts");
        for (int i=0;i<results.length;i++) {
            log.debug("--------- part [ " + (i + 1) + " ] ------------");
            log.debug("Status [ " + results[i].getStatus() + " ] ...");
            if (results[i].getStatus() == SmsSubmissionResult.STATUS_OK)
                log.debug("SUCCESS");
            else if (results[i].getTemporaryError())
                log.debug("TEMPORARY FAILURE - PLEASE RETRY");
            else
                log.debug("SUBMISSION FAILED!");
            log.debug("Message-Id [ " + results[i].getMessageId() + " ] ...");
            log.debug("Error-Text [ " + results[i].getErrorText() + " ] ...");

            if (results[i].getMessagePrice() != null)
                log.debug("Message-Price [ " + results[i].getMessagePrice() + " ] ...");
            if (results[i].getRemainingBalance() != null)
                log.debug("Remaining-Balance [ " + results[i].getRemainingBalance() + " ] ...");
        }
    }    
	public JavaMailSenderImpl getMailTemplate() {
		return mailTemplate;
	}

	public void setMailTemplate(JavaMailSenderImpl mailTemplate) {
		this.mailTemplate = mailTemplate;
	}

//	public void setMailTemplate(MailSender mailTemplate) {
//		this.mailTemplate = mailTemplate;
//	}
//
//	public MailSender getMailTemplate() {
//		return mailTemplate;
//	}
}
