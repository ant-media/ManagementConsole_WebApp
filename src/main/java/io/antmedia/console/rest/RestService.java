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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.antmedia.AppSettingsModel;
import io.antmedia.SystemUtils;
import io.antmedia.console.AdminApplication;
import io.antmedia.console.AdminApplication.ApplicationInfo;
import io.antmedia.console.AdminApplication.BroadcastInfo;
<<<<<<< HEAD
import io.antmedia.console.DataStore;
import io.antmedia.console.SystemUtils;
import io.antmedia.datastore.db.types.Broadcast;
import io.antmedia.datastore.db.types.Licence;
=======
import io.antmedia.console.datastore.DataStoreFactory;
import io.antmedia.console.datastore.IDataStore;
import io.antmedia.datastore.AppSettingsManager;
>>>>>>> refs/remotes/origin/master
import io.antmedia.datastore.preference.PreferenceStore;
import io.antmedia.licence.LicenceService;
import io.antmedia.rest.BroadcastRestService;
import io.antmedia.rest.model.Result;
import io.antmedia.rest.model.User;
import io.antmedia.rest.model.UserType;
import io.antmedia.settings.ServerSettings;

@Component
@Path("/")
public class RestService {


	private static final String USER_PASSWORD = "user.password";

	private static final String USER_EMAIL = "user.email";

	public static final String IS_AUTHENTICATED = "isAuthenticated";

	Gson gson = new Gson();
	Gson gson2 = new Gson();

	private IDataStore dataStore;

	protected static final Logger logger = LoggerFactory.getLogger(RestService.class);


	@Context 
	private ServletContext servletContext;

	@Context
	private HttpServletRequest servletRequest;

	private DataStoreFactory dataStoreFactory;
	private ServerSettings serverSettings;

	private LicenceService licenceService;


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
		if (user != null && !getDataStore().doesUsernameExist(user.getEmail())) {
			result = getDataStore().addUser(user.getEmail(), user.getPassword(), UserType.ADMIN);
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
			result = getDataStore().addUser(user.getEmail(), user.getPassword(), UserType.ADMIN);
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
		boolean result = getDataStore().doesUserExist(user.getEmail(), user.getPassword());
		if (result) {
			HttpSession session = servletRequest.getSession();
			session.setAttribute(IS_AUTHENTICATED, true);
			session.setAttribute(USER_EMAIL, user.getEmail());
			session.setAttribute(USER_PASSWORD, user.getPassword());
		}
		return new Result(result);
	}


	@POST
	@Path("/changeUserPassword")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Result changeUserPassword(User user) {

		String userMail = (String)servletRequest.getSession().getAttribute(USER_EMAIL);

		return changeUserPasswordInternal(userMail, user);

	}

	public Result changeUserPasswordInternal(String userMail, User user) {
		boolean result = false;
		String message = null;
		if (userMail != null) {
			result = getDataStore().doesUserExist(userMail, user.getPassword());
			if (result) {
				result = getDataStore().editUser(userMail, user.getNewPassword(), UserType.ADMIN);

				if (result) {
					HttpSession session = servletRequest.getSession();
					if (session != null) {
						session.setAttribute(IS_AUTHENTICATED, true);
						session.setAttribute(USER_EMAIL, userMail);
						session.setAttribute(USER_PASSWORD, user.getNewPassword());
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

	/**
	 * Refactor name getTotalLiveStreamSize
	 * only return totalLiveStreamSize
	 * @return
	 */
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

	/**
	 * Refactor remove this function and use ProxyServlet to get this info
	 * Before deleting check web panel does not use it
	 * @param name
	 * @return
	 */
	@GET
	@Path("/getAppLiveStreams/{appname}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAppLiveStreams(@PathParam("appname") String name) {
		List<BroadcastInfo> appLiveStreams = getApplication().getAppLiveStreams(name);
		return gson.toJson(appLiveStreams);
	}


	/**
	 * Refactor remove this function and use ProxyServlet to get this info
	 * Before deleting check web panel does not use it
	 * @param name
	 * @return
	 */
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

		ApplicationContext context = getApplication().getApplicationContext(appname);
		return gson.toJson(new Result(AppSettingsManager.updateAppSettings(context, appsettings, true)));

	}



	@POST
	@Path("/changeServerSettings")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String changeServerSettings(ServerSettings serverSettings){


		PreferenceStore store = new PreferenceStore("red5.properties");
		store.setFullPath("conf/red5.properties");

		if(serverSettings.getServerName() == null) {
			store.put("ant.media.server.name", "");
			getServerSettings().setServerName("");

		}else {
			store.put("ant.media.server.name", serverSettings.getServerName());
			getServerSettings().setServerName(serverSettings.getServerName());
		}

		if (serverSettings.getLicenceKey() == null) {
			store.put("licence.key","");
			getServerSettings().setLicenceKey("");
		}else {
			store.put("licence.key", serverSettings.getLicenceKey());
			getServerSettings().setLicenceKey(serverSettings.getLicenceKey());
		}


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

		return AppSettingsManager.getAppSettings(appname);

	}

	public void setDataStore(IDataStore dataStore) {


	@GET
	@Path("/getServerSettings")
	@Produces(MediaType.APPLICATION_JSON)
	public ServerSettings getServerSettings() 
	{

		return fetchServerSettings();
	}


	@GET
	@Path("/requestLicence")
	@Produces(MediaType.APPLICATION_JSON)
	public Result resuestLicence(String key) 
	{
		Result result = new Result(false);

		getLicenceServiceInstance().checkLicence(key);
		result.setSuccess(true);

		return result;
	}

	@GET
	@Path("/getLicenceStatus")
	@Produces(MediaType.APPLICATION_JSON)
	public Licence getLicenceStatus() 
	{
		return getLicenceServiceInstance().getLicenceStatusResponse();
	}




	public void setDataStore(DataStore dataStore) {
		this.dataStore = dataStore;
	}


	public IDataStore getDataStore() {
		if (dataStore == null) {
			dataStore = getDataStoreFactory().getDataStore();
		}
		return dataStore;
	}

	public ServerSettings fetchServerSettings() {

		WebApplicationContext ctxt = WebApplicationContextUtils.getWebApplicationContext(servletContext); 
		serverSettings = (ServerSettings)ctxt.getBean(ServerSettings.BEAN_NAME);

		return serverSettings;
	}



	public LicenceService getLicenceServiceInstance () {
		WebApplicationContext ctxt = WebApplicationContextUtils.getWebApplicationContext(servletContext); 
		licenceService = (LicenceService)ctxt.getBean("ant.media.licence.service");

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
	@Path("/isInClusterMode")
	@Produces(MediaType.APPLICATION_JSON)
	public Result isInClusterMode(){
		WebApplicationContext ctxt = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		//TODO move BEAN name from TCPCluster to IClusterNotifier then use it
		boolean isCluster = ctxt.containsBean("tomcat.cluster");
		return new Result(isCluster, "");
	}
}
