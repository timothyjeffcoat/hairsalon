/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yourhairsalon.booking.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;

/**
 * This implementation uses
 * DirContextAdapter for managing attribute values. It has been specified in the
 * Spring Context that the DirObjectFactory should be used when creating objects
 * from contexts, which defaults to creating DirContextAdapter objects. This
 * means that we can use a ContextMapper to map from the found contexts to our
 * domain objects. This is especially useful since we in this case have
 * properties in our domain objects that depend on parts of the DN.
 * 
 * We could have worked with Attributes and an AttributesMapper implementation
 * instead, but working with Attributes is a bore and also, working with
 * AttributesMapper objects (or, indeed Attributes) does not give us access to
 * the distinguished name. However, we do use it in one method that only needs a
 * single attribute: {@link #getAllPersonNames()}.
 * 
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 * 
 * @modified tim jeffcoat, scheduleem
 */
/**
 * For spring security and dynamic groups I need to consider this thread
 * http://forum.springsource.org/showthread.php?55259-Dynamic-Group-DN-Attribute
 * talks about creating a custom implementation of the LdapAuthoritiesPopulator 
 * 
 */
public class PersonDaoImpl {
	private static Logger log = LoggerFactory.getLogger(PersonDaoImpl.class);
	private LdapTemplate ldapTemplate;
	private String ldaptreepathminusroot;
	private String ldapbase;
	
	/*
	 * @see PersonDao#create(Person)
	 */
	public void create(Person person, String shop) {
		log.debug("ENTERED create");
		log.debug("person "+ person);
		Name dn = buildDn(person);
		log.debug("dn " +dn.toString());
		DirContextAdapter context = new DirContextAdapter(dn);
		log.debug("context "+ context);
		mapToContext(person, context);
		log.debug("about to bind");
		ldapTemplate.bind(dn, context, null);
		log.debug("bound");
		// employeeNumber use as storage for date created
		try{
			
			DirContextOperations context_operation = ldapTemplate.lookupContext(dn);
			log.debug("context_operation 1"+ context_operation);
			context_operation.addAttributeValue("employeeNumber", "" + new Date());
			context_operation.addAttributeValue("o", "" + shop);
			log.debug("context_operation 2"+ context_operation);
			ldapTemplate.modifyAttributes(context_operation);
			log.debug("context_operation 3"+ context_operation);
			context_operation.addAttributeValue("userPassword", "" + person.getPassword());			
		}catch(Exception e){
			log.error("EXCEPTION TRYING TO ADD VALUE TO ATTRIBUTE employeeNumber");
			log.error(e.getMessage());
		}
		log.debug("EXITING create");
	}

	public boolean createCustomer(Person person, String shop){
		log.debug("ENTERED createCustomer");
		boolean succeeded = false;
		// add person
		if(uidExists(person.getUid())== false){
			log.debug("attempting to create person");
			create(person,shop);
			//add person to group
			String tdn = toStringbuildDn(person);
			log.debug("tdn: "+tdn);
			Name dn = groupCustomerAdminDN();
			DirContextOperations context = ldapTemplate.lookupContext(dn);
			context.addAttributeValue("uniqueMember", tdn);
			ldapTemplate.modifyAttributes(context);
			succeeded = true;
		}
		log.debug("EXITING createCustomer");
		return succeeded;
	}

	public boolean createCustomerStaff(Person person, String shop){
		log.debug("ENTERED createCustomerStaff");
		boolean succeeded = false;
		// add person
		if(uidExists(person.getUid())== false){
			log.debug("attempting to create person");
			create(person,shop); // this creates the person in the users
			//add person to group
			String tdn = toStringbuildDn(person);
			log.debug("tdn: "+tdn);
			Name dn = groupCustomerStaffDN();
			log.debug("dn: "+dn.toString());
			DirContextOperations context = ldapTemplate.lookupContext(dn);
			log.debug("track 1");
			context.addAttributeValue("uniqueMember", tdn);
			log.debug("track 2");
			ldapTemplate.modifyAttributes(context); // suppose to add new customerStaff to said groups
			log.debug("track 3");
			succeeded = true;
		}
		log.debug("EXITING createCustomerStaff");
		return succeeded;
	}
	
