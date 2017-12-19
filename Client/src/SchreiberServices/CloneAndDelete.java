package SchreiberServices;
import intradoc.common.*;
import intradoc.data.*;
import intradoc.shared.*;
import intradoc.server.workflow.*;
import intradoc.server.Service;
import intradoc.server.ServiceManager;
import intradoc.server.IdcServiceAction;
import intradoc.filestore.FileStoreProvider;
import intradoc.filestore.FileStoreProviderHelper;
import intradoc.filestore.IdcDescriptorState;
import intradoc.filestore.IdcFileDescriptor;
import java.util.*;
import java.io.File;
import java.io.IOException;

public class CloneAndDelete extends Service
{	
	
	private String sDocAuthor ;
	public void startCloneAndDelete()
		throws ServiceException, DataException
	{
		Report.trace("CloneAndDelete","startCloneAndDelete : Invoked",null);
		
		
		UserData originalUserData = this.m_userData;	
		ResultSet docInfo = this.m_binder.getResultSet("ORIGINAL_DOC_INFO");	
		String sdID = "";
		try
		{
			sdID = this.m_binder.getLocal("dID");
		}
		catch(Exception e1)
		{
			this.m_binder.putLocal("StatusMessage","Required Parameter dID not found");
			this.m_binder.putLocal("StatusCode","-1");
			Report.trace("CloneAndDelete","startCloneAndDelete : dID not found",null);
			this.createServiceException(e1, null);
		}
		String sdDocName = "";
		try
		{
			sdDocName = this.m_binder.getLocal("dDocName");
		}
		catch (Exception e2)
		{
			this.m_binder.putLocal("StatusMessage","Required Parameter dDocName not found");
			this.m_binder.putLocal("StatusCode","-1");
			Report.trace("CloneAndDelete","startCloneAndDelete : dDocName not found",null);
			this.createServiceException(e2, null);
			
		}
		
		String sCategoryID = "";
		try
		{
			sCategoryID = this.m_binder.getLocal("xCategoryID");
		}
		catch (Exception e2)
		{
			sCategoryID = "";
			
		}
		
		boolean isWorkflowRev = StringUtils.convertToBool(this.m_binder.getLocal("IsWorkflow"), false);
		String wfState = ResultSetUtils.getValue(docInfo, "dWorkflowState");	
		sDocAuthor = ResultSetUtils.getValue(docInfo, "dDocAuthor");
		
		String swfAbandon = "";
		try
		{
			swfAbandon = this.m_binder.getLocal("workflowAbandon");
		}
		catch(Exception n)
		{
			swfAbandon = "no";
		}
		Report.trace("CloneAndDelete"," swfAbandon: " + swfAbandon,null);
			
		if ((wfState != null) && (wfState.trim().length() > 0))
		{
			
			if(sDocAuthor.equals(this.m_binder.get("dUser")) )
			{
				//continue;
			}
			else if(swfAbandon.equals("yes") && this.m_binder.get("dUser").equals(returnOriginalReviewAuthor(sdDocName)))
			{
				Report.trace("CloneAndDelete"," original review author",null);
				this.m_userData = SecurityUtils.createDefaultAdminUserData();	
			}
			else if (!this.checkAccess(this.m_binder,docInfo, 8))
			{
				this.m_binder.putLocal("cdStatusCode","-10.2");
				this.m_binder.putLocal("cdStatusMessage","Insufficient Privileges");
				this.m_binder.putLocal("StatusMessage","Insufficient Privileges");
				this.m_binder.putLocal("StatusCode","-1");
				Report.trace("CloneAndDelete","startCloneAndDelete : Insufficient Privileges",null);
				this.m_binder.putLocal("sinfoURL","?IdcService=DOC_INFO_BY_NAME&dDocName=" + sdDocName);		
				return;
			}
		}
		else
		{
			Report.trace("CloneAndDelete","dUser:" + this.m_binder.get("dUser") + ", Author:" + sDocAuthor,null);
			if(sDocAuthor.equals(this.m_binder.get("dUser")) )
			{
				//continue;
			}
			else if (!this.checkAccess(this.m_binder,docInfo, 4))
			{
				this.m_binder.putLocal("cdStatusCode","-10.1");
				this.m_binder.putLocal("cdStatusMessage","Insufficient Privileges");
				this.m_binder.putLocal("StatusMessage","Insufficient Privileges");
				this.m_binder.putLocal("StatusCode","-1");
				Report.trace("CloneAndDelete","startCloneAndDelete : Insufficient Privileges",null);
				this.m_binder.putLocal("sinfoURL","?IdcService=DOC_INFO_BY_NAME&dDocName=" + sdDocName);		
				return;
			}
		}
		
		
		String sNewDocName = sdDocName + "_A";
		
		String strRevLabel = checkItemExists(sNewDocName);
		if(!this.m_binder.getLocal("cdStatusCode").equals("0"))
		{
			this.m_binder.putLocal("sinfoURL","?IdcService=DOC_INFO_BY_NAME&dDocName=" + sdDocName);
			this.m_userData =originalUserData;
			return;
		}
		Report.trace("CloneAndDelete","startCloneAndDelete : new Doc Revision" + strRevLabel,null);
		
		boolean bNew = false;
		int iRevLabel = NumberUtils.parseInteger(strRevLabel, 1);
		
		if(strRevLabel == null || strRevLabel.equals(""))
		{
			createNewContent(sdID,sdDocName,sNewDocName,sCategoryID);
			if(!this.m_binder.getLocal("cdStatusCode").equals("0"))
			{				
				this.m_binder.putLocal("sinfoURL","?IdcService=DOC_INFO_BY_NAME&dDocName=" + sdDocName);
				this.m_userData =originalUserData;
				return;
			}			
		}
		else
		{
			ResultSet rset1 = this.m_binder.getResultSet("ClonedInfo");
			String sNewDID = ResultSetUtils.getValue(rset1, "dID");
			String sdIsCheckedOut = ResultSetUtils.getValue(rset1, "dIsCheckedOut");
			if(sdIsCheckedOut == null || !(sdIsCheckedOut.equals("1")))
			{
				checkoutArchiveItem(sNewDID,sNewDocName);
				if(!this.m_binder.getLocal("cdStatusCode").equals("0"))
				{					
					this.m_binder.putLocal("sinfoURL","?IdcService=DOC_INFO_BY_NAME&dDocName=" + sdDocName);
					this.m_userData =originalUserData;
					return;
				}
			}
			createRevClone(sdID,sdDocName,sNewDID,sNewDocName,iRevLabel,sCategoryID);
			if(!this.m_binder.getLocal("cdStatusCode").equals("0"))
			{	
				this.m_binder.putLocal("sinfoURL","?IdcService=DOC_INFO_BY_NAME&dDocName=" + sdDocName);
				this.m_userData =originalUserData;
				return;
			}
			
		}
		deleteOriginalRevision(sdID,sdDocName);	
		if(!this.m_binder.getLocal("cdStatusCode").equals("0"))
		{	
			this.m_binder.putLocal("sinfoURL","?IdcService=DOC_INFO_BY_NAME&dDocName=" + sdDocName);	
			this.m_userData = originalUserData;			
			return;
		}
		String snomorerev= this.m_binder.getLocal("nomorerevisions");
		if(snomorerev.equals("no"))
		{
			this.m_binder.putLocal("sinfoURL","?IdcService=DOC_INFO_BY_NAME&dDocName=" + sdDocName);
		}
		this.m_userData =originalUserData;
	}
	
