package dataset.BerkeleyDB;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.sleepycat.bind.EntityBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.SerialSerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

import aqpeq.utilities.Dummy.DummyProperties;
import graphInfra.NodeInfra;

public class BerkleleyDB {

	public static Environment environment;
	private static Database database;
	private static Database catalogDb;
	private static StoredClassCatalog catalog;

	// data structure
	//
	private static SerialBinding<String> keyBinding;
	private static SerialBinding<MyBDBObject> dataBinding;
	private static EntityBinding<MySerialSerialBinding> entityBinding;

	// no properties
	private static SerialBinding<NodeInfoObject> dataBindingNodeInfo;
	private static SerialBinding<String> keyBindingNodeInfo;
	private static EntityBinding<MySerialSerialBinding> entityBindingNodeInfo;

	private static SerialBinding<EdgeInfoObject> dataBindingEdgeInfo;
	private static SerialBinding<String> keyBindingEdgeInfo;
	private static EntityBinding<MySerialSerialBinding> entityBindingEdgeInfo;

	// properties
	private static SerialBinding<NodeInfoWithProObject> dataBindingNodeInfoWithPro;
	private static SerialBinding<String> keyBindingNodeInfoWithPro;
	private static EntityBinding<MySerialSerialBinding> entityBindingNodeInfoWithPro;

	private static SerialBinding<LabelNeighborObject> dataBindingLabelNeighbor;
	private static SerialBinding<String> keyBindingLabelNeighbor;
	private static EntityBinding<MySerialSerialBinding> entityBindingLabelNeighbor;

	public BerkleleyDB(String dbName, String catDBName, String envFilePath) throws Exception, DatabaseException {
		if (DummyProperties.withProperties) {
			BerkleleyDB.InitializeWithPro(dbName, catDBName, envFilePath);
		} else {
			BerkleleyDB.InitializeNoPro(dbName, catDBName, envFilePath);
		}
	}

	public static void InitializeNoPro(String dbName, String catDBName, String envFilePath) throws Exception {
		/* Create a new, transactional database environment */
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setTransactional(true);
		envConfig.setAllowCreate(true);
		environment = new Environment(new File(envFilePath), envConfig);

		/* Make a database within that environment */
		Transaction txn = environment.beginTransaction(null, null);
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setTransactional(true);
		dbConfig.setAllowCreate(true);
		dbConfig.setSortedDuplicates(true);
		database = environment.openDatabase(txn, dbName, dbConfig);

		/*
		 * A class catalog database is needed for storing class descriptions for
		 * the serial binding used below. This avoids storing class descriptions
		 * redundantly in each record.
		 */
		DatabaseConfig catalogConfig = new DatabaseConfig();
		catalogConfig.setTransactional(true);
		catalogConfig.setAllowCreate(true);
		catalogDb = environment.openDatabase(txn, catDBName, catalogConfig);
		catalog = new StoredClassCatalog(catalogDb);

		keyBinding = new SerialBinding<String>(catalog, String.class);
		dataBinding = new SerialBinding<MyBDBObject>(catalog, MyBDBObject.class);
		entityBinding = new MySerialSerialBinding(keyBinding, dataBinding);

		dataBindingNodeInfo = new SerialBinding<NodeInfoObject>(catalog, NodeInfoObject.class);
		keyBindingNodeInfo = new SerialBinding<String>(catalog, String.class);
		entityBindingNodeInfo = new MySerialSerialBinding(keyBindingNodeInfo, dataBindingNodeInfo);
		dataBindingEdgeInfo = new SerialBinding<EdgeInfoObject>(catalog, EdgeInfoObject.class);
		keyBindingEdgeInfo = new SerialBinding<String>(catalog, String.class);
		entityBindingEdgeInfo = new MySerialSerialBinding(keyBindingEdgeInfo, dataBindingEdgeInfo);

		dataBindingLabelNeighbor = new SerialBinding<LabelNeighborObject>(catalog, LabelNeighborObject.class);
		keyBindingLabelNeighbor = new SerialBinding<String>(catalog, String.class);
		entityBindingLabelNeighbor = new MySerialSerialBinding(keyBindingLabelNeighbor, dataBindingLabelNeighbor);

		txn.commit();
	}

