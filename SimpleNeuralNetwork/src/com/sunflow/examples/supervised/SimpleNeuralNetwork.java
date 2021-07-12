package com.sunflow.examples.supervised;

import com.sunflow.game.GameBase;
import com.sunflow.logging.LogManager;
import com.sunflow.simpleneuralnetwork.simple.NeuralNetwork;

public class SimpleNeuralNetwork extends GameBase {
	public static void main(String[] args) { new SimpleNeuralNetwork(); }

	private NeuralNetwork brain;

	double[][][] training_data;

	@Override
	public void setup() {
//		createCanvas(200, 200);
		brain = new NeuralNetwork(2, 1, 2);
		training_data = new double[][][] {
				{ { 0, 0 }, { 1 } },
				{ { 0, 1 }, { 1 } },
				{ { 1, 0 }, { 0 } },
				{ { 1, 1 }, { 0 } }
		};

		for (int i = 0; i < 50000; i++) {
			double[][] data = training_data[random(0, 3)];
			brain.train(data[0], data[1]);
		}
		LogManager.info(brain.predict(training_data[0][0])[0]);
		LogManager.info(brain.predict(training_data[1][0])[0]);
		LogManager.info(brain.predict(training_data[2][0])[0]);
		LogManager.info(brain.predict(training_data[3][0])[0]);
	}
}
