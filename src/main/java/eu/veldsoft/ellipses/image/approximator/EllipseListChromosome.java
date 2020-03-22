package eu.veldsoft.ellipses.image.approximator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.math3.genetics.AbstractListChromosome;
import org.apache.commons.math3.genetics.InvalidRepresentationException;

class EllipseListChromosome extends AbstractListChromosome<Ellipse>
		implements
			GCode {

	/** The amount of simple primitives can float, but it has average size. */
	private static int AVERAGE_LENGTH = 0;

	/** Reference to the original image. */
	private static BufferedImage image = null;

	/** Reference to the original image histogram. */
	private static Map<String, Integer> histogram = null;

	/** Reference to the set of reduced colors. */
	private static Vector<Color> colors = null;

	/**
	 * @return The average length of the chromosome.
	 */
	public static int AVERAGE_LENGTH() {
		return AVERAGE_LENGTH;
	}

	/**
	 * @param averageLength
	 *            The average length of the chromosome.
	 */
	public static void AVERAGE_LENGTH(int averageLength) {
		if (averageLength < 0) {
			averageLength = 0;
		}

		AVERAGE_LENGTH = averageLength;
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

	@Override
	public double fitness() {
		/*
		 * Draw ellipses.
		 */
		BufferedImage experimental = new BufferedImage(image.getWidth(),
				image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Util.drawEllipses(experimental, getRepresentation());

		/* Multiple-criteria for fitness value estimation. */
		// double size = getRepresentation().size();
		double distance = Util.distance(image, experimental);
		// double alpha = Util.alphaLevel(experimental, colors);

		// return 1D / size;
		return -distance;
		// return 1D / alpha;
		// return -distance * 1D / size * 1D / alpha;

		// TODO Better handling of multiple criteria should be implemented. At
		// least coefficients should be parameterized from outside.
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
				int dx = 1 + Util.PRNG.nextInt(image.getWidth() / 2);

				ellipse.x1 += dx;
				ellipse.x2 += dx;
			}

			if (ellipse.y1 < 0 || ellipse.y2 < 0) {
				int dy = 1 + Util.PRNG.nextInt(image.getHeight() / 2);

				ellipse.y1 += dy;
				ellipse.y2 += dy;
			}

			if (ellipse.x1 >= image.getWidth()
					|| ellipse.x2 >= image.getWidth()) {
				int dx = 1 + Util.PRNG.nextInt(image.getWidth() / 2);

				ellipse.x1 -= dx;
				ellipse.x2 -= dx;
			}

			if (ellipse.y1 >= image.getHeight()
					|| ellipse.y2 >= image.getHeight()) {
				int dy = 1 + Util.PRNG.nextInt(image.getHeight() / 2);

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
				.get(Util.PRNG.nextInt(getRepresentation().size()));
	}

	@Override
	public String toGCode(Settings configuration) {
		String gCode = "";

		/* Sorting by colors is important for the plotting order. */
		List<Ellipse> list = new ArrayList<Ellipse>(getEllipses());
		Collections.sort(list, Util.usage);
		if (list.size() == 0) {
			return gCode;
		}

		gCode += "(Solution with " + getFitness() + " as fitness value.)";
		gCode += "\n";
		gCode += "(Solution with " + list.size()
				+ " number of primitive shapes.)";
		gCode += "\n";
		gCode += "\n";

		/* Initialization of G Code script. */
		Color color = list.get(0).color;
		gCode += "(G Code instructions for "
				+ color.toString().replace("java.awt.Color", "") + " color.)";
		gCode += "\n";
		gCode += "G21 (All units are in millimeters.)";
		gCode += "\n";
		gCode += "G90 (Switch to absolute coordinates.)";
		gCode += "\n";
		gCode += "G00 Z15.00 (Fast pen move up for initialization.)";
		gCode += "\n";
		gCode += "G00 X0.00 Y0.00 (Fast move to home position for initialization.)";
		gCode += "\n";

		/* Drawing instructions. */
		for (Ellipse ellipse : list) {
			if (color.equals(ellipse.color) == false) {
				gCode += "\n";
				gCode += "G04 P" + configuration.colorChangeTime
						+ " (Wait for setup of the next color.)";
				gCode += "\n";
				gCode += "\n";

				/*
				 * Each change of the color should be handled in separate G Code
				 * region.
				 */
				color = ellipse.color;

				gCode += "(G Code instructions for "
						+ color.toString().replace("java.awt.Color", "")
						+ " color.)";
				gCode += "\n";
				gCode += "G21 (All units are in millimeters.)";
				gCode += "\n";
				gCode += "G90 (Switch to absolute coordinates.)";
				gCode += "\n";
				gCode += "G00 Z15.00 (Fast pen move up for initialization.)";
				gCode += "\n";
				gCode += "G00 X0.00 Y0.00 (Fast move to home position for initialization.)";
				gCode += "\n";
			}

			gCode += "\n";
			gCode += "(" + ellipse.toString() + ")";
			gCode += "\n";

			gCode += ellipse.toGCode(configuration);
			gCode += "\n";
		}

		gCode += "\n";
		gCode += "G04 P" + configuration.colorChangeTime
				+ " (Wait for setup of the next color.)";

		gCode = gCode.trim();

		return gCode;
	}

}
