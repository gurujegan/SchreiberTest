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


public class CustomUpdateDidDocname {
    public CustomUpdateDidDocname() {
        super();
    }
    
    public void SearchAndupdateMetadata()
      throws IdcClientException
    {
        
        ServiceResponse myServiceResponse = null;
        try{
            
              // Getting values from Properties File            
              Properties prop = new Properties();
             // String propFileName = "C:\\JDeveloper\\mywork\\SchreiberTest\\Client\\classes\\com\\schreiber\\ridc\\sustain_config.properties";
            String propFileName = "C:\\JDeveloper\\mywork\\SchreiberTest\\Client\\classes\\com\\schreiber\\ridc\\config.properties";
              FileReader reader = new FileReader(propFileName);
              prop.load(reader);
                        
                String idcURL=prop.getProperty("idchost");            
                String user=prop.getProperty("username");
                String pass=prop.getProperty("password");
                String DocName=prop.getProperty("docname");
                String Did = prop.getProperty("did");
            String metadataname = prop.getProperty("metadataname");
                //String Location = prop.getProperty("xscope"); 
                //String xCodeCitation = prop.getProperty("xCodeCitation"); 
              // String doctype = prop.getProperty("doctype");
             // String xScope = prop.getProperty("xscope");
              // String fParentGUID=prop.getProperty("fParentGUID");
             //String retentionid = prop.getProperty("retentionid");
              String ddocaccount = prop.getProperty("docaccount");
             //String dRevLabel = prop.getProperty("revlabel");
            
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
                 // String approver = "xLocationName";
                  
                 binder.putLocal("IdcService", "UPDATE_DOCINFO");
                 binder.putLocal("dID", dID);
                 binder.putLocal("dDocName", dDocName);
                 // binder.putLocal("dRevLabel", RevLabel); 
            //    // binder.putLocal("xScope",xScope.split("\\|")[i]);
             //  binder.putLocal("fParentGUID",fParentGUID.split("\\|")[i]);
             //  binder.putLocal("dDocType","Training-Programs");
             //  binder.putLocal("xIdcProfile","Express_Check-In");
                 binder.putLocal("dDocAccount",ddocaccount.split("\\|")[i]);   
                 // binder.putLocal("dDocAccount","ECM-PUBLIC-TEAM-ADVISOR");  
               // binder.putLocal("xScope",Location.split("\\|")[i]);
                //binder.putLocal("xIdcProfile","Sustainability");
                                   
                //if(retentionid.split(",")[i].contains("null"))
                //  binder.putLocal("xCategoryID","");   
                //  else
                // binder.putLocal("xCategoryID",retentionid.split(",")[i]);
                      
                 //binder.putLocal("xWCTags","");
                 // binder.putLocal("xApproved","");
                 // binder.putLocal("xClbraAliasList","");
                 // binder.putLocal("xSensitive","");            
                 // binder.putLocal("xOwner",""); 
                 // binder.putLocal("fFolderGUID", ""); 
                //  binder.putLocal("xCodeCitation",xCodeCitation.split("\\|")[i]);

                      
                  myServiceResponse = myIdcClient.sendRequest(myIdcContext, binder);
                  DataBinder myResponseDataBinder1 = myServiceResponse.getResponseAsBinder();
                  DataResultSet myDataResultSet1 = myResponseDataBinder1.getResultSet("DOC_INFO");
                  for (DataObject myDataObject1 : myDataResultSet1.getRows()) {
                    System.out.println("Location"+myDataObject1.get("dDocAccount")+":"+myDataObject1.get(metadataname));
                  //System.out.println("dDocName ="+myDataObject1.get("dDocName")+":"+myDataObject1.get("dRevLabel"));
                  }  
              }
                  catch(Exception e) {
                      e.printStackTrace();
         //             map.put(Did.split(",")[i], e.toString());
                  }
                  
              
            
            }
              
  //          for(Map.Entry m:map.entrySet()){
  //          System.out.println(m.getKey()+" "+m.getValue());  
   //          }  
        }
            catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        CustomUpdateDidDocname s=new CustomUpdateDidDocname();
        try{
           s.SearchAndupdateMetadata();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}

