package io.antmedia.console.rest;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import io.antmedia.AppSettings;
import io.antmedia.datastore.db.types.Licence;
import io.antmedia.rest.model.Result;
import io.antmedia.rest.model.User;
import io.antmedia.settings.LogSettings;
import io.antmedia.settings.ServerSettings;

@Component
@Path("/v2")
public class RestServiceV2 extends CommonRestService {

	@POST
	@Path("/admin/users")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public Result addUser(User user) {

		return super.addUser(user);
	}
	
	@PUT
	@Path("/admin/users")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public Result editUser(User user) {
		return super.editUser(user);
	}
	
	@DELETE
	@Path("/admin/users/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Override
	public Result deleteUser(@PathParam("username") String userName) {
		return super.deleteUser(userName);
	}
	
	
	@GET
	@Path("/admin/user-list")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public List<User> getUserList() {
		return super.getUserList();
	}
	
	
	@POST
	@Path("/admin/applications/settings/{appname}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public String changeSettings(@PathParam("appname") String appname, AppSettings newSettings){

		return super.changeSettings(appname, newSettings);
	}
	
	@POST
	@Path("/admin/server-settings")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public String changeServerSettings(ServerSettings serverSettings){

		return super.changeServerSettings(serverSettings);
	}
	
	/**
	 * This method resets the viewers counts and broadcast status in the db. 
	 * This should be used to recover db after server crashes. 
	 * It's not intended to use to ignore the crash
	 * @param appname the application name that broadcasts will be reset
	 * @return
	 */
	@POST
	@Path("/admin/applications/{appname}/reset")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public Result resetBroadcast(@PathParam("appname") String appname) 
	{
		return super.resetBroadcast(appname);
	}
	
	
	@DELETE
	@Path("/admin/applications/{appName}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Result deleteApplication(@PathParam("appName") String appName) {
		if (appName != null) {
			return super.deleteApplication(appName);
		}
		return new Result(false, "Application name is not defined");
	}
	
	@POST
	@Path("/admin/applications/{appName}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Result createApplication(@PathParam("appName") String appName) 
	{
		Result result;
		if (appName != null && appName.matches("^[a-zA-Z0-9]*$")) 
		{
			List<String> applications = getApplication().getApplications();

			boolean applicationAlreadyExist = false;
			for (String applicationName : applications) 
			{
				if (applicationName.equalsIgnoreCase(appName)) 
				{
					applicationAlreadyExist = true;
					break;
				}
			}
			
			if (!applicationAlreadyExist) 
			{
				result = super.createApplication(appName);
			}
			else 
			{
				result = new Result(false, "Application with the same name already exists");
			}
		}
		else {
			result = new Result(false, "Application name is not alphanumeric. Please provide alphanumeric characters");
		}
		
		return result;
	}

	@GET
	@Path("/admin-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public Result isAdmin(){
		return super.isAdmin();
	}

	@POST
	@Path("/users/initial")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public Result addInitialUser(User user) {

		return super.addInitialUser(user);
	}

	@GET
	@Path("/first-login-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public Result isFirstLogin() 
	{
		return super.isFirstLogin();
	}


	/**
	 * Authenticates user with userName and password
	 * 
	 * 
	 * @param user: The User object to be authenticated 
	 * @return json that shows user is authenticated or not
	 */
	@POST
	@Path("/users/authenticate")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public Result authenticateUser(User user) {

		return super.authenticateUser(user);
	}


	@POST
	@Path("/users/password")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public Result changeUserPassword(User user) {

		return super.changeUserPassword(user);

	}



	@GET
	@Path("/authentication-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
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
	@Path("/system-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
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
	@Path("/jvm-memory-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
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
	@Path("/system-memory-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
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
	@Path("/file-system-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
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
	@Path("/cpu-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String getCPUInfo() {
		return super.getCPUInfo();
	}
	
	@GET
	@Path("/thread-dump")
	@Produces(MediaType.TEXT_PLAIN)
	@Override
	public String getThreadDump() {
		return super.getThreadDump();
	}
	
	@GET
	@Path("/thread-dump")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String getThreadDumpJSON() {
		return super.getThreadDumpJSON();
	}
	
	
	@GET
	@Path("/threads")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String getThreadsInfo() {
		return super.getThreadsInfo();
	}



	// method path was already Restful
	// v2 is added to prevent clashes with older RestService.java
	@GET
	@Path("/heap-dump")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Override
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
	@Path("/server-time")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String getServerTime() {
		return super.getServerTime();
	}

	@GET
	@Path("/system-resources")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String getSystemResourcesInfo() {

		return super.getSystemResourcesInfo();
	}

	@GET
	@Path("/gpu-status")
	@Produces(MediaType.APPLICATION_JSON) 
	@Override
	public String getGPUInfo() 
	{
		return super.getGPUInfo();
	}


	@GET
	@Path("/version")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String getVersion() {
		return super.getVersion();
	}


	@GET
	@Path("/applications")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String getApplications() {

		return super.getApplications();
	}

	/**
	 * Refactor name getTotalLiveStreamSize
	 * only return totalLiveStreamSize
	 * @return the number of live clients
	 */
	@GET
	@Path("/live-clients-size")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String getLiveClientsSize() 
	{

		return super.getLiveClientsSize();
	}

	@GET
	@Path("/applications-info")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
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
	@Path("/applications/live-streams/{appname}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String getAppLiveStreams(@PathParam("appname") String name) {

		return super.getAppLiveStreams(name);
	}
	
	@GET
	@Path("/shutdown-proper-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public Response isShutdownProperly(@QueryParam("appNames") String appNamesArray)
	{

		return super.isShutdownProperly(appNamesArray);
	}
	
	
	@GET
	@Path("/shutdown-properly")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public boolean setShutdownStatus(@QueryParam("appNames") String appNamesArray){

		return super.setShutdownStatus(appNamesArray);
	}



	@GET
	@Path("/enterprise-edition")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Result isEnterpriseEdition(){

		return super.isEnterpriseEdition();
	}

	@GET
	@Path("/applications/settings/{appname}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public AppSettings getSettings(@PathParam("appname") String appname) 
	{
		return super.getSettings(appname);
	}

	@GET
	@Path("/server-settings")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public ServerSettings getServerSettings() 
	{
		return super.getServerSettings();
	}

	@GET
	@Path("/licence-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public Licence getLicenceStatus(@QueryParam("key") String key) 
	{

		return super.getLicenceStatus(key);
	}

	@GET
	@Path("/last-licence-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public Licence getLicenceStatus() 
	{
		return super.getLicenceStatus();
	}
	

	@GET
	@Path("/cluster-mode-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Result isInClusterMode(){

		return super.isInClusterMode();
	}

	@GET
	@Path("/log-level")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public LogSettings getLogSettings() 
	{
		return super.getLogSettings();
	}

	@POST
	@Path("/log-level/{level}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String changeLogSettings(@PathParam("level") String logLevel){

		return super.changeLogSettings(logLevel);
	}

	@GET
	@Path("/log-file/{offsetSize}/{charSize}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public String getLogFile(@PathParam("charSize") int charSize, @QueryParam("logType") String logType,
			@PathParam("offsetSize") long offsetSize) throws IOException {

		return super.getLogFile(charSize,logType, offsetSize);
	}


	
}
