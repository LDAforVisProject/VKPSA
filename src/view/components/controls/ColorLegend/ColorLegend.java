package view.components.controls.ColorLegend;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.controlsfx.control.RangeSlider;

import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import model.workspace.TaskType;
import model.workspace.tasks.ITaskListener;
import view.components.DatapointIDMode;
import view.components.VisualizationComponent;

/**
 * Displays color scale for given range of values and selected colors.
 * Is embedded in other components.
 * @author RM
 *
 */
public class ColorLegend extends VisualizationComponent
{
	/*
	 * GUI elements.
	 * 
	 */
	
	/**
	 * Image of color legend.
	 */
	private ImageView legend;
	
	/**
	 * Label for min. value.
	 */
	private Label minLabel;
	/**
	 * Label for max. value.
	 */
	private Label maxLabel;
	
	/**
	 * RangeSlider for manually defined cutoff.
	 */
	private RangeSlider slider;
	
	/**
	 * Rectangle representing border of actual legend.
	 */
	private Rectangle legendBorder;
	
	/*
	 * Data.
	 */
	
	/**
	 * Dataset holding all relevant data.
	 */
	private ColorLegendDataset data;
	
	/**
	 * Listener to notify once slider value has changed.
	 */
	private ITaskListener listener;
	
	/*
	 * Metadata.
	 */
	
	private int legendWidth;
	private int legendHeight;
	private int legendOffsetX;
	
	public ColorLegend(ITaskListener listener)
	{
		System.out.println("Creating ColorLegend.");
		
		// Remember listener.
		this.listener = listener;

		// Initialize root node.
		initRootNode();
		
		// Initialize labels.
		initLabels();
		
		// Initialize range slider.
		initRangeSlider();
		
		// Initialize legend border shape.
		initLegendBorder();
		
		// Set preferable width for legend.
		legendWidth		= 7;
		legendOffsetX 	= 3;
	}
	
	private void initLegendBorder()
	{
		legendBorder = new Rectangle();
		legendBorder.setStroke(Color.GREY);
		legendBorder.setFill(Color.TRANSPARENT);
	}

	private void initRootNode()
	{
		// Create root node.
		this.rootNode = new AnchorPane();
	}
	
	private void initRangeSlider()
	{
		slider = new RangeSlider();

		slider.setMajorTickUnit(5);
		slider.setMinorTickCount(0);
		
		slider.setSnapToTicks(false);
		slider.setShowTickLabels(false);
		slider.setShowTickMarks(false);
		
		slider.setOrientation(Orientation.VERTICAL);
				
		// Initialize event handler.
		initSliderEventHandler();
	}
	
	/**
	 * Initializes event handler for slider.
	 */
	private void initSliderEventHandler()
	{
		// Add listener to refresh during drag.
		slider.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	refresh();
            }
        });
		
		// Add listener to notify listener after release.
		slider.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	listener.notifyOfTaskCompleted(TaskType.COLOR_LEGEND_MODIFIED);
            }
        });
	}
	
	private void initLabels()
	{
		minLabel 		= new Label();
		maxLabel 		= new Label();
		
		// Add to parent.
		((AnchorPane) rootNode).getChildren().add(minLabel);
		((AnchorPane) rootNode).getChildren().add(maxLabel);
		
		// Set position.
		minLabel.setLayoutX(20);
		maxLabel.setLayoutX(20);
		
		// Set style.
		minLabel.setFont(Font.font("Verdana", FontPosture.ITALIC, 9));
		maxLabel.setFont(Font.font("Verdana", FontPosture.ITALIC, 9));
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
	}
	
	@Override
	public void processSelectionManipulationRequest(double minX, double minY, double maxX, double maxY)
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

	/**
	 * Refresh legend using the provided colours and values.
	 * @param data
	 */
	public void refresh(ColorLegendDataset data)
	{
		this.data = data;
		
		// Update legend.
		updateLegend();
		
		// Update labels.
		updateLabels();
		
		// Update legend border.
		updateLegendBorder();
		
		// Update slider.
		updateSlider();
		
//			@todo: Continue with
//						- Integration into all (other) components
//						- Interactivity
	}
	
	/**
	 * Update legend after refresh.
	 */
	private void updateLegend()
	{
		AnchorPane parent = (AnchorPane) rootNode;
		
		// Remove old ImageView from root, if one exists.
		if (legend != null && parent.getChildren().contains(legend)) {
			// Remove legend.
			parent.getChildren().remove(legend);
		}
		
		// Calculate percentage of legend selected with slider.
		double percentageSelected 	= (slider.getHighValue() - slider.getLowValue()) 	/ (slider.getMax() - slider.getMin());
		double legendOffsetY		= (slider.getMax() - slider.getHighValue()) 		/ (slider.getMax() - slider.getMin()) * legendHeight;
		
		// Create new legend
		legend = ColorScale.createColorScaleImageView(	data.getMin(), data.getMax(), data.getMinColor(), data.getMaxColor(),
														legendWidth, (int)(legendHeight * percentageSelected), Orientation.VERTICAL);
		legend.setLayoutX(legendOffsetX);
		legend.setLayoutY(legendOffsetY);
		
		// Add to root.
		parent.getChildren().add(legend);
	}
	
	/**
	 * Update slider after refresh.
	 */
	private void updateSlider()
	{
		if ( !((AnchorPane) rootNode).getChildren().contains(slider) ) {
			// Add to parent.
			((AnchorPane) rootNode).getChildren().add(slider);
		}
		
		// Set new values.
		slider.setMax(data.getMax());
		slider.setMin(data.getMin());
		
		// Resize.
		slider.setPrefHeight(legendHeight);
		
		// Push to front.
		slider.toFront();
	}
	
	/**
	 * Update labels after refresh.
	 */
	private void updateLabels()
	{
		// Display extrema in lables.
		minLabel.setText( String.valueOf(data.getMin()));
		maxLabel.setText( String.valueOf(data.getMax()).substring(0, 5));
		
		// Reposition labels.
		minLabel.setLayoutY(legendHeight - 15);
		maxLabel.setLayoutY(5);	
	}
	
	@Override
	public void refresh()
	{
		if (this.data != null)
			refresh(this.data);
	}

	@Override
	public void initHoverEventListeners()
	{	
	}

	@Override
	public void highlightHoveredOverDataPoints(Set<Integer> dataPointIDs, DatapointIDMode idMode)
	{	
	}

	@Override
	public void removeHoverHighlighting()
	{	
	}

	@Override
	public void resizeContent(double width, double height)
	{
		// Resize root node/container.
		((AnchorPane)this.rootNode).setPrefSize(width, height);
		
		// Remember new height.
		legendHeight = height > 0 ? (int) height : legendHeight;
		
		// Redraw legend.
		refresh();
	}

	private void updateLegendBorder()
	{
		if ( !((AnchorPane) rootNode).getChildren().contains(legendBorder) ) {
			// Add to parent.
			((AnchorPane) rootNode).getChildren().add(legendBorder);
		}
		
		legendBorder.setLayoutX(legendOffsetX);
		
		legendBorder.setWidth(legendWidth);
		legendBorder.setHeight(legendHeight);
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}
	
	/**
	 * Provide selected extrema.
	 * @return
	 */
	public Pair<Double, Double> getSelectedExtrema()
	{
		return new Pair<Double, Double>(slider.getLowValue(), slider.getHighValue());
	}
  
}
