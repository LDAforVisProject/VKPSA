package view.components;

import java.util.ArrayList;
import java.util.Map;

import control.analysisView.AnalysisController;
import model.LDAConfiguration;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/** 
 * A node which displays a value on hover, but is otherwise empty. Used in DistanceDifferenceCorrelationLinechart.
 * Source: https://gist.github.com/jewelsea/4681797 (modified)
 * @deprecated
 */
public class HoveredThresholdNode extends StackPane
{
	private Map<String, Double> stepValues;
	private ArrayList<String> coupledParameters;
	
	public HoveredThresholdNode(AnalysisController analysisController, double priorValue, double value, 
								Map<String, Double> stepValues, ArrayList<String> coupledParameters)
	{
		this.stepValues				= stepValues;
		this.coupledParameters		= coupledParameters;
		
		if (value == -Double.MAX_VALUE) {
			setPrefSize(0, 0);
		}
		
		else {
			setPrefSize(7, 7);
			
			setOnMouseEntered(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent mouseEvent)
				{
					VBox vbox_lables = new VBox();
					ArrayList<Label> labels = createDataThresholdLabels(priorValue, value);
					vbox_lables.getChildren().addAll(labels);
					vbox_lables.getStyleClass().addAll("chart-line-symbol");
					
//					analysisController.updateLinechartInfo(true, labels);
					
					HoveredThresholdNode target = (HoveredThresholdNode)mouseEvent.getSource();
					target.setScaleX(2.25);
					target.setScaleY(2.25);
					
					setCursor(Cursor.NONE);
					toFront();
				}
			});
			
			setOnMouseExited(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent mouseEvent)
				{
//					analysisController.updateLinechartInfo(false, null);
					getChildren().clear();
					
					HoveredThresholdNode target = (HoveredThresholdNode)mouseEvent.getSource();
					target.setScaleX(1);
					target.setScaleY(1);
					
					setCursor(Cursor.DEFAULT);
				}
			});
		}
	}

	/**
	 * Create labels for data points.
	 * @param priorValue
	 * @param value
	 * @return List of created labels.
	 */
	private ArrayList<Label> createDataThresholdLabels(double priorValue, double value)
	{
		final ArrayList<Label> labels	= new ArrayList<Label>();
		final int decimalPlaces			= 4;
		
		// Output coupled parametes.
		for (String param : coupledParameters) {
			// Add label to list.
			labels.add(generateLabel(param, decimalPlaces, priorValue, value, true));
		}
		
		// Output free parameters.
		for (String param : LDAConfiguration.SUPPORTED_PARAMETERS) {
			if (!coupledParameters.contains(param)) {
				// Add label to list.
				labels.add(generateLabel(param, decimalPlaces, priorValue, value, false));
			}
		}
		
		return labels;
	}
	
	private void setLabelStyle(Label label, double priorValue, double value, boolean isCoupledParameter)
	{
		// Set label style.
		label.getStyleClass().addAll("default-color0", "chart-series-line");
		
		if (isCoupledParameter)
			label.setStyle("-fx-font-size: 10; -fx-font-weight: bold;");
		else
			label.setStyle("-fx-font-size: 10;");
		
		if (priorValue == 0) {
			label.setTextFill(Color.DARKGRAY);
		}
		
		else if (value > priorValue) {
			label.setTextFill(Color.FORESTGREEN);
		} 
		
		else {
			label.setTextFill(Color.FIREBRICK);
		}
		
		label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
	}
	
	private Label generateLabel(String param, int decimalPlaces, double priorValue, double value, boolean isCoupledParameter)
	{
		String shortenedStepValue	 = String.valueOf(stepValues.get(param));
		shortenedStepValue			 = shortenedStepValue.length() > decimalPlaces ? shortenedStepValue.substring(0, decimalPlaces) : shortenedStepValue; 

		Label label = new Label();
		label.setText(param + " = " + shortenedStepValue);
		
		// Set label style.
		setLabelStyle(label, priorValue, value, isCoupledParameter);
		
		return label;
	}
}