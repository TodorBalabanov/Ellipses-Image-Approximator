package eu.veldsoft.ellipses.image.approximator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
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
	 * Print about information on the standard output.
	 */
	private static void printAbout() {
		System.out.println(
				"*******************************************************************************");
		System.out.println(
				"* Ellipses Image Approximator version 0.0.1                                   *");
		System.out.println(
				"* Copyrights (C) 2016-2020 Velbazhd Software LLC                              *");
		System.out.println(
				"*                                                                             *");
		System.out.println(
				"* developed by Todor Balabanov ( todor.balabanov@gmail.com )                  *");
		System.out.println(
				"* Sofia, Bulgaria                                                             *");
		System.out.println(
				"*                                                                             *");
		System.out.println(
				"* This software is partially supported by the Bulgarian Ministry of Education *");
		System.out.println(
				"* and Science (contract D01–205/23.11.2018) under the National Scientific     *");
		System.out.println(
				"* Program \"Information and Communication Technologies for a Single Digital    *");
		System.out.println(
				"* Market in Science, Education and Security (ICTinSES)\", approved by          *");
		System.out.println(
				"* DCM # 577/17.08.2018.                                                       *");
		System.out.println(
				"*                                                                             *");
		System.out.println(
				"* This program is free software: you can redistribute it and/or modify        *");
		System.out.println(
				"* it under the terms of the GNU General Public License as published by        *");
		System.out.println(
				"* the Free Software Foundation, either version 3 of the License, or           *");
		System.out.println(
				"* (at your option) any later version.                                         *");
		System.out.println(
				"*                                                                             *");
		System.out.println(
				"* This program is distributed in the hope that it will be useful,             *");
		System.out.println(
				"* but WITHOUT ANY WARRANTY; without even the implied warranty of              *");
		System.out.println(
				"* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *");
		System.out.println(
				"* GNU General Public License for more details.                                *");
		System.out.println(
				"*                                                                             *");
		System.out.println(
				"* You should have received a copy of the GNU General Public License           *");
		System.out.println(
				"* along with this program. If not, see <http://www.gnu.org/licenses/>.        *");
		System.out.println(
				"*                                                                             *");
		System.out.println(
				"*******************************************************************************");
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
		/* Handling command line arguments with a library. */
		Options options = new Options();

		options.addOption(new Option("help", false, "Help screen."));

		options.addOption(Option.builder("input").argName("file").hasArg()
				.valueSeparator().desc("Image path and file name.").build());

		options.addOption(Option.builder("output").argName("folder").hasArg()
				.valueSeparator()
				.desc("Output folder path (default value current folder).")
				.build());

		options.addOption(Option.builder("ellipse_width").argName("number")
				.hasArg().valueSeparator()
				.desc("Ellipse width (default value 1).").build());

		options.addOption(Option.builder("ellipse_heigth").argName("number")
				.hasArg().valueSeparator()
				.desc("Ellipse heigth (default value 1).").build());

		options.addOption(Option.builder("colors").argName("hex1,hex2,hex3,...")
				.hasArg().valueSeparator()
				.desc("Set of colors as comma separeated list of RGB hexadecimal numbers.")
				.build());

		/* Parse command line arguments. */
		CommandLineParser parser = new DefaultParser();
		CommandLine commands = parser.parse(options, args);

		/* If help is requested print it and quit the program. */
		if (commands.hasOption("help") == true) {
			printAbout();
			System.out.println();
			(new HelpFormatter()).printHelp(
					"java -jar Ellipses-Image-Approximator-all.jar", options,
					true);
			System.out.println();
			System.exit(0);
		}

		/* Associate input file. */
		File input = null;
		if (commands.hasOption("input") == true) {
			input = new File(commands.getOptionValue("input"));
		} else {
			System.out.println("Input file name is missing!");
			System.out.println();
			(new HelpFormatter()).printHelp(
					"java -jar Ellipses-Image-Approximator-all.jar", options,
					true);
			System.out.println();
			System.exit(0);
		}

		/* Associate output folder. */
		File output = null;
		if (commands.hasOption("output") == true) {
			output = new File(commands.getOptionValue("output"));
		} else {
			output = new File(".");
		}
		String path = output.getCanonicalPath() + "/";

		/* Set ellipse width. */
		if (commands.hasOption("ellipse_width") == true) {
			Ellipse.WIDTH = Integer
					.valueOf(commands.getOptionValue("ellipse_width"));
		} else {
			Ellipse.WIDTH = 1;
		}

		/* Set ellipse height. */
		if (commands.hasOption("ellipse_height") == true) {
			Ellipse.HEIGHT = Integer
					.valueOf(commands.getOptionValue("ellipse_height"));
		} else {
			Ellipse.HEIGHT = 1;
		}

		// TODO Ellipse alpha value should be input argument.

		/* Parse hexadecimal values for the colors.. */
		colors.clear();
		if (commands.hasOption("colors") == true) {
			String[] values = commands.getOptionValue("colors").split(",");
			for (String value : values) {
				colors.add(new Color(
						Integer.parseInt(value, 16) | Util.ELLIPSES_ALPHA << 24,
						true));
			}
		}

		/* Read input image. */
		original = ImageIO.read(input);

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
