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
import com.yourhairsalon.booking.domain.Clients;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.HashSet;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.persistence.TypedQuery;
import org.springframework.roo.addon.json.RooJson;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooJson
@RooEntity(finders = { "findClientGroupsByShop" })
public class ClientGroup extends AbstractGroup {

    @Min(0L)
    private Integer number_clients;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<Clients> clients = new HashSet<Clients>();

	public Integer getNumber_clients() {
        return this.number_clients;
    }

	public void setNumber_clients(Integer number_clients) {
        this.number_clients = number_clients;
    }

	public Set<Clients> getClients() {
        return this.clients;
    }

	public void setClients(Set<Clients> clients) {
        this.clients = clients;
    }

	public static long countClientGroups() {
        return entityManager().createQuery("SELECT COUNT(o) FROM ClientGroup o", Long.class).getSingleResult();
    }

	public static List<ClientGroup> findAllClientGroups() {
        return entityManager().createQuery("SELECT o FROM ClientGroup o", ClientGroup.class).getResultList();
    }

	public static ClientGroup findClientGroup(Long id) {
        if (id == null) return null;
        return entityManager().find(ClientGroup.class, id);
    }

	public static List<ClientGroup> findClientGroupEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM ClientGroup o", ClientGroup.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }

	public static ClientGroup fromJsonToClientGroup(String json) {
        return new JSONDeserializer<ClientGroup>().use(null, ClientGroup.class).deserialize(json);
    }

	public static String toJsonArray2(Collection<ClientGroup> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<ClientGroup> fromJsonArrayToClientGroups(String json) {
        return new JSONDeserializer<List<ClientGroup>>().use(null, ArrayList.class).use("values", ClientGroup.class).deserialize(json);
    }

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Clients: ").append(getClients() == null ? "null" : getClients().size()).append(", ");
        sb.append("Createddate: ").append(getCreateddate()).append(", ");
        sb.append("Group_name: ").append(getGroup_name()).append(", ");
        sb.append("Group_notes: ").append(getGroup_notes()).append(", ");
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Number_clients: ").append(getNumber_clients()).append(", ");
        sb.append("Shop: ").append(getShop()).append(", ");
        sb.append("Version: ").append(getVersion());
        return sb.toString();
    }

	public static TypedQuery<ClientGroup> findClientGroupsByShop(Shop shop) {
        if (shop == null) throw new IllegalArgumentException("The shop argument is required");
        EntityManager em = ClientGroup.entityManager();
        TypedQuery<ClientGroup> q = em.createQuery("SELECT o FROM ClientGroup AS o WHERE o.shop = :shop", ClientGroup.class);
        q.setParameter("shop", shop);
        return q;
    }
}
