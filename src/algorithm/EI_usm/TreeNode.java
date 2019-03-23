package algorithm.EI_usm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TreeNode {
	private int depth;
	private int index;
	private boolean isLeaf;
	private double EI;
	// private boolean fringePermit;

	public List<Instance> instanceList;
	public List<TreeNode> sonNode;
	public TreeNode fatherNode;
	public Boolean promotable;

	/*
	 * 两种初始化方法，带参数的给子节点用，不带参数的给root用
	 */
	public TreeNode(TreeNode fatherNode, int index) {
		this.fatherNode = fatherNode;
		this.depth = fatherNode != null ? fatherNode.getDepth() + 1 : 0;
		this.index = index;
		this.isLeaf = false;
		this.listInit();
		this.promotable = true;
		this.EI = 0;
	}

	public TreeNode() {
		this.depth = 0;
		this.index = -1;
		this.isLeaf = false;
		this.fatherNode = null;
		this.listInit();
		this.promotable = false;
		this.EI = 0;
	}

	private void listInit() {
		this.instanceList = new ArrayList<Instance>();
		this.sonNode = new ArrayList<TreeNode>();
	}

	/**
	 * 构建边缘结点的方法，以递归方法进行
	 * 
	 * @param fringeDepth
	 */
	public void buildFringe(TreeNode root, int asz, int osz) {
		if (root.depth == 0)
			return;
		int num = 0;
		if (root.depth % 2 == 1) {
			num = asz;
		} else {
			num = osz;
		}
		for (int i = 0; i < num; i++) {
			TreeNode son = new TreeNode(root, i);
			root.sonNode.add(son);
		}
	}

	public TreeNode getFirstSon() {
		if (this.sonNode.size() > 0)
			return this.sonNode.get(0);
		else
			return null;
	}

	public Iterator<TreeNode> getSonNodeIteritor() {
		return this.sonNode.iterator();
	}

	// getter and setter
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	public int getDepth() {
		return depth;
	}

	public List<Instance> getInstanceList() {
		return instanceList;
	}

	public TreeNode getFatherNode() {
		return fatherNode;
	}

	public double getEI() {
		return this.EI;
	}

	public void setEI(double EI) {
		this.EI = EI;
	}

}
