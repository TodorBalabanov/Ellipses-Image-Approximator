package eu.veldsoft.ellipses.image.approximator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.genetics.Population;

class Util {
	private static ColorComparator euclidean = new EuclideanColorComparator();

	static final Random PRNG = new Random();

	static int DEFAULT_THREAD_POOL_SIZE = 1;

	static final ColorCoordinatesComparator usage = new ColorCoordinatesComparator();

	static String log = "";

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

	static void writeSolution(int width, int height, List<Ellipse> list,
			double fitness, String file) {
		IIOMetadataNode sizeTextEntry = new IIOMetadataNode("tEXtEntry");
		sizeTextEntry.setAttribute("keyword", "size");
		sizeTextEntry.setAttribute("value", "" + list.size());

		IIOMetadataNode fitnessTextEntry = new IIOMetadataNode("tEXtEntry");
		fitnessTextEntry.setAttribute("keyword", "fitness");
		fitnessTextEntry.setAttribute("value", "" + fitness);

		IIOMetadataNode sizeText = new IIOMetadataNode("tEXt");
		sizeText.appendChild(sizeTextEntry);

		IIOMetadataNode fitnessText = new IIOMetadataNode("tEXt");
		fitnessText.appendChild(fitnessTextEntry);

		IIOMetadataNode root = new IIOMetadataNode("javax_imageio_png_1.0");
		root.appendChild(sizeText);
		root.appendChild(fitnessText);

		ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();

		IIOMetadata metadata = writer.getDefaultImageMetadata(
				ImageTypeSpecifier.createFromBufferedImageType(
						BufferedImage.TYPE_INT_ARGB),
				writer.getDefaultWriteParam());

		try {
			metadata.mergeTree("javax_imageio_png_1.0", root);
		} catch (IIOInvalidTreeException e) {
			e.printStackTrace();
		}

		BufferedImage image = Util.drawEllipses(
				new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB),
				list);

		try {
			writer.setOutput(ImageIO
					.createImageOutputStream(new ByteArrayOutputStream()));

			writer.write(metadata, new IIOImage(image, null, metadata),
					writer.getDefaultWriteParam());

			ImageIO.write(image, "png", new File(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
