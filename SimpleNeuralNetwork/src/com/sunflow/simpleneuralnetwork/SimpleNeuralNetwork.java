package com.sunflow.simpleneuralnetwork;

import java.awt.Graphics2D;

import com.sunflow.game.Game2D;
import com.sunflow.util.Log;
import com.sunflow.util.Utils;

public class SimpleNeuralNetwork extends Game2D {
	public static void main(String[] args) {
		new SimpleNeuralNetwork();
	}

	private NeuralNetwork brain;

	float[][][] training_data;

	@Override
	protected void preSetup() {
		training_data = new float[][][] {
				{ { 0, 0 }, { 1 } },
				{ { 0, 1 }, { 1 } },
				{ { 1, 0 }, { 0 } },
				{ { 1, 1 }, { 0 } }
		};
	}

	@Override
	protected void setup() {
		createCanvas(400, 400);

		brain = new NeuralNetwork(2, 2, 1);

		for (int i = 0; i < 5000; i++) {
			float[][] data = training_data[Utils.random(0, 3)];
			brain.train(data[0], data[1]);
		}
		Log.info(brain.feedforward(training_data[0][0]));
		Log.info(brain.feedforward(training_data[1][0]));
		Log.info(brain.feedforward(training_data[2][0]));
		Log.info(brain.feedforward(training_data[3][0]));
	}

	@Override
	protected void render(Graphics2D g) {}
}
