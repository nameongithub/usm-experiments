package algorithm.KSIP_usm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import algorithm.Algorithm;
import algorithm.KSIP_usm.TreeNode;

public class KSIP_USM implements Algorithm {
	public static int FRINGE_DEPTH = 1; // ��Ե��㹹������
	private static double D = 0.2;
	private int lastInstanceNum = 0;

	private List<Instance> instanceList;
	private List<TreeNode> leafList;
	private List<HashMap<TreeNode, Double>> qTable;
	private SuffixTree suffixTree;
	private int observationSize;
	private int actionSize;
	private TreeNode curState;

	// isTarget��ʾ�Ƿ�ȷ��Ψһ��curState�����ڵִ�goal����ٴ�ѧϰ����ʱ������������һ����goal��cell
	// isTarget��Ϊfalse�����ÿ��makeDecision()�����ѡȡaִ�У�����instance����tree����ֱ��curStateΨһ
	private boolean isTarget;

	private double GAMMA = 0.9; // �ۿ�ֵȡ0.9
	private double EPSILON = 0.5; // ̽��-����ϵ��ȡ0.5

	public int fringeDepth = 0;

	/**
	 * USM�㷨���ʼ������������������int�ͱ����õ�O��A���Ĵ�С
	 * 
	 * @param observationSize
	 * @param actionSize
	 */
	public KSIP_USM(int observationSize, int actionSize) {
		this.observationSize = observationSize;
		this.actionSize = actionSize;
		this.suffixTree = new SuffixTree(this.actionSize, this.observationSize);
		this.instanceList = new ArrayList<Instance>();
		this.leafList = new ArrayList<TreeNode>();
		this.leafListInit();
		this.qTable = new ArrayList<HashMap<TreeNode, Double>>();

		for (int i = 0; i < actionSize; i++) {
			TreeNode tn_temp = null;
			Iterator<TreeNode> titr = leafList.iterator();
			qTable.add(new HashMap<TreeNode, Double>());
			while (titr.hasNext()) {
				tn_temp = titr.next();
				qTable.get(i).put(tn_temp, 0.0);
			}

		}
	}

	@Override
	public void generateInstance(int actionIndex, int observationIndex, double reward) {

		Instance last = null;
		// curState�Ѿ�Ψһ
		if (instanceList.size() != 0) {
			last = instanceList.get(instanceList.size() - 1);
		}
		Instance in = new Instance(last, actionIndex, observationIndex, reward);
		last.setNextInstance(in);
		instanceList.add(in);
		curState = instanceMatching(in);
		if (this.isTarget == true) {
			if (curState != null)
				instancePutting(in, curState);
		} else {
			// ƥ������Ϊnull����һ��leaf
			// �����Ϣ���㣬���ص�һ����null;��ʱӦ����������
			if (curState != null && this.leafList.contains(curState)) {
				System.out.println("****set isTarget true****");
				this.isTarget = true;
			}
		}
	}

	@Override
	public int getStateNum() {

		return this.leafList.size();
	}

