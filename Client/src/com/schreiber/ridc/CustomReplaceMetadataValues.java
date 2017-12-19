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


public class CustomReplaceMetadataValues {
    public CustomReplaceMetadataValues() {
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
                String Did = prop.getProperty("did");
            //String Doctype = prop.getProperty("doctype");
                String ddocauthor=prop.getProperty("ddocauthor");
            String replacedstr = "";
            String Metadataname="";
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
                 String dID=Did.split(",")[i];
                  String dDocName=DocName.split(",")[i];
                     // String dDocType= Doctype.split(",")[i];
            
                 
                  System.out.println("Count: "+i+":"+ dDocName+":"+dID);
                                    
                  binder.putLocal("IdcService", "DOC_INFO");
                  binder.putLocal("dID", dID);
                  binder.putLocal("dDocName", dDocName);
                      //specify the metadataname
                  Metadataname = "dDocAccount";
                   myServiceResponse = myIdcClient.sendRequest(myIdcContext, binder);
                  DataBinder myResponseDataBinder1 = myServiceResponse.getResponseAsBinder();
                 DataResultSet myDataResultSet1 = myResponseDataBinder1.getResultSet("DOC_INFO");
                  for (DataObject myDataObject1 : myDataResultSet1.getRows()) {
                      System.out.println("Before Update:"+myDataObject1.get(Metadataname));
                     replacedstr =  myDataObject1.get(Metadataname).replaceAll("SRC", "PUBLIC");
                      System.out.println("After update: "+replacedstr);
                  } 
                  binder.putLocal("IdcService", "UPDATE_DOCINFO");
                  binder.putLocal(Metadataname, replacedstr);
                  myServiceResponse = myIdcClient.sendRequest(myIdcContext, binder);
                      System.out.println("Updated");
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
        CustomReplaceMetadataValues s=new CustomReplaceMetadataValues();
        try{
           s.SearchAndupdateMetadata();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}

