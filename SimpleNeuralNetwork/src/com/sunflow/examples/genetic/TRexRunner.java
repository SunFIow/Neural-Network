package com.sunflow.examples.genetic;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import com.sunflow.game.Game2D;
import com.sunflow.simpleneuralnetwork.Creature;
import com.sunflow.simpleneuralnetwork.NeuralNetwork;
import com.sunflow.simpleneuralnetwork.Population;
import com.sunflow.util.Utils;

public class TRexRunner extends Game2D {
	public static void main(String[] args) {
		new TRexRunner();
	}

	private static int inputs_length = 6;
	private static int outputs_length = 3;
	private static int hidden_length = 10;

	private float groundY;
	private ArrayList<Cactus> cacti;

//	private TRex rex;
	private Population<TRex> population;
	private int cycles;
	private int score;
	private int bestScore;
	private boolean runBest;
	private double highScore;
	private long counter;

	@Override
	protected void setup() {
		createCanvas(800, 400);
		smooth();
		frameRate(60);

		runBest = false;
		cycles = 1;
		highScore = 0;
		score = 0;
		bestScore = 0;

		groundY = heightF - 20;
//		rex = new TRex();
		cacti = new ArrayList<>();
		cacti.add(new Cactus());
		population = new Population<TRex>(50) {
			@Override
			protected TRex getCreature() {
				return new TRex();
			}
		};
	}

	// Start the game over
	private void resetGame() {
		frameCount = 0;
		counter = 0;
		score = 0;
		// Resetting best bird score to 0
		if (population.bestCreature() != null) {
			population.bestCreature().score = 0;
			population.bestCreature().timeAlive = 0;
		}
		cycles = Math.min(cycles, 500);
		cacti = new ArrayList<>();
	}

	protected void logic() {
		for (int n = 0; n < cycles; n++) {
			for (int i = 0; i < population.getActiveSize(); i++) {
				TRex rex = population.get(i);
				rex.think();
				rex.update();
			}
//		rex.think();
//			rex.update();

			for (int i = cacti.size() - 1; i >= 0; i--) {
				Cactus cactus = cacti.get(i);
				cactus.update();
				if (cactus.offScreen()) {
					cacti.remove(i);
					score++;
					if (score > bestScore) {
						bestScore = score;
					}
				} else cactus.show();
			}
			for (int i = 0; i < population.getActiveSize(); i++) {
				if (population.get(i).collide(cacti)) {
					population.remove(i);
					break;
				}
			}

			// What is highest score of the current population
			double tempHighScore = 0;
			// If we're training
			if (!runBest) {
				// Which is the best bird?
				TRex tempBestBird = null;
				for (int i = 0; i < population.getActiveSize(); i++) {
					TRex bird = population.get(i);
					double s = bird.score();
					if (s > tempHighScore) {
						tempHighScore = s;
						tempBestBird = bird;
					}
				}

				// Is it the all time high scorer?
				if (tempHighScore > highScore) {
//							Log.info("new BestBird");
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

			if (counter++ % Math.round((frameRate * 1.5)) == 0) {
				cacti.add(new Cactus());
			}
		}
	}

	@Override
	protected void draw() {
		logic();
		background(240);

		stroke(0);
		strokeWeight(3);
		line(0, groundY, widthF, groundY);

		ArrayList<Cactus> cacti = (ArrayList<Cactus>) this.cacti.clone();
		for (Cactus cactus : cacti) {
			cactus.show();
		}
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
//		rex.show();

		fill(255, 0, 0);
		stroke(50, 0, 0);
		strokeWeight(2);
		textSize(20);
		textO("Generation: " + population.generation(), width - 180, 25);
		textO("Cycles: " + cycles, width - 180, 45);
		textO("Alive: " + population.getActiveSize(), width - 180, 65);
		textO("Score: " + score, width - 180, 85);
		textO("HighScore: " + bestScore, width - 180, 105);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!e.isControlDown()) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_A:
//					rex.jump(true);
					break;
				case KeyEvent.VK_SPACE:
				case KeyEvent.VK_D:
//					rex.jump(false);
					break;
				case KeyEvent.VK_CONTROL:
				case KeyEvent.VK_S:
//					rex.sneak();
					break;
				case KeyEvent.VK_PLUS:
					cycles++;
					break;
				case KeyEvent.VK_MINUS:
					cycles--;
					if (cycles < 1) cycles = 1;
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
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_CONTROL:
			case KeyEvent.VK_S:
//				rex.unSneak();
				break;
		}
	}

