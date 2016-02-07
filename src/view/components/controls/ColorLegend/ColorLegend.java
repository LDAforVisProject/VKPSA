package view.components.controls.ColorLegend;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.util.Pair;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
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
	
	/*
	 * Data.
	 */
	
	/**
	 * Dataset holding all relevant data.
	 */
	private ColorLegendDataset data;
	
	/*
	 * Metadata.
	 */
	
	private int legendWidth;
	private int legendHeight;
	
	public ColorLegend()
	{
		System.out.println("Creating ColorLegend.");
		
		// Create root node.
		this.rootNode 	= new AnchorPane();
		
		// Init labels.
		minLabel 		= new Label();
		maxLabel 		= new Label();
		
		// Add labels.
		initLabels();
		
		// Set preferable width for legend.
		legendWidth = 5;
	}
	
	private void initLabels()
	{
		// Add to parent.
		((AnchorPane) rootNode).getChildren().add(minLabel);
		((AnchorPane) rootNode).getChildren().add(maxLabel);
		
		// Set position.
		minLabel.setLayoutX(10);
		maxLabel.setLayoutX(10);
		minLabel.setLayoutY(100);
		
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
		AnchorPane parent 	= (AnchorPane) rootNode;
		this.data 			= data;
		
		// Remove old ImageView from root, if one exists.
		if (legend != null && parent.getChildren().contains(legend)) {
			// Remove legend.
			parent.getChildren().remove(legend);
		}
		
		// Create new legend
		legend = ColorScale.createColorScaleImageView(	data.getMin(), data.getMax(), data.getMinColor(), data.getMaxColor(),
														legendWidth, legendHeight, Orientation.VERTICAL);
		// Add to root.
		parent.getChildren().add(legend);
		
		// Display extrema in lables.
		minLabel.setText( String.valueOf(data.getMin()) );
		maxLabel.setText( String.valueOf(data.getMax()).substring(0, 5));
		
		// Reposition labels.
		minLabel.setLayoutY(legendHeight - 15);
		maxLabel.setLayoutY(5);
//			@todo: Continue with
//						- Integration into all (other) components
//						- Interactivity
//						- Resizing
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

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}
  
}
