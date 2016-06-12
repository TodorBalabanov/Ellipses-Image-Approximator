package eu.veldsoft.ellipses.image.approximator;

class Task implements Runnable {
	private long evaluations;
	private Population population;

	public Task(long evaluations, Population population) {
		super();
		this.evaluations = evaluations;
		this.population = population;
	}

	@Override
	public void run() {
		for (long g = evaluations; g >= 0; g--) {
			population.select();
			population.crossover();
			population.mutate();
			population.evaluate();
			population.survive();
			try {
				Thread.currentThread().sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}
}
