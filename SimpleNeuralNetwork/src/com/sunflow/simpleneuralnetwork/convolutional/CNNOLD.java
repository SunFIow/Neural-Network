package com.sunflow.simpleneuralnetwork.convolutional;

import com.sunflow.logging.LogManager;
import com.sunflow.math3d.SMatrix;
import com.sunflow.simpleneuralnetwork.util.ActivationFunction;
import com.sunflow.util.Mapper;

public class CNNOLD { // TODO: make inputs and targets nameable
	public static void main(String[] args) {
		CNN.Option options = new CNN.Option();
		CNNOLD model = new CNNOLD(options);
	}

	private final CNN.Option options;

	public CNNOLD(CNN.Option options) {
		this.options = options;

		this.nodes_inputs = options.input();
		this.nodes_outputs = options.output();
		this.nodes_hidden = 10;

		this.weights_ih = new SMatrix(nodes_hidden, nodes_inputs);
		this.weights_ho = new SMatrix(nodes_outputs, nodes_hidden);

		this.bias_h = new SMatrix(nodes_hidden, 1);
		this.bias_o = new SMatrix(nodes_outputs, 1);

		this.setLearningRate(options.learning_rate());
		this.setActivationFunction(options.activation_function());
		this.randomize();
	}

	private void randomize() {
		this.weights_ih.randomize();
		this.weights_ho.randomize();
		this.bias_h.randomize();
		this.bias_o.randomize();
	}

	private int nodes_inputs;
	private int nodes_hidden;
	private int nodes_outputs;

	private SMatrix weights_ih;
	private SMatrix weights_ho;

	private SMatrix bias_h;
	private SMatrix bias_o;

	private float learning_rate;
	private ActivationFunction activation_function;

	private float[] predict(float[] inputs_array) throws PredictionError {
		if (inputs_array.length != nodes_inputs) {
			LogManager.error("CNN#predict: inputs and nn_inputs didn't match");
			throw new PredictionError("NeuralNetwork#predict: inputs and nn_inputs didn't match");
		}
		SMatrix inputs = SMatrix.fromArray(inputs_array);
		SMatrix outputs = predict(inputs);
		return outputs.toArray();

	}

	private SMatrix predict(SMatrix inputs) {
		// Generating the hidden outputs
		SMatrix hidden = genLayer(weights_ih, inputs, bias_h);
		// Generating the real outputs
		SMatrix outputs = genLayer(weights_ho, hidden, bias_o);
		// Sending back to the caller!
		return outputs;
	}

	private void setLearningRate(float learning_rate) { this.learning_rate = learning_rate; }

	private void setActivationFunction(ActivationFunction func) { this.activation_function = func; }

	private float train(float[] inputs_array, float[] targets_array) {
		if (inputs_array.length != nodes_inputs) {
			LogManager.error("CNN#train: input and nn_input didn't match");
		}
		if (targets_array.length != nodes_outputs) {
			LogManager.error("CNN#train: target and nn_output didn't match");
		}
		SMatrix inputs = SMatrix.fromArray(inputs_array);
		SMatrix targets = SMatrix.fromArray(targets_array);
		return train(inputs, targets);
	}

	private float train(SMatrix inputs, SMatrix targets) {
		// Generating the hidden outputs
		SMatrix hidden = genLayer(weights_ih, inputs, bias_h);
		// Generating the real outputs
		SMatrix outputs = genLayer(weights_ho, hidden, bias_o);

		// Calculate the output layer errors
		// ERROR = TARGET - OUTPUT
		SMatrix errors_o = SMatrix.substract(targets, outputs);

		float error_o_sum = 0;
		for (float x : errors_o.toArray()) error_o_sum += x;

		adjustLayer(errors_o, outputs, hidden, weights_ho, bias_o);

		// Calculate the hidden layer errors
		// ERROR = TARGET - OUTPUT
		SMatrix weights_ho_t = SMatrix.transpose(weights_ho);
		SMatrix errors_h = SMatrix.dot(weights_ho_t, errors_o);

//		float error_h_sum = 0;
//		for (float x : errors_h.toArray()) error_h_sum += x;

		adjustLayer(errors_h, hidden, inputs, weights_ih, bias_h);

		return error_o_sum;
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
	public CNNOLD clone() {
		CNNOLD clone = new CNNOLD(this.options);
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
