package com.schreiber.ridc;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
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


public class CustomSubtypeModify{
    public CustomSubtypeModify() {
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
              //  String DocType=prop.getProperty("doctype");
                System.out.println(idcURL+user+pass);
           
              
              // Connection establishment
              IdcClientManager myIdcClientManager = new IdcClientManager();
              IdcClient myIdcClient = myIdcClientManager.createClient(idcURL);
              IdcContext myIdcContext = new IdcContext(user);
              int count =1;
            
              //GET_SEARCH_RESULTS 
                
             // for(int i=0; i<DocType.split(",").length;i++) 
            //  {
             //     String dDoc=DocType.split(",")[i];
            String qt="(xSubType <matches> `Food Safety and Quality Plans`)";
                                                            
              DataBinder myRequestDataBinder = myIdcClient.createBinder();
              myRequestDataBinder.putLocal("IdcService", "GET_SEARCH_RESULTS");
              myRequestDataBinder.putLocal("SearchQueryFormat", "UNIVERSAL");
            
              myRequestDataBinder.putLocal("ResultCount", "1200"); 
              myRequestDataBinder.putLocal("QueryText", qt);
              
              myServiceResponse = myIdcClient.sendRequest(myIdcContext, myRequestDataBinder);
               
              DataBinder myResponseDataBinder = myServiceResponse.getResponseAsBinder();
              DataResultSet myDataResultSet = myResponseDataBinder.getResultSet("SearchResults");
            
              System.out.println("Printing file details...");
              for (DataObject myDataObject : myDataResultSet.getRows()) {
                  DataBinder binder = myIdcClient.createBinder();
                  String dID=myDataObject.get("dID");
                  String dDocName=myDataObject.get("dDocName");
                  String xReviewDate=myDataObject.get("xReviewDate");
                  String dDocType=myDataObject.get("dDocType");
                  System.out.println(+count+ ":"+dDocType+":-"+dDocName+" "+dID+"REviewdate: "+xReviewDate);
                  count++;
                  binder.putLocal("IdcService", "UPDATE_DOCINFO");
                  binder.putLocal("dID", dID);
                  binder.putLocal("dDocName", dDocName);
                  binder.putLocal("xSubType", "Food Quality Plans");
                  
                  myServiceResponse = myIdcClient.sendRequest(myIdcContext, binder);
                  DataBinder myResponseDataBinder1 = myServiceResponse.getResponseAsBinder();
                  DataResultSet myDataResultSet1 = myResponseDataBinder1.getResultSet("DOC_INFO");
                  for (DataObject myDataObject1 : myDataResultSet1.getRows()) {
                      System.out.println("Updated xSubType Name:"+myDataObject1.get("xSubType"));
                  }
                                   
              }
            
            //}
              
             
        }
            catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        CustomSubtypeModify s=new CustomSubtypeModify();
        try{
           s.SearchAndupdateMetadata();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
