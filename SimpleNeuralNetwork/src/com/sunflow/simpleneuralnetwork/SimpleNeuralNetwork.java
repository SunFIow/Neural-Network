package com.sunflow.simpleneuralnetwork;

import java.awt.Graphics2D;

import com.sunflow.game.Game2D;
import com.sunflow.math3d.MatrixF;
import com.sunflow.math3d.MatrixF.Mapper;
import com.sunflow.util.Log;

public class SimpleNeuralNetwork extends Game2D {
	public static void main(String[] args) {
		new SimpleNeuralNetwork();
	}

	private NeuralNetwork brain;

	@Override
	protected void setup() {
		createCanvas(400, 400);

		brain = new NeuralNetwork(3, 3, 1);

		MatrixF a = new MatrixF(2, 3);
		a.randomize(10);
		Log.info(a);

		Mapper doubleIt = new Mapper() {
			@Override
			public float func(float x, int i, int j) {
				return x * 2;
			}
		};
		a.map(doubleIt);
		Log.info(a);
	}

	@Override
	protected void render(Graphics2D g) {
		super.render(g);
	}
}
