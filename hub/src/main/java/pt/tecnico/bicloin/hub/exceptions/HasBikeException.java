package pt.tecnico.bicloin.hub.exceptions;

public class HasBikeException extends Exception {

    public HasBikeException (){
        super("User already has a bike!");
    }
    
}