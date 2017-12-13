package io.antmedia.console.rest;

import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.red5.server.util.ScopeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.antmedia.EncoderSettings;
import io.antmedia.console.AdminApplication;
import io.antmedia.console.AdminApplication.ApplicationInfo;
import io.antmedia.console.AdminApplication.BroadcastInfo;
import io.antmedia.console.DataStore;
import io.antmedia.console.SystemUtils;
import io.antmedia.console.User;
import io.antmedia.console.rest.RestService.OperationResult;
import io.antmedia.datastore.preference.PreferenceStore;
import io.antmedia.rest.BroadcastRestService.Result;

@Component
@Path("/")
public class RestService {


	private static final String USER_PASSWORD = "user.password";

	private static final String USER_EMAIL = "user.email";

	public static final String IS_AUTHENTICATED = "isAuthenticated";

	Gson gson = new Gson();

	private DataStore dataStore;
	
	protected static Logger logger = LoggerFactory.getLogger(RestService.class);


	@Context 
	private ServletContext servletContext;

	public static class AppSettingsModel {
		public boolean mp4MuxingEnabled;
		public boolean addDateTimeToMp4FileName;
		public boolean hlsMuxingEnabled;
		public int hlsListSize;
		public int hlsTime;
		public String hlsPlayListType;

		public String facebookClientId;
		public String facebookClientSecret;

		public String youtubeClientId;
		public String youtubeClientSecret;

		public String periscopeClientId;
		public String periscopeClientSecret;

		public List<EncoderSettings> encoderSettings;
	}

	public static class OperationResult {
		public boolean success = false;


		public OperationResult(boolean success) {
			this.success = success;
		}

		//use if required
		public int id = -1;

		public int errorId = -1;

		public boolean isSuccess() {
			return success;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public int getErrorId() {
			return errorId;
		}

		public void setErrorId(int errorId) {
			this.errorId = errorId;
		}

	}


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
	 * @param userName
	 * @return JSON data
	 * if user is added success will be true
	 * if user is not added success will be false
	 * 	if user is not added, errorId = 1 means username already exist
	 */
	@POST
	@Path("/addUser")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public OperationResult addUser(User user) {
		//TODO: check that request is coming from authorized user
		boolean result = false;
		int errorId = -1;
		if (user != null && !getDataStore().doesUsernameExist(user.email)) {
			result = getDataStore().addUser(user.email, user.password, 1);

		}
		else {
			if (user == null) {
				logger.info("user variable null");
			}
			else {
				logger.info("user already exist in db");
			}
			
			errorId = 1;
		}
		OperationResult operationResult = new OperationResult(result);
		operationResult.setErrorId(errorId);
		return operationResult;
	}


	@POST
	@Path("/addInitialUser")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public OperationResult addInitialUser(User user) {
		boolean result = false;
		int errorId = -1;
		if (getDataStore().getNumberOfUserRecords() == 0) {
			result = getDataStore().addUser(user.email, user.password, 1);
		}

		OperationResult operationResult = new OperationResult(result);
		operationResult.setErrorId(errorId);
		return operationResult;
	}
	
