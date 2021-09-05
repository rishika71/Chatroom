package com.example.chatroom.models;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static final String DB_PROFILE = "profiles";
    public static final String DB_CHAT = "chat";
    public static final String DB_CHATROOM = "chatroom";

    /**
     * @param date
     * @return
     */
    public static String getDateString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:m a");
        return dateFormat.format(date);
    }

}
