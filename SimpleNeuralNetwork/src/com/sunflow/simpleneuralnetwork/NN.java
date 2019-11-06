package com.sunflow.simpleneuralnetwork;

import com.sunflow.math3d.MatrixD;
import com.sunflow.math3d.MatrixD.Mapper;

public interface NN extends Cloneable {
	public double[] predict(double[] inputs_array);

	public MatrixD predict(MatrixD inputs);

	public void train(double[] inputs_array, double[] targets_array);

	public void train(MatrixD inputs, MatrixD targets);

	public void setLearningRate(double learning_rate);

	public void setActivationFunction(ActivationFunction func);

	public void mutate(Mapper func);

	public static class ActivationFunction {
		public Mapper func;
		public Mapper dfunc;

		public ActivationFunction(Mapper func, Mapper dfunc) {
			this.func = func;
			this.dfunc = dfunc;
		}
	}

	public static class Data {
		public double[] inputs;
		public double[] targets;

		public Data(double[] inputs, double[] targets) {
			this.inputs = inputs;
			this.targets = targets;
		}
	}
}
