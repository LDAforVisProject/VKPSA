package view.components.scentedFilter.scentedKeywordFilter;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXML;
import model.LDAConfiguration;
import view.components.DatapointIDMode;
import view.components.VisualizationComponentType;
import view.components.scentedFilter.ScentedFilter;
import view.components.scentedFilter.ScentedFilterDataset;
import view.components.scentedFilter.ScentedFilterOptionset;


/**
 * Scented filter for keywords.
 * Uses a slightly different UI, the functionality is equal. 
 * @author RM
 *
 */
public class ScentedKeywordFilter extends ScentedFilter
{
	/**
	 * Storage for keywords in use (no keyword should be used more than once)
	 */
	static private Set<String> keywordsInUse = new HashSet<String>();
	
	/**
	 * Keyword used for this Filter.
	 */
	private String keyword;
	
	/**
	 * Button for closing filter.
	 */
	private @FXML ImageView closeButton_imageview;
	
	/**
	 * Gap between adjacent keyword filters. 
	 */
	static public final int GAP = 24;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		super.initialize(location, resources);
	
		// Initialize close button.
		initCloseButton();
	}
	
	/**
	 * Initializes button for closing this keyword filter.
	 */
	private void initCloseButton()
	{
		// Use reference to this instance of ScentedKeywordFilter.
		ScentedKeywordFilter autoReference = this;
		
		// Load image for closing button.
		closeButton_imageview.setImage(new Image(getClass().getResourceAsStream("/icons/close-icon.png"), 25, 25, true, true));
		
		// Mouse entered: Change cursor type.
		closeButton_imageview.addEventHandler(MouseEvent.MOUSE_ENTERED, (new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) 
            {
            	analysisController.getScene().setCursor(Cursor.HAND);
            }
		}));
		// Mouse exited: Change cursor type.
		closeButton_imageview.addEventHandler(MouseEvent.MOUSE_EXITED, (new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) 
            {
            	analysisController.getScene().setCursor(Cursor.DEFAULT);
            }
		}));
		// Mouse clicked: Show setup for new filter.
		closeButton_imageview.addEventHandler(MouseEvent.MOUSE_PRESSED, (new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) 
            {
            	// Remove keyword from set of used keywords.
            	ScentedKeywordFilter.keywordsInUse.remove(autoReference.getComponentIdentification());
            	// Remove keyword filter from UI, de-allocate resources.
            	analysisController.removeKeywordFilter(autoReference);
            }
		}));
	}

	@Override
	public void refresh(ScentedFilterDataset data)
	{
		// Common ScentedFilterDataset instead of ScentedKeywordFilterDataset provided: Show error message, return.		
		System.out.println("### ERROR ### ScentedKeywordFilterDataset needed for ScentedKeywordFilter. ScentedFilterDataset provided instead.");
		log("### ERROR ### ScentedKeywordFilterDataset needed for ScentedKeywordFilter. ScentedFilterDataset provided instead.");
		
		return;
	}
	
	/**
	 * Refreshes chart using only updated LDA index associations, but without fetching KeywordRankObjects for this keyword again. 
	 * @param inactiveIndices
	 * @param activeIndices
	 */
	public void refresh(Set<Integer> inactiveIndices, Set<Integer> activeIndices)
	{
		// Update index sets.
		data.setInactiveIndices(inactiveIndices);
		data.setActiveIndices(activeIndices);
		
		System.out.println("refreshing skf");
		// Refresh chart.
		refresh((ScentedKeywordFilterDataset)data);
	}
	
	/**
	 * Refreshes ScentedKeywordFilter.
	 * @param data
	 */
	public void refresh(ScentedKeywordFilterDataset data)
	{
		this.data = data;
		
		/*
		 * Bin data.
		 */
		
		ArrayList<double[]> binnedData = data.binData(	options.getParamID(), options.getNumberOfBins(),
														options.useRangeSlider() ? rangeSlider.getMin() : slider.getMin(),
														options.useRangeSlider() ? rangeSlider.getMax() : slider.getMax());
		
		/*
		 * Adjust controls to new extrema.
		 */
		if (!isAdjustedToExtrema) {
			adjustControlExtrema(data);
			isAdjustedToExtrema = true;
		}
			
		
		/*
		 * Draw data.
		 */
		
		// Clear old data.
		barchart.getData().clear();
		selectedBars.clear();

		// Add active data series to barcharts.
		activeDataSeries	= addParameterHistogramDataSeries(binnedData.get(1), options.getNumberOfBins(), 0);
		// Add inactive data series to barcharts.
		inactiveDataSeries	= addParameterHistogramDataSeries(binnedData.get(0), options.getNumberOfBins(), 1);
		// Add discarded data series to barcharts.
		discardedDataSeries = addParameterHistogramDataSeries(binnedData.get(2), options.getNumberOfBins(), 2);
		
		// Add hover event listeners.
		initHoverEventListeners();
		
		// Lower opacity for all data points.
		removeHoverHighlighting();
	}
	
	@Override
	public void adjustControlExtrema(ArrayList<LDAConfiguration> ldaConfigurations)
	{
		// Common ScentedFilterDataset instead of ScentedKeywordFilterDataset provided: Show error message, return.		
		System.out.println("### ERROR ### ScentedKeywordFilter.adjustControlExtrema needs to be provided with an instance of ScentedKeywordFilterDataset.");
		log("### ERROR ### ScentedKeywordFilter.adjustControlExtrema needs to be provided with an instance of ScentedKeywordFilterDataset.");
		
		return;
	}
	/**
	 * Adjusts control extrma for scented keyword filter.
	 * @param data
	 */
	public void adjustControlExtrema(ScentedKeywordFilterDataset data)
	{
		if (options != null) {
			/* 
			 * Update values of slider. 
			 */
			
			double min		= data.getMin();
			double max		= data.getMax();
			
			// Set range slider values.
			if (options.useRangeSlider()) {
				rangeSlider.setMin(min);
				rangeSlider.setMax(max);
			}
			// Set single slider values.
			else {
				slider.setMin(min);
				slider.setMax(max);
			}
			
			// Adjust major tick unit.
			rangeSlider.setMajorTickUnit( (max - min) / (options.getMajorTickCount() - 1));
			
			// Update text values.
			min_spinner.setNumber(new BigDecimal(min));
			max_spinner.setNumber(new BigDecimal(max));
		}
		
		isAdjustedToExtrema = true;
	}
	
	@Override
	protected void addHoverEventListenersToNode(XYChart.Data<String, Number> bar)
	{
		bar.getNode().setOnMouseEntered(new EventHandler<MouseEvent>()
		{
		    @Override
		    public void handle(MouseEvent event)
		    {   
		    	// Collect indices of data points in this bar.
		    	Set<Integer> ldaConfigIDs = new HashSet<Integer>();
		    	ldaConfigIDs.addAll(data.getBarToDataAssociations().get("discarded_" + bar.getXValue()));
		    	ldaConfigIDs.addAll(data.getBarToDataAssociations().get("inactive_" + bar.getXValue()));
		    	ldaConfigIDs.addAll(data.getBarToDataAssociations().get("active_" + bar.getXValue()));
		    	
//		    	@todo next: 
//		    			- cross.-vis. highlighting with topic-level granularity.
//		    			- fix remaining issues, improve handling.
//		    	after finishing dynamic keyword filters:
//		    			see roadmap/feature requests/paper deadlines discussed with TMö on 2016-04-20.
		    	
		    	// Highlight bar.
		    	for (XYChart.Series<String, Number> series : barchart.getData()) {
		    		for (XYChart.Data<String, Number> tmpBar : series.getData()) {
		    			if (tmpBar.getXValue().equals(bar.getXValue())) {
		    				tmpBar.getNode().setOpacity(1);
		    				break;
		    			}
		    		}
		    	}
		    	
	        	// Notify AnalysisController about hover action.
	        	analysisController.highlightDataPoints(ldaConfigIDs, 
	        											DatapointIDMode.CONFIG_ID, 
	        											VisualizationComponentType.SCENTED_KEYWORD_FILTER, 
	        											((ScentedKeywordFilterDataset)data).getKeyword());
		    }
		});
		
		bar.getNode().setOnMouseExited(new EventHandler<MouseEvent>()
		{
		    @Override
		    public void handle(MouseEvent event)
		    {       
		    	// Remove highlighting.
	        	removeHoverHighlighting();
	        	
	        	// Notify AnalysisController about end of hover action.
	        	analysisController.removeHighlighting(VisualizationComponentType.SCENTED_FILTER);
		    }
		});
	}
	
	@Override
	public void highlightHoveredOverDataPoints(Set<Integer> dataPointIDs, DatapointIDMode idMode)
	{
		if (idMode == DatapointIDMode.CONFIG_ID) {
			Set<String> barsToHighlight = new HashSet<String>();
			
			// Find bars to highlight.
			for (int i = 0; i < options.getNumberOfBins(); i++) {
				// Search in data available for this bar.
				if (isBarToHighlight(i, "discarded_", dataPointIDs) ||
					isBarToHighlight(i, "inactive_", dataPointIDs) 	||
					isBarToHighlight(i, "active_", dataPointIDs)) {
					barsToHighlight.add(Integer.toString(i));
				}
			}
		
			// Highlight bars.
			if (barchart != null) {
				for (XYChart.Series<String, Number> dataSeries : barchart.getData()) {
					for (XYChart.Data<String, Number> bar : dataSeries.getData()) {
						if (barsToHighlight.contains(bar.getXValue()))
							bar.getNode().setOpacity(1);
					}
				}
			}
		}
		
		else {
			System.out.println("### WARNING ### DatapointIDMode.INDEX not supported for ScentedKeywordFilter.");
			log("### WARNING ### DatapointIDMode.INDEX not supported for ScentedKeywordFilter.");
		}
	}
	
	@Override
	public String getComponentIdentification()
	{
		return ((ScentedKeywordFilterDataset)data).getKeyword();
	}
	
	@Override
	public void applyOptions(ScentedFilterOptionset options)
	{
		super.applyOptions(options);
		// Update label ID.
		param_label.setText( options.getParamID() );
	}
	
	/**
	 * Adds keyword to set of used keywords. 
	 * @param keyword
	 * @return False if keyword already exists in list. True if keyword was added successfully.
	 */
	public static boolean addToSetOfUsedKeywords(String keyword)
	{
		return ScentedKeywordFilter.keywordsInUse.add(keyword);
	}
	
	/**
	 * Get number of used keywords.
	 * @return
	 */
	public static int getKeywordCount()
	{
		return ScentedKeywordFilter.keywordsInUse.size();
	}
}
