package com.yourhairsalon.booking.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.transaction.annotation.Transactional;
import ca.digitalface.jasperoo.RooJasperoo;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@Entity
@Configurable
@RooJavaBean
@RooToString
@RooJson
@RooJasperoo
@RooEntity(finders = { "findSchedulesById" })
public class Schedule {

	public static TypedQuery<Schedule> findSchedulesById(Long id) {
        if (id == null) throw new IllegalArgumentException("The id argument is required");
        EntityManager em = Schedule.entityManager();
        TypedQuery<Schedule> q = em.createQuery("SELECT o FROM Schedule AS o WHERE o.id = :id", Schedule.class);
        q.setParameter("id", id);
        return q;
    }

	@PersistenceContext
    transient EntityManager entityManager;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Version
    @Column(name = "version")
    private Integer version;

	public Long getId() {
        return this.id;
    }

	public void setId(Long id) {
        this.id = id;
    }

	public Integer getVersion() {
        return this.version;
    }

	public void setVersion(Integer version) {
        this.version = version;
    }

	@Transactional
    public void persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }

	@Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            Schedule attached = Schedule.findSchedule(this.id);
            this.entityManager.remove(attached);
        }
    }

	@Transactional
    public void flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }

	@Transactional
    public void clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }

	@Transactional
    public Schedule merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Schedule merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new Schedule().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countSchedules() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Schedule o", Long.class).getSingleResult();
    }

	public static List<Schedule> findAllSchedules() {
        return entityManager().createQuery("SELECT o FROM Schedule o", Schedule.class).getResultList();
    }

	public static Schedule findSchedule(Long id) {
        if (id == null) return null;
        return entityManager().find(Schedule.class, id);
    }

	public static List<Schedule> findScheduleEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Schedule o", Schedule.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public boolean isReportable() {
        return true;
    }

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Version: ").append(getVersion());
        return sb.toString();
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static Schedule fromJsonToSchedule(String json) {
        return new JSONDeserializer<Schedule>().use(null, Schedule.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Schedule> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<Schedule> fromJsonArrayToSchedules(String json) {
        return new JSONDeserializer<List<Schedule>>().use(null, ArrayList.class).use("values", Schedule.class).deserialize(json);
    }
}
