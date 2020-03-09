package eu.veldsoft.ellipses.image.approximator;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Observer;
import java.util.Random;

import com.ugos.acs.Ant;
import com.ugos.acs.AntGraph;

class Ant4EIA extends Ant {
	private static final double B = 2;
	private static final double Q0 = 0.8;
	private static final double R = 0.1;

	private static final Random PRNG = new Random(System.currentTimeMillis());

	protected Hashtable<Integer, Integer> nodesToVisit = new Hashtable<Integer, Integer>();

	@Override
	protected boolean better(double value1, double value2) {
		return value1 < value2;
	}

	public Ant4EIA(int start, Observer observer) {
		super(start, observer);
	}

	@Override
	public void init() {
		super.init();

		final AntGraph graph = s_antColony.getGraph();

		/*
		 * inizializza l'array di città da visitare
		 */
		nodesToVisit = new Hashtable<Integer, Integer>(graph.nodes());
		for (int i = 0; i < graph.nodes(); i++)
			nodesToVisit.put(new Integer(i), new Integer(i));

		/*
		 * Rimuove la città corrente
		 */
		nodesToVisit.remove(new Integer(m_nStartNode));
	}

	@Override
	public int stateTransitionRule(int current) {
		final AntGraph graph = s_antColony.getGraph();

		/*
		 * generate a random number
		 */
		double q = PRNG.nextDouble();
		int max = -1;

		/*
		 * Exploitation
		 */
		if (q <= Q0) {
			double maxValue = -1;
			double value;
			int node;

			/*
			 * search the max of the value as defined in Eq. a)
			 */
			Enumeration<Integer> en = nodesToVisit.elements();
			while (en.hasMoreElements()) {
				/*
				 * select a node
				 */
				node = ((Integer) en.nextElement()).intValue();

				/*
				 * check on tau
				 */
				if (graph.tau(current, node) == 0)
					throw new RuntimeException("tau = 0");

				/*
				 * get the value
				 */
				value = graph.tau(current, node)
						* Math.pow(graph.etha(current, node), B);

				/*
				 * check if it is the max
				 */
				if (value > maxValue) {
					maxValue = value;
					max = node;
				}
			}
		} else {
			double sum = 0;
			int node = -1;

			/*
			 * get the sum at denominator
			 */
			Enumeration<Integer> en = nodesToVisit.elements();
			while (en.hasMoreElements()) {
				node = ((Integer) en.nextElement()).intValue();
				if (graph.tau(current, node) == 0)
					throw new RuntimeException("tau = 0");

				/*
				 * Update the sum
				 */
				sum += graph.tau(current, node)
						* Math.pow(graph.etha(current, node), B);
			}

			if (sum == 0)
				throw new RuntimeException("SUM = 0");

			/*
			 * get the everage value
			 */
			double average = sum / (double) nodesToVisit.size();

			/*
			 * search the node in agreement with eq. b)
			 */
			en = nodesToVisit.elements();
			while (en.hasMoreElements() && max < 0) {
				node = ((Integer) en.nextElement()).intValue();

				/*
				 * get the value of p as difined in eq. b)
				 */
				double p = (graph.tau(current, node)
						* Math.pow(graph.etha(current, node), B)) / sum;

				/*
				 * if the value of p is greater the the average value the node
				 * is good
				 */
				if ((graph.tau(current, node)
						* Math.pow(graph.etha(current, node), B)) > average) {
					max = node;
				}
			}

			if (max == -1)
				max = node;
		}

		if (max < 0)
			throw new RuntimeException("maxNode = -1");

		/*
		 * delete the selected node from the list of node to visit
		 */
		nodesToVisit.remove(new Integer(max));

		return max;
	}

	@Override
	public void localUpdatingRule(int current, int next) {
		final AntGraph graph = s_antColony.getGraph();

		/*
		 * get the value of the Eq. c)
		 */
		double value = ((double) 1 - R) * graph.tau(current, next)
				+ (R * (graph.tau0()));

		/*
		 * update tau
		 */
		graph.updateTau(current, next, value);
	}

	@Override
	public boolean end() {
		return nodesToVisit.isEmpty();
	}
}
