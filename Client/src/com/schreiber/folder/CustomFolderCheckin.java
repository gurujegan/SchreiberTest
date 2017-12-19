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

public class CustomFolderCheckin {
    public CustomFolderCheckin() {
        super();
    }

    public void CustomFolderCheckin() throws IdcClientException {

        ServiceResponse myServiceResponse = null;
        try {

            // Getting values from Properties File
            Properties prop = new Properties();
            String propFileName =
                "C:\\JDeveloper\\mywork\\SchreiberTest\\Client\\classes\\com\\schreiber\\ridc\\folderPath.properties";
            FileReader reader = new FileReader(propFileName);
            prop.load(reader);

            String idcURL = prop.getProperty("idchost");
            String user = prop.getProperty("username");
            String pass = prop.getProperty("password");
            String FolderName = prop.getProperty("ffoldername");
            String fparentguid = prop.getProperty("fparentguid");
            String ffolderdescription = prop.getProperty("ffolderdescription");
            String parentguid = null;
            System.out.println(idcURL + user + pass);

            // Connection establishment
            IdcClientManager myIdcClientManager = new IdcClientManager();
            IdcClient myIdcClient = myIdcClientManager.createClient(idcURL);
            IdcContext myIdcContext = new IdcContext(user);
            int count = 1;
            DataBinder dataBinder = myIdcClient.createBinder();
            DataBinder myResponseDataBinder1 = null;


            for (int i = 0; i < FolderName.split("\\|").length; i++) {
                String Folder = FolderName.split("\\|")[i];
                try {
                    dataBinder.putLocal("IdcService", "FLD_CREATE_FOLDER");
                  // dataBinder.putLocal("fParentGUID", "097F00FA0E512D08D6E19EE7E3994587");
                   dataBinder.putLocal("fParentGUID", fparentguid.split("\\|")[i]);
                    dataBinder.putLocal("fFolderName", Folder);
                    dataBinder.putLocal("fOwner", "UCMWeblogic");
                    dataBinder.putLocal("fSecurityGroup", "Internal");
                    dataBinder.putLocal("fFolderDescription",ffolderdescription.split("\\|")[i]);
                   // dataBinder.putLocal("fDocAccount", "ECM-PUBLIC-SUS");

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
        CustomFolderCheckin s = new CustomFolderCheckin();
        try {
            s.CustomFolderCheckin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