	public boolean checkDeletePrivileges()
		throws ServiceException, DataException
	{	
		if (this.checkAccess(this.m_binder, 8))
		{
			return true;
		}
		return false;
	}
	
	public void createClone(String sdID,String sdDocName,String sNewDocName,String sCategoryID)
		throws ServiceException, DataException
	{	
			Report.trace("CloneAndDelete","createClone : Start of createClone",null);			
			String sNewAccount="ECM-SFI-SECURE";
			try
			{
				sNewAccount = this.m_binder.getLocal("newAccount");
			}
			catch(Exception e1)
			{
				sNewAccount = "ECM-SFI-SECURE";
			}
			String sNewSG="Secure";
			try
			{
				sNewSG = this.m_binder.getLocal("newSG");
			}
			catch(Exception e1)
			{
				sNewSG = "Secure";
			}
			if(sNewSG == null)
			{
				sNewSG = "Secure";
			}
			if(sNewAccount == null)
			{
				sNewAccount = "ECM-SFI-SECURE";
			}
			UserData originalUserData = this.m_userData;			
			if (!SecurityUtils.isUserOfRole(this.m_userData, "admin"))
			{
				this.m_userData.addAttribute("role", "admin", "15"); //grant user admin rights required for checkin
				this.m_userData.addAttribute("account", sNewAccount, "2");
			}			
					
			String cmd = "COPY_REVISION";
			DataBinder binder = new DataBinder();			
			Date dte = new Date();
			String deletedDate = LocaleUtils.formatODBC(dte);			
			
			
			
			
			//this.m_addReleasedWebFile = true;
			binder.putLocal("dID",sdID);
			binder.putLocal("dDocName",sdDocName);
			binder.putLocal("newDocName",sNewDocName);
			binder.putLocal("dSecurityGroup",sNewSG);
			binder.putLocal("dDocAccount",sNewAccount);
			binder.putLocal("xContentDeletedOn",deletedDate);	
			binder.putLocal("addReleasedWebFile","1");	
			binder.putLocal("xIdcProfile","");
			
			binder.putLocal("xAuthorName",this.sDocAuthor);
			String sReason = this.m_binder.getLocal("xReason4Archive");
			if(sReason != null)
			{
				binder.putLocal("xReason4Archive",sReason);	
			}
			
			if(sCategoryID == null || sCategoryID.equals(""))
			{
				binder.putLocal("xCategoryID","");
			}
			else
			{
				binder.putLocal("xCategoryID",sCategoryID);	
			}
			
			//All Folder properties
			binder.removeLocal("xLibraryGUID");
			binder.removeLocal("fApplication");
			binder.removeLocal("fClbraAliasList");
			binder.removeLocal("fClbraRoleList");
			binder.removeLocal("fClbraUserList");
			binder.removeLocal("fCreateDate");
			binder.removeLocal("fCreator");
			binder.removeLocal("fDocAccount");
			binder.removeLocal("fFileGUID");
			binder.removeLocal("fFileGUIDs");
			binder.removeLocal("fFileName");
			binder.removeLocal("fInhibitPropagation");
			binder.removeLocal("fLastModifiedDate");
			binder.removeLocal("fLastModifier");
			binder.removeLocal("fLinkRank");
			binder.removeLocal("fOwner");
			binder.removeLocal("fParentGUID");
			binder.removeLocal("fParentGUID_display");
			binder.removeLocal("fPublishedFileName");
			binder.removeLocal("fSecurityGroup");
			binder.removeLocal("ownerFilePath");

			
			Report.trace("CloneAndDeleteBinder","copyanddelete :binder b4" + binder.toString(),null);
			try
			{
				this.m_requestImplementor.executeServiceTopLevelSimple(binder, cmd, this.m_userData);						
				this.m_binder.putLocal("cdStatusCode","0");
				this.m_binder.putLocal("cdStatusMessage","Successfully Created Clone");
			}
			catch(DataException de)
			{
				this.m_binder.putLocal("cdStatusCode","-1.1");
				this.m_binder.putLocal("cdStatusMessage","Failed to Clone");
				Report.trace("CloneAndDelete", null, de);
				Report.error(null, null, de);
			}
			catch(ServiceException se)
			{
				this.m_binder.putLocal("cdStatusCode","-1.2");
				this.m_binder.putLocal("cdStatusMessage","Failed to Clone");
				Report.trace("CloneAndDelete", null, se);
				Report.error(null, null, se);
			}
			finally
			{
				this.m_userData=originalUserData;
			}
			Report.trace("CloneAndDeleteBinder","copyanddelete :binder" + binder.toString(),null);
			ResultSet rset1 = binder.getResultSet("OriginalDocInfo");
			ResultSet rset2 = binder.getResultSet("NewDocInfo");
			if (rset1 != null)
			{
				Report.trace("CloneAndDelete","createClone : OriginalDocInfo exists",null);

			}
			if (rset2 != null)
			{
				Report.trace("CloneAndDelete","createClone : NewDocInfo exists",null);
			}
			this.m_binder.putLocal("archiveddID",binder.getLocal("dID"));
			this.m_binder.putLocal("archiveddDocName",binder.getLocal("dDocName"));
			binder = null;
			Report.trace("CloneAndDelete","createClone : End of createClone",null);
			
	}
	

	
	public void deleteOriginalRevision(String sdID,String sDDocName)
		throws ServiceException, DataException
	{	
			Report.trace("CloneAndDelete","CloneAndDelete : Start of deleteOriginalRevision",null);						
			String cmd = "DELETE_REV";	
			DataBinder binder = new DataBinder();			
			binder.putLocal("dID",sdID);
			try
			{
				this.m_requestImplementor.executeServiceTopLevelSimple(binder, cmd, this.m_userData);
				Report.trace("CloneAndDeleteBinder","checkinClone :deleteOriginalRevision binder" + binder.toString(),null);
				String swfAbandon = "";
				try
				{
					swfAbandon = this.m_binder.getLocal("workflowAbandon");
				}
				catch(Exception n)
				{
					swfAbandon = "no";
				}
				if(swfAbandon.equals("yes"))
				{
					try
					{
						boolean bReview = true;
						UserData admindata = SecurityUtils.createDefaultAdminUserData();
						ResultSet rsRevInfo = null;
						String sReviewDID = "";
						String sStatus  = "";
						rsRevInfo = binder.getResultSet("DOC_INFO");
						sReviewDID = ResultSetUtils.getValue(rsRevInfo, "dID");
						sStatus = ResultSetUtils.getValue(rsRevInfo, "dStatus");
						DataBinder tmpbinder = new DataBinder();
						while(bReview)
						{					
							
							if(sStatus.equals("REVIEW"))
							{
								
								tmpbinder.putLocal("dID",sReviewDID);			
								try
								{
									this.m_requestImplementor.executeServiceTopLevelSimple(tmpbinder, cmd, admindata);
									rsRevInfo = tmpbinder.getResultSet("DOC_INFO");
									sReviewDID = ResultSetUtils.getValue(rsRevInfo, "dID");
									sStatus = ResultSetUtils.getValue(rsRevInfo, "dStatus");
									tmpbinder = new DataBinder();
								}
								catch(Exception ed)
								{
									bReview = false;
								}
							
							}
							else
							{
								bReview = false;
								break;
							}
						}
						admindata = null;
						rsRevInfo = null;
						sReviewDID = null;
						sStatus  = null;
					}
					catch(Exception ig)
					{
						Report.trace("CloneAndDelete", null, ig);
						Report.error(null, null, ig);
					}
				}
				this.m_binder.putLocal("nomorerevisions","no");
				this.m_binder.putLocal("cdStatusCode","0");
				this.m_binder.putLocal("cdStatusMessage","Successfully Deleted Clone");
			}
			catch(DataException de)
			{
				this.m_binder.putLocal("cdStatusCode","-2.1");
				this.m_binder.putLocal("cdStatusMessage","Failed to Delete");				
				Report.trace("CloneAndDelete", null, de);
				Report.error(null, null, de);
			}
			catch(ServiceException se)
			{
				String sprevID = binder.getLocal("prevID");				
				if(sprevID != null && sprevID.equals(sdID))
				{					
					this.m_binder.putLocal("cdStatusCode","0");
					this.m_binder.putLocal("nomorerevisions","yes");
					this.m_binder.putLocal("cdStatusMessage","Successfully Deleted Clone");
					this.m_binder.putLocal("StatusCode","0");
				}
				else
				{
					this.m_binder.putLocal("cdStatusCode","-2.2");
					this.m_binder.putLocal("cdStatusMessage","Failed to Deleted");					
					Report.trace("CloneAndDelete", null, se);
					Report.error(null, null, se);
					
				}
			}
			
			
			//this.m_binder.putLocal("RedirectParams","IdcService=DOC_INFO_BY_NAME&dDocName=" + sDDocName);
			binder = null;
			Report.trace("CloneAndDelete","CloneAndDelete : End of deleteOriginalRevision",null);
			//checkForRedirectResponse();
			
	}
	
	
	public String checkItemExists(String sDocName)
		throws ServiceException, DataException
	{
		Report.trace("CloneAndDelete","checkoutArchiveItem :checkItemExists Start" + sDocName,null);				
				
		String returnValue = "";
		String query = "SELECT dID,dRevLabel,dIsCheckedOut " +
						"FROM Revisions " +
						"WHERE (Revisions.dDocName='"+ sDocName + "' AND Revisions.dStatus<>'DELETED' AND dRevRank=0)";
		
        try
		{
			ResultSet rset = this.m_workspace.createResultSetSQL(query);
			Report.trace("CloneAndDelete","checkoutArchiveItem :checkItemExists 1",null);				
			if (!rset.isEmpty())
			{        
				returnValue = ResultSetUtils.getValue(rset, "dRevLabel");
				DataResultSet drset = new DataResultSet();
				drset.copy(rset);
				this.m_binder.addResultSet("ClonedInfo", drset);
				drset = null;
			}			
			rset = null;
			this.m_binder.putLocal("cdStatusCode","0");
			this.m_binder.putLocal("cdStatusMessage","Successfully Verified if Item exists");
		}	
		catch(DataException de)
		{
			Report.trace("CloneAndDelete","checkoutArchiveItem :checkItemExists Error",null);				
			Report.trace("CloneAndDelete", null, de);
			Report.error(null, null, de);
			this.m_binder.putLocal("cdStatusCode","-3.0");
			this.m_binder.putLocal("cdStatusMessage","Failed to check Item Exists");

		}
		finally
		{
			query = null;			
			return returnValue;
		}		
	}
	
