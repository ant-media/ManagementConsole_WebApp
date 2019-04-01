package com.antstreaming.console.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import io.antmedia.api.periscope.UserEndpoints;
import io.antmedia.console.datastore.MapDBStore;
import io.antmedia.console.rest.RestService;
import io.antmedia.datastore.db.types.Broadcast;
import io.antmedia.rest.model.Result;
import io.antmedia.rest.model.User;
import io.antmedia.settings.LogSettings;


public class RestServiceHttpTest {

	private static final String ROOT_URL = "http://localhost:5080/rest";
	
	private static final String RESET_URL = "http://localhost:5080/rest/changeLogLevel/ALL";
	
	private static final String ERROR_URL = "http://localhost:5080/rest/changeLogLevel/ERROR";
	
	private static final String WRONG_URL = "http://localhost:5080/rest/changeLogLevel/WRONGLEVEL";
	
	private static final String GET_LEVEL_URL = "http://localhost:5080/rest/getLogLevel/";
	
	LogSettings logSettings ;

	Gson gson = new Gson();
	
	RestService restService = new RestService();
	
	private MapDBStore dbStore = new MapDBStore();
	
	//before class
	//clear datastore
	//start server
	//add initial user
	

	@Before
	public void before() {
	}

	@After
	public void after() {
	}


	//test user e-mail: deneme@deneme.com
	//test user pass: 1234567

	@Test
	public void testChangeUserPassword() {
		try {
			
			//authenticate user
			
			String authURL = ROOT_URL + "/authenticateUser";
			CloseableHttpClient client0 = HttpClients.custom()
					.setRedirectStrategy(new LaxRedirectStrategy())
					.build();
			
			User user = new User();
			user.setEmail("deneme@deneme.com");  // user email is fetched from server session context
			user.setPassword("1234567");
			
			HttpUriRequest post = RequestBuilder.post()
					.setUri(authURL)
					.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
					.setEntity(new StringEntity(gson.toJson(user)))
					.build();
			
			HttpResponse response = client0.execute(post);

			StringBuffer result = readResponse(response);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new Exception(result.toString());
			}
			System.out.println("result string: " + result.toString());
			Result tmp = gson.fromJson(result.toString(), Result.class);
			
			assertTrue(tmp.isSuccess());
			
			
			//change password
			
			String url = ROOT_URL + "/changeUserPassword";
			CloseableHttpClient client = HttpClients.custom()
					.setRedirectStrategy(new LaxRedirectStrategy())
					.build();

			user = new User();
			user.setEmail("nope");  // user email is fetched from server session context
			user.setPassword("1234567");
			user.setNewPassword("7654321");


			 post = RequestBuilder.post()
					.setUri(url)
					.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
					.setEntity(new StringEntity(gson.toJson(user)))
					.build();
			
			 response = client.execute(post);

			 result = readResponse(response);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new Exception(result.toString());
			}
			System.out.println("result string: " + result.toString());
			Result tmp1 = gson.fromJson(result.toString(), Result.class);
			
			assertTrue(tmp1.isSuccess());
			
			
			//change password again
			user.setPassword("7654321");
			user.setNewPassword("1234567");
			
			post = RequestBuilder.post()
					.setUri(url)
					.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
					.setEntity(new StringEntity(gson.toJson(user)))
					.build();
			
			response = client.execute(post);

