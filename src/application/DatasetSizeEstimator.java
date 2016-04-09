package application;

import java.math.BigDecimal;

public class DatasetSizeEstimator
{
	public static void main(String[] args)
	{
		// -----------------------------------------------
			// 		Actual (raw and (pre-)processed) data
			// -----------------------------------------------
			
			int n						= 1000;
			int numberOfTopics			= 20;
			int numberOfKeywords		= 14000;
			int keywordStringSize		= 15;
			int primitiveSizeInBytes	= 8;
			
			// Calculated sizes.
			float size_MDS				= n * 2 * primitiveSizeInBytes;
			BigDecimal size_distances	= new BigDecimal(1);
			BigDecimal size_topicDist	= new BigDecimal(1);
			BigDecimal size_rawData		= new BigDecimal(1);
			
			// Topic model size_distances: n * n * primitiveSizeInBytes / 2
			size_distances = size_distances.multiply(new BigDecimal(n));
			size_distances = size_distances.multiply(new BigDecimal(n));
			size_distances = size_distances.multiply(new BigDecimal(primitiveSizeInBytes * 2));
			
			size_distances = size_distances.divide(new BigDecimal(2));
			size_distances = size_distances.divide(new BigDecimal(1024));
			size_distances = size_distances.divide(new BigDecimal(1024));
			size_distances = size_distances.divide(new BigDecimal(1024));
			
			// size_topicDist: n * numberOfTopics * n * numberOfTopics * primitiveSizeInBytes / 2
			size_topicDist = size_topicDist.multiply(new BigDecimal(n));
			size_topicDist = size_topicDist.multiply(new BigDecimal(n));
			size_topicDist = size_topicDist.multiply(new BigDecimal(numberOfTopics));
			size_topicDist = size_topicDist.multiply(new BigDecimal(numberOfTopics));
			size_topicDist = size_topicDist.multiply(new BigDecimal(primitiveSizeInBytes * 2));
			
			size_topicDist = size_topicDist.divide(new BigDecimal(2));
			size_topicDist = size_topicDist.divide(new BigDecimal(1024));
			size_topicDist = size_topicDist.divide(new BigDecimal(1024));
			size_topicDist = size_topicDist.divide(new BigDecimal(1024));
			
			// size_rawData: n * numberOfTopics * numberOfKeywords * (keywordStringSize + primitiveSizeInBytes)
			size_rawData = size_rawData.multiply(new BigDecimal(n));
			size_rawData = size_rawData.multiply(new BigDecimal(numberOfTopics));
			size_rawData = size_rawData.multiply(new BigDecimal(numberOfKeywords));
			size_rawData = size_rawData.multiply(new BigDecimal(keywordStringSize + primitiveSizeInBytes));
			
			size_rawData = size_rawData.divide(new BigDecimal(1024));
			size_rawData = size_rawData.divide(new BigDecimal(1024));
			size_rawData = size_rawData.divide(new BigDecimal(1024));
			
			System.out.println("# Rough estimate of amount of data in or out of memory:");
			System.out.println("# \tEstimated for (n = " + n + ", primitive size in bytes = " + primitiveSizeInBytes + "), without optimizations. ");
			System.out.println("# \tMDS data (in MB)\t\t\t= " + size_MDS / (1024 * 1024));
			System.out.println("# \tTopic model distance data (in GB)\t= " + size_distances);
			System.out.println("# \tTopic distance data (in GB)\t\t= " + size_topicDist);
			System.out.println("# \tRaw data (in GB)\t\t\t= " + size_rawData);
			System.out.println("\n\n");
	}
}