	public static void InitializeWithPro(String dbName, String catDBName, String envFilePath) throws Exception {
		/* Create a new, transactional database environment */
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setTransactional(true);
		envConfig.setAllowCreate(true);
		environment = new Environment(new File(envFilePath), envConfig);

		/* Make a database within that environment */
		Transaction txn = environment.beginTransaction(null, null);
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setTransactional(true);
		dbConfig.setAllowCreate(true);
		dbConfig.setSortedDuplicates(true);
		database = environment.openDatabase(txn, dbName, dbConfig);

		/*
		 * A class catalog database is needed for storing class descriptions for
		 * the serial binding used below. This avoids storing class descriptions
		 * redundantly in each record.
		 */
		DatabaseConfig catalogConfig = new DatabaseConfig();
		catalogConfig.setTransactional(true);
		catalogConfig.setAllowCreate(true);
		catalogDb = environment.openDatabase(txn, catDBName, catalogConfig);
		catalog = new StoredClassCatalog(catalogDb);

		keyBinding = new SerialBinding<String>(catalog, String.class);
		dataBinding = new SerialBinding<MyBDBObject>(catalog, MyBDBObject.class);
		entityBinding = new MySerialSerialBinding(keyBinding, dataBinding);

		dataBindingNodeInfoWithPro = new SerialBinding<NodeInfoWithProObject>(catalog, NodeInfoWithProObject.class);
		keyBindingNodeInfoWithPro = new SerialBinding<String>(catalog, String.class);
		entityBindingNodeInfoWithPro = new MySerialSerialBinding(keyBindingNodeInfoWithPro, dataBindingNodeInfoWithPro);

		dataBindingEdgeInfo = new SerialBinding<EdgeInfoObject>(catalog, EdgeInfoObject.class);
		keyBindingEdgeInfo = new SerialBinding<String>(catalog, String.class);
		entityBindingEdgeInfo = new MySerialSerialBinding(keyBindingEdgeInfo, dataBindingEdgeInfo);

		dataBindingLabelNeighbor = new SerialBinding<LabelNeighborObject>(catalog, LabelNeighborObject.class);
		keyBindingLabelNeighbor = new SerialBinding<String>(catalog, String.class);
		entityBindingLabelNeighbor = new MySerialSerialBinding(keyBindingLabelNeighbor, dataBindingLabelNeighbor);

		txn.commit();
	}

	public void InsertTransformation(Transaction txn, String key, MyBDBObject object) throws Exception {
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();

		keyBinding.objectToEntry(BerkleleyDB.clean(key), keyEntry);
		dataBinding.objectToEntry(object, dataEntry);

		database.put(txn, keyEntry, dataEntry);

	}

	public void InsertNodeInfo(Transaction txn, int key, NodeInfoObject object) throws Exception {
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();

		String keyN = "n#" + String.valueOf(key);
		keyBindingNodeInfo.objectToEntry(keyN, keyEntry);
		dataBindingNodeInfo.objectToEntry(object, dataEntry);

		database.put(txn, keyEntry, dataEntry);

	}

	public void InsertEdgeInfo(Transaction txn, int key, EdgeInfoObject object) throws Exception {
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();

		String keyE = "e#" + String.valueOf(key);
		keyBindingEdgeInfo.objectToEntry(keyE, keyEntry);
		dataBindingEdgeInfo.objectToEntry(object, dataEntry);

		database.put(txn, keyEntry, dataEntry);

	}

	public void InsertNodeInfoWithPro(Transaction txn, int key, NodeInfoWithProObject object) throws Exception {
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();

		String keyN = "np#" + String.valueOf(key);
		keyBindingNodeInfoWithPro.objectToEntry(keyN, keyEntry);
		dataBindingNodeInfoWithPro.objectToEntry(object, dataEntry);

		database.put(txn, keyEntry, dataEntry);

	}

