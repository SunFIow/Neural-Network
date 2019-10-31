package com.sunflow.simpleperceptron.training;

import java.awt.Color;

import com.sunflow.game.GameBase;
import com.sunflow.util.Utils;

public class Point {
	public static float f(float x) {
		// y = mx + b
		return 0.3F * x + 0.2F;
//		return x;
	}

	public float x;
	public float y;
	public int label;

	public Point() {
		this(Utils.random(-1, 1), Utils.random(-1, 1));
	}

	public Point(float x, float y) {
		this.x = x;
		this.y = y;
		if (y > f(x)) {
			label = 1;
		} else {
			label = -1;
		}
	}

	public float pX() {
		return Utils.map(this.x, -1, 1, 0, GameBase.width);
	}

	public float pY() {
		return Utils.map(this.y, -1, 1, GameBase.height, 0);
	}

	public void show(GameBase game) {
		if (label == 1) {
			game.fill(Color.white);
		} else {
			game.fill(Color.black);
		}
		game.stroke(Color.black);
		game.strokeWeight(2);

		game.ellipse(pX(), pY(), 20, 20);
	}
}
