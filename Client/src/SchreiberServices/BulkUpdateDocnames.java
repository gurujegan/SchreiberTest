package SchreiberServices;


import intradoc.common.Log;
import intradoc.common.ServiceException;

import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;

import intradoc.server.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulkUpdateDocnames extends Service {
        
    private static String dDocnames,updatedAuthorName,getAllDidQuery,dbData,userData,Result,getUserDetail,comments= "";  
    int rowcount,temp_count = 0;
    private static Map<String,String> map=new HashMap<String,String>();
    private static  DataResultSet drs,drs1;
    
    public void startBulkUpdate()throws DataException, ServiceException
    {
        String trimmedDocnames = this.m_binder.getLocal("dDocNames").trim().replaceAll(" ", "");
        Log.info("inside service");
        dDocnames = "('";
        if(this.m_binder.getLocal("dDocNames").contains(","))
        dDocnames+= trimmedDocnames.replaceAll(",","','"); 
        else
        dDocnames+= trimmedDocnames;
        dDocnames+= "')";
        Log.info(dDocnames);
        updatedAuthorName = this.m_binder.getLocal("newAuthor").trim().toUpperCase();
        
        getAllDidQuery =  "select d.dID,dDocName,dDocTitle from revisions r,docmeta d where d.did=r.did and dStatus not in ('DELETED') and dRevRank = 0 and dDocName IN "+ dDocnames +" order by dID desc";
        getUserDetail = "select * from USERS where upper(dName) = '"+updatedAuthorName+"'";
        
        int userrowcount = getRowCount(getUserDetail);
       
        
            try 
            {
                ResultSet getAllDidRset = this.m_workspace.createResultSetSQL(getAllDidQuery);
                                DataResultSet drs = new DataResultSet();
                                drs.copy(getAllDidRset);
                                rowcount = drs.getNumRows();
                                Log.info(getAllDidQuery + "//Rowcount :: " + rowcount);
                
                 if(!(rowcount == 0) && !(userrowcount == 0))
                {          
                    for (drs.first(); drs.isRowPresent(); drs.next()) {
                        
                        dbData+= ResultSetUtils.getValue(drs, "dDocName") + ",";
                        DataBinder binder = new DataBinder();
                        binder.putLocal("dID", ResultSetUtils.getValue(drs, "dID"));
                        binder.putLocal("dDocAuthor", this.m_binder.getLocal("newAuthor").trim().toString());
                        binder.putLocal("dDocName", ResultSetUtils.getValue(drs, "dDocName"));
                        try{
                        this.m_requestImplementor.executeServiceTopLevelSimple(binder, "UPDATE_DOCINFO", this.m_userData);
                        }
                        catch(Exception e)
                        {
                                Log.error("While Looping"+e);
                                map.put(ResultSetUtils.getValue(drs, "dDocName"), e.toString());        
                        }
                        
                     }
                    temp_count = rowcount - map.size();
                    comments = ""+temp_count+" out of "+rowcount+" documents updated with "+updatedAuthorName;
                    Log.info(comments);
                    this.m_binder.putLocal("UpdatedReleasedRows",String.valueOf(temp_count));
                    this.m_binder.putLocal("comments",comments);
                    map.clear();
                }
                
                else
                { 
                    if(!(rowcount == 0)  && (userrowcount) == 0)     
                    {
                         comments = "Partner not in ECM";
                        this.m_binder.putLocal("UpdatedReleasedRows","0");
                         this.m_binder.putLocal("comments",comments);
                         Log.info(comments);
                    }
                    else if((rowcount == 0) && !(userrowcount == 0)) {
                            comments = "Documents not available in ECM either archived or deleted by Partners";
                            this.m_binder.putLocal("UpdatedReleasedRows", "0");
                            this.m_binder.putLocal("comments",comments);
                        Log.info(comments+"dfdf");
                    }
                    
                }
                
            }
       
            
            catch(Exception e) 
            {
                Log.error(e.toString());
            }
            
    }
    public int getRowCount(String queryStatement) throws DataException {
        ResultSet getAllQueueRset = this.m_workspace.createResultSetSQL(queryStatement);
        drs1 = new DataResultSet();
        drs1.copy(getAllQueueRset);
        return   drs1.getNumRows();
            
    } 

}
