package SchreiberServices;
import intradoc.common.*;
import intradoc.data.*;
import intradoc.shared.*;
import intradoc.server.workflow.*;
import intradoc.server.Service;
import intradoc.server.DocProfileManager;
import intradoc.server.ServiceHandler;
import java.util.*;
import intradoc.folders.*;
import intradoc.server.IdcServiceAction;


public class customPostCreateFolder extends FoldersService
{

  @IdcServiceAction
  public void postCreateFolder()
    throws DataException, ServiceException
  {
	
	//Report.trace("SchreiberFilters","createFolder:From Alias component",null);	
	String strfPGUID = m_binder.get("fParentGUID");	
	String strfGUID = m_binder.get("fFolderGUID");
	
	createSubFoldersforDIS();
  }

  @IdcServiceAction
  public void posteditFolder()
    throws DataException, ServiceException
  {
	
	//Report.trace("SchreiberFilters","posteditFolder:From Alias component",null);	
	String strfPGUID = m_binder.get("fParentGUID");	
	String strfGUID = m_binder.get("fFolderGUID");
	
	//Report.trace("SchreiberFilters","posteditFolder: " + strfPGUID + "--"+ strfGUID,null);	
	editSubFoldersforDIS();
  }

	public void createSubFoldersforDIS()
		throws ServiceException, DataException
	{
		//Report.trace("SchreiberFilters","createSubFoldersforDIS:1: Start"  ,null);
		BaseFolders baseFolders = FoldersServiceUtils.getOrCreateBaseFoldersObject(this);
		baseFolders.calculateCurrentLocationInformation();
		String folderPath = baseFolders.m_currentItem.getPath();
		String spath = m_binder.getLocal("folderPath");
		String strPath_IS = "/IS-Project";
		if(spath!= null && spath.length()> 0 && spath.toUpperCase().contains(strPath_IS.toUpperCase()))
		{
			//Report.trace("SchreiberFilters","createSubFoldersforDIS:2: " + spath ,null);
		
		}
		else
		{
			//Report.trace("SchreiberFilters","createSubFoldersforDIS:3: path is null" + folderPath ,null);
			
		}
		String strISProjectGUID = fnRootProjectFolder("IS-Project");
		if(strISProjectGUID == null)
		{
			return;
			//Report.trace("SchreiberFilters","createSubFoldersforDIS:4: ISProject folder is not created" ,null);
			
		}
		//Report.trace("SchreiberFilters","createSubFoldersforDIS:5: " + spath ,null);
		//ResultSet rsFolderInfo = m_binder.getResultSet("FolderInfo");
			
		
		
		String strfGUID = m_binder.getLocal("fFolderGUID");
		String strfPGUID = m_binder.get("fParentGUID");
		
		if(strfGUID == null || strfPGUID == null || strfGUID.length() < 1 || strfPGUID.length() < 1)
		{
			return;
		}	
		if(strfPGUID.equals(strISProjectGUID))
		{
			ResultSet rset = null;
			String sRuleName = "IS_properties";
			DocProfileData data = DocProfileManager.getRule(sRuleName);
			DataBinder orulesbinder = new DataBinder();
			if (data == null)
			{
				Report.trace("SchreiberFilters","IS_properties is null" ,null);
			}
			else
			{
				orulesbinder = data.getData();
			}
			
			
			DataResultSet drset = (DataResultSet) orulesbinder.getResultSet("xProjectPhase.RestrictedList");
			if (drset != null)
			Report.trace("SchreiberFilters","xProjectPhase.RestrictedList : " + drset.toString() ,null);	
			String strProjectPhase = null;
			String strPrjPhaseGUID_temp = null;
			drset.first();
			this.m_workspace.beginTranEx(1);
			//loop through all Project phases and create Folders inside Project number
			for (drset.first(); drset.isRowPresent(); )
			{
				strProjectPhase = drset.getStringValueByName("dpRuleListValue");
				strPrjPhaseGUID_temp = fnCreateProjectPhaseFolder(strProjectPhase, strfGUID);
				//Report.trace("SchreiberFilters","createSubFoldersforDIS:88: " + strPrjPhaseGUID_temp ,null);
				String strFName = m_binder.getLocal("fFolderName");
				fnSetFolderDefaults(strPrjPhaseGUID_temp, strFName, strProjectPhase);
				drset.next();
			}
			this.m_workspace.commitTran();
			//Report.trace("SchreiberFilters","createSubFoldersforDIS:99: end of rows"  ,null);
			drset = null;
			orulesbinder = null;
			
			strProjectPhase = null;
			
		}
		else
		{
			//Report.trace("SchreiberFilters","createSubFoldersforDIS:9: not a IS project fodler" ,null);
		
		}
		
		
		
	}
	
	
	public String fnRootProjectFolder(String strbaseFolder)
		throws ServiceException, DataException
	{			
		String query = "select ffolderguid from folderfolders where ffoldername = '" + strbaseFolder + "' and fparentguid = 'FLD_ROOT'" ;
		ResultSet rset = this.m_workspace.createResultSetSQL(query);		
		String strfGUID = null;
		if ((rset != null) && (rset.isRowPresent()))
		{
			strfGUID = rset.getStringValue(0);
		}
		rset = null;
		query = null;
		//Report.trace("SchreiberFilters","fnRootProjectFolder:1: root ID : " + strfGUID ,null);

		return strfGUID;	//IS-Project folder ID	
	
	}
	
