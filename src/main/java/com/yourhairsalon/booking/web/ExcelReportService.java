package com.yourhairsalon.booking.web;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.yourhairsalon.booking.domain.*;
import com.yourhairsalon.booking.form.ClientPlusNote;

public class ExcelReportService {
	
	private static final Log log = LogFactory.getLog(ExcelReportService.class);

	private static ArrayList<Clients> recordsList = new ArrayList();

	static {
		Clients firstClient = new Clients();
		firstClient.setFirstName("John");
		firstClient.setLastName("James");
		firstClient.setUsername("johnjames");
		firstClient.setId(1L);
		recordsList.add(firstClient);
		firstClient.setId(2L);
		recordsList.add(firstClient);
		firstClient.setId(3L);
		recordsList.add(firstClient);
		firstClient.setId(4L);
		recordsList.add(firstClient);
	}	
	
	public List<ClientPlusNote> getClientListPlusNotes(Shop shop){
		TypedQuery<Clients> clients = Clients.findClientsesByShop(shop);
		
		List<ClientPlusNote> clientpluslist = new ArrayList<ClientPlusNote>();
		
		
		for (Iterator it=clients.getResultList().iterator(); it.hasNext(); ) {
			Clients client = (Clients)it.next();
			String note = "";
	        // NOW GET LIST OF APPOINTMENTS FOR THIS CLIENT
	        TypedQuery<Appointment> appt_history = Appointment.findAppointmentsByShopAndClient(shop, client);
	        List<Appointment> appointList = appt_history.getResultList();
	        
			Collections.sort(appointList,
					 new Comparator<Appointment>() {
					
								@Override
								public int compare(Appointment e1, Appointment e2) {
									// the wizardy with Calendars below is to make
									// sure the Dates are the same for Month, day, year
									Calendar cal1 = e1.getBeginDateTime();
									Calendar cal2 = e2.getBeginDateTime();
									
									Calendar tmpcal = Calendar.getInstance();
									
									tmpcal.setTime(e1.getAppointmentDate());
									
									
									cal1.set(Calendar.DATE,tmpcal.get(Calendar.DATE));
									cal1.set(Calendar.MONTH,tmpcal.get(Calendar.MONTH));
									cal1.set(Calendar.YEAR,tmpcal.get(Calendar.YEAR));
									e1.setBeginDateTime(cal1);
									
									cal2.set(Calendar.DATE,tmpcal.get(Calendar.DATE));
									cal2.set(Calendar.MONTH,tmpcal.get(Calendar.MONTH));
									cal2.set(Calendar.YEAR,tmpcal.get(Calendar.YEAR));
									
									e2.setBeginDateTime(cal2);
									
									int i = e1.getAppointmentDate().compareTo(e2.getAppointmentDate());
									
									if(i == 0){
										i = e1.getBeginDateTime().compareTo(e2.getBeginDateTime());
									}
									return i;
								}
							}				
					);	        
	        if(appointList.size() > 0){
				// find the last appointment with a note
				Appointment appt = appointList.get(appointList.size()-1);
				log.debug("The toString of appt: "+ appt.toString());
				
				note = appt.getNotes();
					
	        }
			
			ClientPlusNote cplusnote = new ClientPlusNote();
			cplusnote.copyClient(client);
			cplusnote.setNotes(note);
			clientpluslist.add(cplusnote);
		}
		
		return clientpluslist;
	}
	
	public List<Payments> getTodaysPayments(String staffid,Shop shop){
		List<Payments> todaysPayments = new ArrayList<Payments>();
		log.debug("Entered getTodaysPayments");
		Staff comparestaff = null;
		log.debug("staffid: "+staffid);
		if(staffid != null && !staffid.equalsIgnoreCase("")){
			Long staff_id = Long.parseLong(staffid);
			comparestaff = Staff.findStaff(staff_id);
			if(comparestaff != null){
				log.debug("!!!!!  found staff: "+staffid);
			}
		}
		
		String shoptimezone = "";
		try{
			TypedQuery<ShopSettings> shopsettings = ShopSettings.findShopSettingsesByShop(shop);
			ShopSettings shopsetting = shopsettings.getResultList().get(0);
			shoptimezone = shopsetting.getTimezone();
			if(shoptimezone == null){
				shoptimezone = "MST";
				shopsetting.setTimezone(shoptimezone);
				shopsetting.merge();
			}
		}catch(Exception e){
			log.error(e);
		}
		TimeZone tz = TimeZone.getTimeZone(shoptimezone);
		Date dateValue = new Date();
		Calendar calValue = Calendar.getInstance(tz);
		calValue.setTime(dateValue);
		String selecteddate = "";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date beginofday = null;
		Date endofday = null;
		
		try {
			String dateString = df.format(calValue.getTime());
			endofday = df.parse(dateString);
			beginofday = df.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
			log.error(e);
		}
		endofday.setHours(23);
		endofday.setMinutes(59);

		beginofday.setHours(0);
		beginofday.setMinutes(0);
		

		TypedQuery<Payments> datas = null;
		if(comparestaff == null){
			log.debug("entered no staff");
			datas = com.yourhairsalon.booking.domain.Payments.findPaymentsesByShopAndDatecreatedBetween(shop,beginofday,endofday);
		}else{
			log.debug("entered staff");
			datas = com.yourhairsalon.booking.domain.Payments.findPaymentsesByShopAndDatecreatedBetweenAndStaffEquals(shop,beginofday,endofday,comparestaff);
		}
		List<Payments> list = datas.getResultList();
		log.debug("size of returned payments is: "+list.size());
		Float totalamount = 0.00F;
		Float totalgratuity = 0.00F;
		Float totalamountgratuity = 0.00F;

		for (Iterator i = list.iterator(); i.hasNext();) {
			Payments base = (Payments) i.next();
			float amount = base.getAmount();
			totalamount += amount;
			float grat = base.getGratuity();
			totalgratuity += grat;
		}		
		totalamountgratuity = totalgratuity + totalamount; 
		
		todaysPayments = datas.getResultList();
		
		log.debug("the number of payments returned: "+datas.getResultList().size());
		
		log.debug("Exiting getTodaysPayments");
		return todaysPayments;
	}
	public ArrayList<Clients> getRecordsList() {
		return recordsList;
	}

	public void setRecordsList(ArrayList<Clients> recordsList) {
		ExcelReportService.recordsList = recordsList;
	}
	
}
