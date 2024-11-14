package com.ecofriendly.ian.exceptions;

public class InvalidVehicleException extends Exception{
    public InvalidVehicleException() {
        super();
    }

    public InvalidVehicleException(String message) {
        super(message);
    }
}
