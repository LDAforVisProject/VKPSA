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
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
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
	 * Barchart displaying distribution of probability over words in topic.
	 */
	private @FXML BarChart<String, Number> probability_barchart;
	private @FXML NumberAxis probability_barchart_yAxis_numberaxis;
	
	/**
	 * Scale factor for highlighting bars in probability bar chart. 
	 */
	final double BAR_SCALE_X_FACTOR = 30;
	
	/*
	 * Non-GUI data.
	 */
	
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
	
	/**
	 * Currently selected keyword.
	 */
	private String keyword;
	
	
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

		 // Set listener for mouse exit.
		table.setOnMouseExited(event -> {
			// Reset all bars.
			highlightDocumentInProbabilityChart(-1);
	    });
		
		// Initialize probability bar chart.
		initProbabilityBarchart();
	}
	
	/**
	 * Initiailze probability bar chart.
	 */
	private void initProbabilityBarchart()
	{
		// Hide x-axis.
		probability_barchart.getXAxis().setTickLabelsVisible(false);
		probability_barchart.getXAxis().setOpacity(0);
		
		// Set boundaries for y-axis.
		probability_barchart_yAxis_numberaxis.setAutoRanging(false);
		probability_barchart_yAxis_numberaxis.setLowerBound(0);
		probability_barchart_yAxis_numberaxis.setUpperBound(1);
		probability_barchart_yAxis_numberaxis.setTickUnit(0.25);
	}

	/**
	 * Initialize on-click listener for table.
	 */
	private void initTableRowListener()
	{
		// Add listener for double-click on row.
		table.setRowFactory( tv -> {
		    TableRow<DocumentForLookupTable> row = new TableRow<>();
		    
		    // Set listener for click.
		    row.setOnMouseClicked(event -> {
		        if (event.getClickCount() == 2 && (!row.isEmpty()) ) {
		        	// Show document details.
		            analysisController.showDocumentDetail(row.getItem().getDocumentID(), topicID.getKey(), topicID.getValue(), keyword);
		        }
		    });
		    
		    // Set listener for hover.
		    row.setOnMouseEntered(event -> {
		        if (!row.isEmpty()) {
		        	// Highlight document in chart.
		            highlightDocumentInProbabilityChart(row.getItem().getDocumentID());
		        }
		    });
		    
		    return row;
		});
	}
	
	/**
	 * Highlight document in probability chart.
	 * @param documentID
	 */
	private void highlightDocumentInProbabilityChart(final int documentID)
	{
		// Highlight document with specified ID.
		for (XYChart.Data<String, Number> item : probability_barchart.getData().get(0).getData()) {
			// If item is not selected one: Reset opacity and scale factor.
			if (item == null || (int)item.getExtraValue() != documentID) {
				item.getNode().setOpacity(VisualizationComponent.DEFAULT_OPACITY_FACTOR);
				item.getNode().setScaleX(1);
			}
			// Otherwise: Scale up, maximize opacity.
			else {
				item.getNode().setOpacity(1);
				double adjustedScaleFactor = BAR_SCALE_X_FACTOR * probability_barchart.getData().get(0).getData().size() / 2019.0; 
				item.getNode().setScaleX(adjustedScaleFactor >= 1 ? adjustedScaleFactor : 1);
			}
		}
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
	 * @param topicID Currently examined topic's comprehensive ID.
	 * @param keyword Currently examined keyword.
	 * @param document List of documents, sorted by probability.
	 * @param searchTerm String to filter by.
	 */
	public void refresh(final Pair<Integer, Integer> topicID, final String keyword, final ArrayList<Document> documents, final String searchTerm)
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
		
		// Update keyword.
		this.keyword	= keyword;
		
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
		
		// Refresh probability bar chart.
		refreshBarchart();
	}
	
	/**
	 * Refresh bar chart.
	 */
	private void refreshBarchart()
	{
		// Clear bar chart.
		probability_barchart.getData().clear();
		
		// Allocate new data series.
		XYChart.Series<String, Number> dataSeries = new XYChart.Series<String, Number>(); 
		
		// Get probability for all documents; add to data series.
		int count = 0;
		for (DocumentForLookupTable doc : table.getItems()) {
			// Create new entry.
			Data<String, Number> entry = new XYChart.Data<String, Number>(String.valueOf(count++), doc.getProbability());
			entry.setExtraValue(doc.getDocumentID());
			// Add entry to data series.
			dataSeries.getData().add(entry);
		}
		
		// Add data series to chart.
		probability_barchart.getData().add(dataSeries);
	
		// Adjust opacity.
		for (XYChart.Data<String, Number> item : probability_barchart.getData().get(0).getData()) {
			item.getNode().setOpacity(VisualizationComponent.DEFAULT_OPACITY_FACTOR);
		}
	}
	
	/**
	 * Clears component.
	 */
	public void clear()
	{
		// Clear table.
		table.getItems().clear();
		// Clear label.
		topicID_label.setText("");
		// Clear bar chart.
		probability_barchart.getData().clear();
		// Clear search text field.
		search_textfield.clear();
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
		refresh(this.topicID, this.keyword, this.documents, search_textfield.getText());
	}

	public Map<Integer, Integer> getDocumentRanksByID()
	{
		return documentRanksByID;
	}
	
	/**
	 * Find document with specified ID in current table view.
	 * @param documentID 
	 * @return -1, if document with ID wasn't found. It's row index, if found.
	 */
	public int findDocumentInCurrentTable(final int documentID)
	{
		int row = -1;
		for (DocumentForLookupTable doc : table.getItems()) {
			if (doc.getDocumentID() == documentID) {
				return row;
			}
			
			// Keep track of investigated rows.
			row++;
		}
		
		return -1;
	}

	/**
	 * Jumps to table row containing document with specified ID.
	 * @param documentID
	 * @return True, if document contained in current table view. False, if not.
	 */
	public boolean jumpToDocument(final int documentID)
	{
		// Search for document in table with specified ID.
		int row = 0;
		for (DocumentForLookupTable doc : table.getItems()) {
			if (doc.getDocumentID() == documentID) {
				// Select entry in table.
				table.getSelectionModel().select(row);
				// Scroll to entry.
				table.scrollTo(row);
				
				return true;
			}
			
			// Keep track of investigated rows.
			row++;
		}
		
		return false;
	}
	
	/**
	 * Jumps to specified table row.
	 * @param rowIndex 
	 */
	public void jumpToRow(final int rowIndex)
	{
		// Select entry in table.
		table.getSelectionModel().select(rowIndex);
		// Scroll to entry.
		table.scrollTo(rowIndex);
	}
}
