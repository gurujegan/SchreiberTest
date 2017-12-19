 package SchreiberFilters;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.common.StringUtils;
import intradoc.common.Report;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.Workspace;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;
import intradoc.data.FieldInfo;
import intradoc.server.Service;
import intradoc.server.DocumentAccessSecurity;
import intradoc.shared.SecurityUtils;
import intradoc.shared.UserData;
import intradoc.shared.FilterImplementor;
import intradoc.shared.SharedObjects;
import java.io.PrintStream;
import java.util.Vector;

public class EntitySecurityOnAccounts implements FilterImplementor
{
	
	public int doFilter(Workspace ws, DataBinder m_binder, ExecutionContext cxt)
		throws DataException, ServiceException
	{
		if (cxt == null)
		{
			return CONTINUE;
		}
		
		Object paramObj = cxt.getCachedObject("filterParameter");
		if ((paramObj == null) || (!(paramObj instanceof String)))
		{
		  return 0;
		}
		Service service = null;
		if ((cxt instanceof Service))
		{
			service = (Service)cxt;
		}
		else
		{
			return 0;
		}
		
		String param = (String)paramObj;
		if (!param.equals("isEntitySecurityOnAccounts"))
		{
		  return 0;
		}
		if(!SecurityUtils.m_useEntitySecurity)
		{
			//Report.trace("EntitySecurity", "useEntitySecurity is false", null);
			return 0;
		}
		UserData originalUserData = service.getUserData();
	    //Report.trace("EntitySecurity", "Test", null);
		if (SecurityUtils.isUserOfRole(originalUserData, "admin"))
		{
			//Report.trace("EntitySecurity", "User is Admin", null);
			return 0;
		}
		DocumentAccessSecurity dac = (DocumentAccessSecurity)service.getCachedObject("DocumentAccessSecurity");
		//Report.trace("EntitySecurity", "User is not Admin", null);
		if ((dac.m_userPriv & 0x8) != 0)
		{			
			//Report.trace("EntitySecurity", "User is Admin to security group", null);
			String account = "";
			if (dac.m_rset == null)
			{
				account = m_binder.getLocal("dDocAccount");
				//Report.trace("EntitySecurity", "m_rset: " + 1, null);
			}
			else
			{
				//Report.trace("EntitySecurity", dac.m_rset.toString(), null);
				FieldInfo fi = new FieldInfo();
				int fieldIndex = 0;
				try
				{
					fieldIndex = ResultSetUtils.getIndexMustExist(dac.m_rset, "dDocAccount");
				    //Report.trace("EntitySecurity", "fieldIndex:: "+fieldIndex, null);
				    //Report.trace("EntitySecurity", "Get FieldIndex  Value:: "+dac.m_rset.getStringValue(fieldIndex), null);
					account = dac.m_rset.getStringValue(fieldIndex);
				    //Report.trace("EntitySecurity", "account test:: "+account, null);
				}
				catch(DataException de)
				{
					Report.trace("EntitySecurity", " no account found"+de, null);
                    }catch(Exception e){
                        Report.trace("EntitySecurity", " no account found"+e, null);
                    }
				//Report.trace("EntitySecurity", "m_rset: " + 2, null);
			}			
			if(account != null)
			{
				//Report.trace("EntitySecurity", "m_rset: " + account, null);
				if (account.length() == 0)
				{
					dac.m_userPriv = 4;
					dac.m_service.setReturnValue(new Boolean(true));
					//Report.trace("EntitySecurity", "return : " + 3, null);
				}
				else
				{
					if(SecurityUtils.isAccountAccessible(originalUserData, account, 8))
					{					
						dac.m_service.setReturnValue(new Boolean(false));
						//Report.trace("EntitySecurity", "return : " + 2, null);
					}
					else
					{
						dac.m_service.setReturnValue(new Boolean(true));
						//Report.trace("EntitySecurity", "return : " + 1, null);
					}
				}
			}
		}
		else
		{
			//Report.trace("EntitySecurity", "User is not Admin to security group", null);
		}
		return 0;
	}
}
