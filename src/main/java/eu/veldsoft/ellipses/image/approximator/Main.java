package eu.veldsoft.ellipses.image.approximator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private static Map<String, Integer> histogram = new HashMap<String, Integer>();
	private static Vector<Color> colors = new Vector<Color>();

	private static Population doGeneticAlgorithmOptimization(Population initial,
			double crossoverRate, double mutationRate, int tournamentArity,
			int time) {
		GeneticAlgorithm algorithm = new GeneticAlgorithm(
				new InstructionsCrossover(), crossoverRate,
				new RandomEllipsesMutation(original, histogram, colors),
				mutationRate, new TournamentSelection(tournamentArity));

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

		options.addOption(new Option("orientation_local_search", false,
				"Application of a local search for a better initial orientation of the ellipses (default value false)."));

		options.addOption(new Option("ga", false,
				"Switch on genetic algorithm optimization (default value off)."));

		options.addOption(new Option("aco", false,
				"Switch on ant colony optimization (default value off)."));

		options.addOption(new Option("euclidean", false,
				"Use of Euclidean distance for image similarity estimation (default value off)."));

		options.addOption(new Option("probabilistic", false,
				"Use of hierarchical probabilistic Euclidean distance for image similarity estimation (default value on)."));

		options.addOption(Option.builder("hierarchy_depth").argName("number")
				.hasArg().valueSeparator()
				.desc("Hierarchical depth in probabilistic Euclidean distance (default value 10).")
				.build());

		options.addOption(Option.builder("sample_size").argName("number")
				.hasArg().valueSeparator()
				.desc("Sample size percent (from 0.0 to 1.0) in probabilistic Euclidean distance (default value 0.05).")
				.build());

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

		options.addOption(new Option("g_code_comments", false,
				"Switch on comments in G Code file (off by default)."));

		options.addOption(Option.builder("g_code_x_home").argName("number")
				.hasArg().valueSeparator()
				.desc("X home of the drawing area (default value 0).").build());

		options.addOption(Option.builder("g_code_y_home").argName("number")
				.hasArg().valueSeparator()
				.desc("Y home of the drawing area (default value 0).").build());

		options.addOption(Option.builder("g_code_x_offset").argName("number")
				.hasArg().valueSeparator()
				.desc("X offset of the drawing area (default value 0).")
				.build());

		options.addOption(Option.builder("g_code_y_offset").argName("number")
				.hasArg().valueSeparator()
				.desc("Y offset of the drawing area (default value 0).")
				.build());

		options.addOption(Option.builder("g_code_z_down").argName("number")
				.hasArg().valueSeparator()
				.desc("Z down value (default value 0).").build());

		options.addOption(Option.builder("g_code_z_up").argName("number")
				.hasArg().valueSeparator().desc("Z up value (default value 0).")
				.build());

		options.addOption(Option.builder("g_code_width").argName("number")
				.hasArg().valueSeparator()
				.desc("Drawing canvas width in millimeters (default value 0.0).")
				.build());

		options.addOption(Option.builder("g_code_height").argName("number")
				.hasArg().valueSeparator()
				.desc("Drawing canvas height in millimeters (default value 0.0).")
				.build());

		options.addOption(Option.builder("g_code_refill").argName("number")
				.hasArg().valueSeparator()
				.desc("Paint refill time in seconds (default value 0.0).")
				.build());

		options.addOption(Option.builder("g_code_refill_count")
				.argName("number").hasArg().valueSeparator()
				.desc("Paint refill count (default value 1).").build());

		options.addOption(Option.builder("g_code_color_change")
				.argName("number").hasArg().valueSeparator()
				.desc("Color setup change time in seconds (default value 0).")
				.build());

		options.addOption(Option.builder("ga_population_size").argName("number")
				.hasArg().valueSeparator()
				.desc("Genetic algorithm population size (default value 0).")
				.build());

		options.addOption(Option.builder("ga_chromosome_mean").argName("number")
				.hasArg().valueSeparator()
				.desc("Genetic algorithm chromosome average size (default value depends of the image size and ellipse size).")
				.build());

		options.addOption(Option.builder("ga_chromosome_sd").argName("number")
				.hasArg().valueSeparator()
				.desc("Genetic algorithm chromosome size standard deviation (default value 1.0).")
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
				.desc("Genetic algorithm optimization time in seconds (default value 0). If a suffix is used 1h is one hour, 1m is one minute, 1s is one second, and 1ms is one millisecond.")
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

		/* Probabilistic Euclidean distance sample size. */
		double sampleSize = 0.05;
		if (commands.hasOption("sample_size") == true) {
			sampleSize = Double.valueOf(commands.getOptionValue("sample_size"));
		}

		/* Probabilistic Euclidean distance recursive depth. */
		int recursionDepth = 10;
		if (commands.hasOption("hierarchy_depth") == true) {
			recursionDepth = Integer
					.valueOf(commands.getOptionValue("hierarchy_depth"));
		}

		/*
		 * Switch on hierarchical probabilistic Euclidean distance for image
		 * similarity.
		 */
		if (commands.hasOption("probabilistic") == true) {
			EllipseListChromosome.IMAGE_COMPARATOR(
					new HierarchicalProbabilisticImageComparator(recursionDepth,
							sampleSize));
		}

		/* Switch on Euclidean distance for image similarity. */
		if (commands.hasOption("euclidean") == true) {
			EllipseListChromosome
					.IMAGE_COMPARATOR(new EuclideanImageComparator());
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

		/* Parse hexadecimal values for the colors.. */
		colors.clear();
		if (commands.hasOption("colors") == true) {
			String[] values = commands.getOptionValue("colors").split(",");
			for (String value : values) {
				colors.add(new Color(
						Integer.parseInt(value, 16) | Ellipse.ALPHA() << 24,
						true));
			}
		}

		/*
		 * Calculate the most used colors from the original picture.
		 */
		int pixels[] = original.getRGB(0, 0, original.getWidth(),
				original.getHeight(), null, 0, original.getWidth());
		for (int i = 0; i < pixels.length; i++) {
			Color color = Util.closestColor(pixels[i], colors);
			// TODO It is not clear that mapping to the closest color is the
			// correct way of histogram calculation.

			if (histogram.containsKey(color.toString()) == false) {
				histogram.put(color.toString(), 1);
			} else {
				histogram.put(color.toString(),
						histogram.get(color.toString()) + 1);
			}
		}
		EllipseListChromosome.COORDINATES_COLOR_COMPARATOR()
				.setHistogram(histogram);

		/* Associate output folder. */
		File output = null;
		if (commands.hasOption("output") == true) {
			output = new File(commands.getOptionValue("output"));
		} else {
			output = new File(".");
		}
		String path = output.getCanonicalPath() + "/";

		/* Set ellipse width. */
		Ellipse.WIDTH(1);
		if (commands.hasOption("ellipse_width") == true) {
			Ellipse.WIDTH(
					Integer.valueOf(commands.getOptionValue("ellipse_width")));
		}

		/* Set ellipse height. */
		Ellipse.HEIGHT(1);
		if (commands.hasOption("ellipse_height") == true) {
			Ellipse.HEIGHT(
					Integer.valueOf(commands.getOptionValue("ellipse_height")));
		}

		/* Ellipse alpha level. */
		Ellipse.ALPHA(0xFF);
		if (commands.hasOption("ellipse_alpha") == true) {
			Ellipse.ALPHA(
					Integer.valueOf(commands.getOptionValue("ellipse_alpha")));
		}

		/* Use pixels colors to estimate the most proper color of the set. */
		boolean pixelClosestColor = false;
		if (commands.hasOption("pixel_closest_color") == true) {
			pixelClosestColor = true;
		}

		/*
		 * Application of local search for a better initial orientation of the
		 * ellipse.
		 */
		boolean orientationLocalSearch = false;
		if (commands.hasOption("orientation_local_search") == true) {
			orientationLocalSearch = true;
		}

		/* Set population size for the genetic algorithm. */
		int gaPopulationSize = 0;
		if (commands.hasOption("ga_population_size") == true) {
			gaPopulationSize = Integer
					.valueOf(commands.getOptionValue("ga_population_size"));
		}

		/* Set chromosome average size for the genetic algorithm. */
		int gaChromosomeSizeMean = (int) Math
				.floor((original.getWidth() * original.getHeight())
						/ (Math.PI * Ellipse.WIDTH() * Ellipse.HEIGHT() / 4D));
		if (commands.hasOption("ga_chromosome_mean") == true) {
			gaChromosomeSizeMean = Integer
					.valueOf(commands.getOptionValue("ga_chromosome_mean"));
		}
		EllipseListChromosome.LENGTH_MEAN(gaChromosomeSizeMean);

		/* Set chromosome size standard deviation for the genetic algorithm. */
		double gaChromosomeSizeSd = 1;
		if (commands.hasOption("ga_chromosome_sd") == true) {
			gaChromosomeSizeSd = Double
					.valueOf(commands.getOptionValue("ga_chromosome_sd"));
		}
		EllipseListChromosome.LENGTH_SD(gaChromosomeSizeSd);

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

		/* Set genetic algorithm optimization time in seconds. */
		int gaOptimizationTime = 0;
		if (commands.hasOption("ga_optimization_time") == true) {
			double multiplier = 1;
			String value = commands.getOptionValue("ga_optimization_time");

			if (value.contains("ms") == true) {
				multiplier = 0.001;
				value = value.replace("ms", "");
			} else if (value.contains("s") == true) {
				multiplier = 1.0;
				value = value.replace("s", "");
			} else if (value.contains("m") == true) {
				multiplier = 60;
				value = value.replace("m", "");
			} else if (value.contains("h") == true) {
				multiplier = 360;
				value = value.replace("h", "");
			}

			gaOptimizationTime = (int) (Integer.valueOf(value) * multiplier);
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

		Settings settings = new GCode.Settings(false, 0, 0, 0, 0, 0, 0, 0.0,
				0.0, 0.0, 1, 0);

		/* G Code comments switch on. */
		if (commands.hasOption("g_code_comments") == true) {
			settings.comments = true;
		}

		/* Painting home by X axis. */
		if (commands.hasOption("g_code_x_home") == true) {
			settings.xHome = Double
					.valueOf(commands.getOptionValue("g_code_x_home"));
		}

		/* Painting home by Y axis. */
		if (commands.hasOption("g_code_y_home") == true) {
			settings.yHome = Double
					.valueOf(commands.getOptionValue("g_code_y_home"));
		}

		/* Painting offset by X axis. */
		if (commands.hasOption("g_code_x_offset") == true) {
			settings.xOffset = Double
					.valueOf(commands.getOptionValue("g_code_x_offset"));
		}

		/* Painting offset by Y axis. */
		if (commands.hasOption("g_code_y_offset") == true) {
			settings.yOffset = Double
					.valueOf(commands.getOptionValue("g_code_y_offset"));
		}

		/* Down value of Z axis. */
		if (commands.hasOption("g_code_z_down") == true) {
			settings.zDown = Integer
					.valueOf(commands.getOptionValue("g_code_z_down"));
		}

		/* Up value of Z axis. */
		if (commands.hasOption("g_code_z_up") == true) {
			settings.zUp = Integer
					.valueOf(commands.getOptionValue("g_code_z_up"));
		}

		/* Scaling factor between image width and canvas width. */
		if (commands.hasOption("g_code_width") == true) {
			settings.scaleWidth = Double
					.valueOf(commands.getOptionValue("g_code_width"))
					/ original.getWidth();
		}

		/* Scaling factor between image height and canvas height. */
		if (commands.hasOption("g_code_height") == true) {
			settings.scaleHeight = Double
					.valueOf(commands.getOptionValue("g_code_height"))
					/ original.getHeight();
		}

		/* Paint refill time in seconds. */
		if (commands.hasOption("g_code_refill") == true) {
			settings.penRefillTime = Double
					.valueOf(commands.getOptionValue("g_code_refill"));
		}

		/* Paint refill amount of times. */
		if (commands.hasOption("g_code_refill_count") == true) {
			settings.penRefillCount = Integer
					.valueOf(commands.getOptionValue("g_code_refill_count"));
		}

		/* Color change time in seconds. */
		if (commands.hasOption("g_code_color_change") == true) {
			settings.colorChangeTime = Integer
					.valueOf(commands.getOptionValue("g_code_color_change"));
		}

		Population initial = Util.randomInitialPopulation(original, histogram,
				colors, pixelClosestColor, orientationLocalSearch,
				gaPopulationSize, gaElitismRate);
		Population optimized = initial;

		/*
		 * Report initial best solution.
		 */
		Util.writeSolution(original.getWidth(), original.getHeight(),
				((EllipseListChromosome) optimized.getFittestChromosome())
						.getEllipses(),
				optimized.getFittestChromosome().getFitness(),
				path + System.currentTimeMillis() + ".svg");
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
			String gcode = ((EllipseListChromosome) optimized
					.getFittestChromosome()).toGCode(settings);

			long time = System.currentTimeMillis();
			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(path + time + ".cnc"));
			out.write((gcode + "\n").getBytes());
			out.close();

			int index = -1;
			int counter = 1;
			while ((index = gcode.indexOf("(G Code instructions for ")) != -1) {
				gcode = gcode.substring(index);
				int next = gcode.substring(1)
						.indexOf("(G Code instructions for ");

				String instructions = "";
				if (next == -1) {
					instructions = gcode;
				} else {
					instructions = gcode.substring(0, next);
				}

				String color = gcode.substring(gcode.indexOf(" color.)") - 6,
						gcode.indexOf(" color.)"));

				out = new BufferedOutputStream(new FileOutputStream(
						path + time + "-" + String.format("%03d", counter) + "-"
								+ color + ".cnc"));
				out.write((instructions + "\n").getBytes());
				out.close();

				if (next == -1) {
					gcode = "";
				} else {
					gcode = gcode.substring(next);
				}

				counter++;
			}
		}

		/*
		 * Report best found solution.
		 */
		Util.writeSolution(original.getWidth(), original.getHeight(),
				((EllipseListChromosome) optimized.getFittestChromosome())
						.getEllipses(),
				optimized.getFittestChromosome().getFitness(),
				path + System.currentTimeMillis() + ".png");
		System.out.println("Optimization end ...");
		System.out.write(
				("Fitness: " + optimized.getFittestChromosome().getFitness()
						+ "\n").getBytes());
	}
}
