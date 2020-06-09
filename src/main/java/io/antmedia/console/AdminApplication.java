package io.antmedia.console;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.apache.commons.io.FileUtils;
import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.IContext;
import org.red5.server.api.scope.IBroadcastScope;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.scope.ScopeType;
import org.red5.server.util.ScopeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import io.antmedia.AntMediaApplicationAdapter;
import io.antmedia.IApplicationAdaptorFactory;
import io.antmedia.console.datastore.DataStoreFactory;
import io.antmedia.datastore.db.DataStore;
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

	private static final Logger log = LoggerFactory.getLogger(AdminApplication.class);


	public static final String APP_NAME = "ConsoleApp";
	private DataStoreFactory dataStoreFactory;

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


	public boolean appStart(IScope app) {
		return super.appStart(app);
	}

	/** {@inheritDoc} */
	@Override
	public boolean connect(IConnection conn, IScope scope, Object[] params) {
		this.scope = scope;
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public void disconnect(IConnection conn, IScope scope) {

		super.disconnect(conn, scope);
	}

	public IScope getRootScope() {
		if (rootScope == null) {
			rootScope = ScopeUtils.findRoot(scope);
		}
		return rootScope;
	}

	public int getTotalLiveStreamSize() 
	{
		List<String> appNames = getApplications();
		int size = 0;
		for (String name : appNames) {
			IScope scope = getRootScope().getScope(name);
			size += getAppLiveStreamCount(scope);
		}
		return size;
	}

	public List<ApplicationInfo> getApplicationInfo() {
		List<String> appNames = getApplications();
		List<ApplicationInfo> appsInfo = new ArrayList<>();
		for (String name : appNames) {
			if (name.equals(APP_NAME)) {
				continue;
			}
			ApplicationInfo info = new ApplicationInfo();
			info.name = name;
			info.liveStreamCount = getAppLiveStreamCount(getRootScope().getScope(name));
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

				Object adapter = scope.getContext().getApplicationContext().getBean(AntMediaApplicationAdapter.BEAN_NAME);
				if (adapter instanceof AntMediaApplicationAdapter) 
				{
					DataStore dataStore = ((AntMediaApplicationAdapter)adapter).getDataStore();
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

	public ApplicationContext getApplicationContext(String scopeName) {
		IScope scope = getScope(scopeName);
		if (scope != null) {
			IContext context = scope.getContext();
			if (context != null) {
				return context.getApplicationContext();
			}
		}
		log.warn("Application:{} is not initilized", scopeName);
		return null;
	}


	public void updateServerSettings( ServerSettings settings) {
		serverSettings = getServerSettings();
		serverSettings.setLicenceKey(settings.getLicenceKey());
		log.info(" Server License Key Updated");	
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

	public DataStoreFactory getDataStoreFactory() {
		return dataStoreFactory;
	}

	public void setDataStoreFactory(DataStoreFactory dataStoreFactory) {
		this.dataStoreFactory = dataStoreFactory;
	}

	public int getAppLiveStreamCount(IScope appScope) {
		int size = 0;
		if (appScope != null) {
			Object adapter = ((IApplicationAdaptorFactory) appScope.getContext().getApplicationContext().getBean(AntMediaApplicationAdapter.BEAN_NAME)).getAppAdaptor();
			if (adapter instanceof AntMediaApplicationAdapter) 
			{
				DataStore dataStore = ((AntMediaApplicationAdapter)adapter).getDataStore();
				if (dataStore != null) {
					size =  (int) dataStore.getActiveBroadcastCount();
				}
			}
		}
		return size;
	}
}
