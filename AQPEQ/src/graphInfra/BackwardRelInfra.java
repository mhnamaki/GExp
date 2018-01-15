package graphInfra;

public class BackwardRelInfra extends RelationshipInfra {

	public int correspondingFwdRelId;

	// real source and target in the constructor
	public BackwardRelInfra(int relId, int correspondingFwdRelId, float weight, int sourceNodeId, int targetNodeId) {
		super(relId, sourceNodeId, targetNodeId);
		this.correspondingFwdRelId = correspondingFwdRelId;
		this.weight = weight;
				
	}

}
