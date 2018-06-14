package io.antmedia.console.rest;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

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
import io.antmedia.datastore.preference.PreferenceStore;
import io.antmedia.rest.BroadcastRestService;
import io.antmedia.rest.model.AppSettingsModel;
import io.antmedia.rest.model.Result;
import io.antmedia.rest.model.User;

@Component
@Path("/")
public class RestService {


	private static final String SETTINGS_ACCEPT_ONLY_STREAMS_IN_DATA_STORE = "settings.acceptOnlyStreamsInDataStore";

	private static final String USER_PASSWORD = "user.password";

	private static final String USER_EMAIL = "user.email";

	public static final String IS_AUTHENTICATED = "isAuthenticated";

	Gson gson = new Gson();

	private DataStore dataStore;

	protected static Logger logger = LoggerFactory.getLogger(RestService.class);


	@Context 
	private ServletContext servletContext;

	@Context
	private HttpServletRequest servletRequest;


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
	public Result addUser(User user) {
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
		Result operationResult = new Result(result);
		operationResult.setErrorId(errorId);
		return operationResult;
	}


	@POST
	@Path("/addInitialUser")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Result addInitialUser(User user) {
		boolean result = false;
		int errorId = -1;
		if (getDataStore().getNumberOfUserRecords() == 0) {
			result = getDataStore().addUser(user.email, user.password, 1);
		}

		Result operationResult = new Result(result);
		operationResult.setErrorId(errorId);
		return operationResult;
	}

