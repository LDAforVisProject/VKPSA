package model.documents;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * Document class containing fewer attributes. Used in table in DocumentLookup.
 * @author RM
 *
 */
public class DocumentForLookupTable
{
	private final int documentID;
	private SimpleIntegerProperty rankProperty;
	private final SimpleFloatProperty probability;
	private final SimpleStringProperty title;
	private final SimpleStringProperty authors;
	private final SimpleStringProperty date;
	private final SimpleStringProperty conference;
	
	public DocumentForLookupTable(int documentID, int rank, float probability, String title, String authors, String date, String conference)
	{
		this.documentID			= documentID;
		this.rankProperty		= new SimpleIntegerProperty(rank);
		this.probability		= new SimpleFloatProperty(probability);
		this.title				= new SimpleStringProperty(title);
		this.authors			= new SimpleStringProperty(authors);
		this.date				= new SimpleStringProperty(date);
		this.conference			= new SimpleStringProperty(conference);
	}
	
	public int getDocumentID()
	{
		return documentID;
	}

	public String getTitle()
	{
		return title.get();
	}
	
	public String getAuthors()
	{
		return authors.get();
	}
	
	public String getDate()
	{
		return date.get();
	}
	
	public String getConference()
	{
		return conference.get();
	}

	public float getProbability()
	{
		return probability.get();
	}
	
	public int getRank()
	{
		return rankProperty.get();
	}
	
	public SimpleIntegerProperty rankProperty()
	{
		return rankProperty;
	}
}
