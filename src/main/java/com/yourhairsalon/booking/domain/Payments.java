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
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;
import com.yourhairsalon.booking.domain.Clients;
import javax.persistence.ManyToOne;
import com.yourhairsalon.booking.domain.Staff;
import com.yourhairsalon.booking.domain.Shop;
import java.util.Set;
import com.yourhairsalon.booking.domain.PaymentsType;
import java.util.HashSet;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.validation.constraints.Min;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.transaction.annotation.Transactional;
import com.yourhairsalon.booking.domain.Appointment;
import com.yourhairsalon.booking.domain.PaymentsService;
import ca.digitalface.jasperoo.RooJasperoo;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooJson
@RooJasperoo
@RooEntity(finders = { "findPaymentsesById", "findPaymentsesByShopAndDatecreatedBetween", "findPaymentsesByShopAndDatecreatedBetweenAndStaffEquals" })
public class Payments {

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date datecreated;

    @NotNull
    private Float amount;

    @NotNull
    private Float tax;

    @NotNull
    private String note;

    @NotNull
    private Float gratuity;

    @NotNull
    @ManyToOne
    private Clients client;

    @NotNull
    @ManyToOne
    private Staff staff;

    @NotNull
    @ManyToOne
    private Shop shop;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<PaymentsType> paymentstype = new HashSet<PaymentsType>();

    @NotNull
    @ManyToOne
    private Appointment appointment;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<PaymentsService> paymentsservice = new HashSet<PaymentsService>();

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static Payments fromJsonToPayments(String json) {
        return new JSONDeserializer<Payments>().use(null, Payments.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Payments> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<Payments> fromJsonArrayToPaymentses(String json) {
        return new JSONDeserializer<List<Payments>>().use(null, ArrayList.class).use("values", Payments.class).deserialize(json);
    }

	public boolean isReportable() {
        return true;
    }

	public Date getDatecreated() {
        return this.datecreated;
    }

	public void setDatecreated(Date datecreated) {
        this.datecreated = datecreated;
    }

	public Float getAmount() {
        return this.amount;
    }

	public void setAmount(Float amount) {
        this.amount = amount;
    }

	public Float getTax() {
        return this.tax;
    }

	public void setTax(Float tax) {
        this.tax = tax;
    }

	public String getNote() {
        return this.note;
    }

	public void setNote(String note) {
        this.note = note;
    }

	public Float getGratuity() {
        return this.gratuity;
    }

	public void setGratuity(Float gratuity) {
        this.gratuity = gratuity;
    }

	public Clients getClient() {
        return this.client;
    }

	public void setClient(Clients client) {
        this.client = client;
    }

	public Staff getStaff() {
        return this.staff;
    }

	public void setStaff(Staff staff) {
        this.staff = staff;
    }

	public Shop getShop() {
        return this.shop;
    }

	public void setShop(Shop shop) {
        this.shop = shop;
    }

	public Set<PaymentsType> getPaymentstype() {
        return this.paymentstype;
    }

	public void setPaymentstype(Set<PaymentsType> paymentstype) {
        this.paymentstype = paymentstype;
    }

	public Appointment getAppointment() {
        return this.appointment;
    }

	public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

	public Set<PaymentsService> getPaymentsservice() {
        return this.paymentsservice;
    }

	public void setPaymentsservice(Set<PaymentsService> paymentsservice) {
        this.paymentsservice = paymentsservice;
    }

	public static TypedQuery<Payments> findPaymentsesById(Long id) {
        if (id == null) throw new IllegalArgumentException("The id argument is required");
        EntityManager em = Payments.entityManager();
        TypedQuery<Payments> q = em.createQuery("SELECT o FROM Payments AS o WHERE o.id = :id", Payments.class);
        q.setParameter("id", id);
        return q;
    }

	public static TypedQuery<Payments> findPaymentsesByShopAndDatecreatedBetween(Shop shop, Date minDatecreated, Date maxDatecreated) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (minDatecreated == null) throw new IllegalArgumentException("The minDatecreated argument is required");
        if (maxDatecreated == null) throw new IllegalArgumentException("The maxDatecreated argument is required");
        EntityManager em = Payments.entityManager();
        TypedQuery<Payments> q = em.createQuery("SELECT o FROM Payments AS o WHERE o.shop = :shop AND o.datecreated BETWEEN :minDatecreated AND :maxDatecreated", Payments.class);
        q.setParameter("shop", shop);
        q.setParameter("minDatecreated", minDatecreated);
        q.setParameter("maxDatecreated", maxDatecreated);
        return q;
    }

	public static TypedQuery<Payments> findPaymentsesByShopAndDatecreatedBetweenAndStaffEquals(Shop shop, Date minDatecreated, Date maxDatecreated, Staff staff) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (minDatecreated == null) throw new IllegalArgumentException("The minDatecreated argument is required");
        if (maxDatecreated == null) throw new IllegalArgumentException("The maxDatecreated argument is required");
        if (staff == null) throw new IllegalArgumentException("The staff argument is required");
        EntityManager em = Payments.entityManager();
        TypedQuery<Payments> q = em.createQuery("SELECT o FROM Payments AS o WHERE o.shop = :shop AND o.datecreated BETWEEN :minDatecreated AND :maxDatecreated  AND o.staff = :staff", Payments.class);
        q.setParameter("shop", shop);
        q.setParameter("minDatecreated", minDatecreated);
        q.setParameter("maxDatecreated", maxDatecreated);
        q.setParameter("staff", staff);
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
            Payments attached = Payments.findPayments(this.id);
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
    public Payments merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Payments merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new Payments().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countPaymentses() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Payments o", Long.class).getSingleResult();
    }

	public static List<Payments> findAllPaymentses() {
        return entityManager().createQuery("SELECT o FROM Payments o", Payments.class).getResultList();
    }

	public static Payments findPayments(Long id) {
        if (id == null) return null;
        return entityManager().find(Payments.class, id);
    }

	public static List<Payments> findPaymentsEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Payments o", Payments.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Amount: ").append(getAmount()).append(", ");
        sb.append("Appointment: ").append(getAppointment()).append(", ");
        sb.append("Client: ").append(getClient()).append(", ");
        sb.append("Datecreated: ").append(getDatecreated()).append(", ");
        sb.append("Gratuity: ").append(getGratuity()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Note: ").append(getNote()).append(", ");
        sb.append("Paymentsservice: ").append(getPaymentsservice() == null ? "null" : getPaymentsservice().size()).append(", ");
        sb.append("Paymentstype: ").append(getPaymentstype() == null ? "null" : getPaymentstype().size()).append(", ");
        sb.append("Shop: ").append(getShop()).append(", ");
        sb.append("Staff: ").append(getStaff()).append(", ");
        sb.append("Tax: ").append(getTax()).append(", ");
        sb.append("Version: ").append(getVersion());
        return sb.toString();
    }
}
