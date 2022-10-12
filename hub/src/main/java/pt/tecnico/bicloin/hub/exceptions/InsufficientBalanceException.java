package pt.tecnico.bicloin.hub.exceptions;

public class InsufficientBalanceException extends Exception {

    public InsufficientBalanceException (){
        super("Insufficient balance to bike up, necessary to top up!");
    }

}
