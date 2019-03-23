package environment;

import java.util.HashMap;

public interface Environment {
	public HashMap<String, Integer> getActionList();

	public HashMap<String, Integer> getObservationList();

	public boolean execute(int actionIndex);

	public int getLastO();

	public double getLastR();

	public boolean isGoal();

	// 启动方法，每一个course开始时都会进行,目的是随机得一个起点并返回它的标准观察
	public int newStart();

	public void outputMaze(String path);
}
