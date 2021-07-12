package com.sunflow.simpleneuralnetwork.convolutional;

import java.io.Serializable;

import com.sunflow.math3d.SMatrix;
import com.sunflow.simpleneuralnetwork.util.ActivationFunction;
import com.sunflow.util.Mapper;

public abstract class Layer implements Cloneable, Serializable {
	private static final long serialVersionUID = -4101316699762055216L;

	protected Option options;
	protected SMatrix weights;
	protected SMatrix bias;

	private final String name;

	private Layer(Option options) {
		this.options = options;
		this.weights = new SMatrix(options.output(), options.input());
		this.bias = new SMatrix(options.output(), 1);
		this.name = getDesc() + options.index();
	}

	public Layer(Layer root) {
		this.options = root.options.clone();
		this.weights = root.weights.clone();
		this.bias = root.bias.clone();
		this.name = root.name;
	}

	protected abstract String getDesc();

	@Override
	protected abstract Layer clone();

	protected Option options() { return getOptions(); }

	protected Option getOptions() { return options; }

	public SMatrix getTransposedWeights() { return SMatrix.transpose(weights); }

	public void randomize() {
		this.weights.randomize();
		this.bias.randomize();
	}

	public void mutate(Mapper func) {
		this.weights.map(func);
		this.bias.map(func);
	}

	public void setLearningRate(float amt) { this.options.learning_rate(amt); }

	public void setActivationFunction(ActivationFunction func) { this.options.activation_function(func); }

	protected abstract SMatrix genLayer(SMatrix inputs);

	protected abstract void adjustLayer(SMatrix errors, SMatrix outputs, SMatrix inputs);

	public static class Option implements Cloneable, Serializable {
		private static final long serialVersionUID = 5740389675682300529L;

		protected int index = 0;
		protected float learning_rate = 0.1f;
		protected ActivationFunction activation_function = ActivationFunction.sigmoid;

		protected int input_num;
		protected int output_num;

		public Option() {}

		public Option(Option root) {
			index(root.index);
			learning_rate(root.learning_rate);
			activation_function(root.activation_function);
			input(root.input_num);
			output(root.output_num);
		}

		@Override
		protected Option clone() { return new Option(this); }

		public Option index(int val) { return setIndex(val); }

		public Option setIndex(int index) { this.index = index; return this; }

		public Option learning_rate(float amt) { return setLearning_rate(amt); }

		public Option setLearning_rate(float learning_rate) { this.learning_rate = learning_rate; return this; }

		public Option activation_function(ActivationFunction activation_function) { return setActivation_function(activation_function); }

		public Option setActivation_function(ActivationFunction activation_function) { this.activation_function = activation_function; return this; }

		public Option input(int num) { return setInput(num); }

		public Option setInput(int num) { this.input_num = num; return this; }

		public Option output(int num) { return setOutput(num); }

		public Option setOutput(int num) { this.output_num = num; return this; }

		public int index() { return getIndex(); }

		public int getIndex() { return index; }

		public float learning_rate() { return getLearning_rate(); }

		public float getLearning_rate() { return learning_rate; }

		public ActivationFunction activation_function() { return getActivation_function(); }

		public ActivationFunction getActivation_function() { return activation_function; }

		public int input() { return getInput(); }

		public int getInput() { return input_num; }

		public int output() { return getOutput(); }

		public int getOutput() { return output_num; }
	}

	public static abstract class ImageOption extends Layer.Option {
		private static final long serialVersionUID = 171466182717288300L;

		private int width, height, depth;

		public ImageOption() {}

		public ImageOption(ImageOption root) { super(root); }

		public ImageOption input(int width, int height, int depth) { return setInput(width, height, depth); }

		public ImageOption setInput(int width, int height, int depth) {
			this.width = width;
			this.height = height;
			this.depth = depth;
			input(width * height * depth);
			return this;
		}

		public abstract int calcOutput();

		public int width() { return getWidth(); }

		public int getWidth() { return width; }

		public int height() { return getHeight(); }

		public int getHeight() { return height; }

		public int depth() { return getDepth(); }

		public int getDepth() { return depth; }

		public int outputWidth() { return getOutputWidth(); }

		public abstract int getOutputWidth();

