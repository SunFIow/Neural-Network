package com.sunflow.examples.genetic;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import com.sunflow.game.Game2D;
import com.sunflow.simpleneuralnetwork.Creature;
import com.sunflow.simpleneuralnetwork.NeuralNetwork;
import com.sunflow.simpleneuralnetwork.Population;

public class TRexRunner extends Game2D {
	public static void main(String[] args) {
		new TRexRunner();
	}

	private static int inputs_length = 6;
	private static int outputs_length = 3;
	private static int hidden_length = 10;

	private float groundY;
	private ArrayList<Cactus> cacti;

	private TRex rex;
	private Population<TRex> population;
	private int cycles;
	private int score;
	private int bestScore;
	private boolean runBest;
	private double highScore;
	private long counter;
	private boolean humanPlay;

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
		humanPlay = false;

		groundY = heightF - 20;
		rex = new TRex();
		cacti = new ArrayList<>();
		population = new Population<TRex>(50, TRex::new);
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

	@Override
	protected void update(double delta) {
		for (int n = 0; n < cycles; n++) {
			if (humanPlay) {
//					rex.think();
				rex.update(delta);
			} else {
				for (int i = 0; i < population.getActiveSize(); i++) {
					TRex rex = population.get(i);
					rex.think();
					rex.update(delta);
					if (rex.collide(cacti)) population.remove(rex);
				}
//				for (int i = 0; i < population.getActiveSize(); i++) {
//					if (population.get(i).collide(cacti)) {
//						population.remove(i);
//						break;
//					}
//				}

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
			}
			for (int i = cacti.size() - 1; i >= 0; i--) {
				Cactus cactus = cacti.get(i);
				cactus.update(delta);
				if (cactus.offScreen()) {
					cacti.remove(i);
					score++;
					if (score > bestScore) {
						bestScore = score;
					}
				} else cactus.show();
			}

			if (counter++ % (frameRate * 2) == 0) {
				cacti.add(new Cactus());
			}
		}
	}

	@Override
	protected void draw() {
		background(240);

		stroke(0);
		strokeWeight(3);
		line(0, groundY, widthF, groundY);

		@SuppressWarnings("unchecked")
		ArrayList<Cactus> cacti = (ArrayList<Cactus>) this.cacti.clone();
		for (Cactus cactus : cacti) {
			cactus.show();
		}
		if (humanPlay) {
			rex.show();
		} else {
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
		}

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
		switch (e.getKeyCode()) {
			case KeyEvent.VK_SPACE:
				rex.jump();
				break;
			case KeyEvent.VK_CONTROL:
				rex.sneak();
				break;
		}
		if (!e.isControlDown()) {
			switch (e.getKeyCode()) {
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
			case KeyEvent.VK_SPACE:
				rex.spaceReleased();
				break;
			case KeyEvent.VK_CONTROL:
				rex.unSneak();
				break;
		}
	}

	private class TRex extends Creature<TRex> {
		private static final float gravityFall = 4000F;
		private static final float gravityJump = 4000F;
		private static final float gravityJumpBig = 2000F;
		private static final float jumpForce = 40000F;

		private static final float x = 30;
		private static final float r = 15;

		private float yPos;
		private float yVel;
		private float yAcc;

		private boolean onGround;
		private boolean sneaking;
		private boolean spaceDown;

		private boolean bufferJump;

		private int timeAlive;

		private TRex() {
			super(inputs_length, outputs_length, hidden_length);

			yPos = groundY;

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
			if (!sneaking) ellipse(x, yPos, r * 2, r * 2);
			else ellipse(x, yPos + r / 2, r * 2, r);
		}

		private void jump() {
			if (onGround) {
				applyForce(-jumpForce);
				yPos = groundY - r - 0.0001F;
				onGround = false;
				sneaking = false;
				spaceDown = true;
			} else {
				if (yVel > 0 && yPos > groundY - 30) {
					bufferJump = true;
				}
			}
		}

		private void applyForce(float force) {
			yAcc += force;// * Math.pow(frameRate / 60D, 2);
		}

		private void spaceReleased() {
			spaceDown = false;
		}

		private void sneak() {
			if (onGround) sneaking = true;
		}

		private void unSneak() {
			sneaking = false;
		}

		@Override
		public void update(double delta) {
			applyForce(yVel > 0 ? gravityFall : (spaceDown ? gravityJumpBig : gravityJump));

			yVel += yAcc * delta;
			yPos += yVel * delta + yAcc * 0.5 * delta * delta;
			yAcc = 0;

			if (yPos + r >= groundY) {
				yPos = groundY - r;
				onGround = true;
				yVel = 0;
				if (bufferJump) {
					jump();
					bufferJump = false;
				}
			}

			timeAlive++;
		}

		private void think() {
			double record = 0;
			Cactus closest = null;
			for (Cactus c : cacti) {
				double dist = dist(c.x, 0, x, 0);
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
			inputs[0] = yPos;
			// y-velocity of this t-rex
			inputs[1] = yVel;
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
					jump();
					break;
				case 1:
					spaceReleased();
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
				if (hitBoxCircle(c.x, c.y, c.w, c.h, x, yPos, r * 0.8)) return true;
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
		private final float vel = 300 + 1F * (counter / frameRate);
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

		private void update(double delta) {
			x -= vel * delta;
		}

		private boolean offScreen() {
			return x + w < 0;
		}
	}
}
