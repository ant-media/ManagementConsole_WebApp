package com.antstreaming.console.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;



import io.antmedia.console.datastore.MapDBStore;
import io.antmedia.console.rest.RestService;
import io.antmedia.rest.model.Result;
import io.antmedia.rest.model.User;
import io.antmedia.rest.model.UserType;

public class RestServiceTest {

	private RestService restService;
	private MapDBStore dbStore;

	private static final int OFFSET_NOT_USED = -1;
	private static final int MAX_OFFSET_SIZE = 10000000;
	private static final int MAX_CHAR_SIZE = 512000;
	private static final int MIN_CHAR_SIZE = 10;
	private static final int MIN_OFFSET_SIZE = 10;
	private static final String LOG_CONTENT = "logContent";
	private static final String LOG_SIZE = "logSize";
	private static final String LOG_CONTENT_RANGE = "logContentRange";
	private static final float MEGABYTE = 1024f * 1024f;
	private static final String MB_STRING = "%.2f MB";
	private static final int logHeaderSize = 11;
	private static final String fileNonExistError = "{\"logs\":\""+"There are no registered logs yet\"}";
	private static final String manyCharError = "{\"logs\":\""+"There are no many Chars in File\"}";
	private static final String LOG_TYPE_TEST = "test";
	private static final String TEST_LOG_LOCATION = "target/test-classes/ant-media-server.log";
	private static final String CREATED_FILE_TEXT = "2019-04-24 19:01:24,291 [main] INFO  org.red5.server.Launcher - Ant Media Server Enterprise 1.7.0-SNAPSHOT\n" + 
			"2019-04-24 19:01:24,334 [main] INFO  o.s.c.s.FileSystemXmlApplicationContext - Refreshing org.springframework.context.support.FileSystemXmlApplicationContext@f0f2775: startup date [Wed Apr 24 19:01:24 EET 2019]; root of context hierarchy";
	private static final String fileText = "{\"logs\":\""+"Test Log File String Values Lorem Ipsum Dolor Sit Amet\"}";


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
		dbStore = new MapDBStore();
		restService.setDataStore(dbStore);
	}

	@After
	public void after() {
		// dbStore.clear();
		// dbStore.close();

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
	public void testAddUser() {
		Integer userType = 0;
		String password = "password";
		String userName = "username" + (int) (Math.random() * 1000000000);
		User user = new User(userName, password, UserType.ADMIN);
		Result result = restService.addUser(user);

		// System.out.println("error id: " + result.errorId);
		assertTrue(result.isSuccess());

		user = new User(userName, "second pass", UserType.ADMIN);

		user.setPassword("second pass");
		user.setUserType(UserType.ADMIN);
		result = restService.addUser(user);

		assertFalse(result.isSuccess());

		user.setEmail("ksks" + (int) (Math.random() * 100000));
		user.setPassword("second pass");
		user.setUserType(UserType.ADMIN);
		result = restService.addUser(user);
		// should pass because user type is not important right now
		assertTrue(result.isSuccess());

	}

	private volatile boolean err;

	@Test
	public void testMultipleThreads() {

		Thread thread = null;
		err = false;
		for (int i = 0; i < 10; i++) {
			thread = new Thread() {
				public void run() {

					for (int i = 0; i < 20; i++) {
						try {
							testAddUser();
						} catch (Exception e) {
							e.printStackTrace();
							System.err.println("error--------");
							// fail(e.getMessage());
							err = true;
						} catch (AssertionError error) {
							error.printStackTrace();
							System.err.println("assertion error: " + error);
							// fail(error.getMessage());
							err = true;

						}
					}

				};
			};
			thread.start();
		}

		try {
			/*
			 * while (thread.isAlive()) { Thread.sleep(1000); }
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
		String userName = "username" + (int) (Math.random() * 100000);
		User user = new User(userName, password, UserType.ADMIN);

		Result result = restService.addInitialUser(user);
		assertTrue(result.isSuccess());
		assertEquals(password, dbStore.getUser(userName).getPassword());
		assertEquals((int) userType, dbStore.getUser(userName).getUserType());

		// TODO: open below test

		user.setNewPassword("password2");
		Result result2 = restService.changeUserPasswordInternal(userName, user);
		assertTrue(result2.isSuccess());

		assertEquals(user.getNewPassword(), dbStore.getUser(userName).getPassword());
		// assertEquals((int)userType2, dbStore.getUser(userName).userType);

		user.setPassword(user.getNewPassword());
		user.setNewPassword("12345");
		result2 = restService.changeUserPasswordInternal(userName, user);
		assertTrue(result2.isSuccess());

		assertEquals(user.getNewPassword(), dbStore.getUser(userName).getPassword());

		user.setPassword(user.getNewPassword());
		user.setNewPassword("12345678");
		result2 = restService.changeUserPasswordInternal(userName, user);
		assertTrue(result2.isSuccess());

		assertEquals(user.getNewPassword(), dbStore.getUser(userName).getPassword());

		/*
		 * result = restService.editUser("notexist", password2, userType2);
		 * assertFalse(result.isSuccess());
		 * 
		 * 
		 * result = restService.editUser(userName, password2, 3); //should fail
		 * because user type is 3, it should 0 or 1
		 * assertFalse(result.isSuccess());
		 */

	}

	@Test
	public void testDeleteUser() {
		Integer userType = 0;
		String password = "password";
		String userName = "username" + (int) (Math.random() * 100000);
		User user = new User(userName, password, UserType.ADMIN);
		Result result = restService.addUser(user);
		assertTrue(result.isSuccess());
		assertNotNull(dbStore.getUser(userName));
		// TODO: open below test
		/*
		 * result = restService.deleteUser(userName);
		 * assertTrue(result.isSuccess());
		 * assertNull(dbStore.getUser(userName));
		 */
	}

	@Test
	public void testAuthenticateUser() {
		Integer userType = 1;
		String password = "password";
		String userName = "username" + (int) (Math.random() * 100000);
		User user = new User(userName, password, UserType.ADMIN);
		System.out.println("username: " + userName);
		Result result = restService.addUser(user);
		assertTrue(result.isSuccess());
		assertNotNull(dbStore.getUser(userName));
		// TODO: open below test
		/*
		 * result = restService.authenticateUser(userName, password);
		 * assertTrue(result.isSuccess());
		 * 
		 * result = restService.authenticateUser("nope", password);
		 * assertFalse(result.isSuccess());
		 * 
		 * result = restService.authenticateUser(userName, "nope");
		 * assertFalse(result.isSuccess());
		 */
	}


	@Test
	public void testLogFiles() throws IOException, ParseException {
		
		JSONParser parser = new JSONParser();

		Object resultObject ;
		JSONObject jsonObject;

		//Create Log Files

		writeUsingFiles(CREATED_FILE_TEXT);

		//Check Log File Create

		File file = new File(TEST_LOG_LOCATION);

		assertTrue(file.isFile());

		//Testing offset value = 0 scenarios

		// 1. option charSize > file.lenght() 

		resultObject = parser.parse(restService.getLogFile(MAX_CHAR_SIZE, LOG_TYPE_TEST ,OFFSET_NOT_USED));

		jsonObject = (JSONObject) resultObject;

		assertEquals("0,00 MB - "+ String.format(MB_STRING, ((file.length()) / MEGABYTE)), jsonObject.get(LOG_CONTENT_RANGE));

		assertEquals(CREATED_FILE_TEXT, jsonObject.get(LOG_CONTENT));

		assertEquals(String.format(MB_STRING, (file.length()) / MEGABYTE), jsonObject.get(LOG_SIZE));

		// 2. option charSize < file.lenght() 

		resultObject = parser.parse(restService.getLogFile(MIN_CHAR_SIZE, LOG_TYPE_TEST ,OFFSET_NOT_USED));

		jsonObject = (JSONObject) resultObject;

		assertEquals(String.format(MB_STRING, (file.length() - MIN_CHAR_SIZE) / MEGABYTE)
				+ " - " + String.format(MB_STRING, ((file.length()) / MEGABYTE)), jsonObject.get(LOG_CONTENT_RANGE));

		assertEquals(CREATED_FILE_TEXT.substring(CREATED_FILE_TEXT.length()-MIN_CHAR_SIZE), jsonObject.get(LOG_CONTENT));

		assertEquals(String.format(MB_STRING, (file.length()) / MEGABYTE), jsonObject.get(LOG_SIZE));

		//Testing offset value != 0 scenarios

		// 1- Test offsetSize > file.lenght() 

		// 1-  a- charSize > file.lenght()

		resultObject = parser.parse(restService.getLogFile(MAX_CHAR_SIZE, LOG_TYPE_TEST ,MAX_OFFSET_SIZE));

		jsonObject = (JSONObject) resultObject;

		assertEquals("0,00 MB - " + String.format(MB_STRING, ((file.length()) / MEGABYTE)), jsonObject.get(LOG_CONTENT_RANGE));

		assertEquals(CREATED_FILE_TEXT, jsonObject.get(LOG_CONTENT));

		assertEquals(String.format(MB_STRING, (file.length()) / MEGABYTE), jsonObject.get(LOG_SIZE));

		// 1- b- charSize < file.lenght()

		resultObject = parser.parse(restService.getLogFile(MIN_CHAR_SIZE, LOG_TYPE_TEST ,MAX_OFFSET_SIZE));

		jsonObject = (JSONObject) resultObject;

		assertEquals("0,00 MB - " + String.format(MB_STRING, ((file.length()) / MEGABYTE)), jsonObject.get(LOG_CONTENT_RANGE));

		assertEquals(CREATED_FILE_TEXT.substring(CREATED_FILE_TEXT.length()-10), jsonObject.get(LOG_CONTENT));

		assertEquals(String.format(MB_STRING, (file.length()) / MEGABYTE), jsonObject.get(LOG_SIZE));

		// 2- Test offsetSize < file.lenght() 

		// 2-  a- charSize > file.lenght()

		resultObject = parser.parse(restService.getLogFile(MAX_CHAR_SIZE, LOG_TYPE_TEST ,MIN_OFFSET_SIZE));

		jsonObject = (JSONObject) resultObject;

		assertEquals(String.format(MB_STRING, (MIN_OFFSET_SIZE) / MEGABYTE) + " - "
				+ String.format(MB_STRING, ((MIN_OFFSET_SIZE + MAX_CHAR_SIZE) / MEGABYTE)), jsonObject.get(LOG_CONTENT_RANGE));

		assertEquals(CREATED_FILE_TEXT.substring(10), jsonObject.get(LOG_CONTENT));

		assertEquals(String.format(MB_STRING, (file.length()) / MEGABYTE), jsonObject.get(LOG_SIZE));

		// 2-  b- charSize < file.lenght()
		
		resultObject = parser.parse(restService.getLogFile(MIN_CHAR_SIZE, LOG_TYPE_TEST ,MIN_OFFSET_SIZE));
		
		jsonObject = (JSONObject) resultObject;

		assertEquals(String.format(MB_STRING, (MIN_OFFSET_SIZE) / MEGABYTE) + " - "
				+ String.format(MB_STRING, ((MIN_OFFSET_SIZE + MIN_CHAR_SIZE) / MEGABYTE)), jsonObject.get(LOG_CONTENT_RANGE));

		assertEquals(CREATED_FILE_TEXT.substring(10), jsonObject.get(LOG_CONTENT));

		assertEquals(String.format(MB_STRING, (file.length()) / MEGABYTE), jsonObject.get(LOG_SIZE));


	}

	private static void writeUsingFiles(String data) {
		try {
			Files.write(Paths.get(TEST_LOG_LOCATION), data.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