	public void fnSetFolderDefaults(String strPrjPhaseGUID, String strPrjNum, String strProjectPhase)
		 throws DataException
	{
		try
		{
				//String query = "Update foldermetadefaults set " + 
					//"xProjectNumber = '" + strPrjNum + "', xprojectphase='" + strProjectPhase + "'" +
					//"where ffolderguid = '" + strPrjPhaseGUID + "'";
			String query = "insert into foldermetadefaults (ffolderguid,xProjectnumber, xProjectPhase) values ('" +
				strPrjPhaseGUID + "','" + strPrjNum + "','" + strProjectPhase + "')";
			//Report.trace("SchreiberFilters","fnSetFolderDefaults:1: query : " + query ,null);
					
			this.m_workspace.executeSQL(query);
		}
		catch (DataException e)
		{
			//Report.trace("SchreiberFilters", null, e);
		}
	}
	
	public String fnCreateProjectPhaseFolder(String strPrjPhase, String strPrjNumGUID)
		throws ServiceException, DataException
	{	
		return CreateFolder(strPrjPhase, strPrjNumGUID);
	}
	
	protected String CreateFolder(String strPrjNum, String strParentGUID)
		throws ServiceException, DataException
	{		
		String newGUID = StringUtils.createGUIDEx(32, 0, "");
		//Report.trace("SchreiberFilters","setFolderGUID:Creating folder" ,null);
		Properties oldProps = m_binder.getLocalData();
		Properties p = new Properties();
		m_binder.setLocalData(p);
		 
		Date date = new Date();
		String strDate = LocaleUtils.formatODBC(date);
		
		m_binder.putLocal("fFolderGUID",newGUID);
		m_binder.putLocal("fParentGUID",strParentGUID);
		m_binder.putLocal("fFolderName",strPrjNum);
		m_binder.putLocal("fInhibitPropagation","1");
		m_binder.putLocal("fPromptForMetadata","1");
		m_binder.putLocal("fIsContribution","1");
		m_binder.putLocal("fTargetGUID","");
		m_binder.putLocal("fApplication","framework");
		m_binder.putLocal("fOwner","weblogic");
		m_binder.putLocal("fCreator","weblogic");
		m_binder.putLocal("fLastModifier","weblogic");
		m_binder.putLocal("fCreateDate",strDate);
		m_binder.putLocal("fLastModifiedDate",strDate);
		m_binder.putLocal("fSecurityGroup","Internal");
		m_binder.putLocal("fDocAccount","ECM-IS-PR");
		m_binder.putLocal("fClbraUserList","");
		m_binder.putLocal("fClbraAliasList","");
		m_binder.putLocal("fClbraRoleList","");		
		//begin PS7 changes
		this.m_binder.putLocal("fFolderType","owner");
		this.m_binder.putLocal("fFolderDescription","");
		this.m_binder.putLocal("fLibraryType","");
		this.m_binder.putLocal("fIsLibrary","0");
		this.m_binder.putLocal("fDocClasses","");
		this.m_binder.putLocal("fChildFoldersCount","0");
		this.m_binder.putLocal("fChildFilesCount","0");
		this.m_binder.putLocal("fApplicationGUID","");
		//end PS7 changes
		this.m_workspace.execute("IfldFolder", m_binder);	
		m_binder.setLocalData(oldProps);	
		return newGUID;
	}
	
