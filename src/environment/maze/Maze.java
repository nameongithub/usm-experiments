package environment.maze;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import environment.Environment;

/*
 * settingCode:
 * 
 * #actionList=name0,0;name1,1;...
 * #observationList=name0,0;name1,1;...
 * #range=X,...;Y,...
 * #cellList=x,y,isGoal(0/1),isStart(0/1),standradO(Number or name in obList),specialBonus(optional)
 * #goalBouns=...
 * #stepLoss=...p
 * 
 * example:
 * 
 * #actionList=
 * 		WAIT,0;LEFT,1;RIGHT,2;UP,3;DOWN,4
 * #observationList=
 * 		NO_WALL,0;L_WALL,1;R_WALL,2;U_WALL,3;D_WALL,4;LR_WALL,5;LU_WALL,6;LD_WALL,7;RU_WALL,8;
 * 		RD_WALL,9;UD_WALL,10;LRU_WALL,11;LRD_WALL,12;LUD_WALL,13;RUD_WALL,14;LRUD_WALL,15
 * #range=
 * 		X,4;Y,5
 * #cellList=
 * 		1,0,0,1,LU_WALL,0;
 * 		2,0,0,1,U_WALL,0;
 * 		3,0,0,1,RU_WALL,0;
 * 		1,1,0,1,LR_WALL,0;
 * 		3,1,0,1,LR_WALL,0;
 * 		0,2,1,0,LUD_WALL,0;
 * 		1,2,0,1,RD_WALL,0;
 * 		3,2,,0,1,RLD_WALL,0;
 * #goalBonus=5
 * #stepLoss=0.1
 */

public class Maze implements Environment {

	// private String code = "";

	// 当前环境允许的动作和观察
	private HashMap<String, Integer> actionList;
	private HashMap<String, Integer> observationList;

	// 当前环境的二维范围和地形
	private int rangeX;
	private int rangeY;
	private Cell[][] cellList;

	// 环境的终点奖励和每步损耗
	private double goalBonus;
	private double stepLoss;

	// agent的当前位置和状态
	// private int lastA;
	private int lastO;
	private double lastR;
	private int curX;
	private int curY;

	/**
	 * 环境的初始化方法，用settingCode初始化，包含允许的动作和观察，空间范围和地形等信息
	 * 
	 * @param
	 * @throws IOException
	 */
	public Maze(String s) throws IOException {
		// 从code.txt中读取编码

		String str = load(s);
		this.actionList = new HashMap<String, Integer>();
		this.observationList = new HashMap<String, Integer>();

		// 动作观察集初始化
		AInit(str);
		OInit(str);
		// 范围和地形初始化
		rangeInit(str);
		cellInit(str);
		// 终点奖励和每步损耗初始化
		bonusInit(str);
		// 当前位置和状态初始化
		// this.lastA = -1;
		this.lastO = -1;
		this.lastR = 0;
		for (int i = 0; i < this.rangeY; i++) {
			for (int j = 0; j < this.rangeX; j++) {
				System.out.print(String.valueOf(this.cellList[i][j].standardO) + '\t');
			}
			System.out.println();
		}
	}

	/**
	 * 利用算法自动生成Maze的方法，需要指定长宽，每步损耗和终点奖励,以及生成方法
	 * 
	 * @throws IOException
	 */
	public Maze(int x, int y, double stepLoss, double goalBonus, MGF m) throws IOException {
		this.cellList = new Cell[y][x];
		this.rangeX = x;
		this.rangeY = y;
		this.goalBonus = goalBonus;
		this.stepLoss = stepLoss;
		this.actionList = new HashMap<String, Integer>();
		this.observationList = new HashMap<String, Integer>();
		String code = load("code.txt");
		this.AInit(code);
		this.OInit(code);
		if (m.equals(MGF.DFS)) {
			this.mazeDFS();
		} else if (m.equals(MGF.KA)) {
			this.mazeKA();
		} else if (m.equals(MGF.PRIM)) {
			this.mazePRIM();
		}
		this.lastO = -1;
		this.lastR = 0;
		for (int i = 0; i < this.rangeY; i++) {
			for (int j = 0; j < this.rangeX; j++) {
				System.out.print(String.valueOf(this.cellList[i][j].standardO) + '\t');
			}
			System.out.println();
		}
	}

