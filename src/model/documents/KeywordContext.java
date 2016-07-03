package model.documents;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class KeywordContext
{
	private int id;
	private String keyword;
	private SimpleIntegerProperty documentRank;
	private SimpleStringProperty documentTitle;
	private SimpleStringProperty originalAbstract;
	private SimpleStringProperty refinedAbstract;
	
	public KeywordContext(int id, String keyword, int documentRank, String documentTitle, String originalAbstract, String refinedAbstract)
	{
		this.id 				= id;
		this.keyword 			= keyword;
		this.documentRank		= new SimpleIntegerProperty(documentRank);
		this.documentTitle 		= new SimpleStringProperty(documentTitle);
		this.originalAbstract 	= new SimpleStringProperty(originalAbstract);
		this.refinedAbstract 	= new SimpleStringProperty(refinedAbstract);
	}

	public int getID()
	{
		return id;
	}

	public String getKeyword()
	{
		return keyword;
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
}
