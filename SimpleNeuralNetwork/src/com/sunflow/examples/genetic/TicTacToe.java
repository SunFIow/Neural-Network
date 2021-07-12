package com.sunflow.examples.genetic;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import com.sunflow.game.GameBase;
import com.sunflow.logging.LogManager;

public class TicTacToe extends GameBase {

	public static void main(String[] args) {
		new TicTacToe();
	}

	private static final char X = 'X', O = 'O', empty = ' ';

	private char[][] board;
	private char turn = X;

	private ArrayList<Point> avaiableSpots;

	private boolean gameOver;

	@Override
	public void setup() {
		createCanvas(600, 600);
		smooth();
		frameRate(60);

		textSize(width / 3);
		textAlign(CENTER, CENTER);

		board = new char[3][3];
		avaiableSpots = new ArrayList<>();
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				board[x][y] = empty;
				avaiableSpots.add(new Point(x, y));
			}
		}
		gameOver = false;
	}

//	@Override
//	protected void update() {}

	@Override
	public void draw() {
		background(200);
		strokeWeight(4);
		stroke(0);

		line(width / 3, 0, width / 3, height);
		line(width / 3 * 2, 0, width / 3 * 2, height);

		line(0, height / 3, width, height / 3);
		line(0, height / 3 * 2, width, height / 3 * 2);

		for (int x = 0; x < 3; x++) for (int y = 0; y < 3; y++)
			text("" + board[x][y], width / 6 + (width / 3 * x), height / 6 + (height / 3 * y));
	}

	private boolean checkBoard() {
		String state;
		boolean winner = false;
		for (int x = 0; x < 3; x++) {
			if (board[x][0] != empty && board[x][0] == board[x][1] && board[x][0] == board[x][2]) winner = true;
		}
		if (board[0][0] != empty && board[0][0] == board[1][1] && board[1][1] == board[2][2]) winner = true;
		if (board[0][2] != empty && board[0][2] == board[1][1] && board[1][1] == board[2][0]) winner = true;

		boolean full = true;
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				if (board[x][y] == empty) full = false;
			}
		}
		state = winner ? "" + turn : full ? "tie" : "ongoing";

		if (state != "ongoing") {
			gameOver = true;
			if (state == "tie") LogManager.info("It's a tie!");
			else LogManager.info("Player " + state + " won");
			return true;
		}
		return false;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (!gameOver) {
			int x = (int) map(e.getX(), 0, width, 0, 3);
			int y = (int) map(e.getY(), 0, height, 0, 3);
			if (board[x][y] == empty) {
				board[x][y] = turn;
				avaiableSpots.remove(new Point(x, y));
				if (checkBoard()) return;
				turn = turn == X ? O : X;
			}
			Point spot = avaiableSpots.get(random(avaiableSpots.size()));
			x = spot.x;
			y = spot.y;
			board[x][y] = turn;
			avaiableSpots.remove(spot);
			if (checkBoard()) return;
			turn = turn == X ? O : X;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == SPACE) {
			reset();
		}
	}
}
