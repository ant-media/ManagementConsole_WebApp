package io.antmedia.console.rest;

import ch.qos.logback.classic.Level;
import com.google.gson.Gson;
import io.antmedia.AntMediaApplicationAdapter;
import io.antmedia.AppSettings;
import io.antmedia.console.AdminApplication;
import io.antmedia.console.datastore.DataStoreFactory;
import io.antmedia.console.datastore.IDataStore;
import io.antmedia.datastore.db.types.Licence;
import io.antmedia.licence.ILicenceService;
import io.antmedia.rest.model.Result;
import io.antmedia.rest.model.User;
import io.antmedia.settings.LogSettings;
import io.antmedia.settings.ServerSettings;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
@Path("/")
public class RestServicev2 extends CommonRestService {

	private static final String LOG_TYPE_ERROR = "error";

	private static final String LOG_TYPE_SERVER = "server";

	private static final String FILE_NOT_EXIST = "There is no log yet";

	private static final String ERROR_LOG_LOCATION = "log/antmedia-error.log";

	private static final String SERVER_LOG_LOCATION = "log/ant-media-server.log";

	private static final String LOG_CONTENT = "logContent";

	private static final String LOG_CONTENT_SIZE = "logContentSize";

	private static final String LOG_FILE_SIZE = "logFileSize";

	private static final int MAX_CHAR_SIZE = 512000;

	private static final String LOG_LEVEL_ALL = "ALL";

	private static final String LOG_LEVEL_TRACE = "TRACE";

	private static final String LOG_LEVEL_DEBUG = "DEBUG";

	private static final String LOG_LEVEL_INFO = "INFO";

	private static final String LOG_LEVEL_WARN = "WARN";

	private static final String LOG_LEVEL_ERROR = "ERROR";

	private static final String LOG_LEVEL_OFF = "OFF";

	private static final String USER_PASSWORD = "user.password";

	private static final String USER_EMAIL = "user.email";

	public static final String IS_AUTHENTICATED = "isAuthenticated";

	public static final String SERVER_NAME = "server.name";

	public static final String LICENSE_KEY = "server.licence_key";

	public static final String MARKET_BUILD = "server.market_build";
	
	public static final String NODE_GROUP = "nodeGroup";

	Gson gson = new Gson();

	private IDataStore dataStore;

	private static final String LOG_LEVEL = "logLevel";

	private static final String RED5_PROPERTIES_PATH = "conf/red5.properties";

	protected static final Logger logger = LoggerFactory.getLogger(RestServicev2.class);

	private static final String SOFTWARE_VERSION = "softwareVersion";

	protected ApplicationContext applicationContext;

	@Context
	private ServletContext servletContext;

	@Context
	private HttpServletRequest servletRequest;

	private DataStoreFactory dataStoreFactory;
	private ServerSettings serverSettings;

	private ILicenceService licenceService;




	/**
	 * Add user account on db. 
	 * Username must be unique,
	 * if there is a user with the same name, user will not be created
	 * 
	 * userType = 0 means ready only account
	 * userType = 1 means read-write account
	 * 
	 * Post method should be used.
	 * 
	 * application/json
	 * 
	 * form parameters - case sensitive
	 * "userName", "password", "userType
	 * 
	 * @param user: The user to be added
	 * @return JSON data
	 * if user is added success will be true
	 * if user is not added success will be false
	 * 	if user is not added, errorId = 1 means username already exist
	 */
	@POST
	@Path("/v2/users")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Result addUser(User user) {

		return super.addUser(user);
	}


	@POST
	@Path("/v2/users/initial")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Result addInitialUser(User user) {

		return super.addInitialUser(user);
	}

