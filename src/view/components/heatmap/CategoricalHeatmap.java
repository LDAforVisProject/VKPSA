package view.components.heatmap;

import model.workspace.WorkspaceAction;
import view.components.ColorScale;
import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.paint.Color;
import javafx.util.Pair;

public class CategoricalHeatmap extends Heatmap
{
	/*
	 * GUI elements.
	 */
	
	@FXML private CategoryAxis xAxis;
	@FXML private CategoryAxis yAxis;
	
	
	// -----------------------------------------------
	//					Methods
	// -----------------------------------------------
	
	@Override
	protected void initAxes()
	{
    	xAxis.setAnimated(false);
    	yAxis.setAnimated(false);
	}

	@Override
	protected void applyOptions(HeatmapOptionset options)
	{
		super.applyOptions(options);
		
		// Update axis labeling.
		xAxis.setLabel(options.getKey1());
		yAxis.setLabel(options.getKey2());
		// Update axis visibility.
    	xAxis.setVisible(options.getShowAxes());
    	yAxis.setVisible(options.getShowAxes());
	}

	@Override
	protected void draw(HeatmapDataset data, boolean useBorders)
	{
    	HeatmapOptionset hOptions = (HeatmapOptionset)options;
    	
    	GraphicsContext gc		= canvas.getGraphicsContext2D();
    	double binMatrix[][]		= data.getBinMatrix();
    	double minOccurenceCount	= data.getMinOccurenceCount();
    	double maxOccurenceCount	= data.getMaxOccurenceCount();
    	
    	// Set stroke color.
    	gc.setStroke(Color.BLACK);
    	
    	// Adjust tick values for both axes.
    	if (hOptions.getShowAxes()) {
	    	
    	}
    	
    	// Clear canvas.
    	gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    	
		// Calculate cell width and height (a quadratic matrix is assumed).
		final double cellWidth	= canvas.getWidth() / binMatrix.length; 
		final double cellHeight	= canvas.getHeight() / binMatrix.length;
		
		// Draw each cell in its corresponding place.
		for (int i = 0; i < binMatrix.length; i++) {
			for (int j = 0; j < binMatrix[i].length; j++) {
				// Calculate coordinates (minX, minY, maxX, maxY).
				double[] cellCoordinates	= new double[4];
				cellCoordinates[0]			= cellWidth * i + 1;
				cellCoordinates[1]			= cellHeight * (binMatrix.length - 1 - j) - 1;
				cellCoordinates[2] 			= cellCoordinates[0] + cellWidth;
				cellCoordinates[3] 			= cellCoordinates[1] + cellHeight;
				
				// Set color for this cell.
				Color cellColor = ColorScale.getColorForValue(binMatrix[i][j], minOccurenceCount, maxOccurenceCount, hOptions.getMinColor(), hOptions.getMaxColor());
				gc.setFill(cellColor);
				
				// Draw cell.
				gc.fillRect(cellCoordinates[0], cellCoordinates[1], cellWidth, cellHeight);
				
				// Draw borders, if this is desired and cell has content.
				if (useBorders && cellColor != Color.TRANSPARENT)
					gc.strokeRect(cellCoordinates[0], cellCoordinates[1] + 1, cellWidth - 1, cellHeight - 1);
				
				// Add coordinate metadata to collection.
				cellsToCoordinates.put(new Pair<Integer, Integer>(i, j), cellCoordinates);
			}	
		}	
	}
	
	@Override
	public void notifyOfTaskCompleted(final WorkspaceAction workspaceAction)
	{
		if (workspaceAction == WorkspaceAction.LOAD_SPECIFIC_TOPIC_DISTANCES) {
			...
		}
	}
}
