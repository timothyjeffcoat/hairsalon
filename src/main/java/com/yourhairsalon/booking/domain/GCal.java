package com.yourhairsalon.booking.domain;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;
import com.yourhairsalon.booking.domain.Shop;
import java.util.List;
import javax.validation.constraints.NotNull;
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
import com.yourhairsalon.booking.domain.Staff;
import javax.validation.constraints.Size;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooEntity(finders = { "findGCalsByShopEquals", "findGCalsByUsernameEqualsAndPasswordEquals", "findGCalsByStaffEquals", "findGCalsByCalendarnameidEquals", "findGCalsByShopEqualsAndStaffEquals", "findGCalsByShopEqualsAndStaffEqualsAndCalendarnameidEquals", "findGCalsByShopEqualsAndCalendarnameidEquals", "findGCalsByShopEqualsAndStaffIsNull", "findGCalsByShopEqualsAndStaffIsNullAndCalendarnameidEquals" })
public class GCal {

    @NotNull
    @ManyToOne
    private Shop shop;

    @ManyToOne
    private Staff staff;

    @Size(max = 255)
    private String calendarnameid;

    @Size(max = 255)
    private String username;

    @Size(max = 255)
    private String password;

    private Boolean syncCal;

    @Size(max = 255)
    private String gcal_title;

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

	public String getCalendarnameid() {
        return this.calendarnameid;
    }

	public void setCalendarnameid(String calendarnameid) {
        this.calendarnameid = calendarnameid;
    }

	public String getUsername() {
        return this.username;
    }

	public void setUsername(String username) {
        this.username = username;
    }

	public String getPassword() {
        return this.password;
    }

	public void setPassword(String password) {
        this.password = password;
    }

	public Boolean getSyncCal() {
        return this.syncCal;
    }

	public void setSyncCal(Boolean syncCal) {
        this.syncCal = syncCal;
    }

	public String getGcal_title() {
        return this.gcal_title;
    }

	public void setGcal_title(String gcal_title) {
        this.gcal_title = gcal_title;
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
            GCal attached = GCal.findGCal(this.id);
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
    public GCal merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        GCal merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new GCal().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countGCals() {
        return entityManager().createQuery("SELECT COUNT(o) FROM GCal o", Long.class).getSingleResult();
    }

	public static List<GCal> findAllGCals() {
        return entityManager().createQuery("SELECT o FROM GCal o", GCal.class).getResultList();
    }

	public static GCal findGCal(Long id) {
        if (id == null) return null;
        return entityManager().find(GCal.class, id);
    }

	public static List<GCal> findGCalEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM GCal o", GCal.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static TypedQuery<GCal> findGCalsByCalendarnameidEquals(String calendarnameid) {
        if (calendarnameid == null || calendarnameid.length() == 0) throw new IllegalArgumentException("The calendarnameid argument is required");
        EntityManager em = GCal.entityManager();
        TypedQuery<GCal> q = em.createQuery("SELECT o FROM GCal AS o WHERE o.calendarnameid = :calendarnameid", GCal.class);
        q.setParameter("calendarnameid", calendarnameid);
        return q;
    }

