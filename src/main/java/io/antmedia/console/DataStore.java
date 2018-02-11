package io.antmedia.console;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.antmedia.rest.model.User;


public class DataStore {

	public final static String SERVER_STORAGE_FILE = "server.db";
	public final static String SERVER_STORAGE_MAP_NAME = "serverdb";

	private DB db;
	private HTreeMap<String, String> userMap;
	private Gson gson;
	
	protected static Logger logger = LoggerFactory.getLogger(DataStore.class);

	public DataStore() {
		db = DBMaker.fileDB(SERVER_STORAGE_FILE).transactionEnable().make();
		userMap = db.hashMap(SERVER_STORAGE_MAP_NAME)
				.keySerializer(Serializer.STRING)
				.valueSerializer(Serializer.STRING)
				.counterEnable()
				.createOrOpen();
		gson = new Gson();

	}


	public boolean addUser(String username, String password, Integer userType) {

		boolean result = false;
		if (username != null && password != null && userType != null && (userType == 0  || userType == 1)) {
			try {
				if (!userMap.containsKey(username)) 
				{
					User user = new User(username, password, userType);
					userMap.put(username, gson.toJson(user));
					db.commit();
					result = true;
				}
				else {
					logger.warn("user with " + username + " already exist");
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				result = false;
			}
		}
		return result;
	}

	public boolean editUser(String username, String password, Integer userType) {
		boolean result = false;
		if (username != null && password != null && userType != null && (userType == 0 || userType == 1))  {
			try {
				if (userMap.containsKey(username)) {
					User user = new User(username, password, userType);
					userMap.put(username, gson.toJson(user));
					db.commit();
					result = true;
				}
			}
			catch (Exception e) {
				result = false;
			}
		}
		return result;
	}


	public boolean deleteUser(String username) {
		boolean result = false;
		if (username != null) {
			try {
				if (userMap.containsKey(username)) {
					userMap.remove(username);
					db.commit();
					result = true;
				}
			}
			catch (Exception e) {
				result = false;
			}
		}
		return result;
	}
	
	public boolean doesUsernameExist(String username) {
		return userMap.containsKey(username);
	}

	public boolean doesUserExist(String username, String password) {
		boolean result = false;
		if (username != null && password != null) {
			try {
				if (userMap.containsKey(username)) {
					String value = userMap.get(username);
					User user = gson.fromJson(value, User.class);
					if (user.password.equals(password)) {
						result = true;
					}
				}
			}
			catch (Exception e) {
				result = false;
				e.printStackTrace();
			}
		}
		return result;
	}

	public User getUser(String username) 
	{
		if (username != null)  {
			try {
				if (userMap.containsKey(username)) {
					String value = userMap.get(username);
					return gson.fromJson(value, User.class);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}


	public void clear() {
		userMap.clear();
		db.commit();
	}

	public void close() {
		db.close();
	}
	
	public int getNumberOfUserRecords() {
		return userMap.size();
	}

}