	private class TRex extends Creature<TRex> {
		private static final float gravity = 0.5F;
		private static final float smallJump = 8.5F, bigJump = 10.5F;
		private static final float x = 30;
		private static final float r = 15;

		private float y;

		private float vel;

		private boolean onGround;
		private boolean sneaking;

		private int bufferJump;

		private int timeAlive;

		private TRex() {
			super(inputs_length, outputs_length, hidden_length);

			y = groundY;

			onGround = true;
			sneaking = false;
		}

		private TRex(NeuralNetwork brain) {
			this();
			this.brain = brain.clone();
		}

		private int findHighest(double[] action) {
			double max = -1;
			int maxI = -1;
			for (int i = 0; i < action.length; i++) {
				if (action[i] > max) {
					max = action[i];
					maxI = i;
				}
			}
			return maxI;
		}

		private void show() {
			fill(255, 150, 150, 100);
			stroke(0, 150);
			strokeWeight(3);
			if (!sneaking) ellipse(x, y, r * 2, r * 2);
			else ellipse(x, y + r / 2, r * 2, r);
		}

		private void jump(boolean small) {
			if (onGround) {
				vel = small ? -smallJump : -bigJump;
				onGround = false;
				sneaking = false;
			} else {
				if (vel > 0 && y > groundY - 70) {
					bufferJump = small ? 1 : 2;
				}
			}
		}

		private void sneak() {
			if (onGround) sneaking = true;
		}

		private void unSneak() {
			sneaking = false;
		}

		@Override
		public void update() {
			vel += gravity;
			Utils.clamp(-10, vel, 10);
			y += vel;

			if (y + r >= groundY) {
				y = groundY - r;
				onGround = true;
			}
			if (onGround && bufferJump > 0) {
				jump(bufferJump == 1 ? true : false);
				bufferJump = 0;
			}

			timeAlive++;
		}

		private void think() {
			double record = 0;
			Cactus closest = null;
			for (Cactus c : cacti) {
				double dist = Utils.dist(c.x, 0, x, 0);
				if (dist < record) {
					record = dist;
					closest = c;
				}
			}

			double[] inputs = new double[inputs_length];

//			// x-position of this t-rex
//			inputs[0] = x;
//			// width of this t-rex
//			inputs[0] = r;
//			// height of this t-rex
//			inputs[0] = r;

			// y-position of this t-rex
			inputs[0] = y;
			// y-velocity of this t-rex
			inputs[1] = vel;
			// x-position of the closest cactus
			inputs[2] = 0;
			if (closest != null) {
				// y-position of the closest cactus
				inputs[3] = closest.y;
				// width of the closest cactus
				inputs[4] = closest.w;
				// height of the closest cactus
				inputs[5] = closest.h;
			}

			double[] action = brain.predict(inputs);

			int highest = findHighest(action);
			switch (highest) {
				case 0:
					jump(false);
					break;
				case 1:
					jump(true);
					break;
				case 2:
					sneak();
					break;
				case 3:
					unSneak();
					break;
			}
		}

		public boolean collide(ArrayList<Cactus> cacti) {
			for (Cactus c : cacti) {
				if (Utils.hitBoxCircle(c.x, c.y, c.w, c.h, x, y, r * 0.8)) return true;
			}
			return false;
		}

		@Override
		protected double calcScore() {
			return timeAlive * timeAlive;
		}

		@Override
		public TRex clone() {
			return new TRex(brain);
		}

		@Override
		protected TRex mutate() {
			TRex copy = clone();
			copy.brain().mutate(mutate);
			return copy;
		}
	}

	private class Cactus {
		private final float vel = 5 + 0.12F * (frameCount / frameRate);
		private final float w = 20;
		private final float h = 38;

		private float x;
		private float y;

		private Cactus() {
			x = widthF + 50;
			y = groundY - h;
		}

		private void show() {
			fill(150, 255, 150, 100);
			stroke(0, 150);
			strokeWeight(3);
			rect(x, y, w, h);
		}

		private void update() {
			x -= vel;
		}

		private boolean offScreen() {
			return x + w < 0;
		}
	}
}
