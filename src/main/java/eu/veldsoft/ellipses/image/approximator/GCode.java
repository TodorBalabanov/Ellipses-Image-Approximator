package eu.veldsoft.ellipses.image.approximator;

/**
 * Provide string representation of the object as G Code.
 * 
 * @author Todor Balabanov
 */
interface GCode {
	/**
	 * Transformation parameters.
	 */
	class Settings {
		double xOffset;
		double yOffset;
		double zDown;
		double zUp;
		double scale;
		
		/**
		 * @param xOffset
		 * @param yOffset
		 * @param zDown
		 * @param zUp
		 * @param scale
		 */
		public Settings(double xOffset, double yOffset, double zDown,
				double zUp, double scale) {
			super();
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.zDown = zDown;
			this.zUp = zUp;
			this.scale = scale;
		}
	}

	/**
	 * Transform the object to G Code.
	 * 
	 * @param configuration
	 *            List of parameters for transformation adjustments.
	 * 
	 * @return G Code sequence.
	 */
	public String toGCode(Settings configuration);
}
