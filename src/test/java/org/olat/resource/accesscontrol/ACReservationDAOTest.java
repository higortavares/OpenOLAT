/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.resource.accesscontrol;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.resource.accesscontrol.model.ResourceReservation;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ACReservationDAOTest extends OlatTestCase  {
	
	private final OLog log = Tracing.createLoggerFor(ACReservationDAOTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ACReservationDAO acReservationDao;

	@After
	public void tearDown() throws Exception {
		try {
			DBFactory.getInstance().commitAndCloseSession();
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
			throw e;
		}
	}
	
	@Test
	public void should_service_present() {
		Assert.assertNotNull(acReservationDao);
	}
	
	@Test
	public void testCreateReservation() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("reserv-1-" + UUID.randomUUID().toString());
		OLATResource resource = JunitTestHelper.createRandomResource();
		dbInstance.commitAndCloseSession();

		ResourceReservation reservation = acReservationDao.createReservation(id, resource);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reservation);
		Assert.assertNotNull(reservation.getKey());
		Assert.assertNotNull(reservation.getCreationDate());
		Assert.assertNotNull(reservation.getLastModified());
		Assert.assertEquals(id, reservation.getIdentity());
		Assert.assertEquals(resource, reservation.getResource());
	}
	
	@Test
	public void testLoadReservation() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("reserv-1-" + UUID.randomUUID().toString());
		OLATResource resource = JunitTestHelper.createRandomResource();
		dbInstance.commitAndCloseSession();

		ResourceReservation reservation = acReservationDao.createReservation(id, resource);
		dbInstance.commitAndCloseSession();
		
		//check by load
		ResourceReservation loadedReservation = acReservationDao.loadReservation(id, resource);
		
		Assert.assertNotNull(loadedReservation);
		Assert.assertNotNull(loadedReservation.getKey());
		Assert.assertNotNull(loadedReservation.getCreationDate());
		Assert.assertNotNull(loadedReservation.getLastModified());
		Assert.assertEquals(id, loadedReservation.getIdentity());
		Assert.assertEquals(resource, loadedReservation.getResource());
		Assert.assertEquals(reservation, loadedReservation);
	}
	
	@Test
	public void testLoadOldReservation() throws Exception {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("reserv-1-" + UUID.randomUUID().toString());
		OLATResource resource = JunitTestHelper.createRandomResource();
		dbInstance.commitAndCloseSession();

		ResourceReservation reservation = acReservationDao.createReservation(id, resource);
		dbInstance.commitAndCloseSession();
		
		Thread.sleep(5000);
		
		//check by load
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, -2);
		Date date = cal.getTime();
		List<ResourceReservation> oldReservations = acReservationDao.loadReservationOlderThan(date);
		Assert.assertNotNull(oldReservations);
		Assert.assertFalse(oldReservations.isEmpty());
		Assert.assertTrue(oldReservations.contains(reservation));
	}
	
	@Test
	public void testCountReservation() {
		//create 3 identities and 3 reservations
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("reserv-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("reserv-2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("reserv-3-" + UUID.randomUUID().toString());
		OLATResource resource = JunitTestHelper.createRandomResource();
		dbInstance.commitAndCloseSession();

		ResourceReservation reservation1 = acReservationDao.createReservation(id1, resource);
		Assert.assertNotNull(reservation1);
		ResourceReservation reservation2 = acReservationDao.createReservation(id2, resource);
		Assert.assertNotNull(reservation2);
		ResourceReservation reservation3 = acReservationDao.createReservation(id3, resource);
		Assert.assertNotNull(reservation3);
		dbInstance.commitAndCloseSession();
		
		//count reservations
		int count = acReservationDao.countReservations(resource);
		Assert.assertEquals(3, count);
	}
	
	@Test
	public void testDeleteReservation() {
		//create 3 identities and 3 reservations
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("reserv-4-" + UUID.randomUUID().toString());
		OLATResource resource = JunitTestHelper.createRandomResource();
		ResourceReservation reservation = acReservationDao.createReservation(id, resource);
		dbInstance.commitAndCloseSession();
		
		//count reservations
		int count = acReservationDao.countReservations(resource);
		Assert.assertEquals(1, count);
		
		//delete reservation
		acReservationDao.deleteReservation(reservation);
		dbInstance.commitAndCloseSession();
		
		//recount
		int countAfter = acReservationDao.countReservations(resource);
		Assert.assertEquals(0, countAfter);
		ResourceReservation deletedReservation = acReservationDao.loadReservation(reservation.getKey());
		Assert.assertNull(deletedReservation);
	}
	

}
