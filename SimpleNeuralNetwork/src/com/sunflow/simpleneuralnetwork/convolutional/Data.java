package com.sunflow.simpleneuralnetwork.convolutional;

import java.io.Serializable;

public class Data implements Cloneable, Serializable {

	public Input input;
	public Target target;

	public Data(Input input, Target target) {
		this.input = input;
		this.target = target;
	}

	@Override
	protected Data clone() { return new Data(input.clone(), target.clone()); }

	public void normalize(float min, float max) { input.normalize(min, max); }

	public float getMin() { return input.getMin(); }

	public float getMax() { return input.getMax(); }
}
