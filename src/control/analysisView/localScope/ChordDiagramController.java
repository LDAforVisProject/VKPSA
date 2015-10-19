package control.analysisView.localScope;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import model.LDAConfiguration;

/**
 * Controller for chord diagram in local scope instance.
 * @author RM
 *
 */
public class ChordDiagramController extends LocalScopeVisualizationController
{
	/**
	 * WebView rendering the d3.js visualization.
	 */
	private @FXML WebView content_webview;
	
	/**
	 * Path to HTML template for chord diagram visualization.
	 */
	private final Path templatePath			=  Paths.get("src/js/d3-chord-diagrams-master/testUber.html");
	
	/**
	 * Holds original, unchanged HTML code.
	 */
	private final String templateHTMLCode	= readHTMLTemplate(templatePath);
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println(templateHTMLCode);
		
		// Load adapted HTML.
		content_webview.getEngine().loadContent(adaptHTMLTemplate(templateHTMLCode));
	}

	@Override
	public void refresh(ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations,
						int maxNumberOfTopics, int numberOfTopics, int maxNumberOfKeywords,
						int numberOfKeywords, boolean updateData)
	{
		System.out.println("refresh");
		next step: get own data in there.
	}

	/**
	 * Returns adapted HTML code (adjusted size, current data).
	 * @return Adapted HTML code.
	 */
	private String adaptHTMLTemplate(final String templateCode)
	{
		String adaptedHTMLCode = templateCode;
		
		// Adapt size.
		adaptedHTMLCode = adaptedHTMLCode.replaceAll("var w = x, h = y", "var w = " + content_webview.getWidth() + ", h = " + content_webview.getHeight());
		
		System.out.println("adapted code = ");
		System.out.println(adaptedHTMLCode);
		
		return adaptedHTMLCode;
	}
	
	/**
	 * Returns HTML template code in one string.
	 * @return
	 */
	private String readHTMLTemplate(Path templatePath)
	{
		// String holding the entire HTML template code.
		String code = "";
		
		// Store all lines from code.
		List<String> lines;
		
		try {
			lines = Files.readAllLines(templatePath);
			for (String line : lines) {
				code += line + "\n";
			}
		}
		
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return code;
	}
	
	@Override
	public void resize(double width, double height)
	{	
		// Adapt width.
		if (width > 0) {
			content_webview.setPrefWidth(width);
		}
		
		// Adapt height.
		if (height > 0) {
			content_webview.setPrefHeight(height);
		}
		
		content_webview.getEngine().setJavaScriptEnabled(true	);
		// Load adapted HTML.
		content_webview.getEngine().loadContent(adaptHTMLTemplate(templateHTMLCode));
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
