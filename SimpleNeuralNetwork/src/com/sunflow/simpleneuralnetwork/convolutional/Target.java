package com.sunflow.simpleneuralnetwork.convolutional;

import java.io.Serializable;

public class Target implements Cloneable, Serializable {
	private float[] values;

	@Override
	protected Target clone() { return new Target(values.clone()); }

	public Target(float[] targets) { this.values = targets; }

	public float[] values() { return values; }

}
