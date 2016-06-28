package eu.veldsoft.ellipses.image.approximator;

import java.awt.Color;

class EuclideanColorComparator implements ColorComparator {
	private double deltaRed;
	private double deltaGreen;
	private double deltaBlue;

	public double distance(Color a, Color b) {
		deltaRed = a.getRed() - b.getRed();
		deltaGreen = a.getGreen() - b.getGreen();
		deltaBlue = a.getBlue() - b.getBlue();

		return Math.sqrt(deltaRed * deltaRed + deltaGreen * deltaGreen + deltaBlue * deltaBlue);
	}
}
