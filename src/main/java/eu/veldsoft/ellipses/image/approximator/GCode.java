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
		/** Comments switch on/off flag. */
		boolean comments;
		/** X home of the plotter working area. */
		double xHome;
		/** Y home of the plotter working area. */
		double yHome;
		/** X offset of the plotter working area. */
		double xOffset;
		/** Y offset of the plotter working area. */
		double yOffset;
		/** Z value in millimeters for the pen up state. */
		double zDown;
		/** Z value in millimeters for the pen down state. */
		double zUp;
		/** Scaling factor for transformation from pixels to milliliters. */
		double scaleWidth;
		/** Scaling factor for transformation from pixels to milliliters. */
		double scaleHeight;
		/** Paint refill time in seconds. */
		double penRefillTime;
		/** Paint refill number of times. */
		int penRefillCount;
		/** Color change time in seconds. */
		double colorChangeTime;

		/**
		 * Constructor for setup of the all fields.
		 * 
		 * @param comments
		 *            Comments switch on/off flag.
		 * @param xHome
		 *            X home of the plotter working area.
		 * @param yHome
		 *            Y home of the plotter working area.
		 * @param xOffset
		 *            X offset of the plotter working area.
		 * @param yOffset
		 *            Y offset of the plotter working area.
		 * @param zDown
		 *            Z value in millimeters for the pen up state.
		 * @param zUp
		 *            Z value in millimeters for the pen down state.
		 * @param scaleWidth
		 *            Width scaling factor for transformation from pixels to
		 *            milliliters.
		 * @param scaleHeight
		 *            Height scaling factor for transformation from pixels to
		 *            milliliters.
		 * @param penRefillTime
		 *            Paint refill time in seconds.
		 * @param penRefillCount
		 *            Paint refill amount of times.
		 * @param colorChangeTime
		 *            Color change time in seconds.
		 */
		public Settings(boolean comments, double xHome, double yHome,
				double xOffset, double yOffset, double zDown, double zUp,
				double scaleWidth, double scaleHeight, double penRefillTime, int penRefillCount,
				double colorChangeTime) {
			super();
			this.comments = comments;
			this.xHome = xHome;
			this.yHome = yHome;
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.zDown = zDown;
			this.zUp = zUp;
			this.scaleWidth = scaleWidth;
			this.scaleHeight = scaleHeight;
			this.penRefillTime = penRefillTime;
			this.penRefillCount = penRefillCount;
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
