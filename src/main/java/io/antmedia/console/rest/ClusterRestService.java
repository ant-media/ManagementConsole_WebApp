package io.antmedia.console.rest;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.red5.server.api.scope.IScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import io.antmedia.AntMediaApplicationAdapter;
import io.antmedia.console.DataStore;
import io.antmedia.rest.model.Result;

@Component
@Path("/cluster")
public class ClusterRestService {

	@Context
	private ServletContext servletContext;

	private ApplicationContext appCtx;

	private IScope scope;

	private AntMediaApplicationAdapter appInstance;

	protected static final Logger logger = LoggerFactory.getLogger(ClusterRestService.class);
	
	private DataStore dataStore;
	
	@GET
	@Path("/nodes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ClusterNode> getNodeList() {
		
		return getDataStore().getClusterNodes();
	}
	
	@GET
	@Path("/nodes/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ClusterNode getNode(@PathParam("id") String nodeId) {
		return getDataStore().getClusterNode(nodeId);
	}
	
	@POST
	@Path("/nodes")
	@Produces(MediaType.APPLICATION_JSON)
	public Result addNode(ClusterNode node) {
		boolean ret = getDataStore().addNode(node);
		Result result=new Result(ret);
		
		return result;
	}
	
	@POST
	@Path("/updateNode/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Result updateNode(ClusterNode node, @PathParam("id") String nodeId) {
		boolean ret = getDataStore().updateNode(nodeId, node);
		Result result=new Result(ret);
		
		return result;
	}
	
	@GET
	@Path("/deleteNode/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Result deleteNode(@PathParam("id") String nodeId) {
		boolean ret = getDataStore().deleteNode(nodeId);
		Result result=new Result(ret);
		
		return result;
	}



	@Nullable
	private ApplicationContext getAppContext() {
		if (servletContext != null) {
			appCtx = (ApplicationContext) servletContext
					.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		}
		return appCtx;
	}

	public AntMediaApplicationAdapter getInstance() {
		if (appInstance == null) {
			appInstance = (AntMediaApplicationAdapter) getAppContext().getBean("web.handler");
		}
		return appInstance;
	}

	public IScope getScope() {
		if (scope == null) {
			scope = getInstance().getScope();
		}
		return scope;
	}

	public DataStore getDataStore() {
		if (dataStore == null) {
			WebApplicationContext ctxt = WebApplicationContextUtils.getWebApplicationContext(servletContext); 
			dataStore = (DataStore)ctxt.getBean("dataStore");
		}
		return dataStore;
	}
}
