package com.sunflow.simpleneuralnetwork.convolutional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.sunflow.logging.LogManager;
import com.sunflow.math3d.SMatrix;
import com.sunflow.simpleneuralnetwork.util.ActivationFunction;
import com.sunflow.util.Mapper;

public class CNN implements Cloneable, Serializable { // TODO: make inputs and targets nameable
	private static final long serialVersionUID = -6744523423170970751L;

	private Option options;
	private List<Data> data;
	private List<Layer> layers;

	private int nodes_inputs;
	private int nodes_outputs;

	public CNN(Option options) {
		this.options = options;
		this.initLayers();
		this.randomize();
		this.data = new ArrayList<>();
	}

	public CNN(CNN root) {
		this.options = root.options.clone();

		this.nodes_inputs = options.input();
		this.nodes_outputs = options.output();

		this.layers = root.layers.stream().map(Layer::clone).collect(Collectors.toList());
		this.data = root.getData().stream().map(Data::clone).collect(Collectors.toList());
	}

	private void initLayers() {
		this.nodes_inputs = options.input();
		this.nodes_outputs = options.output();

		this.layers = new ArrayList<>();

		switch (options.task()) {
			case REGRESSION:
				initRegression();
				break;
			case CLASSIFICATION:
				initClassification();
				break;
			case IMAGE_CLASSIFICATION:
				initImageClassification();
				break;
			default:
				throw new IllegalStateException(String.format("The Task '%s' is not known!", options.task()));
		}
	}

	private void initClassification() { throw new UnsupportedOperationException(); }

	private void initRegression() {
		int nodes_hidden = 16 * nodes_outputs;
		Layer.Dense dense1 = new Layer.Dense(new Layer.Dense.Option()
				.index(1)
				.activation_function(options.activation_function())
				.learning_rate(options.learning_rate())
				.input(nodes_inputs)
				.output(nodes_hidden));
		Layer.Dense dense2 = new Layer.Dense(new Layer.Dense.Option()
				.index(2)
				.activation_function(options.activation_function())
				.learning_rate(options.learning_rate())
				.input(nodes_hidden)
				.output(nodes_outputs));
		layers.add(dense1);
		layers.add(dense2);
	}

	private void initImageClassification() {
		Layer.Conv2D.Option opConv2D1 = new Layer.Conv2D.Option();
		opConv2D1.index(1);
		opConv2D1.activation_function(options.activation_function());
		opConv2D1.learning_rate(options.learning_rate());
		opConv2D1.input(64, 64, 4);
		opConv2D1.filterSize(5);
		opConv2D1.filterNum(8);
		opConv2D1.calcOutput();

		Layer.MaxPooling2D.Option opMaxPooling2D1 = new Layer.MaxPooling2D.Option();
		opMaxPooling2D1.index(2);
		opMaxPooling2D1.activation_function(options.activation_function());
		opMaxPooling2D1.learning_rate(options.learning_rate());
		opMaxPooling2D1.size(2);
		opMaxPooling2D1.stride(2);
		opMaxPooling2D1.input(opConv2D1.filterNum());
		opMaxPooling2D1.input(opConv2D1.outputWidth(), opConv2D1.outputHeight(), opConv2D1.outputDepth());
		opMaxPooling2D1.calcOutput();

		Layer.Conv2D.Option opConv2D2 = new Layer.Conv2D.Option();
		opConv2D2.index(3);
		opConv2D2.activation_function(options.activation_function());
		opConv2D2.learning_rate(options.learning_rate());
		opConv2D2.input(opMaxPooling2D1.outputWidth(), opMaxPooling2D1.outputHeight(), opMaxPooling2D1.outputDepth());
		opConv2D2.filterSize(5);
		opConv2D2.filterNum(2);
		opConv2D2.calcOutput();

		Layer.MaxPooling2D.Option opMaxPooling2D2 = new Layer.MaxPooling2D.Option();
		opMaxPooling2D2.index(4);
		opMaxPooling2D2.activation_function(options.activation_function());
		opMaxPooling2D2.learning_rate(options.learning_rate());
		opMaxPooling2D2.size(2);
		opMaxPooling2D2.stride(2);
		opMaxPooling2D2.input(opConv2D2.filterNum());
		opMaxPooling2D2.input(opConv2D2.outputWidth(), opConv2D2.outputHeight(), opConv2D2.outputDepth());
		opMaxPooling2D2.calcOutput();

		Layer.Flatten.Option opFlatten1 = new Layer.Flatten.Option();
		opFlatten1.index(5);
		opFlatten1.activation_function(options.activation_function());
		opFlatten1.learning_rate(options.learning_rate());
		opFlatten1.input(opMaxPooling2D2.outputWidth(), opMaxPooling2D2.outputHeight(), opMaxPooling2D2.outputDepth());
		opFlatten1.output(opMaxPooling2D2.output());

		Layer.Dense.Option opDense1 = new Layer.Dense.Option();
		opDense1.index(6);
		opDense1.activation_function(options.activation_function());
		opDense1.learning_rate(options.learning_rate());
		opDense1.input(opFlatten1.output());
		opDense1.output(nodes_outputs);

		Layer.Conv2D conv2D1 = new Layer.Conv2D(opConv2D1);
		Layer.MaxPooling2D maxPooling2D1 = new Layer.MaxPooling2D(opMaxPooling2D1);
		Layer.Conv2D conv2D2 = new Layer.Conv2D(opConv2D2);
		Layer.MaxPooling2D maxPooling2D2 = new Layer.MaxPooling2D(opMaxPooling2D2);
		Layer.Flatten flatten1 = new Layer.Flatten(opFlatten1);
		Layer.Dense dense1 = new Layer.Dense(opDense1);

		layers.add(conv2D1);
		layers.add(maxPooling2D1);
		layers.add(conv2D2);
		layers.add(maxPooling2D2);
		layers.add(flatten1);
		layers.add(dense1);
	}

