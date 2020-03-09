package eu.veldsoft.ellipses.image.approximator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ChromosomePair;
import org.apache.commons.math3.genetics.CrossoverPolicy;

class InstructionsCrossover implements CrossoverPolicy {
	@Override
	public ChromosomePair crossover(final Chromosome first,
			final Chromosome second) throws MathIllegalArgumentException {
		if (first instanceof EllipseListChromosome == false) {
			throw new IllegalArgumentException();
		}

		if (second instanceof EllipseListChromosome == false) {
			throw new IllegalArgumentException();
		}

		final List<Ellipse> parent1 = ((EllipseListChromosome) first)
				.getEllipses();
		final List<Ellipse> parent2 = ((EllipseListChromosome) second)
				.getEllipses();

		final List<Ellipse> child1 = new ArrayList<Ellipse>();
		final List<Ellipse> child2 = new ArrayList<Ellipse>();

		for (Ellipse ellipse : parent1) {
			if (Util.PRNG.nextBoolean() == true) {
				child1.add(ellipse);
			}
		}
		for (Ellipse ellipse : parent2) {
			if (Util.PRNG.nextBoolean() == true) {
				child1.add(ellipse);
			}
		}

		for (Ellipse ellipse : parent1) {
			if (Util.PRNG.nextBoolean() == true) {
				child2.add(ellipse);
			}
		}
		for (Ellipse ellipse : parent2) {
			if (Util.PRNG.nextBoolean() == true) {
				child2.add(ellipse);
			}
		}

		// TODO Shuffling may be is not needed, because sorting is done in
		// evaluation phase.
		// Collections.shuffle(child1);
		// Collections.shuffle(child2);

		return new ChromosomePair(
				((EllipseListChromosome) first)
						.newFixedLengthChromosome(child1),
				((EllipseListChromosome) second)
						.newFixedLengthChromosome(child2));
	}

}
