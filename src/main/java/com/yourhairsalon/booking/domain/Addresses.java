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
import com.yourhairsalon.booking.domain.AbstractPerson;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
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
@RooEntity(finders = { "findAddressesesByPerson" })
@RooJson
public class Addresses {

    @Size(min = 0, max = 50)
    private String address1;

    @Size(min = 0, max = 50)
    private String address2;

    @Size(max = 30)
    private String citycode;

    @Size(max = 30)
    private String statecode;

    @Size(min = 0, max = 5)
    private String zipcode;

    @ManyToOne
    private AbstractPerson person;

    @NotNull
    @ManyToOne
    private Shop shop;

	public String getAddress1() {
        return this.address1;
    }

	public void setAddress1(String address1) {
        this.address1 = address1;
    }

	public String getAddress2() {
        return this.address2;
    }

	public void setAddress2(String address2) {
        this.address2 = address2;
    }

	public String getCitycode() {
        return this.citycode;
    }

	public void setCitycode(String citycode) {
        this.citycode = citycode;
    }

	public String getStatecode() {
        return this.statecode;
    }

	public void setStatecode(String statecode) {
        this.statecode = statecode;
    }

	public String getZipcode() {
        return this.zipcode;
    }

	public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
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

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Address1: ").append(getAddress1()).append(", ");
        sb.append("Address2: ").append(getAddress2()).append(", ");
        sb.append("Citycode: ").append(getCitycode()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Person: ").append(getPerson()).append(", ");
        sb.append("Shop: ").append(getShop()).append(", ");
        sb.append("Statecode: ").append(getStatecode()).append(", ");
        sb.append("Version: ").append(getVersion()).append(", ");
        sb.append("Zipcode: ").append(getZipcode());
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
            Addresses attached = Addresses.findAddresses(this.id);
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
    public Addresses merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Addresses merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new Addresses().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countAddresseses() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Addresses o", Long.class).getSingleResult();
    }

	public static List<Addresses> findAllAddresseses() {
        return entityManager().createQuery("SELECT o FROM Addresses o", Addresses.class).getResultList();
    }

	public static Addresses findAddresses(Long id) {
        if (id == null) return null;
        return entityManager().find(Addresses.class, id);
    }

	public static List<Addresses> findAddressesEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Addresses o", Addresses.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static TypedQuery<Addresses> findAddressesesByPerson(AbstractPerson person) {
        if (person == null) throw new IllegalArgumentException("The person argument is required");
        EntityManager em = Addresses.entityManager();
        TypedQuery<Addresses> q = em.createQuery("SELECT o FROM Addresses AS o WHERE o.person = :person", Addresses.class);
        q.setParameter("person", person);
        return q;
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static Addresses fromJsonToAddresses(String json) {
        return new JSONDeserializer<Addresses>().use(null, Addresses.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Addresses> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<Addresses> fromJsonArrayToAddresseses(String json) {
        return new JSONDeserializer<List<Addresses>>().use(null, ArrayList.class).use("values", Addresses.class).deserialize(json);
    }
}
