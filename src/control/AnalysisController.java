package control;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import model.LDAConfiguration;

import org.controlsfx.control.RangeSlider;

import view.components.HoveredThresholdNode;
import view.components.heatmap.HeatMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
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
	private @FXML Accordion accordion_options;
	private @FXML TitledPane titledpane_filter;
	
	/*
	 * MDS Scatterchart.
	 */
	
	private @FXML ScatterChart<Number, Number> scatterchart_global;
	private NumberAxis scatterchart_xAxis;
	private NumberAxis scatterchart_yAxis;
	
	/*
	 * Distances barchart. 
	 */
	
	private @FXML BarChart<String, Integer> barchart_distances;
	
	private @FXML Label label_min;
	private @FXML Label label_max;
	private @FXML Label label_avg;
	private @FXML Label label_median;
	
	/*
	 * Toggle buttons indicating view mode.
	 */
	
	private @FXML ToggleButton button_relativeView_distEval;
	private @FXML ToggleButton button_relativeView_paramDist;
	private @FXML ToggleButton button_relativeView_paramDC;
	
	/*
	 * Parameter Space - Distribution.
	 */
	
	private @FXML Canvas canvas_heatmap;
	private @FXML NumberAxis numberaxis_parameterSpace_xaxis;
	private @FXML NumberAxis numberaxis_parameterSpace_yaxis;
	private @FXML ComboBox<String> combobox_parameterSpace_distribution_xAxis;
	private @FXML ComboBox<String> combobox_parameterSpace_distribution_yAxis;
	private HeatMap heatmap_parameterspace;

	/*
	 * Parameter Space - Distance correlation.
	 */
	
	private @FXML LineChart<Float, Double> linechart_distanceCorrelation;
	
	private @FXML CheckBox checkbox_ddc_alpha;
	private @FXML CheckBox checkbox_ddc_kappa;
	private @FXML CheckBox checkbox_ddc_eta;
	
	private @FXML VBox vbox_ddcHoverInformation;
	
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
	
	/**
	 * MDS coordinates of all datasets in workspace / currently loaded.
	 */
	private double[][] coordinates;
	/**
	 * Distances of all datasets in workspace / currently loaded.
	 */
	private double[][] distances;
	/**
	 * LDA configurations all datasets in workspace / currently loaded.
	 */
	private ArrayList<LDAConfiguration> ldaConfigurations;
	
	/**
	 * Set of all datasets matching the currently defined thresholds.
	 */
	private Set<Integer> filteredIndices;
	
	/**
	 * Map storing all selected MDS chart points as values; their respective indices as keys.
	 */
	private Map<Integer, XYChart.Data<Number, Number>> selectedMDSPoints;
	
	/**
	 * Number of steps in dataset distance correlation linechart.
	 */
	private int ddcNumberOfSteps;
	
	/**
	 * Stores filterd coordinates.
	 */
	private double filteredCoordinates[][];
	/**
	 * Stores filtered distances.
	 */
	private double filteredDistances[][];
	/**
	 * List of LDA filtered configurations.
	 */
	private ArrayList<LDAConfiguration> filteredLDAConfigurations;
	
	/**
	 * Global coordinate extrema on x axis (absolute; not filter- or selection-specific).
	 */
	private Pair<Double, Double> globalCoordinateExtrema_X;
	/**
	 * Global coordinate extrema on y axis (absolute; not filter- or selection-specific).
	 */
	private Pair<Double, Double> globalCoordinateExtrema_Y;
	/**
	 * Global distance extrema on x axis (absolute; not filter- or selection-specific).
	 */
	private Pair<Double, Double> globalDistanceExtrema;
	/**
	 * Flag indicating whether or not the global extrema were already identified.
	 */
	private boolean globalExtremaIdentified;
	/**
	 * Holds information about what's the highest number of distances associated with one bin in the 
	 * current environment (in the distance histogram).
	 */
	private int distanceBinCountMaximum;
	private boolean distanceBinCountMaximumDetermined;
	
	// -----------------------------------------------
	// -----------------------------------------------
	// 					Methods
	// -----------------------------------------------
	// -----------------------------------------------
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing SII_AnalysisController.");
	
		// Init collection of selected indices.
		filteredIndices						= new LinkedHashSet<Integer>();
		// Init collection of selected data points in the MDS scatterchart.
		selectedMDSPoints					= new HashMap<Integer, XYChart.Data<Number, Number>>();

		// Init pairs holding global extrema information.
		globalCoordinateExtrema_X			= new Pair<Double, Double>(Double.MAX_VALUE, Double.MIN_VALUE);
		globalCoordinateExtrema_Y			= new Pair<Double, Double>(Double.MAX_VALUE, Double.MIN_VALUE);
		globalExtremaIdentified				= false;
		distanceBinCountMaximum				= 0;
		distanceBinCountMaximumDetermined	= false;
		
		// Init GUI elements.
		initUIElements();
		addResizeListeners();
		
		// Expand filter pane.
		accordion_options.setExpandedPane(titledpane_filter);
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
		initDDCLineChart();
		initHeatmap();
	}
	
	private void initDDCLineChart()
	{
		linechart_distanceCorrelation.setAnimated(false);
		linechart_distanceCorrelation.setCreateSymbols(false);
		
		vbox_ddcHoverInformation.setVisible(false);
		vbox_ddcHoverInformation.getStyleClass().addAll("chart-line-symbol");
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
		scatterchart_xAxis = (NumberAxis) scatterchart_global.getXAxis();
        scatterchart_yAxis = (NumberAxis) scatterchart_global.getYAxis();
        
        // Disable auto ranging.
    	scatterchart_xAxis.setAutoRanging(false);
		scatterchart_yAxis.setAutoRanging(false);
		
		// Use minimum and maximum to create range.
		scatterchart_xAxis.setForceZeroInRange(false);
		scatterchart_yAxis.setForceZeroInRange(false);
		
        scatterchart_global.setVerticalGridLinesVisible(true);
	}
	
	private void initDistanceBarchart()
	{
		barchart_distances.setLegendVisible(false);
		barchart_distances.setAnimated(false);
		barchart_distances.setBarGap(0);
		
		// Disable auto-ranging (absolute view is default).
		barchart_distances.getXAxis().setAutoRanging(true);
		barchart_distances.getYAxis().setAutoRanging(false);
	}
	
	private void initHeatmap()
	{
		combobox_parameterSpace_distribution_xAxis.getItems().clear();
		combobox_parameterSpace_distribution_yAxis.getItems().clear();
		
		for (String param : LDAConfiguration.SUPPORTED_PARAMETERS) {
			combobox_parameterSpace_distribution_xAxis.getItems().add(param);
			combobox_parameterSpace_distribution_yAxis.getItems().add(param);
		}
		
		combobox_parameterSpace_distribution_xAxis.setValue("alpha");
		combobox_parameterSpace_distribution_yAxis.setValue("eta");
		
		heatmap_parameterspace = new HeatMap(canvas_heatmap, numberaxis_parameterSpace_xaxis, numberaxis_parameterSpace_yaxis);
	}
	
	public void refreshVisualizations(boolean filterData)
	{
		// Get LDA configurations. Important: Integrity/consistency checks ensure that
		// workspace.ldaConfigurations and coordinates/distances are in the same order. 
		ldaConfigurations 	= workspace.getLDAConfigurations();
		// Load current MDS data from workspace.
		coordinates			= workspace.getMDSCoordinates();
		// Load current distance data from workspace.
		distances			= workspace.getDistances();
		
		refreshParameterHistograms(50);
		
		// Draw entire data set. Used as initialization call, executed by Workspace instance.
		if (filterData) {
			// Filter data.
			filterData();
		}
		
		// If not done already: Discover global extrema.
		if (!globalExtremaIdentified) {
			identifyGlobalExtrema(coordinates, distances);
		}
		
		// Use AnalysisController.filteredIndices to filter out data in desired parameter boundaries.
		filteredCoordinates			= new double[coordinates.length][filteredIndices.size()];
		filteredDistances			= new double[filteredIndices.size()][filteredIndices.size()];
		filteredLDAConfigurations	= new ArrayList<LDAConfiguration>(filteredIndices.size());
		
		// Copy data corresponding to chosen LDA configurations in new arrays.
		int count = 0;
		for (int selectedIndex : filteredIndices) {
			// Copy MDS coordinates.
			for (int column = 0; column < coordinates.length; column++) {
				filteredCoordinates[column][count] = coordinates[column][selectedIndex];
			}
			
			// Copy distances.
			int innerCount = 0;
			for (int selectedInnerIndex : filteredIndices) {
				filteredDistances[count][innerCount] = distances[selectedIndex][selectedInnerIndex];
				innerCount++;
			}
			
			// Copy LDA configurations.
			filteredLDAConfigurations.add(ldaConfigurations.get(selectedIndex));
			
			count++;
		}
		
		// Refresh visualizations.
		refreshMDSScatterchart(filteredCoordinates, filteredIndices);
		refreshDistanceBarchart(filteredDistances);
		refreshDistanceLinechart(filteredLDAConfigurations, filteredDistances);
		heatmap_parameterspace.refresh(ldaConfigurations, filteredLDAConfigurations, combobox_parameterSpace_distribution_xAxis.getValue(), combobox_parameterSpace_distribution_yAxis.getValue(), button_relativeView_paramDist.isSelected());
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
    	final int numberOfTicks = ldaConfigurations.size();
    	scatterchart_xAxis.setTickUnit( diffX / numberOfTicks);
    	scatterchart_yAxis.setTickUnit( diffY / numberOfTicks);
    	scatterchart_xAxis.setMinorTickCount(4);
    	scatterchart_yAxis.setMinorTickCount(4);
	}
	
	private void identifyGlobalExtrema(double coordinates[][], double distances[][])
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
		
		// Identify global distance extrema.
		minX = Double.MAX_VALUE;
		maxX = Double.MIN_VALUE;
		
		for (int i = 0; i < distances.length; i++) {
			for (int j = i + 1; j < distances[i].length; j++) {
				minX = distances[i][j] < minX ? distances[i][j] : minX;
				maxX = distances[i][j] > maxX ? distances[i][j] : maxX;				
			}
		}
		
		globalDistanceExtrema = new Pair<Double, Double>(minX, maxX);
		
		// Set flag.
		globalExtremaIdentified = true;
	}
	
	private void addEventHandlerToRangeSlider(RangeSlider rs, String parameter)
	{
		// Add listener to determine position during after release.
		rs.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	// Update control values after slider values where changed.
            	updateControlValues(rs, parameter);

            	// Filter by values; refresh visualizations.
            	refreshVisualizations(true);
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
			if (fitsBoundaries && !filteredIndices.contains(i)) {
				filteredIndices.add(i);
			}
			// Else if not in boundaries and contained in selection: Remove.
			else if (!fitsBoundaries && filteredIndices.contains(i)) {
				filteredIndices.remove(i);
			}
		}
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
	 * @param coordinates
	 * @param filteredIndices 
	 */
	private void refreshMDSScatterchart(double coordinates[][], Set<Integer> filteredIndices)
	{			
		// Clear scatterchart.
		scatterchart_global.getData().clear();
		
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
        scatterchart_global.getData().add(0, selectedDataSeries);
        scatterchart_global.getData().add(dataSeries);
        
        // Add mouse listeners.
        addMouseListenersToMDSScatterchart();
        
        // Update scatterchart ranges.
        updateMDSScatterchartRanges();
	}
	
	private void addMouseListenersToMDSScatterchart()
	{
		// Add listeners to all series of non-selected data points.
		for (int i = 1; i < scatterchart_global.getData().size(); i++) {
	        // Add mouse event listeners to points in this data series.
	        for (XYChart.Data<Number, Number> dataPoint : scatterchart_global.getData().get(i).getData()) {
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
					    		refreshMDSScatterchart(filteredCoordinates, filteredIndices);
					    	}
				    	}
//				    	@todo: Next: 	- Highlighting in other charts (if highlighting is enabled)
//				    					- Selection via mouse rectangle
				    }
				});
	        }
		}
		
		// Add mouse listeners for selected data points.
        for (XYChart.Data<Number, Number> dataPoint : scatterchart_global.getData().get(0).getData()) {
        	dataPoint.getNode().setOnMouseClicked(new EventHandler<MouseEvent>()
			{
			    @Override
			    public void handle(MouseEvent mouseEvent)
			    {       
			    	if (mouseEvent.isControlDown()) {
				    	// Remember which points were selected.
			    		selectedMDSPoints.remove(dataPoint.getExtraValue());
			    		// Refresh scatterchart.
			    		refreshMDSScatterchart(filteredCoordinates, filteredIndices);
			    	}
			    }
			});
        }
	}
	
	private void refreshDistanceBarchart(double distances[][])
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
			
			// If in absolute mode for the first time (important: The first draw always happens in absolute
			// view mode!): Get maximal count of distances associated with one bin.
			if (!distanceBinCountMaximumDetermined && !button_relativeView_distEval.isSelected()) {
				distanceBinCountMaximum = 0;
				for (int binCount : distanceBinList) {
					distanceBinCountMaximum = binCount > distanceBinCountMaximum ? binCount : distanceBinCountMaximum;
				}
				
				distanceBinCountMaximumDetermined = true;
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
			
			// Set y-axis range, if in absolute mode.
			if (!button_relativeView_distEval.isSelected()) {
				ValueAxis<Integer> yAxis = (ValueAxis<Integer>) barchart_distances.getYAxis();
				yAxis.setLowerBound(0);
				yAxis.setUpperBound(distanceBinCountMaximum * 1.1);
			}
		}
		
		else {
			// Update text info.
			label_avg.setText("-");
			label_median.setText("-");
			label_min.setText("-");
			label_max.setText("-");
		}
	}
	
	private void refreshDistanceLinechart(ArrayList<LDAConfiguration> ldaConfigurations, double[][] distances)
	{
		// Clear data.
		linechart_distanceCorrelation.getData().clear();
		
		if (ldaConfigurations.size() > 0) {
			// Store chart data.
			Map<String, XYChart.Series<Float, Double>> diffDataPointSeries	= new HashMap<String, XYChart.Series<Float, Double>>();
			
			// Get information which parameters are to be coupled.
			ArrayList<String> coupledParameters = new ArrayList<String>();
			
			if (checkbox_ddc_alpha.isSelected())
				coupledParameters.add("alpha");
			
			if (checkbox_ddc_eta.isSelected())
				coupledParameters.add("eta");
			
			if (checkbox_ddc_kappa.isSelected())
				coupledParameters.add("kappa");
			
			
			// Init data series.
			for (String param : coupledParameters) {
				final XYChart.Series<Float, Double> dataSeries = new XYChart.Series<Float, Double>();
				dataSeries.setName(LDAConfiguration.getSymbolForParameter(param));
				diffDataPointSeries.put(param, dataSeries);
			}
				
			
			/*
			 * Determine metadata (step size, thresholds for all parameters).
			 */
			
			ddcNumberOfSteps								= distances.length;
			Map<String, Double> stepSizes					= new HashMap<String, Double>();
			Map<String, Pair<Double, Double>> thresholds	= determineThresholds(ddcNumberOfSteps, stepSizes, ldaConfigurations, coupledParameters);
			double prevValue								= 0;
			
			
			/*
			 * Calculate distances for each step.
			 */
			
			for (int step = 0; step <= ddcNumberOfSteps; step++) {
				// Store indices of LDAConfigurations matching the current pattern.
				Map<String, Set<Integer>> fixedLDAConfigIndiceSets			= new HashMap<String, Set<Integer>>();
				Map<String, Set<Integer>> freeLDAConfigIndiceSets			= new HashMap<String, Set<Integer>>();
				
				// Init maps of index sets associating a LDA cofiguration with the corresponding collection.
				for (String param : coupledParameters) {
					fixedLDAConfigIndiceSets.put(param, new HashSet<Integer>());
					freeLDAConfigIndiceSets.put(param, new HashSet<Integer>());
				}
				
				// Calculate thresholds at current step for each coupled parameter.			
				Map<String, Pair<Double, Double>> thresholdsForCurrentStep	= calculateThresholdsForStep(step, coupledParameters, stepSizes, thresholds);
				
				// Assign each LDAConfiguration to one of two sets for each step / set of parameter values.
				for (int i = 0; i < ldaConfigurations.size(); i++) {
					// Check each of the coupled parameters: Is current LDA configuration w.r.t. to all coupled parameters within the boundaries?
					for (String param : coupledParameters) {
						// Get value of current parameter in currently examined LDA configuration.
						double paramValue = ldaConfigurations.get(i).getParameter(param);
						// If one of the selected/coupled parameters of the current LDA configuration exceeds the thresholds of the current step:
						// Important: .getkey() ~ minimum, .getValue() ~ maximum. 
						if (paramValue >= thresholdsForCurrentStep.get(param).getKey() && paramValue <= thresholdsForCurrentStep.get(param).getValue()) {
							fixedLDAConfigIndiceSets.get(param).add(i);
						}
						
						else {
							freeLDAConfigIndiceSets.get(param).add(i);
						}
					}
				}
				
				// Group distances; separated to distances between datasets where fixed parameters fall within the boundaries
				// of the current step and those between all other datasets.
				Map<String, Pair<Double, Double>> segregatedDistanceValues = averageSegregatedDistances(distances, fixedLDAConfigIndiceSets);
				
				// Calculate current step values for parameters.
				Map<String, Double> stepValues = new HashMap<String, Double>();
				for (String stepParam : LDAConfiguration.SUPPORTED_PARAMETERS) {
					stepValues.put(stepParam, thresholds.get(stepParam).getKey() + step * stepSizes.get(stepParam));
				}
				
				// Add new data point for every parameter's data series.
				for (String param : coupledParameters) {
					// Add tooltip on hover to new data point.
					// segregatedDistanceValues.getKey(): Datasets with fixed parameters within boundaries, .getValue(): datasets with free parameters.
					// I.e. a positive value means the fixed parameters induce a (on average) greater distance, a negative value means they induce
					// a lesser distance.
					double currValue							= prevValue = segregatedDistanceValues.get(param).getKey() - segregatedDistanceValues.get(param).getValue();
					XYChart.Data<Float, Double> diffDataPoint	= new XYChart.Data<Float, Double>((float)step / ddcNumberOfSteps, currValue);

					// Set hover node.
					diffDataPoint.setNode(new HoveredThresholdNode(this, prevValue, currValue, stepValues, coupledParameters));
					
					// Store prev value.
					prevValue = currValue;
					
					// Add (normalized) data points to data series.
					diffDataPointSeries.get(param).getData().add(diffDataPoint);
				}

			}
			
			// Add data series to chart.
			for (String param : coupledParameters) {
				linechart_distanceCorrelation.getData().add(diffDataPointSeries.get(param));
			}
		}
	}
	
	/**
	 * Examines distances and calculates two sums: One all specified fixed datasets and one for all other.
	 * Does this seperately for each configured fixed parameter.
	 * @param distances
	 * @param fixedLDAConfigIndices
	 * @return Map of pairs containing the sum for (1) all fixed and (2) all other datasets.
	 */
	private Map<String, Pair<Double, Double>> averageSegregatedDistances(double[][] distances, Map<String, Set<Integer>> fixedLDAConfigIndiceSets)
	{
		Map<String, Double> fixedDistancesSums		= new HashMap<String, Double>();
		Map<String, Double> otherDistancesSums		= new HashMap<String, Double>();
		Map<String, Integer> fixedDistancesCounts	= new HashMap<String, Integer>();
		Map<String, Integer> otherDistancesCounts	= new HashMap<String, Integer>();
		
		// Init collections storing distance counts.
		for (String param : fixedLDAConfigIndiceSets.keySet()) {
			fixedDistancesCounts.put(param, 0);
			otherDistancesCounts.put(param, 0);
			fixedDistancesSums.put(param, 0.0);
			otherDistancesSums.put(param, 0.0);
		}
		
		// Sum up distances.
		for (int i = 0; i < distances.length; i++) {
			for (int j = i + 1; j < distances.length; j++) {
				// Are datasets at both indices i and j fixed datasets? 
				// Add current distance (i to j) to corresponding sum variable.
				for (String param : fixedLDAConfigIndiceSets.keySet()) {
					if (fixedLDAConfigIndiceSets.get(param).contains(i) && fixedLDAConfigIndiceSets.get(param).contains(j)) {
						// Add current distance to fixed distances sum for this parameter.
						fixedDistancesSums.put(param, fixedDistancesSums.get(param) + distances[i][j]);
						// Increase count of fixed distances for this parameter by 1.
						fixedDistancesCounts.put(param, fixedDistancesCounts.get(param) + 1);
					}
					
					else {
						// Add current distance to other distances sum for this parameter.
						otherDistancesSums.put(param, otherDistancesSums.get(param) + distances[i][j]);
						// Increase count of other distances for this parameter by 1.
						otherDistancesCounts.put(param, otherDistancesCounts.get(param) + 1);
					}
				}
			}
		}
		
		// Use "common" distance value as default if there are no fixed datasets in the current range.
		// otherDistancesCount should never be 0 with n > 0.
		Map<String, Pair<Double, Double>> normalizedDistanceAverages = new HashMap<String, Pair<Double, Double>>();
		
		for (String param : fixedLDAConfigIndiceSets.keySet()) {
			double normalizedOtherDistanceAverage = otherDistancesCounts.get(param) > 0 ? otherDistancesSums.get(param) / otherDistancesCounts.get(param) : -1;
			double normalizedFixedDistanceAverage = fixedDistancesCounts.get(param) > 0 ? fixedDistancesSums.get(param) / fixedDistancesCounts.get(param) : normalizedOtherDistanceAverage;
			
			normalizedDistanceAverages.put(param, new Pair<Double, Double>(normalizedFixedDistanceAverage, normalizedOtherDistanceAverage));
		}
		
		
		return normalizedDistanceAverages;
	}
	
	/**
	 * Determines thresholds (minima and maxima) for each parameter in the currently selected collection of datasets. 
	 * @param numberOfSteps
	 * @param stepSizes Collection designed to contain the values for each step and parameter. Is modified during this function.  
	 * @param ldaConfigurations
	 * @param coupledParameters
	 * @return A map containing minima and maxima for all supported parameters.
	 */
	private Map<String, Pair<Double, Double>> determineThresholds(final int numberOfSteps, Map<String, Double> stepSizes, final ArrayList<LDAConfiguration> ldaConfigurations, final ArrayList<String> coupledParameters)
	{
		Map<String, Pair<Double, Double>> thresholds = new HashMap<String, Pair<Double, Double>>();
		
		for (LDAConfiguration ldaConfig : ldaConfigurations) {
			// Examine if there is a new maximum or minimum for each parameter in this LDAConfiguration.
			for (String param : LDAConfiguration.SUPPORTED_PARAMETERS) {
				double currMin = thresholds.containsKey(param) ? thresholds.get(param).getKey() : Double.MAX_VALUE;
				double currMax = thresholds.containsKey(param) ? thresholds.get(param).getValue() : Double.MIN_VALUE;
				
				currMin = currMin > ldaConfig.getParameter(param) ? ldaConfig.getParameter(param) : currMin;
				currMax = currMax < ldaConfig.getParameter(param) ? ldaConfig.getParameter(param) : currMax;
				
				// Update thresholds.
				thresholds.put(param, new Pair<Double, Double>(currMin, currMax));
				
				// Update step sizes.
				stepSizes.put(param, (currMax - currMin) / numberOfSteps);
			}
		}
		
		return thresholds;
	}

	/**
	 * Auxiliary function calculating the thresholds for a specific step.
	 * @param coupledParameters
	 * @param stepSizes
	 * @param thresholds
	 * @return
	 */
	private Map<String, Pair<Double, Double>> calculateThresholdsForStep(final int step, final ArrayList<String> coupledParameters, final Map<String, Double> stepSizes, final Map<String, Pair<Double, Double>> thresholds)
	{
		Map<String, Pair<Double, Double>> thresholdsForCurrentStep = new HashMap<String, Pair<Double, Double>>(); 
		
		for (String param : coupledParameters) {
			double stepSize	= stepSizes.get(param);
			// Current data point: Minimum for this parameter + #step * step size for this parameter.
			double point	= thresholds.get(param).getKey() + step * stepSize; 
			// Minimum and maximum: Current data point +- 0.5 * step size.
			double min		= point - stepSize / 2;
			double max		= point + stepSize / 2;
			
			thresholdsForCurrentStep.put(param, new Pair<Double, Double>(min, max));
		}
		
		return thresholdsForCurrentStep;
	}

	private void refreshParameterHistograms(final int numberOfBins)
	{
		// Map storing one bin list for each parameter.
		Map<String, int[]> parameterBinLists = new HashMap<String, int[]>();
		parameterBinLists.put("alpha", new int[numberOfBins]);
		parameterBinLists.put("eta", new int[numberOfBins]);
		parameterBinLists.put("kappa", new int[numberOfBins]);
		
		// Bin data.
		for (int i = 0; i < ldaConfigurations.size(); i++) {
			for (String param : rangeSliders.keySet()) {
				double binInterval	= (rangeSliders.get(param).getMax() - rangeSliders.get(param).getMin()) / numberOfBins;
				// Calculate index of bin in which to store the current value.
				int index_key		= (int) ( (ldaConfigurations.get(i).getParameter(param) - rangeSliders.get(param).getMin()) / binInterval);
				// Check if element is highest allowed entry.
				index_key = index_key < numberOfBins ? index_key : numberOfBins - 1;
				
				// Increment content of corresponding bin.
				parameterBinLists.get(param)[index_key]++;
			}
		}

		/*
		 * Transfer data to scented widgets.
		 */
		
		// Clear old data.
		barchart_alpha.getData().clear();
		barchart_eta.getData().clear();
		barchart_kappa.getData().clear();

		// Add data series to barcharts.
		barchart_alpha.getData().add(generateParameterHistogramDataSeries("alpha", parameterBinLists, numberOfBins));
		barchart_eta.getData().add(generateParameterHistogramDataSeries("eta", parameterBinLists, numberOfBins));
		barchart_kappa.getData().add(generateParameterHistogramDataSeries("kappa", parameterBinLists, numberOfBins));
	}

	/**
	 * Generates data series for histograms in scented widgets controlling the selected parameter boundaries.
	 * @param key
	 * @param parameterBinLists
	 * @param numberOfBins
	 * @return
	 */
	private XYChart.Series<String, Integer> generateParameterHistogramDataSeries(String key, Map<String, int[]> parameterBinLists, final int numberOfBins)
	{
		final XYChart.Series<String, Integer> data_series = new XYChart.Series<String, Integer>();
		
		for (int i = 0; i < numberOfBins; i++) {
			int binContent = parameterBinLists.get(key)[i];
			data_series.getData().add(new XYChart.Data<String, Integer>(String.valueOf(i), binContent ));
		}
		
		return data_series;
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
					heatmap_parameterspace.refresh();
				}
				
				// Adapt height.
				if (height > 0) {
					canvas_heatmap.setHeight(height - 45 - 45);
					heatmap_parameterspace.refresh();
				}
			break;
		}
	}
	
	@FXML
	public void ddcButtonStateChanged(ActionEvent e)
	{
		double filteredDistances[][]							= new double[filteredIndices.size()][filteredIndices.size()];
		ArrayList<LDAConfiguration> filteredLDAConfigurations	= new ArrayList<LDAConfiguration>(filteredIndices.size());
		
		// Copy data corresponding to chosen LDA configurations in new arrays.
		int count = 0;
		for (int selectedIndex : filteredIndices) {
			// Copy distances.
			int innerCount = 0;
			for (int selectedInnerIndex : filteredIndices) {
				filteredDistances[count][innerCount] = distances[selectedIndex][selectedInnerIndex];
				innerCount++;
			}
			
			// Copy LDA configurations.
			filteredLDAConfigurations.add(ldaConfigurations.get(selectedIndex));
			
			count++;
		}
		
		
		// Refresh line chart.
		refreshDistanceLinechart(filteredLDAConfigurations, filteredDistances);
	}
	
	@FXML
	public void updateHeatmap(ActionEvent e)
	{
		ArrayList<LDAConfiguration> filteredLDAConfigurations = new ArrayList<LDAConfiguration>(filteredIndices.size());
		
		// Copy data corresponding to chosen LDA configurations in new arrays.
		for (int selectedIndex : filteredIndices) {
			// Copy LDA configurations.
			filteredLDAConfigurations.add(ldaConfigurations.get(selectedIndex));
		}
		
		if (heatmap_parameterspace != null && combobox_parameterSpace_distribution_xAxis != null && combobox_parameterSpace_distribution_yAxis != null)
			heatmap_parameterspace.refresh(ldaConfigurations, filteredLDAConfigurations, combobox_parameterSpace_distribution_xAxis.getValue(), combobox_parameterSpace_distribution_yAxis.getValue(), button_relativeView_paramDist.isSelected());
	}
	
	public void updateLinechartInfo(boolean show, ArrayList<Label> labels)
	{
		if (show) {
			vbox_ddcHoverInformation.getChildren().clear();
			vbox_ddcHoverInformation.setVisible(true);
			vbox_ddcHoverInformation.getChildren().addAll(labels);
		}
		
		else {
			vbox_ddcHoverInformation.setVisible(false);
		}
	}
	
	@FXML
	public void changeVisualizationViewMode(ActionEvent e)
	{
		System.out.println("changing view mode - " + ((Node)e.getSource()).getId());
		Node source = (Node) e.getSource();
		
		switch (source.getId()) {
			case "button_relativeView_distEval":
				// Toggle auto-ranging.
				barchart_distances.getYAxis().setAutoRanging(button_relativeView_distEval.isSelected());
				// Refresh chart.
				refreshDistanceBarchart(filteredDistances);
			break;

			case "button_relativeView_paramDist":
				updateHeatmap(e);
			break;
		}
	}
}
