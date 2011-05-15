package com.yourhairsalon.booking.domain;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import com.yourhairsalon.booking.reference.CommType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.yourhairsalon.booking.domain.AbstractPerson;
import javax.persistence.ManyToOne;
import com.yourhairsalon.booking.domain.Shop;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import javax.validation.constraints.Min;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Configurable
@RooJavaBean
@RooToString
@RooJson
@RooEntity(finders = { "findCommunicationsesByPerson", "findCommunicationsesByShop", "findCommunicationsesByCommunication_valueEquals" })
public class Communications {

    @Enumerated
    private CommType communication_type;

    @Size(min = 0, max = 50)
    private String communication_value;

    @ManyToOne
    private AbstractPerson person;

    @NotNull
    @ManyToOne
    private Shop shop;

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
            Communications attached = Communications.findCommunications(this.id);
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
    public Communications merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Communications merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new Communications().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countCommunicationses() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Communications o", Long.class).getSingleResult();
    }

	public static List<Communications> findAllCommunicationses() {
        return entityManager().createQuery("SELECT o FROM Communications o", Communications.class).getResultList();
    }

	public static Communications findCommunications(Long id) {
        if (id == null) return null;
        return entityManager().find(Communications.class, id);
    }

	public static List<Communications> findCommunicationsEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Communications o", Communications.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static Communications fromJsonToCommunications(String json) {
        return new JSONDeserializer<Communications>().use(null, Communications.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Communications> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<Communications> fromJsonArrayToCommunicationses(String json) {
        return new JSONDeserializer<List<Communications>>().use(null, ArrayList.class).use("values", Communications.class).deserialize(json);
    }

	public CommType getCommunication_type() {
        return this.communication_type;
    }

	public void setCommunication_type(CommType communication_type) {
        this.communication_type = communication_type;
    }

	public String getCommunication_value() {
        return this.communication_value;
    }

	public void setCommunication_value(String communication_value) {
        this.communication_value = communication_value;
    }

	public AbstractPerson getPerson() {
        return this.person;
    }

	public void setPerson(AbstractPerson person) {
        this.person = person;
    }

	public Shop getShop() {
        return this.shop;
    }

	public void setShop(Shop shop) {
        this.shop = shop;
    }

	public static TypedQuery<Communications> findCommunicationsesByCommunication_valueEquals(String communication_value) {
        if (communication_value == null || communication_value.length() == 0) throw new IllegalArgumentException("The communication_value argument is required");
        EntityManager em = Communications.entityManager();
        TypedQuery<Communications> q = em.createQuery("SELECT o FROM Communications AS o WHERE o.communication_value = :communication_value", Communications.class);
        q.setParameter("communication_value", communication_value);
        return q;
    }

	public static TypedQuery<Communications> findCommunicationsesByPerson(AbstractPerson person) {
        if (person == null) throw new IllegalArgumentException("The person argument is required");
        EntityManager em = Communications.entityManager();
        TypedQuery<Communications> q = em.createQuery("SELECT o FROM Communications AS o WHERE o.person = :person", Communications.class);
        q.setParameter("person", person);
        return q;
    }

	public static TypedQuery<Communications> findCommunicationsesByShop(Shop shop) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        EntityManager em = Communications.entityManager();
        TypedQuery<Communications> q = em.createQuery("SELECT o FROM Communications AS o WHERE o.shop = :shop", Communications.class);
        q.setParameter("shop", shop);
        return q;
    }

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Communication_type: ").append(getCommunication_type()).append(", ");
        sb.append("Communication_value: ").append(getCommunication_value()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Person: ").append(getPerson()).append(", ");
        sb.append("Shop: ").append(getShop()).append(", ");
        sb.append("Version: ").append(getVersion());
        return sb.toString();
    }
}
