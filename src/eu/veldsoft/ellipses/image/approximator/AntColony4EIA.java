package eu.veldsoft.ellipses.image.approximator;

import java.util.Random;

import com.ugos.acs.Ant;
import com.ugos.acs.AntColony;
import com.ugos.acs.AntGraph;

class AntColony4EIA extends AntColony {
	private static final Random PRNG = new Random(System.currentTimeMillis());

	protected static final double A = 0.1;

	public AntColony4EIA(AntGraph graph, int ants, int iterations) {
		super(graph, ants, iterations);
	}

	@Override
	protected Ant[] createAnts(AntGraph graph, int ants) {
		Ant4EIA.reset();
		Ant4EIA.setAntColony(this);
		Ant4EIA ant[] = new Ant4EIA[ants];
		for (int i = 0; i < ants; i++) {
			ant[i] = new Ant4EIA((int) (graph.nodes() * PRNG.nextDouble()),
					this);
		}

		return ant;
	}

	@Override
	protected void globalUpdatingRule() {
		double evaporation = 0;
		double deposition = 0;

		for (int r = 0; r < m_graph.nodes(); r++) {
			for (int s = 0; s < m_graph.nodes(); s++) {
				if (r == s) {
					continue;
				}

				/*
				 * get the value for deltatau
				 */
				double deltaTau = ((double) 1 / Ant4EIA.s_dBestPathValue)
						* (double) Ant4EIA.s_bestPath[r][s];

				/*
				 * get the value for phermone evaporation as defined in eq. d)
				 */
				evaporation = ((double) 1 - A) * m_graph.tau(r, s);

				/*
				 * get the value for pheromone deposition as defined in eq. d)
				 */
				deposition = A * deltaTau;

				/*
				 * update tau
				 */
				m_graph.updateTau(r, s, evaporation + deposition);
			}
		}
	}

}
