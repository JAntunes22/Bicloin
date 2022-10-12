package pt.tecnico.bicloin.hub.exceptions;

public class UnreachableStationException extends Exception {

    public UnreachableStationException (){
        super("This station is unreachable (more than 200 meters)!");
    }
    
}
