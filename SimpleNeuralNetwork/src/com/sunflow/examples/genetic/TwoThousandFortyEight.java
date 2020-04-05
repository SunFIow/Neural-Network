package com.sunflow.examples.genetic;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import com.sunflow.game.Game2D;
import com.sunflow.logging.Log;
import com.sunflow.simpleneuralnetwork.simple.NeuralNetwork;
import com.sunflow.simpleneuralnetwork.util.Creature;
import com.sunflow.simpleneuralnetwork.util.Population;
import com.sunflow.util.Constants;

public class TwoThousandFortyEight extends Game2D {
	public static void main(String[] args) { new TwoThousandFortyEight(); }

	private static int inputs_length = 16 * 16;
	private static int outputs_length = 4;
	private static int hidden_length = 50;

	// All time high score
//	private double highScore;

	// Game ticks per draw
	private int cycles;
	private boolean debug;

	private Field[][] fields;
	private int size;

	private float fW, fH;

	private boolean swiping;
	ArrayList<Field[]> swipePairs = new ArrayList<>();

	private String best2048File = "rec/brains/best2048Brain";

	private Population<TTFEBrain> population;

//	private TTFEBrain[] models;
	private TTFEBrain model;
	private int modelIndex;

	private boolean finishedGen;
	private int bestModel = -1;
	private double bestModelScore = -1;

	private static final int allowedInvalidMoves = 2;
	private int invalidMoves;

	private boolean simulate;

	private boolean genNext;

	@Override
	protected void setup() {
		createCanvas(400, 400);
		smooth();
		frameRate(60);

//		highScore = 0;
		cycles = 1;
		swiping = false;
		simulate = false;
		genNext = true;

		size = 4;
		fields = new Field[size][size];

		for (int x = 0; x < size; x++) for (int y = 0; y < size; y++) {
			fields[x][y] = new Field(x, y);
		}

		for (int i = 0; i < 2; i++) genNewValue();

//		models = new TTFEBrain[50];
//		for (int i = 0; i < models.length; i++) models[i] = new TTFEBrain();

		population = new Population<TTFEBrain>(50, TTFEBrain::new);

		model = population.get(modelIndex++);

	}

	private void genNewValue() {
		try {
			int i;
			int x;
			int y;
			int c = 0;
			do {
				c++;
				if (c > 100000) Log.error("Can't create new Value");
				i = random(15);
				x = i % size;
				y = i / size;
			} while (fields[x][y].value != 0);
			fields[x][y].value = Math.random() < 0.25 ? 4 : 2;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void nextModel() {
//		population.generation++;
//		highScore = 0;
//		cycles = 1;
		swiping = false;
		invalidMoves = 0;

		if (model.score() > bestModelScore) {
			bestModel = modelIndex;
			bestModelScore = model.score;
		}

		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				fields[x][y].reset();
				fields[x][y].value = 0;
			}
		}

		for (int i = 0; i < 2; i++) {
			genNewValue();
		}

		if (modelIndex == population.totalPopulation) {
			if (genNext) {
				// New Generation
				Log.error("next Gen");
				modelIndex = 0;
				population.setBestCreature(population.get(bestModel));
				population.nextGeneration();
				model = population.get(modelIndex++);
			} else {
				model = population.get(bestModel);
				finishedGen = true;
			}
		} else {
//			model = models[modelIndex++];
			model = population.get(modelIndex++);
		}
	}

	private void logic() {
		// How many times to run the game per tick
		for (int n = 0; n < cycles; n++) {
			if (swiping) {
				invalidMoves = 0;
				for (int i = swipePairs.size() - 1; i >= 0; i--) {
					Field[] pair = swipePairs.get(i);
					Field p1 = pair[0];
//					Field p2 = pair[1];
					p1.update();
					if (!p1.swiping) {
//					p2.value += p1.value;
//					p1.value = 0;
						swipePairs.remove(pair);
					}
				}
				if (swipePairs.isEmpty()) {
					swiping = false;
					genNewValue();
				}
			} else if (!finishedGen && simulate) {
				model.think();
				invalidMoves++;
			}

			if (invalidMoves > allowedInvalidMoves) nextModel();
		}
	}

