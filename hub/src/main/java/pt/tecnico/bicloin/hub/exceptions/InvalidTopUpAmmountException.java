package pt.tecnico.bicloin.hub.exceptions;

public class InvalidTopUpAmmountException extends Exception {

    public InvalidTopUpAmmountException (){
        super("Invalid amount, must be between 1 and 20, inclusive!");
    }

}
