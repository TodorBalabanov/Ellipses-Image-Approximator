package eu.veldsoft.ellipses.image.approximator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

interface ColorComparator {
	public double distance(Color a, Color b);
}

class Constants {
	public static boolean USE_PIXEL_INFOMATION = true;

	public static boolean COLORS_EVOTUION = true;
}

class HSVColorComparator implements ColorComparator {
	private double meanRed;
	private int deltaRed;
	private int deltaGreen;
	private int deltaBlue;
	private double weightRed;
	private double weightGreen;
	private double weightBlue;

	public double distance(Color a, Color b) {
		meanRed = (a.getRed() + b.getRed()) / 2;

		deltaRed = a.getRed() - b.getRed();
		deltaGreen = a.getGreen() - b.getGreen();
		deltaBlue = a.getBlue() - b.getBlue();

		weightRed = 2 + meanRed / 256;
		weightGreen = 4.0;
		weightBlue = 2 + (255 - meanRed) / 256;

		// TODO return Math.sqrt(weightRed * red * red + weightGreen * green *
		// green + weightBlue * blue * blue);
		return (weightRed * deltaRed * deltaRed + weightGreen * deltaGreen
				* deltaGreen + weightBlue * deltaBlue * deltaBlue);
	}
}

class EuclideanColorComparator implements ColorComparator {
	private double deltaRed;
	private double deltaGreen;
	private double deltaBlue;

	public double distance(Color a, Color b) {
		deltaRed = a.getRed() - b.getRed();
		deltaGreen = a.getGreen() - b.getGreen();
		deltaBlue = a.getBlue() - b.getBlue();

		// TODO return Math.sqrt(deltaRed * deltaRed + deltaGreen * deltaGreen +
		// deltaBlue * deltaBlue);
		return (deltaRed * deltaRed + deltaGreen * deltaGreen + deltaBlue
				* deltaBlue);
	}
}

class HausdorffColorComparator implements ColorComparator {
	public double distance(Color a, Color b) {
		return 0.0;
	}
}

class Util {
	public static final Random PRNG = new Random();
}

class Ellipse {
	static int width;
	static int height;

	int x1;
	int y1;
	int x2;
	int y2;
	Color color;
	Line2D line;

	void setup(int x, int y, double theta) {
		x1 = (int) (width * Math.cos(theta + Math.PI) / 2.0D + x);
		y1 = (int) (width * Math.sin(theta + Math.PI) / 2.0D + y);
		x2 = (int) (width * Math.cos(theta) / 2.0D + x);
		y2 = (int) (width * Math.sin(theta) / 2.0D + y);

		line.setLine(x1, y1, x2, y2);
	}

	public Ellipse(int x, int y, double theta, Color color) {
		super();

		this.color = new Color(color.getRGB());
		line = new Line2D.Double(0, 0, 0, 0);

		setup(x, y, theta);
	}

	public Ellipse(Ellipse ellipse) {
		x1 = ellipse.x1;
		y1 = ellipse.y1;
		x2 = ellipse.x2;
		y2 = ellipse.y2;
		color = new Color(ellipse.color.getRGB());
		line = new Line2D.Double(x1, y1, x2, y2);
	}

	@Override
	public String toString() {
		return "Ellipse [x1=" + x1 + ", y1=" + y1 + ", x2=" + x2 + ", y2=" + y2
				+ ", color=" + color + "]";
	}
}

class Chromosome {
	Vector<Color> colors = new Vector<Color>();
	Vector<Ellipse> ellipses = new Vector<Ellipse>();
	double fittnes = Double.MAX_VALUE;

	public Chromosome(final Vector<Color> colors,
			final Vector<Ellipse> ellipses, double fittnes) {
		super();
		for (Color c : colors) {
			this.colors.addElement(new Color(c.getRGB()));
		}
		for (Ellipse e : ellipses) {
			this.ellipses.addElement(new Ellipse(e));
		}
		this.fittnes = fittnes;
	}

