package com.sunflow.simpleneuralnetwork;

import java.util.Random;

import com.sunflow.math3d.MatrixD.Mapper;
import com.sunflow.util.Utils;

public abstract class Creature<Type> implements Cloneable {
	protected Mapper mutate = new Mapper() {
		@Override
		public double func(double x, int i, int j) {
			if (Utils.random(1.0D) < 0.01D) {
				double offset = new Random().nextGaussian() * 0.2D;
				double newx = x + offset;
				return newx;
			} else {
				return x;
			}
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
		this.score = 0;
		// Fitness is normalized version of score
		this.fitness = 0;
	}

	// Create a copy of this bird
	@Override
	public abstract Type clone();

	public abstract void update();

	public NeuralNetwork brain() {
		return brain;
	}
}