	/**
	 * ���߷��������ݵ�ǰ״������ʷ��¼��agent�������
	 */
	@Override
	public int makeDecision() {

		// ѧϰѡȡ������ʱ��ѡ��WAIT��������Ϊ���󽵵�Ч��
		int actionIndex = -1;
		double rand = Math.random();
		if (this.isTarget == false || rand < this.EPSILON) {
			// �������һ��������������û������Ķ���������ײǽ
			// ԭ�����㷨Ӧ��֪��ÿ��������ÿ���۲�ľ��庬�壬���翴��LRU��ʱ��֪��ѡ���������Ҷ���û�������
			// ����������Ķ�����environment�ڽ��ܶ�����ʱ��������
			System.out.print("-randomly:\t");
			// �����Ƿ�ֹѡ�������嶯���Ĵ��룬��Ҫ�Ľ�
			int o = this.instanceList.get(this.instanceList.size() - 1).getObservation();
			int a = -1;
			while (true) {
				a = (int) (Math.random() * actionSize);
				if (((a == 1) && (o == 1 || o == 3 || o == 5 || o == 9 || o == 7 || o == 11 || o == 13 || o == 15))
						|| ((a == 2)
								&& (o == 2 || o == 3 || o == 6 || o == 10 || o == 7 || o == 11 || o == 14 || o == 15))
						|| ((a == 3)
								&& (o == 4 || o == 5 || o == 6 || o == 12 || o == 7 || o == 13 || o == 14 || o == 15))
						|| ((a == 4) && (o == 8 || o == 9 || o == 10 || o == 12 || o == 11 || o == 13 || o == 14
								|| o == 15))) {
					continue;
				}
				if (a == 0)
					continue;
				else {
					break;
				}
			}
			return a;
		} else {
			// this.printQvalueTable();
			double maxQ = Double.NEGATIVE_INFINITY;
			int o = this.instanceList.get(this.instanceList.size() - 1).getObservation();
			for (int a = 1; a < actionSize; a++) {
				if (((a == 1) && (o == 1 || o == 3 || o == 5 || o == 9 || o == 7 || o == 11 || o == 13 || o == 15))
						|| ((a == 2)
								&& (o == 2 || o == 3 || o == 6 || o == 10 || o == 7 || o == 11 || o == 14 || o == 15))
						|| ((a == 3)
								&& (o == 4 || o == 5 || o == 6 || o == 12 || o == 7 || o == 13 || o == 14 || o == 15))
						|| ((a == 4) && (o == 8 || o == 9 || o == 10 || o == 12 || o == 11 || o == 13 || o == 14
								|| o == 15))) {
					continue;

				}
				if (qTable.get(a).get(curState) > maxQ) {
					actionIndex = a;
					maxQ = qTable.get(a).get(curState);
				}
			}
			System.out.print("+Unrandomly:\t");
			return actionIndex;
		}

	}

	@Override
	public double getADR() {
		System.out.println("deepest fringe:" + this.fringeDepth);
		double sum = 0;
		for (int i = 0; i < qTable.size(); i++) {
			int j = 0;
			while (!qTable.get(i).isEmpty() && j < leafList.size()) {
				sum += qTable.get(i).get(leafList.get(j));
				j++;
			}
		}
		return sum / this.leafList.size() / this.actionSize;
	}

	@Override
	public void newStart(int newO) {

		// �������n����ֱ��������ȷ��λcurState
		this.isTarget = false;
		System.out.println("****set isTarget false****");
		// ����һ��ʵ���������У���ʵ��������ͳ��
		Instance in = new Instance(null, -1, newO, 0);
		this.instanceList.add(in);

	}

	/**
	 * Ҷ�ڵ㼯�ϣ�Ҳ����״̬���ϵĳ�ʼ��
	 */
	private void leafListInit() {
		Iterator<TreeNode> titr = this.suffixTree.root.getSonNodeIteritor();
		TreeNode tn_temp = null;
		while (titr.hasNext()) {
			tn_temp = titr.next();
			this.leafList.add(tn_temp);
		}
	}

	/**
	 * ����instance�ķ���,�������һ��Qֵ���º�һ�α�Ե�����
	 * 
	 * @param in
	 * @param tn
	 */
	private void instancePutting(Instance in, TreeNode tn) {
		if (tn.isLeaf()) {
			tn.getInstanceList().add(in);
			updateQ();
			// if (checkFringe(tn)) {
			// curState = this.instanceMatching(in);
			// }
			improveKSMode1();

		} else {
			System.out.println("instancePutting error!");
		}
	}

