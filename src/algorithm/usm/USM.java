package algorithm.usm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import algorithm.Algorithm;
import algorithm.usm.Instance;

/**
 * 这说明当前KS检验的方法存在问题，没有起到作用，甚至是起了反作用
 * 
 * @author William
 *
 */
public class USM implements Algorithm {
	// private final double EPS = 0.1;
	public static int FRINGE_DEPTH = 1; // 边缘结点构建层数
	private List<Instance> instanceList;
	private List<TreeNode> leafList;
	private List<HashMap<TreeNode, Double>> qTable;
	private SuffixTree suffixTree;
	private int observationSize;
	private int actionSize;
	private TreeNode curState;
	private boolean turnable = true;

	// 统计数据，事实上这个并没有被用到
	public int fringeDepth = 1;

	// isTarget表示是否确定唯一的curState，用于抵达goal后的再次学习，此时环境会给随机到一个非goal的cell
	// isTarget置为false，随后每次makeDecision()都随机选取a执行（不将instance加入tree），直到curState唯一
	//
	// 當isTarget為false的時候，表示在新的一趟中，根據實例還無法確定是當前處於哪一個狀態。curState即currentState。故在輸出策略的時候，隨機選擇a執行。並且這個實例不加入到實例樹中去。
	private boolean isTarget;

	private double GAMMA = 0.9; // 折扣值取0.9
	private double EPSILON = 0.5; // 探索-决策系数取0.5
	private double D = 0.1;

	/**
	 * USM算法类初始化方法，会请求两个int型变量得到O和A集的大小
	 *
	 *
	 * @param observationSize
	 * @param actionSize
	 */
	public USM(int observationSize, int actionSize) {
		this.observationSize = observationSize; //設置觀察集的大小。
		this.actionSize = actionSize; //設置動作集的大小。
		this.suffixTree = new SuffixTree(this.actionSize, this.observationSize); //創建一個後綴樹。
		this.instanceList = new ArrayList<Instance>(); //創建實例歷史記錄列表。
		this.leafList = new ArrayList<TreeNode>(); //創建葉節點列表。
		this.leafListInit(); //初始化葉節點列表。也就是把suffixTree的葉節點引用取出來，放在leafList中。
		this.qTable = new ArrayList<HashMap<TreeNode, Double>>();
		//創建出Q值表。
        //這個ArrayList的元素個數就是actionSize。所以。qtable.get(3).get(treeNode)的意思就是“狀態treeNode,3號動作的Q值。”


        //初始化Q值表，全部初始化為0。
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

	/**
	 * 根据执行结果生成instance
	 */
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
		// System.out.println("current state: " + this.getLeafName(curState));
		if (curState == null) {
			isTarget = false;
			// System.out.println("***********************************error1");
		}
		if (this.isTarget == true) {
			if (curState != null) {
				instancePutting(in, curState);
			}

		} else {
			// 匹配结果不为null且是一个leaf
			// 如果信息不足，返回的一定是null;此时应当继续尝试
			if (curState != null && this.leafList.contains(curState)) {
				// System.out.println("****set isTarget true****");
				this.isTarget = true;
			}
		}
	}

	/**
	 * 运行结束时返回总状态数目
	 */
	public int getStateNum() {
		return this.leafList.size();
	}

