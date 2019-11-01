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

	public float[] feedforward(float[] input) {
		if (input.length != nodes_input) {
			Log.err("NeuralNetwork#feedforward: input and nn_input didnt match");
		}
		MatrixF output = feedforward(MatrixF.fromArray(input));
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

	public void train(float[] input, float[] target) {
		if (input.length != nodes_input) {
			Log.err("NeuralNetwork#feedforward: input and nn_input didnt match");
		}
		if (target.length != nodes_output) {
			Log.err("NeuralNetwork#feedforward: target and nn_output didnt match");
		}

		MatrixF input_m = MatrixF.fromArray(input);
		MatrixF target_m = MatrixF.fromArray(target);

		// Generating the hidden outputs
		MatrixF hidden = genLayer(weights_ih, input_m, bias_h);
		// Generating the real outputs
		MatrixF output = genLayer(weights_ho, hidden, bias_o);

//		MatrixF output = feedforward(MatrixF.fromArray(input));

		// Calculate the output layer error
		// ERROR = TARGET - OUTPUT
		MatrixF error_o = MatrixF.substract(target_m, output);

		// Calculate output gradient
		MatrixF gradient_o = MatrixF.map(output, (x, i, j) -> dsigmoided(x));
		gradient_o.multiply(error_o);
		gradient_o.multiply(learing_rate);

		// Calculate hidden -> output delta
		MatrixF hidden_t = MatrixF.transpose(hidden);
		MatrixF weight_ho_delta = MatrixF.multiply(gradient_o, hidden_t);
		weights_ho.add(weight_ho_delta);

		// Calculate the hidden layer error
		// ERROR = TARGET - OUTPUT
		MatrixF weight_ho_t = MatrixF.transpose(weights_ho);
		MatrixF error_h = MatrixF.multiply(weight_ho_t, error_o);

		// Calculate hidden gradient
		MatrixF gradient_h = MatrixF.map(hidden, (x, i, j) -> dsigmoided(x));
		gradient_h.multiply(error_h);
		gradient_h.multiply(learing_rate);

		// Calculate input -> hidden delta
		MatrixF input_t = MatrixF.transpose(input_m);
		MatrixF weight_ih_delta = MatrixF.multiply(gradient_h, input_t);
		weights_ih.add(weight_ih_delta);
	}

	private MatrixF genLayer(MatrixF weights, MatrixF input, MatrixF bias) {
		// Generating the layer output
		MatrixF layer = MatrixF.multiply(weights, input);
		layer.add(bias);
		// Activation function
		layer.map((x, i, j) -> sigmoid(x));
		return layer;
	}
}
