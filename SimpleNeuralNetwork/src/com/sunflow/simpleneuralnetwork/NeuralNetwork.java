package com.sunflow.simpleneuralnetwork;

import com.sunflow.math3d.MatrixF;
import com.sunflow.util.Log;

public class NeuralNetwork {
	private float sigmoid(float x) {
		return (float) (1 / (1 + Math.exp(-x)));
	}

	private float dsigmoid(float x) {
		return sigmoid(x) * (1 - sigmoid(x));
	}

	private float dsigmoided(float x) {
		return x * (1 - x);
	}

	private int nodes_input;
	private int nodes_hidden;
	private int nodes_output;

	private MatrixF weights_ih;
	private MatrixF weights_ho;

	private MatrixF bias_h;
	private MatrixF bias_o;

	private float learing_rate;

	public NeuralNetwork(int nodes_input, int nodes_hidden, int nodes_output) {
		this.nodes_input = nodes_input;
		this.nodes_hidden = nodes_hidden;
		this.nodes_output = nodes_output;

		this.weights_ih = new MatrixF(nodes_hidden, nodes_input);
		this.weights_ho = new MatrixF(nodes_output, nodes_hidden);
		this.weights_ih.randomize();
		this.weights_ho.randomize();

		this.bias_h = new MatrixF(nodes_hidden, 1);
		this.bias_o = new MatrixF(nodes_output, 1);
		this.bias_h.randomize();
		this.bias_o.randomize();
		this.learing_rate = 0.1F;
	}

	public float[] feedforward(float[] input_array) {
		if (input_array.length != nodes_input) {
			Log.err("NeuralNetwork#feedforward: input and nn_input didnt match");
		}
		MatrixF output = feedforward(MatrixF.fromArray(input_array));
		return output.toArray();
	}

	public MatrixF feedforward(MatrixF input) {
		// Generating the hidden outputs
		MatrixF hidden = genLayer(weights_ih, input, bias_h);
		// Generating the real outputs
		MatrixF output = genLayer(weights_ho, hidden, bias_o);
		// Sending back to the caller!
		return output;
	}

	public void train(float[] input_array, float[] target_array) {
		if (input_array.length != nodes_input) {
			Log.err("NeuralNetwork#feedforward: input and nn_input didnt match");
		}
		if (target_array.length != nodes_output) {
			Log.err("NeuralNetwork#feedforward: target and nn_output didnt match");
		}

		MatrixF inputs = MatrixF.fromArray(input_array);
		MatrixF targets = MatrixF.fromArray(target_array);

		// Generating the hidden outputs
		MatrixF hidden = genLayer(weights_ih, inputs, bias_h);
		// Generating the real outputs
		MatrixF outputs = genLayer(weights_ho, hidden, bias_o);

//		MatrixF output = feedforward(MatrixF.fromArray(input));

		// Calculate the output layer errors
		// ERROR = TARGET - OUTPUT
		MatrixF errors_o = MatrixF.substract(targets, outputs);

		// Calculate output gradients
		MatrixF gradients_o = MatrixF.map(outputs, (x, i, j) -> dsigmoided(x));
		gradients_o.multiply(errors_o);
		gradients_o.multiply(learing_rate);

		// Calculate hidden -> output deltas
		MatrixF hidden_t = MatrixF.transpose(hidden);
		MatrixF weight_ho_delta = MatrixF.dot(gradients_o, hidden_t);

		// Adjust the weight by deltas
		weights_ho.add(weight_ho_delta);
		// Adjust the bias by its deltas (which is just the gradients)
		bias_o.add(gradients_o);

		// Calculate the hidden layer errors
		// ERROR = TARGET - OUTPUT
		MatrixF weights_ho_t = MatrixF.transpose(weights_ho);
		MatrixF errors_h = MatrixF.dot(weights_ho_t, errors_o);

		// Calculate hidden gradients
		MatrixF gradients_h = MatrixF.map(hidden, (x, i, j) -> dsigmoided(x));
		gradients_h.multiply(errors_h);
		gradients_h.multiply(learing_rate);

		// Calculate input -> hidden deltas
		MatrixF inputs_t = MatrixF.transpose(inputs);
		MatrixF weights_ih_delta = MatrixF.dot(gradients_h, inputs_t);

		// Adjust the weight by deltas
		weights_ih.add(weights_ih_delta);
		// Adjust the bias by its deltas (which is just the gradients)
		bias_h.add(gradients_h);
	}

	private MatrixF genLayer(MatrixF weights, MatrixF preLayer, MatrixF bias) {
		// Generating the layer output
		MatrixF layer = MatrixF.dot(weights, preLayer);
		layer.add(bias);
		// Activation function
		layer.map((x, i, j) -> sigmoid(x));
		return layer;
	}

	public static class Data {
		public float[] inputs;
		public float[] targets;

		public Data(float[] inputs, float[] targets) {
			this.inputs = inputs;
			this.targets = targets;
		}
	}
}
