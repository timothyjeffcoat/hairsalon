package com.yourhairsalon.booking.form;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.yourhairsalon.booking.domain.Appointment;
import com.yourhairsalon.booking.domain.BaseService;
import com.yourhairsalon.booking.domain.CustomService;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class ApptSvcJson {
	private static final Log log = LogFactory.getLog(ApptSvcJson.class);
    
    private List<BaseService> services = new ArrayList<BaseService>();

    public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }
    
    public static ApptSvcJson fromJsonToAppointmentDeep(String json) {
        return new JSONDeserializer<ApptSvcJson>().use(null, ApptSvcJson.class).deserialize(json);
    }
    
    public static String toJsonArray(Collection<ApptSvcJson> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }
    
    public static Collection<ApptSvcJson> fromJsonArrayToAppointments(String json) {
        return new JSONDeserializer<List<ApptSvcJson>>().use(null, ArrayList.class).use("values", ApptSvcJson.class).deserialize(json);
    }
    
    

	public ApptSvcJson(){
		
	}
	
	/** 
	 * accept the Appointment and extracts values and assigns to this class
	 *  
	 * @param appointment
	 */
	public ApptSvcJson(Appointment appointment){
		
		Set<BaseService> svc = appointment.getServices();
		Iterator<BaseService> svcIter = svc.iterator();
		List<BaseService> svclist = new ArrayList<BaseService>();
		int svccounter = 0;
		while (svcIter.hasNext()) {
			BaseService bs = svcIter.next();
			String servicetype = "base";
			if(bs instanceof CustomService){
				log.debug("This BaseService is a CustomService");
				servicetype = "custom";
			}
			log.debug("The id for the service "+svccounter+" is: "+bs.getId());
			
			
			svclist.add(bs);
			svccounter++;
		}		
		setServices(svclist);
		
	}

	public void setServices(List<BaseService> services) {
		this.services = services;
	}

	public List<BaseService> getServices() {
		return services;
	}
	

}
