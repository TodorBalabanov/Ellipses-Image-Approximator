package eu.veldsoft.ellipses.image.approximator;

class HausdorffColorComparator implements ColorComparator {
	@Override
	public double distance(int a, int b) {
		return 0.0;
	}

	@Override
	public double distance(int[] rgb, int color) {
		return 0;
	}
}