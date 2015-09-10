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
import view.components.DistanceDifferenceCorrelationLinechart;
import view.components.DistancesBarchart;
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
import javafx.scene.chart.StackedBarChart;
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
	
	/**
	 * MDSScatterchart component.
	 */
	private MDSScatterchart mdsScatterchart;
	/**
	 * Actual scatterchart.
	 */
	private @FXML ScatterChart<Number, Number> scatterchart_global;
	
	/*
	 * Distances barchart. 
	 */
	
	/**
	 * DistancesBarchart componenet.
	 */
	private DistancesBarchart distancesBarchart;
	
	private @FXML StackedBarChart<String, Integer> barchart_distances;
	private @FXML NumberAxis numberaxis_distanceEvaluation_yaxis;
	private @FXML Label label_avg;
	private @FXML Label label_median;
	private @FXML CheckBox checkbox_logarithmicDistanceBarchart;
	
	/*
	 * Toggle buttons indicating view mode.
	 */
	
	private @FXML ToggleButton button_relativeView_distEval;
	private @FXML ToggleButton button_relativeView_paramDist;
	private @FXML ToggleButton button_relativeView_paramDC;
	
	/*
	 * Parameter Space - Distribution.
	 */
	
	/**
	 * Heatmap component.
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
	
	/**
	 * DDCLinechart component.
	 */
	private DistanceDifferenceCorrelationLinechart parameterspace_ddc_linechart;
	
	private @FXML LineChart<Float, Double> linechart_distanceCorrelation;
	private @FXML NumberAxis numberAxis_distanceCorrelation_xAxis;
	private @FXML NumberAxis numberAxis_distanceCorrelation_yAxis;
	private @FXML VBox vbox_ddcHoverInformation;
	
	private @FXML CheckBox checkbox_ddc_alpha;
	private @FXML CheckBox checkbox_ddc_kappa;
	private @FXML CheckBox checkbox_ddc_eta;
	
	/*
	 * Local scope.
	 */
	
	/**
	 * Local scope instance component.
	 * At the moment: Only one local scope instance supported (parallel tag clouds).
	 */
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
	
	private Map<String, StackedBarChart<String, Integer>> barchartsForFilterControls;
	private @FXML StackedBarChart<String, Integer> barchart_alpha;
	private @FXML StackedBarChart<String, Integer> barchart_eta;
	private @FXML StackedBarChart<String, Integer> barchart_kappa;
	
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
	
	// Discarded (not fitting the defined filter boundaries) data.
	
	/**
	 * Set of all datasets matching the currently defined thresholds and selection.
	 */
	private Set<Integer> discardedIndices;
	/**
	 * Stores discarded coordinates.
	 */
	private double discardedCoordinates[][];
	/**
	 * Stores filtered and selecte distances.
	 */
	private double discardedDistances[][];
	/**
	 * Stores filtered and selected LDA configurations.
	 */
	private ArrayList<LDAConfiguration> discardedLDAConfigurations;
	
	/*
	 * Information on global extrema.
	 */
	
	/**
	 * Flag indicating whether or not the global extrema were already identified.
	 */
	private boolean globalExtremaIdentified;
	
	
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
		discardedIndices						= new LinkedHashSet<Integer>();
		
		// Auxiliary variable storing whether or not the global extrema have already been identified.
		globalExtremaIdentified				= false;
		
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
		parameterspace_ddc_linechart = new DistanceDifferenceCorrelationLinechart(
											this, linechart_distanceCorrelation, 
											numberAxis_distanceCorrelation_xAxis, numberAxis_distanceCorrelation_yAxis,
											vbox_ddcHoverInformation, button_relativeView_paramDC,
											checkbox_ddc_alpha, checkbox_ddc_eta, checkbox_ddc_kappa);
	}

	private void initFilterControls()
	{
		scrollpane_filter.setContent(gridpane_parameterConfiguration);
		
		// Init collections.
		rangeSliders				= new HashMap<String, RangeSlider>();
		textfieldsForFilterControls	= new HashMap<String, Pair<TextField, TextField>>();
		barchartsForFilterControls	= new HashMap<String, StackedBarChart<String, Integer>>();
		
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
			
			rs.setMaxWidth(230);
			rs.setMax(25);
			rs.setMax(100);
			rs.setMajorTickUnit(5);
			rs.setMinorTickCount(4);
			rs.setSnapToTicks(false);
			rs.setShowTickLabels(true);
			rs.setShowTickMarks(true);
			rs.setLowValue(0);
			rs.setHighValue(25);
			rs.setHighValue(100);
			
			// Set width. 
			rs.setMaxWidth(220);
			rs.setMinWidth(220);
			rs.setPrefWidth(220);
			
			// Get some distance between range sliders and bar charts.
			rs.setTranslateX(-6);
			rs.setPadding(new Insets(5, 0, 0, 0));
			
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
		
		// Set gridpane's height (based on the number of supported parameters).
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
													label_avg, label_median, button_relativeView_distEval, checkbox_logarithmicDistanceBarchart);
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
		
		// Call initialization procedure.
		init();
		
		// Draw entire data set. Used as initialization call, executed by Workspace instance.
		if (filterData) {
			// Filter data.
			applyFilter();
		}
		
		// Refresh visualizations.
		refreshParameterHistograms(50);
		mdsScatterchart.refresh(filteredCoordinates, filteredIndices, discardedCoordinates, discardedIndices);
		distancesBarchart.refresh(distances, filteredDistances, selectedFilteredDistances, true);
		parameterspace_ddc_linechart.refresh(filteredLDAConfigurations, filteredDistances, true);
		heatmap_parameterspace.refresh(ldaConfigurations, filteredLDAConfigurations, combobox_parameterSpace_distribution_xAxis.getValue(), combobox_parameterSpace_distribution_yAxis.getValue(), button_relativeView_paramDist.isSelected());
		localScopeInstance.refresh(selectedFilteredLDAConfigurations);
	}

	/**
	 * Inits some auxiliary variables. Needed before first visualization.
	 */
	private void init()
	{
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
	}
	
	/**
	 * Refreshes visualization after data in global MDS scatterchart was selected. 
	 * @param selectedIndices
	 */
	public void refreshVisualizationsAfterGlobalSelection(Set<Integer> selectedIndices, boolean includeLocalScope)
	{
		// Update set of filtered and selected indices.
		selectedFilteredIndices				= createSelectedFilteredIndexSet(filteredIndices, selectedIndices);
		
		// Find selected and filtered values.
		selectedFilteredDistances			= createSelectedFilteredDistanceMatrix(selectedFilteredIndices);
		selectedFilteredLDAConfigurations	= createSelectedFilteredLDAConfigurations(selectedFilteredIndices);
				
		// Refresh other (than MDSScatterchart) visualizations.
		distancesBarchart.refresh(distances, filteredDistances, selectedFilteredDistances, true);
		// Refresh local scope visualization, if specified.
		if (includeLocalScope)
			localScopeInstance.refresh(selectedFilteredLDAConfigurations);
	}
	
	public void refreshLocalScopeAfterGlobalSelection()
	{
		localScopeInstance.refresh(selectedFilteredLDAConfigurations);
	}
	
	/**
	 * Creates set of selected and filtered indices.
	 * @param filteredIndices
	 * @param selectedIndices
	 * @return
	 */
	private Set<Integer> createSelectedFilteredIndexSet(Set<Integer> filteredIndices, Set<Integer> selectedIndices)
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
	 * Creates set of discarded indices.
	 * Picks every index from 0 to the number of LDA configurations that's not in 
	 * the set of filtered indices.
	 * @param filteredIndices
	 * @return
	 */
	private Set<Integer> createDiscardedIndexSet(Set<Integer> filteredIndices)
	{
		Set<Integer> discardedIndices = new HashSet<Integer>();
		
		for (int i = 0; i < ldaConfigurations.size(); i++) {
			if (!filteredIndices.contains(i))
				discardedIndices.add(i);
		}
		
		return discardedIndices;
	}

	/**
	 * Creates matrix of coordinates of discarded (non-filtered) datapoints.
	 * @param discardedIndices
	 * @return
	 */
	private double[][] createDiscardedCoordinateMatrix(Set<Integer> discardedIndices)
	{
		double[][] discardedCoordinates = new double[this.coordinates.length][discardedIndices.size()];
		
		int count = 0;
		for (int discardedIndex : discardedIndices) {
			// Copy MDS coordinates.
			for (int column = 0; column < this.coordinates.length; column++) {
				discardedCoordinates[column][count] = coordinates[column][discardedIndex];
			}
			
			count++;
		}
		
		return discardedCoordinates;
	}
	
	/**
	 * Creates list of LDA configurations out of sets of filtered and selected indices. 
	 * @param selectedFilteredIndices
	 * @return
	 */
	private ArrayList<LDAConfiguration> createSelectedFilteredLDAConfigurations(Set<Integer> selectedFilteredIndices)
	{
		ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations = new ArrayList<LDAConfiguration>(selectedFilteredIndices.size());
		
		for (int index : selectedFilteredIndices) {
			selectedFilteredLDAConfigurations.add(ldaConfigurations.get(index));
		}
		
		return selectedFilteredLDAConfigurations;
	}
	
	/**
	 * Creates list of LDA configurations out of sets of discarded indices. 
	 * @param discardedIndices
	 * @return
	 */
	private ArrayList<LDAConfiguration> createDiscardedLDAConfigurations(Set<Integer> discardedIndices)
	{
		ArrayList<LDAConfiguration> discardedLDAConfigurations = new ArrayList<LDAConfiguration>(discardedIndices.size());
		
		for (int index : discardedIndices) {
			discardedLDAConfigurations.add(ldaConfigurations.get(index));
		}
		
		return discardedLDAConfigurations;
	}	
	
	/**
	 * Creates distance matrix out of sets of filtered and selected indices. 
	 * @param filteredIndices
	 * @param selectedIndices
	 * @return
	 */
	private double[][] createSelectedFilteredDistanceMatrix(Set<Integer> selectedFilteredIndices)
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
	
	/**
	 * Creates distance matrix out of sets of filtered and selected indices. 
	 * @param filteredIndices
	 * @param selectedIndices
	 * @return
	 */
	private double[][] createDiscardedDistanceMatrix(Set<Integer> discardedIndices)
	{
		// Copy actual distance data in array.
		double[][] discardedDistances = new double[discardedIndices.size()][discardedIndices.size()];
		
		int count = 0;
		for (int index : selectedFilteredIndices) {
			int innerCount = 0;
			for (int innerIndex : discardedIndices) {
				discardedDistances[count][innerCount] = distances[index][innerIndex];
				innerCount++;
			}
			
			count++;
		}
		
		return discardedDistances;
	}	
	
	/**
	 * Adds event handler processing slide and other events to a specified range slider.
	 * @param rs
	 * @param parameter
	 */
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
			
			// Check if this particular LDA configuration is in bounds of all specified parameter thresholds.
			for (Map.Entry<String, Pair<Double, Double>> entry : parameterBoundaries.entrySet()) {
				double value	= ldaConfig.getParameter(entry.getKey());
				double min		= entry.getValue().getKey();
				double max		= entry.getValue().getValue();
				
				// Exclude LDA configuration if limits are exceeded.
				if (value < min || value > max) {
					fitsBoundaries = false;
					
					// Stop loop.
					break;
				}
			}
			
			// If in boundaries and not contained in selection:
			if (fitsBoundaries) {
				// Remove from set of discarded indices, if in there.
				if (discardedIndices.contains(i))
					discardedIndices.remove(i);
				
				// Add to set of filtered indices, if not already in there.
				if (!filteredIndices.contains(i))
					filteredIndices.add(i);
			}
			
			// Else if not in boundaries and contained in selection:
			else if (!fitsBoundaries) {
				// Add to set of discarded indices, if not already in there.
				if (!discardedIndices.contains(i))
					discardedIndices.add(i);
				
				// Remove from set of filtered indices, if in there.
				if (filteredIndices.contains(i))
					filteredIndices.remove(i);
			}
		}
	}
	
	private void filterData()
	{
		/*
		 * Update data collections for filtered datasets. 
		 */
		
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
		
		/*
		 * Update data collections for discarded (not filtered) datasets. 
		 */
		
		// Determine set of discarded indices.
		discardedIndices					= createDiscardedIndexSet(filteredIndices);
		// Update set of discarded values.
		discardedCoordinates				= createDiscardedCoordinateMatrix(discardedIndices);
		discardedDistances					= createDiscardedDistanceMatrix(selectedFilteredIndices);
		discardedLDAConfigurations			= createDiscardedLDAConfigurations(selectedFilteredIndices);
		
		/*
		 * Update data collections for filtered and selected datapoints. 
		 */
		
		// Determine set of filtered and selected indices.
		selectedFilteredIndices				= createSelectedFilteredIndexSet(filteredIndices, mdsScatterchart.getSelectedIndices());
		// Update set of filtered and selected values.
		selectedFilteredDistances			= createSelectedFilteredDistanceMatrix(selectedFilteredIndices);
		selectedFilteredLDAConfigurations	= createSelectedFilteredLDAConfigurations(selectedFilteredIndices);
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
		
		// Determine minima and maxima for all parameters.
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
	 * Refresh parameter filter control histograms.
	 * @param numberOfBins
	 */
	private void refreshParameterHistograms(final int numberOfBins)
	{
		// Map storing one bin list for each parameter, counting only filtered datasets.
		Map<String, int[]> parameterBinLists_filtered	= new HashMap<String, int[]>();
		// Map storing one bin list for each parameter, counting only discarded datasets.
		Map<String, int[]> parameterBinLists_discarded	= new HashMap<String, int[]>();
		
		// Add parameters to map of bin lists. 
		for (String supportedParameter : LDAConfiguration.SUPPORTED_PARAMETERS) {
			parameterBinLists_filtered.put(supportedParameter, new int[numberOfBins]);
			parameterBinLists_discarded.put(supportedParameter, new int[numberOfBins]);
		}
		
		// Bin data.
		for (int i = 0; i < ldaConfigurations.size(); i++) {
			// Check if dataset is filtered (as opposed to discarded).
			boolean isFilteredDataset = filteredIndices.contains(i);
			
			// Evaluate bin counts for this dataset for each parameter.
			for (String param : rangeSliders.keySet()) {
				double binInterval	= (rangeSliders.get(param).getMax() - rangeSliders.get(param).getMin()) / numberOfBins;
				// Calculate index of bin in which to store the current value.
				int index_key		= (int) ( (ldaConfigurations.get(i).getParameter(param) - rangeSliders.get(param).getMin()) / binInterval);
				// Check if element is highest allowed entry.
				index_key			= index_key < numberOfBins ? index_key : numberOfBins - 1;
				
				// Check if this dataset fits all boundaries / is filtered - then increment content of corresponding bin.
				if (isFilteredDataset)
					parameterBinLists_filtered.get(param)[index_key]++;
				else
					parameterBinLists_discarded.get(param)[index_key]++;
			}
		}

		/*
		 * Transfer data to scented widgets.
		 */
		
		for (Map.Entry<String, StackedBarChart<String, Integer>> parameterBarchartEntry : barchartsForFilterControls.entrySet()) {
			// Clear old data.
			parameterBarchartEntry.getValue().getData().clear();

			// Add filtered data series to barcharts.
			parameterBarchartEntry.getValue().getData().add(
					generateParameterHistogramDataSeries(parameterBarchartEntry.getKey(), parameterBinLists_filtered, numberOfBins)
			);
			// Color filtered data.
			colorParameterHistogramBarchart(parameterBarchartEntry.getValue(), 0);
			
			// Add discarded data series to barcharts.
			parameterBarchartEntry.getValue().getData().add(
					generateParameterHistogramDataSeries(parameterBarchartEntry.getKey(), parameterBinLists_discarded, numberOfBins)
			);
			// Color discarded data.
			colorParameterHistogramBarchart(parameterBarchartEntry.getValue(), 1);
		}
	}
	
	/**
	 * Colours a parameter histogram barchart in the desired colors (one for filtered, one for discarded).
	 * @param barchart
	 * @param seriesIndex Index of series to be colored. 0 for discarded, 1 for filtered data points.
	 */
	private void colorParameterHistogramBarchart(StackedBarChart<String, Integer> barchart, int seriesIndex)
	{
		// Color bars (todo: color according to defined options).
		for (Node node : barchart.lookupAll(".chart-bar")) {
			switch (seriesIndex)
			{
				// Discarded data.
				case 1:
					if (node.getUserData() == null || node.getUserData().toString() == "discarded") {
						node.setUserData("discarded");
						node.setStyle("-fx-bar-fill: grey;");
					}
				break;
				
				// Filtered data.
				case 0:
					if (node.getUserData() == null || node.getUserData().toString() == "filtered") {
						node.setUserData("filtered");
						node.setStyle("-fx-bar-fill: blue;");
					}
				break;
			}
		}
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
			final int binContent = parameterBinLists.get(key)[i];
			data_series.getData().add( new XYChart.Data<String, Integer>(String.valueOf(i), binContent ));
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
		parameterspace_ddc_linechart.refresh(filteredLDAConfigurations, filteredDistances, true);
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
				parameterspace_ddc_linechart.changeViewMode();
			break;
		}
	}
	
	@FXML
	public void changeScalingType(ActionEvent e)
	{
		distancesBarchart.changeScalingType();
	}
	
	@FXML
	public void changeParameterDistributionGranularityMode(ActionEvent e)
	{
		slider_parameterSpace_distribution_granularity.setDisable(checkbox_parameterSpace_distribution_dynAdjustment.isSelected());
		heatmap_parameterspace.setGranularityInformation(checkbox_parameterSpace_distribution_dynAdjustment.isSelected(), (int) slider_parameterSpace_distribution_granularity.getValue(), true);
	}

	/**
	 * Identify maxima and minima in set of loaded LDA parameters.
	 * @param ldaConfigurations
	 * @return
	 */
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