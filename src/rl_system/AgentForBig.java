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
	public final int MAX_TIME = 20000; //MAX_TIME��һ�Ό�����ߵĲ��E������
	public int courseNum = 0;
	public int MaxNum = 8;
	public String text = "";

	public int stateNum; // agent������֪���㷨�����˶��ٸ�״̬����ʹʵ���ϲ�û���ô�

	public HashMap<String, Integer> actionList;
	public HashMap<String, Integer> observationList;

	public AgentForBig() throws IOException {
		this.actionList = new HashMap<String, Integer>(); //�������ϡ�String�Ǆ��������Q��Integer�Ǵ�̖��
		this.observationList = new HashMap<String, Integer>(); //�^�켯�ϡ�Sting���^������Q��Integer�Ǵ�̖��
		this.agentInit();
	}

	/**
	 * agent��ʼ��������֮������Ӳ���
	 * 
	 * @throws IOException
	 */
	private void agentInit() throws IOException {
		// ѡȡ����,��ʼ��A��O��
		this.envir = new Maze("GridMaze64.txt"); //���������h�����@�e�����x��򞌍�h����
		// this.envir = new Maze("Hallway2.txt");
		// this.envir = new Maze(6, 6, -0.1, 200, MGF.PRIM);
		// this.envir.outputMaze("output.txt");
		this.actionList = envir.getActionList(); //�@ȡ���h��Ҏ���Ą�������
		this.observationList = envir.getObservationList(); //�@ȡ���h��Ҏ�����^�켯�ϡ�һ����16�N�^�죬�������扦����r��
		// ѡȡ�㷨

		// this.algo = new KSIP_USM(observationList.size(), actionList.size());
		this.algo = new USM(observationList.size(), actionList.size()); //����һ��USM�㷨��
		//this.algo = new EI_USM(observationList.size(), actionList.size());
		return;
	}

	/**
	 * ���߷�����ѡ����һ������
	 * �@�emakeStep����˼����һ�������Č�򞡣
	 */
	public void makeStep() {
		long p = System.currentTimeMillis();
		int actionIndex;
		int t = 0; //t��Ӌ�r����

		// �������ն���
		int newO = this.envir.newStart(); // newO���µ��_ʼ�����c���^��ֵ��
		this.algo.newStart(newO); //newStart��newOݔ�뵽�㷨��ȥ,��֪USM�@��newO��һ���µ��_ʼ��

		while (t < MAX_TIME) {
			actionIndex = algo.makeDecision(); //��USM�Ы@ȡ��һ���ěQ��actionIndex��
			// System.out.print(t + ":");
			if (envir.execute(actionIndex)) { //�ڭh���Ј��ЛQ��actionIndex�����ײ�����t����false������Ƅӳɹ����t����true�����actionIndex=0����ͣ�����ӣ�Ҳ�Ƿ���true��
				algo.generateInstance(actionIndex, envir.getLastO(), envir.getLastR()); //�oUSMݔ��һ��������
				if (!envir.isGoal()) { //ԃ���h���������Ƿ��_�˽K�c��
					// �㷨����ʵ����ͳ��
					stateNum = algo.getStateNum(); //���߀�]���_�˽K�c���t��USM�Ы@ȡ��B�Ĕ���������stateNum��ֵ��
				} else {
					// �ִ�Ŀ�꣬׼����һ��ѧϰ
					// System.out.println("One course finished!");
					// this.algo.printQvalueTable();
					courseNum++;//courseNumӛ䛵����˔���
					// long n = System.currentTimeMillis();
					// System.out.println(courseNum + " times training");
					// System.out.println(t + " steps cost " + String.valueOf(n - p) + " millis");
					// System.out.println("ADR: " + this.algo.getADR());
					newO = this.envir.newStart(); //// newO���µ��_ʼ�����c���^��ֵ��
					this.algo.newStart(newO);//newStart��newOݔ�뵽�㷨��ȥ,��֪USM�@��newO��һ���µ��_ʼ��
				}
				t++;//Ӌ�r��������
				if (t == 500 || t == 1000 || t == 1500 || t == 2000 || t == 2500 || t == 3000 || t == 4000 || t == 5000
						|| t == 6000 || t == 8000 || t == 10000 || t == 15000 || t == 20000) {
					//��һЩ����ĕr�g�c���@ȡUSM��ADRֵ��
					long n = System.currentTimeMillis();
					text = text.concat(String.valueOf(this.algo.getADR()) + '\t');
					text = text.concat(String.valueOf(n - p) + '\n'); //ӛ�ϵ�y�r�g��
				}
			}
		}
		System.out.println(text);
	}

	/**
	 * �����������Ὣ����ѧϰ�ļ�¼����
	 * ��makeStep ֮�ᣬ�\�д˷����@ȡADR��
	 *
	 */
	public void generateADR() {
		// this.algo.printQvalueTable();
		System.out.println("ADR: " + this.algo.getADR());
		System.out.println();
	}
}
