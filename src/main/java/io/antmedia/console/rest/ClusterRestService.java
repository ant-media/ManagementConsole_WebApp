package io.antmedia.console.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.antmedia.cluster.ClusterNode;
import io.antmedia.cluster.DBReader;
import io.antmedia.rest.model.Result;

@Component
@Path("/cluster")
public class ClusterRestService {
	protected static Logger logger = LoggerFactory.getLogger(ClusterRestService.class);
	
	@GET
	@Path("/nodes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ClusterNode> getNodeList() {
		
		return DBReader.instance.getClusterStore().getClusterNodes();
	}
	
	@GET
	@Path("/nodes/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ClusterNode getNode(@PathParam("id") String nodeId) {
		return DBReader.instance.getClusterStore().getClusterNode(nodeId);
	}
	
	@POST
	@Path("/nodes")
	@Produces(MediaType.APPLICATION_JSON)
	public Result addNode(ClusterNode node) {
		boolean ret = DBReader.instance.getClusterStore().addNode(node);
		Result result=new Result(ret);
		
		return result;
	}
	
	@POST
	@Path("/updateNode/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Result updateNode(ClusterNode node, @PathParam("id") String nodeId) {
		boolean ret = DBReader.instance.getClusterStore().updateNode(nodeId, node);
		Result result=new Result(ret);
		
		return result;
	}
	
	@GET
	@Path("/deleteNode/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Result deleteNode(@PathParam("id") String nodeId) {
		boolean ret = DBReader.instance.getClusterStore().deleteNode(nodeId);
		Result result=new Result(ret);
		
		return result;
	}
}
