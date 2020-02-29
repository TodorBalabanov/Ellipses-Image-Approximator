package eu.veldsoft.ellipses.image.approximator;

import java.awt.Color;
import java.awt.geom.Line2D;

class Ellipse implements Cloneable, GCode {
	static int width;
	static int height;

	int x1;
	int y1;
	int x2;
	int y2;
	Color color;
	Line2D line;

	void setup(int x, int y, double theta) {
		x1 = (int) (width * Math.cos(theta + Math.PI) / 2.0D + x);
		y1 = (int) (width * Math.sin(theta + Math.PI) / 2.0D + y);
		x2 = (int) (width * Math.cos(theta) / 2.0D + x);
		y2 = (int) (width * Math.sin(theta) / 2.0D + y);

		line.setLine(x1, y1, x2, y2);
	}

	public Ellipse(int x, int y, double theta, Color color) {
		super();

		this.color = new Color(color.getRGB());
		line = new Line2D.Double(0, 0, 0, 0);

		setup(x, y, theta);
	}

	public Ellipse(Ellipse ellipse) {
		x1 = ellipse.x1;
		y1 = ellipse.y1;
		x2 = ellipse.x2;
		y2 = ellipse.y2;
		color = new Color(ellipse.color.getRGB());
		line = new Line2D.Double(x1, y1, x2, y2);
	}

	@Override
	public String toGCode(double xOffset, double yOffset, double zDown, double zUp, double scale) {
		String gCode = "G00 Z"+zUp+" (Fast pen move up.)" + "\n"
				+ "G00 X0.00 Y0.00 (Fast move to home position.)" + "\n"
				+ "G00 Z"+zDown+" (Fast pen move down.)" + "\n"
				+ "G04 P0.1 (Wait for one tenth of a second before proceeding.)"
				+ "\n" + "G00 Z"+zUp+" (Fast pen move up.)" + "\n" + "G00 X" + (xOffset+x1*scale)
				+ " Y" + (yOffset+y1*scale) + " (Fast move to first point position.)" + "\n"
				+ "G01 Z"+zDown+" (Slow pen move down.)" + "\n" + "G01 X" + (xOffset+x2*scale)
				+ " Y" + (yOffset+y1*scale) + " (Slow move to second point position.)" + "\n"
				+ "G01 Z"+zUp+" (Slow pen move up.)";

		return gCode;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new Ellipse(this);
	}

	@Override
	public String toString() {
		return "Ellipse [x1=" + x1 + ", y1=" + y1 + ", x2=" + x2 + ", y2=" + y2
				+ ", color=" + color + "]";
	}
}
