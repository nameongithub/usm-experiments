package algorithm;

public interface Algorithm {
	/**
	 * ������ʷ��¼�Ľӿڷ�����������ִ�е����ݴ����㷨����д�������
	 * 
	 * @param actionIndex
	 * @param reward
	 */
	public void generateInstance(int actionIndex, int observationIndex, double reward); //��USMݔ��һ��������

	public int getStateNum(); //�@ȡUSM���r�Ġ�B������

	public int makeDecision(); //�@ȡUSM�ěQ�߄�����
	public double getADR();  //�@ȡUSM�Į�ǰADR��


	// ����������ÿһ��course��ʼʱ�������,����newO����һ��ĩ��ʵ������isTarget��Ϊfalse
	public void newStart(int newO); //ݔ��һ���_ʼ�Č�����

	public void printQvalueTable();
}