	@Override
	public HashMap<String, Integer> getActionList() {
		// TODO Auto-generated method stub
		return this.actionList;
	}

	@Override
	public HashMap<String, Integer> getObservationList() {
		// TODO Auto-generated method stub
		return this.observationList;
	}

	@Override
	public boolean execute(int actionIndex) {
		// TODO Auto-generated method stub
		if (actionIndex == 0) {
			this.lastO = cellList[curY][curX].standardO;
			this.lastR = (cellList[curY][curX].isGoal ? this.goalBonus : 0) + this.stepLoss
					+ cellList[curY][curX].specialBonus;
		} else if (actionIndex == 1) {
			if (curX != 0 && cellList[curY][curX - 1].isExist) {
				curX--;
				// this.lastA = actionIndex;
				this.lastO = cellList[curY][curX].standardO;
				this.lastR = (cellList[curY][curX].isGoal ? this.goalBonus : 0) + this.stepLoss
						+ cellList[curY][curX].specialBonus;
			} else {
				System.out.println("left: <Collide the wall>");
				return false;
			}
		} else if (actionIndex == 2) {
			if (curX != this.rangeX - 1 && cellList[curY][curX + 1].isExist) {
				curX++;
				// this.lastA = actionIndex;
				this.lastO = cellList[curY][curX].standardO;
				this.lastR = (cellList[curY][curX].isGoal ? this.goalBonus : 0) + this.stepLoss
						+ cellList[curY][curX].specialBonus;

			} else {
				System.out.println("right: <Collide the wall>");
				return false;
			}
		} else if (actionIndex == 3) {
			if (curY != 0 && cellList[curY - 1][curX].isExist) {
				curY--;
				// this.lastA = actionIndex;
				this.lastO = cellList[curY][curX].standardO;
				this.lastR = (cellList[curY][curX].isGoal ? this.goalBonus : 0) + this.stepLoss
						+ cellList[curY][curX].specialBonus;

			} else {
				System.out.println("up: <Collide the wall>");
				return false;
			}
		} else if (actionIndex == 4) {
			if (curY != this.rangeY - 1 && cellList[curY + 1][curX].isExist) {
				curY++;
				// this.lastA = actionIndex;
				this.lastO = cellList[curY][curX].standardO;
				this.lastR = (cellList[curY][curX].isGoal ? this.goalBonus : 0) + this.stepLoss
						+ cellList[curY][curX].specialBonus;

			} else {
				System.out.println("DOWN: <Collide the wall>");
				return false;
			}
		}
		// System.out.println(this.listKeyFind(actionList, actionIndex) + " to (" +
		// this.curX + "," + this.curY+ "),get O as " + this.lastO);
		return true;
	}

	@Override
	public int getLastO() {
		// TODO Auto-generated method stub
		return this.lastO;
	}

	@Override
	public double getLastR() {
		// TODO Auto-generated method stub
		return this.lastR;
	}

	@Override
	public boolean isGoal() {
		// TODO Auto-generated method stub
		return cellList[curY][curX].isGoal;
	}

	@Override
	public int newStart() {

		// TODO Auto-generated method stub
		// this.lastA = -1;
		this.lastO = -1;
		this.lastR = 0;
		int xs = -1, ys = -1;
		while (true) {
			xs = (int) (Math.random() * rangeX);
			ys = (int) (Math.random() * rangeY);
			if (this.cellList[ys][xs].isExist && this.cellList[ys][xs].isStart && !this.cellList[ys][xs].isGoal) {
				break;
			}
		}
		this.curX = xs;
		this.curY = ys;
		// System.out.println("new start,locate at (" + xs + "," + ys + ")");
		return cellList[curY][curX].standardO;
	}

	private void AInit(String code) {
		String cuttingCode = "";
		Pattern pattern = Pattern.compile("(actionList=).*ENDA");
		Matcher matcher = pattern.matcher(code);
		while (matcher.find()) {
			cuttingCode = matcher.group(0);
		}
		pattern = Pattern.compile("(\\w+),(\\d+)");
		matcher = pattern.matcher(cuttingCode);
		while (matcher.find()) {
			String name = matcher.group(1);
			int num = Integer.parseInt(matcher.group(2));
			this.actionList.put(name, num);
		}
	}

