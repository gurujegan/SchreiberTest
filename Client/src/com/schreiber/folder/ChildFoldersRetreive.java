package com.schreiber.folder;

import java.io.FilePermission;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeMap;

import oracle.stellent.ridc.IdcClientException;
import oracle.stellent.ridc.IdcClient;
import oracle.stellent.ridc.IdcClientManager;
import oracle.stellent.ridc.IdcContext;
import oracle.stellent.ridc.model.DataBinder;
import oracle.stellent.ridc.model.DataObject;
import oracle.stellent.ridc.model.DataResultSet;
import oracle.stellent.ridc.protocol.ServiceResponse;;

public class ChildFoldersRetreive {
	
	public Properties prop = new Properties();
	public String propFileName = "C:\\JDeveloper\\mywork\\SridharProject\\ECM\\src\\com\\ecm\\ridc\\config.properties";
	public FileReader reader;	
	private static IdcClientManager myIdcClientManager = new IdcClientManager();
	private static IdcClient myIdcClient;
	private static IdcContext myIdcContext;
	int count = 1;
	public static DataBinder dataBinder;
	public static DataBinder myResponseDataBinder1;
	private static TreeMap<Integer,String> m = new TreeMap();
	public static ServiceResponse myServiceResponse;
	ServiceResponse myServiceResponse1 = null;
	private static String s = "";
	 int i =0;
	
	
	public void FolderchildRetreive() throws IdcClientException {

		
		try {

			// Getting values from Properties File
			reader = new FileReader(propFileName);
			prop.load(reader);
			String idcURL = prop.getProperty("idchost");
			String user = prop.getProperty("username");
			String pass = prop.getProperty("password");
			String FolderName = prop.getProperty("foldername");
			System.out.println(idcURL + user + pass);

			// Connection establishment
			myIdcClientManager = new IdcClientManager();
			myIdcClient = myIdcClientManager.createClient(idcURL);
			myIdcContext = new IdcContext(user);
			dataBinder = myIdcClient.createBinder();
			myResponseDataBinder1 = null;
										
			
		
			
		}
			
			catch (Exception e) 
				{
					e.printStackTrace();
				}
		
		
	}
	
	public static DataResultSet getResultSetCustom(String fFolderGUID,String IdcService,String ResultsetName) throws IdcClientException
	{
		dataBinder.putLocal("fFolderGUID",fFolderGUID);
		dataBinder.putLocal("IdcService", IdcService);
		myServiceResponse = myIdcClient.sendRequest(myIdcContext,dataBinder);
		myResponseDataBinder1 = myServiceResponse.getResponseAsBinder();
                //System.out.println("data"+myResponseDataBinder1.getResultSet(ResultsetName));
		return myResponseDataBinder1.getResultSet(ResultsetName);
                
		
	}
	
	
	public static String getFolderName(DataResultSet ds)
	{
		String foldername = null;	
		for(DataObject myDataObject1 : ds.getRows())
		{
			foldername =  myDataObject1.get("fFolderName");
		}
		return foldername;
				
	}
	
	public void recursive(String s) 
	{
		try
		{
		if(s != null)
		{
		    String mapvalue,folderMeta;
                    
				DataResultSet Child = getResultSetCustom(s,"GETCHILD_FOLDERS","rsChildItems");
                               	int rowsize = Child.getRows().size();
                               
                                                           /* 
                                for(int i=0;i<=NChild.getFields().size();i++)
                                    System.out.println("coming");
                                    
				System.out.println("Value"+NChild.getFields().get(i).getName()); */
		    												
				 for (DataObject myDataObject1 : Child.getRows()) 
					    {
			           	    i++;
                                            				  	
                                                folderMeta = "$folderMeta:";
					 	String childName = myDataObject1.get("fFolderName");
					 	String parentGuid = myDataObject1.get("fParentGUID");
					 	String childGuid = myDataObject1.get("fFolderGUID");
                                                String fDocAccount = myDataObject1.get("fDocAccount");
                                                String fFolderDescription = myDataObject1.get("fFolderDescription");
                                                
                                                                                                
			           	    DataResultSet NChild = getResultSetCustom(childGuid,"GETCHILD_FOLDERS","rsFolderMeta");
			           	                                 
			           	    int newrowsize = NChild.getRows().size();
                                                                             
                                     
                                                if(newrowsize != 0)
                                                 {
                                                   for(int i=0;i<NChild.getRows().size();i++)
                                                   {
                                                       
                                                     String fFieldName = NChild.getRows().get(i).get("fFieldName");
                                                    String fFieldValue = NChild.getRows().get(i).get("fFieldValue");   
                                                                if(!(fFieldValue.isEmpty()))
                                                                   {
                                                                                    
                                                                         if(fFieldName.equals("xIdcProfile"))
                                                                         folderMeta+= "xIdcProfile="+fFieldValue;
                                                                          else if(fFieldName.equals("dDocType"))   
                                                                          folderMeta+= ","+"dDocType="+fFieldValue;   
                                                                         
                                                                                                                 
                                                                                                              }
                                                    }
                                                   
                                                  
                                                } 
                                                 else {
                                                    //System.out.println("Meta null");
                                                    folderMeta+= "xIdcProfile=null,dDocType=null";
                                                }
                                                 
                                                 
					 	
					 	// To retrieve Parent name
					  	DataResultSet Parent = getResultSetCustom(parentGuid,"FLD_INFO","FolderInfo");
						String parentName= getFolderName(Parent);
						
						mapvalue=childName+":"+myResponseDataBinder1.getLocal("folderPath");
                                                mapvalue+= folderMeta;
                                                mapvalue+= ",";
                                                mapvalue+="fDocAccount="+fDocAccount;
                                                mapvalue+=",";
                                                mapvalue+="fFolderDescription="+fFolderDescription;
                                               
                                                
						m.put(i,mapvalue);
			           	    folderMeta= "";
						
					    //System.out.println("Updated Dstatus:::"+'\n'+"parentName----"+parentName+ '\n'+"childName----"+childName +'\n'+"Account----"+myDataObject1.get("fDocAccount")+'\n'+"SecurityGroup----"+myDataObject1.get("fSecurityGroup")+'\n'+"folderPath----"+myResponseDataBinder1.getLocal("folderPath")+'\n'+"Owner----"+myDataObject1.get("fOwner")+'\n'+myDataObject1.get("xIdcProfile"));
					    
					    //Recursive Function Call
					    recursive(childGuid);
					    
                                               
			           	}
                                            
                                 
                                 
				
				
				
		}
		
		}
		catch( Exception e)
		{
			//System.out.println("exit"+ e);
		}
		
		
	}
	
	public static Date getTime(long timeinmillis)
	{
		
		Date date=new Date(timeinmillis);  
		return date;
	}
	
	public static void main(String[] args) {
		ChildFoldersRetreive s = new ChildFoldersRetreive();
		try {
			
			long start = System.currentTimeMillis();
			System.out.println("StartTime:"+getTime(start));  
			
			
			s.FolderchildRetreive();
			s.recursive("DD823E02CDC111392B18140CBEF237E4");
			System.out.println("Map Size-"+m.size());
			
			 for(Map.Entry map:m.entrySet())
			 {  
				 
					System.out.println(map.getValue());
			 }
                         m.clear();
                                           
				 
			 long end = System.currentTimeMillis();
			 System.out.println("EndTime:"+getTime(end)); 
			 long totaldiffer =  end - start ;
                         long diffSeconds = totaldiffer / 1000 % 60;
                         System.out.println("Difference:"+diffSeconds);  
                         
				
			
				  
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}