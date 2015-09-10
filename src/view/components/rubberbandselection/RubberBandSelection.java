package view.components.rubberbandselection;

import javax.xml.soap.Node;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Pair;


/**
 * Based on http://stackoverflow.com/questions/26788034/javafx-rubberband-selection-inside-scrollpane.
 * @author RM
 *
 */
public class RubberBandSelection 
{
	protected final DragContext dragContext;
    protected Rectangle rect;
    protected Pane pane;

    protected EventHandler<MouseEvent> test;
    
    protected EventHandler<MouseEvent> onMousePressedEventHandler;
    protected EventHandler<MouseEvent> onMouseReleasedEventHandler;
    protected EventHandler<MouseEvent> onMouseDraggedEventHandler;
    
    // Selectable component as listener.
    ISelectableComponent listener;
    
    public RubberBandSelection(Pane pane, ISelectableComponent listener) 
    {
    	this.dragContext	= new DragContext();
        this.pane			= pane;
        this.listener		= listener;
        
        rect 				= new Rectangle( 0,0,0,0);
        rect.setStroke(Color.BLUE);
        rect.setStrokeWidth(1);
        rect.setStrokeLineCap(StrokeLineCap.ROUND);
        rect.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));

        // Init event handler.
        initEventHandler();
        
        // Set event handler.
        pane.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
        pane.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
        pane.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);
    }
    
	public void enable()
    {
	    pane.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
	    pane.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
	    pane.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);
    }
    
    public void disable()
    {
    	pane.removeEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
    	pane.removeEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
    	pane.removeEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);
    }
    
    protected void initEventHandler()
    {
    	onMousePressedEventHandler = new EventHandler<MouseEvent>() 
	    {
	        @Override
	        public void handle(MouseEvent event) 
	        {
	        	dragContext.mouseAnchorX = event.getX();
	            dragContext.mouseAnchorY = event.getY();
	
	            rect.setX(dragContext.mouseAnchorX);
	            rect.setY(dragContext.mouseAnchorY);
	            rect.setWidth(0);
	            rect.setHeight(0);
	
	            pane.getChildren().add( rect);
	            
	            // Translate coordinates.
	            translateToVisualization(dragContext.mouseAnchorX, dragContext.mouseAnchorY, dragContext.mouseAnchorX, dragContext.mouseAnchorY);
	        }
	    };
	
	    onMouseReleasedEventHandler = new EventHandler<MouseEvent>() 
	    {
	        @Override
	        public void handle(MouseEvent event) 
	        {
	            // reset selection rectangle
	            rect.setX(0);
	            rect.setY(0);
	            rect.setWidth(0);
	            rect.setHeight(0);
	
	            pane.getChildren().remove( rect);
	            
	            // Translate coordinates.
	            signalEndOfSelection();
	        }
	    };
	
	    onMouseDraggedEventHandler = new EventHandler<MouseEvent>() 
	    {
	        @Override
	        public void handle(MouseEvent event) 
	        {
	        	double sceneX = event.getX();
	            double sceneY = event.getY();
	            
	            double offsetX = sceneX - dragContext.mouseAnchorX;
	            double offsetY = sceneY - dragContext.mouseAnchorY;
	
	            if( offsetX > 0)
	                rect.setWidth( offsetX);
	            else {
	                rect.setX( sceneX);
	                rect.setWidth(dragContext.mouseAnchorX - rect.getX());
	            }
	
	            if( offsetY > 0) {
	                rect.setHeight( offsetY);
	            } 
	            else {
	                rect.setY( sceneY);
	                rect.setHeight(dragContext.mouseAnchorY - rect.getY());
	            }
	            
	            // Translate coordinates.
	            translateToVisualization(sceneX, sceneY, dragContext.mouseAnchorX, dragContext.mouseAnchorY);
	        }
	    };
    }

    protected final class DragContext 
    {
        public double mouseAnchorX;
        public double mouseAnchorY;
    }

    /**
     * Translates recorded coordinates to actual coordinates in visualization. 
     * @param endX
     * @param endY
     * @param startX
     * @param startY
     */
	protected void translateToVisualization(double endX, double endY, double startX, double startY)
	{
		Pair<Integer, Integer> offset	= listener.provideOffsets();
		final int offsetX				= offset.getKey();
		final int offsetY				= offset.getValue();
		
		double maxX = 0;
		double minX = 0;
		double maxY = 0;
		double minY = 0;
		
		if (endX >= startX) {
			maxX = endX - offsetX;
			minX = startX - offsetX;
		}
		
		else {
			minX = endX - offsetX;
			maxX = startX - offsetX;
		}
		
		if (endY >= startY) {
			maxY = endY - offsetY;
			minY = startY - offsetY;
		}
		
		else {
			minY = endY - offsetY;
			maxY = startY - offsetY;
		}
		
		// Pass selection information to listener. 
		listener.processSelectionManipulationRequest(minX, minY, maxX, maxY);
	}
	
	private void signalEndOfSelection()
	{
		listener.processEndOfSelectionManipulation();
	}
}