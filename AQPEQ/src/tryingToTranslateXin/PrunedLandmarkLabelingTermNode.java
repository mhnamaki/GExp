// this is a tested version before adding term-nodes to it.
package tryingToTranslateXin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import aqpeq.utilities.StringPoolUtility;
import graphInfra.GraphInfraReaderArray;
import graphInfra.RelationshipInfra;
import tryingToTranslate.Index_t;

public class PrunedLandmarkLabelingTermNode {

	int kNumBitParallelRoots = 8;
	Index_t[] index_;
	int num_v_;
	Integer RAND_MAX = 0x7fffffff;
	byte INF8 = 100;
	Integer INT_MAX = Integer.MAX_VALUE;
	HashMap<Integer, HashSet<Integer>> nodeIdsOfToken;
	HashMap<Integer, Integer> termOfTermNodeId;
	HashMap<Integer, Integer> termNodeIdOfTerm;

	public PrunedLandmarkLabelingTermNode(int kNumBitParallelRoots) {
		this.kNumBitParallelRoots = kNumBitParallelRoots;
	}

	public PrunedLandmarkLabelingTermNode(int kNumBitParallelRoots, String indexPath) throws Exception {
		this.kNumBitParallelRoots = kNumBitParallelRoots;
		this.LoadIndex(indexPath);
	}

	public void ConstructIndex(String graphPath) throws Exception {

		// read graph into graph-infra
		GraphInfraReaderArray g = new GraphInfraReaderArray(graphPath, false);
		g.read();

		// read edges in an undirected way
		ArrayList<Pair<Integer, Integer>> es = new ArrayList<Pair<Integer, Integer>>();
		for (RelationshipInfra rel : g.relationOfRelId) {
			es.add(new Pair<>(rel.sourceId, rel.destId));
			es.add(new Pair<>(rel.destId, rel.sourceId));
		}

		// get inverted list of tokens
		nodeIdsOfToken = g.indexInvertedListOfTokens(g);
		termOfTermNodeId = new HashMap<Integer, Integer>();
		termNodeIdOfTerm = new HashMap<Integer, Integer>();
		int newMaxNodeId = g.maxNodeId;

		// for any term that we have
		for (Integer tokenId : nodeIdsOfToken.keySet()) {
			// we add a new node id
			newMaxNodeId++;
			termOfTermNodeId.put(newMaxNodeId, tokenId);
			termNodeIdOfTerm.put(tokenId, newMaxNodeId);

			// we connect this node to all of its content nodes
			for (Integer contentNodeId : nodeIdsOfToken.get(tokenId)) {
				es.add(new Pair<>(contentNodeId, newMaxNodeId));
				es.add(new Pair<>(newMaxNodeId, contentNodeId));
			}

		}

		int num_v = newMaxNodeId;

		// the node id should start from 1 not zero!
		num_v_ = num_v + 1;
		int E = es.size();
		int V = 0;
		for (int i = 0; i < es.size(); ++i) {
			V = Math.max(V, Math.max(es.get(i).first, es.get(i).second) + 1);
		}

		// std::vector<std::vector<int> > adj(V);
		ArrayList<ArrayList<Integer>> adj = new ArrayList<>();

		// khodam
		for (int i = 0; i < V; i++) {
			adj.add(new ArrayList<>());
		}

		// for each edge we consider both endpoints to be added into adj.
		for (int i = 0; i < es.size(); ++i) {
			int v = es.get(i).first, w = es.get(i).second;
			adj.get(v).add(w);
			adj.get(w).add(v);
		}

		// generating an index entry for each node.
		index_ = new Index_t[V];
		for (int v = 0; v < V; ++v) {
			// for at most kNumBitParallelRoots of their neighbors
			index_[v] = new Index_t(kNumBitParallelRoots);
		}

		ArrayList<Integer> inv = new ArrayList<>(V);
		{
			ArrayList<Pair<Integer, Integer>> deg = new ArrayList<>(V);

			ArrayList<Integer> rank = new ArrayList<>(V);
			ArrayList<ArrayList<Integer>> new_adj = new ArrayList<>(V);

			// khodam
			for (int i = 0; i < V; i++) {
				deg.add(null);
				inv.add(null);
				rank.add(null);
				new_adj.add(new ArrayList<>());
			}

			// Order
			// setting the degree of nodes in deg array
			for (int v = 0; v < V; ++v) {
				// We add a random value here to diffuse nearby vertices
				// TODO: if everything is fine we can return this it was DOUBLE
				deg.set(v, new Pair<Integer, Integer>(adj.get(v)
						.size() /* + (double) (Math.random() / RAND_MAX) */, v));
			}

			// sorting degree 1st key: degree (desc) 2nd key: v (asc)
			Collections.sort(deg, new Comparator<Pair<Integer, Integer>>() {
				@Override
				public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
					if (o2.first != o1.first) {
						return Integer.compare(o2.first, o1.first);
					} else {
						return Integer.compare(o1.second, o2.second);
					}
				}
			});

			// for test
			// int tempF, tempS;
			// tempF = deg.get(1).first;
			// tempS = deg.get(1).second;
			//
			// deg.get(1).first = deg.get(2).first;
			// deg.get(1).second = deg.get(2).second;
			//
			// deg.get(2).first = tempF;
			// deg.get(2).second = tempS;
			//
			// tempF = deg.get(3).first;
			// tempS = deg.get(3).second;
			//
			// deg.get(3).first = deg.get(4).first;
			// deg.get(3).second = deg.get(4).second;
			//
			// deg.get(4).first = tempF;
			// deg.get(4).second = tempS;

			// inv keeps a map of what is previous id of current node id
			for (int i = 0; i < V; ++i)
				inv.set(i, deg.get(i).second);

			// Relabel the vertex IDs
			// it's opposite of inv. so inv: current -> previous
			// rank: previous -> current
			for (int i = 0; i < V; ++i)
				rank.set(deg.get(i).second, i);

			// now updating adj based on new ids
			for (int v = 0; v < V; ++v) {
				for (int i = 0; i < adj.get(v).size(); ++i) {
					// we set the current new_adj based on current id get from
					// rank.
					// and add the value of current id by accessing to the
					// previous adj
					new_adj.get(rank.get(v)).add(rank.get(adj.get(v).get(i)));
				}
			}

			// ArrayList<ArrayList<Integer>> temp = new ArrayList<>();
			// temp.addAll(new_adj);
			// new_adj.clear();
			// new_adj.addAll(adj);
			adj.clear();
			adj.addAll(new_adj);
		}