	private void OInit(String code) {
		String cuttingCode = "";
		Pattern pattern = Pattern.compile("(observationList=).*ENDO");
		Matcher matcher = pattern.matcher(code);
		while (matcher.find()) {
			cuttingCode = matcher.group(0);
		}
		pattern = Pattern.compile("(\\w+),(\\d+)");
		matcher = pattern.matcher(cuttingCode);
		while (matcher.find()) {
			String name = matcher.group(1);
			int num = Integer.parseInt(matcher.group(2));
			this.observationList.put(name, num);
		}
	}

	private void rangeInit(String code) {
		String cuttingCode = "";
		Pattern pattern = Pattern.compile("(range=).*ENDR");
		Matcher matcher = pattern.matcher(code);
		while (matcher.find()) {
			cuttingCode = matcher.group(0);
		}
		pattern = Pattern.compile("([XYxy]),(\\d+)");
		matcher = pattern.matcher(cuttingCode);
		while (matcher.find()) {
			String name = matcher.group(1);
			int num = Integer.parseInt(matcher.group(2));
			if (name.equalsIgnoreCase("x")) {
				this.rangeX = num;
			} else {
				this.rangeY = num;
			}
		}
		this.cellList = new Cell[this.rangeY][this.rangeX];
		for (int i = 0; i < this.rangeX; i++) {
			for (int j = 0; j < this.rangeY; j++) {
				cellList[j][i] = new Cell();
			}
		}
	}

	private void cellInit(String code) {
		String cuttingCode = "";
		Pattern pattern = Pattern.compile("(cellList=).*ENDC");
		Matcher matcher = pattern.matcher(code);
		while (matcher.find()) {
			cuttingCode = matcher.group(0);
		}
		pattern = Pattern.compile("(\\d+),(\\d+),([01]),([01]),(\\w+),(\\-?\\d+)");
		matcher = pattern.matcher(cuttingCode);
		while (matcher.find()) {
			int x = Integer.parseInt(matcher.group(1));
			int y = Integer.parseInt(matcher.group(2));
			boolean isGoal = Integer.parseInt(matcher.group(3)) == 1;
			boolean isStart = Integer.parseInt(matcher.group(4)) == 1;
			int sb = Integer.parseInt(matcher.group(6));
			Cell cell = new Cell();
			if (this.observationList.containsKey(matcher.group(5)) && x < this.rangeX && y < this.rangeY) {
				cell.isExist = true;
				cell.isGoal = isGoal;
				cell.isStart = isStart;
				cell.standardO = this.observationList.get(matcher.group(5));
				cell.specialBonus = sb;
				this.cellList[y][x] = cell;
			} else {
				System.out.println("Cell information errors!");
			}
		}
	}

	private void bonusInit(String code) {
		String cuttingCode = "";
		Pattern pattern = Pattern.compile("(goalBonus=).*ENDG");
		Matcher matcher = pattern.matcher(code);
		while (matcher.find()) {
			cuttingCode = matcher.group(0);
		}
		pattern = Pattern.compile("\\d+(\\.\\d+)?");
		matcher = pattern.matcher(cuttingCode);
		while (matcher.find()) {
			double bonus = Double.parseDouble(matcher.group(0));
			this.goalBonus = bonus;
		}

		pattern = Pattern.compile("(stepLoss=).*ENDS");
		matcher = pattern.matcher(code);
		while (matcher.find()) {
			cuttingCode = matcher.group(0);
		}
		pattern = Pattern.compile("(\\-)?\\d+(\\.\\d+)?");
		matcher = pattern.matcher(cuttingCode);
		while (matcher.find()) {
			double bonus = Double.parseDouble(matcher.group(0));
			this.stepLoss = bonus;
		}
	}

	private String load(String fileName) throws IOException {
		String str = "";
		File file = new File(fileName);
		FileInputStream out = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(out);
		int ch = 0;
		while ((ch = isr.read()) != -1) {
			if ((char) ch != '\r' && (char) ch != '\n' && (char) ch != '\t')
				str = str.concat(String.valueOf((char) ch));
		}
		isr.close();
		return str;
	}

