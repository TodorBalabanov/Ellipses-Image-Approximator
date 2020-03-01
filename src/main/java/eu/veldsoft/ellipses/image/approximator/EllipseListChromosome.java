package eu.veldsoft.ellipses.image.approximator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.math3.genetics.AbstractListChromosome;
import org.apache.commons.math3.genetics.InvalidRepresentationException;

import eu.veldsoft.ellipses.image.approximator.GCode.Settings;

class EllipseListChromosome extends AbstractListChromosome<Ellipse> implements GCode {
	private BufferedImage imate = null;
	private Vector<Color> colors = null;

	public EllipseListChromosome(Ellipse[] representation, BufferedImage image,
			Vector<Color> colors) throws InvalidRepresentationException {
		super(representation);
		this.imate = image;
		this.colors = colors;
	}

	public EllipseListChromosome(List<Ellipse> representation,
			BufferedImage image, Vector<Color> colors)
			throws InvalidRepresentationException {
		super(representation);
		this.imate = image;
		this.colors = colors;
	}

	public EllipseListChromosome(List<Ellipse> representation, boolean copy,
			BufferedImage image, Vector<Color> colors)
			throws InvalidRepresentationException {
		super(representation, copy);
		this.imate = image;
		this.colors = colors;
	}

	@Override
	public double fitness() {
		/*
		 * Calculate the most used colors from the original picture.
		 */
		Map<Color, Integer> histogram = new HashMap<Color, Integer>();
		int pixels[] = imate.getRGB(0, 0, imate.getWidth(), imate.getHeight(),
				null, 0, imate.getWidth());
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
		Util.usage.setHistogram(histogram);
		Ellipse list[] = getRepresentation().toArray(
				new Ellipse[getRepresentation().size()]);

		/*
		 * Sorting have sense if only there is a histogram calculated.
		 */
		if (histogram != null) {
			Arrays.sort(list, Util.usage);
		}

		/*
		 * Draw ellipses.
		 */
		BufferedImage experimental = new BufferedImage(imate.getWidth(),
				imate.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Util.drawEllipses(experimental, list);

		// TODO Number of ellipses and images distance can be used with some
		// coefficients.
		double size = list.length;
		double distance = Util.distance(imate, experimental);
		double alpha = Util.alphaLevel(experimental, colors);

		//TODO Better handling of multiple criteria should be implemented. At least coefficients should be parameterized from outside.
		return 0.001D / (1D + size) + 0.1D / (1D + distance) + 0.01D
				/ (1D + alpha);
	}

	@Override
	protected void checkValidity(List<Ellipse> list)
			throws InvalidRepresentationException {
	}

	@Override
	public EllipseListChromosome newFixedLengthChromosome(List<Ellipse> list) {
		// TODO Make a deep copy.
		return new EllipseListChromosome(list, true, imate, colors);
	}

	public List<Ellipse> getEllipses() {
		return getRepresentation();
	}

	public Ellipse getRandomElement() {
		return getRepresentation().get(
				Util.PRNG.nextInt(getRepresentation().size()));
	}

	@Override
	public String toGCode(Settings configuration) {
		String gCode = "";
		
		List<Ellipse> list = getRepresentation();
		//TODO Sort by colors and create separate instructions.

		/* Initialization of G Code script. */
		gCode += "G21 (All units are in millimeters.)";
		gCode += "\n";
		gCode += "\n";
		gCode += "G90 (Switch to absolute coordinates.)";
		gCode += "\n";
		gCode += "\n";
		gCode += "G00 Z15.00 (Fast pen move up for initialization.)";
		gCode += "\n";
		gCode += "G00 X0.00 Y0.00 (Fast move to home position for initialization.)";
		gCode += "\n";
		gCode += "\n";
		
		/* Drawing instructions. */
		for(Ellipse elipse : list) {
			gCode += elipse.toGCode(configuration);
			gCode += "\n";
		}
		gCode = gCode.trim();

		return gCode;
	}

}
