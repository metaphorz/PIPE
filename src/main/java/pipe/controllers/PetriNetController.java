package pipe.controllers;

import pipe.controllers.interfaces.IController;
import pipe.historyActions.*;
import pipe.models.PetriNet;
import pipe.models.component.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.*;

public class PetriNetController implements IController, Serializable {

    private final HistoryManager historyManager;
    private final PetriNet petriNet;
    private int placeNumber = 0;
    private int transitionNumber = 0;
    private boolean currentlyCreatingArc = false;
    private NormalArc arc;

    private final Set<PetriNetComponent> selectedComponents = new
            HashSet<PetriNetComponent>();

    public PetriNetController(PetriNet model, HistoryManager historyManager) {
        petriNet = model;
        this.historyManager = historyManager;
    }

    public void addArcPoint(double x, double y, boolean shiftDown) {
        if (currentlyCreatingArc) {
            arc.setTarget(new TemporaryArcTarget(x, y));

            petriNet.notifyObservers();
        }
    }

    /**
     * Starts creating an arc from the source.
     *
     * @param source source model
     */
    //TODO: handle different arc types.
    public void startCreatingArc(Connectable source) {
        currentlyCreatingArc = true;
        this.arc = buildEmptyArc(source);
        addArcToCurrentPetriNet(arc);
    }

    private void addArcToCurrentPetriNet(NormalArc arc) {
        petriNet.addArc(arc);
    }

    private NormalArc buildEmptyArc(Connectable source) {
        return new NormalArc(source,
                             new TemporaryArcTarget(source.getX(),
                                                    source.getY()),
                             new HashMap<Token, String>());
    }

    public boolean isCurrentlyCreatingArc() {
        return currentlyCreatingArc;
    }

    public void cancelArcCreation() {
        currentlyCreatingArc = false;
        petriNet.remove(arc);
    }

    public void finishCreatingArc(Connectable target) {
        arc.setTarget(target);
        historyManager.addNewEdit(new AddPetriNetObject(arc, petriNet));
        currentlyCreatingArc = false;
    }

    /**
     * Returns true if creatingArc and if the potentialEnd is not of
     * the same class as the source.
     *
     * @param potentialEnd
     * @return true if arc can end on the connectable
     */
    public boolean isApplicableEndPoint(Connectable potentialEnd) {
        if (currentlyCreatingArc && potentialEnd.isEndPoint()) {
            return potentialEnd.getClass() != arc.getSource().getClass();
        }
        return false;
    }

    /**
     * Creates unique petri net numbers for each tab
     *
     * @return A unique number for the petrinet in the current tab
     */
    public int getUniquePlaceNumber() {
        int returnValue = placeNumber;
        placeNumber++;
        return returnValue;
    }

    /**
     * Creates unique petri net numbers for each tab
     *
     * @return A unique number for the petrinet in the current tab
     */
    public int getUniqueTransitionNumber() {
        int returnValue = transitionNumber;
        transitionNumber++;
        return returnValue;
    }

    public void select(PetriNetComponent component) {
        selectedComponents.add(component);
    }

    public boolean isSelected(PetriNetComponent component) {
        return selectedComponents.contains(component);
    }

    public void deselect(PetriNetComponent component) {
        selectedComponents.remove(component);
    }

    public void deselectAll() {
        selectedComponents.clear();
    }

    public void translateSelected(Point2D.Double translation) {
        for (PetriNetComponent component : selectedComponents) {
            if (component instanceof Connectable) {
                Connectable connectable = (Connectable) component;
                connectable.setX(connectable.getX() + translation.getX());
                connectable.setY(connectable.getY() + translation.getY());
            }

        }
        petriNet.notifyObservers();
    }


    /**
     * Selects all components within this rectangle
     *
     * @param selectionRectangle
     */
    public void select(Rectangle selectionRectangle) {
        for (Place place : petriNet.getPlaces()) {
            selectConnectable(place, selectionRectangle);
        }
        for (Transition transition : petriNet.getTransitions()) {
            selectConnectable(transition, selectionRectangle);
        }
        for (Arc arc : petriNet.getArcs()) {
            if (selectedComponents.contains(arc.getSource()) ||
                    selectedComponents.contains(arc.getTarget())) {
                select(arc);
            }
        }

    }

    /**
     * Currently must be of type Connectable, since yhis is the only abstract
     * class containing getters for X and Y
     *
     * @param connectable        object to select
     * @param selectionRectangle
     */
    private void selectConnectable(Connectable connectable,
                                   Rectangle selectionRectangle) {
        int x = new Double(connectable.getX()).intValue();
        int y = new Double(connectable.getY()).intValue();
        Rectangle rectangle = new Rectangle(x, y, connectable.getHeight(),
                                            connectable.getWidth());
        if (selectionRectangle.intersects(rectangle)) {
            select(connectable);
        }
    }

    /**
     * Deletes selection and adds to history manager
     */
    public void deleteSelection() {
        historyManager.newEdit();

        for (PetriNetComponent component : selectedComponents) {
            deleteComponent(component);
        }
        selectedComponents.clear();
    }

    /**
     * Deletes single component, starts a newEdit for history manager
     * @param component
     */
    public void delete(PetriNetComponent component) {
        historyManager.newEdit();
        deleteComponent(component);
    }

    /**
     * Deletes a component adding it to the history managers current edit
     * @param component
     */
    private void deleteComponent(PetriNetComponent component) {
        petriNet.remove(component);
        DeletePetriNetObject deleteAction = new DeletePetriNetObject(component, petriNet);
        historyManager.addEdit(deleteAction);
    }

    /**
     *
     * @param name token name to find
     * @return Token from PetriNet
     * @throw RuntimeException if the token does not exist
     */
    private Token getTokenForName(String name) {
        //TODO: Find an O(1) way to do this.... maybe map id to name?
        for (Token token : petriNet.getTokens()) {
            if (token.getId().equals(name)) {
                return token;
            }
        }
        throw new RuntimeException("No " + name + " token found in current petri net");
    }

    /**
     * Adds a new token to the petrinet
     * @param name
     * @param enabled
     * @param color
     */
    public void createNewToken(String name, boolean enabled, Color color) {
        Token token = new Token(name, enabled, 0, color);
        petriNet.addToken(token);
    }

    public Collection<Token> getNetTokens() {
        return petriNet.getTokens();
    }

    public void updateToken(String currentTokenName, String name, Boolean enabled, Color color) {
        petriNet.getToken(currentTokenName);
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public PetriNet getPetriNet() {
        return petriNet;
    }


    public Token getToken(String tokenName) {
        return getTokenForName(tokenName);
    }



    public ArcController getArcController(Arc arc) {
        return new ArcController(arc, historyManager);
    }

    public PlaceController getPlaceController(Place place) {
        return new PlaceController(place, historyManager);
    }

    public TransitionController getTransitionController(
            final Transition transition) {
        return new TransitionController(transition, historyManager);
    }
}
