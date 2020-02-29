package eu.veldsoft.ellipses.image.approximator;

/**
 * Provide string representation of the object as G Code.
 * 
 * @author Todor Balabanov
 */
interface GCode {
	class Settings {}
	
	/**
	 * Transform the object to G Code.
	 * 
	 * @param xOffset
	 * @param yOffset
	 * @param zDown
	 * @param zUp
	 * @param scale
	 * 
	 * @return G Code sequence.
	 */
	public String toGCode(double xOffset, double yOffset, double zDown, double zUp, double scale);
}
