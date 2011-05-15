package com.yourhairsalon.booking.domain;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import com.yourhairsalon.booking.domain.BaseService;
import java.util.HashSet;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.persistence.TypedQuery;
import org.springframework.roo.addon.json.RooJson;
import ca.digitalface.jasperoo.RooJasperoo;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import javax.persistence.ManyToMany;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooJson
@RooJasperoo
@RooEntity(finders = { "findServiceGroupsByShop", "findServiceGroupsByServices", "findServiceGroupsById" })
public class ServiceGroup extends AbstractGroup {

    @Min(0L)
    private Integer number_services;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<BaseService> services = new HashSet<BaseService>();

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Createddate: ").append(getCreateddate()).append(", ");
        sb.append("Group_name: ").append(getGroup_name()).append(", ");
        sb.append("Group_notes: ").append(getGroup_notes()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Number_services: ").append(getNumber_services()).append(", ");
        sb.append("Services: ").append(getServices() == null ? "null" : getServices().size()).append(", ");
        sb.append("Shop: ").append(getShop()).append(", ");
        sb.append("Version: ").append(getVersion());
        return sb.toString();
    }

	public boolean isReportable() {
        return true;
    }

	public static TypedQuery<ServiceGroup> findServiceGroupsById(Long id) {
        if (id == null) throw new IllegalArgumentException("The id argument is required");
        EntityManager em = ServiceGroup.entityManager();
        TypedQuery<ServiceGroup> q = em.createQuery("SELECT o FROM ServiceGroup AS o WHERE o.id = :id", ServiceGroup.class);
        q.setParameter("id", id);
        return q;
    }

	public static TypedQuery<ServiceGroup> findServiceGroupsByServices(Set<BaseService> services) {
        if (services == null) throw new IllegalArgumentException("The services argument is required");
        EntityManager em = ServiceGroup.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM ServiceGroup AS o WHERE");
        for (int i = 0; i < services.size(); i++) {
            if (i > 0) queryBuilder.append(" AND");
            queryBuilder.append(" :services_item").append(i).append(" MEMBER OF o.services");
        }
        TypedQuery<ServiceGroup> q = em.createQuery(queryBuilder.toString(), ServiceGroup.class);
        int servicesIndex = 0;
        for (BaseService _baseservice: services) {
            q.setParameter("services_item" + servicesIndex++, _baseservice);
        }
        return q;
    }

	public static TypedQuery<ServiceGroup> findServiceGroupsByShop(Shop shop) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        EntityManager em = ServiceGroup.entityManager();
        TypedQuery<ServiceGroup> q = em.createQuery("SELECT o FROM ServiceGroup AS o WHERE o.shop = :shop", ServiceGroup.class);
        q.setParameter("shop", shop);
        return q;
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static ServiceGroup fromJsonToServiceGroup(String json) {
        return new JSONDeserializer<ServiceGroup>().use(null, ServiceGroup.class).deserialize(json);
    }

	public static String toJsonArray(Collection<ServiceGroup> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<ServiceGroup> fromJsonArrayToServiceGroups(String json) {
        return new JSONDeserializer<List<ServiceGroup>>().use(null, ArrayList.class).use("values", ServiceGroup.class).deserialize(json);
    }

	public static long countServiceGroups() {
        return entityManager().createQuery("SELECT COUNT(o) FROM ServiceGroup o", Long.class).getSingleResult();
    }

	public static List<ServiceGroup> findAllServiceGroups() {
        return entityManager().createQuery("SELECT o FROM ServiceGroup o", ServiceGroup.class).getResultList();
    }

	public static ServiceGroup findServiceGroup(Long id) {
        if (id == null) return null;
        return entityManager().find(ServiceGroup.class, id);
    }

	public static List<ServiceGroup> findServiceGroupEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM ServiceGroup o", ServiceGroup.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public Integer getNumber_services() {
        return this.number_services;
    }

	public void setNumber_services(Integer number_services) {
        this.number_services = number_services;
    }

	public Set<BaseService> getServices() {
        return this.services;
    }

	public void setServices(Set<BaseService> services) {
        this.services = services;
    }
}
