package com.yourhairsalon.booking.domain;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import javax.validation.constraints.Size;
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
import com.yourhairsalon.booking.domain.Staff;
import javax.persistence.ManyToOne;
import com.yourhairsalon.booking.domain.Clients;
import com.yourhairsalon.booking.domain.Shop;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import javax.validation.constraints.Min;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooEntity
@RooJson
public class Email {

    @Size(max = 255)
    private String message;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date sentDate;

    @NotNull
    @ManyToOne
    private Staff from_staff;

    @NotNull
    @ManyToOne
    private Clients to_client;

    @NotNull
    @ManyToOne
    private Shop shop;

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("From_staff: ").append(getFrom_staff()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Message: ").append(getMessage()).append(", ");
        sb.append("SentDate: ").append(getSentDate()).append(", ");
        sb.append("Shop: ").append(getShop()).append(", ");
        sb.append("To_client: ").append(getTo_client()).append(", ");
        sb.append("Version: ").append(getVersion());
        return sb.toString();
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
            Email attached = Email.findEmail(this.id);
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
    public Email merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Email merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new Email().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countEmails() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Email o", Long.class).getSingleResult();
    }

	public static List<Email> findAllEmails() {
        return entityManager().createQuery("SELECT o FROM Email o", Email.class).getResultList();
    }

	public static Email findEmail(Long id) {
        if (id == null) return null;
        return entityManager().find(Email.class, id);
    }

	public static List<Email> findEmailEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Email o", Email.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public String getMessage() {
        return this.message;
    }

	public void setMessage(String message) {
        this.message = message;
    }

	public Date getSentDate() {
        return this.sentDate;
    }

	public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

	public Staff getFrom_staff() {
        return this.from_staff;
    }

	public void setFrom_staff(Staff from_staff) {
        this.from_staff = from_staff;
    }

	public Clients getTo_client() {
        return this.to_client;
    }

	public void setTo_client(Clients to_client) {
        this.to_client = to_client;
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

	public static Email fromJsonToEmail(String json) {
        return new JSONDeserializer<Email>().use(null, Email.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Email> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<Email> fromJsonArrayToEmails(String json) {
        return new JSONDeserializer<List<Email>>().use(null, ArrayList.class).use("values", Email.class).deserialize(json);
    }
}