	private String listKeyFind(HashMap<String, Integer> hm, int value) {
		String key = null;
		for (String getKey : hm.keySet()) {
			if (hm.get(getKey).equals(value)) {
				key = getKey;
			}
		}
		return key;
	}

	private void mazePRIM() {
		// TODO Auto-generated method stub
		// 建立color数组，已经访问过的标为2，允许访问的邻居标为1，不允许访问的标为0
		int[][] color = new int[this.rangeY][this.rangeX];
		for (int i = 0; i < this.rangeY; i++) {
			for (int j = 0; j < this.rangeX; j++) {
				color[i][j] = 0;
				Cell c = new Cell();
				c.isExist = true;
				c.isStart = true;
				c.standardO = 15;
				this.cellList[i][j] = c;
			}
		}
		// 随机选一个染色2，周围的染色1
		int x = (int) (Math.random() * this.rangeX);
		int y = (int) (Math.random() * this.rangeY);
		color[y][x] = 2;
		if ((x - 1 >= 0) && color[y][x - 1] == 0) {
			color[y][x - 1] = 1;
		}
		if ((x + 1 < this.rangeX) && color[y][x + 1] == 0) {
			color[y][x + 1] = 1;
		}
		if ((y - 1 >= 0) && color[y - 1][x] == 0) {
			color[y - 1][x] = 1;
		}
		if ((y + 1 < this.rangeY) && color[y + 1][x] == 0) {
			color[y + 1][x] = 1;
		}
		boolean isComplete = false;
		while (!isComplete) {
			// 选一个染色2，且周围有染色1的cell
			while (true) {
				x = (int) (Math.random() * this.rangeX);
				y = (int) (Math.random() * this.rangeY);
				if (color[y][x] == 2 && ((x - 1 >= 0 && color[y][x - 1] == 1)
						|| (y + 1 < this.rangeY && color[y + 1][x] == 1) || (y - 1 >= 0 && color[y - 1][x] == 1)
						|| (x + 1 < this.rangeX && color[y][x + 1] == 1))) {
					break;
				} else {
					continue;
				}
			}
			// 在其周围染色1的中随机选一个，打穿墙壁改变观察
			int dx = 0, dy = 0, move = 0;
			while (true) {
				double EP = Math.random();
				if (EP < 0.25) {
					dx = -1;
					dy = 0;
					move = 1;
				} else if (EP >= 0.25 && EP < 0.5) {
					dx = 1;
					dy = 0;
					move = 2;
				} else if (EP >= 0.5 && EP < 0.75) {
					dx = 0;
					dy = 1;
					move = 8;
				} else if (EP >= 0.75 && EP < 1) {
					dx = 0;
					dy = -1;
					move = 4;
				}
				if (x + dx < this.rangeX && x + dx >= 0 && y + dy < this.rangeY && y + dy >= 0
						&& color[y + dy][x + dx] == 1) {
					break;
				} else {
					continue;
				}
			}
			this.cellList[y][x].standardO -= move;
			x += dx;
			y += dy;
			if (dx == 1) {
				this.cellList[y][x].standardO -= 1;
			} else if (dx == -1) {
				this.cellList[y][x].standardO -= 2;
			} else if (dy == 1) {
				this.cellList[y][x].standardO -= 4;
			} else if (dy == -1) {
				this.cellList[y][x].standardO -= 8;
			}
			if (this.cellList[y][x].standardO <= 0) {
				System.out.println("error");
			}
			color[y][x] = 2;
			if ((x - 1 >= 0) && color[y][x - 1] == 0) {
				color[y][x - 1] = 1;
			}
			if ((x + 1 < this.rangeX) && color[y][x + 1] == 0) {
				color[y][x + 1] = 1;
			}
			if ((y - 1 >= 0) && color[y - 1][x] == 0) {
				color[y - 1][x] = 1;
			}
			if ((y + 1 < this.rangeY) && color[y + 1][x] == 0) {
				color[y + 1][x] = 1;
			}
			// 终止条件检验
			isComplete = true;
			for (int i = 0; i < this.rangeY; i++) {
				for (int j = 0; j < this.rangeX; j++) {
					if (color[i][j] != 2)
						isComplete = false;
				}
			}
		}
		// 选一个三面是墙的cell作为goal
		while (true) {
			x = (int) (Math.random() * this.rangeX);
			y = (int) (Math.random() * this.rangeY);
			if (this.cellList[y][x].standardO != 7 && this.cellList[y][x].standardO != 11
					&& this.cellList[y][x].standardO != 13 && this.cellList[y][x].standardO != 14) {
				continue;
			} else {
				this.cellList[y][x].isGoal = true;
				break;
			}
		}
		System.out.println("complete!");
	}

