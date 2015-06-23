package view.components;

import javax.xml.soap.Node;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;


/**
 * Based on http://stackoverflow.com/questions/26788034/javafx-rubberband-selection-inside-scrollpane.
 * @author RM
 *
 */
public class RubberBandSelection 
{
	private final DragContext dragContext;
    private Rectangle rect;
    private Pane pane;

    private EventHandler<MouseEvent> test;
    
    private EventHandler<MouseEvent> onMousePressedEventHandler;
    private EventHandler<MouseEvent> onMouseReleasedEventHandler;
    private EventHandler<MouseEvent> onMouseDraggedEventHandler;
    
    public RubberBandSelection(Pane pane) 
    {
    	this.dragContext	= new DragContext();
        this.pane			= pane;

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
    
    private void initEventHandler()
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
	
	        }
	    };
    }

    private final class DragContext 
    {
        public double mouseAnchorX;
        public double mouseAnchorY;
    }

}