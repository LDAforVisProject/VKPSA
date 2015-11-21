package view.components.legacy;

import control.analysisView.AnalysisController;

/**
 * Base class for visualization components.
 * @author RM
 *
 */
public abstract class VisualizationComponent_Legacy
{
	protected AnalysisController analysisController;
	
	protected VisualizationComponent_Legacy(AnalysisController analysisController)
	{
		this.analysisController	= analysisController;
	}
	
	public abstract void changeViewMode();
}
