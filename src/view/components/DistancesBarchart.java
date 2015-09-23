package view.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import view.components.rubberbandselection.ISelectableComponent;
import view.components.rubberbandselection.RubberBandSelection;

import com.sun.javafx.charts.Legend;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import control.analysisView.AnalysisController;

public class DistancesBarchart extends VisualizationComponent implements ISelectableComponent
{
	/*
	 * GUI elements.
	 */
	
	private BarChart<String, Number> barchart;
	private NumberAxis numberaxis_distanceEvaluation_yaxis;
	
	
	/**
	 * Toggles between absolute and relativeView.
	 */
	private ToggleButton button_relativeView_distEval;
	
	/**
	 * Defines whether distances are to be logarithmically scaled.
	 */
	private CheckBox checkbox_useLogarithmicScaling;
	
	/**
	 * Component enabling rubberband-type selection of points in scatterchart.
	 */
	private RubberBandSelection rubberbandSelection;

	/**
	 * Stores information about CTRL usage.
	 */
	private boolean isCtrlDown;
	
	/*
	 * Metadata.
	 */
	
	/**
	 * Global distance extrema on x axis (absolute; not filter- or selection-specific).
	 */
	private Pair<Double, Double> globalDistanceExtrema;
	/**
	 * Holds extrema in average distances per dataset over all data series (discarded, filtered, selected).  
	 * Filtered distance extrema on x axis (filter-; but not selection-specific).
	 */
	private Pair<Double, Double> absoluteDistanceExtrema;

	/**
	 * Holds information about what's the highest number of distances associated with one bin in the 
	 * current environment (in the distance histogram).
	 */
	private int distanceBinCountMaximum;
	/**
	 * Indicates whether or not the maximal count of distances in a bin was yet determined.
	 */
	private boolean distanceBinCountMaximumDetermined;

	/**
	 * Stores which bars contain distances from which 
	 */
	private Map<String, ArrayList<Integer>> barToDataAssociations;
	/**
	 * Collection of the descriptions of selected bars.
	 */
	private Set<String> selectedBars;
	
	/*
	 * Actual data.
	 */
	
	/**
	 * Matrix containing all loaded distances.
	 */
	private double[][] discardedDistances;
	/**
	 * Matrix containing all currently filtered distances.
	 */
	private double[][] filteredDistances;
	/**
	 * Matrix containing all currently filtered and selected distances.
	 */
	private double[][] selectedFilteredDistances;
	
	/**
	 * Series containing all data.
	 */
	private XYChart.Series<String, Number> discardedDataSeries;
	/**
	 * Series containing filtered data.
	 */
	private XYChart.Series<String, Number> filteredDataSeries;
	/**
	 * Series containing filtered and selected data.
	 */
	private XYChart.Series<String, Number> selectedDataSeries;

	
	/*
	 * Methods.
	 */
	
	public DistancesBarchart(	AnalysisController analysisController, BarChart<String, Number> barchart_distances, 
								NumberAxis numberaxis_distanceEvaluation_yaxis,
								ToggleButton button_relativeView_distEval, CheckBox checkbox_useLogarithmicScaling)
	{
		super(analysisController);

		// Set references to GUI elements.
		this.barchart 								= barchart_distances;
		this.numberaxis_distanceEvaluation_yaxis	= numberaxis_distanceEvaluation_yaxis;
		this.button_relativeView_distEval			= button_relativeView_distEval;
		this.checkbox_useLogarithmicScaling			= checkbox_useLogarithmicScaling;
		
		// Init metadata.
		globalDistanceExtrema				= new Pair<Double, Double>(null, null);
		absoluteDistanceExtrema				= new Pair<Double, Double>(null, null);
		barToDataAssociations				= new HashMap<String, ArrayList<Integer>>();
		selectedBars						= new HashSet<String>();
		distanceBinCountMaximum				= 0;
		distanceBinCountMaximumDetermined	= false;
		
		// Init barchart.
		initBarchart();
	}
	
	/**
	 * Processes global (captured by analysis controller in scene) KeyPressedEvent.
	 * @param ke
	 */
	public void processKeyPressedEvent(KeyEvent ke)
	{
		isCtrlDown = ke.isControlDown();
	}
	