	private void mazeKA() {
		// TODO Auto-generated method stub
		// 墙数组
		boolean[][] wallExist = new boolean[(2 * this.rangeY) - 1][this.rangeX];
		for (int i = 0; i < 2 * this.rangeY - 1; i++) {
			for (int j = 0; j < this.rangeX; j++) {
				wallExist[i][j] = true;
			}
		}
		// 水位数组，从低到高排列，最终每个cell的水位都需要降至0
		int[][] num = new int[this.rangeY][this.rangeX];
		for (int i = 0; i < this.rangeY; i++) {
			for (int j = 0; j < this.rangeX; j++) {
				Cell c = new Cell();
				c.isExist = true;
				c.isStart = true;
				c.standardO = 15;
				this.cellList[i][j] = c;
				num[i][j] = this.rangeY * i + j;
			}
		}
		boolean isComplete = false;
		int x = 0, y = 0, wx = 0, wy = 0;
		while (!isComplete) {
			// 选定要拆掉的墙
			while (true) {
				wx = (int) (Math.random() * (this.rangeX - 1));
				wy = (int) (Math.random() * ((2 * this.rangeY) - 1));
				if (wallExist[wy][wx] == false) {
					continue;
				} else {
					break;
				}
			}
			// 根据墙的坐标找到左右/上下对应的的cell，若两cell水位相同则重选一次
			x = wx;
			if (wy % 2 == 1) {
				y = (wy + 1) / 2;
				if (num[y][x] == num[y - 1][x]) {
					continue;
				} else {
					// 所有cell里和高水位相同的全部变为低水位
					int smallOne = Math.min(num[y][x], num[y - 1][x]);
					int bigOne = Math.max(num[y][x], num[y - 1][x]);
					for (int i = 0; i < this.rangeY; i++) {
						for (int j = 0; j < this.rangeX; j++) {
							if (num[i][j] == bigOne) {
								num[i][j] = smallOne;
							}
						}
					}
					// 同时改变两个cell的观察
					this.cellList[y][x].standardO -= 4;
					this.cellList[y - 1][x].standardO -= 8;
					wallExist[wy][wx] = false;
				}
			} else {
				y = wy / 2;
				if (num[y][x] == num[y][x + 1]) {
					continue;
				} else {
					// 所有cell里和高水位相同的全部变为低水位
					int smallOne = Math.min(num[y][x], num[y][x + 1]);
					int bigOne = Math.max(num[y][x], num[y][x + 1]);
					for (int i = 0; i < this.rangeY; i++) {
						for (int j = 0; j < this.rangeX; j++) {
							if (num[i][j] == bigOne) {
								num[i][j] = smallOne;
							}
						}
					}
					// 同时改变两个cell的观察
					this.cellList[y][x].standardO -= 2;
					this.cellList[y][x + 1].standardO -= 1;
					wallExist[wy][wx] = false;
				}
			}
			// 结束条件
			isComplete = true;
			for (int i = 0; i < this.rangeY; i++) {
				for (int j = 0; j < this.rangeX; j++) {
					if (num[i][j] != 0) {
						isComplete = false;
					}
				}
			}
		}
		System.out.println("complete");
		// 选定三面是墙的cell作为goal
		while (true) {
			x = (int) (Math.random() * this.rangeX);
			y = (int) (Math.random() * this.rangeY);
			if (this.cellList[y][x].standardO != 7 && this.cellList[y][x].standardO != 11
					&& this.cellList[y][x].standardO != 13 && this.cellList[y][x].standardO != 14) {
				continue;
			} else {
				this.cellList[y][x].isGoal = true;
				break;
			}

		}

	}

