package tt.euclidtime3i.vis;

import tt.vis.ParameterControlLayer.ParameterProvider;

public class TimeParameter implements ParameterProvider {

	int time = 0;
	int timeStep;

	public TimeParameter() {
		this(1);
	}

	public TimeParameter(int timeStep) {
		super();
		this.timeStep = timeStep;
	}

	@Override
	public String getName() {
		return "Time";
	}

	@Override
	public String getValue() {
		return Integer.toString(time);
	}

	@Override
	public char getIncreaseKey() {
		return 'q';
	}

	@Override
	public char getDecreaseKey() {
		return 'a';
	}

	@Override
	public void increased() {
		time += timeStep;
	}

	@Override
	public void decreased() {
		time -= timeStep;
	}

	public int getTime() {
		return time;
	}
}
