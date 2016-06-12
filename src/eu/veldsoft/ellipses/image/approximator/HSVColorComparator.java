package eu.veldsoft.ellipses.image.approximator;

import java.awt.Color;

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