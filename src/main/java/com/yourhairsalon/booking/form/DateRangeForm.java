package com.yourhairsalon.booking.form;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DateRangeForm {
	private static final Log log = LogFactory.getLog(DateRangeForm.class);
	
	private Date begindate;
	private Date enddate;
	private String sbegindate;
	private String senddate;
	
	public Date convertStringToDate(String stringdate) {
		log.debug("ENTERED convertStringToDate");
		log.debug("string to be converted to date: "+stringdate);
		Date newdate = new Date();
		try {
			String str_date = stringdate;
			DateFormat formatter;
			Date date;
			formatter = new SimpleDateFormat("MM-dd-yyyy");
			newdate = (Date) formatter.parse(str_date);
			log.debug("converted date: "+newdate.toString());
		} catch (ParseException e) {
			log.error(e.getMessage());
		}
		log.debug("EXITING convertStringToDate");
		return newdate;
	}
	
	public void setBegindate(Date begindate) {
		this.begindate = begindate;
	}
	public Date getBegindate() {
		return convertStringToDate(sbegindate);
	}
	public void setEnddate(Date enddate) {
		this.enddate = enddate;
	}
	public Date getEnddate() {
		return convertStringToDate(senddate);
	}
	public void setSbegindate(String sbegindate) {
		this.sbegindate = sbegindate;
	}
	public String getSbegindate() {
		return sbegindate;
	}
	public void setSenddate(String senddate) {
		this.senddate = senddate;
	}
	public String getSenddate() {
		return senddate;
	}
}
