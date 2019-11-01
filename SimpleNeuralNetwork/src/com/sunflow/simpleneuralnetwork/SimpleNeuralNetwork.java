package com.sunflow.simpleneuralnetwork;

import java.awt.Graphics2D;

import com.sunflow.game.Game2D;
import com.sunflow.util.Log;

public class SimpleNeuralNetwork extends Game2D {
	public static void main(String[] args) {
		new SimpleNeuralNetwork();
	}

//	private NeuralNetwork brain;

	@Override
	protected void setup() {
		createCanvas(400, 400);

//		brain = new NeuralNetwork(2, 2, 1);

		NeuralNetwork nn = new NeuralNetwork(2, 2, 1);
		float[] input = { 1, 0 };
		float[] output = nn.feedforward(input);
		Log.info(output);
	}

	@Override
	protected void render(Graphics2D g) {
		super.render(g);
	}
}
