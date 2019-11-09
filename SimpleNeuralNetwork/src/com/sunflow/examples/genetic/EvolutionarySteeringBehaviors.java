package com.sunflow.examples.genetic;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

import com.sunflow.game.Game2D;
import com.sunflow.math.Vertex2D;
import com.sunflow.math3d.MatrixD.Mapper;
import com.sunflow.simpleneuralnetwork.Creature;
import com.sunflow.simpleneuralnetwork.NeuralNetwork;
import com.sunflow.simpleneuralnetwork.Population;
import com.sunflow.util.Log;
import com.sunflow.util.Utils;

public class EvolutionarySteeringBehaviors extends Game2D {
	public static void main(String[] args) {
		new EvolutionarySteeringBehaviors();
	}

	private String bestRocketFile = "rec/bestVehicleBrain";

	public float goalR;
	private Point2D.Float goal;

	// TheWall
	private Wall wall;

	// Walls
	private ArrayList<Wall> walls;

	// A frame counter to determine when to finish generation
	private int lifespan;

	// All time high score
	private double highScore;

	private Population<Rocket> population;

	private int cycles;

	@Override
	protected void setup() {
		createCanvas(1280, 800);
		smooth();
		frameRate(60);

		lifespan = 200;
		highScore = 0;
		cycles = 1;

		goalR = 50;
		goal = new Point2D.Float(width / 2, goalR);

		wall = new Wall(width / 4, height / 2.5F, width / 2, 40);
		walls = new ArrayList<>();
		walls.add(wall);

		population = new Population<Rocket>(500) {
			@Override
			protected Rocket getCreature() {
				return new Rocket();
			}
		};

	}

	// Start the game over
	private void resetGame() {
		lifespan = 200;
		// Resetting best Rocket score to 0
		if (population.bestCreature() != null) {
			population.bestCreature().invalid();
		}
//		cycles = Math.min(cycles, 100);
		walls = new ArrayList<>();
		walls.add(wall);
	}

	private void logic() {
//		int cycles = this.cycles;
		// Should we speed up cycles per frame
		// How many times to advance the game
		for (int n = 0; n < cycles; n++) {
			// Are we just running the best Rocket
			for (int i = population.getActiveSize() - 1; i >= 0; i--) {
				Rocket rocket = population.getCreature(i);
				// Rocket uses its brain!
				rocket.think(walls);
				rocket.update();

				// Check all the pipes
				for (int j = 0; j < walls.size(); j++) {
					// It's hit a pipe
					if (walls.get(j).hits(rocket)) {
						// Remove this Rocket
						rocket.hitWall();
						population.removeCreatures(rocket);
						break;
					}
				}
				if (rocket.offScreen()) {
					rocket.hitWall();
					population.removeCreatures(rocket);
				}
				if (rocket.hits(goal, goalR)) {
					rocket.finished();
					population.removeCreatures(rocket);
				}
			}

			// What is highest score of the current population
			double tempHighScore = 0;
			// Which is the best Rocket?
			Rocket tempBestRocket = null;
			for (int i = 0; i < population.getActiveSize(); i++) {
				Rocket rocket = population.getCreature(i);
				double s = rocket.score();
				if (s > tempHighScore) {
					tempHighScore = s;
					tempBestRocket = rocket;
				}
			}

			// Is it the all time high scorer?
			if (tempHighScore > highScore) {
				highScore = tempHighScore;
				population.setBestCreature(tempBestRocket);
			}

			lifespan--;
			if (lifespan == 0 || population.getActiveSize() == 0)
				return;
		}
	}

