package model.documents;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class KeywordContext
{
	private int documentID;
	private String keyword;
	private int occurenceCount;
	private SimpleIntegerProperty documentRank;
	private SimpleStringProperty documentTitle;
	private SimpleStringProperty originalAbstract;
	private SimpleStringProperty refinedAbstract;
	
	public KeywordContext(int documentID, String keyword, int documentRank, String documentTitle, int occurenceCount, String originalAbstract, String refinedAbstract)
	{
		this.documentID 		= documentID;
		this.keyword 			= keyword;
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

	public String getKeyword()
	{
		return keyword;
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
		return 	term == null							||
				documentTitle.get().contains(term) 		||
				originalAbstract.get().contains(term)	||
				refinedAbstract.get().contains(term)	||
				String.valueOf((documentRank.get())).contains(term);
			
	}
	
	/**
	 * Determine number of search term's occurences in title. 
	 * @param term
	 * @return
	 */
	public int getKeywordOccurenceInTitle(final String term)
	{
		int count 		= 0;
		int currIndex	= 0;
		
		if (documentTitle != null && documentTitle.get() != null && term != null) {
			while ( (currIndex = documentTitle.get().indexOf(term, currIndex)) >= 0 ) {
				count++;
				currIndex++;
			}
		}
		
		return count;
	}
}
