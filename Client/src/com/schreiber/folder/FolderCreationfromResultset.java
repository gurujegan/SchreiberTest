package com.schreiber.folder;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeMap;

import oracle.stellent.ridc.IdcClientException;
import oracle.stellent.ridc.IdcClient;
import oracle.stellent.ridc.IdcClientManager;
import oracle.stellent.ridc.IdcContext;
import oracle.stellent.ridc.model.DataBinder;
import oracle.stellent.ridc.model.DataObject;
import oracle.stellent.ridc.model.DataResultSet;
import oracle.stellent.ridc.protocol.ServiceResponse;

public class FolderCreationfromResultset {

    public static Properties prop = new Properties();
    public static String propFileName =
        "C:\\JDeveloper\\mywork\\SridharProject\\ECM\\src\\com\\ecm\\ridc\\config.properties";


    public static String temp = null;
    public static String resGUID = null;
    public static TreeMap<String, String> map = new TreeMap();

    public static String idcURL = prop.getProperty("idchost");
    public static String user = prop.getProperty("username");
    public static String pass = prop.getProperty("password");
    public static String folderPath;
    public static String parentguid = null;

    // Connection establishment
    public static IdcClientManager myIdcClientManager;
    public static IdcClient myIdcClient;
    public static IdcContext myIdcContext;
    int count = 1;
    public static DataBinder dataBinder, dataBinder1;
    public static DataBinder myResponseDataBinder1, myResponseDataBinder = null;
    public static ServiceResponse myServiceResponse, myServiceResponse1 = null;

    public FolderCreationfromResultset() {
        super();
    }

    public static DataResultSet getResultSetCustom(String fFolderGUID, String IdcService,
                                                   String ResultsetName) throws IdcClientException {
        dataBinder.putLocal("fFolderGUID", fFolderGUID);

        dataBinder.putLocal("IdcService", IdcService);

        myServiceResponse = myIdcClient.sendRequest(myIdcContext, dataBinder);
        myResponseDataBinder = myServiceResponse.getResponseAsBinder();
        return myResponseDataBinder.getResultSet(ResultsetName);

    }

    public static String getFolderName(DataResultSet ds) {
        String foldername = null;
        for (DataObject myDataObject1 : ds.getRows()) {
            foldername = myDataObject1.get("fFolderName");
        }
        return foldername;

    }

    public void FolderCreationfromResultset() throws IdcClientException {


        try {

            // Getting values from Properties File
            FileReader reader = new FileReader(propFileName);
            prop.load(reader);

            idcURL = prop.getProperty("idchost");
            user = prop.getProperty("username");
            pass = prop.getProperty("password");
            folderPath = prop.getProperty("folderPath");
            parentguid = null;

            myIdcClientManager = new IdcClientManager();
            System.out.println(idcURL);
            myIdcClient = myIdcClientManager.createClient(idcURL);
            myIdcContext = new IdcContext(user);
            dataBinder = myIdcClient.createBinder();
            dataBinder1 = myIdcClient.createBinder();


            for (int i = 0; i < folderPath.split("\\|").length; i++) {

                String profile, dtype = null , fFolderDescription= null;
                String Data = folderPath.split("\\|")[i];
                String childname = Data.split(":")[0];

                String path = Data.split(":")[1].split("\\$folderMeta")[0];
                profile = Data.split("xIdcProfile=")[1].split(",")[0];
                String fDocAccount = Data.split("fDocAccount=")[1].split(",")[0];
                System.out.println(fDocAccount);
                if (Data.contains("dDocType")) 
                {
                    dtype = Data.split("dDocType=")[1].split(",")[0];
                    System.out.println("Doctype=" + dtype);
                }
                    if (Data.contains("fFolderDescription"))
                {
                     fFolderDescription = Data.split("fFolderDescription=")[1];
                     System.out.println("FolderDescription="+fFolderDescription); 
                }


                //System.out.println("folderpath--"+path);
                //System.out.println("childname"+childname);
                if (map.size() == 0) {
                    temp = "F2572250C65819757AEECAFA909E6B70";
                    String s = "/Enterprise Libraries/Demo_Portal/TestFolder";

                    map.put(s, temp);
                } else {
                    temp = map.get(path);
                    //System.out.println(temp);

                }


                dataBinder1.putLocal("IdcService", "FLD_CREATE_FOLDER");
                dataBinder1.putLocal("fParentGUID", temp);
                dataBinder1.putLocal("fFolderName", childname);
                dataBinder1.putLocal("fOwner", "weblogic");
                dataBinder1.putLocal("fSecurityGroup", "Public");
                dataBinder1.putLocal("fDocAccount", fDocAccount);
                if( fFolderDescription != null)
                {
                dataBinder1.putLocal("fFolderDescription", fFolderDescription);
                }
                
                

                myServiceResponse1 = myIdcClient.sendRequest(myIdcContext, dataBinder1);
                //System.out.println("asdasd"+myServiceResponse1);
                myResponseDataBinder1 = myServiceResponse1.getResponseAsBinder();

                resGUID = myResponseDataBinder1.getLocalData().get("fFolderGUID");
                // To Set Profile Name fro content Item
                if (profile != null || dtype != null )
                {

                    dataBinder1.putLocal("IdcService", "FLD_EDIT_METADATA_RULES");
                    dataBinder1.putLocal("fFolderGUID", resGUID);
                    dataBinder1.putLocal("xIdcProfile", profile);
                    dataBinder1.putLocal("dDocType", dtype);
                   
                    myServiceResponse1 = myIdcClient.sendRequest(myIdcContext, dataBinder1);

                }

                DataResultSet Parent = getResultSetCustom(resGUID, "FLD_INFO", "FolderInfo");

                String parentName = getFolderName(Parent);
                //System.out.println();


                String mapvalue = myResponseDataBinder.getLocal("folderPath");
                map.put(mapvalue, resGUID);
                System.out.println("Created FolderName :" + myResponseDataBinder1.getLocalData().get("fFolderName") +
                                   ",folderguid :" + resGUID);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FolderCreationfromResultset s = new FolderCreationfromResultset();
        try {
            s.FolderCreationfromResultset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
