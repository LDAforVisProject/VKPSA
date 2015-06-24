package view.components;

import java.util.Set;

import control.analysisView.AnalysisController;

/**
 * Base class for visualization components.
 * @author RM
 *
 */
public abstract class VisualizationComponent
{
	protected AnalysisController analysisController;
	
	protected VisualizationComponent(AnalysisController analysisController)
	{
		this.analysisController	= analysisController;
	}
	
	public abstract void changeViewMode(double[][] data, Set<Integer> selectedIndices);
}
