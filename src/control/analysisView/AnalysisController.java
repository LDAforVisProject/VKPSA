package control.analysisView;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.controlsfx.control.PopOver;

import model.AnalysisDataspace;
import model.LDAConfiguration;
import model.workspace.Workspace;
import control.Controller;
import control.analysisView.localScope.LocalScopeVisualizationType;
import view.components.DatapointIDMode;
import view.components.VisualizationComponent;
import view.components.VisualizationComponentType;
import view.components.heatmap.CategoricalHeatmap;
import view.components.heatmap.HeatmapOptionset;
import view.components.legacy.LocalScopeInstance;
import view.components.legacy.mdsScatterchart.MDSScatterchart;
import view.components.scatterchart.ScatterchartDataset;
import view.components.scatterchart.ScatterchartOptionset;
import view.components.scatterchart.ParameterSpaceScatterchart;
import view.components.scentedFilter.ScentedFilter;
import view.components.scentedFilter.ScentedFilterDataset;
import view.components.scentedFilter.ScentedFilterOptionset;
import view.components.settingsPopup.SettingsPanel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
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
	
	/*
	 * Anchorpanes hosting visualizations. 
	 */
	
	/**
	 * Root pane.
	 */
	private @FXML AnchorPane analysisRoot_anchorPane;
	
	/**
	 * For settings.
	 */
	private @FXML AnchorPane settings_anchorPane;
	/**
	 * For filter settings.
	 */
	private @FXML AnchorPane settings_filter_anchorPane;
	
	
	// For MDS scatterchart.
	// --------------------
	/**
	 * AnchorPane holding scroll pane (important for zoom). 
	 */
	private @FXML AnchorPane mds_anchorPane;
	/**
	 * ScrollPane holding component.
	 */
	private @FXML ScrollPane mds_content_scrollPane;
	/**
	 * AnchorPane holding actual scatterchart. 
	 */
	private @FXML AnchorPane mds_content_anchorPane;
	// --------------------
	
	/**
	 * For distance evaluation barchart.
	 */
	private @FXML AnchorPane distEval_anchorPane;
	
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
	
	/*
	 * Local scope.
	 */
	
	/**
	 * Heatmap for comparison of topic models.
	 */	
	CategoricalHeatmap tmcHeatmap;
	
	/**
	 * Parallel tag cloud in local scope.
	 */
	private LocalScopeInstance localScopeInstance;

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
	private @FXML ImageView settings_paramDist_icon;
	private @FXML ImageView settings_localScope_icon;
	
	/*
	 * Settings panel and related elements.
	 */
	
	/**
	 * Settings panel. 
	 */
	private SettingsPanel settingsPanel;
	/**
	 * Popover for settings panel.
	 */
	private PopOver settingsPopOver;
	
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
		initSettingsPanel();
		initFilterControls();
		initMDSScatterchart();
		initParameterSpaceScatterchart();
		initLocalScopeView();
		initComparisonHeatmaps();
		
		// Bring setting icons to front.
		settings_mds_icon.toFront();
		settings_paramDist_icon.toFront();
		settings_localScope_icon.toFront();
	}
	
	private void initSettingsPanel()
	{
		settingsPanel 	= (SettingsPanel)VisualizationComponent.generateInstance(VisualizationComponentType.SETTINGS_PANEL, this, null, null, null);
		settingsPopOver = new PopOver(settingsPanel.getRoot());
		
		settingsPanel.init();
		settingsPopOver.detach();
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
															settingsPanel.getLocalScopeTopicNumberSlider(), settingsPanel.getLocalScopeTopicNumberTextField(),
															settingsPanel.getLocalScopeKeywordNumberSlider(), settingsPanel.getLocalScopeTopicNumberTextField());
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
				filter.applyOptions(new ScentedFilterOptionset(param, true, 0, 15, 50, 0.05, true, true, false));
			else
				filter.applyOptions(new ScentedFilterOptionset(param, true, 2, 25, 50, 1, true, true, false));
			
			// Add to collection of filters.
			filters.add(filter);
			// Embed in containing VBox.
			filter.embedIn(filters_vbox);
		}
		
		// Add seperating line.
		Separator separator = new Separator();
		separator.setTranslateX(15);
		separator.setTranslateY(15);
		filters_vbox.getChildren().add(separator);
		
		// Add distance filter - note: Is derived data.
		ScentedFilter distanceFilter = (ScentedFilter) VisualizationComponent.generateInstance(VisualizationComponentType.SCENTED_FILTER, this, this.workspace, this.log_protocol_progressindicator, this.log_protocol_textarea);
		distanceFilter.applyOptions(new ScentedFilterOptionset("distance", true, 0, 15, 50, 0.01, true, true, false));
		// Add to collection of filters.
		filters.add(distanceFilter);
		// Embed in containing VBox.
		distanceFilter.embedIn(filters_vbox);
		
		// Add containing VBox to pane.
		settings_filter_anchorPane.getChildren().add(filters_vbox);
		settings_filter_anchorPane.applyCss();
		settings_filter_anchorPane.layout();
		settings_filter_anchorPane.requestLayout();
		filters_vbox.resize(100, filters_vbox.getHeight());
	}

	private void initMDSScatterchart()
	{
		// Init scatterchart. checkbox_mdsHeatmap_distribution_dynAdjustment
		mds_content_scrollPane.toBack();
		globalScatterplot = new MDSScatterchart(	this, mds_scatterchart, mdsHeatmap_canvas, mds_content_scrollPane,
													settingsPanel.getGlobalScatterchartDHMGranularityCheckbox(), settingsPanel.getGlobalScatterchartDHMGranularitySlider(),
													settingsPanel.getGlobalScatterchartDHMMinColor(), settingsPanel.getGlobalScatterchartDHMMaxColor());
	}
	
	private void initParameterSpaceScatterchart()
	{
		// Init parameter space scatterchart.
		paramSpaceScatterchart = (ParameterSpaceScatterchart) VisualizationComponent.generateInstance(VisualizationComponentType.PARAMSPACE_SCATTERCHART, this, null, null, null);
		paramSpaceScatterchart.applyOptions(new ScatterchartOptionset(	true, true, false,
																		settingsPanel.isParamSpaceDHMVisible(), settingsPanel.isParamSpaceDHMGranularityDynamic(), settingsPanel.getParamSpaceDHMGranularityValue(),
																		settingsPanel.calculatePSScatterchartCategoriesValue(),
																		settingsPanel.getParamSpaceDHMMinColor(), settingsPanel.getParamSpaceDHMMaxColor()));
		paramSpaceScatterchart.embedIn(paramSpace_distribution_anchorPane_selected);
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
		
		// Draw entire data set. Used as initialization call, executed by Workspace instance.
		if (filterData) {
			// Filter data.
			applyFilter();
		}

		// Call initialization procedure.
		init();
		
		/*
		 * Refresh visualizations.
		 */
		
		// 	Parameter filtering controls:
		refreshScentedFilters();
		
		// 	MDS scatterchart:
		globalScatterplot.refresh(	dataspace.getCoordinates(), dataspace.getReferenceTMIndex(),
									dataspace.getAvailableCoordinates(), dataspace.getInactiveIndices(), 
									dataspace.getActiveCoordinates(), dataspace.getActiveIndices(), 
									dataspace.getDiscardedCoordinates(), dataspace.getDiscardedIndices());

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
			globalScatterplot.identifyGlobalExtrema(dataspace.getCoordinates());
			paramSpaceScatterchart.identifyGlobalExtrema(dataspace.getLDAConfigurations());
			
			// Add keyboard listener in order to enable selection.
			addKeyListener();
			
			// Mark global extrema as found.
			globalExtremaIdentified = true;

			// Adjust controls for filters for primitive attribute.
			for (ScentedFilter filter : filters.subList(0, 3)) {
				// Adjust global extrema for controls.
				filter.adjustControlExtrema(dataspace.getLDAConfigurations());
				// Set initial width.
				filter.resizeContent(300, 0);
			}
			// Adjust controls for filters with derived attributes.
			filters.get(3).adjustDerivedControlExtrema(dataspace.getAverageDistances());
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
			//localScopeInstance.refresh(dataspace.getActiveLDAConfigurations());
			// Refresh topic model comparison heatmap.
			refreshTMCHeatmap(dataspace.getActiveLDAConfigurations());
		}
	}
	
	/**
	 * Integrates data/heatmap cells selected in TMC heatmap into dataspace and displays the
	 * selection in local scope.
	 * @param selectedTopicConfigIDs
	 */
	public void integrateTMCHeatmapSelection(Set<Pair<Integer, Integer>> selectedTopicConfigIDs)
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
		// 	MDS scatterchart:
		globalScatterplot.refresh(		dataspace.getCoordinates(), dataspace.getReferenceTMIndex(),
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
																settingsPanel.getTMCMinColor(), settingsPanel.getTMCMaxColor(), new Color(0.0, 0.0, 1.0, 0.5), new Color(1.0, 0.0, 0.0, 0.5),
																"", "",
																true, false, true);
			// Instruct heatmap to fetch topic distance data asynchronously.
			tmcHeatmap.fetchTopicDistanceData(selectedLDAConfigurations, tmcOptions);
		}
		
		else
			tmcHeatmap.clear();
	}
	
	/**
	 * Refreshes heatmap for topic model comparison. Uses available data. 
	 */
	private void refreshTMCHeatmap()
	{
		// Update colors by refreshing the TMC heatmap.
		HeatmapOptionset tmcOptions = new HeatmapOptionset(	true, -1, 
															settingsPanel.getTMCMinColor(), settingsPanel.getTMCMaxColor(), new Color(0.0, 0.0, 1.0, 0.5), new Color(1.0, 0.0, 0.0, 0.5),
															"", "",
															true, false, true);
		tmcHeatmap.applyOptions(tmcOptions);
		tmcHeatmap.refresh();
	}
	
	/**
	 * Refreshes heatmap for topic model comparison. Uses available data. 
	 * @param minColor
	 * @param maxColor
	 */
	private void refreshTMCHeatmap(Color minColor, Color maxColor)
	{
		// Update colors by refreshing the TMC heatmap.
		HeatmapOptionset tmcOptions = new HeatmapOptionset(	true, -1, 
															minColor, maxColor, new Color(0.0, 0.0, 1.0, 0.5), new Color(1.0, 0.0, 0.0, 0.5),
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
		
		// Here: Consider only filter for primitive attributes.
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
				// Update content anchor pane. Deduct 5 pixels for scroll bars.
				mds_content_anchorPane.setPrefWidth(mds_content_scrollPane.getWidth() - 5);
				mds_content_anchorPane.setPrefHeight(mds_content_scrollPane.getHeight() - 5);
				
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
			case "button_relativeView_paramDist":
			break;
		}
	}
	
	/**
	 * Processes new information about granularity of density heatmap in global scatterchart.
	 * @param isGranularityDynamic
	 * @param granularity
	 */
	public void changeGlobalScatterplotDHMGranularity(boolean isGranularityDynamic, int granularity)
	{
		globalScatterplot.setHeatmapGranularityInformation(isGranularityDynamic, granularity, true);
	}

	/**
	 * Toggles global scatterplot's density heatmap visiblity.
	 * @param isVisible
	 */
	public void changeGlobalScatterplotDHMVisibility(boolean isVisible)
	{
		globalScatterplot.setHeatmapVisiblity(isVisible);
	}
	
	/**
	 * Adds listener processing keyboard events (like toggling from group to single selection mode).
	 * @param scene
	 */
	public void addKeyListener()
	{
		/*
		 * Consume space bar key event in scroll pane, manually propagate event to
		 * AnalysisController's key event processing. 
		 */
		EventHandler<KeyEvent> scrollPaneKEHandler = (new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) 
            {
            	if (ke.getCode() == KeyCode.SPACE || ke.getCharacter().equals(" ")) {
	            	notifyKeyEventSubscribers(ke);
	            	ke.consume();
            	}
            }
		});
		
		globalScatterplot.setSpaceKeyHandler(scrollPaneKEHandler);
		paramSpaceScatterchart.setSpaceKeyHandler(scrollPaneKEHandler);
		
		/**
		 * Process key events.
		 */
		
		EventHandler<KeyEvent> defaultKEHandler = (new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) 
            {
            	notifyKeyEventSubscribers(ke);
            }
		});
		
		scene.setOnKeyPressed(defaultKEHandler);
		scene.setOnKeyTyped(defaultKEHandler);
		scene.setOnKeyReleased(defaultKEHandler);
	}
	
	/**
	 * Notifies all subscribers about key events.
	 * @param ke
	 */
	private void notifyKeyEventSubscribers(KeyEvent ke)
	{
		if (ke.getEventType() == KeyEvent.KEY_PRESSED || ke.getEventType() == KeyEvent.KEY_RELEASED) {
	    	globalScatterplot.processKeyReleasedEvent(ke);
	    	tmcHeatmap.processKeyReleasedEvent(ke);
	    	for (ScentedFilter filter : filters)
	    		filter.processKeyReleasedEvent(ke);
	    	paramSpaceScatterchart.processKeyReleasedEvent(ke);
		}
		
		else if (ke.getEventType() == KeyEvent.KEY_TYPED) {
			globalScatterplot.processKeyPressedEvent(ke);
			paramSpaceScatterchart.processKeyPressedEvent(ke);
		}
		
    	ke.consume();
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
		settingsPopOver.show(analysisRoot_anchorPane);
		settingsPopOver.setX(e.getSceneX());
		settingsPopOver.setY(e.getSceneY());
		settingsPopOver.detach();
		
		settingsPanel.openSettingsPane(((Node)e.getSource()).getId());
	}
	
	/**
	 * Workaround for selection-using panels: Check if clicked coordinates fit a settings icon.
	 * @param x
	 * @param y
	 */
	public void checkIfSettingsIconWasClicked(double x, double y, String iconID)
	{		
		if(	(iconID == "settings_mds_icon" 				&& settings_mds_icon.getBoundsInParent().contains(x, y))			||
			(iconID == "settings_paramDist_icon" 		&& settings_paramDist_icon.getBoundsInParent().contains(x, y))		||
			(iconID == "settings_localScope_icon" 		&& settings_localScope_icon.getBoundsInParent().contains(x, y))
		) {
			settingsPopOver.show(analysisRoot_anchorPane);
			settingsPopOver.setX(0);
			settingsPopOver.setY(0);
			settingsPopOver.detach();
		}
	}
	
	/**
	 * Reset font styles in setting panel's titles.
	 */
	private void resetSettingsPanelsFontStyles()
	{
		// Change all font weights back to normal.
		filter_titledPane.lookup(".title").setStyle("-fx-font-weight:normal");
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
		// Refresh filters for primitive parameters.
		for (ScentedFilter filter : filters.subList(0, 3)) {
			filter.refresh(new ScentedFilterDataset(dataspace.getLDAConfigurations(), dataspace.getInactiveIndices(), dataspace.getActiveIndices()));
		}
		// Refresh filters for derived parameters.
		//	Distance filter:
		filters.get(3).refresh(new ScentedFilterDataset(dataspace.getLDAConfigurations(), dataspace.getInactiveIndices(), dataspace.getActiveIndices(),
														dataspace.getAverageDistances()));
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
		// ... for settings panel.
		settingsPanel.setReferences(workspace, logPI, logTA);
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
	public void refreshParameterSpaceScatterchart()
	{
		paramSpaceScatterchart.applyOptions(new ScatterchartOptionset(	true, true, false,
											settingsPanel.isParamSpaceDHMVisible(), settingsPanel.isParamSpaceDHMGranularityDynamic(), settingsPanel.getParamSpaceDHMGranularityValue(),
											settingsPanel.calculatePSScatterchartCategoriesValue(),
											settingsPanel.getParamSpaceDHMMinColor(), settingsPanel.getParamSpaceDHMMaxColor()));
		paramSpaceScatterchart.refresh();
	}
	
	/**
	 * Update color in global scatterchart density heatmap.
	 * @param minColor
	 * @param maxColor
	 */
	public void updateTMCHeatmapColorSpectrum(Color minColor, Color maxColor)
	{
		refreshTMCHeatmap(minColor, maxColor);
	}
	
	/**
	 * Update color in global scatterchart density heatmap.
	 * @param minColor
	 * @param maxColor
	 */
	public void updateGlobalScatterchartDHMColorSpectrum(Color minColor, Color maxColor)
	{
		globalScatterplot.updateDHMColorSpectrum(minColor, maxColor);
	}
	
	/**
	 * Instructs visualization components to highlight delivered data points.
	 * @param dataPointIDs
	 * @param idMode
	 */
	public void highlightDataPoints(Set<Integer> dataPointIDs, DatapointIDMode idMode)
	{
		
	}
	
	/**
	 * Instructs visualization components to remove highlighting from points.
	 * @param dataPointIDs
	 * @param idMode
	 */
	public void removeHighlighting()
	{
		
	}
}