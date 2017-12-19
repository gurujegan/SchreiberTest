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


public class CustomCheckin {
    public CustomCheckin() {
        super();
    }
    
    public void CheckinCustom()
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
            
            
            //}
            for(int i=0;i<=1000;i++){ 
            DataBinder dataBinder = myIdcClient.createBinder();
            dataBinder.putLocal("IdcService", "CHECKIN_NEW");
              dataBinder.putLocal("dDocTitle", "Testdoc2131312"+i);
              dataBinder.putLocal("xIdcProfile", "SQF");
              dataBinder.putLocal("dDocType", "Policy-Procedure");
              dataBinder.putLocal("dRevLabel", "1");
              dataBinder.putLocal("dDocAuthor", "UCMWeblogic");
              dataBinder.putLocal("xLevel1Approvers", "guruj12");
              dataBinder.putLocal("dSecurityGroup", "Internal");
              dataBinder.putLocal("createPrimaryMetaFile", "1");
            
           
            
            
            
            myServiceResponse = myIdcClient.sendRequest(myIdcContext, dataBinder);
            DataBinder myResponseDataBinder1 = myServiceResponse.getResponseAsBinder();
           
                System.out.println(+i+"Updated dDocName :"+myResponseDataBinder1.getLocalData().get("dDocName"));
          
            }
              
             
        }
            catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        CustomCheckin s=new CustomCheckin();
        try{
           s.CheckinCustom();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}