package control.analysisView;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import model.LDAConfiguration;
import model.workspace.Workspace;

import org.controlsfx.control.RangeSlider;

import control.Controller;
import view.components.DistancesBarchart;
import view.components.HoveredThresholdNode;
import view.components.LocalScopeInstance;
import view.components.MDSScatterchart;
import view.components.heatmap.HeatMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

public class AnalysisController extends Controller
{
	// -----------------------------------------------
	// 				UI elements
	// -----------------------------------------------
	
	private Scene scene;
	
	/*
	 * Anchor pane for options.
	 */
	
	private @FXML AnchorPane anchorpane_parameterSpace_distribution;
	private @FXML Accordion accordion_options;
	private @FXML TitledPane titledpane_filter;
	
	/*
	 * MDS Scatterchart.
	 */
	
	private MDSScatterchart mdsScatterchart;
	
	private @FXML ScatterChart<Number, Number> scatterchart_global;
	
	/*
	 * Distances barchart. 
	 */
	
	private DistancesBarchart distancesBarchart;
	
	private @FXML BarChart<String, Integer> barchart_distances;
	private @FXML NumberAxis numberaxis_distanceEvaluation_yaxis;
	
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
	
	private HeatMap heatmap_parameterspace;
	
	private @FXML Canvas canvas_heatmap;
	private @FXML NumberAxis numberaxis_parameterSpace_xaxis;
	private @FXML NumberAxis numberaxis_parameterSpace_yaxis;
	private @FXML ComboBox<String> combobox_parameterSpace_distribution_xAxis;
	private @FXML ComboBox<String> combobox_parameterSpace_distribution_yAxis;
	
	private @FXML Slider slider_parameterSpace_distribution_granularity;
	private @FXML CheckBox checkbox_parameterSpace_distribution_dynAdjustment;

	/*
	 * Parameter Space - Distance correlation.
	 */
	
	private @FXML LineChart<Float, Double> linechart_distanceCorrelation;
	private @FXML NumberAxis numberAxis_distanceCorrelation_xAxis;
	private @FXML NumberAxis numberAxis_distanceCorrelation_yAxis;
	
	private @FXML CheckBox checkbox_ddc_alpha;
	private @FXML CheckBox checkbox_ddc_kappa;
	private @FXML CheckBox checkbox_ddc_eta;
	
	private @FXML VBox vbox_ddcHoverInformation;
	
	/*
	 * Local scope.
	 */
	
	// At the moment: Only one local scope instance supported (parallel tag clouds).
	private LocalScopeInstance localScopeInstance;
	
	private @FXML AnchorPane anchorpane_localScope;
	private @FXML Label label_visType;
	// Local scope options.
	private @FXML Slider slider_localScope_numTopicsToUse;
	private @FXML TextField textfield_localScope_numTopicsToUse;
	private @FXML Slider slider_localScope_numKeywordsToUse;
	private @FXML TextField textfield_localScope_numKeywordsToUse;
	
	/*
	 * Filter controls.
	 */
	
	private @FXML ScrollPane scrollpane_filter;
	private @FXML GridPane gridpane_parameterConfiguration;
	
	private Map<String, BarChart<String, Integer>> barchartsForFilterControls;
	private @FXML BarChart<String, Integer> barchart_alpha;
	private @FXML BarChart<String, Integer> barchart_eta;
	private @FXML BarChart<String, Integer> barchart_kappa;
	
	private Map<String, RangeSlider> rangeSliders;
	private @FXML VBox vbox_alpha;
	private @FXML VBox vbox_eta;
	private @FXML VBox vbox_kappa;
	
	private Map<String, Pair<TextField, TextField>> textfieldsForFilterControls;
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
	 * Number of steps in dataset distance correlation linechart.
	 */
	private int ddcNumberOfSteps;
	
	/*
	 * Collections holding metadata of filtered or selected datasets. 
	 */
	
	// Filtered data.
	
