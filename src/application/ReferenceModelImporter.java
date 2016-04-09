package application;

import database.DBManagement;

public class ReferenceModelImporter
{
	public static void main(String[] args)
	{

		DBManagement dbm = new DBManagement("D:\\VKPSA_exe\\data\\vkpsa.db");
		dbm.importReferenceTopicModel("D:\\VKPSA_exe\\data\\referenceTopicModel.csv");
	}
		
}
