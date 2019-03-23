package rl_system;

import java.io.IOException;

public class RL_System {
	private Agent agent;
	private AgentForBig agentForBig;

	public void run() throws IOException {
		this.agent = new Agent();
		agent.makeStep();
		// this.agent.generateADR();
	}

	public void runBig() throws IOException {
		this.agentForBig = new AgentForBig();  //創建出一個代理。
		agentForBig.makeStep();//makeStep的意思就是進行一次完整的實驗的意思。
		this.agentForBig.generateADR();//進行完一次實驗之後，獲取ADR。
	}

	public static void main(String[] args) throws IOException {
		RL_System rl = new RL_System(); //整個實驗系統。
		double start = System.currentTimeMillis(); //記錄開始時間。
		// rl.run();
		for(int i=0;i<10;i++) {//這個實驗系統完全獨立運行10次。
			rl.runBig();
		}
		double end = System.currentTimeMillis(); //記錄結束時間。
		System.out.println("time cost: " + String.valueOf(end - start));
	}
}
