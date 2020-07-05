package eu.veldsoft.ellipses.image.approximator;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Compare to raster images by using Euclidean distance between the pixels but
 * in probabilistic sampling on hierarchical image detailization.
 * 
 * @author Todor Balabanov
 */
class HierarchicalProbabilisticImageComparator implements ImageComparator {
	/** A pseudo-random number generator instance. */
	private static final Random PRNG = new Random();

	/**
	 * Euclidean distance color comparator instance.
	 */
	private static final ColorComparator EUCLIDEAN = new EuclideanColorComparator();

	/** Recursive descent depth level. */
	private int depthLevel = 1;

	/**
	 * Size of the sample in percentages from the size of the population (from
	 * 0.0 to 1.0).
	 */
	private double samplePercent = 0.1;

	/** A supportive array for the first image pixels. */
	private int aPixels[] = null;

	/** A supportive array for the second image pixels. */
	private int bPixels[] = null;

	/**
	 * Constructor without parameters for default members' values.
	 */
	public HierarchicalProbabilisticImageComparator() {
		this(1, 0.1);
	}

	/**
	 * Constructor with all parameters.
	 * 
	 * @param depthLevel
	 *            Recursive descent depth level.
	 * @param samplePercent
	 *            Size of the sample in percentages from the size of the
	 *            population (from 0.0 to 1.0).
	 */
	public HierarchicalProbabilisticImageComparator(int depthLevel,
			double samplePercent) {
		super();

		this.depthLevel = depthLevel;
		this.samplePercent = samplePercent;
	}

	private double distance(int width, int level, int minX, int minY, int maxX,
			int maxY) {
		/*
		 * At the bottom of the recursive descent, distance is zero, and
		 * descending is canceled.
		 */
		if (level > depthLevel) {
			return 0;
		}

		/* Rectangle's boundaries should be observed. */
		if (maxX <= minX || maxY <= minY) {
			return 0;
		}

		/*
		 * Sample size calculated according formula.
		 * 
		 * https://www.surveymonkey.com/mp/sample-size-calculator/
		 */
		int sampleSize = (int) ((maxX - minX) * (maxY - minY) * samplePercent);

		/* Is sample size is zero the distance is accepted zero. */
		if (sampleSize <= 0) {
			return 0;
		}

		/* The sample should be a smaller part of the image. */
		if (sampleSize >= (maxX - minX) * (maxY - minY)) {
			throw new RuntimeException( "It is not allowed the sample size to be bigger than the population size!" );
		}
		
		/* Generate unique indices of pixels with the size of the sample. */
		Set<Integer> indices = new HashSet<Integer>();
		while (indices.size() < sampleSize) {
			int x = minX + PRNG.nextInt(maxX - minX + 1);
			int y = minY + PRNG.nextInt(maxY - minY + 1);
			indices.add(y * width + x);
		}

		/* The Euclidean distance of the randomly selected pixels. */
		double sum = 0;
		for (int index : indices) {
			sum += EUCLIDEAN.distance(aPixels[index], bPixels[index]);
		}

		/* Do a recursive descent. */
		return (sum / sampleSize) * level
				+ distance(width, level + 1, minX, minY,
						maxX - (maxX - minX) / 2, maxY - (maxY - minY) / 2)
				+ distance(width, level + 1, maxX - (maxX - minX) / 2, minY,
						maxX, maxY - (maxY - minY) / 2)
				+ distance(width, level + 1, minX, maxY - (maxY - minY) / 2,
						maxX - (maxX - minX) / 2, maxY)
				+ distance(width, level + 1, maxX - (maxX - minX) / 2,
						maxY - (maxY - minY) / 2, maxX, maxY);
	}

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

		aPixels = a.getRGB(0, 0, a.getWidth(), a.getHeight(), null, 0,
				a.getWidth());

		bPixels = b.getRGB(0, 0, b.getWidth(), b.getHeight(), null, 0,
				b.getWidth());

		/* Do a recursive calculation. */
		return distance(Math.min(a.getWidth(), b.getWidth()), 1, 0, 0,
				Math.min(a.getWidth() - 1, b.getWidth() - 1),
				Math.min(a.getHeight() - 1, b.getHeight() - 1));
	}
}
