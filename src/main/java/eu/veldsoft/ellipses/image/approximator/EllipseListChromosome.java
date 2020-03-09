package eu.veldsoft.ellipses.image.approximator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

	/** Reference to the original image */
	private BufferedImage image = null;

	/** Reference to the set of reduced colors. */
	private Vector<Color> colors = null;

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

	private List<Ellipse> sort() {
		/*
		 * Calculate the most used colors from the original picture.
		 */
		Map<String, Integer> histogram = new HashMap<String, Integer>();
		int pixels[] = image.getRGB(0, 0, image.getWidth(), image.getHeight(),
				null, 0, image.getWidth());
		for (int i = 0; i < pixels.length; i++) {
			Color color = new Color(pixels[i]);
			color = Util.closestColor(color, colors);
			// TODO It is not clear that mapping to the closest color is the
			// correct way of histogram calculation.

			if (histogram.containsKey(color.toString()) == false) {
				histogram.put(color.toString(), 1);
			} else {
				histogram.put(color.toString(),
						histogram.get(color.toString()) + 1);
			}
		}

		/*
		 * Sort according color usage and x-y coordinates. The most used colors
		 * should be drawn first.
		 */
		Util.usage.setHistogram(histogram);
		List<Ellipse> list = new ArrayList<Ellipse>(getRepresentation());

		/*
		 * Sorting have sense if only there is a histogram calculated.
		 */
		Collections.sort(list, Util.usage);

		return list;
	}

	public EllipseListChromosome(Ellipse[] representation, BufferedImage image,
			Vector<Color> colors) throws InvalidRepresentationException {
		super(representation);
		this.image = image;
		this.colors = colors;

		checkValidity(getRepresentation());
	}

	public EllipseListChromosome(List<Ellipse> representation,
			BufferedImage image, Vector<Color> colors)
			throws InvalidRepresentationException {
		super(representation);
		this.image = image;
		this.colors = colors;

		checkValidity(getRepresentation());
	}

	public EllipseListChromosome(List<Ellipse> representation, boolean copy,
			BufferedImage image, Vector<Color> colors)
			throws InvalidRepresentationException {
		super(representation, copy);
		this.image = image;
		this.colors = colors;

		checkValidity(getRepresentation());
	}

	@Override
	public double fitness() {
		/* Sort ellipses by color with the most used color first. */
		List<Ellipse> list = sort();

		/*
		 * Draw ellipses.
		 */
		BufferedImage experimental = new BufferedImage(image.getWidth(),
				image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Util.drawEllipses(experimental, list);

		/* Multiple-criteria for fitness value estimation. */
		double size = list.size();
		double distance = Util.distance(image, experimental);
		double alpha = Util.alphaLevel(experimental, colors);

		// TODO Better handling of multiple criteria should be implemented. At
		// least coefficients should be parameterized from outside.
		return 0.001D / (1D + size) + 0.1D / (1D + distance)
				+ 0.01D / (1D + alpha);
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
			int dx1, dx2, dy1, dy2;

			dx1 = dx2 = dy1 = dy2 = 0;

			if (ellipse.x1 < 0) {
				dx1 = -ellipse.x1;
			}
			if (ellipse.x2 < 0) {
				dx2 = -ellipse.x2;
			}
			if (ellipse.y1 < 0) {
				dy1 = -ellipse.y1;
			}
			if (ellipse.y2 < 0) {
				dy2 = -ellipse.y2;
			}

			ellipse.x1 += Math.max(dx1, dx2);
			ellipse.x2 += Math.max(dx1, dx2);
			ellipse.y1 += Math.max(dy1, dy2);
			ellipse.y2 += Math.max(dy1, dy2);

			dx1 = dx2 = dy1 = dy2 = 0;

			if (ellipse.x1 >= image.getWidth()) {
				dx1 = ellipse.x1 - image.getWidth() + 1;
			}

			if (ellipse.x2 >= image.getWidth()) {
				dx2 = ellipse.x2 - image.getWidth() + 1;
			}

			if (ellipse.y1 >= image.getWidth()) {
				dy1 = ellipse.y1 - image.getHeight() + 1;
			}

			if (ellipse.y2 >= image.getWidth()) {
				dy2 = ellipse.y2 - image.getHeight() + 1;
			}

			ellipse.x1 -= Math.max(dx1, dx2);
			ellipse.x2 -= Math.max(dx1, dx2);
			ellipse.y1 -= Math.max(dy1, dy2);
			ellipse.y2 -= Math.max(dy1, dy2);
		}
	}

	@Override
	public EllipseListChromosome newFixedLengthChromosome(List<Ellipse> list) {
		// TODO Make a deep copy.
		return new EllipseListChromosome(list, true, image, colors);
	}

	public List<Ellipse> getEllipses() {
		return getRepresentation();
	}

	public List<Ellipse> getSortedEllipses() {
		return sort();
	}

	public Ellipse getRandomElement() {
		return getRepresentation()
				.get(Util.PRNG.nextInt(getRepresentation().size()));
	}

	@Override
	public String toGCode(Settings configuration) {
		String gCode = "";

		/* Sorting by colors is important for the plotting order. */
		List<Ellipse> list = sort();

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
