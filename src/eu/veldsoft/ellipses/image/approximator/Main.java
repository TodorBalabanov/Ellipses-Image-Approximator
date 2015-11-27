package eu.veldsoft.ellipses.image.approximator;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.imageio.ImageIO;

class Util {
	public static final Random PRNG = new Random();
}

class Ellipse {
	int x;
	int y;
	int width;
	int height;
	double theta;
	Color color;

	public Ellipse(int x, int y, int width, int height, double theta,
			Color color) {
		super();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.theta = theta;
		this.color = color;
	}

	public Ellipse(Ellipse ellipse) {
		this(ellipse.x, ellipse.y, ellipse.width, ellipse.height,
				ellipse.theta, ellipse.color);
	}

}

class Chromosome {
	Vector<Ellipse> ellipses = new Vector<Ellipse>();
	double fittnes = Double.MAX_VALUE;

	public Chromosome(Vector<Ellipse> ellipses, double fittnes) {
		super();
		this.ellipses = ellipses;
		this.fittnes = fittnes;
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

	public Population(int size, BufferedImage image) {
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
			offspring = new Chromosome(Main.randomApproximatedEllipses(image),
					Double.MAX_VALUE);
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

		offspring = new Chromosome(new Vector<Ellipse>(), Double.MAX_VALUE);

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

		double factor = Util.PRNG.nextDouble();
		Ellipse e = offspring.ellipses.get(Util.PRNG.nextInt(offspring.ellipses
				.size()));

		int dx = (int) (e.width * factor);
		int dy = (int) (e.height * factor);
		double dtheta = e.theta * factor;

		/*
		 * Mutate color in some cases.
		 */
		if (Util.PRNG.nextDouble() < factor) {
			e.color = offspring.ellipses.get(Util.PRNG
					.nextInt(offspring.ellipses.size())).color;
		}

		/*
		 * Mutate rotation.
		 */
		e.theta += dtheta;
		if (e.theta > 2 * Math.PI) {
			e.theta -= 2 * Math.PI;
		}

		/*
		 * Mutate positions.
		 */
		if (Util.PRNG.nextBoolean() == true) {
			e.x -= dx;
		} else {
			e.x += dx;
		}
		if (Util.PRNG.nextBoolean() == true) {
			e.y -= dy;
		} else {
			e.y += dy;
		}

		/*
		 * Ellipse should not be outside of the image.
		 */
		if (e.x < 0) {
			e.x = 0;
		}
		if (e.y < 0) {
			e.y = 0;
		}
		if (e.x >= image.getWidth() - e.width) {
			e.x = image.getWidth() - e.width - 1;
		}
		if (e.y >= image.getHeight() - e.height) {
			e.y = image.getHeight() - e.height - 1;
		}
	}

	void evaluate() {
		/*
		 * The most used colors should be drawn first.
		 */
		Map<Color, Integer> histogram = new HashMap<Color, Integer>();
		for (Ellipse e : offspring.ellipses) {
			if (histogram.containsKey(e.color) == false) {
				histogram.put(e.color, 1);
			} else {
				histogram.put(e.color, histogram.get(e.color) + 1);
			}
		}

		/*
		 * Sort according color usage and x-y coordinates.
		 */
		for (int i = 0; i < offspring.ellipses.size(); i++) {
			for (int j = i + 1; j < offspring.ellipses.size(); j++) {
				Ellipse a = offspring.ellipses.get(i);
				Ellipse b = offspring.ellipses.get(j);

				if (histogram.get(a.color) < histogram.get(b.color)) {
					Collections.swap(offspring.ellipses, i, j);
				} else if (histogram.get(a.color) == histogram.get(b.color)) {
					if (a.x * a.x + a.y * a.y > b.x * b.x + b.y * b.y) {
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
		offspring.fittnes = 10D * offspring.ellipses.size() + 60D
				* Main.distance(image, experimental) + 30D
				* Main.alphaLevel(experimental);
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
						ImageIO.write(experimental, "png",
								new File("" + System.currentTimeMillis() + ".png"));
					} catch (IOException e) {
					}
				}
			})).start();
		}
	}
}

public class Main {
	private static BufferedImage original = null;
	private static int ellipseWidth = 0;
	private static int ellipseHeight = 0;
	private static Vector<Color> colors = new Vector<Color>();

