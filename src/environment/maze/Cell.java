package environment.maze;

public class Cell {
	public boolean isExist;
	public boolean isGoal;

	// isStart��ʾ�Ƿ�������Ϊ���
	public boolean isStart;

	public int standardO;
	public double specialBonus;

	public Cell() {
		this.isExist = false;
	}
}
