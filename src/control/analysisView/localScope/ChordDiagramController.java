package control.analysisView.localScope;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import model.LDAConfiguration;

public class ChordDiagramController extends LocalScopeVisualizationController
{
	private @FXML WebView content_webview;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{	
	}

	@Override
	public void refresh(ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations,
						int maxNumberOfTopics, int numberOfTopics, int maxNumberOfKeywords,
						int numberOfKeywords, boolean updateData)
	{	
	}

	@Override
	public void resize(double width, double height)
	{	
	}

	@Override
	protected void updateData()
	{	
	}

	@Override
	public void reset(	ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations,
						int maxNumberOfTopics, int numberOfTopics, int maxNumberOfKeywords,
						int numberOfKeywords)
	{	
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}

}
