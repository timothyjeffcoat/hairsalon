package com.yourhairsalon.booking.domain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
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
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import com.yourhairsalon.booking.domain.Staff;
import javax.persistence.ManyToOne;
import com.yourhairsalon.booking.domain.Clients;
import com.yourhairsalon.booking.domain.BaseService;
import com.yourhairsalon.booking.domain.Shop;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import java.util.Calendar;
import javax.persistence.ManyToMany;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.transaction.annotation.Transactional;
import com.yourhairsalon.booking.reference.ScheduleStatus;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import javax.persistence.Enumerated;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooJson
@RooEntity(finders = { "findAppointmentsByShopAndAppointmentDateEquals", "findAppointmentsByShopAndAppointmentDateBetween", "findAppointmentsByShopAndCancelledNot", "findAppointmentsByShopAndClient", "findAppointmentsByShopAndCancelledNotAndAppointmentDateBetween", "findAppointmentsByShopAndStatusNotAndAppointmentDateBetween", "findAppointmentsByShopAndStatusNotAndCancelledNotAndAppointmentDateBetween", "findAppointmentsByAppointmentDateBetween", "findAppointmentsByBeginDateTimeGreaterThanAndEndDateTimeLessThan", "findAppointmentsByShopAndBeginDateTimeGreaterThanAndEndDateTimeLessThan", "findAppointmentsByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanAndEndDateTimeLessThan", "findAppointmentsByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanEqualsAndEndDateTimeLessThanEquals", "findAppointmentsByShopAndStatusAndAppointmentDateBetween", "findAppointmentsByShopAndStatusAndAppointmentDateGreaterThanEquals", "findAppointmentsByShopAndStatus", "findAppointmentsByShopAndStatusAndClientNotAndAppointmentDateBetween", "findAppointmentsByShopAndStatusAndClientIsNotNullAndAppointmentDateBetween", "findAppointmentsByShopAndStaff", "findAppointmentsByShopAndStaffEquals", "findAppointmentsByShopAndStatusNotAndAppointmentDateBetweenAndStaffEquals", "findAppointmentsByShopAndAppointmentDateGreaterThanAndRecur_parentEquals", "findAppointmentsByShopAndStatusAndClientIsNotNullAndAppointmentDateBetweenAndStaffEquals" })
public class Appointment {

    private static final Log log = LogFactory.getLog(Appointment.class);

    private Boolean confirmed;

    private Boolean cancelled;

    private Boolean reoccurring;

    private Boolean reoccurring_email_all;

    private Integer frequency_week;

    private Integer duration_month;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date reoccur_start_date;

    @Size(max = 255)
    private String description;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date appointmentDate;

    @NotNull
    @Min(0L)
    private Integer starttime;

    @NotNull
    @Min(0L)
    private Integer endtime;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date checkintime;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date checkouttime;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date confirmeddate;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date createddate;

    @NotNull
    @ManyToOne
    private Staff staff;

    @ManyToOne
    private Clients client;

