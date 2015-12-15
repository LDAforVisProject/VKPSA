package control.analysisView;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import model.AnalysisDataspace;
import model.LDAConfiguration;
import model.workspace.Workspace;
import control.Controller;
import control.analysisView.localScope.LocalScopeVisualizationType;
import view.components.VisualizationComponent;
import view.components.VisualizationComponentType;
import view.components.heatmap.CategoricalHeatmap;
import view.components.heatmap.HeatmapOptionset;
import view.components.legacy.DistancesBarchart;
import view.components.legacy.LocalScopeInstance;
import view.components.legacy.mdsScatterchart.MDSScatterchart;
import view.components.scatterchart.ScatterchartDataset;
import view.components.scatterchart.ScatterchartOptionset;
import view.components.scatterchart.ParameterSpaceScatterchart;
import view.components.scentedFilter.ScentedFilter;
import view.components.scentedFilter.ScentedFilterDataset;
import view.components.scentedFilter.ScentedFilterOptionset;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;

public class AnalysisController extends Controller
{
	// -----------------------------------------------
	// 				UI elements
	// -----------------------------------------------
	
	private Scene scene;
	
	/*
	 * Accordion and other elements for options section.
	 */
	
	private @FXML Accordion accordion_options;
	// Panes in accordion.
	private @FXML TitledPane filter_titledPane;
	private @FXML TitledPane mdsDistEval_titledPane;
	private @FXML TitledPane localScope_titledPane;
	private @FXML TitledPane paramSpace_titledPane;
	
	/*
	 * Anchorpanes hosting visualizations. 
	 */
	
	/**
	 * For settings.
	 */
	private @FXML AnchorPane settings_anchorPane;
	/**
	 * For filter settings.
	 */
	private @FXML AnchorPane settings_filter_anchorPane;
	
	/**
	 * For MDS scatterchart.
	 */
	private @FXML AnchorPane mds_anchorPane;
	/**
	 * For distance evaluation barchart.
	 */
	private @FXML AnchorPane distEval_anchorPane;
	
	/**
	 * For both parameter space distribution heatmaps.
	 */
	private @FXML AnchorPane paramSpace_distribution_anchorPane;
	/**
	 * For the parameter space distribution heatmap (using filtered data).
	 */
	private @FXML AnchorPane paramSpace_distribution_anchorPane_filtered;
	/**
	 * For the parameter space distribution heatmap (using selected data).
	 */
	private @FXML AnchorPane paramSpace_distribution_anchorPane_selected;
	
	/**
	 * For local scope visualization(s): Parallel Tag Cloud.
	 */
	private @FXML AnchorPane localScope_ptc_anchorPane;
	/**
	 * For local scope visualization(s): Comparison of topic models.
	 */
	private @FXML AnchorPane localscope_tmc_anchorPane;
	
	/*
	 * MDS Scatterchart.
	 */
	
	/**
	 * MDSScatterchart component.
	 */
	private MDSScatterchart globalScatterplot;
	/**
	 * Actual scatterchart.
	 */
	private @FXML ScatterChart<Number, Number> mds_scatterchart;
	/**
	 * Canvas showing the heatmap for the MDS scatter chart.
	 */
	private @FXML Canvas mdsHeatmap_canvas;
	
	// Controls for MDS heatmap.
	/**
	 * Checkbox for heatmap.
	 */
	private @FXML CheckBox checkbox_mdsHeatmap_distribution_dynAdjustment;
	/**
	 * Slider specifying MDS heatmap granularity.
	 */
	private @FXML Slider slider_mds_distribution_granularity;
	/**
	 * Specifies whether or not the MDS heatmap is to be shown.
	 */
	private @FXML CheckBox showMSDHeatmap_checkbox;
	/**
	 * Color chooser for lower end of heatmap values.
	 */
	private @FXML ColorPicker mdsHeatmap_dhmColor_min_colChooser;
	/**
	 * Color chooser for upper end of heatmap values.
	 */
	private @FXML ColorPicker mdsHeatmap_dhmColor_max_colChooser;
	
	/*
	 * Distances barchart. 
	 */
	
	/**
	 * DistancesBarchart componenet.
	 */
	private DistancesBarchart distancesBarchart;
	
	private @FXML BarChart<String, Number> barchart_distances;
	private @FXML NumberAxis numberaxis_distanceEvaluation_yaxis;
	private @FXML CheckBox checkbox_logarithmicDistanceBarchart;
	
	/*
	 * Toggle buttons indicating view mode.
	 */
	
	private @FXML ToggleButton button_relativeView_distEval;
	private @FXML ToggleButton button_relativeView_paramDC;
	
	/*
	 * Parameter Space - Distribution.
	 */
	
	/**
	 * ParameterSpaceScatterchart for display of parameter values.
	 */
	private ParameterSpaceScatterchart paramSpaceScatterchart;
	
	// Heatmap setting controls (and metadata).
	private @FXML ToggleButton button_relativeView_paramDist;
	