		public void checkoutArchiveItem(String sNewDID,String sNewDocName)
		throws ServiceException, DataException
	{	
			Report.trace("CloneAndDelete","startCloneAndDelete : Start of checkoutArchiveItem",null);						
			UserData originalUserData = this.m_userData;			
			if (!SecurityUtils.isUserOfRole(this.m_userData, "admin"))
			{
				this.m_userData.addAttribute("role", "admin", "15"); //grant user admin rights required for checkin
				String sNewAccount="ECM-SFI-SECURE";
				try
				{
					sNewAccount = this.m_binder.getLocal("newAccount");
				}
				catch(Exception e1)
				{
					sNewAccount = "ECM-SFI-SECURE";
				}				
				this.m_binder.getLocal("newAccount");
				this.m_userData.addAttribute("account", sNewAccount, "2");
			}			
					
			String cmd = "CHECKOUT";
			DataBinder binder = new DataBinder();			
		
			
			//this.m_addReleasedWebFile = true;
			binder.putLocal("dID",sNewDID);
			binder.putLocal("dDocName",sNewDocName);			
			binder.putLocal("dSecurityGroup","Secure");			
			try
			{
				this.m_requestImplementor.executeServiceTopLevelSimple(binder, cmd, this.m_userData);						
				this.m_binder.putLocal("cdStatusCode","0");
				this.m_binder.putLocal("cdStatusMessage","Successfully Checked out Clone");
			}
			catch(DataException de)
			{
				this.m_binder.putLocal("cdStatusCode","-4.1");
				this.m_binder.putLocal("cdStatusMessage","Failed to checkout");
				Report.trace("CloneAndDelete", null, de);
				Report.error(null, null, de);
			}
			catch(ServiceException se)
			{
				this.m_binder.putLocal("cdStatusCode","-4.2");
				this.m_binder.putLocal("cdStatusMessage","Failed to checkout");
				Report.trace("CloneAndDelete", null, se);
				Report.error(null, null, se);
			}
			finally
			{
				this.m_userData=originalUserData;
			}				
			Report.trace("CloneAndDeleteBinder","startCloneAndDelete :binder" + binder.toString(),null);			
			binder = null;			
			
	}