	@Override
	protected void draw() {
		logic();
		background(25);
		fW = width / size;
		fH = height / size;
		// Draw everything!
		textAlign(Constants.CENTER, Constants.CENTER);
		textSize(32);

		stroke(187, 173, 160);
		strokeWeight(20);
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				fill(color(205, 190, 180));
				rect(x * fW, y * fH, fW, fH);
			}
		}

		for (int x = 0; x < fields.length; x++) {
			for (int y = 0; y < fields[x].length; y++) {
				fields[x][y].show();
			}
		}
		for (Field[] pair : swipePairs) {
			pair[1].show();
		}
		for (Field[] pair : swipePairs) {
			pair[0].show();
		}

		textAlign(Constants.LEFT, Constants.BASELINE);
		textSize(16);
		fill(255, 0, 0, 100);
		stroke(0, 0, 0, 50);
		strokeWeight(4);
		float xoff = width - 150;
		float yoff = 16;
		text("Generation: " + population.generation(), xoff, yoff * 1);
		text("Cycles: " + cycles, xoff, yoff * 2);
		text("ModelIndex: " + modelIndex, xoff, yoff * 3);
		text("HighScore: " + bestModelScore, xoff, yoff * 4);
		text((simulate ? "" : "not ") + "simulating", xoff, yoff * 5);
	}

	/**
	 * @param i
	 *            x direction
	 * @param j
	 *            y direction
	 */

	private void swipe(int dir) {
		if (swiping) return;
		boolean swiped = false;

		if (dir == Constants.RIGHT) {
			// Swipe Right
			for (int x = size - 2; x >= 0; x--) {
				for (int y = 0; y < size; y++) {
					boolean sucess = false;
					Field f1 = fields[x][y];
					int k;
					for (k = x + 1; k < size; k++) {
						if (fields[k][y].value != 0) break;
					}
					k = Math.min(k, size - 1);
					Field f2 = fields[k][y];
					if (f2.value == 0 && f1.value != 0 || (f2.value == f1.value && f2.value != 0)) {
						sucess = true;
					} else if (k - 1 != x) {
						f2 = fields[k - 1][y];
						if (f2.value == 0 && f1.value != 0 || (f2.value == f1.value && f2.value != 0)) {
							sucess = true;
						}
					}
					if (sucess) {
						swipePairs.add(new Field[] { f1.clone(), f2.clone() });
						f2.value += f1.value;
						f1.value = 0;
						swiped = true;
					}
				}
			}
		} else if (dir == Constants.LEFT) {
			// Swipe Left
			for (int x = 1; x < size; x++) {
				for (int y = 0; y < size; y++) {
					boolean sucess = false;
					Field f1 = fields[x][y];
					int k;
					for (k = x - 1; k >= 0; k--) {
						if (fields[k][y].value != 0) break;
					}
					k = Math.max(k, 0);
					Field f2 = fields[k][y];
					if (f2.value == 0 && f1.value != 0 || (f2.value == f1.value && f2.value != 0)) {
						sucess = true;
					} else if (k + 1 != x) {
						f2 = fields[k + 1][y];
						if (f2.value == 0 && f1.value != 0 || (f2.value == f1.value && f2.value != 0)) {
							sucess = true;
						}
					}
					if (sucess) {
						swipePairs.add(new Field[] { f1.clone(), f2.clone() });
						f2.value += f1.value;
						f1.value = 0;
						swiped = true;
					}
				}
			}
		} else if (dir == Constants.DOWN) {
			// Swipe Down
			for (int x = 0; x < size; x++) {
				for (int y = size - 2; y >= 0; y--) {
					boolean sucess = false;
					Field f1 = fields[x][y];
					int k;
					for (k = y + 1; k < size; k++) {
						if (fields[x][k].value != 0) break;
					}
					k = Math.min(k, size - 1);
					Field f2 = fields[x][k];
					if (f2.value == 0 && f1.value != 0 || (f2.value == f1.value && f2.value != 0)) {
						sucess = true;
					} else if (k - 1 != y) {
						f2 = fields[x][k - 1];
						if (f2.value == 0 && f1.value != 0 || (f2.value == f1.value && f2.value != 0)) {
							sucess = true;
						}
					}
					if (sucess) {
						swipePairs.add(new Field[] { f1.clone(), f2.clone() });
						f2.value += f1.value;
						f1.value = 0;
						swiped = true;
					}
				}
			}
		} else if (dir == Constants.UP) {
			// Swipe Up
			for (int x = 0; x < size; x++) {
				for (int y = 1; y < size; y++) {
					boolean sucess = false;
					Field f1 = fields[x][y];
					int k;
					for (k = y - 1; k >= 0; k--) {
						if (fields[x][k].value != 0) break;
					}
					k = Math.max(k, 0);
					Field f2 = fields[x][k];
					if (f2.value == 0 && f1.value != 0 || (f2.value == f1.value && f2.value != 0)) {
						sucess = true;
					} else if (k + 1 != y) {
						f2 = fields[x][k + 1];
						if (f2.value == 0 && f1.value != 0 || (f2.value == f1.value && f2.value != 0)) {
							sucess = true;
						}
					}
					if (sucess) {
						swipePairs.add(new Field[] { f1.clone(), f2.clone() });
						f2.value += f1.value;
						f1.value = 0;
						swiped = true;
					}
				}
			}
		}
		if (!swiped) {
			boolean full = true;
			for (int x = 0; x < fields.length; x++) {
				for (int y = 0; y < fields[x].length; y++) {
					if (fields[x][y].value == 0) full = false;
				}
			}
			if (full) {
				boolean matchingNeighbors = false;
				for (int x = 0; x < fields.length; x++) {
					for (int y = 0; y < fields[x].length; y++) {
						int _x = x < 1 ? 1 : x > size - 2 ? size - 2 : x;
						int _y = y < 1 ? 1 : y > size - 2 ? size - 2 : y;
						int v = fields[_x][_y].value;
						int v1 = fields[_x + 1][_y].value;
						int v2 = fields[_x - 1][_y].value;
						int v3 = fields[_x][_y + 1].value;
						int v4 = fields[_x][_y - 1].value;
						if (v == v1 || v == v2 || v == v3 || v == v4) matchingNeighbors = true;
					}
				}

				if (!matchingNeighbors) {
					nextModel();
				}
				return;
			}
		} else {
			swiping = true;
		}

//		swipePairs.sort((x, y) -> {
//			int val = 0;
//			switch (dir) {
//				case Constants.UP:
//					val = y[0].y - x[0].y;
//					break;
//				case Constants.LEFT:
//					val = y[0].x - x[0].x;
//					break;
//				case Constants.DOWN:
//					val = x[0].y - y[0].y;
//					break;
//				case Constants.RIGHT:
//					val = x[0].x - y[0].x;
//					break;
//			}
//			return val;
//		});

		for (Field[] pair : swipePairs) {
			Field f1 = pair[0];
			Field f2 = pair[1];
			f1.swipe(dir, f2);
//			Log.infoArray(pair);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!e.isControlDown()) {
			switch (e.getKeyCode()) {
				case Constants.UP:
				case Constants.LEFT:
				case Constants.DOWN:
				case Constants.RIGHT:
					swipe(e.getKeyCode());
					break;

				case KeyEvent.VK_PLUS:
					cycles++;
					break;

				case KeyEvent.VK_MINUS:
					cycles--;
					if (cycles < 1) cycles = 1;
					break;

				case KeyEvent.VK_A:
					genNewValue();
					break;

				case KeyEvent.VK_D:
					debug = !debug;
					break;
			}
		} else {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_SPACE:
					simulate = !simulate;
					break;

				case KeyEvent.VK_P:
					genNext = !genNext;
					Log.info("Generate infinit: " + genNext);
					break;

				case KeyEvent.VK_ENTER:
					model.think();
					invalidMoves++;
					break;

				case KeyEvent.VK_PLUS:
					cycles += 10;
					break;

				case KeyEvent.VK_MINUS:
					cycles -= 10;
					if (cycles < 1) cycles = 1;
					break;

				case KeyEvent.VK_S:
					serialize(best2048File, population.bestCreature().brain());
					Log.info("serialized");
					break;

				case KeyEvent.VK_L:
					population.populateOf(new TTFEBrain((NeuralNetwork) deserialize(best2048File)));
					Log.info("deserialized");
					break;
			}
		}
	}

	private int getTextColor(int value) {
		if (value == 2 || value == 4) return color(120, 110, 100);
		return color(255, 255, 255);
	}

	private int getFieldColor(int value) {
		if (value == 0) return color(205, 190, 180);
		if (value == 2) return color(240, 230, 220);
		if (value == 4) return color(240, 225, 200);
		if (value == 8) return color(250, 150, 100);
		if (value == 16) return color(250, 150, 100);
		if (value == 32) return color(250, 125, 95);
		if (value == 64) return color(245, 95, 60);
		if (value == 128) return color(240, 210, 115);
		if (value == 256) return color(240, 205, 100);
		if (value == 512) return color(240, 200, 80);
		if (value == 1024) return color(240, 195, 60);
		if (value == 2048) return color(240, 195, 45);
		if (value == 4096) return color(180, 130, 170);
		if (value == 8192) return color(170, 105, 165);
		if (value == 16384) return color(160, 80, 160);
		if (value == 32768) return color(140, 20, 140);

		return color(0, 0, 255);
	}

	private class TTFEBrain extends Creature<TTFEBrain> {

		public TTFEBrain() {
			super(inputs_length, outputs_length, hidden_length);
			this.brain.setLearningRate(0.01f);
		}

		public TTFEBrain(NeuralNetwork brain) {
			this();
			this.brain = brain.clone();
		}

		// if it should jump or not jump!
		public void think() {
			// Now create the inputs to the neural network
			float[] inputs = new float[inputs_length];

//			float[][] i01 = new float[size][size];
			int index = 0;
			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
//					float hasVal = 0;
					for (int i = 0; i < 16; i++) {
						int val = (int) Math.pow(2, i);
						int inp = fields[x][y].value == (val == 1 ? 0 : val) ? 1 : 0;
						inputs[index++] = inp;
//						if (inp == 1 && fields[x][y].value != 0) hasVal = 1;
					}
//					i01[x][y] = hasVal;
				}
			}

