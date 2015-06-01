package control.dataView;

import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.control.PopOver;

import control.Controller;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.PopupWindow.AnchorLocation;

public class DataViewController extends Controller
{
	// -----------------------------------------------
	// 				UI elements.
	// -----------------------------------------------
	
	private @FXML AnchorPane root;
	private @FXML AnchorPane loading_anchorpane;
	private @FXML AnchorPane preprocessing_anchorpane;
	private @FXML AnchorPane generation_anchorpane;
	
	private PopOver workspaceChooser_popover;
	private Node workspaceChooser_owner;
	private Node workspaceChooser_content;
	
	private Scene scene;
	
	// -----------------------------------------------
	// 				Methods.
	// -----------------------------------------------
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing SII_DataViewController.");
		
		workspaceChooser_popover = null;
	}

	public void showWorkspaceChooser(final Node workspaceChooser_content, final Node owner, final Scene scene)
	{
		if(workspaceChooser_popover == null) {
			this.workspaceChooser_owner		= owner;
			this.workspaceChooser_content	= workspaceChooser_content;
			this.scene						= scene;
			workspaceChooser_popover		= new PopOver(workspaceChooser_content);
			
			workspaceChooser_popover.detach();
			workspaceChooser_popover.setDetachedTitle("Choose your workspace");
			workspaceChooser_popover.show(owner);
			
			workspaceChooser_popover.setX(scene.getWidth() / 2 - workspaceChooser_content.getLayoutBounds().getWidth() / 4);
			workspaceChooser_popover.setY(scene.getHeight() / 2 - workspaceChooser_content.getLayoutBounds().getHeight() / 4);
		}
	}
	
	public void toggleWorkspaceChooserStatus()
	{
		if (workspaceChooser_popover.isShowing()) {
			workspaceChooser_popover.hide();
		}
		
		else {
			workspaceChooser_popover.detach();
			workspaceChooser_popover.setDetachedTitle("Choose your workspace");
			workspaceChooser_popover.show(workspaceChooser_owner);
			
			workspaceChooser_popover.setX(scene.getWidth() / 2 - workspaceChooser_content.getLayoutBounds().getWidth() / 4);
			workspaceChooser_popover.setY(scene.getHeight() / 2 - workspaceChooser_content.getLayoutBounds().getHeight() / 4);
		}
	}
	
	public Node getContainer(String viewID)
	{
		Node result = null;
		
		switch (viewID) {
			case "load":
				result = root;
			break;
			
			case "preprocess":
				result = preprocessing_anchorpane;
			break;
				
			case "generate":
				result = generation_anchorpane;
			break;
			
			default:
				System.out.println("viewID '" + viewID + "' unknown.");
		}
		
		return result;
	}
}
