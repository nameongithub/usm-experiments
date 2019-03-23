package rl_system;

import java.io.IOException;
import java.util.HashMap;

import algorithm.Algorithm;
import algorithm.EI_usm.EI_USM;
import algorithm.usm.USM;
import environment.Environment;
import environment.maze.Maze;

public class AgentForBig {
	public Algorithm algo;
	public Environment envir;
	public final int MAX_TIME = 20000; //MAX_TIME是一次實驗所走的步驟數量。
	public int courseNum = 0;
	public int MaxNum = 8;
	public String text = "";

	public int stateNum; // agent有理由知道算法给出了多少个状态，即使实际上并没有用处

	public HashMap<String, Integer> actionList;
	public HashMap<String, Integer> observationList;

	public AgentForBig() throws IOException {
		this.actionList = new HashMap<String, Integer>(); //動作集合。String是動作的名稱，Integer是代號。
		this.observationList = new HashMap<String, Integer>(); //觀察集合。Sting是觀察的名稱，Integer是代號。
		this.agentInit();
	}

	/**
	 * agent初始化方法，之后会增加参数
	 * 
	 * @throws IOException
	 */
	private void agentInit() throws IOException {
		// 选取环境,初始化A和O集
		this.envir = new Maze("GridMaze64.txt"); //創建出實驗環境。這裡可以選擇實驗實驗環境。
		// this.envir = new Maze("Hallway2.txt");
		// this.envir = new Maze(6, 6, -0.1, 200, MGF.PRIM);
		// this.envir.outputMaze("output.txt");
		this.actionList = envir.getActionList(); //獲取實驗環境規定的動作集。
		this.observationList = envir.getObservationList(); //獲取實驗環境規定的觀察集合。一共有16種觀察，對應四面墻的情況。
		// 选取算法

		// this.algo = new KSIP_USM(observationList.size(), actionList.size());
		this.algo = new USM(observationList.size(), actionList.size()); //創建一個USM算法。
		//this.algo = new EI_USM(observationList.size(), actionList.size());
		return;
	}

	/**
	 * 决策方法，选择下一个动作
	 * 這裡makeStep的意思是做一次完整的實驗。
	 */
	public void makeStep() {
		long p = System.currentTimeMillis();
		int actionIndex;
		int t = 0; //t是計時器。

		// 环境接收动作
		int newO = this.envir.newStart(); // newO是新的開始的起點的觀察值。
		this.algo.newStart(newO); //newStart把newO輸入到算法中去,告知USM這個newO是一個新的開始。

		while (t < MAX_TIME) {
			actionIndex = algo.makeDecision(); //從USM中獲取下一步的決策actionIndex。
			// System.out.print(t + ":");
			if (envir.execute(actionIndex)) { //在環境中執行決策actionIndex。如果撞墻，則返回false，如果移動成功，則返回true。如果actionIndex=0，即停留不動，也是返回true。
				algo.generateInstance(actionIndex, envir.getLastO(), envir.getLastR()); //給USM輸入一個實例。
				if (!envir.isGoal()) { //詢問環境，代理是否到達了終點？
					// 算法产生实例并统计
					stateNum = algo.getStateNum(); //如果還沒有達了終點，則從USM中獲取狀態的數量，更新stateNum的值。
				} else {
					// 抵达目标，准备下一次学习
					// System.out.println("One course finished!");
					// this.algo.printQvalueTable();
					courseNum++;//courseNum記錄的是趟數。
					// long n = System.currentTimeMillis();
					// System.out.println(courseNum + " times training");
					// System.out.println(t + " steps cost " + String.valueOf(n - p) + " millis");
					// System.out.println("ADR: " + this.algo.getADR());
					newO = this.envir.newStart(); //// newO是新的開始的起點的觀察值。
					this.algo.newStart(newO);//newStart把newO輸入到算法中去,告知USM這個newO是一個新的開始。
				}
				t++;//計時器自增。
				if (t == 500 || t == 1000 || t == 1500 || t == 2000 || t == 2500 || t == 3000 || t == 4000 || t == 5000
						|| t == 6000 || t == 8000 || t == 10000 || t == 15000 || t == 20000) {
					//在一些特殊的時間點，獲取USM的ADR值。
					long n = System.currentTimeMillis();
					text = text.concat(String.valueOf(this.algo.getADR()) + '\t');
					text = text.concat(String.valueOf(n - p) + '\n'); //記錄系統時間。
				}
			}
		}
		System.out.println(text);
	}

	/**
	 * 结束方法，会将本次学习的记录导出
	 * 在makeStep 之後，運行此方法獲取ADR。
	 *
	 */
	public void generateADR() {
		// this.algo.printQvalueTable();
		System.out.println("ADR: " + this.algo.getADR());
		System.out.println();
	}
}
