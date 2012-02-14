package com.yourhairsalon.booking.task;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestParam;

import com.paypal.api.payments.Payment;

public interface PayPalService {
	
	public Payment createPayment(
			String payerid,
			String guid,
			String returnURL,
			String cancelURL,
			HttpServletResponse resp
			);
	
}