	/**
	 * 决策方法，根据当前状况和历史记录向agent提出方案
	 */
	public int makeDecision() {
		// 学习选取动作的时候不选择WAIT动作，因为会大大降低效率
		int actionIndex = -1;
		double rand = Math.random();
		if (this.isTarget == false || rand < this.EPSILON) {
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
			// this.printQvalueTable();
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

	/**
	 * 结束时返回ADR
	 */
	public double getADR() {
		// System.out.println("deepest fringe:" + String.valueOf(this.fringeDepth + 1));
		double sum = 0;
		for (Instance in : this.instanceList) {
			if (in.getLastInstance() != null) {
				sum += this.calDR(in, 0);
				//sum += in.getReward();
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

	/**
	 * 抵达终点后开始下一次训练，知道明确当前所处的状态之后才会把实例放入词缀树。
	 * 一次實驗可能會有很多趟開始。每一趟開始的時候輸入開始位置的觀察集合。
	 * 不理會在該開始位置的收益值，只是記錄為0。
	 */
	public void newStart(int newO) {
		// 随机运行n步，直到可以明确定位curState
		this.isTarget = false;
		// System.out.println("****set isTarget false****");
		// 构建一个实例加入序列，该实例不计入统计
		Instance in = new Instance(null, -1, newO, 0);
		this.instanceList.add(in);

	}

	/**
	 * 叶节点集合，也就是状态集合的初始化
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
	 * 放入instance的方法,随后会进行一次Q值更新和一次边缘结点检查
	 * 
	 * @param in
	 * @param tn
	 */
	private void instancePutting(Instance in, TreeNode tn) {
		if (tn.isLeaf()) {
			tn.getInstanceList().add(in);
			// System.out.println("put into: " + this.getLeafName(curState));
			updateQ();
			this.matchingTest();
			// if (checkFringe(tn)) {
			// curState = this.instanceMatching(in);
			// if (curState == null) {
			// isTarget = false;
			// // System.out.println("*******************************error");
			// }
			// }
		} else {
			System.out.println("instancePutting error!");
		}
	}

	/**
	 * instance的词缀树结点匹配算法,根据instance找到对应的leaf
	 * 
	 * @param in
	 */
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
				// System.out.println("index:" + index + "\t" + "nodeIndex:" + node.getIndex());
				if (node.getIndex() == index) {
					found = true;

					if (!node.equals(tn)) {
						tn_temp = node;
						// if (tn_temp.sonNode.size() == 0) {
						// System.out.println("warning");
						// System.out.println("tn:" + tn.getDepth() + "," + tn.getIndex());
						// System.out.println("found:" + tn_temp.getFatherNode().getDepth() + ","
						// + tn_temp.getFatherNode().getIndex());
						// }
						if (depth % 2 == 0) {
							in_temp = in_temp.getLastInstance() != null ? in_temp.getLastInstance() : null;
						}
						depth++;
						break;
					} else {
						// System.out.println("tn:" + tn.getDepth() + "," + tn.getIndex());
						// System.out.println("found:" + node.getDepth() + "," + node.getIndex());
						return in_temp;
					}
				}
			}
			if (!found) {
				System.out.println("error: cannot find history node");
				// this.historyMatching(tn, in);
				return null;
			}
		}
	}

	/**
	 * 更新所有状态的Qvalue的方法
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
	 * 获取某个状态下的U的方法
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
	 * 计算转移收益和sum(p*U)的方法
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
	 * 计算立即收益R(s，a)的方法
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

	/**
	 * 统计一个节点下实例的qvalue值
	 * 
	 * @param tn
	 * @return
	 */
	private Double[] calQDistribution(TreeNode tn) {
		ArrayList<Double> qvalueList = new ArrayList<Double>();
		// 遍历leaf的所有实例，统计Q
		for (Instance in : tn.instanceList) {
			if (in.getNextInstance() != null) {
				TreeNode temp = instanceMatching(in.getNextInstance());
				if (temp != null) {
					double Q = in.getReward() + this.GAMMA * this.calU(instanceMatching(in.getNextInstance()));
					qvalueList.add(Q);
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

	/**
	 * 边缘结点检查方法，在每一次instance放入的时候检查当前leaf下的几个fringe
	 * 
	 * @param tn
	 */
	private boolean checkFringe(TreeNode tn) {
		boolean isPromote = false;
		Double[] data1 = this.calQDistribution(tn);
		// System.out.println("history matching......");
		for (TreeNode son : tn.sonNode) {
			for (Instance in : tn.instanceList) {
				Instance in_his = this.historyMatching(tn, in);
				if ((tn.getDepth() % 2 == 1 && in_his.getAction() == son.getIndex())
						|| (tn.getDepth() % 2 == 0 && in_his.getLastInstance().getObservation() == son.getIndex())) {
					if (!son.instanceList.contains(in))
						son.instanceList.add(in);
				}
			}
			Double[] data2 = this.calQDistribution(son);
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
				isPromote = true;
			}
		}
		if (isPromote == true) {
			this.leafList.remove(tn);
			tn.setLeaf(false);
			for (TreeNode tn_temp : tn.sonNode) {
				this.leafList.add(tn_temp);
				tn_temp.setLeaf(true);
				tn_temp.buildFringe(tn_temp, FRINGE_DEPTH, this.actionSize, this.observationSize);
			}
			for (Instance in_temp : tn.instanceList) {
				for (TreeNode tn_son : tn.sonNode) {
					Instance in_his = this.historyMatching(tn, in_temp);
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
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 打印q（s，a）表中不为零的项
	 */
	public void printQvalueTable() {
		for (int i = 0; i < actionSize; i++) {
			System.out.println("A" + i + ":\t");
			Iterator<TreeNode> ltr = this.leafList.iterator();
			while (ltr.hasNext()) {
				TreeNode tn = ltr.next();
				if (this.qTable.get(i).get(tn) != 0)
					System.out.println(String.format('\t' + "%.2f", this.qTable.get(i).get(tn)) + '\t'
							+ tn.instanceList.size() + '\t' + this.getLeafName(tn));
			}
			System.out.println();
		}
	}

	/**
	 * 返回一个状态从root到叶节点的词缀
	 * 
	 * @param leaf
	 * @return
	 */
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
	 * 判断一个a是否是合理的；当前不接受相邻两步走回头路 该方法可能是ADR偏低的原因
	 * 
	 * @param a
	 * @param o
	 * @param lastA
	 * @return
	 */
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

	private void matchingTest() {
		for (TreeNode tn : this.leafList) {
			for (Instance in : tn.instanceList) {
				Instance in_his = this.historyMatching(tn, in);
				if (in_his == null) {
					System.out.println("pause!error!");
					System.out.println(this.getLeafName(tn));
				}
			}
		}
	}
}
