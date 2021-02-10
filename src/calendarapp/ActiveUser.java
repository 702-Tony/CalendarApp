/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calendarapp;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Locale;

/**
 *
 * @author Anthony Adams
 */
public class ActiveUser {
    // this stores active user information once logged in
    private static String userName;
    private static int userId;
    private static LocalDateTime userLocalDateTime;
    private static ZonedDateTime zonedTime;
    private static Locale locale;
    public static void setUser(String userName, int userId) {
        ActiveUser.userName = userName;
        ActiveUser.userId = userId;
        userLocalDateTime = LocalDateTime.now();
        zonedTime = ZonedDateTime.now();
        System.out.println("User Created : " + ActiveUser.userName);
        System.out.println("Timezone : " + zonedTime.getZone());
        ActiveUser.locale = Locale.getDefault();
    }
    public static int getUserId() { return userId; }
    public static String getUserName() { return userName; }
    public static Locale getLocale() { return locale; }
    
}