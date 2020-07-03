package eu.veldsoft.ellipses.image.approximator;

/**
 * Compare two RGB color.
 * 
 * @author Todor Balabanov
 */
interface ColorComparator {
	/**
	 * Calculates the distance between two colors when colors are represented as
	 * integer numbers.
	 * 
	 * @param a
	 *            First color.
	 * @param b
	 *            Second color.
	 * 
	 * @return Numerical estimation of the distance.
	 */
	public double distance(int a, int b);

	/**
	 * Calculates the distance between two colors when colors are represented as
	 * RGB array and integer number.
	 * 
	 * @param rgb
	 *            First color.
	 * @param color
	 *            Second color.
	 * 
	 * @return Numerical estimation of the distance.
	 */
	public double distance(int rgb[], int color);
}