	/**
	 * instance�Ĵ�׺�����ƥ���㷨,����instance�ҵ���Ӧ��leaf
	 * 
	 * @param in
	 */
	private TreeNode instanceMatching(Instance in) {
		Instance in_temp = in;
		TreeNode tn_temp = null;
		Iterator<TreeNode> titr = this.suffixTree.root.getSonNodeIteritor();

		while (titr.hasNext()) {
			tn_temp = titr.next();
			if ((tn_temp.getDepth() % 2 == 0 && tn_temp.getIndex() == in_temp.getAction())
					|| (tn_temp.getDepth() % 2 != 0 && tn_temp.getIndex() == in_temp.getObservation())) {
				// ƥ��ɹ���TreeNode���ж��ǲ���Ҷ�ڵ�
				if (tn_temp.isLeaf()) {
					// Ҷ�ڵ㣬ֱ�ӷ���
					return tn_temp;
				} else {
					// ����Ҷ�ڵ㣬����ƥ�䣺���������������������Ҫƥ��o�����instance
					if (tn_temp.getDepth() % 2 == 0)
						in_temp = in_temp.getLastInstance();
					if (in_temp == null)
						break;
					titr = tn_temp.getSonNodeIteritor();
				}
			}
		}
		// System.out.println("instanceMatching:no matching!");
		return null;
	}

	/**
	 * ��������״̬��Qvalue�ķ���
	 */
	private void updateQ() {
		List<HashMap<TreeNode, Double>> qTable1 = new ArrayList<HashMap<TreeNode, Double>>();
		for (int i = 0; i < actionSize; i++) {
			qTable1.add(new HashMap<TreeNode, Double>());
			for (TreeNode tn : leafList) {
				double newQ = this.calR(i, tn);
				qTable1.get(i).put(tn, newQ + GAMMA * calTran(i, tn));
			}
		}
		this.qTable = qTable1;
	}

	/**
	 * ��ȡĳ��״̬�µ�U�ķ���
	 * 
	 * @param leaf
	 * @return
	 */
	private double calU(TreeNode leaf) {
		if (leaf == null)
			return 0;
		double U = 0.0;
		for (int i = 0; i < actionSize; i++) {
			if (qTable.get(i).containsKey(leaf) == true)
				U = Math.max(U, qTable.get(i).get(leaf));
			else {
				U = Math.max(U, qTable.get(i).get(leaf.fatherNode));
			}
		}
		return U;
	}

	/**
	 * ����ת�������sum(p*U)�ķ���
	 * 
	 * @param actionIndex
	 * @param leaf
	 * @return
	 */
	private double calTran(int actionIndex, TreeNode leaf) {
		int num = 0;
		double rewardSum = 0;
		Instance in_temp;
		Iterator<Instance> intr = leaf.instanceList.iterator();
		while (intr.hasNext()) {
			in_temp = intr.next();
			if (in_temp.getNextInstance() != null && in_temp.getNextInstance().getAction() == actionIndex) {
				num++;
				rewardSum += calU(instanceMatching(in_temp.getNextInstance()));
			}
		}
		if (num == 0)
			return 0.0;
		return rewardSum / num;

	}

	/**
	 * ������������R(s��a)�ķ���
	 * 
	 * @param actionIndex
	 * @param leaf
	 * @return
	 */
	private double calR(int actionIndex, TreeNode leaf) {
		int num = 0;
		double rewardSum = 0;
		Instance in_temp;
		Iterator<Instance> intr = leaf.instanceList.iterator();
		while (intr.hasNext()) {
			in_temp = intr.next();
			if (in_temp.getNextInstance() != null && in_temp.getNextInstance().getAction() == actionIndex) {
				num++;
				rewardSum += in_temp.getNextInstance().getReward();
			}
		}
		if (num == 0)
			return 0.0;
		return rewardSum / num;
	}

	// private void printQvalueTable() {
	// for (int i = 0; i < actionSize; i++) {
	// System.out.print("A" + i + ":\t");
	// Iterator<TreeNode> ltr = this.leafList.iterator();
	// while (ltr.hasNext()) {
	// TreeNode tn = ltr.next();
	// if (this.qTable.get(i).get(tn) != 0)
	// System.out.print(
	// "(" + tn.getIndex() + "," + String.format("%.2f", this.qTable.get(i).get(tn))
	// + ")\t");
	// }
	// System.out.println();
	// }
	// }

