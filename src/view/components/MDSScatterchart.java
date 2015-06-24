package view.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javafx.event.EventHandler;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import control.AnalysisController;

public class MDSScatterchart extends VisualizationComponent
{
	/*
	 * GUI elements.
	 */
	
	private ScatterChart<Number, Number> scatterchart;
	private NumberAxis scatterchart_xAxis;
	private NumberAxis scatterchart_yAxis;
	
	/*
	 * Other data.
	 */
	
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
	
	
	public MDSScatterchart(AnalysisController analysisController, ScatterChart<Number, Number> scatterchart)
	{
		super(analysisController);
		
		this.scatterchart			= scatterchart;
		
		// Init collection of selected data points in the MDS scatterchart.
		selectedMDSPoints			= new HashMap<Integer, XYChart.Data<Number, Number>>();
		globalCoordinateExtrema_X	= new Pair<Double, Double>(Double.MAX_VALUE, Double.MIN_VALUE);
		globalCoordinateExtrema_Y	= new Pair<Double, Double>(Double.MAX_VALUE, Double.MIN_VALUE);
		
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
        
        // @todo For selection of MDS points:
        //			- Store reference to instance of RubberBandSelection.
        //			- Add key listener -> If ctrl is pressed, enable single point mode (disable rubberband listeners). If shift is pressed, enable group mode (rubberband selection).
        //			- Map rubberband are coordinates to MDS chart.
        //			- Map MDS chart coordinates to datapoints.
        //			- Add datapoints to selection (selectedMDSPoints).
        // @todo After that: Create working version of parallel tag cloud visualization.
        // @todo After that: Introduce highlighting of selection of MDS datapoints in other charts (and vice versa).
        // @todo After that: Switch to SQLite (instead of a file-based system).
        
        // Add rubberband selection tool.
        //new RubberBandSelection((Pane)scatterchart_global.getParent());
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
	 * @param filteredIndices 
	 */
	public void refresh(double coordinates[][], Set<Integer> filteredIndices)
	{			
		// Clear scatterchart.
		scatterchart.getData().clear();
		
        final Series<Number, Number> dataSeries			= new XYChart.Series<>();
        final Series<Number, Number> selectedDataSeries	= new XYChart.Series<>();
        
        dataSeries.setName("Data");
        selectedDataSeries.setName("Selected Data");
        
        // Add filtered points to scatterchart.
        int count = 0;
        for (int index : filteredIndices) {
        	// Add point only if it's not part of the set of manually selected indices.
        	if (!selectedMDSPoints.containsKey(index)) {
	        	XYChart.Data<Number, Number> dataPoint = new XYChart.Data<Number, Number>(coordinates[0][count], coordinates[1][count]);
	        	dataPoint.setExtraValue(index);
	        	dataSeries.getData().add(dataPoint);
        	}
        	
        	count++;
        }
        
        // Add selected points to scatterchart.
        for (Map.Entry<Integer, XYChart.Data<Number, Number>> selectedPoint : selectedMDSPoints.entrySet()) {
        	XYChart.Data<Number, Number> dataPoint = new XYChart.Data<Number, Number>(selectedPoint.getValue().getXValue(), selectedPoint.getValue().getYValue());
        	dataPoint.setExtraValue(selectedPoint.getValue().getExtraValue());
        	selectedDataSeries.getData().add(dataPoint);	
        }
        
        // Add data in scatterchart.
        scatterchart.getData().add(0, selectedDataSeries);
        scatterchart.getData().add(dataSeries);
        
        // Add mouse listeners.
        addMouseListenersToMDSScatterchart(coordinates, filteredIndices);
        
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
				    	if (mouseEvent.isControlDown()) {
					    	// Remember which points were selected. 
					    	if (!selectedMDSPoints.containsKey(dataPoint.getExtraValue())) {
					    		selectedMDSPoints.put((int)dataPoint.getExtraValue(), dataPoint);
					    		// Refresh scatterchart.
					    		refresh(coordinates, filteredIndices);
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
			    	if (mouseEvent.isControlDown()) {
				    	// Remember which points were selected.
			    		selectedMDSPoints.remove(dataPoint.getExtraValue());
			    		// Refresh scatterchart.
			    		refresh(coordinates, filteredIndices);
			    	}
			    }
			});
        }
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
    	final int numberOfTicks = analysisController.getLDAConfigurations().size();
    	scatterchart_xAxis.setTickUnit( diffX / numberOfTicks);
    	scatterchart_yAxis.setTickUnit( diffY / numberOfTicks);
    	scatterchart_xAxis.setMinorTickCount(4);
    	scatterchart_yAxis.setMinorTickCount(4);
	}
	
	@Override
	public void changeViewMode(double[][] coordinates)
	{
		
	}

}