	public void InsertLabelNeighbor(Transaction txn, String key, LabelNeighborObject object) throws Exception {
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();

		keyBindingLabelNeighbor.objectToEntry(key.toUpperCase(), keyEntry);
		dataBindingLabelNeighbor.objectToEntry(object, dataEntry);

		database.put(txn, keyEntry, dataEntry);

	}

	public HashSet<Integer> SearchNodeIdsByToken(String key) throws Exception {
		Transaction txn = environment.beginTransaction(null, null);
		DatabaseEntry KeyEntry = new DatabaseEntry();
		DatabaseEntry DataEntry = new DatabaseEntry();
		keyBinding.objectToEntry(BerkleleyDB.clean(key), KeyEntry);
		OperationStatus status = database.get(txn, KeyEntry, DataEntry, LockMode.DEFAULT);
		txn.commit();

		MyBDBObject object;
		if (status == OperationStatus.SUCCESS) {
			object = dataBinding.entryToObject(DataEntry);
		} else {
			System.out.println("OperationStatus is unSUCCESS");
			return null;
		}

		return object.nodeId;
	}

	public HashSet<Integer> SearchNodeIdsByKeyword(ArrayList<String> keyword) throws Exception {
		HashSet<Integer> result = new HashSet<Integer>();
		for (String token : keyword) {
			ArrayList<Integer> nodeId = new ArrayList<Integer>();
			Transaction txn = environment.beginTransaction(null, null);
			DatabaseEntry KeyEntry = new DatabaseEntry();
			DatabaseEntry DataEntry = new DatabaseEntry();
			keyBinding.objectToEntry(BerkleleyDB.clean(token), KeyEntry);
			OperationStatus status = database.get(txn, KeyEntry, DataEntry, LockMode.DEFAULT);
			txn.commit();

			MyBDBObject object;
			if (status == OperationStatus.SUCCESS) {
				object = dataBinding.entryToObject(DataEntry);
				ArrayList<String> synonmyList = object.synonym;
				HashSet<Integer> synonymNodeId = new HashSet<Integer>();
				HashSet<Integer> temResult = new HashSet<Integer>();
				if (!synonmyList.isEmpty()) {
					for (String synonym : synonmyList) {
						Transaction txnS = environment.beginTransaction(null, null);
						DatabaseEntry KeyEntryS = new DatabaseEntry();
						DatabaseEntry DataEntryS = new DatabaseEntry();
						keyBinding.objectToEntry(BerkleleyDB.clean(synonym), KeyEntryS);
						OperationStatus statusS = database.get(txnS, KeyEntryS, DataEntryS, LockMode.DEFAULT);
						txnS.commit();

						MyBDBObject objectS;
						if (statusS == OperationStatus.SUCCESS) {
							objectS = dataBinding.entryToObject(DataEntryS);
							HashSet<Integer> tem = objectS.nodeId;
							synonymNodeId.addAll(tem);
						} else {
							continue;
						}
					}
					temResult = synonymNodeId;
					temResult.addAll(object.nodeId);
				} else {
					// if token doesn't have synonym
					temResult = object.nodeId;
				}
				if (result.isEmpty()) {
					result = temResult;
				} else {
					result.retainAll(temResult);
				}
			} else {

				System.err.println("OperationStatus is unSUCCESS");
				return null;
			}

		}

		return result;

	}

