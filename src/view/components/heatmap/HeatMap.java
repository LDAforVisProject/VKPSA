package view.components.heatmap;


import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import view.components.ColorScale;
import view.components.VisualizationComponent;
import view.components.rubberbandselection.RubberBandSelection;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * Heatmap, offering various transformation methods for converting various
 * data sources into a usable (double[n][n]) format.
 */
public class Heatmap extends VisualizationComponent
{
	/*
	 * GUI elements.
	 */
	
	@FXML private Canvas canvas;
	@FXML private NumberAxis xAxis;
	@FXML private NumberAxis yAxis;
	
	/**
	 * Component enabling rubberband-type selection of points in scatterchart.
	 */
	private RubberBandSelection rubberbandSelection;
	
	/*
	 * Metadata.
	 */
	
	/**
	 * Stores index of an cell and the coordinates (minX, minY, maxX, maxY) of this cell.
	 */
	private Map<Pair<Integer, Integer>, double[]> cellsToCoordinates;
	/**
	 * Store which cells were selected.
	 */
	Set<Pair<Integer, Integer>> selectedCellsCoordinates;
	
	/*
	 * Other data.
	 */
	
	/**
	 * Signifies whether the ctrl key is down at any given time.
	 */
	private boolean isCtrlDown;
	
	
	// -----------------------------------------------
	//					Methods
	// -----------------------------------------------
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing heatmap component.");
		
		// Init metadata collections.
		cellsToCoordinates					= new HashMap<Pair<Integer, Integer>, double[]>();
		selectedCellsCoordinates			= new HashSet<Pair<Integer,Integer>>();
		
		// Init axis settings.
		initAxes();
		
