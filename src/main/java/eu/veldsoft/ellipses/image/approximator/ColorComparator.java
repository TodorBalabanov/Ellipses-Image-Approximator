package eu.veldsoft.ellipses.image.approximator;

import java.awt.Color;

interface ColorComparator {
	public double distance(Color a, Color b);
}
