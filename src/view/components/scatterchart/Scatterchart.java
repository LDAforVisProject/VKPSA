package view.components.scatterchart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import model.LDAConfiguration;
import model.workspace.Workspace;

import com.sun.javafx.charts.Legend;

import control.analysisView.AnalysisController;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import view.components.VisualizationComponent;
import view.components.VisualizationComponentType;
import view.components.heatmap.CategoricalHeatmap;
import view.components.heatmap.HeatmapOptionset;
import view.components.heatmap.NumericalHeatmap;
import view.components.legacy.mdsScatterchart.DataPointState;
import view.components.rubberbandselection.RubberBandSelection;

public abstract class Scatterchart extends VisualizationComponent
{
	/*
	 * GUI elements.
	 */
	
	protected @FXML AnchorPane container_anchorPane;
	
	protected @FXML ScatterChart<Number, Number> scatterchart;
	protected @FXML NumberAxis xAxis_numberaxis;
	protected @FXML NumberAxis yAxis_numberaxis;
	
	/**
	 * Density heatmap. Used as overlay when requested; not shown otherwise.
	 */
	NumericalHeatmap densityHeatmap;
	HeatmapOptionset densityHeatmapOptions;
	
	/*
	 * Data needed for selection.
	 */
	
	/**
	 * Selection mode (single or group).
	 */
	protected SelectionMode selectionMode;
	
	/**
	 * Set of point indices changed in current selection action. 
	 */
	protected Set<Integer> pointsManipulatedInCurrSelectionStep;
			
	/**
	 * Stores last drag coordinates.
	 */
	protected MouseEvent lastMouseEvent;
	
	/**
	 * Flips when selection was changed. Refresh of visualizations is only
	 * triggered if this is true.
	 */
	protected boolean changeInSelectionDetected;
	/**
	 * Flips when selection was changed. Local scope visualization refresh is
	 * only triggered if this is true.
	 */
	protected boolean changeInSelectionDetected_localScope;
	
	/**
	 * Map storing all active chart points as values; their respective indices as keys.
	 * Used to track changes over time when user manipulates the current selection.
	 */
	protected Map<Integer, XYChart.Data<Number, Number>> activePoints;
	/**
	 * Map storing all chart points whose status was changed as values; their respective indices as keys.
	 * Used to track changes over time when user manipulates the current selection.
	 */
	protected Map<Integer, XYChart.Data<Number, Number>> inactivePoints;
	
	/*
	 * Data series for scatterchart.
	 */
	
	/**
	 * Data series holding all discarded data points.
	 */
	protected Series<Number, Number> discardedDataSeries;
	/**
	 * Data series holding all filtered data points.
	 */
	protected Series<Number, Number> inactiveDataSeries;
	/**
	 * Data series holding all selected data points.
	 */
	protected Series<Number, Number> activeDataSeries;
	
	/*
	 * Data.
	 */
	
	/**
	 * Data set for this instance of ParameterSpaceScatterchart.
	 */
	protected ScatterchartDataset data;
	/**
	 * Option set for this instance of ParameterSpaceScatterchart.
	 */
	protected ScatterchartOptionset options;
	
	/**
	 * Global extrema in provided data. 
	 */
	protected Map<String, double[]> globalExtrema;
	
	
	// -----------------------------------
	//				Methods
	// -----------------------------------
	
	/**
	 * Initializes basic data.
	 */
	protected void initialize()
	{
		// Init flags.
		changeInSelectionDetected				= false;
		changeInSelectionDetected_localScope	= false;
		isCtrlDown								= false;
		
		// Init other data.
		selectionMode							= SelectionMode.GROUP;
		pointsManipulatedInCurrSelectionStep	= new HashSet<Integer>();
		
		// Initialize data series.
		initDataSeries();
		
		// Initialize selection.
		initSelection();
		
		// Init density heatmap.
		initDensityHeatmap();
	}
	
	/**
	 * Initiailzes density heatmap.
	 */
	private void initDensityHeatmap()
	{
		densityHeatmap = (NumericalHeatmap)VisualizationComponent.generateInstance(VisualizationComponentType.NUMERICAL_HEATMAP, this.analysisController, null, null, null);
		densityHeatmap.embedIn(container_anchorPane);
		
		densityHeatmapOptions	= new HeatmapOptionset(	true, -1, 
														Color.BLUE, Color.DARKBLUE, new Color(0.0, 0.0, 1.0, 0.5), new Color(1.0, 0.0, 0.0, 0.5),
														"", "",
														false, false, true);
		densityHeatmap.applyOptions(densityHeatmapOptions);
	}
	
