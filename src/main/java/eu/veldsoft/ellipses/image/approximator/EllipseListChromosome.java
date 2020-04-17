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
		double distance = Util.distance(image, experimental);
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
				+ String.format("%06X", color.getRGB() & 0xFFFFFF) + " color.)";
		gCode += "\n";
		gCode += "G90 (Switch to absolute coordinates.)";
		gCode += "\n";
		gCode += "G21 (All units are in millimeters.)";
		gCode += "\n";
		gCode += "M3S" + configuration.zUp + " (Move up for initialization.)";
		gCode += "\n";
		gCode += "X" + configuration.xHome + " Y" + configuration.yHome
				+ " (Move to home position for initialization.)";
		gCode += "\n";

		/* Drawing instructions. */
		for (Ellipse ellipse : list) {
			if (color.equals(ellipse.color) == false) {
				gCode += "\n";

				gCode += "G04 P" + configuration.colorChangeTime
						+ " (Color change timeout.)";
				gCode += "\n";
				gCode += "\n";

				/*
				 * Each change of the color should be handled in separate G Code
				 * region.
				 */
				color = ellipse.color;

				gCode += "(G Code instructions for "
						+ String.format("%06X", color.getRGB() & 0xFFFFFF)
						+ " color.)";
				gCode += "\n";
				gCode += "G90 (Switch to absolute coordinates.)";
				gCode += "\n";
				gCode += "G21 (All units are in millimeters.)";
				gCode += "\n";
				gCode += "M3S" + configuration.zUp
						+ " (Move up for initialization.)";
				gCode += "\n";
				gCode += "X0 Y0 (Move to home position for initialization.)";
				gCode += "\n";
			}

			gCode += "\n";
			gCode += "(" + ellipse.toString() + ")";
			gCode += "\n";

			gCode += ellipse.toGCode(configuration);
			gCode += "\n";
		}

		return gCode.trim();
	}

}
