package com.dawninfotek.logx.extension.log4j12;

import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

public class LogXThrowableInformationPatternConverter extends LoggingEventPatternConverter {
	
	  /**
	   * Maximum lines of stack trace to output.
	   */
	  private int maxLines = Integer.MAX_VALUE;

	  /**
	   * Private constructor.
	   * @param options options, may be null.
	   */
	  private LogXThrowableInformationPatternConverter(
	    final String[] options) {
	    super("Throwable", "throwable");

	    if ((options != null) && (options.length > 0)) {
	      if("none".equals(options[0])) {
	          maxLines = 0;
	      } else if("short".equals(options[0])) {
	          maxLines = 1;
	      } else {
	          try {
	              maxLines = Integer.parseInt(options[0]);
	          } catch(NumberFormatException ex) {
	          }
	      }
	    }
	  }

	  /**
	   * Gets an instance of the class.
	    * @param options pattern options, may be null.  If first element is "short",
	   * only the first line of the throwable will be formatted.
	   * @return instance of class.
	   */
	  public static LogXThrowableInformationPatternConverter newInstance(
	    final String[] options) {
	    return new LogXThrowableInformationPatternConverter(options);
	  }

	  /**
	   * {@inheritDoc}
	   */
	  public void format(final LoggingEvent event, final StringBuffer toAppendTo) {
	    if (maxLines != 0) {
	      ThrowableInformation information = event.getThrowableInformation();

	      if (information != null) {
	        String[] stringRep = information.getThrowableStrRep();

	        int length = stringRep.length;
	        if (maxLines < 0) {
	            length += maxLines;
	        } else if (length > maxLines) {
	            length = maxLines;
	        }

	        for (int i = 0; i < length; i++) {
	            String string = stringRep[i];
	            toAppendTo.append(string);
	            //Do not add new line on the end of the formatted text. 
	            if(i < length -1) {
	            	toAppendTo.append("\n");
	            }
	        }	       
	      }
	    }
	    
	    
	  }

	  /**
	   * This converter obviously handles throwables.
	   * @return true.
	   */
	  public boolean handlesThrowable() {
	    return true;
	  }
}
