package environment;

import java.util.HashMap;

public interface Environment {
	public HashMap<String, Integer> getActionList();

	public HashMap<String, Integer> getObservationList();

	public boolean execute(int actionIndex);

	public int getLastO();

	public double getLastR();

	public boolean isGoal();

	// ����������ÿһ��course��ʼʱ�������,Ŀ���������һ����㲢�������ı�׼�۲�
	public int newStart();

	public void outputMaze(String path);
}
