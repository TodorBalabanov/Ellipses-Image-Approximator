package eu.veldsoft.ellipses.image.approximator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.MutationPolicy;

class RandomEllipsesMutation implements MutationPolicy {
	private BufferedImage image = null;
	private Map<String, Integer> histogram = null;
	private Vector<Color> colors = null;

	public RandomEllipsesMutation(BufferedImage image,
			Map<String, Integer> histogram, Vector<Color> colors) {
		this.image = image;
		this.histogram = histogram;
		this.colors = colors;
	}

	@Override
	public Chromosome mutate(Chromosome original) {
		if (original instanceof EllipseListChromosome == false) {
			throw new IllegalArgumentException();
		}

		double factor = Util.PRNG.nextDouble();

		List<Ellipse> values = new ArrayList<Ellipse>();
		for (Ellipse value : ((EllipseListChromosome) original).getEllipses()) {
			int dx = (int) (Ellipse.WIDTH * factor);
			int dy = (int) (Ellipse.HEIGHT * factor);
			double theta = 2 * Math.PI * Util.PRNG.nextDouble();

			Ellipse ellipse = new Ellipse(value);

			/*
			 * Mutate color in some cases by taking color of other ellipse.
			 */
			if (Util.PRNG.nextDouble() < factor) {
				ellipse.color = ((EllipseListChromosome) original)
						.getRandomElement().color;
			}

			/*
			 * Mutate positions.
			 */
			if (Util.PRNG.nextBoolean() == true) {
				ellipse.x1 -= dx;
			} else {
				ellipse.x1 += dx;
			}
			if (Util.PRNG.nextBoolean() == true) {
				ellipse.y1 -= dy;
			} else {
				ellipse.y1 += dy;
			}

			/*
			 * Mutate rotation.
			 */
			ellipse.setup((int) ((ellipse.x1 + ellipse.x2) / 2.0),
					(int) ((ellipse.y1 + ellipse.y2) / 2.0), theta);

			values.add(ellipse);
		}

		return new EllipseListChromosome(values, image, histogram, colors);
	}
}
