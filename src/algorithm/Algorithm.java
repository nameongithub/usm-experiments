package algorithm;

public interface Algorithm {
	/**
	 * 传输历史记录的接口方法，将本次执行的数据传给算法类进行处理的入口
	 * 
	 * @param actionIndex
	 * @param reward
	 */
	public void generateInstance(int actionIndex, int observationIndex, double reward); //就是向USM輸入一個

	public int getStateNum();

	public int makeDecision();

	public double getADR();

	// 启动方法，每一个course开始时都会进行,根据newO建立一个末端实例并将isTarget置为false
	public void newStart(int newO);

	public void printQvalueTable();
}
