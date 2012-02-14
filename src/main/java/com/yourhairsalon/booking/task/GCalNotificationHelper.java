package com.yourhairsalon.booking.task;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.beans.factory.annotation.Autowired;

public class GCalNotificationHelper {

    @Autowired
    private transient JmsTemplate jmsTemplate;

    public void sendMessage(Object messageObject) {
    	getJmsTemplate().convertAndSend(messageObject);
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

}
