package com.yourhairsalon.booking.messaging;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Properties;

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


public class JmsRegistrationNotificationListQueueListener {
	private static final Log log = LogFactory.getLog(JmsRegistrationNotificationListQueueListener.class);
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
		log.debug("ENTERED JmsRegistrationNotificationListQueueListener SENDMAIL");
		Properties properties = new Properties();
		String bcc = "tim@jeffcoat.net";
		try {
		    properties.load(this.getClass().getClassLoader().getResourceAsStream("application_reg.properties"));
		    bcc = properties.getProperty("registration_bcc");
		    
		} catch (IOException e) {
			log.error(e);
		}			
		
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
			if(bcc != null && !bcc.equalsIgnoreCase("")){
				helper.setBcc(bcc);
			}
			// use the true flag to indicate the text included is HTML
			helper.setText(message, true);
			getMailTemplate().send(mime_message);
		}catch(Exception e){
			log.error(e.getMessage());
			log.debug("FAILED FAILED FAILED OOOOH THE AGONY OF FAILURE ");
		}
		log.debug("EXITING JmsRegistrationNotificationListQueueListener SENDMAIL");
	}
	
    public void onMessage(Object message) {
    	// Ths is where the message from the queue is sent out via smtp
    	log.debug("ENTERED JmsRegistrationNotificationListQueueListener onMessage");
    	AppointmentNotification appt = (AppointmentNotification)message;
        log.debug("JMS message received: " + appt.getClientname()+ " " + appt.getDescription());
        
        SimpleDateFormat dateformatter = new SimpleDateFormat("EEEE MM/dd/yyyy");
        String appointmenttime = dateformatter.format(appt.getAppointmentdate());
        
        String msg = "";
        msg += appt.getClientname()+",";
        msg += "<br>";
        msg += appointmenttime;
        msg += "<br>";
        msg += appt.getDescription();
        msg += "<br>";
        msg += appt.getShop_email_message();
        
        msg += "<br>";
        msg += appt.getShop_email_signature();
        sendMessage(appt.getShop_email_address(), appt.getShop_email_subject(),appt.getEmail(), msg);
        log.debug("EXITING JmsRegistrationNotificationListQueueListener onMessage");
    }

	public JavaMailSenderImpl getMailTemplate() {
		return mailTemplate;
	}

	public void setMailTemplate(JavaMailSenderImpl mailTemplate) {
		this.mailTemplate = mailTemplate;
	}

}
