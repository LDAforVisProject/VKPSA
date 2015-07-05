package view.components;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
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
	
	private BarChart<String, Integer> barchart;
	private NumberAxis numberaxis_distanceEvaluation_yaxis;
	
	private Label label_avg;
	private Label label_median;
	
	private ToggleButton button_relativeView_distEval;
	
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
	 * Indicates wheteher or not the maximal count of distances in a bin was yet determined.
	 */
	private boolean distanceBinCountMaximumDetermined;
	
	/**
	 * Matrix containing all currently filtered distances.
	 */
	private double[][] filteredDistances;
	/**
	 * Matrix containing all currently filtered and selected distances.
	 */
	private double[][] selectedFilteredDistances;
	
	
	public DistancesBarchart(	AnalysisController analysisController, BarChart<String, Integer> barchart_distances, 
								NumberAxis numberaxis_distanceEvaluation_yaxis, Label label_avg, 
								Label label_median, ToggleButton button_relativeView_distEval)
	{
		super(analysisController);

		this.barchart 								= barchart_distances;
		this.numberaxis_distanceEvaluation_yaxis	= numberaxis_distanceEvaluation_yaxis;
		this.label_avg								= label_avg;
		this.label_median							= label_median;
		this.button_relativeView_distEval			= button_relativeView_distEval;
		
		globalDistanceExtrema				= new Pair<Double, Double>(null, null);
		filteredDistanceExtrema				= new Pair<Double, Double>(null, null);
		distanceBinCountMaximum				= 0;
		distanceBinCountMaximumDetermined	= false;
		
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
		barchart.setBarGap(0);
		barchart.setCategoryGap(1);
		
		// Disable auto-ranging (absolute view is default).
		barchart.getXAxis().setAutoRanging(true);
		barchart.getYAxis().setAutoRanging(false);
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
	public void refresh(double filteredDistances[][], double[][] selectedFilteredDistances, boolean haveFilterSettingsChanged)
	{
		this.filteredDistances			= filteredDistances;
		this.selectedFilteredDistances	= selectedFilteredDistances;
		
		//	Clear old barchart data.
		barchart.getData().clear();
		
		if (filteredDistances != null) {
			// If filter settings may have been changed: Refresh filtered extrema.
			if (haveFilterSettingsChanged)
				filteredDistanceExtrema = identifyExtrema(filteredDistances);
			
			// Calculate binning parameters.
			final int numberOfBins		= 50;
			final int numberOfElements	= ((filteredDistances.length - 1) * (filteredDistances.length - 1) + (filteredDistances.length - 1)) / 2;
			
			if (numberOfElements > 0) {
				// Add selected (and filtered) distances to chart.
				if (selectedFilteredDistances != null) {
					final int numberOfSelectedElements = ((selectedFilteredDistances.length - 1) * (selectedFilteredDistances.length - 1) + (selectedFilteredDistances.length - 1)) / 2;			
					if (numberOfSelectedElements > 0)
						addDataSeries("Selected", selectedFilteredDistances, numberOfBins, numberOfSelectedElements, false);
					
					else
						addEmptyDataSeries("Selected");
				}
				
				else {
					addEmptyDataSeries("Selected");
				}
				
				// Add all filtered distances to chart.
				int[] distanceBinList = addDataSeries("Filtered", filteredDistances, numberOfBins, numberOfElements, true);
				
				// Set y-axis range, if in absolute mode.
				if (!button_relativeView_distEval.isSelected()) {
					// Adjust x-axis.
					findDistanceBinCountMaximum(distanceBinList);
					
					// Adjust y-axis.
					adjustYAxisRange();
				}
			}
			
			else {
				// Update text info.
				label_avg.setText("λ: -");
				label_median.setText("η: -");
			}
		}
		
		else {
			// Update text info.
			label_avg.setText("λ: -");
			label_median.setText("η: -");
		}	
	}
	
	private void addEmptyDataSeries(String name)
	{
		final XYChart.Series<String, Integer> emptyDataSeries = new XYChart.Series<String, Integer>();
		emptyDataSeries.setName(name);
		barchart.getData().add(emptyDataSeries);
	}

	/**
	 * Assign given distances to specified number of bins. Adds these bins to the distance barchart automatically.
	 * Return list of bins containing the number of distances fitting the respective boundaries.
	 * @param distances
	 * @param numberOfBins
	 * @param numberOfElements
	 * @param updateQuantileLabels
	 * @return
	 */
	private int[] addDataSeries(String name, double[][] distances, int numberOfBins, int numberOfElements, boolean updateQuantileLabels)
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
		
		// Examine distance data.
		for (int i = 0; i < distances.length; i++) {
			for (int j = i + 1; j < distances.length; j++) {
				// Sum up distances.
				sum += distances[i][j];
				
				// Copy values in one-dimensional array.
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
		min = button_relativeView_distEval.isSelected() ? filteredDistanceExtrema.getKey().floatValue() : globalDistanceExtrema.getKey().floatValue();
		max = button_relativeView_distEval.isSelected() ? filteredDistanceExtrema.getValue().floatValue() : globalDistanceExtrema.getValue().floatValue();

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
			label_avg.setText("λ: " + String.valueOf(avg));
			label_median.setText("η: " + String.valueOf(median));
		}
		
		// Update barchart - add data series.
		barchart.getData().add(generateDistanceHistogramDataSeries(name, distanceBinList, numberOfBins, binInterval, min, max));
		
		return distanceBinList;
	}
	
	/**
	 * Generates data series for histogram of dataset distances.
	 * @param numberOfBins
	 * @param binInterval
	 * @param min
	 * @param max
	 * @return
	 */
	private XYChart.Series<String, Integer> generateDistanceHistogramDataSeries(String name, int[] distanceBinList, int numberOfBins, double binInterval, double min, double max)
	{
		final XYChart.Series<String, Integer> data_series	= new XYChart.Series<String, Integer>();
		//Map<String, Integer> categoryCounts					= new HashMap<String, Integer>();
		
		// Set name.
		data_series.setName(name);
		
		// Add data points.
		for (int i = 0; i < numberOfBins; i++) {
			String categoryDescription	= String.valueOf(min + i * binInterval);
			categoryDescription			= categoryDescription.length() > 5 ? categoryDescription.substring(0, 5) : categoryDescription;
			int value					= distanceBinList[i];
			
			// Avoid category clash by adding "invisible" characters.
			for (int x = 0; x < i; x++)
				categoryDescription += (char)29;
			
			// Bin again by category name, if that's necessary.
			/*
			if (!categoryCounts.containsKey(categoryDescription)) {
				categoryCounts.put(categoryDescription, value);
			}
			
			else {
				value += categoryCounts.get(categoryDescription);
				categoryCounts.put(categoryDescription, value);
			}
			*/
			data_series.getData().add(new XYChart.Data<String, Integer>(categoryDescription, value));
		}
		
		return data_series;
	}
	
	@Override
	public void changeViewMode()
	{
		// Toggle auto-ranging.
		barchart.getYAxis().setAutoRanging(button_relativeView_distEval.isSelected());
		
		if (button_relativeView_distEval.isSelected()) {
			filteredDistanceExtrema = identifyExtrema(filteredDistances);
		}
		
		// Refresh chart.
		refresh(filteredDistances, selectedFilteredDistances, false);
	}
	
	private void findDistanceBinCountMaximum(int[] distanceBinList)
	{
		// If in absolute mode for the first time (important: The first draw always happens in absolute
		// view mode!): Get maximal count of distances associated with one bin.
		if (!distanceBinCountMaximumDetermined) {
			distanceBinCountMaximum = 0;
			for (int binCount : distanceBinList) {
				distanceBinCountMaximum = binCount > distanceBinCountMaximum ? binCount : distanceBinCountMaximum;
			}
			
			distanceBinCountMaximumDetermined = true;
		}
	}
	
	/**
	 * Default view mode is absolute - disable auto-ranging on axes, configure ticks.
	 */
	private void adjustYAxisRange()
	{
		numberaxis_distanceEvaluation_yaxis.setLowerBound(0);
		numberaxis_distanceEvaluation_yaxis.setUpperBound(distanceBinCountMaximum * 1.1);
		
    	// Adjust tick width.
    	final int numberOfTicks = 5;
    	numberaxis_distanceEvaluation_yaxis.setTickUnit( (float)distanceBinCountMaximum / numberOfTicks);
    	numberaxis_distanceEvaluation_yaxis.setMinorTickCount(4);
	}
}
