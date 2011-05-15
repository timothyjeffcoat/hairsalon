package com.yourhairsalon.booking.domain;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import com.yourhairsalon.booking.domain.Clients;
import javax.persistence.ManyToOne;
import java.util.List;
import java.util.Set;
import com.yourhairsalon.booking.domain.Appointment;
import java.util.HashSet;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.CascadeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import com.yourhairsalon.booking.domain.Shop;
import com.yourhairsalon.booking.domain.BaseService;
import com.yourhairsalon.booking.domain.AbstractService;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooEntity(finders = { "findCustomPricesByShopAndAppointmentAndService", "findCustomPricesByShopAndAppointmentAndServiceAndClient", "findCustomPricesByShop", "findCustomPricesByShopAndAppointment", "findCustomPricesByShopAndAppointmentAndClient" })
public class CustomPrice {

    @NotNull
    @Min(0L)
    private Float cost;

    @NotNull
    @ManyToOne
    private Clients client;

    @ManyToOne
    private Appointment appointment;

    @NotNull
    @ManyToOne
    private Shop shop;

    @ManyToOne
    private AbstractService service;

	public static TypedQuery<CustomPrice> findCustomPricesByShop(Shop shop) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        EntityManager em = CustomPrice.entityManager();
        TypedQuery<CustomPrice> q = em.createQuery("SELECT o FROM CustomPrice AS o WHERE o.shop = :shop", CustomPrice.class);
        q.setParameter("shop", shop);
        return q;
    }

	public static TypedQuery<CustomPrice> findCustomPricesByShopAndAppointment(Shop shop, Appointment appointment) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (appointment == null) throw new IllegalArgumentException("The appointment argument is required");
        EntityManager em = CustomPrice.entityManager();
        TypedQuery<CustomPrice> q = em.createQuery("SELECT o FROM CustomPrice AS o WHERE o.shop = :shop AND o.appointment = :appointment", CustomPrice.class);
        q.setParameter("shop", shop);
        q.setParameter("appointment", appointment);
        return q;
    }

	public static TypedQuery<CustomPrice> findCustomPricesByShopAndAppointmentAndClient(Shop shop, Appointment appointment, Clients client) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (appointment == null) throw new IllegalArgumentException("The appointment argument is required");
        if (client == null) throw new IllegalArgumentException("The client argument is required");
        EntityManager em = CustomPrice.entityManager();
        TypedQuery<CustomPrice> q = em.createQuery("SELECT o FROM CustomPrice AS o WHERE o.shop = :shop AND o.appointment = :appointment AND o.client = :client", CustomPrice.class);
        q.setParameter("shop", shop);
        q.setParameter("appointment", appointment);
        q.setParameter("client", client);
        return q;
    }

	public static TypedQuery<CustomPrice> findCustomPricesByShopAndAppointmentAndService(Shop shop, Appointment appointment, AbstractService service) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (appointment == null) throw new IllegalArgumentException("The appointment argument is required");
        if (service == null) throw new IllegalArgumentException("The service argument is required");
        EntityManager em = CustomPrice.entityManager();
        TypedQuery<CustomPrice> q = em.createQuery("SELECT o FROM CustomPrice AS o WHERE o.shop = :shop AND o.appointment = :appointment AND o.service = :service", CustomPrice.class);
        q.setParameter("shop", shop);
        q.setParameter("appointment", appointment);
        q.setParameter("service", service);
        return q;
    }

	public static TypedQuery<CustomPrice> findCustomPricesByShopAndAppointmentAndServiceAndClient(Shop shop, Appointment appointment, AbstractService service, Clients client) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (appointment == null) throw new IllegalArgumentException("The appointment argument is required");
        if (service == null) throw new IllegalArgumentException("The service argument is required");
        if (client == null) throw new IllegalArgumentException("The client argument is required");
        EntityManager em = CustomPrice.entityManager();
        TypedQuery<CustomPrice> q = em.createQuery("SELECT o FROM CustomPrice AS o WHERE o.shop = :shop AND o.appointment = :appointment AND o.service = :service AND o.client = :client", CustomPrice.class);
        q.setParameter("shop", shop);
        q.setParameter("appointment", appointment);
        q.setParameter("service", service);
        q.setParameter("client", client);
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
            CustomPrice attached = CustomPrice.findCustomPrice(this.id);
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
    public CustomPrice merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        CustomPrice merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new CustomPrice().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countCustomPrices() {
        return entityManager().createQuery("SELECT COUNT(o) FROM CustomPrice o", Long.class).getSingleResult();
    }

	public static List<CustomPrice> findAllCustomPrices() {
        return entityManager().createQuery("SELECT o FROM CustomPrice o", CustomPrice.class).getResultList();
    }

	public static CustomPrice findCustomPrice(Long id) {
        if (id == null) return null;
        return entityManager().find(CustomPrice.class, id);
    }

	public static List<CustomPrice> findCustomPriceEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM CustomPrice o", CustomPrice.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Appointment: ").append(getAppointment()).append(", ");
        sb.append("Client: ").append(getClient()).append(", ");
        sb.append("Cost: ").append(getCost()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Service: ").append(getService()).append(", ");
        sb.append("Shop: ").append(getShop()).append(", ");
        sb.append("Version: ").append(getVersion());
        return sb.toString();
    }

	public Float getCost() {
        return this.cost;
    }

	public void setCost(Float cost) {
        this.cost = cost;
    }

	public Clients getClient() {
        return this.client;
    }

	public void setClient(Clients client) {
        this.client = client;
    }

	public Appointment getAppointment() {
        return this.appointment;
    }

	public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

	public Shop getShop() {
        return this.shop;
    }

	public void setShop(Shop shop) {
        this.shop = shop;
    }

	public AbstractService getService() {
        return this.service;
    }

	public void setService(AbstractService service) {
        this.service = service;
    }
}