	public void createRevClone(String sdID,String sdDocName,String sNewDID,String sNewDocName,int iRevLabel,String sCategoryID)
		throws ServiceException, DataException
	{	
			Report.trace("CloneAndDelete","createRevClone : Start of createRevClone",null);		
			iRevLabel = iRevLabel + 1;			
			UserData originalUserData = this.m_userData;	
			String sNewAccount="ECM-SFI-SECURE";
			try
			{
				sNewAccount = this.m_binder.getLocal("newAccount");
			}
			catch(Exception e1)
			{
				sNewAccount = "ECM-SFI-SECURE";
			}
			String sNewSG="Secure";
			try
			{
				sNewSG = this.m_binder.getLocal("newSG");
			}
			catch(Exception e1)
			{
				sNewSG = "Secure";
			}
			if(sNewSG == null)
			{
				sNewSG = "Secure";
			}
			if(sNewAccount == null)
			{
				sNewAccount = "ECM-SFI-SECURE";
			}


			
			if (!SecurityUtils.isUserOfRole(this.m_userData, "admin"))
			{
				this.m_userData.addAttribute("role", "admin", "15"); //grant user admin rights required for checkin
				this.m_userData.addAttribute("account", sNewAccount, "2");
			}			
					
			String cmd = "CHECKIN_SEL";
			DataBinder binder = new DataBinder();			
			Date dte = new Date();
			String deletedDate = LocaleUtils.formatODBC(dte);
			
			
			ResultSet docInfo = this.m_binder.getResultSet("ORIGINAL_DOC_INFO");			
			Table t = ResourceContainerUtils.getDynamicTableResource("CopyRevisionFieldsToInherit");    
			int numFields = docInfo.getNumFields();
			for (int i = 0; i < numFields; i++)
			{
				String fieldName = docInfo.getFieldName(i);
				if ((StringUtils.findStringIndex(t.m_colNames, fieldName) < 0) && (!fieldName.startsWith("x"))) {
					continue;
				}
				if (binder.getLocal(fieldName) != null)
				continue;
				binder.putLocal(fieldName, docInfo.getStringValue(i));
			}
			
			String tempFileName = new StringBuilder().append("").append(DataBinder.getNextFileCounter()).toString();
			String extension = ResultSetUtils.getValue(docInfo, "dExtension");
			String originalName = ResultSetUtils.getValue(docInfo, "dOriginalName");
						Report.trace("CloneAndDelete","createRevClone : Start of createRevClone 2",null);		

			if ((extension != null) && (extension.length() > 0))
			{
			  tempFileName = new StringBuilder().append(tempFileName).append(".").append(extension).toString();
			}
			String tempFilePath = new StringBuilder().append(DataBinder.m_tempDir).append(tempFileName).toString();
			DataBinder newBinder = new DataBinder();
			newBinder.addResultSet("ORIGINAL_DOC_INFO", docInfo);
			newBinder.putLocal("RenditionId", "primaryFile");
			newBinder.putLocal("RenditionId", "primaryFile");
			IdcFileDescriptor d = this.m_fileStore.createDescriptor(newBinder, null, this);
			newBinder = null;
			Report.trace("CloneAndDelete","createRevClone : Start of createRevClone 3",null);		

			try
			{
				this.m_fileStore.copyToLocalFile(d, new File(tempFilePath), null);
			}
			catch (IOException e)
			{
				Report.trace("CloneAndDelete","createRevClone : Exception ",null);
				this.createServiceException(e, null);
			}
			finally
			{
				this.m_userData=originalUserData;
			}
			Report.trace("CloneAndDelete","createRevClone : Start of createRevClone 4",null);		
			
			binder.addTempFile(tempFilePath);
			binder.putLocal("primaryFile", originalName);
			binder.putLocal("primaryFile:path", tempFilePath);
			binder.putLocal("RenditionId", "webViewableFile");
			binder.putLocal("dID",sNewDID);
			binder.putLocal("dDocName",sNewDocName);			
			binder.putLocal("dRevLabel","" + iRevLabel);
			binder.putLocal("dSecurityGroup",sNewSG);
			binder.putLocal("dDocAccount",sNewAccount);
			binder.putLocal("xContentDeletedOn",deletedDate);			
			binder.putLocal("dDocAuthor",this.m_userData.m_name);	
			binder.putLocal("addReleasedWebFile","1");	
			binder.putLocal("xIdcProfile","");
			
			binder.putLocal("xAuthorName",this.sDocAuthor);
			String sReason = this.m_binder.getLocal("xReason4Archive");
			if(sReason != null)
			{
				binder.putLocal("xReason4Archive",sReason);	
			}
			
			if(sCategoryID == null || sCategoryID.equals(""))
			{
				binder.putLocal("xCategoryID","");
			}
			else
			{
				binder.putLocal("xCategoryID",sCategoryID);	
			}
			
			//All Folder properties
			binder.removeLocal("xLibraryGUID");
			binder.removeLocal("fApplication");
			binder.removeLocal("fClbraAliasList");
			binder.removeLocal("fClbraRoleList");
			binder.removeLocal("fClbraUserList");
			binder.removeLocal("fCreateDate");
			binder.removeLocal("fCreator");
			binder.removeLocal("fDocAccount");
			binder.removeLocal("fFileGUID");
			binder.removeLocal("fFileGUIDs");
			binder.removeLocal("fFileName");
			binder.removeLocal("fInhibitPropagation");
			binder.removeLocal("fLastModifiedDate");
			binder.removeLocal("fLastModifier");
			binder.removeLocal("fLinkRank");
			binder.removeLocal("fOwner");
			binder.removeLocal("fParentGUID");
			binder.removeLocal("fParentGUID_display");
			binder.removeLocal("fPublishedFileName");
			binder.removeLocal("fSecurityGroup");
			binder.removeLocal("ownerFilePath");
			Report.trace("CloneAndDeleteBinder","copyanddelete :binder before checkin sel" + binder.toString(),null);
			
			try
			{
				this.m_requestImplementor.executeServiceTopLevelSimple(binder, cmd, this.m_userData);
				this.m_binder.putLocal("cdStatusCode","0");
				this.m_binder.putLocal("cdStatusMessage","Successfully Checked in Rev Clone");
			}
			catch(DataException de)
			{
				this.m_binder.putLocal("cdStatusCode","-5.1");
				this.m_binder.putLocal("cdStatusMessage","Failed to create rev clone");
				Report.trace("CloneAndDelete", null, de);
				Report.error(null, null, de);
			}
			catch(ServiceException se)
			{
				this.m_binder.putLocal("cdStatusCode","-5.2");
				this.m_binder.putLocal("cdStatusMessage","Failed to create rev clone");
				Report.trace("CloneAndDelete", null, se);
				Report.error(null, null, se);
			}
			finally
			{
				this.m_userData=originalUserData;
			}
			Report.trace("CloneAndDeleteBinder","copyanddelete :binder after" + binder.toString(),null);
			
			this.m_binder.putLocal("archiveddID",binder.getLocal("dID"));
			this.m_binder.putLocal("archiveddDocName",binder.getLocal("dDocName"));
			binder = null;
			Report.trace("CloneAndDelete","createClone : End of createRevClone",null);
			
	}
	
