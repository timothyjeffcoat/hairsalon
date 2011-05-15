package com.yourhairsalon.booking.domain;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.validation.constraints.NotNull;
import com.yourhairsalon.booking.reference.PaymentTypes;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;
import com.yourhairsalon.booking.domain.Payments;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooEntity
@RooJson
public class PaymentsType {

    @NotNull
    private Float amount;

    @NotNull
    @Enumerated
    private PaymentTypes type;

    @ManyToOne
    private Payments payments;

	public Float getAmount() {
        return this.amount;
    }

	public void setAmount(Float amount) {
        this.amount = amount;
    }

	public PaymentTypes getType() {
        return this.type;
    }

	public void setType(PaymentTypes type) {
        this.type = type;
    }

	public Payments getPayments() {
        return this.payments;
    }

	public void setPayments(Payments payments) {
        this.payments = payments;
    }

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Amount: ").append(getAmount()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Payments: ").append(getPayments()).append(", ");
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
            PaymentsType attached = PaymentsType.findPaymentsType(this.id);
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
    public PaymentsType merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        PaymentsType merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new PaymentsType().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countPaymentsTypes() {
        return entityManager().createQuery("SELECT COUNT(o) FROM PaymentsType o", Long.class).getSingleResult();
    }

	public static List<PaymentsType> findAllPaymentsTypes() {
        return entityManager().createQuery("SELECT o FROM PaymentsType o", PaymentsType.class).getResultList();
    }

	public static PaymentsType findPaymentsType(Long id) {
        if (id == null) return null;
        return entityManager().find(PaymentsType.class, id);
    }

	public static List<PaymentsType> findPaymentsTypeEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM PaymentsType o", PaymentsType.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static PaymentsType fromJsonToPaymentsType(String json) {
        return new JSONDeserializer<PaymentsType>().use(null, PaymentsType.class).deserialize(json);
    }

	public static String toJsonArray(Collection<PaymentsType> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<PaymentsType> fromJsonArrayToPaymentsTypes(String json) {
        return new JSONDeserializer<List<PaymentsType>>().use(null, ArrayList.class).use("values", PaymentsType.class).deserialize(json);
    }
}
