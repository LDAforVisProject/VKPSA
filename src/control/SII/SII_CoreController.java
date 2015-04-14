package control.SII;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class SII_CoreController implements Initializable
{
	private @FXML Node root;
	
	private @FXML ImageView icon_settings;
	private @FXML ImageView icon_help;
	private @FXML ImageView icon_lock;
	
	private @FXML Label label_title;
	private @FXML ImageView icon_current;
	
	private @FXML ImageView icon_load;
	private @FXML ImageView icon_generate;
	private @FXML ImageView icon_preprocess;
	private @FXML ImageView icon_analyze;
	
	private @FXML AnchorPane pane_content;
	
	private Scene scene;
	
	// References to instances of content pane's controllers.
	private SII_AnalysisController analysisController;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing SII_CoreController.");
	}
	
	public void setScene(Scene scene)
	{
		this.scene = scene;
	}
	
	// Action icon event.
	@FXML
	public void actionButtonClicked(MouseEvent e) 
	{
		// Do nothing if ImageView is not source of the event.
		if (!(e.getSource() instanceof ImageView))
			return;
		
		// Load .fxml file, get reference to controller.
    	FXMLLoader fxmlLoader		= new FXMLLoader();
        Node contentNode			= null;
        
        // Get event source.
        ImageView source = (ImageView) e.getSource();
		
        try {
			switch (source.getId()) {
				case "icon_analyze":
					label_title.setText("Analyze");
					contentNode			= (Node) fxmlLoader.load(getClass().getResource("/view/SII/SII_Content_Analysis.fxml").openStream());
					analysisController	= (SII_AnalysisController)fxmlLoader.getController();
					
					// Add to content pane. @todo Check if this (analysis) pane was already loaded.
					// If so: Don't add it again, rather manipulate z-index of associated child
					// of pane_content.
					pane_content.getChildren().add(contentNode);
				break;
			}
			
			// Ensure resizability of tab content.
    		AnchorPane.setTopAnchor(contentNode, 0.0);
    		AnchorPane.setBottomAnchor(contentNode, 0.0);
    		AnchorPane.setLeftAnchor(contentNode, 0.0);
    		AnchorPane.setRightAnchor(contentNode, 0.0);
        }
        
        catch (IOException ioEx) {
        	
        }
		
	}
}
