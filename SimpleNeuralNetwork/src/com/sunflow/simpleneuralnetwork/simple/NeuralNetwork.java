package com.sunflow.simpleneuralnetwork.simple;

import java.io.Serializable;

import com.sunflow.logging.Log;
import com.sunflow.math3d.MatrixF;
import com.sunflow.simpleneuralnetwork.util.ActivationFunction;
import com.sunflow.util.GameUtils;
import com.sunflow.util.Mapper;

public class NeuralNetwork implements Cloneable, Serializable, GameUtils {
	public static ActivationFunction sigmoid = new ActivationFunction(
			x -> 1f / (1f + (float) Math.exp(-x)),
			y -> y * (1 - y));

	public static ActivationFunction tanh = new ActivationFunction(
			x -> (float) Math.tanh(x),
			y -> 1f - y * y);

	private int nodes_inputs;
	private int nodes_hidden;
	private int nodes_outputs;

	private MatrixF weights_ih;
	private MatrixF weights_ho;

	private MatrixF bias_h;
	private MatrixF bias_o;

	private float learning_rate;
	private ActivationFunction activation_function;

	public NeuralNetwork(int nodes_inputs, int nodes_outputs, int nodes_hidden) {
		this.nodes_inputs = nodes_inputs;
		this.nodes_outputs = nodes_outputs;
		this.nodes_hidden = nodes_hidden;

		this.weights_ih = new MatrixF(nodes_hidden, nodes_inputs);
		this.weights_ho = new MatrixF(nodes_outputs, nodes_hidden);
//		this.weights_ih.randomize();
//		this.weights_ho.randomize();

		this.bias_h = new MatrixF(nodes_hidden, 1);
		this.bias_o = new MatrixF(nodes_outputs, 1);
//		this.bias_h.randomize();
//		this.bias_o.randomize();

		this.setLearningRate(0.1F);
		this.setActivationFunction(sigmoid);
		randomize();
	}

	public void randomize() {
		this.weights_ih.randomize();
		this.weights_ho.randomize();
		this.bias_h.randomize();
		this.bias_o.randomize();
	}

	public double[] predict(double[] inputs_array) {
		return convertArray(predict(convertArray(inputs_array)));
	}

	public float[] predict(float[] inputs_array) {
		if (inputs_array.length != nodes_inputs) {
			Log.error("NeuralNetwork#predict: inputs and nn_inputs didnt match");
		}
		MatrixF inputs = MatrixF.fromArray(inputs_array);
		MatrixF outputs = predict(inputs);
		return outputs.toArray();
	}

	public MatrixF predict(MatrixF inputs) {
		// Generating the hidden outputs
		MatrixF hidden = genLayer(weights_ih, inputs, bias_h);
		// Generating the real outputs
		MatrixF outputs = genLayer(weights_ho, hidden, bias_o);
		// Sending back to the caller!
		return outputs;
	}

	public void setLearningRate(double learning_rate) { setLearningRate((float) learning_rate); }

	public void setLearningRate(float learning_rate) { this.learning_rate = learning_rate; }

	public void setActivationFunction(ActivationFunction func) { this.activation_function = func; }

	public void train(double[] inputs_array, double[] target_array) {
		train(convertArray(inputs_array), convertArray(target_array));
	}

	public void train(float[] inputs_array, float[] targets_array) {
		if (inputs_array.length != nodes_inputs) {
			Log.error("NeuralNetwork#train: input and nn_input didnt match");
		}
		if (targets_array.length != nodes_outputs) {
			Log.error("NeuralNetwork#train: target and nn_output didnt match");
		}
		MatrixF inputs = MatrixF.fromArray(inputs_array);
		MatrixF targets = MatrixF.fromArray(targets_array);
		train(inputs, targets);
	}

	public void train(MatrixF inputs, MatrixF targets) {
		// Generating the hidden outputs
		MatrixF hidden = genLayer(weights_ih, inputs, bias_h);
		// Generating the real outputs
		MatrixF outputs = genLayer(weights_ho, hidden, bias_o);

		// Calculate the output layer errors
		// ERROR = TARGET - OUTPUT
		MatrixF errors_o = MatrixF.substract(targets, outputs);
		adjustLayer(errors_o, outputs, hidden, weights_ho, bias_o);

		// Calculate the hidden layer errors
		// ERROR = TARGET - OUTPUT
		MatrixF weights_ho_t = MatrixF.transpose(weights_ho);
		MatrixF errors_h = MatrixF.dot(weights_ho_t, errors_o);
		adjustLayer(errors_h, hidden, inputs, weights_ih, bias_h);
	}

	private MatrixF genLayer(MatrixF weights, MatrixF inputs, MatrixF bias) {
		// Generating the layer output
		MatrixF outputs = MatrixF.dot(weights, inputs);
		outputs.add(bias);
		// Activation function
		outputs.map(activation_function.func);
		return outputs;
	}

	private void adjustLayer(MatrixF errors, MatrixF layer, MatrixF layer_p, MatrixF weights, MatrixF bias) {
		// Calculate gradients
		MatrixF gradients = MatrixF.map(layer, activation_function.dfunc);
		gradients.multiply(errors);
		gradients.multiply(learning_rate);

		// Calculate deltas
		MatrixF layer_p_t = MatrixF.transpose(layer_p);
		MatrixF weights_delta = MatrixF.dot(gradients, layer_p_t);

		// Adjust the weights by deltas
		weights.add(weights_delta);
		// Adjust the bias by its deltas (which is just the gradients)
		bias.add(gradients);
	}

	@Override
	public NeuralNetwork clone() {
		NeuralNetwork clone = new NeuralNetwork(nodes_inputs, nodes_outputs, nodes_hidden);
		clone.nodes_inputs = this.nodes_inputs;
		clone.nodes_outputs = this.nodes_outputs;
		clone.nodes_hidden = this.nodes_hidden;

		clone.weights_ih = this.weights_ih.clone();
		clone.weights_ho = this.weights_ho.clone();

		clone.bias_h = this.bias_h.clone();
		clone.bias_o = this.bias_o.clone();

		clone.activation_function = this.activation_function.clone();
		clone.learning_rate = this.learning_rate;

		return clone;
	}

	public void mutate(Mapper func) {
		weights_ih.map(func);
		weights_ho.map(func);
		bias_h.map(func);
		bias_o.map(func);
	}
}
