package com.schreiber.ridc;


import intradoc.common.ServiceException;

import intradoc.data.DataException;

import intradoc.data.IdcProperties;
import intradoc.data.Workspace;

import intradoc.server.schema.SchemaManager;
import intradoc.server.schema.SchemaStorage;

import intradoc.server.schema.SchemaUtils;
import intradoc.server.schema.ServerSchemaManager;

import intradoc.shared.ComponentClassFactory;
import intradoc.shared.schema.SchemaEditHelper;
import intradoc.shared.schema.SchemaHelper;
import intradoc.shared.schema.SchemaSecurityFilter;
import intradoc.shared.schema.SchemaViewData;

import java.util.Properties;

import oracle.stellent.ridc.IdcClient;
import oracle.stellent.ridc.IdcClientException;
import oracle.stellent.ridc.IdcClientManager;
import oracle.stellent.ridc.IdcContext;
import oracle.stellent.ridc.model.DataBinder;
import oracle.stellent.ridc.protocol.ServiceResponse;
import java.util.concurrent.ConcurrentHashMap;

public class ECMTest {


   
    public static void main(String[] args) throws DataException, ServiceException {
        IdcClient myIdcClient;
        DataBinder db;
        IdcContext myIdcContext;
        ECMTest eCMTest = new ECMTest();
        ServiceResponse myServiceResponse = null;
        Workspace ws = null;
         ServerSchemaManager m_schemaManager = SchemaManager.getManager(ws);
         SchemaUtils m_schemaUtils  =((SchemaUtils)ComponentClassFactory.createClassInstance("SchemaUtils", "intradoc.server.schema.SchemaUtils", null));;
         SchemaHelper m_schemaHelper;
         SchemaEditHelper m_editHelper;
         SchemaStorage m_tables;
        Properties m_safeEnvironment = new IdcProperties(new ConcurrentHashMap(), null);
         Properties m_secureEnvironment = new IdcProperties(new ConcurrentHashMap(), m_safeEnvironment);
         
         
         SchemaStorage m_relation;
        SchemaStorage m_views =  m_schemaManager.getStorageImplementor("SchemaViewConfig");      
        try {
            Generic_JavaCall.getProperty();
            myIdcClient = Generic_JavaCall.getClient();
            myIdcContext = new IdcContext(Generic_JavaCall.user);
            db = myIdcClient.createBinder();
                    
                        System.out.println(myIdcClient.getClientManager().getClientNames());
            
            db.putLocal("IdcService", "GET_SCHEMA_VIEW");
            db.putLocal("schViewName","AdditionalPlantApproversAR");
             myServiceResponse = myIdcClient.sendRequest(myIdcContext, db);
            DataBinder myResponseDataBinder = myServiceResponse.getResponseAsBinder();
            
          
            System.out.println(myResponseDataBinder.toString());
            SchemaViewData viewData= null;
            
           
            
           viewData = (SchemaViewData) m_views.load("AdditionalPlantApproversAR", true);
            SchemaSecurityFilter filter = m_schemaUtils.getSecurityImplementor(viewData);
            System.out.println(filter);

        } catch (IdcClientException e) {
        }


    }
}
