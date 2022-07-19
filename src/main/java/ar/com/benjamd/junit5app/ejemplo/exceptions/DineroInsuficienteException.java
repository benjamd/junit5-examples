package ar.com.benjamd.junit5app.ejemplo.exceptions;

public class DineroInsuficienteException extends  RuntimeException{

    public DineroInsuficienteException(String message) {
        super(message);
    }
}