	@GET
	@Path("/v2/first-login-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Result isFirstLogin() 
	{

		return super.isFirstLogin();
	}

	/**
	 * Edit user account on db. 
	 * Username cannot be changed, password or userType can be changed
	 * userType = 0 means ready only account
	 * userType = 1 means read-write account
	 * 
	 * Post method should be used.
	 * 
	 * application/x-www-form-urlencoded
	 * 
	 * form parameters - case sensitive
	 * "userName", "password", "userType
	 * 
	 * @param userName
	 * @return JSON data
	 * if user is edited, success will be true
	 * if not, success will be false
	 * 	errorId = 2 means user does not exist
	 */
	/*
	@POST
	@Path("/editUser")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public OperationResult editUser(@FormParam("userName") String userName, @FormParam("password") String password, @FormParam("userType") Integer userType) {
		//TODO: check that request is coming from authorized user
		boolean result = false;
		int errorId = -1;
		if (userName != null && getDataStore().doesUsernameExist(userName)) {
			result = getDataStore().editUser(userName, password, userType);
		}
		else {
			errorId = 2;
		}

		OperationResult operationResult = new OperationResult(result);
		operationResult.setErrorId(errorId);
		return operationResult;
	}
	 */

	/**
	 * Deletes user account from db
	 * 
	 * Post method should be used.
	 * 
	 * application/x-www-form-urlencoded
	 * 
	 * form parameters - case sensitive
	 * "userName"
	 * 
	 * @param userName
	 * @return
	 */
	/*
	@POST
	@Path("/deleteUser")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public OperationResult deleteUser(@FormParam("userName") String userName) {
		//TODO: check that request is coming from authorized user
		boolean result = getDataStore().deleteUser(userName);
		return new OperationResult(result);
	}
	 */



	/**
	 * Authenticates user with userName and password
	 * 
	 * 
	 * @param user: The User object to be authenticated 
	 * @return json that shows user is authenticated or not
	 */
	@POST
	@Path("/v2/users/authenticate")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Result authenticateUser(User user) {

		return super.authenticateUser(user);
	}


	@POST
	@Path("/v2/users/password")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Result changeUserPassword(User user) {

		return super.changeUserPassword(user);

	}

	public Result changeUserPasswordInternal(String userMail, User user) {

		return super.changeUserPasswordInternal(userMail, user);
	}




	@GET
	@Path("/v2/authentication-status")
	@Produces(MediaType.APPLICATION_JSON)
	public Result isAuthenticatedRest(){
		return super.isAuthenticatedRest();
	}


	/*
	 * 	os.name						:Operating System Name
	 * 	os.arch						: x86/x64/...
	 * 	java.specification.version	: Java Version (Required 1.5 or 1.6 and higher to run Red5)
	 * 	-------------------------------
	 * 	Runtime.getRuntime()._____  (Java Virtual Machine Memory)
	 * 	===============================
	 * 	maxMemory()					: Maximum limitation
	 * 	totalMemory()				: Total can be used
	 * 	freeMemory()				: Availability
	 * 	totalMemory()-freeMemory()	: In Use
	 * 	availableProcessors()		: Total Processors available
	 * 	-------------------------------
	 *  getOperatingSystemMXBean()	(Actual Operating System RAM)
	 *	===============================
	 *  osCommittedVirtualMemory()	: Virtual Memory
	 *  osTotalPhysicalMemory()		: Total Physical Memory
	 *  osFreePhysicalMemory()		: Available Physical Memory
	 *  osInUsePhysicalMemory()		: In Use Physical Memory
	 *  osTotalSwapSpace()			: Total Swap Space
	 *  osFreeSwapSpace()			: Available Swap Space
	 *  osInUseSwapSpace()			: In Use Swap Space
	 *  -------------------------------
	 *  File						(Actual Harddrive Info: Supported for JRE 1.6)
	 *	===============================
	 *	osHDUsableSpace()			: Usable Space
	 *	osHDTotalSpace()			: Total Space
	 *	osHDFreeSpace()				: Available Space
	 *	osHDInUseSpace()			: In Use Space
	 **/


	@GET
	@Path("/v2/system-status")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSystemInfo() {
		return super.getSystemInfo();
	}


	/*
	 * 	Runtime.getRuntime()._____  (Java Virtual Machine Memory)
	 * 	===============================
	 * 	maxMemory()					: Maximum limitation
	 * 	totalMemory()				: Total can be used
	 * 	freeMemory()				: Availability
	 * 	totalMemory()-freeMemory()	: In Use
	 * 	availableProcessors()		: Total Processors available
	 */
	@GET
	@Path("/v2/jvm-memory-status")
	@Produces(MediaType.APPLICATION_JSON)
	public String getJVMMemoryInfo() {
		return super.getJVMMemoryInfo();
	}


	/*
	 *  osCommittedVirtualMemory()	: Virtual Memory
	 *  osTotalPhysicalMemory()		: Total Physical Memory
	 *  osFreePhysicalMemory()		: Available Physical Memory
	 *  osInUsePhysicalMemory()		: In Use Physical Memory
	 *  osTotalSwapSpace()			: Total Swap Space
	 *  osFreeSwapSpace()			: Available Swap Space
	 *  osInUseSwapSpace()			: In Use Swap Space
	 */
	@GET
	@Path("/v2/system-memory-status")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSystemMemoryInfo() {
		return super.getSystemMemoryInfo();
	}


	/*
	 *  File						(Actual Harddrive Info: Supported for JRE 1.6)
	 *	===============================
	 *	osHDUsableSpace()			: Usable Space
	 *	osHDTotalSpace()			: Total Space
	 *	osHDFreeSpace()				: Available Space
	 *	osHDInUseSpace()			: In Use Space
	 **/
	@GET
	@Path("/v2/file-system-status")
	@Produces(MediaType.APPLICATION_JSON)
	public String getFileSystemInfo() {
		return super.getFileSystemInfo();
	}

	/**
	 * getProcessCpuTime:  microseconds CPU time used by the process
	 * 
	 * getSystemCpuLoad:	"% recent cpu usage" for the whole system. 
	 * 
	 * getProcessCpuLoad: "% recent cpu usage" for the Java Virtual Machine process. 
	 * @return the CPU load info
	 */
	@GET
	@Path("/v2/cpu-status")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCPUInfo() {
		return super.getCPUInfo();
	}
	
	@GET
	@Path("/v2/thread-dump")
	@Produces(MediaType.TEXT_PLAIN)
	public String getThreadDump() {
		return super.getThreadDump();
	}
	
	@GET
	@Path("/v2/thread-dump")
	@Produces(MediaType.APPLICATION_JSON)
	public String getThreadDumpJSON() {
		return super.getThreadDumpJSON();
	}
	
	
	@GET
	@Path("/v2/threads")
	@Produces(MediaType.APPLICATION_JSON)
	public String getThreadsInfo() {
		return super.getThreadsInfo();
	}



	// method path was already Restful
	// v2 is added to prevent clashes with older RestService.java
	@GET
	@Path("/v2/heap-dump")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getHeapDump() {

		return super.getHeapDump();
	}
	
	

	/**
	 * Return server uptime and startime in milliseconds
	 * @return JSON object contains the server uptime and start time
	 */

	// method path was already Restful
	// v2 is added to prevent clashes with older RestService.java
	@GET
	@Path("/v2/server-time")
	@Produces(MediaType.APPLICATION_JSON)
	public String getServerTime() {
		return super.getServerTime();
	}

	@GET
	@Path("/v2/system-resources")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSystemResourcesInfo() {

		return super.getSystemResourcesInfo();
	}

	@GET
	@Path("/v2/gpu-status")
	@Produces(MediaType.APPLICATION_JSON) 
	public String getGPUInfo() 
	{
		return super.getGPUInfo();
	}


	@GET
	@Path("/v2/version")
	@Produces(MediaType.APPLICATION_JSON)
	public String getVersion() {
		return super.getVersion();
	}


	@GET
	@Path("/v2/applications")
	@Produces(MediaType.APPLICATION_JSON)
	public String getApplications() {

		return super.getApplications();
	}

	/**
	 * Refactor name getTotalLiveStreamSize
	 * only return totalLiveStreamSize
	 * @return the number of live clients
	 */
	@GET
	@Path("/v2/live-clients-size")
	@Produces(MediaType.APPLICATION_JSON)
	public String getLiveClientsSize() 
	{

		return super.getLiveClientsSize();
	}

	@GET
	@Path("/v2/applications-info")
	@Produces(MediaType.APPLICATION_JSON)
	public String getApplicationInfo() {

		return super.getApplicationInfo();
	}

	/**
	 * Refactor remove this function and use ProxyServlet to get this info
	 * Before deleting check web panel does not use it
	 * @param name: application name 
	 * @return live streams in the application
	 */
	@GET
	@Path("/v2/applications/live-streams/{appname}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAppLiveStreams(@PathParam("appname") String name) {

		return super.getAppLiveStreams(name);
	}


	/**
	 * Refactor remove this function and use ProxyServlet to get this info
	 * Before deleting check web panel does not use it
	 * @param name application name
	 * @param streamName the stream name to be deleted
	 * @return operation value
	 */
	@Deprecated
	@POST
	@Path("/v2/vod-streams/{appname}")
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteVoDStream(@PathParam("appname") String name, @FormParam("streamName") String streamName) {

		return super.deleteVoDStream(name, streamName);
	}


	@POST
	@Path("/v2/applications/settings/{appname}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String changeSettings(@PathParam("appname") String appname, AppSettings newSettings){

		return super.changeSettings(appname, newSettings);
	}
	
	
	@Deprecated
	@GET
	@Path("/v2/shutdown-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean getShutdownStatus(@QueryParam("appNames") String appNamesArray){

		return super.getShutdownStatus(appNamesArray);
	}
	
	public AntMediaApplicationAdapter getAppAdaptor(String appName) {

		return super.getAppAdaptor(appName);
	}
	
	
	@GET
	@Path("/v2/shutdown-proper-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response isShutdownProperly(@QueryParam("appNames") String appNamesArray)
	{

		return super.isShutdownProperly(appNamesArray);
	}
	
	
	@GET
	@Path("/v2/shutdown-properly")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean setShutdownStatus(@QueryParam("appNames") String appNamesArray){

		return super.setShutdownStatus(appNamesArray);
	}

	@POST
	@Path("/v2/server-settings")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String changeServerSettings(ServerSettings serverSettings){

		return super.changeServerSettings(serverSettings);
	}

	@GET
	@Path("/v2/enterprise-edition")
	@Produces(MediaType.APPLICATION_JSON)
	public Result isEnterpriseEdition(){

		return super.isEnterpriseEdition();
	}

	@GET
	@Path("/v2/applications/settings/{appname}")
	@Produces(MediaType.APPLICATION_JSON)
	public AppSettings getSettings(@PathParam("appname") String appname) 
	{

		return super.getSettings(appname);
	}

	@GET
	@Path("/v2/server-settings")
	@Produces(MediaType.APPLICATION_JSON)
	public ServerSettings getServerSettings() 
	{
		return super.getServerSettings();
	}

	@GET
	@Path("/v2/licence-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Licence getLicenceStatus(@QueryParam("key") String key) 
	{

		return super.getLicenceStatus(key);
	}

	@GET
	@Path("/v2/last-licence-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Licence getLicenceStatus() 
	{
		return super.getLicenceStatus();
	}
	
	/**
	 * This method resets the viewers counts and broadcast status in the db. 
	 * This should be used to recover db after server crashes. 
	 * It's not intended to use to ignore the crash
	 * @param appname the application name that broadcasts will be reset
	 * @return
	 */
	@POST
	@Path("/applications/{appname}/reset")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Result resetBroadcast(@PathParam("appname") String appname) 
	{

		return super.resetBroadcast(appname);
	}

	public void setDataStore(IDataStore dataStore) {
		this.dataStore = dataStore;
	}

	public IDataStore getDataStore() {
		if (dataStore == null) {
			dataStore = getDataStoreFactory().getDataStore();
		}
		return dataStore;
	}

	private ServerSettings getServerSettingsInternal() {

		if(serverSettings == null) {

			WebApplicationContext ctxt = WebApplicationContextUtils.getWebApplicationContext(servletContext); 
			serverSettings = (ServerSettings)ctxt.getBean(ServerSettings.BEAN_NAME);
		}
		return serverSettings;
	}



	public ILicenceService getLicenceServiceInstance () {
		if(licenceService == null) {

			WebApplicationContext ctxt = WebApplicationContextUtils.getWebApplicationContext(servletContext); 
			licenceService = (ILicenceService)ctxt.getBean(ILicenceService.BeanName.LICENCE_SERVICE.toString());
		}
		return licenceService;
	}


	public AdminApplication getApplication() {
		WebApplicationContext ctxt = WebApplicationContextUtils.getWebApplicationContext(servletContext); 
		return (AdminApplication)ctxt.getBean("web.handler");
	}

	public DataStoreFactory getDataStoreFactory() {
		if(dataStoreFactory == null)
		{
			WebApplicationContext ctxt = WebApplicationContextUtils.getWebApplicationContext(servletContext); 
			dataStoreFactory = (DataStoreFactory) ctxt.getBean("dataStoreFactory");
		}
		return dataStoreFactory;
	}

	public void setDataStoreFactory(DataStoreFactory dataStoreFactory) {
		this.dataStoreFactory = dataStoreFactory;
	}

	@GET
	@Path("/v2/cluster-mode-status")
	@Produces(MediaType.APPLICATION_JSON)
	public Result isInClusterMode(){

		return super.isInClusterMode();
	}

	@GET
	@Path("/v2/log-level")
	@Produces(MediaType.APPLICATION_JSON)
	public LogSettings getLogSettings() 
	{

		return super.getLogSettings();
	}

	@GET
	@Path("/v2/log-level-settings/{level}")
	@Produces(MediaType.APPLICATION_JSON)
	public String changeLogSettings(@PathParam("level") String logLevel){

		return super.changeLogSettings(logLevel);
	}

	public Level currentLevelDetect(String logLevel) {

		return super.currentLevelDetect(logLevel);
	}

	@GET
	@Path("/v2/log-file/{offsetSize}/{charSize}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getLogFile(@PathParam("charSize") int charSize, @QueryParam("logType") String logType,
			@PathParam("offsetSize") long offsetSize) throws IOException {

		return super.getLogFile(charSize,logType, offsetSize);
	}

	public String getMD5Hash(String pass){
		String passResult= "";
		try {
			MessageDigest m=MessageDigest.getInstance("MD5");
			m.reset();
			m.update(pass.getBytes(Charset.forName("UTF8")));
			byte[] digestResult=m.digest();
			passResult= Hex.encodeHexString(digestResult);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return passResult;
	}


	// method path was already Restful
	// v2 is added to prevent clashed with RestService.java - identical operations can't have matching parameters
	// this should be fixed with a tag like v2 or a single rest service file should be used

	@POST
	@Path("/v2/applications")
	@Produces(MediaType.APPLICATION_JSON)
	public Result createApplication(@QueryParam("appName") String appName) {

		return super.createApplication(appName);
	}

	// method path was already Restful
	// v2 is added to prevent clashed with RestService.java - identical operations can't have matching parameters
	// there is also a typo in the method name, it also should be fixed
	@DELETE
	@Path("/v2/applications/{appName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Result deleteApplication(@PathParam("appName") String appName) {

		return super.deleteApplication(appName);
	}

}
