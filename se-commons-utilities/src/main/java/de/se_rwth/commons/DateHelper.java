/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

/**
 * Provides helper infrastructure to work with java date representation.
 *
 */
public class DateHelper {
  
  private static DateHelper theInstance = null;
  
  private final List<SimpleDateFormat> dateFormatters;
  
  private final List<String> timeFormats = Lists.newArrayList("dd.MM.yyyy HH:mm", "dd.MM.yyyy");
  
  private final SimpleDateFormat defaultDateFormatter;
  
  /**
   * Disables instantiation. Use getInstance instead. Constructor for cd2gui.helper.DateHelper
   */
  private DateHelper() {
    Locale local = Locale.GERMAN;
    defaultDateFormatter = new SimpleDateFormat(timeFormats.get(0), local);
    
    dateFormatters = Lists.newArrayList();
    for (String format : timeFormats) {
      dateFormatters.add(new SimpleDateFormat(format, local));
    }
  }
  
  /**
   * @return
   */
  public static DateHelper getInstance() {
    if (theInstance == null) {
      theInstance = new DateHelper();
    }
    
    return theInstance;
  }
  
  /**
   * Converts the data based on the default formatter. Default local: GERMAN, Default pattern:
   * "dd.MM.yyyy hh:mm"
   *
   * @return date as a string or null if the {@code date} argument is null
   */
  public String format(Date date) {
    if (date != null) {
      return defaultDateFormatter.format(date);
      
    }
    else {
      // LOG.debug("format: date argument is null!" );
      return "";
    }
    
  }
  
  /**
   * Tries to parse date with several formats: ."dd.MM.yyyy hh:mm", "dd.MM.yyyy"
   *
   * @param date
   * @return Date object or null, if the string doesn't conform the format.
   */
  public Date parse(String date) {
    Date result = null;
    if (date == null) {
      // LOG.debug("Cannot convert null to java.util.Date: " + date);
      return result;
    }
    for (SimpleDateFormat dateFormatter : dateFormatters) {
      try {
        
        return dateFormatter.parse(date);
        
      }
      catch (ParseException e) {
        // LOG.debug("Cannot convert the following string to java.util.Date object: " + date, e);
      }
    }
    return result;
  }
  
  /**
   * Gets the calendar associated with default date/time formatter.
   *
   * @return the calendar associated with default date/time formatter.
   */
  public Calendar getCalendar() {
    return defaultDateFormatter.getCalendar();
  }
  
  /**
   * Returns a Date object representing this Calendar's time value
   *
   * @return a Date representing the time value.
   */
  public Date getTime() {
    return getCalendar().getTime();
  }
  
  /**
   * Returns the current Date (This method is intended for templates with access to an instance of
   * DateHelper)
   */
  public Date getCurrentDate() {
    return new Date();
  }
  
}
