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
		/** X offset of the plotter working area. */
		double xOffset;
		/** Y offset of the plotter working area. */
		double yOffset;
		/** Z value in millimeters for the pen up state. */
		double zDown;
		/** Z value in millimeters for the pen down state. */
		double zUp;
		/** Scaling factor for transformation from pixels to milliliters. */
		double scale;
		/** Paint refill time in seconds. */
		double penRefillTime;
		/** Color change time in seconds. */
		double colorChangeTime;
		
		/**
		 * Constructor for setup of the all fields.
		 * 
		 * @param xOffset X offset of the plotter working area.
		 * @param yOffset Y offset of the plotter working area.
		 * @param zDown Z value in millimeters for the pen up state.
		 * @param zUp Z value in millimeters for the pen down state.
		 * @param scale Scaling factor for transformation from pixels to milliliters.
		 * @param penRefillTime Paint refill time in seconds.
		 * @param colorChangeTime Color change time in seconds.
		 */
		public Settings(double xOffset, double yOffset, double zDown,
				double zUp, double scale, double penRefillTime, double colorChangeTime) {
			super();
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.zDown = zDown;
			this.zUp = zUp;
			this.scale = scale;
			this.penRefillTime = penRefillTime;
			this.colorChangeTime = colorChangeTime;
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
