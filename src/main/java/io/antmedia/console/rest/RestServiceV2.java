package io.antmedia.console.rest;

import io.antmedia.AntMediaApplicationAdapter;
import io.antmedia.AppSettings;
import io.antmedia.datastore.db.types.Broadcast;
import io.antmedia.datastore.db.types.Licence;
import io.antmedia.rest.model.Result;
import io.antmedia.rest.model.User;
import io.antmedia.settings.LogSettings;
import io.antmedia.settings.ServerSettings;
import io.swagger.annotations.*;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;


@Api(value = "ManagementRestService")
@SwaggerDefinition(
		info = @Info(
				description = "Ant Media Server REST API Reference",
				version = "v2.0",
				title = "Ant Media Server REST API Reference",
				contact = @Contact(name = "Ant Media Info", email = "contact@antmedia.io", url = "https://antmedia.io"),
				license = @License(name = "Apache 2.0", url = "http://www.apache.org")),
		consumes = {"application/json"},
		produces = {"application/json"},
		schemes = {SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS},
		externalDocs = @ExternalDocs(value = "External Docs", url = "https://antmedia.io"),
		basePath = "/v2"
)
@Component
@Path("/v2/management")
public class RestServiceV2 extends CommonRestService {

	@ApiOperation(value = "Creates a new user", response = Result.class)
	@POST
	@Path("/users")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Result addUser(@ApiParam(value = "User object. If it is null, new user won't be created.", required = true) User user) {

		return super.addUser(user);
	}

