package com.sunflow.examples;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;

import com.sunflow.game.Game2D;
import com.sunflow.simpleneuralnetwork.NeuralNetwork;
import com.sunflow.util.Utils;

public class ColorPredictor extends Game2D implements Serializable {
	public static void main(String[] args) {
		new ColorPredictor();
	}

	private NeuralNetwork brain;

	private float r;
	private float g;
	private float b;

	private boolean hidePrediction;

	private ArrayList<float[]> training_data = new ArrayList<float[]>();

	@Override
	protected void setup() {
		createCanvas(800, 400);
		antialias = true;

		pickNew();

		brain = new NeuralNetwork(3, 4, 2);
		brain.setLearningRate(0.3F);
	}

	@Override
	protected void render(Graphics2D g_) {
		background(r, g, b);

		noStroke();
		fill(0);
		text("black", width / 4 - 40, height / 2, 30);

		fill(255);
		text("white", width / 4 + width / 2 - 25, height / 2, 30);

		float[] guess = brain.predict(new float[] { r, g, b });

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
				text(String.valueOf(guess[1]), width / 4 - 40, height / 2 + 60, 30);
				text(String.valueOf(guess[0]), width / 4 - 40, height / 2 + 80, 18);
			} else {
				fill(255);
				text(String.valueOf(guess[0]), width / 4 + width / 2 - 25, height / 2 + 60, 30);
				text(String.valueOf(guess[1]), width / 4 + width / 2 - 25, height / 2 + 80, 18);
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
			training_data.add(new float[] { r, g, b, 0F, 1F });
//			brain.train(new float[] { r, g, b }, new float[] { 0F, 1F });
		} else {
			training_data.add(new float[] { r, g, b, 1F, 0F });
//			brain.train(new float[] { r, g, b }, new float[] { 1F, 0F });
		}
		for (int i = 0; i < Math.min(10, training_data.size()); i++) {
			float[] d = training_data.get(i);
			float[] inputs = new float[] { d[0], d[1], d[2] };
			float[] targets = new float[] { d[3], d[4] };
			brain.train(inputs, targets);
		}
		pickNew();
	}

	private void pickNew() {
		r = Utils.random(1F);
		g = Utils.random(1F);
		b = Utils.random(1F);
	}
}
