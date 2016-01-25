package view.components.heatmap;


import view.components.ColorScale;
import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.NumberAxis;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * Heatmap, offering various transformation methods for converting various
 * data sources into a usable (double[n][n]) format.
 */
public class NumericalHeatmap extends Heatmap
{
	/*
	 * GUI elements.
	 */
	
	@FXML private NumberAxis xAxis;
	@FXML private NumberAxis yAxis;
	
	
	// -----------------------------------------------
	//					Methods
	// -----------------------------------------------
	
	@Override
	protected void initAxes()
	{
		xAxis.setAutoRanging(false);
		yAxis.setAutoRanging(false);
		
		xAxis.setForceZeroInRange(false);
		yAxis.setForceZeroInRange(false);
		
    	xAxis.setAnimated(false);
    	yAxis.setAnimated(false);
    	
    	xAxis.setMinorTickVisible(false);
    	yAxis.setMinorTickVisible(false);
	}

	/**
	 * Applies a given option set.
	 * @param options
	 */
	@Override
	public void applyOptions(HeatmapOptionset options)
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
    protected void draw(HeatmapDataset data, boolean useBorders, boolean updateBlockCoordinates)
    {
		draw(data, useBorders);
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
	    	// Set axis label values.
	    	xAxis.setLowerBound(data.getMinKey1());
	    	xAxis.setUpperBound(data.getMaxKey1());
	    	yAxis.setLowerBound(data.getMinKey2());
	    	yAxis.setUpperBound(data.getMaxKey2());
	    	
	    	// Adjust tick width.
	    	final int numberOfTicks = binMatrix.length;
	    	xAxis.setTickUnit( (data.getMaxKey1() - data.getMinKey1()) / numberOfTicks);
	    	yAxis.setTickUnit( (data.getMaxKey2() - data.getMinKey2()) / numberOfTicks);
	    	xAxis.setMinorTickCount(4);
	    	yAxis.setMinorTickCount(4);
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
}
