package edu.wpi.always.user.calendar;

import java.io.PrintStream;
import java.util.*;
import org.joda.time.*;

public abstract class CalendarUtils {

   public static DateMidnight toMidnight (LocalDate date) {
      return new DateMidnight(date.getYear(), date.getMonthOfYear(),
            date.getDayOfMonth());
   }

   public static LocalDate getFirstDayOfWeek (LocalDate day) {
      if ( day.getDayOfWeek() == 7 )
         return day;
      return day.withDayOfWeek(1).minusDays(1);
   }

   public static int getDayOfWeek (DateTime date) {
      int day = date.getDayOfWeek() + 1;
      if ( day == 8 )
         return 1;
      return day;
   }

   public static LocalDate withDayOfWeek (LocalDate dateTime, int day) {
      LocalDate firstDayOfWeek;
      if ( dateTime.getDayOfWeek() == 7 )
         firstDayOfWeek = dateTime;
      else
         firstDayOfWeek = dateTime.withDayOfWeek(1).minusDays(1);
      return firstDayOfWeek.plusDays(day - 1);
   }

   public static DateTime toDateTime (LocalDate date, LocalTime time) {
      return new DateTime(date.getYear(), date.getMonthOfYear(),
            date.getDayOfMonth(), time.getHourOfDay(), time.getMinuteOfHour(),
            0, 0);
   }

   public static LocalDate getDate (DateTime time) {
      return new LocalDate(time.getYear(), time.getMonthOfYear(),
            time.getDayOfMonth());
   }

   public static LocalTime getTime (DateTime time) {
      return new LocalTime(time.getHourOfDay(), time.getMinuteOfHour(), 0, 0);
   }
   
   public static void print (Calendar calendar, PrintStream stream) {
      Iterator<CalendarEntry> iterator = calendar.iterator();
      while (iterator.hasNext()) stream.println(iterator.next());
   }
   
   /**
    * @return true iff calendar has one or more entries in remainder of today 
    * or tomorrow.
    */
   public static boolean isImminent (Calendar calendar) {
      DateTime now = new DateTime();
      return !calendar.retrieve (now, now.plusDays(2).withTimeAtStartOfDay()).isEmpty();
   }
   
   private CalendarUtils () {}
}
