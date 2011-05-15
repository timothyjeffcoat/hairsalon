package com.yourhairsalon.booking.domain;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import com.yourhairsalon.booking.domain.Shop;
import javax.validation.constraints.NotNull;
import javax.persistence.ManyToOne;
import com.yourhairsalon.booking.domain.Staff;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
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
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Configurable
@RooJavaBean
@RooToString
@RooEntity(finders = { "findAuditsByShop", "findAuditsByShopAndTsBetween", "findAuditsByShopAndTsEquals", "findAuditsByShopAndTsGreaterThan", "findAuditsByShopAndTsGreaterThanEquals", "findAuditsByShopAndStaff","findAuditsByShopAndStaffAndType", "findAuditsByShopAndTsLessThan", "findAuditsByShopAndTsLessThanEquals", "findAuditsByShopAndTsNotEquals","findAuditsByShopAndTypeAndTsBetween","findAuditsByShopAndStaffAndTypeAndTsBetween" })
@RooJson
public class Audit {

    @NotNull
    @ManyToOne
    private Shop shop;

    @ManyToOne
    private Staff staff;

    @Size(max = 255)
    private String description;

    @Size(max = 255)
    @Column(name="type", columnDefinition = "TEXT default 'GENERAL'")
    private String type;
    
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date ts;

