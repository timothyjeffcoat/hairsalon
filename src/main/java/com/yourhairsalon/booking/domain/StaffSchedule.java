package com.yourhairsalon.booking.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
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
@RooEntity(finders = { "findStaffSchedulesByShop", "findStaffSchedulesById" })
public class StaffSchedule extends AbstractSchedule {

	public boolean isReportable() {
        return true;
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static StaffSchedule fromJsonToStaffSchedule(String json) {
        return new JSONDeserializer<StaffSchedule>().use(null, StaffSchedule.class).deserialize(json);
    }

	public static String toJsonArray(Collection<StaffSchedule> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<StaffSchedule> fromJsonArrayToStaffSchedules(String json) {
        return new JSONDeserializer<List<StaffSchedule>>().use(null, ArrayList.class).use("values", StaffSchedule.class).deserialize(json);
    }

	public static TypedQuery<StaffSchedule> findStaffSchedulesById(Long id) {
        if (id == null) throw new IllegalArgumentException("The id argument is required");
        EntityManager em = StaffSchedule.entityManager();
        TypedQuery<StaffSchedule> q = em.createQuery("SELECT o FROM StaffSchedule AS o WHERE o.id = :id", StaffSchedule.class);
        q.setParameter("id", id);
        return q;
    }

	public static TypedQuery<StaffSchedule> findStaffSchedulesByShop(Shop shop) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        EntityManager em = StaffSchedule.entityManager();
        TypedQuery<StaffSchedule> q = em.createQuery("SELECT o FROM StaffSchedule AS o WHERE o.shop = :shop", StaffSchedule.class);
        q.setParameter("shop", shop);
        return q;
    }

	public static long countStaffSchedules() {
        return entityManager().createQuery("SELECT COUNT(o) FROM StaffSchedule o", Long.class).getSingleResult();
    }

	public static List<StaffSchedule> findAllStaffSchedules() {
        return entityManager().createQuery("SELECT o FROM StaffSchedule o", StaffSchedule.class).getResultList();
    }

	public static StaffSchedule findStaffSchedule(Long id) {
        if (id == null) return null;
        return entityManager().find(StaffSchedule.class, id);
    }

	public static List<StaffSchedule> findStaffScheduleEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM StaffSchedule o", StaffSchedule.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("End_time: ").append(getEnd_time()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Schedule_date: ").append(getSchedule_date()).append(", ");
        sb.append("Schedule_status: ").append(getSchedule_status()).append(", ");
        sb.append("Shop: ").append(getShop()).append(", ");
        sb.append("Start_time: ").append(getStart_time()).append(", ");
        sb.append("Version: ").append(getVersion());
        return sb.toString();
    }
}
