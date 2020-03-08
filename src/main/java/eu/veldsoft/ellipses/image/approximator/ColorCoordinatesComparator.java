package eu.veldsoft.ellipses.image.approximator;

import java.util.Comparator;
import java.util.Map;

public class ColorCoordinatesComparator implements Comparator<Ellipse> {
	/**
	 * It is amazing that Map can not be used with Color as key. It is just not
	 * working.
	 */
	private Map<String, Integer> histogram = null;

	public void setHistogram(Map<String, Integer> histogram) {
		this.histogram = histogram;
	}

	@Override
	public int compare(Ellipse a, Ellipse b) {
		if (histogram == null) {
			throw (new RuntimeException("This comparator uses histogram!"));
		}

		// TODO Ellipses from the same color should to be as far away as
		// possible.
		if (a.color.getRGB() == b.color.getRGB()) {
			return 0;
		}

		/*
		 * If the color is not in the histogram, just add one.
		 */
		if (histogram.containsKey(a.color.toString()) == false) {
			// TODO Maybe it should be implemented by some other way.
			histogram.put(a.color.toString(), 1);
		}

		/*
		 * If the color is not in the histogram, just add one.
		 */
		if (histogram.containsKey(b.color.toString()) == false) {
			// TODO Maybe it should be implemented by some other way.
			histogram.put(b.color.toString(), 1);
		}

		// TODO It is not tested and maybe is not correct.
		return histogram.get(b.color.toString())
				- histogram.get(a.color.toString());
	}

}
