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
import io.antmedia.console.DataStore;
import io.antmedia.console.rest.RestService;
import io.antmedia.datastore.db.types.Broadcast;
import io.antmedia.rest.model.Result;
import io.antmedia.rest.model.User;


public class RestServiceHttpTest {

	private static final String ROOT_URL = "http://localhost:5080/ConsoleApp/rest";

	Gson gson = new Gson();
	
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
			user.email = "deneme@deneme.com";  // user email is fetched from server session context
			user.password = "1234567";
			
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
			user.email = "nope";  // user email is fetched from server session context
			user.password = "1234567";
			user.newPassword = "7654321";


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
			user.password = "7654321";
			user.newPassword = "1234567";
			
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
