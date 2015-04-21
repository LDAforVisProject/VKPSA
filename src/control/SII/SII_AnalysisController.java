package control.SII;

import java.net.URL;
import java.util.ResourceBundle;

import model.Dataset;
import model.Workspace;
import model.WorkspaceAction;
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
	
	private Workspace workspace;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing SII_AnalysisController.");
		
		// Init scatterchart. @todo Get maxima and minima automatically.
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
//		coordinates = workspace.test_sampleData(false);
		coordinates = workspace.executeWorkspaceAction(WorkspaceAction.LOAD_MDS_COORDINATES);
		
        final Series<Number, Number> dataSeries = new XYChart.Series<>();
        dataSeries.setName("Testdata");
        
        for (int i = 0; i < coordinates[0].length; i++) {
        	dataSeries.getData().add(new XYChart.Data<Number, Number>(coordinates[0][i], coordinates[1][i]));
        }
        
        // Add data in scatterchart.
        scatterchart_global.getData().add(dataSeries);
	}
	
	public void setWorkspace(Workspace workspace)
	{
		this.workspace = workspace;
	}
}
