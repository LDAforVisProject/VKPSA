package view.components;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.util.Pair;
import control.AnalysisController;

public class DistancesBarchart extends VisualizationComponent
{
	/*
	 * GUI elements.
	 */
	
	private BarChart<String, Integer> barchart;
	private NumberAxis numberaxis_distanceEvaluation_yaxis;
	
	private Label label_min;
	private Label label_max;
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
	 * Holds information about what's the highest number of distances associated with one bin in the 
	 * current environment (in the distance histogram).
	 */
	private int distanceBinCountMaximum;
	/**
	 * Indicates wheteher or not the maximal count of distances in a bin was yet determined.
	 */
	private boolean distanceBinCountMaximumDetermined;
	
	
	public DistancesBarchart(	AnalysisController analysisController, BarChart<String, Integer> barchart_distances, 
								NumberAxis numberaxis_distanceEvaluation_yaxis, Label label_min, Label label_max, 
								Label label_avg, Label label_median, ToggleButton button_relativeView_distEval)
	{
		super(analysisController);

		this.barchart 								= barchart_distances;
		this.numberaxis_distanceEvaluation_yaxis	= numberaxis_distanceEvaluation_yaxis;
		this.label_min								= label_min;
		this.label_max								= label_max;
		this.label_avg								= label_avg;
		this.label_median							= label_median;
		this.button_relativeView_distEval			= button_relativeView_distEval;
		
		globalDistanceExtrema				= new Pair<Double, Double>(null, null);
		distanceBinCountMaximum				= 0;
		distanceBinCountMaximumDetermined	= false;
		
		initBarchart();
	}
	
	private void initBarchart()
	{
		barchart.setLegendVisible(false);
		barchart.setAnimated(false);
		barchart.setBarGap(0);
		
		// Disable auto-ranging (absolute view is default).
		barchart.getXAxis().setAutoRanging(true);
		barchart.getYAxis().setAutoRanging(false);
	}

	public void identifyGlobalExtrema(double[][] distances)
	{
		// Identify global distance extrema.
		double minX				= Double.MAX_VALUE;
		double maxX 			= Double.MIN_VALUE;
		
		for (int i = 0; i < distances.length; i++) {
			for (int j = i + 1; j < distances[i].length; j++) {
				minX = distances[i][j] < minX ? distances[i][j] : minX;
				maxX = distances[i][j] > maxX ? distances[i][j] : maxX;				
			}
		}
		
		globalDistanceExtrema = new Pair<Double, Double>(minX, maxX);
	}
	
	public void refresh(double distances[][])
	{
		// Calculate binning parameters.
		final int numberOfBins		= 50;
		final int numberOfElements	= ((distances.length - 1) * (distances.length - 1) + (distances.length - 1)) / 2;
		float distancesFlattened[]	= new float[numberOfElements];
	
		if (numberOfElements > 0) {
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
			min = button_relativeView_distEval.isSelected() ? distancesFlattened[0] : globalDistanceExtrema.getKey().floatValue();
			max = button_relativeView_distEval.isSelected() ? distancesFlattened[distancesFlattened.length - 1] : globalDistanceExtrema.getValue().floatValue();

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
			label_avg.setText(String.valueOf(avg));
			label_median.setText(String.valueOf(median));
			label_min.setText(String.valueOf(min));
			label_max.setText(String.valueOf(max));
			
			// Update barchart.
			//	Clear old data.
			barchart.getData().clear();
			//	Add data series.
			barchart.getData().add(generateDistanceHistogramDataSeries(distanceBinList, numberOfBins, binInterval, min, max));
			
			// Set y-axis range, if in absolute mode.
			if (!button_relativeView_distEval.isSelected()) {
				// If in absolute mode for the first time (important: The first draw always happens in absolute
				// view mode!): Get maximal count of distances associated with one bin.
				if (!distanceBinCountMaximumDetermined) {
					distanceBinCountMaximum = 0;
					for (int binCount : distanceBinList) {
						distanceBinCountMaximum = binCount > distanceBinCountMaximum ? binCount : distanceBinCountMaximum;
					}
					
					distanceBinCountMaximumDetermined = true;
				}
				
				ValueAxis<Integer> yAxis = (ValueAxis<Integer>) barchart.getYAxis();
				yAxis.setLowerBound(0);
				yAxis.setUpperBound(distanceBinCountMaximum * 1.1);
			}
		}
		
		else {
			// Clear chart.
			barchart.getData().clear();
			
			// Update text info.
			label_avg.setText("-");
			label_median.setText("-");
			label_min.setText("-");
			label_max.setText("-");
		}
	}
	
	/**
	 * Generates data series for histogram of dataset distances.
	 * @param numberOfBins
	 * @param binInterval
	 * @param min
	 * @param max
	 * @return
	 */
	private XYChart.Series<String, Integer> generateDistanceHistogramDataSeries(int[] distanceBinList, int numberOfBins, double binInterval, double min, double max)
	{
		final XYChart.Series<String, Integer> data_series	= new XYChart.Series<String, Integer>();
		Map<String, Integer> categoryCounts					= new HashMap<String, Integer>();
		
		for (int i = 0; i < numberOfBins; i++) {
			String categoryDescription	= String.valueOf(min + i * binInterval);
			categoryDescription			= categoryDescription.length() > 5 ? categoryDescription.substring(0, 5) : categoryDescription;
			int value					= distanceBinList[i];
			
			// Bin again by category name, if that's necessary.
			if (!categoryCounts.containsKey(categoryDescription)) {
				categoryCounts.put(categoryDescription, value);
			}
			
			else {
				value += categoryCounts.get(categoryDescription);
				categoryCounts.put(categoryDescription, value);
			}
			
			data_series.getData().add(new XYChart.Data<String, Integer>(categoryDescription, value));
		}
		
		return data_series;
	}
	
	@Override
	public void changeViewMode(double[][] distances)
	{
		// Toggle auto-ranging.
		barchart.getYAxis().setAutoRanging(button_relativeView_distEval.isSelected());
		
		// If in absolute mode: Manually set axis range options.
		if (!button_relativeView_distEval.isSelected())
			adjustYAxisTickWidth();
		
		// Refresh chart.
		refresh(distances);
	}
	
	/**
	 * Default view mode is absolute - disable auto-ranging on axes, configure ticks.
	 */
	private void adjustYAxisTickWidth()
	{
    	// Adjust tick width.
    	final int numberOfTicks = 5;
    	numberaxis_distanceEvaluation_yaxis.setTickUnit( (float)distanceBinCountMaximum / numberOfTicks);
    	numberaxis_distanceEvaluation_yaxis.setMinorTickCount(4);
	}
}
