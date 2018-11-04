package io.antmedia.console.datastore;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;

import io.antmedia.cluster.ClusterNode;
import io.antmedia.console.SystemUtils;
import io.antmedia.datastore.DBUtils;
import io.antmedia.datastore.db.types.Broadcast;
import io.antmedia.rest.model.User;
import io.antmedia.rest.model.UserType;;

public class MongoStore implements IDataStore {

	private Morphia morphia;
	private Datastore datastore, clusterDatastore;

	protected static Logger logger = LoggerFactory.getLogger(MongoStore.class);

	public MongoStore(String dbHost, String dbUser, String dbPassword) {
		String dbName = SERVER_STORAGE_MAP_NAME;

		morphia = new Morphia();
		//morphia.map(io.antmedia.rest.model.ClusterNode.class);
		//morphia.map(io.antmedia.rest.model.User.class);
		
		List<MongoCredential> credentialList = new ArrayList<MongoCredential>();
		credentialList.add(MongoCredential.createCredential(dbUser, dbName, dbPassword.toCharArray()));
		//datastore = morphia.createDatastore(new MongoClient(new ServerAddress(dbHost), credentialList), dbName);
		//clusterDatastore = morphia.createDatastore(new MongoClient(new ServerAddress(dbHost), credentialList), CLUSTER_STORAGE_MAP_NAME);
		datastore = morphia.createDatastore(new MongoClient(dbHost), dbName);
		clusterDatastore = morphia.createDatastore(new MongoClient(dbHost), CLUSTER_STORAGE_MAP_NAME);
		datastore.ensureIndexes();
		clusterDatastore.ensureIndexes();
	}

	@Override
	public boolean addUser(String username, String password, UserType userType) {
		boolean result = false;

		if (username != null && password != null && userType != null) {
			User existingUser = datastore.find(User.class).field("email").equal(username).get();
			if (existingUser == null) 
			{
				User user = new User(username, password, userType);
				datastore.save(user);
				result = true;
			}
			else {
				logger.warn("user with " + username + " already exist");
			}
		}
		return result;
	}

	@Override
	public boolean editUser(String username, String password, UserType userType) {
		try {
			Query<User> query = datastore.createQuery(User.class).field("email").equal(username);
			UpdateOperations<User> ops = datastore.createUpdateOperations(User.class).set("email", username)
					.set("password", password).set("userType", userType);

			UpdateResults update = datastore.update(query, ops);

			return update.getUpdatedCount() == 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean deleteUser(String username) {
		try {
			Query<User> query = datastore.createQuery(User.class).field("email").equal(username);
			WriteResult delete = datastore.delete(query);
			return delete.getN() == 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean doesUsernameExist(String username) {
		User existingUser = datastore.find(User.class).field("email").equal(username).get();

		return existingUser != null;
	}

	@Override
	public boolean doesUserExist(String username, String password) {
		User existingUser = datastore.find(User.class).field("email").equal(username).get();
		if(existingUser != null)
		{
			return existingUser.getPassword().contentEquals(password);
		}
		return false;
	}

	@Override
	public User getUser(String username) {
		return datastore.find(User.class).field("email").equal(username).get();
	}

	@Override
	public void clear() {
		datastore.delete(datastore.createQuery(User.class));
	}

	@Override
	public void close() {
		datastore.getMongo().close();
	}

	@Override
	public int getNumberOfUserRecords() {
		return (int) datastore.getCount(User.class);
	}

	@Override
	public List<ClusterNode> getClusterNodes() {
		return clusterDatastore.find(ClusterNode.class).asList();
	}

	@Override
	public ClusterNode getClusterNode(String nodeId) {
		return clusterDatastore.find(ClusterNode.class).field("id").equal(nodeId).get();
	}

	@Override
	public boolean addNode(ClusterNode node) {
		clusterDatastore.save(node);
		return true;
	}

	@Override
	public boolean updateNode(String nodeId, ClusterNode node) {
		try {
			Query<ClusterNode> query = clusterDatastore.createQuery(ClusterNode.class).field("id").equal(nodeId);
			UpdateOperations<ClusterNode> ops = clusterDatastore.createUpdateOperations(ClusterNode.class).set("status", "alive");
			UpdateResults update = clusterDatastore.update(query, ops);
			return update.getUpdatedCount() == 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;	
	}

	@Override
	public boolean deleteNode(String nodeId) {
		try {
			Query<ClusterNode> query = clusterDatastore.createQuery(ClusterNode.class).field("id").equal(nodeId);
			WriteResult delete = clusterDatastore.delete(query);
			return delete.getN() == 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean registerAsNode() {
		ClusterNode node = new ClusterNode(DBUtils.getHostAddress());
		ClusterNode existingNode = clusterDatastore.find(ClusterNode.class).field("id").equal(node.getId()).get();
		if(existingNode != null) {
			return updateNode(node.getId(), node);
		}		
		else {
			return addNode(node);
		}
	}

}
