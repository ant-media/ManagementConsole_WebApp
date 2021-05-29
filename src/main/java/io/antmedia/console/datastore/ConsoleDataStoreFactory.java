package io.antmedia.console.datastore;

import org.springframework.beans.factory.annotation.Value;

import io.antmedia.AppSettings;
import io.antmedia.datastore.db.IDataStoreFactory;

import java.util.HashMap;
import java.util.Map;

public class ConsoleDataStoreFactory  {

	private IConsoleDataStore dataStore;
	
	@Value( "${"+AppSettings.SETTINGS_DB_APP_NAME+":#{null}}" )
	private String appName;
	
	@Value( "${"+io.antmedia.datastore.db.DataStoreFactory.SETTINGS_DB_NAME+":#{null}}" )
	private String dbName;
	
	@Value( "${"+io.antmedia.datastore.db.DataStoreFactory.SETTINGS_DB_TYPE+":#{null}}" )
	private String dbType;
	
	@Value( "${"+io.antmedia.datastore.db.DataStoreFactory.SETTINGS_DB_HOST+":#{null}}" )
	private String dbHost;
	
	@Value( "${"+io.antmedia.datastore.db.DataStoreFactory.SETTINGS_DB_USER+":#{null}}" )
	private String dbUser;
	
	@Value( "${"+io.antmedia.datastore.db.DataStoreFactory.SETTINGS_DB_PASS+":#{null}}" )
	private String dbPassword;
	
	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public String getDbHost() {
		return dbHost;
	}

	public void setDbHost(String dbHost) {
		this.dbHost = dbHost;
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public IConsoleDataStore getDataStore() {
		if (dataStore == null) {
			if(dbType.contentEquals("mongodb"))
			{
				
				dataStore = new MongoStore(dbHost, dbUser, dbPassword);
			}
			else if(dbType.contentEquals("mapdb"))
			{
				dataStore = new MapDBStore();
			}
		}
		return dataStore;
	}

	public Map<String, Integer> invalidLoginCountMap = new HashMap<String, Integer>();

	public Map<String, Long> blockTimeMap = new HashMap<String, Long>();

	public Map<String, Boolean> isBlockedMap = new HashMap<String, Boolean>();


	public long getBlockTime(String usermail) {
		return blockTimeMap.get(usermail);
	}
	public int getInvalidLoginCount(String usermail){
		return invalidLoginCountMap.get(usermail);
	}
	public boolean isUserBlocked(String usermail){
		return isBlockedMap.get(usermail);
	}

	public void setBlockTime(String usermail, long blockTime){
		blockTimeMap.put(usermail,blockTime);
	}
	public void incrementInvalidLoginCount(String usermail){
		invalidLoginCountMap.put(usermail, getInvalidLoginCount(usermail)+1);
	}

	public void resetInvalidLoginCount(String usermail){
		invalidLoginCountMap.put(usermail, 0);
	}
	public void setBlocked(String usermail){
		isBlockedMap.put(usermail,true);
	}
	public void setUnBlocked(String usermail){
		isBlockedMap.put(usermail,false);
	}

}
	
