package view.components.rubberbandselection;

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
	 * Provide the necessary offsets from this component to the parent node.
	 * @return
	 */
	public Pair<Integer, Integer> provideOffsets();
	/**
	 * Signals end of selection manipulation.
	 */
	public void processEndOfSelectionManipulation();
}
