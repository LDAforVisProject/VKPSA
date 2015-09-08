package view.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sun.javafx.charts.Legend;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.util.Pair;
import control.analysisView.AnalysisController;

public class DistancesBarchart extends VisualizationComponent
{
	/*
	 * GUI elements.
	 */
	
	private StackedBarChart<String, Integer> barchart;
	private NumberAxis numberaxis_distanceEvaluation_yaxis;
	
	private Label label_avg;
	private Label label_median;
	
	private ToggleButton button_relativeView_distEval;
	
	private CheckBox checkbox_useLogarithmicScaling;
	
	/*
	 * Other data.
	 */
	
	/**
	 * Global distance extrema on x axis (absolute; not filter- or selection-specific).
	 */
	private Pair<Double, Double> globalDistanceExtrema;
	/**
	 * Filtered distance extrema on x axis (filter-; but not selection-specific).
	 */
	private Pair<Double, Double> filteredDistanceExtrema;
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
	 * Matrix containing all loaded distances.
	 */
	private double[][] distances;
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
	private XYChart.Series<String, Integer> allDataSeries;
	/**
	 * Series containing filtered data.
	 */
	private XYChart.Series<String, Integer> filteredDataSeries;
	/**
	 * Series containing filtered and selected data.
	 */
	private XYChart.Series<String, Integer> selectedDataSeries;

	
	public DistancesBarchart(	AnalysisController analysisController, StackedBarChart<String, Integer> barchart_distances, 
								NumberAxis numberaxis_distanceEvaluation_yaxis, Label label_avg, 
								Label label_median, ToggleButton button_relativeView_distEval, CheckBox checkbox_useLogarithmicScaling)
	{
		super(analysisController);

		this.barchart 								= barchart_distances;
		this.numberaxis_distanceEvaluation_yaxis	= numberaxis_distanceEvaluation_yaxis;
		this.label_avg								= label_avg;
		this.label_median							= label_median;
		this.button_relativeView_distEval			= button_relativeView_distEval;
		this.checkbox_useLogarithmicScaling			= checkbox_useLogarithmicScaling;
		
		globalDistanceExtrema				= new Pair<Double, Double>(null, null);
		filteredDistanceExtrema				= new Pair<Double, Double>(null, null);
		distanceBinCountMaximum				= 0;
		distanceBinCountMaximumDetermined	= false;
		
		// Init barchart.
		initBarchart();
		
		// Init tooltips for labels.
		initTooltips();
	}
	
	/**
	 * Init tooltips for labels.
	 */
	private void initTooltips()
	{
		Tooltip tooltip_avg = new Tooltip("Average");
		tooltip_avg.setMaxWidth(75);
		tooltip_avg.setAutoHide(false);
		tooltip_avg.setWrapText(true);
		
        Tooltip.install(label_avg, tooltip_avg);
        
		Tooltip tooltip_median = new Tooltip("Median");
		tooltip_median.setMaxWidth(75);
		tooltip_median.setAutoHide(false);
		tooltip_median.setWrapText(true);
		
        Tooltip.install(label_median, tooltip_median);
	}

