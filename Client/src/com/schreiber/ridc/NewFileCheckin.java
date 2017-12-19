package com.schreiber.ridc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import oracle.stellent.ridc.IdcClientException;
import oracle.stellent.ridc.IdcClient;
import oracle.stellent.ridc.IdcClientManager;
import oracle.stellent.ridc.IdcContext;
import oracle.stellent.ridc.model.DataBinder;
import oracle.stellent.ridc.model.DataObject;
import oracle.stellent.ridc.model.DataResultSet;
import oracle.stellent.ridc.model.TransferFile;
import oracle.stellent.ridc.protocol.ServiceResponse;
import oracle.stellent.ridc.model.impl.DataObjectEncodingUtils;
import java.util.Date;


public class NewFileCheckin {
		
	
	
	    public NewFileCheckin() {
        super();
    }
    
    public void CheckinCustom()
      throws IdcClientException
    {
        
        ServiceResponse myServiceResponse = null;
        try{
            
                      
              // Getting values from Properties File            
              Properties prop = new Properties();
              String propFileName = "C:\\JDeveloper\\mywork\\SchreiberTest\\Client\\classes\\com\\schreiber\\ridc\\config.properties";
              FileReader reader = new FileReader(propFileName);
              prop.load(reader);
                        
                String idcURL=prop.getProperty("idchost");            
                String user=prop.getProperty("username");
                String pass=prop.getProperty("password");
                String xProcessSystem=prop.getProperty("xProcessSystem");
				String dDocType=prop.getProperty("dDocType");
				String xSubType=prop.getProperty("xSubType");
				String dDocTitle=prop.getProperty("dDocTitle");
				String xDepartment=prop.getProperty("xDepartment");
				String xOperation=prop.getProperty("xOperation");
				String xProcess=prop.getProperty("xProcess");
				String xLevel1Approvers=prop.getProperty("xLevel1Approvers");
				String xLevel2Approvers=prop.getProperty("xLevel2Approvers");
				String xLevel3Approvers=prop.getProperty("xLevel3Approvers");
				String dInDate=prop.getProperty("dInDate");
				String xReviewDate=prop.getProperty("xReviewDate");
				String dDocAuthor=prop.getProperty("dDocAuthor");
				String filePath = prop.getProperty("filePath");  
                                String xCategoryID = prop.getProperty("xCategoryID"); 
                                String xAuthorName = prop.getProperty("xAuthorName"); 
                                String dRevLabel=prop.getProperty("dRevLabel");
                                
				
               Map<String,String> map=new HashMap<String,String>();
                System.out.println(idcURL+user+pass);
           
              
              // Connection establishment
              IdcClientManager myIdcClientManager = new IdcClientManager();
              IdcClient myIdcClient = myIdcClientManager.createClient(idcURL);
              IdcContext myIdcContext = new IdcContext(user,pass);
              int count =1;
                        
                        
             
              InputStream fileStream = null;
              
              DataBinder myRequestDataBinder;
                         
              		try {
              			//String replace = file.folderPath.replaceAll("\", );
              			System.out.println(filePath.split("\\|").length);
              			for(int i=0; i<filePath.split("\\|").length;i++) 
              				{
              						//String replace = file.folderPath.split("\\|")[i];;
              						String fullPath = filePath.split("\\|")[i];
              						String orgFile = fullPath;
              						int index = orgFile.lastIndexOf("\\");
              						//String fileName = orgFile.substring(index + 1);
              					//	String Ofile = fileName.split("\\.")[0];
              						fileStream = new FileInputStream(orgFile);
					                long fileLength = new File(orgFile).length();
					                myRequestDataBinder = myIdcClient.createBinder();
					                
					                myRequestDataBinder.putLocal("IdcService", "CHECKIN_UNIVERSAL");
					               
									myRequestDataBinder.putLocal("dSecurityGroup", "Internal");
									myRequestDataBinder.putLocal("xIdcProfile", "SQF");
									myRequestDataBinder.putLocal("xAddress", "2 Grande Rue, 55110 Cléry le Petit, France");
									myRequestDataBinder.putLocal("xLocationName","Clery-le-Petit");
									myRequestDataBinder.putLocal("xAppliesTo","Clery");
									myRequestDataBinder.putLocal("dDocAccount","ECM-PUBLIC-SQF-CLE");
									myRequestDataBinder.putLocal("xIsOrganic","No");
									myRequestDataBinder.putLocal("xComments","");
									myRequestDataBinder.putLocal("xProcessSystem",xProcessSystem.split("\\|")[i]);
									myRequestDataBinder.putLocal("dDocType",dDocType.split("\\|")[i]);
									myRequestDataBinder.putLocal("xSubType",xSubType.split("\\|")[i]);
									myRequestDataBinder.putLocal("dDocTitle",dDocTitle.split("\\|")[i]);
									myRequestDataBinder.putLocal("xDepartment",xDepartment.split("\\|")[i]);
									myRequestDataBinder.putLocal("xOperation",xOperation.split("\\|")[i]);
									myRequestDataBinder.putLocal("xProcess",xProcess.split("\\|")[i]);
									myRequestDataBinder.putLocal("xLevel1Approvers",xLevel1Approvers.split("\\|")[i]);
									myRequestDataBinder.putLocal("xLevel2Approvers",xLevel2Approvers.split("\\|")[i]);
									myRequestDataBinder.putLocal("xLevel3Approvers",xLevel3Approvers.split("\\|")[i]);
              				  		    String dCreateDate = DataObjectEncodingUtils.encodeDate(new Date(dInDate.split("\\|")[i]));
									//myRequestDataBinder.putLocal("dInDate",dInDate.split("\\|")[i]);
									myRequestDataBinder.putLocal("dInDate",dCreateDate);
                                                                        myRequestDataBinder.putLocal("xEffectiveDate",dCreateDate);
              				  		    String dReviewDate = DataObjectEncodingUtils.encodeDate(new Date(xReviewDate.split("\\|")[i]));
									//myRequestDataBinder.putLocal("xReviewDate",xReviewDate.split("\\|")[i]);
									myRequestDataBinder.putLocal("xReviewDate",dReviewDate);
									myRequestDataBinder.putLocal("dDocAuthor",dDocAuthor.split("\\|")[i]);
          				  		    myRequestDataBinder.putLocal("xCategoryID",xCategoryID.split("\\|")[i]);
              				  		    myRequestDataBinder.putLocal("xAuthorName",xAuthorName.split("\\|")[i]);
              				  		    myRequestDataBinder.putLocal("xCustomer",xAuthorName.split("\\|")[i]);
              				  		   // myRequestDataBinder.putLocal("dRevLabel",dRevLabel.split("\\|")[i]);
                                    
					               
					                myRequestDataBinder.addFile("primaryFile", new TransferFile(fileStream, orgFile, fileLength));
					                myServiceResponse = myIdcClient.sendRequest(myIdcContext, myRequestDataBinder);
							//		DataBinder myResponseDataBinder1 = myServiceResponse.getResponseAsBinder();
							//		DataResultSet myDataResultSet1 = myResponseDataBinder1.getResultSet("DOC_INFO");
                                
					                 
					                //InputStream myInputStream = myServiceResponse.getResponseStream();
					              //  String myResponseString = myServiceResponse.getResponseAsString();
					                
					                
					              
					                DataBinder myResponseDataBinder = myServiceResponse.getResponseAsBinder();
					              
					                System.out.println(+i+":Uploaded Docname="+myResponseDataBinder.getLocalData().get("dDocName"));
              				  		}
              			
					              } catch (IdcClientException idcce) {
					                System.out.println("IDC Client Exception occurred. Unable to upload file. Message: " + idcce.getMessage() + ", Stack trace: ");
					                idcce.printStackTrace();
					              } catch (IOException ioe) {
					                System.out.println("IO Exception occurred. Unable to upload file. Message: " + ioe.getMessage() + ", Stack trace: ");
					                ioe.printStackTrace();
					              } catch (Exception e) {
					                System.out.println("Exception occurred. Unable to upload file. Message: " + e.getMessage() + ", Stack trace: ");
					                e.printStackTrace();
					              } finally {
					                if (myServiceResponse != null) {
					                  myServiceResponse.close();
					                }
					                if (fileStream != null) {
					                  try {
					                    fileStream.close();
					                  }catch(Exception e) {
					                    e.printStackTrace();
					                  }    
					                }
					             }
              					
					             
					        }
					            catch(Exception e){
					            e.printStackTrace();
					        }
					    }
    
    public static void main(String[] args) {
        NewFileCheckin s=new NewFileCheckin();
        try{
           s.CheckinCustom();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
