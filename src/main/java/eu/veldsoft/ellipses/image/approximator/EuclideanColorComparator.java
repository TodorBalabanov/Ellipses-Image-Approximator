package eu.veldsoft.ellipses.image.approximator;

class EuclideanColorComparator implements ColorComparator {
	private double deltaRed;
	private double deltaGreen;
	private double deltaBlue;

	public double distance(int a, int b) {
		deltaRed = ((a >> 16) & 0xFF) - ((b >> 16) & 0xFF);
		deltaGreen = ((a >> 8) & 0xFF) - ((b >> 8) & 0xFF);
		deltaBlue = (a & 0xFF) - (b & 0xFF);

		// return deltaRed * deltaRed + deltaGreen * deltaGreen
		// + deltaBlue * deltaBlue;
		return Math.sqrt(deltaRed * deltaRed + deltaGreen * deltaGreen
				+ deltaBlue * deltaBlue);
	}
}
