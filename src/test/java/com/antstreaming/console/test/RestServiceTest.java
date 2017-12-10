package com.antstreaming.console.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.antmedia.console.DataStore;
import io.antmedia.console.User;
import io.antmedia.console.rest.RestService;
import io.antmedia.console.rest.RestService.OperationResult;
import io.antmedia.rest.BroadcastRestService.Result;

public class RestServiceTest {
	
	private RestService restService;
	private DataStore dbStore;
	
	@Before
	public void before() {
		restService = new RestService();
		dbStore = new DataStore();
		restService.setDataStore(dbStore);
	}
	
	@After
	public void after() {
		dbStore.clear();
		dbStore.close();
	}
	
	@Test
	public void testAddUser(){
		Integer userType = 0;
		String password = "password";
		String userName = "username";
		User user = new User(userName, password, userType);
		OperationResult result = restService.addUser(user);
		assertTrue(result.isSuccess());
		
		user.password ="second pass";
		user.userType = 1;
		result = restService.addUser(user);
		assertFalse(result.isSuccess());
		
		user.email = "ksks";
		user.password ="second pass";
		user.userType = 6;
		result = restService.addUser(user);
		//should fail because user type is 6
		assertFalse(result.isSuccess());
		
		
		
	}
	
	
	@Test
	public void testEditUser() {
		
		Integer userType = 1;
		String password = "password";
		String userName = "username";
		User user = new User(userName, password, userType);
		OperationResult result = restService.addInitialUser(user);
		assertTrue(result.isSuccess());
		assertEquals(password, dbStore.getUser(userName).password);
		assertEquals((int)userType, dbStore.getUser(userName).userType);
		
		//TODO: open below test
		
		
		user.newPassword = "password2";
		Result result2 = restService.changeUserPasswordInternal(userName, user);
		assertTrue(result2.success);
		
		assertEquals(user.newPassword, dbStore.getUser(userName).password);
		//assertEquals((int)userType2, dbStore.getUser(userName).userType);
		
		user.password = user.newPassword;
		user.newPassword = "12345";
		result2 = restService.changeUserPasswordInternal(userName, user);
		assertTrue(result2.success);
		
		assertEquals(user.newPassword, dbStore.getUser(userName).password);
		
		
		user.password = user.newPassword;
		user.newPassword = "12345678";
		result2 = restService.changeUserPasswordInternal(userName, user);
		assertTrue(result2.success);
		
		assertEquals(user.newPassword, dbStore.getUser(userName).password);
		
		/*
		result = restService.editUser("notexist", password2, userType2);
		assertFalse(result.isSuccess());
		
		
		result = restService.editUser(userName, password2, 3);
		//should fail because user type is 3, it should 0 or 1
		assertFalse(result.isSuccess());
		*/
		
		
	}
	
	
	@Test
	public void testDeleteUser() {
		Integer userType = 0;
		String password = "password";
		String userName = "username";
		User user = new User(userName, password, userType);
		OperationResult result = restService.addUser(user);
		assertTrue(result.isSuccess());
		assertNotNull(dbStore.getUser(userName));
		//TODO: open below test
		/*
		result = restService.deleteUser(userName);
		assertTrue(result.isSuccess());
		assertNull(dbStore.getUser(userName));
		*/
	}
	
	
	@Test
	public void testAuthenticateUser() {
		Integer userType = 1;
		String password = "password";
		String userName = "username";
		User user = new User(userName, password, userType);
		OperationResult result = restService.addUser(user);
		assertTrue(result.isSuccess());
		assertNotNull(dbStore.getUser(userName));
		//TODO: open below test
		/*
		result = restService.authenticateUser(userName, password);
		assertTrue(result.isSuccess());
		
		result = restService.authenticateUser("nope", password);
		assertFalse(result.isSuccess());
		
		result = restService.authenticateUser(userName, "nope");
		assertFalse(result.isSuccess());
		*/
	}
	
	
	
	
	
	

}
