package eu.veldsoft.ellipses.image.approximator;

import java.awt.Color;
import java.util.Comparator;
import java.util.Map;

public class ColorCoordinatesComparator implements Comparator<Ellipse> {
	private Map<Color, Integer> histogram = null;

	public void setHistogram(Map<Color, Integer> histogram) {
		this.histogram = histogram;
	}

	@Override
	public int compare(Ellipse a, Ellipse b) {
		if (histogram == null) {
			return 0;
		}

		/*
		 * If the color is not in the histogram, just add one.
		 */
		if (histogram.containsKey(a.color) == false) {
			//TODO Maybe it should be implemented by some other way.
			histogram.put(a.color, 1);
		}

		/*
		 * If the color is not in the histogram, just add one.
		 */
		if (histogram.containsKey(b.color) == false) {
			//TODO Maybe it should be implemented by some other way.
			histogram.put(b.color, 1);
		}

		// TODO It is not tested and maybe is not correct.
		int difference = histogram.get(a.color) - histogram.get(b.color);
		if (difference < 0 || 0 < difference) {
			return difference;
		} else if (difference == 0) {
			difference = (a.x1 * a.x1 + a.y1 * a.y1)
					- (b.x1 * b.x1 + b.y1 * b.y1);
			return difference;
		}

		return 0;
	}

}
