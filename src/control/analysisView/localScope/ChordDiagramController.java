package control.analysisView.localScope;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import netscape.javascript.JSObject;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import model.LDAConfiguration;
import model.workspace.WorkspaceAction;
import model.workspace.tasks.Task_LoadTopicDistancesForSelection;

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
	private final Path templatePath				= Paths.get("src/js/chordDiagram.html");
	
	/**
	 * Holds original, unchanged HTML code.
	 */
	private final String templateHTMLCode	= readHTMLTemplate(templatePath);
	
	/**
	 * Task executing the loading of the current topic data (and holding the results).
	 */
	private Task_LoadTopicDistancesForSelection topicLoadingTask;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		// Init JS -> Java callbacks.
		initCallbackInjection();
	}
	
	/**
	 * Sets up controllers for monitoring and processing of callbacks from .js file to Java.
	 */
	private void initCallbackInjection()
	{
		// Instantiate callback monitor.
		JSCallbackMonitor jsCallbackMonitor = new JSCallbackMonitor(this);
		
		// Initialize web controller injection for JS -> Java callbacks.
		content_webview.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
	          @Override
	          public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
	              if (newState == State.SUCCEEDED) {
	            	  JSObject window = (JSObject) content_webview.getEngine().executeScript("window");
	                  window.setMember("callbackMonitor", jsCallbackMonitor);
	              }
	          }
	      }
		);
	}
	
	/**
	 * Propagates information about the selected connection.
	 * @param ldaID1
	 * @param ldaID2
	 * @param topicID1
	 * @param topicID2
	 */
	public void propagateSelectedDistanceInformation(final int ldaID1, final int ldaID2, final int topicID1, final int topicID2)
	{
		localScope.refreshPTC(ldaID1, ldaID2, topicID1, topicID2);
	}
	
	/**
	 * Propagates information about hover over a LDA config.
	 * @param ldaID
	 */
	public void propagateLDAHoverInformation(final int ldaID)
	{
		localScope.propagateLDAHoverInformation(ldaID);
	}
	
    /**
     * Process information that no group is hovered anymore.
     */
	public void propagateLDAHoverExitedInformation()
	{
		localScope.propagateLDAHoverExitedInformation();
	}
	
	@Override
	public void clear()
	{
		content_webview.getEngine().load("about:blank");
	}

	@Override
	public void refresh(ArrayList<LDAConfiguration> selectedLDAConfigurations,
						int maxNumberOfTopics, int numberOfTopics,
						int maxNumberOfKeywords, int numberOfKeywords,
						boolean updateData)
	{
		if (selectedLDAConfigurations.size() > 0) {
			// Load topic distance data for selection.
			topicLoadingTask = new Task_LoadTopicDistancesForSelection(workspace, WorkspaceAction.LOAD_SPECIFIC_TOPIC_DISTANCES, null, selectedLDAConfigurations, localScope.getFilterThresholds());
			topicLoadingTask.addListener(this);
			
			// Write list to file.
			(new Thread(topicLoadingTask)).start();
		}
	}
	
	@Override
	public void notifyOfTaskCompleted(final WorkspaceAction workspaceAction)
	{
		content_webview.getEngine().loadContent(adaptHTMLTemplate(templateHTMLCode));
	}

	/**
	 * Returns adapted HTML code (adjusted size, current data).
	 * @return Adapted HTML code.
	 */
	private String adaptHTMLTemplate(final String templateCode)
	{
		String adaptedHTMLCode = templateCode;
		
		// Adapt size of chord diagram.
		adaptedHTMLCode = adaptedHTMLCode.replaceAll("var w = x, h = y", "var w = " + content_webview.getWidth() + ", h = " + content_webview.getHeight());
		
		// Adapt size of mouseover radar chart.
		double higherSizeValue = content_webview.getWidth() > content_webview.getHeight() ? content_webview.getWidth() : content_webview.getHeight();
		adaptedHTMLCode = adaptedHTMLCode.replaceAll("rcWidth", String.valueOf((higherSizeValue / 3)) );
		
		// Rename parameters with greek letters. 
		adaptedHTMLCode = adaptedHTMLCode.replaceAll("_alpha", "α");
		adaptedHTMLCode = adaptedHTMLCode.replaceAll("_eta", "η");
		adaptedHTMLCode = adaptedHTMLCode.replaceAll("_kappa", "κ");
		
		// Adapt workspace path.
		String jsPath	= (System.getProperty("user.dir") + "\\src\\js\\").replace("\\", "/");
		adaptedHTMLCode = adaptedHTMLCode.replace("workspacePath", jsPath);
		
		// Adapt chord diagram data.
		if (topicLoadingTask != null) {
			final String mmap	= topicLoadingTask.getJSONTopicsMMap();
			final String matrix = topicLoadingTask.gettJSONTopicDistancesString();
			
			// Insert topic metadata.
			adaptedHTMLCode = adaptedHTMLCode.replaceAll("mmap = x", "mmap = " + mmap);
			// Insert topic distance matrix.
			adaptedHTMLCode = adaptedHTMLCode.replaceAll("matrix = y", "matrix = " + matrix);
		}
		
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
		
		content_webview.getEngine().setJavaScriptEnabled(true);
		// Load adapted HTML.
//		content_webview.getEngine().loadContent(adaptHTMLTemplate(templateHTMLCode));
		content_webview.getEngine().loadContent(adaptHTMLTemplate(readHTMLTemplate(templatePath)));
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
