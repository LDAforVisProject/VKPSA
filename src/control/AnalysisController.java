package control;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import mdsj.Data;
import model.workspace.WorkspaceAction;
import javafx.fxml.FXML;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;

public class AnalysisController extends Controller
{
	// -----------------------------------------------
	// 				UI elements
	// -----------------------------------------------
	
	private @FXML ScatterChart<Number, Number> scatterchart_global;
	private Axis<Number> xAxis;
	private Axis<Number> yAxis;
	
	private @FXML BarChart<String, Integer> barchart_distances;
	
	private @FXML Label label_min;
	private @FXML Label label_max;
	private @FXML Label label_avg;
	private @FXML Label label_median;
	
	// -----------------------------------------------
	// 				Other data
	// -----------------------------------------------
	
	private double[][] coordinates;
	private double[][] distances;
	
	// -----------------------------------------------
	// -----------------------------------------------
	// 					Methods
	// -----------------------------------------------
	// -----------------------------------------------
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing SII_AnalysisController.");
		
		initScatterchart();
	}
	
	private void initScatterchart()
	{
		// Init scatterchart.
		xAxis = scatterchart_global.getXAxis();
        yAxis = scatterchart_global.getYAxis();
        
        xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);
        
        scatterchart_global.setLegendVisible(false);
        scatterchart_global.setVerticalGridLinesVisible(true);
	}
	
	public void refreshVisualizations()
	{
		// Load current MDS data from workspace.
		coordinates = workspace.getMDSCoordinates();
		// Load current distance data from workspace.
		distances = workspace.getDistances();
		
		// Refresh visualizations.
		refreshMDSScatterchart();
		refreshDistanceBarchart();
	}
	
	/**
	 * Refresh scatterchart with data from MDS coordinates. 
	 */
	private void refreshMDSScatterchart()
	{			
        final Series<Number, Number> dataSeries = new XYChart.Series<>();
        dataSeries.setName("Data");
        
        for (int i = 0; i < coordinates[0].length; i++) {
        	dataSeries.getData().add(new XYChart.Data<Number, Number>(coordinates[0][i], coordinates[1][i]));
        }
        
        // Add data in scatterchart.
        scatterchart_global.getData().add(dataSeries);
	}
	
	private void refreshDistanceBarchart()
	{
		long start = System.nanoTime();
		
		final int numberOfBins		= 50;
		final int numberOfElements	= ((distances.length - 1) * (distances.length - 1) + (distances.length - 1)) / 2;
		float distancesFlattened[]	= new float[numberOfElements];
		
		// Determine statistical measures.
		// @todo Add boxplot for quantiles?
		
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
		
		// Get maximum and minimum.
		min = distancesFlattened[0];
		max = distancesFlattened[distancesFlattened.length - 1];
		
		// Calculate median.
		if (distancesFlattened.length % 2 == 0)
		    median = (distancesFlattened[distancesFlattened.length / 2] + distancesFlattened[distancesFlattened.length / 2 - 1]) / 2;
		else
		    median = distancesFlattened[distancesFlattened.length / 2];
		
		/*
		 * Bin data.
		 */
		
		int distanceBinList[] 	= new int[numberOfBins];
		double binInterval		= max / numberOfBins;
		
		for (float value : distancesFlattened) {
			// Increment content of corresponding bin.
			distanceBinList[(int) ( (value) / binInterval)]++;	
		}
		
		/*
		 * Update UI. 
		 */

		// Update text info.
		label_avg.setText(String.valueOf(avg));
		label_median.setText(String.valueOf(median));
		label_min.setText(String.valueOf(min));
		label_max.setText(String.valueOf(max));
		
		// @todo Improve data binning.
		
		// Update barchart.
		//	Clear old data.
		barchart_distances.getData().clear();
		//	Add data series.
		barchart_distances.getData().add(generateDistanceHistogramDataSeries(distanceBinList, numberOfBins));
		
		System.out.println("Distance analysis took " + (System.nanoTime() - start) / (1000 * 1000) + " milliseconds.");
	}
	
	private XYChart.Series<String, Integer> generateDistanceHistogramDataSeries(int[] distanceBinList, int numberOfBins)
	{
		final XYChart.Series<String, Integer> data_series = new XYChart.Series<String, Integer>();
		
		for (int i = 0; i < numberOfBins; i++) {
			data_series.getData().add(new XYChart.Data<String, Integer>(String.valueOf(i), distanceBinList[i] ));
		}
		
		return data_series;
	}
}