	public boolean createCustomerCustomer(Person person, String shop){
		log.debug("ENTERED createCustomerCustomer");
		boolean succeeded = false;
		// add person
		if(uidExists(person.getUid())== false){
			log.debug("attempting to create person");
			create(person, shop);
			//add person to group
			String tdn = toStringbuildDn(person);
			log.debug("tdn: "+tdn);
			Name dn = groupCustomerCustomerDN();
			DirContextOperations context = ldapTemplate.lookupContext(dn);
			context.addAttributeValue("uniqueMember", tdn);
			ldapTemplate.modifyAttributes(context);
			succeeded = true;
		}
		log.debug("EXITING createCustomerCustomer");
		return succeeded;
	}
	
	public boolean createDirectAccount(Person person, String shop){
		boolean succeeded = false;
		// add person
		if(uidExists(person.getUid())== false){
			create(person,shop);
			//add person to group
			String tdn = toStringbuildDn(person);
			
			Name dn = groupDirectAccountsDN();
			DirContextOperations context = ldapTemplate.lookupContext(dn);
			context.addAttributeValue("uniqueMember", tdn);
			ldapTemplate.modifyAttributes(context);
			succeeded = true;
		}
		return succeeded;
	}

	public boolean createSystemAccount(Person person, String shop){
		boolean succeeded = false;
		// add person
		if(uidExists(person.getUid())== false){
			create(person, shop);
			//add person to group
			String tdn = toStringbuildDn(person);
			
			Name dn = groupSystemAccountsDN();
			DirContextOperations context = ldapTemplate.lookupContext(dn);
			context.addAttributeValue("uniqueMember", tdn);
			ldapTemplate.modifyAttributes(context);
			succeeded = true;
		}
		return succeeded;
	}
	
	public boolean updateUsernme(Person person) {
		Name dn = buildDn(person);
		DirContextAdapter context = (DirContextAdapter) ldapTemplate.lookup(dn);
		boolean success = mapToContextUpdateUid(person, context);
		ldapTemplate.modifyAttributes(dn, context.getModificationItems());
		return success;
	}

	/*
	 * @see PersonDao#update(Person)
	 */
	public boolean update(Person person) {
		Name dn = buildDn(person);
		DirContextAdapter context = (DirContextAdapter) ldapTemplate.lookup(dn);
		boolean success = mapToContextUpdate(person, context);
		ldapTemplate.modifyAttributes(dn, context.getModificationItems());
		return success;
	}
	/**
	 * Bypasses the check on the old password. Updates based on password.
	 */
	public boolean changePassword(Person person) {
		log.debug("ENTERED changePassword");
		Name dn = buildDn(person);
		log.debug("debug 1");
		DirContextAdapter context = (DirContextAdapter) ldapTemplate.lookup(dn);
		log.debug("debug 2");
		boolean success = mapToContextUpdatePassword(person, context);
		log.debug("debug 3");
		ldapTemplate.modifyAttributes(dn, context.getModificationItems());
		log.debug("EXITING changePassword");
		return success;
	}

	/*
	 * @see PersonDao#delete(Person)
	 */
	public void delete(Person person) {
		// delete from groups
		String tdn = toStringbuildDn(person);
		
		Name dn = groupCustomerAdminDN();
		Name acctdn = groupDirectAccountsDN();
		Name sysdn = groupSystemAccountsDN();
		
		DirContextOperations context = ldapTemplate.lookupContext(dn);
		context.removeAttributeValue("uniqueMember", tdn);
		ldapTemplate.modifyAttributes(context);
		
		DirContextOperations acctcontext = ldapTemplate.lookupContext(acctdn);
		acctcontext.removeAttributeValue("uniqueMember", tdn);
		ldapTemplate.modifyAttributes(acctcontext);
		
		DirContextOperations syscontext = ldapTemplate.lookupContext(sysdn);
		syscontext.removeAttributeValue("uniqueMember", tdn);
		ldapTemplate.modifyAttributes(syscontext);

		
		// delete from people
		ldapTemplate.unbind(buildDn(person));
	}