	/**
	 * Initializes data series.
	 */
	protected void initDataSeries()
	{
		discardedDataSeries						= new Series<Number, Number>();
		inactiveDataSeries						= new Series<Number, Number>();
		activeDataSeries						= new Series<Number, Number>();
		
		discardedDataSeries.setName("Discarded");
		inactiveDataSeries.setName("Inactive");
		activeDataSeries.setName("Active");
		
		activePoints							= new HashMap<Integer, XYChart.Data<Number, Number>>();
		inactivePoints							= new HashMap<Integer, XYChart.Data<Number, Number>>();
	}

	/**
	 * Initializes scatterchart.
	 */
	protected void initScatterchart()
	{
        // Disable auto ranging.
        xAxis_numberaxis.setAutoRanging(false);
        yAxis_numberaxis.setAutoRanging(false);
		
		// Use minimum and maximum to create range.
		xAxis_numberaxis.setForceZeroInRange(false);
		yAxis_numberaxis.setForceZeroInRange(false);
		
        scatterchart.setVerticalGridLinesVisible(true);
        
        // Init automatic update of references to labels in legend.
        updateLegendLabels();
        
        // Initialize zooming capabiliy.
        initZoom();
	}

	/**
	 * Updates references to lables in scatterchart legend.
	 * Sets color according to data series.
	 */
	@SuppressWarnings("restriction")
	protected void updateLegendLabels()
	{
		for (Node n : scatterchart.getChildrenUnmodifiable()) { 
			if (n instanceof Legend) { 
				final Legend legend = (Legend) n;
				
				for (Node legendNode : legend.getChildren()) {
					if (legendNode instanceof Label) {
						Label label = (Label)legendNode;
						
						String txt = "";
                		if (label.getText().contains("Discarded") && data.getDiscardedLDAConfigurations() != null) {
                			txt = "Discarded (" + data.getDiscardedLDAConfigurations().size() + ")";
	            		}
	            		
	            		else if (label.getText().contains("Inactive") && data.getInactiveLDAConfigurations() != null) {
	            			txt = "Inactive (" + (data.getInactiveLDAConfigurations().size() - activePoints.size()) + ")";
	            		}
	            		
	            		else if (label.getText().contains("Active") && data.getInactiveLDAConfigurations() != null) {
	            			txt = "Active (" + activePoints.size() + ")";
	            		}
                		
            			label.setText(txt);
					}
					
				}
	        }
	    }	
	}
	
	/**
	 * Initializes zoom in scatterchart.
	 */
	protected abstract void initZoom();
	
	/**
	 * Identifies global extrema based on provided data. 
	 * @param ldaConfigurations
	 */
	public void identifyGlobalExtrema(ArrayList<LDAConfiguration> ldaConfigurations)
	{
		globalExtrema = identifyExtrema(LDAConfiguration.SUPPORTED_PARAMETERS, ldaConfigurations);
	}
	
	/**
	 * Generates an list of extrema (x-minimum, x-maximum, y-minimum, y-maximum) for every parameter.
	 * @param paramValues Possible parameter values for which extrema have to be identified.
	 * @return
	 */
	protected Map<String, double[]> identifyExtrema(final String[] paramValues, ArrayList<LDAConfiguration> ldaConfigurations)
	{
		Map<String, double[]> extrema = new LinkedHashMap<String, double[]>();
		
		// Init map.
		for (String paramValue : paramValues) {
			extrema.put(paramValue, new double[2]);
			
			// Set initial values.
			extrema.get(paramValue)[0] = Double.MAX_VALUE;
			extrema.get(paramValue)[1] = Double.MIN_VALUE;
		}
		
		// Identify global coordinate extrema.
		for (LDAConfiguration ldaConfig : ldaConfigurations) {
			// For each parameter:
			for (Map.Entry<String, double[]> paramSet : extrema.entrySet()) {
				paramSet.getValue()[0] = 	ldaConfig.getParameter(paramSet.getKey()) < paramSet.getValue()[0] ? 
											ldaConfig.getParameter(paramSet.getKey()) : paramSet.getValue()[0];
				paramSet.getValue()[1] = 	ldaConfig.getParameter(paramSet.getKey()) > paramSet.getValue()[1] ? 
											ldaConfig.getParameter(paramSet.getKey()) : paramSet.getValue()[1];
			}	
		}
		
		return extrema;
	}
	
