package com.yourhairsalon.booking.domain;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.Min;
import com.yourhairsalon.booking.domain.Shop;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Configurable
@RooJavaBean
@RooToString
@RooEntity(inheritanceType = "TABLE_PER_CLASS")
@RooJson
public abstract class AbstractService {

    @NotNull
    private boolean sendReminders;

    @Size(min = 3, max = 30)
    private String description;

    @NotNull
    @Min(0L)
    private Integer processtime;

    @NotNull
    @Min(0L)
    private Integer finishtime;

    @NotNull
    @Min(0L)
    private Integer minsetup;

    @NotNull
    @Min(0L)
    private Float cost;

    @NotNull
    @Min(0L)
    private Integer amounttime;

    @NotNull
    @Min(0L)
    private Integer length_time;

    @Size(min = 0, max = 250)
    private String info_note;

    @NotNull
    @ManyToOne
    private Shop shop;
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
//        sb.append("Amounttime: ").append(getAmounttime()).append(", ");
//        sb.append("Cost: ").append(getCost()).append(", ");
        sb.append("Description: ").append(getDescription());
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
    

	public boolean isSendReminders() {
        return this.sendReminders;
    }

	public void setSendReminders(boolean sendReminders) {
        this.sendReminders = sendReminders;
    }

	public String getDescription() {
        return this.description;
    }

	public void setDescription(String description) {
        this.description = description;
    }

	public Integer getProcesstime() {
        return this.processtime;
    }

	public void setProcesstime(Integer processtime) {
        this.processtime = processtime;
    }

	public Integer getFinishtime() {
        return this.finishtime;
    }

	public void setFinishtime(Integer finishtime) {
        this.finishtime = finishtime;
    }

	public Integer getMinsetup() {
        return this.minsetup;
    }

	public void setMinsetup(Integer minsetup) {
        this.minsetup = minsetup;
    }

	public Float getCost() {
        return this.cost;
    }

	public void setCost(Float cost) {
        this.cost = cost;
    }

	public Integer getAmounttime() {
        return this.amounttime;
    }

	public void setAmounttime(Integer amounttime) {
        this.amounttime = amounttime;
    }

	public Integer getLength_time() {
        return this.length_time;
    }

	public void setLength_time(Integer length_time) {
        this.length_time = length_time;
    }

	public String getInfo_note() {
        return this.info_note;
    }

	public void setInfo_note(String info_note) {
        this.info_note = info_note;
    }

	public Shop getShop() {
        return this.shop;
    }

	public void setShop(Shop shop) {
        this.shop = shop;
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static AbstractService fromJsonToAbstractService(String json) {
        return new JSONDeserializer<AbstractService>().use(null, AbstractService.class).deserialize(json);
    }

//	public static String toJsonArray(Collection<AbstractService> collection) {
//        return new JSONSerializer().exclude("*.class").serialize(collection);
//    }

	public static Collection<AbstractService> fromJsonArrayToAbstractServices(String json) {
        return new JSONDeserializer<List<AbstractService>>().use(null, ArrayList.class).use("values", AbstractService.class).deserialize(json);
    }

	@PersistenceContext
    transient EntityManager entityManager;

	@Id
    @GeneratedValue(strategy = GenerationType.TABLE)
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
            AbstractService attached = AbstractService.findAbstractService(this.id);
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
    public AbstractService merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        AbstractService merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new AbstractService() {
        }.entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countAbstractServices() {
        return entityManager().createQuery("SELECT COUNT(o) FROM AbstractService o", Long.class).getSingleResult();
    }

	public static List<AbstractService> findAllAbstractServices() {
        return entityManager().createQuery("SELECT o FROM AbstractService o", AbstractService.class).getResultList();
    }

	public static AbstractService findAbstractService(Long id) {
        if (id == null) return null;
        return entityManager().find(AbstractService.class, id);
    }

	public static List<AbstractService> findAbstractServiceEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM AbstractService o", AbstractService.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
