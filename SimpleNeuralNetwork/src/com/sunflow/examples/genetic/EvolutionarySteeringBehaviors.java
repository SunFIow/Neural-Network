package com.sunflow.examples.genetic;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

import com.sunflow.game.Game2D;
import com.sunflow.logging.Log;
import com.sunflow.math.SVector;
import com.sunflow.simpleneuralnetwork.simple.NeuralNetwork;
import com.sunflow.simpleneuralnetwork.util.Creature;
import com.sunflow.simpleneuralnetwork.util.Population;
import com.sunflow.util.Mapper;

public class EvolutionarySteeringBehaviors extends Game2D {
	public static void main(String[] args) {
		new EvolutionarySteeringBehaviors();
	}

	private String bestVehicleFile = "rec/brains/bestVehicleBrain";

	// A frame counter to determine when to finish generation

	// All time high score
	private double highScore;

	private Population<Vehicle> population;

	private int cycles;

	private ArrayList<SVector> food;

	private ArrayList<SVector> poison;

	private boolean debug;

	private static final int popSize = 30, maxFood = 300, maxPoison = 80;
	private static final float foodNut = 0.3F, poisonNut = -0.5F;
	private static final double foodRate = 0.2, poisonRate = 0.1, babyRate = 0.002;

	@Override
	protected void setup() {
		createCanvas(800, 600);
		smooth();
		frameRate(60);

		highScore = 0;
		cycles = 1;

		population = new Population<Vehicle>(popSize, Vehicle::new);

		food = new ArrayList<>();
		for (int i = 0; i < maxFood / 3; i++) {
			float x = random(width);
			float y = random(height);
			food.add(new SVector(x, y));
		}

		poison = new ArrayList<>();
		for (int i = 0; i < maxPoison; i++) {
			float x = random(width);
			float y = random(height);
			poison.add(new SVector(x, y));
		}
	}

	private void resetGame() {
		population.generation++;
		highScore = 0;
		cycles = 1;

		food = new ArrayList<>();
		for (int i = 0; i < maxFood / 3; i++) {
			float x = random(width);
			float y = random(height);
			food.add(new SVector(x, y));
		}

		poison = new ArrayList<>();
		for (int i = 0; i < maxPoison; i++) {
			float x = random(width);
			float y = random(height);
			poison.add(new SVector(x, y));
		}

		population.populateOf(population.bestCreature());
	}

	private void logic() {
		// Should we speed up cycles per frame
		// How many times to advance the game
		for (int n = 0; n < cycles; n++) {
			// Are we just running the best Rocket
			for (int i = population.getActiveSize() - 1; i >= 0; i--) {
				Vehicle vehicle = population.get(i);
				// Rocket uses its brain!
				vehicle.think(food, poison);
				vehicle.update();

				vehicle.eat(food, foodNut);
				vehicle.eat(poison, poisonNut);

				if (!vehicle.offScreen() && Math.random() < babyRate) {
					Log.error("Baby");
					Vehicle v = vehicle.mutate();
					v.pos = SVector.add(vehicle.pos, new SVector((float) Math.random() * 6 - 4, (float) Math.random() * 6 - 4));
					population.add(v);
				}

				if (!vehicle.isAlive()) {
					population.remove(vehicle);
				}
			}

			if (Math.random() < (float) (maxFood - poison.size()) / maxFood * foodRate) {
				float x = random(width);
				float y = random(height);
				food.add(new SVector(x, y));
			}

			if (Math.random() < (float) (maxPoison - poison.size()) / maxPoison * poisonRate) {
				float x = random(width);
				float y = random(height);
				poison.add(new SVector(x, y));
			}

			// What is highest score of the current population
			double tempHighScore = 0;
			// Which is the best Rocket?
			Vehicle tempBestVehicle = null;
			for (int i = 0; i < population.getActiveSize(); i++) {
				Vehicle vehicle = population.get(i);
				double s = vehicle.score();
				if (s > tempHighScore) {
					tempHighScore = s;
					tempBestVehicle = vehicle;
				}
			}

			// Is it the all time high scorer?
			if (tempHighScore > highScore) {
				highScore = tempHighScore;
				population.setBestCreature(tempBestVehicle);
			}

			if (population.getActiveSize() == 0) {
				resetGame();
			}
		}
	}