	/**
	 * Checks if node is positioned within specified bounds.
	 * @param node
	 * @param minX
	 * @param minY
	 * @param maxX
	 * @param maxY
	 * @return
	 */
	protected boolean isNodeWithinBounds(Node node, double minX, double minY, double maxX, double maxY)
	{
		boolean nodeXWithinBounds = node.getLayoutX() <= maxX && node.getLayoutX() >= minX;
		boolean nodeYWithinBounds = node.getLayoutY() <= maxY && node.getLayoutY() >= minY;
		
		return nodeXWithinBounds && nodeYWithinBounds;
	}
	
	@Override
	public void processSelectionManipulationRequest(double minX, double minY, double maxX, double maxY)
	{
		// Check if settings icon was used. Workaround due to problems with selection's mouse event handling. 
		if (minX == maxX && minY == maxY) {
			Pair<Integer, Integer> offsets = provideOffsets();
			analysisController.checkIfSettingsIconWasClicked(minX + offsets.getKey(), minY + offsets.getValue(), "settings_paramDist_icon");
		}
		
		// Set of data points to add to selection. 
		Set<XYChart.Data<Number, Number>> pointsToAddToSelection		= new HashSet<XYChart.Data<Number,Number>>();
		// Set of data points to remove from selection.
		Set<XYChart.Data<Number, Number>> pointsToRemoveFromSelection	= new HashSet<XYChart.Data<Number,Number>>();
		
		// If control is not down: Ignore selected points, add all non-selected in chosen area.
		if (!isCtrlDown) {
			// Iterate over all filtered points.
			for (XYChart.Data<Number, Number> datapoint : inactiveDataSeries.getData()) {
				
				// Point was selected - add to sets of selected indices and selected points.
				if (	isNodeWithinBounds(datapoint.getNode(), minX, minY, maxX, maxY) &&
						!activePoints.containsKey((int)datapoint.getExtraValue())
					) {
					// Set dirty flags.
					changeInSelectionDetected				= true;
					changeInSelectionDetected_localScope	= true;
					
					// Update collection of selected points.
					activePoints.put((int)datapoint.getExtraValue(), datapoint);
					inactivePoints.remove((int)datapoint.getExtraValue());
					
					// Update set of values added to selection in this action.
					pointsToAddToSelection.add(datapoint);
					
					// Mark data point as manipulated in this step.
					pointsManipulatedInCurrSelectionStep.add((int) datapoint.getExtraValue());
				}
			}

			// Iterate over all selected points.
			for (XYChart.Data<Number, Number> datapoint : activeDataSeries.getData()) {
				if (	!isNodeWithinBounds(datapoint.getNode(), minX, minY, maxX, maxY) 	&&
						activePoints.containsKey((int)datapoint.getExtraValue())			&&
						pointsManipulatedInCurrSelectionStep.contains((int) datapoint.getExtraValue())
					) {
					// Set dirty flags.
					changeInSelectionDetected				= true;
					changeInSelectionDetected_localScope	= true;
					
					// Update collection of selected points.
					activePoints.remove((int)datapoint.getExtraValue());
					
					// Update set of values removed from selection in this action.
					pointsToRemoveFromSelection.add(datapoint);
				}
			}
		}
		
		// Else if control is down: Remove selected points in chose area from selection (in data series 0).
		else {
			// Iterate over selected points.
			for (XYChart.Data<Number, Number> datapoint : activeDataSeries.getData()) {
				// Point was deselected - remove from of selected points and selected indices.
				if (isNodeWithinBounds(datapoint.getNode(), minX, minY, maxX, maxY) &&
					activePoints.containsKey((int)datapoint.getExtraValue())
				) {
					// Set dirty flags.
					changeInSelectionDetected				= true;
					changeInSelectionDetected_localScope	= true;
					
					// Update collection of selected points.
					inactivePoints.put((int)datapoint.getExtraValue(), datapoint);
					activePoints.remove((int)datapoint.getExtraValue());
					
					// Update set of values removed from selection in this action.
					pointsToRemoveFromSelection.add(datapoint);			
					
					// Mark data point as manipulated in this step.
					pointsManipulatedInCurrSelectionStep.add((int) datapoint.getExtraValue());
				}
			}
			
			// Iterate over all filtered points.
			for (XYChart.Data<Number, Number> datapoint : inactiveDataSeries.getData()) {
				// Point was deselected - remove from of selected points and selected indices.
				if (!isNodeWithinBounds(datapoint.getNode(), minX, minY, maxX, maxY)	&&
					!activePoints.containsKey((int)datapoint.getExtraValue())			&&
					pointsManipulatedInCurrSelectionStep.contains((int) datapoint.getExtraValue())
				) {
					// Set dirty flags.
					changeInSelectionDetected				= true;
					changeInSelectionDetected_localScope	= true;
					
					// Update collection of selected points.
					activePoints.remove((int)datapoint.getExtraValue());
					inactivePoints.remove((int)datapoint.getExtraValue());
					
					// Update set of values removed from selection in this action.
					pointsToAddToSelection.add(datapoint);						
				}
			}
		}
		
		// Incorporate identified selection changes into data point's series associations.
		if (changeInSelectionDetected) {
			// Add newly selected data points to selection.
			for (XYChart.Data<Number, Number> newlySelectedDataPoint : pointsToAddToSelection) {
				// Change selection status.
				changeDataPointSelectionStatus(newlySelectedDataPoint, true);
				
				// Update collection of selected points.
				activePoints.put((int)newlySelectedDataPoint.getExtraValue(), newlySelectedDataPoint);
			}
			
			// Remove newly de-selected data points to selection.
			for (XYChart.Data<Number, Number> newlyDeselectedDataPoint : pointsToRemoveFromSelection) {
				// Change selection status.
				changeDataPointSelectionStatus(newlyDeselectedDataPoint, false);
			}
			
    		// Reset flag.
    		changeInSelectionDetected = false;
    		
    		// Clear set of active points.
//    		pointsManipulatedInCurrSelectionStep.clear();
//    		activePoints.clear();
    		
    		// Update data series names with number of datasets.
    		updateLegendLabels();
		}
	}
	
