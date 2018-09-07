package io.antmedia.console.datastore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.antmedia.cluster.ClusterNode;
import io.antmedia.rest.model.User;
import kotlin.jvm.functions.Function1;


public class MapDBStore implements IDataStore {

	private DB db;
	private HTreeMap<String, String> userMap;
	private HTreeMap<String, String> nodeMap;
	private Gson gson;
	
	protected static Logger logger = LoggerFactory.getLogger(MapDBStore.class);

	public MapDBStore() {
		db = DBMaker.fileDB(SERVER_STORAGE_FILE).transactionEnable().make();
		userMap = db.hashMap(SERVER_STORAGE_MAP_NAME)
				.keySerializer(Serializer.STRING)
				.valueSerializer(Serializer.STRING)
				.counterEnable()
				.createOrOpen();
		nodeMap = db.hashMap(CLUSTER_STORAGE_MAP_NAME)
				.keySerializer(Serializer.STRING)
				.valueSerializer(Serializer.STRING)
				.counterEnable()
				.createOrOpen();
		gson = new Gson();
	}


	public boolean addUser(String username, String password, Integer userType) {

		boolean result = false;
		if (username != null && password != null && userType != null && (userType == 0  || userType == 1)) {
			try {
				if (!userMap.containsKey(username)) 
				{
					User user = new User(username, password, userType);
					userMap.put(username, gson.toJson(user));
					db.commit();
					result = true;
				}
				else {
					logger.warn("user with " + username + " already exist");
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				result = false;
			}
		}
		return result;
	}

	public boolean editUser(String username, String password, Integer userType) {
		boolean result = false;
		if (username != null && password != null && userType != null && (userType == 0 || userType == 1))  {
			try {
				if (userMap.containsKey(username)) {
					User user = new User(username, password, userType);
					userMap.put(username, gson.toJson(user));
					db.commit();
					result = true;
				}
			}
			catch (Exception e) {
				result = false;
			}
		}
		return result;
	}


	public boolean deleteUser(String username) {
		boolean result = false;
		if (username != null) {
			try {
				if (userMap.containsKey(username)) {
					userMap.remove(username);
					db.commit();
					result = true;
				}
			}
			catch (Exception e) {
				result = false;
			}
		}
		return result;
	}
	
	public boolean doesUsernameExist(String username) {
		return userMap.containsKey(username);
	}

	public boolean doesUserExist(String username, String password) {
		boolean result = false;
		if (username != null && password != null) {
			try {
				if (userMap.containsKey(username)) {
					String value = userMap.get(username);
					User user = gson.fromJson(value, User.class);
					if (user.password.equals(password)) {
						result = true;
					}
				}
			}
			catch (Exception e) {
				result = false;
				e.printStackTrace();
			}
		}
		return result;
	}

	public User getUser(String username) 
	{
		if (username != null)  {
			try {
				if (userMap.containsKey(username)) {
					String value = userMap.get(username);
					return gson.fromJson(value, User.class);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}


	public void clear() {
		userMap.clear();
		db.commit();
	}

	public void close() {
		db.close();
	}
	
	public int getNumberOfUserRecords() {
		return userMap.size();
	}


	public List<ClusterNode> getClusterNodes() {
		ArrayList<ClusterNode> list = new ArrayList<>();
		nodeMap.forEach((k,v)->list.add(gson.fromJson(v, ClusterNode.class)));
		return list;
	}


	public ClusterNode getClusterNode(String nodeId) {
		ClusterNode node = null;
		if (nodeMap.containsKey(nodeId)) {
			node = gson.fromJson(nodeMap.get(nodeId), ClusterNode.class);
		}
		return node;
	}


	public boolean addNode(ClusterNode node) {
		nodeMap.put(node.getId(), gson.toJson(node));
		db.commit();
		return true;
	}


	public boolean updateNode(String nodeId, ClusterNode node) {
		if (nodeMap.containsKey(nodeId)) {
			nodeMap.put(nodeId, gson.toJson(node));
			db.commit();
			return true;
		}
		
		return false;
	}


	public boolean deleteNode(String nodeId) {
		if (nodeMap.containsKey(nodeId)) {
			nodeMap.remove(nodeId);
			db.commit();
			return true;
		}
		
		return false;
	}

	@Override
	public boolean registerAsNode() {
		return true;
	}

}
