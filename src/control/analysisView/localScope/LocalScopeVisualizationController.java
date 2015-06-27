package control.analysisView.localScope;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import view.components.LocalScopeInstance;
import model.LDAConfiguration;
import model.workspace.Workspace;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import control.Controller;

public abstract class LocalScopeVisualizationController extends Controller
{
	/*
	 * UI elements.
	 */
	
	protected AnchorPane anchorPane;

	/*
	 * Data.
	 */
	
	/**
	 * Visualization type.
	 */
	protected LocalScopeVisualizationType visualizationType;
	
	/**
	 * Reference to containing instance of local scope.
	 */
	protected LocalScopeInstance localScope;
	
	/**
	 * Reference to workspace.
	 */
	protected Workspace workspace;
	
	
	@Override
	public abstract void initialize(URL arg0, ResourceBundle arg1);

	@Override
	public void resizeContent(double width, double height)
	{
		System.out.println("Resizing local scope");
	}
	
	public void setAnchorPane(AnchorPane anchorPane)
	{
		this.anchorPane = anchorPane;
	}

	/**
	 * Redraw visualization.
	 */
	public abstract void refresh(ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations);

	/**
	 * Resize visualization.
	 * @param width
	 * @param height
	 */
	public abstract void resize(double width, double height);

	public void updateLabelWithVisualizationType(Label label)
	{
		switch (visualizationType) {
			case PARALLEL_TAG_CLOUDS:
				label.setText("Parallel Tag Clouds");
			break;
			
			default:
				label.setText("Unknown");
		}
	}
	
	public LocalScopeVisualizationType getVisualizationType()
	{
		return visualizationType;
	}
	
	public void setLocalScopeInstance(LocalScopeInstance localScope)
	{
		this.localScope = localScope;
	}
	
	public void setWorkspace(Workspace workspace)
	{
		this.workspace = workspace;
	}
	
	protected abstract void updateData();
	
	public abstract void clear();
}
