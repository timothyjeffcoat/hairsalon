package com.yourhairsalon.booking.domain;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;
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
import com.yourhairsalon.booking.domain.Shop;
import javax.persistence.ManyToOne;
import com.yourhairsalon.booking.reference.RegistrationTypes;
import javax.persistence.Enumerated;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooEntity
public class Registration {

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date datecreated;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date dateupdated;

    private String itemname;

    private String itemnumber;

    private String paymentstatus;

    private Float paymentamount;

    private String paymentcurrency;

    private String transactionid;

    private String receiveremail;

    private String payeremail;

    private String note;

    @NotNull
    @ManyToOne
    private Shop shop;

    @NotNull
    @Enumerated
    private RegistrationTypes type;

    private String token;

    private String payerid;

	public Date getDatecreated() {
        return this.datecreated;
    }

	public void setDatecreated(Date datecreated) {
        this.datecreated = datecreated;
    }

	public Date getDateupdated() {
        return this.dateupdated;
    }

	public void setDateupdated(Date dateupdated) {
        this.dateupdated = dateupdated;
    }

	public String getItemname() {
        return this.itemname;
    }

	public void setItemname(String itemname) {
        this.itemname = itemname;
    }

	public String getItemnumber() {
        return this.itemnumber;
    }

	public void setItemnumber(String itemnumber) {
        this.itemnumber = itemnumber;
    }

	public String getPaymentstatus() {
        return this.paymentstatus;
    }

	public void setPaymentstatus(String paymentstatus) {
        this.paymentstatus = paymentstatus;
    }

	public Float getPaymentamount() {
        return this.paymentamount;
    }

	public void setPaymentamount(Float paymentamount) {
        this.paymentamount = paymentamount;
    }

	public String getPaymentcurrency() {
        return this.paymentcurrency;
    }

	public void setPaymentcurrency(String paymentcurrency) {
        this.paymentcurrency = paymentcurrency;
    }

	public String getTransactionid() {
        return this.transactionid;
    }

	public void setTransactionid(String transactionid) {
        this.transactionid = transactionid;
    }

	public String getReceiveremail() {
        return this.receiveremail;
    }

	public void setReceiveremail(String receiveremail) {
        this.receiveremail = receiveremail;
    }

	public String getPayeremail() {
        return this.payeremail;
    }

	public void setPayeremail(String payeremail) {
        this.payeremail = payeremail;
    }

	public String getNote() {
        return this.note;
    }

	public void setNote(String note) {
        this.note = note;
    }

	public Shop getShop() {
        return this.shop;
    }

	public void setShop(Shop shop) {
        this.shop = shop;
    }

	public RegistrationTypes getType() {
        return this.type;
    }

	public void setType(RegistrationTypes type) {
        this.type = type;
    }

	public String getToken() {
        return this.token;
    }

	public void setToken(String token) {
        this.token = token;
    }

	public String getPayerid() {
        return this.payerid;
    }

	public void setPayerid(String payerid) {
        this.payerid = payerid;
    }

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Datecreated: ").append(getDatecreated()).append(", ");
        sb.append("Dateupdated: ").append(getDateupdated()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Itemname: ").append(getItemname()).append(", ");
        sb.append("Itemnumber: ").append(getItemnumber()).append(", ");
        sb.append("Note: ").append(getNote()).append(", ");
        sb.append("Payeremail: ").append(getPayeremail()).append(", ");
        sb.append("Payerid: ").append(getPayerid()).append(", ");
        sb.append("Paymentamount: ").append(getPaymentamount()).append(", ");
        sb.append("Paymentcurrency: ").append(getPaymentcurrency()).append(", ");
        sb.append("Paymentstatus: ").append(getPaymentstatus()).append(", ");
        sb.append("Receiveremail: ").append(getReceiveremail()).append(", ");
        sb.append("Shop: ").append(getShop()).append(", ");
        sb.append("Token: ").append(getToken()).append(", ");
        sb.append("Transactionid: ").append(getTransactionid()).append(", ");
        sb.append("Type: ").append(getType()).append(", ");
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
            Registration attached = Registration.findRegistration(this.id);
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
    public Registration merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Registration merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new Registration().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countRegistrations() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Registration o", Long.class).getSingleResult();
    }

	public static List<Registration> findAllRegistrations() {
        return entityManager().createQuery("SELECT o FROM Registration o", Registration.class).getResultList();
    }

	public static Registration findRegistration(Long id) {
        if (id == null) return null;
        return entityManager().find(Registration.class, id);
    }

	public static List<Registration> findRegistrationEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Registration o", Registration.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
