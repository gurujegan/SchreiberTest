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

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;


public class SchreiberFolderCustomization extends Service {

    
    public void updateAndUnexpireEvent() throws DataException, ServiceException {
        Log.info("Inside updateAndUnexpireEvent");
        Log.info("m_binder:::" + m_binder);
        String tmp_docname = this.m_binder.getLocal("dDocName");
        String getAllDidQuery = "";
        

        try {
            getAllDidQuery =
                "select d.dID,dStatus,xCodeCitation from revisions r,docmeta d where d.did=r.did and  dDocName = '" + tmp_docname +
                "' order by dID desc";
            ResultSet getAllDidRset = this.m_workspace.createResultSetSQL(getAllDidQuery);
            DataResultSet drs = new DataResultSet();
            drs.copy(getAllDidRset);
            int rowcount = drs.getNumRows();
            Log.info(getAllDidQuery + "  rowcount :: " + rowcount);
            for (drs.first(); drs.isRowPresent(); drs.next()) {

                DataBinder binder = new DataBinder();
                binder.putLocal("dID",ResultSetUtils.getValue(drs, "dID"));
                Log.info("dID :::" + ResultSetUtils.getValue(drs, "dID"));
                binder.putLocal("xCategoryID", "");
                String xCodeCitation=ResultSetUtils.getValue(drs, "xCodeCitation");
               if(xCodeCitation.split("|")[1]!=null && !xCodeCitation.split("|")[1].isEmpty()){
                binder.putLocal("fParentGUID", xCodeCitation.split("\\|")[1]);
                binder.putLocal("xCodeCitation",xCodeCitation.split("\\|")[0]);
                }
                //xCodeCitation */
                binder.putLocal("dDocName",tmp_docname);
                this.m_requestImplementor.executeServiceTopLevelSimple(binder, "UPDATE_DOCINFO",
                                                                       this.m_userData);
               
            }
        }catch(Exception e){
            Log.error("Error :::" + e);
            
        }
        DataBinder binder1 = new DataBinder();
        binder1.putLocal("action", "unexpire");
        binder1.putLocal("dDocName", tmp_docname);
        Log.info(tmp_docname);
        this.m_requestImplementor.executeServiceTopLevelSimple(binder1, "EXPIRE_UNEXPIRE_DOC_BY_NAME",
                                                               this.m_userData);
        
    }
    public void deleteEvent() throws DataException, ServiceException {
        Log.info("Override Folder Delete");
        Log.info("m_binder:::" + m_binder);
       // final long ONE_MINUTE_IN_MILLIS=60000;//millisecs
        String tmp_did = this.m_binder.getLocal("dID");
		boolean isWorkflow = false;
        String tmp_docname = "";
        String RecordFilingDate = "";
        //String account = "";
        this.m_requestImplementor.executeServiceTopLevelSimple(m_binder, "DOC_INFO", this.m_userData);
        ResultSet docInfo = this.m_binder.getResultSet("DOC_INFO");
        String account = ResultSetUtils.getValue(docInfo, "dDocAccount");
        tmp_docname = this.m_binder.getLocal("dDocName");
        String tmp_fParentGUID = this.m_binder.getLocal("fParentGUID");
        Log.info("tmp_fParentGUID:::" + tmp_fParentGUID);
        try {
            /**Get ProfileValue from Config.cfg and compare with current profile
             * if CurrentContentProfile is not part of ProfileValue proceed to normal delete
             * if CurrentContentProfile is part of ProfileValue then check for user access
             * If user is part of RWD the Expire the content else Delete
             * **/
            String ProfileValue = m_binder.getEnvironmentValue("ProfileValue").toUpperCase();
            String CurrentContentProfile = m_binder.getLocal("xIdcProfile").toUpperCase();
            Log.info("CurrentContentProfile::" + CurrentContentProfile+" Ddocaccount: "+account);
				
			
			     if (!ProfileValue.contains(CurrentContentProfile)) {
                m_binder.putLocal("PermanentDelete", "1");
                this.m_requestImplementor.executeServiceTopLevelSimple(m_binder, "DELETE_DOC1", this.m_userData);
            } else {
                                 
				
				
                if (CurrentContentProfile.equals("HR-BENEFITS")) {
                    account = "ECM-HR-BEN";
					Log.info("Inside HR-Ben accountset");
                }
				else
                                Log.info("Outside HR-Ben accountset");
				
                String user = this.m_binder.getLocal("dUser");
                // Geting User Account Attribute details
                this.m_requestImplementor.executeServiceTopLevelSimple(m_binder, "GET_USER_INFO", this.m_userData);
				ResultSet UserAttribInfo = this.m_binder.getResultSet("UserAttribInfo");
				String accounts_list = ResultSetUtils.getValue(UserAttribInfo, "AttributeInfo");
				//Log.info(accounts_list);
			
                 String dattr = "";
                 String tmp_account="";
                   
                // Retrieving Hierarchical Account Info if dDocaccount is being virtual
                if(accounts_list.length() > 0)
                {
					
                                        if(accounts_list.contains("#all,15"))
                                        dattr = "15";
                                        else
					for(int i=1; i<account.length();i++)
					{
          
					    
							if(accounts_list.contains(account))
							{
							   dattr = accounts_list.split(account+",")[1].split(",")[0];
							}
                                                       
					 
						   if((dattr.length() > 0) && ((dattr.equals("7")) || (dattr.equals("15"))))
						   {
							Log.info(account+"&dAttrValue="+dattr);   
							break;
						   }
                                                  else {
                                                        tmp_account = account.substring(0, account.lastIndexOf("-"));
                                                        account = tmp_account;
                                                    }
					}
							
                }
                    
                           
                                     

                if (dattr.equals("15")) {
                    Log.info("Access Level :"+dattr+"//Permanent Delete call Initiated");
                    m_binder.putLocal("PermanentDelete", "1");
                    this.m_requestImplementor.executeServiceTopLevelSimple(m_binder, "DELETE_DOC1", this.m_userData);
                } else if (dattr.equals("7")) {
                    Log.info("Access Level : "+dattr+"//Expire Event call Initiated instead of Delete");
                    String getAllDidQuery = "";


                    try {
                        getAllDidQuery =
                            "select d.dID,dStatus,xCodeCitation from revisions r,docmeta d where r.did=d.did and  dDocName = '" + tmp_docname +
                            "' order by dID desc";
                        ResultSet getAllDidRset = this.m_workspace.createResultSetSQL(getAllDidQuery);
                        DataResultSet drs = new DataResultSet();
                        drs.copy(getAllDidRset);
                        int rowcount = drs.getNumRows();
                        Log.info(getAllDidQuery + "  rowcount :: " + rowcount);
                        for (drs.first(); drs.isRowPresent(); drs.next())
							{

                            String dStatus = ResultSetUtils.getValue(drs, "dStatus");
                            String dID = ResultSetUtils.getValue(drs, "dID");
                            Log.info("dID :::" + dID);
                            m_binder.putLocal("dID", dID);
                            if (dStatus.equalsIgnoreCase("REVIEW")) {
                                isWorkflow = true;
                                Log.info("dStatus :::" + dStatus);
                                if (!SecurityUtils.isUserOfRole(this.m_userData, "admin")) {
                                    this.m_userData.addAttribute("role", "admin", "15");
                                }
                                DataBinder dbinder = new DataBinder();
                                dbinder.putLocal("dID", dID);
                                UserData admindata = SecurityUtils.createDefaultAdminUserData();
                                dbinder.putLocal("PermanentDelete", "1");
                               
                               
                                this.m_requestImplementor.executeServiceTopLevelSimple(dbinder, "DELETE_REV", this.m_userData);
                                                                                                 
                                Log.info("Workflow Document(Rev ID:"+this.m_binder.getLocal("dID")+") removed from ECM");
								  
                              
                            } 
                            else {
                                isWorkflow = false;
                                DataBinder binder = new DataBinder();
                                binder.putLocal("xCategoryID", "20.001");
                                binder.putLocal("dID",dID);
                                binder.putLocal("dDocName",this.m_binder.getLocal("dDocName"));
                                                          
                                Log.info("xCodeCitation:"+ResultSetUtils.getValue(drs, "xCodeCitation"));
                                String tmp_length[] = ResultSetUtils.getValue(drs, "xCodeCitation").split("\\|");
                                if(tmp_length.length == 1 || tmp_length.length == 0 )
                                {
                                                    binder.putLocal("xCodeCitation", ResultSetUtils.getValue(drs, "xCodeCitation")+"|"+tmp_fParentGUID);
                                                    Log.info(binder.getLocal("xCodeCitation"));
                                                this.m_requestImplementor.executeServiceTopLevelSimple(binder,"UPDATE_DOCINFO",this.m_userData);
                                }
                            }
                        }

                    }

                    catch (Exception e) {
                       Log.error("Error::" + e);
                    }
					
					if(isWorkflow == false)
					{
                                 DataBinder binder = new DataBinder();
                    
                                    m_binder.putLocal("action", "expire");
                                    m_binder.putLocal("dDocName", tmp_docname);
                                    m_binder.putLocal("dID", "");
                                    this.m_requestImplementor.executeServiceTopLevelSimple(this.m_binder, "EXPIRE_UNEXPIRE_DOC_BY_NAME",
                                                                                           this.m_userData); 
					}
                }

            }
        } catch (Exception e) {
            Log.info("Error::::" + e);
        }

        Log.info("Override Folder Delete done");

    }

}