	public Chromosome(Chromosome chromosome) {
		this.fittnes = chromosome.fittnes;
		for (Ellipse e : chromosome.ellipses) {
			this.ellipses.addElement(new Ellipse(e));
		}
		for (Color c : chromosome.colors) {
			this.colors.addElement(new Color(c.getRGB()));
		}
	}

	@Override
	public String toString() {
		return "Chromosome [colors=" + colors + ", ellipses=" + ellipses
				+ ", fittnes=" + fittnes + "]";
	}
}

class Population {
	Chromosome best = null;
	private Chromosome first = null;
	private Chromosome second = null;
	private Chromosome offspring = null;
	private Chromosome result = null;
	private Vector<Chromosome> chromosomes = new Vector<Chromosome>();
	private BufferedImage image = null;
	private BufferedImage experimental = null;

	public Population(Population population) {
		super();

		this.first = null;
		this.second = null;
		this.offspring = null;
		this.result = null;
		this.image = population.image;
		this.experimental = null;
		this.best = population.best;
		this.chromosomes = new Vector<Chromosome>();

		for (Chromosome c : population.chromosomes) {
			chromosomes.add(new Chromosome(c));
		}
	}

	public Population(int size, BufferedImage image, Vector<Color> colors) {
		super();

		this.image = image;

		/*
		 * At least 4 chromosomes are needed in order the algorithm to work.
		 */
		if (size < 4) {
			size = 4;
		}

		/*
		 * Generate random population and evaluate chromosomes in it.
		 */
		for (int i = 0; i < size; i++) {
			offspring = new Chromosome(colors, Main.randomApproximatedEllipses(
					image, colors), Double.MAX_VALUE);
			evaluate();
			chromosomes.addElement(offspring);
		}
		offspring = null;

		/*
		 * Find the best initial chromosome.
		 */
		best = chromosomes.firstElement();
		for (Chromosome c : chromosomes) {
			if (best.fittnes > c.fittnes) {
				best = c;
			}
		}
	}

	void select() {
		do {
			first = chromosomes.get(Util.PRNG.nextInt(chromosomes.size()));
			second = chromosomes.get(Util.PRNG.nextInt(chromosomes.size()));
			result = chromosomes.get(Util.PRNG.nextInt(chromosomes.size()));

			/*
			 * Selection of better parents.
			 */
			double level = Util.PRNG.nextDouble();
			if (level > 0.95) {
				if (result.fittnes > first.fittnes) {
					Chromosome buffer = result;
					result = first;
					first = buffer;
				}
				if (result.fittnes > second.fittnes) {
					Chromosome buffer = result;
					result = second;
					second = buffer;
				}
			} else if (level > 0.65) {
				if (first.fittnes > second.fittnes) {
					Chromosome buffer = first;
					first = second;
					second = buffer;
				}
				if (result.fittnes < first.fittnes) {
					Chromosome buffer = result;
					result = first;
					first = buffer;
				}
				if (result.fittnes > second.fittnes) {
					Chromosome buffer = result;
					result = second;
					second = buffer;
				}
			} else if (level > 0.00) {
				if (result.fittnes < first.fittnes) {
					Chromosome buffer = result;
					result = first;
					first = buffer;
				}
				if (result.fittnes < second.fittnes) {
					Chromosome buffer = result;
					result = second;
					second = buffer;
				}
			}
		} while (result == best || first == second || first == result
				|| second == result);
	}

	void crossover() {
		/*
		 * Crossover probability.
		 */
		if (Util.PRNG.nextDouble() > 0.96) {
			do {
				offspring = chromosomes.get(Util.PRNG.nextInt(chromosomes
						.size()));
			} while (offspring == best);
			return;
		}

		offspring = new Chromosome(new Vector<Color>(), new Vector<Ellipse>(),
				Double.MAX_VALUE);

		for (int i = 0; i < first.colors.size() && i < second.colors.size(); i++) {
			if (Util.PRNG.nextBoolean()) {
				offspring.colors.add(first.colors.elementAt(i));
			} else {
				offspring.colors.add(second.colors.elementAt(i));
			}
		}

		for (Ellipse e : first.ellipses) {
			if (Util.PRNG.nextBoolean() == true) {
				offspring.ellipses.add(new Ellipse(e));
			}
		}

		for (Ellipse e : second.ellipses) {
			if (Util.PRNG.nextBoolean() == true) {
				offspring.ellipses.add(new Ellipse(e));
			}
		}
	}