	/**
	 * Processes global (captured by analysis controller in scene) KeyReleasedEvent.
	 * @param ke
	 */
	public void processKeyReleasedEvent(KeyEvent ke)
	{	
		isCtrlDown = ke.isControlDown();
	}

	private void initBarchart()
	{
		barchart.setLegendVisible(true);
		barchart.setAnimated(false);
		barchart.setBarGap(0);
		barchart.setCategoryGap(1);
		
		// Disable auto-ranging (absolute view is default).
		barchart.getXAxis().setAutoRanging(true);
		barchart.getYAxis().setAutoRanging(false);
		
		// Initialize data series.
		discardedDataSeries	= new XYChart.Series<String, Number>();
		filteredDataSeries	= new XYChart.Series<String, Number>();
		selectedDataSeries	= new XYChart.Series<String, Number>();
		
		discardedDataSeries.setName("Discarded");
		filteredDataSeries.setName("Filtered");
		selectedDataSeries.setName("Selected");
		
		barchart.getData().clear();
		barchart.getData().add(discardedDataSeries);
		barchart.getData().add(filteredDataSeries);
		barchart.getData().add(selectedDataSeries);
		
		// Init legend.
		initBarchartLegend();
		
        // Add rubberband selection tool.
        rubberbandSelection = new RubberBandSelection((Pane) barchart.getParent(), this);
	}
	
	/**
	 * Inits barchart legend.
	 * Sets color according to data series.
	 */
	@SuppressWarnings("restriction")
	private void initBarchartLegend()
	{
		for (Node n : barchart.getChildrenUnmodifiable()) { 
			if (n instanceof Legend) { 
				final Legend legend = (Legend) n;
				 // remove the legend
	            legend.getChildrenUnmodifiable().addListener(new ListChangeListener<Object>()
	            {
	                @Override
	                public void onChanged(Change<?> arg0)
	                {
	                    for (Node node : legend.getChildrenUnmodifiable()) {
	                        if (node instanceof Label) 
	                        {
	                            final Label label = (Label) node;
	                            label.getChildrenUnmodifiable().addListener(new ListChangeListener<Object>()
	                            {
	                                @Override
	                                public void onChanged(Change<?> arg0)
	                                {
	                                	for (Node legendNode : legend.getChildren()) {
	                                		Label label = (Label)legendNode;
	                                		
	                                		if (label.getChildrenUnmodifiable().size() > 0)
	                                			if (label.getChildrenUnmodifiable().get(0) != null) {
	                                				String labelColor = "";
	                                				switch (label.getText())
	                                				{
	                                					case "Filtered":
	                                						labelColor = "blue";
	                                					break;
	                                					
	                                					case "Discarded":
	                                						labelColor = "grey";
		                                				break;
		                                				
		                                				default:
		                                					labelColor = "red";
	                                				}
	                                				 
	                                				// Set label color.
	                                				label.getChildrenUnmodifiable().get(0).setStyle("-fx-background-color: " + labelColor + ";");
	                                			}
	                                	}
	                                }

	                            });
	                        }
	                    }
	                }
	            });
	        }
	    }	
	}
	
	public void identifyGlobalExtrema(double[][] distances)
	{
		globalDistanceExtrema = identifyExtrema(distances);
	}
	
	/**
	 * Identifies and return extreme average distances in given distance matrix.
	 * @param distances
	 * @return
	 */
	private Pair<Double, Double> identifyExtrema(double[][] distances)
	{
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		
		for (int i = 0; i < distances.length; i++) {
			double tempSum = 0;
			
			for (int j = 0; j < distances[i].length; j++) {
				tempSum += distances[i][j];
			}
			
			// Normalize tempSum.
			tempSum /= distances.length;
			
			// Determine extrema.
			min = tempSum < min ? tempSum : min;
			max = tempSum > max ? tempSum : max;
		}
		
		return new Pair<Double, Double>(min, max);
	}
	
