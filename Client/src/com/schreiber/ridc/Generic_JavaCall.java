package com.schreiber.ridc;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Stack;
import java.util.TreeMap;

import oracle.stellent.ridc.IdcClient;
import oracle.stellent.ridc.IdcClientException;
import oracle.stellent.ridc.IdcClientManager;
import oracle.stellent.ridc.IdcContext;
import oracle.stellent.ridc.model.DataBinder;
import oracle.stellent.ridc.model.DataObject;
import oracle.stellent.ridc.model.DataResultSet;
import oracle.stellent.ridc.protocol.ServiceResponse;

public class Generic_JavaCall {
 public static Properties prop = new Properties();
 public static String propFileName = "C:\\JDeveloper\\mywork\\SchreiberTest\\Client\\classes\\com\\schreiber\\ridc\\config.properties";
 public static String folderPath = prop.getProperty("folderPath");
 public static String foldername = "";
 public static FileReader reader; 
 public static IdcClientManager myIdcClientManager = new IdcClientManager();
 public static IdcClient myIdcClient;
 public static IdcContext myIdcContext;
 public static DataBinder dataBinder;
 public static DataBinder myResponseDataBinder, myRequestDataBinder;
 
 public static ServiceResponse myServiceResponse;
 public static DataResultSet getFolderInfo;
 public static String idcURL;
 public static String user;
 public static String pass;
 
 public  Generic_JavaCall() {
  super();
 }

  
    
  public static  void getProperty() throws IdcClientException
  {
   try
   {
    
   
   reader = new FileReader(propFileName);
   prop.load(reader);
   idcURL = prop.getProperty("idchost");
   user = prop.getProperty("username");
   pass = prop.getProperty("password");
   folderPath = prop.getProperty("folderPath");
   System.out.println("success"+folderPath);
      
   
   }
   catch(Exception e)
   {
    
   }
   
  }
  public static IdcClient getClient() throws IdcClientException
  {
      
   myIdcClientManager = new IdcClientManager();
   myIdcClient = myIdcClientManager.createClient(idcURL);
   myIdcContext = new IdcContext(user);
   myRequestDataBinder = myIdcClient.createBinder();
   
   return myIdcClient;
   
  }
  
  
  public  DataResultSet getResultSetCustom(String fFolderGUID,String IdcService,String ResultsetName) throws IdcClientException,NullPointerException
  { 
       
    this.dataBinder.putLocal("fFolderGUID",fFolderGUID);
    System.out.println("coming");
    this.dataBinder.putLocal("IdcService", IdcService);
    this.myServiceResponse = myIdcClient.sendRequest(myIdcContext,dataBinder);
    this.myResponseDataBinder = myServiceResponse.getResponseAsBinder();
    return this.myResponseDataBinder.getResultSet(ResultsetName);
  }
  public static  String getFolderName(DataResultSet ds)
  {
   foldername = null; 
   for(DataObject myDataObject1 : ds.getRows())
   {
    foldername =  myDataObject1.get("fFolderName");
   }
   return foldername;
     
  }



  public DataBinder createBinder() {
   // TODO Auto-generated method stub
   return null;
  }



  
  

 

}
