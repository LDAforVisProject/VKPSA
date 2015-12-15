package view.components.rubberbandselection;

import javafx.scene.input.KeyEvent;
import javafx.util.Pair;

public interface ISelectableComponent
{
	/**
	 * Process selected coordinate data.
	 * @param endX
	 * @param endY
	 * @param startX
	 * @param startY
	 */
	public void processSelectionManipulationRequest(double minX, double minY, double maxX, double maxY);
	
	/**
	 * Signals end of selection manipulation.
	 */
	public void processEndOfSelectionManipulation();
	
	/**
	 * Provide the necessary offsets from this component to the parent node.
	 * @return
	 */
	public Pair<Integer, Integer> provideOffsets();	
	
	/**
	 * Process KeyPressedEvent captured by controller.
	 * @param ke
	 */
	public void processKeyPressedEvent(KeyEvent ke);
	
	/**
	 * Process KeyReleasedEvent captured by controller.
	 * @param ke
	 */
	public void processKeyReleasedEvent(KeyEvent ke);
	
}