	/**
	 * Refreshes visualization.
	 * @param filteredDistances
	 * @param selectedFilteredDistances
	 * @param haveFilterSettingsChanged Determines if filter settings may have been changed since the last call.
	 */
	public void refresh(double discardedDistances[][], double filteredDistances[][], double[][] selectedFilteredDistances, boolean haveFilterSettingsChanged)
	{
		// Set/update references.
		this.discardedDistances			= discardedDistances;
		this.filteredDistances			= filteredDistances;
		this.selectedFilteredDistances	= selectedFilteredDistances;
		
		//	Clear old barchart data.
		barchart.getData().clear();
		barToDataAssociations.clear();
		
		// Calculate binning parameters.
		final int numberOfBins					= 25;
		int numberOfSelectedElements			= 0;
		final int numberOfFilteredElements		= ((filteredDistances.length - 1) * (filteredDistances.length - 1) + (filteredDistances.length - 1)) / 2;
		final int numberOfDiscardedElements		= ((discardedDistances.length - 1) * (discardedDistances.length - 1) + (discardedDistances.length - 1)) / 2;
		
		// Process data.
		if (filteredDistances != null) {
			// If filter settings may have been changed: Refresh filtered extrema.
			if (haveFilterSettingsChanged)
				absoluteDistanceExtrema = identifyExtrema(filteredDistances);
			
			// Process selected distances, if there are any.
			if (numberOfFilteredElements > 0) {
				// Add selected distances to chart.
				if (selectedFilteredDistances != null) {
					numberOfSelectedElements = ((selectedFilteredDistances.length - 1) * (selectedFilteredDistances.length - 1) + (selectedFilteredDistances.length - 1)) / 2;			

					// Add data series only to chart if it contains data points.
					if (numberOfSelectedElements > 0) {
						// Add distance values to data series.
						addToDataSeries(selectedDataSeries, 2, selectedFilteredDistances, numberOfBins, numberOfSelectedElements);
					}
				}
				
				// Add all filtered distances to chart.
				addToDataSeries(filteredDataSeries, 1, filteredDistances, numberOfBins, numberOfFilteredElements);
				
				// Set y-axis range, if in absolute mode and the maximal bin count has not yet been determined.
				if (!button_relativeView_distEval.isSelected() && !distanceBinCountMaximumDetermined) {
					adjustYAxisRange();
					distanceBinCountMaximumDetermined = true;
				}
			}
			
			if (numberOfDiscardedElements > 0) {
				// Add all (other) distances to chart.
				if (discardedDistances.length > 0) {
					addToDataSeries(discardedDataSeries, 0, discardedDistances, numberOfBins, numberOfDiscardedElements);
				}
			}
		}
	}


