package com.yourhairsalon.booking.domain;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;
import javax.validation.constraints.Min;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Configurable
@RooJavaBean
@RooToString
@RooEntity
@RooJson
public class PublicSchedule {

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Id: ").append(getId()).append(", ");
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
            PublicSchedule attached = PublicSchedule.findPublicSchedule(this.id);
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
    public PublicSchedule merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        PublicSchedule merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new PublicSchedule().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countPublicSchedules() {
        return entityManager().createQuery("SELECT COUNT(o) FROM PublicSchedule o", Long.class).getSingleResult();
    }

	public static List<PublicSchedule> findAllPublicSchedules() {
        return entityManager().createQuery("SELECT o FROM PublicSchedule o", PublicSchedule.class).getResultList();
    }

	public static PublicSchedule findPublicSchedule(Long id) {
        if (id == null) return null;
        return entityManager().find(PublicSchedule.class, id);
    }

	public static List<PublicSchedule> findPublicScheduleEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM PublicSchedule o", PublicSchedule.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static PublicSchedule fromJsonToPublicSchedule(String json) {
        return new JSONDeserializer<PublicSchedule>().use(null, PublicSchedule.class).deserialize(json);
    }

	public static String toJsonArray(Collection<PublicSchedule> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<PublicSchedule> fromJsonArrayToPublicSchedules(String json) {
        return new JSONDeserializer<List<PublicSchedule>>().use(null, ArrayList.class).use("values", PublicSchedule.class).deserialize(json);
    }
}
