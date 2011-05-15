package com.yourhairsalon.booking.domain;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.transaction.annotation.Transactional;
import ca.digitalface.jasperoo.RooJasperoo;
import com.yourhairsalon.booking.reference.RegistrationTypes;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import javax.validation.constraints.NotNull;
import javax.persistence.Enumerated;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Temporal;
import javax.persistence.Column;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Configurable
@RooJavaBean
@RooToString
@RooJson
@RooJasperoo
@RooEntity(finders = { "findShopsByShop_name", "findShopsByShop_nameEquals", "findShopsByShop_nameIsNotNull", "findShopsByShop_nameIsNull", "findShopsByShop_nameLike", "findShopsByShop_nameNotEquals", "findShopsByShopuuid", "findShopsById", "findShopsByShop_urlEquals" })
public class Shop {

    @Size(min = 1, max = 250)
    @Column(name = "shop_name", unique = true)
    private String shop_name;

    @Size(min = 1, max = 250)
    private String shopuuid;

    @NotNull
    @Enumerated
    private RegistrationTypes type;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date expiration_date;

    // # of purchased messages. incremented each time purchase happens
    // Long
    private Long number_sms_purchased;
    
    // # of messages used. cumulative number of messages sent
    // Long
    private Long number_sms_sent;
    
    // the last datetime messages purchased. this value updated each time new purchase happens
    // DateTime
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date sms_last_purchase_date;
    
