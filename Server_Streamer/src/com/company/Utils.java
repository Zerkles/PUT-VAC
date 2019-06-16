package com.company;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class Utils {
    private final static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    static synchronized void log(String text) {
        Date date = new Date();
        System.out.print(ConsoleColors.BLUE + dateFormat.format(date));
        System.out.print(ConsoleColors.RESET + ' ' + text + '\n');
    }
}
