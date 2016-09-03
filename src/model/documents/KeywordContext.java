package model.documents;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class KeywordContext
{
	private int documentID;
	/**
	 * Currently examined keyword.
	 */
	private String activeKeyword;
	/**
	 * Field in document containing paper's keywords.
	 */
	private String keywords;
	private int occurenceCount;
	private SimpleIntegerProperty documentRank;
	private SimpleStringProperty documentTitle;
	private SimpleStringProperty originalAbstract;
	private SimpleStringProperty refinedAbstract;
	
	public KeywordContext(int documentID, String keyword, String keywordsContent, int documentRank, String documentTitle, int occurenceCount, String originalAbstract, String refinedAbstract)
	{
		this.documentID 		= documentID;
		this.activeKeyword 			= keyword;
		this.occurenceCount		= occurenceCount;
		this.documentRank		= new SimpleIntegerProperty(documentRank);
		this.documentTitle 		= new SimpleStringProperty(documentTitle);
		this.originalAbstract 	= new SimpleStringProperty(originalAbstract);
		this.refinedAbstract 	= new SimpleStringProperty(refinedAbstract);
	}

	public int getDocumentID()
	{
		return documentID;
	}

	public String getActiveKewyord()
	{
		return activeKeyword;
	}
	
	public int getOccurenceCount()
	{
		return occurenceCount;
	}

	public SimpleStringProperty documentTitleProperty()
	{
		return documentTitle;
	}

	public SimpleStringProperty originalAbstractProperty()
	{
		return originalAbstract;
	}

	public SimpleStringProperty refinedAbstractProperty()
	{
		return refinedAbstract;
	}
	
	public SimpleIntegerProperty documentRankProperty()
	{
		return documentRank;
	}
	
	public void setOriginalAbstract(SimpleStringProperty originalAbstract)
	{
		this.originalAbstract = originalAbstract;
	}

	public void setRefinedAbstract(SimpleStringProperty refinedAbstract)
	{
		this.refinedAbstract = refinedAbstract;
	}

	public String getDocumentTitle()
	{
		return documentTitle.get();
	}
	
	/**
	 * Determines whether instance contains specified string in one of it's attributes.
	 * @param term
	 * @return
	 */
	public boolean containsTerm(final String term)
	{
		return 	term == null														||
				documentTitle.get().toLowerCase().contains(term.toLowerCase()) 		||
				originalAbstract.get().toLowerCase().contains(term.toLowerCase())	||
				refinedAbstract.get().toLowerCase().contains(term.toLowerCase())	||
				String.valueOf((documentRank.get())).toLowerCase().contains(term.toLowerCase());
			
	}
	
	/**
	 * Count occurences of all terms in all fields.
	 * @param term
	 * @return
	 */
	public int countTermOccurances(final String term)
	{
		this.occurenceCount = 	getTermOccurenceInTitle(term) +
								getTermOccurenceInKeywords(term) + 
								getTermOccurenceInOriginalAbstract(term) + 
								getTermOccurenceInRefinedAbstract(term);
		
		return this.occurenceCount;
	}
	
	/**
	 * Determine number of search term's occurences in title. 
	 * @param term
	 * @return
	 */
	private int getTermOccurenceInTitle(final String term)
	{
		int count 		= 0;
		int currIndex	= 0;
		
		if (documentTitle != null && documentTitle.get() != null && term != null) {
			while ( (currIndex = documentTitle.get().toLowerCase().indexOf(term.toLowerCase(), currIndex)) >= 0 ) {
				count++;
				currIndex++;
			}
		}
		
		return count;
	}
	
	/**
	 * Determine number of search term's occurences in keywords. 
	 * @param term
	 * @return
	 */
	private int getTermOccurenceInKeywords(final String term)
	{
		int count 		= 0;
		int currIndex	= 0;
		
		if (keywords != null && term != null) {
			while ( (currIndex = keywords.toLowerCase().indexOf(term.toLowerCase(), currIndex)) >= 0 ) {
				count++;
				currIndex++;
			}
		}
		
		return count;
	}
	
	/**
	 * Determine number of search term's occurences in refined abstract. 
	 * @param term
	 * @return
	 */
	private int getTermOccurenceInRefinedAbstract(final String term)
	{
		int count 		= 0;
		int currIndex	= 0;
		
		if (refinedAbstract != null && refinedAbstract.get() != null && term != null) {
			while ( (currIndex = refinedAbstract.get().toLowerCase().indexOf(term.toLowerCase(), currIndex)) >= 0 ) {
				count++;
				currIndex++;
			}
		}
		
		return count;
	}
	
	/**
	 * Determine number of search term's occurences in original abstract. 
	 * @param term
	 * @return
	 */
	private int getTermOccurenceInOriginalAbstract(final String term)
	{
		int count 		= 0;
		int currIndex	= 0;
		
		if (originalAbstract != null && originalAbstract.get() != null && term != null) {
			while ( (currIndex = originalAbstract.get().toLowerCase().indexOf(term.toLowerCase(), currIndex)) >= 0 ) {
				count++;
				currIndex++;
			}
		}
		
		return count;
	}

	public String getKeywords()
	{
		return keywords;
	}
}
