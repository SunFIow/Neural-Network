package com.sunflow.examples;

import java.awt.Graphics2D;

import com.sunflow.game.Game2D;
import com.sunflow.simpleneuralnetwork.NeuralNetwork;
import com.sunflow.util.Utils;

public class Xor extends Game2D {
	public static void main(String[] args) {
		new Xor();
	}

	private NeuralNetwork brain;
	float[][][] training_data;

	private String predict00;
	private String predict01;
	private String predict10;
	private String predict11;
	private String iterationS;
	private int iteration;

	@Override
	protected void refresh() {
		brain = null;
		training_data = null;
		iteration = 0;
	}

	@Override
	protected void setup() {
		createCanvas(400, 400);
		training_data = new float[][][] {
				{ { 0, 0 }, { 0 } },
				{ { 0, 1 }, { 1 } },
				{ { 1, 0 }, { 1 } },
				{ { 1, 1 }, { 0 } }
		};
		brain = new NeuralNetwork(2, 4, 1);
		brain.setLearningRate(0.2F);
	}

	@Override
	protected void tick(double multiplier) {
		for (int i = 0; i < 100; i++) {
			float[][] data = training_data[Utils.random(0, 3)];
			brain.train(data[0], data[1]);
			iteration++;
		}

		if (frames % (targetFrameRate / 2) == 0) {
			iterationS = "Iteration :   " + iteration;
			predict00 = "0 | 0 :   " + brain.predict(training_data[0][0])[0];
			predict01 = "0 | 1 :   " + brain.predict(training_data[1][0])[0];
			predict10 = "1 | 0 :   " + brain.predict(training_data[2][0])[0];
			predict11 = "1 | 1 :   " + brain.predict(training_data[3][0])[0];
		}
	}

	@Override
	protected void render(Graphics2D g) {
		strokeWeight(10);
		stroke(0, 255, 0);
		fill(255, 0, 255);
		text("Test", 100, 100, 40);

		int resolution = 10;
		float cols = width / resolution;
		float rows = height / resolution;

		stroke(0, 0, 0);
		strokeWeight(1);
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				float x1 = i / cols;
				float x2 = j / rows;
				float[] inputs = { x1, x2 };
				float guess = brain.predict(inputs)[0];
				fill((int) Math.floor(guess * 255F));
				rect(i * resolution, j * resolution, resolution, resolution);
			}
		}

		fill(255);
		stroke(0, 0, 0, 150);
		strokeWeight(10);
		text(iterationS, width / 2 - 75, height / 2 - 50, 16);
		text(predict00, width / 2 - 75, height / 2 - 30, 16);
		text(predict01, width / 2 - 75, height / 2 - 10, 16);
		text(predict10, width / 2 - 75, height / 2 + 10, 16);
		text(predict11, width / 2 - 75, height / 2 + 30, 16);
	}
}