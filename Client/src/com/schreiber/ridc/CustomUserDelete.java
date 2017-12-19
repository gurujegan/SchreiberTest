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


public class CustomUserDelete {
    public CustomUserDelete() {
        super();
    }

    public void CustomUserDelete() throws IdcClientException {

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
            String userList = prop.getProperty("userList");
            System.out.println(idcURL + user + pass);

            // Connection establishment
            IdcClientManager myIdcClientManager = new IdcClientManager();
            IdcClient myIdcClient = myIdcClientManager.createClient(idcURL);
            IdcContext myIdcContext = new IdcContext(user);
            int count = 1;
            DataBinder dataBinder = myIdcClient.createBinder();
            DataBinder myResponseDataBinder1 = null;


            for (int i = 0; i < userList.split(",").length; i++) {
                
                try {
                    dataBinder.putLocal("IdcService", "DELETE_USER");
                    dataBinder.putLocal("dName", userList.split(",")[i]);
                    myServiceResponse = myIdcClient.sendRequest(myIdcContext, dataBinder);

                    myResponseDataBinder1 = myServiceResponse.getResponseAsBinder();
                    System.out.println(+i+"-"+myResponseDataBinder1.getLocalData().get("dUserName"));
                    
                    System.out.println();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CustomUserDelete s = new CustomUserDelete();
        try {
            s.CustomUserDelete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