	public static TypedQuery<Audit> findAuditsByShop(Shop shop) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        EntityManager em = Audit.entityManager();
        TypedQuery<Audit> q = em.createQuery("SELECT o FROM Audit AS o WHERE o.shop = :shop ORDER BY o.ts desc", Audit.class);
        q.setMaxResults(100);
        q.setParameter("shop", shop);
        return q;
    }

	public static TypedQuery<Audit> findAuditsByShopAndStaff(Shop shop, Staff staff) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (staff == null) throw new IllegalArgumentException("The staff argument is required");
        EntityManager em = Audit.entityManager();
        TypedQuery<Audit> q = em.createQuery("SELECT o FROM Audit AS o WHERE o.shop = :shop AND o.staff = :staff", Audit.class);
        q.setParameter("shop", shop);
        q.setParameter("staff", staff);
        return q;
    }

	public static TypedQuery<Audit> findAuditsByShopAndStaffAndType(Shop shop, Staff staff, String type) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (staff == null) throw new IllegalArgumentException("The staff argument is required");
        if (type == null) throw new IllegalArgumentException("The type argument is required");
        EntityManager em = Audit.entityManager();
        TypedQuery<Audit> q = em.createQuery("SELECT o FROM Audit AS o WHERE o.shop = :shop AND o.staff = :staff AND o.type = :type", Audit.class);
        q.setParameter("shop", shop);
        q.setParameter("staff", staff);
        q.setParameter("type", type);
        return q;
    }

	public static TypedQuery<Audit> findAuditsByShopAndStaffAndTypeAndTsBetween(Shop shop, Staff staff, String type,Date minTs, Date maxTs) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (staff == null) throw new IllegalArgumentException("The staff argument is required");
        if (type == null) throw new IllegalArgumentException("The type argument is required");
        EntityManager em = Audit.entityManager();
        TypedQuery<Audit> q = em.createQuery("SELECT o FROM Audit AS o WHERE o.shop = :shop AND o.staff = :staff AND o.type = :type AND o.ts BETWEEN :minTs AND :maxTs", Audit.class);
        q.setParameter("shop", shop);
        q.setParameter("staff", staff);
        q.setParameter("type", type);
        q.setParameter("minTs", minTs);
        q.setParameter("maxTs", maxTs);
        return q;
    }
	public static TypedQuery<Audit> findAuditsByShopAndTypeAndTsBetweenAndDescriptionIsATextMessage(Shop shop, String type,Date minTs, Date maxTs) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (type == null) throw new IllegalArgumentException("The type argument is required");
        EntityManager em = Audit.entityManager();
        TypedQuery<Audit> q = em.createQuery("SELECT o FROM Audit AS o WHERE o.shop = :shop AND o.type = :type AND o.ts BETWEEN :minTs AND :maxTs AND o.description LIKE :description", Audit.class);
        q.setParameter("shop", shop);
        q.setParameter("type", type);
        q.setParameter("minTs", minTs);
        q.setParameter("maxTs", maxTs);
        q.setParameter("description", "Success sending message%");
        return q;
    }

	public static TypedQuery<Audit> findAuditsByShopAndTypeAndTsBetween(Shop shop, String type,Date minTs, Date maxTs) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (type == null) throw new IllegalArgumentException("The type argument is required");
        EntityManager em = Audit.entityManager();
        TypedQuery<Audit> q = em.createQuery("SELECT o FROM Audit AS o WHERE o.shop = :shop AND o.type = :type AND o.ts BETWEEN :minTs AND :maxTs", Audit.class);
        q.setParameter("shop", shop);
        q.setParameter("type", type);
        q.setParameter("minTs", minTs);
        q.setParameter("maxTs", maxTs);
        return q;
    }

	public static TypedQuery<Audit> findAuditsByShopAndTsBetween(Shop shop, Date minTs, Date maxTs) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (minTs == null) throw new IllegalArgumentException("The minTs argument is required");
        if (maxTs == null) throw new IllegalArgumentException("The maxTs argument is required");
        EntityManager em = Audit.entityManager();
        TypedQuery<Audit> q = em.createQuery("SELECT o FROM Audit AS o WHERE o.shop = :shop AND o.ts BETWEEN :minTs AND :maxTs", Audit.class);
        q.setParameter("shop", shop);
        q.setParameter("minTs", minTs);
        q.setParameter("maxTs", maxTs);
        return q;
    }

	public static TypedQuery<Audit> findAuditsByShopAndTsEquals(Shop shop, Date ts) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (ts == null) throw new IllegalArgumentException("The ts argument is required");
        EntityManager em = Audit.entityManager();
        TypedQuery<Audit> q = em.createQuery("SELECT o FROM Audit AS o WHERE o.shop = :shop AND o.ts = :ts", Audit.class);
        q.setParameter("shop", shop);
        q.setParameter("ts", ts);
        return q;
    }

	public static TypedQuery<Audit> findAuditsByShopAndTsGreaterThan(Shop shop, Date ts) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (ts == null) throw new IllegalArgumentException("The ts argument is required");
        EntityManager em = Audit.entityManager();
        TypedQuery<Audit> q = em.createQuery("SELECT o FROM Audit AS o WHERE o.shop = :shop AND o.ts > :ts", Audit.class);
        q.setParameter("shop", shop);
        q.setParameter("ts", ts);
        return q;
    }

	public static TypedQuery<Audit> findAuditsByShopAndTsGreaterThanEquals(Shop shop, Date ts) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (ts == null) throw new IllegalArgumentException("The ts argument is required");
        EntityManager em = Audit.entityManager();
        TypedQuery<Audit> q = em.createQuery("SELECT o FROM Audit AS o WHERE o.shop = :shop AND o.ts >= :ts", Audit.class);
        q.setParameter("shop", shop);
        q.setParameter("ts", ts);
        return q;
    }

	public static TypedQuery<Audit> findAuditsByShopAndTsLessThan(Shop shop, Date ts) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (ts == null) throw new IllegalArgumentException("The ts argument is required");
        EntityManager em = Audit.entityManager();
        TypedQuery<Audit> q = em.createQuery("SELECT o FROM Audit AS o WHERE o.shop = :shop AND o.ts < :ts", Audit.class);
        q.setParameter("shop", shop);
        q.setParameter("ts", ts);
        return q;
    }

	public static TypedQuery<Audit> findAuditsByShopAndTsLessThanEquals(Shop shop, Date ts) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (ts == null) throw new IllegalArgumentException("The ts argument is required");
        EntityManager em = Audit.entityManager();
        TypedQuery<Audit> q = em.createQuery("SELECT o FROM Audit AS o WHERE o.shop = :shop AND o.ts <= :ts", Audit.class);
        q.setParameter("shop", shop);
        q.setParameter("ts", ts);
        return q;
    }

	public static TypedQuery<Audit> findAuditsByShopAndTsNotEquals(Shop shop, Date ts) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (ts == null) throw new IllegalArgumentException("The ts argument is required");
        EntityManager em = Audit.entityManager();
        TypedQuery<Audit> q = em.createQuery("SELECT o FROM Audit AS o WHERE o.shop = :shop AND o.ts != :ts", Audit.class);
        q.setParameter("shop", shop);
        q.setParameter("ts", ts);
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
            Audit attached = Audit.findAudit(this.id);
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
    public Audit merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Audit merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new Audit().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countAudits() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Audit o", Long.class).getSingleResult();
    }

	public static List<Audit> findAllAudits() {
        return entityManager().createQuery("SELECT o FROM Audit o", Audit.class).getResultList();
    }

	public static Audit findAudit(Long id) {
        if (id == null) return null;
        return entityManager().find(Audit.class, id);
    }

	public static List<Audit> findAuditEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Audit o", Audit.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Description: ").append(getDescription()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Shop: ").append(getShop()).append(", ");
        sb.append("Staff: ").append(getStaff()).append(", ");
        sb.append("Ts: ").append(getTs()).append(", ");
        sb.append("Version: ").append(getVersion());
        return sb.toString();
    }

	public Shop getShop() {
        return this.shop;
    }

	public void setShop(Shop shop) {
        this.shop = shop;
    }

	public Staff getStaff() {
        return this.staff;
    }

	public void setStaff(Staff staff) {
        this.staff = staff;
    }

	public String getDescription() {
        return this.description;
    }

	public void setDescription(String description) {
        this.description = description;
    }

	public Date getTs() {
        return this.ts;
    }

	public void setTs(Date ts) {
        this.ts = ts;
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static Audit fromJsonToAudit(String json) {
        return new JSONDeserializer<Audit>().use(null, Audit.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Audit> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<Audit> fromJsonArrayToAudits(String json) {
        return new JSONDeserializer<List<Audit>>().use(null, ArrayList.class).use("values", Audit.class).deserialize(json);
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