	//  Controls for parameter space density heatmap: ...
	// ...for granularity.
	private @FXML CheckBox paramSpace_dhmGranularity_checkbox;
	private @FXML CheckBox paramSpace_dhmVisibility_checkbox;
	private @FXML Slider paramSpace_dhmGranularity_slider;
	// ...for category selection.
	private @FXML CheckBox paramSpace_dhmCategories_active_checkbox;
	private @FXML CheckBox paramSpace_dhmCategories_inactive_checkbox;
	private @FXML CheckBox paramSpace_dhmCategories_discarded_checkbox;
	// ...for color spectrum.
	/**
	 * Color chooser for lower end of heatmap values.
	 */
	private @FXML ColorPicker paramSpace_dhmColor_min_colChooser;
	/**
	 * Color chooser for upper end of heatmap values.
	 */
	private @FXML ColorPicker paramSpace_dhmColor_max_colChooser;
	
	/*
	 * Local scope.
	 */
	
	/**
	 * Heatmap for comparison of topic models.
	 */	
	CategoricalHeatmap tmcHeatmap;
	
	// TMC options:
	/**
	 * Color chooser for lower end of heatmap values.
	 */
	private @FXML ColorPicker tmc_color_min_colChooser;
	/**
	 * Color chooser for lower end of heatmap values.
	 */
	private @FXML ColorPicker tmc_color_max_colChooser;
	
	/**
	 * Parallel tag cloud in local scope.
	 */
	private LocalScopeInstance localScopeInstance;
	
	// Local scope options.
	 
	private @FXML Slider slider_localScope_numTopicsToUse;
	private @FXML TextField textfield_localScope_numTopicsToUse;
	private @FXML Slider slider_localScope_numKeywordsToUse;
	private @FXML TextField textfield_localScope_numKeywordsToUse;
	
	/*
	 * Filter controls.
	 */
	
	/**
	 * Container for scented filters.
	 */
	private VBox filters_vbox;
	/**
	 * Scented filters for parameters.
	 */
	private ArrayList<ScentedFilter> filters;
	
	/*
	 * Setting shortcuts icons.
	 */
	
	private @FXML ImageView settings_mds_icon;
	private @FXML ImageView settings_distEval_icon;
	private @FXML ImageView settings_paramDist_icon;
	private @FXML ImageView settings_localScope_icon;
	
	
	// -----------------------------------------------
	// 				Data.
	// -----------------------------------------------
	
	/**
	 * Dataspace for all relevant data handled in AnalysisController.
	 */
	private AnalysisDataspace dataspace;

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
		
		// Create dataspace.
		dataspace = new AnalysisDataspace(this);
		
		// Auxiliary variable storing whether or not the global extrema have already been identified.
		globalExtremaIdentified	= false;
		
		// Init GUI elements.
		initUIElements();
		addResizeListeners();
		
