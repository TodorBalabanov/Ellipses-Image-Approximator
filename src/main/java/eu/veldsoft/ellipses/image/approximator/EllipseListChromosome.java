package eu.veldsoft.ellipses.image.approximator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.math3.genetics.AbstractListChromosome;
import org.apache.commons.math3.genetics.InvalidRepresentationException;

class EllipseListChromosome extends AbstractListChromosome<Ellipse>
		implements
			GCode {
	/** A pseudo-random number generator instance. */
	private static final Random PRNG = new Random();

	/**
	 * Image comparator instance.
	 */
	private static final ImageComparator IMAGE_COMPARATOR = new HierarchicalProbabilisticImageComparator(
			10, 0.05);

	/** Color comparator on coordinates instance. */
	private static final ColorCoordinatesComparator COORDINATES_COLOR_COMPARATOR = new ColorCoordinatesComparator();

	/** The amount of simple primitives can float, but it has average size. */
	private static double LENGTH_MEAN = 0;

	/**
	 * The amount of simple primitives can float with standard deviation for
	 * size.
	 */
	private static double LENGTH_SD = 0;

	/** Reference to the original image. */
	private static BufferedImage image = null;

	/** Reference to the original image histogram. */
	private static Map<String, Integer> histogram = null;

	/** Reference to the set of reduced colors. */
	private static Vector<Color> colors = null;

	/**
	 * Coordinates color comparator reference getter.
	 * 
	 * @return Reference to the comparator.
	 */
	public static ColorCoordinatesComparator COORDINATES_COLOR_COMPARATOR() {
		return COORDINATES_COLOR_COMPARATOR;
	}

	/**
	 * @return The average length of the chromosome.
	 */
	public static double LENGTH_MEAN() {
		return LENGTH_MEAN;
	}

	/**
	 * @return The standard deviation for length of the chromosome.
	 */
	public static double LENGTH_SD() {
		return LENGTH_SD;
	}

	/**
	 * @param lengthMean
	 *            The average length of the chromosome.
	 */
	public static void LENGTH_MEAN(double lengthMean) {
		if (lengthMean < 0) {
			lengthMean = 0;
		}

		LENGTH_MEAN = lengthMean;
	}

	/**
	 * @param lengthSd
	 *            The average length of the chromosome.
	 */
	public static void LENGTH_SD(double lengthSd) {
		if (lengthSd < 0) {
			lengthSd = 0;
		}

		LENGTH_SD = lengthSd;
	}

	public EllipseListChromosome(Ellipse[] representation, BufferedImage image,
			Map<String, Integer> histogram, Vector<Color> colors)
			throws InvalidRepresentationException {
		super(representation);
		EllipseListChromosome.image = image;
		EllipseListChromosome.histogram = histogram;
		EllipseListChromosome.colors = colors;

		checkValidity(getRepresentation());
	}

	public EllipseListChromosome(List<Ellipse> representation,
			BufferedImage image, Map<String, Integer> histogram,
			Vector<Color> colors) throws InvalidRepresentationException {
		super(representation);
		EllipseListChromosome.image = image;
		EllipseListChromosome.histogram = histogram;
		EllipseListChromosome.colors = colors;

		checkValidity(getRepresentation());
	}

	public EllipseListChromosome(List<Ellipse> representation, boolean copy,
			BufferedImage image, Map<String, Integer> histogram,
			Vector<Color> colors) throws InvalidRepresentationException {
		super(representation, copy);
		EllipseListChromosome.image = image;
		EllipseListChromosome.histogram = histogram;
		EllipseListChromosome.colors = colors;

		checkValidity(getRepresentation());
	}

	/**
	 * The most fitted chromosome has a minimum value.
	 * 
	 * @return Chromosome fitness value.
	 */
	@Override
	public double fitness() {
		// TODO Better handling of multiple criteria should be implemented. At
		// least coefficients should be parameterized from outside.

		/*
		 * Draw ellipses.
		 */
		BufferedImage experimental = new BufferedImage(image.getWidth(),
				image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Util.drawEllipses(experimental, getRepresentation());

		/* Multiple-criteria for fitness value estimation. */
		double disproportion = Math.abs((getRepresentation().size() * Math.PI
				* Ellipse.WIDTH() * Ellipse.HEIGHT() / 4D)
				- (image.getWidth() * image.getHeight()));
		double distance = IMAGE_COMPARATOR.distance(image, experimental);
		double alpha = 1 + Util.alphaLevel(experimental, colors);

		/*
		 * All criteria taken separately should go to a minimum value as
		 * possible. Apache Commons Genetic Algorithms framework is organized in
		 * such a way that if single criteria should go down they should be
		 * taken with negative signs. Like this:
		 * 
		 * return -disproportion;
		 * 
		 * return -distance;
		 * 
		 * return -alpha;
		 */
		return -(alpha * distance * disproportion);
	}

	@Override
	protected void checkValidity(List<Ellipse> list)
			throws InvalidRepresentationException {
		if (list == null) {
			return;
		}

		if (image == null) {
			return;
		}

		/* Put all coordinates inside image dimensions. */
		for (Ellipse ellipse : list) {
			if (ellipse.x1 < 0 || ellipse.x2 < 0) {
				int dx = 1 + PRNG.nextInt(image.getWidth() / 2);

				ellipse.x1 += dx;
				ellipse.x2 += dx;
			}

			if (ellipse.y1 < 0 || ellipse.y2 < 0) {
				int dy = 1 + PRNG.nextInt(image.getHeight() / 2);

				ellipse.y1 += dy;
				ellipse.y2 += dy;
			}

			if (ellipse.x1 >= image.getWidth()
					|| ellipse.x2 >= image.getWidth()) {
				int dx = 1 + PRNG.nextInt(image.getWidth() / 2);

				ellipse.x1 -= dx;
				ellipse.x2 -= dx;
			}

			if (ellipse.y1 >= image.getHeight()
					|| ellipse.y2 >= image.getHeight()) {
				int dy = 1 + PRNG.nextInt(image.getHeight() / 2);

				ellipse.y1 -= dy;
				ellipse.y2 -= dy;
			}
		}
	}

	@Override
	public EllipseListChromosome newFixedLengthChromosome(List<Ellipse> list) {
		// TODO Make a deep copy.
		return new EllipseListChromosome(list, true, image, histogram, colors);
	}

	public List<Ellipse> getEllipses() {
		return getRepresentation();
	}

	public Ellipse getRandomElement() {
		return getRepresentation()
				.get(PRNG.nextInt(getRepresentation().size()));
	}

	private String gCodeColorStart(Color color, Settings configuration) {
		String gCode = "";

		if (configuration.comments == true) {
			gCode += "(G Code instructions for "
					+ String.format("%06X", color.getRGB() & 0xFFFFFF)
					+ " color.)";
			gCode += "\r\n";
		}

		gCode += "G90";
		gCode += (configuration.comments == true)
				? " (Switch to absolute coordinates.)"
				: "";
		gCode += (configuration.comments == true) ? "\r\n" : "";
		gCode += "G21";
		gCode += (configuration.comments == true)
				? " (All units are in millimeters.)"
				: "";
		gCode += (configuration.comments == true) ? "\r\n" : " ";
		gCode += "M3S" + configuration.zDown;
		gCode += (configuration.comments == true)
				? " (Move down for initialization.)"
				: "";
		gCode += "\r\n";
		gCode += "G04P1.0";
		gCode += (configuration.comments == true) ? " (Pause.)" : "";
		gCode += "\r\n";
		gCode += "M5";
		gCode += (configuration.comments == true) ? " (Pen move up.)" : "";
		gCode += "\r\n";
		gCode += "G00X" + configuration.xHome + "Y" + configuration.yHome;
		gCode += (configuration.comments == true)
				? " (Move to home position for initialization.)"
				: "";
		gCode += "\r\n";
		gCode += "G04P1.0";
		gCode += (configuration.comments == true)
				? " (Initialization timeout.)"
				: "";
		gCode += "\r\n";

		return gCode;
	}

	@Override
	public String toGCode(Settings configuration) {
		String gCode = "";

		/* Sorting by colors is important for the plotting order. */
		List<Ellipse> list = new ArrayList<Ellipse>(getEllipses());
		Collections.sort(list, COORDINATES_COLOR_COMPARATOR);
		if (list.size() == 0) {
			return gCode;
		}

		if (configuration.comments == true) {
			gCode += "(Solution with " + getFitness() + " as fitness value.)";
			gCode += "\r\n";
			gCode += "(Solution with " + list.size()
					+ " number of primitive shapes.)";
			gCode += "\r\n";
			gCode += "\r\n";
		}

		/* Initialization of G Code script. */
		Color color = list.get(0).color;

		gCode += gCodeColorStart(color, configuration);

		/* Drawing instructions. */
		for (Ellipse ellipse : list) {
			if (color.equals(ellipse.color) == false) {
				gCode += (configuration.comments == true) ? "\r\n" : "";

				gCode += "G04P" + configuration.colorChangeTime;
				gCode += (configuration.comments == true)
						? " (Color change timeout.)"
						: "";
				gCode += "\r\n";
				gCode += (configuration.comments == true) ? "\r\n" : "";

				/*
				 * Each change of the color should be handled in separate G Code
				 * region.
				 */
				color = ellipse.color;

				gCode += gCodeColorStart(color, configuration);
			}

			if (configuration.comments == true) {
				gCode += "\r\n";
				gCode += "(" + ellipse.toString() + ")";
				gCode += "\r\n";
			}

			gCode += ellipse.toGCode(configuration);
			gCode += "\r\n";
		}

		return gCode.trim();
	}

}
