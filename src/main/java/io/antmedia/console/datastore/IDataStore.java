package io.antmedia.console.datastore;

import io.antmedia.rest.model.User;
import io.antmedia.rest.model.UserType;


public interface IDataStore {
	
	public static final String SERVER_STORAGE_FILE = "server.db";
	public static final String SERVER_STORAGE_MAP_NAME = "serverdb";

	public boolean addUser(String username, String password, UserType userType);

	public boolean editUser(String username, String password, UserType userType);

	public boolean deleteUser(String username);
	
	public boolean doesUsernameExist(String username);

	public boolean doesUserExist(String username, String password);

	public User getUser(String username);

	public void clear();

	public void close();
	
	public int getNumberOfUserRecords();
}