    @NotNull
    @ManyToOne
    private Shop shop;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "MS")
    private Calendar endDateTime;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "MS")
    private Calendar beginDateTime;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<BaseService> services = new HashSet<BaseService>();

    @Size(max = 255)
    private String notes;

    @Size(max = 255)
    private String personallabel;

    @Enumerated
    private ScheduleStatus status;

    @Min(0L)
    private Integer parent;

    @Min(0L)
    private Long recur_parent;

    @Size(max = 255)
    private String requested_image_path;

    @Size(max = 255)
    private String requested_image_name;

    @Size(max = 255)
    private String gcalid;

    @Size(max = 255)
    private String staff_gcalid;

    public static TypedQuery<Appointment> appointmentConflict(Shop shop, Date appointmentDate, Calendar beginDateTime, Calendar endDateTime) {
        log.debug("ENTERED appointmentConflict");
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (appointmentDate == null) throw new IllegalArgumentException("The appointmentDate argument is required");
        if (beginDateTime == null) throw new IllegalArgumentException("The beginDateTime argument is required");
        if (endDateTime == null) throw new IllegalArgumentException("The endDateTime argument is required");
        log.debug("appointmentDate: " + appointmentDate.toString());
        log.debug("beginDateTime: " + beginDateTime.toString());
        log.debug("endDateTime: " + endDateTime.toString());
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.appointmentDate = :appointmentDate  AND o.beginDateTime < :endDateTime  AND o.endDateTime > :beginDateTime", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("appointmentDate", appointmentDate);
        q.setParameter("beginDateTime", beginDateTime);
        q.setParameter("endDateTime", endDateTime);
        log.debug("EXITING appointmentConflict");
        return q;
    }

    public static TypedQuery<Appointment> appointmentConflictStaff(Staff staff, Shop shop, Date appointmentDate, Calendar beginDateTime, Calendar endDateTime, ScheduleStatus status) {
        log.debug("ENTERED appointmentConflictStaff");
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (appointmentDate == null) throw new IllegalArgumentException("The appointmentDate argument is required");
        if (beginDateTime == null) throw new IllegalArgumentException("The beginDateTime argument is required");
        if (endDateTime == null) throw new IllegalArgumentException("The endDateTime argument is required");
        log.debug("appointmentDate: " + appointmentDate.toString());
        log.debug("beginDateTime: " + beginDateTime.toString());
        log.debug("endDateTime: " + endDateTime.toString());
        log.debug("staff: " + staff.toString());
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.appointmentDate = :appointmentDate  AND o.beginDateTime < :endDateTime  AND o.endDateTime > :beginDateTime AND o.staff = :staff", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("appointmentDate", appointmentDate);
        q.setParameter("beginDateTime", beginDateTime);
        q.setParameter("endDateTime", endDateTime);
        q.setParameter("staff", staff);
        log.debug("EXITING appointmentConflictStaff");
        return q;
    }

    public static TypedQuery<Appointment> appointmentConflictStaffStatus(Staff staff, Shop shop, Date appointmentDate, Calendar beginDateTime, Calendar endDateTime, ScheduleStatus status) {
        log.debug("ENTERED appointmentConflictStaffStatus");
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (appointmentDate == null) throw new IllegalArgumentException("The appointmentDate argument is required");
        if (beginDateTime == null) throw new IllegalArgumentException("The beginDateTime argument is required");
        if (endDateTime == null) throw new IllegalArgumentException("The endDateTime argument is required");
        log.debug("appointmentDate: " + appointmentDate.toString());
        log.debug("beginDateTime: " + beginDateTime.toString());
        log.debug("endDateTime: " + endDateTime.toString());
        log.debug("staff: " + staff.toString());
        log.debug("status: " + status.toString());
        long sid = staff.getId();
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.status = :status  AND o.appointmentDate = :appointmentDate  AND o.beginDateTime < :endDateTime  AND o.endDateTime > :beginDateTime AND o.staff = :staff", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("appointmentDate", appointmentDate);
        q.setParameter("beginDateTime", beginDateTime);
        q.setParameter("endDateTime", endDateTime);
        q.setParameter("status", status);
        q.setParameter("staff", staff);
        log.debug("EXITING appointmentConflictStaffStatus");
        return q;
    }

    public static TypedQuery<Appointment> appointmentConflictStaffStatusList(Staff staff, Shop shop, Date appointmentDate, Calendar beginDateTime, Calendar endDateTime) {
        log.debug("ENTERED appointmentConflictStaffStatus");
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (appointmentDate == null) throw new IllegalArgumentException("The appointmentDate argument is required");
        if (beginDateTime == null) throw new IllegalArgumentException("The beginDateTime argument is required");
        if (endDateTime == null) throw new IllegalArgumentException("The endDateTime argument is required");
        log.debug("appointmentDate: " + appointmentDate.toString());
        log.debug("beginDateTime: " + beginDateTime.toString());
        log.debug("endDateTime: " + endDateTime.toString());
        log.debug("staff: " + staff.toString());
        List ids = new ArrayList<ScheduleStatus>(5);
        ids.add(ScheduleStatus.ACTIVE);
        ids.add(ScheduleStatus.LUNCH);
        ids.add(ScheduleStatus.SICK);
        ids.add(ScheduleStatus.HOLIDAY);
        ids.add(ScheduleStatus.OTHER);
        long sid = staff.getId();
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.status IN(:status)  AND o.appointmentDate = :appointmentDate  AND o.beginDateTime < :endDateTime  AND o.endDateTime > :beginDateTime AND o.staff = :staff", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("appointmentDate", appointmentDate);
        q.setParameter("beginDateTime", beginDateTime);
        q.setParameter("endDateTime", endDateTime);
        q.setParameter("status", ids);
        q.setParameter("staff", staff);
        log.debug("EXITING appointmentConflictStaffStatus");
        return q;
    }

    public Long getRecur_parent() {
        return recur_parent;
    }

    public void setRecur_parent(Long recur_parent) {
        this.recur_parent = recur_parent;
    }

	public Boolean getConfirmed() {
        return this.confirmed;
    }

	public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

	public Boolean getCancelled() {
        return this.cancelled;
    }

	public void setCancelled(Boolean cancelled) {
        this.cancelled = cancelled;
    }

	public Boolean getReoccurring() {
        return this.reoccurring;
    }

	public void setReoccurring(Boolean reoccurring) {
        this.reoccurring = reoccurring;
    }

	public Boolean getReoccurring_email_all() {
        return this.reoccurring_email_all;
    }

	public void setReoccurring_email_all(Boolean reoccurring_email_all) {
        this.reoccurring_email_all = reoccurring_email_all;
    }

	public Integer getFrequency_week() {
        return this.frequency_week;
    }

	public void setFrequency_week(Integer frequency_week) {
        this.frequency_week = frequency_week;
    }

	public Integer getDuration_month() {
        return this.duration_month;
    }

	public void setDuration_month(Integer duration_month) {
        this.duration_month = duration_month;
    }

	public Date getReoccur_start_date() {
        return this.reoccur_start_date;
    }

	public void setReoccur_start_date(Date reoccur_start_date) {
        this.reoccur_start_date = reoccur_start_date;
    }

	public String getDescription() {
        return this.description;
    }

	public void setDescription(String description) {
        this.description = description;
    }

	public Date getAppointmentDate() {
        return this.appointmentDate;
    }

	public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

	public Integer getStarttime() {
        return this.starttime;
    }

	public void setStarttime(Integer starttime) {
        this.starttime = starttime;
    }

	public Integer getEndtime() {
        return this.endtime;
    }

	public void setEndtime(Integer endtime) {
        this.endtime = endtime;
    }

	public Date getCheckintime() {
        return this.checkintime;
    }

	public void setCheckintime(Date checkintime) {
        this.checkintime = checkintime;
    }

	public Date getCheckouttime() {
        return this.checkouttime;
    }

	public void setCheckouttime(Date checkouttime) {
        this.checkouttime = checkouttime;
    }

	public Date getConfirmeddate() {
        return this.confirmeddate;
    }

	public void setConfirmeddate(Date confirmeddate) {
        this.confirmeddate = confirmeddate;
    }

	public Date getCreateddate() {
        return this.createddate;
    }

	public void setCreateddate(Date createddate) {
        this.createddate = createddate;
    }

	public Staff getStaff() {
        return this.staff;
    }

	public void setStaff(Staff staff) {
        this.staff = staff;
    }

	public Clients getClient() {
        return this.client;
    }

	public void setClient(Clients client) {
        this.client = client;
    }

	public Shop getShop() {
        return this.shop;
    }

	public void setShop(Shop shop) {
        this.shop = shop;
    }

	public Calendar getEndDateTime() {
        return this.endDateTime;
    }

	public void setEndDateTime(Calendar endDateTime) {
        this.endDateTime = endDateTime;
    }

	public Calendar getBeginDateTime() {
        return this.beginDateTime;
    }

	public void setBeginDateTime(Calendar beginDateTime) {
        this.beginDateTime = beginDateTime;
    }

	public Set<BaseService> getServices() {
        return this.services;
    }

	public void setServices(Set<BaseService> services) {
        this.services = services;
    }

	public String getNotes() {
        return this.notes;
    }

	public void setNotes(String notes) {
        this.notes = notes;
    }

	public String getPersonallabel() {
        return this.personallabel;
    }

	public void setPersonallabel(String personallabel) {
        this.personallabel = personallabel;
    }

	public ScheduleStatus getStatus() {
        return this.status;
    }

	public void setStatus(ScheduleStatus status) {
        this.status = status;
    }

	public Integer getParent() {
        return this.parent;
    }

	public void setParent(Integer parent) {
        this.parent = parent;
    }

	public String getRequested_image_path() {
        return this.requested_image_path;
    }

	public void setRequested_image_path(String requested_image_path) {
        this.requested_image_path = requested_image_path;
    }

	public String getRequested_image_name() {
        return this.requested_image_name;
    }

	public void setRequested_image_name(String requested_image_name) {
        this.requested_image_name = requested_image_name;
    }

	public String getGcalid() {
        return this.gcalid;
    }

	public void setGcalid(String gcalid) {
        this.gcalid = gcalid;
    }

	public String getStaff_gcalid() {
        return this.staff_gcalid;
    }

	public void setStaff_gcalid(String staff_gcalid) {
        this.staff_gcalid = staff_gcalid;
    }

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AppointmentDate: ").append(getAppointmentDate()).append(", ");
        sb.append("BeginDateTime: ").append(getBeginDateTime() == null ? "null" : getBeginDateTime().getTime()).append(", ");
        sb.append("Cancelled: ").append(getCancelled()).append(", ");
        sb.append("Checkintime: ").append(getCheckintime()).append(", ");
        sb.append("Checkouttime: ").append(getCheckouttime()).append(", ");
        sb.append("Client: ").append(getClient()).append(", ");
        sb.append("Confirmed: ").append(getConfirmed()).append(", ");
        sb.append("Confirmeddate: ").append(getConfirmeddate()).append(", ");
        sb.append("Createddate: ").append(getCreateddate()).append(", ");
        sb.append("Description: ").append(getDescription()).append(", ");
        sb.append("Duration_month: ").append(getDuration_month()).append(", ");
        sb.append("EndDateTime: ").append(getEndDateTime() == null ? "null" : getEndDateTime().getTime()).append(", ");
        sb.append("Endtime: ").append(getEndtime()).append(", ");
        sb.append("Frequency_week: ").append(getFrequency_week()).append(", ");
        sb.append("Gcalid: ").append(getGcalid()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Notes: ").append(getNotes()).append(", ");
        sb.append("Parent: ").append(getParent()).append(", ");
        sb.append("Personallabel: ").append(getPersonallabel()).append(", ");
        sb.append("Recur_parent: ").append(getRecur_parent()).append(", ");
        sb.append("Reoccur_start_date: ").append(getReoccur_start_date()).append(", ");
        sb.append("Reoccurring: ").append(getReoccurring()).append(", ");
        sb.append("Reoccurring_email_all: ").append(getReoccurring_email_all()).append(", ");
        sb.append("Requested_image_name: ").append(getRequested_image_name()).append(", ");
        sb.append("Requested_image_path: ").append(getRequested_image_path()).append(", ");
        sb.append("Services: ").append(getServices() == null ? "null" : getServices().size()).append(", ");
        sb.append("Shop: ").append(getShop()).append(", ");
        sb.append("Staff: ").append(getStaff()).append(", ");
        sb.append("Staff_gcalid: ").append(getStaff_gcalid()).append(", ");
        sb.append("Starttime: ").append(getStarttime()).append(", ");
        sb.append("Status: ").append(getStatus()).append(", ");
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
            Appointment attached = Appointment.findAppointment(this.id);
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
    public Appointment merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Appointment merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new Appointment().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countAppointments() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Appointment o", Long.class).getSingleResult();
    }

	public static List<Appointment> findAllAppointments() {
        return entityManager().createQuery("SELECT o FROM Appointment o", Appointment.class).getResultList();
    }

	public static Appointment findAppointment(Long id) {
        if (id == null) return null;
        return entityManager().find(Appointment.class, id);
    }

	public static List<Appointment> findAppointmentEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Appointment o", Appointment.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static Appointment fromJsonToAppointment(String json) {
        return new JSONDeserializer<Appointment>().use(null, Appointment.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Appointment> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<Appointment> fromJsonArrayToAppointments(String json) {
        return new JSONDeserializer<List<Appointment>>().use(null, ArrayList.class).use("values", Appointment.class).deserialize(json);
    }

	public static TypedQuery<Appointment> findAppointmentsByAppointmentDateBetween(Date minAppointmentDate, Date maxAppointmentDate) {
        if (minAppointmentDate == null) throw new IllegalArgumentException("The minAppointmentDate argument is required");
        if (maxAppointmentDate == null) throw new IllegalArgumentException("The maxAppointmentDate argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.appointmentDate BETWEEN :minAppointmentDate AND :maxAppointmentDate", Appointment.class);
        q.setParameter("minAppointmentDate", minAppointmentDate);
        q.setParameter("maxAppointmentDate", maxAppointmentDate);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByBeginDateTimeGreaterThanAndEndDateTimeLessThan(Calendar beginDateTime, Calendar endDateTime) {
        if (beginDateTime == null) throw new IllegalArgumentException("The beginDateTime argument is required");
        if (endDateTime == null) throw new IllegalArgumentException("The endDateTime argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.beginDateTime > :beginDateTime  AND o.endDateTime < :endDateTime", Appointment.class);
        q.setParameter("beginDateTime", beginDateTime);
        q.setParameter("endDateTime", endDateTime);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndAppointmentDateBetween(Shop shop, Date minAppointmentDate, Date maxAppointmentDate) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (minAppointmentDate == null) throw new IllegalArgumentException("The minAppointmentDate argument is required");
        if (maxAppointmentDate == null) throw new IllegalArgumentException("The maxAppointmentDate argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.appointmentDate BETWEEN :minAppointmentDate AND :maxAppointmentDate", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("minAppointmentDate", minAppointmentDate);
        q.setParameter("maxAppointmentDate", maxAppointmentDate);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndAppointmentDateEquals(Shop shop, Date appointmentDate) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (appointmentDate == null) throw new IllegalArgumentException("The appointmentDate argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.appointmentDate = :appointmentDate", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("appointmentDate", appointmentDate);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanAndEndDateTimeLessThan(Shop shop, Date appointmentDate, Calendar beginDateTime, Calendar endDateTime) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (appointmentDate == null) throw new IllegalArgumentException("The appointmentDate argument is required");
        if (beginDateTime == null) throw new IllegalArgumentException("The beginDateTime argument is required");
        if (endDateTime == null) throw new IllegalArgumentException("The endDateTime argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.appointmentDate = :appointmentDate  AND o.beginDateTime > :beginDateTime  AND o.endDateTime < :endDateTime", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("appointmentDate", appointmentDate);
        q.setParameter("beginDateTime", beginDateTime);
        q.setParameter("endDateTime", endDateTime);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndAppointmentDateEqualsAndBeginDateTimeGreaterThanEqualsAndEndDateTimeLessThanEquals(Shop shop, Date appointmentDate, Calendar beginDateTime, Calendar endDateTime) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (appointmentDate == null) throw new IllegalArgumentException("The appointmentDate argument is required");
        if (beginDateTime == null) throw new IllegalArgumentException("The beginDateTime argument is required");
        if (endDateTime == null) throw new IllegalArgumentException("The endDateTime argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.appointmentDate = :appointmentDate  AND o.beginDateTime >= :beginDateTime  AND o.endDateTime <= :endDateTime", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("appointmentDate", appointmentDate);
        q.setParameter("beginDateTime", beginDateTime);
        q.setParameter("endDateTime", endDateTime);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndAppointmentDateGreaterThanAndRecur_parentEquals(Shop shop, Date appointmentDate, Long recur_parent) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (appointmentDate == null) throw new IllegalArgumentException("The appointmentDate argument is required");
        if (recur_parent == null) throw new IllegalArgumentException("The recur_parent argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.appointmentDate > :appointmentDate  AND o.recur_parent = :recur_parent", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("appointmentDate", appointmentDate);
        q.setParameter("recur_parent", recur_parent);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndBeginDateTimeGreaterThanAndEndDateTimeLessThan(Shop shop, Calendar beginDateTime, Calendar endDateTime) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (beginDateTime == null) throw new IllegalArgumentException("The beginDateTime argument is required");
        if (endDateTime == null) throw new IllegalArgumentException("The endDateTime argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.beginDateTime > :beginDateTime  AND o.endDateTime < :endDateTime", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("beginDateTime", beginDateTime);
        q.setParameter("endDateTime", endDateTime);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndCancelledNot(Shop shop, Boolean cancelled) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (cancelled == null) throw new IllegalArgumentException("The cancelled argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.cancelled IS NOT :cancelled", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("cancelled", cancelled);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndCancelledNotAndAppointmentDateBetween(Shop shop, Boolean cancelled, Date minAppointmentDate, Date maxAppointmentDate) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (cancelled == null) throw new IllegalArgumentException("The cancelled argument is required");
        if (minAppointmentDate == null) throw new IllegalArgumentException("The minAppointmentDate argument is required");
        if (maxAppointmentDate == null) throw new IllegalArgumentException("The maxAppointmentDate argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.cancelled IS NOT :cancelled  AND o.appointmentDate BETWEEN :minAppointmentDate AND :maxAppointmentDate", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("cancelled", cancelled);
        q.setParameter("minAppointmentDate", minAppointmentDate);
        q.setParameter("maxAppointmentDate", maxAppointmentDate);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndClient(Shop shop, Clients client) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (client == null) throw new IllegalArgumentException("The client argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.client = :client", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("client", client);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndStaff(Shop shop, Staff staff) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (staff == null) throw new IllegalArgumentException("The staff argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.staff = :staff", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("staff", staff);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndStaffEquals(Shop shop, Staff staff) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (staff == null) throw new IllegalArgumentException("The staff argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.staff = :staff", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("staff", staff);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndStatus(Shop shop, ScheduleStatus status) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (status == null) throw new IllegalArgumentException("The status argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.status = :status", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("status", status);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndStatusAndAppointmentDateBetween(Shop shop, ScheduleStatus status, Date minAppointmentDate, Date maxAppointmentDate) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (status == null) throw new IllegalArgumentException("The status argument is required");
        if (minAppointmentDate == null) throw new IllegalArgumentException("The minAppointmentDate argument is required");
        if (maxAppointmentDate == null) throw new IllegalArgumentException("The maxAppointmentDate argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.status = :status AND o.appointmentDate BETWEEN :minAppointmentDate AND :maxAppointmentDate", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("status", status);
        q.setParameter("minAppointmentDate", minAppointmentDate);
        q.setParameter("maxAppointmentDate", maxAppointmentDate);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndStatusAndAppointmentDateGreaterThanEquals(Shop shop, ScheduleStatus status, Date appointmentDate) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (status == null) throw new IllegalArgumentException("The status argument is required");
        if (appointmentDate == null) throw new IllegalArgumentException("The appointmentDate argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.status = :status AND o.appointmentDate >= :appointmentDate", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("status", status);
        q.setParameter("appointmentDate", appointmentDate);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndStatusAndClientIsNotNullAndAppointmentDateBetween(Shop shop, ScheduleStatus status, Date minAppointmentDate, Date maxAppointmentDate) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (status == null) throw new IllegalArgumentException("The status argument is required");
        if (minAppointmentDate == null) throw new IllegalArgumentException("The minAppointmentDate argument is required");
        if (maxAppointmentDate == null) throw new IllegalArgumentException("The maxAppointmentDate argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.status = :status AND o.client IS NOT NULL  AND o.appointmentDate BETWEEN :minAppointmentDate AND :maxAppointmentDate", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("status", status);
        q.setParameter("minAppointmentDate", minAppointmentDate);
        q.setParameter("maxAppointmentDate", maxAppointmentDate);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndStatusAndClientIsNotNullAndAppointmentDateBetweenAndStaffEquals(Shop shop, ScheduleStatus status, Date minAppointmentDate, Date maxAppointmentDate, Staff staff) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (status == null) throw new IllegalArgumentException("The status argument is required");
        if (minAppointmentDate == null) throw new IllegalArgumentException("The minAppointmentDate argument is required");
        if (maxAppointmentDate == null) throw new IllegalArgumentException("The maxAppointmentDate argument is required");
        if (staff == null) throw new IllegalArgumentException("The staff argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.status = :status AND o.client IS NOT NULL  AND o.appointmentDate BETWEEN :minAppointmentDate AND :maxAppointmentDate  AND o.staff = :staff", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("status", status);
        q.setParameter("minAppointmentDate", minAppointmentDate);
        q.setParameter("maxAppointmentDate", maxAppointmentDate);
        q.setParameter("staff", staff);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndStatusAndClientNotAndAppointmentDateBetween(Shop shop, ScheduleStatus status, Clients client, Date minAppointmentDate, Date maxAppointmentDate) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (status == null) throw new IllegalArgumentException("The status argument is required");
        if (client == null) throw new IllegalArgumentException("The client argument is required");
        if (minAppointmentDate == null) throw new IllegalArgumentException("The minAppointmentDate argument is required");
        if (maxAppointmentDate == null) throw new IllegalArgumentException("The maxAppointmentDate argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.status = :status AND o.client IS NOT :client  AND o.appointmentDate BETWEEN :minAppointmentDate AND :maxAppointmentDate", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("status", status);
        q.setParameter("client", client);
        q.setParameter("minAppointmentDate", minAppointmentDate);
        q.setParameter("maxAppointmentDate", maxAppointmentDate);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndStatusNotAndAppointmentDateBetween(Shop shop, ScheduleStatus status, Date minAppointmentDate, Date maxAppointmentDate) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (status == null) throw new IllegalArgumentException("The status argument is required");
        if (minAppointmentDate == null) throw new IllegalArgumentException("The minAppointmentDate argument is required");
        if (maxAppointmentDate == null) throw new IllegalArgumentException("The maxAppointmentDate argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.status IS NOT :status  AND o.appointmentDate BETWEEN :minAppointmentDate AND :maxAppointmentDate", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("status", status);
        q.setParameter("minAppointmentDate", minAppointmentDate);
        q.setParameter("maxAppointmentDate", maxAppointmentDate);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndStatusNotAndAppointmentDateBetweenAndStaffEquals(Shop shop, ScheduleStatus status, Date minAppointmentDate, Date maxAppointmentDate, Staff staff) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (status == null) throw new IllegalArgumentException("The status argument is required");
        if (minAppointmentDate == null) throw new IllegalArgumentException("The minAppointmentDate argument is required");
        if (maxAppointmentDate == null) throw new IllegalArgumentException("The maxAppointmentDate argument is required");
        if (staff == null) throw new IllegalArgumentException("The staff argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.status IS NOT :status  AND o.appointmentDate BETWEEN :minAppointmentDate AND :maxAppointmentDate  AND o.staff = :staff", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("status", status);
        q.setParameter("minAppointmentDate", minAppointmentDate);
        q.setParameter("maxAppointmentDate", maxAppointmentDate);
        q.setParameter("staff", staff);
        return q;
    }

	public static TypedQuery<Appointment> findAppointmentsByShopAndStatusNotAndCancelledNotAndAppointmentDateBetween(Shop shop, ScheduleStatus status, Boolean cancelled, Date minAppointmentDate, Date maxAppointmentDate) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        if (status == null) throw new IllegalArgumentException("The status argument is required");
        if (cancelled == null) throw new IllegalArgumentException("The cancelled argument is required");
        if (minAppointmentDate == null) throw new IllegalArgumentException("The minAppointmentDate argument is required");
        if (maxAppointmentDate == null) throw new IllegalArgumentException("The maxAppointmentDate argument is required");
        EntityManager em = Appointment.entityManager();
        TypedQuery<Appointment> q = em.createQuery("SELECT o FROM Appointment AS o WHERE o.shop = :shop AND o.status IS NOT :status  AND o.cancelled IS NOT :cancelled  AND o.appointmentDate BETWEEN :minAppointmentDate AND :maxAppointmentDate", Appointment.class);
        q.setParameter("shop", shop);
        q.setParameter("status", status);
        q.setParameter("cancelled", cancelled);
        q.setParameter("minAppointmentDate", minAppointmentDate);
        q.setParameter("maxAppointmentDate", maxAppointmentDate);
        return q;
    }
}
