package eu.veldsoft.ellipses.image.approximator;

interface ColorComparator {
	public double distance(int a, int b);

	public double distance(int rgb[], int color);
}
