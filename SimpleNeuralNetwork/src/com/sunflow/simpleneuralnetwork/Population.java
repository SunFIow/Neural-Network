package com.sunflow.simpleneuralnetwork;

import java.util.ArrayList;

import com.sunflow.util.Utils;

public abstract class Population<Type extends Creature<Type>> {

//	private int totalPopulation;
	// All active birds (not yet collided with pipe)
	private ArrayList<Type> activeCreatures;
	// All birds for any given population
	private ArrayList<Type> allCreatures;

	private Type bestCreature;
	private int generation = 1;

	public Population(int totalPopulation) {
//		this.totalPopulation = totalPopulation;

		activeCreatures = new ArrayList<>();
		allCreatures = new ArrayList<>();

		// Create a population
		for (int i = 0; i < totalPopulation; i++) {
			Type creature = getCreature();
			activeCreatures.add(creature);
			allCreatures.add(creature);
		}
	}

	protected abstract Type getCreature();

	@SuppressWarnings("unchecked")
	public void nextGeneration() {
		// Normalize the fitness values 0-1
		normalizeFitness(allCreatures);
		// Generate a new set of birds
		activeCreatures = generate(allCreatures);
		// Copy those birds to another array
		allCreatures = (ArrayList<Type>) activeCreatures.clone();
		generation = generation() + 1;
	}

	private ArrayList<Type> generate(ArrayList<Type> oldCreatures) {
		ArrayList<Type> newCreatures = new ArrayList<>();
		for (int i = 0; i < oldCreatures.size(); i++) {
			// Select a bird based on fitness
			Type creature = poolSelection(oldCreatures);
			newCreatures.add(creature);
		}
		return newCreatures;
	}

	private void normalizeFitness(ArrayList<Type> creatures) {
		// Make score exponentially better?
		for (int i = 0; i < creatures.size(); i++) {
			creatures.get(i).score = Math.pow(creatures.get(i).score, 2);
		}

		// Add up all the scores
		double sum = 0;
		for (int i = 0; i < creatures.size(); i++) {
			sum += creatures.get(i).score;
		}
		// Divide by the sum
		for (int i = 0; i < creatures.size(); i++) {
			creatures.get(i).fitness = creatures.get(i).score / sum;
		}
	}

	private Type poolSelection(ArrayList<Type> creatures) {
		// Start at 0
		int index = 0;

		// Pick a random number between 0 and 1
		double r = Utils.random(1.0D);

		// Keep subtracting probabilities until you get less than zero
		// Higher probabilities will be more likely to be fixed since they will
		// subtract a larger number towards zero
		while (r > 0.0D) {
			r -= creatures.get(index).fitness;
			// And move on to the next
			index += 1;
		}

		// Go back one
		index -= 1;

		// Make sure it's a copy!
		// (this includes mutation)
		return creatures.get(index).clone();
	}

//	public ArrayList<Type> getCreatures() {
//		return activeCreatures;
//	}

	public Type getCreature(int i) {
		return activeCreatures.get(i);
	}

	public void removeCreatures(int i) {
		activeCreatures.remove(i);
	}

	public int getActiveSize() {
		return activeCreatures.size();
	}

	public Type bestCreature() {
		return bestCreature;
	}

	public void setBestCreature(Type bestCreature) {
		this.bestCreature = bestCreature;
	}

	public int generation() {
		return generation;
	}
}