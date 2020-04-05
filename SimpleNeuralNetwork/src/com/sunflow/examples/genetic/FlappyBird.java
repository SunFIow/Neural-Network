package com.sunflow.examples.genetic;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import com.sunflow.game.Game2D;
import com.sunflow.logging.Log;
import com.sunflow.simpleneuralnetwork.simple.NeuralNetwork;
import com.sunflow.simpleneuralnetwork.util.Creature;
import com.sunflow.simpleneuralnetwork.util.Population;

public class FlappyBird extends Game2D {
	public static void main(String[] args) {
		new FlappyBird();
	}

	private String bestBirdFile = "rec/brains/bestBirdBrain";

	// Pipes
	private ArrayList<Pipe> pipes;

	// A frame counter to determine when to add a pipe
	private int counter;

	// Interface elements
//	int speedSlider;
//	int speedSpan;
//	int highScoreSpan;
//	int allTimeHighScoreSpan;

	// All time high score
	private double highScore;

	// Training or just showing the current best
	private boolean runBest;

	private Population<Bird> population;

	private int cycles;

	private int score;
	private int bestScore;

	@Override
	protected void setup() {
		createCanvas(600, 400);
		smooth();
		frameRate(60);
		showInfo(true);

		cycles = 1;
		highScore = 0;
		score = 0;
		bestScore = 0;

		pipes = new ArrayList<>();

		population = new Population<Bird>(500, Bird::new, this::resetGame);

		// Access the interface elements
//		speedSlider = select('#speedSlider');
//		speedSpan = select('#speed');
//		highScoreSpan = select('#hs');
//		allTimeHighScoreSpan = select('#ahs');
//	 	runBestButton = select('#best');
//		runBestButton.mousePressed(toggleState); 

	}

	// Toggle the state of the simulation
	@SuppressWarnings("unused")
	private void toggleState() {
		runBest = !runBest;
		// Go train some more
		resetGame();
		if (!runBest) {
			population.nextGeneration();
//		    runBestButton.html('run best');
			// Show the best bird
		} else {
//		    runBestButton.html('continue training');
		}
	}

	// Start the game over
	private void resetGame() {
		counter = 0;
		score = 0;
		// Resetting best bird score to 0
		if (population.bestCreature() != null) {
			population.bestCreature().score = 0;
			population.bestCreature().timeAlive = 0;
		}
		cycles = Math.min(cycles, 500);
		pipes = new ArrayList<>();
	}

	@Override
	protected void update() {
		// Should we speed up cycles per frame
		int cycles = this.cycles; // speedSlider.value();
//		speedSpan.html(cycles);

		// How many times to advance the game
		for (int n = 0; n < cycles; n++) {
			for (int i = pipes.size() - 1; i >= 0; i--) {
				pipes.get(i).update();
				if (pipes.get(i).offscreen()) {
					score++;
					if (score > bestScore) {
						bestScore = score;
					}
					pipes.remove(i);
				}
			}
			// Are we just running the best bird
			if (runBest) {
				population.bestCreature().think(pipes);
				population.bestCreature().update();
				for (int i = 0; i < pipes.size(); i++) {
					// Start over, bird hit pipe
					if (pipes.get(i).hits(population.bestCreature())) {
						resetGame();
						break;
					}
				}

				if (population.bestCreature().bottomTop()) {
					resetGame();
				}
				// Or are we running all the active birds
			} else {
				for (int i = population.getActiveSize() - 1; i >= 0; i--) {
					Bird bird = population.get(i);
					// Bird uses its brain!
					bird.think(pipes);
					bird.update();

					// Check all the pipes
					for (int j = 0; j < pipes.size(); j++) {
						// It's hit a pipe
						if (pipes.get(j).hits(bird)) {
							// Remove this bird
							population.remove(i);
							break;
						}
					}

					if (bird.bottomTop()) {
						population.remove(i);
					}
					if (population.getActiveSize() == 0)
						return;
				}
			}

			// Add a new pipe every so often
			if (counter % 75 == 0) {
				pipes.add(new Pipe());
			}
			counter++;

			// What is highest score of the current population
			double tempHighScore = 0;
			// If we're training
			if (!runBest) {
				// Which is the best bird?
				Bird tempBestBird = null;
				for (int i = 0; i < population.getActiveSize(); i++) {
					Bird bird = population.get(i);
					double s = bird.score();
					if (s > tempHighScore) {
						tempHighScore = s;
						tempBestBird = bird;
					}
				}

				// Is it the all time high scorer?
				if (tempHighScore > highScore) {
//					Log.info("new BestBird");
					highScore = tempHighScore;
					population.setBestCreature(tempBestBird);
				}
			} else {
				// Just one bird, the best one so far
				tempHighScore = population.bestCreature().score();
				if (tempHighScore > highScore) {
					highScore = tempHighScore;
				}
			}
		}

		// Update DOM Elements
//		highScoreSpan.html(tempHighScore);
//		allTimeHighScoreSpan.html(highScore);

	}

