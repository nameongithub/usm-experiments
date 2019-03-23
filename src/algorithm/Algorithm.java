package algorithm;

public interface Algorithm {
	/**
	 * ������ʷ��¼�Ľӿڷ�����������ִ�е����ݴ����㷨����д�������
	 * 
	 * @param actionIndex
	 * @param reward
	 */
	public void generateInstance(int actionIndex, int observationIndex, double reward); //������USMݔ��һ��

	public int getStateNum();

	public int makeDecision();

	public double getADR();

	// ����������ÿһ��course��ʼʱ�������,����newO����һ��ĩ��ʵ������isTarget��Ϊfalse
	public void newStart(int newO);

	public void printQvalueTable();
}
