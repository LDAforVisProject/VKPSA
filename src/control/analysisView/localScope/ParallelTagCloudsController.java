package control.analysisView.localScope;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import model.LDAConfiguration;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class ParallelTagCloudsController extends LocalScopeVisualizationController
{
	protected @FXML Canvas canvas;
	private ArrayList<VBox> tagCloudContainer;
	
	private ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations;
	
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing SII_ParallelTagCloudsController.");
		
		// Set visualization type.
		this.visualizationType = LocalScopeVisualizationType.PARALLEL_TAG_CLOUDS;
	}

	@Override
	public void refresh(ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations)
	{
		// Reset visualization.
		clear();
		
		// Update reference to data collections.
		this.selectedFilteredLDAConfigurations = selectedFilteredLDAConfigurations;
		
		// Update data. This type of visualization is reasonable for one LDA configuration
		// at the same time only, therefore we use only the first selected LDAConfiguration.
//		workspace.getDatabaseManagement().getLimitedKITData(ldaConfigurations);
		// @todo NEXT: Get limited KIT data for all topics in one (first) LDA configuration,
		// 		 visualize it using one cloud for each topic (use control values for number
		//		 of topics and keywords to use).
		
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		gc.setFill(Color.GREEN);
	    gc.setStroke(Color.BLUE);
	    gc.fillRect(5, 5, canvas.getWidth() - 10, canvas.getHeight() - 10);
	    
	    // Init tag cloud container.
	    double intervalX					= canvas.getWidth() / selectedFilteredLDAConfigurations.size();
	    tagCloudContainer					= new ArrayList<VBox>(selectedFilteredLDAConfigurations.size());
	    
		for (int i = 0; i < selectedFilteredLDAConfigurations.size(); i++) {
			LDAConfiguration lda = selectedFilteredLDAConfigurations.get(i); 
			
			VBox vbox = new VBox();
			vbox.setLayoutX(canvas.getLayoutX() + i * intervalX);
			vbox.setLayoutY(canvas.getLayoutY() + 5);
			tagCloudContainer.add(vbox);
			
			// Init labels to container.
			for (int j = 0; j < 10; j++) {
				Label label = new Label();
				label.setText("test #" + j);
				vbox.getChildren().add(label);
			}
			
			anchorPane.getChildren().add(vbox);
		}
		
	}

	@Override
	public void resize(double width, double height)
	{
		if (width > 0) {
			canvas.setWidth(width);
			refresh(selectedFilteredLDAConfigurations);
		}
		
		// Adapt height.
		if (height > 0) {
			canvas.setHeight(height - 35);
			refresh(selectedFilteredLDAConfigurations);
		}
	}

	@Override
	public void clear()
	{
		if (tagCloudContainer != null) {
			for (VBox vbox : tagCloudContainer) {
				anchorPane.getChildren().remove(vbox);
			}
			
			tagCloudContainer.clear();
		}
	}

	@Override
	protected void updateData()
	{
		// @todo Auto-generated method stub
		
	}
}
