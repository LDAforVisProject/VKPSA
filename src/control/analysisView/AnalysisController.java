package control.analysisView;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import model.AnalysisDataspace;
import model.LDAConfiguration;
import model.workspace.Workspace;

import org.controlsfx.control.RangeSlider;

import control.Controller;
import control.analysisView.localScope.LocalScopeVisualizationType;
import view.components.VisualizationComponent;
import view.components.VisualizationComponentType;
import view.components.heatmap.CategoricalHeatmap;
import view.components.heatmap.NumericalHeatmap;
import view.components.heatmap.HeatmapDataset;
import view.components.heatmap.HeatmapOptionset;
import view.components.legacy.DistancesBarchart;
import view.components.legacy.LocalScopeInstance;
import view.components.legacy.heatmap.HeatMap;
import view.components.legacy.heatmap.HeatmapDataBinding;
import view.components.legacy.heatmap.HeatmapDataType;
import view.components.legacy.mdsScatterchart.MDSScatterchart;
import view.components.scentedFilter.ScentedFilter;
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
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.control.Accordion;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
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
	
	/**
	 * For distance correlation linechart.
	 * @deprecated
	 */
	private @FXML AnchorPane paramSpace_correlation_anchorPane;
	
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
	private @FXML ScatterChart<Number, Number> mds_scatterchart;
	/**
	 * Canvas showing the heatmap for the MDS scatter chart.
	 */
	private @FXML Canvas mdsHeatmap_canvas;
	
	// Following: Controls for MDS heatmap.
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
	
	// Component for heatmap showing filtered data:
	private NumericalHeatmap parameterspace_heatmap_filtered;
	
	// Component for heatmap showing selected data:
	private NumericalHeatmap parameterspace_heatmap_selected;
	
	// Heatmap setting controls (and metadata).
	
	private @FXML ToggleButton button_relativeView_paramDist;
	private @FXML ComboBox<String> combobox_parameterSpace_distribution_xAxis;
	private @FXML ComboBox<String> combobox_parameterSpace_distribution_yAxis;
	private @FXML Slider slider_parameterSpace_distribution_granularity;
	private @FXML CheckBox checkbox_parameterSpace_distribution_dynAdjustment;
	
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
	private @FXML ImageView settings_paramDistCorr_icon;
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
		initParameterSpaceHeatmaps();
		initLocalScopeView();
		initComparisonHeatmaps();
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
		scrollpane_filter.setContent(gridpane_parameterConfiguration);
		
		// Init collections.
		filters						= new ArrayList<ScentedFilter>();
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
		rangeSliders.get("kappa").setSnapToTicks(false);
		
		// Temporary workaround: Fix length of kappa range slider interval at 1.
		rangeSliders.get("kappa").lowValueProperty().addListener(new ChangeListener<Number>() {
		    @Override 
		    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue)
		    {
		    	rangeSliders.get("kappa").setHighValue(rangeSliders.get("kappa").getLowValue() + 1);
		    }
		});
		rangeSliders.get("kappa").highValueProperty().addListener(new ChangeListener<Number>() {
		    @Override 
		    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue)
		    {
		    	rangeSliders.get("kappa").setLowValue(rangeSliders.get("kappa").getHighValue() - 1);
		    }
		});
		
		// Adapt textfield values.
		for (Map.Entry<String, Pair<TextField, TextField>> entry : textfieldsForFilterControls.entrySet()) {
			entry.getValue().getKey().setText( String.valueOf(rangeSliders.get(entry.getKey()).getMin()) );
			entry.getValue().getValue().setText( String.valueOf(rangeSliders.get(entry.getKey()).getMax()) );
		}
		
		// Add range slider to GUI.
		vbox_alpha.getChildren().add(rangeSliders.get("alpha"));
		vbox_eta.getChildren().add(rangeSliders.get("eta"));
		vbox_kappa.getChildren().add(rangeSliders.get("kappa"));
		
		// Init container for filters.
		filters_vbox = new VBox();
		filters_vbox.resize(100, filters_vbox.getHeight());
		// Iterate over supported parameters.
		for (String param : LDAConfiguration.SUPPORTED_PARAMETERS) {
			// Create new filter.
			ScentedFilter filter = (ScentedFilter) VisualizationComponent.generateInstance(VisualizationComponentType.SCENTED_FILTER, this, this.workspace, this.log_protocol_progressindicator, this.log_protocol_textarea);

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
		
		// Set gridpane's height (based on the number of supported parameters).
		final int prefRowHeight = 100;
		gridpane_parameterConfiguration.setPrefHeight(LDAConfiguration.SUPPORTED_PARAMETERS.length * prefRowHeight);
		gridpane_parameterConfiguration.setMinHeight(LDAConfiguration.SUPPORTED_PARAMETERS.length * prefRowHeight);

	}

	private void initMDSScatterchart()
	{
		mdsScatterchart = new MDSScatterchart(	this, mds_scatterchart, mdsHeatmap_canvas,
												checkbox_mdsHeatmap_distribution_dynAdjustment, slider_mds_distribution_granularity);
	}
	
	private void initDistanceBarchart()
	{
		distancesBarchart = new DistancesBarchart(	this, barchart_distances, numberaxis_distanceEvaluation_yaxis, 
													button_relativeView_distEval, checkbox_logarithmicDistanceBarchart);
	}
	
	private void initParameterSpaceHeatmaps()
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
		
		// Init heatmaps.
		parameterspace_heatmap_selected	= (NumericalHeatmap) VisualizationComponent.generateInstance(VisualizationComponentType.NUMERICAL_HEATMAP, this, null, null, null);
		parameterspace_heatmap_filtered	= (NumericalHeatmap) VisualizationComponent.generateInstance(VisualizationComponentType.NUMERICAL_HEATMAP, this, null, null, null);
		// Embed heatmaps in parent.
		parameterspace_heatmap_filtered.embedIn(paramSpace_distribution_anchorPane_filtered);
		parameterspace_heatmap_selected.embedIn(paramSpace_distribution_anchorPane_selected);
		
		/*
		 * Init option controls.
		 */
		
		// Add listener to determine position during after release.
		slider_parameterSpace_distribution_granularity.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	parameterspace_heatmap_selected.setGranularityInformation(checkbox_parameterSpace_distribution_dynAdjustment.isSelected(), (int) slider_parameterSpace_distribution_granularity.getValue(), true);
            	parameterspace_heatmap_filtered.setGranularityInformation(checkbox_parameterSpace_distribution_dynAdjustment.isSelected(), (int) slider_parameterSpace_distribution_granularity.getValue(), true);
            }
        });
		
		// Add listener to determine position during mouse drag.
		slider_parameterSpace_distribution_granularity.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	parameterspace_heatmap_selected.setGranularityInformation(checkbox_parameterSpace_distribution_dynAdjustment.isSelected(), (int) slider_parameterSpace_distribution_granularity.getValue(), true);
            	parameterspace_heatmap_filtered.setGranularityInformation(checkbox_parameterSpace_distribution_dynAdjustment.isSelected(), (int) slider_parameterSpace_distribution_granularity.getValue(), true);
            };
        });
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
		
		
		// Refresh visualizations.
		
		// 	Parameter filtering controls:
		refreshParameterHistograms(50);

		// 	MDS scatterchart:
		mdsScatterchart.refresh(	dataspace.getCoordinates(),
									dataspace.getFilteredCoordinates(), dataspace.getFilteredIndices(), 
									dataspace.getSelectedCoordinates(), dataspace.getSelectedIndices(), 
									dataspace.getDiscardedCoordinates(), dataspace.getDiscardedIndices());

		//	Distance evaluation barchart:
		distancesBarchart.refresh(	dataspace.getDiscardedIndices(), dataspace.getFilteredIndices(), dataspace.getSelectedIndices(),
									dataspace.getDiscardedDistances(), dataspace.getFilteredDistances(), dataspace.getSelectedFilteredDistances(), 
									true);

		// 	Heatmaps:
		refreshParameterspaceHeatmaps();
		
		//	Local scope:
		localScopeInstance.refresh(dataspace.getSelectedLDAConfigurations());
		// TM comparison heatmap:
		refreshTMCHeatmap(dataspace.getSelectedLDAConfigurations());
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
			mdsScatterchart.identifyGlobalExtrema(dataspace.getCoordinates());

			// Add keyboard listener in order to enable selection.
			addKeyListener();
			
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
	public void integrateMDSSelection(Set<Integer> selectedIndices, boolean includeLocalScope)
	{
		boolean changeDetected = !( selectedIndices.containsAll(dataspace.getSelectedIndices()) && dataspace.getSelectedIndices().containsAll(selectedIndices) );

		// Update set of filtered and selected indices.
		if (changeDetected) {
			dataspace.updateSelectedIndexSet(selectedIndices);
			
			// Find selected and filtered values.
			dataspace.updateSelectedDistanceMatrix();
			dataspace.updateSelectedLDAConfigurations();
			dataspace.updateSelectedCoordinateMatrix();
			dataspace.updateReductiveFilteredDistanceMatrix();
			
			// Refresh other (than MDSScatterchart) visualizations.
			
			//	Distances barchart:
			distancesBarchart.refresh(	dataspace.getDiscardedIndices(), dataspace.getFilteredIndices(), dataspace.getSelectedIndices(),
										dataspace.getDiscardedDistances(), dataspace.getFilteredDistances(), dataspace.getSelectedFilteredDistances(), 
										true);
			
			//	Paramer space heatmaps:
			refreshParameterspaceHeatmaps();
			
			// 	Local scope:
			if (includeLocalScope)
				localScopeInstance.refresh(dataspace.getSelectedLDAConfigurations());
			
			// 	Parameter histograms:
			refreshParameterHistograms(50);
		}
		
		// Even if no change on global scope detected: Update local scope, if requested. 
		else if (includeLocalScope) {
			localScopeInstance.refresh(dataspace.getSelectedLDAConfigurations());
			// Refresh topic model comparison heatmap.
			refreshTMCHeatmap(dataspace.getSelectedLDAConfigurations());
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
	 * Integrates heatmap selection into MDS selection, then fires update for all relevant visualizations.
	 * @param newlySelectedLDAConfigIDs
	 * @param isAddition
	 */
	public void integrateHeatmapSelection(Set<Integer> newlySelectedLDAConfigIDs, final boolean isAddition)
	{
		// Check if there is any change in the set of selected datasets.
		boolean changeDetected = false;
		
		// 1. 	Check which elements are to be added/removed from current selection by comparing 
		// 		with set of filtered and selected datasets.
		
		// 1.a.	Selection should be added:
		if (isAddition) {
			// Check if any of the newly selected IDs are not contained in global selection yet.
			// If so: Add them.
			for (LDAConfiguration selectedLDAConfiguration : dataspace.getSelectedLDAConfigurations()) {
				final int alreadySelectedLDAConfigID = selectedLDAConfiguration.getConfigurationID(); 
				
				// If newly selected set already contained in existing selection: Remove from addition set.
				if (newlySelectedLDAConfigIDs.contains(alreadySelectedLDAConfigID)) {
					newlySelectedLDAConfigIDs.remove(alreadySelectedLDAConfigID);
				}
			}
			
			// If set of newly selected indices still contains elements: Change detected.
			if (newlySelectedLDAConfigIDs.size() > 0) {
				// Update flag.
				changeDetected = true;
				
				// Add missing LDA configurations to collection.
				for (final int ldaConfigIndex : dataspace.getFilteredIndices()) {
					// Get LDA configuration for this index.
					final LDAConfiguration ldaConfiguration = dataspace.getLDAConfigurations().get(ldaConfigIndex);
					
					// Check if this LDA configuration is part of the set of newly selected LDA configurations. 
					if ( newlySelectedLDAConfigIDs.contains(ldaConfiguration.getConfigurationID()) )
						dataspace.getSelectedIndices().add(ldaConfigIndex);
				}
			}
		}
		
		// 1.b.	Selection should be removed:
		else {
			// Set of dataset indices (instead of configuration IDs) to delete.
			Set<Integer> indicesToDeleteFromSelection = new HashSet<Integer>();
			
			// Check if any of the newly selected IDs are contained in global selection.
			// If so: Remove them.
			for (final int ldaConfigIndex : dataspace.getSelectedIndices()) {
				// Get LDA configuration for this index.
				final LDAConfiguration ldaConfiguration = this.dataspace.getLDAConfigurations().get(ldaConfigIndex); 
				
				// If currently examine LDAConfiguration is in set of newly selected indices:
				// Remove LDAConfiguration from set of selected indices.
				if (newlySelectedLDAConfigIDs.contains(ldaConfiguration.getConfigurationID())) {
					// Update flag.
					changeDetected = true;
					
					// Add dataset index to collection of indices to remove from selection.
					indicesToDeleteFromSelection.add(ldaConfigIndex);
				}
			}
			
			// Remove set of indices to delete from set of selected indices.
			dataspace.getSelectedIndices().removeAll(indicesToDeleteFromSelection);
		}
		
		// 2. 	Update related (i.e. dependent on the set of selected entities) datasets and visualization, if there were any changes made.
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
				if (!dataspace.getSelectedIndices().contains( newlySelectedLocalIndices.get(i)) ) {
					// Add to collection of selected indices.
					dataspace.getSelectedIndices().add( newlySelectedLocalIndices.get(i) );
					
					// Change detected.
					changeDetected = true;
				}
			}
		}
		
		else {
			// Translate local indices to global indices.
			for (int i = 0; i < newlySelectedLocalIndices.size(); i++) {
				// Add to set of selected, translated indices. 
				if (dataspace.getSelectedIndices().contains( newlySelectedLocalIndices.get(i)) ) {
					dataspace.getSelectedIndices().remove( newlySelectedLocalIndices.get(i) );
					
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
		// Find selected and filtered values.
		dataspace.updateSelectedDistanceMatrix();
		dataspace.updateSelectedLDAConfigurations();
		dataspace.updateSelectedCoordinateMatrix();
		dataspace.updateReductiveFilteredDistanceMatrix();

		// 4.	Refresh visualizations.
		// 	Distances barchart:
		distancesBarchart.refresh(		dataspace.getDiscardedIndices(), dataspace.getFilteredIndices(), dataspace.getSelectedIndices(),
										dataspace.getDiscardedDistances(), dataspace.getFilteredDistances(), dataspace.getSelectedFilteredDistances(), 
										true);
		// 	MDS scatterchart:
		mdsScatterchart.refresh(		dataspace.getCoordinates(),
										dataspace.getFilteredCoordinates(), dataspace.getFilteredIndices(), 
										dataspace.getSelectedCoordinates(), dataspace.getSelectedIndices(), 
										dataspace.getDiscardedCoordinates(), dataspace.getDiscardedIndices());
		// 	Parameter space heatmap:
		refreshParameterspaceHeatmaps();

		//	Local scope:
		localScopeInstance.refresh(dataspace.getSelectedLDAConfigurations());
		refreshTMCHeatmap(dataspace.getSelectedLDAConfigurations());
		
		// 	Parameter histograms:
		refreshParameterHistograms(50);
	}
	
	/**
	 * Refreshes both heatmaps in parameter space using the current default values.
	 */
	private void refreshParameterspaceHeatmaps()
	{
		// Refresh heatmap using filtered data:
		HeatmapOptionset fOptions 	= new HeatmapOptionset(	checkbox_parameterSpace_distribution_dynAdjustment.isSelected(), (int)slider_parameterSpace_distribution_granularity.getValue(), 
															Color.GREY, Color.GREY, Color.gray(0.5, 0.5), new Color(0.0, 0.0, 1.0, 0.5), 
															combobox_parameterSpace_distribution_xAxis.getValue(), combobox_parameterSpace_distribution_yAxis.getValue(),
															true, button_relativeView_paramDist.isSelected(), true);
		HeatmapDataset fData		= new HeatmapDataset(dataspace.getLDAConfigurations(), dataspace.getFilteredLDAConfigurations(), fOptions);
		parameterspace_heatmap_filtered.refresh(fOptions, fData);

		// Refresh heatmap using selected data:
		HeatmapOptionset sOptions 	= new HeatmapOptionset(	checkbox_parameterSpace_distribution_dynAdjustment.isSelected(), (int)slider_parameterSpace_distribution_granularity.getValue(), 
															Color.LIGHTBLUE, Color.DARKBLUE, Color.gray(0.5, 0.5), new Color(0.0, 0.0, 1.0, 0.5),
															combobox_parameterSpace_distribution_xAxis.getValue(), combobox_parameterSpace_distribution_yAxis.getValue(),
															true, button_relativeView_paramDist.isSelected(), true);
		HeatmapDataset sData		= new HeatmapDataset(dataspace.getLDAConfigurations(), dataspace.getSelectedLDAConfigurations(), fOptions);
		parameterspace_heatmap_selected.refresh(sOptions, sData);
	}
	
	/**
	 * Refresh heatmap for topic model comparison.
	 */
	private void refreshTMCHeatmap(ArrayList<LDAConfiguration> selectedLDAConfigurations)
	{
		// If data sets were selected:
		if (selectedLDAConfigurations.size() > 0) {
			HeatmapOptionset tmcOptions = new HeatmapOptionset(	true, -1, 
																Color.BLUE, Color.DARKBLUE, new Color(0.0, 0.0, 1.0, 0.5), new Color(1.0, 0.0, 0.0, 0.5),
																"", "",
																true, false, true);
			// Instruct heatmap to fetch topic distance data asynchronously.
			tmcHeatmap.fetchTopicDistanceData(selectedLDAConfigurations, tmcOptions);
		}
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
		
		dataspace.filterData();
	};
	
	private void filterIndices()
	{
		// Get parameter boundaries.
		Map<String, Pair<Double, Double>> parameterBoundaries = new HashMap<String, Pair<Double, Double>>();
		
		for (String param : rangeSliders.keySet()) {
			parameterBoundaries.put(param, new Pair<Double, Double>(rangeSliders.get(param).getLowValue(), rangeSliders.get(param).getHighValue()));
		}
		
		// Filter indices.
		dataspace.filterIndices(parameterBoundaries);
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
		
		// Update textfield values.
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

	/**
	 * Refresh parameter filter control histograms.
	 * @param numberOfBins
	 */
	private void refreshParameterHistograms(final int numberOfBins)
	{
		// Map storing one bin list for each parameter, counting only filtered datasets.
		Map<String, int[]> parameterBinLists_filtered	= new HashMap<String, int[]>();
		// Map storing one bin list for each parameter, counting only selected datasets.
		Map<String, int[]> parameterBinLists_selected	= new HashMap<String, int[]>();
		// Map storing one bin list for each parameter, counting only discarded datasets.
		Map<String, int[]> parameterBinLists_discarded	= new HashMap<String, int[]>();
		
		// Add parameters to map of bin lists. 
		for (String supportedParameter : LDAConfiguration.SUPPORTED_PARAMETERS) {
			parameterBinLists_filtered.put(supportedParameter, new int[numberOfBins]);
			parameterBinLists_selected.put(supportedParameter, new int[numberOfBins]);
			parameterBinLists_discarded.put(supportedParameter, new int[numberOfBins]);
		}
		
		// Bin data.
		for (int i = 0; i < dataspace.getLDAConfigurations().size(); i++) {
			// Check if dataset is filtered (as opposed to discarded).
			boolean isFilteredDataset = dataspace.getFilteredIndices().contains(i);
			boolean isSelectedDataset = dataspace.getSelectedIndices().contains(i);
			
			// Evaluate bin counts for this dataset for each parameter.
			for (String param : rangeSliders.keySet()) {
				double binInterval	= (rangeSliders.get(param).getMax() - rangeSliders.get(param).getMin()) / numberOfBins;
				// Calculate index of bin in which to store the current value.
				int index_key		= (int) ( (dataspace.getLDAConfigurations().get(i).getParameter(param) - rangeSliders.get(param).getMin()) / binInterval);
				// Check if element is highest allowed entry.
				index_key			= index_key < numberOfBins ? index_key : numberOfBins - 1;
				
				// Check if this dataset fits all boundaries / is filtered, selected or discarded - then increment content of corresponding bin.
				if (isFilteredDataset) {
					// Filtered dataset:
					if (!isSelectedDataset)
						parameterBinLists_filtered.get(param)[index_key]++;
					// Selected dataset:
					else 
						parameterBinLists_selected.get(param)[index_key]++;
				}
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
			generateParameterHistogramDataSeries(parameterBarchartEntry, parameterBinLists_filtered, numberOfBins, 0);
			
			// Add discarded data series to barcharts.
			parameterBarchartEntry.getValue().getData().add(
					dataspace.generateParameterHistogramDataSeries(parameterBarchartEntry.getKey(), parameterBinLists_discarded, numberOfBins)
			);
			// Color discarded data.
			colorParameterHistogramBarchart(parameterBarchartEntry.getValue(), 1);
			
			// Add selected data series to barcharts.
			parameterBarchartEntry.getValue().getData().add(
					dataspace.generateParameterHistogramDataSeries(parameterBarchartEntry.getKey(), parameterBinLists_selected, numberOfBins)
			);
			// Color selected data.
			colorParameterHistogramBarchart(parameterBarchartEntry.getValue(), 2);
		}
	}
	
	/**
	 * Generates parameter histogram in/for specified barcharts.
	 * @param parameterBarchartEntry
	 * @param paramterBinLists
	 * @param numberOfBins
	 * @param seriesIndex
	 */
	private void generateParameterHistogramDataSeries(	Map.Entry<String, StackedBarChart<String, Integer>> parameterBarchartEntry, 
														Map<String, int[]> paramterBinLists,
														final int numberOfBins, final int seriesIndex)
	{
		// Add data series to barcharts.
		parameterBarchartEntry.getValue().getData().add(
				dataspace.generateParameterHistogramDataSeries(parameterBarchartEntry.getKey(), paramterBinLists, numberOfBins)
		);
		// Color data.
		colorParameterHistogramBarchart(parameterBarchartEntry.getValue(), seriesIndex);
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
				// Selectede data.
				case 2:
					if (node.getUserData() == null || node.getUserData().toString() == "active") {
						node.setUserData("active");
						node.setStyle("-fx-bar-fill: blue;");
					}
				break;
				
				// Discarded data.
				case 1:
					if (node.getUserData() == null || node.getUserData().toString() == "discarded") {
						node.setUserData("discarded");
						node.setStyle("-fx-bar-fill: lightgrey;");
					}
				break;
				
				// Filtered data.
				case 0:
					if (node.getUserData() == null || node.getUserData().toString() == "inactive") {
						node.setUserData("inactive");
						node.setStyle("-fx-bar-fill: darkgrey;");
					}
				break;
			}
		}
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
				// Adapt size.
				parameterspace_heatmap_filtered.resizeContent(width, height);
			break;
			
			case "paramSpace_distribution_anchorPane_selected":
				// Adapt size.
				parameterspace_heatmap_selected.resizeContent(width, height);
			break;
			
			// Resize scatter plot.
			case "mds_anchorPane":
	        	// Update MDS heatmap position/indentation.
				mdsScatterchart.updateHeatmapPosition();
				// Redraw heatmap.
				mdsScatterchart.refreshHeatmapAfterResize();
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
			break;
		}
	}
	
	@FXML
	public void ddcButtonStateChanged(ActionEvent e)
	{
		double filteredDistances[][]							= new double[dataspace.getFilteredIndices().size()][dataspace.getFilteredIndices().size()];
		ArrayList<LDAConfiguration> filteredLDAConfigurations	= new ArrayList<LDAConfiguration>(dataspace.getFilteredIndices().size());
		
		// Copy data corresponding to chosen LDA configurations in new arrays.
		int count = 0;
		for (int selectedIndex : dataspace.getFilteredIndices()) {
			// Copy distances.
			int innerCount = 0;
			for (int selectedInnerIndex : dataspace.getFilteredIndices()) {
				filteredDistances[count][innerCount] = dataspace.getDistances()[selectedIndex][selectedInnerIndex];
				innerCount++;
			}
			
			// Copy LDA configurations.
			filteredLDAConfigurations.add(dataspace.getLDAConfigurations().get(selectedIndex));
			
			count++;
		}
	}
	
	@FXML
	public void updateHeatmap(ActionEvent e)
	{
//		ArrayList<LDAConfiguration> filteredLDAConfigurations = new ArrayList<LDAConfiguration>(dataspace.getFilteredIndices().size());
//		
//		// Copy data corresponding to chosen LDA configurations in new arrays.
//		for (int selectedIndex : dataspace.getFilteredIndices()) {
//			// Copy LDA configurations.
//			filteredLDAConfigurations.add(dataspace.getLDAConfigurations().get(selectedIndex));
//		}
		
		if (	parameterspace_heatmap_filtered != null 			&&
				parameterspace_heatmap_selected != null 			&&
				combobox_parameterSpace_distribution_xAxis != null 	&& 
				combobox_parameterSpace_distribution_yAxis != null) {
			// Refresh heatmaps.
			refreshParameterspaceHeatmaps();
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
		slider_parameterSpace_distribution_granularity.setDisable(checkbox_parameterSpace_distribution_dynAdjustment.isSelected());

		// Propagate information to heatmaps.
		parameterspace_heatmap_filtered.setGranularityInformation(checkbox_parameterSpace_distribution_dynAdjustment.isSelected(), (int) slider_parameterSpace_distribution_granularity.getValue(), true);
		parameterspace_heatmap_selected.setGranularityInformation(checkbox_parameterSpace_distribution_dynAdjustment.isSelected(), (int) slider_parameterSpace_distribution_granularity.getValue(), true);
	}
	
	/**
	 * Processes new information about granularity of MDS heatmap.
	 * @param e
	 */
	@FXML
	public void changeHeatmapGranularityMode(ActionEvent e)
	{
		slider_mds_distribution_granularity.setDisable(checkbox_mdsHeatmap_distribution_dynAdjustment.isSelected());
		mdsScatterchart.setHeatmapGranularityInformation(checkbox_mdsHeatmap_distribution_dynAdjustment.isSelected(), (int) slider_mds_distribution_granularity.getValue(), true);
	}

	/**
	 * Toggles MDS heatmap visibility.
	 * @param e
	 */
	@FXML
	public void changeMDSHeatmapVisibility(ActionEvent e)
	{
		mdsScatterchart.setHeatmapVisiblity(showMSDHeatmap_checkbox.isSelected());
	}
	
	/**
	 * Adjusts minimal and maximal control values so that they fit the loaded data set.
	 */
	private void adjustControlExtrema()
	{
		Map<String, Pair<Double, Double>> ldaParameterExtrema = dataspace.identifyLDAParameterExtrema(dataspace.getLDAConfigurations());
		
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
	
	/**
	 * Adds listener processing keyboard events (like toggling from group to single selection mode).
	 * @param scene
	 */
	public void addKeyListener()
	{
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) 
            {
            	mdsScatterchart.processKeyPressedEvent(ke);
            	distancesBarchart.processKeyPressedEvent(ke);
            	parameterspace_heatmap_filtered.processKeyPressedEvent(ke);
            	parameterspace_heatmap_selected.processKeyPressedEvent(ke);
            	tmcHeatmap.processKeyPressedEvent(ke);
            }
		});
		
		scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) 
            {
            	mdsScatterchart.processKeyReleasedEvent(ke);
            	distancesBarchart.processKeyReleasedEvent(ke);
            	parameterspace_heatmap_filtered.processKeyReleasedEvent(ke);
            	parameterspace_heatmap_selected.processKeyReleasedEvent(ke);
            	tmcHeatmap.processKeyReleasedEvent(ke);
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
			(iconID == "settings_paramDistCorr_icon" 	&& settings_paramDistCorr_icon.getBoundsInParent().contains(x, y))	||
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
			
			case "paramSpace_distribution_anchorPane":
				settings_paramDist_icon.setVisible(true);
			break;
			
			case "localScope_containingAnchorPane":
				settings_localScope_icon.setVisible(true);
			break;
			
			case "paramSpace_correlation_anchorPane":
				settings_paramDistCorr_icon.setVisible(true);
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
			
			case "paramSpace_correlation_anchorPane":
				settings_paramDistCorr_icon.setVisible(false);
			break;			
		}		
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}
	
	/**
	 * Returns extrema for all currently supported parameters.
	 * @return
	 */
	public Map<String, Pair<Double, Double>> getParamExtrema()
	{
		Map<String, Pair<Double, Double>> parameterFilterThresholds = new HashMap<String, Pair<Double, Double>>();
		
		for (Map.Entry<String, RangeSlider> entry : rangeSliders.entrySet()) {
			parameterFilterThresholds.put(entry.getKey(), new Pair<Double, Double>(entry.getValue().getMin(), entry.getValue().getMax()));
		}
		
		return parameterFilterThresholds;
	}
	
	/**
	 * Induces cross-visualization highlighting of one particular LDA configuration.
	 * @param ldaConfigurationID
	 */
	public void induceCrossChartHighlighting(final int ldaConfigurationID)
	{
		// Find index of this LDA configuration (remark: data structure not appropriate for this task).
		int index = -1;
		for (int i : dataspace.getSelectedIndices()) {
			LDAConfiguration ldaConfig = dataspace.getLDAConfigurations().get(i);
			
			if (ldaConfig.getConfigurationID() == ldaConfigurationID)
				index = i;
		}
		
		mdsScatterchart.highlightLDAConfiguration(index);
	}

	/**
	 * Removes all highlighting from charts (used e.g. after user doesn't hover over group/LDA info in CD anymore). 
	 */
	public void removeCrossChartHighlighting()
	{
		mdsScatterchart.highlightLDAConfiguration(-1);
	}
	
	@Override
	public void setReferences(Workspace workspace, ProgressIndicator logPI, TextArea logTA)
	{
		super.setReferences(workspace, logPI, logTA);
		
		// Set references for child elements.
		//	...for TMC heatmap.
		tmcHeatmap.setReferences(workspace, logPI, logTA);
		// ...for parameter space heatmaps. 
		parameterspace_heatmap_filtered.setReferences(workspace, logPI, logTA);
		parameterspace_heatmap_selected.setReferences(workspace, logPI, logTA);
		//	...for local scope instance.
		localScopeInstance.setReferences(workspace, logPI, logTA);
		//	... for scented filter.
		for (ScentedFilter filter : filters) {
			filter.setReferences(workspace, logPI, logTA);
		}
	}
}