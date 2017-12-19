package com.schreiber.ridc;

import java.util.List;

import oracle.stellent.ridc.IdcClient;
import oracle.stellent.ridc.IdcClientManager;
import oracle.stellent.ridc.IdcContext;
import oracle.stellent.ridc.model.DataBinder;
import oracle.stellent.ridc.model.DataObject;
import oracle.stellent.ridc.model.DataResultSet;
import oracle.stellent.ridc.protocol.ServiceResponse;


public class WCCUtil {
    public WCCUtil() {

    }

    public static void main(String[] a) {
        WCCUtil wccUtil = new WCCUtil();
        wccUtil.testResults();
    }

    public void testResults() {
        try {
            int startIndex = 1;
            int count = 1; 
            int pageNumber = 1;
            int totalRows = 20;
            IdcClientManager idcClientManager = new IdcClientManager();

            IdcClient idcClient = idcClientManager.createClient("idc://hould881.sficorp.com:4444");
            IdcContext idcContext = new IdcContext("ucmweblogic", "ecmocs14");
            boolean stillLoop = true;
            while (stillLoop) {
                System.out.println(" Fetching From  startIndex = " + startIndex);
                DataBinder requestBinder = idcClient.createBinder();
                requestBinder.putLocal("IdcService", "GET_SEARCH_RESULTS");
                    requestBinder.putLocal("SearchQueryFormat", "Universal");
                requestBinder.putLocal("QueryText", "(dDocAuthor <matches> `anand14`)");
               
                ServiceResponse serviceResponse = idcClient.sendRequest(idcContext, requestBinder);
                DataBinder responseBinder = serviceResponse.getResponseAsBinder(false);
                System.out.println(" responseBinder.getLocal(\"StatusMessageKey\") = " + responseBinder.getLocal("StatusMessageKey"));
                DataResultSet resultSet = responseBinder.getResultSet("SearchResults");
                if (resultSet != null && resultSet.getRows() != null) {
                        List<DataObject> dataObjects = resultSet.getRows();
                       System.out.println(" Fetching From  resultSet.getRows().size() = " + dataObjects.size());
                       
                      System.out.println("dataObjects.size() = " + dataObjects.size());
                      for (DataObject dataObject : dataObjects) {
                         String dID = dataObject.get("dID").toString();
                        System.out.println((++count) + " :: dID = " + dID);
                      }
            }
                
            }

        } catch (Exception e) {
            e.printStackTrace();

        }


    }
}

