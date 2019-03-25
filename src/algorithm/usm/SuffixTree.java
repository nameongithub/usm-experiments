package algorithm.usm;

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

		//創建和觀察集合數量相同的葉節點。這是初始化的時候運行的。
		for (int i = 0; i < observationSize; i++) {
			TreeNode tn = new TreeNode(this.root, i);
			// tn.setFringePermit(true);
			tn.setLeaf(true);
			tn.buildFringe(tn, FRINGE_DEPTH, actionSize, observationSize); //建立邊緣節點。
			root.sonNode.add(tn); //加入到根節點的子節點列表中去。
		}
	}

}
