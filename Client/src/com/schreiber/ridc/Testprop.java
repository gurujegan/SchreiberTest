package com.schreiber.ridc;

import java.io.FileInputStream;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import java.io.InputStreamReader;

import java.util.Properties;

public class Testprop {
    public Testprop() {
        super();
    }

    public static void main(String[] args) throws IOException {
        Testprop testprop = new Testprop();
        Properties prop = new Properties();
      //  String propFileName = "C:\\JDeveloper\\mywork\\SchreiberTest\\Client\\classes\\com\\schreiber\\ridc\\config.properties";
      String propFileName = "config.properties";
     FileReader reader = new FileReader(propFileName);
        prop.load(reader);
      
                  
          String idcURL=prop.getProperty("idchost");            
          String user=prop.getProperty("username");
          String pass=prop.getProperty("password");
          String DocType=prop.getProperty("doctype");
          System.out.println(idcURL+user+pass+DocType);
    }
}