	@GET
	@Path("/isFirstLogin")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public OperationResult isFirstLogin() 
	{
		boolean result = false;
		if (getDataStore().getNumberOfUserRecords() == 0) {
			result = true;
		}
		return new OperationResult(result);
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
	 * Post method should be used.
	 * 
	 * application/x-www-form-urlencoded
	 * 
	 * form parameters - case sensitive
	 * "userName" and "password"
	 * 
	 * @param userName
	 * @param password
	 * @return json that shows user is authenticated or not
	 */
	@POST
	@Path("/authenticateUser")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public OperationResult authenticateUser(User user) {
		//TODO: check that request is coming from authorized user
		boolean result = getDataStore().doesUserExist(user.email, user.password);
		//boolean result = true;
		if (result) {
			servletContext.setAttribute(IS_AUTHENTICATED, true);
			servletContext.setAttribute(USER_EMAIL, user.email);
			servletContext.setAttribute(USER_PASSWORD, user.password);
		}
		return new OperationResult(result);
	}


	@POST
	@Path("/changeUserPassword")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Result changeUserPassword(User user) {
		//TODO: check that request is coming from authorized user
		
		String userMail = (String)servletContext.getAttribute(USER_EMAIL);
		
		return changeUserPasswordInternal(userMail, user);
		
	}
	
	public Result changeUserPasswordInternal(String userMail, User user) {
		boolean result = false;
		String message = null;
		if (userMail != null) {
			result = getDataStore().doesUserExist(userMail, user.password);
			//boolean result = true;
			if (result) {
				result = getDataStore().editUser(userMail, user.newPassword, 1);
			
				if (result) {
					if (servletContext != null) {
						servletContext.setAttribute(IS_AUTHENTICATED, true);
						servletContext.setAttribute(USER_EMAIL, userMail);
						servletContext.setAttribute(USER_PASSWORD, user.newPassword);
					}
				}
			}
			else {
				message = "User not exist with that name and pass";
			}
		}
		else {
			message = "User name does not exist in context";
		}

		//System.out.println("user name: " + user.email + " pass:" + user.password + " user newpass:" + user.getNewPassword());
		return new Result(result, message);
	}




	@GET
	@Path("/isAuthenticated")
	@Produces(MediaType.APPLICATION_JSON)
	public OperationResult isAuthenticatedRest(){

		return new OperationResult(isAuthenticated(servletContext));
	}

	public static boolean isAuthenticated(ServletContext servletContext) 
	{
		Object isAuthenticated = servletContext.getAttribute(IS_AUTHENTICATED);
		Object userEmail = servletContext.getAttribute(USER_EMAIL);
		Object userPassword = servletContext.getAttribute(USER_PASSWORD);
		boolean result = false;
		if (isAuthenticated != null && userEmail != null && userPassword != null) {
			result = true;
		}
		return result;
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
	@Path("/getSystemInfo")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSystemInfo() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("osName", SystemUtils.osName);
		jsonObject.addProperty("osArch", SystemUtils.osArch);
		jsonObject.addProperty("javaVersion", SystemUtils.jvmVersion);
		jsonObject.addProperty("processorCount", SystemUtils.osProcessorX);
		return gson.toJson(jsonObject);
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
	@Path("/getJVMMemoryInfo")
	@Produces(MediaType.APPLICATION_JSON)
	public String getJVMMemoryInfo() {
		JsonObject jsonObject = new JsonObject();

		jsonObject.addProperty("maxMemory", SystemUtils.jvmMaxMemory("B", false));
		jsonObject.addProperty("totalMemory", SystemUtils.jvmTotalMemory("B", false));
		jsonObject.addProperty("freeMemory", SystemUtils.jvmFreeMemory("B", false));
		jsonObject.addProperty("inUseMemory", SystemUtils.jvmInUseMemory("B", false));

		return gson.toJson(jsonObject);
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
	@Path("/getSystemMemoryInfo")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSystemMemoryInfo() {
		JsonObject jsonObject = new JsonObject();

		jsonObject.addProperty("virtualMemory", SystemUtils.osCommittedVirtualMemory("B", false));
		jsonObject.addProperty("totalMemory", SystemUtils.osTotalPhysicalMemory("B", false));
		jsonObject.addProperty("freeMemory", SystemUtils.osFreePhysicalMemory("B", false));
		jsonObject.addProperty("inUseMemory", SystemUtils.osInUsePhysicalMemory("B", false));
		jsonObject.addProperty("totalSwapSpace", SystemUtils.osTotalSwapSpace("B", false));
		jsonObject.addProperty("freeSwapSpace", SystemUtils.osFreeSwapSpace("B", false));
		jsonObject.addProperty("inUseSwapSpace", SystemUtils.osInUseSwapSpace("B", false));

		return gson.toJson(jsonObject);
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
	@Path("/getFileSystemInfo")
	@Produces(MediaType.APPLICATION_JSON)
	public String getFileSystemInfo() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("usableSpace", SystemUtils.osHDUsableSpace(null,"B", false));
		jsonObject.addProperty("totalSpace", SystemUtils.osHDTotalSpace(null, "B", false));
		jsonObject.addProperty("freeSpace", SystemUtils.osHDFreeSpace(null,  "B", false));
		jsonObject.addProperty("inUseSpace", SystemUtils.osHDInUseSpace(null, "B", false));

		return gson.toJson(jsonObject);
	}

	/**
	 * getProcessCpuTime:  microseconds CPU time used by the process
	 * 
	 * getSystemCpuLoad:	"% recent cpu usage" for the whole system. 
	 * 
	 * getProcessCpuLoad: "% recent cpu usage" for the Java Virtual Machine process. 
	 * @return
	 */
	@GET
	@Path("/getCPUInfo")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCPUInfo() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("processCPUTime", SystemUtils.getProcessCpuTime());
		jsonObject.addProperty("systemCPULoad", SystemUtils.getSystemCpuLoad());
		jsonObject.addProperty("processCPULoad", SystemUtils.getProcessCpuLoad());			
		return gson.toJson(jsonObject);
	}

	@GET
	@Path("/getApplications")
	@Produces(MediaType.APPLICATION_JSON)
	public String getApplications() {
		List<String> applications = getApplication().getApplications();
		JsonObject jsonObject = new JsonObject();
		JsonArray jsonArray = new JsonArray();

		for (String appName : applications) {
			if (!appName.equals(AdminApplication.APP_NAME)) {
				jsonArray.add(appName);
			}
		}
		jsonObject.add("applications", jsonArray);
		return gson.toJson(jsonObject);
	}

	@GET
	@Path("/getLiveClientsSize")
	@Produces(MediaType.APPLICATION_JSON)
	public String getLiveClientsSize() 
	{
		int totalConnectionSize = getApplication().getTotalConnectionSize();
		int totalLiveStreamSize = getApplication().getTotalLiveStreamSize();
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("totalConnectionSize", totalConnectionSize);
		jsonObject.addProperty("totalLiveStreamSize", totalLiveStreamSize);

		return gson.toJson(jsonObject);
	}

	@GET
	@Path("/getApplicationsInfo")
	@Produces(MediaType.APPLICATION_JSON)
	public String getApplicationInfo() {
		List<ApplicationInfo> info = getApplication().getApplicationInfo();
		return gson.toJson(info);
	}

	@GET
	@Path("/getAppLiveStreams/{appname}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAppLiveStreams(@PathParam("appname") String name) {
		List<BroadcastInfo> appLiveStreams = getApplication().getAppLiveStreams(name);
		return gson.toJson(appLiveStreams);
	}

	@GET
	@Path("/getAppVoDStreams/{appname}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAppVoDStreams(@PathParam("appname") String name) {
		List<BroadcastInfo> appLiveStreams = getApplication().getAppVoDStreams(name);
		return gson.toJson(appLiveStreams);
	}

	@POST
	@Path("/deleteVoDStream/{appname}")
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteVoDStream(@PathParam("appname") String name, @FormParam("streamName") String streamName) {
		boolean deleteVoDStream = getApplication().deleteVoDStream(name, streamName);
		return gson.toJson(new OperationResult(deleteVoDStream));
	}




	@POST
	@Path("/changeSettings/{appname}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String changeSettings(@PathParam("appname") String appname, AppSettingsModel appsettings){


		PreferenceStore store = new PreferenceStore("red5-web.properties");
		store.setFullPath("webapps/"+appname+"/WEB-INF/red5-web.properties");

		store.put("settings.mp4MuxingEnabled", String.valueOf(appsettings.mp4MuxingEnabled));
		store.put("settings.addDateTimeToMp4FileName", String.valueOf(appsettings.addDateTimeToMp4FileName));
		store.put("settings.hlsMuxingEnabled", String.valueOf(appsettings.hlsMuxingEnabled));
		if (appsettings.hlsListSize < 5) {
			store.put("settings.hlsListSize", "2");
		}
		else {
			store.put("settings.hlsListSize", String.valueOf(appsettings.hlsListSize));
		}

		if (appsettings.hlsTime < 2) {
			store.put("settings.hlsTime", "2");
		}
		else {
			store.put("settings.hlsTime", String.valueOf(appsettings.hlsTime));
		}

		if (appsettings.hlsPlayListType == null) {
			store.put("settings.hlsPlayListType", "");
		}
		else {
			store.put("settings.hlsPlayListType", appsettings.hlsPlayListType);
		}

		if (appsettings.facebookClientId == null){
			store.put("facebook.clientId", "");
		}
		else {
			store.put("facebook.clientId", appsettings.facebookClientId);
		}

		if (appsettings.encoderSettings == null) {
			store.put("settings.encoderSettingsString", "");
		}
		else {
			store.put("settings.encoderSettingsString", io.antmedia.AppSettings.getEncoderSettingsString(appsettings.encoderSettings));
		}

		/*		
		if (appsettings.facebookClientSecret == null){
			store.put("facebook.clientSecret", "");
		}
		else {
			store.put("facebook.clientSecret", appsettings.facebookClientSecret);
		}

		if (appsettings.youtubeClientId == null) {
			store.put("youtube.clientId", "");
		}
		else {
			store.put("youtube.clientId", appsettings.youtubeClientId);
		}

		if (appsettings.youtubeClientSecret == null) {
			store.put("youtube.clientSecret", "");
		}
		else {
			store.put("youtube.clientSecret", appsettings.youtubeClientSecret);
		}

		if (appsettings.periscopeClientId == null) {
			store.put("periscope.clientId", "");
		}
		else {
			store.put("periscope.clientId", appsettings.periscopeClientId);
		}

		if (appsettings.periscopeClientSecret == null) {
			store.put("periscope.clientSecret", "");
		}
		else {
			store.put("periscope.clientSecret", appsettings.periscopeClientSecret);
		}
		 */	

		getApplication().updateAppSettings(appname, appsettings);

		return gson.toJson(new OperationResult(store.save()));
	}

	public boolean isClass(String className) {
		try  {
			Class.forName(className);
			return true;
		}  catch (ClassNotFoundException e) {
			return false;
		}
	}

	@GET
	@Path("/isEnterpriseEdition")
	@Produces(MediaType.APPLICATION_JSON)
	public Result isEnterpriseEdition(){
		boolean isEnterprise = isClass("io.antmedia.enterprise.adaptive.EncoderAdaptor");
		return new Result(isEnterprise, "");
	}




	@GET
	@Path("/getSettings/{appname}")
	@Produces(MediaType.APPLICATION_JSON)
	public AppSettingsModel getSettings(@PathParam("appname") String appname) 
	{
		PreferenceStore store = new PreferenceStore("red5-web.properties");
		store.setFullPath("webapps/"+appname+"/WEB-INF/red5-web.properties");
		AppSettingsModel appSettings = new AppSettingsModel();

		if (store.get("settings.mp4MuxingEnabled") != null) {
			appSettings.mp4MuxingEnabled = Boolean.parseBoolean(store.get("settings.mp4MuxingEnabled"));
		}
		if (store.get("settings.addDateTimeToMp4FileName") != null) {
			appSettings.addDateTimeToMp4FileName = Boolean.parseBoolean(store.get("settings.addDateTimeToMp4FileName"));
		}
		if (store.get("settings.hlsMuxingEnabled") != null) {
			appSettings.hlsMuxingEnabled = Boolean.parseBoolean(store.get("settings.hlsMuxingEnabled"));
		}
		if (store.get("settings.hlsListSize") != null) {
			appSettings.hlsListSize = Integer.valueOf(store.get("settings.hlsListSize"));
		}

		if (store.get("settings.hlsTime") != null) {
			appSettings.hlsTime = Integer.valueOf(store.get("settings.hlsTime"));
		}
		appSettings.hlsPlayListType = store.get("settings.hlsPlayListType");
		appSettings.facebookClientId = store.get("facebook.clientId");
		appSettings.facebookClientSecret = store.get("facebook.clientSecret");
		appSettings.youtubeClientId = store.get("youtube.clientId");
		appSettings.youtubeClientSecret = store.get("youtube.clientSecret");
		appSettings.periscopeClientId = store.get("periscope.clientId");
		appSettings.periscopeClientSecret = store.get("periscope.clientSecret");

		appSettings.encoderSettings = io.antmedia.AppSettings.getEncoderSettingsList(store.get("settings.encoderSettingsString"));


		return appSettings;
	}

	public void setDataStore(DataStore dataStore) {
		this.dataStore = dataStore;
	}


	public DataStore getDataStore() {
		if (dataStore == null) {
			WebApplicationContext ctxt = WebApplicationContextUtils.getWebApplicationContext(servletContext); 
			dataStore = (DataStore)ctxt.getBean("dataStore");
		}
		return dataStore;
	}


	public AdminApplication getApplication() {
		WebApplicationContext ctxt = WebApplicationContextUtils.getWebApplicationContext(servletContext); 
		return (AdminApplication)ctxt.getBean("web.handler");
	}






}
