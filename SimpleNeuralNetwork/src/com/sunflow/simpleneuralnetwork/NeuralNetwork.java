package com.sunflow.simpleneuralnetwork;

import com.sunflow.math3d.MatrixF;

public class NeuralNetwork {
	private float sigmoid(float x) {
		return (float) (1 / (1 + Math.exp(-x)));
	}

	private int nodes_input;
	private int nodes_hidden;
	private int nodes_output;

	private MatrixF weights_ih;
	private MatrixF weights_ho;

	private MatrixF bias_h;
	private MatrixF bias_o;

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
	}

	public float[] feedforward(float[] input) {
		MatrixF output = feedforward(MatrixF.fromArray(input));
		return output.toArray();
	}

	public MatrixF feedforward(MatrixF input) {
		// Generating the hidden outputs
		MatrixF hidden = MatrixF.dot(weights_ih, input);
		hidden.add(bias_h);
		// Activation function
		hidden.map((x, i, j) -> sigmoid(x));

		// Generating the real outputs
		MatrixF output = MatrixF.dot(weights_ho, hidden);
		output.add(bias_o);
		// Activation function
		output.map((x, i, j) -> sigmoid(x));

		// Sending back to the caller!
		return output;

//		MatrixF hidden = genLayer(weights_ih, input, bias_h);
//		MatrixF output = genLayer(weights_ho, hidden, bias_o);
//		return output;
	}

	private MatrixF genLayer(MatrixF weights, MatrixF input, MatrixF bias) {
		// Generating the layer outputs
		MatrixF layer = MatrixF.dot(weights, input);
		layer.add(bias);
		// Activation function
		layer.map((x, i, j) -> sigmoid(x));
		return layer;
	}
}