	/**
	 * �����Ľ���KS���鷽�� 1.����ÿ������ʵ��������KS���� 2.����KS����ʱ��ֻ�����������ھ�ֵ��son�Ż����KS����
	 * 3.��������KS�����son���ϣ�����˫�رȽ� 4.������������й���
	 */
	public void improveKSMode1() {
		int instanceAddition = this.instanceList.size() - this.lastInstanceNum;
		// 1.����ÿ������ʵ��������KS����,��һ�μ���100��instance��֮��ÿ����20%�������ͼ���һ��
		if ((this.lastInstanceNum == 0 && instanceAddition > 10)
				|| (instanceAddition / this.instanceList.size() > 0.2)) {
			this.lastInstanceNum = this.instanceList.size();
			ArrayList<TreeNode> promotableLeaf = new ArrayList<TreeNode>();
			// ��������leaf������Q�ֲ�
			for (TreeNode tn : this.leafList) {
				ArrayList<TreeNode> doubleCheckList = new ArrayList<TreeNode>();
				Double[] data = this.calQDistribution(tn);
				// ����leaf������son���ȷ���ʵ��
				for (TreeNode son : tn.sonNode) {
					for (Instance in : tn.instanceList) {
						if (in.getLastInstance() != null) {
							if ((tn.getDepth() % 2 == 1 && in.getLastInstance().getAction() == son.getIndex())
									|| (tn.getDepth() % 2 == 0
											&& in.getLastInstance().getObservation() == son.getIndex())) {
								son.instanceList.add(in);
							}
						}
					}
					// ����ʵ��������son�о�ֵ�Ĳ���KS����,�ȼ���son��Q�ֲ�
					if (son.instanceList.size() < tn.instanceList.size() / tn.sonNode.size()) {
						continue;
					}
					Double[] data2 = this.calQDistribution(son);
					// ��son��Q�ֲ���leaf���жԱȣ�����ͨ����leaf����promotableLeaf��ͨ����son����doubleCheckList
					int n1 = data.length;
					int n2 = data2.length;
					int j1 = 0, j2 = 0;
					double fn1 = 0.0, fn2 = 0.0;
					double en1 = n1;
					double en2 = n2;
					double d = 0.0;

					while (j1 < n1 && j2 < n2) {
						while ((j1 < n1 - 1) && data[j1].equals(data[j1 + 1]))
							j1++;
						while ((j2 < n2 - 1) && data2[j2].equals(data2[j2 + 1]))
							j2++;
						double d1 = data[j1].doubleValue();
						double d2 = data2[j2].doubleValue();
						if (d1 <= d2)
							fn1 = ++j1 / en1;
						if (d2 <= d1)
							fn2 = ++j2 / en2;
						double dt = Math.abs(fn2 - fn1);
						if (dt > d)
							d = dt;
					}
					if (d > D) {
						promotableLeaf.add(tn);
					} else {
						doubleCheckList.add(son);
					}

				}
				// ����ͨ��KS�����son������˫�ؼ���
				if (doubleCheckList.size() > 1) {
					for (int i = 0; i < doubleCheckList.size(); i++) {
						for (int j = i + 1; j < doubleCheckList.size(); j++) {
							Double[] data1 = this.calQDistribution(doubleCheckList.get(i));
							Double[] data2 = this.calQDistribution(doubleCheckList.get(j));
							int n1 = data1.length;
							int n2 = data2.length;
							int j1 = 0, j2 = 0;
							double fn1 = 0.0, fn2 = 0.0;
							double en1 = n1;
							double en2 = n2;
							double d = 0.0;
							while (j1 < n1 && j2 < n2) {
								while ((j1 < n1 - 1) && data1[j1].equals(data1[j1 + 1]))
									j1++;
								while ((j2 < n2 - 1) && data2[j2].equals(data2[j2 + 1]))
									j2++;
								double d1 = data1[j1].doubleValue();
								double d2 = data2[j2].doubleValue();
								if (d1 <= d2)
									fn1 = ++j1 / en1;
								if (d2 <= d1)
									fn2 = ++j2 / en2;
								double dt = Math.abs(fn2 - fn1);
								if (dt > d)
									d = dt;
							}
							if (d > D) {
								promotableLeaf.add(tn);
							}
						}
					}
				}
			}
			// ��������Ҫpromote��leaf��������
			for (TreeNode tn : promotableLeaf) {
				this.promoteLeaf(tn);
			}

		}
	}

