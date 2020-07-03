package eu.veldsoft.ellipses.image.approximator;

import java.awt.image.BufferedImage;

/**
 * Compare to raster images by using Euclidean distance between the pixels.
 * 
 * @author Todor Balabanov
 */
class EuclideanImageComparator implements ImageComparator {
	/**
	 * Euclidean distance color comparator instance.
	 */
	private static final ColorComparator EUCLIDEAN = new EuclideanColorComparator();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double distance(BufferedImage a, BufferedImage b) {
		if (a.getWidth() != b.getWidth()) {
			throw new RuntimeException("Images width should be identical!");
		}

		if (a.getHeight() != b.getHeight()) {
			throw new RuntimeException("Images height should be identical!");
		}

		int aPixels[] = a.getRGB(0, 0, a.getWidth(), a.getHeight(), null, 0,
				a.getWidth());

		int bPixels[] = b.getRGB(0, 0, b.getWidth(), b.getHeight(), null, 0,
				b.getWidth());

		int size = 0;
		double sum = 0;
		int length = Math.min(aPixels.length, bPixels.length);
		for (size = 0; size < length; size++) {
			sum += EUCLIDEAN.distance(aPixels[size], bPixels[size]);
		}

		return sum / size;
	}
}
