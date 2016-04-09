package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class TagRefinery_Preprocessor
{
	public static void main(String[] args)
	{
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/abstracts_for_tagRefinery.csv"), "utf-8"))) {
			BufferedReader br = new BufferedReader(new FileReader("data/abstracts.txt"));
			try {
			    String line = br.readLine();
			    int count = 0;
			    while (line != null) {
			        line = br.readLine();

			        if (line != null) {
			        	System.out.println(line);
			        	writer.write(String.valueOf(count++) + "," + line.replaceAll(",", "; ") + ",1\n");
//			        	writer.write(line + ",1\n");
			        }
			    }
			}
			
			finally {
			    br.close();
			}
		}
		
		catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
