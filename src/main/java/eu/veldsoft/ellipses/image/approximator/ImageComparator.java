package eu.veldsoft.ellipses.image.approximator;

import java.awt.image.BufferedImage;

/**
 * Compare to raster images.
 * 
 * @author Todor Balabanov
 */
interface ImageComparator {
	/**
	 * Calculates the distance between two images when they are represented as
	 * buffered image objects.
	 * 
	 * @param a
	 *            First image.
	 * @param b
	 *            Second image.
	 * 
	 * @return Numerical estimation of the distance.
	 */
	public double distance(BufferedImage a, BufferedImage b);
}
