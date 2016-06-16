package view.components.documentLookup;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import model.documents.Document;
import model.documents.DocumentForLookupTable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
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
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing DocumentLookup component.");
	
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
		((TableColumn<DocumentForLookupTable, Float>)columns.get(0)).setCellValueFactory(new PropertyValueFactory<DocumentForLookupTable, Float>("probability"));
		((TableColumn<DocumentForLookupTable, String>)columns.get(1)).setCellValueFactory(new PropertyValueFactory<DocumentForLookupTable, String>("title"));
		((TableColumn<DocumentForLookupTable, String>)columns.get(2)).setCellValueFactory(new PropertyValueFactory<DocumentForLookupTable, String>("date"));
		((TableColumn<DocumentForLookupTable, String>)columns.get(3)).setCellValueFactory(new PropertyValueFactory<DocumentForLookupTable, String>("authors"));
		((TableColumn<DocumentForLookupTable, String>)columns.get(4)).setCellValueFactory(new PropertyValueFactory<DocumentForLookupTable, String>("conference"));
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
	 * @param document List of documents, sorted by probability.
	 */
	public void refresh(ArrayList<Document> documents)
	{
		// Clear previous state.
		table.getItems().clear();
		
		// Update table data.
		for (Document doc : documents) {
			table.getItems().add(doc.generateDocumentForLookup());
		}
	}
	
	/**
	 * Clears component.
	 */
	public void clear()
	{
		table.getItems().clear();
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
