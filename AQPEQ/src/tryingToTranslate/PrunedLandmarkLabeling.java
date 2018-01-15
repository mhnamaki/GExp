package tryingToTranslate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public class PrunedLandmarkLabeling {

	int kNumBitParallelRoots = 4;
	Index_t[] index_;
	int num_v_;
	Integer RAND_MAX = 0x7fffffff;
	Integer INF8 = 100;
	Integer INT_MAX = Integer.MAX_VALUE;

	public PrunedLandmarkLabeling(int kNumBitParallelRoots) {
		this.kNumBitParallelRoots = kNumBitParallelRoots;
	}

	public PrunedLandmarkLabeling(int kNumBitParallelRoots, String indexPath) throws Exception {
		this.kNumBitParallelRoots = kNumBitParallelRoots;
		this.LoadIndex(indexPath);
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
				idx.bpspt_s[i][0] = Long.parseUnsignedLong(lineArr[1]);
				idx.bpspt_s[i][1] = Long.parseUnsignedLong(lineArr[2]);

				lineArr = null;

			}

			Integer s = Integer.parseInt(it.nextLine().trim().split(":")[1]);

			for (int i = 0; i < s; ++i) {
				index_[v].spt_v = new int[s];
				index_[v].spt_d = new byte[s];
			}

			for (int i = 0; i < s; ++i) {
				String[] lineArr = it.nextLine().trim().split(",");
				idx.spt_v[i] = Integer.parseInt(lineArr[0]);
				idx.spt_d[i] = Byte.parseByte(lineArr[1]);
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

}
