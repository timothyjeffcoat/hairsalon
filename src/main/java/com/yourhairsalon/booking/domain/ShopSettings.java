package com.yourhairsalon.booking.domain;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.yourhairsalon.booking.reference.ClientDisplay;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import com.yourhairsalon.booking.domain.Shop;
import javax.persistence.ManyToOne;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.transaction.annotation.Transactional;
import ca.digitalface.jasperoo.RooJasperoo;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import javax.validation.constraints.Min;

@Entity
@Configurable
@RooJavaBean
@RooToString
@RooJson
@RooJasperoo
@RooEntity(finders = { "findShopSettingsesByShop", "findShopSettingsesById", "findShopSettingsesByEmail_address" })
public class ShopSettings {

    @NotNull
    private boolean receiveConfirmations;

    @Size(min = 1, max = 250)
    private String email_subject;

    @Size(min = 1, max = 250)
    private String email_message;

    @Size(min = 1, max = 250)
    private String email_signature;

    @Size(min = 1, max = 250)
    private String username;

    @Size(min = 1, max = 250)
    private String user_password;

    @Size(min = 1, max = 16)
    private String store_phone;

    @Size(min = 1, max = 16)
    private String default_appt_length;

    @NotNull
    @Enumerated
    private ClientDisplay clientdisplay;

    @Size(min = 1, max = 16)
    private String image_file_logo;

    @NotNull
    @ManyToOne
    private Shop shop;

    @Size(min = 1, max = 250)
    private String locale;

    @Size(min = 4, max = 250)
    private String email_address;

    @Size(min = 1, max = 250)
    @Column(name = "timezone", columnDefinition = "TIMEZONE DEFAULT MST")
    private String timezone;

    @Min(0L)
    private Integer number_days_notify;

    @NotNull
    private boolean use_gcalendar_for_shop;

    @NotNull
    private boolean allow_staff_gcalendar;

    @Size(min = 1, max = 250)
    @Column(name = "iemail_subject", nullable = false, columnDefinition = "TEXT default 'Thank you for scheduling your appointent for a ${apptservicename} on ${apptdate} @ ${appttime}'")
    private String iemail_subject;

    @Size(min = 1, max = 500)
    @Column(name = "iemail_message", nullable = false, columnDefinition = "TEXT default '<p>	Dear ${clientfullname},</p><p>	Your appointment for a ${apptservicename}</p><p>	is on ${apptdate} @ ${appttime}</p><p>	Please give 24 hours notice if you need to change or cancel this appointment.</p><p>	Sincerely,</p><p>	${stafffullname}</p><p>	p.s. Thank you for referring your friends and family.&nbsp; We appreciate your help!</p>'")
    private String iemail_message;

    @Size(min = 1, max = 250)
    @Column(name = "iemail_signature", nullable = false, columnDefinition = "TEXT default 'Thank you for scheduling your appointent for a ${apptservicename} on ${apptdate} @ ${appttime}'")
    private String iemail_signature;

    @Size(min = 4, max = 250)
    @Column(name = "iemail_address", nullable = false, columnDefinition = "TEXT default 'ENTER YOUR EMAIL ADDRESS HERE'")
    private String iemail_address;

    @Size(min = 1, max = 250)
    @Column(name = "remail_subject", nullable = false, columnDefinition = "TEXT default 'Appointment receipt'")
    private String remail_subject;

    @Size(min = 1, max = 500)
    @Column(name = "remail_message", nullable = false, columnDefinition = "TEXT default '<p>	Dear ${clientfullname},</p><p>	Here is your receipt for</p><p>	your ${apptservicename}</p><p>	${apptdate} @ ${appttime}</p><p>	${apptserviceprice}</p><p>	I appreciate your business!</p><p>	${stafffullname}</p>'")
    private String remail_message;

    @Size(min = 1, max = 250)
    @Column(name = "remail_signature", nullable = false, columnDefinition = "TEXT default 'p.s. I would love to work for your friends and family too. Thank you for telling them about me!'")
    private String remail_signature;

    @Size(min = 4, max = 250)
    @Column(name = "remail_address", nullable = false, columnDefinition = "TEXT default 'ENTER YOUR EMAIL ADDRESS HERE'")
    private String remail_address;

    @Size(min = 1, max = 500)
    @Column(name = "email_big_message", nullable = false, columnDefinition = "TEXT default '<p>	Dear ${clientfullname},</p><p>	This is a reminder that your appointment for ${apptservicename}</p><p>	Is on date ${apptdate} time ${appttime}</p><p>	&nbsp;</p><p>	Sincerely,</p><p>	${stafffullname}</p>'")
    private String email_big_message;

    @Size(min = 1, max = 500)
    @Column(name = "isms_message", nullable = false, columnDefinition = "TEXT default 'Hi ${clientfullname}, this is a quick reminder that you have an appointment on ${apptdate} ${appttime} at ${shopname}. We look forward to seeing you then. Please call us at ${shopphone} if you need to cancel or reschedule :)'")
    private String isms_message;
    