	/**
	 * Set of all datasets matching the currently defined thresholds.
	 */
	private Set<Integer> filteredIndices;
	/**
	 * Stores filterd coordinates.
	 */
	private double filteredCoordinates[][];
	/**
	 * Stores filtered distances.
	 */
	private double filteredDistances[][];
	/**
	 * List of filtered LDA configurations.
	 */
	private ArrayList<LDAConfiguration> filteredLDAConfigurations;
	
	// Filtered and selected data.
	
	/**
	 * Set of all datasets matching the currently defined thresholds and selection.
	 */
	private Set<Integer> selectedFilteredIndices;
	
	/**
	 * Stores filtered and selecte distances.
	 */
	private double selectedFilteredDistances[][];
	/**
	 * Stores filtered and selected LDA configurations.
	 */
	private ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations;
	
	/*
	 * Information on global extrema.
	 */
	
	/**
	 * Flag indicating whether or not the global extrema were already identified.
	 */
	private boolean globalExtremaIdentified;
	/**
	 * Holds information about what's the highest value associated with a parameter in 
	 * current environment (in the distance line/distance correlation chart).
	 */
	private Pair<Double, Double> dcValueExtrema;
	/**
	 * Indicates wheteher or not the maximal value in the distance line chart was yet determined.
	 */
	private boolean dcValueMaximumDetermined;
	
	
	// -----------------------------------------------
	// -----------------------------------------------
	// 					Methods
	// -----------------------------------------------
	// -----------------------------------------------
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing SII_AnalysisController.");
	
		// Init collection of filtered/selected indices.
		filteredIndices						= new LinkedHashSet<Integer>();
		selectedFilteredIndices				= new LinkedHashSet<Integer>();

		// Init pairs holding global extrema information.
		globalExtremaIdentified				= false;
		dcValueMaximumDetermined			= false;
		dcValueExtrema						= new Pair<Double, Double>(Double.MAX_VALUE, Double.MIN_VALUE);
		
		// Init GUI elements.
		initUIElements();
		addResizeListeners();
		
