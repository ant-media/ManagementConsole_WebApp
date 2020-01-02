package io.antmedia.console.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gson.Gson;

import io.antmedia.SystemUtils;
import io.antmedia.licence.ILicenceService;
import io.antmedia.rest.RestServiceBase;
import io.antmedia.rest.model.Result;
import io.antmedia.rest.model.Version;
import io.antmedia.settings.ServerSettings;
import io.antmedia.statistic.IStatsCollector;

@Component
@Path("/support")
public class SupportRestService {
	class SupportResponse {
		private boolean result;

		public boolean isResult() {
			return result;
		}

		public void setResult(boolean result) {
			this.result = result;
		}
	}
	
	protected static Logger logger = LoggerFactory.getLogger(SupportRestService.class);
	
	@Context
	private ServletContext servletContext;
	private ILicenceService licenceService;
	private IStatsCollector statsCollector;
	private ServerSettings serverSettings;
	private Gson gson = new Gson();
	
	@POST
	@Path("/request")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Result sendSupportRequest(SupportRequest request) {
		boolean success = false;
		logger.info("New Support Request");
		try {
			success = sendSupport(request);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		return new Result(success);
	}	
	
	public ILicenceService getLicenceServiceInstance () {
		if(licenceService == null) {

			WebApplicationContext ctxt = WebApplicationContextUtils.getWebApplicationContext(servletContext); 
			licenceService = (ILicenceService)ctxt.getBean(ILicenceService.BeanName.LICENCE_SERVICE.toString());
		}
		return licenceService;
	}
	
	public IStatsCollector getStatsCollector () {
		if(statsCollector == null) {
			WebApplicationContext ctxt = WebApplicationContextUtils.getWebApplicationContext(servletContext); 
			statsCollector = (IStatsCollector)ctxt.getBean(IStatsCollector.BEAN_NAME);
		}
		return statsCollector;
	}
	
	public boolean sendSupport(SupportRequest supportRequest) throws Exception {
		boolean success = false;
		String cpuInfo = "not allowed to get";
		
		if(supportRequest.isSendSystemInfo()) {
			cpuInfo = getCpuInfo();
		}
		Version version = RestServiceBase.getSoftwareVersion();
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpPost httppost = new HttpPost("https://antmedia.io/livedemo/upload/upload.php");
			List<NameValuePair> nameValuePairs = new ArrayList<>();
			nameValuePairs.add(new BasicNameValuePair("Content-Type", "application/x-www-form-urlencoded;"));
			nameValuePairs.add(new BasicNameValuePair("name", supportRequest.getName()));
			nameValuePairs.add(new BasicNameValuePair("email", supportRequest.getEmail()));
			nameValuePairs.add(new BasicNameValuePair("title", supportRequest.getTitle()));
			nameValuePairs.add(new BasicNameValuePair("description", supportRequest.getDescription()));
			nameValuePairs.add(new BasicNameValuePair("isEnterprise", RestServiceBase.isEnterprise()+""));
			nameValuePairs.add(new BasicNameValuePair("licenseKey", getServerSettings().getLicenceKey()));
			nameValuePairs.add(new BasicNameValuePair("cpuInfo", cpuInfo));
			nameValuePairs.add(new BasicNameValuePair("cpuUsage", getStatsCollector().getCpuLoad()+""));
			nameValuePairs.add(new BasicNameValuePair("ramUsage", SystemUtils.osFreePhysicalMemory()+"/"+SystemUtils.osTotalPhysicalMemory()));
			nameValuePairs.add(new BasicNameValuePair("diskUsage", SystemUtils.osHDFreeSpace(null)+"/"+SystemUtils.osHDTotalSpace(null)));
			nameValuePairs.add(new BasicNameValuePair("version", version.getVersionType()+" "+version.getVersionName()+" "+version.getBuildNumber()));
			
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, StandardCharsets.UTF_8));

			CloseableHttpResponse response = httpclient.execute(httppost);
			try {
				if (response.getStatusLine().getStatusCode() == 200) {
					String jsonResponse = readResponse(response).toString();
					SupportResponse supportResponse = gson.fromJson(jsonResponse, SupportResponse.class);
					success = supportResponse.isResult();
				}
				
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
		if (!success) {
			logger.error("Cannot send e-mail in support form for e-mail: {}", supportRequest.getEmail());
		}
		return success;
	}
	
	public String getCpuInfo() {
		StringBuilder cpuInfo = new StringBuilder();
		ProcessBuilder pb = new ProcessBuilder("lscpu");
		try {
			Process process = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				cpuInfo.append(line);
			}			
		} catch (IOException e) {
			logger.error(ExceptionUtils.getMessage(e));
		}
		
		return cpuInfo.toString();
	}

	public ServerSettings getServerSettings() {
		if(serverSettings == null) {

			WebApplicationContext ctxt = WebApplicationContextUtils.getWebApplicationContext(servletContext); 
			serverSettings = (ServerSettings)ctxt.getBean(ServerSettings.BEAN_NAME);
		}
		return serverSettings;
	}

	public StringBuilder readResponse(HttpResponse response) throws IOException {
		StringBuilder result = new StringBuilder();
		if(response.getEntity() != null) {
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line+"\r\n");
			}
		}
		return result;
	}
}
