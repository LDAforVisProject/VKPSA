package view.components.heatmap;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import mdsj.Data;
import model.LDAConfiguration;
import model.workspace.WorkspaceAction;
import model.workspace.tasks.Task_LoadTopicDistancesForSelection;
import model.workspace.tasks.Task_LoadTopicDistancesForSelection_CD;
import view.components.ColorScale;
import view.components.DatapointIDMode;
import view.components.VisualizationComponent;
import view.components.VisualizationComponentType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Pair;

public class CategoricalHeatmap extends HeatMap
{
	/*
	 * GUI elements.
	 */
	
	@FXML private CategoryAxis xAxis;
	@FXML private CategoryAxis yAxis;
	
	/**
	 * Custom axis labeling for x-axis, since CategoryAxis' doesn't seem to work properly.
	 */
	private ArrayList<Label> xAxisLabels;
	/**
	 * Custom axis labeling for y-axis, since CategoryAxis' doesn't seem to work properly.
	 */
	private ArrayList<Label> yAxisLabels;
	
	/*
	 * Other data. 
	 */
	
	/**
	 * Task executing the loading of the current topic data (and holding the results).
	 */
	private Task_LoadTopicDistancesForSelection topicDistanceLoadingTask;
	
	/**
	 * IDs of LDA configuration match currently hovered over.
	 * Is marked/highlighted; may be selected by single click.
	 */
	private Pair<Integer, Integer> hoveredOverLDAMatchID;
	/**
	 * Set of LDA match IDs currently highlighted (through selection or cross-vis. highlighting at hover event).
	 */
	private Set<Pair<Integer, Integer>> highlightedLDAConfigIDs;
	
	/**
	 * Map holding coordinates of LDA configuration matches.
	 */
	private Map<Pair<Integer, Integer>, double[]> ldaMatchCoordinates;
	
	// -----------------------------------------------
	//					Methods
	// -----------------------------------------------
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		super.initialize(location, resources);
		
