package view.components.settingsPopup;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import view.components.VisualizationComponent;

public class SettingsPanel extends VisualizationComponent
{
	// -----------------------------------------------------------
	//					Properties
	// -----------------------------------------------------------
		
	
	/*
	 * Accordion and other elements for options section.
	 */
	
	/**
	 * Accordion.
	 */
	private @FXML Accordion accordion_options;
	
	// Panes in accordion.
	/**
	 * Accordion pane for global scatterchart and distance evaluation.
	 */
	private @FXML TitledPane mdsDistEval_titledPane;
	/**
	 * Accordion pane for local scope visualization.
	 */
	private @FXML TitledPane localScope_titledPane;
	/**
	 * Accordion pane for parameter space scatterchart.
	 */
	private @FXML TitledPane paramSpace_titledPane;
	
	/*
	 *  Controls for density heatmap in global scatterchart.
	 */
	
	/**
	 * Checkbox for heatmap.
	 */
	private @FXML CheckBox globalScatterchartDHM_granularity_checkbox;
	/**
	 * Slider specifying MDS heatmap granularity.
	 */
	private @FXML Slider globalScatterchartDHM_granularity_slider;
	/**
	 * Specifies whether or not the MDS heatmap is to be shown.
	 */
	private @FXML CheckBox globalScatterplot_DHM_visibility_checkbox;
	/**
	 * Color chooser for lower end of heatmap values.
	 */
	private @FXML ColorPicker mdsHeatmap_dhmColor_min_colChooser;
	/**
	 * Color chooser for upper end of heatmap values.
	 */
	private @FXML ColorPicker mdsHeatmap_dhmColor_max_colChooser;
	
	/*
	 * Constrols for distances barchart.
	 */
	
	/**
	 * Indicates whether logarithmic axis should be used. 
	 */
	private @FXML CheckBox distanceBarchart_logarithmicScaling_checkbox;
	
	/*
	 * Controls for density heatmap in parameter space scatterchart.
	 */
	
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
	 * Controls for topic model comparison heatmap.
	 */
	
	/**
	 * Color chooser for lower end of heatmap values.
	 */
	private @FXML ColorPicker tmc_color_min_colChooser;
	/**
	 * Color chooser for lower end of heatmap values.
	 */
	private @FXML ColorPicker tmc_color_max_colChooser;
	
	/*
	 * Options for local scope visualization.
	 */
	
	/**
	 * Denotes number of topics to show in visualization - in slider.
	 * @deprecated
	 */
	private @FXML Slider slider_localScope_numTopicsToUse;
	/**
	 * Denotes number of topics to show in visualization - in textfield.
	 * @deprecated
	 */
	private @FXML TextField textfield_localScope_numTopicsToUse;
	/**
	 * Denotes number of keywords to show in visualization - in slider.
	 */
	private @FXML Slider slider_localScope_numKeywordsToUse;
	/**
	 * Denotes number of keywords to show in visualization - in textfield.
	 */
	private @FXML TextField textfield_localScope_numKeywordsToUse;
	
	
	
	// -----------------------------------------------------------
	//					Methods
	// -----------------------------------------------------------
	