	public Input input(float... inputs) {
		if (inputs.length != nodes_inputs) {
			LogManager.error("CNN#input: inputs and nn_inputs didn't match");
		}
		return new Input(inputs);
	}

	public Target target(float... targets) {
		if (targets.length != nodes_outputs) {
			LogManager.error("CNN#target: targets and nn_outputs didn't match");
		}
		return new Target(targets);
	}

	public void addData(Input input, Target target) { this.getData().add(new Data(input, target)); } // TODO: Count num of diffrent target names

	public void normalizeData() {
		float min = getData().stream().map(Data::getMin).min(Float::compare).get();
		float max = getData().stream().map(Data::getMax).max(Float::compare).get();
		getData().forEach(d -> d.normalize(min, max));
	}

	public void train(Option.Training options, Runnable finishedTraining) {
		train(options, null, finishedTraining);
	}

	public void train(Option.Training options, BiConsumer<Integer, Float> whileTraining, Runnable finishedTraining) {
		int epochs = options.epoch();
		int batchs = options.batch();

		for (int i = 0; i < epochs; i++) {
			// Shuffle Data
			List<Data> epochData = new ArrayList<>(getData());
			Collections.shuffle(epochData);

			// Train and sum loss
			float loss_sum = 0;
			int j = 0;
			for (Data data : epochData) {
				if (j++ > batchs) break;
				loss_sum += train(data.input.values(), data.target.values());
			}

			// Show current state
			if (whileTraining != null) whileTraining.accept(i, loss_sum);
		}
		finishedTraining.run();
	}

	public void classify(Input input, BiConsumer<Optional<NNError>, Optional<Result>> gotResult) {
		NNError error = null;
		Result result = null;
		try {
			float[] prediction = predict(input.values());
			result = new Result(prediction);
		} catch (PredictionError e) {
			e.printStackTrace();
			error = e;
		}
		gotResult.accept(Optional.ofNullable(error), Optional.ofNullable(result));
	}

	public void randomize() { this.layers.forEach(Layer::randomize); }

	public void mutate(Mapper func) { this.layers.forEach(layer -> layer.mutate(func)); }

	private void setLearningRate(float learning_rate) { this.options.learning_rate(learning_rate); this.layers.forEach(layer -> layer.setLearningRate(learning_rate)); }

