package com.yourhairsalon.booking.domain;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.roo.addon.json.RooJson;
import ca.digitalface.jasperoo.RooJasperoo;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooJson
@RooJasperoo
@RooEntity(finders = { "findStaffsByShop", "findStaffsById", "findStaffsByShopAndFirstNameEqualsAndLastNameEquals", "findStaffsByUsername", "findStaffsByUsernameEquals" })
public class Staff extends AbstractPerson {

    @NotNull
    private boolean use_gcalendar;
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
//        sb.append("Address: ").append(getAddress() == null ? "null" : getAddress().size()).append(", ");
//        sb.append("BirthDay: ").append(getBirthDay()).append(", ");
//        sb.append("Communication: ").append(getCommunication() == null ? "null" : getCommunication().size()).append(", ");
        sb.append(getFirstName()).append(" ");
//        sb.append("Id: ").append(getId()).append(", ");
        sb.append(getLastName());
//        sb.append("Shop: ").append(getShop()).append(", ");
//        sb.append("Username: ").append(getUsername()).append(", ");
//        sb.append("Version: ").append(getVersion()).append(", ");
//        sb.append("Use_gcalendar: ").append(isUse_gcalendar());
        return sb.toString();
    }
    

	public boolean isUse_gcalendar() {
        return this.use_gcalendar;
    }

	public void setUse_gcalendar(boolean use_gcalendar) {
        this.use_gcalendar = use_gcalendar;
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static Staff fromJsonToStaff(String json) {
        return new JSONDeserializer<Staff>().use(null, Staff.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Staff> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<Staff> fromJsonArrayToStaffs(String json) {
        return new JSONDeserializer<List<Staff>>().use(null, ArrayList.class).use("values", Staff.class).deserialize(json);
    }

	public static long countStaffs() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Staff o", Long.class).getSingleResult();
    }

	public static List<Staff> findAllStaffs() {
        return entityManager().createQuery("SELECT o FROM Staff o", Staff.class).getResultList();
    }

	public static Staff findStaff(Long id) {
        if (id == null) return null;
        return entityManager().find(Staff.class, id);
    }

	public static List<Staff> findStaffEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Staff o", Staff.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static TypedQuery<Staff> findStaffsById(Long id) {
        if (id == null) throw new IllegalArgumentException("The id argument is required");
        EntityManager em = Staff.entityManager();
        TypedQuery<Staff> q = em.createQuery("SELECT o FROM Staff AS o WHERE o.id = :id", Staff.class);
        q.setParameter("id", id);
        return q;
    }

	public static TypedQuery<Staff> findStaffsByShop(Shop shop) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        EntityManager em = Staff.entityManager();
        TypedQuery<Staff> q = em.createQuery("SELECT o FROM Staff AS o WHERE o.shop = :shop", Staff.class);
        q.setParameter("shop", shop);
        return q;
    }

	public static TypedQuery<Staff> findStaffsByShopAndFirstNameEqualsAndLastNameEquals(Shop shop, String firstName, String lastName) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (firstName == null || firstName.length() == 0) throw new IllegalArgumentException("The firstName argument is required");
        if (lastName == null || lastName.length() == 0) throw new IllegalArgumentException("The lastName argument is required");
        EntityManager em = Staff.entityManager();
        TypedQuery<Staff> q = em.createQuery("SELECT o FROM Staff AS o WHERE o.shop = :shop AND o.firstName = :firstName  AND o.lastName = :lastName", Staff.class);
        q.setParameter("shop", shop);
        q.setParameter("firstName", firstName);
        q.setParameter("lastName", lastName);
        return q;
    }

	public static TypedQuery<Staff> findStaffsByUsername(String username) {
        if (username == null || username.length() == 0) throw new IllegalArgumentException("The username argument is required");
        EntityManager em = Staff.entityManager();
        TypedQuery<Staff> q = em.createQuery("SELECT o FROM Staff AS o WHERE o.username = :username", Staff.class);
        q.setParameter("username", username);
        return q;
    }

	public static TypedQuery<Staff> findStaffsByUsernameEquals(String username) {
        if (username == null || username.length() == 0) throw new IllegalArgumentException("The username argument is required");
        EntityManager em = Staff.entityManager();
        TypedQuery<Staff> q = em.createQuery("SELECT o FROM Staff AS o WHERE o.username = :username", Staff.class);
        q.setParameter("username", username);
        return q;
    }

	public boolean isReportable() {
        return true;
    }
}
