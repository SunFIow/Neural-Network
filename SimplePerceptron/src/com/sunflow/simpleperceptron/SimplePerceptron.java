package com.sunflow.simpleperceptron;

import java.awt.Color;
import java.awt.Graphics2D;

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
		frameRate(30);
		createCanvas(500, 500);
		background(Color.white);
		this.antialias = true;

		points = new Point[100];
		brain = new Perceptron(3);

		for (int i = 0; i < points.length; i++) {
			points[i] = new Point();
		}

		float[] inputs = { -1, 0.5F, 1 };
		int guess = brain.guess(inputs);
		Log.info(guess);
	}

	@Override
	protected void tick(double multiplier) {
		for (int i = 0; i < 5; i++) {
			Point p = points[++trainingIndex == points.length ? trainingIndex = 0 : trainingIndex];
			float[] inputs = { p.x, p.y, 1 };
			int target = p.label;

			brain.train(inputs, target);
		}
	}

	@Override
	protected void render(Graphics2D g) {
		background(255);
		for (Point p : points) {
			p.show(this);
			float[] inputs = { p.x, p.y, 1 };
			int target = p.label;

			int guess = brain.guess(inputs);
			if (guess == target) {
				fill(Color.green);
			} else {
				fill(Color.red);
			}
			noStroke();

			ellipse(p.pX(), p.pY(), 12, 12);
		}

		strokeWeight(3);
		stroke(Color.blue);
		Point p1 = new Point(-1, Point.f(-1));
		Point p2 = new Point(1, Point.f(1));
		line(p1.pX(), p1.pY(), p2.pX(), p2.pY());

		stroke(Color.magenta);
		Point bp1 = new Point(-1, brain.guessY(-1));
		Point bp2 = new Point(1, brain.guessY(1));
		line(bp1.pX(), bp1.pY(), bp2.pX(), bp2.pY());
	}
}
