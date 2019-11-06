package com.sunflow.examples.supervised;

import com.sunflow.game.Game2D;
import com.sunflow.simpleneuralnetwork.NeuralNetwork;
import com.sunflow.util.Log;
import com.sunflow.util.Utils;

public class SimpleNeuralNetwork extends Game2D {
	public static void main(String[] args) {
		new SimpleNeuralNetwork();
	}

	private NeuralNetwork brain;

	double[][][] training_data;

	@Override
	protected void setup() {
		brain = new NeuralNetwork(2, 1, 2);
		training_data = new double[][][] {
				{ { 0, 0 }, { 1 } },
				{ { 0, 1 }, { 1 } },
				{ { 1, 0 }, { 0 } },
				{ { 1, 1 }, { 0 } }
		};

		for (int i = 0; i < 5000; i++) {
			double[][] data = training_data[Utils.random(0, 3)];
			brain.train(data[0], data[1]);
		}
		Log.info(brain.predict(training_data[0][0]));
		Log.info(brain.predict(training_data[1][0]));
		Log.info(brain.predict(training_data[2][0]));
		Log.info(brain.predict(training_data[3][0]));

	}
}
