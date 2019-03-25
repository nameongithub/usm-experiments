package algorithm.usm;

/**
 * ��root��װ��ԭ���Ƿ�����չ
 * 
 * @author William
 *
 */
public class SuffixTree {
	public static int FRINGE_DEPTH = 1; // ��Ե��㹹������
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

		//�������^�켯�ϔ�����ͬ���~���c���@�ǳ�ʼ���ĕr���\�еġ�
		for (int i = 0; i < observationSize; i++) {
			TreeNode tn = new TreeNode(this.root, i);
			// tn.setFringePermit(true);
			tn.setLeaf(true);
			tn.buildFringe(tn, FRINGE_DEPTH, actionSize, observationSize); //����߅�����c��
			root.sonNode.add(tn); //���뵽�����c���ӹ��c�б���ȥ��
		}
	}

}
