package eu.veldsoft.ellipses.image.approximator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.genetics.Population;

import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.ChunkCopyBehaviour;

class Util {
	private static ColorComparator euclidean = new EuclideanColorComparator();

	static final Random PRNG = new Random();

	static int DEFAULT_THREAD_POOL_SIZE = 1;

	static final ColorCoordinatesComparator usage = new ColorCoordinatesComparator();

	static String log = "";

	static private void writePngSolution(int width, int height,
			List<Ellipse> list, double fitness, String file) {
		ByteArrayOutputStream os = null;
		try {
			ImageIO.write(
					Util.drawEllipses(new BufferedImage(width, height,
							BufferedImage.TYPE_INT_ARGB), list),
					"png", os = new ByteArrayOutputStream());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		PngReader reader = new PngReader(
				new ByteArrayInputStream(os.toByteArray()));
		PngWriter writer = new PngWriter(new File(file), reader.imgInfo, true);

		writer.copyChunksFrom(reader.getChunksList(),
				ChunkCopyBehaviour.COPY_ALL_SAFE);
		writer.getMetadata().setText("Ellipses", "" + list.size());
		writer.getMetadata().setText("Fitness", "" + fitness);

		for (int row = 0; row < reader.imgInfo.rows; row++) {
			writer.writeRow(reader.readRow());
		}

		reader.end();
		writer.end();
	}

	static private void writeSvgSolution(int width, int height,
			List<Ellipse> list, double fitness, String file) {
		SVGGraphics2D graphics = new SVGGraphics2D(
				GenericDOMImplementation.getDOMImplementation().createDocument(
						"http://www.w3.org/2000/svg", "svg", null));

		graphics.setSVGCanvasSize(new Dimension(width, height));

		for (Ellipse shape : list) {
			graphics.setColor(shape.color);
			graphics.setBackground(shape.color);

			Shape ellipse = new Ellipse2D.Double(-Ellipse.WIDTH() / 2D,
					-Ellipse.HEIGHT() / 2D, Ellipse.WIDTH(), Ellipse.HEIGHT());

			AffineTransform rotator = new AffineTransform();
			rotator.rotate(shape.theta);
			ellipse = rotator.createTransformedShape(ellipse);

			AffineTransform translator = new AffineTransform();
			translator.translate(
					-ellipse.getBounds().getX() + Math.min(shape.x1, shape.x2),
					-ellipse.getBounds().getY() + Math.min(shape.y1, shape.y2));
			ellipse = translator.createTransformedShape(ellipse);

			graphics.fill(ellipse);
			graphics.draw(ellipse);
		}

		try {
			Writer out = new BufferedWriter(new FileWriter(new File(file)));
			graphics.stream(out, false);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void writeSolution(int width, int height, List<Ellipse> list,
			double fitness, String file) {
		if (file.indexOf(".png") != -1) {
			writePngSolution(width, height, list, fitness, file);
		}

		if (file.indexOf(".svg") != -1) {
			writeSvgSolution(width, height, list, fitness, file);
		}
	}

	static double distance(BufferedImage a, BufferedImage b) {
		int aPixels[] = a.getRGB(0, 0, a.getWidth(), a.getHeight(), null, 0,
				a.getWidth());
		int bPixels[] = b.getRGB(0, 0, b.getWidth(), b.getHeight(), null, 0,
				b.getWidth());

		int size = 0;
		double sum = 0;
		int length = (aPixels.length < bPixels.length)
				? aPixels.length
				: bPixels.length;
		for (size = 0; size < length; size++) {
			sum += euclidean.distance(aPixels[size], bPixels[size]);
		}

		return sum / size;
	}

	static Color closestColor(int rgb, Vector<Color> colors) {
		if (colors.size() <= 0) {
			throw new RuntimeException("List of colors can not be emtpy!");
		}

		Color candidate, bestColor = colors.get(0);
		double distance, bestDistance = euclidean.distance(rgb & 0xFFFFFF,
				bestColor.getRGB() & 0xFFFFFF);

		for (int i = colors.size() - 1; i > 0; i--) {
			candidate = colors.get(i);
			distance = euclidean.distance(rgb & 0xFFFFFF,
					candidate.getRGB() & 0xFFFFFF);

			if (distance < bestDistance) {
				bestColor = candidate;
				bestDistance = distance;
			}
		}

		return bestColor;
	}

	static double alphaLevel(BufferedImage image, Vector<Color> colors) {
		int level = 0;

		int pixels[] = image.getRGB(0, 0, image.getWidth(), image.getHeight(),
				null, 0, image.getWidth());
		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] == 0x01FFFFFF) {
				level++;
			}
		}

		return (double) level / (double) pixels.length;
	}

	static BufferedImage drawEllipses(BufferedImage image,
			List<Ellipse> ellipses) {
		// TODO Implement colors merge in overlapping ellipses.

		Graphics2D graphics = (Graphics2D) image.getGraphics();

		/* Fill with light background. */
		graphics.setColor(new Color(0xFF, 0xFF, 0xFF, 0x01));
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

		graphics.setStroke(Ellipse.STROKE);
		for (Ellipse ellipse : ellipses) {
			graphics.setColor(ellipse.color);
			graphics.draw(ellipse.line);
		}

		return image;
	}

	static Vector<Ellipse> randomApproximatedEllipses(BufferedImage image,
			Vector<Color> colors, boolean pixelClosestColor) {
		Vector<Ellipse> ellipses = new Vector<Ellipse>();

		for (int i = 0, x, y; i < EllipseListChromosome.AVERAGE_LENGTH(); i++) {
			Color color = colors.elementAt(PRNG.nextInt(colors.size()));
			x = PRNG.nextInt(image.getWidth());
			y = PRNG.nextInt(image.getHeight());

			if (pixelClosestColor == true) {
				color = closestColor(image.getRGB(x, y), colors);
			}

			double theta = 2.0D * Math.PI * PRNG.nextDouble();

			ellipses.add(new Ellipse(x, y, theta, color));
		}

		return ellipses;
	}

	static List<Ellipse> randomRepresentation(BufferedImage image,
			Vector<Color> colors, boolean pixelClosestColor, int length) {
		List<Ellipse> random = new ArrayList<Ellipse>();

		for (int i = 0, x, y; i < length; i++) {
			Color color = colors.elementAt(PRNG.nextInt(colors.size()));
			x = PRNG.nextInt(image.getWidth());
			y = PRNG.nextInt(image.getHeight());

			if (pixelClosestColor == true) {
				color = closestColor(image.getRGB(x, y), colors);
			}

			double theta = 2.0D * Math.PI * PRNG.nextDouble();

			random.add(new Ellipse(x, y, theta, color));
		}

		return random;
	}

	static Population randomInitialPopulation(BufferedImage image,
			Map<String, Integer> histogram, Vector<Color> colors,
			boolean pixelClosestColor, int populationSize, double elitismRate) {
		List<Chromosome> list = new LinkedList<Chromosome>();
		for (int i = 0; i < populationSize; i++) {
			list.add(new EllipseListChromosome(
					randomRepresentation(image, colors, pixelClosestColor,
							EllipseListChromosome.AVERAGE_LENGTH()),
					image, histogram, colors));
		}
		return new ElitisticListPopulation(list, list.size(), elitismRate);
	}
}
