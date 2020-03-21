package eu.veldsoft.ellipses.image.approximator;

class EuclideanColorComparator implements ColorComparator {
	private int deltaRed;
	private int deltaGreen;
	private int deltaBlue;

	@Override
	public double distance(int a, int b) {
		// deltaRed = ((a >> 16) & 0xFF) - ((b >> 16) & 0xFF);
		// deltaGreen = ((a >> 8) & 0xFF) - ((b >> 8) & 0xFF);
		// deltaBlue = (a & 0xFF) - (b & 0xFF);
		deltaRed = ((a & 0xFF0000) - (b & 0xFF0000)) >> 16;
		deltaGreen = ((a & 0xFF00) - (b & 0xFF00)) >> 8;
		deltaBlue = (a & 0xFF) - (b & 0xFF);

		// return deltaRed * deltaRed + deltaGreen * deltaGreen
		// + deltaBlue * deltaBlue;
		return Math.sqrt(deltaRed * deltaRed + deltaGreen * deltaGreen
				+ deltaBlue * deltaBlue);
	}

	@Override
	public double distance(int[] rgb, int color) {
		deltaRed = (rgb[0] - (color & 0xFF0000)) >> 16;
		deltaGreen = (rgb[1] - (color & 0xFF00)) >> 8;
		deltaBlue = (rgb[2] - (color & 0xFF));

		return Math.sqrt(deltaRed * deltaRed + deltaGreen * deltaGreen
				+ deltaBlue * deltaBlue);
	}
}