	private void setActivationFunction(ActivationFunction func) { this.options.activation_function(func); this.layers.forEach(layer -> layer.setActivationFunction(func)); }

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
		SMatrix[] outputs = new SMatrix[layers.size()];
		for (int i = 0; i < layers.size(); i++) {
			outputs[i] = layers.get(i).genLayer(i == 0 ? inputs : outputs[i - 1]);
		}
		return outputs[outputs.length - 1];
	}

	public float train(float[] inputs_array, float[] targets_array) {
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
		SMatrix[] outputs = new SMatrix[layers.size()];
		outputs[0] = layers.get(0).genLayer(inputs);
		for (int i = 1; i < layers.size(); i++) outputs[i] = layers.get(i).genLayer(outputs[i - 1]);

		SMatrix[] errors = new SMatrix[layers.size()];
		int ls_1 = outputs.length - 1;
		errors[ls_1] = SMatrix.substract(targets, outputs[ls_1]);
		layers.get(ls_1).adjustLayer(errors[ls_1], outputs[ls_1], ls_1 == 0 ? inputs : outputs[ls_1 - 1]);
		for (int i = outputs.length - 2; i >= 1; i--) {
			errors[i] = SMatrix.dot(layers.get(i + 1).getTransposedWeights(), errors[i + 1]);
			layers.get(i).adjustLayer(errors[i], outputs[i], inputs);
		}
		errors[0] = SMatrix.dot(layers.get(1).getTransposedWeights(), errors[1]);
		layers.get(0).adjustLayer(errors[0], outputs[0], inputs);

		float error_o_sum = 0;
//		for (float x : errors[ls_1].toArray()) error_o_sum += x;
		for (float[] x : errors[ls_1].data()) for (float y : x) error_o_sum += y;
		return error_o_sum;
	}

	@Override
	public CNN clone() { return new CNN(this); }

	public List<Data> getData() { return data; }

	public static class Option implements Cloneable, Serializable {
		private static final long serialVersionUID = -8343083847763916037L;

		private int input_num = 0;
		private String[] input_names;

		private int output_num = 0;
		private String[] output_names;

		private Task task = Task.REGRESSION;
		private boolean debug = false;
		private float learning_rate = 0.1f;
		private ActivationFunction activation_function = ActivationFunction.sigmoid;

		@Override
		protected Option clone() {
			return new Option()
					.input(input_names.clone())
					.output(output_names.clone())
					.task(task)
					.debug(debug)
					.learning_rate(learning_rate)
					.activation_function(activation_function);
		}

		public Option input(int num) { this.input_num = num; this.input_names = new String[num]; for (int i = 0; i < num; i++) this.input_names[i] = "" + i; return this; }

		public Option input(String... names) { this.input_num = names.length; this.input_names = names; return this; }

		public Option output(int num) { this.output_num = num; this.output_names = new String[num]; for (int i = 0; i < num; i++) this.output_names[i] = "" + i; return this; }

		public Option output(String... names) { this.output_num = names.length; this.output_names = names; return this; }

		public Option task(Task task) { this.task = task; return this; }

		public Option debug(boolean val) { this.debug = val; return this; }

		public Option learning_rate(float amt) { this.learning_rate = amt; return this; }

		public Option activation_function(ActivationFunction activation_function) { this.activation_function = activation_function; return this; }

		public int input() { return input_num; }

		public String[] input_names() { return input_names; }

		public int output() { return output_num; }

		public String[] output_names() { return output_names; }

		public Task task() { return task; }

		public boolean debug() { return debug; }

		public float learning_rate() { return learning_rate; }

		public ActivationFunction activation_function() { return activation_function; }

		public static class Training implements Cloneable, Serializable {
			private static final long serialVersionUID = 5022758943188196258L;

			private int epoch_num = 50;
			private int batch_num = 50;

			@Override
			protected Option.Training clone() {
				return new Option.Training()
						.epoch(epoch_num)
						.batch(batch_num);
			}

			public Option.Training epoch(int num) { this.epoch_num = num; return this; }

			public Option.Training batch(int num) { this.batch_num = num; return this; }

			public int epoch() { return epoch_num; }

			public int batch() { return batch_num; }
		}
	}
}
