package com.yourhairsalon.booking.domain;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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
import com.yourhairsalon.booking.domain.Shop;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import javax.persistence.ManyToOne;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooEntity
@RooJson
public abstract class AbstractGroup {

    @NotNull
    @Size(min = 1, max = 50)
    private String group_name;

    @Size(min = 1, max = 250)
    private String group_notes;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date createddate;

    @NotNull
    @ManyToOne
    private Shop shop;

	public String getGroup_name() {
        return this.group_name;
    }

	public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

	public String getGroup_notes() {
        return this.group_notes;
    }

	public void setGroup_notes(String group_notes) {
        this.group_notes = group_notes;
    }

	public Date getCreateddate() {
        return this.createddate;
    }

	public void setCreateddate(Date createddate) {
        this.createddate = createddate;
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

	public static AbstractGroup fromJsonToAbstractGroup(String json) {
        return new JSONDeserializer<AbstractGroup>().use(null, AbstractGroup.class).deserialize(json);
    }

//	public static String toJsonArray(Collection<AbstractGroup> collection) {
//        return new JSONSerializer().exclude("*.class").serialize(collection);
//    }

	public static Collection<AbstractGroup> fromJsonArrayToAbstractGroups(String json) {
        return new JSONDeserializer<List<AbstractGroup>>().use(null, ArrayList.class).use("values", AbstractGroup.class).deserialize(json);
    }

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Createddate: ").append(getCreateddate()).append(", ");
        sb.append("Group_name: ").append(getGroup_name()).append(", ");
        sb.append("Group_notes: ").append(getGroup_notes()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Shop: ").append(getShop()).append(", ");
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
            AbstractGroup attached = AbstractGroup.findAbstractGroup(this.id);
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
    public AbstractGroup merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        AbstractGroup merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new AbstractGroup() {
        }.entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countAbstractGroups() {
        return entityManager().createQuery("SELECT COUNT(o) FROM AbstractGroup o", Long.class).getSingleResult();
    }

	public static List<AbstractGroup> findAllAbstractGroups() {
        return entityManager().createQuery("SELECT o FROM AbstractGroup o", AbstractGroup.class).getResultList();
    }

	public static AbstractGroup findAbstractGroup(Long id) {
        if (id == null) return null;
        return entityManager().find(AbstractGroup.class, id);
    }

	public static List<AbstractGroup> findAbstractGroupEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM AbstractGroup o", AbstractGroup.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
