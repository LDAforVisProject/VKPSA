package view.components;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import javafx.scene.layout.Pane;
import control.analysisView.AnalysisController;
import view.components.rubberbandselection.ISelectableComponent;
import view.components.rubberbandselection.RubberBandSelection;

enum SelectionMode
{
	SINGULAR, GROUP
};

public class MDSScatterchart extends VisualizationComponent implements ISelectableComponent
{
	/*
	 * GUI elements.
	 */
	
	private ScatterChart<Number, Number> scatterchart;
	private NumberAxis scatterchart_xAxis;
	private NumberAxis scatterchart_yAxis;
	
	private RubberBandSelection rubberbandSelection;
	
	/*
	 * Other data.
	 */
	
	private SelectionMode selectionMode;
	private boolean isCtrlDown;
	
	/**
	 * Flips when selection was changed. Refresh of visualizations is only
	 * triggered if this is true.
	 */
	private boolean changeInSelectionDetected;
	/**
	 * Flips when selection was changed. Local scope visualization refresh is
	 * only triggered if this is true.
	 */
	private boolean changeInSelectionDetected_localScope;
	
	/**
	 * Map storing all selected MDS chart points as values; their respective indices as keys.
	 */
	private Map<Integer, XYChart.Data<Number, Number>> selectedMDSPoints;
	
	/**
	 * Global coordinate extrema on x axis (absolute; not filter- or selection-specific).
	 */
	private Pair<Double, Double> globalCoordinateExtrema_X;
	/**
	 * Global coordinate extrema on y axis (absolute; not filter- or selection-specific).
	 */
	private Pair<Double, Double> globalCoordinateExtrema_Y;
	
	/**
	 * Reference to this workspace's coordinate collection.
	 */
	private double coordinates[][];
	/**
	 * Reference to this workspace's collection of filtered indices.
	 */
	private Set<Integer> indices;
	
	
	public MDSScatterchart(AnalysisController analysisController, ScatterChart<Number, Number> scatterchart)
	{
		super(analysisController);
		
		this.scatterchart						= scatterchart;
		
		// Init collection of selected data points in the MDS scatterchart.
		selectedMDSPoints						= new HashMap<Integer, XYChart.Data<Number, Number>>();
		globalCoordinateExtrema_X				= new Pair<Double, Double>(Double.MAX_VALUE, Double.MIN_VALUE);
		globalCoordinateExtrema_Y				= new Pair<Double, Double>(Double.MAX_VALUE, Double.MIN_VALUE);
		
		// Init flags.
		changeInSelectionDetected				= false;
		changeInSelectionDetected_localScope	= false;
		
		// Init scatterchart.
		initScatterchart();
	}

	private void initScatterchart()
	{
		// Init scatterchart.
		scatterchart_xAxis = (NumberAxis) scatterchart.getXAxis();
        scatterchart_yAxis = (NumberAxis) scatterchart.getYAxis();
        
        // Disable auto ranging.
    	scatterchart_xAxis.setAutoRanging(false);
		scatterchart_yAxis.setAutoRanging(false);
		
		// Use minimum and maximum to create range.
		scatterchart_xAxis.setForceZeroInRange(false);
		scatterchart_yAxis.setForceZeroInRange(false);
		
        scatterchart.setVerticalGridLinesVisible(true);
        
        // Add rubberband selection tool.
        rubberbandSelection = new RubberBandSelection((Pane) scatterchart.getParent(), this);
	}
	
