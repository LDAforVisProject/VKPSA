package control;

import java.net.URL;
import java.util.ResourceBundle;

import model.workspace.WorkspaceAction;
import javafx.fxml.FXML;
import javafx.scene.chart.Axis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;

public class AnalysisController extends Controller
{
	private @FXML ScatterChart<Number, Number> scatterchart_global;
	
	private Axis<Number> xAxis;
	private Axis<Number> yAxis;
	
	private double[][] coordinates;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing SII_AnalysisController.");
		
		// Init scatterchart.
		xAxis = scatterchart_global.getXAxis();
        yAxis = scatterchart_global.getYAxis();
        
        xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);
	}
	
	/**
	 * Refresh scatterchart with data from coordinate. 
	 */
	public void refreshScatterchart()
	{	
		// Calculate/load MDS'ed data from test data archive.
		coordinates = workspace.executeWorkspaceAction(WorkspaceAction.ALL);
		
        final Series<Number, Number> dataSeries = new XYChart.Series<>();
        dataSeries.setName("Data");
        
        for (int i = 0; i < coordinates[0].length; i++) {
        	dataSeries.getData().add(new XYChart.Data<Number, Number>(coordinates[0][i], coordinates[1][i]));
        }
        
        // Add data in scatterchart.
        scatterchart_global.getData().add(dataSeries);
	}
}