		// Initialize collections for dealing with LDA matches.
		ldaMatchCoordinates 	= new LinkedHashMap<Pair<Integer,Integer>, double[]>();
		highlightedLDAConfigIDs	= new HashSet<Pair<Integer,Integer>>();
	}
	
	/**
	 * Sets up selection mechanism.
	 * Note: Does't use rubberband selection, instead block-wise point-and-click mechanism is used.
	 */
	@Override
	protected void initSelection()
	{
		// Add listener for mouse entry.
		canvas.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	rootNode.getScene().setCursor(Cursor.HAND);
            }
        });
		// Add listener for mouse exit.
		canvas.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	// Change cursor back to default.
            	rootNode.getScene().setCursor(Cursor.DEFAULT);
            	// Clear selection.
            	highlightedLDAConfigIDs.clear();
            	hoveredOverLDAMatchID = null;
            	
            	// Redraw.
            	if (data != null)
            		draw((HeatmapDataset) data, false, false);
            	
	        	// Notify AnalysisController about end of hover action.
	        	analysisController.removeHighlighting(VisualizationComponentType.CATEGORICAL_HEATMAP);
            	
            }
        });
		
		// Add listener for mouse move.
		canvas.addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	// Make copy of old LDA match.
				Pair<Integer, Integer> oldHoveredOverLDAConfigIDs	= 	hoveredOverLDAMatchID != null ?
																		new Pair<Integer, Integer>(hoveredOverLDAMatchID.getKey(), hoveredOverLDAMatchID.getValue()) : null;
				// Resolve mouse position to LDA match.
            	Pair<Integer, Integer> ldaMatch 					= resolveMousePositionToLDAMatch(event.getX(), event.getY());
            	
            	// Check if newly detected LDA config. IDs are different from old ones.
				if (ldaMatch != null && (oldHoveredOverLDAConfigIDs == null || !oldHoveredOverLDAConfigIDs.equals(ldaMatch)) ) {
					// If so: Update reference, then ...
					hoveredOverLDAMatchID = ldaMatch;
					
					// ...redraw (to remove previous marker drawings).
					draw((HeatmapDataset) data, false, false);
					
		        	// Notify AnalysisController about end of hover action.
					if (oldHoveredOverLDAConfigIDs != null)
						analysisController.removeHighlighting(VisualizationComponentType.CATEGORICAL_HEATMAP);
		        	
					// Prepare data for propagation of hover action information.
					Set<Integer> dataPoints = new HashSet<Integer>();
					dataPoints.add(hoveredOverLDAMatchID.getKey());
					dataPoints.add(hoveredOverLDAMatchID.getValue());
		        	// Notify AnalysisController about hover action.
		        	analysisController.highlightDataPoints(dataPoints, DatapointIDMode.CONFIG_ID, VisualizationComponentType.CATEGORICAL_HEATMAP);
				}
            }
        });
		
		// Add listener for mouse click.
		canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	// Clear selection, if control is not pressed.
            	if (!isCtrlDown)
            		highlightedLDAConfigIDs.clear();
            	
            	// Add to current selection.
            	highlightedLDAConfigIDs.add(hoveredOverLDAMatchID);
            	
        		// Gather content in all selected cells.
        		Set<Pair<Integer, Integer>> selectedTopicConfigIDs = new HashSet<Pair<Integer,Integer>>();
        		
        		// Convert selected LDA matches to set of LDA configuration IDs.
        		Set<Integer> selectedLDAConfigurationIDs = new HashSet<Integer>();
        		for(Pair<Integer, Integer> ldaMatch : highlightedLDAConfigIDs) {
        			if ( !selectedLDAConfigurationIDs.contains(ldaMatch.getKey()) )
        				selectedLDAConfigurationIDs.add(ldaMatch.getKey());
        			if ( !selectedLDAConfigurationIDs.contains(ldaMatch.getValue()) )
        				selectedLDAConfigurationIDs.add(ldaMatch.getValue());
        		}
        		
        		// Cross-reference with LDAConfiguration objects to get number of topics.
        		for (LDAConfiguration ldaConfig : data.getAllLDAConfigurations()) {
        			if ( selectedLDAConfigurationIDs.contains(ldaConfig.getConfigurationID()) ) {
        				for (int i = 0; i < ldaConfig.getKappa(); i++) {
        					selectedTopicConfigIDs.add(new Pair<Integer, Integer>(ldaConfig.getConfigurationID(), i));
        				}
        				
        			}
        		}
        		
        		// Pass references to selected data onward to AnalysisController.
        		if (selectedTopicConfigIDs.size() > 0)
        			analysisController.integrateTMCHeatmapSelection(selectedTopicConfigIDs);
            }
        });
	}
	
	/**
	 * Resolves mouse event coordinates to LDA match hovered over/clicked.
	 * @param x
	 * @param y
	 * @return Pair of two integers, if match was identified. Null, if no match was found.
	 */
	private Pair<Integer, Integer> resolveMousePositionToLDAMatch(double x, double y)
	{
		HeatmapDataset hData 							= (HeatmapDataset) this.data;
		Pair<Integer, Integer> hoveredOverLDAConfigIDs 	= null;
		
		if (hData != null) {
		 	for (Map.Entry<Pair<Integer, Integer>, double[]> cellCoordinateEntry : cellsToCoordinates.entrySet()) {
	    		double[] coordinates = cellCoordinateEntry.getValue();
	    		
	    		// Check if cell contains mouse event.
	    		// minx, miny, maxx, maxy
	    		if (x >= coordinates[0] && x <= coordinates[2]) {
	    			if (y >= coordinates[1] && y <= coordinates[3]) {
	    				// Store all values in list.
	    				List<Integer> ldaConfigs = new ArrayList<Integer>(hData.getCellsToConfigurationIDs().get(cellCoordinateEntry.getKey()));
	    				
	    				// If only one LDA configuration ID: Match of one topic model vs. itself.
	    				if (ldaConfigs.size() == 1)
	    					hoveredOverLDAConfigIDs = new Pair<Integer, Integer>(ldaConfigs.get(0), ldaConfigs.get(0));
	        			// Otherwise: Match of one topic model vs. another.
	    				else if (ldaConfigs.size() == 2)
	    					hoveredOverLDAConfigIDs = new Pair<Integer, Integer>(ldaConfigs.get(0), ldaConfigs.get(1));
	    				else {
	    					System.out.println("### ERROR ### More than two LDA configurations in CategoricalHeatmap entry detected.");
	    					log("### ERROR ### More than two LDA configurations in CategoricalHeatmap entry detected.");
	    				}
	    				
	    				// Return match.
	    				return hoveredOverLDAConfigIDs;
	        		}	
	    		}
	    	}
		}
	 	
	 	return null;
	}
	
	@Override
	protected void initAxes()
	{
    	xAxis.setAnimated(false);
    	yAxis.setAnimated(false);
    	
    	xAxisLabels = new ArrayList<Label>();
    	yAxisLabels = new ArrayList<Label>();
	}

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
    	
    	xAxis.setAutoRanging(false);
    	xAxis.setGapStartAndEnd(false);
	}
	
	/**
	 * Update customized labels on both axes.
	 * @param data
	 * @param cellsToCoordinates 
	 */
	private void updateLabels(HeatmapDataset data, Map<Pair<Integer, Integer>, double[]> cellsToCoordinates)
	{
		// Prepare for graphical seperation of LDA configurations.
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(0.5);
		
		// Remove old labels.
    	for (Label label : xAxisLabels) {
    		parent_anchorpane.getChildren().remove(label);
    	}
    	for (Label label : yAxisLabels) {
    		parent_anchorpane.getChildren().remove(label);
    	}
    	xAxisLabels.clear();
    	yAxisLabels.clear();
    	
    	// Get number of topics in total.
    	int numberOfTopics = 0;
    	for (LDAConfiguration ldaConfig : data.getAllLDAConfigurations()) {
    		numberOfTopics += ldaConfig.getKappa();
    	}
    	
    	// Current position on x-axis.
    	double currXPos			= 0;
    	// Current position on y-axis.
    	double currYPos			= 0;
    	
    	// Base interval per topic on x-axis.
    	double[] defaultCell	= cellsToCoordinates.get(new Pair<Integer, Integer>(0, 0));
    	final double xInterval 	= defaultCell[2] - defaultCell[0];
    	// Base interval per topic on y-axis.
    	final double yInterval 	= defaultCell[3] - defaultCell[1];
    	
//    	for (int i = data.getAllLDAConfigurations().size() - 1; i >= 0; i--) {
    	for (int i = 0; i < data.getAllLDAConfigurations().size(); i++) {
    		LDAConfiguration ldaConfig = data.getAllLDAConfigurations().get(i);  
    				
    		// Calculate width of this block on x-axis.
    		double xDiff = xInterval * ldaConfig.getKappa();
    		double yDiff = yInterval * ldaConfig.getKappa();
    		
    		// For x-axis.
    		Label xLabel = new Label();
    		xLabel.setText( String.valueOf(ldaConfig.getConfigurationID()) );
    		xLabel.setLayoutX(xAxis.getLayoutX() + currXPos + xDiff / 2);
    		xLabel.setLayoutY(xAxis.getLayoutY() + 10);
    		xLabel.setFont(new Font(9));
    		
    		// For y-axis.
    		Label yLabel = new Label();
    		yLabel.setText( String.valueOf(ldaConfig.getConfigurationID()) );
    		yLabel.setLayoutX(yAxis.getLayoutX() + 15);
    		yLabel.setLayoutY(yAxis.getLayoutY() + currYPos + yDiff / 2);
    		yLabel.setAlignment(Pos.CENTER_RIGHT);
    		yLabel.setFont(new Font(9));
    		
    		// Draw separation line between LDA configurations.
    		if (currXPos != 0) {
	    		gc.strokeLine(currXPos, 0, currXPos, canvas.getHeight() - 2);
	    		gc.strokeLine(0, currYPos, canvas.getWidth(), currYPos);
    		}
    		
    		// Update current positions on axes.
    		currXPos += xDiff;
    		currYPos += yDiff;
    		
    		// Add label to collection and to parent.
    		xAxisLabels.add(xLabel);
    		yAxisLabels.add(yLabel);
    		parent_anchorpane.getChildren().add(xLabel);
    		parent_anchorpane.getChildren().add(yLabel);
    	}
	}
	
	/**
	 * Clears canvas.
	 */
	public void clear()
	{
		// Reset data.
		this.data 		= null;
		this.options	= null;
		
    	// Clear canvas.
		canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		// Clear axis labels.
    	for (Label label : xAxisLabels) {
    		parent_anchorpane.getChildren().remove(label);
    	}
    	for (Label label : yAxisLabels) {
    		parent_anchorpane.getChildren().remove(label);
    	}
    	xAxisLabels.clear();
    	yAxisLabels.clear();
	}
	
	@Override
	protected void draw(HeatmapDataset data, boolean useBorders, boolean updateBlockCoordinates)
	{
		// Cast option set.
    	HeatmapOptionset hOptions = (HeatmapOptionset)options;
    	
    	// Prepare map for match coordinates.
    	if (updateBlockCoordinates) {
	    	ldaMatchCoordinates.clear();
	    	for (LDAConfiguration lda1 : data.getAllLDAConfigurations()) {
	    		for (LDAConfiguration lda2 : data.getAllLDAConfigurations()) {
	        		ldaMatchCoordinates.put(new Pair<Integer, Integer>(lda1.getConfigurationID(), lda2.getConfigurationID()), null);
	        	}	
	    	}
    	}
    	
    	// Prepare drawing.
    	GraphicsContext gc			= canvas.getGraphicsContext2D();
    	double binMatrix[][]		= data.getBinMatrix();
    	double minOccurenceCount	= data.getGlobalExtrema() == null ? data.getMinOccurenceCount() : data.getGlobalExtrema().getKey();
    	double maxOccurenceCount	= data.getGlobalExtrema() == null ? data.getMaxOccurenceCount() : data.getGlobalExtrema().getValue();
    	
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
			for (int j = 0; j < binMatrix.length; j++) {	
				// Remember cell indices.
				Pair<Integer, Integer> cellIndices	= new Pair<Integer, Integer>(i, j); 
				// Fetch LDA configurations involved in current cell.
				Pair<Integer, Integer> ldaMatchID 	= getLDAMatchForCell(data, cellIndices);
				
				// Calculate cell coordinates (minX, minY, maxX, maxY).
				double[] cellCoordinates	= new double[4];
				cellCoordinates[0]			= cellWidth * i + 1;
				cellCoordinates[1]			= cellHeight * j - 1;
				cellCoordinates[2] 			= cellCoordinates[0] + cellWidth;
				cellCoordinates[3] 			= cellCoordinates[1] + cellHeight;
				
				// Set color for this cell.
				Color cellColor = ColorScale.getColorForValue(binMatrix[i][j] == -1 ? 0 : binMatrix[i][j], minOccurenceCount, maxOccurenceCount, hOptions.getMinColor(), hOptions.getMaxColor());
				// Adapt cell opacity to current hover events (i.e.: Lower opacity, if other LDA config. is hovered over). Ignore transparent cells.
				if ( (	hoveredOverLDAMatchID != null || 
						highlightedLDAConfigIDs != null && isDisplayingExternalHoverEvent ) && 
						cellColor != Color.TRANSPARENT) {
					cellColor = Color.hsb(	cellColor.getHue(), cellColor.getSaturation(), cellColor.getBrightness(), 
											ldaMatchID.equals(hoveredOverLDAMatchID) || highlightedLDAConfigIDs.contains(ldaMatchID) ? 1 : VisualizationComponent.HOVER_OPACITY_FACTOR);
				}
				
				// Set fill color.
				gc.setFill(cellColor);
				// Draw cell.
				gc.fillRect(cellCoordinates[0], cellCoordinates[1], cellWidth, cellHeight);
				
				// Draw borders, if this is desired and cell has content.
				if (useBorders && cellColor != Color.TRANSPARENT)
					gc.strokeRect(cellCoordinates[0], cellCoordinates[1] + 1, cellWidth - 1, cellHeight - 1);
				
				// Add coordinate metadata to collection.
				cellsToCoordinates.put(cellIndices, cellCoordinates);
				
				// Update coordinates of LDA matches.
				if (updateBlockCoordinates)
					updateLDAMatchCoordinates(data, cellIndices); 
			}	
		}
		
		// Update labels.
    	updateLabels(data, cellsToCoordinates);
		
	}
	
	@Override
	protected void draw(HeatmapDataset data, boolean useBorders)
	{
		draw(data, useBorders, true);
	}
	
	/**
	 * Returns LDA match IDs as pair. 
	 * @param data
	 * @param cellIndices
	 * @return
	 */
	private Pair<Integer, Integer> getLDAMatchForCell(HeatmapDataset data, Pair<Integer, Integer> cellIndices)
	{
		// Fetch LDA configurations involved in match.
		List<Integer> ldaConfigurationIDs 	= new ArrayList<Integer>(data.getCellsToConfigurationIDs().get(cellIndices));
		// Convert to pair.
		Pair<Integer, Integer> ldaMatchID	= null;
		if (ldaConfigurationIDs.size() == 1)
			ldaMatchID = new Pair<Integer, Integer>(ldaConfigurationIDs.get(0), ldaConfigurationIDs.get(0));
		else if (ldaConfigurationIDs.size() == 2)
			ldaMatchID = new Pair<Integer, Integer>(ldaConfigurationIDs.get(0), ldaConfigurationIDs.get(1));
		
		return ldaMatchID;
	}
	
	/**
	 * Update coordinates of LDA match associated with particular cell.
	 * @param cellIndices
	 */
	private void updateLDAMatchCoordinates(HeatmapDataset data, Pair<Integer, Integer> cellIndices)
	{
		// Convert to pair.
		Pair<Integer, Integer> ldaMatchID	= getLDAMatchForCell(data, cellIndices);
		
		// Get corresponding coordinates from map containing LDA match coordinates.
		double[] matchCoordinates			= ldaMatchCoordinates.get(ldaMatchID);
		// Get cell coordinates.
		double[] cellCoordinates			= cellsToCoordinates.get(cellIndices);
		
		// If coordinates not set yet: Set coordinates of cell.
		if (matchCoordinates == null) {
			ldaMatchCoordinates.put(ldaMatchID, cellCoordinates);
		}
		
		// If not null: Compare coordinates to find maximum/minimum.
		else {
			double[] updateMatchCoordinates = new double[4];
			updateMatchCoordinates[0] 		= matchCoordinates[0] > cellCoordinates[0] ? cellCoordinates[0] : matchCoordinates[0];
			updateMatchCoordinates[1] 		= matchCoordinates[1] > cellCoordinates[1] ? cellCoordinates[1] : matchCoordinates[1];
			updateMatchCoordinates[2] 		= matchCoordinates[2] < cellCoordinates[2] ? cellCoordinates[2] : matchCoordinates[2];
			updateMatchCoordinates[3] 		= matchCoordinates[3] < cellCoordinates[3] ? cellCoordinates[3] : matchCoordinates[3];
			
			ldaMatchCoordinates.put(ldaMatchID, updateMatchCoordinates);
		}
	}
	
	/**
	 * Fetch topic distance data for requested LDA configurations from DB asynchronously.
	 * @param ldaConfigurations
	 * @param options
	 */
	public void fetchTopicDistanceData(ArrayList<LDAConfiguration> ldaConfigurations, HeatmapOptionset options)
	{
		log("Loading topic distance data.");
		
		if (ldaConfigurations.size() > 0) {
			this.options = options;
			
			// If new LDA configuration mash-up: Clean old selection.
			hasDataChanged = data == null || !(ldaConfigurations.equals(data.getAllLDAConfigurations())); 
			if (hasDataChanged) {
				highlightedLDAConfigIDs.clear();
			}
			
			// Load topic distance data for selection.
			topicDistanceLoadingTask = new Task_LoadTopicDistancesForSelection(workspace, WorkspaceAction.LOAD_SPECIFIC_TOPIC_DISTANCES, null, ldaConfigurations);
			topicDistanceLoadingTask.addListener(this);
			
			// Start thread.
			(new Thread(topicDistanceLoadingTask)).start();
		}
	}
	
	@Override
	public void notifyOfTaskCompleted(final WorkspaceAction workspaceAction)
	{
		if (workspaceAction == WorkspaceAction.LOAD_SPECIFIC_TOPIC_DISTANCES) {
			log("Loaded topic distance data.");
			
			// Create dataset.
			System.out.println(topicDistanceLoadingTask);
		
			this.data = new HeatmapDataset(	topicDistanceLoadingTask.getLDAConfigurationsToLoad(), topicDistanceLoadingTask.getSpatialIDsForLDATopicConfiguration(), 
											topicDistanceLoadingTask.getTopicDistances(), topicDistanceLoadingTask.getTopicDistanceExtrema(), (HeatmapOptionset)options);
			
			// Refresh heatmap.
			this.refresh();
		}
	}
	
	@Override
	public void processEndOfSelectionManipulation()
	{
		// Cast inherited properties to correct format.
		HeatmapDataset hData = (HeatmapDataset)data;
				
		// Gathre content in all selected cells.
		Set<Pair<Integer, Integer>> selectedTopicConfigIDs = new HashSet<Pair<Integer,Integer>>();
		
		for (Pair<Integer, Integer> cellCoordinates : selectedCellsCoordinates) {
			selectedTopicConfigIDs.addAll(hData.getCellsToTopicConfigurationIDs().get(cellCoordinates));
		}
		
		// Pass references to selected data onward to AnalysisController.
		if (selectedTopicConfigIDs.size() > 0)
			analysisController.integrateTMCHeatmapSelection(selectedTopicConfigIDs);
		
		// Clear collection of selected cell (coordinates).
		selectedCellsCoordinates.clear();
	}
	
	@Override
	public void initHoverEventListeners()
	{
		// Hover listener is already implemented as part of selection mechanism.
	}
	
	@Override
	public void highlightHoveredOverDataPoints(Set<Integer> dataPointIDs, DatapointIDMode idMode)
	{
		isDisplayingExternalHoverEvent = true;
		
		if (idMode == DatapointIDMode.CONFIG_ID) {
			if (data != null) {
				highlightedLDAConfigIDs.clear();
				
				// Iterate over all LDA configurations.
				for (LDAConfiguration lda1 : data.getAllLDAConfigurations()) {
					// If LDA configuration is to highlight:
					if ( dataPointIDs.contains(lda1.getConfigurationID()) ) {
						// Store all LDA matches with this LDA configuration in collection of LDA matches to highlight.
						for (LDAConfiguration lda2 : data.getAllLDAConfigurations()) {
							highlightedLDAConfigIDs.add(new Pair<Integer, Integer>(lda1.getConfigurationID(), lda2.getConfigurationID()));
							highlightedLDAConfigIDs.add(new Pair<Integer, Integer>(lda2.getConfigurationID(), lda1.getConfigurationID()));
						}		
					}
				}
				
				// Redraw.
				if (data != null)					
					draw((HeatmapDataset) data, false, false);
			}
		}
		
		else {
			System.out.println("### ERROR ### DatapointIDMode.INDEX not supported for CategoricalHeatmap.");
			log("### ERROR ### DatapointIDMode.INDEX not supported for CategoricalHeatmap.");
		}
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
	public void removeHoverHighlighting()
	{
		isDisplayingExternalHoverEvent = false;
		
		// Reset variables storing hovered over/selected LDA matches.
		if (highlightedLDAConfigIDs != null) {
			highlightedLDAConfigIDs.clear();
		}
		
		// Redraw.
		if (data != null)
			draw((HeatmapDataset) data, false, true);
	}
}