		// Expand filter pane.
		accordion_options.setExpandedPane(filter_titledPane);
		//filter_titledPane.lookup(".title").setStyle("-fx-font-weight:bold");
	}
	
	private void addResizeListeners()
	{
		// Define list of anchor panes / other elements to add resize listeners to.
		ArrayList<Pane> anchorPanesToResize = new ArrayList<Pane>();
		
		anchorPanesToResize.add(paramSpace_distribution_anchorPane_filtered);
		anchorPanesToResize.add(paramSpace_distribution_anchorPane_selected);
		anchorPanesToResize.add(mds_anchorPane);
		anchorPanesToResize.add(localscope_tmc_anchorPane);
		anchorPanesToResize.add(localScope_ptc_anchorPane);
		anchorPanesToResize.add(settings_anchorPane);
		anchorPanesToResize.add(settings_filter_anchorPane);
		
		// Add width and height resize listeners to all panes.
		for (Pane ap : anchorPanesToResize) {
			// Add listener to width property.
			ap.widthProperty().addListener(new ChangeListener<Number>() {
			    @Override 
			    public void changed(ObservableValue<? extends Number> observableValue, Number oldWidth, Number newWidth)
			    {
			        resizeElement(ap, newWidth.doubleValue(), 0);
			    }
			});
			
			// Add listener to height property.
			ap.heightProperty().addListener(new ChangeListener<Number>() {
			    @Override 
			    public void changed(ObservableValue<? extends Number> observableValue, Number oldHeight, Number newHeight) 
			    {
			    	resizeElement(ap, 0, newHeight.doubleValue());
			    }
			});
		}
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
		initParameterSpaceScatterchart();
		initLocalScopeView();
		initComparisonHeatmaps();
		
		// Bring setting icons to front.
		settings_mds_icon.toFront();
		settings_distEval_icon.toFront();
		settings_paramDist_icon.toFront();
		settings_localScope_icon.toFront();
	}
	
	private void initComparisonHeatmaps()
	{
		tmcHeatmap = (CategoricalHeatmap)VisualizationComponent.generateInstance(VisualizationComponentType.CATEGORICAL_HEATMAP, this, null, null, null);
		tmcHeatmap.embedIn(localscope_tmc_anchorPane);
	}

	private void initLocalScopeView()
	{
		// Create new instance of local scope.
		localScopeInstance = new LocalScopeInstance(this, 	localScope_ptc_anchorPane, localscope_tmc_anchorPane,
															slider_localScope_numTopicsToUse, textfield_localScope_numTopicsToUse,
															slider_localScope_numKeywordsToUse, textfield_localScope_numKeywordsToUse);
		localScopeInstance.load();
	}

	private void initFilterControls()
	{
		// Init collections.
		filters						= new ArrayList<ScentedFilter>();
		
		// Init container for filters.
		filters_vbox = new VBox();
		filters_vbox.resize(100, filters_vbox.getHeight());
		// Iterate over supported parameters.
		for (String param : LDAConfiguration.SUPPORTED_PARAMETERS) {
			// Create new filter.
			ScentedFilter filter = (ScentedFilter) VisualizationComponent.generateInstance(VisualizationComponentType.SCENTED_FILTER, this, this.workspace, this.log_protocol_progressindicator, this.log_protocol_textarea);

			// Init filter.
			if (param != "kappa")
				filter.applyOptions(new ScentedFilterOptionset(param, true, 0, 15, 50, true, true, false));
			else
				filter.applyOptions(new ScentedFilterOptionset(param, false, 2, 25, 50, true, true, false));
			
			// Add to collection of filters.
			filters.add(filter);
			// Embed in containing VBox.
			filter.embedIn(filters_vbox);
		}
		
		// Add containing VBox to pane.
		settings_filter_anchorPane.getChildren().add(filters_vbox);
		settings_filter_anchorPane.applyCss();
		settings_filter_anchorPane.layout();
		settings_filter_anchorPane.requestLayout();
		filters_vbox.resize(100, filters_vbox.getHeight());
	}

	private void initMDSScatterchart()
	{
		// Init color chooser.
		mdsHeatmap_dhmColor_min_colChooser.setValue(Color.RED);
		mdsHeatmap_dhmColor_max_colChooser.setValue(Color.DARKRED);
		
		// Init scatterchart.
		globalScatterplot = new MDSScatterchart(	this, mds_scatterchart, mdsHeatmap_canvas,
													checkbox_mdsHeatmap_distribution_dynAdjustment, slider_mds_distribution_granularity,
													mdsHeatmap_dhmColor_min_colChooser.getValue(), mdsHeatmap_dhmColor_max_colChooser.getValue());
	}
	
	private void initDistanceBarchart()
	{
		distancesBarchart = new DistancesBarchart(	this, barchart_distances, numberaxis_distanceEvaluation_yaxis, 
													button_relativeView_distEval, checkbox_logarithmicDistanceBarchart);
	}
	
	private void initParameterSpaceScatterchart()
	{
		// Init color chooser.
		paramSpace_dhmColor_min_colChooser.setValue(Color.RED);
		paramSpace_dhmColor_max_colChooser.setValue(Color.DARKRED);
		
		// Init parameter space scatterchart.
		paramSpaceScatterchart = (ParameterSpaceScatterchart) VisualizationComponent.generateInstance(VisualizationComponentType.PARAMSPACE_SCATTERCHART, this, null, null, null);
		paramSpaceScatterchart.applyOptions(new ScatterchartOptionset(	true, true, false,
																		paramSpace_dhmVisibility_checkbox.isSelected(), paramSpace_dhmGranularity_checkbox.isSelected(), (int)paramSpace_dhmGranularity_slider.getValue(),
																		calculatePSScatterchartCategoriesValue(),
																		paramSpace_dhmColor_min_colChooser.getValue(), paramSpace_dhmColor_max_colChooser.getValue()));
		paramSpaceScatterchart.embedIn(paramSpace_distribution_anchorPane_selected);
				
		/*
		 * Init option controls.
		 */
		
		// Add listener to determine position during after release.
		paramSpace_dhmGranularity_slider.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	refreshParameterSpaceScatterchart();
            }
        });
		
		// Add listener to determine position during mouse drag.
		paramSpace_dhmGranularity_slider.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	refreshParameterSpaceScatterchart();
            };
        });
		
		// Init color chooser.
		tmc_color_min_colChooser.setValue(Color.LIGHTBLUE);
		tmc_color_max_colChooser.setValue(Color.DARKBLUE);
	}
	
	/**
	 * Refreshes all visualizations (e.g. after a filter was manipulated).
	 * @param filterData
	 */
	public void refreshVisualizations(boolean filterData)
	{
		log("Refreshing visualizations.");
		
		// Set references to data collections.
		dataspace.setDataReferences(workspace.getLDAConfigurations(),  workspace.getMDSCoordinates(), workspace.getDistances());
		
		// Call initialization procedure.
		init();
		
		// Draw entire data set. Used as initialization call, executed by Workspace instance.
		if (filterData) {
			// Filter data.
			applyFilter();
		}
		
		
		/*
		 * Refresh visualizations.
		 */
		
		// 	Parameter filtering controls:
		refreshScentedFilters();
		
		// 	MDS scatterchart:
		globalScatterplot.refresh(	dataspace.getCoordinates(),
									dataspace.getAvailableCoordinates(), dataspace.getInactiveIndices(), 
									dataspace.getActiveCoordinates(), dataspace.getActiveIndices(), 
									dataspace.getDiscardedCoordinates(), dataspace.getDiscardedIndices());

		//	Distance evaluation barchart:
		distancesBarchart.refresh(	dataspace.getDiscardedIndices(), dataspace.getInactiveIndices(), dataspace.getActiveIndices(),
									dataspace.getDiscardedDistances(), dataspace.getAvaibleDistances(), dataspace.getActiveDistances(), 
									true);

		// 	Parameter space scatterchart:
		paramSpaceScatterchart.refresh(new ScatterchartDataset(	dataspace.getLDAConfigurations(), dataspace.getDiscardedLDAConfigurations(),
																dataspace.getInactiveLDAConfigurations(), dataspace.getActiveLDAConfigurations()));
		
		//	Local scope:
		localScopeInstance.refresh(dataspace.getActiveLDAConfigurations());
		// TM comparison heatmap:
		refreshTMCHeatmap(dataspace.getActiveLDAConfigurations());
	}

	/**
	 * Inits some auxiliary variables. Needed before first visualization.
	 */
	private void init()
	{
		// If not done already: Discover global extrema, adapt components.
		if (!globalExtremaIdentified) {
			// Identify global extrema.
			distancesBarchart.identifyGlobalExtrema(dataspace.getDistances());
			globalScatterplot.identifyGlobalExtrema(dataspace.getCoordinates());
			paramSpaceScatterchart.identifyGlobalExtrema(dataspace.getLDAConfigurations());
			
			// Add keyboard listener in order to enable selection.
			addKeyListener();
			
			// Mark global extrema as found.
			globalExtremaIdentified = true;

			for (ScentedFilter filter : filters) {
				// Adjust global extrema for controls.
				filter.adjustControlExtrema(dataspace.getLDAConfigurations());
				// Set initial width.
				filter.resizeContent(300, 0);
			}
		}
	}
	
	/**
	 * Refreshes visualization after data in global MDS scatterchart was selected. 
	 * @param selectedIndices
	 */
	public void integrateMDSSelection(Set<Integer> selectedIndices, boolean includeLocalScope)
	{
		boolean changeDetected = !( selectedIndices.containsAll(dataspace.getActiveIndices()) && dataspace.getActiveIndices().containsAll(selectedIndices) );

		// Update set of filtered and selected indices.
		if (changeDetected) {
			// Update dataspace.
			dataspace.updateAfterSelection(selectedIndices);
			
			// Refresh other (than MDSScatterchart) visualizations.
			
			//	Distances barchart:
			distancesBarchart.refresh(	dataspace.getDiscardedIndices(), dataspace.getInactiveIndices(), dataspace.getActiveIndices(),
										dataspace.getDiscardedDistances(), dataspace.getAvaibleDistances(), dataspace.getActiveDistances(), 
										true);
			
			//	Paramer space scatterchart:
			paramSpaceScatterchart.refresh(new ScatterchartDataset(	dataspace.getLDAConfigurations(), dataspace.getDiscardedLDAConfigurations(),
																	dataspace.getInactiveLDAConfigurations(), dataspace.getActiveLDAConfigurations()));
			
			// 	Local scope:
			if (includeLocalScope)
				localScopeInstance.refresh(dataspace.getActiveLDAConfigurations());
			
			// 	Parameter histograms:
			refreshScentedFilters();
		}
		
		// Even if no change on global scope detected: Update local scope, if requested. 
		else if (includeLocalScope) {
			localScopeInstance.refresh(dataspace.getActiveLDAConfigurations());
			// Refresh topic model comparison heatmap.
			refreshTMCHeatmap(dataspace.getActiveLDAConfigurations());
		}
	}
	
	/**
	 * Integrates data/heatmap cells selected in TMC heatmap into dataspace and displays the
	 * selection in local scope.
	 * @param selectedTopicConfigIDs
	 * @param isCtrlDown Determines whether currently transferred data should be added or subtracted from current data set.
	 */
	public void integrateTMCHeatmapSelection(Set<Pair<Integer, Integer>> selectedTopicConfigIDs, boolean isCtrlDown)
	{
		localScopeInstance.refreshPTC(selectedTopicConfigIDs);
	}
	
	/**
	 * Integrates heatmap selection into dataspace, then fires update for all relevant visualizations.
	 * @param newlySelectedLDAConfigIDs
	 * @param isAddition
	 */
	public void integrateSelection(Set<Integer> newlySelectedLDAConfigIDs, final boolean isAddition)
	{
		// Check if there is any change in the set of selected datasets.
		boolean changeDetected = dataspace.integrateSelection(newlySelectedLDAConfigIDs, isAddition);

		// If so: Refresh visualizations.
		if (changeDetected)
			refreshVisualizationsAfterLocalSelection(isAddition);
	}
	
	/**
	 * Integrates barchart selection into MDS selection, then fires update for all relevant visualizations.
	 * @param newlySelectedLocalIndices
	 * @param isAddition true for addition of selected data to global selection; false for their removal.
	 */
	public void integrateBarchartSelection(ArrayList<Integer> newlySelectedLocalIndices, final boolean isAddition)
	{
		// Check if there is any change in the set of selected datasets.
		boolean changeDetected				= false;
		
		// 1. 	Check which elements are to be added/removed from current selection by comparing 
		// 		with set of filtered and selected datasets.
		
		// Selection should be added:
		if (isAddition) {
			// Translate local indices to global indices.
			for (int i = 0; i < newlySelectedLocalIndices.size(); i++) {
				if (!dataspace.getActiveIndices().contains( newlySelectedLocalIndices.get(i)) ) {
					// Add to collection of selected indices.
					dataspace.getActiveIndices().add( newlySelectedLocalIndices.get(i) );
					
					// Change detected.
					changeDetected = true;
				}
			}
		}
		
		else {
			// Translate local indices to global indices.
			for (int i = 0; i < newlySelectedLocalIndices.size(); i++) {
				// Add to set of selected, translated indices. 
				if (dataspace.getActiveIndices().contains( newlySelectedLocalIndices.get(i)) ) {
					dataspace.getActiveIndices().remove( newlySelectedLocalIndices.get(i) );
					
					// Change detected.
					changeDetected = true;
				}
			}
		}
		
		// 2. 	Update related (i.e. dependent on the set of selected entities) datasets and visualization, if there were any changes made.
		if (changeDetected)
			refreshVisualizationsAfterLocalSelection(isAddition);
	}
	
	
	/**
	 * Updates data and refreshes relevant visualizations after a change in selection was enacted
	 * through a (non-MDS-scatterchart) visualization.
	 * @param isAddition
	 */
	private void refreshVisualizationsAfterLocalSelection(boolean isAddition)
	{
		// Update dataspace.
		dataspace.updateAfterSelection(null);

		// 4.	Refresh visualizations.
		// 	Distances barchart:
		distancesBarchart.refresh(		dataspace.getDiscardedIndices(), dataspace.getInactiveIndices(), dataspace.getActiveIndices(),
										dataspace.getDiscardedDistances(), dataspace.getAvaibleDistances(), dataspace.getActiveDistances(), 
										true);
		// 	MDS scatterchart:
		globalScatterplot.refresh(		dataspace.getCoordinates(),
										dataspace.getAvailableCoordinates(), dataspace.getInactiveIndices(), 
										dataspace.getActiveCoordinates(), dataspace.getActiveIndices(), 
										dataspace.getDiscardedCoordinates(), dataspace.getDiscardedIndices());
		// 	Parameter space heatmap:
		paramSpaceScatterchart.refresh(new ScatterchartDataset(	dataspace.getLDAConfigurations(), dataspace.getDiscardedLDAConfigurations(),
																dataspace.getInactiveLDAConfigurations(), dataspace.getActiveLDAConfigurations()));
		
		//	Local scope:
		localScopeInstance.refresh(dataspace.getActiveLDAConfigurations());
		refreshTMCHeatmap(dataspace.getActiveLDAConfigurations());
		
		// 	Parameter histograms:
		refreshScentedFilters();
	}
	
	/**
	 * Refresh heatmap for topic model comparison. Loads new data from DB. 
	 * @param selectedLDAConfigurations
	 */
	private void refreshTMCHeatmap(ArrayList<LDAConfiguration> selectedLDAConfigurations)
	{
		// If data sets were selected:
		if (selectedLDAConfigurations.size() > 0) {
			HeatmapOptionset tmcOptions = new HeatmapOptionset(	true, -1, 
																tmc_color_min_colChooser.getValue(), tmc_color_max_colChooser.getValue(), new Color(0.0, 0.0, 1.0, 0.5), new Color(1.0, 0.0, 0.0, 0.5),
																"", "",
																true, false, true);
			// Instruct heatmap to fetch topic distance data asynchronously.
			tmcHeatmap.fetchTopicDistanceData(selectedLDAConfigurations, tmcOptions);
		}
	}
	
	/**
	 * Refreshes heatmap for topic model comparison. Uses available data. 
	 */
	private void refreshTMCHeatmap()
	{
		// Update colors by refreshing the TMC heatmap.
		HeatmapOptionset tmcOptions = new HeatmapOptionset(	true, -1, 
															tmc_color_min_colChooser.getValue(), tmc_color_max_colChooser.getValue(), new Color(0.0, 0.0, 1.0, 0.5), new Color(1.0, 0.0, 0.0, 0.5),
															"", "",
															true, false, true);
		tmcHeatmap.applyOptions(tmcOptions);
		tmcHeatmap.refresh();
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
		
		dataspace.filterData();
	};
	
	private void filterIndices()
	{
		// Get parameter boundaries.
		Map<String, Pair<Double, Double>> parameterBoundaries = new HashMap<String, Pair<Double, Double>>();
		
//		for (String param : rangeSliders.keySet()) {
//			parameterBoundaries.put(param, new Pair<Double, Double>(rangeSliders.get(param).getLowValue(), rangeSliders.get(param).getHighValue()));
//		}
		for (ScentedFilter filter : filters) {
			filter.addThresholdsToMap(parameterBoundaries);
		}
		
		// Filter indices.
		dataspace.filterIndices(parameterBoundaries);
	}
	
	@Override
	public void resizeContent(double width, double height)
	{
	}
	
	@Override
	protected void resizeElement(Node node, double width, double height)
	{
		switch (node.getId()) {
			case "paramSpace_distribution_anchorPane_filtered":
			break;
			
			case "paramSpace_distribution_anchorPane_selected":
				// Adapt size.
				paramSpaceScatterchart.resizeContent(width, height);
			break;
			
			// Resize scatter plot.
			case "mds_anchorPane":
	        	// Update MDS heatmap position/indentation.
				globalScatterplot.updateHeatmapPosition();
				// Redraw heatmap.
				globalScatterplot.refreshHeatmapAfterResize();
			break;
			
			// Resize local scope element: Chord diagram.
			case "localscope_tmc_anchorPane":
				tmcHeatmap.resizeContent(width, height);
			break;
			
			// Resize local scope element: Parallel tag clouds.
			case "localScope_ptc_anchorPane":
				localScopeInstance.resize(width, height, LocalScopeVisualizationType.PARALLEL_TAG_CLOUDS);
			break;
			
			case "settings_anchorPane":
				filters_vbox.setPrefWidth(width - 20);
				for (ScentedFilter filter : filters) {
					filter.resizeContent(width - 20, 0);
				}
			break;
		}
	}
	
	@FXML
	public void ddcButtonStateChanged(ActionEvent e)
	{
		double filteredDistances[][]							= new double[dataspace.getInactiveIndices().size()][dataspace.getInactiveIndices().size()];
		ArrayList<LDAConfiguration> filteredLDAConfigurations	= new ArrayList<LDAConfiguration>(dataspace.getInactiveIndices().size());
		
		// Copy data corresponding to chosen LDA configurations in new arrays.
		int count = 0;
		for (int selectedIndex : dataspace.getInactiveIndices()) {
			// Copy distances.
			int innerCount = 0;
			for (int selectedInnerIndex : dataspace.getInactiveIndices()) {
				filteredDistances[count][innerCount] = dataspace.getDistances()[selectedIndex][selectedInnerIndex];
				innerCount++;
			}
			
			// Copy LDA configurations.
			filteredLDAConfigurations.add(dataspace.getLDAConfigurations().get(selectedIndex));
			
			count++;
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
			break;
		}
	}
	
	@FXML
	public void changeScalingType(ActionEvent e)
	{
		distancesBarchart.changeScalingType();
	}
	
	/**
	 * Processes new information about granularity of parameter space distribution heatmap.
	 * @param e
	 */
	@FXML
	public void changeParameterDistributionGranularityMode(ActionEvent e)
	{
	}
	
	/**
	 * Processes new information about granularity of MDS heatmap.
	 * @param e
	 */
	@FXML
	public void changeHeatmapGranularityMode(ActionEvent e)
	{
		slider_mds_distribution_granularity.setDisable(checkbox_mdsHeatmap_distribution_dynAdjustment.isSelected());
		globalScatterplot.setHeatmapGranularityInformation(checkbox_mdsHeatmap_distribution_dynAdjustment.isSelected(), (int) slider_mds_distribution_granularity.getValue(), true);
	}
	
	/**
	 * Processes new information about granularity of parameter space scatterchart heatmap.
	 * @param e
	 */
	@FXML
	public void changePSHeatmapGranularityMode(ActionEvent e)
	{
		System.out.println("wat");
		paramSpace_dhmGranularity_slider.setDisable(false); // !paramSpace_dhmGranularity_checkbox.isSelected()
		refreshParameterSpaceScatterchart();
	}	

	/**
	 * Toggles MDS heatmap visibility.
	 * @param e
	 */
	@FXML
	public void changeMDSHeatmapVisibility(ActionEvent e)
	{
		globalScatterplot.setHeatmapVisiblity(showMSDHeatmap_checkbox.isSelected());
	}
	
	/**
	 * Adds listener processing keyboard events (like toggling from group to single selection mode).
	 * @param scene
	 */
	public void addKeyListener()
	{
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) 
            {
            	globalScatterplot.processKeyPressedEvent(ke);
            	distancesBarchart.processKeyPressedEvent(ke);
            	tmcHeatmap.processKeyPressedEvent(ke);
            	for (ScentedFilter filter : filters)
            		filter.processKeyPressedEvent(ke);
            	paramSpaceScatterchart.processKeyPressedEvent(ke);
            }
		});
		
		scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) 
            {
            	globalScatterplot.processKeyReleasedEvent(ke);
            	distancesBarchart.processKeyReleasedEvent(ke);
            	tmcHeatmap.processKeyReleasedEvent(ke);
            	for (ScentedFilter filter : filters)
            		filter.processKeyReleasedEvent(ke);
            	paramSpaceScatterchart.processKeyReleasedEvent(ke);
            }
		});
	}
	
	public ArrayList<LDAConfiguration> getLDAConfigurations()
	{
		return dataspace.getLDAConfigurations();
	}

	public Scene getScene()
	{
		return scene;
	}

	public void setScene(Scene scene)
	{
		this.scene = scene;
	}

	/**
	 * Switches to settings panel after corresponding icon has been clicked.
	 * @param e
	 */
	@FXML
	public void switchToSettingsPane(MouseEvent e)
	{
		openSettingsPane( ((Node)e.getSource()).getId() );
	}
	
	/**
	 * Workaround for selection-using panels: Check if clicked coordinates fit a settings icon.
	 * @param x
	 * @param y
	 */
	public void checkIfSettingsIconWasClicked(double x, double y, String iconID)
	{
		if(	(iconID == "settings_mds_icon" 				&& settings_mds_icon.getBoundsInParent().contains(x, y))			||
			(iconID == "settings_distEval_icon" 		&& settings_distEval_icon.getBoundsInParent().contains(x, y))		||
			(iconID == "settings_paramDist_icon" 		&& settings_paramDist_icon.getBoundsInParent().contains(x, y))		||
			(iconID == "settings_localScope_icon" 		&& settings_localScope_icon.getBoundsInParent().contains(x, y))
		) {
			// Open corresponding settings panel.
			openSettingsPane(iconID);
		}
	}
	
	/**
	 * Opens pane in settings panel.
	 * @param paneID
	 */
	private void openSettingsPane(String paneID)
	{	
		// Reset title styles.
		resetSettingsPanelsFontStyles();
		
		// Select new pane.
		switch (paneID) {
			case "settings_mds_icon":
				accordion_options.setExpandedPane(mdsDistEval_titledPane);
				mdsDistEval_titledPane.lookup(".title").setStyle("-fx-font-weight:bold");
			break;
			
			case "settings_distEval_icon":
				accordion_options.setExpandedPane(mdsDistEval_titledPane);
				mdsDistEval_titledPane.lookup(".title").setStyle("-fx-font-weight:bold");
			break;
				
			case "settings_paramDist_icon":
				accordion_options.setExpandedPane(paramSpace_titledPane);
				paramSpace_titledPane.lookup(".title").setStyle("-fx-font-weight:bold");
			break;
				
			case "settings_paramDistCorr_icon":
				accordion_options.setExpandedPane(paramSpace_titledPane);
				paramSpace_titledPane.lookup(".title").setStyle("-fx-font-weight:bold");
			break;
				
			case "settings_localScope_icon":
				accordion_options.setExpandedPane(localScope_titledPane);
				localScope_titledPane.lookup(".title").setStyle("-fx-font-weight:bold");
			break;
		}
	}
	
	/**
	 * Reset font styles in setting panel's titles.
	 */
	private void resetSettingsPanelsFontStyles()
	{
		// Change all font weights back to normal.
		filter_titledPane.lookup(".title").setStyle("-fx-font-weight:normal");
		mdsDistEval_titledPane.lookup(".title").setStyle("-fx-font-weight:normal");
		paramSpace_titledPane.lookup(".title").setStyle("-fx-font-weight:normal");
		paramSpace_titledPane.lookup(".title").setStyle("-fx-font-weight:normal");
		localScope_titledPane.lookup(".title").setStyle("-fx-font-weight:normal");
	}
	
	/**
	 * Called when user opens a settings pane directly.
	 * @param e
	 */
	@FXML
	public void selectSettingsPane(MouseEvent e) 
	{
		// Reset title styles.
		resetSettingsPanelsFontStyles();
		
		// Set new font style.
		((TitledPane) e.getSource()).lookup(".title").setStyle("-fx-font-weight:bold");
	}

	/**
	 * Shows respective settings icon.
	 * @param e
	 */
	@FXML
	public void showSettingsIcon(MouseEvent e)
	{
		switch ( ((Node)e.getSource()).getId() ) {
			case "mds_anchorPane":
				settings_mds_icon.setVisible(true);
			break;
			
			case "distEval_anchorPane":
				settings_distEval_icon.setVisible(true);
			break;
			
			case "paramSpace_anchorPane":
				settings_paramDist_icon.setVisible(true);
			break;
			
			case "localScope_containingAnchorPane":
				settings_localScope_icon.setVisible(true);
			break;			
		}
	}
	
	/**
	 * Hide respective settings icon.
	 * @param e
	 */
	@FXML
	public void hideSettingsIcon(MouseEvent e)
	{
		switch ( ((Node)e.getSource()).getId() ) {
			case "mds_anchorPane":
				settings_mds_icon.setVisible(false);
			break;
			
			case "distEval_anchorPane":
				settings_distEval_icon.setVisible(false);
			break;
			
			case "paramSpace_distribution_anchorPane":
				settings_paramDist_icon.setVisible(false);
			break;
			
			case "localScope_containingAnchorPane":
				settings_localScope_icon.setVisible(false);
			break;
		}		
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}
	
	/**
	 * Induces cross-visualization highlighting of one particular LDA configuration.
	 * @param ldaConfigurationID
	 */
	public void induceCrossChartHighlighting(final int ldaConfigurationID)
	{
		// Find index of this LDA configuration (remark: data structure not appropriate for this task).
		int index = -1;
		for (int i : dataspace.getActiveIndices()) {
			LDAConfiguration ldaConfig = dataspace.getLDAConfigurations().get(i);
			
			if (ldaConfig.getConfigurationID() == ldaConfigurationID)
				index = i;
		}
		
		globalScatterplot.highlightLDAConfiguration(index);
	}

	/**
	 * Removes all highlighting from charts (used e.g. after user doesn't hover over group/LDA info in CD anymore). 
	 */
	public void removeCrossChartHighlighting()
	{
		globalScatterplot.highlightLDAConfiguration(-1);
	}
	
	/**
	 * Refreshes all scented filters.
	 */
	private void refreshScentedFilters()
	{
		for (ScentedFilter filter : filters) {
			filter.refresh(new ScentedFilterDataset(dataspace.getLDAConfigurations(), dataspace.getInactiveIndices(), dataspace.getActiveIndices()));
		}
	}
	
	@Override
	public void setReferences(Workspace workspace, ProgressIndicator logPI, TextArea logTA)
	{
		super.setReferences(workspace, logPI, logTA);
		
		// Set references for child elements.
		//	...for TMC heatmap.
		tmcHeatmap.setReferences(workspace, logPI, logTA);
		// ...for parameter space visualizations. 
		paramSpaceScatterchart.setReferences(workspace, logPI, logTA);
		//	...for local scope instance.
		localScopeInstance.setReferences(workspace, logPI, logTA);
		//	... for scented filter.
		for (ScentedFilter filter : filters) {
			filter.setReferences(workspace, logPI, logTA);
		}
	}
	
	/**
	 * Changes visibility of heatmap in parameter space scatterchart.
	 * @param e
	 */
	@FXML
	public void changePSHeatmapVisibility(ActionEvent e)
	{
		refreshParameterSpaceScatterchart();
	}
	
	/**
	 * Refreshs parameter space scatterchart using existing data.
	 */
	private void refreshParameterSpaceScatterchart()
	{
		
		paramSpaceScatterchart.applyOptions(new ScatterchartOptionset(	true, true, false,
											paramSpace_dhmVisibility_checkbox.isSelected(), paramSpace_dhmGranularity_checkbox.isSelected(), (int)paramSpace_dhmGranularity_slider.getValue(),
											calculatePSScatterchartCategoriesValue(),
											paramSpace_dhmColor_min_colChooser.getValue(), paramSpace_dhmColor_max_colChooser.getValue()));
		paramSpaceScatterchart.refresh();
	}
	
	/**
	 * Updates data categories used in parameter space scatterchart.
	 * @param e
	 */
	@FXML
	public void updateParamSpaceDHMCategories(ActionEvent e)
	{
		System.out.println("refreshing");
		refreshParameterSpaceScatterchart();
	}
	
	/**
	 * Calculate the categories value for parameter space scatterchart's density heatmap.
	 * @return
	 */
	private int calculatePSScatterchartCategoriesValue()
	{
		int value = 0;
		
		value += paramSpace_dhmCategories_active_checkbox.isSelected() 		? 1 : 0;
		value += paramSpace_dhmCategories_inactive_checkbox.isSelected() 	? 2 : 0;
		value += paramSpace_dhmCategories_discarded_checkbox.isSelected() 	? 4 : 0;
		
		return value;
	}
	
	/**
	 * Update color in TMC heatmap.
	 * @param e
	 */
	@FXML
	public void updateTMCHeatmapColorSpectrum(ActionEvent e)
	{
		refreshTMCHeatmap();
	}
	
	/**
	 * Update color in global scatterchart density heatmap.
	 * @param e
	 */	
	@FXML
	public void updateGlobalScatterchartDHMColorSpectrum(ActionEvent e)
	{
		globalScatterplot.updateDHMColorSpectrum(mdsHeatmap_dhmColor_min_colChooser.getValue(), mdsHeatmap_dhmColor_max_colChooser.getValue());
	}
	
	/**
	 * Update color in parameter space scatterchart density heatmap.
	 * @param e
	 */		
	@FXML
	public void updatePSScatterchartDHMColorSpectrum(ActionEvent e)
	{
		refreshParameterSpaceScatterchart();
	}
}