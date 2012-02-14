package com.yourhairsalon.booking.messaging;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;

import javax.mail.internet.MimeUtility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;

import com.yourhairsalon.booking.form.AppointmentNotification;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import javax.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

public class JmsGeneralNotificationListQueueListener {
	private static final Log log = LogFactory.getLog(JmsGeneralNotificationListQueueListener.class);
	
	@Autowired
	private transient JavaMailSenderImpl mailTemplate;
	
//	@Autowired
//	private transient MailSender mailTemplate;
    /**
     * Send the email message
     * @param mailFrom
     * @param subject
     * @param mailTo
     * @param message
     */
	public void sendMessage(String mailFrom, String subject, String mailTo,
			String message) {
		log.debug("ENTERED JmsGeneralNotificationListQueueListener SENDMAIL");
//		mailFrom = "Schedule'em Reminder <donotreply@scheduleem.com>";
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
			// let's include the infamous windows Sample file (this time copied to c:/)
//			FileSystemResource res = new FileSystemResource(new File("c:/Sample.jpg"));
//			helper.addInline("identifier1234", res);
			if(getMailTemplate()==null){
				log.debug("!!!!!!!!!!   getMailTemplate() is NULL NULL NULL NULL !!!!!!!!!!!!!!!");
			}else{
				log.debug("username of mailTemplate: "+getMailTemplate().getUsername());
			}
			getMailTemplate().send(mime_message);
		}catch(Exception e){
			log.error(e.getMessage());
			log.debug("FAILED FAILED FAILED OOOOH THE AGONY OF FAILURE ");
		}
		log.debug("EXITING JmsGeneralNotificationListQueueListener SENDMAIL");
	}
	
    public void onMessage(Object message) {
    	// Ths is where the message from the queue is sent out via smtp
    	log.debug("ENTERED JmsGeneralNotificationListQueueListener onMessage");
    	AppointmentNotification appt = (AppointmentNotification)message;
        log.debug("JMS message received: " + appt.getClientname()+ " " + appt.getDescription());
        
        String msg = "";
        msg += appt.getDescription();
        msg += "\n";
        msg += appt.getShop_email_message();
        msg += "\n";
        msg += appt.getShop_email_signature();
        sendMessage(appt.getShop_email_address(), appt.getShop_email_subject(),appt.getEmail(), msg);
        log.debug("EXITING JmsGeneralNotificationListQueueListener onMessage");
    }

	public JavaMailSenderImpl getMailTemplate() {
		return mailTemplate;
	}

	public void setMailTemplate(JavaMailSenderImpl mailTemplate) {
		this.mailTemplate = mailTemplate;
	}
}
