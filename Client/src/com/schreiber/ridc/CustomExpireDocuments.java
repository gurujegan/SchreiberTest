package com.schreiber.ridc;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import oracle.stellent.ridc.IdcClientException;
import oracle.stellent.ridc.IdcClient;
import oracle.stellent.ridc.IdcClientManager;
import oracle.stellent.ridc.IdcContext;
import oracle.stellent.ridc.model.DataBinder;
import oracle.stellent.ridc.model.DataObject;
import oracle.stellent.ridc.model.DataResultSet;
import oracle.stellent.ridc.protocol.ServiceResponse;


public class CustomExpireDocuments {
    public CustomExpireDocuments() {
        super();
    }
    
    public void SearchAndupdateMetadata()
      throws IdcClientException
    {
        
        ServiceResponse myServiceResponse = null;
        try{
            
              // Getting values from Properties File            
              Properties prop = new Properties();
              String propFileName = "C:\\JDeveloper\\mywork\\SchreiberTest\\Client\\classes\\com\\schreiber\\ridc\\config.properties";
              FileReader reader = new FileReader(propFileName);
              prop.load(reader);
                        
                String idcURL=prop.getProperty("idchost");            
                String user=prop.getProperty("username");
                String pass=prop.getProperty("password");
                String DocName=prop.getProperty("docname");
              //  String Did = prop.getProperty("did");
            //String Doctype = prop.getProperty("doctype");
             //   String ddocauthor=prop.getProperty("docauthor");
                System.out.println(idcURL+user+pass+DocName);
           
              
              // Connection establishment
              IdcClientManager myIdcClientManager = new IdcClientManager();
              IdcClient myIdcClient = myIdcClientManager.createClient(idcURL);
              IdcContext myIdcContext = new IdcContext(user,pass);
              int count =1;
            
              //GET_SEARCH_RESULTS 
            for(int i=0;i<DocName.split(",").length;i++){
                              
                  try{
                  DataBinder binder = myIdcClient.createBinder();
                 //String dID=Did.split(",")[i];
                  String dDocName=DocName.split(",")[i];
                    
                  System.out.println("Count: "+i+":"+ dDocName);
                  //String approver = "dDocAccount";
                  
                  binder.putLocal("IdcService", "EXPIRE_UNEXPIRE_DOC_BY_NAME");
                    binder.putLocal("dDocName", dDocName);
                      binder.putLocal("action", "expire");
                  
                  myServiceResponse = myIdcClient.sendRequest(myIdcContext, binder);
                  DataBinder myResponseDataBinder1 = myServiceResponse.getResponseAsBinder();
                 DataResultSet myDataResultSet1 = myResponseDataBinder1.getResultSet("DOC_INFO");
                  for (DataObject myDataObject1 : myDataResultSet1.getRows()) {
                      System.out.println("Updated :"+myDataObject1.get("dStatus"));
                  } 
              }
                  catch(Exception e) {
                      e.printStackTrace();
                  }
                  
              
            
            }
              
             
        }
            catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        CustomExpireDocuments s=new CustomExpireDocuments();
        try{
           s.SearchAndupdateMetadata();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}


 