	private void mazeDFS() {
		// TODO Auto-generated method stub
		boolean[][] isVisit = new boolean[this.rangeY][this.rangeX];
		for (int i = 0; i < this.rangeY; i++) {
			for (int j = 0; j < this.rangeX; j++) {
				isVisit[i][j] = false;
				Cell c = new Cell();
				this.cellList[i][j] = c;
				c.isExist = true;
				c.isStart = true;
				this.cellList[i][j].standardO = 15;
			}
		}
		// 建立栈，其中放的是上一个动作
		Stack<Integer> stack = new Stack<Integer>();
		int x = (int) (Math.random() * this.rangeX), y = (int) (Math.random() * this.rangeY);
		isVisit[y][x] = true;
		boolean isComplete = false;
		while (!isComplete) {
			// 查看四周是否都被访问过
			if ((x - 1 >= 0 && isVisit[y][x - 1] == false) || (x + 1 < this.rangeX && isVisit[y][x + 1] == false)
					|| (y - 1 >= 0 && isVisit[y - 1][x] == false)
					|| (y + 1 < this.rangeY && isVisit[y + 1][x] == false)) {
				// 选取一个没有被访问过的邻居
				int dx = 0, dy = 0, move = 0;
				while (true) {
					double EP = Math.random();
					if (EP < 0.25) {
						dx = -1;
						dy = 0;
						move = 1;
					} else if (EP >= 0.25 && EP < 0.5) {
						dx = 1;
						dy = 0;
						move = 2;
					} else if (EP >= 0.5 && EP < 0.75) {
						dx = 0;
						dy = 1;
						move = 8;
					} else if (EP >= 0.75 && EP < 1) {
						dx = 0;
						dy = -1;
						move = 4;
					}
					if (x + dx < this.rangeX && x + dx >= 0 && y + dy < this.rangeY && y + dy >= 0
							&& isVisit[y + dy][x + dx] == false) {
						break;
					}
				}
				this.cellList[y][x].standardO -= move;

				x = x + dx;
				y = y + dy;

				stack.push(move);
				isVisit[y][x] = true;
				if (dx == 1) {
					this.cellList[y][x].standardO -= 1;
				} else if (dx == -1) {
					this.cellList[y][x].standardO -= 2;
				} else if (dy == 1) {
					this.cellList[y][x].standardO -= 4;
				} else if (dy == -1) {
					this.cellList[y][x].standardO -= 8;
				}
				if (this.cellList[y][x].standardO <= 0) {
					System.out.println("error");
				}

			} // 所有邻居都被访问过了，从栈中移除栈顶
			else {
				if (!stack.isEmpty()) {
					int lastMove = stack.pop();
					if (lastMove == 1) {
						x++;
					} else if (lastMove == 2) {
						x--;
					} else if (lastMove == 4) {
						y++;
					} else if (lastMove == 8) {
						y--;
					}
				}

			}

			isComplete = true;
			for (int i = 0; i < this.rangeY; i++) {
				for (int j = 0; j < this.rangeX; j++) {
					if (isVisit[i][j] == false)
						isComplete = false;
				}
			}
		}
		System.out.println("Maze generates completely!");
		// 选定一个三面是墙的cell作为goal
		while (true) {
			x = (int) (Math.random() * this.rangeX);
			y = (int) (Math.random() * this.rangeY);
			if (this.cellList[y][x].standardO != 7 && this.cellList[y][x].standardO != 11
					&& this.cellList[y][x].standardO != 13 && this.cellList[y][x].standardO != 14) {
				continue;
			} else {
				this.cellList[y][x].isGoal = true;
				break;
			}

		}

	}

	public void outputMaze(String path) {
		try {
			BufferedOutputStream bf = new BufferedOutputStream(new FileOutputStream(new File(path)));
			Cell c_temp = null;
			for (int y = 0; y < this.rangeY; y++) {
				for (int x = 0; x < this.rangeX; x++) {
					c_temp = this.cellList[y][x];
					if (c_temp.isExist) {
						String str = "";
						str = str.concat("\t\t" + x + "," + y + ",");
						if (c_temp.isGoal) {
							str = str.concat("1,");
						} else
							str = str.concat("0,");
						if (c_temp.isStart) {
							str = str.concat("1,");
						} else
							str = str.concat("0,");
						str = str.concat(String.valueOf(c_temp.standardO) + ",");
						str = str.concat("0;");
						str = str.concat("\n");
						bf.write(str.getBytes());
					}
				}
			}
			bf.flush();
			bf.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
