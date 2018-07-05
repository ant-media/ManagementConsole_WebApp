package io.antmedia.console;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.apache.commons.io.FileUtils;
import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.scope.IBroadcastScope;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.scope.ScopeType;
import org.red5.server.api.statistics.IScopeStatistics;
//import org.slf4j.Logger;
import org.red5.server.util.ScopeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import io.antmedia.AntMediaApplicationAdapter;
import io.antmedia.AppSettings;
import io.antmedia.EncoderSettings;
import io.antmedia.datastore.db.IDataStore;
import io.antmedia.rest.model.AppSettingsModel;
import io.antmedia.security.AcceptOnlyStreamsInDataStore;
import io.antmedia.settings.ServerSettings;


/**
 * Sample application that uses the client manager.
 * 
 * @author The Red5 Project (red5@osflash.org)
 */
public class AdminApplication extends MultiThreadedApplicationAdapter {
	@Context 
	private ServletContext servletContext;
	private ApplicationContext appCtx;

	//private static Logger log = Red5LoggerFactory.getLogger(Application.class);


	public static final String APP_NAME = "ConsoleApp";

	public static class ApplicationInfo {
		public String name;
		public int liveStreamCount;
		public int vodCount;
		public long storage;
	}

	public static class BroadcastInfo {
		public String name;
		public int watcherCount;

		public BroadcastInfo(String name, int watcherCount) {
			this.name = name;
			this.watcherCount = watcherCount;
		}
	}
	private IScope rootScope;
	private ServerSettings serverSettings;

