package eu.veldsoft.ellipses.image.approximator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.commons.math3.genetics.AbstractListChromosome;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.genetics.FixedElapsedTime;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.InvalidRepresentationException;
import org.apache.commons.math3.genetics.MutationPolicy;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.TournamentSelection;
import org.apache.commons.math3.genetics.UniformCrossover;

public class Main {
	private static class EllipseListChromosome extends
			AbstractListChromosome<Ellipse> {
		private BufferedImage image = null;

		public EllipseListChromosome(Ellipse[] representation,
				BufferedImage image) throws InvalidRepresentationException {
			super(representation);
		}

		public EllipseListChromosome(List<Ellipse> representation,
				BufferedImage image) throws InvalidRepresentationException {
			super(representation);
		}

		public EllipseListChromosome(List<Ellipse> representation,
				boolean copy, BufferedImage image)
				throws InvalidRepresentationException {
			super(representation, copy);
		}

		public void setImage(BufferedImage image) {
			this.image = image;
		}

		@Override
		public double fitness() {
			/*
			 * Calculate the most used colors from the original picture.
			 */
			Map<Color, Integer> histogram = new HashMap<Color, Integer>();
			int pixels[] = image.getRGB(0, 0, image.getWidth(),
					image.getHeight(), null, 0, image.getWidth());
			for (int i = 0; i < pixels.length; i++) {
				Color color = new Color(pixels[i]);

				if (histogram.containsKey(color) == false) {
					histogram.put(color, 1);
				} else {
					histogram.put(color, histogram.get(color) + 1);
				}
			}

			// TODO Implement image similarity estimation by using void
			// evaluate() function.

			// return similarity(getRepresentation());

			return 0;
		}

		@Override
		protected void checkValidity(List<Ellipse> list)
				throws InvalidRepresentationException {
		}

		@Override
		public EllipseListChromosome newFixedLengthChromosome(List<Ellipse> list) {
			// TODO Make a deep copy.
			return new EllipseListChromosome(list, true, image);
		}

		public List<Ellipse> getEllipses() {
			return getRepresentation();
		}

		public Ellipse getRandomElement() {
			return getRepresentation().get(
					Util.PRNG.nextInt(getRepresentation().size()));
		}

	}

	private static class RandomEllipsesMutation implements MutationPolicy {
		private BufferedImage image;

		public RandomEllipsesMutation(BufferedImage image) {
			this.image = image;
		}

		@Override
		public Chromosome mutate(Chromosome original) {
			if (!(original instanceof EllipseListChromosome)) {
				throw new IllegalArgumentException();
			}

			double factor = Util.PRNG.nextDouble();

			List<Ellipse> values = new ArrayList<Ellipse>();
			for (Ellipse value : ((EllipseListChromosome) original)
					.getEllipses()) {
				int dx = (int) (value.width * factor);
				int dy = (int) (value.height * factor);
				double theta = 2 * Math.PI * Util.PRNG.nextDouble();

				Ellipse ellipse = new Ellipse(value);

				/*
				 * Mutate color in some cases by taking color of other ellipse.
				 */
				if (Util.PRNG.nextDouble() < factor) {
					ellipse.color = ((EllipseListChromosome) original)
							.getRandomElement().color;
				}

				/*
				 * Mutate positions.
				 */
				if (Util.PRNG.nextBoolean() == true) {
					ellipse.x1 -= dx;
				} else {
					ellipse.x1 += dx;
				}
				if (Util.PRNG.nextBoolean() == true) {
					ellipse.y1 -= dy;
				} else {
					ellipse.y1 += dy;
				}

				/*
				 * Mutate rotation.
				 */
				ellipse.setup((int) ((ellipse.x1 + ellipse.x2) / 2.0),
						(int) ((ellipse.y1 + ellipse.y2) / 2.0), theta);

				// TODO Ellipse should not be outside of the image.
				if (ellipse.x1 < 0 || ellipse.y1 < 0 || ellipse.x2 < 0
						|| ellipse.y2 < 0 || ellipse.x1 >= image.getWidth()
						|| ellipse.y1 >= image.getHeight()
						|| ellipse.x2 >= image.getWidth()
						|| ellipse.y2 >= image.getHeight()) {
					ellipse.setup((int) (image.getWidth() / 2.0),
							(int) (image.getHeight() / 2.0), theta);
				}

				values.add(ellipse);
			}

			return new EllipseListChromosome(values, image);
		}
	}

	private static final int POPULATION_SIZE = 37;

	private static final double CROSSOVER_RATE = 0.9;

	private static final double MUTATION_RATE = 0.03;

	private static final double ELITISM_RATE = 0.1;

	private static final int TOURNAMENT_ARITY = 2;

	private static final long OPTIMIZATION_TIMEOUT_SECONDS = 60 * 60 * 24;

	private static BufferedImage original = null;

	private static ColorComparator comparator = new EuclideanColorComparator();

	static Color closestColor(Color color, Vector<Color> colors) {
		if (colors.size() <= 0) {
			return color;
		}

		Color best = colors.get(0);
		for (Color candidate : colors) {
			if (comparator.distance(color, candidate) < comparator.distance(
					color, best)) {
				best = candidate;
			}
		}

		return best;
	}

