package eu.veldsoft.ellipses.image.approximator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.genetics.Population;

class Util {
	private static ColorComparator euclidean = new EuclideanColorComparator();

	static final Random PRNG = new Random();

	static int DEFAULT_THREAD_POOL_SIZE = 1;

	static final ColorCoordinatesComparator usage = new ColorCoordinatesComparator();

	static String log = "";

	static double distance(BufferedImage a, BufferedImage b) {
		int aPixels[] = a.getRGB(0, 0, a.getWidth(), a.getHeight(), null, 0,
				a.getWidth());
		int bPixels[] = b.getRGB(0, 0, b.getWidth(), b.getHeight(), null, 0,
				b.getWidth());

		int size = 0;
		double sum = 0;
		for (size = 0; size < aPixels.length && size < bPixels.length; size++) {
			sum += euclidean.distance(aPixels[size] & 0xFFFFFF,
					bPixels[size] & 0xFFFFFF);
		}

		return sum / size;
	}

	static Color closestColor(Color color, Vector<Color> colors) {
		if (colors.size() <= 0) {
			return color;
		}

		Color best = colors.get(0);
		for (Color candidate : colors) {
			if (euclidean.distance(color.getRGB() & 0xFFFFFF,
					candidate.getRGB() & 0xFFFFFF) < euclidean.distance(
							color.getRGB() & 0xFFFFFF,
							best.getRGB() & 0xFFFFFF)) {
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
			for (Color color : colors) {
				if (pixels[i] == color.getRGB()) {
					level++;
					break;
				}
			}
		}

		return level / pixels.length;
	}

	static BufferedImage drawEllipses(BufferedImage image,
			List<Ellipse> ellipses) {
		// TODO Implement colors merge in overlapping ellipses.
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setStroke(new BasicStroke(Ellipse.HEIGHT,
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		for (Ellipse ellipse : ellipses) {
			graphics.setColor(ellipse.color);
			graphics.draw(ellipse.line);
		}

		return image;
	}

	static Vector<Ellipse> randomApproximatedEllipses(BufferedImage image,
			Vector<Color> colors, boolean pixelClosestColor) {
		Vector<Ellipse> ellipses = new Vector<Ellipse>();

		for (int i = 0, x, y; i < EllipseListChromosome.AVERAGE_LENGTH(); i++) {
			Color color = colors.elementAt(PRNG.nextInt(colors.size()));
			x = PRNG.nextInt(image.getWidth());
			y = PRNG.nextInt(image.getHeight());

			if (pixelClosestColor == true) {
				color = closestColor(new Color(image.getRGB(x, y)), colors);
			}

			double theta = 2.0D * Math.PI * PRNG.nextDouble();

			ellipses.add(new Ellipse(x, y, theta, color));
		}

		return ellipses;
	}

	static List<Ellipse> randomRepresentation(BufferedImage image,
			Vector<Color> colors, boolean pixelClosestColor, int length) {
		List<Ellipse> random = new ArrayList<Ellipse>();

		for (int i = 0, x, y; i < length; i++) {
			Color color = colors.elementAt(PRNG.nextInt(colors.size()));
			x = PRNG.nextInt(image.getWidth());
			y = PRNG.nextInt(image.getHeight());

			if (pixelClosestColor == true) {
				color = closestColor(new Color(image.getRGB(x, y)), colors);
			}

			double theta = 2.0D * Math.PI * PRNG.nextDouble();

			random.add(new Ellipse(x, y, theta, color));
		}

		return random;
	}

	static Population randomInitialPopulation(BufferedImage image,
			Vector<Color> colors, boolean pixelClosestColor, int populationSize,
			double elitismRate) {
		List<Chromosome> list = new LinkedList<Chromosome>();
		for (int i = 0; i < populationSize; i++) {
			list.add(new EllipseListChromosome(
					randomRepresentation(image, colors, pixelClosestColor,
							EllipseListChromosome.AVERAGE_LENGTH()),
					image, colors));
		}
		return new ElitisticListPopulation(list, 2 * list.size(), elitismRate);
	}

	static void writeSolution(BufferedImage image, List<Ellipse> list,
			String file) {
		try {
			ImageIO.write(Util.drawEllipses(new BufferedImage(image.getWidth(),
					image.getHeight(), BufferedImage.TYPE_INT_ARGB), list),
					"png", new File(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
