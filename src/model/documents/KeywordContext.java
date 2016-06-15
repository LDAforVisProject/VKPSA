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

	public SimpleStringProperty getDocumentTitle()
	{
		return documentTitle;
	}

	public SimpleStringProperty getOriginalAbstract()
	{
		return originalAbstract;
	}

	public SimpleStringProperty getRefinedAbstract()
	{
		return refinedAbstract;
	}	
}
