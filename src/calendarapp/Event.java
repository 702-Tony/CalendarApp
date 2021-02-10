/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calendarapp;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

/**
 *
 * @author Anthony Adams
 */
public class Event {

    private int appointmentId;
    private int userId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String title;
    private String description;
    private String location;
    private int customerId;
    private String type;
    private String contact;
    
    Event(LocalDateTime startDate, 
            String title, 
            String description, 
            int customerId, 
            String type, 
            int appointmentId, 
            LocalDateTime endDate,
            String location,
            int userId,
            String contact) {
        // constructor
        this.startDate = startDate;
        this.title = title;
        this.description = description;
        this.customerId = customerId;
        this.type = type;
        this.appointmentId = appointmentId;
        this.endDate = endDate;
        this.location = location;
        this.userId = userId;
        this.contact = contact;
    }
    
    Event(LocalDateTime start, LocalDateTime end, int appointmentId) {
        this.startDate = start;
        this.endDate = end;
        this.appointmentId = appointmentId;
    }
    //for TableView CellValueFactory get function should match the property column value string
    public int getAppointmentId() { return appointmentId; }
    public LocalDateTime getStartDate() { return startDate;}
    public String getTitle() { return title;}
    public String getDescription() { return description;}
    public int getCustomerId() { return customerId;}
    public String getType() { return type;}
    public LocalDateTime getEndDate() { return endDate; }
    public String getLocation() { return location; }
    public String getContact() { return contact; }
    public String getTableDate() {
        
        Time myTime;
        DateFormat sdf = new SimpleDateFormat("H:mm");
        String tableDate;
        int year = startDate.getYear();
        int month = startDate.getMonthValue();
        int day = startDate.getDayOfMonth();
        int hour = startDate.getHour();
        int minute = startDate.getMinute();
        int second = startDate.getSecond();
        myTime = new Time(hour,minute,second);
        // Month / Day / Year  HH 
        tableDate = year + "/" + month + "/" + day + " at " + sdf.format(myTime);
        return tableDate;
    }
}
