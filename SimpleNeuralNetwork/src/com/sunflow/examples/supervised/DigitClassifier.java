package com.sunflow.examples.supervised;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import com.sunflow.game.Game2D;
import com.sunflow.logging.Log;
import com.sunflow.simpleneuralnetwork.simple.NeuralNetwork;
import com.sunflow.util.MNISTDecoder;
import com.sunflow.util.MNISTDecoder.Digit;

public class DigitClassifier extends Game2D {
	public static void main(String[] args) { new DigitClassifier(); }

	private String bestDigitClassifier = "rec/brains/bestDigitClassifier";

	private static final int SIZE = 28;
	private static final int size = SIZE * 5;

	private static final int ROWS = 4;
	private static final int COLS = 3;

	private List<Digit> digits;

	private NeuralNetwork brain;

	private int red = color(255, 0, 0);
	private int green = color(0, 255, 0);

	private boolean learning = false;
	private boolean predict = false;

	@Override
	protected void setup() {
		String pathData = "rec/mnist/train-images.idx3-ubyte";
		String pathLabels = "rec/mnist/train-labels.idx1-ubyte";

		long start = System.currentTimeMillis();

		digits = MNISTDecoder.loadDataSet(pathData, pathLabels);

		brain = new NeuralNetwork(SIZE * SIZE, 10, 50);
		brain.setLearningRate(0.01);

		long end = System.currentTimeMillis();
		long dt = end - start;
		Log.info(dt);

		Log.info("Found Datasets: " + digits.size());

		createCanvas(COLS * size * 2 + SIZE * 4, ROWS * (size + SIZE) + SIZE);
		showInfo(true);
		textSize(size);
		textAlign(CENTER, CENTER);
	}

	@Override
	protected void update() {
		if (!learning) return;

		for (int i = 0; i < 100; i++) {
			int index = random(digits.size() - 1);
			Digit d = digits.get(index);

			float[] input = new float[SIZE * SIZE];

			for (int k = 0; k < SIZE; k++) for (int j = 0; j < SIZE; j++) {
				int gray = MNISTDecoder.toUnsignedByte(d.data[j][k]);
				input[index(j, k, SIZE)] = gray / 255f;
			}
			float[] target = new float[10];
			target[d.label] = 1;

			brain.train(input, target);
		}
	}

	@Override
	protected void draw() {
		if (!predict) return;
		predict = false;
		predict();
	}

	private void predict() {
		background(0);
		noFill();
		for (int y = 0; y < ROWS; y++) for (int x = 0; x < COLS; x++) {
			int index = random(digits.size());
			Digit d = digits.get(index);

			float[] input = new float[SIZE * SIZE];

			loadPixels();
			for (int k = 0; k < SIZE; k++) for (int j = 0; j < SIZE; j++) {
				int gray = MNISTDecoder.toUnsignedByte(d.data[j][k]);
				input[index(j, k, SIZE)] = gray / 255f;
			}

			float[] output = brain.predict(input);

			int maxI = -1;
			float max = -1;
			for (int i = 0; i < output.length; i++) {
				if (output[i] < max) continue;
				max = output[i];
				maxI = i;
			}

			boolean right = maxI == d.label;

			// draw
			int xpos = x * size * 2 + x * SIZE + SIZE;
			int ypos = y * (size + SIZE) + SIZE;
			noSmooth();
			image(digits.get(index), xpos, ypos, size, size);

			stroke(255);
			rect(xpos, ypos, size, size);

			noStroke();
			fill(255);
			smooth();
//			text(digits.get(index).toString(), xpos + size * 1.4f, ypos + size * 0.5f);
			text("" + maxI, xpos + size * 1.4f, ypos + size * 0.5f);

			noFill();
			stroke(right ? green : red);
			rect(xpos + size, ypos, size, size);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) { predict = true; }

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case SPACE:
				brain.randomize();
				break;
			case ENTER:
				learning = !learning;
				Log.debug("I am now " + (learning ? "" : "not ") + "learning!");
				break;
			case KeyEvent.VK_S:
				serialize(bestDigitClassifier, brain);
				Log.info("serialized");
				break;
			case KeyEvent.VK_L:
				brain = (NeuralNetwork) deserialize(bestDigitClassifier);
				Log.info("deserialized");
				break;
			default:
				break;
		}
		predict = true;
	}
}