	public void addKeyListener(Scene scene)
	{
		selectionMode	= SelectionMode.GROUP;
		isCtrlDown		= false;
		
		// Ensure that CAPS LOCK is off.
		//Toolkit.getDefaultToolkit().setLockingKeyState(java.awt.event.KeyEvent.VK_CAPS_LOCK, false);
		
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) 
            {
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
        });
        
        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) 
            {
            	// Remember if CTRL is down.
            	isCtrlDown = ke.isControlDown();
            }
        });
	}
	
	public void identifyGlobalExtrema(double[][] coordinates)
	{
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		
		// Identify global coordinate extrema.
		for (int i = 0; i < coordinates[0].length; i++) {
			// Search for minima.
			minX = coordinates[0][i] < minX ? coordinates[0][i] : minX;
			minY = coordinates[1][i] < minY ? coordinates[1][i] : minY;
			
			// Search for maxima.
			maxX = coordinates[0][i] > maxX ? coordinates[0][i] : maxX;
			maxY = coordinates[1][i] > maxY ? coordinates[1][i] : maxY;			
		}
		
		globalCoordinateExtrema_X = new Pair<Double, Double>(minX, maxX);
		globalCoordinateExtrema_Y = new Pair<Double, Double>(minY, maxY);
	}
	
	/**
	 * Refresh scatterchart with data from MDS coordinates. 
	 * @param coordinates
	 * @param indices 
	 */
	public void refresh(double coordinates[][], Set<Integer> indices)
	{	
		// Store references to data collection.
		this.coordinates	= coordinates;
		this.indices		= indices;
		
		// Clear scatterchart.
		scatterchart.getData().clear();
		
        final Series<Number, Number> dataSeries			= new XYChart.Series<>();
        final Series<Number, Number> selectedDataSeries	= new XYChart.Series<>();
        
        dataSeries.setName("Data");
        selectedDataSeries.setName("Selected Data");
        
        // Add filtered points to scatterchart.
        int count = 0;
        for (int index : indices) {
        	// Add point only if it's not part of the set of manually selected indices.
        	if (!selectedMDSPoints.containsKey(index)) {
	        	XYChart.Data<Number, Number> dataPoint = new XYChart.Data<Number, Number>(coordinates[0][count], coordinates[1][count]);
	        	dataPoint.setExtraValue(index);
	        	dataSeries.getData().add(dataPoint);
        	}
        	
        	else {
        		XYChart.Data<Number, Number> selectedPoint = selectedMDSPoints.get(index);
        		XYChart.Data<Number, Number> dataPoint = new XYChart.Data<Number, Number>(selectedPoint.getXValue(), selectedPoint.getYValue());
	        	dataPoint.setExtraValue(selectedPoint.getExtraValue());
	        	selectedDataSeries.getData().add(dataPoint);
        	}
        	
        	count++;
        }
        
        // Add data in scatterchart.
        scatterchart.getData().add(0, selectedDataSeries);
        scatterchart.getData().add(dataSeries);
        
        // Add mouse listeners.
        addMouseListenersToMDSScatterchart(this.coordinates, this.indices);
        
        // Update scatterchart ranges.
        updateMDSScatterchartRanges();
	}
	
	/**
	 * Adds mouse event listeners handling single-point requests.
	 */
	private void addMouseListenersToMDSScatterchart(double coordinates[][], Set<Integer> filteredIndices)
	{
		// Add listeners to all series of non-selected data points.
		for (int i = 1; i < scatterchart.getData().size(); i++) {
	        // Add mouse event listeners to points in this data series.
	        for (XYChart.Data<Number, Number> dataPoint : scatterchart.getData().get(i).getData()) {
	        	dataPoint.getNode().setOnMouseClicked(new EventHandler<MouseEvent>()
				{
				    @Override
				    public void handle(MouseEvent mouseEvent)
				    {       
				    	// If control is not down: Add to selection.
				    	if (!mouseEvent.isControlDown()) {
					    	if (!selectedMDSPoints.containsKey(dataPoint.getExtraValue())) {
					    		// Add to map of selected data points.
					    		selectedMDSPoints.put((int)dataPoint.getExtraValue(), dataPoint);
					    		
					    		// Refresh scatterchart.
					    		refresh(coordinates, filteredIndices);
					    		// Refresh other charts.
					    		analysisController.refreshVisualizationsAfterGlobalSelection(selectedMDSPoints.keySet(), false);
					    	}
				    	}
				    }
				});
	        }
		}
		
		// Add mouse listeners for selected data points.
        for (XYChart.Data<Number, Number> dataPoint : scatterchart.getData().get(0).getData()) {
        	dataPoint.getNode().setOnMouseClicked(new EventHandler<MouseEvent>()
			{
			    @Override
			    public void handle(MouseEvent mouseEvent)
			    {       
			    	// If control is down: Remove from selection.
			    	if (mouseEvent.isControlDown()) {
				    	// Remove from set of selected data points.
			    		selectedMDSPoints.remove(dataPoint.getExtraValue());
			    		
			    		// Refresh scatterchart.
			    		refresh(coordinates, filteredIndices);
			    		// Refresh other charts.
			    		analysisController.refreshVisualizationsAfterGlobalSelection(selectedMDSPoints.keySet(), false);
			    	}
			    }
			});
        }
	}
	
	@Override
	public void processSelectionManipulationRequest(double minX, double minY, double maxX, double maxY)
	{
		// If control is not down: Ignore selected points, add all non-selected in chosen area.
		if (!isCtrlDown) {
			for (int i = 1; i < scatterchart.getData().size(); i++) {
				// Get data series.
				XYChart.Series<Number, Number> dataSeries = scatterchart.getData().get(i);
				// Iterate over all data points.
				for (XYChart.Data<Number, Number> datapoint : dataSeries.getData()) {
					Node datapointNode			= datapoint.getNode();
					boolean nodeXWithinBounds	= datapointNode.getLayoutX() <= maxX && datapointNode.getLayoutX() >= minX;
					boolean nodeYWithinBounds	= datapointNode.getLayoutY() <= maxY && datapointNode.getLayoutY() >= minY;
					
					// Point was selected - add to sets of selected indices and selected points.
					if (nodeXWithinBounds && nodeYWithinBounds) {
						if (!selectedMDSPoints.containsKey((int)datapoint.getExtraValue())) {
							// Set dirty flags.
							changeInSelectionDetected				= true;
							changeInSelectionDetected_localScope	= true;
							
							// Update collection of selected points.
							selectedMDSPoints.put((int)datapoint.getExtraValue(), datapoint);
						}
					}
				}
			}
			
			if (changeInSelectionDetected) {
	    		// Refresh scatterchart.
	    		refresh(coordinates, indices);
	    		// Refresh other charts.
	    		analysisController.refreshVisualizationsAfterGlobalSelection(selectedMDSPoints.keySet(), false);
	    		
	    		// Reset flag.
	    		changeInSelectionDetected = false;
			}
		}
		
		// Else if control is down: Remove selected points in chose area from selection (in data series 0).
		else {
			// Get data series.
			XYChart.Series<Number, Number> dataSeries = scatterchart.getData().get(0);
			// Iterate over all data points.
			for (XYChart.Data<Number, Number> datapoint : dataSeries.getData()) {
				Node datapointNode			= datapoint.getNode();
				boolean nodeXWithinBounds	= datapointNode.getLayoutX() <= maxX && datapointNode.getLayoutX() >= minX;
				boolean nodeYWithinBounds	= datapointNode.getLayoutY() <= maxY && datapointNode.getLayoutY() >= minY;
				
				// Point was deselected - remove from of selected points and selected indices.
				if (nodeXWithinBounds && nodeYWithinBounds) {
					if (selectedMDSPoints.containsKey((int)datapoint.getExtraValue())) {
						// Set dirty flags.
						changeInSelectionDetected				= true;
						changeInSelectionDetected_localScope	= true;
						
						// Update collection of selected points.
						selectedMDSPoints.remove((int)datapoint.getExtraValue());
					}
				}
			}
			
			if (changeInSelectionDetected) {
	    		// Refresh scatterchart.
	    		refresh(coordinates, indices);
	    		// Refresh other charts.
	    		analysisController.refreshVisualizationsAfterGlobalSelection(selectedMDSPoints.keySet(), false);
	    		
	    		// Reset flag.
	    		changeInSelectionDetected = false;
			}
		}
	}
	
	@Override
	public void processEndOfSelectionManipulation()
	{
		// Update local scope.
		if (changeInSelectionDetected_localScope) {
			// Refresh local scope visualization.
			analysisController.refreshLocalScopeAfterGlobalSelection();
			// Reset dirty flag.
			changeInSelectionDetected_localScope = false;
		}
	}
	
	@Override
	public Pair<Integer, Integer> provideOffsets()
	{
		return new Pair<Integer, Integer>(55, 44);
	}
	
	/**
	 * Updates x- and y-range of the MDS scatterchart.
	 * @param coordinates
	 */
	public void updateMDSScatterchartRanges()
	{
		// Else: Absolute view - set ranges manually.
		double diffX = globalCoordinateExtrema_X.getValue() - globalCoordinateExtrema_X.getKey(); 
		double diffY = globalCoordinateExtrema_Y.getValue() - globalCoordinateExtrema_Y.getKey();
		
	 	// Set axis label values.
		scatterchart_xAxis.setLowerBound(globalCoordinateExtrema_X.getKey() - diffX / 10);
		scatterchart_xAxis.setUpperBound(globalCoordinateExtrema_X.getValue() + diffX / 10);
		scatterchart_yAxis.setLowerBound(globalCoordinateExtrema_Y.getKey() - diffY / 10);
		scatterchart_yAxis.setUpperBound(globalCoordinateExtrema_Y.getValue() + diffY / 10);
    	
    	// Adjust tick width.
    	final int numberOfTicks = 10;
    	scatterchart_xAxis.setTickUnit( diffX / numberOfTicks);
    	scatterchart_yAxis.setTickUnit( diffY / numberOfTicks);
    	scatterchart_xAxis.setMinorTickCount(4);
    	scatterchart_yAxis.setMinorTickCount(4);
	}
	
	@Override
	public void changeViewMode()
	{
		
	}
	
	/**
	 * Get set of selected indices.
	 * @return
	 */
	public Set<Integer> getSelectedIndices()
	{
		return selectedMDSPoints.keySet();
	}
}