	@Override
	protected void draw() {
		logic();

		background(25);

		// Draw everything!

		int size = population.getActiveSize();
		for (int i = 0; i < size; i++) {
			population.get(i).show();
		}

		fill(0, 255, 0, 100);
		stroke(0);
		strokeWeight(1);
		size = food.size();
		for (int i = 0; i < size; i++) {
			SVector f = food.get(i);
			ellipse(f.x, f.y, 10, 10);
		}

		fill(255, 0, 0, 100);
		size = poison.size();
		for (int i = 0; i < size; i++) {
			SVector p = poison.get(i);
			ellipse(p.x, p.y, 10, 10);
		}

		fill(255, 0, 0);
		stroke(50, 0, 0);
		strokeWeight(2);
		textSize(20);
		text("Generation: " + population.generation(), width - 200, 25);
		text("Cycles: " + cycles, width - 200, 45);
		text("Alive: " + population.getActiveSize(), width - 200, 65);
//		textO("LifeSpawn: " + lifespan, width - 200, 85);
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
				case KeyEvent.VK_D:
					debug = !debug;
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
					serialize(bestVehicleFile, population.bestCreature().brain());
					Log.info("serialized");
					break;
				case KeyEvent.VK_L:
					population.populateOf(new Vehicle((NeuralNetwork) deserialize(bestVehicleFile)));
					Log.info("deserialized");
					break;
			}
		}
	}

	private static int inputs_length = 9;
	private static int outputs_length = 2;
	private static int hidden_length = 10;

	private class Vehicle extends Creature<Vehicle> {
		protected Mapper mutate = new Mapper() {
			@Override
			public float func(float x, int i, int j) {
				if (random(1.0f) < 0.01f) {
					float offset = (float) new Random().nextGaussian() * 0.25f;
					float newx = x + offset;
					return newx;
				} else return x;
			}
		};

		private SVector pos;
		private float r;

		private SVector velocity;
		private SVector acceleration;

		private float visionFood;
		private float visionPoison;

		private float health;
		private int timeAlive;

		public Vehicle() {
			super(inputs_length, outputs_length, hidden_length);
			// position and size of Rocket
			float x = random(width);
			float y = random(height);

			pos = new SVector(x, y);
			velocity = SVector.fromAngle((float) (-1 * Math.random() * Math.PI), null);
			acceleration = new SVector();

			r = 8F;
			health = 1;

			visionFood = random(10, 400);
			visionPoison = random(10, 400);
		}

		public Vehicle(NeuralNetwork brain) {
			this();
			this.brain = brain.clone();
		}

//		public SVector dir() {
//			return velocity.clone().normalize();
//		}

//		public float x2() {
//			return dir().x * r + x;
//		}
//
//		public float y2() {
//			return dir().y * r + y;
//		}

		// Create a copy of this Rocket
		@Override
		public Vehicle mutate() {
			Vehicle copy = clone();
			copy.brain().mutate(mutate);
//			copy.pos = pos.clone();
			copy.visionFood = random(1.0) < 0.1 ? mutate.func(visionFood, 0, 0) : visionFood;
			copy.visionPoison = random(1.0) < 0.1 ? mutate.func(visionPoison, 0, 0) : visionPoison;
			return copy;
		}

		@Override
		public Vehicle clone() { return new Vehicle(brain); }

		@Override
		public float calcScore() { return timeAlive; }

		@Override
		public void update(double dt) {
			velocity.add(acceleration).limit(5);
			pos.add(velocity);
			acceleration.mult(0);

			timeAlive++;
			health -= offScreen() ? 0.01 : 0.005;
		}

		public boolean offScreen() {
			return (pos.x < 0 || pos.y < 0 || pos.x > width || pos.y > height);
		}

		public boolean isAlive() { return health > 0; }

		public void eat(ArrayList<SVector> list, float nutrition) {
			for (int i = list.size() - 1; i >= 0; i--) {
				if (SVector.dist(pos, list.get(i)) < r) {
					health += nutrition;
					list.remove(i);
				}
			}
		}

		// This is the key function now that decides
		// if it should jump or not jump!
		public void think(ArrayList<SVector> food, ArrayList<SVector> poison) {
			// Now create the inputs to the neural network
			double[] inputs = new double[inputs_length];

			Pair<SVector, Float> closestFood = getClosest(pos, food.toArray(new SVector[0]));
			Pair<SVector, Float> closestPoison = getClosest(pos, poison.toArray(new SVector[0]));

			// x position the vehicle
			inputs[0] = map(pos.x, 0, width, 0, 1);
			// y position the vehicle
			inputs[1] = map(pos.y, 0, height, 0, 1);
			// velocity x the vehicle
			inputs[2] = map(velocity.x, -5, 5, 0, 1);
			// velocity y the vehicle
			inputs[3] = map(velocity.y, -5, 5, 0, 1);
			if (closestFood.a != null) {
				// x position the closest food
				inputs[4] = map(closestFood.a.x, 0, width, 0, 1);
				// y position the closest food
				inputs[5] = map(closestFood.a.y, 0, height, 0, 1);
			}
			if (closestPoison.a != null) {
				// x position the closest poison
				inputs[6] = map(closestPoison.a.x, 0, width, 0, 1);
				// y position the closest poison
				inputs[7] = map(closestPoison.a.y, 0, height, 0, 1);
			}
			// health of the vehicle
			inputs[8] = health;
			// Get the outputs from the network
			double[] action = this.brain().predict(inputs);
			// Decide the thrust!
//			double angle = -1 * Math.random() * Math.PI;
//			thrust = Vertex2D.of(action[0], action[1]);
//			applyForce(new SVector((float) (action[0] - action[1]), (float) (action[2] - action[3])).normalize().mult(2));
			if (visionFood > closestFood.b) {
				SVector pointer = SVector.sub(closestFood.a, pos);
				applyForce(pointer.normalize().mult((float) (action[0] * 2 - 1)));
			}
			if (visionPoison > closestPoison.b) {
				SVector pointer = SVector.sub(closestPoison.a, pos);
				applyForce(pointer.normalize().mult((float) (action[1] * 2 - 1)));
			}

		}

		private void applyForce(SVector force) {
			acceleration.add(force);
		}

		// Display the Rocket
		public void show() {
			fill(lerpColor(color(255, 0, 0), color(0, 255, 0), health), 100);
			stroke(0);
			strokeWeight(1);
//			line(x, y, x2(), y2());
			ellipse(pos.x, pos.y, r * 2, r * 2);

			if (debug) {
				noFill();
				stroke(255, 0, 0, 100);
				ellipse(pos.x, pos.y, visionPoison, visionPoison);

				stroke(0, 255, 0, 100);
				ellipse(pos.x, pos.y, visionFood, visionFood);
			}
		}
	}
}