	@GET
	@Path("/isFirstLogin")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Result isFirstLogin() 
	{
		boolean result = false;
		if (getDataStore().getNumberOfUserRecords() == 0) {
			result = true;
		}
		return new Result(result);
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
	 * @param user
	 * @return json that shows user is authenticated or not
	 */
	@POST
	@Path("/authenticateUser")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Result authenticateUser(User user) {
		//TODO: check that request is coming from authorized user
		boolean result = getDataStore().doesUserExist(user.email, user.password);
		//boolean result = true;
		if (result) {
			HttpSession session = servletRequest.getSession();
			session.setAttribute(IS_AUTHENTICATED, true);
			session.setAttribute(USER_EMAIL, user.email);
			session.setAttribute(USER_PASSWORD, user.password);
		}
		return new Result(result);
	}


	@POST
	@Path("/changeUserPassword")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Result changeUserPassword(User user) {
		//TODO: check that request is coming from authorized user


		String userMail = (String)servletRequest.getSession().getAttribute(USER_EMAIL);

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
					HttpSession session = servletRequest.getSession();
					if (session != null) {
						session.setAttribute(IS_AUTHENTICATED, true);
						session.setAttribute(USER_EMAIL, userMail);
						session.setAttribute(USER_PASSWORD, user.newPassword);
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
	public Result isAuthenticatedRest(){
		return new Result(isAuthenticated(servletRequest.getSession()));
	}

	public static boolean isAuthenticated(HttpSession session) 
	{

		Object isAuthenticated = session.getAttribute(IS_AUTHENTICATED);
		Object userEmail = session.getAttribute(USER_EMAIL);
		Object userPassword = session.getAttribute(USER_PASSWORD);
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
		return gson.toJson(new Result(deleteVoDStream));
	}




	@POST
	@Path("/changeSettings/{appname}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String changeSettings(@PathParam("appname") String appname, AppSettingsModel appsettings){


		PreferenceStore store = new PreferenceStore("red5-web.properties");
		store.setFullPath("webapps/"+appname+"/WEB-INF/red5-web.properties");

		store.put("settings.mp4MuxingEnabled", String.valueOf(appsettings.isMp4MuxingEnabled()));
		store.put("settings.addDateTimeToMp4FileName", String.valueOf(appsettings.isAddDateTimeToMp4FileName()));
		store.put("settings.hlsMuxingEnabled", String.valueOf(appsettings.isHlsMuxingEnabled()));
		store.put(SETTINGS_ACCEPT_ONLY_STREAMS_IN_DATA_STORE, String.valueOf(appsettings.isAcceptOnlyStreamsInDataStore()));
		store.put("settings.objectDetectionEnabled", String.valueOf(appsettings.isObjectDetectionEnabled()));

		if (appsettings.getVodFolder() == null) {
			store.put("settings.vodFolder", "");
		}else {
			store.put("settings.vodFolder", appsettings.getVodFolder());
		}


		if (appsettings.getHlsListSize() < 5) {
			store.put("settings.hlsListSize", "5");
		}
		else {
			store.put("settings.hlsListSize", String.valueOf(appsettings.getHlsListSize()));
		}

		if (appsettings.getHlsTime() < 2) {
			store.put("settings.hlsTime", "2");
		}
		else {
			store.put("settings.hlsTime", String.valueOf(appsettings.getHlsTime()));
		}

		if (appsettings.getHlsPlayListType() == null) {
			store.put("settings.hlsPlayListType", "");
		}
		else {
			store.put("settings.hlsPlayListType", appsettings.getHlsPlayListType());
		}

		if (appsettings.getFacebookClientId() == null){
			store.put("facebook.clientId", "");
		}
		else {
			store.put("facebook.clientId", appsettings.getFacebookClientId());
		}

		if (appsettings.getEncoderSettings() == null) {
			store.put("settings.encoderSettingsString", "");
		}
		else {
			store.put("settings.encoderSettingsString", io.antmedia.AppSettings.getEncoderSettingsString(appsettings.getEncoderSettings()));
		}

		store.put("settings.previewOverwrite", String.valueOf(appsettings.isPreviewOverwrite()));
		
		getApplication().updateAppSettings(appname, appsettings);

		return gson.toJson(new Result(store.save()));
	}

	@GET
	@Path("/isEnterpriseEdition")
	@Produces(MediaType.APPLICATION_JSON)
	public Result isEnterpriseEdition(){
		boolean isEnterprise = BroadcastRestService.isEnterprise();
		return new Result(isEnterprise, "");
	}

	@GET
	@Path("/getSettings/{appname}")
	@Produces(MediaType.APPLICATION_JSON)
	public AppSettingsModel getSettings(@PathParam("appname") String appname) 
	{

		//TODO: Get bean from app context not read file

		PreferenceStore store = new PreferenceStore("red5-web.properties");
		store.setFullPath("webapps/"+appname+"/WEB-INF/red5-web.properties");
		AppSettingsModel appSettings = new AppSettingsModel();

		if (store.get("settings.mp4MuxingEnabled") != null) {
			appSettings.setMp4MuxingEnabled(Boolean.parseBoolean(store.get("settings.mp4MuxingEnabled")));
		}
		if (store.get("settings.addDateTimeToMp4FileName") != null) {
			appSettings.setAddDateTimeToMp4FileName(Boolean.parseBoolean(store.get("settings.addDateTimeToMp4FileName")));
		}
		if (store.get("settings.hlsMuxingEnabled") != null) {
			appSettings.setHlsMuxingEnabled(Boolean.parseBoolean(store.get("settings.hlsMuxingEnabled")));
		}
		if (store.get("settings.objectDetectionEnabled") != null) {
			appSettings.setObjectDetectionEnabled(Boolean.parseBoolean(store.get("settings.objectDetectionEnabled")));
		}
		
		if (store.get("settings.hlsListSize") != null) {
			appSettings.setHlsListSize(Integer.valueOf(store.get("settings.hlsListSize")));
		}

		if (store.get("settings.hlsTime") != null) {
			appSettings.setHlsTime(Integer.valueOf(store.get("settings.hlsTime")));
		}
		appSettings.setHlsPlayListType(store.get("settings.hlsPlayListType"));
		appSettings.setFacebookClientId(store.get("facebook.clientId"));
		appSettings.setFacebookClientSecret(store.get("facebook.clientSecret"));
		appSettings.setYoutubeClientId(store.get("youtube.clientId"));
		appSettings.setYoutubeClientSecret(store.get("youtube.clientSecret"));
		appSettings.setPeriscopeClientId(store.get("periscope.clientId"));
		appSettings.setPeriscopeClientSecret(store.get("periscope.clientSecret"));
		appSettings.setAcceptOnlyStreamsInDataStore(Boolean.valueOf(store.get(SETTINGS_ACCEPT_ONLY_STREAMS_IN_DATA_STORE)));
		appSettings.setVodFolder(store.get("settings.vodFolder"));

		appSettings.setEncoderSettings(io.antmedia.AppSettings.getEncoderSettingsList(store.get("settings.encoderSettingsString")));

		if (store.get("settings.previewOverwrite") != null) {
			appSettings.setPreviewOverwrite(Boolean.parseBoolean(store.get("settings.previewOverwrite")));
		}

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