	static double alphaLevel(BufferedImage image, Vector<Color> colors) {
		double level = 0;

		int pixels[] = image.getRGB(0, 0, image.getWidth(), image.getHeight(),
				null, 0, image.getWidth());
		for (int i = 0; i < pixels.length; i++) {
			if (colors.contains(new Color(pixels[i])) == false) {
				level++;
			}
		}

		return level / pixels.length;
	}

	static double distance(BufferedImage a, BufferedImage b) {
		double result = 0;

		int aPixels[] = a.getRGB(0, 0, a.getWidth(), a.getHeight(), null, 0,
				a.getWidth());

		int bPixels[] = b.getRGB(0, 0, b.getWidth(), b.getHeight(), null, 0,
				b.getWidth());

		for (int i = 0; i < aPixels.length && i < bPixels.length; i++) {
			result += comparator.distance(new Color(aPixels[i]), new Color(
					bPixels[i]));
		}

		return result;
	}

	// TODO Implement colors merge in overlapping ellipses.
	static BufferedImage drawEllipses(BufferedImage image,
			List<Ellipse> ellipses) {
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setStroke(new BasicStroke(Ellipse.height,
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		for (Ellipse e : ellipses) {
			graphics.setColor(e.color);
			graphics.draw(e.line);
		}

		return image;
	}

	static Vector<Ellipse> randomApproximatedEllipses(BufferedImage image,
			Vector<Color> colors) {
		Vector<Ellipse> ellipses = new Vector<Ellipse>();

		int numberOfEllipses = (int) ((4 * image.getWidth() * image.getHeight()) / (Math.PI
				* Ellipse.width * Ellipse.height));

		/*
		 * It is not clear why this multiplication is needed.
		 */
		numberOfEllipses *= 1.0D + 9.0D * Util.PRNG.nextDouble();

		for (int i = 0, x, y; i < numberOfEllipses; i++) {
			Color color = colors.elementAt(Util.PRNG.nextInt(colors.size()));
			x = Util.PRNG.nextInt(image.getWidth());
			y = Util.PRNG.nextInt(image.getHeight());

			if (Util.USE_PIXEL_INFOMATION == true) {
				color = closestColor(new Color(image.getRGB(x, y)), colors);
			}

			double theta = 2.0D * Math.PI * Util.PRNG.nextDouble();

			ellipses.add(new Ellipse(x, y, theta, color));
		}

		return ellipses;
	}

	private static List<Ellipse> randomRepresentation(BufferedImage image,
			Vector<Color> colors, int length) {
		List<Ellipse> random = new ArrayList<Ellipse>();

		for (int i = 0, x, y; i < length; i++) {
			Color color = colors.elementAt(Util.PRNG.nextInt(colors.size()));
			x = Util.PRNG.nextInt(image.getWidth());
			y = Util.PRNG.nextInt(image.getHeight());

			if (Util.USE_PIXEL_INFOMATION == true) {
				color = closestColor(new Color(image.getRGB(x, y)), colors);
			}

			double theta = 2.0D * Math.PI * Util.PRNG.nextDouble();

			random.add(new Ellipse(x, y, theta, color));
		}

		return random;
	}

	private static Population randomInitialPopulation(BufferedImage image,
			Vector<Color> colors) {
		List<Chromosome> list = new LinkedList<Chromosome>();
		for (int i = 0; i < POPULATION_SIZE; i++) {
			list.add(new EllipseListChromosome(
					randomRepresentation(
							image,
							colors,
							(int) ((4 * image.getWidth() * image.getHeight()) / (Math.PI
									* Ellipse.width * Ellipse.height))), image));
		}
		return new ElitisticListPopulation(list, 2 * list.size(), ELITISM_RATE);
	}

	/**
	 * java Main <image file name> <population size> <number of evolutions>
	 * <primitive width> <primitive height> <list of colors>
	 * 
	 * Example: java Main ./dat/0001.jpg 11 10 6 9 000000 808080 C0C0C0 FFFFFF
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

		Population initial = randomInitialPopulation(original, colors);

		/*
		 * Report initial best solution.
		 */
		ImageIO.write(
				drawEllipses(
						new BufferedImage(original.getWidth(), original
								.getHeight(), BufferedImage.TYPE_INT_ARGB),
								((EllipseListChromosome) initial
								.getFittestChromosome()).getEllipses()), "png",
				new File(path + System.currentTimeMillis() + ".png"));
		System.out.println("Optimization start ...");

		
		// TODO Crossover is for chromosomes with different length.
		GeneticAlgorithm algorithm = new GeneticAlgorithm(
				new UniformCrossover<Ellipse>(0.5), CROSSOVER_RATE,
				new RandomEllipsesMutation(original), MUTATION_RATE,
				new TournamentSelection(TOURNAMENT_ARITY));
		Population optimized = algorithm.evolve(initial, new FixedElapsedTime(
				OPTIMIZATION_TIMEOUT_SECONDS));

		/*
		 * Print plotting instructions.
		 */
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(path + System.currentTimeMillis() + ".txt"));
		out.write(((EllipseListChromosome) optimized
				.getFittestChromosome()).getEllipses().toString().getBytes());
		out.close();

		/*
		 * Report result.
		 */
		ImageIO.write(
				drawEllipses(
						new BufferedImage(original.getWidth(), original
								.getHeight(), BufferedImage.TYPE_INT_ARGB),
								((EllipseListChromosome) optimized
										.getFittestChromosome()).getEllipses()), "png",
				new File(path + System.currentTimeMillis() + ".png"));
		System.out.println("Optimization end ...");
	}
}
