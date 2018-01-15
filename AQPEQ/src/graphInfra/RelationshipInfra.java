package graphInfra;

import java.util.HashMap;
import java.util.HashSet;

public class RelationshipInfra {
	public int relId;
	public int sourceId;
	public int destId;
	public HashSet<Integer> types;
	public HashMap<Integer, HashSet<Integer>> properties;
	public float weight = 1f;

	public RelationshipInfra(int relId, int sourceId, int destId, float weight, HashSet<Integer> types,
			HashMap<Integer, HashSet<Integer>> properties) {
		this.relId = relId;
		this.sourceId = sourceId;
		this.destId = destId;
		this.types = types;
		this.properties = properties;
	}

	public RelationshipInfra(int relId, int sourceId, int destId) {
		this.relId = relId;
		this.sourceId = sourceId;
		this.destId = destId;
	}

	public void addType(Integer type) {
		if (types == null) {
			types = new HashSet<>();
		}
		types.add(type);

	}

	public void addProperties(Integer key, HashSet<Integer> value) {
		if (properties == null) {
			properties = new HashMap<Integer, HashSet<Integer>>();
		}
		properties.put(key, value);

	}

	@Override
	public String toString() {
		return " id:" + relId + " w:" + weight + " t:" + types;
	}

	@Override
	public int hashCode() {
		return relId;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		RelationshipInfra other = (RelationshipInfra) obj;
		if (this.relId != other.relId)
			return false;

		return true;
	}

}