	// public void improveKSMode1() {
	// int instanceAddition = this.instanceList.size() - this.lastInstanceNum;
	// // 1.����ÿ������ʵ��������KS����,��һ�μ���100��instance��֮��ÿ����20%�������ͼ���һ��
	// if ((this.lastInstanceNum == 0 && instanceAddition > 100)
	// || (instanceAddition / this.instanceList.size() > 0.2)) {
	// this.lastInstanceNum = this.instanceList.size();
	// ArrayList<TreeNode> promotableLeaf = new ArrayList<TreeNode>();
	// // ��������leaf������Q�ֲ�
	// for (TreeNode tn : this.leafList) {
	// ArrayList<TreeNode> doubleCheckList = new ArrayList<TreeNode>();
	// double[] distribution = this.calQDistribution(tn);
	// // ����leaf������son���ȷ���ʵ��
	// for (TreeNode son : tn.sonNode) {
	// for (Instance in : tn.instanceList) {
	// if (in.getLastInstance() != null) {
	// if ((tn.getDepth() % 2 == 1 && in.getLastInstance().getAction() ==
	// son.getIndex())
	// || (tn.getDepth() % 2 == 0
	// && in.getLastInstance().getObservation() == son.getIndex())) {
	// son.instanceList.add(in);
	// }
	// }
	// }
	// // ����ʵ��������son�о�ֵ�Ĳ���KS����,�ȼ���son��Q�ֲ�
	// if (son.instanceList.size() < tn.instanceList.size() / tn.sonNode.size()) {
	// continue;
	// }
	// double[] distribution_son = this.calQDistribution(son);
	// // ��son��Q�ֲ���leaf���жԱȣ�����ͨ����leaf����promotableLeaf��ͨ����son����doubleCheckList
	// boolean isPromote = false;
	// // ���޸Ĵ���
	// // for (int i = 0; i < SPLIT; i++) {
	// // if (Math.abs(distribution[i] - distribution_son[i]) > 1 / SPLIT) {
	// // promotableLeaf.add(tn);
	// // isPromote = true;
	// // }
	// // }
	// if (isPromote == false) {
	// doubleCheckList.add(son);
	// }
	// }
	// // ����ͨ��KS�����son������˫�ؼ���
	// if (doubleCheckList.size() > 1) {
	// for (int i = 0; i < doubleCheckList.size(); i++) {
	// for (int j = i + 1; j < doubleCheckList.size(); j++) {
	// double[] sonA = this.calQDistribution(doubleCheckList.get(i));
	// double[] sonB = this.calQDistribution(doubleCheckList.get(j));
	// // ���޸Ĵ���
	// // for (int s = 0; s < SPLIT; s++) {
	// // if (Math.abs(sonA[s] - sonB[s]) > 1 / SPLIT) {
	// // promotableLeaf.add(tn);
	// // }
	// // }
	// }
	// }
	// }
	// }
	// // ��������Ҫpromote��leaf��������
	// for (TreeNode tn : promotableLeaf) {
	// this.promoteLeaf(tn);
	// }
	//
	// }
	// }
	/**
	 * ����Q�ֲ����㷨
	 * 
	 * @param tn
	 * @return
	 */
	private Double[] calQDistribution(TreeNode tn) {
		ArrayList<Double> qvalueList = new ArrayList<Double>();
		double qMax = Double.NEGATIVE_INFINITY;
		double qMin = Double.POSITIVE_INFINITY;
		// ����leaf������ʵ����ͳ��Q
		for (Instance in : tn.instanceList) {
			if (in.getNextInstance() != null) {
				TreeNode temp = instanceMatching(in.getNextInstance());
				if (temp != null) {
					double Q = in.getReward() + this.GAMMA * this.calU(instanceMatching(in.getNextInstance()));
					qvalueList.add(Q);
					if (Q > qMax)
						qMax = Q;
					if (Q < qMin)
						qMin = Q;
				}
			}
		}
		Double[] set = new Double[qvalueList.size()];
		int i = 0;
		for (double d : qvalueList) {
			set[i++] = d;
		}
		Arrays.sort(set);
		return set;
	}

