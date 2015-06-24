package view.components;

import control.AnalysisController;

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
	
	public abstract void changeViewMode(double[][] data);
}