	/*
	 * @see PersonDao#getAllPersonNames()
	 */
	public List getAllPersonNames() {
		EqualsFilter filter = new EqualsFilter("objectclass", "inetOrgPerson");
		return ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter.encode(), new AttributesMapper() {
			public Object mapFromAttributes(Attributes attrs) throws NamingException {
				return attrs.get("cn").get();
			}
		});
	}

	/*
	 * @see PersonDao#findAll()
	 */
	public List findAll() {
		EqualsFilter filter = new EqualsFilter("objectclass", "inetOrgPerson");
		return ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter.encode(), getContextMapper());
	}

	public Person findByUid(Person uid){
		DistinguishedName dn = buildDn(uid);
		String[] att = new String[]{"uid",uid.getUid()};
		return (Person) ldapTemplate.lookup(dn, att, getContextMapper());
	}
	
	public boolean uidExists(String uid){
		boolean exists = false;
		List list = ldapTemplate.search("", "(uid="+uid+")", new AttributesMapper() {
			public Object mapFromAttributes(Attributes attrs) throws NamingException {
				return attrs.get("uid").get();
			}
		});	
		if(list.size()>0){
			exists = true;
		}
		return exists;
	}
	/**
	 * the primary key is uid
	 * 
	 * @see PersonDao#findByPrimaryKey(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	public Person findByPrimaryKey(String uid) {
		DistinguishedName dn = buildDn(uid);
		return (Person) ldapTemplate.lookup(dn, getContextMapper());
	}

	private ContextMapper getContextMapper() {
		return new PersonContextMapper();
	}

	private DistinguishedName buildDn(Person person) {
		return buildDn(person.getUid());
	}

	private DistinguishedName buildDn(String uid) {
		log.debug("ENTERED buildDn");
		
		DistinguishedName dn = new DistinguishedName();
		log.debug("dn");
		// parse ldaptreepathminusroot and sequentially add attributes
		String  t = ldaptreepathminusroot;
		log.debug("ldaptreepathminusroot: " + ldaptreepathminusroot);
		NavigableMap<String, String> items = new TreeMap<String, String>();
		log.debug("2");
		String[] pairs = t.split(",");
		log.debug("3");
		for (String pair : pairs)
		{
		    String[] kv = pair.split("=");
		    items.put(kv[0], kv[1]);
		}		
		log.debug("4");
		for (Map.Entry<String, String> e : items.entrySet()) {
			dn.add(e.getKey(), e.getValue());
		}		
		log.debug("5 uid" + uid);
		dn.add("uid", uid);
		log.debug("EXITING buildDn");
		return dn;
	}

	private String toStringbuildDn(Person uid) {
		String dn = ldapbase;
		// parse ldaptreepathminusroot and sequentially add attributes
		String  t = ldaptreepathminusroot;
		NavigableMap<String, String> items = new TreeMap<String, String>();
		String[] pairs = t.split(",");
		for (String pair : pairs)
		{
		    String[] kv = pair.split("=");
		    items.put(kv[0], kv[1]);
		}		
		for (Map.Entry<String, String> e : items.descendingMap().entrySet()) {
			dn = e.getKey() + "="+ e.getValue()+","+dn;
		}		
		dn = "uid="+ uid.getUid()+"," + dn;
		return dn;
	}

	/**
	 * putting a user into a customer's customer group
	 * @return
	 */
	private DistinguishedName groupCustomerCustomerDN() {
		DistinguishedName dn = new DistinguishedName();
		dn.add("ou","groups");
		dn.add("cn", "customersCustomer");
		return dn;
	}
	
	private DistinguishedName groupCustomerAdminDN() {
		DistinguishedName dn = new DistinguishedName();
		dn.add("ou","groups");
		dn.add("cn", "customerAdmin");
		return dn;
	}

	private DistinguishedName groupCustomerStaffDN() {
		DistinguishedName dn = new DistinguishedName();
		dn.add("ou","Groups");
		dn.add("cn", "customerStaff");
		return dn;
	}

	private DistinguishedName groupSystemAccountsDN() {
		DistinguishedName dn = new DistinguishedName();
		dn.add("ou","groups");
		dn.add("cn", "systemAccounts");
		return dn;
	}

	private DistinguishedName groupDirectAccountsDN() {
		DistinguishedName dn = new DistinguishedName();
		dn.add("ou","groups");
		dn.add("cn", "directAccounts");
		return dn;
	}
	
	/**
	 * specifically for updating the password
	 * 
	 * @param person
	 * @param context
	 */
	private boolean mapToContextUpdate(Person person, DirContextAdapter context) {
		boolean success = true;
		context.setAttributeValues("objectclass", new String[] { "top", "inetOrgPerson" });
		context.setAttributeValue("cn", "PLACEHOLDER");
		context.setAttributeValue("sn", "PLACEHOLDER");
		log.debug("AM I UPDATING AN OLD PASSWORD WITH A NEW ONE???");
		if(person.getUserPassword() != null && person.getOldPassword() != null){
			// verify the old password is correct before allowing setting new password
			log.debug("TRYING TO UPDATE OLD PASSWORD WITH A NEW ONE!!!!");
			boolean allow = ldapTemplate.authenticate(DistinguishedName.EMPTY_PATH, "(uid="+person.getUid()+")", person.getOldPassword());
			if(allow){
				// now allow updating of new password
				log.debug("UPDATE OLD PASSWORD WITH A NEW ONE SUCCEEDED !!!!");
				context.setAttributeValue("userPassword", person.getUserPassword());
			}else{
				success = false;
			}
		}
		return success;
	}
	private boolean mapToContextUpdateUid(Person person, DirContextAdapter context) {
		boolean success = true;
		context.setAttributeValues("objectclass", new String[] { "top", "inetOrgPerson" });
		context.setAttributeValue("cn", "PLACEHOLDER");
		context.setAttributeValue("sn", "PLACEHOLDER");
		log.debug("AM I UPDATING Username UID");
		context.setAttributeValue("uid", person.getUsername());
		log.debug("UPDATE Uid WITH A NEW ONE SUCCEEDED !!!!");
		return success;
	}

	/**
	 * checks to see if password is correct
	 * 
	 * @param person
	 * @param context
	 * @return
	 */
	public boolean verifyPassword(Person person){
		log.debug("ENTERED changePassword");
		Name dn = buildDn(person);
		log.debug("debug 1");
		DirContextAdapter context = (DirContextAdapter) ldapTemplate.lookup(dn);
		boolean allow = false;
		try {
			allow = ldapTemplate.authenticate(DistinguishedName.EMPTY_PATH, "(uid="+person.getUid()+")", person.getOldPassword());
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			allow = false;
		}
		return allow;
	}
	/**
	 * specifically for updating the password, bypassing check against current password
	 * 
	 * @param person
	 * @param context
	 */
	private boolean mapToContextUpdatePassword(Person person, DirContextAdapter context) {
		boolean success = true;
		context.setAttributeValues("objectclass", new String[] { "top", "inetOrgPerson" });
		context.setAttributeValue("cn", "PLACEHOLDER");
		context.setAttributeValue("sn", "PLACEHOLDER");
		log.debug("AM I UPDATING AN OLD PASSWORD WITH A NEW ONE???");
		if(person.getUserPassword() != null ){
			// now allow updating of new password
			context.setAttributeValue("userPassword", person.getUserPassword());
			log.debug("UPDATE OLD PASSWORD WITH A NEW ONE SUCCEEDED !!!!");
		}
		return success;
	}
	
	private void mapToContext(Person person, DirContextAdapter context) {
		context.setAttributeValues("objectclass", new String[] { "top", "inetOrgPerson" });
		context.setAttributeValue("cn", "PLACEHOLDER");
		context.setAttributeValue("sn", "PLACEHOLDER");
		if(person.getUserPassword() != null){
			context.setAttributeValue("userPassword", person.getUserPassword());
		}
//		if(person.getUid() != null){
//			context.setAttributeValue("userPassword", person.getUid());
//		}
		if(person.getPassword() != null){
			context.setAttributeValue("userPassword", person.getPassword());
		}
		
	}

	/**
	 * Maps from DirContextAdapter to Person objects. A DN for a person will be
	 * of the form <code>cn=[fullname],ou=[company],c=[country]</code>, so
	 * the values of these attributes must be extracted from the DN. For this,
	 * we use the DistinguishedName.
	 * 
	 *Mattias Hellborg Arthurssonellborg Arthursson
	 * @author Ulrik Sandberg
	 * 
	 * @modified tim jeffcoat, scheduleem
	 */
	private static class PersonContextMapper implements ContextMapper {

		public Object mapFromContext(Object ctx) {
			DirContextAdapter context = (DirContextAdapter) ctx;
			DistinguishedName dn = new DistinguishedName(context.getDn());
			Person person = new Person();
			person.setCountry(dn.getLdapRdn(0).getComponent().getValue());
			person.setCompany(dn.getLdapRdn(1).getComponent().getValue());
			person.setUid(context.getStringAttribute("uid"));
			person.setUsername(context.getStringAttribute("uid"));
			return person;
		}
	}

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	public void setLdaptreepathminusroot(String ldaptreepathminusroot) {
		this.ldaptreepathminusroot = ldaptreepathminusroot;
	}

	public String getLdaptreepathminusroot() {
		return ldaptreepathminusroot;
	}

	public void setLdapbase(String ldapbase) {
		this.ldapbase = ldapbase;
	}

	public String getLdapbase() {
		return ldapbase;
	}
}