	@Override
	protected void draw() {
//		logic();
		background(10);

		strokeWeight(5);
		// Draw everything!
		for (int i = 0; i < pipes.size(); i++) {
			pipes.get(i).show();
		}
		strokeWeight(1);

		if (runBest) {
			population.bestCreature().show();
		} else {
			for (int i = 0; i < population.getActiveSize(); i++) {
				population.get(i).show();
			}
			// If we're out of birds go to the next generation
			if (population.getActiveSize() == 0) {
				resetGame();
				population.nextGeneration();
			}
		}

		fill(255, 0, 0);
		stroke(50, 0, 0);
		strokeWeight(2);
		textSize(20);
		text("Generation: " + population.generation(), width - 180, 25);
		text("Cycles: " + cycles, width - 180, 45);
		text("Alive: " + population.getActiveSize(), width - 180, 65);
		text("Score: " + score, width - 180, 85);
		text("HighScore: " + bestScore, width - 180, 105);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!e.isControlDown()) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_PLUS:
					cycles++;
					break;
				case KeyEvent.VK_MINUS:
					cycles--;
					if (cycles < 1) cycles = 1;
					break;
				case KeyEvent.VK_A:
					population.addCreature();
					break;
			}
		} else {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_PLUS:
					cycles += 10;
					break;
				case KeyEvent.VK_MINUS:
					cycles -= 10;
					if (cycles < 1) cycles = 1;
					break;
				case KeyEvent.VK_S:
					serialize(bestBirdFile, population.bestCreature().brain());
					Log.info("serialized");
					break;
				case KeyEvent.VK_L:
					population.populateOf(new Bird((NeuralNetwork) deserialize(bestBirdFile)));
					Log.info("deserialized");
					break;
			}
		}
	}

	private class Bird extends Creature<Bird> {
//		private Mapper mutate = new Mapper() {
//			@Override
//			public double func(double x, int i, int j) {
//				if (random(1.0D) < 0.01D) {
//					double offset = new Random().nextGaussian() * 0.2D;
//					double newx = x + offset;
//					return newx;
//				} else {
//					return x;
//				}
//			}
//		};

		public float x;
		public float y;
		public float r;
		private float gravity;
		private float lift;
		private float velocity;
		private int timeAlive;

		public Bird() {
			super(5, 2, 8);
			// position and size of bird
			this.x = 64F;
			this.y = height / 2F;
			this.r = 12F;

			// Gravity, lift and velocity
			this.gravity = 0.8F;
			this.lift = -12.0F;
			this.velocity = 0.0F;
		}

		public Bird(NeuralNetwork brain) {
			this();
			this.brain = brain.clone();
		}

		// Create a copy of this bird
		@Override
		public Bird mutate() {
			Bird copy = clone();
			copy.brain().mutate(mutate);
			return copy;
		}

		@Override
		public Bird clone() {
			return new Bird(brain);
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
				inputs[0] = map(closest.x, this.x, width, 0, 1d);
				// top of closest pipe opening
				inputs[1] = map(closest.top, 0, height, 0, 1d);
				// bottom of closest pipe opening
				inputs[2] = map(closest.bottom, 0, height, 0, 1d);
				// bird's y position
				inputs[3] = map(this.y, 0, height, 0, 1d);
				// bird's y velocity
				inputs[4] = map(this.velocity, -30, 30, 0, 1d);

				// Get the outputs from the network
				double[] action = this.brain().predict(inputs);
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

		@Override
		public void update(double dt) {
			this.velocity += this.gravity;
			this.velocity = clamp(-30F, this.velocity, 30F);
			this.y += this.velocity;

			// Every frame it is alive increases the score
			this.timeAlive++;
		}

		@Override
		public float calcScore() { return timeAlive; }
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
			this.centery = random(spacing, height - spacing);

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
