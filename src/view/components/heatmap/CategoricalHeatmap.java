package view.components.heatmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mdsj.Data;
import model.LDAConfiguration;
import model.workspace.WorkspaceAction;
import model.workspace.tasks.Task_LoadTopicDistancesForSelection;
import model.workspace.tasks.Task_LoadTopicDistancesForSelection_CD;
import view.components.ColorScale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Pair;

public class CategoricalHeatmap extends Heatmap
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
	
	// -----------------------------------------------
	//					Methods
	// -----------------------------------------------
	
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
	protected void draw(HeatmapDataset data, boolean useBorders)
	{
		// Cast option set.
    	HeatmapOptionset hOptions = (HeatmapOptionset)options;
    	
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
		
		//cellsToCoordinates.clear();
		// Draw each cell in its corresponding place.
//		for (int i = binMatrix.length - 1; i >= 0; i--) {
//			for (int j = binMatrix.length - 1; j >= 0; j--) {
		for (int i = 0; i < binMatrix.length; i++) {
			for (int j = 0; j < binMatrix.length; j++) {		
				// Calculate coordinates (minX, minY, maxX, maxY).
				double[] cellCoordinates	= new double[4];
				cellCoordinates[0]			= cellWidth * i + 1;
				cellCoordinates[1]			= cellHeight * j - 1;
				cellCoordinates[2] 			= cellCoordinates[0] + cellWidth;
				cellCoordinates[3] 			= cellCoordinates[1] + cellHeight;
				
				// Set color for this cell.
				Color cellColor = ColorScale.getColorForValue(binMatrix[i][j] == -1 ? 0 : binMatrix[i][j], minOccurenceCount, maxOccurenceCount, hOptions.getMinColor(), hOptions.getMaxColor());
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

		// Update labels.
    	updateLabels(data, cellsToCoordinates);
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
			analysisController.integrateTMCHeatmapSelection(selectedTopicConfigIDs, !isCtrlDown);
		
		// Clear collection of selected cell (coordinates).
		selectedCellsCoordinates.clear();
	}
}
