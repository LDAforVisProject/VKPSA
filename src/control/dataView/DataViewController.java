package control.dataView;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.controlsfx.control.PopOver;

import control.Controller;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.stage.WindowEvent;

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
	
	private Scene scene;
	
	private Node workspaceChooser_owner;
	private Node workspaceChooser_content;
	
	private Map<String, Node> dataSubViewNodes;
	private Map<String, Controller> dataSubViewControllers;
	
	// -----------------------------------------------
	// 				Other attributes.
	// -----------------------------------------------	
	
	private boolean isDataLoaded;
	
	// -----------------------------------------------
	// 				Methods.
	// -----------------------------------------------
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing SII_DataViewController.");
		
		workspaceChooser_popover 	= null;
		isDataLoaded				= false;
	}

	public void initWorkspaceChooser(final Node workspaceChooser_content, final Node owner, final Scene scene)
	{
		this.workspaceChooser_owner		= owner;
		this.workspaceChooser_content	= workspaceChooser_content;
		this.scene						= scene;
		workspaceChooser_popover		= new PopOver(workspaceChooser_content);
		
		// Add listener on closing event to popover: Show sub data view elements only when data has loaded.
		workspaceChooser_popover.setOnHiding(new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent window)
            {
                if (isDataLoaded) {
                	toggleSubDataViews();
                }
            }
        });
	}
	
	public void showWorkspaceChooser(boolean disableSubDataViewse)
	{
		if (workspaceChooser_popover != null && !workspaceChooser_popover.isShowing()) {
			workspaceChooser_popover.detach();
			workspaceChooser_popover.setDetachedTitle("Choose your workspace");
			workspaceChooser_popover.show(workspaceChooser_owner);
			
			workspaceChooser_popover.setX(scene.getWidth() / 2 - workspaceChooser_content.getLayoutBounds().getWidth() / 4);
			workspaceChooser_popover.setY(scene.getHeight() / 2 - workspaceChooser_content.getLayoutBounds().getHeight() / 4);
			
			if (disableSubDataViewse) {
				dataSubViewNodes.get("generate").setDisable(true);
				dataSubViewNodes.get("preprocess").setDisable(true);
			}
		}
	}
	
	public void hideWorkspaceChooser(boolean enableSubDataViews)
	{
		if (workspaceChooser_popover != null && workspaceChooser_popover.isShowing()) {
			workspaceChooser_popover.hide();
			
			if (enableSubDataViews) {
				dataSubViewNodes.get("generate").setDisable(false);
				dataSubViewNodes.get("preprocess").setDisable(false);
			}
		}
	}
	
	public void toggleWorkspaceChooserStatus(boolean toggleSubDataViews)
	{
		// Toggle subDataView's .enable option, if desired.
		if (toggleSubDataViews) {
			toggleSubDataViews();
		}
		
		// Toggle workspace chooser popup.
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
	
	private void toggleSubDataViews()
	{
		dataSubViewNodes.get("generate").setDisable(!dataSubViewNodes.get("generate").isDisabled());
		dataSubViewNodes.get("preprocess").setDisable(!dataSubViewNodes.get("preprocess").isDisabled());
	}
	
	public void freezeOptionControls()
	{
		for (Controller controller : dataSubViewControllers.values()) {
			DataSubViewController dsvController = (DataSubViewController)controller;
			dsvController.freezeOptionControls();
		}
	}
	
	public void unfreezeOptionControls()
	{
		for (Controller controller : dataSubViewControllers.values()) {
			DataSubViewController dsvController = (DataSubViewController)controller;
			dsvController.unfreezeOptionControls();
		}
	}
	
	/*
	 * From here: Getter and setter.
	 */
	
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
	
	public void setDataSubViews(Map<String, Node> dataSubViewNodes, Map<String, Controller> dataSubViewControllers)
	{
		this.dataSubViewNodes		= dataSubViewNodes;
		this.dataSubViewControllers	= dataSubViewControllers;
	}
	
	public void setDataLoaded(boolean isDataLoaded)
	{
		this.isDataLoaded = isDataLoaded;
	}
	
	public ProgressIndicator getProgressIndicator_calculateMDSCoordinates()
	{
		return ((PreprocessingController)dataSubViewControllers.get("preprocess")).getProgressIndicator_calculateMDSCoordinates();
	}

	public ProgressIndicator getProgressIndicator_distanceCalculation()
	{
		return ((PreprocessingController)dataSubViewControllers.get("preprocess")).getProgressIndicator_distanceCalculation();
	}
}