		// Init selection tools.
		initSelection();
	}
	
	/**
	 * Applies a given option set.
	 * @param options
	 */
	private void applyOptions(HeatmapOptionset options)
	{
		// Update selection mode.
		if (!options.isSelectionEnabled())
        	rubberbandSelection.disable();
        else
        	rubberbandSelection.enable();
        
		// Update axis labeling.
		xAxis.setLabel(options.getKey1());
		yAxis.setLabel(options.getKey2());
		// Update axis visibility.
    	xAxis.setVisible(options.getShowAxes());
    	yAxis.setVisible(options.getShowAxes());
    	
    	// Update granularity information.
    	setGranularityInformation(options.isGranularityDynamic(), options.getGranularity(), false);
	}
	
	private void initAxes()
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

	private void initSelection()
	{
        // Add rubberband selection tool.
        rubberbandSelection = new RubberBandSelection((Pane) canvas.getParent(), this);
	}
	
	/**
	 * Refresh visualization, given a generic, preprocessed dataset.
	 * @param options
	 * @param data
	 */
	public void refresh(HeatmapOptionset options, HeatmapDataset data)
	{
		this.options 	= options;
		this.data		= data;
		System.out.println("granuarlity rset - before: " + ((HeatmapOptionset)options).getGranularity() + ", " + ((HeatmapOptionset)options).isGranularityDynamic());
    	
		// Apply options.
		applyOptions(options);
		
		// Draw.
		draw(data, false);
	}
	
	/**
	 * Refreshes heatmap 
	 */
	@Override
	public void refresh()
	{
		refresh((HeatmapOptionset)this.options, (HeatmapDataset)this.data);
	}

	@Override
	public void resizeContent(double width, double height)
	{
		if (width > 0)
			canvas.setWidth(width - 68);
		if (height > 0)
			canvas.setHeight(height - 67);
		
		// Refresh heatmap.
		refresh();
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}

    /**
     * Draw data to canvas.
     * @param data
     * @param useBorders 
     */
    private void draw(HeatmapDataset data, boolean useBorders)
    {
    	HeatmapOptionset hOptions = (HeatmapOptionset)options;
    	
    	GraphicsContext gc		= canvas.getGraphicsContext2D();
    	int binMatrix[][]		= data.getBinMatrix();
    	int minOccurenceCount	= data.getMinOccurenceCount();
    	int maxOccurenceCount	= data.getMaxOccurenceCount();
    	
    	// Set stroke color.
    	gc.setStroke(Color.BLACK);
    	
    	// Adjust tick values at both axes.
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
    
    /**
     * Manipulates heatmap's granularity information.
     * @param isGranularityDynamic
     * @param granularity
     * @param update
     */
    public void setGranularityInformation(boolean isGranularityDynamic, int granularity, boolean update)
    {
    	// Cast inherited properties to correct format.
    	HeatmapOptionset hOptions	= (HeatmapOptionset)options;
    	HeatmapDataset hData		= (HeatmapDataset)data;
    	
    	hOptions.setGranularityDynamic(isGranularityDynamic);
    	hOptions.setGranularity(granularity);
    	
    	if (update) {
    		// Re-compile data for new granularity.
    		hData = new HeatmapDataset(hData.getAllLDAConfigurations(), hData.getChosenLDAConfigurations(), hOptions);
    		// Refresh.
    		this.refresh(hOptions, hData);
    	}
    }

	@Override
	public void processSelectionManipulationRequest(double minX, double minY, double maxX, double maxY)
	{	
		// Cast inherited properties to correct format.
		HeatmapOptionset hOptions 	= (HeatmapOptionset)options;
		HeatmapDataset hData		= (HeatmapDataset)data;
				
		// Get GraphicsContext for drawing.
		GraphicsContext gc					= canvas.getGraphicsContext2D();
		
		// Get current metadata.
		final int[][] binMatrix				= hData.getBinMatrix();
		final int minOccurenceCount			= hData.getMinOccurenceCount();
		final int maxOccurenceCount			= hData.getMaxOccurenceCount();
    	
		// Define color spectrum.
		// Set highlight color (red for additional selection, blue for subtractive).
		final Color highlightColor 	= isCtrlDown ? new Color(0.0, 0.0, 1.0, 0.5) : new Color(1.0, 0.0, 0.0, 0.5);
    	
		// Check if settings icon was used. Workaround due to problems with selection's mouse event handling. 
		if (minX == maxX && minY == maxY) {
			final Pair<Integer, Integer> offsets = provideOffsets();
			analysisController.checkIfSettingsIconWasClicked(minX + offsets.getKey(), minY + offsets.getValue(), "settings_paramDist_icon");
		}
		
		// Check which cells are in selected area, highlight them.
		for (Map.Entry<Pair<Integer, Integer>, double[]> cellCoordinateEntry : cellsToCoordinates.entrySet()) {
			double cellMinX = cellCoordinateEntry.getValue()[0];
			double cellMinY = cellCoordinateEntry.getValue()[1];
			double cellMaxX = cellCoordinateEntry.getValue()[2];
			double cellMaxY = cellCoordinateEntry.getValue()[3];
			
			// Set color.
			gc.setFill(highlightColor);
			
			// Get cell ID/location.
			final Pair<Integer, Integer> cellCoordinates = cellCoordinateEntry.getKey();
			
			if (	cellMinX > minX && cellMaxX < maxX &&
					cellMinY > minY && cellMaxY < maxY) {
				// Get cell's contents.
				final Set<Integer> cellContentSet	= hData.getCellsToConfigurationIDs().get(cellCoordinateEntry.getKey()); 

				// Add cell content to collection (if there is any).
				if (cellContentSet != null) {
					// Check if cell('s content) was already selected. If not: Highlight it, add content to selection.  
					if (!selectedCellsCoordinates.contains(cellCoordinates)) {
						// Highlight cell.
						gc.fillRect(cellMinX, cellMinY, cellMaxX - cellMinX, cellMaxY - cellMinY);
						
						// Add to collection of selected cells.
						selectedCellsCoordinates.add(cellCoordinates);
					}
				}
			}
			
			// If not in selected area anymore: Paint in original color, remove from selection.
			else if (selectedCellsCoordinates.contains(cellCoordinates)) {
				// Get row and column.
				final int cellRow		= cellCoordinateEntry.getKey().getKey();
				final int cellColumn 	= cellCoordinateEntry.getKey().getValue();
				
				// Calculate original color.
				Color cellColor = ColorScale.getColorForValue(binMatrix[cellRow][cellColumn], minOccurenceCount, maxOccurenceCount, hOptions.getMinColor(), hOptions.getMaxColor());
				gc.setFill(cellColor);
				
				// Paint cell in original color.
				gc.fillRect(cellMinX, cellMinY, cellMaxX - cellMinX, cellMaxY - cellMinY);
				
				// Remove from selection.
				selectedCellsCoordinates.remove(cellCoordinates);
			}
		}
	}

	@Override
	public void processEndOfSelectionManipulation()
	{
		// Cast inherited properties to correct format.
		HeatmapDataset hData = (HeatmapDataset)data;
				
		// Collect content in all selected cells.
		Set<Integer> selectedLDAConfigIDs = new HashSet<Integer>();
		
		for (Pair<Integer, Integer> cellCoordinates : selectedCellsCoordinates) {
			selectedLDAConfigIDs.addAll(hData.getCellsToConfigurationIDs().get(cellCoordinates));
		}
		
		// Pass references to selected data onward to AnalysisController.
		analysisController.integrateHeatmapSelection(selectedLDAConfigIDs, !isCtrlDown);
	}

	@Override
	public Pair<Integer, Integer> provideOffsets()
	{
		return new Pair<Integer, Integer>(52, 21);
	}

	@Override
	public void processKeyPressedEvent(KeyEvent ke)
	{		
		isCtrlDown = ke.isControlDown();
	}


	@Override
	public void processKeyReleasedEvent(KeyEvent ke)
	{
		isCtrlDown = ke.isControlDown();
	}
}
