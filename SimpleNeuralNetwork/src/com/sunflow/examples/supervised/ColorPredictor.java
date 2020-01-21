package com.sunflow.examples.supervised;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;

import com.sunflow.game.Game2DAsynchron;
import com.sunflow.simpleneuralnetwork.NeuralNetwork;
import com.sunflow.util.Constants;

public class ColorPredictor extends Game2DAsynchron implements Serializable {
	public static void main(String[] args) {
		new ColorPredictor();
	}

	private NeuralNetwork brain;

	private float r;
	private float g;
	private float b;

	private boolean hidePrediction;

	private ArrayList<double[]> training_data = new ArrayList<>();

	@Override
	protected void setup() {
		createCanvas(800, 400);
		smooth();

		pickNew();

		brain = new NeuralNetwork(3, 2, 4);
		brain.setLearningRate(0.3F);
	}

	@Override
	protected void draw() {
		background(r * 255, g * 255, b * 255);
//		background(255, 0, 0);

		textAlign(Constants.CENTER, Constants.CENTER);

		noStroke();
		fill(0);
		textSize(50);
		text("black", width / 4f, height / 2f);

		fill(255);
		text("white", width / 4f + width / 2f, height / 2f);

		stroke(0);

		double[] guess = brain.predict(new double[] { r, g, b });

		if (!hidePrediction) {
			noFill();
			stroke(255, 0, 0);
			strokeWeight(10);
			if (guess[0] < guess[1]) {
				rect(0, 0, width / 2 - 10, height);
			} else {
				rect(width / 2 + 10, 0, width / 2 - 10, height);
			}
			noStroke();
			if (guess[0] < guess[1]) {
				fill(0);
				textSize(30);
				text(String.format("%f", guess[1]), width / 4f, height / 2f + 60);
				textSize(18);
				text(String.format("%f", guess[0]), width / 4f, height / 2f + 90);
			} else {
				fill(255);
				textSize(30);
				text(String.format("%f", guess[0]), width / 4f + width / 2f, height / 2f + 60);
				textSize(18);
				text(String.format("%f", guess[1]), width / 4f + width / 2f, height / 2f + 90);
			}
		}
		fill(0);
		noStroke();
		rect(width / 2 - 10, 0, 20, height);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			hidePrediction = !hidePrediction;
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getX() < width / 2) {
			training_data.add(new double[] { r, g, b, 0, 1 });
//			brain.train(new float[] { r, g, b }, new float[] { 0F, 1F });
		} else {
			training_data.add(new double[] { r, g, b, 1, 0 });
//			brain.train(new float[] { r, g, b }, new float[] { 1F, 0F });
		}
		for (int i = 0; i < Math.min(10, training_data.size()); i++) {
			double[] d = training_data.get(i);
			double[] inputs = new double[] { d[0], d[1], d[2] };
			double[] targets = new double[] { d[3], d[4] };
			brain.train(inputs, targets);
		}
		pickNew();
	}

	private void pickNew() {
		r = random(1F);
		g = random(1F);
		b = random(1F);
	}
}