	// private double[] calQDistribution(TreeNode tn) {
	// double[] distribution = new double[SPLIT];
	// ArrayList<Double> qvalueList = new ArrayList<Double>();
	// double qMax = Double.NEGATIVE_INFINITY;
	// double qMin = Double.POSITIVE_INFINITY;
	// // ����leaf������ʵ����ͳ��Q
	// for (Instance in : tn.instanceList) {
	// if (in.getLastInstance() != null) {
	// TreeNode temp = instanceMatching(in.getNextInstance());
	// if (temp != null) {
	// double Q = in.getReward() + this.GAMMA *
	// this.calU(instanceMatching(in.getNextInstance()));
	// qvalueList.add(Q);
	// if (Q > qMax)
	// qMax = Q;
	// if (Q < qMin)
	// qMin = Q;
	// }
	// }
	// }
	// // ͳ��leaf��ʵ����Q�ϵķ�
	// for (Double q : qvalueList) {
	// if (q.equals(qMax))
	// distribution[SPLIT - 1]++;
	// else if (q.equals(qMin))
	// distribution[0]++;
	// else {
	// distribution[(int) ((q - qMin) / (qMax - qMin) * SPLIT)]++;
	// }
	// }
	// for (int i = 0; i < distribution.length; i++) {
	// distribution[i] /= qvalueList.size();
	// }
	// return distribution;
	// }

	private void promoteLeaf(TreeNode tn) {
		// ���޸�curState
		if (curState.equals(tn)) {
			for (TreeNode son : tn.sonNode) {
				if (tn.getDepth() % 2 == 1) {
					if (this.instanceList.get(this.instanceList.size() - 1).getLastInstance().getAction() == son
							.getIndex()) {
						curState = son;
					}
				} else {
					if (this.instanceList.get(this.instanceList.size() - 1).getLastInstance().getObservation() == son
							.getIndex()) {
						curState = son;
					}
				}

			}
		}
		Iterator<TreeNode> tntr;
		Iterator<Instance> intr;
		this.leafList.remove(tn);
		tn.setLeaf(false);
		tntr = tn.sonNode.iterator();
		// �����е�sonNode����leafList
		while (tntr.hasNext()) {
			TreeNode tn_temp = tntr.next();
			this.leafList.add(tn_temp);
			tn_temp.setLeaf(true);
			tn_temp.buildFringe(tn_temp, FRINGE_DEPTH, this.actionSize, this.observationSize);
		}
		intr = tn.instanceList.iterator();
		while (intr.hasNext()) {
			Instance in_temp = intr.next();
			tntr = tn.sonNode.iterator();
			while (tntr.hasNext()) {
				TreeNode tn_son = tntr.next();
				if ((tn_son.getDepth() % 2 == 0 && in_temp.getAction() == tn_son.getIndex())
						|| tn_son.getDepth() % 2 != 0 && in_temp.getObservation() == tn_son.getIndex()) {
					tn_son.instanceList.add(in_temp);
				}
			}
		}
		tn.instanceList.clear();
		this.updateQ();
		if (tn.getDepth() > this.fringeDepth)
			this.fringeDepth = tn.getDepth();
		// System.out.println(" ****fringe promote and Qvalue update,depth:" +
		// (tn.getDepth() + 1) + "****");
	}

	public void printQvalueTable() {
		for (int i = 0; i < actionSize; i++) {
			System.out.print("A" + i + ":\t");
			Iterator<TreeNode> ltr = this.leafList.iterator();
			while (ltr.hasNext()) {
				TreeNode tn = ltr.next();
				if (this.qTable.get(i).get(tn) != 0)
					System.out.print(
							"(" + tn.getIndex() + "," + String.format("%.2f", this.qTable.get(i).get(tn)) + ")\t");
			}
			System.out.println();
		}
	}
}