	/**
	 * Assign given distances to specified number of bins. Adds these bins to the distance barchart automatically.
	 * Return list of bins containing the number of distances fitting the respective boundaries.
	 * @param dataSeries
	 * @param seriesIndex 2 for selected, 1 for filtered, 0 for selected. 
	 * @param distances
	 * @param numberOfBins
	 * @param numberOfElements
	 * @param updateQuantileLabels
	 * @return
	 */
	private int[] addToDataSeries(	XYChart.Series<String, Number> dataSeries, final int seriesIndex,
									double[][] distances, final int numberOfBins, final int numberOfElements)
	{
		// Holds the average distance of one datapoint to all other datapoints in one element.
		double averageDistances[]								= new double[distances.length];
		
		// If this is the filtered data series: Remember which bins are associated with which elements. 
		HashMap<Integer, ArrayList<Integer>> binToDatasetIndices = new HashMap<Integer, ArrayList<Integer>>();
		// Init map.
		for (int i = 0; i < numberOfBins; i++) {
			binToDatasetIndices.put(i, new ArrayList<Integer>());
		}
		
		// Determine statistical measures.	
		double max		= Float.MIN_VALUE;
		double min		= Float.MAX_VALUE;
		double avg		= 0;
		double median	= 0;
		double avgSum	= 0;
		
		// Average data on element basis.	
		for (int i = 0; i < distances.length; i++) {
			double distanceSum = 0;
			
			for (int j = 0; j < distances.length; j++) { 
				// Sum up distances.
				distanceSum	+= distances[i][j];
			}
			
			averageDistances[i]  = distanceSum / distances.length;
			avgSum				+= averageDistances[i]; 
		}

		// Calculate average.
		avg = avgSum / averageDistances.length;
		
		// Sort array.
		Arrays.sort(averageDistances);
		
		// Calculate median.
		if (averageDistances.length % 2 == 0)
		    median = (averageDistances[averageDistances.length / 2] + averageDistances[averageDistances.length / 2 - 1]) / 2;
		else
		    median = averageDistances[averageDistances.length / 2];
		
		// Determine extrema.
		// 	Depending on whether absolute or relative mode is enabled: Use global or current extrema.  
		min = button_relativeView_distEval.isSelected() ? absoluteDistanceExtrema.getKey().floatValue() 	: 0;
		max = button_relativeView_distEval.isSelected() ? absoluteDistanceExtrema.getValue().floatValue() 	: globalDistanceExtrema.getValue().floatValue();
		
		/*
		 * Bin data.
		 */
		
		int distanceBinList[] 	= new int[numberOfBins];	
		double binInterval		= (max - min) / numberOfBins;

		for (int i = 0; i < averageDistances.length; i++) {
			double value	= averageDistances[i];
			int index_key	= (int) ( (value - min) / binInterval);
			index_key		= index_key < numberOfBins ? index_key : numberOfBins - 1;
			
			if (index_key == -1)
				System.out.println("v = " + value + "; min = " + min + ", max = " + max);
			
			// Increment content of corresponding bin.
			distanceBinList[index_key]++;
			
			
			// Insert entry into map translating bin indices to dataset indices.
			binToDatasetIndices.get(index_key).add(i);
		}
		
		// Transform absolute numbers in bins to percentages.
		for (int i = 0; i < distanceBinList.length; i++) {
			distanceBinList[i] /= ( (double)distances.length / 100);
		}
		
		/*
		 * Update UI. 
		 */
		
		// If logarithmic scaling is enabled:
		if (checkbox_useLogarithmicScaling.isSelected()) {
			// Scale bin count values accordingly.
			for (int i = 0; i < distanceBinList.length; i++) {
				distanceBinList[i] = distanceBinList[i] > 0 ? (int) Math.log(distanceBinList[i]) : 0;
			}
		}
		
		// Update barchart - add data series.
		dataSeries.getData().clear();
		
		//dataSeries.setData(generateDataForDistanceHistogram(distanceBinList, numberOfBins, binInterval, min, max));
		generateDataForDistanceHistogram(	dataSeries, 
											distanceBinList, dataSeries.getName() != "Discarded" ? binToDatasetIndices : null, 
											numberOfBins, binInterval, min, max);

		// Add anew to fire redraw event.
		barchart.getData().add(dataSeries);
		
		// Color bars (todo: color according to defined options).
		for (Node node : barchart.lookupAll(".chart-bar")) {
			switch (seriesIndex)
			{
				case 0:
					if (node.getUserData() == null || node.getUserData().toString() == "discarded") {
						node.setUserData("discarded");
						node.setStyle("-fx-bar-fill: grey;");
					}
				break;
				
				// Filtered data.
				case 1:
					if (node.getUserData() == null || node.getUserData().toString() == "filtered") {
						node.setUserData("filtered");
						node.setStyle("-fx-bar-fill: blue;");
					}
				break;
				
				// Selected data.
				case 2:
					if (node.getUserData() == null || node.getUserData().toString() == "selected") {
						node.setUserData("selected");
						node.setStyle("-fx-bar-fill: red;");
					}	
				break;
			}
		}
		
		return distanceBinList;
	}
	
	/**
	 * Toggles border glow (indicating the bar having been selected) on/off.
	 * @param node
	 * @param color 
	 */
	private void setBarHighlighting(Node node, boolean on, Color color)
	{
		if (!on) {
			node.setEffect(null);
		}
		
		else {
			 // Setting the uniform variable for the glow width and height
			int depth = 20;
			
			DropShadow borderGlow= new DropShadow();
			borderGlow.setOffsetY(0f);
			borderGlow.setOffsetX(0f);
			
			borderGlow.setRadius(1);
			borderGlow.setColor(color);
			borderGlow.setWidth(depth);
			borderGlow.setHeight(depth); 
			
			// Apply the borderGlow effect to the JavaFX node.
			node.setEffect(borderGlow); 
		}
	}

