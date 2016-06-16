package view.components.contextSearch;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import model.documents.DocumentForLookupTable;
import model.documents.KeywordContext;
import javafx.beans.property.SimpleStringProperty;
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

public class ContextSearch extends VisualizationComponent
{
	private @FXML ImageView searchIcon_imageview;
	private @FXML TextField search_textfield;
	private @FXML TableView<KeywordContext> table;
	
	/**
	 * Define number of allowed words in context (both previous and afterwards).
	 */
	private static final int CONTEXT_WORD_COUNT = 3;
	
	
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
		
		// Get columns in table.
		ObservableList<TableColumn<KeywordContext, ?>> columns = table.getColumns();
		// Bind columns to properties.
		((TableColumn<KeywordContext, String>)columns.get(0)).setCellValueFactory(new PropertyValueFactory<KeywordContext, String>("documentTitle"));
		((TableColumn<KeywordContext, String>)columns.get(1)).setCellValueFactory(new PropertyValueFactory<KeywordContext, String>("originalAbstract"));
		((TableColumn<KeywordContext, String>)columns.get(2)).setCellValueFactory(new PropertyValueFactory<KeywordContext, String>("refinedAbstract"));
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
	 * @param keyword
	 * @param keywordContextList List of keyword's appearances. 
	 */
	public void refresh(final String keyword, final ArrayList<KeywordContext> keywordContextList)
	{
		// Clear table.
		table.getItems().clear();
		
//		CONTINUE HERE:
//			- Mails: Interest in tests?
//			- Prepare text for display in table.
//			- Color keyword occurences.
//			- Create pop-up for detail info on paper.
//			- Cosmetic improvements (e.g. reduce filter width relative to panel width.)
		
		// Iterate over all instances of KeywordContext.
		for (KeywordContext kc : keywordContextList) {
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
			
			table.getItems().add(new KeywordContext(kc.getID(), kc.getKeyword(), kc.documentTitleProperty().get(), contextStringsForDocument.getKey(), contextStringsForDocument.getValue()));
		}
	}
	
	
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
	   	 for (startIndex = keywordIndex; startIndex > 0 && spaceCount < 4; startIndex--) {
	   		 // If space is found: Increase space count.
	   		 if (target.charAt(startIndex) == ' ')
	   			 spaceCount++;
	   	 }
   	 
	   	 // Reset space count.
	   	 spaceCount	= 0;
			
	   	 // Find last index from which to add to context neighbourhood:
    	 // Go forward until the n-th space was found (-> n after before currently processed keyword occurence).
    	 for (endIndex = keywordIndex; endIndex < target.length() && spaceCount < 4; endIndex++) {
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

	/**
	 * Clear component.
	 */
	public void clear()
	{
		table.getItems().clear();
	}
}
