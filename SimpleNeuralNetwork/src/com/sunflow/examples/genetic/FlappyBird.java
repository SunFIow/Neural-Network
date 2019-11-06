package com.sunflow.examples.genetic;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import com.sunflow.game.Game2D;
import com.sunflow.math3d.MatrixD.Mapper;
import com.sunflow.simpleneuralnetwork.NeuralNetwork;
import com.sunflow.util.Utils;

public class FlappyBird extends Game2D {
	public static void main(String[] args) {
		new FlappyBird();
	}

	int totalPopulation;
	// All active birds (not yet collided with pipe)
	ArrayList<Bird> activeBirds;
	// All birds for any given population
	ArrayList<Bird> allBirds;
	// Pipes
	ArrayList<Pipe> pipes;
	// A frame counter to determine when to add a pipe
	int counter;

	// Interface elements
//	int speedSlider;
//	int speedSpan;
//	int highScoreSpan;
//	int allTimeHighScoreSpan;

	// All time high score
	double highScore;

	// Training or just showing the current best
	boolean runBest;
	int runBestButton;

	@Override
	protected void setup() {
		createCanvas(600, 400);
		antialias = true;
		frameRate(60);

		activeBirds = new ArrayList<>();
		allBirds = new ArrayList<>();
		pipes = new ArrayList<>();
		totalPopulation = 500;

		// Access the interface elements
//		speedSlider = select('#speedSlider');
//		speedSpan = select('#speed');
//		highScoreSpan = select('#hs');
//		allTimeHighScoreSpan = select('#ahs');
//	 	runBestButton = select('#best');
//		runBestButton.mousePressed(toggleState); 

		// Create a population
		for (int i = 0; i < totalPopulation; i++) {
			Bird bird = new Bird();
			activeBirds.add(bird);
			allBirds.add(bird);
		}
	}

	// Toggle the state of the simulation
	private void toggleState() {
		runBest = !runBest;
		// Show the best bird
		if (runBest) {
			resetGame();
//	    runBestButton.html('continue training');
			// Go train some more
		} else {
			nextGeneration();
//	    runBestButton.html('run best');
		}
	}

