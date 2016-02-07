package view.components.controls.ColorLegend;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.geometry.Orientation;
import javafx.scene.input.KeyEvent;
import javafx.util.Pair;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
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
	/**
	 * Image of color legend.
	 */
	private ImageView legend;
	
	/**
	 * Dataset holding all relevant data.
	 */
	private ColorLegendDataset data;
	
	public ColorLegend()
	{
		System.out.println("Creating ColorLegend.");
		
		// Create root node.
		this.rootNode = new AnchorPane();	
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
		
		// If new dataset differs from old one: Redraw.
		if (this.data == null || !this.data.equals(data)) {
			this.data  = data;
			
			// Remove old ImageView from root, if one exists.
			if (legend != null && parent.getChildren().contains(legend))
				parent.getChildren().remove(legend);
			
			// Create new legend
			legend = ColorScale.createColorScaleImageView(	data.getMin(), data.getMax(), data.getMinColor(), data.getMaxColor(),
															20, 100, Orientation.VERTICAL);
			// Add to root.
			parent.getChildren().add(legend);
			
			@todo: Continue with
						- Labelling for legend
						- Integration into all (other) components
						- Interactivity
						- Resizing
		}
	}
	
	@Override
	public void refresh()
	{
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
		// @todo Implenent ColorLegend.resizeContent().	
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}
  
}
