package com.yourhairsalon.booking.domain;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import com.yourhairsalon.booking.domain.ServiceGroup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.TypedQuery;
import com.yourhairsalon.booking.domain.Appointment;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import org.springframework.roo.addon.json.RooJson;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooEntity(finders = { "findBaseServicesByShop" })
@RooJson
public class BaseService extends AbstractService {

//    @ManyToOne
//    private ServiceGroup service_group;
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
//        sb.append("Cost: ").append(getCost()).append(", ");
        sb.append(getDescription()).append(" ");
        if(getAmounttime() != null){
        	sb.append("(").append(getAmounttime()).append(")");
        }
//        sb.append("Finishtime: ").append(getFinishtime()).append(", ");
//        sb.append("Id: ").append(getId()).append(", ");
//        sb.append("Info_note: ").append(getInfo_note()).append(", ");
//        sb.append("Length_time: ").append(getLength_time()).append(", ");
//        sb.append("Minsetup: ").append(getMinsetup()).append(", ");
//        sb.append("Processtime: ").append(getProcesstime()).append(", ");
//        sb.append("Shop: ").append(getShop()).append(", ");
//        sb.append("Version: ").append(getVersion()).append(", ");
//        sb.append("SendReminders: ").append(isSendReminders());
        return sb.toString();
    }
	

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static BaseService fromJsonToBaseService(String json) {
        return new JSONDeserializer<BaseService>().use(null, BaseService.class).deserialize(json);
    }

	public static String toJsonArray2(Collection<BaseService> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<BaseService> fromJsonArrayToBaseServices(String json) {
        return new JSONDeserializer<List<BaseService>>().use(null, ArrayList.class).use("values", BaseService.class).deserialize(json);
    }

	public static long countBaseServices() {
        return entityManager().createQuery("SELECT COUNT(o) FROM BaseService o", Long.class).getSingleResult();
    }

	public static List<BaseService> findAllBaseServices() {
        return entityManager().createQuery("SELECT o FROM BaseService o", BaseService.class).getResultList();
    }

	public static BaseService findBaseService(Long id) {
        if (id == null) return null;
        return entityManager().find(BaseService.class, id);
    }

	public static List<BaseService> findBaseServiceEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM BaseService o", BaseService.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static TypedQuery<BaseService> findBaseServicesByShop(Shop shop) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        EntityManager em = BaseService.entityManager();
        TypedQuery<BaseService> q = em.createQuery("SELECT o FROM BaseService AS o WHERE o.shop = :shop", BaseService.class);
        q.setParameter("shop", shop);
        return q;
    }
}
