package com.schreiber.folder;

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

public class CustomFolderInfoUpdate {
    public CustomFolderInfoUpdate() {
        super();
    }

    public void CustomFolderInfoUpdate() throws IdcClientException {

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
            String fFolderGUID = prop.getProperty("folderguid");
            String fFolderDescription = prop.getProperty("flddesc");
            String parentguid = null;
            System.out.println(idcURL + user + pass);

            // Connection establishment
            IdcClientManager myIdcClientManager = new IdcClientManager();
            IdcClient myIdcClient = myIdcClientManager.createClient(idcURL);
            IdcContext myIdcContext = new IdcContext(user);
            int count = 1;
            DataBinder dataBinder = myIdcClient.createBinder();
            DataBinder myResponseDataBinder1 = null;


            for (int i = 0; i < fFolderGUID.split("\\|").length; i++) {
               
                try {
                    dataBinder.putLocal("IdcService", "FLD_EDIT_FOLDER");
                   
                   dataBinder.putLocal("fFolderGUID", fFolderGUID.split("\\|")[i]);
                    dataBinder.putLocal("fFolderDescription", fFolderDescription.split("\\|")[i]);
                   

                    myServiceResponse = myIdcClient.sendRequest(myIdcContext, dataBinder);

                    myResponseDataBinder1 = myServiceResponse.getResponseAsBinder();
                    System.out.println("Created FolderName :" +
                                       myResponseDataBinder1.getLocalData().get("fFolderName") + ",folderguid :" +
                                       myResponseDataBinder1.getLocalData().get("fFolderGUID"));
                    //parentguid = myResponseDataBinder1.getLocalData().get("fParentGUID");
                    //parentguid = myResponseDataBinder1.getLocalData().g
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CustomFolderInfoUpdate s = new CustomFolderInfoUpdate();
        try {
            s.CustomFolderInfoUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

