package model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import mdsj.MDSJ;

/**
 * One instance of dataset corresponds to the output of a execution of the LDA
 * algorithm with one specific set of parameters.
 * @author RM
 *
 */
public class Dataset
{
	private LDAConfiguration parametrization;
	
	private ArrayList<Topic> topics;

	private Pair<Double, Double> calculatedMDSCoordinate;
	
	private double minimum;
	private double maximum;
	private double average;
	private double median;
	
	public Dataset(final LDAConfiguration parametrization)
	{
		this.parametrization	= parametrization;
		this.topics				= new ArrayList<Topic>();
	}
	
	public Dataset(final LDAConfiguration parametrization, final ArrayList<Topic> topics)
	{
		this.parametrization	= parametrization;
		this.topics				= new ArrayList<Topic>(topics);
	}
	
	public void setTopics(final ArrayList<Topic> topics)
	{
		this.topics.clear();
		this.topics.addAll(topics);
	}
	
	public ArrayList<Topic> getTopics()
	{
		return topics;
	}
}