			result = readResponse(response);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new Exception(result.toString());
			}
			System.out.println("result string: " + result.toString());
			tmp1 = gson.fromJson(result.toString(), Result.class);
			
			assertTrue(tmp1.isSuccess());
			
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}


	}

	@Test
	public void testBroadcasGetApps() {
		try {
			/// get broadcast 
			String url = ROOT_URL + "/getApplications";

			CloseableHttpClient client = HttpClients.custom()
					.setRedirectStrategy(new LaxRedirectStrategy())
					.build();

			//Broadcast broadcast = null; //new Broadcast();
			//broadcast.name = "name";



			HttpUriRequest get = RequestBuilder.get()
					.setUri(url)
					.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
					//	.setEntity(new StringEntity(gson.toJson(broadcast)))
					.build();

			CloseableHttpResponse response = client.execute(get);

			StringBuffer result = readResponse(response);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new Exception(result.toString());
			}
			System.out.println("result string: " + result.toString());


		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testLogLevel() {
		
		
		try {
			
			// Reset Log Level 
			
			CloseableHttpClient resetLogLevelClient = HttpClients.custom()
					.setRedirectStrategy(new LaxRedirectStrategy())
					.build();


			HttpUriRequest resetLevelRequest = RequestBuilder.get()
					.setUri(RESET_URL)
					.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
					.build();

			CloseableHttpResponse resetLevelResponse = resetLogLevelClient.execute(resetLevelRequest);
			
			StringBuffer resultResetLevel = readResponse(resetLevelResponse);
			
			System.out.println("resultGetLevel string: " + resultResetLevel.toString());
			
			// Check Reset Log Level 
			
			CloseableHttpClient getLevelClient = HttpClients.custom()
					.setRedirectStrategy(new LaxRedirectStrategy())
					.build();


			HttpUriRequest getLogLevelRequest = RequestBuilder.get()
					.setUri(GET_LEVEL_URL)
					.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
					.build();

			CloseableHttpResponse getLevelCloseResponse = getLevelClient.execute(getLogLevelRequest);
			
			StringBuffer resultGetLevel = readResponse(getLevelCloseResponse);
			
			System.out.println("resultGetLevel string: " + resultGetLevel.toString());
			
			assertEquals("{\"logLevel\":\"ALL\"}", resultGetLevel.toString());
			
			
			// changeLogLevel ALL -> ERROR
			
			CloseableHttpClient changeLevelClient = HttpClients.custom()
					.setRedirectStrategy(new LaxRedirectStrategy())
					.build();


			HttpUriRequest getChangeLevelRequest = RequestBuilder.get()
					.setUri(ERROR_URL)
					.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
					.build();

			CloseableHttpResponse resultChangeLevelResponse = changeLevelClient.execute(getChangeLevelRequest);
			
			StringBuffer resultChangeLevel = readResponse(resultChangeLevelResponse);
			
			System.out.println("resultChangeLevel string: " + resultChangeLevel.toString());
			
			//getLogLevel
			
			CloseableHttpClient getLevelClient2 = HttpClients.custom()
					.setRedirectStrategy(new LaxRedirectStrategy())
					.build();


			HttpUriRequest getLogLevelRequest2 = RequestBuilder.get()
					.setUri(GET_LEVEL_URL)
					.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
					.build();

			CloseableHttpResponse getLevelCloseResponse2 = getLevelClient2.execute(getLogLevelRequest2);
			
			StringBuffer resultGetLevel2 = readResponse(getLevelCloseResponse2);
			
			System.out.println("resultGetLevel string: " + resultGetLevel2.toString());
			
			// test Log Check
			
			assertEquals("{\"logLevel\":\"ERROR\"}", resultGetLevel2.toString());
			
			
			// input wrong Log Level
			
			CloseableHttpClient wrongLevelClient = HttpClients.custom()
					.setRedirectStrategy(new LaxRedirectStrategy())
					.build();


			HttpUriRequest getWrongLevelRequest = RequestBuilder.get()
					.setUri(WRONG_URL)
					.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
					.build();

			CloseableHttpResponse resultWrongLevelResponse = wrongLevelClient.execute(getWrongLevelRequest);
			
			StringBuffer resultWrongLevel = readResponse(resultWrongLevelResponse);
			
			System.out.println("resultChangeLevel string: " + resultWrongLevel.toString());
			
			//getLogLevel
			
			CloseableHttpClient getLevelClient3 = HttpClients.custom()
					.setRedirectStrategy(new LaxRedirectStrategy())
					.build();


			HttpUriRequest getLogLevelRequest3 = RequestBuilder.get()
					.setUri(GET_LEVEL_URL)
					.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
					.build();

			CloseableHttpResponse getLevelCloseResponse3 = getLevelClient3.execute(getLogLevelRequest3);
			
			StringBuffer resultGetLevel3 = readResponse(getLevelCloseResponse3);
			
			System.out.println("resultGetLevel string: " + resultGetLevel3.toString());
			
			// test Log Check
			
			assertEquals("{\"logLevel\":\"ERROR\"}", resultGetLevel3.toString());
			

		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}

	protected StringBuffer readResponse(HttpResponse response) throws IOException {
		BufferedReader rd = new BufferedReader(
				new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		return result;
	}

	
	











}
