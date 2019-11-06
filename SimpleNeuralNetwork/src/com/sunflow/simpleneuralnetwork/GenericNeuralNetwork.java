package com.sunflow.simpleneuralnetwork;

import com.sunflow.math3d.MatrixD;
import com.sunflow.math3d.MatrixD.Mapper;
import com.sunflow.util.Log;

public class GenericNeuralNetwork implements NN {
	public ActivationFunction sigmoid = new ActivationFunction(
			(x, i, j) -> (1 / (1 + Math.exp(-x))),
			(y, i, j) -> y * (1 - y));

	public ActivationFunction tanh = new ActivationFunction(
			(x, i, j) -> Math.tanh(x),
			(y, i, j) -> 1 - (y * y));

	private int nodes_inputs;
	private int nodes_outputs;
	private int[] nodes_hidden;

	private MatrixD[] weights;
	private MatrixD[] bias;

	private double learning_rate;
	private ActivationFunction activation_function;

	public GenericNeuralNetwork(int nodes_inputs, int nodes_outputs, int... nodes_hidden) {
		this.nodes_inputs = nodes_inputs;
		this.nodes_outputs = nodes_outputs;
		this.nodes_hidden = nodes_hidden;

		this.weights = new MatrixD[nodes_hidden.length + 1];
		this.bias = new MatrixD[nodes_hidden.length + 1];

		for (int i = 0; i <= nodes_hidden.length; i++) {
			int nodes_I = i == 0 ? nodes_inputs : nodes_hidden[i - 1];
			int nodes_O = i == nodes_hidden.length ? nodes_outputs : nodes_hidden[i];

			this.weights[i] = new MatrixD(nodes_O, nodes_I);
			this.weights[i].randomize();

			this.bias[i] = new MatrixD(nodes_O, 1);
			this.bias[i].randomize();
		}

		this.setLearningRate(0.1F);
		this.setActivationFunction(sigmoid);
	}

	@Override
	public double[] predict(double[] inputs_array) {
		if (inputs_array.length != nodes_inputs) {
			Log.err("NeuralNetwork#predict: input and nn_input didnt match");
		}
		MatrixD inputs = MatrixD.fromArray(inputs_array);
		MatrixD outputs = predict(inputs);
		return outputs.toArray();
	}

	@Override
	public MatrixD predict(MatrixD inputs) {
		// Generating the outputs
		MatrixD outputs = inputs;
		for (int i = 0; i <= nodes_hidden.length; i++) {
			outputs = genLayer(weights[i], outputs, bias[i]);
		}
		// Sending back to the caller!
		return outputs;
	}

	@Override
	public void setLearningRate(double learning_rate) {
		this.learning_rate = learning_rate;
	}

	@Override
	public void setActivationFunction(ActivationFunction func) {
		this.activation_function = func;
	}

	@Override
	public void train(double[] inputs_array, double[] target_array) {
		if (inputs_array.length != nodes_inputs) {
			Log.err("NeuralNetwork#train: input and nn_input didnt match");
		}
		if (target_array.length != nodes_outputs) {
			Log.err("NeuralNetwork#train: target and nn_output didnt match");
		}
		MatrixD inputs = MatrixD.fromArray(inputs_array);
		MatrixD targets = MatrixD.fromArray(target_array);
		train(inputs, targets);
	}

	@Override
	public void train(MatrixD inputs, MatrixD targets) {
		MatrixD[] outputs = new MatrixD[nodes_hidden.length + 1];
		for (int i = 0; i <= nodes_hidden.length; i++) {
			outputs[i] = genLayer(weights[i], i == 0 ? inputs : outputs[i - 1], bias[i]);
		}

		MatrixD errors_p = null;
		for (int i = nodes_hidden.length; i >= 0; i--) {
			MatrixD errors;
			MatrixD layer = outputs[i];
			MatrixD layer_p = i == 0 ? inputs : outputs[i - 1];
			MatrixD weights = this.weights[i];
			MatrixD bias = this.bias[i];
			if (i == nodes_hidden.length) {
				errors = MatrixD.substract(targets, outputs[nodes_hidden.length]);
			} else {
				MatrixD weights_p = this.weights[i + 1];
				MatrixD weights_p_t = MatrixD.transpose(weights_p);
				errors = MatrixD.dot(weights_p_t, errors_p);
			}
			errors_p = errors;

			adjustLayer(errors, layer, layer_p, weights, bias);
		}
	}

	private MatrixD genLayer(MatrixD weights, MatrixD inputs, MatrixD bias) {
		// Generating the layer output
		MatrixD outputs = MatrixD.dot(weights, inputs);
		outputs.add(bias);
		// Activation function
		outputs.map(activation_function.func);
		return outputs;
	}

	private void adjustLayer(MatrixD errors, MatrixD layer, MatrixD layer_p, MatrixD weights, MatrixD bias) {
		// Calculate gradients
		MatrixD gradients = MatrixD.map(layer, activation_function.dfunc);
		gradients.multiply(errors);
		gradients.multiply(learning_rate);

		// Calculate deltas
		MatrixD layer_p_t = MatrixD.transpose(layer_p);
		MatrixD weights_delta = MatrixD.dot(gradients, layer_p_t);

		// Adjust the weights by deltas
		weights.add(weights_delta);
		// Adjust the bias by its deltas (which is just the gradients)
		bias.add(gradients);
	}

	@Override
	public GenericNeuralNetwork clone() {
		try {
			return (GenericNeuralNetwork) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;

	}

	@Override
	public void mutate(Mapper func) {
		for (MatrixD weights : weights) {
			weights.map(func);
		}
		for (MatrixD bias : bias) {
			bias.map(func);
		}
	}
}
