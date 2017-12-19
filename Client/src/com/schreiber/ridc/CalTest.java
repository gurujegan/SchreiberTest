package com.schreiber.ridc;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

public class CalTest {
    public CalTest() {
        super();
    }
    
    void collectionExample() {
        String ddocnames = "fhjfg,fgf,fgfg";
         
        Collection firstList = new ArrayList() {
           {
           for(int i=0;i<ddocnames.split(",").length;i++)
           {
           add(ddocnames.split(",")[i]);
           }
        }};

        Collection secondList = new ArrayList() {{
           add("apple");
           add("orange");
           add("banana");
           add("strawberry");
        }};

        // Show the "before" lists
        System.out.println("First List: " + firstList);
        System.out.println("Second List: " + secondList);

        // Remove all elements in firstList from secondList
        secondList.removeAll(firstList);

        // Show the "after" list
        System.out.println("Result: " + secondList);
    }
    
    void ECMdateParse() {
        try
        {
                       String sourceDate = "2016-09-23 07:06:01";
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        Date myDate = format.parse(sourceDate);
                        Calendar cal = Calendar.getInstance();
                      //  cal.setTime(myDate);
                        cal.add(Calendar.DATE, 5);
                      sourceDate =format.format(cal.getTime());
                        System.out.println(sourceDate);
            String s = "fhfgh,dfd,dfddf,dfd";
            
            if(!s.isEmpty() && !s.equals(null)) {
                System.out.println(s.replaceAll(",", "','"));
            }
            else
            System.out.println("empty");
            
        }
        catch(ParseException e) {
            
        }
    }

    public static void main(String[] args) {
        CalTest calTest = new CalTest();
         String n = "ECM-CORP-CAL-ghg";
         n.lastIndexOf("-");
         String ghgh = "";
       int c = n.split("-").length;
       System.out.println(c);
 
       
         for(int i=1 ;i<c; i++)
         {
            
             n= n.substring(0, n.lastIndexOf("-"));
             
             if(i >= 0)
             break;
             System.out.println(i+n);
             
            
         }
      
         
       // System.out.println(n);
       
       
        
                
         
    }
}