//			Log.error(" --- --- --- ---");
//			for (int x = 0; x < size; x++) {
//				String line = "| ";
//				for (int y = 0; y < size; y++) {
//					line += String.format("%d%s", (int) i01[x][y], x < size ? " | " : "");
//				}
//				Log.error(line);
//				if (x < size) Log.error(" --- --- --- ---");
//			}
//			Log.error("");
//			Log.error("");

//			index = 0;
//			for (int i = 0; i < size * size; i++) {
//				int x = i % 4;
//				int y = i / 4;
//				String line = "[" + x + "][" + y + "] | ";
//				for (int j = 0; j < size * size; j++) {
//					line += String.format("%d | ", (int) inputs[index++]);
//				}
//				Log.error(line);
//				Log.error(" --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---");
//			}

			// Get the outputs from the network
			float[] action = this.brain().predict(inputs);
			// Decide to jump or not!
			int highest = findHighest(action);

			switch (highest) {
				case 0:
					swipe(Constants.UP);
					break;
				case 1:
					swipe(Constants.LEFT);
					break;
				case 2:
					swipe(Constants.RIGHT);
					break;
				case 3:
					swipe(Constants.DOWN);
					break;
			}
		}

		private int findHighest(float[] action) {
			float max = -1;
			int maxI = -1;
			for (int i = 0; i < action.length; i++) {
				if (action[i] > max) {
					max = action[i];
					maxI = i;
				}
			}
			return maxI;
		}

		@Override
		public TTFEBrain clone() { return new TTFEBrain(brain); }

		@Override
		protected TTFEBrain mutate() {
			TTFEBrain copy = clone();
			copy.brain().mutate(mutate);
			return copy;
		}

		@Override
		public void update(double dt) {}

		@Override
		protected float calcScore() {
			float sum = 0;
			for (int x = 0; x < size; x++) for (int y = 0; y < size; y++) {
				sum += fields[x][y].value / 100f;
			}
			return sum * sum;
		}
	}

	private class Field implements Cloneable {
		private int x, y;
		private int value;

		private float sx, sy;
		private boolean swiping;
		private int swipeDir;
		private Field swipingTo;

		public Field(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public Field(Field f) {
			this.x = f.x;
			this.y = f.y;
			this.value = f.value;

			this.sx = f.sx;
			this.sy = f.sy;
			this.swiping = f.swiping;
			this.swipeDir = f.swipeDir;
			if (swipingTo != null)
				this.swipingTo = new Field(swipingTo);
		}

		public void show() {
			noStroke();
			fill(getFieldColor(value));
			rect(x() * fW + 10, y() * fH + 10, fW - 20, fH - 20);

			if (value != 0) {
				stroke(0, 50);
				strokeWeight(4);
				fill(getTextColor(value));
				text(String.valueOf(value), x() * fW + fW / 2, y() * fH + fH / 2);
			}
		}

		public void swipe(int dir, Field pair) {
			sx = 0;
			sy = 0;
			swiping = true;
			swipeDir = dir;
			swipingTo = pair;

		}

		public void reset() {
			sx = 0;
			sy = 0;
			swiping = false;
		}

		private static final float speed = 0.2F;

		public void update() {
			boolean finished = false;
			switch (swipeDir) {
				case Constants.LEFT:
					sx -= speed;
					finished = x() <= swipingTo.x - speed / 2;
					break;
				case Constants.RIGHT:
					sx += speed;
					finished = x() >= swipingTo.x + speed / 2;
					break;
				case Constants.UP:
					sy -= speed;
					finished = y() <= swipingTo.y - speed / 2;
					break;
				case Constants.DOWN:
					sy += speed;
					finished = y() >= swipingTo.y + speed / 2;
					break;
			}
			if (finished) reset();

		}

		public float x() { return x + sx; }

		public float y() { return y + sy; }

		@Override
		public String toString() { return String.format("[%d][%d](%d)", x, y, value); }

		@Override
		protected Field clone() { return new Field(this); }
	}

}
