package control;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import view.components.heatmap.HeatMap;
import mdsj.Data;
import model.workspace.WorkspaceAction;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

public class AnalysisController extends Controller
{
	// -----------------------------------------------
	// 				UI elements
	// -----------------------------------------------
	
	private @FXML AnchorPane anchorpane_parameterSpace_distribution;
	
	/*
	 * MDS Scatterchart.
	 */
	
	private @FXML ScatterChart<Number, Number> scatterchart_global;
	private Axis<Number> xAxis;
	private Axis<Number> yAxis;
	
	/*
	 * Distances barchart. 
	 */
	
	private @FXML BarChart<String, Integer> barchart_distances;
	
	private @FXML Label label_min;
	private @FXML Label label_max;
	private @FXML Label label_avg;
	private @FXML Label label_median;
	
	/*
	 * Parameter Space - Distribution
	 */
	
	private @FXML Label label_parameterSpace_distribution_key1;
	private @FXML Label label_parameterSpace_distribution_key2;
	private @FXML Canvas canvas_heatmap;
	private HeatMap heatmap_parameterspace;
	
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
		initDistanceBarchart();
		initHeatmap();
		
		addResizeListeners();
	}
	
	private void addResizeListeners()
	{
		anchorpane_parameterSpace_distribution.widthProperty().addListener(new ChangeListener<Number>() {
		    @Override 
		    public void changed(ObservableValue<? extends Number> observableValue, Number oldWidth, Number newWidth)
		    {
		        resizeElement(anchorpane_parameterSpace_distribution, newWidth.doubleValue(), 0);
		    }
		});
		
		anchorpane_parameterSpace_distribution.heightProperty().addListener(new ChangeListener<Number>() {
		    @Override 
		    public void changed(ObservableValue<? extends Number> observableValue, Number oldHeight, Number newHeight) 
		    {
		    	resizeElement(anchorpane_parameterSpace_distribution, 0, newHeight.doubleValue());
		    }
		});
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
	
	private void initDistanceBarchart()
	{
		barchart_distances.setLegendVisible(false);
	}
	
	private void initHeatmap()
	{
		heatmap_parameterspace = new HeatMap(canvas_heatmap, label_parameterSpace_distribution_key1, label_parameterSpace_distribution_key2);
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
		heatmap_parameterspace.update(workspace.getLDAConfigurations(), "alpha", "eta");
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
		final int numberOfBins		= 50;
		final int numberOfElements	= ((distances.length - 1) * (distances.length - 1) + (distances.length - 1)) / 2;
		float distancesFlattened[]	= new float[numberOfElements];
		
		// Determine statistical measures.
		// @todo Add boxplot for quantiles (and average)? Drop labels instead.
		
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
		double binInterval		= (max - min) / numberOfBins;
		
		for (float value : distancesFlattened) {
			// Increment content of corresponding bin.
			distanceBinList[(int) ( (value - min) / binInterval)]++;	
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
		barchart_distances.getData().clear();
		//	Add data series.
		barchart_distances.getData().add(generateDistanceHistogramDataSeries(distanceBinList, numberOfBins, binInterval, min, max));
	}
	
	private XYChart.Series<String, Integer> generateDistanceHistogramDataSeries(int[] distanceBinList, int numberOfBins, double binInterval, double min, double max)
	{
		final XYChart.Series<String, Integer> data_series = new XYChart.Series<String, Integer>();
		
		for (int i = 0; i < numberOfBins; i++) {
			String categoryDescription	= String.valueOf(min + i * binInterval);
			categoryDescription			= categoryDescription.length() > 4 ? categoryDescription.substring(0, 4) : categoryDescription;
			
			data_series.getData().add(new XYChart.Data<String, Integer>(categoryDescription, distanceBinList[i] ));
		}
		
		return data_series;
	}

	@Override
	public void resizeContent(double width, double height)
	{
	}
	
	@Override
	protected void resizeElement(Node node, double width, double height)
	{
		switch (node.getId()) {
			case "anchorpane_parameterSpace_distribution":
				
				// Adapt width.
				if (width > 0) {
					canvas_heatmap.setWidth(width - 30 - 20);
					heatmap_parameterspace.update();
				}
				
				// Adapt height.
				if (height > 0) {
					canvas_heatmap.setHeight(height - 40 - 30);
					heatmap_parameterspace.update();
				}
			break;
		}
	}
}
