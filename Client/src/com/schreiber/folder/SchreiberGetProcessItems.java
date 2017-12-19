package com.schreiber.folder;
import intradoc.common.LocaleUtils;
import intradoc.common.Log;
import intradoc.common.Report;
import intradoc.common.ServiceException;
import intradoc.common.StringUtils;
import intradoc.common.SystemUtils;

import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.FieldInfo;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;
import intradoc.data.Workspace;

import intradoc.server.DocumentAccessSecurity;
import intradoc.server.Service;

import intradoc.shared.SecurityUtils;
import intradoc.shared.UserData;

import java.util.HashMap;
import java.util.Map;


public class SchreiberGetProcessItems extends Service {
   
    private static String getParamValues,distinctoutput;
    private static Map<String,String> map =new HashMap<String,String>();
    private static String queryToFilter;
  
    
    public void filterItems() throws DataException, ServiceException 
    {
        getParamValues = this.m_binder.getLocal("criteria");
        distinctoutput = this.m_binder.getLocal("output");
        queryToFilter = "select DISTINCT "+distinctoutput+" from Revisions R INNER JOIN Docmeta D on D.did=R.did where ";
        Log.info(getParamValues);    
        
        for(int i=0; i<getParamValues.split(",").length;i++) 
        {
            String key = getParamValues.split(",")[i].split(":")[0].toString();
            String value = getParamValues.split(",")[i].split(":")[1].toString();
            map.put(key,value);

        }
        String mapsize=Integer.toString(map.size());
        Log.info(mapsize);
        int count = 1;
        for(Map.Entry m:map.entrySet())
        {
            
            queryToFilter += m.getKey() + "= '" + m.getValue()+"' ";
            if(map.size() != count)
            {
              queryToFilter += "AND ";
            }
            count++;
        }
        Log.info(queryToFilter);
        ResultSet rsprocesslist = this.m_workspace.createResultSetSQL(queryToFilter);
        DataResultSet drs = new DataResultSet();
        drs.copy(rsprocesslist);
       // this.m_binder.addResultSet("CustomResult", rsprocesslist);
        this.m_binder.addResultSet("CustomResult", drs);
        map.clear();
    }
    
       
}
