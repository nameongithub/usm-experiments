package algorithm;

public interface Algorithm {
	/**
	 * 传输历史记录的接口方法，将本次执行的数据传给算法类进行处理的入口
	 * 
	 * @param actionIndex
	 * @param reward
	 */
	public void generateInstance(int actionIndex, int observationIndex, double reward); //想USM輸入一個實例。

	public int getStateNum(); //獲取USM實時的狀態數量。

	public int makeDecision(); //獲取USM的決策動作。
	public double getADR();  //獲取USM的當前ADR。


	// 启动方法，每一个course开始时都会进行,根据newO建立一个末端实例并将isTarget置为false
	public void newStart(int newO); //輸入一個開始的實例。

	public void printQvalueTable();
}
