package algorithm.usm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TreeNode {
	private int depth; //這個TreeNode的深度。
	private int index; //每個節點都有一個唯一的index,作為id。根節點rootNode是-1。
	private boolean isLeaf; //是否是葉節點。
	// private boolean fringePermit;

	public List<Instance> instanceList; //每個節點都會有一個實例集合。
	public List<TreeNode> sonNode; //這是這個節點的子節點。
	public TreeNode fatherNode; //這個節點的父節點。

	/*
	 * 两种初始化方法，带参数的给子节点用，不带参数的给root用
	 */
	public TreeNode(TreeNode fatherNode, int index) {
		this.fatherNode = fatherNode;
		this.depth = fatherNode.getDepth() + 1;
		this.index = index;
		this.isLeaf = false;
		// this.fringePermit = false;
		this.listInit();
	}

	public TreeNode() {
		this.depth = 0;
		this.index = -1;
		this.isLeaf = false;
		this.fatherNode = null;
		// this.fringePermit = false;
		this.listInit();
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
	public void buildFringe(TreeNode root, int fringeDepth, int asz, int osz) {
		if (fringeDepth <= 0)
			return;
		if (root.depth == 0)
			return;
		if (root.depth % 2 == 1) {
			for (int i = 0; i < asz; i++) {
				TreeNode son = new TreeNode(root, i);
				root.sonNode.add(son);
				son.buildFringe(son, fringeDepth - 1, asz, osz);

			}
		} else {
			for (int i = 0; i < osz; i++) {
				TreeNode son = new TreeNode(root, i);
				root.sonNode.add(son);
				son.buildFringe(son, fringeDepth - 1, asz, osz);
			}
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

	// public boolean isFringePermit() {
	// return fringePermit;
	// }
	//
	// public void setFringePermit(boolean fringePermit) {
	// this.fringePermit = fringePermit;
	// }

	public int getDepth() {
		return depth;
	}

	public List<Instance> getInstanceList() {
		return instanceList;
	}

	public TreeNode getFatherNode() {
		return fatherNode;
	}

}