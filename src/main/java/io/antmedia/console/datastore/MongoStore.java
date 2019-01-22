package io.antmedia.console.datastore;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteResult;

import io.antmedia.datastore.DBUtils;
import io.antmedia.rest.model.User;
import io.antmedia.rest.model.UserType;;

public class MongoStore implements IDataStore {

	private Morphia morphia;
	private Datastore datastore;

	protected static Logger logger = LoggerFactory.getLogger(MongoStore.class);

	public MongoStore(String dbHost, String dbUser, String dbPassword) {
		String dbName = SERVER_STORAGE_MAP_NAME;

		String uri = DBUtils.getUri(dbHost, dbUser, dbPassword);

		MongoClientURI mongoUri = new MongoClientURI(uri);
		MongoClient client = new MongoClient(mongoUri);
		
		morphia = new Morphia();
  		
		datastore = morphia.createDatastore(client, dbName);
		datastore.ensureIndexes();
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
}
