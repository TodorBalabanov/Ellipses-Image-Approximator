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

import eu.veldsoft.ellipses.image.approximator.GCode.Settings;

public class Main {
	private static BufferedImage original = null;
	private static Vector<Color> colors = new Vector<Color>();

	private static Population doGeneticAlgorithmOptimization(Population initial,
			double crossoverRate, double mutationRate, int tournamentArity,
			int time) {
		GeneticAlgorithm algorithm = new GeneticAlgorithm(
				new InstructionsCrossover(), crossoverRate,
				new RandomEllipsesMutation(original, colors), mutationRate,
				new TournamentSelection(tournamentArity));

		Population optimized = algorithm.evolve(initial,
				new FixedElapsedTime(time));

		return optimized;
	}

	private static void doAntColonyOptimization(List<Ellipse> ellipses,
			int antsAmount, int repetitions, int iterations) {
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
		for (int i = 0; i < repetitions; i++) {
			graph.resetTau();
			AntColony4EIA colony = new AntColony4EIA(graph, antsAmount,
					iterations);
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

		options.addOption(new Option("pixel_closest_color", false,
				"Use RGB values of the pixels to match the best color from the set during the initialization of the population (default value false)."));

		options.addOption(new Option("ga", false,
				"Switch on genetic algorithm optimization (default value off)."));

		options.addOption(new Option("aco", false,
				"Switch on ant colony optimization (default value off)."));

		options.addOption(new Option("g_code_print", false,
				"Switch on G Code generation (default value off)."));

		options.addOption(Option.builder("input").argName("file").hasArg()
				.valueSeparator().desc("Image path and file name.").build());

		options.addOption(Option.builder("output").argName("folder").hasArg()
				.valueSeparator()
				.desc("Output folder path (default value current folder).")
				.build());

		options.addOption(Option.builder("ellipse_width").argName("number")
				.hasArg().valueSeparator()
				.desc("Ellipse width (default value 1).").build());

		options.addOption(Option.builder("ellipse_height").argName("number")
				.hasArg().valueSeparator()
				.desc("Ellipse height (default value 1).").build());

		options.addOption(Option.builder("ellipse_alpha").argName("number")
				.hasArg().valueSeparator()
				.desc("Ellipse alpha channel value between 0 and 255 (default value 255).")
				.build());

		options.addOption(Option.builder("colors").argName("hex1,hex2,hex3,...")
				.hasArg().valueSeparator()
				.desc("Set of colors as comma separeated list of RGB hexadecimal numbers.")
				.build());

		options.addOption(Option.builder("g_code_x_offset").argName("number").hasArg()
				.valueSeparator()
				.desc("X offset of the drawing area (default value 0).")
				.build());

		options.addOption(Option.builder("g_code_y_offset").argName("number").hasArg()
				.valueSeparator()
				.desc("Y offset of the drawing area (default value 0).")
				.build());

		options.addOption(Option.builder("g_code_x_down").argName("number").hasArg()
				.valueSeparator()
				.desc("Z down value (default value 0).")
				.build());

		options.addOption(Option.builder("g_code_x_up").argName("number").hasArg()
				.valueSeparator()
				.desc("Z up value (default value 0).")
				.build());

		options.addOption(Option.builder("g_code_scaling").argName("number").hasArg()
				.valueSeparator()
				.desc("Scaling factor image pixels to drawing area millimeters (default value 1.0).")
				.build());

		options.addOption(Option.builder("g_code_refill").argName("number")
				.hasArg().valueSeparator()
				.desc("Paint refill time in seconds (default value 0.0).")
				.build());

		options.addOption(Option.builder("g_code_color_change").argName("number")
				.hasArg().valueSeparator()
				.desc("Color setup change time in seconds (default value 0).")
				.build());

		options.addOption(Option.builder("ga_population_size").argName("number")
				.hasArg().valueSeparator()
				.desc("Genetic algorithm population size (default value 0).")
				.build());

		options.addOption(Option.builder("ga_chromosome_size").argName("number")
				.hasArg().valueSeparator()
				.desc("Genetic algorithm chromosome average size (default value depends of the image size and ellipse size).")
				.build());

		options.addOption(Option.builder("ga_tournament_arity")
				.argName("number").hasArg().valueSeparator()
				.desc("Genetic algorithm tournament selection arity (default value 2).")
				.build());

		options.addOption(Option.builder("ga_crossover_rate").argName("number")
				.hasArg().valueSeparator()
				.desc("Genetic algorithm crossover rate from 0.0. to 1.0 (default value 0.9).")
				.build());

		options.addOption(Option.builder("ga_mutation_rate").argName("number")
				.hasArg().valueSeparator()
				.desc("Genetic algorithm mutation rate from 0.0. to 1.0 (default value 0.01).")
				.build());

		options.addOption(Option.builder("ga_elitism_rate").argName("number")
				.hasArg().valueSeparator()
				.desc("Genetic algorithm elitism rate from 0.0. to 1.0 (default value one individual).")
				.build());

		options.addOption(Option.builder("ga_optimization_time")
				.argName("number").hasArg().valueSeparator()
				.desc("Genetic algorithm optimization time in seconds (default value 0).")
				.build());

		options.addOption(Option.builder("aco_ants_amount").argName("number")
				.hasArg().valueSeparator()
				.desc("Ant colony optimization number of ants (default value 0).")
				.build());

		options.addOption(Option.builder("aco_repetitions").argName("number")
				.hasArg().valueSeparator()
				.desc("Ant colony optimization number optimization repetitions (default value 0).")
				.build());

		options.addOption(Option.builder("aco_iterations").argName("number")
				.hasArg().valueSeparator()
				.desc("Ant colony optimization number iterations in single repetition (default value 0).")
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

		/* Switch on genetic algorithm optimization. */
		boolean useGeneticAlgorithmOptimization = false;
		if (commands.hasOption("ga") == true) {
			useGeneticAlgorithmOptimization = true;
		}

		/* Switch on ant colony optimization. */
		boolean useAntColonyOptimization = false;
		if (commands.hasOption("aco") == true) {
			useAntColonyOptimization = true;
		}

		/* Switch on ant colony optimization. */
		boolean gCodeOutput = false;
		if (commands.hasOption("g_code_print") == true) {
			gCodeOutput = true;
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

		/* Read input image. */
		original = ImageIO.read(input);

		/* Associate output folder. */
		File output = null;
		if (commands.hasOption("output") == true) {
			output = new File(commands.getOptionValue("output"));
		} else {
			output = new File(".");
		}
		String path = output.getCanonicalPath() + "/";

		/* Set ellipse width. */
		Ellipse.WIDTH = 1;
		if (commands.hasOption("ellipse_width") == true) {
			Ellipse.WIDTH = Integer
					.valueOf(commands.getOptionValue("ellipse_width"));
		}

		/* Set ellipse height. */
		Ellipse.HEIGHT = 1;
		if (commands.hasOption("ellipse_height") == true) {
			Ellipse.HEIGHT = Integer
					.valueOf(commands.getOptionValue("ellipse_height"));
		}

		/* Ellipse alpha level. */
		Ellipse.ALPHA = 0xFF;
		if (commands.hasOption("ellipse_alpha") == true) {
			Ellipse.ALPHA = Integer
					.valueOf(commands.getOptionValue("ellipse_alpha"));
		}

		/* Parse hexadecimal values for the colors.. */
		colors.clear();
		if (commands.hasOption("colors") == true) {
			String[] values = commands.getOptionValue("colors").split(",");
			for (String value : values) {
				colors.add(new Color(
						Integer.parseInt(value, 16) | Ellipse.ALPHA << 24,
						true));
			}
		}

		/* Use pixels colors to estimate the most proper color of the set. */
		boolean pixelClosestColor = false;
		if (commands.hasOption("-pixel_closest_color") == true) {
			pixelClosestColor = true;
		}

		/* Set population size for the genetic algorithm. */
		int gaPopulationSize = 0;
		if (commands.hasOption("ga_population_size") == true) {
			gaPopulationSize = Integer
					.valueOf(commands.getOptionValue("ga_population_size"));
		}

		/* Set chromosome average size for the genetic algorithm. */
		int gaChromosomeAverageSize = (original.getWidth()
				* original.getHeight()) / (Ellipse.WIDTH * Ellipse.HEIGHT);
		if (commands.hasOption("ga_chromosome_size") == true) {
			gaPopulationSize = Integer
					.valueOf(commands.getOptionValue("ga_chromosome_size"));
		}

		/* Set genetic algorithm tournament selection arity. */
		int gaTournamentArity = 2;
		if (commands.hasOption("ga_tournament_arity") == true) {
			gaTournamentArity = Integer
					.valueOf(commands.getOptionValue("ga_tournament_arity"));
		}

		/* Set genetic algorithm crossover rate. */
		double gaCrossoverRate = 0.9;
		if (commands.hasOption("ga_crossover_rate") == true) {
			gaCrossoverRate = Double
					.valueOf(commands.getOptionValue("ga_crossover_rate"));
		}

		/* Set genetic algorithm mutation rate. */
		double gaMutationRate = 0.01;
		if (commands.hasOption("ga_mutation_rate") == true) {
			gaMutationRate = Double
					.valueOf(commands.getOptionValue("ga_mutation_rate"));
		}

		/* Set genetic algorithm elitism rate. */
		double gaElitismRate = 1.0 / gaPopulationSize;
		if (commands.hasOption("ga_elitism_rate") == true) {
			gaElitismRate = Double
					.valueOf(commands.getOptionValue("ga_elitism_rate"));
		}

		/* Set genetic algorithm optimization time. */
		int gaOptimizationTime = 0;
		if (commands.hasOption("ga_optimization_time") == true) {
			gaOptimizationTime = Integer
					.valueOf(commands.getOptionValue("ga_optimization_time"));
		}

		/* Number of ants in the graph. */
		int acoAntsAmount = 0;
		if (commands.hasOption("aco_ants_amount") == true) {
			acoAntsAmount = Integer
					.valueOf(commands.getOptionValue("aco_ants_amount"));
		}

		/* How many times the optimization to be executed. */
		int acoRepetitions = 0;
		if (commands.hasOption("aco_repetitions") == true) {
			acoRepetitions = Integer
					.valueOf(commands.getOptionValue("aco_repetitions"));
		}

		/* Number of iterations during single execution of the optimization. */
		int acoIterations = 0;
		if (commands.hasOption("aco_iterations") == true) {
			acoIterations = Integer
					.valueOf(commands.getOptionValue("aco_iterations"));
		}

		Settings settings = new GCode.Settings(0, 0, 0, 0, 0.0, 0.0, 0);

		/* Painting offset by X axis. */
		if (commands.hasOption("g_code_x_offset") == true) {
			settings.xOffset = Integer
					.valueOf(commands.getOptionValue("g_code_x_offset"));
		}

		/* Painting offset by Y axis. */
		if (commands.hasOption("g_code_y_offset") == true) {
			settings.yOffset = Integer
					.valueOf(commands.getOptionValue("g_code_y_offset"));
		}

		/* Down value of Z axis. */
		if (commands.hasOption("g_code_z_down") == true) {
			settings.yOffset = Integer
					.valueOf(commands.getOptionValue("g_code_z_down"));
		}

		/* Up value of Z axis. */
		if (commands.hasOption("g_code_z_up") == true) {
			settings.yOffset = Integer
					.valueOf(commands.getOptionValue("g_code_z_up"));
		}

		/* Scaling factor between image size and painting area. */
		if (commands.hasOption("g_code_scaling") == true) {
			settings.penRefillTime = Double
					.valueOf(commands.getOptionValue("g_code_scaling"));
		}

		/* Paint refill time in seconds. */
		if (commands.hasOption("g_code_refill") == true) {
			settings.penRefillTime = Double
					.valueOf(commands.getOptionValue("g_code_refill_time"));
		}

		/* Color change time in seconds. */
		if (commands.hasOption("g_code_color_change") == true) {
			settings.colorChangeTime = Integer
					.valueOf(commands.getOptionValue("g_code_color_change"));
		}

		EllipseListChromosome.AVERAGE_LENGTH(gaChromosomeAverageSize);
		Population initial = Util.randomInitialPopulation(original, colors,
				pixelClosestColor, gaPopulationSize, gaElitismRate);
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

		if (useGeneticAlgorithmOptimization == true) {
			optimized = doGeneticAlgorithmOptimization(initial, gaCrossoverRate,
					gaMutationRate, gaTournamentArity, gaOptimizationTime);
		}

		if (useAntColonyOptimization == true) {
			doAntColonyOptimization(
					((EllipseListChromosome) optimized.getFittestChromosome())
							.getEllipses(),
					acoAntsAmount, acoRepetitions, acoIterations);
		}

		/*
		 * Print plotting instructions.
		 */
		if (gCodeOutput == true) {
			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(
							path + System.currentTimeMillis() + ".cnc"));
			out.write(((EllipseListChromosome) optimized.getFittestChromosome())
					.toGCode(settings).getBytes());
			out.close();
		}

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
