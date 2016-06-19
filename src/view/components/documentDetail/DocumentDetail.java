package view.components.documentDetail;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import model.documents.Document;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.util.Pair;
import view.components.DatapointIDMode;
import view.components.VisualizationComponent;

/**
 * Component for display of a document's detailed information.
 * @author RM
 *
 */
public class DocumentDetail extends VisualizationComponent
{
	/*
	 * UI elements.
	 */
	
	private @FXML Label title_label;
	private @FXML Label authors_label;
	private @FXML Label date_label;
	private @FXML Label conference_label;
	private @FXML Label keywords_label;
	private @FXML TextArea originalAbstract_textfield;
	private @FXML TextArea processedAbstract_textfield;
	
	/**
	 * Currently displayed document.
	 */
	private Document document;
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing DocumentDetail.");
	}
	
	/**
	 * Updates UI.
	 * @param document
	 */
	public void update(final Document document)
	{
		// Store displayed document.
		this.document = document;
		
		// Update UI elements.
		title_label.setText(document.getTitle());
		authors_label.setText(document.getAuthors());
		date_label.setText(document.getDate());
		conference_label.setText(document.getConference());
		keywords_label.setText(document.getKeywords());
		originalAbstract_textfield.setText(document.getOriginalAbstract());
		processedAbstract_textfield.setText(document.getProcessedAbstract());
	}
	
	@Override
	public void processSelectionManipulationRequest(double minX, double minY, double maxX, double maxY)
	{		
	}

	@Override
	public void processEndOfSelectionManipulation()
	{
	}

	@Override
	public Pair<Integer, Integer> provideOffsets()
	{
		return null;
	}

	@Override
	public void processKeyPressedEvent(KeyEvent ke)
	{
	}

	@Override
	public void processKeyReleasedEvent(KeyEvent ke)
	{
	}

	@Override
	public void refresh()
	{	
	}

	@Override
	public void initHoverEventListeners()
	{	
	}

	@Override
	public void highlightHoveredOverDataPoints(Set<Integer> dataPointIDs, DatapointIDMode idMode)
	{	
	}

	@Override
	public void removeHoverHighlighting()
	{	
	}

	@Override
	public void resizeContent(double width, double height)
	{	
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}

}
