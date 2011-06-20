package com.yourhairsalon.booking.form;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.yourhairsalon.booking.domain.Addresses;
import com.yourhairsalon.booking.domain.Clients;
import com.yourhairsalon.booking.domain.Communications;
import com.yourhairsalon.booking.reference.CommType;

public class ClientPlusNote extends FullClientForm{
	private String notes;

    /**
     * Grab the client's cell phone number
     * 
     * @param client
     * @return
     */
    private String getCellPhoneFromClient( Clients client){
    	String phonenumber = "";
    	
		Set<Communications> comm = client.getCommunication();
		int sz = comm.size();
		
		
		for (Iterator it=comm.iterator(); it.hasNext(); ) {
			Communications element = (Communications)it.next();
			String comval = element.getCommunication_value();
			if(comval!=null){
				if(element.getCommunication_type().name().equalsIgnoreCase(CommType.CELL_PHONE.name())){
					phonenumber = comval;
					break;
				}
			}
		}
    	return phonenumber;
    }	

    private String getEmailFromClient( Clients client){
    	String phonenumber = "";
    	
		Set<Communications> comm = client.getCommunication();
		int sz = comm.size();
		
		
		for (Iterator it=comm.iterator(); it.hasNext(); ) {
			Communications element = (Communications)it.next();
			String comval = element.getCommunication_value();
			if(comval!=null){
				if(element.getCommunication_type().name().equalsIgnoreCase(CommType.EMAIL.name())){
					phonenumber = comval;
					break;
				}
			}
		}
    	return phonenumber;
    }	
    
    private String getHomephoneFromClient( Clients client){
    	String phonenumber = "";
    	
		Set<Communications> comm = client.getCommunication();
		int sz = comm.size();
		
		
		for (Iterator it=comm.iterator(); it.hasNext(); ) {
			Communications element = (Communications)it.next();
			String comval = element.getCommunication_value();
			if(comval!=null){
				if(element.getCommunication_type().name().equalsIgnoreCase(CommType.HOME_PHONE.name())){
					phonenumber = comval;
					break;
				}
			}
		}
    	return phonenumber;
    }	
    private String getWorkphoneFromClient( Clients client){
    	String phonenumber = "";
    	
		Set<Communications> comm = client.getCommunication();
		int sz = comm.size();
		
		
		for (Iterator it=comm.iterator(); it.hasNext(); ) {
			Communications element = (Communications)it.next();
			String comval = element.getCommunication_value();
			if(comval!=null){
				if(element.getCommunication_type().name().equalsIgnoreCase(CommType.HOME_PHONE.name())){
					phonenumber = comval;
					break;
				}
			}
		}
    	return phonenumber;
    }	
    /**
	 * copy the contents of a Clients into the  ClientPlusNote fields
	 * @param client
	 */
	public void copyClient(Clients client){
		
		this.setFirstName(client.getFirstName());
		this.setBirthDate(client.getBirthDay());
		if(client.getBirthDay() != null) this.setBirthDay(client.getBirthDay().toString());
		this.setCell_phone(getCellPhoneFromClient(client));
		this.setEmail(getEmailFromClient(client));
		this.setHome_phone(getHomephoneFromClient(client));
		this.setLastName(client.getLastName());
		this.setWork_phone(getWorkphoneFromClient(client));
		
		List<Addresses> addr = Addresses.findAddressesesByPerson(client).getResultList();
		if(addr.size()>0){
			this.setAddress1(addr.get(0).getAddress1());
			this.setAddress2(addr.get(0).getAddress2());
			this.setCitycode(addr.get(0).getCitycode());
			this.setStatecode(addr.get(0).getStatecode());
			this.setZipcode(addr.get(0).getZipcode());
		}
		
		
		
	}
	
	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