	/**
	 * Changes selection status of a point in filtered/selected data series.
	 * @param data
	 * @param selected Indicates that data point should be selected, if this is true.
	 */
	private void changeDataPointSelectionStatus(XYChart.Data<Number, Number> data, boolean selected)
	{
		// Copy data point in question.
		XYChart.Data<Number, Number> dataCopy = new XYChart.Data<Number, Number>(data.getXValue(), data.getYValue());
		dataCopy.setExtraValue(data.getExtraValue());
		
		if (selected) {
			// Remove from filteredDataSeries.
			inactiveDataSeries.getData().remove(data);
			
			// Add copy to selectedDataSeries.
			if (!activeDataSeries.getData().contains(data)) {
				activeDataSeries.getData().add(dataCopy);

				// Add listener for single selection mode.
	    		addSingleSelectionModeMouseListenerToNode(dataCopy, DataPointState.ACTIVE);
			}
		}
		
		else {
			// Remove from selectedDataSeries.
			activeDataSeries.getData().remove(data);
			
			// Add copy to filteredDataSeries.
			if (!inactiveDataSeries.getData().contains(data)) {
				inactiveDataSeries.getData().add(dataCopy);
				
				// Add listener for single selection mode.
	    		addSingleSelectionModeMouseListenerToNode(dataCopy, DataPointState.INACTIVE);
			}
		}
	}
	
	
	/**
	 * Assigns mouse listener for single selection node to data point's node.
	 * @param dataPoint
	 */
	private void addSingleSelectionModeMouseListenerToNode(XYChart.Data<Number, Number> dataPoint, DataPointState state)
	{
		switch (state)
		{
			case INACTIVE:
				dataPoint.getNode().setOnMouseClicked(new EventHandler<MouseEvent>()
				{
				    @Override
				    public void handle(MouseEvent mouseEvent)
				    {       
				    	// If control is not down: Add to selection.
				    	if (!mouseEvent.isControlDown()) {
					    	if (!activePoints.containsKey(dataPoint.getExtraValue())) {
					    		// Update collection of selected points.
								activePoints.put((int)dataPoint.getExtraValue(), dataPoint);

								// Change data point selection status.
								changeDataPointSelectionStatus(dataPoint, true);
					    		
								// Refresh other charts.
					    		analysisController.integrateMDSSelection(activePoints.keySet(), false);
					    	}
				    	}
				    	else
				    		System.out.println("blub");
				    }
				});
			break;
			
			case ACTIVE:
	        	dataPoint.getNode().setOnMouseClicked(new EventHandler<MouseEvent>()
    			{
    			    @Override
    			    public void handle(MouseEvent mouseEvent)
    			    {       
    			    	System.out.println("in event");
    			    	// If control is down: Remove from selection.
    			    	if (isCtrlDown || mouseEvent.isControlDown()) {
				    		// Update collection of selected points.
				    		activePoints.remove(dataPoint.getExtraValue());
							
							// Change data point selection status.
							changeDataPointSelectionStatus(dataPoint, false);
							
							// Refresh other charts.
				    		analysisController.integrateMDSSelection(activePoints.keySet(), false);
    			    	}
    			    }
    			});
			break;
		}
	}
	