	public static TypedQuery<GCal> findGCalsByShopEquals(Shop shop) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        EntityManager em = GCal.entityManager();
        TypedQuery<GCal> q = em.createQuery("SELECT o FROM GCal AS o WHERE o.shop = :shop", GCal.class);
        q.setParameter("shop", shop);
        return q;
    }

	public static TypedQuery<GCal> findGCalsByShopEqualsAndCalendarnameidEquals(Shop shop, String calendarnameid) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (calendarnameid == null || calendarnameid.length() == 0) throw new IllegalArgumentException("The calendarnameid argument is required");
        EntityManager em = GCal.entityManager();
        TypedQuery<GCal> q = em.createQuery("SELECT o FROM GCal AS o WHERE o.shop = :shop  AND o.calendarnameid = :calendarnameid", GCal.class);
        q.setParameter("shop", shop);
        q.setParameter("calendarnameid", calendarnameid);
        return q;
    }

	public static TypedQuery<GCal> findGCalsByShopEqualsAndStaffEquals(Shop shop, Staff staff) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (staff == null) throw new IllegalArgumentException("The staff argument is required");
        EntityManager em = GCal.entityManager();
        TypedQuery<GCal> q = em.createQuery("SELECT o FROM GCal AS o WHERE o.shop = :shop  AND o.staff = :staff", GCal.class);
        q.setParameter("shop", shop);
        q.setParameter("staff", staff);
        return q;
    }

	public static TypedQuery<GCal> findGCalsByShopEqualsAndStaffEqualsAndCalendarnameidEquals(Shop shop, Staff staff, String calendarnameid) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (staff == null) throw new IllegalArgumentException("The staff argument is required");
        if (calendarnameid == null || calendarnameid.length() == 0) throw new IllegalArgumentException("The calendarnameid argument is required");
        EntityManager em = GCal.entityManager();
        TypedQuery<GCal> q = em.createQuery("SELECT o FROM GCal AS o WHERE o.shop = :shop  AND o.staff = :staff  AND o.calendarnameid = :calendarnameid", GCal.class);
        q.setParameter("shop", shop);
        q.setParameter("staff", staff);
        q.setParameter("calendarnameid", calendarnameid);
        return q;
    }

	public static TypedQuery<GCal> findGCalsByShopEqualsAndStaffIsNull(Shop shop) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        EntityManager em = GCal.entityManager();
        TypedQuery<GCal> q = em.createQuery("SELECT o FROM GCal AS o WHERE o.shop = :shop  AND o.staff IS NULL", GCal.class);
        q.setParameter("shop", shop);
        return q;
    }

	public static TypedQuery<GCal> findGCalsByShopEqualsAndStaffIsNullAndCalendarnameidEquals(Shop shop, String calendarnameid) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (calendarnameid == null || calendarnameid.length() == 0) throw new IllegalArgumentException("The calendarnameid argument is required");
        EntityManager em = GCal.entityManager();
        TypedQuery<GCal> q = em.createQuery("SELECT o FROM GCal AS o WHERE o.shop = :shop  AND o.staff IS NULL  AND o.calendarnameid = :calendarnameid", GCal.class);
        q.setParameter("shop", shop);
        q.setParameter("calendarnameid", calendarnameid);
        return q;
    }

	public static TypedQuery<GCal> findGCalsByStaffEquals(Staff staff) {
        if (staff == null) throw new IllegalArgumentException("The staff argument is required");
        EntityManager em = GCal.entityManager();
        TypedQuery<GCal> q = em.createQuery("SELECT o FROM GCal AS o WHERE o.staff = :staff", GCal.class);
        q.setParameter("staff", staff);
        return q;
    }

	public static TypedQuery<GCal> findGCalsByUsernameEqualsAndPasswordEquals(String username, String password) {
        if (username == null || username.length() == 0) throw new IllegalArgumentException("The username argument is required");
        if (password == null || password.length() == 0) throw new IllegalArgumentException("The password argument is required");
        EntityManager em = GCal.entityManager();
        TypedQuery<GCal> q = em.createQuery("SELECT o FROM GCal AS o WHERE o.username = :username  AND o.password = :password", GCal.class);
        q.setParameter("username", username);
        q.setParameter("password", password);
        return q;
    }

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Calendarnameid: ").append(getCalendarnameid()).append(", ");
        sb.append("Gcal_title: ").append(getGcal_title()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Password: ").append(getPassword()).append(", ");
        sb.append("Shop: ").append(getShop()).append(", ");
        sb.append("Staff: ").append(getStaff()).append(", ");
        sb.append("SyncCal: ").append(getSyncCal()).append(", ");
        sb.append("Username: ").append(getUsername()).append(", ");
        sb.append("Version: ").append(getVersion());
        return sb.toString();
    }
}