		public int outputHeight() { return getOutputHeight(); }

		public abstract int getOutputHeight();

		public int outputDepth() { return getOutputDepth(); }

		public int getOutputDepth() { return getDepth(); }
	}

	public static class Conv2D extends Layer {
		private static final long serialVersionUID = -4795392320162347546L;

		public Conv2D(Layer.Conv2D root) { super(root); }

		public Conv2D(Option options) { super(options); }

		@Override
		protected String getDesc() { return "conv2d_Conv2D"; }

		@Override
		protected Layer.Conv2D clone() { return new Layer.Conv2D(this); }

		@Override
		protected Option getOptions() { return (Option) super.getOptions(); }

		@Override
		protected SMatrix genLayer(SMatrix inputs) { throw new UnsupportedOperationException(); }

		@Override
		protected void adjustLayer(SMatrix errors, SMatrix outputs, SMatrix inputs) { throw new UnsupportedOperationException(); }

		public static class Option extends ImageOption {
			private static final long serialVersionUID = -709751690049589038L;

			private int filter_size, filter_num;

			public Option() {}

			public Option(Option option) { super(option); }

			@Override
			protected Option clone() { return new Option(this); }

			@Override
			public int calcOutput() { return this.output_num = outputWidth() * outputHeight() * outputDepth() * filterNum(); }

			public Option filterNum(int num) { return setFilterNum(num); }

			public Option setFilterNum(int num) { this.filter_num = num; return this; }

			public Option filterSize(int size) { return setFilterSize(size); }

			public Option setFilterSize(int size) { this.filter_size = size; return this; }

			public int filterNum() { return getFilterNum(); }

			public int getFilterNum() { return filter_num; }

			public int filterSize() { return getFilterSize(); }

			public int getFilterSize() { return filter_size; }

			@Override
			public int getOutputWidth() { return getWidth() - (getFilterSize() / 2 - 1); }

			@Override
			public int getOutputHeight() { return getHeight() - (getFilterSize() / 2 - 1); }

			@Override
			public int getOutput() { return getOutputWidth() * getOutputHeight() * getDepth() * getFilterNum(); }
		}
	}

	public static class MaxPooling2D extends Layer {
		private static final long serialVersionUID = 4626840094560779846L;

		public MaxPooling2D(Layer.MaxPooling2D root) {
			super(root);
		}

		public MaxPooling2D(Option options) {
			super(options);
			weights.map(w -> 1);
			bias.map(b -> 0);
		}

		@Override
		protected String getDesc() { return "max_pooling2d_MaxPooling2D"; }

		@Override
		protected Layer.MaxPooling2D clone() { return new Layer.MaxPooling2D(this); }

		@Override
		protected Option getOptions() { return (Option) super.getOptions(); }

		@Override
		public void randomize() {}

		@Override
		public void mutate(Mapper func) {}

		@Override
		protected SMatrix genLayer(SMatrix inputs) {
			int widthIn = getOptions().getWidth();
			int widthOut = getOptions().getOutputWidth();
			int heightOut = getOptions().getOutputHeight();
			int depthOut = getOptions().getOutputDepth();
			int size = getOptions().getSize();
			int stride = getOptions().getStride();

			float[][] data = inputs.data();
			float[][] dataOut = new float[widthOut * heightOut][depthOut];
			for (int y = 0; y < heightOut; y++) for (int x = 0; x < widthOut; x++) {
				int indexOut = x + y * widthOut;
//				for (int d = 0; d < depthOut; d++) {
//					float max = 0;
//					for (int sy = 0; sy < size; sy++) for (int sx = 0; sx < size; sx++) {
//						int indexIn = (x * stride + sx) + (y * stride + sy) * widthIn;
//						float current = data[indexIn][d];
//						if (current > max) max = current;
//					}
//					dataOut[indexOut][d] = max;
//				}
				float[] max = new float[depthOut];
				for (int sy = 0; sy < size; sy++) for (int sx = 0; sx < size; sx++) {
					int indexIn = (x * stride + sx) + (y * stride + sy) * widthIn;
					for (int d = 0; d < depthOut; d++) {
						float current = data[indexIn][d];
						if (current > max[d]) max[d] = current;
					}
				}
				dataOut[indexOut] = max;
			}

			SMatrix output = new SMatrix(dataOut);
			return output;
		}

