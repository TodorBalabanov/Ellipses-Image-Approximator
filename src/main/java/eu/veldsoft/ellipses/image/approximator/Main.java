package eu.veldsoft.ellipses.image.approximator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.commons.math3.genetics.FixedElapsedTime;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.TournamentSelection;

import com.ugos.acs.AntGraph;

public class Main {
	private static BufferedImage original = null;
	private static Vector<Color> colors = new Vector<Color>();

	private static Population doGeneticAlgorithmOptimization(Population initial,
			int time) {
		GeneticAlgorithm algorithm = new GeneticAlgorithm(
				new InstructionsCrossover(), Util.CROSSOVER_RATE,
				new RandomEllipsesMutation(original, colors),
				Util.MUTATION_RATE,
				new TournamentSelection(Util.TOURNAMENT_ARITY));

		Population optimized = algorithm.evolve(initial,
				new FixedElapsedTime(time));

		return optimized;
	}

	private static void doAntColonyOptimization(List<Ellipse> ellipses) {
		/*
		 * For ant colony graph.
		 */
		double neighbours[][] = new double[ellipses.size()][ellipses.size()];
		for (int i = 0; i < ellipses.size(); i++) {
			for (int j = 0; j < ellipses.size(); j++) {
				/*
				 * Node will not be connected to itself.
				 */
				if (i == j) {
					neighbours[i][j] = 0;
					continue;
				}

				// TODO Maybe other distance formula should be used.
				neighbours[i][j] = Math.abs(ellipses.get(i).color.getRGB()
						- ellipses.get(j).color.getRGB());
				neighbours[j][i] = neighbours[i][j];
			}
		}
		AntGraph graph = new AntGraph(ellipses.size(), neighbours);

		/*
		 * Run ant colony optimization.
		 */
		for (int i = 0; i < Util.NUMBER_OF_REPETITIONS; i++) {
			graph.resetTau();
			AntColony4EIA colony = new AntColony4EIA(graph, Util.NUMBER_OF_ANTS,
					Util.NUMBER_OF_ITERATIONS);
			colony.start();
		}
	}

	/**
	 * Single entry point of the program.
	 * 
	 * @param args
	 *            Command line parameters.
	 * @throws Exception
	 *             Exception program stop.
	 */
	public static void main(String[] args) throws Exception {
		File input = new File(args[0]);
		File output = new File(args[1]);
		String path = output.getCanonicalPath() + "/";

		original = ImageIO.read(input);

		Ellipse.WIDTH = Integer.valueOf(args[4]);
		Ellipse.HEIGHT = Integer.valueOf(args[5]);

		colors.clear();
		for (int i = 6; i < args.length; i++) {
			colors.add(new Color(
					Integer.parseInt(args[i], 16) | Util.ELLIPSES_ALPHA << 24,
					true));
		}

		// TODO Should be some kind of external parameter. The area of the image
		// divided by the area of the bounding rectangle of the simple graphic
		// primitive is a good starting point.
		EllipseListChromosome
				.AVERAGE_LENGTH((original.getWidth() * original.getHeight())
						/ (Ellipse.WIDTH * Ellipse.HEIGHT));

		Population initial = Util.randomInitialPopulation(original, colors,
				Integer.valueOf(args[2]));
		Population optimized = initial;

		/*
		 * Report initial best solution.
		 */
		Util.writeSolution(original,
				((EllipseListChromosome) initial.getFittestChromosome())
						.getSortedEllipses(),
				path + System.currentTimeMillis() + ".png");
		System.out.println("Optimization start ...");
		System.out.write(
				("Fitness: " + initial.getFittestChromosome().getFitness()
						+ "\n").getBytes());

		boolean useGeneticAlgorithmOptimization = true;
		if (useGeneticAlgorithmOptimization == true) {
			optimized = doGeneticAlgorithmOptimization(initial,
					Integer.valueOf(args[3]));
		}

		boolean useAntColonyOptimization = false;
		if (useAntColonyOptimization == true) {
			doAntColonyOptimization(
					((EllipseListChromosome) optimized.getFittestChromosome())
							.getEllipses());
		}

		/*
		 * Print plotting instructions.
		 */
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(
						path + System.currentTimeMillis() + ".cnc"));
		// TODO Test G Code generation with working area 200x200 millimeters.
		out.write(
				((EllipseListChromosome) optimized.getFittestChromosome())
						.toGCode(new GCode.Settings(30, 30, 35, 15,
								200.0 / Math.max(original.getWidth(),
										original.getHeight()),
								0.5, 600))
						.getBytes());
		out.close();

		/*
		 * Report best found solution.
		 */
		Util.writeSolution(original,
				((EllipseListChromosome) optimized.getFittestChromosome())
						.getSortedEllipses(),
				path + System.currentTimeMillis() + ".png");
		System.out.println("Optimization end ...");
		System.out.write(
				("Fitness: " + optimized.getFittestChromosome().getFitness()
						+ "\n").getBytes());
	}
}
