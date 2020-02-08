package com.sunflow.simpleneuralnetwork;

import java.io.Serializable;

import com.sunflow.math3d.MatrixD;
import com.sunflow.util.Mapper;

public interface NN extends Cloneable, Serializable {
	public double[] predict(double[] inputs_array);

	public MatrixD predict(MatrixD inputs);

	public void train(double[] inputs_array, double[] targets_array);

	public void train(MatrixD inputs, MatrixD targets);

	public void setLearningRate(double learning_rate);

	public void setActivationFunction(ActivationFunction func);

	public void mutate(Mapper func);

}
