package com.sunflow.simpleperceptron;

import com.sunflow.game.Game2D;
import com.sunflow.simpleperceptron.training.Point;
import com.sunflow.util.Log;

public class SimplePerceptron extends Game2D {

	public static void main(String[] args) {
		new SimplePerceptron();
	}

	private Perceptron brain;
	private Point[] points;
	private int trainingIndex;

	@Override
	protected void setup() {
		createCanvas(500, 500);
		frameRate(30);
		smooth();

		points = new Point[100];
		brain = new Perceptron(3);

		for (int i = 0; i < points.length; i++) {
			points[i] = new Point(this);
		}

		float[] inputs = { -1, 0.5F, 1 };
		int guess = brain.guess(inputs);
		Log.info(guess);
	}

	@Override
	protected void update(double multiplier) {
		for (int i = 0; i < 5; i++) {
			Point p = points[++trainingIndex == points.length ? trainingIndex = 0 : trainingIndex];
			float[] inputs = { p.x, p.y, 1 };
			int target = p.label;

			brain.train(inputs, target);
		}
	}

	@Override
	protected void draw() {
		background(255);
		for (Point p : points) {
			p.show(this);
			float[] inputs = { p.x, p.y, 1 };
			int target = p.label;

			int guess = brain.guess(inputs);
			if (guess == target) {
				fill(0, 255, 0);
			} else {
				fill(255, 0, 0);
			}
			noStroke();

			ellipse(p.pX(), p.pY(), 12, 12);
		}

		strokeWeight(3);
		stroke(0, 0, 255);
		Point p1 = new Point(this, -1, Point.f(-1));
		Point p2 = new Point(this, 1, Point.f(1));
		line(p1.pX(), p1.pY(), p2.pX(), p2.pY());

		stroke(255, 0, 200);
		Point bp1 = new Point(this, -1, brain.guessY(-1));
		Point bp2 = new Point(this, 1, brain.guessY(1));
		line(bp1.pX(), bp1.pY(), bp2.pX(), bp2.pY());
	}
}
