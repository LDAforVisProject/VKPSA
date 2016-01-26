package view.components.heatmap;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import model.LDAConfiguration;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import view.components.ColorScale;
import view.components.VisualizationComponent;
import view.components.rubberbandselection.RubberBandSelection;

public abstract class HeatMap extends VisualizationComponent
{
	/*
	 * GUI elements.
	 */
	
	@FXML protected AnchorPane parent_anchorpane;
	
	@FXML protected Canvas canvas;
	
	/*
	 * Metadata.
	 */
	
	/**
	 * Stores index of an cell and the coordinates (minX, minY, maxX, maxY) of this cell.
	 */
	protected Map<Pair<Integer, Integer>, double[]> cellsToCoordinates;
	/**
	 * Store which cells were selected.
	 */
	protected Set<Pair<Integer, Integer>> selectedCellsCoordinates;
	
	/**
	 * Flag indicating if new dataset differs from old one in terms of LDA configurations involved.
	 */
	protected boolean hasDataChanged;
	
	
	// -----------------------------------------------
	//					Methods
	// -----------------------------------------------
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing heatmap component.");
		
		// Init metadata collections.
		cellsToCoordinates					= new LinkedHashMap<Pair<Integer, Integer>, double[]>();
		selectedCellsCoordinates			= new HashSet<Pair<Integer,Integer>>();
		
		// Init axis settings.
		initAxes();
		
		// Init selection tools.
		initSelection();
	}
	
	/**
	 * Initialize axes.
	 */
	protected abstract void initAxes();
	
	/**
	 * Initializes the rubberband selection tool.
	 */
	protected void initSelection()
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
		this.options 		= options;
		this.data			= data;
    	
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
		if (this.options != null && this.data != null)
			refresh((HeatmapOptionset)this.options, (HeatmapDataset)this.data);
	}
	
	/**
	 * Applies a given option set.
	 * @param options
	 */
	public void applyOptions(HeatmapOptionset options)
	{
		this.options = options;
		
		// Update selection mode.
		if (!options.isSelectionEnabled() && rubberbandSelection != null)
        	rubberbandSelection.disable();
        else if (rubberbandSelection != null)
        	rubberbandSelection.enable();
    	
    	// Update granularity information.
    	setGranularityInformation(options.isGranularityDynamic(), options.getGranularity(), false);
	}
	
    /**
     * Draw data to canvas.
     * @param data
     * @param useBorders 
     */
	protected abstract void draw(HeatmapDataset data, boolean useBorders);
	
	 /**
     * Draw data to canvas. Allows distinction between draw operation including block update or not.
     * @param data
     * @param useBorders
     * @param updateBlockCoordinates 
     */
	protected abstract void draw(HeatmapDataset data, boolean useBorders, boolean updateBlockCoordinates);
	
    /**
     * Manipulates heatmap's granularity information.
     * @param isGranularityDynamic
     * @param granularity
     * @param update
     */
    public void setGranularityInformation(boolean isGranularityDynamic, int granularity, boolean update)
    {
    	if (this.data != null && this.options != null) {
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
    }
	
	@Override
	public void processSelectionManipulationRequest(double minX, double minY, double maxX, double maxY)
	{
		// Cast inherited properties to correct format.
		HeatmapOptionset hOptions 	= (HeatmapOptionset)options;
		HeatmapDataset hData		= (HeatmapDataset)data;
		
		if (hOptions.isSelectionEnabled()) {
			// Get GraphicsContext for drawing.
			GraphicsContext gc					= canvas.getGraphicsContext2D();
			
			// Get current metadata.
			final double[][] binMatrix			= hData.getBinMatrix();
			final double minOccurenceCount		= hData.getMinOccurenceCount();
			final double maxOccurenceCount		= hData.getMaxOccurenceCount();
	    	
			// Define color spectrum.
			// Set highlight color (red for additional selection, blue for subtractive).
			final Color highlightColor 	= isCtrlDown ? hOptions.getSubtractiveSelectionColor() : hOptions.getAdditiveSelectionColor(); 
			
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
		analysisController.integrateSelection(selectedLDAConfigIDs, !isCtrlDown);
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

	@Override
	public void resizeContent(double width, double height)
	{
		if (width > 0)
			canvas.setWidth(width - 79);
		if (height > 0)
			canvas.setHeight(height - 68);
		
		// Refresh heatmap.
		refresh();
	}

	@Override
	public Pair<Integer, Integer> provideOffsets()
	{
		return new Pair<Integer, Integer>(62, 21);
	}
	
	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}
}