package queryExpansion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import aqpeq.utilities.StringPoolUtility;

public class StreamDivQ {

	int k;
	double epsilon;
	double f_max = 0;
	double lambda;
	ObjectiveHandler objHanlder;

	HashSet<Integer> qT = new HashSet<>();
	HashMap<Double, SieveInfo> sieveInfoOfV = new HashMap<Double, SieveInfo>();

	double old_F_s;
	double old_UB_F;
	boolean f_max_is_discovered = false;
	int howManyInSieves = 0;

	public StreamDivQ(int k, double epsilon, double lambda) {
		this.k = k;
		this.epsilon = epsilon;
		this.lambda = lambda;
		f_max_is_discovered = false;
		old_F_s = 0;
		old_UB_F = 0;

		objHanlder = new ObjectiveHandler(k, epsilon, lambda);
	}

	public HashSet<Integer> checkDivAndAdd(int tokenId, double UB_F, double F_s, double f_t,
			HashMap<Integer, TermInfo> infosOfTokenId, HashMap<Integer, HashSet<Integer>> nodeIdsOfCluster,
			double distanceBound) throws Exception {

		// if not needed, do not generate sieves.
		if (F_s != old_F_s && !f_max_is_discovered && F_s > f_max) {
			old_F_s = F_s;
			old_UB_F = UB_F;
			generateSieves(UB_F, F_s);
		}

		int howManySieveStillMayFindABetterAnswer = 0;
		// if all have k-items.
		boolean isCompleted = true;
		for (Double v : sieveInfoOfV.keySet()) {
			HashSet<Integer> qt = sieveInfoOfV.get(v).QtOfV;

			if (qt.size() == k) {
				continue;
			}

			isCompleted = false;

			double checkVal = (v / 2.0 - sieveInfoOfV.get(v).fValue) / (k - qt.size());

			if (f_t < checkVal) {
				continue;
			} else {
				howManySieveStillMayFindABetterAnswer++;
			}

			double mg = objHanlder.marginalGainSieve(sieveInfoOfV.get(v), tokenId, infosOfTokenId, nodeIdsOfCluster,
					distanceBound);
			if (mg >= checkVal) {
				// QT (v) := QT (v) [ ftg;
				qt.add(tokenId);
				sieveInfoOfV.get(v).fValue = objHanlder.computeF(qt, infosOfTokenId, nodeIdsOfCluster, distanceBound,
						null);
			}

			if (f_t <= checkVal && mg >= checkVal) {
				System.err.println("f_t <= checkVal &&  mg>= checkVal => f_t:" + f_t + ", mg:" + mg);
			}
		}

		if (isCompleted || howManySieveStillMayFindABetterAnswer == 0) {
			return getTheBestCurrentQT();
		}

		return null;
	}

	private void generateSieves(double UB_F, double F_s) {

		f_max = F_s;

		int min_i = 0;
		int max_i = 0;
		if (UB_F > F_s) {
			// O := {(1 + e)^i|Fmax <=  (1 + e)^i <=  2k.Fmax}
			min_i = (int) Math.floor(Math.log(f_max) / Math.log(1 + epsilon));
			max_i = (int) Math.ceil(Math.log(2 * k * f_max) / Math.log(1 + epsilon));

		} else {
			// O := {(1 + e)^i|Fmax <=  (1 + e)^i <=  2k.Fmax}
			min_i = (int) Math.floor(Math.log(f_max) / Math.log(1 + epsilon));
			max_i = (int) Math.ceil(Math.log(k * f_max) / Math.log(1 + epsilon));

		}

		// Oi = f(1 + )ijm  (1 + )i  2  k  mg
		HashSet<Double> O = new HashSet<Double>();
		for (int i = min_i; i <= max_i; i++) {
			O.add(getRounded(i));
		}

		// Delete all Sv such that v =2 Oi.
		Iterator<Double> itr = sieveInfoOfV.keySet().iterator();
		while (itr.hasNext()) {
			Double v = itr.next();
			if (!O.contains(v)) {
				itr.remove();
				continue;
			}
		}

		for (Double v : O) {
			sieveInfoOfV.putIfAbsent(v, new SieveInfo());
		}

		if (UB_F <= F_s) {
			f_max_is_discovered = true;

			for (Double v : sieveInfoOfV.keySet()) {
				howManyInSieves += sieveInfoOfV.get(v).QtOfV.size();
			}
		}
	}

	private Double getRounded(int i) {
		return Math.round(Math.pow(1 + epsilon, i) * 100.0) / 100.0;
	}

	public HashSet<Integer> getTheBestCurrentQT() {
		HashSet<Integer> bestQt = null;
		double maxQt = 0;
		for (Double v : sieveInfoOfV.keySet()) {
			if (sieveInfoOfV.get(v).fValue > maxQt) {
				bestQt = sieveInfoOfV.get(v).QtOfV;
				maxQt = sieveInfoOfV.get(v).fValue;
			}
		}
		return bestQt;
	}
	
	public double getTheBestCurrentQTValue() {
		HashSet<Integer> bestQt = null;
		double maxQt = 0;
		for (Double v : sieveInfoOfV.keySet()) {
			if (sieveInfoOfV.get(v).fValue > maxQt) {
				bestQt = sieveInfoOfV.get(v).QtOfV;
				maxQt = sieveInfoOfV.get(v).fValue;
			}
		}
		return maxQt;
	}

}

class SieveInfo {
	HashSet<Integer> QtOfV = new HashSet<Integer>();
	double fValue = 0;

	public SieveInfo() {

	}

	public void setFValue(double fValue) {
		this.fValue = fValue;
	}
}