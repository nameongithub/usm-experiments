package rl_system;

import java.io.IOException;
import java.util.HashMap;

import algorithm.Algorithm;
import algorithm.EI_usm.EI_USM;
import algorithm.usm.USM;
import environment.Environment;
import environment.maze.Maze;

public class Agent {
	public Algorithm algo;
	public Environment envir;
	public final int MAX_TIME = 3000;
	public int courseNum = 0;
	public String text = "";

	public int stateNum; // agent������֪���㷨�����˶��ٸ�״̬����ʹʵ���ϲ�û���ô�

	public HashMap<String, Integer> actionList;
	public HashMap<String, Integer> observationList;

	public Agent() throws IOException {
		this.actionList = new HashMap<String, Integer>();
		this.observationList = new HashMap<String, Integer>();
		this.agentInit();
	}

	/**
	 * agent��ʼ��������֮������Ӳ���
	 * 
	 * @throws IOException
	 */
	private void agentInit() throws IOException {
		// ѡȡ����,��ʼ��A��O��
		// this.envir = new Maze("code.txt");
		this.envir = new Maze("Hallway2.txt");
		// this.envir = new Maze(16, 16, -0.1, 1000, MGF.PRIM);
		this.actionList = envir.getActionList();
		this.observationList = envir.getObservationList();
		
		// this.algo = new KSIP_USM(observationList.size(), actionList.size());
		this.algo = new USM(observationList.size(), actionList.size());
		this.algo = new EI_USM(observationList.size(), actionList.size());
		return;
	}

	/**
	 * ���߷�����ѡ����һ������
	 */
	public void makeStep() {
		long p = System.currentTimeMillis();
		int actionIndex = -1; //�@���ǈ��Є����Ĵ�̖��
		int t = 0;
		// �������ն���
		int newO = this.envir.newStart();
		this.algo.newStart(newO);

		while (t < MAX_TIME) {
			actionIndex = algo.makeDecision();
			// System.out.print(t + ":");
			if (envir.execute(actionIndex)) {
				algo.generateInstance(actionIndex, envir.getLastO(), envir.getLastR());
				if (!envir.isGoal()) {
					// �㷨����ʵ����ͳ��
					stateNum = algo.getStateNum();
				} else {
					// �ִ�Ŀ�꣬׼����һ��ѧϰ
					courseNum++;
					// System.out.println("One course finished!");

					// this.algo.printQvalueTable();
					newO = this.envir.newStart();
					this.algo.newStart(newO);
				}
				t++;
				if (t == 50 || t == 100 || t == 150 || t == 200 || t == 250 || t == 300 || t == 400 || t == 500
						|| t == 600 || t == 800 || t == 1000 ) {
					long n = System.currentTimeMillis();
					// System.out.println("******" + courseNum + " times training");
					// System.out.println("******" + t + " steps cost " + String.valueOf(n - p) + "
					// millis");
					// System.out.println("******" + "ADR: " + this.algo.getADR() + "\n");
					text = text.concat(String.valueOf(this.algo.getADR()) + '\t');
					text = text.concat(String.valueOf(n - p) + '\n');
				}
			}
		}
		System.out.println(text);
	}

	/*
	 * �����������Ὣ����ѧϰ�ļ�¼����
	 */
	public void generateADR() {
		//this.algo.printQvalueTable();
		System.out.println("ADR: " + this.algo.getADR());
		System.out.println();
	}
}