    @Size(min = 1, max = 500)
    @Column(name = "rsms_message", nullable = false, columnDefinition = "TEXT default '​${clientfullname}, Receipt for appointment on ${apptdate} ${appttime} at ${shopname}.'")
    private String rsms_message;
    
    @Size(min = 1, max = 500)
    @Column(name = "sms_message", nullable = false, columnDefinition = "TEXT default 'Hi ${clientfullname}, this is a quick reminder that you have an appointment on ${apptdate} ${appttime} at ${shopname}. We look forward to seeing you then. Please call us at ${shopphone} if you need to cancel or reschedule :)'")
    private String sms_message;
    
    
	public boolean isReportable() {
        return true;
    }

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Clientdisplay: ").append(getClientdisplay()).append(", ");
        sb.append("Default_appt_length: ").append(getDefault_appt_length()).append(", ");
        sb.append("Email_address: ").append(getEmail_address()).append(", ");
        sb.append("sms_message: ").append(getSms_message()).append(", ");
        sb.append("isms_message: ").append(getIsms_message()).append(", ");
        sb.append("rsms_message: ").append(getRsms_message()).append(", ");
        sb.append("Email_big_message: ").append(getEmail_big_message()).append(", ");
        sb.append("Email_message: ").append(getEmail_message()).append(", ");
        sb.append("Email_signature: ").append(getEmail_signature()).append(", ");
        sb.append("Email_subject: ").append(getEmail_subject()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Iemail_address: ").append(getIemail_address()).append(", ");
        sb.append("Iemail_message: ").append(getIemail_message()).append(", ");
        sb.append("Iemail_signature: ").append(getIemail_signature()).append(", ");
        sb.append("Iemail_subject: ").append(getIemail_subject()).append(", ");
        sb.append("Image_file_logo: ").append(getImage_file_logo()).append(", ");
        sb.append("Locale: ").append(getLocale()).append(", ");
        sb.append("Number_days_notify: ").append(getNumber_days_notify()).append(", ");
        sb.append("Remail_address: ").append(getRemail_address()).append(", ");
        sb.append("Remail_message: ").append(getRemail_message()).append(", ");
        sb.append("Remail_signature: ").append(getRemail_signature()).append(", ");
        sb.append("Remail_subject: ").append(getRemail_subject()).append(", ");
        sb.append("Shop: ").append(getShop()).append(", ");
        sb.append("Store_phone: ").append(getStore_phone()).append(", ");
        sb.append("Timezone: ").append(getTimezone()).append(", ");
        sb.append("User_password: ").append(getUser_password()).append(", ");
        sb.append("Username: ").append(getUsername()).append(", ");
        sb.append("Version: ").append(getVersion()).append(", ");
        sb.append("Allow_staff_gcalendar: ").append(isAllow_staff_gcalendar()).append(", ");
        sb.append("ReceiveConfirmations: ").append(isReceiveConfirmations()).append(", ");
        sb.append("Use_gcalendar_for_shop: ").append(isUse_gcalendar_for_shop());
        return sb.toString();
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static ShopSettings fromJsonToShopSettings(String json) {
        return new JSONDeserializer<ShopSettings>().use(null, ShopSettings.class).deserialize(json);
    }

	public static String toJsonArray(Collection<ShopSettings> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<ShopSettings> fromJsonArrayToShopSettingses(String json) {
        return new JSONDeserializer<List<ShopSettings>>().use(null, ArrayList.class).use("values", ShopSettings.class).deserialize(json);
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
            ShopSettings attached = ShopSettings.findShopSettings(this.id);
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
    public ShopSettings merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        ShopSettings merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new ShopSettings().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countShopSettingses() {
        return entityManager().createQuery("SELECT COUNT(o) FROM ShopSettings o", Long.class).getSingleResult();
    }

	public static List<ShopSettings> findAllShopSettingses() {
        return entityManager().createQuery("SELECT o FROM ShopSettings o", ShopSettings.class).getResultList();
    }

	public static ShopSettings findShopSettings(Long id) {
        if (id == null) return null;
        return entityManager().find(ShopSettings.class, id);
    }

	public static List<ShopSettings> findShopSettingsEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM ShopSettings o", ShopSettings.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public boolean isReceiveConfirmations() {
        return this.receiveConfirmations;
    }

	public void setReceiveConfirmations(boolean receiveConfirmations) {
        this.receiveConfirmations = receiveConfirmations;
    }

	public String getEmail_subject() {
        return this.email_subject;
    }

	public void setEmail_subject(String email_subject) {
        this.email_subject = email_subject;
    }

	public String getEmail_message() {
        return this.email_message;
    }

	public void setEmail_message(String email_message) {
        this.email_message = email_message;
    }

	public String getEmail_signature() {
        return this.email_signature;
    }

	public void setEmail_signature(String email_signature) {
        this.email_signature = email_signature;
    }

	public String getUsername() {
        return this.username;
    }

	public void setUsername(String username) {
        this.username = username;
    }

	public String getUser_password() {
        return this.user_password;
    }

	public void setUser_password(String user_password) {
        this.user_password = user_password;
    }

	public String getStore_phone() {
        return this.store_phone;
    }

	public void setStore_phone(String store_phone) {
        this.store_phone = store_phone;
    }

	public String getDefault_appt_length() {
        return this.default_appt_length;
    }

	public void setDefault_appt_length(String default_appt_length) {
        this.default_appt_length = default_appt_length;
    }

	public ClientDisplay getClientdisplay() {
        return this.clientdisplay;
    }

	public void setClientdisplay(ClientDisplay clientdisplay) {
        this.clientdisplay = clientdisplay;
    }

	public String getImage_file_logo() {
        return this.image_file_logo;
    }

	public void setImage_file_logo(String image_file_logo) {
        this.image_file_logo = image_file_logo;
    }

	public Shop getShop() {
        return this.shop;
    }

	public void setShop(Shop shop) {
        this.shop = shop;
    }

	public String getLocale() {
        return this.locale;
    }

	public void setLocale(String locale) {
        this.locale = locale;
    }

	public String getEmail_address() {
        return this.email_address;
    }

	public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

	public String getTimezone() {
        return this.timezone;
    }

	public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

	public Integer getNumber_days_notify() {
        return this.number_days_notify;
    }

	public void setNumber_days_notify(Integer number_days_notify) {
        this.number_days_notify = number_days_notify;
    }

	public boolean isUse_gcalendar_for_shop() {
        return this.use_gcalendar_for_shop;
    }

	public void setUse_gcalendar_for_shop(boolean use_gcalendar_for_shop) {
        this.use_gcalendar_for_shop = use_gcalendar_for_shop;
    }

	public boolean isAllow_staff_gcalendar() {
        return this.allow_staff_gcalendar;
    }

	public void setAllow_staff_gcalendar(boolean allow_staff_gcalendar) {
        this.allow_staff_gcalendar = allow_staff_gcalendar;
    }

	public String getIemail_subject() {
        return this.iemail_subject;
    }

	public void setIemail_subject(String iemail_subject) {
        this.iemail_subject = iemail_subject;
    }

	public String getIemail_message() {
        return this.iemail_message;
    }

	public void setIemail_message(String iemail_message) {
        this.iemail_message = iemail_message;
    }

	public String getIemail_signature() {
        return this.iemail_signature;
    }

	public void setIemail_signature(String iemail_signature) {
        this.iemail_signature = iemail_signature;
    }

	public String getIemail_address() {
        return this.iemail_address;
    }

	public void setIemail_address(String iemail_address) {
        this.iemail_address = iemail_address;
    }

	public String getRemail_subject() {
        return this.remail_subject;
    }

	public void setRemail_subject(String remail_subject) {
        this.remail_subject = remail_subject;
    }

	public String getRemail_message() {
        return this.remail_message;
    }

	public void setRemail_message(String remail_message) {
        this.remail_message = remail_message;
    }

	public String getRemail_signature() {
        return this.remail_signature;
    }

	public void setRemail_signature(String remail_signature) {
        this.remail_signature = remail_signature;
    }

	public String getRemail_address() {
        return this.remail_address;
    }

	public void setRemail_address(String remail_address) {
        this.remail_address = remail_address;
    }

	public String getEmail_big_message() {
        return this.email_big_message;
    }

	public void setEmail_big_message(String email_big_message) {
        this.email_big_message = email_big_message;
    }

	public static TypedQuery<ShopSettings> findShopSettingsesByEmail_address(String email_address) {
        if (email_address == null || email_address.length() == 0) throw new IllegalArgumentException("The email_address argument is required");
        EntityManager em = ShopSettings.entityManager();
        TypedQuery<ShopSettings> q = em.createQuery("SELECT o FROM ShopSettings AS o WHERE o.email_address = :email_address", ShopSettings.class);
        q.setParameter("email_address", email_address);
        return q;
    }

	public static TypedQuery<ShopSettings> findShopSettingsesById(Long id) {
        if (id == null) throw new IllegalArgumentException("The id argument is required");
        EntityManager em = ShopSettings.entityManager();
        TypedQuery<ShopSettings> q = em.createQuery("SELECT o FROM ShopSettings AS o WHERE o.id = :id", ShopSettings.class);
        q.setParameter("id", id);
        return q;
    }

	public static TypedQuery<ShopSettings> findShopSettingsesByShop(Shop shop) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        EntityManager em = ShopSettings.entityManager();
        TypedQuery<ShopSettings> q = em.createQuery("SELECT o FROM ShopSettings AS o WHERE o.shop = :shop", ShopSettings.class);
        q.setParameter("shop", shop);
        return q;
    }

	public String getIsms_message() {
		return isms_message;
	}

	public void setIsms_message(String isms_message) {
		this.isms_message = isms_message;
	}

	public String getRsms_message() {
		return rsms_message;
	}

	public void setRsms_message(String rsms_message) {
		this.rsms_message = rsms_message;
	}

	public String getSms_message() {
		return sms_message;
	}

	public void setSms_message(String sms_message) {
		this.sms_message = sms_message;
	}
}
