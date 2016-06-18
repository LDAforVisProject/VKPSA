package model.documents;

import javafx.beans.property.SimpleStringProperty;

public class KeywordContext
{
	private int id;
	private String keyword;
	private SimpleStringProperty documentTitle;
	private SimpleStringProperty originalAbstract;
	private SimpleStringProperty refinedAbstract;
	
	public KeywordContext(int id, String keyword, String documentTitle, String originalAbstract, String refinedAbstract)
	{
		this.id 				= id;
		this.keyword 			= keyword;
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
	
	public void setOriginalAbstract(SimpleStringProperty originalAbstract)
	{
		this.originalAbstract = originalAbstract;
	}

	public void setRefinedAbstract(SimpleStringProperty refinedAbstract)
	{
		this.refinedAbstract = refinedAbstract;
	}

	public SimpleStringProperty getDocumentTitle()
	{
		return documentTitle;
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
				refinedAbstract.get().contains(term);
			
	}
}
