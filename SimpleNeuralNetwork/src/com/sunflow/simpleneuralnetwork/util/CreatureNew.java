package com.sunflow.simpleneuralnetwork.util;

import java.util.Random;

import com.sunflow.simpleneuralnetwork.convolutional.CNN;
import com.sunflow.util.MathUtils;
import com.sunflow.util.SimpleMapper;

public abstract class CreatureNew<Type> implements Cloneable, MathUtils {
//	protected Mapper mutate = (x, i, j) -> {
	protected SimpleMapper mutate = x -> {
		if (random(1.0F) < 0.01F) {
			float offset = (float) (new Random().nextGaussian() * 0.25F);
			float newx = x + offset;
			return newx;
		} else {
			return x;
		}
	};

	protected CNN brain;
	public double score;
	public double fitness;

	public CreatureNew(int nodes_inputs, int nodes_outputs, int nodes_hidden) {
		// Is this a copy of another Bird or a new one?
		// The Neural Network is the bird's "brain"
//		this.brain = new NeuralNetwork(nodes_inputs, nodes_outputs, nodes_hidden);
		this.brain = new CNN(new CNN.Option()
				.input(nodes_inputs)
				.output(nodes_outputs));

		// Score is how many frames it's been alive
		this.score = -1;
		// Fitness is normalized version of score
		this.fitness = 0;
	}

	public CNN brain() { return brain; }

	public double score() {
		score = calcScore();
		return score;
	}

	public void invalid() { score = -1; }

	// Create a copy of this bird
	@Override
	public abstract Type clone();

	public void update() { update(-1); }

	public abstract void update(double delta);

	protected abstract Type mutate();

	protected abstract float calcScore();
}