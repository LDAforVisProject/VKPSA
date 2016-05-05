package control.analysisView;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.controlsfx.control.PopOver;

import model.AnalysisDataspace;
import model.LDAConfiguration;
import model.misc.KeywordRankObject;
import model.workspace.Workspace;
import control.Controller;
import view.components.DatapointIDMode;
import view.components.VisualizationComponent;
import view.components.VisualizationComponentType;
import view.components.controls.keywordFilterSetup.KeywordFilterSetup;
import view.components.heatmap.CategoricalHeatmap;
import view.components.heatmap.HeatmapOptionset;
import view.components.legacy.mdsScatterchart.MDSScatterchart;
import view.components.parallelTagCloud.ParallelTagCloud;
import view.components.parallelTagCloud.ParallelTagCloudDataset;
import view.components.parallelTagCloud.ParallelTagCloudOptionset;
import view.components.scatterchart.ScatterchartDataset;
import view.components.scatterchart.ScatterchartOptionset;
import view.components.scatterchart.ParameterSpaceScatterchart;
import view.components.scentedFilter.ScentedFilter;
import view.components.scentedFilter.ScentedFilterDataset;
import view.components.scentedFilter.ScentedFilterOptionset;
import view.components.scentedFilter.scentedKeywordFilter.ScentedKeywordFilter;
import view.components.scentedFilter.scentedKeywordFilter.ScentedKeywordFilterDataset;
import view.components.settingsPopup.SettingsPanel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
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
	 * Split panes dividing UI into resizable sections. 
	 */
	
	/**
	 * SplitPane dividing history and content panel.
	 */
	private @FXML SplitPane content_history_splitpane;
	/**
	 * SplitPane dividing filter and visualization panels.
	 */
	private @FXML SplitPane filter_vis_splitpane;
	/**
	 * SplitPane dividing overview and detail vis. components.
	 */
	private @FXML SplitPane overview_detail_splitpane;
	
	/*
	 * Tab pane and other elements for options section.
	 */
	
	/**
	 * TabPane for filters and settings.
	 */
	private @FXML TabPane options_tabpane;
	/**
	 * Tab for settings. 
	 */
	private @FXML Tab settings_tab;
	/**
	 * Tab for filters. 
	 */
	private @FXML Tab filter_tab;
	
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
	private @FXML AnchorPane settings_anchorpane;
	/**
	 * For filter settings.
	 */
	private @FXML AnchorPane filter_anchorpane;
	
	
	// For global scatterchart.

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
	 * Global Scatterchart.
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
	
	
	/**
	 * Heatmap for comparison of topic models.
	 */	
	private CategoricalHeatmap tmcHeatmap;

	
	/**
	 * Parallel tag cloud component.
	 */
	private ParallelTagCloud parallelTagCloud;

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
	
	/**
	 * PopOver used for displaying setup for new keyword filter.
	 */
	private PopOver newKeywordFilter_popover;
	/**
	 * Component used for selection of keywords to filter by.
	 */
	private KeywordFilterSetup keywordFilterSetup;
	
	/*
	 * Setting shortcuts icons.
	 */

	private @FXML ImageView settings_mds_icon;
	private @FXML ImageView settings_paramDist_icon;
	private @FXML ImageView settings_tmc_icon;
	private @FXML ImageView settings_ptc_icon;
	
	/*
	 * Maximization icons. 
	 */
	
	private @FXML ImageView maximize_ptc_icon;
	
	/*
	 * Settings panel and related elements.
	 */
	
	/**
	 * Settings panel. 
	 */
	private SettingsPanel settingsPanel;
	
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
	}
	
	private void addResizeListeners()
	{
		// Define list of anchor panes / other elements to add resize listeners to.
		ArrayList<Pane> anchorPanesToResize = new ArrayList<Pane>();

		anchorPanesToResize.add(paramSpace_distribution_anchorPane_selected);
		anchorPanesToResize.add(mds_anchorPane);
		anchorPanesToResize.add(localscope_tmc_anchorPane);
		anchorPanesToResize.add(localScope_ptc_anchorPane);
		anchorPanesToResize.add(settings_anchorpane);
		anchorPanesToResize.add(filter_anchorpane);
		
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
		initParallelTagCloud();
		initComparisonHeatmaps();
		
		// Bring settings icons to front.
		settings_mds_icon.toFront();
		settings_paramDist_icon.toFront();
		settings_tmc_icon.toFront();
		settings_ptc_icon.toFront();
		// Bring maximization icons to front.
		maximize_ptc_icon.toFront();
	}
	
	private void initSettingsPanel()
	{
		settingsPanel 	= (SettingsPanel)VisualizationComponent.generateInstance(VisualizationComponentType.SETTINGS_PANEL, this, null, null, null);		
		settingsPanel.init();
		settingsPanel.embedIn(settings_anchorpane);
	}
	
	private void initComparisonHeatmaps()
	{
		tmcHeatmap = (CategoricalHeatmap)VisualizationComponent.generateInstance(VisualizationComponentType.CATEGORICAL_HEATMAP, this, null, null, null);
		tmcHeatmap.embedIn(localscope_tmc_anchorPane);
	}

	private void initParallelTagCloud()
	{
		parallelTagCloud = (ParallelTagCloud)VisualizationComponent.generateInstance(VisualizationComponentType.PARALLEL_TAG_CLOUD, this, null, null, null);
		parallelTagCloud.embedIn(localScope_ptc_anchorPane);
	}
	
	private void initFilterControls()
	{
		// Init collections.
		filters						= new ArrayList<ScentedFilter>();
		
		// Init container for filters.
		filters_vbox 				= new VBox();
		filters_vbox.resize(100, filters_vbox.getHeight());
		
		// Create node for setup of new filter.
		keywordFilterSetup 			= new KeywordFilterSetup(this);
		// Init popover for setup of new keyword filter.
		newKeywordFilter_popover	= new PopOver(keywordFilterSetup.getRoot());
		
		// Iterate over supported parameters.
		for (String param : LDAConfiguration.SUPPORTED_PARAMETERS) {
			// Create new filter.
			ScentedFilter filter = (ScentedFilter) VisualizationComponent.generateInstance(VisualizationComponentType.SCENTED_FILTER, this, this.workspace, this.log_protocol_progressindicator, this.log_protocol_textarea);

			// Init filter.
			if (param != "kappa")
				filter.applyOptions(new ScentedFilterOptionset(param, 
																true, 
																0, 
																15, 
																50, 
																0.1,
																5,
																true, 
																true, 
																false, 
																true));
			else
				filter.applyOptions(new ScentedFilterOptionset(	param, 
																true, 
																2, 
																25, 
																50, 
																1,
																5,
																true, 
																true, 
																false, 
																false));
			
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
		
		/*
		 * Add filters using derived data (i.e.: non-primitive
		 * arguments. 
		 */
		
		// Add distance filter - note: Is derived data.
		ScentedFilter distanceFilter = (ScentedFilter) VisualizationComponent.generateInstance(VisualizationComponentType.SCENTED_FILTER, this, this.workspace, this.log_protocol_progressindicator, this.log_protocol_textarea);
		distanceFilter.applyOptions(new ScentedFilterOptionset(	"distance", 
																true, 
																0, 
																15, 
																50, 
																0.01,
																5,
																true, 
																true, 
																false, 
																false));
		// Add to collection of filters.
		filters.add(distanceFilter);
		// Embed in containing VBox.
		distanceFilter.embedIn(filters_vbox);
		
		// Add seperating line.
		Separator separator2 = new Separator();
		separator2.setTranslateX(10);
		separator2.setTranslateY(15);
		filters_vbox.getChildren().add(separator2);
		
		// Add button for adding new keyword filter.
		filters_vbox.getChildren().add(createKeywordFilterButton());
		
		/*
		 * Add filters to GUI.
		 */
		
		// Add containing VBox to pane.
		filter_anchorpane.getChildren().add(filters_vbox);
		filter_anchorpane.applyCss();
		filter_anchorpane.layout();
		filter_anchorpane.requestLayout();
		filters_vbox.resize(100, filters_vbox.getHeight());
	}

	/**
	 * Creates button for generating new keyword filter buttons.
	 * @return
	 */
	private Button createKeywordFilterButton()
	{
		ImageView plusImage 			= new ImageView(new Image(getClass().getResourceAsStream("../../icons/add.png"), 25, 25, true, true));
		Button addKeywordFilter_button 	= new Button("Create topic-based keyword filter", plusImage);
		
		/*
		 * Add event handler.
		 */
		
		// Mouse entered: Change cursor type.
		addKeywordFilter_button.addEventHandler(MouseEvent.MOUSE_ENTERED, (new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) 
            {
            	scene.setCursor(Cursor.HAND);
            }
		}));
		// Mouse exited: Change cursor type.
		addKeywordFilter_button.addEventHandler(MouseEvent.MOUSE_EXITED, (new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) 
            {
            	scene.setCursor(Cursor.DEFAULT);
            }
		}));
		// Mouse clicked: Show setup for new filter.
		addKeywordFilter_button.addEventHandler(MouseEvent.MOUSE_CLICKED, (new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) 
            {
            	if (!newKeywordFilter_popover.isShowing()) {
            		newKeywordFilter_popover.setX(0);
            		newKeywordFilter_popover.setY(0);
            		newKeywordFilter_popover.show(addKeywordFilter_button);
            	}
            }
		}));
		
		addKeywordFilter_button.setStyle("-fx-background-color:#fff; -fx-border-color:grey;");// setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		addKeywordFilter_button.setTranslateY(25);
		
		return addKeywordFilter_button;
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
	 * Integrates data/heatmap cells selected in TMC heatmap into dataspace and displays the
	 * selection in local scope.
	 * @param selectedTopicConfigIDs
	 */
	public void integrateTMCHeatmapSelection(Set<Pair<Integer, Integer>> selectedTopicConfigIDs)
	{
		ParallelTagCloudDataset ptcData 		= new ParallelTagCloudDataset(selectedTopicConfigIDs);
		ParallelTagCloudOptionset ptcOptions 	= new ParallelTagCloudOptionset(true, true, false, (int) settingsPanel.getLocalScopeKeywordNumberSlider().getValue());
		// Refresh parallel tag cloud.
		parallelTagCloud.refresh(ptcOptions, ptcData);
	}

	/**
	 * Integrates index selection into MDS selection, then fires update for all relevant visualizations.
	 * @param newlySelectedLocalIndices
	 * @param isAddition true for addition of selected data to global selection; false for their removal.
	 * @param idMode
	 * @param forceRefresh
	 */
	public void integrateSelectionOfDataPoints(Set<Integer> newlySelectedLocalIndices, final boolean isAddition, final DatapointIDMode idMode, final boolean forceRefresh)
	{
		// Check if there is any change in the set of selected datasets.
		boolean changeDetected = dataspace.integrateSelection(newlySelectedLocalIndices, isAddition, idMode);
		
		// 2. 	Update related (i.e. dependent on the set of selected entities) datasets and visualization, if there were any changes made.
		if (changeDetected || forceRefresh)
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
		
		//	Topic model comparison heatmap:
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
		
		else {
			// Clear TMC heatmap.
			tmcHeatmap.clear();
			// Clear PTC.
			parallelTagCloud.clear();
		}
	}
	
	/**
	 * Refreshes parallel tag cloud after settings change. Uses available data. 
	 */
	public void refreshPTC()
	{
		ParallelTagCloudOptionset ptcOptions 	= new ParallelTagCloudOptionset(true, true, false, (int) settingsPanel.getLocalScopeKeywordNumberSlider().getValue());
		// Refresh parallel tag cloud.
		parallelTagCloud.refresh(ptcOptions);
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
			// Filter by primitive and derivate attributes.
			if (filter.getClass().getName().endsWith("ScentedFilter"))
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
				mds_content_anchorPane.setPrefWidth(mds_anchorPane.getWidth() - 5);
				mds_content_anchorPane.setPrefHeight(mds_anchorPane.getHeight() - 5 - 30);
				
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
				parallelTagCloud.resizeContent(width, height);
			break;
			
			case "settings_anchorpane":
				filters_vbox.setPrefWidth(width - 20);
				for (ScentedFilter filter : filters) {
					filter.resizeContent(width - 20, 0);
				}
			break;
		}
	}
	
	/**
	 * Used for initial resizing of individual nodes.
	 * @param node
	 * @param width
	 * @param height
	 */
	private void resizeElementManually(Node node, double width, double height)
	{
		switch (node.getId()) {
			case "paramSpace_distribution_anchorPane_filtered":
			break;
			
			case "paramSpace_distribution_anchorPane_selected":
				// Adapt size.
				paramSpaceScatterchart.resizeContent(width, height);
			break;
			
			// Resize local scope element: Chord diagram.
			case "localscope_tmc_anchorPane":
				tmcHeatmap.resizeContent(width, height);
			break;
			
			// Resize local scope element: Parallel tag clouds.
			case "localScope_ptc_anchorPane":
				parallelTagCloud.resizeContent(width, height);
			break;
			
			case "settings_anchorpane":
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
		
		// Set customized keyhandler for zoomable (i.e. contained in scroll pane) components.
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
		if (ke.getEventType() == KeyEvent.KEY_PRESSED) {
			globalScatterplot.processKeyPressedEvent(ke);
	    	tmcHeatmap.processKeyPressedEvent(ke);
	    	for (ScentedFilter filter : filters)
	    		filter.processKeyPressedEvent(ke);
	    	paramSpaceScatterchart.processKeyPressedEvent(ke);
	    	parallelTagCloud.processKeyPressedEvent(ke);
		}
			
		else if (ke.getEventType() == KeyEvent.KEY_RELEASED) {
	    	globalScatterplot.processKeyReleasedEvent(ke);
	    	tmcHeatmap.processKeyReleasedEvent(ke);
	    	for (ScentedFilter filter : filters)
	    		filter.processKeyReleasedEvent(ke);
	    	paramSpaceScatterchart.processKeyReleasedEvent(ke);
	    	parallelTagCloud.processKeyReleasedEvent(ke);
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
		// Switch to settings pane.
		options_tabpane.getSelectionModel().select(settings_tab);
		
		// Open pane.
		settingsPanel.openSettingsPane(((Node)e.getSource()).getId());
	}
	
	/**
	 * Maximizes panel.
	 * @param e
	 */
	@FXML
	public void maximizePanel(MouseEvent e)
	{
		overview_detail_splitpane.setDividerPosition(0, 0.02);
		content_history_splitpane.setDividerPosition(0, 0.98);
		filter_vis_splitpane.setDividerPosition(0, 0.02);
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
				settings_tmc_icon.setVisible(true);
			break;
			
			case "detailView_anchorpane":
				settings_ptc_icon.setVisible(true);
				maximize_ptc_icon.setVisible(true);
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
			
			case "paramSpace_anchorPane":
				settings_paramDist_icon.setVisible(false);
			break;
			
			case "localScope_containingAnchorPane":
				settings_tmc_icon.setVisible(false);
			break;
			
			case "detailView_anchorpane":
				settings_ptc_icon.setVisible(false);
				maximize_ptc_icon.setVisible(false);
			break;						
		}		
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
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
		/*
		 *  Refresh filters for primitive parameters.
		 */
		for (ScentedFilter filter : filters.subList(0, 3)) {
			filter.refresh(new ScentedFilterDataset(dataspace.getLDAConfigurations(), dataspace.getInactiveIndices(), dataspace.getActiveIndices()));
		}
		
		/*
		 *  Refresh filters for derived parameters.
		 */
		
		//	Distance filter:
		filters.get(3).refresh(new ScentedFilterDataset(dataspace.getLDAConfigurations(), dataspace.getInactiveIndices(), dataspace.getActiveIndices(),
														dataspace.getAverageDistances()));
		
		/*
		 * Refresh keyword filters.
		 */
		
		for (ScentedFilter filter : filters.subList(4, filters.size())) {
			((ScentedKeywordFilter)filter).refresh(	dataspace.getInactiveIndices(), 
													dataspace.getActiveIndices());
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
		//	... for scented filter.
		for (ScentedFilter filter : filters) {
			filter.setReferences(workspace, logPI, logTA);
		}
		// ...for parallel tag cloud.
		parallelTagCloud.setReferences(workspace, logPI, logTA);
		// ... for settings panel.
		settingsPanel.setReferences(workspace, logPI, logTA);
		// ... for keyword filter setup control.
		keywordFilterSetup.setReferences(workspace, logPI, logTA);
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
	 * Wrapper method for {@link AnalysisController#highlightDataPoints(Set, DatapointIDMode, VisualizationComponentType)}.
	 * @param dataPointID
	 * @param idMode
	 * @param visType
	 */
	public void highlightDataPoints(int dataPointID, DatapointIDMode idMode, VisualizationComponentType visType)
	{
		Set<Integer> dataPointIDs = new HashSet<Integer>();
		dataPointIDs.add(dataPointID);
		
		// Call actual method.
		highlightDataPoints(dataPointIDs, idMode, visType, null);
	}
	
	/**
	 * Instructs visualization components to highlight delivered data points.
	 * @param dataPointIDs
	 * @param idMode
	 * @param sourceVisType
	 * @param componentInstanceID Arbitrarily chosen identification for a visualization component to distinguish  
	 * between several instances of the same component. null is an accepted value if this is not requested.
	 */
	public void highlightDataPoints(Set<Integer> dataPointIDs, DatapointIDMode idMode, VisualizationComponentType sourceVisType, String componentInstanceID)
	{
		/*
		 * 1. Translate configuration IDs to indices.
		 */
		
		Set<Integer> dataPointConfigIDs = idMode == DatapointIDMode.CONFIG_ID 	? dataPointIDs : dataspace.translateBetweenIDModes(dataPointIDs, DatapointIDMode.INDEX);
		Set<Integer> dataPointIndices	= idMode == DatapointIDMode.INDEX		? dataPointIDs : dataspace.translateBetweenIDModes(dataPointIDs, DatapointIDMode.CONFIG_ID);
		
		/*
		 * 2. Propagate information about hover event.
		 */
		
		if (sourceVisType != VisualizationComponentType.GLOBAL_SCATTERCHART)
			globalScatterplot.highlightHoveredOverDataPoints(dataPointIndices, DatapointIDMode.INDEX);
		
		if (sourceVisType != VisualizationComponentType.PARAMSPACE_SCATTERCHART)
			paramSpaceScatterchart.highlightHoveredOverDataPoints(dataPointConfigIDs, DatapointIDMode.CONFIG_ID);
		
		// Workaround due to convenience: Apply on filters regardless - since only one of them was 
		// being manipulated, we want the others to be updated too.
		for (ScentedFilter filter : filters) {
			// For common scented filters: Use index mode.
			if (filter.getClass().getName().endsWith("ScentedFilter"))
				filter.highlightHoveredOverDataPoints(dataPointIndices, DatapointIDMode.INDEX);
			
			// Consider keyword filter: Use configuration IDs.
			else if (filter.getClass().getName().endsWith("ScentedKeywordFilter")) {
				// Only update if componend ID doesn't match, i.e. the same component is not updated twice.
				if (	componentInstanceID == null || 
						!componentInstanceID.equals(filter.getComponentIdentification())) {
					filter.highlightHoveredOverDataPoints(dataPointIDs, DatapointIDMode.CONFIG_ID);
				}
			}
			else 
				System.out.println("what: " + filter.getClass().getName());
		}
		
		if (sourceVisType != VisualizationComponentType.CATEGORICAL_HEATMAP)
			tmcHeatmap.highlightHoveredOverDataPoints(dataPointConfigIDs, DatapointIDMode.CONFIG_ID);
		
		// Remove highlighting from parallel tag cloud.
		if (sourceVisType != VisualizationComponentType.PARALLEL_TAG_CLOUD && parallelTagCloud != null)  {
			parallelTagCloud.highlightHoveredOverDataPoints(dataPointConfigIDs, DatapointIDMode.CONFIG_ID);
		}
	}
	
	/**
	 * Instructs visualization components to remove highlighting from points.
	 * @param sourceVisType
	 */
	public void removeHighlighting(VisualizationComponentType sourceVisType)
	{
		// Remove highlighting from global scatterchart.
		if (sourceVisType != VisualizationComponentType.GLOBAL_SCATTERCHART)
			globalScatterplot.removeHoverHighlighting();

		// Remove highlighting from parameter space scatterchart.		
		if (sourceVisType != VisualizationComponentType.PARAMSPACE_SCATTERCHART)
			paramSpaceScatterchart.removeHoverHighlighting();
		
		// Workaround due to convenience: Apply on filters regardless - since only one of them was 
		// being manipulated, we want the others to be updated too.
		for (ScentedFilter filter : filters) {
			filter.removeHoverHighlighting();
		}
		
		// Remove highlighting from TMC.
		if (sourceVisType != VisualizationComponentType.CATEGORICAL_HEATMAP)
			tmcHeatmap.removeHoverHighlighting();
		
		// Remove highlighting from parallel tag cloud.
		if (sourceVisType != VisualizationComponentType.PARALLEL_TAG_CLOUD)
			parallelTagCloud.removeHoverHighlighting();		
	}
	
	/**
	 * Creates new keyword filter for the given keyword (as long as it's valid).
	 * @param keyword
	 */
	public void createKeywordFilter(String keyword)
	{
		try {
			// Add new filter only if keyword exists.
			if (	workspace.getDatabaseManagement().doesKeywordExist(keyword) &&
					ScentedKeywordFilter.addToSetOfUsedKeywords(keyword)) {
				/*
				 * Retrive rank information for keyword. 
				 */
				ArrayList<KeywordRankObject> keywordRankObjects = workspace.getDatabaseManagement().loadKeywordRankInformation(keyword);
				
				/*
				 * Add filter component to GUI. 
				 */
				
				// Add  keyword filter. 
				ScentedKeywordFilter keywordFilter = (ScentedKeywordFilter) VisualizationComponent.generateInstance(VisualizationComponentType.SCENTED_KEYWORD_FILTER, this, this.workspace, this.log_protocol_progressindicator, this.log_protocol_textarea);
				// For binning: Get number of keywords.
				final int numberOfKeywords = workspace.getDatabaseManagement().readNumberOfKeywords(true);
				// Apply option set.
				keywordFilter.applyOptions(new ScentedFilterOptionset(	keyword, 
																		true, 
																		1, 
																		numberOfKeywords, 
																		20, 
																		numberOfKeywords / 50,
																		5,
																		true, 
																		true, 
																		false, 
																		true));
		
				// Add to collection of filters.
				filters.add(keywordFilter);
				// Embed in containing VBox.
				keywordFilter.embedIn(filters_vbox);
				
				// Adjust height of filter pane.
				adjustFilterPaneHeight(true);
				
				/*
				 * Fill component with data.
				 */
				keywordFilter.refresh(new ScentedKeywordFilterDataset(	dataspace.getLDAConfigurations(), 
																		dataspace.getInactiveIndices(), 
																		dataspace.getActiveIndices(),
																		keywordRankObjects, 
																		keyword,
																		numberOfKeywords));
			}
			
		} 
		
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Removes dynamic keyword filter from GUI and storage.
	 * @param filterToRemove
	 */
	public void removeKeywordFilter(ScentedKeywordFilter filterToRemove)
	{
		boolean wasFilterFound = false;
		
		// Error checking: Try to identify filter.
		for (ScentedFilter sf : filters) {
			if (filterToRemove.getComponentIdentification().equals(sf.getComponentIdentification())) {
				wasFilterFound = true;
				break;
			}
		}
		
		if (wasFilterFound) {
			// Remove filter from GUI.
			filters_vbox.getChildren().remove(filterToRemove.getRoot());
			
			// Remove filter instance from collection.
			filters.remove(filterToRemove);
			
			// Adapt height of filter pane.
			adjustFilterPaneHeight(false);
		}
		
		else {
			log("### ERROR ### Trying to remove non-existent filter.");
			System.out.println("### ERROR ### Trying to remove non-existent filter.");
		}
		
	}
	
	/**
	 * Adjust height of filter pane in accordance to number of registered filters.
	 * @param isAddition Determines whether pane height should be in- or decreased.
	 */
	private void adjustFilterPaneHeight(boolean isAddition)
	{
		// Increase height of containing pane.
		if (isAddition) {
			System.out.println("pref height = " + filter_anchorpane.getHeight() + 92 + (ScentedKeywordFilter.getKeywordCount() - 1) * ScentedKeywordFilter.GAP);
			filter_anchorpane.setPrefHeight(filter_anchorpane.getHeight() + 92 + ScentedKeywordFilter.GAP);
		}
		
		// Decrease height of containing pane.
		else {
			filter_anchorpane.setPrefHeight(filter_anchorpane.getHeight() - 92 - ScentedKeywordFilter.GAP);
		}
		
		/*
		 * Re-position all dynamic keyword filter.
		 */
		
		int count = 0;
		for (ScentedFilter sf : filters) {
			if (sf.getClass().toString().endsWith("ScentedKeywordFilter")) {
				// Add spacing for keyword filter.
				sf.getRoot().setTranslateY(36 + count++ * ScentedKeywordFilter.GAP);
			}
		}

		
	}
	
	/**
	 * Returns dataspace used for analysis.
	 * @return
	 */
	public final AnalysisDataspace getDataspace()
	{
		return dataspace;
	}
}