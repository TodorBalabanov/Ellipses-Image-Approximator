package eu.veldsoft.ellipses.image.approximator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Line2D;

import org.apache.commons.math3.util.Precision;

class Ellipse implements Cloneable, GCode {
	private static int WIDTH = 0;
	private static int HEIGHT = 0;
	private static int ALPHA = 0xFF;

	static BasicStroke STROKE = null;

	double theta;
	int x1;
	int y1;
	int x2;
	int y2;
	Color color;
	Line2D line;

	/**
	 * @return The width of the ellipse.
	 */
	public static int WIDTH() {
		return WIDTH;
	}

	/**
	 * @param width
	 *            The width of the ellipse.
	 */
	public static void WIDTH(int width) {
		WIDTH = width;

		STROKE = new BasicStroke(Ellipse.HEIGHT, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
	}

	/**
	 * @return The height of the ellipse.
	 */
	public static int HEIGHT() {
		return HEIGHT;
	}

	/**
	 * @param height
	 *            Height of the ellipse.
	 */
	public static void HEIGHT(int height) {
		HEIGHT = height;

		STROKE = new BasicStroke(Ellipse.HEIGHT, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
	}

	/**
	 * @return Alpha value of the color.
	 */
	public static int ALPHA() {
		return ALPHA;
	}

	/**
	 * @param alpha
	 *            Alpha value of the color.
	 */
	public static void ALPHA(int alpha) {
		ALPHA = alpha;
	}

	void setup(int x, int y, double theta) {
		this.theta = theta;
		x1 = (int) (WIDTH * Math.cos(theta + Math.PI) / 2.0D + x);
		y1 = (int) (WIDTH * Math.sin(theta + Math.PI) / 2.0D + y);
		x2 = (int) (WIDTH * Math.cos(theta) / 2.0D + x);
		y2 = (int) (WIDTH * Math.sin(theta) / 2.0D + y);

		line.setLine(x1, y1, x2, y2);
	}

	public Ellipse(int x, int y, double theta, Color color) {
		super();

		this.color = color;
		line = new Line2D.Double(0, 0, 0, 0);

		setup(x, y, theta);
	}

	public Ellipse(Ellipse ellipse) {
		theta = ellipse.theta;
		x1 = ellipse.x1;
		y1 = ellipse.y1;
		x2 = ellipse.x2;
		y2 = ellipse.y2;
		color = ellipse.color;
		line = new Line2D.Double(x1, y1, x2, y2);
	}

	@Override
	public String toGCode(Settings configuration) {
		String refill = "";

		for (int i = 0; i < configuration.penRefillCount; i++) {
			refill += "M3S" + configuration.zDown + " (Pen move down.)" + "\n";
			refill += "M3S" + configuration.zUp + " (Pen move up.)" + "\n";
		}

		String gCode = "M3S" + configuration.zUp + " (Pen move up.)" + "\n"

				+ "X" + configuration.xHome + " Y" + configuration.yHome
				+ " (Move to home position.)" + "\n"

				+ refill

				+ "X"
				+ Precision.round(
						configuration.xOffset + x1 * configuration.scale, 2)
				+ " Y"
				+ Precision.round(
						configuration.yOffset + y1 * configuration.scale, 2)
				+ " (Move to first point position.)" + "\n"

				+ "M3S" + configuration.zDown + " (Pen move down.)" + "\n"

				+ "X"
				+ Precision.round(
						configuration.xOffset + x2 * configuration.scale, 2)
				+ " Y"
				+ Precision.round(
						configuration.yOffset + y2 * configuration.scale, 2)
				+ " (Move to second point position.)" + "\n"

				+ "M3S" + configuration.zUp + " (Pen move up.)" + "\n";

		return gCode.trim();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new Ellipse(this);
	}

	@Override
	public String toString() {
		return "Ellipse [x1=" + x1 + ", y1=" + y1 + ", x2=" + x2 + ", y2=" + y2
				+ ", red=" + color.getRed() + ", green=" + color.getGreen()
				+ ", blue=" + color.getBlue() + "]";
	}
}
