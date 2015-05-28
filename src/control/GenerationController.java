 package control;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.controlsfx.control.RangeSlider;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class GenerationController extends Controller
{
	@FXML
	private GridPane parameterConfiguration_gridPane;
	
	@FXML
	private BarChart<String, Float> alpha_barchart;
	@FXML
	private BarChart<String, Float> eta_barchart;
	@FXML
	private BarChart<String, Float> kappa_barchart;
	
	@FXML
	private TextField alpha_min_textfield;
	@FXML
	private TextField alpha_max_textfield;
	@FXML
	private TextField eta_min_textfield;
	@FXML
	private TextField eta_max_textfield;
	@FXML
	private TextField kappa_min_textfield;
	@FXML
	private TextField kappa_max_textfield;
	
	@FXML
	private TextField numberOfDatasets_textfield;
	
	@FXML
	private Button parameterCoupling_button;
	
	@FXML
	private ComboBox<String> sampling_combobox;

	private Map<String, RangeSlider> rangeSliders;
	private Map<String, VBox> vBoxes;
	
	private int numberOfDatasetsToGenerate;
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing SII_GenerationController.");
		
		//spinners		= new HashMap<String, Spinner<Float>>();
		rangeSliders	= new HashMap<String, RangeSlider>();
		vBoxes			= new HashMap<String, VBox>();
		
		try {
			numberOfDatasetsToGenerate = Integer.parseInt(numberOfDatasets_textfield.getText());
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		// Init UI elements.
		initUIElements();
	}
	
	/**
	 * Updates UI as created with SceneBuilder with better fitting controls.
	 * @return
	 */
	private void initUIElements() 
	{
		// Remove deprecated UI elements in parameter configuration grid pane.
		int vboxCount = 0;
		for (Object child : parameterConfiguration_gridPane.getChildren().toArray()) {
			Node node = (Node)child;
			
			//System.out.println(node.getClass().getName());
			switch (node.getClass().getName()) 
			{
	    		// Replace TextFields with numeric steppers / spinners.
	    		case "javafx.scene.control.TextField":
	    			// Set initial values.
	    			if (node.getId().contains("min"))
	    				((TextField)node).setText("0");
	    			else
	    				((TextField)node).setText("100");
	    		break;
	    		
	    		case "javafx.scene.layout.VBox":
	    			VBox currentVBox = ((VBox)node);
	    			// Look for Sliders in VBox nodes; replace with RangeSliders.
	    			for (Object vboxChild : currentVBox.getChildren().toArray()) {
	    				Node vboxNode = (Node)vboxChild;
	    				
	    				if (vboxNode.getClass().getName() == "javafx.scene.control.Slider") {
	    					currentVBox.getChildren().remove(vboxNode);
	    				}
	    			}
	    			
	    			// Store references to VBoxes.
	    			switch(vboxCount++)
	    			{
	    				case 0:
	    					vBoxes.put("alpha", currentVBox);
	    				break;
	    				
	    				case 1:
	    					vBoxes.put("eta", currentVBox);
	    				break;
	    				
	    				case 2:
	    					vBoxes.put("kappa", currentVBox);
	    				break;
	    			}
	    		break;
	    	}
	    }
		
		// Init range sliders.
		rangeSliders.put("alpha", new RangeSlider());
		rangeSliders.put("eta", new RangeSlider());
		rangeSliders.put("kappa", new RangeSlider());
		
		for (Map.Entry<String, RangeSlider> entry : rangeSliders.entrySet()) {
			RangeSlider rs = entry.getValue();
			
			rs.setMaxWidth(360);
			rs.setMax(100);
			rs.setMajorTickUnit(5);
			rs.setSnapToTicks(false);
			rs.setShowTickMarks(true);
			rs.setLowValue(0);
			rs.setHighValue(100);
			
			addEventHandlerToRangeSlider(rs, entry.getKey());
		}
		
		// Init combobox.
		sampling_combobox.getItems().clear();
		sampling_combobox.getItems().add("Random");
		sampling_combobox.getItems().add("Cartesic");
		sampling_combobox.getItems().add("Hypercube");
		
		// Add to respective parents.
		vBoxes.get("alpha").getChildren().add(rangeSliders.get("alpha"));
		vBoxes.get("eta").getChildren().add(rangeSliders.get("eta"));
		vBoxes.get("kappa").getChildren().add(rangeSliders.get("kappa"));
	}

	private void addEventHandlerToRangeSlider(RangeSlider rs, String parameter)
	{
		// Add listener to determine position during after release.
		rs.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	updateTextFields(rs, parameter);
            	// Re-generate parameter values.
            	generateParameterValues();
            };
        });
		
		// Add listener to determine position during mouse drag.
		rs.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	updateTextFields(rs, parameter);
            };
        });
	}
	
	private void updateTextFields(RangeSlider rs, String parameter)
	{
    	if (!parameterCoupling_button.isDefaultButton()) {
        	switch (parameter) 
        	{
        		case "alpha":
        			alpha_min_textfield.setText(String.valueOf(rs.getLowValue()));
                	alpha_max_textfield.setText(String.valueOf(rs.getHighValue()));
        		break;
        		
        		case "eta":
        	       	eta_min_textfield.setText(String.valueOf(rs.getLowValue()));
                	eta_max_textfield.setText(String.valueOf(rs.getHighValue()));
            	break;
            		
        		case "kappa":
        			kappa_min_textfield.setText(String.valueOf(rs.getLowValue()));
                	kappa_max_textfield.setText(String.valueOf(rs.getHighValue()));
            	break;	
        	}            		
    	}
    	
    	else {
			alpha_min_textfield.setText(String.valueOf(rs.getLowValue()));
        	alpha_max_textfield.setText(String.valueOf(rs.getHighValue()));
		 	eta_min_textfield.setText(String.valueOf(rs.getLowValue()));
        	eta_max_textfield.setText(String.valueOf(rs.getHighValue()));
    		kappa_min_textfield.setText(String.valueOf(rs.getLowValue()));
        	kappa_max_textfield.setText(String.valueOf(rs.getHighValue()));
        	
        	for (RangeSlider rangeSlider : rangeSliders.values()) {
        		rangeSlider.setLowValue(rs.getLowValue());
        		rangeSlider.setHighValue(rs.getHighValue());
        	}
    	}
	}
	
	@FXML
	public void changeParameterCouplingMode(ActionEvent e)
	{
		parameterCoupling_button.setDefaultButton(!parameterCoupling_button.isDefaultButton());
		
		if (parameterCoupling_button.isDefaultButton())
			System.out.println("Enabled parameter coupling.");
		
		else
			System.out.println("Disabled parameter coupling.");
	}
	
	@FXML
	private void updateNumberOfDatasets(ActionEvent e)
	{
		try {
			numberOfDatasetsToGenerate = Integer.parseInt(numberOfDatasets_textfield.getText());
			System.out.println("New number of datasets to generate: " + numberOfDatasetsToGenerate);
		}
		
		catch (NumberFormatException ex) {
			System.out.println("Only numbers are accepted. Correct input.");
		}
	}
	
	/**
	 * Generate parameter values based on defined parameter intervals and the number of datasets to create.
	 */
	private void generateParameterValues()
	{
		// @todo Generate parameter vaulues in dependency from sampling mode.
		switch (sampling_combobox.getValue())
		{
			case "Random":
				System.out.println("Randomly generating parameter data.");
				
				for (int i = 0; i < numberOfDatasetsToGenerate; i++) {
					// @todo Here: Generate random parameter values.
				}
			break;
			
			case "Cartesic":
				System.out.println("Sampling mode 'Cartesic' currently not supported.");
			break;
			
			case "Hypercube":
				System.out.println("Sampling mode 'Hypercube' currently not supported.");
			break;			
		}
		// Construct data for histogram of generated alpha parameters.
		alpha_barchart.getData().clear();
		final XYChart.Series<String, Float> alpha_series = new XYChart.Series<String, Float>();
		alpha_series.setName("alpha parameter data");
		for (int i = 0; i < rangeSliders.get("alpha").getMax(); i++) {
			// If in defined range: Output parameter counts.
			if (i >= rangeSliders.get("alpha").getLowValue() && i <= rangeSliders.get("alpha").getHighValue())
				alpha_series.getData().add(new XYChart.Data<String, Float>(String.valueOf(i), (float)(i * 10) ));
			// Otherwise: Output 0.
			else
				alpha_series.getData().add(new XYChart.Data<String, Float>(String.valueOf(i), (float)0 ));
		}
		alpha_barchart.getData().add(alpha_series);
	}
}