	/**
	 * Generates data series for histogram of dataset distances.
	 * @param dataSeries 
	 * @param subtractiveSeriesList 
	 * @param binToDatasetIndices 
	 * @param numberOfBins
	 * @param binInterval
	 * @param min
	 * @param max
	 * @return
	 */
	private ObservableList<Data<String, Number>> generateDataForDistanceHistogram(	Series<String, Number> dataSeries,
																					int[] distanceBinList, HashMap<Integer, ArrayList<Integer>> binToDatasetIndices, 
																					int numberOfBins, double binInterval, double min, double max)
	{
		final XYChart.Series<String, Number> data_series = new XYChart.Series<String, Number>();
		
		// Add data points.
		for (int i = 0; i < numberOfBins; i++) {
			String categoryDescription	= String.valueOf(min + i * binInterval);
			categoryDescription			= categoryDescription.length() > 5 ? categoryDescription.substring(0, 5) : categoryDescription;
			int value					= distanceBinList[i];
			
			// Avoid category clash by adding "invisible" characters.
			for (int x = 0; x < i; x++)
				categoryDescription += (char)29;
			
			// Store bin to dataset(s) translation.
			if (binToDatasetIndices != null) {
				// Add new bar -> local dataset index association.
				barToDataAssociations.put(categoryDescription + dataSeries.getName(), binToDatasetIndices.get(i));
			}
			
			// Add data to series.
			dataSeries.getData().add(new XYChart.Data<String, Number>(categoryDescription, value));
		}
		
		// Return newly generated data series.
		return data_series.getData();
	}
	
	/**
	 * Default view mode is absolute - disable auto-ranging on axes, configure ticks.
	 */
	private void adjustYAxisRange()
	{
		numberaxis_distanceEvaluation_yaxis.setLowerBound(0);
		numberaxis_distanceEvaluation_yaxis.setUpperBound(100);
		
    	// Adjust tick width.
    	final int numberOfTicks = 4;
    	numberaxis_distanceEvaluation_yaxis.setTickUnit( (float)100 / numberOfTicks);
    	numberaxis_distanceEvaluation_yaxis.setMinorTickCount(2);
	}
	
	@Override
	public void changeViewMode()
	{
		// Toggle auto-ranging.
		barchart.getYAxis().setAutoRanging(button_relativeView_distEval.isSelected());
		
		// Relative view: Determine extrema on y axis to adjust chart accordingly.
		if (button_relativeView_distEval.isSelected()) {
			Pair<Double, Double> filteredDistanceExtrema 	= identifyExtrema(filteredDistances);
			Pair<Double, Double> selectedDistanceExtrema	= identifyExtrema(selectedFilteredDistances);
			Pair<Double, Double> discardedDistanceExtrema	= identifyExtrema(discardedDistances);
			
			// Find absolute extrema.
			double min = Double.MAX_VALUE;
			min = filteredDistanceExtrema.getKey() < selectedDistanceExtrema.getKey() ? filteredDistanceExtrema.getKey() : selectedDistanceExtrema.getKey();
			min = discardedDistanceExtrema.getKey() < min ? discardedDistanceExtrema.getKey() : min;
			
			double max = Double.MIN_VALUE;
			max = filteredDistanceExtrema.getValue() > selectedDistanceExtrema.getValue() ? filteredDistanceExtrema.getValue() : selectedDistanceExtrema.getValue();
			max = discardedDistanceExtrema.getValue() > min ? discardedDistanceExtrema.getValue() : max;
			
			// Store new absolute extrema.
			absoluteDistanceExtrema = new Pair<Double, Double>(min, max);
		}
		// Absolute view: Readjust y-axis.
		else {
			adjustYAxisRange();
		}
		
		// Refresh chart.
		refresh(discardedDistances, filteredDistances, selectedFilteredDistances, false);
	}
	
