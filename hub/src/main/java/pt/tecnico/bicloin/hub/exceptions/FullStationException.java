package pt.tecnico.bicloin.hub.exceptions;

public class FullStationException extends Exception {

    public FullStationException (){
        super("This station is full, no docks available!");
    }

}
 