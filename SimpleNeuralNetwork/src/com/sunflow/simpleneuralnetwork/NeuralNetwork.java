package com.sunflow.simpleneuralnetwork;

import com.sunflow.math3d.MatrixD;
import com.sunflow.math3d.MatrixD.Mapper;
import com.sunflow.util.GameUtils;
import com.sunflow.util.Log;

public class NeuralNetwork implements NN, GameUtils {
	public static ActivationFunction sigmoid = new ActivationFunction(
			(x, i, j) -> (1 / (1 + Math.exp(-x))),
			(y, i, j) -> y * (1 - y));

	public static ActivationFunction tanh = new ActivationFunction(
			(x, i, j) -> Math.tanh(x),
			(y, i, j) -> 1 - (y * y));

	private int nodes_inputs;
	private int nodes_hidden;
	private int nodes_outputs;

	private MatrixD weights_ih;
	private MatrixD weights_ho;

	private MatrixD bias_h;
	private MatrixD bias_o;

	private double learning_rate;
	private ActivationFunction activation_function;

	public NeuralNetwork(int nodes_inputs, int nodes_outputs, int nodes_hidden) {
		this.nodes_inputs = nodes_inputs;
		this.nodes_outputs = nodes_outputs;
		this.nodes_hidden = nodes_hidden;

		this.weights_ih = new MatrixD(nodes_hidden, nodes_inputs);
		this.weights_ho = new MatrixD(nodes_outputs, nodes_hidden);
		this.weights_ih.randomize();
		this.weights_ho.randomize();

		this.bias_h = new MatrixD(nodes_hidden, 1);
		this.bias_o = new MatrixD(nodes_outputs, 1);
		this.bias_h.randomize();
		this.bias_o.randomize();

		this.setLearningRate(0.1F);
		this.setActivationFunction(sigmoid);
	}

	public float[] predict(float[] inputs_array) { return convertArray(predict(convertArray(inputs_array))); }

	@Override
	public double[] predict(double[] inputs_array) {
		if (inputs_array.length != nodes_inputs) {
			Log.err("NeuralNetwork#predict: inputs and nn_inputs didnt match");
		}
		MatrixD inputs = MatrixD.fromArray(inputs_array);
		MatrixD outputs = predict(inputs);
		return outputs.toArray();
	}

	@Override
	public MatrixD predict(MatrixD inputs) {
		// Generating the hidden outputs
		MatrixD hidden = genLayer(weights_ih, inputs, bias_h);
		// Generating the real outputs
		MatrixD outputs = genLayer(weights_ho, hidden, bias_o);
		// Sending back to the caller!
		return outputs;
	}

	@Override
	public void setLearningRate(double learning_rate) { this.learning_rate = learning_rate; }

	@Override
	public void setActivationFunction(ActivationFunction func) { this.activation_function = func; }

	public void train(float[] inputs_array, float[] targets_array) { train(convertArray(inputs_array), convertArray(targets_array)); }

	@Override
	public void train(double[] inputs_array, double[] targets_array) {
		if (inputs_array.length != nodes_inputs) {
			Log.err("NeuralNetwork#train: input and nn_input didnt match");
		}
		if (targets_array.length != nodes_outputs) {
			Log.err("NeuralNetwork#train: target and nn_output didnt match");
		}
		MatrixD inputs = MatrixD.fromArray(inputs_array);
		MatrixD targets = MatrixD.fromArray(targets_array);
		train(inputs, targets);
	}

	@Override
	public void train(MatrixD inputs, MatrixD targets) {
		// Generating the hidden outputs
		MatrixD hidden = genLayer(weights_ih, inputs, bias_h);
		// Generating the real outputs
		MatrixD outputs = genLayer(weights_ho, hidden, bias_o);

		// Calculate the output layer errors
		// ERROR = TARGET - OUTPUT
		MatrixD errors_o = MatrixD.substract(targets, outputs);
		adjustLayer(errors_o, outputs, hidden, weights_ho, bias_o);

		// Calculate the hidden layer errors
		// ERROR = TARGET - OUTPUT
		MatrixD weights_ho_t = MatrixD.transpose(weights_ho);
		MatrixD errors_h = MatrixD.dot(weights_ho_t, errors_o);
		adjustLayer(errors_h, hidden, inputs, weights_ih, bias_h);
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
	public NeuralNetwork clone() {
		NeuralNetwork copy = new NeuralNetwork(nodes_inputs, nodes_outputs, nodes_hidden);
		copy.nodes_inputs = this.nodes_inputs;
		copy.nodes_outputs = this.nodes_outputs;
		copy.nodes_hidden = this.nodes_hidden;

		copy.weights_ih = this.weights_ih.clone();
		copy.weights_ho = this.weights_ho.clone();

		copy.bias_h = this.bias_h.clone();
		copy.bias_o = this.bias_o.clone();

		copy.setLearningRate(0.1F);
		copy.setActivationFunction(sigmoid);
		return copy;

	}

	@Override
	public void mutate(Mapper func) {
		weights_ih.map(func);
		weights_ho.map(func);
		bias_h.map(func);
		bias_o.map(func);
	}
}
