package com.sunflow.simpleperceptron.training;

import com.sunflow.game.Game2DAsynchron;
import com.sunflow.util.MathUtils;
import com.sunflow.util.StaticUtils;

public class Point implements MathUtils {
	public static float f(float x) {
		// y = mx + b
		return 0.3F * x + 0.2F;
//		return x;
	}

	private Game2DAsynchron game;

	public float x;
	public float y;
	public int label;

	public Point(Game2DAsynchron game) {
		this(game, StaticUtils.instance.random(-1F, 1F), StaticUtils.instance.random(-1F, 1F));
	}

	public Point(Game2DAsynchron game, float x, float y) {
		this.game = game;
		this.x = x;
		this.y = y;
		if (y > f(x)) {
			label = 1;
		} else {
			label = -1;
		}
	}

	public float pX() {
		return map(this.x, -1, 1, 0, game.width());
	}

	public float pY() {
		return map(this.y, -1, 1, game.height(), 0);
	}

	public void show(Game2DAsynchron game) {
		if (label == 1) {
			game.fill(255);
		} else {
			game.fill(0);
		}
		game.stroke(0);
		game.strokeWeight(2);

		game.ellipse(pX(), pY(), 20, 20);
	}
}
