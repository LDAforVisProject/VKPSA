package view.components.legacy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import view.components.DatapointIDMode;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import control.analysisView.AnalysisController;

/**
 * Base class for visualization components.
 * @author RM
 *
 */
public abstract class VisualizationComponent_Legacy
{
	protected AnalysisController analysisController;
	
	/**
	 * Generates new instance of {@link VisualizationComponent_Legacy}.
	 * @param analysisController
	 */
	protected VisualizationComponent_Legacy(AnalysisController analysisController)
	{
		this.analysisController	= analysisController;
	}
	
	/**
	 * Change view mode.
	 */
	public abstract void changeViewMode();
	
	/**
	 * Initialize hover event listeners.
	 */
	public abstract void initHoverEventListeners();
	
	/**
	 * Highlights all atomic visual entities containing the delivered data point IDs. 
	 * @param dataPointIDs
	 * @param idMode
	 */
	public abstract void highlightHoveredOverDataPoints(Set<Integer> dataPointIDs, DatapointIDMode idMode);
	
	/**
	 * Wrapper for highlightHoveredOverDataPoints() - used for single data points.
	 * @param dataPointIDs
	 * @param idMode
	 */
	public void highlightHoveredOverDataPoint(int dataPointID, DatapointIDMode idMode)
	{
		Set<Integer> dataPointIDs = new HashSet<Integer>();
		dataPointIDs.add(dataPointID);
		
		highlightHoveredOverDataPoints(dataPointIDs, idMode);
	}
	
	/**
	 * Removes highlighting / repaints with default settings.
	 */
	public abstract void removeHoverHighlighting();
}