	private void initBarchart()
	{
		barchart.setLegendVisible(true);
		barchart.setAnimated(false);
		//barchart.setBarGap(0);
		barchart.setCategoryGap(1);
		
		// Disable auto-ranging (absolute view is default).
		barchart.getXAxis().setAutoRanging(true);
		barchart.getYAxis().setAutoRanging(false);
		
		// Initialize data series.
		allDataSeries		= new XYChart.Series<String, Integer>();
		filteredDataSeries	= new XYChart.Series<String, Integer>();
		selectedDataSeries	= new XYChart.Series<String, Integer>();
		
		allDataSeries.setName("Discarded");
		filteredDataSeries.setName("Filtered");
		selectedDataSeries.setName("Selected");
		
		barchart.getData().clear();
		barchart.getData().add(allDataSeries);
		barchart.getData().add(filteredDataSeries);
		barchart.getData().add(selectedDataSeries);
		
		// Init legend.
		initBarchartLegend();
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
	
	private Pair<Double, Double> identifyExtrema(double[][] distances)
	{
		double minX				= Double.MAX_VALUE;
		double maxX 			= Double.MIN_VALUE;
		
		for (int i = 0; i < distances.length; i++) {
			for (int j = i + 1; j < distances[i].length; j++) {
				minX = distances[i][j] < minX ? distances[i][j] : minX;
				maxX = distances[i][j] > maxX ? distances[i][j] : maxX;
			}
		}
		
		return new Pair<Double, Double>(minX, maxX);
	}
	
	/**
	 * Refreshes visualization.
	 * @param filteredDistances
	 * @param selectedFilteredDistances
	 * @param haveFilterSettingsChanged Determines if filter settings may have been changed since the last call.
	 */
	public void refresh(double distances[][], double filteredDistances[][], double[][] selectedFilteredDistances, boolean haveFilterSettingsChanged)
	{
		this.distances					= distances;
		this.filteredDistances			= filteredDistances;
		this.selectedFilteredDistances	= selectedFilteredDistances;
		
		//	Clear old barchart data.
		barchart.getData().clear();
		
		// Process data.
		if (filteredDistances != null) {
			// If filter settings may have been changed: Refresh filtered extrema.
			if (haveFilterSettingsChanged)
				filteredDistanceExtrema = identifyExtrema(filteredDistances);
			
			// Calculate binning parameters.
			final int numberOfBins					= 25;
			int numberOfSelectedElements			= 0;
			final int numberOfFilteredElements		= ((filteredDistances.length - 1) * (filteredDistances.length - 1) + (filteredDistances.length - 1)) / 2;
			final int numberOfAllElements			= ((distances.length - 1) * (distances.length - 1) + (distances.length - 1)) / 2;
			
			// Data series holding subtractive values.
			ArrayList<ObservableList<Data<String, Integer>>> subtractiveSeriesList = new ArrayList<ObservableList<Data<String,Integer>>>(); 
			
			// Process selected distances, if there are any.
			if (numberOfFilteredElements > 0) {
				// Add selected distances to chart.
				if (selectedFilteredDistances != null) {
					numberOfSelectedElements = ((selectedFilteredDistances.length - 1) * (selectedFilteredDistances.length - 1) + (selectedFilteredDistances.length - 1)) / 2;			

					// Add data series only to chart if it contains data points.
					if (numberOfSelectedElements > 0) {
						// Add distance values to data series.
						addToDataSeries(selectedDataSeries, 2, null, selectedFilteredDistances, numberOfBins, numberOfSelectedElements, false);
						// Add to list of subtractive values.
						subtractiveSeriesList.add(selectedDataSeries.getData());
					}
				}
				
				// Add all filtered distances to chart.
				int[] filteredDistanceBinList= addToDataSeries(filteredDataSeries, 1, subtractiveSeriesList, filteredDistances, numberOfBins, numberOfFilteredElements, true);
				// Add to list of subtractive values.
				subtractiveSeriesList.add(filteredDataSeries.getData());
				
				// Add all (other) distances to chart.
				int[] distanceBinList			= addToDataSeries(allDataSeries, 0, subtractiveSeriesList, distances, numberOfBins, numberOfAllElements, true); 
				
				// Set y-axis range, if in absolute mode and the maximal bin count has not yet been determined.
				if (!button_relativeView_distEval.isSelected() && !distanceBinCountMaximumDetermined) {
					// Adjust x-axis.
					findDistanceBinCountMaximum(distanceBinList);
					
					// Adjust y-axis.
					adjustYAxisRange();
					
					// Update flag.
					distanceBinCountMaximumDetermined = true;
				}
			}
			
			else {
				// Update text info.
				label_avg.setText("x̄: -");
				label_median.setText("M: -");
			}
		}
		
		else {
			// Update text info.
			label_avg.setText("x̄: -");
			label_median.setText("M: -");
		}
	}

	/**
	 * Assign given distances to specified number of bins. Adds these bins to the distance barchart automatically.
	 * Return list of bins containing the number of distances fitting the respective boundaries.
	 * @param subtractiveValues 
	 * @param distances
	 * @param numberOfBins
	 * @param numberOfElements
	 * @param updateQuantileLabels
	 * @return
	 */
	private int[] addToDataSeries(	XYChart.Series<String, Integer> dataSeries, int seriesIndex, ArrayList< ObservableList<Data<String, Integer>> > subtractiveSeriesList, 
									double[][] distances, int numberOfBins, int numberOfElements, boolean updateQuantileLabels)
	{
		// Contais distance matrix in flattened form.
		float distancesFlattened[] = new float[numberOfElements];
		
		// Determine statistical measures.	
		float sum		= 0;
		float max		= Float.MIN_VALUE;
		float min		= Float.MAX_VALUE;
		float avg		= 0;
		float median	= 0;
		int flatCounter	= 0;
		
		// Examine distance data, copy values in one-dimensional array.		
		for (int i = 0; i < distances.length; i++) {
			for (int j = i + 1; j < distances.length; j++) { 
				// Sum up distances.
				sum += distances[i][j];
				// Flatten matrix.
				distancesFlattened[flatCounter++] = (float)distances[i][j];
			}
		}
	
		// Calculate average.
		avg = sum / numberOfElements;
		
		// Sort array.
		Arrays.sort(distancesFlattened);
		
		// Calculate median.
		if (distancesFlattened.length % 2 == 0)
		    median = (distancesFlattened[distancesFlattened.length / 2] + distancesFlattened[distancesFlattened.length / 2 - 1]) / 2;
		else
		    median = distancesFlattened[distancesFlattened.length / 2];
		
		// Determine extrema.
		// 	Depending on whether absolute or relative mode is enabled: Use global or current extrema.  
		min = button_relativeView_distEval.isSelected() ? filteredDistanceExtrema.getKey().floatValue() 	: globalDistanceExtrema.getKey().floatValue();
		max = button_relativeView_distEval.isSelected() ? filteredDistanceExtrema.getValue().floatValue() 	: globalDistanceExtrema.getValue().floatValue();
		
		/*
		 * Bin data.
		 */
		
		int distanceBinList[] 	= new int[numberOfBins];
		double binInterval		= (max - min) / numberOfBins;
		
		for (float value : distancesFlattened) {
			int index_key	= (int) ( (value - min) / binInterval);
			index_key		= index_key < numberOfBins ? index_key : numberOfBins - 1;
			
			// Increment content of corresponding bin.
			distanceBinList[index_key]++;	
		}
		
		/*
		 * Update UI. 
		 */

		// Update text info.
		if (updateQuantileLabels) {
			label_avg.setText("?: " + String.valueOf(avg));
			label_median.setText("?: " + String.valueOf(median));
		}
		
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
		generateDataForDistanceHistogram(dataSeries, subtractiveSeriesList, distanceBinList, numberOfBins, binInterval, min, max);

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
//					if (node.getUserData() != "selected") {
					if (node.getUserData() == null || node.getUserData().toString() == "filtered") {
						node.setUserData("filtered");
						node.setStyle("-fx-bar-fill: blue;");
					}
				break;
				
				// Selected data.
				case 2:
//					if (node.getUserData() != "filtered") {
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
	 * Generates data series for histogram of dataset distances.
	 * @param dataSeries 
	 * @param subtractiveSeriesList 
	 * @param numberOfBins
	 * @param binInterval
	 * @param min
	 * @param max
	 * @return
	 */
	private ObservableList<Data<String, Integer>> generateDataForDistanceHistogram(	Series<String, Integer> dataSeries, ArrayList<ObservableList<Data<String, Integer>>> subtractiveSeriesList, 
																					int[] distanceBinList, int numberOfBins, double binInterval, double min, double max)
	{
		final XYChart.Series<String, Integer> data_series	= new XYChart.Series<String, Integer>();
		//Map<String, Integer> categoryCounts					= new HashMap<String, Integer>();
		
		// Add data points.
		for (int i = 0; i < numberOfBins; i++) {
			String categoryDescription	= String.valueOf(min + i * binInterval);
			categoryDescription			= categoryDescription.length() > 5 ? categoryDescription.substring(0, 5) : categoryDescription;
			int value					= distanceBinList[i];
			
			// Avoid category clash by adding "invisible" characters.
			for (int x = 0; x < i; x++)
				categoryDescription += (char)29;
			
			// There are subtractive value lists.
			if (subtractiveSeriesList != null && subtractiveSeriesList.size() > 0) {
				// Subtract values at index i from all subtractive series.
				for (ObservableList<Data<String, Integer>> subtractiveSeries : subtractiveSeriesList) {
					value -= subtractiveSeries.get(i).getYValue();
				}
				
				// Add adjusted datapoint.
				dataSeries.getData().add( new XYChart.Data<String, Integer>(categoryDescription, value) );
			}
			
			// No subtractive value lists.
			else {
				dataSeries.getData().add(new XYChart.Data<String, Integer>(categoryDescription, value));
			}
		}
		
		// Return newly generated data series.
		return data_series.getData();
	}
	
	/**
	 * Find maximum in list of bin values.
	 * @param distanceBinList
	 */
	private void findDistanceBinCountMaximum(int[] distanceBinList)
	{
		// If in absolute mode for the first time (important: The first draw always happens in absolute
		// view mode!): Get maximal count of distances associated with one bin.
		//if (!distanceBinCountMaximumDetermined) {
			distanceBinCountMaximum = 0;
			for (int binCount : distanceBinList) {
				distanceBinCountMaximum = binCount > distanceBinCountMaximum ? binCount : distanceBinCountMaximum;
			}
			
			distanceBinCountMaximumDetermined = true;
		//}
		
//		System.out.println("dbcMax = " + distanceBinCountMaximum);
	}
	
	/**
	 * Default view mode is absolute - disable auto-ranging on axes, configure ticks.
	 */
	private void adjustYAxisRange()
	{
		numberaxis_distanceEvaluation_yaxis.setLowerBound(0);
		numberaxis_distanceEvaluation_yaxis.setUpperBound(distanceBinCountMaximum * 1.1);
		
    	// Adjust tick width.
    	final int numberOfTicks = 3;
    	numberaxis_distanceEvaluation_yaxis.setTickUnit( (float)distanceBinCountMaximum / numberOfTicks);
    	numberaxis_distanceEvaluation_yaxis.setMinorTickCount(2);
	}
	
	@Override
	public void changeViewMode()
	{
		// Toggle auto-ranging.
		barchart.getYAxis().setAutoRanging(button_relativeView_distEval.isSelected());
		
		// Relative view: Determine extrema on y axis to adjust chart accordingly.
		if (button_relativeView_distEval.isSelected()) {
			filteredDistanceExtrema = identifyExtrema(filteredDistances);
		}
		// Absolute view: Readjust y-axis.
		else {
			adjustYAxisRange();
		}
		
		// Refresh chart.
		refresh(distances, filteredDistances, selectedFilteredDistances, false);
	}
	
	/**
	 * Checks if logarithmic scaling is enabled.
	 * Rescales values, if necessary.
	 */
	public void changeScalingType()
	{
		// Refresh chart.
		refresh(distances, filteredDistances, selectedFilteredDistances, false);
	}
}
