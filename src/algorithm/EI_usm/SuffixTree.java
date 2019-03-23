package algorithm.EI_usm;

public class SuffixTree {
	public TreeNode root;

	public SuffixTree(int actionSize, int observationSize) {
		this.buildTree(actionSize, observationSize);
	}

	/**
	 * ��ʼ��������׺���ķ���
	 * 
	 * @param observationSize
	 */
	private void buildTree(int actionSize, int observationSize) {
		this.root = new TreeNode();
		for (int i = 0; i < observationSize; i++) {
			TreeNode tn = new TreeNode(this.root, i);
			tn.setLeaf(true);
			tn.buildFringe(tn, actionSize, observationSize);
			root.sonNode.add(tn);
		}
	}

}