	void mutate() {
		/*
		 * Mutation probability.
		 */
		if (Util.PRNG.nextDouble() > 0.01) {
			return;
		}

		if (Constants.COLORS_EVOTUION == true) {
			offspring.colors.setElementAt(
					new Color(Util.PRNG.nextInt(0x1000000)),
					Util.PRNG.nextInt(offspring.colors.size()));
		}

		double factor = Util.PRNG.nextDouble();
		Ellipse e = offspring.ellipses.get(Util.PRNG.nextInt(offspring.ellipses
				.size()));

		int dx = (int) (e.width * factor);
		int dy = (int) (e.height * factor);
		double theta = 2 * Math.PI * Util.PRNG.nextDouble();

		/*
		 * Mutate color in some cases by taking color of other ellipse.
		 */
		if (Util.PRNG.nextDouble() < factor) {
			e.color = offspring.ellipses.get(Util.PRNG
					.nextInt(offspring.ellipses.size())).color;
		}

		/*
		 * Mutate positions.
		 */
		if (Util.PRNG.nextBoolean() == true) {
			e.x1 -= dx;
		} else {
			e.x1 += dx;
		}
		if (Util.PRNG.nextBoolean() == true) {
			e.y1 -= dy;
		} else {
			e.y1 += dy;
		}

		/*
		 * Mutate rotation.
		 */
		e.setup((int) ((e.x1 + e.x2) / 2.0), (int) ((e.y1 + e.y2) / 2.0), theta);

		// TODO Ellipse should not be outside of the image.
		if (e.x1 < 0 || e.y1 < 0 || e.x2 < 0 || e.y2 < 0
				|| e.x1 >= image.getWidth() || e.y1 >= image.getHeight()
				|| e.x2 >= image.getWidth() || e.y2 >= image.getHeight()) {
			e.setup((int) (image.getWidth() / 2.0),
					(int) (image.getHeight() / 2.0), theta);
		}
	}

	void evaluate() {
		/*
		 * Calculate the most used colors from the original picture.
		 */
		Map<Color, Integer> histogram = new HashMap<Color, Integer>();
		int pixels[] = image.getRGB(0, 0, image.getWidth(), image.getHeight(),
				null, 0, image.getWidth());
		for (int i = 0; i < pixels.length; i++) {
			Color color = new Color(pixels[i]);

			if (histogram.containsKey(color) == false) {
				histogram.put(color, 1);
			} else {
				histogram.put(color, histogram.get(color) + 1);
			}
		}

		/*
		 * Sort according color usage and x-y coordinates. The most used colors
		 * should be drawn first.
		 */
		for (int i = 0; i < offspring.ellipses.size(); i++) {
			for (int j = i + 1; j < offspring.ellipses.size(); j++) {
				Ellipse a = offspring.ellipses.get(i);
				Ellipse b = offspring.ellipses.get(j);

				if (histogram.get(a.color) == null
						|| histogram.get(b.color) == null) {
					continue;
				}

				if (histogram.get(a.color) < histogram.get(b.color)) {
					Collections.swap(offspring.ellipses, i, j);
				} else if (histogram.get(a.color) == histogram.get(b.color)) {
					if (a.x1 * a.x1 + a.y1 * a.y1 > b.x1 * b.x1 + b.y1 * b.y1) {
						Collections.swap(offspring.ellipses, i, j);
					}
				}
			}
		}

		/*
		 * Draw ellipses.
		 */
		experimental = new BufferedImage(image.getWidth(), image.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Main.drawEllipses(experimental, offspring.ellipses);

		// TODO Number of ellipses and images distance can be used with some
		// coefficients.
		double size = offspring.ellipses.size();
		double distance = Main.distance(image, experimental);
		double alpha = Main.alphaLevel(experimental, offspring.colors);
		offspring.fittnes = 0.1D * size + 0.6D * distance * distance * distance
				+ 0.3D * alpha * alpha;
	}

	void survive() {
		result.ellipses = offspring.ellipses;
		result.fittnes = offspring.fittnes;

		if (result.fittnes < best.fittnes) {
			best = result;

			/*
			 * Only for optimization progress report.
			 */
			(new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						synchronized (experimental) {
							ImageIO.write(experimental, "png", new File(""
									+ System.currentTimeMillis() + ".png"));

							BufferedOutputStream out = new BufferedOutputStream(
									new FileOutputStream(""
											+ System.currentTimeMillis()
											+ ".txt"));
							out.write(best.toString().getBytes());
							out.close();
						}
					} catch (IOException e) {
					}
				}
			})).start();
		}
	}
}

