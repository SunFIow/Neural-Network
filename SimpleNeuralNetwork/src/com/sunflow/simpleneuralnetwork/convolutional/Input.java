package com.sunflow.simpleneuralnetwork.convolutional;

import java.io.Serializable;

import com.sunflow.util.MathUtils;

public class Input implements Cloneable, Serializable {
	private float[] values;

	public Input(float[] inputs) { this.values = inputs; }

	@Override
	protected Input clone() { return new Input(values.clone()); }

	public float[] values() { return values; }

	public void normalize(float min, float max) {
		for (int i = 0; i < values.length; i++)
			values[i] = MathUtils.instance.norm(values[i], min, max);
	}

	public float getMin() {
		float min = Float.MAX_VALUE;
		for (int i = 0; i < values.length; i++)
			if (values[i] < min) min = values[i];
		return min;
	}

	public float getMax() {
		float max = Float.MIN_VALUE;
		for (int i = 0; i < values.length; i++)
			if (values[i] > max) max = values[i];
		return max;
	}

}
