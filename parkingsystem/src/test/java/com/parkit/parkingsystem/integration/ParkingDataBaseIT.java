package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    ParkingType parkingType = ParkingType.CAR;
    String readVehicleRegistrationNumber = "ABCDEF";

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

     @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        
        assertNotEquals(0, ticketDAO.getNbTicket(readVehicleRegistrationNumber));
        assertEquals(2, parkingSpotDAO.getNextAvailableSlot(parkingType));
    }

    @Test
    public void testParkingLotExit(){
        testParkingACar();
        //modification of the entry time
        Ticket ticket = ticketDAO.getTicket(readVehicleRegistrationNumber);
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        ticket.setInTime( inTime );
        ticketDAO.updateTicketInTime(ticket);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        ticket = ticketDAO.getTicket(readVehicleRegistrationNumber);

        assertNotEquals(0, ticket.getPrice());
        assertNotEquals(0, ticket.getOutTime());
    }

    @Test
    public void testParkingLotExitRecurringUser(){
        //first passage
    	testParkingLotExit();
    	Ticket ticket = ticketDAO.getTicket(readVehicleRegistrationNumber);
        double priceWithoutRecurringUser = ticket.getPrice();
        
        //second passage
        testParkingLotExit();
        ticket = ticketDAO.getTicket(readVehicleRegistrationNumber);
        double priceWithRecurringUser = ticket.getPrice();
         
        assertEquals(Math.round(priceWithoutRecurringUser*0.95*10.0)/10.0, priceWithRecurringUser);
    }

}