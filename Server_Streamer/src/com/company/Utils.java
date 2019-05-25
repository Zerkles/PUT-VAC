package com.company;

import com.company.ConsoleColors;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class Utils {
    final static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    static void log(String text) {
        Date date = new Date();
        System.out.print(ConsoleColors.BLUE + dateFormat.format(date));
        System.out.print(ConsoleColors.RESET + ' ' + text + '\n');
    }

    static void log(Integer number){
        log(number.toString());
    }
}
