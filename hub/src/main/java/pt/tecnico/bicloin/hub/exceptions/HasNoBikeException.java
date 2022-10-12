package pt.tecnico.bicloin.hub.exceptions;

public class HasNoBikeException extends Exception {

    public HasNoBikeException (){
        super("User doesn't have a bike to bike down!");
    }

}
