package eu.veldsoft.ellipses.image.approximator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

public class Main {
	private static int DEFAULT_THREAD_POOL_SIZE = 1;

	private static BufferedImage original = null;
	private static ColorComparator comparator = new EuclideanColorComparator();

	static Color closestColor(Color color, Vector<Color> colors) {
		if (colors.size() <= 0) {
			return color;
		}

		Color best = colors.get(0);

		for (Color candidate : colors) {
			if (comparator.distance(color, candidate) < comparator.distance(
					color, best)) {
				best = candidate;
			}
		}

		return best;
	}

	static double alphaLevel(BufferedImage image, Vector<Color> colors) {
		double level = 0;

		int pixels[] = image.getRGB(0, 0, image.getWidth(), image.getHeight(),
				null, 0, image.getWidth());
		for (int i = 0; i < pixels.length; i++) {
			if (colors.contains(new Color(pixels[i])) == false) {
				level++;
			}
		}

		return level / pixels.length;
	}

	static double distance(BufferedImage a, BufferedImage b) {
		double result = 0;

		int aPixels[] = a.getRGB(0, 0, a.getWidth(), a.getHeight(), null, 0,
				a.getWidth());

		int bPixels[] = b.getRGB(0, 0, b.getWidth(), b.getHeight(), null, 0,
				b.getWidth());

		for (int i = 0; i < aPixels.length && i < bPixels.length; i++) {
			result += comparator.distance(new Color(aPixels[i]), new Color(
					bPixels[i]));
		}

		return result;
	}

	//TODO Implement colors merge in overlapping elipses.
	static BufferedImage drawEllipses(BufferedImage image,
			Vector<Ellipse> ellipses) {
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setStroke(new BasicStroke(Ellipse.height,
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		for (Ellipse e : ellipses) {
			graphics.setColor(e.color);
			graphics.draw(e.line);
		}

		return image;
	}

	static Vector<Ellipse> randomApproximatedEllipses(BufferedImage image,
			Vector<Color> colors) {
		Vector<Ellipse> ellipses = new Vector<Ellipse>();

		int numberOfEllipses = (int) ((4 * image.getWidth() * image.getHeight()) / (Math.PI
				* Ellipse.width * Ellipse.height));

		/*
		 * It is not clear why this multiplication is needed.
		 */
		numberOfEllipses *= 1.0D + 9.0D * Util.PRNG.nextDouble();

		for (int i = 0, x, y; i < numberOfEllipses; i++) {
			Color color = colors.elementAt(Util.PRNG.nextInt(colors.size()));
			x = Util.PRNG.nextInt(image.getWidth());
			y = Util.PRNG.nextInt(image.getHeight());

			if (Constants.USE_PIXEL_INFOMATION == true) {
				color = closestColor(new Color(image.getRGB(x, y)), colors);
			}

			double theta = 2.0D * Math.PI * Util.PRNG.nextDouble();

			ellipses.add(new Ellipse(x, y, theta, color));
		}

		return ellipses;
	}

	public static void main(String[] args) throws Exception {
		original = ImageIO.read(new File(args[0]));

		Ellipse.width = Integer.valueOf(args[3]);
		Ellipse.height = Integer.valueOf(args[4]);

		Vector<Color> colors = new Vector<Color>();
		for (int i = 5; i < args.length; i++) {
			colors.add(new Color(Integer.parseInt(args[i], 16)));
		}

		Population population = new Population(Integer.valueOf(args[1]),
				original, colors);

		/*
		 * Report initial best solution.
		 */
		ImageIO.write(
				drawEllipses(
						new BufferedImage(original.getWidth(), original
								.getHeight(), BufferedImage.TYPE_INT_ARGB),
						population.best.ellipses), "png",
				new File("" + System.currentTimeMillis() + ".png"));
		System.out.println("Optimization start ...");

		int totalEvaluations = Integer.valueOf(args[2]);

		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);

		for (int i = 1; i < DEFAULT_THREAD_POOL_SIZE; i++) {
			executor.execute(new Task(totalEvaluations
					/ DEFAULT_THREAD_POOL_SIZE, new Population(population)));
		}
		executor.execute(new Task(totalEvaluations / DEFAULT_THREAD_POOL_SIZE
				+ totalEvaluations % DEFAULT_THREAD_POOL_SIZE, population));

		executor.shutdown();
		executor.awaitTermination(1000, TimeUnit.DAYS);

		/*
		 * Print plotting instructions.
		 */
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream("" + System.currentTimeMillis() + ".txt"));
		out.write(population.best.toString().getBytes());
		out.close();

		/*
		 * Report result.
		 */
		ImageIO.write(
				drawEllipses(
						new BufferedImage(original.getWidth(), original
								.getHeight(), BufferedImage.TYPE_INT_ARGB),
						population.best.ellipses), "png",
				new File("" + System.currentTimeMillis() + ".png"));
		System.out.println("Optimization end ...");
	}
}
