package view.components.contextSearch;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import model.documents.DocumentForLookupTable;
import model.documents.KeywordContext;
import model.workspace.Workspace;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.util.Pair;
import view.components.DatapointIDMode;
import view.components.VisualizationComponent;

public class ContextSearch extends VisualizationComponent
{
	private @FXML ImageView searchIcon_imageview;
	private @FXML TextField search_textfield;
	private @FXML TableView<KeywordContext> table;
	private @FXML Label keyword_label;
	
	/**
	 * Menu item used for jumping to document in DocumentLookup.
	 */
	private MenuItem jumpToMenuItem;
	
	/**
	 * Define number of allowed words in context (both previous and afterwards).
	 */
	private static final int CONTEXT_WORD_COUNT = 3;
	
	/**
	 * Currently examined topic's comprehensive ID.
	 * Actually not neeeded in ContextSearch except to forward it to DocumentDetail.
	 */
	private Pair<Integer, Integer> topicID;
	
	/**
	 * Currently selected keyword.
	 */
	private String keyword;
	
	/**
	 * Collection of instances of KeywordContext.
	 */
	private ArrayList<KeywordContext> keywordContextList;
	
	/**
	 * Map of results in document lookup table: Document ID -> rank.
	 */
	Map<Integer, Integer> documentRanksByID;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing SearchContext component.");
		
