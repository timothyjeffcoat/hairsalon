package com.yourhairsalon.booking.domain;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import com.yourhairsalon.booking.domain.Communications;
import java.util.HashSet;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;
import com.yourhairsalon.booking.domain.Addresses;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import java.util.Date;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
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
public abstract class AbstractPerson {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "person")
    private Set<Communications> communication = new HashSet<Communications>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "person")
    private Set<Addresses> address = new HashSet<Addresses>();

    @Size(min = 1, max = 30)
    private String firstName;

    @NotNull
    @Size(min = 1, max = 30)
    private String lastName;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date birthDay;

    @Size(min = 1, max = 255)
    private String username;
    
    @NotNull
    @ManyToOne
    private Shop shop;

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Address: ").append(getAddress() == null ? "null" : getAddress().size()).append(", ");
        sb.append("BirthDay: ").append(getBirthDay()).append(", ");
        sb.append("Communication: ").append(getCommunication() == null ? "null" : getCommunication().size()).append(", ");
        sb.append("FirstName: ").append(getFirstName()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("LastName: ").append(getLastName()).append(", ");
        sb.append("Shop: ").append(getShop()).append(", ");
        sb.append("Username: ").append(getUsername()).append(", ");
        sb.append("Version: ").append(getVersion());
        return sb.toString();
    }

	public Set<Communications> getCommunication() {
        return this.communication;
    }

	public void setCommunication(Set<Communications> communication) {
        this.communication = communication;
    }

	public Set<Addresses> getAddress() {
        return this.address;
    }

	public void setAddress(Set<Addresses> address) {
        this.address = address;
    }

	public String getFirstName() {
        return this.firstName;
    }

	public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

	public String getLastName() {
        return this.lastName;
    }

	public void setLastName(String lastName) {
        this.lastName = lastName;
    }

	public Date getBirthDay() {
        return this.birthDay;
    }

	public void setBirthDay(Date birthDay) {
        this.birthDay = birthDay;
    }

	public String getUsername() {
        return this.username;
    }

	public void setUsername(String username) {
        this.username = username;
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

	public static AbstractPerson fromJsonToAbstractPerson(String json) {
        return new JSONDeserializer<AbstractPerson>().use(null, AbstractPerson.class).deserialize(json);
    }

//	public static String toJsonArray(Collection<AbstractPerson> collection) {
//        return new JSONSerializer().exclude("*.class").serialize(collection);
//    }

	public static Collection<AbstractPerson> fromJsonArrayToAbstractpeople(String json) {
        return new JSONDeserializer<List<AbstractPerson>>().use(null, ArrayList.class).use("values", AbstractPerson.class).deserialize(json);
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
            AbstractPerson attached = AbstractPerson.findAbstractPerson(this.id);
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
    public AbstractPerson merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        AbstractPerson merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new AbstractPerson() {
        }.entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countAbstractpeople() {
        return entityManager().createQuery("SELECT COUNT(o) FROM AbstractPerson o", Long.class).getSingleResult();
    }

	public static List<AbstractPerson> findAllAbstractpeople() {
        return entityManager().createQuery("SELECT o FROM AbstractPerson o", AbstractPerson.class).getResultList();
    }

	public static AbstractPerson findAbstractPerson(Long id) {
        if (id == null) return null;
        return entityManager().find(AbstractPerson.class, id);
    }

	public static List<AbstractPerson> findAbstractPersonEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM AbstractPerson o", AbstractPerson.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
