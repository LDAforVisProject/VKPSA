package view.components.scatterchart;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

import model.LDAConfiguration;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyEvent;
import javafx.util.Pair;
import view.components.VisualizationComponent;

public class ParameterSpaceScatterchart extends VisualizationComponent
{
	/*
	 * GUI elements.
	 */
	
	private @FXML ComboBox<String> paramX_combobox;
	private @FXML ComboBox<String> paramY_combobox;
	
	private @FXML ScatterChart<Number, Number> scatterchart;
	private @FXML NumberAxis xAxis_numberaxis;
	private @FXML NumberAxis yAxis_numberaxis;
	

	// -----------------------------------------------
	//				Methods
	// -----------------------------------------------
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing ParameterSpaceScatterchart component.");
		
		// Initialize comboboxes.
		paramX_combobox.getItems().addAll(LDAConfiguration.SUPPORTED_PARAMETERS);
		paramY_combobox.getItems().addAll(LDAConfiguration.SUPPORTED_PARAMETERS);
	}
	
	@Override
	public void processSelectionManipulationRequest(double minX, double minY, double maxX, double maxY)
	{
		// @todo Auto-generated method stub
		
	}

	@Override
	public void processEndOfSelectionManipulation()
	{
		// @todo Auto-generated method stub
		
	}

	@Override
	public Pair<Integer, Integer> provideOffsets()
	{
		// @todo Auto-generated method stub
		return null;
	}

	@Override
	public void processKeyPressedEvent(KeyEvent ke)
	{
		// @todo Auto-generated method stub
		
	}

	@Override
	public void processKeyReleasedEvent(KeyEvent ke)
	{
		// @todo Auto-generated method stub
		
	}

	@Override
	public void refresh()
	{
		// @todo Auto-generated method stub
		
	}

	@Override
	public void resizeContent(double width, double height)
	{
		if (paramX_combobox != null && paramY_combobox != null && xAxis_numberaxis != null && yAxis_numberaxis != null) {
			if (width > 0)
				paramX_combobox.setLayoutX(100 + (width - 123) / 2 - paramX_combobox.getWidth() / 2);
			if (height > 0)
				paramY_combobox.setLayoutY(25 + (height - 94) / 2 - paramY_combobox.getHeight() / 2);
		}
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}

}