    @Size(min = 1, max = 250)
    private String shop_url;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    @Column(name="ts", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date ts;

    public void setType(RegistrationTypes regtype) {
        this.type = regtype;
    }

    public RegistrationTypes getType() {
        return this.type;
    }

    public void setExpiration_date(Date expdate) {
        this.expiration_date = expdate;
    }

    public Date getExpiration_date() {
        return this.expiration_date;
    }

    public String getShop_name() {
        return this.shop_name;
    }

    public void setShop_name(String shop_name) {
        this.shop_name = shop_name;
    }

    public String getShopuuid() {
        return this.shopuuid;
    }

    /**
     * set the uid or unique identifier that is associated with the 
     * ldap security login
     * 
     * @param shopuuid
     */
    public void setShopuuid(String shopuuid) {
        this.shopuuid = shopuuid;
    }

	public String getShop_url() {
        return this.shop_url;
    }

	public void setShop_url(String shop_url) {
        this.shop_url = shop_url;
    }

	public Date getTs() {
        return this.ts;
    }

	public void setTs(Date ts) {
        this.ts = ts;
    }

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Expiration_date: ").append(getExpiration_date()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Shop_name: ").append(getShop_name()).append(", ");
        sb.append("Shop_url: ").append(getShop_url()).append(", ");
        sb.append("Shopuuid: ").append(getShopuuid()).append(", ");
        sb.append("Ts: ").append(getTs()).append(", ");
        sb.append("Type: ").append(getType()).append(", ");
        sb.append("number_sms_purchased: ").append(getNumber_sms_purchased()).append(", ");
        sb.append("number_sms_sent: ").append(getNumber_sms_sent()).append(", ");
        sb.append("sms_last_purchase_date: ").append(getSms_last_purchase_date()).append(", ");
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
            Shop attached = Shop.findShop(this.id);
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
    public Shop merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Shop merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new Shop().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countShops() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Shop o", Long.class).getSingleResult();
    }

	public static List<Shop> findAllShops() {
        return entityManager().createQuery("SELECT o FROM Shop o", Shop.class).getResultList();
    }

	public static Shop findShop(Long id) {
        if (id == null) return null;
        return entityManager().find(Shop.class, id);
    }

	public static List<Shop> findShopEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Shop o", Shop.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public static TypedQuery<Shop> findShopsById(Long id) {
        if (id == null) throw new IllegalArgumentException("The id argument is required");
        EntityManager em = Shop.entityManager();
        TypedQuery<Shop> q = em.createQuery("SELECT o FROM Shop AS o WHERE o.id = :id", Shop.class);
        q.setParameter("id", id);
        return q;
    }

	public static TypedQuery<Shop> findShopsByShop_name(String shop_name) {
        if (shop_name == null || shop_name.length() == 0) throw new IllegalArgumentException("The shop_name argument is required");
        EntityManager em = Shop.entityManager();
        TypedQuery<Shop> q = em.createQuery("SELECT o FROM Shop AS o WHERE o.shop_name = :shop_name", Shop.class);
        q.setParameter("shop_name", shop_name);
        return q;
    }

	public static TypedQuery<Shop> findShopsByShop_nameEquals(String shop_name) {
        if (shop_name == null || shop_name.length() == 0) throw new IllegalArgumentException("The shop_name argument is required");
        EntityManager em = Shop.entityManager();
        TypedQuery<Shop> q = em.createQuery("SELECT o FROM Shop AS o WHERE o.shop_name = :shop_name", Shop.class);
        q.setParameter("shop_name", shop_name);
        return q;
    }

	public static TypedQuery<Shop> findShopsByShop_nameIsNotNull() {
        EntityManager em = Shop.entityManager();
        TypedQuery<Shop> q = em.createQuery("SELECT o FROM Shop AS o WHERE o.shop_name IS NOT NULL", Shop.class);
        return q;
    }

	public static TypedQuery<Shop> findShopsByShop_nameIsNull() {
        EntityManager em = Shop.entityManager();
        TypedQuery<Shop> q = em.createQuery("SELECT o FROM Shop AS o WHERE o.shop_name IS NULL", Shop.class);
        return q;
    }

	public static TypedQuery<Shop> findShopsByShop_nameLike(String shop_name) {
        if (shop_name == null || shop_name.length() == 0) throw new IllegalArgumentException("The shop_name argument is required");
        shop_name = shop_name.replace('*', '%');
        if (shop_name.charAt(0) != '%') {
            shop_name = "%" + shop_name;
        }
        if (shop_name.charAt(shop_name.length() - 1) != '%') {
            shop_name = shop_name + "%";
        }
        EntityManager em = Shop.entityManager();
        TypedQuery<Shop> q = em.createQuery("SELECT o FROM Shop AS o WHERE LOWER(o.shop_name) LIKE LOWER(:shop_name)", Shop.class);
        q.setParameter("shop_name", shop_name);
        return q;
    }

	public static TypedQuery<Shop> findShopsByShop_nameNotEquals(String shop_name) {
        if (shop_name == null || shop_name.length() == 0) throw new IllegalArgumentException("The shop_name argument is required");
        EntityManager em = Shop.entityManager();
        TypedQuery<Shop> q = em.createQuery("SELECT o FROM Shop AS o WHERE o.shop_name != :shop_name", Shop.class);
        q.setParameter("shop_name", shop_name);
        return q;
    }

	public static TypedQuery<Shop> findShopsByShop_urlEquals(String shop_url) {
        if (shop_url == null || shop_url.length() == 0) throw new IllegalArgumentException("The shop_url argument is required");
        EntityManager em = Shop.entityManager();
        TypedQuery<Shop> q = em.createQuery("SELECT o FROM Shop AS o WHERE o.shop_url = :shop_url", Shop.class);
        q.setParameter("shop_url", shop_url);
        return q;
    }

	public static TypedQuery<Shop> findShopsByShopuuid(String shopuuid) {
        if (shopuuid == null || shopuuid.length() == 0) throw new IllegalArgumentException("The shopuuid argument is required");
        EntityManager em = Shop.entityManager();
        TypedQuery<Shop> q = em.createQuery("SELECT o FROM Shop AS o WHERE o.shopuuid = :shopuuid", Shop.class);
        q.setParameter("shopuuid", shopuuid);
        return q;
    }

	public boolean isReportable() {
        return true;
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static Shop fromJsonToShop(String json) {
        return new JSONDeserializer<Shop>().use(null, Shop.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Shop> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<Shop> fromJsonArrayToShops(String json) {
        return new JSONDeserializer<List<Shop>>().use(null, ArrayList.class).use("values", Shop.class).deserialize(json);
    }

	public Long getNumber_sms_purchased() {
		return number_sms_purchased;
	}

	public void setNumber_sms_purchased(Long number_sms_purchased) {
		this.number_sms_purchased = number_sms_purchased;
	}

	public Long getNumber_sms_sent() {
		return number_sms_sent;
	}

	public void setNumber_sms_sent(Long number_sms_sent) {
		this.number_sms_sent = number_sms_sent;
	}

	public Date getSms_last_purchase_date() {
		return sms_last_purchase_date;
	}

	public void setSms_last_purchase_date(Date sms_last_purchase_date) {
		this.sms_last_purchase_date = sms_last_purchase_date;
	}

}