	/**
	 * Initializes settings panel.
	 */
	public void init()
	{
		globalScatterchartDHM_granularity_slider.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	analysisController.changeGlobalScatterplotDHMGranularity(globalScatterchartDHM_granularity_checkbox.isSelected(), (int)globalScatterchartDHM_granularity_slider.getValue());
            }
        });
		
		// Add listener to determine position during mouse drag.
		globalScatterchartDHM_granularity_slider.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	analysisController.changeGlobalScatterplotDHMGranularity(globalScatterchartDHM_granularity_checkbox.isSelected(), (int)globalScatterchartDHM_granularity_slider.getValue());
            };
        });
	}
	
	@Override
	public void processSelectionManipulationRequest(double minX, double minY,	double maxX, double maxY)
	{	
	}

	@Override
	public void processEndOfSelectionManipulation()
	{
	}

	@Override
	public Pair<Integer, Integer> provideOffsets()
	{
		return null;
	}

	@Override
	public void processKeyPressedEvent(KeyEvent ke)
	{	
	}

	@Override
	public void processKeyReleasedEvent(KeyEvent ke)
	{	
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing SettingsPopup.");
	}

	@Override
	public void refresh()
	{
		// @todo Auto-generated method stub
		
	}

	@Override
	public void resizeContent(double width, double height)
	{
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}
	
	/*
	 * From here: Listener.
	 */

	@FXML
	public void changeDBCScalingType(ActionEvent e)
	{
		analysisController.changeDBCScalingType(distanceBarchart_logarithmicScaling_checkbox.isSelected());
	}
	
	/**
	 * Toggles global scatterplot's heatmap visibility.
	 * @param e
	 */
	@FXML
	public void changeGlobalScatterplotDHMVisibility(ActionEvent e)
	{
		analysisController.changeGlobalScatterplotDHMVisibility(globalScatterplot_DHM_visibility_checkbox.isSelected());
	}
	
	/**
	 * Processes new information about granularity of global scatterchart's density heatmap.
	 * @param e
	 */
	@FXML
	public void changeGlobalScatterplotDHMGranularityMode(ActionEvent e)
	{
		globalScatterchartDHM_granularity_slider.setDisable(globalScatterchartDHM_granularity_checkbox.isSelected());
		analysisController.changeGlobalScatterplotDHMGranularity(globalScatterchartDHM_granularity_checkbox.isSelected(), (int)globalScatterchartDHM_granularity_slider.getValue());
	}
	
	/**
	 * Update color in global scatterchart density heatmap.
	 * @param e
	 */	
	@FXML
	public void updateGlobalScatterchartDHMColorSpectrum(ActionEvent e)
	{
		// @todo Implement settings listener.
	}
	
	/**
	 * Processes new information about granularity of parameter space scatterchart heatmap.
	 * @param e
	 */
	@FXML
	public void changePSHeatmapGranularityMode(ActionEvent e)
	{
		// @todo Implement settings listener.
	}
	
	/**
	 * Changes visibility of heatmap in parameter space scatterchart.
	 * @param e
	 */
	@FXML
	public void changePSHeatmapVisibility(ActionEvent e)
	{
		// @todo Implement settings listener.
	}
	
	/**
	 * Updates data categories used in parameter space scatterchart.
	 * @param e
	 */
	@FXML
	public void updateParamSpaceDHMCategories(ActionEvent e)
	{
		// @todo Implement settings listener.
	}
	
	/**
	 * Update color in parameter space scatterchart density heatmap.
	 * @param e
	 */		
	@FXML
	public void updatePSScatterchartDHMColorSpectrum(ActionEvent e)
	{
		//  @todo Implement settings listener.
	}
	
	/**
	 * Update color in TMC heatmap.
	 * @param e
	 */
	@FXML
	public void updateTMCHeatmapColorSpectrum(ActionEvent e)
	{
		//  @todo Implement settings listener.
	}
	
	/**
	 * Called when user opens a settings pane directly.
	 * @param e
	 */
	@FXML
	public void selectSettingsPane(MouseEvent e) 
	{
		//  @todo Implement settings listener.
	}
	
	/**
	 * Opens pane in settings panel.
	 * @param paneID
	 */
	public void openSettingsPane(String paneID)
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
		mdsDistEval_titledPane.lookup(".title").setStyle("-fx-font-weight:normal");
		paramSpace_titledPane.lookup(".title").setStyle("-fx-font-weight:normal");
		paramSpace_titledPane.lookup(".title").setStyle("-fx-font-weight:normal");
		localScope_titledPane.lookup(".title").setStyle("-fx-font-weight:normal");
	}

	/*
	 * Getter and setter.
	 */
	
	public boolean isDBCLogarithmicallyScaled()
	{
		return distanceBarchart_logarithmicScaling_checkbox.isSelected(); 
	}
}
