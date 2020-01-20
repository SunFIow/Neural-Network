package com.sunflow.simpleneuralnetwork;

import java.util.Random;

import com.sunflow.math3d.MatrixD.Mapper;
import com.sunflow.util.GameUtils;

public abstract class Creature<Type> implements Cloneable, GameUtils {
	protected Mapper mutate = (x, i, j) -> {
		if (random(1.0D) < 0.01D) {
			double offset = new Random().nextGaussian() * 0.25D;
			double newx = x + offset;
			return newx;
		} else {
			return x;
		}
	};

	public double score;
	protected NeuralNetwork brain;
	public double fitness;

	public Creature(int nodes_inputs, int nodes_outputs, int nodes_hidden) {
		// Is this a copy of another Bird or a new one?
		// The Neural Network is the bird's "brain"
		this.brain = new NeuralNetwork(nodes_inputs, nodes_outputs, nodes_hidden);

		// Score is how many frames it's been alive
		this.score = -1;
		// Fitness is normalized version of score
		this.fitness = 0;
	}

	public NeuralNetwork brain() {
		return brain;
	}

	public double score() {
		score = calcScore();
		return score;
	}

	public void invalid() {
		score = -1;
	}

	// Create a copy of this bird
	@Override
	public abstract Type clone();

	protected abstract Type mutate();

	public void update() {
		update(-1);
	}

	public abstract void update(double delta);

	protected abstract double calcScore();
}