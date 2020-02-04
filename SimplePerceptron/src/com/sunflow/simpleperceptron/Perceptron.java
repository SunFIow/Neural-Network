package com.sunflow.simpleperceptron;

import com.sunflow.util.MathUtils;

public class Perceptron implements MathUtils {
	private float[] weights;
	// The learing rate
	private float lr = 0.003F;

	// Constructor
	public Perceptron(int n) {
		// Initialize the weights randomly
		weights = new float[n];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = random(-1, 1);
			String s;
		}
	}

	// The guess function
	public int guess(float[] inputs) {
		if (inputs.length != weights.length) {
			new Throwable("weights and inputs have to be of the same length, weights:" + weights.length + ", inputs: " + inputs.length).printStackTrace();
		}
		float sum = 0;
		for (int i = 0; i < inputs.length; i++) {
			sum += inputs[i] * weights[i];
		}
		int output = activation(sum);
		return output;
	}

	public void train(float[] inputs, int target) {
		int guess = guess(inputs);
		int error = target - guess;

		for (int i = 0; i < weights.length; i++) {
			weights[i] += error * inputs[i] * lr;
		}
	}

	// The activation function
	private int activation(float n) {
		return (int) Math.signum(n);
	}

	public float guessY(float x) {
		float w0 = weights[0];
		float w1 = weights[1];
		float w2 = weights[2];
		return -(w2 / w1) - (w0 / w1) * x;
	}
}