	@ApiOperation(value = "Creates initial user", response = Result.class)
	@POST
	@Path("/users/initial")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Result addInitialUser(@ApiParam(value = "User object. If it is null, new user won't be created.", required = true)User user) {

		return super.addInitialUser(user);
	}
	@ApiOperation(value = "Checks first login status", response = Result.class)
	@GET
	@Path("/first-login-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
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
	@ApiOperation(value = "Shows whether user is authenticated or not", response = Result.class)
	@POST
	@Path("/users/authenticate")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Result authenticateUser(@ApiParam(value = "User object to authenticate", required = true) User user) {

		return super.authenticateUser(user);
	}

	@ApiOperation(value = "Changes the user password", response = Result.class)
	@POST
	@Path("/users/password")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Result changeUserPassword(@ApiParam(value = "User object to change the password", required = true)User user) {

		return super.changeUserPassword(user);

	}

	public Result changeUserPasswordInternal(String userMail, User user) {

		return super.changeUserPasswordInternal(userMail, user);
	}


	//TODO add apioperation value, currently I don't know what isauthenticatedRest does.
	@ApiOperation(value = "", response = Result.class)
	@GET
	@Path("/authentication-status")
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

	@ApiOperation(value = "Gets system information", response = Result.class)
	@GET
	@Path("/system-status")
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
	@ApiOperation(value = "Gets JVM memory status", response = Result.class)
	@GET
	@Path("/jvm-memory-status")
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
	@ApiOperation(value = "Gets system memory status", response = Result.class)
	@GET
	@Path("/system-memory-status")
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
	@ApiOperation(value = "Gets system file status", response = Result.class)
	@GET
	@Path("/file-system-status")
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
	@ApiOperation(value = "Gets system and process cpu load", response = Result.class)
	@GET
	@Path("/cpu-status")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCPUInfo() {
		return super.getCPUInfo();
	}
	
	@ApiOperation(value = "Gets thread dump in plain text", response = Result.class)
	@GET
	@Path("/thread-dump")
	@Produces(MediaType.TEXT_PLAIN)
	public String getThreadDump() {
		return super.getThreadDump();
	}

	@ApiOperation(value = "Gets thread dump in json format", response = Result.class)
	@GET
	@Path("/thread-dump")
	@Produces(MediaType.APPLICATION_JSON)
	public String getThreadDumpJSON() {
		return super.getThreadDumpJSON();
	}

	@ApiOperation(value = "Gets thread information", response = Result.class)
	@GET
	@Path("/threads")
	@Produces(MediaType.APPLICATION_JSON)
	public String getThreadsInfo() {
		return super.getThreadsInfo();
	}



	// method path was already Restful
	// v2 is added to prevent clashes with older RestService.java
	@ApiOperation(value = "Gets heap dump", response = Result.class)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Returns the heap dump")})
	@GET
	@Path("/heap-dump")
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
	@ApiOperation(value = "Gets server time", response = Result.class)
	@GET
	@Path("/server-time")
	@Produces(MediaType.APPLICATION_JSON)
	public String getServerTime() {
		return super.getServerTime();
	}

	@ApiOperation(value = "Gets system resource information", response = Result.class)
	@GET
	@Path("/system-resources")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSystemResourcesInfo() {

		return super.getSystemResourcesInfo();
	}
	@ApiOperation(value = "Gets GPU information", response = Result.class)
	@GET
	@Path("/gpu-status")
	@Produces(MediaType.APPLICATION_JSON) 
	public String getGPUInfo() 
	{
		return super.getGPUInfo();
	}

	@ApiOperation(value = "Gets software version", response = Result.class)
	@GET
	@Path("/version")
	@Produces(MediaType.APPLICATION_JSON)
	public String getVersion() {
		return super.getVersion();
	}

	@ApiOperation(value = "Gets the applications in the server", response = Result.class)
	@GET
	@Path("/applications")
	@Produces(MediaType.APPLICATION_JSON)
	public String getApplications() {

		return super.getApplications();
	}

	/**
	 * Refactor name getTotalLiveStreamSize
	 * only return totalLiveStreamSize
	 * @return the number of live clients
	 */
	@ApiOperation(value = "Gets total amount of live streams", response = Result.class)
	@GET
	@Path("/live-clients-size")
	@Produces(MediaType.APPLICATION_JSON)
	public String getLiveClientsSize() 
	{

		return super.getLiveClientsSize();
	}
	@ApiOperation(value = "Gets application info", response = Result.class)
	@GET
	@Path("/applications-info")
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
	@ApiOperation(value = "Gets live streams in the application", response = Result.class)
	@GET
	@Path("/applications/live-streams/{appname}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAppLiveStreams(@ApiParam(value = "Application name", required = true) @PathParam("appname") String name) {

		return super.getAppLiveStreams(name);
	}

	@ApiOperation(value = "Changes the application settings", response = Result.class)
	@POST
	@Path("/applications/settings/{appname}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String changeSettings(@ApiParam(value = "Application name", required = true) @PathParam("appname") String appname, @ApiParam(value = "New application settings, null fields will be set to default values", required = true) AppSettings newSettings){

		return super.changeSettings(appname, newSettings);
	}
	
	public AntMediaApplicationAdapter getAppAdaptor(String appName) {

		return super.getAppAdaptor(appName);
	}
	@ApiOperation(value = "Checks whether application or applications have shutdown properly or not", response = Result.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Returns the shutdown status of entered applications."),
			@ApiResponse(code = 400, message = "Either entered in wrong format or typed incorrectly application names")})
	@GET
	@Path("/shutdown-proper-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response isShutdownProperly(@ApiParam(value = "Application name", required = true) @QueryParam("appNames") String appNamesArray)
	{

		return super.isShutdownProperly(appNamesArray);
	}

	@ApiOperation(value = "Set application or applications shutdown properly to true", response = Result.class)
	@GET
	@Path("/shutdown-properly")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean setShutdownStatus(@ApiParam(value = "Application name", required = true) @QueryParam("appNames") String appNamesArray){

		return super.setShutdownStatus(appNamesArray);
	}
	@ApiOperation(value = "Changes server settings", response = Result.class)
	@POST
	@Path("/server-settings")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String changeServerSettings(@ApiParam(value = "Server settings", required = true) ServerSettings serverSettings){

		return super.changeServerSettings(serverSettings);
	}
	@ApiOperation(value = "Checks whether its enterprise edition or not", response = Result.class)
	@GET
	@Path("/enterprise-edition")
	@Produces(MediaType.APPLICATION_JSON)
	public Result isEnterpriseEdition(){

		return super.isEnterpriseEdition();
	}
	@ApiOperation(value = "Gets application settings", response = Result.class)
	@GET
	@Path("/applications/settings/{appname}")
	@Produces(MediaType.APPLICATION_JSON)
	public AppSettings getSettings(@ApiParam(value = "Application name", required = true) @PathParam("appname") String appname) 
	{

		return super.getSettings(appname);
	}
	@ApiOperation(value = "Gets server settings", response = Result.class)
	@GET
	@Path("/server-settings")
	@Produces(MediaType.APPLICATION_JSON)
	public ServerSettings getServerSettings() 
	{
		return super.getServerSettings();
	}

	@ApiOperation(value = "Gets license status", response = Result.class)
	@GET
	@Path("/licence-status")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Licence getLicenceStatus(@ApiParam(value = "License key", required = true) @QueryParam("key") String key) 
	{

		return super.getLicenceStatus(key);
	}
	
	@ApiOperation(value = "Gets last license status", response = Result.class)
	@GET
	@Path("/last-licence-status")
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
	@ApiOperation(value = "Resets the viewer counts and broadcasts statuses in the db. This can be used after server crashes to recover db. It's not intended to use to ignore the crash.", response = Result.class)
	@POST
	@Path("/applications/{appname}/reset")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Result resetBroadcast(@ApiParam(value = "Application name", required = true) @PathParam("appname") String appname) 
	{
		return super.resetBroadcast(appname);
	}

	@ApiOperation(value = "Checks whether server in cluster mode or not", response = Result.class)
	@GET
	@Path("/cluster-mode-status")
	@Produces(MediaType.APPLICATION_JSON)
	public Result isInClusterMode(){

		return super.isInClusterMode();
	}
	
	@ApiOperation(value = "Gets log level of server", response = Result.class)
	@GET
	@Path("/log-level")
	@Produces(MediaType.APPLICATION_JSON)
	public LogSettings getLogSettings() 
	{

		return super.getLogSettings();
	}
	@ApiOperation(value = "Sets log level of server. ALL, INFO, TRACE, DEBUG, WARN, ERROR and OFF can be set", response = Result.class)
	@POST
	@Path("/log-level/{level}")
	@Produces(MediaType.APPLICATION_JSON)
	public String changeLogSettings(@ApiParam(value = "Log level", required = true) @PathParam("level") String logLevel){

		return super.changeLogSettings(logLevel);
	}
	
	@ApiOperation(value = "Gets log file", response = Result.class)
	@GET
	@Path("/log-file/{offsetSize}/{charSize}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getLogFile(@ApiParam(value = "Char size of the log", required = true) @PathParam("charSize") int charSize, @ApiParam(value = "Log type. ERROR can be used to get only error logs", required = true) @QueryParam("logType") String logType,
							 @ApiParam(value = "Offset of the retrieved log", required = true) @PathParam("offsetSize") long offsetSize) throws IOException {

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

	@ApiOperation(value = "Creates a new application with given name", response = Result.class)
	@POST
	@Path("/applications/{appName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Result createApplication(@ApiParam(value = "Name for the new application", required = true) @PathParam("appName") String appName) 
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


	@ApiOperation(value = "Deletes application with given name", response = Result.class)
	@DELETE
	@Path("/applications/{appName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Result deleteApplication(@ApiParam(value = "Name of the application to delete", required = true) @PathParam("appName") String appName) {
		if (appName != null) {
			return super.deleteApplication(appName);
		}
		return new Result(false, "Application name is not defined");
	}

}