class Task implements Runnable {
	private long evaluations;
	private Population population;

	public Task(long evaluations, Population population) {
		super();
		this.evaluations = evaluations;
		this.population = population;
	}

	@Override
	public void run() {
		for (long g = evaluations; g >= 0; g--) {
			population.select();
			population.crossover();
			population.mutate();
			population.evaluate();
			population.survive();
			try {
				Thread.currentThread().sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}
}

public class Main {
	private static int DEFAULT_THREAD_POOL_SIZE = 1;

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

	static BufferedImage drawEllipses(BufferedImage image,
			Vector<Ellipse> ellipses) {
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

			if (Constants.USE_PIXEL_INFOMATION == true) {
				color = closestColor(new Color(image.getRGB(x, y)), colors);
			}

			double theta = 2.0D * Math.PI * Util.PRNG.nextDouble();

			ellipses.add(new Ellipse(x, y, theta, color));
		}

		return ellipses;
	}

	public static void main(String[] args) throws Exception {
		original = ImageIO.read(new File(args[0]));

		Ellipse.width = Integer.valueOf(args[3]);
		Ellipse.height = Integer.valueOf(args[4]);

		Vector<Color> colors = new Vector<Color>();
		for (int i = 5; i < args.length; i++) {
			colors.add(new Color(Integer.parseInt(args[i], 16)));
		}

		Population population = new Population(Integer.valueOf(args[1]),
				original, colors);

		/*
		 * Report initial best solution.
		 */
		ImageIO.write(
				drawEllipses(
						new BufferedImage(original.getWidth(), original
								.getHeight(), BufferedImage.TYPE_INT_ARGB),
						population.best.ellipses), "png",
				new File("" + System.currentTimeMillis() + ".png"));
		System.out.println("Optimization start ...");

		int totalEvaluations = Integer.valueOf(args[2]);

		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);

		for (int i = 1; i < DEFAULT_THREAD_POOL_SIZE; i++) {
			executor.execute(new Task(totalEvaluations
					/ DEFAULT_THREAD_POOL_SIZE, new Population(population)));
		}
		executor.execute(new Task(totalEvaluations / DEFAULT_THREAD_POOL_SIZE
				+ totalEvaluations % DEFAULT_THREAD_POOL_SIZE, population));

		executor.shutdown();
		executor.awaitTermination(1000, TimeUnit.DAYS);

		/*
		 * Print plotting instructions.
		 */
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream("" + System.currentTimeMillis() + ".txt"));
		out.write(population.best.toString().getBytes());
		out.close();

		/*
		 * Report result.
		 */
		ImageIO.write(
				drawEllipses(
						new BufferedImage(original.getWidth(), original
								.getHeight(), BufferedImage.TYPE_INT_ARGB),
						population.best.ellipses), "png",
				new File("" + System.currentTimeMillis() + ".png"));
		System.out.println("Optimization end ...");
	}
}
