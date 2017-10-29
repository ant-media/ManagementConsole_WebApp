package com.antstreaming.console;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DataStore {

	public final static String SERVER_STORAGE_FILE = "server.db";
	public final static String SERVER_STORAGE_MAP_NAME = "serverdb";

	private DB db;
	private HTreeMap<String, String> map;
	private Gson gson;

	public DataStore() {
		db = DBMaker.fileDB(SERVER_STORAGE_FILE).fileMmapEnableIfSupported().closeOnJvmShutdown().make();
		map = db.hashMap(SERVER_STORAGE_MAP_NAME)
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
				if (!map.containsKey(username)) 
				{
					User user = new User(username, password, userType);
					map.put(username, gson.toJson(user));
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

	public boolean editUser(String username, String password, Integer userType) {
		boolean result = false;
		if (username != null && password != null && userType != null && (userType == 0 || userType == 1))  {
			try {
				if (map.containsKey(username)) {
					User user = new User(username, password, userType);
					map.put(username, gson.toJson(user));
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
				if (map.containsKey(username)) {
					map.remove(username);
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
		return map.containsKey(username);
	}

	public boolean doesUserExist(String username, String password) {
		boolean result = false;
		if (username != null && password != null) {
			try {
				if (map.containsKey(username)) {
					String value = map.get(username);
					User user = gson.fromJson(value, User.class);
					if (user.password.equals(password)) {
						result = true;
					}
				}
			}
			catch (Exception e) {
				result = false;
			}
		}
		return result;
	}

	public User getUser(String username) 
	{
		if (username != null)  {
			try {
				if (map.containsKey(username)) {
					String value = map.get(username);
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
		map.clear();
		db.commit();
	}

	public void close() {
		db.close();
	}
	
	public int getNumberOfRecords() {
		return map.size();
	}

}
