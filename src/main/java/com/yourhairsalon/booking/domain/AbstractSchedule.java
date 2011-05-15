package com.yourhairsalon.booking.domain;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import com.yourhairsalon.booking.reference.ScheduleStatus;
import javax.persistence.Enumerated;
import com.yourhairsalon.booking.domain.Shop;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Configurable
@RooJavaBean
@RooToString
@RooEntity
@RooJson
public abstract class AbstractSchedule {

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date schedule_date;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date start_time;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date end_time;

    @NotNull
    @Enumerated
    private ScheduleStatus schedule_status;

    @NotNull
    @ManyToOne
    private Shop shop;

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static AbstractSchedule fromJsonToAbstractSchedule(String json) {
        return new JSONDeserializer<AbstractSchedule>().use(null, AbstractSchedule.class).deserialize(json);
    }

//	public static String toJsonArray(Collection<AbstractSchedule> collection) {
//        return new JSONSerializer().exclude("*.class").serialize(collection);
//    }

	public static Collection<AbstractSchedule> fromJsonArrayToAbstractSchedules(String json) {
        return new JSONDeserializer<List<AbstractSchedule>>().use(null, ArrayList.class).use("values", AbstractSchedule.class).deserialize(json);
    }

	public Date getSchedule_date() {
        return this.schedule_date;
    }

	public void setSchedule_date(Date schedule_date) {
        this.schedule_date = schedule_date;
    }

	public Date getStart_time() {
        return this.start_time;
    }

	public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

	public Date getEnd_time() {
        return this.end_time;
    }

	public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }

	public ScheduleStatus getSchedule_status() {
        return this.schedule_status;
    }

	public void setSchedule_status(ScheduleStatus schedule_status) {
        this.schedule_status = schedule_status;
    }

	public Shop getShop() {
        return this.shop;
    }

	public void setShop(Shop shop) {
        this.shop = shop;
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
            AbstractSchedule attached = AbstractSchedule.findAbstractSchedule(this.id);
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
    public AbstractSchedule merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        AbstractSchedule merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new AbstractSchedule() {
        }.entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countAbstractSchedules() {
        return entityManager().createQuery("SELECT COUNT(o) FROM AbstractSchedule o", Long.class).getSingleResult();
    }

	public static List<AbstractSchedule> findAllAbstractSchedules() {
        return entityManager().createQuery("SELECT o FROM AbstractSchedule o", AbstractSchedule.class).getResultList();
    }

	public static AbstractSchedule findAbstractSchedule(Long id) {
        if (id == null) return null;
        return entityManager().find(AbstractSchedule.class, id);
    }

	public static List<AbstractSchedule> findAbstractScheduleEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM AbstractSchedule o", AbstractSchedule.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