	public NodeInfra SearchNodeInfoWithPro(int key) throws Exception {
		NodeInfra node = new NodeInfra(key);
		if (DummyProperties.withProperties) {
			Transaction txn = environment.beginTransaction(null, null);
			DatabaseEntry KeyEntry = new DatabaseEntry();
			DatabaseEntry DataEntry = new DatabaseEntry();
			String keyN = "np#" + String.valueOf(key);
			keyBindingNodeInfoWithPro.objectToEntry(keyN, KeyEntry);
			OperationStatus status = database.get(txn, KeyEntry, DataEntry, LockMode.DEFAULT);
			txn.commit();

			NodeInfoWithProObject object;
			if (status == OperationStatus.SUCCESS) {
				object = dataBindingNodeInfoWithPro.entryToObject(DataEntry);
			} else {
				System.out.println("OperationStatus is unSUCCESS");
				return null;
			}

			node.labels = object.label;
			node.setProperties(object.properties);
		} else {
			Transaction txn = environment.beginTransaction(null, null);
			DatabaseEntry KeyEntry = new DatabaseEntry();
			DatabaseEntry DataEntry = new DatabaseEntry();
			String keyN = "n#" + String.valueOf(key);
			keyBindingNodeInfo.objectToEntry(keyN, KeyEntry);
			OperationStatus status = database.get(txn, KeyEntry, DataEntry, LockMode.DEFAULT);
			txn.commit();

			NodeInfoObject object;
			if (status == OperationStatus.SUCCESS) {
				object = dataBindingNodeInfo.entryToObject(DataEntry);
			} else {
				System.out.println("OperationStatus is unSUCCESS");
				return null;
			}
			node.labels = object.label;
		}
		return node;
	}
	
//	public HashSet<String> SearchNodeInfoByNodeId(int key) throws Exception {
//		Transaction txn = environment.beginTransaction(null, null);
//		DatabaseEntry KeyEntry = new DatabaseEntry();
//		DatabaseEntry DataEntry = new DatabaseEntry();
//		
//		String keyN = "n" + String.valueOf(key);
//		keyBindingNodeInfo.objectToEntry(keyN, KeyEntry);
//		OperationStatus status = database.get(txn, KeyEntry, DataEntry, LockMode.DEFAULT);
//		txn.commit();
//
//		NodeInfoObject object;
//		if (status == OperationStatus.SUCCESS) {
//			object = dataBindingNodeInfo.entryToObject(DataEntry);
//		} else {
//			System.out.println("OperationStatus is unSUCCESS");
//			return null;
//		}
//		return object.label;
//	}
	
	public NodeInfra SearchNodeInfoWithPropByNodeId(int key) throws Exception {
		NodeInfra node = new NodeInfra(key);
		Transaction txn = environment.beginTransaction(null, null);
		DatabaseEntry KeyEntry = new DatabaseEntry();
		DatabaseEntry DataEntry = new DatabaseEntry();
		
		String keyN = "np#" + String.valueOf(key);
		keyBindingNodeInfoWithPro.objectToEntry(keyN, KeyEntry);
		OperationStatus status = database.get(txn, KeyEntry, DataEntry, LockMode.DEFAULT);
		txn.commit();

		NodeInfoWithProObject object;
		if (status == OperationStatus.SUCCESS) {
			object = dataBindingNodeInfoWithPro.entryToObject(DataEntry);
		} else {
			System.out.println("OperationStatus is unSUCCESS");
			return null;
		}
		node.labels = object.label;
		node.setProperties(object.properties);
		return node;
	}

	public HashSet<String> SearchEdgeTypeByRelId(int key) throws Exception {
		Transaction txn = environment.beginTransaction(null, null);
		DatabaseEntry KeyEntry = new DatabaseEntry();
		DatabaseEntry DataEntry = new DatabaseEntry();
		String keyE = "e#" + String.valueOf(key);
		keyBindingEdgeInfo.objectToEntry(keyE, KeyEntry);
		OperationStatus status = database.get(txn, KeyEntry, DataEntry, LockMode.DEFAULT);
		txn.commit();

		EdgeInfoObject object;
		if (status == OperationStatus.SUCCESS) {
			object = dataBindingEdgeInfo.entryToObject(DataEntry);
		} else {
			System.out.println("OperationStatus is unSUCCESS");
			return null;
		}
		return object.types;
	}

	public HashSet<String> SearchLabelNeighbor(String key) throws Exception {
		Transaction txn = environment.beginTransaction(null, null);
		DatabaseEntry KeyEntry = new DatabaseEntry();
		DatabaseEntry DataEntry = new DatabaseEntry();
		keyBindingLabelNeighbor.objectToEntry(key.toUpperCase(), KeyEntry);
		OperationStatus status = database.get(txn, KeyEntry, DataEntry, LockMode.DEFAULT);
		txn.commit();

		LabelNeighborObject object;
		if (status == OperationStatus.SUCCESS) {
			object = dataBindingLabelNeighbor.entryToObject(DataEntry);
		} else {
			System.out.println("OperationStatus is unSUCCESS");
			return null;
		}

		return object.neighbor;
	}