		// Expand filter pane.
		accordion_options.setExpandedPane(titledpane_filter);
	}
	
	private void addResizeListeners()
	{
		/*
		 * Add resize listeners for parameter space anchor pane.
		 */
		
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
		
		/*
		 * Add resize listeners for local scope anchor pane.
		 */
		
		anchorpane_localScope.widthProperty().addListener(new ChangeListener<Number>() {
		    @Override 
		    public void changed(ObservableValue<? extends Number> observableValue, Number oldWidth, Number newWidth)
		    {
		        resizeElement(anchorpane_localScope, newWidth.doubleValue(), 0);
		    }
		});
		
		anchorpane_localScope.heightProperty().addListener(new ChangeListener<Number>() {
		    @Override 
		    public void changed(ObservableValue<? extends Number> observableValue, Number oldHeight, Number newHeight) 
		    {
		    	resizeElement(anchorpane_localScope, 0, newHeight.doubleValue());
		    }
		});
	}
	
	/**
	 * Updates UI as created with SceneBuilder with better fitting controls.
	 * @return
	 */
	private void initUIElements() 
	{
		initFilterControls();
		initMDSScatterchart();
		initDistanceBarchart();
		initDDCLineChart();
		initHeatmap();
		initLocalScopeView();
	}
	
	private void initLocalScopeView()
	{
		// Determine path to visualization to be loaded.
		String fxmlPath = "/view/SII/localScope/SII_Content_Analysis_LocalScope_ParallelTagCloud.fxml";
		
		// Create new instance of local scope.
		localScopeInstance = new LocalScopeInstance(this, anchorpane_localScope, label_visType, 
													slider_localScope_numTopicsToUse, textfield_localScope_numTopicsToUse,
													slider_localScope_numKeywordsToUse, textfield_localScope_numKeywordsToUse);
		localScopeInstance.load(fxmlPath);
	}
	
	private void initDDCLineChart()
	{
		linechart_distanceCorrelation.setAnimated(false);
		linechart_distanceCorrelation.setCreateSymbols(false);
		
		vbox_ddcHoverInformation.setVisible(false);
		vbox_ddcHoverInformation.getStyleClass().addAll("chart-line-symbol");
		
		resetDDCAxisOptions();
	}

	private void initFilterControls()
	{
		scrollpane_filter.setContent(gridpane_parameterConfiguration);
		
		// Init collections.
		rangeSliders				= new HashMap<String, RangeSlider>();
		textfieldsForFilterControls	= new HashMap<String, Pair<TextField, TextField>>();
		barchartsForFilterControls	= new HashMap<String, BarChart<String, Integer>>();
		
		// Add range sliders to collection.
		rangeSliders.put("alpha", new RangeSlider());
		rangeSliders.put("eta", new RangeSlider());
		rangeSliders.put("kappa", new RangeSlider());
		
		// Add textfields to collection.
		textfieldsForFilterControls.put("alpha", new Pair<TextField, TextField>(alpha_min_textfield, alpha_max_textfield));
		textfieldsForFilterControls.put("eta", new Pair<TextField, TextField>(eta_min_textfield, eta_max_textfield));
		textfieldsForFilterControls.put("kappa", new Pair<TextField, TextField>(kappa_min_textfield, kappa_max_textfield));
		
		// Add barcharts to collections.
		barchartsForFilterControls.put("alpha", barchart_alpha);
		barchartsForFilterControls.put("eta", barchart_eta);
		barchartsForFilterControls.put("kappa", barchart_kappa);
		
		// Init range slider.
		for (Map.Entry<String, RangeSlider> entry : rangeSliders.entrySet()) {
			RangeSlider rs = entry.getValue();
			
			rs.setMaxWidth(360);
			rs.setMax(25);
			rs.setMax(100);
			rs.setMajorTickUnit(5);
			rs.setMinorTickCount(29);
			rs.setSnapToTicks(true);
			rs.setShowTickLabels(true);
			rs.setShowTickMarks(true);
			rs.setLowValue(0);
			rs.setHighValue(25);
			rs.setHighValue(100);
			
			// Get some distance between range sliders and bar charts.
			rs.setPadding(new Insets(5, 0, 0, 4));
			
			// Add event handler - trigger update of visualizations (and the
			// data preconditioning necessary for that) if filter settings
			// are changed.
			addEventHandlerToRangeSlider(rs, entry.getKey());
		}
		
		// Set variable-specific minima and maxima.
		rangeSliders.get("kappa").setMin(1);
		rangeSliders.get("kappa").setMax(50);
		rangeSliders.get("kappa").setHighValue(50);
		
		// Adapt textfield values.
		for (Map.Entry<String, Pair<TextField, TextField>> entry : textfieldsForFilterControls.entrySet()) {
			entry.getValue().getKey().setText( String.valueOf(rangeSliders.get(entry.getKey()).getMin()) );
			entry.getValue().getValue().setText( String.valueOf(rangeSliders.get(entry.getKey()).getMax()) );
		}
		
		// Add range slider to GUI.
		vbox_alpha.getChildren().add(rangeSliders.get("alpha"));
		vbox_eta.getChildren().add(rangeSliders.get("eta"));
		vbox_kappa.getChildren().add(rangeSliders.get("kappa"));
		
		// Set gridpane's height.
		final int prefRowHeight = 100;
		gridpane_parameterConfiguration.setPrefHeight(LDAConfiguration.SUPPORTED_PARAMETERS.length * prefRowHeight);
		gridpane_parameterConfiguration.setMinHeight(LDAConfiguration.SUPPORTED_PARAMETERS.length * prefRowHeight);
	}

	private void initMDSScatterchart()
	{
		mdsScatterchart = new MDSScatterchart(this, scatterchart_global);
	}
	
	private void initDistanceBarchart()
	{
		distancesBarchart = new DistancesBarchart(	this, barchart_distances, numberaxis_distanceEvaluation_yaxis, 
													label_avg, label_median, button_relativeView_distEval);
	}
	
	private void initHeatmap()
	{
		combobox_parameterSpace_distribution_xAxis.getItems().clear();
		combobox_parameterSpace_distribution_yAxis.getItems().clear();
		
		// Add supported parameters to axis comboboxes. 
		for (String param : LDAConfiguration.SUPPORTED_PARAMETERS) {
			combobox_parameterSpace_distribution_xAxis.getItems().add(param);
			combobox_parameterSpace_distribution_yAxis.getItems().add(param);
		}
		
		// Set default axes.
		combobox_parameterSpace_distribution_xAxis.setValue("alpha");
		combobox_parameterSpace_distribution_yAxis.setValue("eta");
		
		// Init heatmap.
		heatmap_parameterspace = new HeatMap(this, canvas_heatmap, numberaxis_parameterSpace_xaxis, numberaxis_parameterSpace_yaxis);
		
		/*
		 * Init option controls.
		 */
		
		// Add listener to determine position during after release.
		slider_parameterSpace_distribution_granularity.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	heatmap_parameterspace.setGranularityInformation(checkbox_parameterSpace_distribution_dynAdjustment.isSelected(), (int) slider_parameterSpace_distribution_granularity.getValue(), true);
            }
        });
		
		// Add listener to determine position during mouse drag.
		slider_parameterSpace_distribution_granularity.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	heatmap_parameterspace.setGranularityInformation(checkbox_parameterSpace_distribution_dynAdjustment.isSelected(), (int) slider_parameterSpace_distribution_granularity.getValue(), true);
            };
        });
	}
	
	/**
	 * Refreshes all visualizations (e.g. after a filter was manipulated).
	 * @param filterData
	 */
	public void refreshVisualizations(boolean filterData)
	{
		// Get LDA configurations. Important: Integrity/consistency checks ensure that
		// workspace.ldaConfigurations and coordinates/distances are in the same order. 
		ldaConfigurations 	= workspace.getLDAConfigurations();
		// Load current MDS data from workspace.
		coordinates			= workspace.getMDSCoordinates();
		// Load current distance data from workspace.
		distances			= workspace.getDistances();
		
		// If not done already: Discover global extrema, adapt components.
		if (!globalExtremaIdentified) {
			// Identify global extrema.
			distancesBarchart.identifyGlobalExtrema(distances);
			mdsScatterchart.identifyGlobalExtrema(coordinates);

			// Add keyboard listener in order to enable selection.
			mdsScatterchart.addKeyListener(scene);
			
			// Mark global extrema as found.
			globalExtremaIdentified = true;
			
			// Adapt controls to new data.
			adjustControlExtrema();
		}
		
		// Draw entire data set. Used as initialization call, executed by Workspace instance.
		if (filterData) {
			// Filter data.
			applyFilter();
		}
		
		// Refresh visualizations.
		refreshParameterHistograms(50);
		mdsScatterchart.refresh(filteredCoordinates, filteredIndices);
		distancesBarchart.refresh(filteredDistances, selectedFilteredDistances, true);
		refreshDistanceLinechart(filteredLDAConfigurations, filteredDistances);
		heatmap_parameterspace.refresh(ldaConfigurations, filteredLDAConfigurations, combobox_parameterSpace_distribution_xAxis.getValue(), combobox_parameterSpace_distribution_yAxis.getValue(), button_relativeView_paramDist.isSelected());
		localScopeInstance.refresh(selectedFilteredLDAConfigurations);
	}

	/**
	 * Refreshes visualization after data in global MDS scatterchart was selected. 
	 * @param selectedIndices
	 */
	public void refreshVisualizationsAfterGlobalSelection(Set<Integer> selectedIndices, boolean includeLocalScope)
	{
		// Update set of filtered and selected indices.
		selectedFilteredIndices				= createSelectedFilteredIndices(filteredIndices, selectedIndices);
		
		// Find selected and filtered values.
		selectedFilteredDistances			= createFilteredSelectedDistanceMatrix(selectedFilteredIndices);
		selectedFilteredLDAConfigurations	= createFilteredSelectedLDAConfigurations(selectedFilteredIndices);
				
		// Refresh other (than MDSScatterchart) visualizations.
		distancesBarchart.refresh(filteredDistances, selectedFilteredDistances, true);
		if (includeLocalScope)
			localScopeInstance.refresh(selectedFilteredLDAConfigurations);
	}
	
	public void refreshLocalScopeAfterGlobalSelection()
	{
		localScopeInstance.refresh(selectedFilteredLDAConfigurations);
	}
	
	private Set<Integer> createSelectedFilteredIndices(Set<Integer> filteredIndices, Set<Integer> selectedIndices)
	{
		Set<Integer> selectedFilteredIndices = new HashSet<Integer>(selectedIndices.size());
		
		for (int selectedIndex : selectedIndices) {
			if (filteredIndices.contains(selectedIndex)) {
				selectedFilteredIndices.add(selectedIndex);
			}
		}
		
		return selectedFilteredIndices;
	}

	/**
	 * Creates list of LDA configurations out of sets of filtered and selected indices. 
	 * @param filteredIndices
	 * @param selectedIndices
	 * @return
	 */
	private ArrayList<LDAConfiguration> createFilteredSelectedLDAConfigurations(Set<Integer> selectedFilteredIndices)
	{
		ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations = new ArrayList<LDAConfiguration>(selectedFilteredIndices.size());
		
		for (int index : selectedFilteredIndices) {
			selectedFilteredLDAConfigurations.add(ldaConfigurations.get(index));
		}
		
		return selectedFilteredLDAConfigurations;
	}
	
	
	
	/**
	 * Creates distance matrix out of sets of filtered and selected indices. 
	 * @param filteredIndices
	 * @param selectedIndices
	 * @return
	 */
	private double[][] createFilteredSelectedDistanceMatrix(Set<Integer> selectedFilteredIndices)
	{
		// Copy actual distance data in array.
		double[][] filteredSelectedDistances = new double[selectedFilteredIndices.size()][selectedFilteredIndices.size()];
		int count = 0;
		for (int index : selectedFilteredIndices) {
			int innerCount = 0;
			for (int innerIndex : selectedFilteredIndices) {
				filteredSelectedDistances[count][innerCount] = distances[index][innerIndex];
				innerCount++;
			}
			
			count++;
		}
		
		return filteredSelectedDistances;
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
	
	private void applyFilter()
	{
		/*
		 * 1. Find indices matching the defined boundaries.
		 */
		
		filterIndices();
		
		/*
		 * 2. Update data collections based on set of indices within boundaries.
		 */
		
		filterData();
	};
	
	private void filterIndices()
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
	}
	
	private void filterData()
	{
		// Use AnalysisController.filteredIndices to filter out data in desired parameter boundaries.
		filteredCoordinates			= new double[coordinates.length][filteredIndices.size()];
		filteredDistances			= new double[filteredIndices.size()][filteredIndices.size()];
		filteredLDAConfigurations	= new ArrayList<LDAConfiguration>(filteredIndices.size());
		
		// Copy data corresponding to chosen LDA configurations in new arrays.
		int count = 0;
		for (int filteredIndex : filteredIndices) {
			// Copy MDS coordinates.
			for (int column = 0; column < coordinates.length; column++) {
				filteredCoordinates[column][count] = coordinates[column][filteredIndex];
			}
			
			// Copy distances.
			int innerCount = 0;
			for (int filteredInnerIndex : filteredIndices) {
				filteredDistances[count][innerCount] = distances[filteredIndex][filteredInnerIndex];
				innerCount++;
			}
			
			// Copy LDA configurations.
			filteredLDAConfigurations.add(ldaConfigurations.get(filteredIndex));
			
			count++;
		}
		
		// Determine set of filtered and selected indices.
		selectedFilteredIndices				= createSelectedFilteredIndices(filteredIndices, mdsScatterchart.getSelectedIndices());
		
		// Update set of filtered and selected values.
		selectedFilteredDistances			= createFilteredSelectedDistanceMatrix(selectedFilteredIndices);
		selectedFilteredLDAConfigurations	= createFilteredSelectedLDAConfigurations(selectedFilteredIndices);
	}
	
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
			// Depending on whether relative view is enabled: Use entire loaded dataset or only filtered/selected componenents.
			Map<String, Pair<Double, Double>> thresholds	= determineThresholds(ddcNumberOfSteps, stepSizes, button_relativeView_paramDC.isSelected() ? ldaConfigurations : this.ldaConfigurations, coupledParameters);
			double prevValue								= 0;
			
			
			/*
			 * Calculate distances for each step.
			 */
			
			for (int step = 0; step <= ddcNumberOfSteps; step++) {
				// Calculate thresholds at current step for each coupled parameter.			
				Map<String, Pair<Double, Double>> thresholdsForCurrentStep	= calculateThresholdsForStep(step, coupledParameters, stepSizes, thresholds);
				
				// Assign each LDAConfiguration to one of two sets for each step / set of parameter values.
				Pair<Map<String, Set<Integer>>, Map<String, Set<Integer>>> ldaConfigurationSets = segregateLDAConfigurations(ldaConfigurations, coupledParameters, thresholdsForCurrentStep);
				Map<String, Set<Integer>> fixedLDAConfigIndiceSets								= ldaConfigurationSets.getKey();

				// Group distances; separated to distances between datasets where fixed parameters fall within the boundaries
				// of the current step and those between all other datasets.
				Map<String, Pair<Double, Double>> segregatedDistanceValues	= averageSegregatedDistances(distances, fixedLDAConfigIndiceSets);
				
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
					double currValue							= segregatedDistanceValues.get(param).getKey() - segregatedDistanceValues.get(param).getValue();
					// If there were either no fixed (within boundaries) or/nor free (not within boundaries) for current step and parameter:
					// Don't plot point, since it wouldn't contain any sensible information.  
					double currCheckedValue						= (segregatedDistanceValues.get(param).getKey() == -Double.MAX_VALUE || segregatedDistanceValues.get(param).getValue() == -Double.MAX_VALUE) ? 0 : currValue;
					// If fixedDatasetsDistances == -Double.MAX_VALUE: No datasets in boundaries for this parameter and step. Use 0 as actual value, mark as non-existent.
					XYChart.Data<Float, Double> diffDataPoint	= new XYChart.Data<Float, Double>((float)step / ddcNumberOfSteps, currCheckedValue);

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
			
			// Set y-axis range, if in absolute mode.
			if (!button_relativeView_paramDC.isSelected()) {
				// If in absolute mode for the first time (important: The first draw always happens in absolute
				// view mode!): Get maxima and minima.
				if (!dcValueMaximumDetermined) {
					double max = dcValueExtrema.getValue();
					double min = dcValueExtrema.getKey();
					
					// Seek for extrema.
					for (String param : coupledParameters) {
						for (XYChart.Data<Float, Double> dataPoint : diffDataPointSeries.get(param).getData()) {
							max = dataPoint.getYValue() > max ? dataPoint.getYValue() : max;
							min = dataPoint.getYValue() < min ? dataPoint.getYValue() : min;
						}
					}
					
					// Remember minimum and maximum.
					dcValueExtrema				= new Pair<Double, Double>(min, max);
					// Set flag.
					dcValueMaximumDetermined	= true;
				}
				
				// Set y-axis range.
				numberAxis_distanceCorrelation_yAxis.setLowerBound(dcValueExtrema.getKey() * 1.2);
				numberAxis_distanceCorrelation_yAxis.setUpperBound(dcValueExtrema.getValue() * 1.2);
				// Configure tick options.
		    	final int numberOfTicks = 5;
		    	double diff				= dcValueExtrema.getValue() * 1.2 - dcValueExtrema.getKey() * 1.2;
		    	numberAxis_distanceCorrelation_yAxis.setTickUnit( diff / numberOfTicks);
		    	numberAxis_distanceCorrelation_yAxis.setMinorTickCount(4);
			}
			
		}
	}
	
	/**
	 * Segregates currently filtered/selected LDA configurations based on if they pass the boundaries
	 * for the current step. 
	 * @param ldaConfigurations
	 * @param coupledParameters
	 * @param thresholdsForCurrentStep
	 * @return A pair of two maps (each one containing parameter/set pairs); the first one for configurations
	 * fitting the boundaries (fixed indices), the second one those which do not (free indices). 
	 */
	private Pair<Map<String, Set<Integer>>, Map<String, Set<Integer>>> segregateLDAConfigurations(final ArrayList<LDAConfiguration> ldaConfigurations, final ArrayList<String> coupledParameters, final Map<String, Pair<Double, Double>> thresholdsForCurrentStep)
	{
		Map<String, Set<Integer>> fixedLDAConfigIndiceSets	= new HashMap<String, Set<Integer>>();
		Map<String, Set<Integer>> freeLDAConfigIndiceSets	= new HashMap<String, Set<Integer>>();
		
		// Init sets.
		for (String param : coupledParameters) {
			fixedLDAConfigIndiceSets.put(param, new HashSet<Integer>());
			freeLDAConfigIndiceSets.put(param, new HashSet<Integer>());
		}
		
		// Assign LDA configurations to sets.
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
		
		return new Pair<Map<String, Set<Integer>>, Map<String, Set<Integer>>>(fixedLDAConfigIndiceSets, freeLDAConfigIndiceSets);
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
			double normalizedOtherDistanceAverage = otherDistancesCounts.get(param) > 0 ? otherDistancesSums.get(param) / otherDistancesCounts.get(param) : -Double.MAX_VALUE;
			// If no fixed datasets for this parameter and step: Use -Double.MAX_VALUE to indicate that.
			double normalizedFixedDistanceAverage = fixedDistancesCounts.get(param) > 0 ? fixedDistancesSums.get(param) / fixedDistancesCounts.get(param) : -Double.MAX_VALUE;
			
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
					heatmap_parameterspace.refresh(false);
				}
				
				// Adapt height.
				if (height > 0) {
					canvas_heatmap.setHeight(height - 45 - 45);
					heatmap_parameterspace.refresh(false);
				}
			break;
			
			// Resize local scope element.
			case "anchorpane_localScope":	
				localScopeInstance.resize(width, height);
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
		Node source = (Node) e.getSource();
		
		switch (source.getId()) {
			case "button_relativeView_distEval":
				distancesBarchart.changeViewMode();
			break;

			case "button_relativeView_paramDist":
				updateHeatmap(e);
			break;
			
			case "button_relativeView_paramDC":
				// Toggle auto-ranging.
				linechart_distanceCorrelation.getYAxis().setAutoRanging(button_relativeView_paramDC.isSelected());
				linechart_distanceCorrelation.getXAxis().setAutoRanging(button_relativeView_paramDC.isSelected());
				
				// If in absolute mode: Manually set axis range options.
				if (!button_relativeView_paramDC.isSelected())
					resetDDCAxisOptions();
				
				// Refresh chart.
				refreshDistanceLinechart(filteredLDAConfigurations, filteredDistances);
			break;
		}
	}
	
	@FXML
	public void changeParameterDistributionGranularityMode(ActionEvent e)
	{
		slider_parameterSpace_distribution_granularity.setDisable(checkbox_parameterSpace_distribution_dynAdjustment.isSelected());
		heatmap_parameterspace.setGranularityInformation(checkbox_parameterSpace_distribution_dynAdjustment.isSelected(), (int) slider_parameterSpace_distribution_granularity.getValue(), true);
	}
	
	/**
	 * Default view mode is absolute - disable auto-ranging on axes, configure ticks.
	 */
	private void resetDDCAxisOptions()
	{
		// Disable auto-ranging.
		numberAxis_distanceCorrelation_yAxis.setAutoRanging(false);
		numberAxis_distanceCorrelation_xAxis.setAutoRanging(false);
	
		// Set upper and lower bound for x axis.
		numberAxis_distanceCorrelation_xAxis.setLowerBound(-0.1);
		numberAxis_distanceCorrelation_xAxis.setUpperBound(1.1);
		
    	// Adjust tick width.
    	final int numberOfTicks = 5;
    	numberAxis_distanceCorrelation_xAxis.setTickUnit( 1.2 / numberOfTicks);
    	numberAxis_distanceCorrelation_xAxis.setMinorTickCount(4);
	}
	

	private Map<String, Pair<Double, Double>> identifyLDAParameterExtrema(ArrayList<LDAConfiguration> ldaConfigurations)
	{
		Map<String, Pair<Double, Double>> parameterExtrema = new HashMap<String, Pair<Double, Double>>(LDAConfiguration.SUPPORTED_PARAMETERS.length);
		
		// Init parameter extrema collection.
		for (String param : LDAConfiguration.SUPPORTED_PARAMETERS) {
			parameterExtrema.put(param, new Pair<Double, Double>(Double.MAX_VALUE, Double.MIN_VALUE));
		}
		
		// Search for extrema in all LDA configurations.
		for (LDAConfiguration ldaConfig : ldaConfigurations) {
			// For all supported parameters:
			for (String param : LDAConfiguration.SUPPORTED_PARAMETERS) {
				double value	= ldaConfig.getParameter(param);
				double min		= value < parameterExtrema.get(param).getKey()		? value : parameterExtrema.get(param).getKey();
				double max 		= value > parameterExtrema.get(param).getValue() 	? value : parameterExtrema.get(param).getValue();
				
				parameterExtrema.put(param, new Pair<Double, Double>(min, max));
			}
		}
		
		return parameterExtrema;
	}
	
	/**
	 * Adjusts minimal and maximal control values so that they fit the loaded data set.
	 */
	private void adjustControlExtrema()
	{
		Map<String, Pair<Double, Double>> ldaParameterExtrema = identifyLDAParameterExtrema(ldaConfigurations);
		
		// Update values of range slider. 
		for (Map.Entry<String, RangeSlider> rs : rangeSliders.entrySet()) {
			String param	= rs.getKey();
			double min		= ldaParameterExtrema.get(param).getKey();
			double max		= ldaParameterExtrema.get(param).getValue();
			
			// Set range slider values.
			rs.getValue().setMin(min);
			rs.getValue().setMax(max);
			
			// Set range slider's textfield values.
			textfieldsForFilterControls.get(param).getKey().setText(String.valueOf(min));
			textfieldsForFilterControls.get(param).getValue().setText(String.valueOf(max));
			
			// Adapt barchart axis.
			ValueAxis<Integer> yAxis = (ValueAxis<Integer>) barchartsForFilterControls.get(param).getYAxis();
			yAxis.setMinorTickVisible(false);
			
			
		}
	}
	
	public ArrayList<LDAConfiguration> getLDAConfigurations()
	{
		return ldaConfigurations;
	}

	public Scene getScene()
	{
		return scene;
	}

	public void setScene(Scene scene)
	{
		this.scene = scene;
	}

	@Override
	public void setWorkspace(Workspace workspace)
	{
		super.setWorkspace(workspace);
		
		// Pass reference on to instance of local scope component.
		localScopeInstance.setWorkspace(workspace);
	}
}