	private static double distance(Color a, Color b) {
		double rmean = (a.getRed() + b.getRed()) / 2;
		int red = a.getRed() - b.getRed();
		int green = a.getGreen() - b.getGreen();
		int blue = a.getBlue() - b.getBlue();
		double weightRed = 2 + rmean / 256;
		double weightGreen = 4.0;
		double weightBlue = 2 + (255 - rmean) / 256;
		return Math.sqrt(weightRed * red * red + weightGreen * green * green
				+ weightBlue * blue * blue);
	}

	private static Color closestColor(Color color) {
		if (colors.size() <= 0) {
			return color;
		}

		Color best = colors.get(0);

		for (Color candidate : colors) {
			if (distance(color, candidate) < distance(color, best)) {
				best = candidate;
			}
		}

		return best;
	}

	static double alphaLevel(BufferedImage image) {
		double level = 0;

		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				if (colors.contains(new Color(image.getRGB(i, j))) == false) {
					level++;
				}
			}
		}

		return level / (image.getWidth() * image.getHeight());
	}

	static double distance(BufferedImage a, BufferedImage b) {
		double result = 0;

		for (int i = 0; i < a.getWidth() && i < b.getWidth(); i++) {
			for (int j = 0; j < a.getHeight() && j < b.getHeight(); j++) {
				result += distance(new Color(a.getRGB(i, j)),
						new Color(b.getRGB(i, j)));
			}
		}

		return result;
	}

	static BufferedImage drawEllipses(BufferedImage image,
			Vector<Ellipse> ellipses) {
		Graphics2D graphics = (Graphics2D) image.getGraphics();

		for (Ellipse ellipse : ellipses) {
			Shape ellipse1 = new Ellipse2D.Double(-ellipse.width / 2,
					-ellipse.height / 2, ellipse.width, ellipse.height);
			Shape ellipse2 = AffineTransform.getRotateInstance(ellipse.theta)
					.createTransformedShape(ellipse1);
			Shape ellipse3 = AffineTransform.getTranslateInstance(ellipse.x,
					ellipse.y).createTransformedShape(ellipse2);

			graphics.setColor(ellipse.color);
			graphics.setBackground(ellipse.color);
			graphics.fill(ellipse3);
		}

		return image;
	}

	static Vector<Ellipse> randomApproximatedEllipses(BufferedImage image) {
		Vector<Ellipse> ellipses = new Vector<Ellipse>();

		int numberOfEllipses = (int) ((4 * image.getWidth() * image.getHeight()) / (Math.PI
				* ellipseWidth * ellipseHeight));

		/*
		 * It is not clear why this multiplication is needed.
		 */
		numberOfEllipses *= 1.0D + 3.0D * Util.PRNG.nextDouble();

		for (int i = 0; i < numberOfEllipses; i++) {
			int x = Util.PRNG.nextInt(image.getWidth());
			int y = Util.PRNG.nextInt(image.getHeight());
			Color color = closestColor(new Color(image.getRGB(x, y)));
			double theta = 2.0D * Math.PI * Util.PRNG.nextDouble();

			ellipses.add(new Ellipse(x, y, ellipseWidth, ellipseHeight, theta,
					color));
		}

		return ellipses;
	}

	public static void main(String[] args) throws Exception {
		original = ImageIO.read(new File(args[0]));

		ellipseWidth = Integer.valueOf(args[3]);
		ellipseHeight = Integer.valueOf(args[4]);

		for (int i = 5; i < args.length; i++) {
			colors.add(new Color(Integer.parseInt(args[i], 16)));
		}

		Population population = new Population(Integer.valueOf(args[1]),
				original);

		/*
		 * Report initial best solution.
		 */
		ImageIO.write(
				drawEllipses(
						new BufferedImage(original.getWidth(), original
								.getHeight(), BufferedImage.TYPE_INT_ARGB),
						population.best.ellipses), "png",
				new File("" + System.currentTimeMillis() + ".png"));
		
		for (long g = Long.valueOf(args[2]); g >= 0; g--) {
			population.select();
			population.crossover();
			population.mutate();
			population.evaluate();
			population.survive();
			Thread.currentThread().sleep(100);
			System.out.println( g );
		}

		/*
		 * Report result.
		 */
		ImageIO.write(
				drawEllipses(
						new BufferedImage(original.getWidth(), original
								.getHeight(), BufferedImage.TYPE_INT_ARGB),
						population.best.ellipses), "png",
				new File("" + System.currentTimeMillis() + ".png"));
	}
}
