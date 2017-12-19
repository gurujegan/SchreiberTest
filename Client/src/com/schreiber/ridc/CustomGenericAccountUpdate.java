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


public class CustomGenericAccountUpdate {
    public CustomGenericAccountUpdate() {
        super();
    }

    public void SearchAndupdateMetadata() throws IdcClientException {

        ServiceResponse myServiceResponse = null;
        try {

            // Getting values from Properties File
            Properties prop = new Properties();
            String propFileName =
                "C:\\JDeveloper\\mywork\\SchreiberTest\\Client\\classes\\com\\schreiber\\ridc\\config.properties";
            FileReader reader = new FileReader(propFileName);
            prop.load(reader);

            String idcURL = prop.getProperty("idchost");
            String user = prop.getProperty("username");
            String pass = prop.getProperty("password");
            String ddocaccount = prop.getProperty("ddocaccount");
            String updatedaccount = prop.getProperty("updatedaccount");
            System.out.println(idcURL + user + pass);


            // Connection establishment
            IdcClientManager myIdcClientManager = new IdcClientManager();
            IdcClient myIdcClient = myIdcClientManager.createClient(idcURL);
            IdcContext myIdcContext = new IdcContext(user);
            int count = 1;

            // String qt= "(dDocAccount <matches> `ECM-AR`)  <AND>  (xApproved <matches> `No`)";
            String qt = "(dDocAccount <matches> `" + ddocaccount + "`)";
            // String qt="(xIdcProfile <matches> `MKT`)";
            DataBinder myRequestDataBinder = myIdcClient.createBinder();
            myRequestDataBinder.putLocal("IdcService", "GET_SEARCH_RESULTS");
            myRequestDataBinder.putLocal("SearchQueryFormat", "UNIVERSAL");

            myRequestDataBinder.putLocal("ResultCount", "2000");
            myRequestDataBinder.putLocal("QueryText", qt);

            myServiceResponse = myIdcClient.sendRequest(myIdcContext, myRequestDataBinder);

            DataBinder myResponseDataBinder = myServiceResponse.getResponseAsBinder();
            DataResultSet myDataResultSet = myResponseDataBinder.getResultSet("SearchResults");

            System.out.println("Printing file details...");
            for (DataObject myDataObject : myDataResultSet.getRows()) {
                DataBinder binder = myIdcClient.createBinder();
                String dID = myDataObject.get("dID");
                String dDocName = myDataObject.get("dDocName");
                String xApproved = myDataObject.get("xApproved");
                String xIdcProfile = myDataObject.get("xIdcProfile");
                System.out.println(+count + ":" + xIdcProfile + ":" + dDocName + ":" + dID + ":" + updatedaccount);
                count++;
                binder.putLocal("IdcService", "UPDATE_DOCINFO");
                binder.putLocal("dID", dID);
                binder.putLocal("dDocName", dDocName);
                // binder.putLocal("xApproved", "");
                binder.putLocal("dDocAccount", updatedaccount);
                myServiceResponse = myIdcClient.sendRequest(myIdcContext, binder);

            }

            //}


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CustomGenericAccountUpdate s = new CustomGenericAccountUpdate();
       try {
              for(int i=0;i<1;i++){
             System.out.println("Loop Done i::"+i);
            s.SearchAndupdateMetadata();
             }
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }   

}


