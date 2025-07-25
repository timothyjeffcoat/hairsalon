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

import java.util.List;

/**
 * Data Access Object interface for the Person entity.
 * 
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
public interface PersonDao {
   void create(Person person);
   
   boolean createCustomer(Person person);
   
   boolean createSystemAccount(Person person);
   
   boolean createDirectAccount(Person person);
   
   boolean update(Person person);

   void delete(Person person);

   List getAllPersonNames();

   boolean uidExists(String uid);
   
   Person findByUid(Person uid);
   
   List findAll();

   Person findByPrimaryKey(String uid);
}
