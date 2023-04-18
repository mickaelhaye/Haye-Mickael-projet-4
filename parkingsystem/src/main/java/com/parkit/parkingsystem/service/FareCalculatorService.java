package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        double inHour = ticket.getInTime().getTime();
        double outHour = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        double duration = (outHour - inHour) / 3600000;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                if (duration<0.5){
                    ticket.setPrice(0);
                }
                else{
                    if(discount){
                        ticket.setPrice(0.95*duration * Fare.CAR_RATE_PER_HOUR);
                    }
                    else{
                        ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                    }
                }
                break;
            }
            case BIKE: {
                if (duration<0.5){
                    ticket.setPrice(0);
                }
                else{
                    if(discount){
                        ticket.setPrice(0.95*duration * Fare.BIKE_RATE_PER_HOUR);
                    }
                    else{
                        ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                    }
                }
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}