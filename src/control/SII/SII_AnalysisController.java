package control.SII;

import java.net.URL;
import java.util.ResourceBundle;

import model.Dataset;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;

public class SII_AnalysisController implements Initializable
{
	private @FXML ScatterChart<Number, Number> scatterchart_global;
	
	private Axis<Number> xAxis;
	private Axis<Number> yAxis;
	
	private double[][] coordinates;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing SII_AnalysisController.");
		
		// Calculate/load MDS'ed data from test data archive.
		coordinates = Dataset.sampleTestData(true);
		
		// Init scatterchart. @todo Get maxima and minima automatically.
		xAxis = scatterchart_global.getXAxis();
        yAxis = scatterchart_global.getYAxis();//new NumberAxis(-0.5, 0.5, 0.0001);
        
        xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);
        
        // Load data @todo Load data dynamically, not just at initialization.
        final Series<Number, Number> dataSeries = new XYChart.Series<>();
        dataSeries.setName("Testdata");
        
        for (int i = 0; i < coordinates[0].length; i++) {
        	dataSeries.getData().add(new XYChart.Data<Number, Number>(coordinates[0][i], coordinates[1][i]));
        }
        
        // Add data in scatterchart.
        scatterchart_global.getData().addAll(dataSeries);
	}
	
	public void drawScatterplot()
	{
		
	}
}
