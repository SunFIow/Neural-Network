package com.sunflow.simpleneuralnetwork;

import java.util.ArrayList;

import com.sunflow.util.GameUtils;

public class Population<Type extends Creature<Type>> implements GameUtils {

	public int totalPopulation;
	private int totalPopulationOnLoaded;
	// All active birds (not yet collided with pipe)
	private ArrayList<Type> activeCreatures;
	// All birds for any given population
	private ArrayList<Type> allCreatures;

	private Type bestCreature;
	public int generation = 1;

	private MethodNewCreature<Type> newCreatureM;
	@SuppressWarnings("unused")
	private MethodReset resetM;

	public Population(int totalPopulation, MethodNewCreature<Type> nC) {
		this.totalPopulation = totalPopulation;
		this.totalPopulationOnLoaded = totalPopulation;
		this.newCreatureM = nC;

		activeCreatures = new ArrayList<>();
		allCreatures = new ArrayList<>();

		// Create a population
		for (int i = 0; i < totalPopulation; i++) {
			Type creature = getCreature();
			activeCreatures.add(creature);
			allCreatures.add(creature);
		}
	}

	public Population(int total, MethodNewCreature<Type> nC, MethodReset resetM) {
		this(total, nC);
		this.resetM = resetM;
	}

	public static interface MethodNewCreature<Type> {
		Type invoke();
	}

	public static interface MethodReset {
		void invoke();
	}

	protected Type getCreature() {
		return newCreatureM.invoke();
	}

//	protected Type getCreature() {
//		return null;
//	}

	@SuppressWarnings("unchecked")
	public void nextGeneration() {
		// Normalize the fitness values 0-1
		calcScores(allCreatures);
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
		newCreatures.add(bestCreature.clone());
		for (int i = 1; i < totalPopulation; i++) {
			// Select a bird based on fitness
			Type creature = poolSelection(oldCreatures);
			newCreatures.add(creature);
		}
		return newCreatures;
	}

	private void calcScores(ArrayList<Type> creatures) {
		for (int i = 0; i < creatures.size(); i++) {
			creatures.get(i).calcScore();
		}
	}

	private void normalizeFitness(ArrayList<Type> creatures) {
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
		double r = random(1.0D);

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
		return creatures.get(index).mutate();
	}

//	public ArrayList<Type> getCreatures() {
//		return activeCreatures;
//	}

	public Type get(int i) {
		return activeCreatures.get(i);
	}

	public void addCreature() {
		add(getCreature());
	}

	public void add(Type creature) {
		activeCreatures.add(creature);
	}

	public void remove(int i) {
		activeCreatures.remove(i);
	}

	public void remove(Type creature) {
		activeCreatures.remove(creature);
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

	@SuppressWarnings("unchecked")
	public void populateOf(Creature<Type> creature) {
		// Generate a new set of birds
		ArrayList<Type> newCreatures = new ArrayList<>();
		newCreatures.add(creature.clone());
		for (int i = 1; i < totalPopulationOnLoaded; i++) {
			// Select a bird based on fitness
			newCreatures.add(creature.mutate());
		}
		activeCreatures = newCreatures;
		// Copy those birds to another array
		allCreatures = (ArrayList<Type>) activeCreatures.clone();
		generation = 1;
	}
}
