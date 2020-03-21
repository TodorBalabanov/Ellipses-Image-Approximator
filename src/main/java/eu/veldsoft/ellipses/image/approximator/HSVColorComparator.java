package eu.veldsoft.ellipses.image.approximator;

class HSVColorComparator implements ColorComparator {
	private double meanRed;
	private int deltaRed;
	private int deltaGreen;
	private int deltaBlue;
	private double weightRed;
	private double weightGreen;
	private double weightBlue;

	@Override
	public double distance(int a, int b) {
		meanRed = (((a >> 16) & 0xFF) + ((b >> 16) & 0xFF)) / 2D;

		deltaRed = ((a >> 16) & 0xFF) - ((b >> 16) & 0xFF);
		deltaGreen = ((a >> 8) & 0xFF) - ((b >> 8) & 0xFF);
		deltaBlue = (a & 0xFF) - (b & 0xFF);

		weightRed = 2D + meanRed / 256D;
		weightGreen = 4D;
		weightBlue = 2D + (255D - meanRed) / 256D;

		// TODO return Math.sqrt(weightRed * red * red + weightGreen * green *
		// green + weightBlue * blue * blue);
		return (weightRed * deltaRed * deltaRed
				+ weightGreen * deltaGreen * deltaGreen
				+ weightBlue * deltaBlue * deltaBlue);
	}

	@Override
	public double distance(int[] rgb, int color) {
		// TODO Should be implemented particularly for this case.
		return distance(rgb[0] << 16 | rgb[1] << 8 | rgb[2], color);
	}
}
