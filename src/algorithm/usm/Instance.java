package algorithm.usm;

public class Instance {
	public Instance getLastInstance() {
		return lastInstance;
	}

	public int getAction() {
		return action;
	}

	public int getObservation() {
		return observation;
	}

	public double getReward() {
		return reward;
	}

	public Instance getNextInstance() {
		return this.nextInstance;
	}

	public void setNextInstance(Instance in) {
		this.nextInstance = in;
	}

	private Instance lastInstance;
	private Instance nextInstance;
	private int action;
	private int observation;
	private double reward;

	public Instance(Instance lastInstance, int actionIndex, int observationIndex, double reward) {
		this.lastInstance = lastInstance;
		this.action = actionIndex;
		this.observation = observationIndex;
		this.reward = reward;
		this.nextInstance = null;
	}

}