	/**
	 * Initializes selection mechanism.
	 */
	protected void initSelection()
	{
		rubberbandSelection = new RubberBandSelection((Pane) scatterchart.getParent(), this);
	}
	
	/**
	 * Refreshes scatterchart component.
	 * @param data
	 */
	public abstract void refresh(ScatterchartDataset data);
	
	@Override
	public void processEndOfSelectionManipulation()
	{
		// Clear selection-step-dependent data collections.
		pointsManipulatedInCurrSelectionStep.clear();
		
		// Update local scope.
		if (changeInSelectionDetected_localScope) {
			// Reset dirty flag.
			changeInSelectionDetected_localScope = false;
			
			// Refresh local scope visualization.
			analysisController.integrateSelection(!isCtrlDown ? activePoints.keySet() : inactivePoints.keySet(), !isCtrlDown);
		}
	}
	
	@Override
	public void processKeyPressedEvent(KeyEvent ke)
	{
    	// Remember if CTRL is down.
    	isCtrlDown = ke.isControlDown();
	}

	@Override
	public void processKeyReleasedEvent(KeyEvent ke)
	{
    	// Check if CAPS was released.
		if (ke.getCode() == KeyCode.CAPS) {
			
    		// Switch selection mode.
    		if (selectionMode == SelectionMode.GROUP) {
    			selectionMode = SelectionMode.SINGULAR;
    		
    			// Disable rubber band selection listener.
    			rubberbandSelection.disable();
    		}
    		
    		else {
    			selectionMode = SelectionMode.GROUP;
    			
    			// Enable rubber band selection listener.
    			rubberbandSelection.enable();
    		}
    	}
    	
		// Remember if CTRL is down.
    	isCtrlDown = ke.isControlDown();	
	}
	
	/**
	 * Auxiliary method to add selected data points to an arbitrary data series in scatterchart. 
	 * @param dataSeries
	 * @param ldaConfigurations
	 * @param xParam
	 * @param yParam
	 * @param isActive
	 */
	protected void addDataPoints(	XYChart.Series<Number, Number> dataSeries, ArrayList<LDAConfiguration> ldaConfigurations,
									String xParam, String yParam, DataPointState state)
	{
        // Add selected data points.
        for (LDAConfiguration ldaConfig : ldaConfigurations) {
    		XYChart.Data<Number, Number> dataPoint = new XYChart.Data<Number, Number>(ldaConfig.getParameter(xParam), ldaConfig.getParameter(yParam));
        	dataPoint.setExtraValue(ldaConfig.getConfigurationID());
        	
        	dataSeries.getData().add(dataPoint);
        	if (state == DataPointState.ACTIVE)
        		activePoints.put(ldaConfig.getConfigurationID(), dataPoint);
        }
	}
	
	/**
	 * Refreshes density heatmap.
	 */
	protected abstract void refreshDensityHeatmap();
	
	@Override
	public void setReferences(AnalysisController analysisController, Workspace workspace, ProgressIndicator logPI, TextArea logTA)
	{
		super.setReferences(workspace, logPI, logTA);
		this.analysisController = analysisController;
		
		// Set references in density heatmap.
		densityHeatmap.setReferences(workspace, logPI, logTA);
	}
	
	/**
	 * Update position of chart's heatmap layer.
	 */
	protected abstract void updateHeatmapPosition();
}
