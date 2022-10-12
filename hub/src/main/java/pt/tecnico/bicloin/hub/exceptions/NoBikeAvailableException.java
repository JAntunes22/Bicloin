package pt.tecnico.bicloin.hub.exceptions;

public class NoBikeAvailableException extends Exception {

    public NoBikeAvailableException (){
        super("No bikes availbale in this station!");
    }

}
