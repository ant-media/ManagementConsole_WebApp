package io.antmedia.console.datastore;

import java.util.ArrayList;
import java.util.List;

import io.antmedia.cluster.ClusterNode;
import io.antmedia.rest.model.User;
import io.antmedia.rest.model.UserType;


public interface IDataStore {
	
	public final static String SERVER_STORAGE_FILE = "server.db";
	public final static String SERVER_STORAGE_MAP_NAME = "serverdb";
	public final static String CLUSTER_STORAGE_MAP_NAME = "clusterdb";

	public boolean addUser(String username, String password, UserType userType);

	public boolean editUser(String username, String password, UserType userType);

	public boolean deleteUser(String username);
	
	public boolean doesUsernameExist(String username);

	public boolean doesUserExist(String username, String password);

	public User getUser(String username);

	public void clear();

	public void close();
	
	public int getNumberOfUserRecords();

	public List<ClusterNode> getClusterNodes();

	public ClusterNode getClusterNode(String nodeId);

	public boolean addNode(ClusterNode node);

	public boolean updateNode(String nodeId, ClusterNode node);

	public boolean deleteNode(String nodeId);

	public boolean registerAsNode();

}