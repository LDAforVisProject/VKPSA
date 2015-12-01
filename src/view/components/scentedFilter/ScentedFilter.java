package view.components.scentedFilter;

import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.controlsfx.control.RangeSlider;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.util.Pair;
import view.components.VisualizationComponent;

public class ScentedFilter extends VisualizationComponent
{
	/*
	 * GUI elements.
	 */
	
	private @FXML Label param_label;
	private @FXML TextField min_textfield;
	private @FXML TextField max_textfield;
	
	/**
	 * Barchart displaying histogram.
	 */
	private @FXML StackedBarChart barchart;
	
	/**
	 * Range slider for value interval control.
	 */
	private RangeSlider rangeSlider;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing ScentedFilter component.");
		
		// Init metadata collections.
//		cellsToCoordinates					= new LinkedHashMap<Pair<Integer, Integer>, double[]>();
//		selectedCellsCoordinates			= new HashSet<Pair<Integer,Integer>>();
//		
//		// Init axis settings.
//		initAxes();
//		
//		// Init selection tools.
//		initSelection();
	}
	

	@Override
	public void processSelectionManipulationRequest(double minX, double minY,
			double maxX, double maxY)
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
	protected Map<String, Integer> prepareOptionSet()
	{
		// @todo Auto-generated method stub
		return null;
	}


	@Override
	public void resizeContent(double width, double height)
	{
		//System.out.println("new width = " + width + " vs. root.width = " + );
		System.out.println("resizing filter");
	}

}
