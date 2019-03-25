package algorithm.usm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TreeNode {
	private int depth; //�@��TreeNode����ȡ�
	private int index; //�@��INDEX�ǹ��c�Ę˻`�������findex=1��ʾ�^���1���߄�����1�����w���^��߀�Ǆ���Ҫ����depth�ǻ���߀��ż���Q����
	private boolean isLeaf; //�Ƿ����~���c��
	// private boolean fringePermit;

	public List<Instance> instanceList; //ÿ�����c������һ���������ϡ�
	public List<TreeNode> sonNode; //�@���@�����c���ӹ��c��
	public TreeNode fatherNode; //�@�����c�ĸ����c��

	/*
	 * ���ֳ�ʼ���������������ĸ��ӽڵ��ã����������ĸ�root��
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
	 * ������Ե���ķ������Եݹ鷽������
	 * 
	 * @param fringeDepth
	 */
	public void buildFringe(TreeNode root, int fringeDepth, int asz, int osz) {
		if (fringeDepth <= 0)
			return;
		if (root.depth == 0)
			return;
		if (root.depth % 2 == 1) {//Ҫ����fringe�ĸ��H���c����^�켯�ϵČӡ����������fringeҪ�Ǆ����ӡ�
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