	@Override
	protected void render(Graphics2D g) {
		logic();

		background(0);

		// Draw everything!
		for (int i = 0; i < walls.size(); i++) {
			walls.get(i).show();
		}

		fill(200);
		stroke(100);
		strokeWeight(5);
		ellipse(goal.x, goal.y, goalR, goalR);

		for (int i = 0; i < population.getActiveSize(); i++) {
			population.getCreature(i).show();
		}

		if (lifespan == 0) {
			for (int i = population.getActiveSize() - 1; i >= 0; i--) {
//				Rocket rocket = population.getCreature(i);
//				rocket.calcScore();
				population.removeCreatures(i);
			}
		}

		// If we're out of Rockets go to the next generation
		if (population.getActiveSize() == 0) {
			resetGame();
			population.nextGeneration();
		}

		fill(255, 0, 0);
		stroke(50, 0, 0);
		strokeWeight(2);
		textSize(20);
		textO("Generation: " + population.generation(), width - 200, 25);
		textO("Cycles: " + cycles, width - 200, 45);
		textO("Alive: " + population.getActiveSize(), width - 200, 65);
//		textO("LifeSpawn: " + lifespan, width - 200, 85);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_PLUS) {
			cycles++;
		} else if (e.getKeyCode() == KeyEvent.VK_MINUS) {
			cycles--;
			if (cycles < 1)
				cycles = 1;
		} else if (e.isControlDown()) {
			if (e.getKeyCode() == KeyEvent.VK_S) {
				serialize("bestRocketBrain", population.bestCreature().brain());
				Log.info("serialized");
			} else if (e.getKeyCode() == KeyEvent.VK_L) {
//				population.setBestCreature(new Rocket((NeuralNetwork) deserialize("bestRocketBrain")));
				resetGame();
				population.populateOf(new Rocket((NeuralNetwork) deserialize("bestRocketBrain")));
				Log.info("deserialized");
			}
		}
	}

	private static int inputs_length = 10;
	private static int outputs_length = 2;
	private static int hidden_length = 20;

	private class Rocket extends Creature<Rocket> {
		protected Mapper mutate = new Mapper() {
			@Override
			public double func(double x, int i, int j) {
				if (Utils.random(1.0D) < 0.01D) {
					double offset = new Random().nextGaussian() * 0.25D;
					double newx = x + offset;
					return newx;
				} else {
					return x;
				}
			}
		};

		private float x;
		private float y;
		private float l;

		private Vertex2D velocity;
		private Vertex2D thrust;

		private int timeAlive;
		private boolean hitWall;
		private boolean finished;

//		private Vertex2D vel;

		public Rocket() {
			super(inputs_length, outputs_length, hidden_length);
			// position and size of Rocket
			x = width / 2F;
			y = height - 20F;
			l = 12F;

//			velocity = new Vertex2D();
			double angle = -1 * Math.random() * Math.PI;
			velocity = Vertex2D.fromAngle(angle, null);
		}

		public Rocket(NeuralNetwork brain) {
			this();
			this.brain = brain.clone();
		}

		public Vertex2D dir() {
			return velocity.clone().normalize();
		}

		public float x2() {
			return (float) (dir().x * l + x);
		}

		public float y2() {
			return (float) (dir().y * l + y);
		}

		// Create a copy of this Rocket
		@Override
		public Rocket mutate() {
			Rocket copy = clone();
			copy.brain().mutate(mutate);
			return copy;
		}

		@Override
		public Rocket clone() {
			return new Rocket(brain);
		}

		public void hitWall() {
			hitWall = true;
		}

		public void finished() {
			finished = true;
		}

		@Override
		public void calcScore() {
			double dist = Utils.dist(x, y, goal.x, goal.y);
			score = 1.0D / Math.pow(dist, 4);
			score *= timeAlive / 150D;
			if (finished) score *= 10;
			if (hitWall) score /= 4;
//			if (hits(goal, goalR)) score *= 2;
		}

		@Override
		public void update() {
			velocity.add(thrust);
			velocity.limit(10D);

			x += velocity.x;
			y += velocity.y;

			timeAlive++;
		}

		public boolean hits(Point2D.Float goal, float goalR) {
//			return Utils.hitBoxCircle(x, y, l, l, goal.x, goal.y, goalR);
//			return Utils.hitLineCircle(x, y, x2(), y2(), goal.x, goal.y, goalR);
			return Utils.dist(x, y, goal.x, goal.y) < goalR - l;
		}

		// This is the key function now that decides
		// if it should jump or not jump!
		public void think(ArrayList<Wall> walls) {
			// Now create the inputs to the neural network
			double[] inputs = new double[inputs_length];
			// x position the rocket
			inputs[0] = Utils.map(x, 0, width, 0, 1);
			// y position the rocket
			inputs[1] = Utils.map(y, 0, height, 0, 1);
			// velocity x the rocket
			inputs[2] = Utils.map(velocity.x, -10, 10, 0, 1);
			// velocity y the rocket
			inputs[3] = Utils.map(velocity.y, -10, 10, 0, 1);
			// x position of the goal
			inputs[4] = Utils.map(goal.x, 0, width, 0, 1);
			// y position of the goal
			inputs[5] = Utils.map(goal.y, 0, height, 0, 1);
			for (int i = 0; i < walls.size(); i++) {
				Wall w = walls.get(i);
				// x position of the wall
				inputs[6 + i] = Utils.map(w.x, 0, width, 0, 1);
				// y position of the wall
				inputs[7 + i] = Utils.map(w.y, 0, height, 0, 1);
				// width of the wall
				inputs[8 + i] = Utils.map(w.w, 0, width, 0, 1);
				// height of the wall
				inputs[9 + i] = Utils.map(w.h, 0, height, 0, 1);
			}

			// Get the outputs from the network
			double[] action = this.brain().predict(inputs);
			// Decide the thrust!
//			double angle = -1 * Math.random() * Math.PI;
//			thrust = Vertex2D.of(action[0], action[1]);
			thrust = new Vertex2D(action[0] * 2 - 1, action[1] * 2 - 1).mult(2);
		}

		public boolean offScreen() {
			return (x < 0 || y < 0 || x > width || y > height);
		}

		// Display the Rocket
		public void show() {
			fill(255, 100);
			stroke(255);
			strokeWeight(5);
			line(x, y, x2(), y2());
//			ellipse(this.x, this.y, this.r * 2, this.r * 2);
		}
	}

	private class Wall {
		private float x;
		private float y;
		private float w;
		private float h;

		@SuppressWarnings("unused")
		public Wall() {
			this(
					Utils.random(0, width),
					Utils.random(0, height),
					Utils.random(width / 8, width - width / 8),
					Utils.random(height / 32, height / 16));

		}

		public Wall(float x, float y, float w, float h) {
			this.x = Utils.clamp(0, x, width - w);
			this.y = Utils.clamp(0, y, height - h);
			this.w = w;
			this.h = h;
		}

		// Did this wall hit a Rocket?
		public boolean hits(Rocket rocket) {
			return Utils.hitLineBox(rocket.x, rocket.y, rocket.x2(), rocket.y2(), x, y, w, h);
		}

		// Draw the wall
		public void show() {
			fill(200);
			stroke(255);
			strokeWeight(5);
			rect(x, y, w, h);
		}
	}
}
