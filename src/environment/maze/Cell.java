package environment.maze;

public class Cell {
	public boolean isExist;
	public boolean isGoal;

	// isStart表示是否允许作为起点
	public boolean isStart;

	public int standardO;
	public double specialBonus;

	public Cell() {
		this.isExist = false;
	}
}
