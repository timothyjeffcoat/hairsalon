package com.yourhairsalon.booking.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.beans.factory.annotation.Autowired;


public class GeneralSMSNotificationHelper {
	private static final Log log = LogFactory.getLog(GeneralSMSNotificationHelper.class);
	
    @Autowired
    private transient JmsTemplate jmsTemplate;

    public void sendMessage(Object messageObject) {
    	log.debug("Entered GeneralSMSNotificationHelper sendMessage");
    	getJmsTemplate().convertAndSend(messageObject);
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

}
