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
		this.agentForBig = new AgentForBig();  //������һ������
		agentForBig.makeStep();//makeStep����˼�����M��һ�������Č�����˼��
		this.agentForBig.generateADR();//�M����һ�Ό��֮�ᣬ�@ȡADR��
	}

	public static void main(String[] args) throws IOException {
		RL_System rl = new RL_System(); //�������ϵ�y��
		double start = System.currentTimeMillis(); //ӛ��_ʼ�r�g��
		// rl.run();
		for(int i=0;i<10;i++) {//�@�����ϵ�y��ȫ�����\��10�Ρ�
			rl.runBig();
		}
		double end = System.currentTimeMillis(); //ӛ䛽Y���r�g��
		System.out.println("time cost: " + String.valueOf(end - start));
	}
}
