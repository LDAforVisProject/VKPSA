package view.components.documentLookup;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import model.documents.Document;
import model.documents.DocumentForLookupTable;
import model.documents.KeywordContext;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.util.Pair;
import view.components.DatapointIDMode;
import view.components.VisualizationComponent;

public class DocumentLookup extends VisualizationComponent
{
	private @FXML ImageView searchIcon_imageview;
	private @FXML TextField search_textfield;
	private @FXML TableView<DocumentForLookupTable> table;
	private @FXML Label topicID_label;
	
	/**
	 * ID of currently selected topic.
	 */
	private Pair<Integer, Integer> topicID;
	
	/**
	 * Selection of currently loaded documents.
	 */
	private ArrayList<Document> documents;
	
	/**
	 * Remember documents' ranks.
	 */
	private Map<Integer, Integer> documentRanksByID;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing DocumentLookup component.");
	
		// Set up map for document ID -> rank associations.
		documentRanksByID = new HashMap<Integer, Integer>();
		
		// Initialize table.
		initTable();
	}
	
	private void initTable()
	{
		// Table isn't editable.
		table.setEditable(false);
		
		// Get columns in table.
		ObservableList<TableColumn<DocumentForLookupTable, ?>> columns = table.getColumns();
		// Bind columns to properties.
		((TableColumn<DocumentForLookupTable, Integer>)columns.get(0)).setCellValueFactory(new PropertyValueFactory<DocumentForLookupTable, Integer>("rank"));
		((TableColumn<DocumentForLookupTable, Float>)columns.get(1)).setCellValueFactory(new PropertyValueFactory<DocumentForLookupTable, Float>("probability"));
		((TableColumn<DocumentForLookupTable, String>)columns.get(2)).setCellValueFactory(new PropertyValueFactory<DocumentForLookupTable, String>("title"));
		((TableColumn<DocumentForLookupTable, String>)columns.get(3)).setCellValueFactory(new PropertyValueFactory<DocumentForLookupTable, String>("date"));
		((TableColumn<DocumentForLookupTable, String>)columns.get(4)).setCellValueFactory(new PropertyValueFactory<DocumentForLookupTable, String>("authors"));
		((TableColumn<DocumentForLookupTable, String>)columns.get(5)).setCellValueFactory(new PropertyValueFactory<DocumentForLookupTable, String>("conference"));
	
		// Init on-click listeners for table.
		initTableRowListener();
	}
	
	/**
	 * Initialize on-click listener for table.
	 */
	private void initTableRowListener()
	{
		// Add listener for double-click on row.
		table.setRowFactory( tv -> {
		    TableRow<DocumentForLookupTable> row = new TableRow<>();
		    row.setOnMouseClicked(event -> {
		        if (event.getClickCount() == 2 && (!row.isEmpty()) ) {
		        	// Show document details.
		            analysisController.showDocumentDetail(row.getItem().getID());
		        }
		    });
		    
		    return row;
		});
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

	/**
	 * Refreshes data.
	 * @param topicID
	 * @param document List of documents, sorted by probability.
	 * @param searchTerm String to filter by.
	 */
	public void refresh(Pair<Integer, Integer> topicID, ArrayList<Document> documents, String searchTerm)
	{
		// Clear previous state.
		table.getItems().clear();
		documentRanksByID.clear();
		
		// Update topic ID.
		this.topicID	= topicID;
		// Update collection of documents.
		this.documents	= documents;
		
		// Update label for topic ID.
		topicID_label.setText(topicID.getKey() + "#" + topicID.getValue());
		
		// Count document ranks.
		int count = 1;
		
		// Update table data.
		for (Document doc : documents) {
			// Show in table only if document contains search term in any form.
			if (doc.containsTerm(searchTerm)) {
				table.getItems().add(doc.generateDocumentForLookup(count));
			}
			
			// Store rank in map.
			documentRanksByID.put(doc.getID(), count);
			
			// Iterate count.
			count++;
		}
	}
	
	/**
	 * Clears component.
	 */
	public void clear()
	{
		table.getItems().clear();
		topicID_label.setText("");
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

	@FXML
	public void searchForTerm(ActionEvent e)
	{
		System.out.println("Searching for term " + search_textfield.getText() + ".");
		log("Searching for term " + search_textfield.getText() + ".");
		
		// Refresh table, only displaying items containing the specified term.
		refresh(this.topicID, this.documents, search_textfield.getText());
	}

	public Map<Integer, Integer> getDocumentRanksByID()
	{
		return documentRanksByID;
	}
}