		// Initialize table.
		initTable();
	}
	
	private void initTable()
	{
		// Table isn't editable.
		table.setEditable(false);
		
		// Init context menu for table.
		initTableContextMenu();
		
		// Get columns in table.
		ObservableList<TableColumn<KeywordContext, ?>> columns = table.getColumns();
		// Bind columns to properties.
		((TableColumn<KeywordContext, Integer>)columns.get(0)).setCellValueFactory(new PropertyValueFactory<KeywordContext, Integer>("documentRank"));
		((TableColumn<KeywordContext, String>)columns.get(1)).setCellValueFactory(new PropertyValueFactory<KeywordContext, String>("documentTitle"));
		((TableColumn<KeywordContext, String>)columns.get(2)).setCellValueFactory(new PropertyValueFactory<KeywordContext, String>("originalAbstract"));
		((TableColumn<KeywordContext, String>)columns.get(3)).setCellValueFactory(new PropertyValueFactory<KeywordContext, String>("refinedAbstract"));
		
		// Init on-click listeners for table.
		setTableRowListener();
	}
	
	private void initTableContextMenu()
	{
		ContextMenu menu	= new ContextMenu();
		jumpToMenuItem		= new MenuItem("Jump to entry in document lookup");
		menu.getItems().add(jumpToMenuItem);
		
		// Define context menu.
		table.setContextMenu(menu);
		
		// removeMenuItem will remove the row from the table:
		jumpToMenuItem.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent e) {
		    	// Fetch selected entry, jump to entry in document lookup.
		        analysisController.getDocumentLookup().jumpToDocument(table.getSelectionModel().getSelectedItem().getDocumentID());
		    }
		});
	}
	
	/**
	 * Initialize on-click listener for table.
	 */
	private void setTableRowListener()
	{
		// Add listener for double-click on row.
		table.setRowFactory( tv -> {
		    TableRow<KeywordContext> row = new TableRow<>();
		    row.setOnMouseClicked(event -> {
		    	// On double click: Show document detail.
		        if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
		        	// Show document details.
		            analysisController.showDocumentDetail(row.getItem().getDocumentID(), topicID.getKey(), topicID.getValue());
		        }
		        
		        // On right click: Check if document is currently shown in DocumentLookup.
		        // If not, temporarily disable context menu's "Jump to" item.
		        else if (event.getClickCount() == 1 && event.getButton() == MouseButton.SECONDARY) {
		        	// Get requested document ID.
		        	final int documentID	= table.getSelectionModel().getSelectedItem().getDocumentID();
		        	
		        	// Enable, if document was found in table.
		        	if (analysisController.getDocumentLookup().findDocumentInCurrentTable(documentID) >= 0)
		        		jumpToMenuItem.setDisable(false);
		        	// Disable, if not.
		        	else
		        		jumpToMenuItem.setDisable(true);
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
	 * Refresh ContextSearch component.
	 * @param topicID Comprehensive topic ID.
	 * @param keyword
	 * @param keywordContextList List of keyword's appearances. 
	 * @param documentRanksByID Map with document ID -> rank in document lookup results.
	 * @param searchTerm String to filter by.
	 */
	public void refresh(final Pair<Integer, Integer> topicID, final String keyword, final ArrayList<KeywordContext> keywordContextList, 
						final Map<Integer, Integer> documentRanksByID, final String searchTerm)
	{
		// Clear table.
		table.getItems().clear();
		
		// Update LDA configuration ID.
		this.topicID			= topicID;
		
		// Update selected keyword.
		this.keyword			= keyword;
		// Update collection of context instances.
		this.keywordContextList	= keywordContextList;
		
		// Update keyword label.
		this.keyword_label.setText(keyword);
		
		// Update document rank listing.
		this.documentRanksByID	= documentRanksByID;
		
		// Set listener for table row action.
		setTableRowListener();
		
		// Iterate over all instances of KeywordContext.
		for (KeywordContext kc : keywordContextList) {
			
			// Check if KeywordContext contains search term.
			if (kc.containsTerm(searchTerm)) {
				/*
				 * 1. Generate context strings.
				 */
				
				Pair<String, String> contextStringsForDocument = generateContextStrings(keyword, kc);
				
				/*
				 * 2. Add context summaries for this document to collection. 
				 */
			
				kc.setOriginalAbstract(new SimpleStringProperty(contextStringsForDocument.getKey()));
				kc.setRefinedAbstract(new SimpleStringProperty(contextStringsForDocument.getValue()));
				
				/*
				 * 3. Update table.  
				 */
				
				table.getItems().add(new KeywordContext(kc.getDocumentID(), kc.getKeyword(), this.documentRanksByID.get(kc.getDocumentID()), kc.getDocumentTitle(), contextStringsForDocument.getKey(), contextStringsForDocument.getValue()));
			}
		}
	}
	
	/**
	 * Generate string containing context in specified document.
	 * @param keyword
	 * @param keywordContext
	 * @param searchTerm
	 * @return
	 */
	private Pair<String, String> generateContextStrings(final String keyword, final KeywordContext keywordContext)
	{
		// Context string for original abstract.
		String originalAbstractContextSummary	= "...";
		// Context string for processed abstract.
		String refinedAbstractContextSummary	= "...";
		
		// Index of found keyword.
		int keywordIndex						= -1;
		
		/*
		 * 1. Find indices of keyword's occurences in original abstract.
		 */
		
		do {
		     // If keyword was found:
		     if (keywordIndex >= 0) {
		    	 // Find delimiter positions in original abstract's neighbourhood.
		    	 Pair<Integer, Integer> delimiterPositions = findNeighbourhoodDelimiterPositions(keywordIndex, keywordContext.originalAbstractProperty().get());
		    	 
		    	 // Add neighbourhood to context string.
		    	 originalAbstractContextSummary += keywordContext.originalAbstractProperty().get().substring(delimiterPositions.getKey(), delimiterPositions.getValue()) + "...";
		     }
		     
		     // Find next occurence.
		     keywordIndex = keywordContext.originalAbstractProperty().get().indexOf(keyword, keywordIndex + 1);
		} while(keywordIndex >= 0);
		
		// Reset keyword index.
		keywordIndex = -1;
		
		/*
		 * 2. Find indices of keyword's occurences in processed abstract.
		 */
		
		do {
		     // If keyword was found:
		     if (keywordIndex >= 0) {
		    	 // Find delimiter positions in original abstract's neighbourhood.
		    	 Pair<Integer, Integer> delimiterPositions = findNeighbourhoodDelimiterPositions(keywordIndex, keywordContext.refinedAbstractProperty().get());
		    	 
		    	 // Add neighbourhood to context string.
		    	 refinedAbstractContextSummary += keywordContext.refinedAbstractProperty().get().substring(delimiterPositions.getKey(), delimiterPositions.getValue()) + "...";
		     }
		     
		     // Find next occurence.
		     keywordIndex = keywordContext.refinedAbstractProperty().get().indexOf(keyword, keywordIndex + 1);
		} while(keywordIndex >= 0);		
		
		// Remove dots, if no results.
		if (originalAbstractContextSummary.equals("..."))
			originalAbstractContextSummary = "";
		if (refinedAbstractContextSummary.equals("..."))
			refinedAbstractContextSummary = "";
		
		// Return results.
		return new Pair<String, String>(originalAbstractContextSummary, refinedAbstractContextSummary);
	}
	
	/**
	 * Finds delimiter indices of currently specified index.
	 * Used to find start and end of a keyword's neighbourhood inside a context. 
	 * @param keywordIndex Start index of keyword.
	 * @param target String to inspect.
	 * @return
	 */
	private Pair<Integer, Integer> findNeighbourhoodDelimiterPositions(final int keywordIndex, final String target)
	{
		// Temporary storage for results.
		int startIndex	= -1;
		int endIndex	= -1;
		
		// Auxiliary variable used for counting spaces.
		int spaceCount	= 0;
		
		 // Find first index from which to add to context neighbourhood:
	   	 // Go backwards until the n-th space was found (-> n words before currently processed keyword occurence).
	   	 for (startIndex = keywordIndex; startIndex > 0 && spaceCount <= ContextSearch.CONTEXT_WORD_COUNT; startIndex--) {
	   		 // If space is found: Increase space count.
	   		 if (target.charAt(startIndex) == ' ')
	   			 spaceCount++;
	   	 }
   	 
	   	 // Reset space count.
	   	 spaceCount	= 0;
			
	   	 // Find last index from which to add to context neighbourhood:
    	 // Go forward until the n-th space was found (-> n after before currently processed keyword occurence).
    	 for (endIndex = keywordIndex; endIndex < target.length() && spaceCount <= ContextSearch.CONTEXT_WORD_COUNT; endIndex++) {
    		 // If space is found: Increase space count.
    		 if (target.charAt(endIndex) == ' ')
    			 spaceCount++;
    	 }
    	 
    	 // Wrap up.
    	 if (startIndex > 0)
	   		 startIndex++;
	   	 if (endIndex < target.length() - 1)
	   		 endIndex--;
	   	 
		// Return results.
		return new Pair<Integer, Integer>(startIndex, endIndex);
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

	/**
	 * Clear component.
	 */
	public void clear()
	{
		table.getItems().clear();
		keyword_label.setText("");
		
		// Clear search text field.
		search_textfield.clear();
	}
	
	@FXML
	public void searchForTerm(ActionEvent e)
	{
		System.out.println("Searching for term " + search_textfield.getText() + ".");
		log("Searching for term " + search_textfield.getText() + ".");
		
		// Refresh table, only displaying items containing the specified term.
		refresh(this.topicID, this.keyword, this.keywordContextList, this.documentRanksByID, search_textfield.getText());
	}

	@Override
	public void refresh()
	{
	}
}
