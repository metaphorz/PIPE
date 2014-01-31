package pipe.views;

import javafx.util.Pair;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.*;

public class ZoomUI extends LayerUI<JComponent> {
    public double zoom = 1;

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.scale(zoom, zoom);
        super.paint(g2, c);
        g2.dispose();
    }

    @Override
    protected void processMouseEvent(MouseEvent e, JLayer<? extends JComponent> l) {
        MouseEvent localEvent = translateToLayerCoordinates(e, l);
        Component component = getComponentClickedOn(l, localEvent);

        if (localEvent.getID() == MouseEvent.MOUSE_PRESSED) {

            for (ActionListener listener : component.getListeners(ActionListener.class)) {
                ActionEvent actionEvent = new ActionEvent(component, localEvent.getID(), "CLICK");
                listener.actionPerformed(actionEvent);
            }
            for (MouseListener listener : component.getListeners(MouseListener.class)) {
                listener.mousePressed(getNewMouseClickEvent(component, localEvent));
            }
        } else if (localEvent.getID() == MouseEvent.MOUSE_RELEASED) {
            for (MouseListener listener : component.getListeners(MouseListener.class)) {
                listener.mouseReleased(getNewMouseClickEvent(component, localEvent));
            }
        }
        e.consume();
    }

    private MouseEvent getNewMouseClickEvent(Component component, MouseEvent mouseEvent) {
        Pair<Integer, Integer> coordinates = zoomedXY(mouseEvent);
        return new MouseEvent(component, mouseEvent.getID(), mouseEvent.getWhen(), mouseEvent.getModifiers(), coordinates.getKey(), coordinates.getValue(),
                mouseEvent.getClickCount(), mouseEvent.isPopupTrigger(), mouseEvent.getButton());
    }

    /**
     * @param e
     * @param layer
     * @return a new event with x y pointing to the coordinate space of the layer
     * rather than the whole application
     */
    private MouseEvent translateToLayerCoordinates(MouseEvent e, JLayer<? extends JComponent> layer) {
        return SwingUtilities.convertMouseEvent(e.getComponent(), e, layer);
    }

    /**
     * @param l
     * @param e mouse event with coordinates releative to l
     * @return component in l clicked on
     */
    private Component getComponentClickedOn(JLayer<? extends JComponent> l, MouseEvent e) {

        Pair<Integer, Integer> coordinates = zoomedXY(e);
        return l.getView().getComponentAt(coordinates.getKey(), coordinates.getValue());
    }

    /**
     * @param e mouse click event
     * @return the events x y coordinates zoomed
     */
    private Pair<Integer, Integer> zoomedXY(MouseEvent e) {
        int x = e.getX() == 0 ? 0 : (int) (e.getX() / zoom);
        int y = e.getY() == 0 ? 0 : (int) (e.getY() / zoom);
        return new Pair<>(x, y);
    }

    //TODO: Remove duplication of x,y
    @Override
    protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JComponent> l) {
        MouseEvent localEvent = translateToLayerCoordinates(e, l);
        Component component = getComponentClickedOn(l, localEvent);
        if (localEvent.getID() == MouseEvent.MOUSE_MOVED) {
            for (MouseMotionListener listener : l.getView().getListeners(MouseMotionListener.class)) {
                listener.mouseMoved(getNewMouseClickEvent(component, localEvent));
            }
        } else if (localEvent.getID() == MouseEvent.MOUSE_DRAGGED) {
            for (MouseMotionListener listener : component.getListeners(MouseMotionListener.class)) {
                listener.mouseDragged(getNewMouseClickEvent(component, localEvent));
            }
        }
        e.consume();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        JLayer<? extends JComponent> jlayer = (JLayer<? extends JComponent>) c;
        jlayer.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    @Override
    public void uninstallUI(JComponent c) {
        JLayer<? extends JComponent> jlayer = (JLayer<? extends JComponent>) c;
        jlayer.setLayerEventMask(0);
        super.uninstallUI(c);
    }
}