	/**
	 * Checks if logarithmic scaling is enabled.
	 * Rescales values, if necessary.
	 */
	public void changeScalingType()
	{
		// Refresh chart.
		refresh(discardedDistances, filteredDistances, selectedFilteredDistances, false);
	}

	@Override
	public void processSelectionManipulationRequest(double minX, double minY, double maxX, double maxY)
	{
		// Check if settings icon was used. Workaround due to problems with selection's mouse event handling. 
		if (minX == maxX && minY == maxY) {
			Pair<Integer, Integer> offsets = provideOffsets();
			analysisController.checkIfSettingsIconWasClicked(minX + offsets.getKey(), minY + offsets.getValue(), "settings_distEval_icon");
		}
				
		// If control is not down: Ignore selected points, add all non-selected in chosen area.
		if (!isCtrlDown) {
			// Process filtered, non-selected data.
			for (Data<String, Number> data : filteredDataSeries.getData()) {
				Node dataNode = data.getNode();
				//System.out.println("full name asked = " + data.getXValue() + filteredDataSeries.getName() + ", " + barToDataAssociations.containsKey(data.getXValue() + filteredDataSeries.getName()));
				if (	barToDataAssociations.get(data.getXValue() + filteredDataSeries.getName()).size() > 0 &&
						dataNode.getLayoutX() >= minX && dataNode.getLayoutX() + dataNode.getBoundsInLocal().getWidth() <= maxX &&
						dataNode.getLayoutY() >= minY && dataNode.getLayoutY() + dataNode.getBoundsInLocal().getHeight() <= maxY ) {
					// Highlight bar.
					setBarHighlighting(dataNode, true, Color.RED);
				
					// Add to collection.
					if (!selectedBars.contains(data.getXValue())) {
						selectedBars.add(data.getXValue());
					}
				}
				
				else {
					// Remove bar highlighting.
					setBarHighlighting(dataNode, false, null);
					
					// Remove from collection.
					selectedBars.remove(data.getXValue());
				}
			}
		}
		
		else {
			// Process filtered, non-selected data.
			for (Data<String, Number> data : selectedDataSeries.getData()) {
				Node dataNode = data.getNode();
				
				if (	barToDataAssociations.containsKey(data.getXValue() + selectedDataSeries.getName()) && 
						barToDataAssociations.get(data.getXValue() + selectedDataSeries.getName()).size() > 0 &&
						dataNode.getLayoutX() >= minX && dataNode.getLayoutX() + dataNode.getBoundsInLocal().getWidth() <= maxX &&
						dataNode.getLayoutY() >= minY && dataNode.getLayoutY() + dataNode.getBoundsInLocal().getHeight() <= maxY ) {
					// Highlight bar.
					setBarHighlighting(dataNode, true, Color.BLUE);
					
					// Add to collection.
					if (!selectedBars.contains(data.getXValue())) {
						selectedBars.add(data.getXValue());
					}
				}
				
				else {
					// Remove bar highlighting.
					setBarHighlighting(dataNode, false, null);
					
					// Remove from collection.
					selectedBars.remove(data.getXValue());
				}
			}
		}
	}

	@Override
	public void processEndOfSelectionManipulation()
	{
		ArrayList<Integer> selectedLocalIndices = new ArrayList<Integer>();
		
		for (String description : selectedBars) {
			// Add to collection.
			final String seriesSuffix = !isCtrlDown ? filteredDataSeries.getName() : selectedDataSeries.getName(); 
			selectedLocalIndices.addAll(barToDataAssociations.get(description + seriesSuffix));
		}
		
		// Pass selection data on to controller.
		analysisController.integrateBarchartSelection(selectedLocalIndices, !isCtrlDown);
	}
	
	@Override
	public Pair<Integer, Integer> provideOffsets()
	{
		return new Pair<Integer, Integer>(47, 46);
	}

}

