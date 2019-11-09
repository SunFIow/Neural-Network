package com.sunflow;

import java.awt.Graphics2D;

import com.sunflow.game.Game2D;
import com.sunflow.game.PConstants;

public class Test extends Game2D {
	public static void main(String[] args) {
		new Test();
	}

	@Override
	protected void setup() {
		createCanvas(400, 400);
		smooth();

	}

	@Override
	protected void render(Graphics2D g) {
		background(150);

		fill(200);
		stroke(100);
//		strokeWeight(5);
		ellipse(50, 150, 20, 50);
		rect(100, 40, 16.5F, 66.6F);

		strokeWeight(2);
		stroke(155, 50, 135, 150);
		line(0, 0, width, height);

		strokeWeight(20);
		point(width / 2, height / 2);

		textSize(32);
		strokeWeight(8);
//		textFont(createDefaultFont());
//		textFont(font);

		textO("Test", width / 2F - 100, height / 2F);
		textAlign(PConstants.CENTER, PConstants.CENTER);
		text("Fgg", width / 2F + 100, height / 2F);

		stroke(100, 150);
		strokeWeight(2);
		line(0, height / 2F, width, height / 2F);

		line(width / 2F - 100, 0, width / 2F - 100, height);
		line(width / 2F + 100, 0, width / 2F + 100, height);

	}
}
