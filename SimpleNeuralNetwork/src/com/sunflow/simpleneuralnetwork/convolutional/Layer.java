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
		this.weights = new SMatrix(options.getWeightsRows(), options.getWeightsCols());
		this.bias = new SMatrix(options.getBiasRows(), options.getBiasCols());
		this.name = getDesc() + options.getIndex();
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

	public void setLearningRate(float amt) { this.options.setLearning_rate(amt); }

	public void setActivationFunction(ActivationFunction func) { this.options.setActivation_function(func); }

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
			setIndex(root.index);
			setLearning_rate(root.learning_rate);
			setActivation_function(root.activation_function);
			setInput(root.input_num);
			setOutput(root.output_num);
		}

		@Override
		protected Option clone() { return new Option(this); }

		public Option setIndex(int index) { this.index = index; return this; }

		public Option setLearning_rate(float learning_rate) { this.learning_rate = learning_rate; return this; }

		public Option setActivation_function(ActivationFunction activation_function) { this.activation_function = activation_function; return this; }

		public Option setInput(int num) { this.input_num = num; return this; }

		public Option setOutput(int num) { this.output_num = num; return this; }

		public int getIndex() { return index; }

		public float getLearning_rate() { return learning_rate; }

		public ActivationFunction getActivation_function() { return activation_function; }

		public int getInput() { return input_num; }

		public int getOutput() { return output_num; }

		public int getWeightsRows() { return getOutput(); }

		public int getWeightsCols() { return getInput(); }

		public int getBiasRows() { return getWeightsRows(); }

		public int getBiasCols() { return 1; }
	}

	public static abstract class ImageOption extends Layer.Option {
		private static final long serialVersionUID = 171466182717288300L;

		private int width, height, channels;
		private int count;

		public ImageOption() {}

		public ImageOption(ImageOption root) { super(root); setInput(root.width, root.height, root.channels, root.count); }

		public ImageOption setInput(int width, int height, int channels, int count) {
			setWidth(width);
			setHeight(height);
			setChannels(channels);
			setCount(count);
			setInput(width * height * channels * count);
			return this;
		}

		private void setWidth(int width) { this.width = width; }

		private void setHeight(int height) { this.height = height; }

		private void setChannels(int channels) { this.channels = channels; }

		private void setCount(int count) { this.count = count; }

		public int getWidth() { return width; }

		public int getHeight() { return height; }

		public int getChannels() { return channels; }

		public int getCount() { return count; }

		public abstract int getOutputWidth();

		public abstract int getOutputHeight();

		public abstract int getOutputChannels();

		public abstract int getOutputCount();
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
		protected SMatrix genLayer(SMatrix inputs) {
			final int widthIn = getOptions().getWidth();
			final int heightIn = getOptions().getHeight();
			final int channelsIn = getOptions().getChannels();
			final int countIn = getOptions().getCount();
			final int widthOut = getOptions().getOutputWidth();
			final int heightOut = getOptions().getOutputHeight();
			final int channelsOut = getOptions().getOutputChannels();
			final int countOut = getOptions().getOutputCount();
			final int filterNum = getOptions().getFilterNum();
			final int filterSize = getOptions().getFilterSize();
			final int stride = 1;

			final float[][] FILTERS = weights.data();
			final float[][] dataIn = inputs.data();
			final float[][] dataOut = new float[widthOut * heightOut * channelsOut][countOut];

			for (int y = 0; y < heightIn - filterSize; y += stride) for (int x = 0; x < widthIn - filterSize; x += stride) {
				for (int c = 0; c < countIn; c++) for (int f = 0; f < filterNum; f++) for (int ch = 0; ch < channelsIn; ch++) {
					float[] filter = FILTERS[c * channelsIn * filterNum + f * channelsIn + ch];
					float acc[] = new float[countOut];
					for (int fy = 0; fy < filterSize; fy++) for (int fx = 0; fx < filterSize; fx++) {
						int indexIn = (x + fx) + (y + fy) * widthIn + ch;
						float factor = filter[fx + fy * filterSize];
						float current = dataIn[indexIn][c];
						acc[c * filterNum + f] += current * factor;
					}
					int indexOut = x + y * widthIn + ch;
					dataOut[indexOut] = acc;
				}
			}

			SMatrix output = new SMatrix(dataOut);
			return output;
		}

		@Override
		protected void adjustLayer(SMatrix errors, SMatrix outputs, SMatrix inputs) {
//			throw new UnsupportedOperationException();
		}

		public static class Option extends ImageOption {
			private static final long serialVersionUID = -709751690049589038L;

			private int filter_size, filter_num;

			public Option() {}

			public Option(Option option) { super(option); }

			@Override
			protected Option clone() { return new Option(this); }

			public Option setFilter(int size, int num) { this.filter_size = size; this.filter_num = num; return this; }

			public int getFilterNum() { return filter_num; }

			public int getFilterSize() { return filter_size; }

			@Override
			public int getOutputWidth() { return getWidth() - (getFilterSize() - 1); }

			@Override
			public int getOutputHeight() { return getHeight() - (getFilterSize() - 1); }

			@Override
			public int getOutputChannels() { return getChannels(); }

			@Override
			public int getOutputCount() { return getCount() * getFilterNum(); }

			@Override
			public int getOutput() { return getOutputWidth() * getOutputHeight() * getOutputChannels() * getOutputCount(); }

			@Override
			public int getWeightsRows() { return getCount() * getChannels() * getFilterNum(); }

			@Override
			public int getWeightsCols() { return getFilterSize() * getFilterSize(); }

			@Override
			public int getBiasCols() { return getWeightsCols(); }
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
			final int widthIn = getOptions().getWidth();
			final int channelsIn = getOptions().getChannels();
			final int countIn = getOptions().getCount();
			final int widthOut = getOptions().getOutputWidth();
			final int heightOut = getOptions().getOutputHeight();
			final int channelsOut = getOptions().getOutputChannels();
			final int countOut = getOptions().getOutputCount();
			final int size = getOptions().getSize();
			final int stride = getOptions().getStride();

			final float[][] dataIn = inputs.data();
			final float[][] dataOut = new float[widthOut * heightOut * channelsOut][countOut];
			for (int y = 0; y < heightOut; y++) for (int x = 0; x < widthOut; x++) {
				for (int c = 0; c < channelsIn; c++) {
					float[] max = new float[countIn];
					for (int sy = 0; sy < size; sy++) for (int sx = 0; sx < size; sx++) {
						int indexIn = (x * stride + sx) + (y * stride + sy) * widthIn + c;
						for (int d = 0; d < countIn; d++) {
							float current = dataIn[indexIn][d];
							if (current > max[d]) max[d] = current;
						}
					}
					int indexOut = x + y * widthOut + c;
					dataOut[indexOut] = max;
				}
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
			public int getOutputChannels() { return getChannels(); }

			@Override
			public int getOutputCount() { return getCount(); }

			@Override
			public int getOutput() { return getOutputWidth() * getOutputHeight() * getOutputChannels() * getOutputCount(); }
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
			int channelsIn = getOptions().getChannels();

			float[][] data = inputs.data();
			float[][] dataOut = new float[getOptions().getOutput()][1];
			int index = 0;
			for (int y = 0; y < heightIn; y++) for (int x = 0; x < widthIn; x++) for (int d = 0; d < channelsIn; d++) {
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
			public int getOutputWidth() { return 0; }

			@Override
			public int getOutputHeight() { return 0; }

			@Override
			public int getOutputChannels() { return 0; }

			@Override
			public int getOutputCount() { return 0; }

			@Override
			public int getOutput() { return getWidth() * getHeight() * getChannels() * getCount(); }
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
			outputs.map(options.getActivation_function().func);
			return outputs;
		}

		@Override
		protected void adjustLayer(SMatrix errors, SMatrix outputs, SMatrix inputs) {
			// Calculate gradients
			SMatrix gradients = SMatrix.map(outputs, options.getActivation_function().dfunc);
			gradients.multiply(errors);
			gradients.multiply(options.getLearning_rate());

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