		//
		// Bit-parallel labeling
		//
		ArrayList<Boolean> usd = new ArrayList<>(V); // Used as root? (in new
														// label)
		{

			ArrayList<Byte> tmp_d = new ArrayList<>(V);
			ArrayList<Pair<Long, Long>> tmp_s = new ArrayList<>(V);
			ArrayList<Integer> que = new ArrayList<>(V);

			// khodam
			for (int v = 0; v < V; v++) {
				usd.add(false);
				que.add(0);
				tmp_d.add(INF8); // because of line 222 in C++;
				tmp_s.add(new Pair<Long, Long>(0l, 0l));
			}

			ArrayList<Pair<Integer, Integer>> sibling_es = new ArrayList<>(E);
			ArrayList<Pair<Integer, Integer>> child_es = new ArrayList<>(E);

			// initializing sibling es and children es based on number of edges
			for (int i = 0; i < E; i++) {
				sibling_es.add(new Pair<Integer, Integer>(0, 0));
				child_es.add(new Pair<Integer, Integer>(0, 0));
			}

			int r = 0; // potential root
			// i_bpspt: which bit now?!
			for (int i_bpspt = 0; i_bpspt < kNumBitParallelRoots; ++i_bpspt) {

				// if already has been used as a root go to the next one
				while (r < V && usd.get(r) && termOfTermNodeId.containsKey(r))
					++r;

				// if all the vertices has been used for root
				// set all bpspt_d to INF8
				// continue to the next bit!
				if (r == V) {
					for (int v = 0; v < V; ++v)
						index_[v].bpspt_d[i_bpspt] = INF8;
					continue;
				}

				// if we reach here with r, we want to consider it as a root
				// and do the BFS.
				// so, set it to the "used"
				usd.set(r, true);

				// (P[v], S−1r [v], S0r [v]) ← (∞, ∅, ∅) for all v ∈ V
				// (P[r], S−1r [r], S0r [r]) ← (0, ∅, ∅)
				for (int v = 0; v < V; v++) {

					// the distance from r to v is inf now.
					tmp_d.set(v, INF8); // because of line 222 in C++;
					tmp_s.set(v, new Pair<Long, Long>(0l, 0l));
				}

				// TODO: que_t0: a temporary pointer to move
				// TODO: que_t1: a temporary pointer to move.
				// TODO: que_h: a pointer to the last added element in que
				int que_t0 = 0, que_t1 = 0, que_h = 0;

				// Enqueue r onto Q0
				que.set(que_h++, r);

				// the temp distance from r =0
				tmp_d.set(r, (byte) 0);
				que_t1 = que_h;

				int ns = 0;

				// what's the usage of vs? it's just being pushed!
				ArrayList<Integer> vs = new ArrayList<>();

				// sorting the neighbors of r
				Collections.sort(adj.get(r));

				// for each direct adjacent of current selected root node
				for (int i = 0; i < adj.get(r).size(); ++i) {
					int v = adj.get(r).get(i);

					// if the adjacent node v didn't use as a root
					if (!usd.get(v)) {

						// add it to que and set it as a used!
						usd.set(v, true);
						que.set(que_h++, v);

						// the temp distance from r to v is 1.
						tmp_d.set(v, (byte) 1);

						// TODO: very important
						tmp_s.get(v).first = 1L << ns;

						vs.add(v);

						// at most b number of its neighbors
						if (++ns == 32)
							break;
					}
				}

				for (int d = 0; que_t0 < que_h; ++d) {
					int num_sibling_es = 0, num_child_es = 0;

					for (int que_i = que_t0; que_i < que_t1; ++que_i) {

						// Dequeue v from Q0.
						int v = que.get(que_i);

						// for all u ∈ NG(v) do
						for (int i = 0; i < adj.get(v).size(); ++i) {

							// target node v
							int tv = adj.get(v).get(i);

							// distance from v to t is d+1;
							int td = d + 1;

							// if already have a better distance for this tv
							// from r.
							// don't bother yourself!
							if (d > tmp_d.get(tv))
								;
							// otherwise, if p[u]==p[v]
							else if (d == tmp_d.get(tv)) {
								if (v < tv) {
									// E0 ← E0 ∪ {(v, u)}
									// same distance from the root
									sibling_es.set(num_sibling_es, new Pair<Integer, Integer>(v, tv));
									++num_sibling_es;
								}
							} else {

								// if P[u] = ∞ then
								if (tmp_d.get(tv) == INF8) {

									// if it's term-node we do not add it to the
									// Q1
									if (!termOfTermNodeId.keySet().contains(tv)) {
										// Enqueue u onto Q1.
										que.set(que_h++, tv);
									}

									// P[u] ← P[v] + 1 note that td is d+1 now
									tmp_d.set(tv, (byte) td);
								}

								// E1 ← E1 ∪ {(v, u)}
								child_es.set(num_child_es, new Pair<Integer, Integer>(v, tv));
								++num_child_es;
							}
						}
					}

					// for all (v, u) ∈ E0 do
					for (int i = 0; i < num_sibling_es; ++i) {

						// S0r[u] ← S0r[u] ∪ S−1r[v]
						int v = sibling_es.get(i).first, w = sibling_es.get(i).second;
						tmp_s.get(v).second |= tmp_s.get(w).first;
						tmp_s.get(w).second |= tmp_s.get(v).first;
					}

					// for all (v, u) ∈ E1 do
					for (int i = 0; i < num_child_es; ++i) {

						// S−1r [u] ← S−1r [u] ∪ S−1r [v]
						// S0r [u] ← S0r [u] ∪ S0r [v]
						int v = child_es.get(i).first, c = child_es.get(i).second;
						tmp_s.get(c).first |= tmp_s.get(v).first;
						tmp_s.get(c).second |= tmp_s.get(v).second;
					}

					// Q0 ← Q1 and Q1 ← ∅
					que_t0 = que_t1;
					que_t1 = que_h;
				}

				for (int v = 0; v < V; ++v) {
					index_[inv.get(v)].bpspt_d[i_bpspt] = tmp_d.get(v);
					index_[inv.get(v)].bpspt_s[i_bpspt][0] = tmp_s.get(v).first;
					index_[inv.get(v)].bpspt_s[i_bpspt][1] = tmp_s.get(v).second & ~tmp_s.get(v).first;

				}
			}
		}

