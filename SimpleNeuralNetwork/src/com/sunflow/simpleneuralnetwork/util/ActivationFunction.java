package com.sunflow.simpleneuralnetwork.util;

import java.io.Serializable;

import com.sunflow.util.SimpleMapper;

public class ActivationFunction implements Cloneable, Serializable {
	private static final long serialVersionUID = 3841024686079717844L;

	public static ActivationFunction sigmoid = new ActivationFunction(
			x -> 1f / (1f + (float) Math.exp(-x)),
			y -> y * (1 - y));

	public static ActivationFunction tanh = new ActivationFunction(
			x -> (float) Math.tanh(x),
			y -> 1f - y * y);
	public SimpleMapper func;
	public SimpleMapper dfunc;

	public ActivationFunction(SimpleMapper func, SimpleMapper dfunc) {
		this.func = func;
		this.dfunc = dfunc;
	}

	@Override
	public ActivationFunction clone() {
		return new ActivationFunction(func, dfunc);
	}
}