package control.analysisView.localScope;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import view.components.legacy.LocalScopeInstance;
import model.LDAConfiguration;
import model.workspace.Workspace;
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
	
	/**
	 * Maximal number of topics made possible to select in the GUI.
	 */
	protected int maxNumberOfTopics;
	/**
	 * Maximal number of keywords to use in this visualization.
	 */
	protected int numberOfTopics;
	/**
	 * Maximal number of keywords made possible to select in the GUI.
	 */
	protected int maxNumberOfKeywords;
	/**
	 * Maximal number of keywords to use in this visualization.
	 */
	protected int numberOfKeywords;
	
	
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
	 * @param maxNumberOfKeywords 
	 * @param maxNumberOfTopics 
	 */
	public abstract void refresh(ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations, int maxNumberOfTopics, int numberOfTopics, int maxNumberOfKeywords, int numberOfKeywords, boolean updateData);

	/**
	 * Resize visualization.
	 * @param width
	 * @param height
	 */
	public abstract void resize(double width, double height);
	
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
	
	/**
	 * Resets visualization.
	 * @param selectedFilteredLDAConfigurations
	 * @param maxNumberOfTopics
	 * @param numberOfTopics
	 * @param maxNumberOfKeywords
	 * @param numberOfKeywords
	 */
	public abstract void reset(ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations, int maxNumberOfTopics, int numberOfTopics, int maxNumberOfKeywords, int numberOfKeywords);
	
	/**
	 * Clears visualization.
	 */
	public abstract void clear();
}
