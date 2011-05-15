package com.yourhairsalon.booking.domain;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.roo.addon.json.RooJson;
import com.yourhairsalon.booking.domain.Clients;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.TypedQuery;
import com.yourhairsalon.booking.domain.Appointment;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import javax.validation.constraints.Min;

@Entity
@Configurable
@RooJavaBean
@RooToString
@RooJson
@RooEntity(finders = { "findCustomServicesByShopAndClient", "findCustomServicesByShopAndClientAndOriginalid", "findCustomServicesByClient", "findCustomServicesByShop" })
public class CustomService extends BaseService {

    @NotNull
    @ManyToOne
    private Clients client;

    @NotNull
    private Long originalid;

	public static long countCustomServices() {
        return entityManager().createQuery("SELECT COUNT(o) FROM CustomService o", Long.class).getSingleResult();
    }

	public static List<CustomService> findAllCustomServices() {
        return entityManager().createQuery("SELECT o FROM CustomService o", CustomService.class).getResultList();
    }

	public static CustomService findCustomService(Long id) {
        if (id == null) return null;
        return entityManager().find(CustomService.class, id);
    }

	public static List<CustomService> findCustomServiceEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM CustomService o", CustomService.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static TypedQuery<CustomService> findCustomServicesByClient(Clients client) {
        if (client == null) throw new IllegalArgumentException("The client argument is required");
        EntityManager em = CustomService.entityManager();
        TypedQuery<CustomService> q = em.createQuery("SELECT o FROM CustomService AS o WHERE o.client = :client", CustomService.class);
        q.setParameter("client", client);
        return q;
    }

	public static TypedQuery<CustomService> findCustomServicesByShop(Shop shop) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        EntityManager em = CustomService.entityManager();
        TypedQuery<CustomService> q = em.createQuery("SELECT o FROM CustomService AS o WHERE o.shop = :shop", CustomService.class);
        q.setParameter("shop", shop);
        return q;
    }

	public static TypedQuery<CustomService> findCustomServicesByShopAndClient(Shop shop, Clients client) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (client == null) throw new IllegalArgumentException("The client argument is required");
        EntityManager em = CustomService.entityManager();
        TypedQuery<CustomService> q = em.createQuery("SELECT o FROM CustomService AS o WHERE o.shop = :shop AND o.client = :client", CustomService.class);
        q.setParameter("shop", shop);
        q.setParameter("client", client);
        return q;
    }

	public static TypedQuery<CustomService> findCustomServicesByShopAndClientAndOriginalid(Shop shop, Clients client, Long originalid) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (client == null) throw new IllegalArgumentException("The client argument is required");
        if (originalid == null) throw new IllegalArgumentException("The originalid argument is required");
        EntityManager em = CustomService.entityManager();
        TypedQuery<CustomService> q = em.createQuery("SELECT o FROM CustomService AS o WHERE o.shop = :shop AND o.client = :client AND o.originalid = :originalid", CustomService.class);
        q.setParameter("shop", shop);
        q.setParameter("client", client);
        q.setParameter("originalid", originalid);
        return q;
    }

	public Clients getClient() {
        return this.client;
    }

	public void setClient(Clients client) {
        this.client = client;
    }

	public Long getOriginalid() {
        return this.originalid;
    }

	public void setOriginalid(Long originalid) {
        this.originalid = originalid;
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static CustomService fromJsonToCustomService(String json) {
        return new JSONDeserializer<CustomService>().use(null, CustomService.class).deserialize(json);
    }

	public static String toJsonArray(Collection<CustomService> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<CustomService> fromJsonArrayToCustomServices(String json) {
        return new JSONDeserializer<List<CustomService>>().use(null, ArrayList.class).use("values", CustomService.class).deserialize(json);
    }

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Amounttime: ").append(getAmounttime()).append(", ");
        sb.append("Client: ").append(getClient()).append(", ");
        sb.append("Cost: ").append(getCost()).append(", ");
        sb.append("Description: ").append(getDescription()).append(", ");
        sb.append("Finishtime: ").append(getFinishtime()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Info_note: ").append(getInfo_note()).append(", ");
        sb.append("Length_time: ").append(getLength_time()).append(", ");
        sb.append("Minsetup: ").append(getMinsetup()).append(", ");
        sb.append("Originalid: ").append(getOriginalid()).append(", ");
        sb.append("Processtime: ").append(getProcesstime()).append(", ");
        sb.append("Shop: ").append(getShop()).append(", ");
        sb.append("Version: ").append(getVersion()).append(", ");
        sb.append("SendReminders: ").append(isSendReminders());
        return sb.toString();
    }
}
