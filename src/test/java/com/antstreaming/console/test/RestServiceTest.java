package com.antstreaming.console.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import io.antmedia.console.DataStore;
import io.antmedia.console.rest.RestService;
import io.antmedia.rest.model.Result;
import io.antmedia.rest.model.User;

public class RestServiceTest {

	private RestService restService;
	private DataStore dbStore;

	@Before
	public void before() {
		File f = new File("server.db");
		if (f.exists()) {
			try {
				Files.delete(f.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		restService = new RestService();
		dbStore = new DataStore();
		restService.setDataStore(dbStore);
	}

	@After
	public void after() {
		//dbStore.clear();
		//dbStore.close();
		
		File f = new File("server.db");
		if (f.exists()) {
			try {
				Files.delete(f.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testAddUser(){
		Integer userType = 0;
		String password = "password";
		String userName = "username" + (int)(Math.random() * 1000000000);
		User user = new User(userName, password, userType);
		Result result = restService.addUser(user);
		
		//System.out.println("error id: " + result.errorId);
		assertTrue(result.isSuccess());

		user = new User(userName, "second pass", userType);
	
		user.password ="second pass";
		user.userType = 1;
		result = restService.addUser(user);
		
		assertFalse(result.isSuccess());

		user.email = "ksks" + (int)(Math.random() * 100000);
		user.password ="second pass";
		user.userType = 6;
		result = restService.addUser(user);
		//should pass because user type is not important right now
		assertTrue(result.isSuccess());

	}
	
	
	
	private volatile boolean err;


	@Test
	public void testMultipleThreads() {

		Thread thread = null;
		err = false;
		for(int i=0; i < 10; i++) {
			thread = new Thread() {
				public void run() {
					
					for (int i = 0; i < 20; i++) {
						try {
							testAddUser();
						}
						catch (Exception e) {
							e.printStackTrace();
							System.err.println("error--------");
							//fail(e.getMessage());
							err = true;
						}
						catch (AssertionError error) {
							error.printStackTrace();
							System.err.println("assertion error: " + error);
							//fail(error.getMessage());
							err = true;
							
						}
					}
					
					

				};
			};
			thread.start();
		}

		try {
			/*
			while (thread.isAlive()) {
				Thread.sleep(1000);
			}
			*/
			thread.join();
			assertFalse(err);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testEditUser() {

		Integer userType = 1;
		String password = "password";
		String userName = "username" + (int)(Math.random() * 100000);
		User user = new User(userName, password, userType);
		
		Result result = restService.addInitialUser(user);
		assertTrue(result.isSuccess());
		assertEquals(password, dbStore.getUser(userName).password);
		assertEquals((int)userType, dbStore.getUser(userName).userType);

		//TODO: open below test


		user.newPassword = "password2";
		Result result2 = restService.changeUserPasswordInternal(userName, user);
		assertTrue(result2.isSuccess());

		assertEquals(user.newPassword, dbStore.getUser(userName).password);
		//assertEquals((int)userType2, dbStore.getUser(userName).userType);

		user.password = user.newPassword;
		user.newPassword = "12345";
		result2 = restService.changeUserPasswordInternal(userName, user);
		assertTrue(result2.isSuccess());

		assertEquals(user.newPassword, dbStore.getUser(userName).password);


		user.password = user.newPassword;
		user.newPassword = "12345678";
		result2 = restService.changeUserPasswordInternal(userName, user);
		assertTrue(result2.isSuccess());

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
		String userName = "username" + (int)(Math.random() * 100000);
		User user = new User(userName, password, userType);
		Result result = restService.addUser(user);
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
		String userName = "username" + (int)(Math.random() * 100000);
		User user = new User(userName, password, userType);
		System.out.println("username: " + userName);
		Result result = restService.addUser(user);
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
