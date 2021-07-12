package com.sunflow.examples.supervised;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.sunflow.game.GameBase;
import com.sunflow.logging.LogManager;
import com.sunflow.simpleneuralnetwork.convolutional.CNN;
import com.sunflow.util.MNISTDecoder;
import com.sunflow.util.MNISTDecoder.Digit;

public class DigitClassifierNew extends GameBase {
	public static void main(String[] args) { new DigitClassifierNew(); }

	private String bestDigitClassifier = "rec/brains/bestDigitClassifier";

	private static final int SIZE = 28;
	private static final int size = SIZE * 3;

	private static final int ROWS = 4;
	private static final int COLS = 3;

	private List<Digit> digits;

	private CNN brain;

	private int red = color(255, 0, 0);
	private int green = color(0, 255, 0);

	private boolean learning = false;
	private boolean predict = false;

	@Override
	public void setup() {
		String pathData = "rec/mnist/train-images.idx3-ubyte";
		String pathLabels = "rec/mnist/train-labels.idx1-ubyte";

		long start = System.currentTimeMillis();

		digits = MNISTDecoder.loadDataSet(pathData, pathLabels);

//		brain = new CNN(SIZE * SIZE, 10, 50);
//		brain.setLearningRate(0.01);
		brain = new CNN(new CNN.Option()
				.input(SIZE * SIZE)
				.output(10)
				.learning_rate(0.001f));

		long end = System.currentTimeMillis();
		long dt = end - start;
		LogManager.info(dt);

		LogManager.info("Found Datasets: " + digits.size());

		digits.forEach(digit -> {
			float[] input = new float[SIZE * SIZE];

			for (int x = 0; x < SIZE; x++) for (int y = 0; y < SIZE; y++) {
				int gray = MNISTDecoder.toUnsignedByte(digit.data[x][y]);
				input[index(x, y, SIZE)] = gray / 255f;
			}
			float[] target = new float[10];
			target[digit.label] = 1;

			brain.addData(brain.input(input), brain.target(target));
		});

		createCanvas(COLS * size * 2 + SIZE * 4, ROWS * (size + SIZE) + SIZE);
		showInfo(true);
		textSize(size);
		infoSize(20);
		textAlign(CENTER, CENTER);
	}

	private int learning_steps = 0;

	long start;

	@Override
	public void update() {
		if (!learning) return;

		brain.train(new CNN.Option.Training().epoch(1).batch(500), (i, loss) -> {
			learning_steps += 500;
			if (learning_steps == 10000) System.out.println(System.currentTimeMillis() - start);
		}, () -> {

		});
	}

//	@Override
//	protected void draw() {
//		if (!predict) return;
//		predict = false;
//		predict();
//	}

	private void predict() {
		if (learning) info("Learning steps: " + learning_steps);
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

			AtomicReference<float[]> bla = new AtomicReference<>();
			this.brain.classify(this.brain.input(input), (error, result) -> {
				error.ifPresent(System.err::println);
				result.ifPresent(r -> {
					bla.set(r.prediction);
				});

			});
			float[] prediction = bla.get();

			int maxI = -1;
			float max = -1;
			for (int i = 0; i < prediction.length; i++) {
				if (prediction[i] < max) continue;
				max = prediction[i];
				maxI = i;
			}
			boolean right = maxI == d.label;
			// draw
			int xpos = x * size * 2 + x * SIZE + SIZE;
			int ypos = y * (size + SIZE) + SIZE;
			noSmooth();
			image(digits.get(index), xpos, ypos, size, size);
//			info(digits.get(index).toData());

			stroke(255);
			fill(250, 0, 0, 0);
			rect(xpos, ypos, size, size);

			noStroke();
			fill(255);
			smooth();
//				text(digits.get(index).toString(), xpos + size * 1.4f, ypos + size * 0.5f);
			text("" + maxI, xpos + size * 1.4f, ypos + size * 0.5f);

			noFill();
//				stroke(right ? green : red);
//				rect(xpos + size, ypos, size, size);
			stroke(right ? color(0, 255, 0) : color(255, 0, 0));
			rect(xpos, ypos, size, size);

		}
	}

	@Override
	public void mousePressed(MouseEvent e) { predict = true; predict(); }

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case SPACE:
				brain.randomize();
				break;
			case ENTER:
				learning = !learning;
				start = System.currentTimeMillis();
				LogManager.debug("I am now " + (learning ? "" : "not ") + "learning!");
				break;
			case KeyEvent.VK_S:
				serialize(bestDigitClassifier, brain);
				LogManager.info("serialized");
				break;
			case KeyEvent.VK_L:
				brain = (CNN) deserialize(bestDigitClassifier);
				LogManager.info("deserialized");
				break;
			default:
				break;
		}
//		predict = true;
		predict();
	}
}