	public static int RetrieveAll() throws Exception {
		int all = 0;
		DatabaseEntry keyEntry = new DatabaseEntry();
		DatabaseEntry dataEntry = new DatabaseEntry();

		/* retrieve the data */
		Cursor cursor = database.openCursor(null, null);

		while (cursor.getNext(keyEntry, dataEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			all += cursor.count();
		}
		cursor.close();
		return all;
	}

	public void Delete(String key) throws Exception {
		Transaction txn = environment.beginTransaction(null, null);
		DatabaseEntry keyEntry = new DatabaseEntry();
		keyBinding.objectToEntry(key, keyEntry);
		database.delete(txn, keyEntry);
		System.out.println("delete");
		txn.commit();
	}

	public void CloseDatabase() throws Exception {
		this.catalogDb.close();
		this.database.close();
		this.environment.close();
	}

	@SuppressWarnings("serial")
	public static class MyBDBObject implements Serializable {
		public ArrayList<String> synonym = new ArrayList<String>();// maybe
																	// HashSet
		public HashSet<Integer> nodeId = new HashSet<Integer>();

		MyBDBObject(ArrayList<String> synonym, HashSet<Integer> nodeId) {
			this.synonym = synonym;
			this.nodeId = nodeId;
		}
	}

	// NodeID -> NodeLabel
	@SuppressWarnings("serial")
	public static class NodeInfoObject implements Serializable {
		public HashSet<String> label = new HashSet<String>();

		NodeInfoObject(HashSet<String> label) {
			this.label = label;
		}
	}

	// EdgeID -> NodeLabel
	@SuppressWarnings("serial")
	public static class EdgeInfoObject implements Serializable {
		public HashSet<String> types = new HashSet<String>();

		EdgeInfoObject(HashSet<String> types) {
			this.types = types;
		}
	}

	// NodeID -> NodeLabel
	@SuppressWarnings("serial")
	public static class NodeInfoWithProObject implements Serializable {
		public HashSet<String> label = new HashSet<String>();
		public HashMap<String, String> properties = new HashMap<String, String>();

		NodeInfoWithProObject(HashSet<String> label, HashMap<String, String> properties) {
			this.label = label;
			this.properties = properties;
		}
	}

	// EdgeID -> EdgeLabel and Prop
	// @SuppressWarnings("serial")
	// public static class EdgeInfoWithProObject implements Serializable {
	// public HashSet<String> types = new HashSet<String>();
	// public HashMap<String, String> properties = new HashMap<String,
	// String>();
	//
	// EdgeInfoWithProObject(HashSet<String> types, HashMap<String, String>
	// properties) {
	// this.types = types;
	// this.properties = properties;
	// }
	// }

	// Label Neighbor Table
	@SuppressWarnings("serial")
	public static class LabelNeighborObject implements Serializable {
		public HashSet<String> neighbor = new HashSet<String>();

		LabelNeighborObject(HashSet<String> neighbor) {
			this.neighbor = neighbor;
		}
	}

	@SuppressWarnings("serial")
	public static class MySerialSerialBinding extends SerialSerialBinding {

		MySerialSerialBinding(SerialBinding keyBinding, SerialBinding valueBinding) {

			super(keyBinding, valueBinding);
		}

		@Override
		public Object entryToObject(Object keyInput, Object dataInput) {
			return "" + keyInput + '#' + dataInput;
		}

		@Override
		public Object objectToKey(Object object) {
			String s = (String) object;
			int i = s.indexOf('#');
			if (i < 0 || i == s.length() - 1) {
				throw new IllegalArgumentException(s);
			} else {
				return s.substring(0, i);
			}
		}

		@Override
		public Object objectToData(Object object) {
			String s = (String) object;
			int i = s.indexOf('#');
			if (i < 0 || i == s.length() - 1) {
				throw new IllegalArgumentException(s);
			} else {
				return s.substring(i + 1);
			}
		}
	}

	public static String clean(String str) {
		return str.toLowerCase().trim();

	}

}
