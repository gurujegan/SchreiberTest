
 package SchreiberFilters;

import intradoc.shared.*;
import intradoc.data.*;
import intradoc.common.*;
import intradoc.server.*;
import java.util.Properties;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Date;


public class setFolderGUID implements FilterImplementor
{
	Workspace m_ws;
	DataBinder m_binder;
	ExecutionContext m_cxt;
	public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt)
		throws DataException, ServiceException
	{
		
		Report.trace("SchreiberFilters","setFolderGUID:Filter invoked!",null);		
		this.m_ws = ws;
		this.m_binder = binder;
		this.m_cxt = cxt;
		int returnCode = 0;
		
		if (cxt == null)
		{
			return CONTINUE;
		}
		Object paramObj = cxt.getCachedObject("filterParameter");
		if ((paramObj == null) || (!(paramObj instanceof String)))
		{
		  return 0;
		}
		
		String parameter = (String)paramObj;		
		if (parameter.equals("createFolderAtCheckin"))
		{
		  returnCode = createFolderAtCheckin();
		  return returnCode;
		}
		else if (parameter.equals("createSubFoldersforDIS"))
		{
		  returnCode = createSubFoldersforDIS();
		  return returnCode;
		}
		parameter = null;
		return returnCode;
	}
	
	
	public int createFolderAtCheckin()
		throws ServiceException, DataException
	{
		Report.trace("SchreiberFilters","inside method createFolderAtCheckin:start" ,null);
		String strIdcService = this.m_binder.getSearchAllAllowMissing("IdcService");		
		if(strIdcService == null )
		{
			Report.trace("SchreiberFilters","createFolderAtCheckin: IdcService is null" ,null);
			return CONTINUE;
		}
		
		String strProfile = this.m_binder.getSearchAllAllowMissing("xIdcProfile");		
		if(strProfile == null) 
		{
			Report.trace("SchreiberFilters","createFolderAtCheckin: Profile is null" ,null);
			return CONTINUE;
		}
		if(!(strProfile.equals("IS-Project"))){
			Report.trace("SchreiberFilters","createFolderAtCheckin:not IS-Project Profile" ,null);
			return CONTINUE;
		}
		String strSecGroup = this.m_binder.getSearchAllAllowMissing("dSecurityGroup");
		if(strSecGroup == null || !(strSecGroup.equals("Internal")))
		{
			Report.trace("SchreiberFilters","createFolderAtCheckin:not Internal" ,null);
			return CONTINUE;
		}
		String strISProjectGUID = fnRootProjectFolder("IS-Project");
		if(strISProjectGUID == null)
		{
			Report.trace("SchreiberFilters","createFolderAtCheckin: No base ISProject folder" ,null);
			return CONTINUE;
		}
		
		String strPrjNum = this.m_binder.getLocal("xProjectNumber");
		String strPrjPhase = this.m_binder.getLocal("xProjectPhase");
		
		
		boolean isNew = DataBinderUtils.getBoolean(this.m_binder, "isNew", false);
		boolean isUpdate = DataBinderUtils.getBoolean(this.m_binder, "isUpdate", false);

		
		if(strPrjNum == null || strPrjPhase == null)
		{
			Report.trace("SchreiberFilters","Proj No or Phase is null" ,null);
			return CONTINUE;
		}
		String strPrjNumGUID = fnExistsProjectNoFolder(strPrjNum,strISProjectGUID);
		String strPrjPhaseGUID = null;
		String strPrjPhaseGUID_temp = null;
		if(strPrjNumGUID == null || strPrjNumGUID.length() == 0) //Project Number folder doesnt exist, so create a folder
		{
			Report.trace("SchreiberFilters","createFolderAtCheckin: Project Folder does not exist" ,null);
			//Create Project Number Folder and get the GUID
			strPrjNumGUID = fnCreateProjectNoFolder(strPrjNum, strISProjectGUID);					
			//String optionListName = "ProjectPhaseList";
			//Map args = new HashMap();
			//args.put("dKey", optionListName);
			// Load Project Phases
			//ResultSet rset = this.m_ws.createResultSet("QoptionList", new MapParameters(args));
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
			//FieldInfo[] fis = ResultSetUtils.createInfoList(drset, new String[] { "dOption" }, true);
			String strProjectPhase = null;
			
			//loop through all Project phases and create Folders inside Project number
			for (drset.first(); drset.isRowPresent(); )
			{
				strProjectPhase = drset.getStringValueByName("dpRuleListValue");
				
				strPrjPhaseGUID_temp = fnCreateProjectPhaseFolder(strProjectPhase, strPrjNumGUID);
				
				fnSetFolderDefaults(strPrjPhaseGUID_temp, strPrjNum, strProjectPhase);
				//Get the GUID for the project Phase metadata field
				if(strPrjPhase.toUpperCase().equals(strProjectPhase.toUpperCase()))
				{
					strPrjPhaseGUID = strPrjPhaseGUID_temp;						
				}
				drset.next();
			}
			//Report.trace("SchreiberFilters","createFolderAtCheckin:99: end of rows"  ,null);
			drset = null;
			strProjectPhase = null;
			orulesbinder = null;
			
		}
		else if(strPrjNumGUID != null && strPrjNumGUID.length() > 0) //folder with Project number already exists
		{
			//Report.trace("SchreiberFilters","createFolderAtCheckin:11: Project Num " +strPrjNumGUID  ,null);
			strPrjPhaseGUID = fnExistsProjectPhaseFolder(strPrjPhase, strPrjNumGUID);			
			if(strPrjPhaseGUID == null)
			{
				//Report.trace("SchreiberFilters","createFolderAtCheckin:99: null "  ,null);
				strPrjPhaseGUID = fnCreateProjectPhaseFolder(strPrjPhase, strPrjNumGUID);				
			}
			//Report.trace("SchreiberFilters","createFolderAtCheckin:12: Project Phase " + strPrjPhaseGUID  ,null);
		}
		
		if(strPrjPhaseGUID != null && strPrjPhaseGUID.length() > 0)
		{
			//set the Project Phase GUID as the content Parent GUID
			if(isNew)
			{
				Report.trace("SchreiberFilters","createFolderAtCheckin:14: New Checkin : " + strPrjPhaseGUID ,null);
				this.m_binder.putLocal("fParentGUID",strPrjPhaseGUID);
			}
			else if(isUpdate)
			{
			
				Report.trace("SchreiberFilters","createFolderAtCheckin:15: Update"  ,null);
				ResultSet rsetDI = this.m_binder.getResultSet("DOC_INFO");
				String stroldProjNo = null;
				String stroldProjPhase = null;
				if(rsetDI != null)
				{
					stroldProjNo = this.m_binder.getResultSetValue(rsetDI,"xProjectNumber").toUpperCase();
					stroldProjPhase = this.m_binder.getResultSetValue(rsetDI,"xProjectPhase").toUpperCase();
				}
				else
				{
					//Report.trace("SchreiberFilters","createFolderAtCheckin: DOC_INFO is null",null);			
				}
				if(stroldProjNo != null && stroldProjPhase != null)
				{
					if(stroldProjNo.equals(strPrjNum.toUpperCase()) && stroldProjPhase.equals(strPrjPhase.toUpperCase()))
					{
						Report.trace("SchreiberFilters","setFolderGUIDNew: no change in project number" ,null);	
						return CONTINUE;
					}
					else
					{
						Report.trace("SchreiberFilters","createFolderAtCheckin:18: before move : " ,null);
						moveDocumentToFolder(strPrjPhaseGUID);					
					}
				
				}
			}
			return CONTINUE;
		}
		else
		{
			Report.trace("SchreiberFilters","createFolderAtCheckin:22: phase guid null : " ,null);
		}
		
		
		//moveSQFDocumentsToFolder();		
		
		
		strIdcService = null;
		strProfile = null;
		strISProjectGUID = null;
		strPrjNum = null;
		strPrjNumGUID = null;
		strPrjPhaseGUID = null;
		strPrjPhaseGUID_temp = null;
		return CONTINUE;
	}
	
	//check if the Project Number fodler exists in IS-Project
	public String fnRootProjectFolder(String strbaseFolder)
		throws ServiceException, DataException
	{			
		String query = "select ffolderguid from folderfolders where ffoldername = '" + strbaseFolder + "' and fparentguid = 'FLD_ROOT'" ;
		ResultSet rset = this.m_ws.createResultSetSQL(query);		
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
	
	//check if the Project Number fodler exists in IS-Project
	public String fnExistsProjectNoFolder(String strPrjNum, String strISProjectGUID)
		throws ServiceException, DataException
	{			
		String query = "select ffolderguid from folderFolders where " +
					"upper(ffoldername) = upper('" + strPrjNum + "') AND " +
					"fparentguid='" + strISProjectGUID + "'" ;
		//Report.trace("SchreiberFilters","fnExistsProjectNoFolder:1: prj query : " + query ,null);
		ResultSet rset = this.m_ws.createResultSetSQL(query);		
		String strfGUID = null;
		if ((rset != null) && (rset.isRowPresent()))
		{
			strfGUID = rset.getStringValue(0);
		}
		rset = null;
		query = null;
		return strfGUID;	//Project Number folder ID	
		
	
	}
	//check if Project Phase Exists
	public String fnExistsProjectPhaseFolder(String strPrjPhase, String strParentFGUID)
		throws ServiceException, DataException
	{
		String query = "select ffolderguid from folderFolders where " +
					"upper(ffoldername) = upper('" + strPrjPhase + "') AND " +
					"fparentguid= '" + strParentFGUID + "'" ;
		//Report.trace("SchreiberFilters","fnExistsProjectPhaseFolder:1: prj query : " + query ,null);
		ResultSet rset = this.m_ws.createResultSetSQL(query);		
		String strfGUID = null;
		if ((rset != null) && (rset.isRowPresent()))
		{
			strfGUID = rset.getStringValue(0);		
		}
		
		rset = null;
		query = null;
		return strfGUID;	//Project phase folder ID	
		
	}
	public String fnCreateProjectNoFolder(String strPrjNum, String strISProjectGUID)
		throws ServiceException, DataException
	{
		return CreateFolder(strPrjNum.toUpperCase(), strISProjectGUID);		
	}
	public String fnCreateProjectPhaseFolder(String strPrjPhase, String strPrjNumGUID)
		throws ServiceException, DataException
	{	
		return CreateFolder(strPrjPhase, strPrjNumGUID);
	}
	
	//function to set default metadata for folders.
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
					
			this.m_ws.executeSQL(query);
		}
		catch (DataException e)
		{
			//Report.trace("SchreiberFilters", null, e);
		}
	}
	
	
	
	protected void moveDocumentToFolder(String strPrjPhaseGUID)
		throws ServiceException, DataException
	{
		
			String docGUID = null;
			String strfFileName = null;
			Properties oldProps = this.m_binder.getLocalData();
			Properties p = new Properties();	
			this.m_binder.setLocalData(p);			
			
			Map args = new HashMap();
			String dDocName = this.m_binder.getAllowMissing("dDocName");
			args.put("dDocName", dDocName);
			ResultSet rsetFI = this.m_ws.createResultSet("QfldOwnerFileByDocName", new MapParameters(args));			
			if(rsetFI != null)
			{
				docGUID = this.m_binder.getResultSetValue(rsetFI, "fFileGUID");
				strfFileName = this.m_binder.getResultSetValue(rsetFI, "fFileName");
				//Report.trace("SchreiberFilters","moveDocumentToFolder: 1--" + docGUID + "--" + strfFileName,null);
			}
			else
			{
				//Report.trace("SchreiberFilters","moveDocumentToFolder: 2 File_INFO set is null" ,null);
			
			}
			this.m_binder.setLocalData(oldProps);	

			//Report.trace("SchreiberFilters","moveDocumentToFolder: 3" ,null);

			if(strfFileName == null)
			{
				ResultSet rsetDI = this.m_binder.getResultSet("DOC_INFO");
				if(rsetDI != null)
				{
					strfFileName = this.m_binder.getResultSetValue(rsetDI, "dOriginalName");
				}
				else
				{
					strfFileName = "NewDoc.docx";
				}
				//Report.trace("SchreiberFilters","moveDocumentToFolder:4:" + strfFileName,null);

			}
			if(docGUID == null || docGUID.length() < 1)
			{
				docGUID = StringUtils.createGUIDEx(32, 0, "");
				//Report.trace("SchreiberFilters","moveDocumentToFolder: 5" + docGUID,null);
			}
			else
			{
				MoveDocToFolder(docGUID,strPrjPhaseGUID, strfFileName);		
			}
			this.m_binder.setLocalData(oldProps);
	}
	
	protected String CreateFolder(String strPrjNum, String strParentGUID)
		throws ServiceException, DataException
	{		
		String newGUID = StringUtils.createGUIDEx(32, 0, "");
		Report.trace("SchreiberFilters","setFolderGUID:Creating folder" + strPrjNum,null);
		Properties oldProps = this.m_binder.getLocalData();
		Properties p = new Properties();
		this.m_binder.setLocalData(p);
		 
		Date date = new Date();
		String strDate = LocaleUtils.formatODBC(date);
		
		
		
		this.m_binder.putLocal("fFolderGUID",newGUID);
		this.m_binder.putLocal("fParentGUID",strParentGUID);
		this.m_binder.putLocal("fFolderName",strPrjNum);
		this.m_binder.putLocal("fInhibitPropagation","1");
		this.m_binder.putLocal("fPromptForMetadata","1");
		this.m_binder.putLocal("fIsContribution","1");
		this.m_binder.putLocal("fTargetGUID","");
		this.m_binder.putLocal("fApplication","framework");
		this.m_binder.putLocal("fOwner","weblogic");
		this.m_binder.putLocal("fCreator","weblogic");
		this.m_binder.putLocal("fLastModifier","weblogic");
		this.m_binder.putLocal("fCreateDate",strDate);
		this.m_binder.putLocal("fLastModifiedDate",strDate);
		this.m_binder.putLocal("fSecurityGroup","Internal");
		this.m_binder.putLocal("fDocAccount","ECM-PUBLIC-IS-PR");
		this.m_binder.putLocal("fClbraUserList","");
		this.m_binder.putLocal("fClbraAliasList","");
		this.m_binder.putLocal("fClbraRoleList","");	
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
		this.m_ws.execute("IfldFolder", this.m_binder);	
		this.m_binder.setLocalData(oldProps);
		
		return newGUID;
	}
	
	
	protected String MoveDocToFolder(String docGUID,String strfGUID, String strfFileName)
		throws ServiceException, DataException
	{		
		String newGUID = StringUtils.createGUIDEx(32, 0, "");
		Report.trace("SchreiberFilters","setFolderGUID:MoveDocToFolder" ,null);
		Properties oldProps = this.m_binder.getLocalData();
		Properties p = new Properties();
		
		//Report.trace("SchreiberFilters","setFolderGUIDNew: 31" + docGUID,null);
			 
		Date date = new Date();
		String strDate = LocaleUtils.formatODBC(date);
		
		this.m_binder.putLocal("fFileGUID",docGUID);
		this.m_binder.putLocal("fParentGUID",strfGUID);				
		this.m_binder.putLocal("fOwner","weblogic");		
		this.m_binder.putLocal("fLastModifier","weblogic");	
		this.m_binder.putLocal("fLastModifiedDate",strDate);
		this.m_binder.putLocal("fInhibitPropagation","1");		 
		this.m_binder.putLocal("fFileName",strfFileName);
		
		
		this.m_ws.execute("UfldMoveFile", this.m_binder);	

		this.m_binder.setLocalData(oldProps);	
		return newGUID;
	}
	
	
	
	
	public int 	createSubFoldersforDIS()
		throws ServiceException, DataException
	{
		Report.trace("SchreiberFilters","createSubFoldersforDIS:Start"  ,null);

		String spath = this.m_binder.getLocal("folderPath");
		String strPath_IS = "/IS-Project";
		if(spath!= null && spath.length()> 0 && spath.toUpperCase().contains(strPath_IS.toUpperCase()))
		{
			//Report.trace("SchreiberFilters","createSubFoldersforDIS:2: " + spath ,null);
		
		}
		else
		{
			//Report.trace("SchreiberFilters","createSubFoldersforDIS:3: path is null"  ,null);
		}
		String strISProjectGUID = fnRootProjectFolder("IS-Project");
		if(strISProjectGUID == null)
		{
			Report.trace("SchreiberFilters","createSubFoldersforDIS: ISProject folder is not created" ,null);
			return CONTINUE;
		}
		//Report.trace("SchreiberFilters","createSubFoldersforDIS:5: " + spath ,null);
		
			
		
		
		String strfGUID = this.m_binder.getLocal("fFolderGUID");
		String strfPGUID = this.m_binder.get("fParentGUID");
		
		if(strfGUID == null || strfPGUID == null || strfGUID.length() < 1 || strfPGUID.length() < 1)
		{
			Report.trace("SchreiberFilters","createSubFoldersforDIS: Unknown" ,null);
			return CONTINUE;
		}	
		if(strfPGUID.equals(strISProjectGUID))
		{
			//Report.trace("SchreiberFilters","createSubFoldersforDIS:8: creatign sub folders" ,null);
			String optionListName = "ProjectPhaseList";
			Map args = new HashMap();
			args.put("dKey", optionListName);
			// Load Project Phases
			ResultSet rset = this.m_ws.createResultSet("QoptionList", new MapParameters(args));
			int optionIndex  = 0;
			if(rset != null)
			{
				DataResultSet drset = new DataResultSet();
				drset.copy(rset);
				//Report.trace("SchreiberFilters","createSubFoldersforDIS:7: optionslist not null: " + drset.getNumRows() ,null);				
				FieldInfo[] fis = ResultSetUtils.createInfoList(drset, new String[] { "dOption" }, true);
				String strProjectPhase = null;
				String strPrjPhaseGUID_temp = null;
				drset.first();
				//loop through all Project phases and create Folders inside Project number
				while (drset.isRowPresent())
				{
					strProjectPhase = drset.getStringValue(fis[0].m_index).toUpperCase();
					//Report.trace("SchreiberFilters","createSubFoldersforDIS:8: " + strProjectPhase ,null);
					strPrjPhaseGUID_temp = fnCreateProjectPhaseFolder(strProjectPhase, strfGUID);
					//Report.trace("SchreiberFilters","createSubFoldersforDIS:88: " + strPrjPhaseGUID_temp ,null);
					String strFName = this.m_binder.getLocal("fFolderName");
					fnSetFolderDefaults(strPrjPhaseGUID_temp, strFName, strProjectPhase);
					drset.next();
				}
				//Report.trace("SchreiberFilters","createSubFoldersforDIS:99: end of rows"  ,null);
				drset = null;
				rset = null;
				fis = null;
				strProjectPhase = null;
			}
			else
			{
				Report.trace("SchreiberFilters","createSubFoldersforDIS:10: optionslist is null" ,null);
			}
		}
		else
		{
			Report.trace("SchreiberFilters","createSubFoldersforDIS:9: not a IS project folder" ,null);
			return CONTINUE;
		}
		
		
		return CONTINUE;
	}
}
