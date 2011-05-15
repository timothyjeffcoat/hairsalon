package com.yourhairsalon.booking.domain;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.validation.constraints.Size;
import com.yourhairsalon.booking.domain.Shop;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import javax.validation.constraints.NotNull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;
import javax.validation.constraints.Min;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooEntity
@RooJson
public class Reports {

    @Size(max = 255)
    private String report_name;

    @NotNull
    @ManyToOne
    private Shop shop;

	public String getReport_name() {
        return this.report_name;
    }

	public void setReport_name(String report_name) {
        this.report_name = report_name;
    }

	public Shop getShop() {
        return this.shop;
    }

	public void setShop(Shop shop) {
        this.shop = shop;
    }

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Report_name: ").append(getReport_name()).append(", ");
        sb.append("Shop: ").append(getShop()).append(", ");
        sb.append("Version: ").append(getVersion());
        return sb.toString();
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static Reports fromJsonToReports(String json) {
        return new JSONDeserializer<Reports>().use(null, Reports.class).deserialize(json);
    }

	public static String toJsonArray(Collection<Reports> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<Reports> fromJsonArrayToReportses(String json) {
        return new JSONDeserializer<List<Reports>>().use(null, ArrayList.class).use("values", Reports.class).deserialize(json);
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
            Reports attached = Reports.findReports(this.id);
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
    public Reports merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Reports merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new Reports().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countReportses() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Reports o", Long.class).getSingleResult();
    }

	public static List<Reports> findAllReportses() {
        return entityManager().createQuery("SELECT o FROM Reports o", Reports.class).getResultList();
    }

	public static Reports findReports(Long id) {
        if (id == null) return null;
        return entityManager().find(Reports.class, id);
    }

	public static List<Reports> findReportsEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Reports o", Reports.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
