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

	// ��ǰ��������Ķ����͹۲�
	private HashMap<String, Integer> actionList;
	private HashMap<String, Integer> observationList;

	// ��ǰ�����Ķ�ά��Χ�͵���
	private int rangeX;
	private int rangeY;
	private Cell[][] cellList;

	// �������յ㽱����ÿ�����
	private double goalBonus;
	private double stepLoss;

	// agent�ĵ�ǰλ�ú�״̬
	// private int lastA;
	private int lastO;
	private double lastR;
	private int curX;
	private int curY;

	/**
	 * �����ĳ�ʼ����������settingCode��ʼ������������Ķ����͹۲죬�ռ䷶Χ�͵��ε���Ϣ
	 * 
	 * @param
	 * @throws IOException
	 */
	public Maze(String s) throws IOException {
		// ��code.txt�ж�ȡ����

		String str = load(s);
		this.actionList = new HashMap<String, Integer>();
		this.observationList = new HashMap<String, Integer>();

		// �����۲켯��ʼ��
		AInit(str);
		OInit(str);
		// ��Χ�͵��γ�ʼ��
		rangeInit(str);
		cellInit(str);
		// �յ㽱����ÿ����ĳ�ʼ��
		bonusInit(str);
		// ��ǰλ�ú�״̬��ʼ��
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
	 * �����㷨�Զ�����Maze�ķ�������Ҫָ������ÿ����ĺ��յ㽱��,�Լ����ɷ���
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
		// ����color���飬�Ѿ����ʹ��ı�Ϊ2��������ʵ��ھӱ�Ϊ1����������ʵı�Ϊ0
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
		// ���ѡһ��Ⱦɫ2����Χ��Ⱦɫ1
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
			// ѡһ��Ⱦɫ2������Χ��Ⱦɫ1��cell
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
			// ������ΧȾɫ1�������ѡһ������ǽ�ڸı�۲�
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
			// ��ֹ��������
			isComplete = true;
			for (int i = 0; i < this.rangeY; i++) {
				for (int j = 0; j < this.rangeX; j++) {
					if (color[i][j] != 2)
						isComplete = false;
				}
			}
		}
		// ѡһ��������ǽ��cell��Ϊgoal
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
		// ǽ����
		boolean[][] wallExist = new boolean[(2 * this.rangeY) - 1][this.rangeX];
		for (int i = 0; i < 2 * this.rangeY - 1; i++) {
			for (int j = 0; j < this.rangeX; j++) {
				wallExist[i][j] = true;
			}
		}
		// ˮλ���飬�ӵ͵������У�����ÿ��cell��ˮλ����Ҫ����0
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
			// ѡ��Ҫ�����ǽ
			while (true) {
				wx = (int) (Math.random() * (this.rangeX - 1));
				wy = (int) (Math.random() * ((2 * this.rangeY) - 1));
				if (wallExist[wy][wx] == false) {
					continue;
				} else {
					break;
				}
			}
			// ����ǽ�������ҵ�����/���¶�Ӧ�ĵ�cell������cellˮλ��ͬ����ѡһ��
			x = wx;
			if (wy % 2 == 1) {
				y = (wy + 1) / 2;
				if (num[y][x] == num[y - 1][x]) {
					continue;
				} else {
					// ����cell��͸�ˮλ��ͬ��ȫ����Ϊ��ˮλ
					int smallOne = Math.min(num[y][x], num[y - 1][x]);
					int bigOne = Math.max(num[y][x], num[y - 1][x]);
					for (int i = 0; i < this.rangeY; i++) {
						for (int j = 0; j < this.rangeX; j++) {
							if (num[i][j] == bigOne) {
								num[i][j] = smallOne;
							}
						}
					}
					// ͬʱ�ı�����cell�Ĺ۲�
					this.cellList[y][x].standardO -= 4;
					this.cellList[y - 1][x].standardO -= 8;
					wallExist[wy][wx] = false;
				}
			} else {
				y = wy / 2;
				if (num[y][x] == num[y][x + 1]) {
					continue;
				} else {
					// ����cell��͸�ˮλ��ͬ��ȫ����Ϊ��ˮλ
					int smallOne = Math.min(num[y][x], num[y][x + 1]);
					int bigOne = Math.max(num[y][x], num[y][x + 1]);
					for (int i = 0; i < this.rangeY; i++) {
						for (int j = 0; j < this.rangeX; j++) {
							if (num[i][j] == bigOne) {
								num[i][j] = smallOne;
							}
						}
					}
					// ͬʱ�ı�����cell�Ĺ۲�
					this.cellList[y][x].standardO -= 2;
					this.cellList[y][x + 1].standardO -= 1;
					wallExist[wy][wx] = false;
				}
			}
			// ��������
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
		// ѡ��������ǽ��cell��Ϊgoal
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
		// ����ջ�����зŵ�����һ������
		Stack<Integer> stack = new Stack<Integer>();
		int x = (int) (Math.random() * this.rangeX), y = (int) (Math.random() * this.rangeY);
		isVisit[y][x] = true;
		boolean isComplete = false;
		while (!isComplete) {
			// �鿴�����Ƿ񶼱����ʹ�
			if ((x - 1 >= 0 && isVisit[y][x - 1] == false) || (x + 1 < this.rangeX && isVisit[y][x + 1] == false)
					|| (y - 1 >= 0 && isVisit[y - 1][x] == false)
					|| (y + 1 < this.rangeY && isVisit[y + 1][x] == false)) {
				// ѡȡһ��û�б����ʹ����ھ�
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

			} // �����ھӶ������ʹ��ˣ���ջ���Ƴ�ջ��
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
		// ѡ��һ��������ǽ��cell��Ϊgoal
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
