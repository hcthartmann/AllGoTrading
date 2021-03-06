package org.yats.common;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tool {

    final static Logger log = LoggerFactory.getLogger(Tool.class);


    public static String getOsName()
    {
        if(OS == null) { OS = System.getProperty("os.name"); }
        return OS;
    }

    public static boolean isWindows()
    {
        return getOsName().startsWith("Windows");
    }

    public static String getPersonalConfigFilename(String pathBase, String className)
    {
        String username = System.getProperty("user.name").replace(" ","");
        String path = pathBase+"/"+username;
        FileTool.createDirectories(path);
        String userSpecificFilename = path+"/"+className+".properties";
        log.info("Trying to read config file: "+userSpecificFilename);
        if(!FileTool.exists(userSpecificFilename))
            throw new CommonExceptions.FileReadException(userSpecificFilename+" not found!");
        return userSpecificFilename;
    }

    public static String getPersonalSubdirConfigFilename(String pathBase, String subDir, String className)
    {
        String username = System.getProperty("user.name").replace(" ","");
        String path = pathBase+"/"+username+"/"+subDir;
        FileTool.createDirectories(path);
        String userSpecificFilename = path+"/"+className+".properties";
        return userSpecificFilename;
    }


    public static void sleepABit() {
        sleepFor(200);
    }

    public static void sleepFor(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String getUTCTimestampString() {
        return getUTCTimestamp().toString();
    }

    public static DateTime getUTCTimestamp() {
        return DateTime.now(DateTimeZone.UTC);
    }


    public static String removeBlanks(String s) {
        return s.replace(" ","").replace("\\t","");
    }

    public static String removeReturns(String s) {
        return s.replace("\\r","");
    }

    ///////////////////////////

    private static String OS = null;

}
