package algorithm.EI_usm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import algorithm.Algorithm;
import algorithm.EI_usm.Instance;

public class EI_USM implements Algorithm {

	private double GAMMA = 0.9;
	private double EPSILON = 0.5;
	private double PHI = 0.5;
	private double THETA = 0.01;
	private double SPLIT = 0.1;

	private double lastEI_tree = 0;
	private int fringeDepth = 1;

	private List<Instance> instanceList;
	private List<TreeNode> leafList;
	private List<HashMap<TreeNode, Double>> qTable;
	private SuffixTree suffixTree;
	private int observationSize;
	private int actionSize;

	private TreeNode curState;
	private boolean isTarget;
	private boolean turnable = true;

	public EI_USM(int observationSize, int actionSize) {
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

	public void generateInstance(int actionIndex, int observationIndex, double reward) {
		Instance last = null;
		// curState已经唯一
		if (instanceList.size() != 0) {
			last = instanceList.get(instanceList.size() - 1);
		}
		Instance in = new Instance(last, actionIndex, observationIndex, reward);
		last.setNextInstance(in);
		instanceList.add(in);

		curState = instanceMatching(in);
		if (curState == null) {
			isTarget = false;
			// System.out.println("***********************************error1");
		}
		if (this.isTarget == true) {
			if (curState != null)
				instancePutting(in, curState);
		} else {
			// 匹配结果不为null且是一个leaf
			// 如果信息不足，返回的一定是null;此时应当继续尝试
			if (curState != null && this.leafList.contains(curState)) {
				// System.out.println("****set isTarget true****");
				this.isTarget = true;
			}
		}
	}

	public int getStateNum() {
		return leafList.size();
	}

	public int makeDecision() {
		// 学习选取动作的时候不选择WAIT动作，因为会大大降低效率
		int actionIndex = -1;
		double rand = Math.random();
		// System.out.println("-------------"+this.calEI_tree());

		double eps = this.EPSILON;
		if (this.curState == null) {
			eps = EPSILON;
		} else {
			double half_EI = this.calEI(curState) / 2;
			if (half_EI > EPSILON) {
				eps = EPSILON;
			} else if (half_EI < 0.1) {
				eps = 0.1;
			} else {
				eps = half_EI;
			}
		}

		if (this.isTarget == false || rand < eps) {
			// System.out.print("-R:\t");
			int o = this.instanceList.get(this.instanceList.size() - 1).getObservation();
			int a = -1;
			while (true) {
				a = (int) (Math.random() * actionSize);
				int lastA = this.instanceList.isEmpty() ? -1
						: this.instanceList.get(this.instanceList.size() - 1).getAction();
				// 和上一步相反的动作不能选,不走回头路
				if (!this.judgeActionAcceptable(a, o, lastA, false)) {
					continue;
				} else {
					break;
				}
			}
			this.turnable = true;
			return a;
		} else {
			double maxQ = Double.NEGATIVE_INFINITY;
			int o = this.instanceList.get(this.instanceList.size() - 1).getObservation();
			for (int a = 1; a < actionSize; a++) {
				int lastA = this.instanceList.isEmpty() ? -1
						: this.instanceList.get(this.instanceList.size() - 1).getAction();
				if (!this.judgeActionAcceptable(a, o, lastA, false)) {
					continue;
				}
				if (qTable.get(a).get(curState) > maxQ) {
					actionIndex = a;
					maxQ = qTable.get(a).get(curState);
				}
			}
			// System.out.print("+UR:\t");
			this.turnable = false;
			return actionIndex;
		}
	}

	public double getADR() {
		double sum = 0;
		for (Instance in : this.instanceList) {
			if (in.getLastInstance() != null) {
				sum += this.calDR(in, 0);
				// sum += in.getReward();
			}
		}
		return sum / this.instanceList.size();
	}

	public double calDR(Instance in, int depth) {
		if (in.getReward() > 0 || depth == 20) {
			return in.getReward();
		} else if (in.getNextInstance() == null) {
			return in.getReward();
		} else {
			return in.getReward() + this.GAMMA * calDR(in.getNextInstance(), depth + 1);
		}

	}

	public void newStart(int newO) {
		// 随机运行n步，直到可以明确定位curState
		this.isTarget = false;
		// System.out.println("****set isTarget false****");
		// 构建一个实例加入序列，该实例不计入统计
		Instance in = new Instance(null, -1, newO, 0);
		this.instanceList.add(in);
	}

	private TreeNode instanceMatching(Instance in) {
		int time = 0;
		Instance in_temp = in;
		TreeNode tn_temp = this.suffixTree.root;
		int depth = 1;
		int index = -1;
		while (true) {
			if (depth % 2 == 0) {
				index = in_temp.getAction();
			} else {
				index = in_temp.getObservation();
			}
			for (TreeNode tn : tn_temp.sonNode) {
				if (tn.getIndex() == index) {
					if (tn.isLeaf()) {
						return tn;
					} else {
						tn_temp = tn;
						if (depth % 2 == 0) {
							if (in_temp.getLastInstance() != null) {
								in_temp = in_temp.getLastInstance();
							} else {
								return null;
							}
						}
						depth++;
					}
				}
			}
			time++;
			if (time > 15) {
				// System.out.println("pause");
				return null;
			}
		}
	}

	/**
	 * 已知in在历史上经过tn leaf，从实例集中找到那段历史
	 * 
	 * @param tn
	 * @param in
	 * @return
	 */
	private Instance historyMatching(TreeNode tn, Instance in) {
		Instance in_temp = in;
		TreeNode tn_temp = this.suffixTree.root;
		int depth = 1;
		int index = -1;
		while (true) {
			if (in_temp == null) {
				System.out.println("error: find a null history");
				return null;
			}
			if (depth % 2 == 0) {
				index = in_temp.getAction();
			} else {
				index = in_temp.getObservation();
			}
			boolean found = false;
			for (TreeNode node : tn_temp.sonNode) {
				if (node.getIndex() == index) {
					found = true;
					if (!node.equals(tn)) {
						tn_temp = node;
						if (depth % 2 == 0) {
							in_temp = in_temp.getLastInstance() != null ? in_temp.getLastInstance() : null;
						}
						depth++;
						break;
					} else {
						if (in_temp.getAction() == -1) {
							// System.out.println("pause!");
						}
						return in_temp;
					}
				}
			}
			if (!found) {
				System.out.println("error: cannot find history node");
				return null;
			}
		}
	}

	private void instancePutting(Instance in, TreeNode tn) {
		if (tn.isLeaf()) {
			tn.getInstanceList().add(in);
			this.updateQ();
			curState = this.instanceMatching(in);
			if (curState == null) {
				isTarget = false;
				// System.out.println("*******************************error");
			}
			if (in.getReward() > 0 || this.instanceList.size() % 50 == 0) {
				if (this.calEI_tree() > this.lastEI_tree)
					treeIteration();
			}

		} else {
			System.out.println("instancePutting error!");
		}
	}

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

	private void leafListInit() {
		Iterator<TreeNode> titr = this.suffixTree.root.getSonNodeIteritor();
		TreeNode tn_temp = null;
		while (titr.hasNext()) {
			tn_temp = titr.next();
			this.leafList.add(tn_temp);
		}
	}

	private boolean judgeActionAcceptable(int a, int o, int lastA, boolean turnable) {
		boolean b = true;
		if (a == 0) {
			b = false;
		}
		if (((a == 1) && (o == 1 || o == 3 || o == 5 || o == 9 || o == 7 || o == 11 || o == 13 || o == 15))
				|| ((a == 2) && (o == 2 || o == 3 || o == 6 || o == 10 || o == 7 || o == 11 || o == 14 || o == 15))
				|| ((a == 3) && (o == 4 || o == 5 || o == 6 || o == 12 || o == 7 || o == 13 || o == 14 || o == 15))
				|| ((a == 4) && (o == 8 || o == 9 || o == 10 || o == 12 || o == 11 || o == 13 || o == 14 || o == 15))) {
			b = false;
		} else if (o != 7 && o != 11 && o != 13 && o != 14 && !turnable) {
			if ((a == 1 && lastA == 2) || (a == 2 && lastA == 1) || (a == 4 && lastA == 3) || (a == 3 && lastA == 4)) {
				b = false;
			}
		}
		return b;
	}

	public void printQvalueTable() {
		for (int i = 0; i < actionSize; i++) {
			System.out.println("A" + i + ":\t");
			Iterator<TreeNode> ltr = this.leafList.iterator();
			while (ltr.hasNext()) {
				TreeNode tn = ltr.next();
				if (!tn.isLeaf()) {
					continue;
				}
				if (this.qTable.get(i).get(tn) != 0)
					System.out.println('\t' + "Q: " + String.format("%.2f", this.qTable.get(i).get(tn)) + '\t' + "EI: "
							+ String.format("%.2f", this.calEI(tn)) + '\t' + tn.instanceList.size() + '\t'
							+ this.getLeafName(tn));
			}
			System.out.println();
		}
	}

	private String getLeafName(TreeNode leaf) {
		String str = "";
		String temp = "";
		while (leaf.getDepth() > 0) {
			if (leaf.getDepth() % 2 == 0) {
				temp = "A" + String.valueOf(leaf.getIndex()) + "<-";
			} else {
				temp = "O" + String.valueOf(leaf.getIndex()) + "<-";
			}
			str = str.concat(temp);
			leaf = leaf.fatherNode;
		}
		temp = "root";
		str = str.concat(temp);
		return str;
	}

	/**
	 * 以下为EI_USM算法新增的方法
	 */

	/**
	 * 树形结构迭代，对当前的树形结构，选择信息熵变化最大的边缘结点提升选项进行提升；
	 */
	public void treeIteration() {
		// 先将所有叶节点设为可提升
		for (TreeNode tn : this.leafList) {
			tn.promotable = true;
		}
		this.qTable_ConvengenceUpdate();
		int num = 0;
		while (true) {
			// this.printQvalueTable();
			// 计算所有叶节点的信息熵，以及整棵树的信息熵；找出EI最大的叶节点，并记录所有叶节点的权值
			// 并记录lambda作为阈值，EI超过该值的才允许提升
			ArrayList<Double> percent = new ArrayList<Double>();
			double lambda = 0;
			TreeNode maxEINode = this.leafList.get(0);
			for (TreeNode s : this.leafList) {
				this.calEI(s);
				if (s.getEI() > maxEINode.getEI()) {
					maxEINode = s;
				}
				percent.add((double) s.instanceList.size() / (double) this.instanceList.size());
				lambda += Math.pow(percent.get(percent.size() - 1), 2) * s.getEI();
			}
			double EI_tree = this.calEI_tree();
			// 记录当前树的EI值
			this.lastEI_tree = EI_tree;
			// 寻找信息熵减最大的节点
			TreeNode maxDeltaNode = this.leafList.get(0);
			double maxDelta = 0;
			for (int i = 0; i < this.leafList.size(); i++) {
				TreeNode s = this.leafList.get(i);
				if (s.promotable == true && percent.get(i) * s.getEI() >= lambda) {
					// 估算熵减
					double deltaEI_tree = this.calDeltaEI(s, percent.get(i));
					// 比较
					if (deltaEI_tree >= maxDelta) {
						maxDelta = deltaEI_tree;
						maxDeltaNode = s;
					}
				}
			}
			// 终止条件,先定为0.01
			if (maxDelta <= THETA) {
				if (maxEINode.promotable == true && maxEINode.getEI() > PHI) {
					this.promote(maxEINode, false);
					num++;
				}
				// System.out.println("promote " + num + " times");
				break;
			}
			// 看看EI最大的节点和deltaEI最大的节点是否是同一个
			if (maxDeltaNode.equals(maxEINode)) {
				// 是，提升
				// System.out.println("-------------promote leaf:" +
				// this.getLeafName(maxDeltaNode));
				this.promote(maxDeltaNode, true);
				num++;
			} else {
				// 否，两种都提升，并将EI最大的叶节点锁定，本次迭代不继续提升
				// System.out.println("-------------promote deltaMax leaf:" +
				// this.getLeafName(maxDeltaNode));
				this.promote(maxDeltaNode, true);
				// System.out.println("-------------promote EIMax leaf:" +
				// this.getLeafName(maxEINode));
				this.promote(maxEINode, false);
				num += 2;
			}
			this.qTable_ConvengenceUpdate();
		}
	}

	/**
	 * 
	 * @param tn
	 */
	public void promote(TreeNode tn, boolean son_promotable) {
		this.leafList.remove(tn);
		tn.setLeaf(false);
		for (TreeNode tn_temp : tn.sonNode) {
			this.leafList.add(tn_temp);
			tn_temp.setLeaf(true);
			// 若son_prommotable是false，则所有新的子节点暂时无法再被提升
			if (!son_promotable) {
				tn_temp.promotable = false;
			}
			tn_temp.buildFringe(tn_temp, this.actionSize, this.observationSize);
		}

		for (Instance in_temp : tn.instanceList) {
			for (TreeNode tn_son : tn.sonNode) {
				Instance in_his = this.historyMatching(tn, in_temp);
				if (in_his.getAction() == -1 || in_his.getObservation() == -1) {
					continue;
				}
				if ((tn_son.getDepth() % 2 == 0 && in_his.getAction() == tn_son.getIndex())
						|| tn_son.getDepth() % 2 != 0
								&& in_his.getLastInstance().getObservation() == tn_son.getIndex()) {
					if (!tn_son.instanceList.contains(in_temp))
						tn_son.instanceList.add(in_temp);
				}
			}
		}
		tn.instanceList.clear();
		this.updateQ();
		// System.out.println(" ****fringe promote and Qvalue update,depth:" +
		// (tn.getDepth() + 1) + "****");
		if (tn.getDepth() > this.fringeDepth)
			this.fringeDepth = tn.getDepth();
	}

	/**
	 * 在本q table上进行贝尔曼更新直到收敛
	 */
	public void qTable_ConvengenceUpdate() {
		int n = 1;
		while (true) {
			List<HashMap<TreeNode, Double>> qTable_past = new ArrayList<HashMap<TreeNode, Double>>();
			for (int i = 0; i < actionSize; i++) {
				qTable_past.add(new HashMap<TreeNode, Double>());
				for (TreeNode tn : this.leafList) {
					qTable_past.get(i).put(tn, this.qTable.get(i).get(tn));
				}
			}
			this.updateQ();
			boolean pass = true;
			// this.printQvalueTable();
			for (int i = 0; i < actionSize; i++) {
				qTable_past.add(new HashMap<TreeNode, Double>());
				for (TreeNode tn : leafList) {
					double past = qTable_past.get(i).get(tn);
					double now = this.qTable.get(i).get(tn);
					if (Math.abs(now - past) > 0.1)
						pass = false;
				}
			}
			if (pass == true) {
				break;
			}
			n++;
		}
		// System.out.println("iteratation round " + n);
	}

	/**
	 * 计算并改变某leaf的信息熵 当前选择EI最大的动作作为代表，没有使用p*EI的加权方法；因p较低的动作，EI也较低（基本都是0） 出于严谨性还是应当更正
	 * 
	 * @param tn
	 * @return
	 */
	public double calEI(TreeNode tn) {
		double max = 0;
		double EI_tn[] = new double[this.actionSize];
		// 计算不同a下的信息熵
		for (int a = 0; a < this.actionSize; a++) {
			ArrayList<Double> qvalueList = new ArrayList<Double>();
			for (int i = 0; i < tn.instanceList.size(); i++) {
				Instance in_next = tn.instanceList.get(i).getNextInstance();
				if (in_next != null && in_next.getAction() == a) {
					double qvalue_of_i = in_next.getReward() + this.GAMMA * this.calU(instanceMatching(in_next));
					qvalueList.add(qvalue_of_i);
				}
			}
			if (qvalueList.size() <= 1) {
				EI_tn[a] = 0;
				continue;
			}
			Collections.sort(qvalueList);
			int k_min = (int) (qvalueList.get(0) / SPLIT);
			int k_max = (int) (qvalueList.get(qvalueList.size() - 1) / SPLIT);
			double Pk[] = new double[k_max - k_min + 1];
			for (Double d : qvalueList) {
				Pk[(int) (d / SPLIT) - k_min]++;
			}
			int size = qvalueList.size();
			for (double d : Pk) {
				d /= size;
				if (d != 0) {
					EI_tn[a] += -d * Math.log(d) / Math.log(2);
				}
			}
		}
		max = EI_tn[0];
		for (double d : EI_tn) {
			if (d > max) {
				max = d;
			}
		}
		tn.setEI(max);
		return max;
	}

	/**
	 * 
	 * @param tn
	 * @return
	 */
	public double calDeltaEI(TreeNode tn, double percent) {
		double last = calEI(tn);
		int num = 0;
		if (tn.getDepth() % 2 == 1) {
			num = this.actionSize;
		} else {
			num = this.observationSize;
		}
		// 构造边缘结点，先不要加入tn的子节点容器
		ArrayList<TreeNode> sonList = new ArrayList<TreeNode>();
		for (int i = 0; i < num; i++) {
			sonList.add(new TreeNode(null, i));
		}
		// 将tn的instance放入这些边缘结点里
		for (Instance in : tn.instanceList) {
			Instance in_his = this.historyMatching(tn, in);
			if (in_his.getAction() == -1 || in_his.getObservation() == -1) {
				continue;
			}
			if (tn.getDepth() % 2 == 0) {
				TreeNode tn_temp = sonList.get(in_his.getLastInstance().getObservation());
				if (tn_temp.getIndex() == in_his.getLastInstance().getObservation()) {
					tn_temp.instanceList.add(in);
				} else {
					System.out.println("index error");
				}
			} else {

				TreeNode tn_temp = sonList.get(in_his.getAction());
				if (tn_temp.getIndex() == in_his.getAction()) {
					tn_temp.instanceList.add(in);
				} else {
					System.out.println("index error");
				}
			}
		}
		// 计算deltaEI
		double sum = 0;
		for (TreeNode son : sonList) {
			sum += this.calEI(son) * (double) son.instanceList.size() / (double) tn.instanceList.size();
		}
		double delta = (last - sum) * percent;
		if (delta == 0) {
			// System.out.println(this.getLeafName(tn) + " get a zero delta!");
		} else if (delta < 0) {
			// System.out.println(this.getLeafName(tn) + " error! get a negetive delta!");
		}
		return delta;
	}

	/**
	 * 树结构的信息熵大小计算； 可以直接用this.instanceList.size()是因为末端实例的存在同时对所有s都造成了影响，对绝对值影响不大
	 * 
	 * @return
	 */
	public double calEI_tree() {
		double sum = 0;
		for (TreeNode s : this.leafList) {
			int num = s.instanceList.size();
			double percent = (double) num / (double) this.instanceList.size();
			sum += percent * this.calEI(s);
		}
		return sum;
	}

}