//private int[] addToDataSeries(	XYChart.Series<String, Integer> dataSeries, int seriesIndex, ArrayList< ObservableList<Data<String, Integer>> > subtractiveSeriesList, 
//double[][] distances, int numberOfBins, int numberOfElements, boolean updateQuantileLabels)
//{
//// Contais distance matrix in flattened form.
//float distancesFlattened[] = new float[numberOfElements];
//
//// Determine statistical measures.
//float sum = 0;
//float max = Float.MIN_VALUE;
//float min = Float.MAX_VALUE;
//float avg = 0;
//float median = 0;
//int flatCounter = 0;
//
//// Examine distance data, copy values in one-dimensional array.
//for (int i = 0; i < distances.length; i++) {
//for (int j = i + 1; j < distances.length; j++) {
//	// Sum up distances.
//	sum += distances[i][j];
//	// Flatten matrix.
//	distancesFlattened[flatCounter++] = (float) distances[i][j];
//}
//}
//
//// Calculate average.
//avg = sum / numberOfElements;
//
//// Sort array.
//Arrays.sort(distancesFlattened);
//
//// Calculate median.
//if (distancesFlattened.length % 2 == 0)
//median = (distancesFlattened[distancesFlattened.length / 2] + distancesFlattened[distancesFlattened.length / 2 - 1]) / 2;
//else
//median = distancesFlattened[distancesFlattened.length / 2];
//
//// Determine extrema.
//// Depending on whether absolute or relative mode is enabled: Use global
//// or current extrema.
//min = button_relativeView_distEval.isSelected() ? filteredDistanceExtrema
//	.getKey().floatValue() : globalDistanceExtrema.getKey()
//	.floatValue();
//max = button_relativeView_distEval.isSelected() ? filteredDistanceExtrema
//	.getValue().floatValue() : globalDistanceExtrema.getValue()
//	.floatValue();
//
///*
//* Bin data.
//*/
//
//int distanceBinList[] = new int[numberOfBins];
//double binInterval = (max - min) / numberOfBins;
//
//for (float value : distancesFlattened) {
//int index_key = (int) ((value - min) / binInterval);
//index_key = index_key < numberOfBins ? index_key : numberOfBins - 1;
//
//// Increment content of corresponding bin.
//distanceBinList[index_key]++;
//}
//
///*
//* Update UI.
//*/
//
//// Update text info.
//if (updateQuantileLabels) {
//label_avg.setText("?: " + String.valueOf(avg));
//label_median.setText("?: " + String.valueOf(median));
//}
//
//// If logarithmic scaling is enabled:
//if (checkbox_useLogarithmicScaling.isSelected()) {
//// Scale bin count values accordingly.
//for (int i = 0; i < distanceBinList.length; i++) {
//	distanceBinList[i] = distanceBinList[i] > 0 ? (int) Math
//			.log(distanceBinList[i]) : 0;
//}
//}
//
//// Update barchart - add data series.
//dataSeries.getData().clear();
//
//// dataSeries.setData(generateDataForDistanceHistogram(distanceBinList,
//// numberOfBins, binInterval, min, max));
//generateDataForDistanceHistogram(dataSeries, subtractiveSeriesList,
//	distanceBinList, null, numberOfBins, binInterval, min, max);
//
//// Add anew to fire redraw event.
//barchart.getData().add(dataSeries);
//
//// Color bars (todo: color according to defined options).
//for (Node node : barchart.lookupAll(".chart-bar")) {
//switch (seriesIndex) {
//	case 0:
//		if (node.getUserData() == null
//				|| node.getUserData().toString() == "discarded") {
//			node.setUserData("discarded");
//			node.setStyle("-fx-bar-fill: grey;");
//		}
//	break;
//
//	// Filtered data.
//	case 1:
//		if (node.getUserData() == null
//				|| node.getUserData().toString() == "filtered") {
//			node.setUserData("filtered");
//			node.setStyle("-fx-bar-fill: blue;");
//		}
//	break;
//
//	// Selected data.
//	case 2:
//		if (node.getUserData() == null
//				|| node.getUserData().toString() == "selected") {
//			node.setUserData("selected");
//			node.setStyle("-fx-bar-fill: red;");
//		}
//	break;
//}
//}
//
//return distanceBinList;
//}