	public void editSubFoldersforDIS()
		throws ServiceException, DataException
	{
		//Report.trace("SchreiberFilters","editSubFoldersforDIS:1: Start"  ,null);
		BaseFolders baseFolders = FoldersServiceUtils.getOrCreateBaseFoldersObject(this);
		baseFolders.calculateCurrentLocationInformation();
		String folderPath = baseFolders.m_currentItem.getPath();
		String spath = m_binder.getLocal("folderPath");
		String strPath_IS = "/IS-Project";
		if(spath!= null && spath.length()> 0 && spath.toUpperCase().contains(strPath_IS.toUpperCase()))
		{
			//Report.trace("SchreiberFilters","editSubFoldersforDIS:2: " + spath ,null);		
		}
		else
		{
			//Report.trace("SchreiberFilters","editSubFoldersforDIS:3: path is null"  + folderPath  ,null);			
		}
		String strISProjectGUID = fnRootProjectFolder("IS-Project");
		if(strISProjectGUID == null)
		{
			//Report.trace("SchreiberFilters","editSubFoldersforDIS:4: ISProject folder is not created" ,null);			
		}
		//Report.trace("SchreiberFilters","editSubFoldersforDIS:5: " + spath ,null);
		//ResultSet rsFolderInfo = m_binder.getResultSet("FolderInfo");
	
		String strfGUID = m_binder.getLocal("fFolderGUID");
		String strfPGUID = m_binder.get("fParentGUID");
		String strfName = m_binder.getLocal("fFolderName");
		//Report.trace("SchreiberFilters","editSubFoldersforDIS:64:" + strfGUID + "--" + strfPGUID + "--" + strfName,null);			
		if(strfGUID == null || strfPGUID == null || strfGUID.length() < 1 || strfPGUID.length() < 1)
		{
			//Report.trace("SchreiberFilters","editSubFoldersforDIS:-23: parent folder null",null);			

			return;
		}	
		if(strfPGUID.equals(strISProjectGUID))
		{
			String query = "select fFolderGUID,fFolderName from folderFolders where fParentGUID = '" + strfGUID + "'";			
			ResultSet rset = this.m_workspace.createResultSetSQL(query);	
			//Report.trace("SchreiberFilters","editSubFoldersforDIS:33: ",null);						
			rset.first();
			
			if(rset != null)
			{
				String strfCGUID = null;
				String strfCName = null;
				DataResultSet drset = new DataResultSet();
				drset.copy(rset);				
				drset.first();
				this.m_workspace.beginTranEx(1);
				//Report.trace("SchreiberFilters","editSubFoldersforDIS:33: " + drset.getNumRows() ,null);						
				while (drset.isRowPresent())
				{
					strfCGUID = drset.getStringValue(0);
					strfCName = drset.getStringValue(1);
					fnUpdateFolderDefaults(strfCGUID, strfName, strfCName);
					drset.next();			
				}
				this.m_workspace.commitTran();
			}
			else
			{
				//Report.trace("SchreiberFilters","editSubFoldersforDIS:66: rset null ",null);						
			}
			
		}	
	
	}
	
	public void fnUpdateFolderDefaults(String strPrjPhaseGUID, String strPrjNum, String strProjectPhase)
		 throws DataException
	{
		try
		{
				String query = "Update foldermetadefaults set " + 
					"xProjectNumber = '" + strPrjNum + "', xprojectphase='" + strProjectPhase + "'" +
					"where ffolderguid = '" + strPrjPhaseGUID + "'";			
			//Report.trace("SchreiberFilters","fnSetFolderDefaults:1: query : " + query ,null);
					
			this.m_workspace.executeSQL(query);
		}
		catch (DataException e)
		{
			//Report.trace("SchreiberFilters", null, e);
		}
	}
}	