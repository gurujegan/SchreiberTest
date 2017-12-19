package com.schreiber.ridc;


import intradoc.common.Log;

import java.io.FileReader;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


import oracle.stellent.ridc.IdcClientException;
import oracle.stellent.ridc.IdcClient;
import oracle.stellent.ridc.IdcClientManager;
import oracle.stellent.ridc.IdcContext;
import oracle.stellent.ridc.model.DataBinder;
import oracle.stellent.ridc.model.DataObject;
import oracle.stellent.ridc.model.DataResultSet;
import oracle.stellent.ridc.protocol.ServiceResponse;


public class CustomSQFprocessSystemUpdate {
    public CustomSQFprocessSystemUpdate() {
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
            
            String ProcessSystem = prop.getProperty("xProcessSystem"); 
           
          
               String metadataname = prop.getProperty("metadataname"); 
                           
               Map<String,String> map=new HashMap<String,String>();
                System.out.println(idcURL+user+pass);
           
              
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
                        
                  System.out.println("Count: "+i+":"+ dDocName+":"+dID);
                                  
                  binder.putLocal("IdcService", "UPDATE_DOCINFO");
                  binder.putLocal("dID", dID);
                  binder.putLocal("dDocName", dDocName);
              binder.putLocal("xProcessSystem",ProcessSystem.split("\\|")[i]);
            //  binder.putLocal("xProcessSystem","Specifications, Identity and Labeling");
                      
                  myServiceResponse = myIdcClient.sendRequest(myIdcContext, binder);
                  DataBinder myResponseDataBinder1 = myServiceResponse.getResponseAsBinder();
                 DataResultSet myDataResultSet1 = myResponseDataBinder1.getResultSet("DOC_INFO");
                  for (DataObject myDataObject1 : myDataResultSet1.getRows()) {
                      System.out.println("Updated "+metadataname+":"+myDataObject1.get(metadataname));
                  }  
              }
                  catch(Exception e) {
                      e.printStackTrace();
                      map.put(Did.split(",")[i], e.toString());
                  }
                  
              
            
            }
              
            for(Map.Entry m:map.entrySet()){
            System.out.println(m.getKey()+" "+m.getValue());  
             }  
        }
            catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        CustomSQFprocessSystemUpdate s=new CustomSQFprocessSystemUpdate();
        try{
           s.SearchAndupdateMetadata();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}

