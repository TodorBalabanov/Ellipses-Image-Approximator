package eu.veldsoft.ellipses.image.approximator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.commons.math3.genetics.FixedElapsedTime;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.TournamentSelection;

public class Main {
	private static BufferedImage original = null;

	/**
	 * java Main <image file name> <population size> <number of evolutions>
	 * <primitive width> <primitive height> <list of colors>
	 * 
	 * Example: java Main ./dat/0001.jpg 31 100 3 9 000000 808080 C0C0C0 FFFFFF
	 * 800000 FF0000 808000 FFFF00 008000 00FF00 008080 00FFFF 000080 0000FF
	 * 800080 FF00FF
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		File input = new File(args[0]);
		String path = input.getCanonicalPath().substring(0,
				input.getCanonicalPath().length() - input.getName().length());

		original = ImageIO.read(input);

		Ellipse.width = Integer.valueOf(args[3]);
		Ellipse.height = Integer.valueOf(args[4]);

		Vector<Color> colors = new Vector<Color>();
		for (int i = 5; i < args.length; i++) {
			colors.add(new Color(Integer.parseInt(args[i], 16)
					| Util.ELLIPSES_ALPHA << 24, true));
		}

		Population initial = Util.randomInitialPopulation(original, colors);

		/*
		 * Report initial best solution.
		 */
		Util.writeSolution(original, ((EllipseListChromosome) initial
				.getFittestChromosome()).getEllipses(),
				path + System.currentTimeMillis() + ".png");
		System.out.println("Optimization start ...");
		System.out.write(("Fitness: "
				+ initial.getFittestChromosome().getFitness() + "\n")
				.getBytes());

		// TODO Crossover is for chromosomes with different length.
		GeneticAlgorithm algorithm = new GeneticAlgorithm(
				new InstructionsCrossover(), Util.CROSSOVER_RATE,
				new RandomEllipsesMutation(original, colors),
				Util.MUTATION_RATE, new TournamentSelection(
						Util.TOURNAMENT_ARITY));
		Population optimized = algorithm.evolve(initial, new FixedElapsedTime(
				Util.OPTIMIZATION_TIMEOUT_SECONDS));

		/*
		 * Print plotting instructions.
		 */
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(path + System.currentTimeMillis() + ".txt"));
		out.write(("Fitness: " + optimized.getFittestChromosome().getFitness() + "\n")
				.getBytes());
		out.write(((EllipseListChromosome) optimized.getFittestChromosome())
				.getEllipses().toString().getBytes());
		out.close();

		/*
		 * Report result.
		 */
		Util.writeSolution(original, ((EllipseListChromosome) optimized
				.getFittestChromosome()).getEllipses(),
				path + System.currentTimeMillis() + ".png");
		System.out.println("Optimization end ...");
		System.out.write(("Fitness: "
				+ optimized.getFittestChromosome().getFitness() + "\n")
				.getBytes());
	}
}
