package com.schreiber.folder;

import intradoc.common.Log;
import intradoc.common.Report;
import intradoc.common.ServiceException;

import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;

import intradoc.server.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SchreiberUtilities extends Service {

     private static String author_replacement,fullName,dName;
     private static String previous_author;
     private static String getAllDidQuery,getUserDetail = "";
     private static String queryWorkflowInQueue,queryWorkflowHistory = "";
     private static Map<String,String> map=new HashMap<String,String>();
     private static int temp_count,rowcount = 0;
     private static String comments;
     String traceinfo="";

    public void updateDocAuthorEvent() throws DataException, ServiceException {
        Log.info("Inside Partner Termination Call");
        
        author_replacement = this.m_binder.getLocal("newPartnerID").toUpperCase();
        previous_author = this.m_binder.getLocal("terminatedPartnerID").toUpperCase();
        
        	if((author_replacement == null || author_replacement.isEmpty()) || (previous_author == null || previous_author.isEmpty()))
		{
			 comments = "Not a valid User ID";	
             this.m_binder.putLocal("authorUpdateStatus", "PASS");
             this.m_binder.putLocal("userDeleteStatus","PASS");
             this.m_binder.putLocal("comments",comments);
		     genericMail(comments);
		}
		else
		{
        try {
                                            
            getAllDidQuery =
                "select d.dID,dDocName,dDocTitle from revisions r,docmeta d where d.did=r.did and dStatus not in ('DELETED') and upper(dDocAuthor) = '" +
                previous_author + "' and dRevRank = 0 order by dID desc";
            
            getUserDetail = "select * from USERS where upper(dName) = '"+previous_author+"'";
            int userrowcount = getRowCount(getUserDetail);
            
            fullName= getUserInfo(author_replacement,"dFullName");
           
            
            ResultSet getAllDidRset = this.m_workspace.createResultSetSQL(getAllDidQuery);
            DataResultSet drs = new DataResultSet();
            drs.copy(getAllDidRset);
            rowcount = drs.getNumRows();
            Log.info(getAllDidQuery + "//Rowcount :: " + rowcount);
            if(!(rowcount == 0) && !(userrowcount == 0))
            {          
            for (drs.first(); drs.isRowPresent(); drs.next()) {

                DataBinder binder = new DataBinder();
                binder.putLocal("dID", ResultSetUtils.getValue(drs, "dID"));
                binder.putLocal("dDocAuthor", this.m_binder.getLocal("newPartnerID").trim().toString());
                binder.putLocal("dDocName", ResultSetUtils.getValue(drs, "dDocName"));
                try{
                this.m_requestImplementor.executeServiceTopLevelSimple(binder, "UPDATE_DOCINFO", this.m_userData);
                }
                catch(Exception e)
                {
                    traceinfo = "Error:" + e.toString();
                    Report.trace("PartnerTermination", traceinfo, null);
                   // Log.error("Inside Looping"+e);
                        map.put(ResultSetUtils.getValue(drs, "dDocName"), e.toString());        
                }
                temp_count = rowcount - map.size();
                comments = ""+temp_count+" out of "+rowcount+" documents updated with "+author_replacement+"."+previous_author+" removed in ECM"; 
                this.m_binder.putLocal("authorUpdateStatus", "PASS");
                this.m_binder.putLocal("comments",comments);
                
             }
                deleteUser();
                sendMail(drs);
                for(Map.Entry m:map.entrySet()){
                               Log.info(m.getKey()+":"+m.getValue());  
                   }  
               
            }     
            
            else
            { 
                if((rowcount == 0)  && (userrowcount) == 0)     
                {
					 comments = "Partner not in ECM";
                     this.m_binder.putLocal("authorUpdateStatus", "PASS");
                     this.m_binder.putLocal("userDeleteStatus","PASS");
                     this.m_binder.putLocal("comments",comments);
                }
                else if((rowcount == 0) && !(userrowcount == 0)) {
					comments = "No documents to Update."+previous_author+" removed in ECM";
                    this.m_binder.putLocal("authorUpdateStatus", "PASS");
                    deleteUser();
                    this.m_binder.putLocal("comments",comments);
                }
                
            }
           
           
                
                                      
        } catch (Exception e) {
            Log.error("Error :::" + e);
           
        }
		    finally{  
		       map.clear();
		       temp_count = 0;
		        rowcount = 0;
                        
		    }
       
		}
    }
    
    public int getRowCount(String queryStatement) throws DataException {
        ResultSet getAllQueueRset = this.m_workspace.createResultSetSQL(queryStatement);
        DataResultSet drs1 = new DataResultSet();
        drs1.copy(getAllQueueRset);
        return   drs1.getNumRows();
            
    }   
    //Delete User from ECM 
    public void deleteUser() throws DataException, ServiceException {
        
        String msg;
        Report.trace("PartnerTermination", "Inside Delete User Event", null);
       // queryWorkflowInQueue = "select did,ddocname,dwfname from workflowinqueue where upper(duser) = '"+previous_author+"'";
        //int returncount  = getRowCount(queryWorkflowInQueue);
        //Log.info("QueueCount:"+returncount+queryWorkflowInQueue);
        queryWorkflowHistory = "select DISTINCT DDOCNAME from WORKFLOWHISTORY where upper(duser) = '"+previous_author+"'";
            int workflowCount = getRowCount(queryWorkflowHistory);
        
		dName = getUserInfo(previous_author,"dName"); 
		
        this.m_binder.putLocal("dName", dName);
        try{
        this.m_requestImplementor.executeServiceTopLevelSimple(m_binder, "DELETE_USER", this.m_userData);
        this.m_binder.putLocal("userDeleteStatus","PASS");
        msg =""+temp_count+" out of "+rowcount+" documents updated with "+author_replacement+".<br>"+previous_author+" removed from ECM."; 
        msg+= "<br>Workflow History count for Terminated Partner:"+workflowCount+"";
			
          
        }
       catch(Exception e)
        {
            msg =""+temp_count+" out of "+rowcount+" documents updated with "+author_replacement+".<br>"+previous_author+" not removed in ECM"; 
            traceinfo = "User Deletion Error:" + e.toString();
            Report.trace("PartnerTermination", traceinfo, null);
            this.m_binder.putLocal("userDeleteStatus","FAIL");
            this.m_binder.putLocal("comments",msg);
            Log.info(traceinfo);
        } 
        genericMail(msg);   
    }
   
   public void sendMail(DataResultSet drs1)throws DataException, ServiceException 
   {
       Report.trace("PartnerTermination", "Inside Mail Function", null);
     
       try
       {  
           String htmlMessage="";
           htmlMessage = "<p>Hello "+fullName+",</p><p>The following documents in ECM were previously authored by "+previous_author+ ". Due to this person leaving the organization and you being listed as their manager, you are now the author on these documents.</p>";
           htmlMessage+= "<p>If you should not be listed as the author on some or all of these documents, please open a request with the Help Desk and indicate who should be the author for each document. The Help Desk can be contacted by email: help@schreiberfoods.com, phone: x6205, or using HEAT.<p>";
           htmlMessage+= "<table style =\"font-family: arial, sans-serif;border-collapse: collapse;width: 100%;\"><tr style = \"border: 1px solid #dddddd;text-align: left;padding: 2px;\"><th>Document No.</th><th>Document Title</th></tr>";
           for (drs1.first(); drs1.isRowPresent(); drs1.next()) 
           {
               htmlMessage+= "<tr style = \"border: 1px solid #dddddd;text-align: left;padding: 2px;\"><td>" +ResultSetUtils.getValue(drs1, "dDocName")+"</td>";
               htmlMessage+= "<td>" +ResultSetUtils.getValue(drs1, "dDocTitle")+"</td></tr>";
            }
           
           htmlMessage+= "</table>";
           htmlMessage+="<p>Thank you,";
           htmlMessage+="<br>ECM Administrators</p>";
          // Log.info(htmlMessage);
           this.m_binder.putLocal("testEmailRecipients",author_replacement);
           this.m_binder.putLocal("testEmailSubject","ECM Document Author Update");
           this.m_binder.putLocal("testEmailMessage",htmlMessage);
           this.m_binder.putLocal("testEmailFormat","html");
           this.m_binder.putLocal("testEmailCcRecipients","");
           this.m_requestImplementor.executeServiceTopLevelSimple(this.m_binder, "TEST_EMAIL", this.m_userData);
           
       }
       catch(Exception e){}
       
   }
   
    public void genericMail(String comments)throws DataException, ServiceException 
   {
       Report.trace("PartnerTermination", "ECM Admin Mail Function", null);
       
       try
       {  
           String htmlMessage="<p>"+comments+ "</p>";
		   htmlMessage+= "<p>Author Update Failure:</p>";
		   for(Map.Entry m:map.entrySet())
		   {
                     htmlMessage+= "<br>"+ m.getKey().toString();
		   }    
		   
           htmlMessage+= "</p>";
		   
           // Log.info(htmlMessage);
           this.m_binder.putLocal("testEmailRecipients","ECM_Admin");
           this.m_binder.putLocal("testEmailSubject","ECM Partner Termination - "+previous_author);
           this.m_binder.putLocal("testEmailMessage",htmlMessage);
           this.m_binder.putLocal("testEmailFormat","html");
           this.m_binder.putLocal("testEmailCcRecipients","");
           this.m_requestImplementor.executeServiceTopLevelSimple(this.m_binder, "TEST_EMAIL", this.m_userData);
           
       }
       catch(Exception e){}
   }

   
   public String getUserInfo(String Username,String Metadata) throws DataException {
   
            String query = "select * from USERS where upper(dName) = '"+Username+"'";
            ResultSet getFullName = this.m_workspace.createResultSetSQL(query);
            DataResultSet drs1 = new DataResultSet();
            drs1.copy(getFullName);
            return ResultSetUtils.getValue(drs1,Metadata);
           
   }
   
   
   
   
   
   
}