		// for test
		// for (int v = 0; v < V; ++v) {
		// for (int i_bpspt = 0; i_bpspt < 2; ++i_bpspt) {
		// System.out.println(inv.get(v) + ", " +
		// index_[inv.get(v)].bpspt_d[i_bpspt] + ", "
		// + index_[inv.get(v)].bpspt_s[i_bpspt][0] + ", " +
		// index_[inv.get(v)].bpspt_s[i_bpspt][1]);
		// }
		// }

		//
		// Pruned labeling
		//
		{
			// Sentinel (V, INF8) is added to all the vertices
			// a temporary index for maintaining int[] spt_v & byte[] spt_d
			ArrayList<Pair<ArrayList<Integer>, ArrayList<Byte>>> tmp_idx = new ArrayList<>();

			for (int v = 0; v < V; ++v) {
				tmp_idx.add(new Pair<ArrayList<Integer>, ArrayList<Byte>>(new ArrayList<>(), new ArrayList<>()));
			}
			for (int v = 0; v < V; ++v) {
				tmp_idx.get(v).first.add(V);
				tmp_idx.get(v).second.add(INF8);
			}

			ArrayList<Boolean> vis = new ArrayList<>(V);
			ArrayList<Integer> que = new ArrayList<>(V);
			ArrayList<Byte> dst_r = new ArrayList<Byte>(V + 1);

			for (int v = 0; v < V; ++v) {
				vis.add(false);
				que.add(null);
			}
			for (int v = 0; v <= V; ++v) {
				dst_r.add(INF8);
			}

			for (int r = 0; r < V; ++r) {
				// if we've already done a BFS from it, good, go to the next!
				if (usd.get(r) || termOfTermNodeId.containsKey(r))
					continue;

				// fetch its actual index to use also information from bit
				// parallel
				Index_t idx_r = index_[inv.get(r)];

				// TODO: should it really be filled with temporary values or the
				// original idx_r?!
				// create a temporary index initialized by temporary values!
				Pair<ArrayList<Integer>, ArrayList<Byte>> tmp_idx_r = new Pair<ArrayList<Integer>, ArrayList<Byte>>(
						tmp_idx.get(r).first, tmp_idx.get(r).second);

				for (int i = 0; i < tmp_idx_r.first.size(); ++i) {
					// from r to first.get(i) how much distance?!
					dst_r.set(tmp_idx_r.first.get(i), tmp_idx_r.second.get(i));
				}

				int que_t0 = 0, que_t1 = 0, que_h = 0;

				// Q ← a queue with only one element r
				que.set(que_h++, r);
				vis.set(r, true);
				que_t1 = que_h;

				boolean goToPruned = false;

				for (int d = 0; que_t0 < que_h; ++d) {
					for (int que_i = que_t0; que_i < que_t1; ++que_i) {

						// Dequeue v from Q.
						int v = que.get(que_i);

						Pair<ArrayList<Integer>, ArrayList<Byte>> tmp_idx_v = new Pair<ArrayList<Integer>, ArrayList<Byte>>(
								tmp_idx.get(v).first, tmp_idx.get(v).second);

						Index_t idx_v = index_[inv.get(v)];

						// Prefetch
						// _mm_prefetch(&idx_v.bpspt_d[0], _MM_HINT_T0);
						// _mm_prefetch(&idx_v.bpspt_s[0][0], _MM_HINT_T0);
						// _mm_prefetch(&tmp_idx_v.first[0], _MM_HINT_T0);
						// _mm_prefetch(&tmp_idx_v.second[0], _MM_HINT_T0);

						// Prune?
						if (usd.get(v))
							continue;
						for (int i = 0; i < kNumBitParallelRoots; ++i) {
							int td = idx_r.bpspt_d[i] + idx_v.bpspt_d[i];
							if (td - 2 <= d) {
								// TODO: possibly due to being not unsigned may
								// be it's less than zero
								// but with the s
								if ((idx_r.bpspt_s[i][0] & idx_v.bpspt_s[i][0]) > 0) {
									td += -2;
								} else if (((idx_r.bpspt_s[i][0] & idx_v.bpspt_s[i][1])
										| (idx_r.bpspt_s[i][1] & idx_v.bpspt_s[i][0])) > 0) {
									td += -1;
								} else {

									if (((idx_r.bpspt_s[i][0] & idx_v.bpspt_s[i][0]) < 0)
											|| (((idx_r.bpspt_s[i][0] & idx_v.bpspt_s[i][1])
													| (idx_r.bpspt_s[i][1] & idx_v.bpspt_s[i][0])) < 0)) {
										System.err.println("negative!");
									}

									td += 0;
								}

								if (td <= d) {
									goToPruned = true;// goto pruned;
									break;
								}
							}
						}
						if (!goToPruned) {
							for (int i = 0; i < tmp_idx_v.first.size(); ++i) {
								int w = tmp_idx_v.first.get(i);
								int td = tmp_idx_v.second.get(i) + dst_r.get(w);
								if (td <= d) {
									goToPruned = true;// goto pruned;
									break;
								}
							}
						}

						if (!goToPruned) {

							if (!termOfTermNodeId.keySet().contains(v)) {
								// Traverse
								tmp_idx_v.first.set(tmp_idx_v.first.size() - 1, r);
								tmp_idx_v.second.set(tmp_idx_v.second.size() - 1, (byte) d);
								tmp_idx_v.first.add(V);
								tmp_idx_v.second.add(INF8);
								for (int i = 0; i < adj.get(v).size(); ++i) {
									int w = adj.get(v).get(i);
									if (!vis.get(w)) {
										que.set(que_h++, w);
										vis.set(w, true);
									}
								}
							}
						}
						if (goToPruned) {
							goToPruned = false;
						}
					}

					que_t0 = que_t1;
					que_t1 = que_h;
				}

				for (int i = 0; i < que_h; ++i)
					vis.set(que.get(i), false);
				for (int i = 0; i < tmp_idx_r.first.size(); ++i) {
					dst_r.set(tmp_idx_r.first.get(i), INF8);
				}
				usd.set(r, true);
			}

			for (int v = 0; v < V; ++v) {
				int k = tmp_idx.get(v).first.size();

				index_[inv.get(v)].spt_v = new int[k];
				index_[inv.get(v)].spt_d = new byte[k];

				// for (int i = 0; i < k; ++i) {
				// index_[inv.get(v)].spt_v.add(null);
				// index_[inv.get(v)].spt_d.add(null);
				// }

				for (int i = 0; i < k; ++i)
					index_[inv.get(v)].spt_v[i] = tmp_idx.get(v).first.get(i);
				for (int i = 0; i < k; ++i)
					index_[inv.get(v)].spt_d[i] = tmp_idx.get(v).second.get(i);
				tmp_idx.get(v).first.clear();
				tmp_idx.get(v).second.clear();
			}
		}
	}

	//
	public int queryDistance(int s, int t) {
		if (s >= num_v_ || t >= num_v_)
			return s == t ? 0 : INT_MAX;

		Index_t idx_s = index_[s];
		Index_t idx_t = index_[t];
		int d = INF8;

		for (int i = 0; i < kNumBitParallelRoots; ++i) {
			int td = idx_s.bpspt_d[i] + idx_t.bpspt_d[i];
			if (td - 2 <= d) {
				if ((idx_s.bpspt_s[i][0] & idx_t.bpspt_s[i][0]) > 0) {
					td += -2;
				} else if (((idx_s.bpspt_s[i][0] & idx_t.bpspt_s[i][1])
						| (idx_s.bpspt_s[i][1] & idx_t.bpspt_s[i][0])) > 0) {
					td += -1;
				} else {
					td += 0;
				}

				if (td < d)
					d = td;
			}
		}
		for (int i1 = 0, i2 = 0;;) {
			int v1 = idx_s.spt_v[i1], v2 = idx_t.spt_v[i2];
			if (v1 == v2) {
				if (v1 == num_v_)
					break; // Sentinel
				int td = idx_s.spt_d[i1] + idx_t.spt_d[i2];
				if (td < d)
					d = td;
				++i1;
				++i2;
			} else {
				i1 += v1 < v2 ? 1 : 0;
				i2 += v1 > v2 ? 1 : 0;
			}
		}

		if (d >= INF8 - 2)
			d = INT_MAX;
		return d;
	}

	public void StoreIndex(String indexName) throws Exception {

		File ftn = new File("termnodemap.txt");
		FileOutputStream fostn = new FileOutputStream(ftn);
		BufferedWriter bwtn = new BufferedWriter(new OutputStreamWriter(fostn, "UTF8"));

		for (Integer termId : termNodeIdOfTerm.keySet()) {
			bwtn.write(StringPoolUtility.getStringOfId(termId) + ";" + termNodeIdOfTerm.get(termId) + "\n");
		}
		bwtn.close();

		File fout = new File(indexName);
		FileOutputStream fos = new FileOutputStream(fout);

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF8"));

		int num_v = num_v_, num_bpr = kNumBitParallelRoots;
		bw.write("num_v:" + num_v);
		bw.newLine();
		bw.write("num_bpr:" + num_bpr);
		bw.newLine();
		bw.flush();

		for (int v = 0; v < num_v_; ++v) {
			// bw.write("v:" + v);
			// bw.newLine();
			Index_t idx = index_[v];

			for (int i = 0; i < kNumBitParallelRoots; ++i) {
				// int d = idx.bpspt_d[i];
				// long a = idx.bpspt_s[i][0];
				// long b = idx.bpspt_s[i][1];
				// bw.write("d:" + d);
				// bw.newLine();
				// bw.write("a:" + a);
				// bw.newLine();
				// bw.write("b:" + b);
				// bw.newLine();
				bw.write(idx.bpspt_d[i] + ", " + idx.bpspt_s[i][0] + ", " + idx.bpspt_s[i][1]);
				bw.newLine();

			}

			int s;
			for (s = 1; idx.spt_v[s - 1] != num_v; ++s)
				continue; // Find the sentinel
			bw.write("s:" + s);
			bw.newLine();
			for (int i = 0; i < s; ++i) {
				int l = idx.spt_v[i];
				int d = idx.spt_d[i];
				// bw.write("l:" + l);
				// bw.newLine();
				// bw.write("d:" + d);
				// bw.newLine();
				bw.write(idx.spt_v[i] + ", " + idx.spt_d[i]);
				bw.newLine();
			}
			// bw.newLine();
			// bw.newLine();
		}
		bw.close();
	}

	public boolean LoadIndex(String indexName) throws Exception {

		double loadingStartTime = System.nanoTime();

		LineIterator it = FileUtils.lineIterator(new File(indexName), "UTF-8");

		Integer num_v, num_bpr;
		num_v = Integer.parseInt(it.nextLine().trim().split(":")[1]);

		num_bpr = Integer.parseInt(it.nextLine().trim().split(":")[1]);

		num_v_ = num_v;
		if (kNumBitParallelRoots != num_bpr) {
			num_v_ = 0;
			LineIterator.closeQuietly(it);
			return false;
		}

		index_ = new Index_t[num_v_];
		for (int v = 0; v < num_v_; ++v) {
			index_[v] = new Index_t(kNumBitParallelRoots);
		}

		for (int v = 0; v < num_v_; ++v) {
			if (v % 100000 == 0)
				System.out.println("v: " + v);

			// String vLine = it.nextLine();
			// System.out.println(vLine + " ? " + v);
			Index_t idx = index_[v];
			for (int i = 0; i < kNumBitParallelRoots; ++i) {
				String[] lineArr = it.nextLine().trim().split(",");
				idx.bpspt_d[i] = Byte.parseByte(lineArr[0]);
				idx.bpspt_s[i][0] = Long.parseUnsignedLong(lineArr[1].trim());
				idx.bpspt_s[i][1] = Long.parseUnsignedLong(lineArr[2].trim());

				lineArr = null;

			}

			Integer s = Integer.parseInt(it.nextLine().trim().split(":")[1]);

			for (int i = 0; i < s; ++i) {
				index_[v].spt_v = new int[s];
				index_[v].spt_d = new byte[s];
			}

			for (int i = 0; i < s; ++i) {
				String[] lineArr = it.nextLine().trim().split(",");
				idx.spt_v[i] = Integer.parseInt(lineArr[0].trim());
				idx.spt_d[i] = Byte.parseByte(lineArr[1].trim());
				lineArr = null;
			}
		}

		LineIterator.closeQuietly(it);

		double loadingDurationTime = (System.nanoTime() - loadingStartTime) / 1e6;
		System.out.println("loadingDurationTime: " + loadingDurationTime);

		int bytes = 0;
		int longs = 0;
		int integers = 0;
		for (int i = 0; i < index_.length; i++) {
			bytes += index_[i].bpspt_d.length;
			bytes += index_[i].spt_d.length;
			longs += index_[i].bpspt_s.length * 2;
			integers += index_[i].spt_v.length;
		}

		System.out.println("bytes:" + bytes);
		System.out.println("longs:" + longs);
		System.out.println("integers:" + integers);

		System.gc();
		System.runFinalization();

		return true;
	}

	// public boolean LoadIndex(String indexName) throws Exception {
	//
	// FileInputStream fis = new FileInputStream(indexName);
	// BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	//
	// Integer num_v, num_bpr;
	// num_v = Integer.parseInt(br.readLine().trim().split(":")[1]);
	//
	// num_bpr = Integer.parseInt(br.readLine().trim().split(":")[1]);
	//
	// num_v_ = num_v;
	// if (kNumBitParallelRoots != num_bpr) {
	// num_v_ = 0;
	// br.close();
	// return false;
	// }
	//
	// index_ = new ArrayList<Index_t>(num_v_ + 1);
	// for (int v = 0; v < num_v_; ++v) {
	// index_.add(new Index_t(kNumBitParallelRoots));
	// }
	//
	// for (int v = 0; v < num_v_; ++v) {
	//
	// if (v % 100000 == 0)
	// System.out.println("v: " + v);
	//
	// String vLine = br.readLine();
	// // System.out.println(vLine + " ? " + v);
	// Index_t idx = index_.get(v);
	// for (int i = 0; i < kNumBitParallelRoots; ++i) {
	// String[] lineArr = br.readLine().trim().split(",");
	// idx.bpspt_d[i] = Byte.parseByte(lineArr[0]);
	// idx.bpspt_s[i][0] = Long.parseUnsignedLong(lineArr[1]);
	// idx.bpspt_s[i][1] = Long.parseUnsignedLong(lineArr[2]);
	//
	// }
	//
	// Integer s = Integer.parseInt(br.readLine().trim().split(":")[1]);
	//
	// for (int i = 0; i < s; ++i) {
	// index_.get(v).spt_v = new int[s];
	// index_.get(v).spt_d = new byte[s];
	// }
	//
	// for (int i = 0; i < s; ++i) {
	// String[] lineArr = br.readLine().trim().split(",");
	// idx.spt_v[i] = Integer.parseInt(lineArr[0]);
	// idx.spt_d[i] = Byte.parseByte(lineArr[1]);
	// }
	// }
	//
	// br.close();
	// return true;
	// }

	public static void main(String[] args) throws Exception {
		String index_path = "/Users/mnamaki/Desktop/index_from_java";
		String graph_path = "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/Synthetics/termNodes/";
		// String graph_path =
		// "/Users/mnamaki/Documents/Education/PhD/Summer2017/AQEQ/Datasets/Synthetics/termNodes/relationships.tsv";

		ArrayList<Pair<Integer, Integer>> pairs = new ArrayList<Pair<Integer, Integer>>();
		HashSet<Integer> Vs = new HashSet<Integer>();

		// PrunedLandmarkLabeling pl = new PrunedLandmarkLabeling(8);
		PrunedLandmarkLabelingTermNode pl = new PrunedLandmarkLabelingTermNode(8);

		// FileInputStream fis = new FileInputStream(graph_path);
		//
		// // Construct BufferedReader from InputStreamReader
		// BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		//
		// String line = null;
		// while ((line = br.readLine()) != null) {
		// String[] edgePoints = line.split("\t");
		// int src = Integer.parseInt(edgePoints[0]);
		// int dest = Integer.parseInt(edgePoints[1]);
		// Pair<Integer, Integer> pair = new Pair<Integer, Integer>(src, dest);
		// pairs.add(pair);
		// Vs.add(src);
		// Vs.add(dest);
		// }
		//
		// br.close();

		pl.ConstructIndex(graph_path);
		// pl.ConstructIndex(pairs, Vs.size());

		pl.StoreIndex(index_path);

		pl.LoadIndex(index_path);

		// printDist(pl, 1, 1);
		// printDist(pl, 1, 2);
		pl.printDist(1, 6);
		// pl.printDist(6, 1);
		// printDist(pl, 6, 4);

	}

	public void printDist(int i, int j) {
		int[] num = new int[] { i, j };
		System.out.println("i:" + num[0] + ", j:" + num[1] + ", dist:" + queryDistance(num[0], num[1]));

	}
}
