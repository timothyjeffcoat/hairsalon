package com.yourhairsalon.booking.domain;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import javax.validation.constraints.NotNull;
import com.yourhairsalon.booking.domain.Payments;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;

@Entity
@Configurable
@RooJavaBean
@RooToString
@RooEntity
public class PaymentsService {

    @NotNull
    private Float amount;

    @NotNull
    private String description;

    @ManyToOne
    private Payments payments;

	public Float getAmount() {
        return this.amount;
    }

	public void setAmount(Float amount) {
        this.amount = amount;
    }

	public String getDescription() {
        return this.description;
    }

	public void setDescription(String description) {
        this.description = description;
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
        sb.append("Description: ").append(getDescription()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Payments: ").append(getPayments()).append(", ");
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
            PaymentsService attached = PaymentsService.findPaymentsService(this.id);
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
    public PaymentsService merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        PaymentsService merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new PaymentsService().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countPaymentsServices() {
        return entityManager().createQuery("SELECT COUNT(o) FROM PaymentsService o", Long.class).getSingleResult();
    }

	public static List<PaymentsService> findAllPaymentsServices() {
        return entityManager().createQuery("SELECT o FROM PaymentsService o", PaymentsService.class).getResultList();
    }

	public static PaymentsService findPaymentsService(Long id) {
        if (id == null) return null;
        return entityManager().find(PaymentsService.class, id);
    }

	public static List<PaymentsService> findPaymentsServiceEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM PaymentsService o", PaymentsService.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
