package algorithm.KSIP_usm;

/**
 * 将root包装的原因是方便拓展
 * 
 * @author William
 *
 */
public class SuffixTree {
	public static int FRINGE_DEPTH = 1; // 边缘结点构建层数
	public TreeNode root;

	public SuffixTree(int actionSize, int observationSize) {
		this.buildTree(actionSize, observationSize);
	}

	/**
	 * 初始化构建词缀树的方法
	 * 
	 * @param observationSize
	 */
	private void buildTree(int actionSize, int observationSize) {
		this.root = new TreeNode();
		for (int i = 0; i < observationSize; i++) {
			TreeNode tn = new TreeNode(this.root, i);
			// tn.setFringePermit(true);
			tn.setLeaf(true);
			tn.buildFringe(tn, FRINGE_DEPTH, actionSize, observationSize);
			root.sonNode.add(tn);
		}
	}

}