	/** {@inheritDoc} */
	@Override
	public boolean connect(IConnection conn, IScope scope, Object[] params) {
		this.scope = scope;
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public void disconnect(IConnection conn, IScope scope) {
		//log.info("disconnect");
		super.disconnect(conn, scope);
	}

	private IScope getRootScope() {
		if (rootScope == null) {
			rootScope = ScopeUtils.findRoot(scope);
		}
		return rootScope;
	}


	public int getTotalLiveStreamSize() 
	{
		IScope root = getRootScope();
		java.util.Set<String> names = root.getScopeNames();
		int size = 0;
		for (String name : names) {
			IScope scope = root.getScope(name);
			if (scope != null) {
				//logger.info("name of the scope: " + );
				Object adapter = scope.getContext().getApplicationContext().getBean(AntMediaApplicationAdapter.BEAN_NAME);
				if (adapter instanceof AntMediaApplicationAdapter) 
				{
					IDataStore dataStore = ((AntMediaApplicationAdapter)adapter).getDataStore();
					if (dataStore != null) {
						size +=  dataStore.getActiveBroadcastCount();
					}
				}
			}
		}
		return size;
	}

	public List<ApplicationInfo> getApplicationInfo() {
		List<String> appNames = getApplications();
		List<ApplicationInfo> appsInfo = new ArrayList<>();
		IScope root = getRootScope();
		for (String name : appNames) {
			if (name.equals(APP_NAME)) {
				continue;
			}
			ApplicationInfo info = new ApplicationInfo();
			info.name = name;
			info.liveStreamCount = getRootScope().getScope(name).getBasicScopeNames(ScopeType.BROADCAST).size();
			info.vodCount = getVoDCount(name);
			info.storage = getStorage(name);
			appsInfo.add(info);
		}

		return appsInfo;
	}

	private long getStorage(String name) {
		File appFolder = new File("webapps/"+name);
		return FileUtils.sizeOfDirectory(appFolder);
	}

	private int getVoDCount(String appName) {


		IScope root = getRootScope();
		java.util.Set<String> names = root.getScopeNames();
		int size = 0;
		for (String name : names) {

			IScope scope = root.getScope(name);

			if (scope != null && appName.equals(scope.getName())){
				//logger.info("name of the scope:{} ", scope.getName() );

				Object adapter = scope.getContext().getApplicationContext().getBean(AntMediaApplicationAdapter.BEAN_NAME);
				if (adapter instanceof AntMediaApplicationAdapter) 
				{
					IDataStore dataStore = ((AntMediaApplicationAdapter)adapter).getDataStore();
					if (dataStore != null) {
						size =  (int) dataStore.getTotalVodNumber();
					}
				}
			}


		}

		return size;
	}





	public List<BroadcastInfo> getAppLiveStreams(String name) {
		IScope root = getRootScope();
		IScope appScope = root.getScope(name);

		List<BroadcastInfo> broadcastInfoList = new ArrayList<>();
		Set<String> basicScopeNames = appScope.getBasicScopeNames(ScopeType.BROADCAST);
		for (String scopeName : basicScopeNames) {
			IBroadcastScope broadcastScope = appScope.getBroadcastScope(scopeName);
			BroadcastInfo info = new BroadcastInfo(broadcastScope.getName(), broadcastScope.getConsumers().size());
			broadcastInfoList.add(info);
		}
		return broadcastInfoList;
	}


	public boolean deleteVoDStream(String appname, String streamName) {
		File vodStream = new File("webapps/"+appname+"/streams/"+ streamName);
		boolean result = false;
		if (vodStream.exists()) {
			try {
				Files.delete(vodStream.toPath());
				result = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public List<String> getApplications() {
		IScope root = getRootScope();

		java.util.Set<String> names = root.getScopeNames();
		List<String> apps = new ArrayList<String>();
		for (String name : names) {
			apps.add(name);
		}
		return apps;
	}

	public int getTotalConnectionSize(){
		IScope root = getRootScope();
		return root.getStatistics().getActiveClients();
	}

	public HashMap<Integer, String> getConnections(String scopeName) {
		HashMap<Integer, String> connections = new HashMap<Integer, String>();
		IScope root = getScope(scopeName);
		if (root != null) {
			Set<IClient> clients = root.getClients();
			Iterator<IClient> client = clients.iterator();
			int id = 0;
			while (client.hasNext()) {
				IClient c = client.next();
				String user = c.getId();
				connections.put(id, user);
				id++;
			}
		}
		return connections;
	}

	public void updateAppSettings(String scopeName, AppSettingsModel settingsModel) {
		ApplicationContext applicationContext = getScope(scopeName).getContext().getApplicationContext();
		if (applicationContext.containsBean(AppSettings.BEAN_NAME)) {
			AppSettings appSettings = (AppSettings) applicationContext.getBean(AppSettings.BEAN_NAME);

			appSettings.setMp4MuxingEnabled(settingsModel.isMp4MuxingEnabled());
			appSettings.setAddDateTimeToMp4FileName(settingsModel.isAddDateTimeToMp4FileName());
			appSettings.setHlsMuxingEnabled(settingsModel.isHlsMuxingEnabled());
			appSettings.setObjectDetectionEnabled(settingsModel.isObjectDetectionEnabled());
			appSettings.setHlsListSize(String.valueOf(settingsModel.getHlsListSize()));
			appSettings.setHlsTime(String.valueOf(settingsModel.getHlsTime()));
			appSettings.setHlsPlayListType(settingsModel.getHlsPlayListType());
			appSettings.setAcceptOnlyStreamsInDataStore(settingsModel.isAcceptOnlyStreamsInDataStore());


			appSettings.setAdaptiveResolutionList(settingsModel.getEncoderSettings());

			String oldVodFolder = appSettings.getVodFolder();

			appSettings.setVodFolder(settingsModel.getVodFolder());
			appSettings.setPreviewOverwrite(settingsModel.isPreviewOverwrite());

			AntMediaApplicationAdapter bean = (AntMediaApplicationAdapter) applicationContext.getBean("web.handler");

			bean.synchUserVoDFolder(oldVodFolder, settingsModel.getVodFolder());

			log.warn("app settings updated");	
		}
		else {
			log.warn("App has no app.settings bean");
		}
		if (applicationContext.containsBean(AcceptOnlyStreamsInDataStore.BEAN_NAME)) {
			AcceptOnlyStreamsInDataStore securityHandler = (AcceptOnlyStreamsInDataStore) applicationContext.getBean(AcceptOnlyStreamsInDataStore.BEAN_NAME);
			securityHandler.setEnabled(settingsModel.isAcceptOnlyStreamsInDataStore());
		}
	}


	public void updateServerSettings( ServerSettings settings) {
		serverSettings = getServerSettings();

		//	serverSettings.setServerName(settings.getServerName());
		serverSettings.setLicenceKey(settings.getLicenceKey());

		log.warn(" settings updated");	

	}

	private IScope getScope(String scopeName) {
		IScope root = ScopeUtils.findRoot(scope);
		return getScopes(root, scopeName);
	}

	@Nullable
	private ApplicationContext getAppContext() {
		if (servletContext != null) {
			appCtx = (ApplicationContext) servletContext
					.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		}
		return appCtx;
	}

	public ServerSettings getServerSettings() {

		WebApplicationContext ctxt = WebApplicationContextUtils.getWebApplicationContext(servletContext); 
		serverSettings = (ServerSettings)ctxt.getBean(ServerSettings.BEAN_NAME);

		return serverSettings;
	}


	/**
	 * Search through all the scopes in the given scope to a scope with the
	 * given name
	 * 
	 * @param root
	 * @param scopeName
	 * @return IScope the requested scope
	 */
	private IScope getScopes(IScope root, String scopeName) {
		// log.info("Found scope "+root.getName());
		if (root.getName().equals(scopeName)) {
			return root;
		} else {
			if (root instanceof IScope) {
				Set<String> names = root.getScopeNames();
				for (String name : names) {
					try {
						IScope parent = root.getScope(name);
						IScope scope = getScopes(parent, scopeName);
						if (scope != null) {
							return scope;
						}
					} catch (NullPointerException npe) {
						log.debug(npe.toString());
					}
				}
			}
		}
		return null;
	}

}