	public void createNewContent(String sdID,String sdDocName,String sNewDocName,String sCategoryID)
		throws ServiceException, DataException
	{	
			Report.trace("CloneAndDelete","createNewContent : Start of createNewContent",null);							
			UserData originalUserData = this.m_userData;	
			String sNewAccount="ECM-SFI-SECURE";
			try
			{
				sNewAccount = this.m_binder.getLocal("newAccount");
			}
			catch(Exception e1)
			{
				sNewAccount = "ECM-SFI-SECURE";
			}
			String sNewSG="Secure";
			try
			{
				sNewSG = this.m_binder.getLocal("newSG");
			}
			catch(Exception e1)
			{
				sNewSG = "Secure";
			}
			if(sNewSG == null)
			{
				sNewSG = "Secure";
			}
			if(sNewAccount == null)
			{
				sNewAccount = "ECM-SFI-SECURE";
			}


			
			if (!SecurityUtils.isUserOfRole(this.m_userData, "admin"))
			{
				this.m_userData.addAttribute("role", "admin", "15"); //grant user admin rights required for checkin
				this.m_userData.addAttribute("account", sNewAccount, "2");
			}			
					
			String cmd = "CHECKIN_NEW";
			DataBinder binder = new DataBinder();			
			Date dte = new Date();
			String deletedDate = LocaleUtils.formatODBC(dte);
			
			
			ResultSet docInfo = this.m_binder.getResultSet("ORIGINAL_DOC_INFO");			
			Table t = ResourceContainerUtils.getDynamicTableResource("CopyRevisionFieldsToInherit");    
			int numFields = docInfo.getNumFields();
			for (int i = 0; i < numFields; i++)
			{
				String fieldName = docInfo.getFieldName(i);
				if ((StringUtils.findStringIndex(t.m_colNames, fieldName) < 0) && (!fieldName.startsWith("x"))) {
					continue;
				}
				if (binder.getLocal(fieldName) != null)
				continue;
				binder.putLocal(fieldName, docInfo.getStringValue(i));
			}
			
			String tempFileName = new StringBuilder().append("").append(DataBinder.getNextFileCounter()).toString();
			String extension = ResultSetUtils.getValue(docInfo, "dExtension");
			String originalName = ResultSetUtils.getValue(docInfo, "dOriginalName");
			Report.trace("CloneAndDelete","createNewContent : Start of createNewContent 2",null);		

			if ((extension != null) && (extension.length() > 0))
			{
			  tempFileName = new StringBuilder().append(tempFileName).append(".").append(extension).toString();
			}
			String tempFilePath = new StringBuilder().append(DataBinder.m_tempDir).append(tempFileName).toString();
			DataBinder newBinder = new DataBinder();
			newBinder.addResultSet("ORIGINAL_DOC_INFO", docInfo);
			newBinder.putLocal("RenditionId", "primaryFile");
			newBinder.putLocal("RenditionId", "primaryFile");
			IdcFileDescriptor d = this.m_fileStore.createDescriptor(newBinder, null, this);
			newBinder = null;
			Report.trace("CloneAndDelete","createNewContent : Start of createNewContent 3",null);		

			try
			{
			  this.m_fileStore.copyToLocalFile(d, new File(tempFilePath), null);
			}
			catch (IOException e)
			{
				Report.trace("CloneAndDelete","createNewContent : Exception ",null);

				this.createServiceException(e, null);
			}
			finally
			{
				this.m_userData=originalUserData;
			}
			Report.trace("CloneAndDelete","createNewContent : Start of createNewContent 4",null);		
			
			binder.addTempFile(tempFilePath);
			binder.putLocal("primaryFile", originalName);
			binder.putLocal("primaryFile:path", tempFilePath);
			binder.putLocal("RenditionId", "webViewableFile");			
			binder.putLocal("dDocName",sNewDocName);						
			binder.putLocal("dSecurityGroup",sNewSG);
			binder.putLocal("dDocAccount",sNewAccount);
			binder.putLocal("xContentDeletedOn",deletedDate);
                        Report.trace("CloneAndDelete","deletedDate : " +deletedDate,null);   
                        binder.putLocal("xRecordFilingDate",deletedDate);
			binder.putLocal("dDocAuthor",this.m_userData.m_name);	
			binder.putLocal("addReleasedWebFile","1");	
			binder.putLocal("xIdcProfile","");
			
			binder.putLocal("xAuthorName",this.sDocAuthor);
			String sReason = this.m_binder.getLocal("xReason4Archive");
			if(sReason != null)
			{
				binder.putLocal("xReason4Archive",sReason);	
			}
			
			if(sCategoryID == null || sCategoryID.equals(""))
			{
				binder.putLocal("xCategoryID","");
			}
			else
			{
				binder.putLocal("xCategoryID",sCategoryID);	
			}
			
			//All Folder properties
			binder.removeLocal("xLibraryGUID");
			binder.removeLocal("fApplication");
			binder.removeLocal("fClbraAliasList");
			binder.removeLocal("fClbraRoleList");
			binder.removeLocal("fClbraUserList");
			binder.removeLocal("fCreateDate");
			binder.removeLocal("fCreator");
			binder.removeLocal("fDocAccount");
			binder.removeLocal("fFileGUID");
			binder.removeLocal("fFileGUIDs");
			binder.removeLocal("fFileName");
			binder.removeLocal("fInhibitPropagation");
			binder.removeLocal("fLastModifiedDate");
			binder.removeLocal("fLastModifier");
			binder.removeLocal("fLinkRank");
			binder.removeLocal("fOwner");
			binder.removeLocal("fParentGUID");
			binder.removeLocal("fParentGUID_display");
			binder.removeLocal("fPublishedFileName");
			binder.removeLocal("fSecurityGroup");
			binder.removeLocal("ownerFilePath");
			Report.trace("CloneAndDeleteBinder","createNewContent :binder before checkin sel" + binder.toString(),null);
			
			try
			{
				this.m_requestImplementor.executeServiceTopLevelSimple(binder, cmd, this.m_userData);
				this.m_binder.putLocal("cdStatusCode","0");
				this.m_binder.putLocal("cdStatusMessage","Successfully Checked in Rev Clone");
			}
			catch(DataException de)
			{
				this.m_binder.putLocal("cdStatusCode","-1.1");
				this.m_binder.putLocal("cdStatusMessage","Failed to Clone");
				Report.trace("CloneAndDelete", null, de);
				Report.error(null, null, de);
			}
			catch(ServiceException se)
			{
				this.m_binder.putLocal("cdStatusCode","-1.2");
				this.m_binder.putLocal("cdStatusMessage","Failed to Clone");
				Report.trace("CloneAndDelete", null, se);
				Report.error(null, null, se);
			}
			finally
			{
				this.m_userData=originalUserData;
			}
			Report.trace("CloneAndDeleteBinder","createNewContent :binder after" + binder.toString(),null);
			
			this.m_binder.putLocal("archiveddID",binder.getLocal("dID"));
			this.m_binder.putLocal("archiveddDocName",binder.getLocal("dDocName"));
			binder = null;
			Report.trace("CloneAndDelete","createNewContent : End of createRevClone",null);
			
			
	}

	public String returnOriginalReviewAuthor(String sDocName)
		throws ServiceException, DataException
	{		
		String returnValue = "";
		String query = "select dDocAuthor,drevrank from revisions" + 
						" where ddocname = '" + sDocName + "'" +
						" and dstatus = 'REVIEW'" +
						" order by drevrank desc";
		
        Report.trace("CloneAndDelete","query: " + query,null);
		try
		{
			ResultSet rset = this.m_workspace.createResultSetSQL(query);			
			if (!rset.isEmpty())
			{        
				 Report.trace("CloneAndDelete","query: not empty" ,null);
				rset.first();
				returnValue = ResultSetUtils.getValue(rset, "dDocAuthor");
			}	
			else
			{
				Report.trace("CloneAndDelete","query: empty" ,null);
			}
			rset = null;			
		}			
		finally
		{
			query = null;			
			Report.trace("CloneAndDelete","returnValue: " + returnValue,null);
			return returnValue;
		}		
	}
	
}