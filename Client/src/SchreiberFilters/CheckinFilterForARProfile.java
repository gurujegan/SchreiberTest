package SchreiberFilters;

import intradoc.shared.*;
import intradoc.common.*;
import intradoc.data.*;

import intradoc.server.Service;
import intradoc.server.PageMerger;
import intradoc.server.script.ScriptExtensionUtils;

import java.io.IOException;

import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import java.io.InputStream;
import java.util.Iterator;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CheckinFilterForARProfile implements FilterImplementor
{	
	public HashMap cellCollection = null;
	Workspace m_ws;
	public int doFilter(Workspace ws, DataBinder m_binder, ExecutionContext cxt)
		throws DataException, ServiceException
	{	  	
		this.m_ws = ws;
		if (cxt == null) 
		{				
			return CONTINUE;
		}		
		
		Object paramObj = cxt.getCachedObject("filterParameter");
		if ((paramObj == null) || (!(paramObj instanceof String)))
		{			
		  return 0;
		}
		int returnCode = 0;
		String param = (String)paramObj;
		if (!param.equals("CheckinFilterForARProfile"))
		{		
			return CONTINUE;
		}		
		
		boolean isCheckin = DataBinderUtils.getLocalBoolean(m_binder, "isCheckin", false);		
		if (!isCheckin) 
		{		
			return CONTINUE;
		}
		
		String strIdcProfile = m_binder.getLocal("xIdcProfile");				
		String strDocType = m_binder.getAllowMissing("dDocType");
		String strSubType = m_binder.getAllowMissing("xSubType");				
		
		if(strIdcProfile == null || strIdcProfile.length() == 0 || !(strIdcProfile.equals("AR") || strIdcProfile.equals("ARForms")))
		{			
			strDocType = null;
			strSubType = null;
			return CONTINUE;
		}		
		if(strDocType == null || (!strDocType.equals("Form")) )
		{			
			strIdcProfile = null;			
			strDocType = null;
			strSubType = null;
			return CONTINUE;
		}
		
		if(strSubType == null || strSubType.length() == 0) 
		{		
			strIdcProfile = null;			
			strDocType = null;
			strSubType = null;
			return CONTINUE;
		}
	
		try
		{
			
			computeCustomMetadata(m_binder);
			Service service = null;
			if ((cxt instanceof Service))
			{
				service = (Service)cxt;
				String strsheetNotFound = m_binder.getAllowMissing("sheetNotFound");
				String strNoLocationFound = m_binder.getAllowMissing("NoLocationFound");
				String strFoundHomeOffice = m_binder.getAllowMissing("FoundHomeOffice");
				String strPlantApprNotFound = m_binder.getAllowMissing("PlantApproversNotFound");
				String strIntlPlantNotFound = m_binder.getAllowMissing("IntlPlantNotFound");
				if(strsheetNotFound != null && strsheetNotFound.equals("true"))
				{							
					PageMerger pageMerger = ScriptExtensionUtils.getPageMerger(cxt);
					pageMerger.evaluateResourceInclude("compute_checkin_error_message_ar");
					String strARMail = m_binder.get("strARAdminMail");
					String errMsg = LocaleUtils.encodeMessage("arCheckinErrMsg", null, strARMail);							
					throw new ServiceException(errMsg);
				}
				else if(strFoundHomeOffice != null && strFoundHomeOffice.equals("true"))
				{
					PageMerger pageMerger = ScriptExtensionUtils.getPageMerger(cxt);
					pageMerger.evaluateResourceInclude("compute_errmsg_plantapprovers_ar");
					String errMsg = m_binder.get("errARPlantMsg");					
					throw new ServiceException(errMsg);
				}
				else if(strNoLocationFound != null && strNoLocationFound.equals("true"))
				{
					PageMerger pageMerger = ScriptExtensionUtils.getPageMerger(cxt);
					pageMerger.evaluateResourceInclude("compute_checkin_error_message_ar");
					String strARMail = m_binder.get("strARAdminMail");
					String errMsg = "Location cannot be empty. Contact AR Admin(" + strARMail + ") for Additional help.";
					throw new ServiceException(errMsg);
				}
				else if(strPlantApprNotFound != null && strPlantApprNotFound.equals("true"))
				{
					PageMerger pageMerger = ScriptExtensionUtils.getPageMerger(cxt);
					pageMerger.evaluateResourceInclude("compute_errmsg_plantapprovers_ar");					
					String errMsg = m_binder.get("errARPlantMsg");					
					throw new ServiceException(errMsg);
				}
				else if(strIntlPlantNotFound != null && strIntlPlantNotFound.equals("true"))
				{
					PageMerger pageMerger = ScriptExtensionUtils.getPageMerger(cxt);
					pageMerger.evaluateResourceInclude("compute_errmsg_plantapprovers_ar");					
					String errMsg = m_binder.get("errARPlantMsg");					
					throw new ServiceException(errMsg);
				}
			}
			else
			{
				return 0;
			}
		}
		catch (IOException ioe1)
		{	
			Report.error("ARCheckin",null,ioe1);

			return CONTINUE;					
		}
		catch (SAXException se1)
		{	
			Report.error("ARCheckin",null,se1);
			return CONTINUE;					
		}
		catch (OpenXML4JException oxml1)
		{	
			Report.error("ARCheckin",null,oxml1);
			return CONTINUE;					
		}
			
		
		strIdcProfile = null;		
		strDocType = null;
		strSubType = null;
		return CONTINUE;
	}
	
	
	public String getCollectionValue(String id)
	{
		
		String strValue = (String)cellCollection.get(id);
		if(strValue == null)
			strValue = "";
		return strValue;
	}
	
	public String getCollectionValuePercent(String id)
	{
		
		String strValue = (String)cellCollection.get(id);
		if(strValue == null)
		{
			strValue = "";
		}
		else
		{
			NumberFormat numberFormatter1 = NumberFormat.getPercentInstance();
			try
			{
				strValue  = numberFormatter1.format(Double.parseDouble(strValue));
			}
			catch(java.lang.NumberFormatException en)
			{
				strValue  = "";
			}
		}
		
		return strValue;
	}
	
	
	public String getCollectionValueCurrency(String id)
	{
		
		String strValue = (String)cellCollection.get(id);
		if(strValue == null)
		{
			strValue = "";
		}
		else
		{
			strValue = "$" + strValue;
		}
		return strValue;
	}
	
/**
function: computeCustomMetadata
description: read data from checked in Excel file and update metadata values.
*/
	public  void computeCustomMetadata(DataBinder m_binder)
		throws IOException, SAXException, OpenXML4JException
	{
		OPCPackage pkg = null;
		String strSubType = m_binder.getAllowMissing("xSubType");	
		String strIsHomeOffice = m_binder.getAllowMissing("xSensitive");	
		try 
		{
			String strFilePath = m_binder.getLocal("primaryFile:path");
			if(strFilePath == null || strFilePath.length() == 0)
			{				
				return;			
			}
			String ext = "";
			int iIndex= strFilePath.lastIndexOf(".");
			if(iIndex > 0)
			{
				ext = strFilePath.substring(iIndex+1,strFilePath.length());
			}
			else
			{
				return;
			}
			//Extract data from xlsm and xlsx files.
			if(ext == null || ext.length() == 0 || !(ext.toLowerCase().equals("xlsm") || ext.toLowerCase().equals("xlsx") ))
			{				
				return;	
			}
			
			try
			{
				pkg = OPCPackage.open(strFilePath);
			}
			catch(InvalidFormatException ife)
			{
				m_binder.putLocal("sheetNotFound","true");
				return;				
			}
			cellCollection = new HashMap();
			XSSFReader objReader = new XSSFReader(pkg);
			SharedStringsTable sst = objReader.getSharedStringsTable();
			XMLReader parser = fetchSheetParser(sst);
			XSSFReader.SheetIterator sheetiter = (XSSFReader.SheetIterator)objReader.getSheetsData();			
			String sheetName;
			InputStream stream;
			boolean blSheetFound = false;
			while (sheetiter.hasNext()) 
			{
				stream = sheetiter.next();
				sheetName = sheetiter.getSheetName().toLowerCase();				
				if(sheetName.equals("metadata"))
				{
					blSheetFound = true;
					InputSource sheetSource = new InputSource(stream);
					parser.parse(sheetSource);
					sheetSource = null;
					break;
				}				
				stream.close();				
			}
			stream = null;
			sheetName = null;
			sheetiter=null;
			parser = null;
			sst = null;
			objReader = null;
			if(!blSheetFound)
			{
				m_binder.putLocal("sheetNotFound","true");	
				Report.trace("ARCheckin", "Metadata sheet Not found", null);
				return;	
			}
			NumberFormat  numberFormatter = NumberFormat.getCurrencyInstance();
			
			//reading excel cell values and assigning to ECM Metadata
			int ind = 0;
			String str99 = getCollectionValue("B1");
			ind = str99.indexOf('.');
			if(ind > 0)
			{
				str99 = str99.substring(0,ind);
			}
			m_binder.putLocal("xProjectNumber",str99);		
			str99 = "";
			
			
			String strLocationofPlant = getCollectionValue("B2");			
			
			//start changes for project #4848
			boolean isNew = DataBinderUtils.getBoolean(m_binder, "isNew", false);
			if(strSubType.equals("Appropriation Request Form") || strSubType.equals("Expedited AR Form") || strSubType.equals("Consulting, Contracts or Lease Form") || strSubType.equals("Project Closing/Asset Addition Form"))
			{
				
				if(strIsHomeOffice.equals("No"))
				{
					
					if(strLocationofPlant == null || strLocationofPlant.length() == 0)
					{
						m_binder.putLocal("NoLocationFound","true");	
						Report.trace("ARCheckin", "Location Not found", null);
						return;	
					}
					
                                        /*Guru: New Cell B30 has been Introduced to compute the Full LocationName
                                          after triming Location Code from Template. 
                                          B30 value will be assgined to strLocationofPlant if Subtype equals to "Project Closing/Asset Addition Form"  */
                                         
					if(strSubType.equals("Project Closing/Asset Addition Form") )
					{
						
							strLocationofPlant = (String)cellCollection.get("B30");
												
							if(strLocationofPlant == null || strLocationofPlant.length() == 0)
							{
								m_binder.putLocal("NoLocationFound","true");	
								Report.trace("ARCheckin", "Location Not found", null);
								return;	
							}
							else
							{
								m_binder.putLocal("xLocationName",strLocationofPlant);
								Report.trace("LocationFullname",strLocationofPlant,null);
							}
								
							
					}
					
					else
					{
						int iLoc = strLocationofPlant.indexOf("-") ;
						if(iLoc > -1)
						{
							strLocationofPlant = strLocationofPlant.substring(strLocationofPlant.indexOf("-") + 1);
						}
						strLocationofPlant = strLocationofPlant.trim();
						m_binder.putLocal("xLocationName",strLocationofPlant);
					
										
					}				
					if(strLocationofPlant.equals("Home Office"))
					{
						//throw error
						m_binder.putLocal("FoundHomeOffice","true");	
						Report.trace("ARCheckin", "Found HO in excel when user selected No.", null);
						return;
					}
					else if(isNew)
					{
						//get approvers from PlantApproversforAR table
						String sBPL = getCollectionValue("B14");
						m_binder.putLocal("xBusinessPlanLineDivisions",sBPL);
						//String query = "Select Location , DCTL, MTL, PATL from PlantApproversforAR where Location = '" + strLocationofPlant + "'";
						String query = "SELECT * FROM DOMESTIC_PLANT_APPROVERS_MV" +
										" WHERE lower(Location) in (" +
										" SELECT lower(LOCATIONHR) FROM ARLOCATIONALIASES" +
										" WHERE lower(LOCATIONAR) = lower('" + strLocationofPlant + "')" +
										" UNION ALL" + 
										" SELECT lower('" + strLocationofPlant + "') FROM dual" + 
										" WHERE NOT EXISTS (SELECT LOCATIONHR FROM ARLOCATIONALIASES where lower(LOCATIONAR) = lower(' + strLocationofPlant + ')))";
										
						
						Report.trace("ARCheckin", "Query: " + query, null);
						try
						{
							ResultSet rset = this.m_ws.createResultSetSQL(query);
							if (!rset.isEmpty())
							{       
								for (rset.first(); rset.isRowPresent(); rset.next())
								{
									String s1 = "";
									if(sBPL.equals("Distribution Centers"))
									{
										s1 = rset.getStringValue(2) + "";
									}
									else
									{
										s1 = rset.getStringValue(1) + "";
									}
									
									String s2 = rset.getStringValue(3) + "";
									String s3 = rset.getStringValue(4) + "";
									String s = "";
									Report.trace("ARCheckin", strLocationofPlant +" - " + s1 + "/" + s2 + "/" + s3, null);
									if(s1 != null && !s1.isEmpty())
									{
										s = s1;
									}
									if(s2 != null && !s2.isEmpty())
									{
										if(s != null && !s.isEmpty())
										{
											s = s + "," + s2;
										}
										else
										{
											s = s2;
										}
									}
									if(s3 != null && !s3.isEmpty())
									{
										if(s != null && !s.isEmpty())
										{
											s = s + "," + s3;
										}
										else
										{
											s = s3;
										}
									}								
									m_binder.putLocal("xLevel3Approvers",s);									
								}
							}
							else
							{								
								//plant is not added to the table
								m_binder.putLocal("PlantApproversNotFound","true");
								Report.trace("ARCheckin", "Data not found for " + strLocationofPlant, null);
								return;
							}
							rset = null;
						}
						catch(DataException de)
						{
							Report.error("ARCheckin", null, de);
						}
					}
				}
				else if(strIsHomeOffice.equals("Yes"))
				{
					Report.trace("ARCheckin", "user has selected the Location as Home Office", null);
					m_binder.putLocal("xLocationName","Home Office");
				}
			}
			else if(strSubType.equals("International - All Forms") )
			{
				
				if(strLocationofPlant == null || strLocationofPlant.length() == 0)
				{
					m_binder.putLocal("NoLocationFound","true");	
					Report.trace("ARCheckin", "Location Not found", null);
					return;	
				}
				
				strLocationofPlant = strLocationofPlant.trim();
				m_binder.putLocal("xLocationName",strLocationofPlant);
				
				
				String query = "Select * from ARInternationalApprovers where PlantLocation = '" + strLocationofPlant + "'";
				
				Report.trace("ARCheckin", "Query: " + query, null);
				try
				{
					ResultSet rset = this.m_ws.createResultSetSQL(query);
					if(rset.isEmpty())
					{
						//Location is not added to table
						m_binder.putLocal("IntlPlantNotFound","true");								
						Report.trace("ARCheckin", "Data not found for " + strLocationofPlant, null);
						return;
					}					
					else
					{
						for (rset.first(); rset.isRowPresent(); rset.next())
						{
							Report.trace("ARCheckin", "Data not found for " + rset.toString(), null);
							String s = "";
							String PlantLocation = rset.getStringValueByName("PlantLocation");
							String PlantController = rset.getStringValueByName("PlantController");
							String PlantManager = rset.getStringValueByName("PlantManager");
							String LocalMD = rset.getStringValueByName("LocalMD");
							String RegionalMD = rset.getStringValueByName("RegionalMD");
							String IntlOpsManager = rset.getStringValueByName("IntlOpsManager");
							String TLIntlEng = rset.getStringValueByName("TLIntlEng");
							String TLIntlFinance = rset.getStringValueByName("TLIntlFinance");
							
							if ((PlantController != null) && (PlantController.trim().length() > 0))
							{
								s = PlantController.trim();
							}
							if ((PlantManager != null) && (PlantManager.trim().length() > 0))
							{
								if(s.length() > 0)
								{
									s = s + "," + PlantManager.trim();
								}
								else
								{
									s = PlantManager.trim();
								}
							}
							if ((LocalMD != null) && (LocalMD.trim().length() > 0))
							{
								if(s.length() > 0)
								{
									s = s + "," + LocalMD.trim();
								}
								else
								{
									s = LocalMD.trim();
								}
							}
							m_binder.putLocal("xPartnerName",s);
							if ((RegionalMD != null) && (RegionalMD.trim().length() > 0))
							{
								m_binder.putLocal("xCompetitor",RegionalMD.trim());
							}
							if ((IntlOpsManager != null) && (IntlOpsManager.trim().length() > 0))
							{
								m_binder.putLocal("xBrandName",IntlOpsManager.trim());
							}
							if ((TLIntlEng != null) && (TLIntlEng.trim().length() > 0))
							{
								m_binder.putLocal("xEntityOfParty",TLIntlEng.trim());
							}
							if ((TLIntlFinance != null) && (TLIntlFinance.trim().length() > 0))
							{
								m_binder.putLocal("xPO",TLIntlFinance.trim());
							}
						}
					}
					
				}
				catch(DataException de)
				{					
					Report.error("ARCheckin", null, de);
				}
				
				if(isNew)
				{
					query = "Select * from ARFormsApproversDefaults";				
					Report.trace("ARCheckin", "Query: " + query, null);
					try
					{
						ResultSet rset = this.m_ws.createResultSetSQL(query);
						if (!rset.isEmpty())
						{
							for (rset.first(); rset.isRowPresent(); rset.next())
							{
								String arstep = rset.getStringValue(0);
								String arapprovers = rset.getStringValue(1);
								if(arapprovers != null && arapprovers.trim().length() > 0)
								{
									if(arstep.equals("FYI"))
									{
										m_binder.putLocal("xEventName",arapprovers);
									}
									else if(arstep.equals("EQA"))
									{
										m_binder.putLocal("xApprovedBy",arapprovers);
									}
									else if(arstep.equals("ENVREG"))
									{
										m_binder.putLocal("xAuthorName",arapprovers);
									}
									else if(arstep.equals("SFTYCOMP"))
									{
										m_binder.putLocal("xCustomer",arapprovers);
									}
									else if(arstep.equals("MISCINTL"))
									{
										m_binder.putLocal("xAuditingAgency",arapprovers);
									}
									else if(arstep.equals("DINTLFIN"))
									{
										m_binder.putLocal("xNonconformanceIDNo",arapprovers);
									}
									else if(arstep.equals("DINTLOPS"))
									{
										m_binder.putLocal("xVendorName",arapprovers);
									}
									else if(arstep.equals("VPINTL"))
									{
										m_binder.putLocal("xBuyerName",arapprovers);
									}
									else if(arstep.equals("FIN"))
									{
										m_binder.putLocal("xPolicy_Number",arapprovers);
									}
									else if(arstep.equals("DIRENG"))
									{
										m_binder.putLocal("xSupplierName",arapprovers);
									}
									else if(arstep.equals("FINAR"))
									{
										m_binder.putLocal("xCustomerItemNumber",arapprovers);
									}
									else if(arstep.equals("PSTAFF"))
									{
										m_binder.putLocal("xPLMCode",arapprovers);
									}
									else if(arstep.equals("BOD"))
									{
										m_binder.putLocal("xSTKNumber",arapprovers);
									}
								}
							}
						}
						rset = null;
					}
					catch(DataException de1)
					{
						Report.error("ARCheckin", null, de1);
					}
					
				}
				
			}
				
			else
			{
				if(strLocationofPlant == null || strLocationofPlant.length() == 0)
				{
					m_binder.putLocal("NoLocationFound","true");	
					Report.trace("ARCheckin", "Location Not found", null);
					return;	
				}
				int iLoc = strLocationofPlant.indexOf("-") ;
				if(iLoc > -1)
				{
					strLocationofPlant = strLocationofPlant.substring(strLocationofPlant.indexOf("-") + 1);
				}
				strLocationofPlant = strLocationofPlant.trim();
				m_binder.putLocal("xLocationName",strLocationofPlant);
			}
			//end changes #4848
			
			strLocationofPlant = null;
			m_binder.putLocal("xDepartmentText",getCollectionValue("B3"));							
			m_binder.putLocal("xProjectName",getCollectionValue("B11"));		
					
			if(!strSubType.equals("Asset Disposition Request Form"))
			{	
				
				m_binder.putLocal("xTotalFCast",getCollectionValueCurrency("B10"));
				
				if(!strSubType.equals("Project Closing/Asset Addition Form"))
				{					
					m_binder.putLocal("xProjectSummary",getCollectionValue("B15"));
				}
				if(strSubType.equals("Appropriation Request Form") || strSubType.equals("Appropriation Request Form (International)") || 
				strSubType.equals("Project Change Request Form") || strSubType.equals("Project Closing/Asset Addition Form") || 
				strSubType.equals("Expedited AR Form") || strSubType.equals("Consulting, Contracts or Lease Form")  || strSubType.equals("International - All Forms") )
				{
					if(!strSubType.equals("Project Closing/Asset Addition Form"))
					{						
						m_binder.putLocal("xCapExPlan",getCollectionValue("B6"));
					}
					if(strSubType.equals("Appropriation Request Form") || strSubType.equals("Appropriation Request Form (International)") || 
					strSubType.equals("Expedited AR Form") || strSubType.equals("Consulting, Contracts or Lease Form") || strSubType.equals("International - All Forms") )
					{
						m_binder.putLocal("xDiscretionary",getCollectionValue("B9"));
					}
					
					if(strSubType.equals("Project Change Request Form"))
					{
						m_binder.putLocal("xTypeOfChange",getCollectionValue("B23"));
					}
					
							
					//m_binder.putLocal("xROI",getCollectionValue("B10"));
					m_binder.putLocal("xCapitalFCast", getCollectionValueCurrency("B11"));
					m_binder.putLocal("xPStaffCat",getCollectionValue("B12"));
					m_binder.putLocal("xBusinessPlanCategories",getCollectionValue("B13"));
					m_binder.putLocal("xBusinessPlanLineDivisions",getCollectionValue("B14"));
					m_binder.putLocal("xARSavingsAmount",getCollectionValueCurrency("B16"));
					NumberFormat  numberFormatter1 = NumberFormat.getPercentInstance ();
					
					
					m_binder.putLocal("xARROI",getCollectionValuePercent("B17"));
					
					if(!strSubType.equals("Project Closing/Asset Addition Form"))
					{
						m_binder.putLocal("xNPVCashFlows",getCollectionValueCurrency("B18"));
						m_binder.putLocal("xNPVShareholderValue",getCollectionValueCurrency("B19"));
					}
					
					m_binder.putLocal("xCapExBasePlanAmount",getCollectionValueCurrency("B20"));
					m_binder.putLocal("xCapExEstimatedSavings",getCollectionValueCurrency("B21"));
					m_binder.putLocal("xCapExEstimatedROI",getCollectionValuePercent("B2"));
					
					if(strSubType.equals("Project Closing/Asset Addition Form"))
					{						
						m_binder.putLocal("xTotalActual",getCollectionValueCurrency("B24"));						
						m_binder.putLocal("xSpendDifferent",getCollectionValuePercent("B25"));
						m_binder.putLocal("xTotalSavingsForecast",getCollectionValueCurrency("B26"));
						m_binder.putLocal("xTotalActualSavings",getCollectionValueCurrency("B27"));
						m_binder.putLocal("xSavingsDifferent",getCollectionValuePercent("B28"));
					}
							
				}				
			}			
			
		}
		catch (IOException e) 
		{
			Report.error("ARCheckin",null,e);			
		}
		finally 
		{			
			if(pkg != null)
			{
				pkg.close();
				pkg = null;
			}
		}
	}
	
	
	
	public XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException 
	{
		//XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		XMLReader parser =XMLReaderFactory.createXMLReader();
		ContentHandler handler = new SheetHandler(sst);
		parser.setContentHandler(handler);
		return parser;
	}

	
	private class SheetHandler extends DefaultHandler 
	{
		private SharedStringsTable sst;
		private String lastContents;
		private boolean nextIsString;
		private String curRowLabel;
		private int cellIndex = 0;	
		private SheetHandler(SharedStringsTable sst) 
		{
			this.sst = sst;
		}
		
		public void startElement(String uri, String localName, String name,Attributes attributes) 
			throws SAXException 
		{			
			if(name.equals("row")) 
			{
				cellIndex = 0;
			}
			else if(name.equals("c")) 
			{
				cellIndex++;
				curRowLabel = attributes.getValue("r");				
				String cellType = attributes.getValue("t");
				if(cellType != null && cellType.equals("s")) 
				{
					nextIsString = true;
				}
				else 
				{
					nextIsString = false;
				}
			}			
			lastContents = "";
		}
		
		public void endElement(String uri, String localName, String name)
			throws SAXException 
		{			
			if(nextIsString) 
			{
				int idx = Integer.parseInt(lastContents);
				lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
				nextIsString = false;
			}

			if(name.equals("v")) 
			{
				if(cellIndex == 2)
				{
					if(lastContents == null)
					{
						lastContents  = "";
					}					
					cellCollection.put(curRowLabel, lastContents);
				}
			}			
			else if(name.equals("row")) 
			{
				curRowLabel = "";
			}
		}

		public void characters(char[] ch, int start, int length)
			throws SAXException 
		{
			lastContents += new String(ch, start, length);
		}
	}
	
}