	// Start the game over
	private void resetGame() {
		counter = 0;
		// Resetting best bird score to 0
		if (bestBird != null) {
			bestBird.score = 0;
		}
		pipes = new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	private void nextGeneration() {
		resetGame();
		// Normalize the fitness values 0-1
		normalizeFitness(allBirds);
		// Generate a new set of birds
		activeBirds = generate(allBirds);
		// Copy those birds to another array
		allBirds = (ArrayList<Bird>) activeBirds.clone();
	}

	private ArrayList<Bird> generate(ArrayList<Bird> oldBirds) {
		ArrayList<Bird> newBirds = new ArrayList<>();
		for (int i = 0; i < oldBirds.size(); i++) {
			// Select a bird based on fitness
			Bird bird = poolSelection(oldBirds);
			newBirds.add(bird);
		}
		return newBirds;
	}

	private void normalizeFitness(ArrayList<Bird> birds) {
		// Make score exponentially better?
		for (int i = 0; i < birds.size(); i++) {
			birds.get(i).score = Math.pow(birds.get(i).score, 2);
		}

		// Add up all the scores
		double sum = 0;
		for (int i = 0; i < birds.size(); i++) {
			sum += birds.get(i).score;
		}
		// Divide by the sum
		for (int i = 0; i < birds.size(); i++) {
			birds.get(i).fitness = birds.get(i).score / sum;
		}
	}

	private Bird poolSelection(ArrayList<Bird> birds) {
		// Start at 0
		int index = 0;

		// Pick a random number between 0 and 1
		double r = Utils.random(1.0D);

		// Keep subtracting probabilities until you get less than zero
		// Higher probabilities will be more likely to be fixed since they will
		// subtract a larger number towards zero
		while (r > 0.0D) {
			r -= birds.get(index).fitness;
			// And move on to the next
			index += 1;
		}

		// Go back one
		index -= 1;

		// Make sure it's a copy!
		// (this includes mutation)
		return birds.get(index).copy();
	}

	@Override
	protected void tick(double multiplier) {}

	Bird bestBird;

	@Override
	protected void render(Graphics2D g) {
		background(0);

		// Should we speed up cycles per frame
		int cycles = 1; // speedSlider.value();
//		speedSpan.html(cycles);

		// How many times to advance the game
		for (int n = 0; n < cycles; n++) {
			for (int i = pipes.size() - 1; i >= 0; i--) {
				pipes.get(i).update();
				if (pipes.get(i).offscreen()) {
					pipes.remove(i);
				}
			}
			// Are we just running the best bird
			if (runBest) {
				bestBird.think(pipes);
				bestBird.update();
				for (int i = 0; i < pipes.size(); i++) {
					// Start over, bird hit pipe
					if (pipes.get(i).hits(bestBird)) {
						resetGame();
						break;
					}
				}

				if (bestBird.bottomTop()) {
					resetGame();
				}
				// Or are we running all the active birds
			} else {
				for (int i = activeBirds.size() - 1; i >= 0; i--) {
					Bird bird = activeBirds.get(i);
					// Bird uses its brain!
					bird.think(pipes);
					bird.update();

					// Check all the pipes
					for (int j = 0; j < pipes.size(); j++) {
						// It's hit a pipe
						if (pipes.get(j).hits(bird)) {
							// Remove this bird
							activeBirds.remove(i);
							break;
						}
					}

					if (bird.bottomTop()) {
						activeBirds.remove(i);
					}

				}
			}

			// Add a new pipe every so often
			if (counter % 75 == 0) {
				pipes.add(new Pipe());
			}
			counter++;
		}

		// What is highest score of the current population
		double tempHighScore = 0;
		// If we're training
		if (!runBest) {
			// Which is the best bird?
			Bird tempBestBird = null;
			for (int i = 0; i < activeBirds.size(); i++) {
				double s = activeBirds.get(i).score;
				if (s > tempHighScore) {
					tempHighScore = s;
					tempBestBird = activeBirds.get(i);
				}
			}

			// Is it the all time high scorer?
			if (tempHighScore > highScore) {
				highScore = tempHighScore;
				bestBird = tempBestBird;
			}
		} else {
			// Just one bird, the best one so far
			tempHighScore = bestBird.score;
			if (tempHighScore > highScore) {
				highScore = tempHighScore;
			}
		}

		// Update DOM Elements
//		highScoreSpan.html(tempHighScore);
//		allTimeHighScoreSpan.html(highScore);

		// Draw everything!
		for (int i = 0; i < pipes.size(); i++) {
			pipes.get(i).show();
		}

		if (runBest) {
			bestBird.show();
		} else {
			for (int i = 0; i < activeBirds.size(); i++) {
				activeBirds.get(i).show();
			}
			// If we're out of birds go to the next generation
			if (activeBirds.size() == 0) {
				nextGeneration();
			}
		}
	}

	private Random rndm = new Random();

	private class Bird implements Cloneable {
		private Mapper mutate = new Mapper() {
			@Override
			public double func(double x, int i, int j) {
				if (rndm.nextDouble() < 0.01D) {
					double offset = new Random().nextGaussian() * 0.2D;
					double newx = x + offset;
					return newx;
				} else {
					return x;
				}
			}
		};
		public double score;
		public float x;
		public float y;
		public float r;
		private float gravity;
		private float lift;
		private float velocity;
		private NeuralNetwork brain;
		private double fitness;

		public Bird() {
			// position and size of bird
			this.x = 64F;
			this.y = height / 2F;
			this.r = 12F;

			// Gravity, lift and velocity
			this.gravity = 0.8F;
			this.lift = -12.0F;
			this.velocity = 0.0F;

			// Is this a copy of another Bird or a new one?
			// The Neural Network is the bird's "brain"
			this.brain = new NeuralNetwork(5, 2, 8);

			// Score is how many frames it's been alive
			this.score = 0;
			// Fitness is normalized version of score
			this.fitness = 0;
		}

		public Bird copy() {
			Bird copy = new Bird();
			copy.brain = this.brain.clone();
			copy.brain.mutate(mutate);
			return copy;
		}

		// Create a copy of this bird
		@Override
		public Bird clone() {
			try {
				return (Bird) super.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			return bestBird;
		}

		// Display the bird
		public void show() {
			fill(255, 100);
			stroke(255);
			ellipse(this.x, this.y, this.r * 2, this.r * 2);
		}

		// This is the key function now that decides
		// if it should jump or not jump!
		public void think(ArrayList<Pipe> pipes) {
			// First find the closest pipe
			Pipe closest = null;
			float record = Float.POSITIVE_INFINITY;

			for (int i = 0; i < pipes.size(); i++) {
				float diff = pipes.get(i).x - this.x;
				if (diff > 0F && diff < record) {
					record = diff;
					closest = pipes.get(i);
				}
			}

			if (closest != null) {
				// Now create the inputs to the neural network
				double[] inputs = new double[5];
				// x position of closest pipe
				inputs[0] = Utils.map(closest.x, this.x, width, 0, 1);
				// top of closest pipe opening
				inputs[1] = Utils.map(closest.top, 0, height, 0, 1);
				// bottom of closest pipe opening
				inputs[2] = Utils.map(closest.bottom, 0, height, 0, 1);
				// bird's y position
				inputs[3] = Utils.map(this.y, 0, height, 0, 1);
				// bird's y velocity
				inputs[4] = Utils.map(this.velocity, -5, 5, 0, 1);

				// Get the outputs from the network
				double[] action = this.brain.predict(inputs);
				// Decide to jump or not!
				if (action[1] > action[0]) {
					this.up();
				}
			}
		}

		private void up() {
			this.velocity += this.lift;
		}

		public boolean bottomTop() {
			// Bird dies when hits bottom?
			return (this.y > height || this.y < 0.0F);
		}

		public void update() {
			this.velocity += this.gravity;
			this.velocity = Utils.clamp(-30F, this.velocity, 30F);
			// this.velocity *= 0.9;
			this.y += this.velocity;
//			this.velocity = 0;

			// Every frame it is alive increases the score
			this.score++;
		}
	}

	private class Pipe {
		private float spacing;
		private float centery;
		private float top;
		private float bottom;
		private float x;
		private float w;
		private float speed;

		public Pipe() {
			// How big is the empty space
			this.spacing = 125F;
			// Where is th center of the empty space
			this.centery = Utils.random(spacing, height - spacing);

			// Top and bottom of pipe
			this.top = centery - spacing / 2;
			this.bottom = height - (centery + spacing / 2);
			// Starts at the edge
			this.x = width;
			// Width of pipe
			this.w = 80;
			// How fast
			this.speed = 6;
		}

		// Did this pipe hit a bird?
		public boolean hits(Bird bird) {
			if ((bird.y - bird.r) < this.top || (bird.y + bird.r) > (height - this.bottom)) {
				if (bird.x > this.x && bird.x < this.x + this.w) {
					return true;
				}
			}
			return false;
		}

		// Draw the pipe
		public void show() {
			stroke(255);
			fill(200);
			rect(this.x, 0, this.w, this.top);
			rect(this.x, height - this.bottom, this.w, this.bottom);
		}

		// Update the pipe
		public void update() {
			this.x -= this.speed;
		}

		// Has it moved offscreen?
		public boolean offscreen() {
			if (this.x < -this.w) {
				return true;
			} else {
				return false;
			}
		}
	}
}
