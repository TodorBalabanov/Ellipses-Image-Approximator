package eu.veldsoft.ellipses.image.approximator;

import java.awt.Color;
import java.util.Vector;

class Chromosome {
	Vector<Color> colors = new Vector<Color>();
	Vector<Ellipse> ellipses = new Vector<Ellipse>();
	double fittnes = Double.MAX_VALUE;

	public Chromosome(final Vector<Color> colors,
			final Vector<Ellipse> ellipses, double fittnes) {
		super();
		for (Color c : colors) {
			this.colors.addElement(new Color(c.getRGB()));
		}
		for (Ellipse e : ellipses) {
			this.ellipses.addElement(new Ellipse(e));
		}
		this.fittnes = fittnes;
	}

	public Chromosome(Chromosome chromosome) {
		this.fittnes = chromosome.fittnes;
		for (Ellipse e : chromosome.ellipses) {
			this.ellipses.addElement(new Ellipse(e));
		}
		for (Color c : chromosome.colors) {
			this.colors.addElement(new Color(c.getRGB()));
		}
	}

	@Override
	public String toString() {
		return "Chromosome [colors=" + colors + ", ellipses=" + ellipses
				+ ", fittnes=" + fittnes + "]";
	}
}