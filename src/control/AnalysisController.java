package control;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import mdsj.Data;
import model.LDAConfiguration;

import org.controlsfx.control.RangeSlider;

import view.components.heatmap.HeatMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

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
	 * Parameter Space - Distribution.
	 */
	
	private @FXML Canvas canvas_heatmap;
	private @FXML NumberAxis numberaxis_parameterSpace_xaxis;
	private @FXML NumberAxis numberaxis_parameterSpace_yaxis;
	private HeatMap heatmap_parameterspace;
	
	/*
	 * Filter controls.
	 */
	@FXML private BarChart<String, Integer> barchart_alpha;
	@FXML private BarChart<String, Integer> barchart_eta;
	@FXML private BarChart<String, Integer> barchart_kappa;
	
	private Map<String, RangeSlider> rangeSliders;
	private @FXML VBox vbox_alpha;
	private @FXML VBox vbox_eta;
	private @FXML VBox vbox_kappa;
	
	private @FXML TextField alpha_min_textfield;
	private @FXML TextField alpha_max_textfield;
	private @FXML TextField eta_min_textfield;
	private @FXML TextField eta_max_textfield;
	private @FXML TextField kappa_min_textfield;
	private @FXML TextField kappa_max_textfield;
	
	private @FXML CheckBox checkbox_parameterCoupling;
	
	// -----------------------------------------------
	// 				Other data
	// -----------------------------------------------
	
	private double[][] coordinates;
	private double[][] distances;
	
	private ArrayList<LDAConfiguration> ldaConfigurations;
	private Set<Integer> selectedIndices;
	
	// -----------------------------------------------
	// -----------------------------------------------
	// 					Methods
	// -----------------------------------------------
	// -----------------------------------------------
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing SII_AnalysisController.");
	
		selectedIndices = new HashSet<Integer>();
		
		initUIElements();
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
	
	/**
	 * Updates UI as created with SceneBuilder with better fitting controls.
	 * @return
	 */
	private void initUIElements() 
	{
		initRangeSliders();
		initScatterchart();
		initDistanceBarchart();
		initHeatmap();
	}
	
	private void initRangeSliders()
	{
		rangeSliders	= new HashMap<String, RangeSlider>();
	
		rangeSliders.put("alpha", new RangeSlider());
		rangeSliders.put("eta", new RangeSlider());
		rangeSliders.put("kappa", new RangeSlider());
		
		for (Map.Entry<String, RangeSlider> entry : rangeSliders.entrySet()) {
			RangeSlider rs = entry.getValue();
			
			rs.setMaxWidth(360);
//			rs.setMax(25);
			rs.setMax(100);
			rs.setMajorTickUnit(5);
			rs.setMinorTickCount(29);
			rs.setSnapToTicks(true);
			rs.setShowTickLabels(true);
			rs.setShowTickMarks(true);
			rs.setLowValue(0);
//			rs.setHighValue(25);
			rs.setHighValue(100);
			
			// Get some distance between range sliders and bar charts.
			rs.setPadding(new Insets(10, 0, 0, 0));
			
			addEventHandlerToRangeSlider(rs, entry.getKey());
		}
		
		// Set variable-specific minima and maxima.
		rangeSliders.get("kappa").setMin(1);
//		rangeSliders.get("kappa").setMax(50);
//		rangeSliders.get("kappa").setHighValue(50);
		
		// Adapt textfield values.
		alpha_min_textfield.setText(String.valueOf(rangeSliders.get("alpha").getMin()));
    	alpha_max_textfield.setText(String.valueOf(rangeSliders.get("alpha").getMax()));
	 	eta_min_textfield.setText(String.valueOf(rangeSliders.get("eta").getMin()));
    	eta_max_textfield.setText(String.valueOf(rangeSliders.get("eta").getMax()));
		kappa_min_textfield.setText(String.valueOf(rangeSliders.get("kappa").getMin()));
    	kappa_max_textfield.setText(String.valueOf(rangeSliders.get("kappa").getMax()));
		
		// Add range slider to GUI.
		vbox_alpha.getChildren().add(rangeSliders.get("alpha"));
		vbox_eta.getChildren().add(rangeSliders.get("eta"));
		vbox_kappa.getChildren().add(rangeSliders.get("kappa"));
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
		barchart_distances.setAnimated(false);
		barchart_distances.setBarGap(0);
	}
	
	private void initHeatmap()
	{
		heatmap_parameterspace = new HeatMap(canvas_heatmap, numberaxis_parameterSpace_xaxis, numberaxis_parameterSpace_yaxis);
	}
	
	public void refreshVisualizations(boolean useFilter)
	{
		// Get LDA configurations. Important: Integrity/consistency checks ensure that
		// workspace.ldaConfigurations and coordinates/distances are in the same order. 
		ldaConfigurations 	= workspace.getLDAConfigurations();
		// Load current MDS data from workspace.
		coordinates			= workspace.getMDSCoordinates();
		// Load current distance data from workspace.
		distances			= workspace.getDistances();
		
		// Draw entire data set.
		if (!useFilter) {
			// Refresh visualizations.
			refreshMDSScatterchart(coordinates);
			refreshDistanceBarchart(distances);
			heatmap_parameterspace.update(workspace.getLDAConfigurations(), "alpha", "eta");
		}
		
		// Use AnalysisController.selectedIndices to filter out data in desired parameter boundaries.
		else {
			double filteredCordinates[][]	= new double[coordinates.length][selectedIndices.size()];
			double filteredDistances[][]	= new double[selectedIndices.size()][selectedIndices.size()];
			
			// Copy data corresponding to chosen LDA configurations in new arrays.
			int count = 0;
			for (int selectedIndex : selectedIndices) {
				// Copy MDS coordinates.
				for (int column = 0; column < coordinates.length; column++) {
					filteredCordinates[column][count] = coordinates[column][selectedIndex];
				}
				
				int innerCount = 0;
				for (int selectedInnerIndex : selectedIndices) {
					filteredDistances[count][innerCount] = distances[selectedIndex][selectedInnerIndex];
					innerCount++;
				}
				
				count++;
			}
			
			// Refresh visualizations.
			refreshMDSScatterchart(filteredCordinates);
			refreshDistanceBarchart(filteredDistances);
		}
	}
	
	private void addEventHandlerToRangeSlider(RangeSlider rs, String parameter)
	{
		// Add listener to determine position during after release.
		rs.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	// Update control values after slider values where changed.
            	updateControlValues(rs, parameter);

            	// Filter by values.
            	filterData();
            }
        });
		
		// Add listener to determine position during mouse drag.
		rs.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	// Update control values after slider values where changed.
            	updateControlValues(rs, parameter);
            };
        });
	}
	
	private void filterData()
	{
		// Get parameter boundaries.
		Map<String, Pair<Double, Double>> parameterBoundaries = new HashMap<String, Pair<Double, Double>>();
		
		for (String param : rangeSliders.keySet()) {
			parameterBoundaries.put(param, new Pair<Double, Double>(rangeSliders.get(param).getLowValue(), rangeSliders.get(param).getHighValue()));
		}
		
		// Iterate through all LDA configurations in workspace.
		for (int i = 0; i < ldaConfigurations.size(); i++) {
			LDAConfiguration ldaConfig	= ldaConfigurations.get(i);
			boolean fitsBoundaries		= true;
			
			// Check if LDA configurations is in bounds of all specified parameter boundaries.
			for (Map.Entry<String, Pair<Double, Double>> entry: parameterBoundaries.entrySet()) {
				double value	= ldaConfig.getParameter(entry.getKey());
				double min		= entry.getValue().getKey();
				double max		= entry.getValue().getValue();
				
				// Exclude LDA configuration if limits are exceeded.
				if (value < min || value > max) {
					fitsBoundaries = false;
				}
			}
			
			// If in boundaries and not contained in selection: Add.
			if (fitsBoundaries && !selectedIndices.contains(i)) {
				selectedIndices.add(i);
			}
			// Else if not in boundaries and contained in selection: Remove.
			else if (!fitsBoundaries && selectedIndices.contains(i)) {
				selectedIndices.remove(i);
			}
		}
		
		System.out.println("In selection: " + selectedIndices.size() + " out of " + ldaConfigurations.size());
		
		// Update visualizations.
		refreshVisualizations(true);
	};
	
	/**
	 * Updates control values after slide event ended.
	 * @param rs
	 * @param parameter
	 */
	private void updateControlValues(RangeSlider rs, String parameter)
	{
		Map<String, Double> parameterValues_low		= new HashMap<String, Double>();
		Map<String, Double> parameterValues_high	= new HashMap<String, Double>();
		
		for (String param : rangeSliders.keySet()) {
			parameterValues_low.put(param, rs.getLowValue() >= rangeSliders.get(param).getMin() ? rs.getLowValue() : rangeSliders.get(param).getMin());
			parameterValues_high.put(param, rs.getHighValue() <= rangeSliders.get(param).getMax() ? rs.getHighValue() : rangeSliders.get(param).getMax());
		}
		
		if (!checkbox_parameterCoupling.isSelected()) {
        	switch (parameter) 
        	{
        		case "alpha":
        			alpha_min_textfield.setText(String.valueOf(parameterValues_low.get("alpha")));
                	alpha_max_textfield.setText(String.valueOf(parameterValues_high.get("alpha")));
        		break;
        		
        		case "eta":
        	       	eta_min_textfield.setText(String.valueOf(parameterValues_low.get("eta")));
                	eta_max_textfield.setText(String.valueOf(parameterValues_high.get("eta")));
            	break;
            		
        		case "kappa":
        			kappa_min_textfield.setText(String.valueOf(parameterValues_low.get("kappa")));
                	kappa_max_textfield.setText(String.valueOf(parameterValues_high.get("kappa")));
            	break;	
        	}            		
    	}
    	
    	else {
			alpha_min_textfield.setText(String.valueOf(parameterValues_low.get(parameter)));
        	alpha_max_textfield.setText(String.valueOf(parameterValues_high.get(parameter)));
		 	eta_min_textfield.setText(String.valueOf(parameterValues_low.get(parameter)));
        	eta_max_textfield.setText(String.valueOf(parameterValues_high.get(parameter)));
    		kappa_min_textfield.setText(String.valueOf(parameterValues_low.get(parameter)));
        	kappa_max_textfield.setText(String.valueOf(parameterValues_high.get(parameter)));
        	
        	for (RangeSlider rangeSlider : rangeSliders.values()) {
        		rangeSlider.setLowValue(parameterValues_low.get(parameter));
        		rangeSlider.setHighValue(parameterValues_high.get(parameter));
        	}
    	}
	}
	
	/**
	 * Refresh scatterchart with data from MDS coordinates. 
	 */
	private void refreshMDSScatterchart(double coordinates[][])
	{			
		// Clear scatterchart.
		scatterchart_global.getData().clear();
		
        final Series<Number, Number> dataSeries = new XYChart.Series<>();
        dataSeries.setName("Data");
        
        for (int i = 0; i < coordinates[0].length; i++) {
        	dataSeries.getData().add(new XYChart.Data<Number, Number>(coordinates[0][i], coordinates[1][i]));
        }
        
        // Add data in scatterchart.
        scatterchart_global.getData().add(dataSeries);
	}
	
	private void refreshDistanceBarchart(double distances[][])
	{
		// Clear bar chart.
		barchart_distances.getData().clear();
		
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
			barchart_distances.getData().clear();
			//	Add data series.
			barchart_distances.getData().add(generateDistanceHistogramDataSeries(distanceBinList, numberOfBins, binInterval, min, max));
		}
		
		else {
			// Update text info.
			label_avg.setText("-");
			label_median.setText("-");
			label_min.setText("-");
			label_max.setText("-");
		}
	}
	
	private XYChart.Series<String, Integer> generateDistanceHistogramDataSeries(int[] distanceBinList, int numberOfBins, double binInterval, double min, double max)
	{
		final XYChart.Series<String, Integer> data_series = new XYChart.Series<String, Integer>();
		Map<String, Integer> categoryCounts = new HashMap<String, Integer>();
		
		for (int i = 0; i < numberOfBins; i++) {
			String categoryDescription	= String.valueOf(min + i * binInterval);
			categoryDescription			= categoryDescription.length() > 4 ? categoryDescription.substring(0, 4) : categoryDescription;
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
					canvas_heatmap.setWidth(width - 59 - 57);
					heatmap_parameterspace.update();
				}
				
				// Adapt height.
				if (height > 0) {
					canvas_heatmap.setHeight(height - 45 - 45);
					heatmap_parameterspace.update();
				}
			break;
		}
	}
}
