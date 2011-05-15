package com.yourhairsalon.booking.domain;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import com.yourhairsalon.booking.domain.ClientGroup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Min;
import org.springframework.roo.addon.json.RooJson;
import ca.digitalface.jasperoo.RooJasperoo;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooJson
@RooJasperoo
@RooEntity(finders = { "findClientsesByShop", "findClientsesById", "findClientsesByUsernameEquals", "findClientsesByShopAndFirstNameEqualsAndLastNameEquals" })
public class Clients extends AbstractPerson {

    @ManyToOne
    private ClientGroup client_group;

    @Column(name="accepts_notifications", columnDefinition = "bool default true")
    private Boolean accepts_notifications;
    @Column(name="accepts_receipts", columnDefinition = "bool default true")
    private Boolean accepts_receipts;
    @Column(name="accepts_initial", columnDefinition = "bool default true")
    private Boolean accepts_initial;

    
    @Column(name="accepts_sms_notifications", columnDefinition = "bool default false")
    private Boolean accepts_sms_notifications;
    @Column(name="accepts_sms_receipts", columnDefinition = "bool default false")
    private Boolean accepts_sms_receipts;
    @Column(name="accepts_sms_initial", columnDefinition = "bool default false")
    private Boolean accepts_sms_initial;
    
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getFirstName()).append(" ");
        sb.append(getLastName());
        return sb.toString();
    }

	public boolean isReportable() {
        return true;
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static Clients fromJsonToClients(String json) {
        return new JSONDeserializer<Clients>().use(null, Clients.class).deserialize(json);
    }

	public static String toJsonArray2(Collection<Clients> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<Clients> fromJsonArrayToClientses(String json) {
        return new JSONDeserializer<List<Clients>>().use(null, ArrayList.class).use("values", Clients.class).deserialize(json);
    }

	public static TypedQuery<Clients> findClientsesById(Long id) {
        if (id == null) throw new IllegalArgumentException("The id argument is required");
        EntityManager em = Clients.entityManager();
        TypedQuery<Clients> q = em.createQuery("SELECT o FROM Clients AS o WHERE o.id = :id", Clients.class);
        q.setParameter("id", id);
        return q;
    }

	public static TypedQuery<Clients> findClientsesByShop(Shop shop) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        EntityManager em = Clients.entityManager();
        TypedQuery<Clients> q = em.createQuery("SELECT o FROM Clients AS o WHERE o.shop = :shop", Clients.class);
        q.setParameter("shop", shop);
        return q;
    }

	public static TypedQuery<Clients> findClientsesByShopAndFirstNameEqualsAndLastNameEquals(Shop shop, String firstName, String lastName) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (firstName == null || firstName.length() == 0) throw new IllegalArgumentException("The firstName argument is required");
        if (lastName == null || lastName.length() == 0) throw new IllegalArgumentException("The lastName argument is required");
        EntityManager em = Clients.entityManager();
        TypedQuery<Clients> q = em.createQuery("SELECT o FROM Clients AS o WHERE o.shop = :shop AND o.firstName = :firstName  AND o.lastName = :lastName", Clients.class);
        q.setParameter("shop", shop);
        q.setParameter("firstName", firstName);
        q.setParameter("lastName", lastName);
        return q;
    }

	public static TypedQuery<Clients> findClientsesByUsernameEquals(String username) {
        if (username == null || username.length() == 0) throw new IllegalArgumentException("The username argument is required");
        EntityManager em = Clients.entityManager();
        TypedQuery<Clients> q = em.createQuery("SELECT o FROM Clients AS o WHERE o.username = :username", Clients.class);
        q.setParameter("username", username);
        return q;
    }

	public ClientGroup getClient_group() {
        return this.client_group;
    }

	public void setClient_group(ClientGroup client_group) {
        this.client_group = client_group;
    }

	public Boolean getAccepts_notifications() {
        return this.accepts_notifications;
    }

	public void setAccepts_notifications(Boolean accepts_notifications) {
        this.accepts_notifications = accepts_notifications;
    }

	public Boolean getAccepts_receipts() {
        return this.accepts_receipts;
    }

	public void setAccepts_receipts(Boolean accepts_receipts) {
        this.accepts_receipts = accepts_receipts;
    }

	public Boolean getAccepts_initial() {
        return this.accepts_initial;
    }

	public void setAccepts_initial(Boolean accepts_initial) {
        this.accepts_initial = accepts_initial;
    }

	
	
	
	
	public Boolean getAccepts_sms_notifications() {
        return this.accepts_sms_notifications;
    }

	public void setAccepts_sms_notifications(Boolean accepts_sms_notifications) {
        this.accepts_sms_notifications = accepts_sms_notifications;
    }

	public Boolean getAccepts_sms_receipts() {
        return this.accepts_sms_receipts;
    }

	public void setAccepts_sms_receipts(Boolean accepts_sms_receipts) {
        this.accepts_sms_receipts = accepts_sms_receipts;
    }

	public Boolean getAccepts_sms_initial() {
        return this.accepts_sms_initial;
    }

	public void setAccepts_sms_initial(Boolean accepts_sms_initial) {
        this.accepts_sms_initial = accepts_sms_initial;
    }
	
	
	
	public static long countClientses() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Clients o", Long.class).getSingleResult();
    }

	public static List<Clients> findAllClientses() {
        return entityManager().createQuery("SELECT o FROM Clients o", Clients.class).getResultList();
    }

	public static Clients findClients(Long id) {
        if (id == null) return null;
        return entityManager().find(Clients.class, id);
    }

	public static List<Clients> findClientsEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Clients o", Clients.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
