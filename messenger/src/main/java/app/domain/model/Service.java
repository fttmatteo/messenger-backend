package app.domain.model;

import java.sql.Date;
import java.io.ObjectInputFilter.Status;

import app.domain.model.enums.*;

public class Service {
    private Long id_service;
    private String plate;
    private TypePlate type_plate;
    private Status status;
    private Date creation_date;
    private Date delivery_date;
    private Employee messenger;
    private String description;
    
}
