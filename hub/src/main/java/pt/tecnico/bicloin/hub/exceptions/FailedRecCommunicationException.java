package pt.tecnico.bicloin.hub.exceptions;

public class FailedRecCommunicationException extends Exception {
 
    public FailedRecCommunicationException () {
        super("Failed communication with rec!");
    }
 
}