		@Override
		protected void adjustLayer(SMatrix errors, SMatrix outputs, SMatrix inputs) {}

		public static class Option extends ImageOption {
			private static final long serialVersionUID = -4105240036232380627L;

			private int stride;
			private int size;

			public Option() {}

			public Option(Option option) { super(option); }

			@Override
			protected Option clone() { return new Option(this); }

			@Override
			public int calcOutput() { return this.output_num = outputWidth() * outputHeight() * outputDepth() * input(); }

			public Option size(int size) { return setSize(size); }

			public Option setSize(int size) { this.size = size; return this; }

			public Option stride(int stride) { return setStride(stride); }

			public Option setStride(int stride) { this.stride = stride; return this; }

			public int size() { return getSize(); }

			public int getSize() { return size; }

			public int stride() { return getStride(); }

			public int getStride() { return stride; }

			@Override
			public int getOutputWidth() { return getWidth() / stride; }

			@Override
			public int getOutputHeight() { return getHeight() / stride; }

			@Override
			public int getOutput() { return getOutputWidth() * getOutputHeight() * getDepth(); }
		}
	}

	public static class Flatten extends Layer {
		private static final long serialVersionUID = -8339177460928188082L;

		public Flatten(Layer.Flatten root) { super(root); }

		public Flatten(Option options) { super(options); }

		@Override
		protected String getDesc() { return "flatten_Flatten"; }

		@Override
		protected Layer.Flatten clone() { return new Layer.Flatten(this); }

		@Override
		protected Option getOptions() { return (Option) super.getOptions(); }

		@Override
		protected SMatrix genLayer(SMatrix inputs) {
			int widthIn = getOptions().getWidth();
			int heightIn = getOptions().getHeight();
			int depthIn = getOptions().getDepth();

			float[][] data = inputs.data();
			float[][] dataOut = new float[getOptions().getOutput()][1];
			int index = 0;
			for (int y = 0; y < heightIn; y++) for (int x = 0; x < widthIn; x++) for (int d = 0; d < depthIn; d++) {
				dataOut[index++][0] = data[x + y * widthIn][d];
			}
			SMatrix output = new SMatrix(dataOut);
			return output;
		}

		@Override
		protected void adjustLayer(SMatrix errors, SMatrix outputs, SMatrix inputs) {}

		public static class Option extends ImageOption {
			private static final long serialVersionUID = -4105240036232380627L;

			public Option() {}

			public Option(Option option) { super(option); }

			@Override
			protected Option clone() { return new Option(this); }

			@Override
			public int calcOutput() { return this.output_num = outputWidth() * outputHeight() * outputDepth(); }

			@Override
			public int getOutputWidth() { return getOutput(); }

			@Override
			public int getOutputHeight() { return 1; }

			@Override
			public int getOutputDepth() { return 1; }

		}
	}

	public static class Dense extends Layer {
		private static final long serialVersionUID = -1830707487137135556L;

		public Dense(Layer.Dense root) { super(root); }

		public Dense(Option options) { super(options); }

		@Override
		protected String getDesc() { return "dense_Dense"; }

		@Override
		protected Layer.Dense clone() { return new Layer.Dense(this); }

		@Override
		protected Option getOptions() { return super.getOptions(); }

		@Override
		protected SMatrix genLayer(SMatrix inputs) {
			// Generating the layer output
			SMatrix outputs = SMatrix.dot(weights, inputs);
			outputs.add(bias);
			// Activation function
			outputs.map(options.activation_function().func);
			return outputs;
		}

		@Override
		protected void adjustLayer(SMatrix errors, SMatrix outputs, SMatrix inputs) {
			// Calculate gradients
			SMatrix gradients = SMatrix.map(outputs, options.activation_function().dfunc);
			gradients.multiply(errors);
			gradients.multiply(options.learning_rate());

			// Calculate deltas
			SMatrix layer_p_t = SMatrix.transpose(inputs);
			SMatrix weights_delta = SMatrix.dot(gradients, layer_p_t);

			// Adjust the weights by deltas
			weights.add(weights_delta);
			// Adjust the bias by its deltas (which is just the gradients)
			bias.add(gradients);
		}
	}
}
