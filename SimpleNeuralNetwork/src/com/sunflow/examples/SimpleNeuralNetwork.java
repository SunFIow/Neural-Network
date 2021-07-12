package com.sunflow.examples;

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
		brain = new NeuralNetwork(2, 1, 10);
		training_data = new double[][][] {
				{ { 72, 99 }, { 27 } }, // 0
				{ { 27, 45 }, { 18 } }, // 1
				{ { 18, 39 }, { 21 } }, // 2
				{ { 21, 36 }, { 12 } }, // 3
				{ { 12, 28 }, { 13 } }, // 4
				{ { 13, 21 }, { 7 } }, // 5

				{ { 7, 46 }, { 17 } }, // 6
				{ { 17, 52 }, { 15 } }, // 7
				{ { 15, 76 }, { 19 } }, // 8
				{ { 19, 69 }, { 25 } }, // 9
				{ { 35, 42 }, { 14 } }, // 10
				{ { 16, 73 }, { 17 } } // 11
		};
		for (int i = 0; i < training_data.length; i++) {
			training_data[i][0][0] /= 100;
			training_data[i][0][1] /= 100;
			training_data[i][1][0] /= 100;
		}
		int[] rans = new int[12];
		for (int i = 0; i < 100_000_000; i++) {
			if (i % 1_000_000 == 0) LogManager.debug(i);
			int ran = random(0, 12);
			rans[ran]++;
			double[][] data = training_data[ran];
			brain.train(data[0], data[1]);
		}
		System.out.println();
		for (int i = 0; i < rans.length; i++)
			System.out.print(rans[i] + ", ");
		System.out.println();

		LogManager.info(brain.predict(training_data[0][0])[0] * 100);
		LogManager.info(brain.predict(training_data[1][0])[0] * 100);
		LogManager.info(brain.predict(training_data[2][0])[0] * 100);
		LogManager.info(brain.predict(training_data[3][0])[0] * 100);
		LogManager.info(brain.predict(training_data[4][0])[0] * 100);
		LogManager.info(brain.predict(training_data[5][0])[0] * 100);
		LogManager.info(brain.predict(training_data[6][0])[0] * 100);
		LogManager.info(brain.predict(training_data[7][0])[0] * 100);
		LogManager.info(brain.predict(training_data[8][0])[0] * 100);
		LogManager.info(brain.predict(training_data[9][0])[0] * 100);
		LogManager.info(brain.predict(training_data[10][0])[0] * 100);
		LogManager.info(brain.predict(training_data[11][0])[0] * 100);
	}
}
