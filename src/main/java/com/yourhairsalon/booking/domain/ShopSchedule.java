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
@RooEntity(finders = { "findShopSchedulesByShop", "findShopSchedulesById" })
public class ShopSchedule extends AbstractSchedule {

	public boolean isReportable() {
        return true;
    }

	public static long countShopSchedules() {
        return entityManager().createQuery("SELECT COUNT(o) FROM ShopSchedule o", Long.class).getSingleResult();
    }

	public static List<ShopSchedule> findAllShopSchedules() {
        return entityManager().createQuery("SELECT o FROM ShopSchedule o", ShopSchedule.class).getResultList();
    }

	public static ShopSchedule findShopSchedule(Long id) {
        if (id == null) return null;
        return entityManager().find(ShopSchedule.class, id);
    }

	public static List<ShopSchedule> findShopScheduleEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM ShopSchedule o", ShopSchedule.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static TypedQuery<ShopSchedule> findShopSchedulesById(Long id) {
        if (id == null) throw new IllegalArgumentException("The id argument is required");
        EntityManager em = ShopSchedule.entityManager();
        TypedQuery<ShopSchedule> q = em.createQuery("SELECT o FROM ShopSchedule AS o WHERE o.id = :id", ShopSchedule.class);
        q.setParameter("id", id);
        return q;
    }

	public static TypedQuery<ShopSchedule> findShopSchedulesByShop(Shop shop) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        EntityManager em = ShopSchedule.entityManager();
        TypedQuery<ShopSchedule> q = em.createQuery("SELECT o FROM ShopSchedule AS o WHERE o.shop = :shop", ShopSchedule.class);
        q.setParameter("shop", shop);
        return q;
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static ShopSchedule fromJsonToShopSchedule(String json) {
        return new JSONDeserializer<ShopSchedule>().use(null, ShopSchedule.class).deserialize(json);
    }

	public static String toJsonArray(Collection<ShopSchedule> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<ShopSchedule> fromJsonArrayToShopSchedules(String json) {
        return new JSONDeserializer<List<ShopSchedule>>().use(null, ArrayList.class).use("values", ShopSchedule.class).deserialize(json);
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
