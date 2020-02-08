package com.sunflow.simpleneuralnetwork;

import java.io.Serializable;

import com.sunflow.logging.Log;
import com.sunflow.math3d.MatrixF;
import com.sunflow.util.Mapper;
import com.sunflow.util.StaticUtils;

public class NeuralNetworkGeneric implements Cloneable, Serializable {
	public static ActivationFunction sigmoid = new ActivationFunction(
			x -> 1f / (1f + (float) Math.exp(-x)),
			y -> y * (1 - y));

	public static ActivationFunction tanh = new ActivationFunction(
			x -> (float) Math.tanh(x),
			y -> 1f - y * y);

	private int nodes_inputs;
	private int nodes_outputs;
	private int[] nodes_hidden;

	private MatrixF[] weights;
	private MatrixF[] bias;

	private float learning_rate;
	private ActivationFunction activation_function;

	public NeuralNetworkGeneric(int nodes_inputs, int nodes_outputs, int... nodes_hidden) {
		this.nodes_inputs = nodes_inputs;
		this.nodes_outputs = nodes_outputs;
		this.nodes_hidden = nodes_hidden;

		this.weights = new MatrixF[nodes_hidden.length + 1];
		this.bias = new MatrixF[nodes_hidden.length + 1];

		for (int i = 0; i <= nodes_hidden.length; i++) {
			int nodes_I = i == 0 ? nodes_inputs : nodes_hidden[i - 1];
			int nodes_O = i == nodes_hidden.length ? nodes_outputs : nodes_hidden[i];

			this.weights[i] = new MatrixF(nodes_O, nodes_I);
//			this.weights[i].randomize();

			this.bias[i] = new MatrixF(nodes_O, 1);
//			this.bias[i].randomize();
		}

		this.setLearningRate(0.1F);
		this.setActivationFunction(sigmoid);
		randomize();
	}

	public void randomize() {
		for (MatrixF weight : weights) weight.randomize();
		for (MatrixF bias : bias) bias.randomize();
	}

	public double[] predict(double[] inputs_array) {
		return StaticUtils.instance.convertArray(predict(StaticUtils.instance.convertArray(inputs_array)));
	}

	public float[] predict(float[] inputs_array) {
		if (inputs_array.length != nodes_inputs) {
			Log.error("NeuralNetwork#predict: input and nn_input didnt match");
		}
		MatrixF inputs = MatrixF.fromArray(inputs_array);
		MatrixF outputs = predict(inputs);
		return outputs.toArray();
	}

	public MatrixF predict(MatrixF inputs) {
		// Generating the outputs
		MatrixF outputs = inputs;
		for (int i = 0; i <= nodes_hidden.length; i++)
			outputs = genLayer(weights[i], outputs, bias[i]);
		// Sending back to the caller!
		return outputs;
	}

	public void setLearningRate(double learning_rate) { setLearningRate((float) learning_rate); }

	public void setLearningRate(float learning_rate) { this.learning_rate = learning_rate; }

	public void setActivationFunction(ActivationFunction func) { this.activation_function = func; }

	public void train(double[] inputs_array, double[] target_array) {
		train(StaticUtils.instance.convertArray(inputs_array), StaticUtils.instance.convertArray(target_array));
	}

	public void train(float[] inputs_array, float[] target_array) {
		if (inputs_array.length != nodes_inputs) {
			Log.error("NeuralNetwork#train: input and nn_input didnt match");
		}
		if (target_array.length != nodes_outputs) {
			Log.error("NeuralNetwork#train: target and nn_output didnt match");
		}
		MatrixF inputs = MatrixF.fromArray(inputs_array);
		MatrixF targets = MatrixF.fromArray(target_array);
		train(inputs, targets);
	}

	public void train(MatrixF inputs, MatrixF targets) {
		MatrixF[] outputs = new MatrixF[nodes_hidden.length + 1];
		for (int i = 0; i <= nodes_hidden.length; i++) {
			outputs[i] = genLayer(weights[i], i == 0 ? inputs : outputs[i - 1], bias[i]);
		}

		MatrixF errors_p = null;
		for (int i = nodes_hidden.length; i >= 0; i--) {
			MatrixF errors;
			MatrixF layer = outputs[i];
			MatrixF layer_p = i == 0 ? inputs : outputs[i - 1];
			MatrixF weights = this.weights[i];
			MatrixF bias = this.bias[i];
			if (i == nodes_hidden.length) {
				errors = MatrixF.substract(targets, outputs[nodes_hidden.length]);
			} else {
				MatrixF weights_p = this.weights[i + 1];
				MatrixF weights_p_t = MatrixF.transpose(weights_p);
				errors = MatrixF.dot(weights_p_t, errors_p);
			}
			errors_p = errors;

			adjustLayer(errors, layer, layer_p, weights, bias);
		}
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
	public NeuralNetworkGeneric clone() {
		NeuralNetworkGeneric copy = new NeuralNetworkGeneric(nodes_inputs, nodes_outputs, nodes_hidden);
		copy.nodes_inputs = this.nodes_inputs;
		copy.nodes_outputs = this.nodes_outputs;
		copy.nodes_hidden = this.nodes_hidden.clone();

		copy.weights = this.weights.clone();

		copy.bias = this.bias.clone();

		copy.activation_function = this.activation_function.clone();
		copy.learning_rate = this.learning_rate;

		return copy;
	}

	public void mutate(Mapper func) {
		for (MatrixF weights : weights) weights.map(func);
		for (MatrixF bias : bias) bias.map(func);
	}
}
