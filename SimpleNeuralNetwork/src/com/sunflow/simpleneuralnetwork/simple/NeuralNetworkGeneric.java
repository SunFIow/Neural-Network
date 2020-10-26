package com.sunflow.simpleneuralnetwork.simple;

import java.io.Serializable;

import com.sunflow.logging.Log;
import com.sunflow.math3d.SMatrix;
import com.sunflow.simpleneuralnetwork.util.ActivationFunction;
import com.sunflow.util.GameUtils;
import com.sunflow.util.Mapper;

public class NeuralNetworkGeneric implements Cloneable, Serializable, GameUtils {
	public static ActivationFunction sigmoid = new ActivationFunction(
			x -> 1f / (1f + (float) Math.exp(-x)),
			y -> y * (1 - y));

	public static ActivationFunction tanh = new ActivationFunction(
			x -> (float) Math.tanh(x),
			y -> 1f - y * y);

	private int nodes_inputs;
	private int nodes_outputs;
	private int[] nodes_hidden;

	private SMatrix[] weights;
	private SMatrix[] bias;

	private float learning_rate;
	private ActivationFunction activation_function;

	public NeuralNetworkGeneric(int nodes_inputs, int nodes_outputs, int... nodes_hidden) {
		this.nodes_inputs = nodes_inputs;
		this.nodes_outputs = nodes_outputs;
		this.nodes_hidden = nodes_hidden;

		this.weights = new SMatrix[nodes_hidden.length + 1];
		this.bias = new SMatrix[nodes_hidden.length + 1];

		for (int i = 0; i <= nodes_hidden.length; i++) {
			int nodes_I = i == 0 ? nodes_inputs : nodes_hidden[i - 1];
			int nodes_O = i == nodes_hidden.length ? nodes_outputs : nodes_hidden[i];

			this.weights[i] = new SMatrix(nodes_O, nodes_I);
//			this.weights[i].randomize();

			this.bias[i] = new SMatrix(nodes_O, 1);
//			this.bias[i].randomize();
		}

		this.setLearningRate(0.1F);
		this.setActivationFunction(sigmoid);
		randomize();
	}

	public void randomize() {
		for (SMatrix weight : weights) weight.randomize();
		for (SMatrix bias : bias) bias.randomize();
	}

	public double[] predict(double[] inputs_array) {
		return convertArray(predict(convertArray(inputs_array)));
	}

	public float[] predict(float[] inputs_array) {
		if (inputs_array.length != nodes_inputs) {
			Log.error("NeuralNetwork#predict: input and nn_input didnt match");
		}
		SMatrix inputs = SMatrix.fromArray(inputs_array);
		SMatrix outputs = predict(inputs);
		return outputs.toArray();
	}

	public SMatrix predict(SMatrix inputs) {
		// Generating the outputs
		SMatrix outputs = inputs;
		for (int i = 0; i <= nodes_hidden.length; i++)
			outputs = genLayer(weights[i], outputs, bias[i]);
		// Sending back to the caller!
		return outputs;
	}

	public void setLearningRate(double learning_rate) { setLearningRate((float) learning_rate); }

	public void setLearningRate(float learning_rate) { this.learning_rate = learning_rate; }

	public void setActivationFunction(ActivationFunction func) { this.activation_function = func; }

	public void train(double[] inputs_array, double[] target_array) {
		train(convertArray(inputs_array), convertArray(target_array));
	}

	public void train(float[] inputs_array, float[] target_array) {
		if (inputs_array.length != nodes_inputs) {
			Log.error("NeuralNetwork#train: input and nn_input didnt match");
		}
		if (target_array.length != nodes_outputs) {
			Log.error("NeuralNetwork#train: target and nn_output didnt match");
		}
		SMatrix inputs = SMatrix.fromArray(inputs_array);
		SMatrix targets = SMatrix.fromArray(target_array);
		train(inputs, targets);
	}

	public void train(SMatrix inputs, SMatrix targets) {
		SMatrix[] outputs = new SMatrix[nodes_hidden.length + 1];
		for (int i = 0; i <= nodes_hidden.length; i++) {
			outputs[i] = genLayer(weights[i], i == 0 ? inputs : outputs[i - 1], bias[i]);
		}

		SMatrix errors_p = null;
		for (int i = nodes_hidden.length; i >= 0; i--) {
			SMatrix errors;
			SMatrix layer = outputs[i];
			SMatrix layer_p = i == 0 ? inputs : outputs[i - 1];
			SMatrix weights = this.weights[i];
			SMatrix bias = this.bias[i];
			if (i == nodes_hidden.length) {
				errors = SMatrix.substract(targets, outputs[nodes_hidden.length]);
			} else {
				SMatrix weights_p = this.weights[i + 1];
				SMatrix weights_p_t = SMatrix.transpose(weights_p);
				errors = SMatrix.dot(weights_p_t, errors_p);
			}
			errors_p = errors;

			adjustLayer(errors, layer, layer_p, weights, bias);
		}
	}

	private SMatrix genLayer(SMatrix weights, SMatrix inputs, SMatrix bias) {
		// Generating the layer output
		SMatrix outputs = SMatrix.dot(weights, inputs);
		outputs.add(bias);
		// Activation function
		outputs.map(activation_function.func);
		return outputs;
	}

	private void adjustLayer(SMatrix errors, SMatrix layer, SMatrix layer_p, SMatrix weights, SMatrix bias) {
		// Calculate gradients
		SMatrix gradients = SMatrix.map(layer, activation_function.dfunc);
		gradients.multiply(errors);
		gradients.multiply(learning_rate);

		// Calculate deltas
		SMatrix layer_p_t = SMatrix.transpose(layer_p);
		SMatrix weights_delta = SMatrix.dot(gradients, layer_p_t);

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
		for (SMatrix weights : weights) weights.map(func);
		for (SMatrix bias : bias) bias.map(func);
	}
}
