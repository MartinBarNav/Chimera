package com.saterskog.cell_lab.apiutils;

public class RequestDeniedException extends Exception{
    public RequestDeniedException(String message){
        super(message);
    }
}
