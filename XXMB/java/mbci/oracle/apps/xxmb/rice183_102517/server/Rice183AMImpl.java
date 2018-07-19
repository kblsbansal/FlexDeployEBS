package mbci.oracle.apps.xxmb.rice183.server;

import java.sql.CallableStatement;
import oracle.jbo.domain.Date;

import java.sql.Connection;
//import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.text.SimpleDateFormat;

import oracle.apps.fnd.framework.OAException;
import oracle.apps.fnd.framework.OARow;
import oracle.apps.fnd.framework.OAViewObject;
import oracle.apps.fnd.framework.server.OAApplicationModuleImpl;

import oracle.jbo.server.ViewLinkImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class Rice183AMImpl extends OAApplicationModuleImpl {
    /**This is the default constructor (do not remove)
     */
    public Rice183AMImpl() {
    }

    /**Sample main for debugging Business Components code using the tester.
     */
    public static void main(String[] args) {
        launchTester("mbci.oracle.apps.xxmb.rice183.server", /* package name */
      "Rice183AMLocal" /* Configuration Name */);
    }

    /**Container's getter for LovOrgCodeVO1
     */
    public LovOrgCodeVOImpl getLovOrgCodeVO1() {
        return (LovOrgCodeVOImpl)findViewObject("LovOrgCodeVO1");
    }


    /**Container's getter for LovSupplierVO1
     */
    public LovSupplierVOImpl getLovSupplierVO1() {
        return (LovSupplierVOImpl)findViewObject("LovSupplierVO1");
    }

    /**Container's getter for LovContainerVO1
     */
    public LovContainerVOImpl getLovContainerVO1() {
        return (LovContainerVOImpl)findViewObject("LovContainerVO1");
    }

    /**Container's getter for WkbQryVO1
     */
    public WkbQryVOImpl getWkbQryVO1() {
        return (WkbQryVOImpl)findViewObject("WkbQryVO1");
    }


    public String relPendingReqs(String queryID) {

        String msg = "Ok";
        
        OAViewObject vo = (OAViewObject)findViewObject("ReqHdrVO1");

        String sql = "Begin xxmb_rice183_pkg.confirm_release(pQueryID=> :1); end;";
        
        CallableStatement cs = 
                getOADBTransaction().createCallableStatement(sql, 1);
        try {
            cs.setString(1, queryID);
//            cs.setString(2, reqNumber);
            cs.execute();
            cs.close();
        }catch (Exception e) {
            e.printStackTrace();
            msg = e.getMessage();
        }

        vo.executeQuery();
        
        return msg;
        
    }
    
    public String delPendingReqs(String queryID, String reqNum) {

        String msg = "Ok";
        String reqNumber = "";
        
        
        if ("ALL".equals(reqNum)) {
            reqNumber = reqNum;
        }
        else {
            try {
            
            reqNumber = reqNum;
            
            } catch(Exception e) {
                e.printStackTrace();
                if (e.getMessage() != null) {
                    msg = e.getMessage();
                }
                else {
                    msg = "Unexpected Error Cancelling Requistion: " + reqNum;
                }
            }
        }
        
        if ("Ok".equals(msg)) {
        
            String sql = "Begin xxmb_rice183_pkg.cancel_release(pQueryID=> :1, pReqNum=> :2); end;";
            
            CallableStatement cs = 
                    getOADBTransaction().createCallableStatement(sql, 1);
            try {
                cs.setString(1, queryID);
                cs.setString(2, reqNumber);
                cs.execute();
                cs.close();
            }catch (Exception e) {
                e.printStackTrace();
                msg = e.getMessage();
            }
            
            OAViewObject vo = (OAViewObject)findViewObject("ReqHdrVO1");
            vo.executeQuery();
        }
        
        return msg;
    }

    public void loadPendingReqs(String queryID) {
    
        OAViewObject vo = (OAViewObject)findViewObject("ReqHdrVO1");
        vo.setNamedWhereClauseParam("pQueryID",queryID);
        vo.executeQuery();
    }

    public void ItemSelChg(String row) {

        OARow cRow = (OARow)findRowByRef(row);
        
        if (cRow != null) {
            String rowID = cRow.getAttribute("Rowid1").toString();
            String queryID = cRow.getAttribute("WkbqryId").toString();
            String itmSelected= cRow.getAttribute("Selected").toString();

    //   Update MTX table with new data
             String sql = "Begin xxmb_rice183_pkg.update_mtx_sel(pRowID=> :1, pSelected => :2); end;";
             
             CallableStatement cs = 
                     getOADBTransaction().createCallableStatement(sql, 1);
             try {
                 cs.setString(1, rowID);
                 cs.setString(2, itmSelected);
                 cs.execute();
                 cs.close();
             }catch (Exception e) {
                 e.printStackTrace();
             }
    //   Update Form Totals
             calcTotals(queryID);
        }
    
    }


    public String OneDayButton(String queryID) {
        OAViewObject vo = (OAViewObject)findViewObject("ParmValuesVO1");
        OAViewObject vo1 =(OAViewObject)findViewObject("WkbQryVO1");
        
        String msg = "";

        if (vo != null)  {  
            OARow row = (OARow)vo.first();  
            float adjFactor = Float.valueOf(row.getAttribute("OneDayDemand").toString());
            
        //   Update MTX table with new data
            String sql = "Begin xxmb_rice183_pkg.oneday_mtx_qty(pQueryID => :1, pAdjFactor => :2); end;";
                     
            CallableStatement cs = getOADBTransaction().createCallableStatement(sql, 1);
            try {
                cs.setString(1,  queryID);
                cs.setFloat(2,  adjFactor);
                cs.execute();
                cs.close();
                vo1.executeQuery();
            }catch (Exception e) {
                e.printStackTrace();
                msg = e.getMessage();
            }
        //   Update Form Totals
            calcTotals(queryID);
                
        }
        return msg;
    }

    public String AdjDataButton(String queryID, String baseAdd) {
        
        String retVal = "1";
        
        OAViewObject vo = (OAViewObject)findViewObject("ParmValuesVO1");
        OAViewObject vo1 =(OAViewObject)findViewObject("WkbQryVO1");

        if (vo != null)  {  
            OARow row = (OARow)vo.first();  
            float adjFactor = 0;
            if (baseAdd.equals("B")) {
                adjFactor = Float.valueOf(row.getAttribute("AdjFactor").toString());
            }
            else {
                adjFactor = Float.valueOf(row.getAttribute("AdjFactorAdd").toString());
            }
            
            
        //   Update MTX table with new data
            String sql = "Begin xxmb_rice183_pkg.adj_mtx_qty(pBaseAdd => :1, pQueryID => :2, pAdjFactor => :3); end;";
                     
            CallableStatement cs = getOADBTransaction().createCallableStatement(sql, 1);
            try {
                cs.setString(1,  baseAdd);
                cs.setString(2,  queryID);
                cs.setFloat(3,  adjFactor);
                cs.execute();
                cs.close();
                vo1.executeQuery();
            }catch (Exception e) {
                e.printStackTrace();
                retVal = "Error: " + retVal + e.getMessage();
            }
        //   Update Form Totals
            calcTotals(queryID);
                
        }
        
        return retVal;
    }

    public String ProcessButton(String queryID, String vendorID) {
        OAViewObject vo = (OAViewObject)findViewObject("ParmValuesVO1");
//        OAViewObject vo1 =(OAViewObject)findViewObject("WkbQryVO1");
        String msg = "XXX";
        
        
        if (vo != null)  {  
            OARow row = (OARow)vo.first();  
            row.setAttribute("AdjFactor", 1);
            
        //   Update MTX table with new data
            String sql = "Begin xxmb_rice183_pkg.process_release(pQueryID => :1, pDockDate => :2, pNeedByDate => :3, pVendorID => :4, pMessage => :5); end;";
                     
            CallableStatement cs = getOADBTransaction().createCallableStatement(sql, 1);
            try {
                cs.setString(1,  queryID);
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                String dDate = row.getAttribute("DockDate").toString();
                String nDate = row.getAttribute("OrderDate").toString();
//                msg = "Dates: " + sdf.parse(dDate) + ", " + nDate;
                cs.setString(2,  dDate);
                cs.setString(3,   nDate);
                cs.setString(4,   vendorID);
                cs.registerOutParameter(5, Types.VARCHAR);
                cs.execute();
                msg = cs.getString(5);
                cs.close();
//                vo1.executeQuery();
            }catch (Exception e) {
                e.printStackTrace();
                msg = msg + e.getMessage();
            }
        //   Update Form Totals
            calcTotals(queryID);
                
        }
        
        return msg;
    }



    public void ItemQtyChg(String row, String updMode) {

        OAViewObject vo1 =(OAViewObject)findViewObject("WkbQryVO1");
        OARow cRow = (OARow)findRowByRef(row);
        
        if (cRow != null) {
            String rowID = cRow.getAttribute("Rowid1").toString();
            String queryID = cRow.getAttribute("WkbqryId").toString();
            int itmSkidQty = Integer.valueOf(cRow.getAttribute("ItemSkidQty").toString());
            
            int itmQty = Integer.valueOf(cRow.getAttribute("SumQty").toString());
            int itmAddQty = Integer.valueOf(cRow.getAttribute("SumAddQty").toString());
        
            int itmSkids = (int) Math.ceil((float)(itmQty + itmAddQty) / (float)itmSkidQty);
            int itmTQty = itmSkids * itmSkidQty;
            
            cRow.setAttribute("SumSkids", itmSkids);
            cRow.setAttribute("TotalQty", itmTQty);

//   Update MTX table with new data
                 String sql = "Begin xxmb_rice183_pkg.update_mtx_qty(pBaseAdd => :1, pRowID=> :2, pItmQty => :3, pSkidQty => :4, pTQty => :5); end;";
             
             CallableStatement cs = 
                     getOADBTransaction().createCallableStatement(sql, 1);
             try {
                 cs.setString(1,  updMode);
                 cs.setString(2, rowID);
                 if (updMode.equals("B")) {
                   cs.setInt(3,  itmQty );
                 }
                 else {
                     cs.setInt(3,  itmAddQty );   
                 }
                 cs.setInt(4,  itmSkids );
                 cs.setInt(5,  itmTQty );
                 cs.execute();
                 cs.close();
                 vo1.executeQuery();
             }catch (Exception e) {
                 e.printStackTrace();
             }
//   Update Form Totals
             calcTotals(queryID);
        }
    
    }

    public void SelectAllChg(String queryID, String selectAll) {

        OAViewObject vo1 =(OAViewObject)findViewObject("WkbQryVO1");
        String sql = "Begin xxmb_rice183_pkg.process_select_all(pQueryID => :1, pSelectAll => :2); end;";
        
        CallableStatement cs = 
                getOADBTransaction().createCallableStatement(sql, 1);
        try {
            cs.setString(1, queryID);
            cs.setString(2, selectAll);
            cs.execute();
            cs.close();
            vo1.executeQuery();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setQueryID(String wkbqryid, String daysFrmToday)  {  

        Number val = 1; 
        int queryId = Integer.parseInt(wkbqryid);
        int daysFilter = Integer.parseInt(daysFrmToday);
        OAViewObject vo = (OAViewObject)findViewObject("ParmValuesVO1");

        
        if (vo != null)  {  
            if (vo.getFetchedRowCount() == 0)  {  
                vo.setMaxFetchSize(0);  
                vo.executeQuery();  
                vo.insertRow(vo.createRow());  
                OARow row = (OARow)vo.first();  
           
                row.setAttribute("RowKey", val); 
                row.setAttribute("wkbQryID", queryId);    
                row.setAttribute("MaxWeight", 0);
                row.setAttribute("MaxVol", 0);
                row.setAttribute("MaxArea", 0);
                row.setAttribute("MaxLinear", 0);
                row.setAttribute("MaxUnits", 0);
                row.setAttribute("CurWeight", 0);
                row.setAttribute("CurVol", 0);
                row.setAttribute("CurArea", 0);
                row.setAttribute("CurLinear", 0);
                row.setAttribute("CurUnits", 0);
                row.setAttribute("OpnWeight", 0);
                row.setAttribute("OpnVol", 0);
                row.setAttribute("OpnArea", 0);
                row.setAttribute("OpnLinear", 0);
                row.setAttribute("OpnUnits", 0);
                row.setAttribute("AdjFactor", 1);
                row.setAttribute("DaysFrmToday", daysFilter);
                row.setAttribute("CurContWeight", 0);
                row.setAttribute("CurContVolume", 0);
                row.setAttribute("CurContArea", 0);
                row.setAttribute("CurContLinear", 0);
                row.setAttribute("CurContQty", 0);
                row.setAttribute("OneDayDemand", 1);
                row.setAttribute("AdjFactorAdd", 1);
                row.setAttribute("AddDaysFrmToday", 0);  // Initialize Additional Days From Today to ZERO

                // Get Initial Date Parameters
                Connection conn = getOADBTransaction().getJdbcConnection();            
//                String Query =  "select min(need_by_date) order_date, min(new_dock_date) dock_date ";
//                Query = Query + "from xxmb_wkbqry_results where wkbqry_id = " + wkbqryid;  
                String Query =  "select trunc(sysdate) current_date";
                Query = Query + "from dual " ;  
                PreparedStatement stmt;
                try {
                    stmt = conn.prepareStatement(Query);
                    for(ResultSet resultset = stmt.executeQuery(); resultset.next();)  {
                        row.setAttribute("ReqDate", resultset.getDate("current_date") );
                        row.setAttribute("OrderDate", resultset.getDate("current_date") );
                        row.setAttribute("DockDate", resultset.getDate("current_date") );
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } 
        } 
    }

    public String loadGTTQuery() {

        String queryID = "0";
        String sql = "Begin xxmb_rice183_pkg.process_gtt_query(pQueryID => :1); end;";
        
        CallableStatement cs = 
                getOADBTransaction().createCallableStatement(sql, 1);
        try {
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.execute();
            queryID = cs.getString(1);
            cs.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return queryID;   
    }

    public String ReloadGTTQuery(String queryID, String daysFrmToday ) {

//        OAViewObject vo = (OAViewObject)findViewObject("LovPlanPgmVO1");
        
        OAViewObject vo = (OAViewObject)findViewObject("ParmValuesVO1");
        OARow row = (OARow)vo.first();  
        
        row.setAttribute("AddDaysFrmToday", 0);
        
        String sql = "Begin xxmb_rice183_pkg.process_query(pQueryID => :1, pDaysFrmToday => :2); end;";
        
        CallableStatement cs = 
                getOADBTransaction().createCallableStatement(sql, 1);
        try {
            cs.setInt(1,  Integer.parseInt(queryID) );
            cs.setInt(2,  Integer.parseInt(daysFrmToday) );
            cs.execute();
            cs.close();
            
//            vo.executeQuery();
            
        }catch (Exception e) {
            e.printStackTrace();
        }
        return queryID;   
    }

    public String reloadAddDays(String queryID, String daysFrmToday, String addDays ) {

    //        OAViewObject vo = (OAViewObject)findViewObject("LovPlanPgmVO1");

        String retVal = "1";
        String sql = "Begin xxmb_rice183_pkg.process_query_add(pQueryID => :1, pDaysFrmToday => :2, pAddDaysFrmToday=> :3); end;";
        
        CallableStatement cs = 
                getOADBTransaction().createCallableStatement(sql, 1);
        try {
            cs.setInt(1,  Integer.parseInt(queryID) );
            cs.setInt(2,  Integer.parseInt(daysFrmToday) );
            cs.setInt(3,  Integer.parseInt(addDays) );
            cs.execute();
            cs.close();
            
    //            vo.executeQuery();
            
        }catch (Exception e) {
            e.printStackTrace();
            retVal = e.getMessage();
        }
        return retVal;
    }

    public String calcDates(String queryID) {

        OAViewObject vo = (OAViewObject)findViewObject("ParmValuesVO1");
        OARow row = (OARow)vo.first();  
        
        String msg = "Ok";
        String step = "1.0";
        
        String sql = "Begin xxmb_rice183_pkg.calc_dates(pQueryID => :1, pOrderDate => :2, ";
        sql = sql +  "pDockDate => :3, pNeedbyDate=> :4); end;";

        CallableStatement cs = 
                getOADBTransaction().createCallableStatement(sql, 1);
        try {
            step = "2.0";
            cs.setInt(1,  Integer.parseInt(queryID) );
            cs.setString(2, row.getAttribute("ReqDate").toString());
            cs.registerOutParameter(3, Types.DATE);
            cs.registerOutParameter(4, Types.DATE);
            
            step = "3.0";
            cs.execute();
            
            step = "4.0";
            row.setAttribute("OrderDate", cs.getDate(4) );
            row.setAttribute("DockDate", cs.getDate(3) );
        
        }catch (Exception e) {
            e.printStackTrace();
            msg = step +  " - " + e.getMessage();
        }        
        
        return msg;
    }


    public void loadMTXQuery(String queryID, String orgID, String plannerCode, String supplierID, String incSecondary, String updMode) {
        
        OAViewObject vo = (OAViewObject)findViewObject("ParmValuesVO1");
        OARow row = (OARow)vo.first();  
        
        String sql = "Begin xxmb_rice183_pkg.process_mtx_query(pQueryID => :1, pOrgID => :2, ";
        sql = sql +  "pPlannerCode => :3, pSupplierID => :4, pIncSecondary => :5, ";
        sql = sql +  "pReqDate => :6, pOrderDate => :7, pDockDate => :8, pMode => :9); end;";
        
        CallableStatement cs = 
                getOADBTransaction().createCallableStatement(sql, 1);
        try {
            cs.setInt(1,  Integer.parseInt(queryID) );
//            cs.setInt(2,  Integer.parseInt(orgID) );
            cs.setString(2,  orgID );
            cs.setString(3, plannerCode);
//            cs.setInt(4,  Integer.parseInt(supplierID) );
            cs.setString(4,  supplierID);
            cs.setString(5, incSecondary);
            cs.setString(9, updMode);
            cs.registerOutParameter(6, Types.DATE);
            cs.registerOutParameter(7, Types.DATE);
            cs.registerOutParameter(8, Types.DATE);
            cs.execute();
            row.setAttribute("ReqDate", cs.getDate(6) );
            row.setAttribute("OrderDate", cs.getDate(7) );
            row.setAttribute("DockDate", cs.getDate(8) );
            
            row.setAttribute("AdjFactor", 1);
            row.setAttribute("AdjFactorAdd", 1);
            
            cs.close();
            calcTotals(queryID);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void calcTotals(String queryID) {

        String sql = "Begin xxmb_rice183_pkg.calc_mtx_totals(pQueryID => :1, pWeight => :2, pVol => :3, pArea => :4, pLinear => :5, pUnits => :6); end;";
        float cVal = 0;
        float mVal = 0;

        OAViewObject vo = (OAViewObject)findViewObject("ParmValuesVO1");
        OARow row = (OARow)vo.first();  

        OAViewObject vo1 = (OAViewObject)findViewObject("LovContainerVO1");
        OARow row1 = (OARow)vo1.getCurrentRow();  

        
        CallableStatement cs = 
                getOADBTransaction().createCallableStatement(sql, 1);
        try {
            cs.setInt(1,  Integer.parseInt(queryID) );
            cs.registerOutParameter(2, Types.FLOAT);
            cs.registerOutParameter(3, Types.FLOAT);
            cs.registerOutParameter(4, Types.FLOAT);
            cs.registerOutParameter(5, Types.FLOAT);
            cs.registerOutParameter(6, Types.FLOAT);
            cs.execute();

            cVal = cs.getFloat(2);
			float cf1 = (float)cVal * 100;
			int i = (int)cf1;
			cf1 = i;
			cf1 = cf1/100;
            row.setAttribute("CurWeight", cf1);
            mVal = Float.valueOf(row.getAttribute("MaxWeight").toString());
			cf1 = 0;
			cf1 = (mVal-cVal)*100;
			i = (int)cf1;
			cf1 = i;
			cf1 = cf1/100;
            row.setAttribute("OpnWeight",cf1);
            // Calc Total Container for Weight UOM
            if (row1.getAttribute("MaxWeight") != null) {
                float cWeight = Float.valueOf(row1.getAttribute("MaxWeight").toString());
				cf1 = 0;
				cf1 = (cVal/cWeight)*100;
				i = (int)cf1;
				cf1 = i;
				cf1 = cf1/100;
                row.setAttribute("CurContWeight", cf1);    
            }

            cVal = cs.getFloat(3);
			cf1 = 0;
			cf1 = (float)cVal * 100;
			i = (int)cf1;
			cf1 = i;
			cf1 = cf1/100;
            row.setAttribute("CurVol", cf1);
            mVal = Float.valueOf(row.getAttribute("MaxVol").toString());
			cf1 = 0;
			cf1 = (mVal-cVal)*100;
			i = (int)cf1;
			cf1 = i;
			cf1 = cf1/100;
            row.setAttribute("OpnVol", cf1);
            // Calc Total Container for Weight UOM
            if (row1.getAttribute("MaxVolume") != null) {
                float cVolume = Float.valueOf(row1.getAttribute("MaxVolume").toString());
				cf1 = 0;
				cf1 = (cVal/cVolume)*100;
				i = (int)cf1;
				cf1 = i;
				cf1 = cf1/100;
                row.setAttribute("CurContVolume", cf1);    
			}

            cVal = cs.getFloat(4);
			cf1 = 0;
			cf1 = (float)cVal * 100;
			i = (int)cf1;
			cf1 = i;
			cf1 = cf1/100;
			row.setAttribute("CurArea", cf1);
            mVal = Float.valueOf(row.getAttribute("MaxArea").toString());
			cf1 = 0;
			cf1 = (mVal-cVal)*100;
			i = (int)cf1;
			cf1 = i;
			cf1 = cf1/100;
            row.setAttribute("OpnArea", cf1);
            // Calc Total Container for Weight UOM
            if (row1.getAttribute("MaxArea") != null) {
                float cVolume = Float.valueOf(row1.getAttribute("MaxArea").toString());
				cf1 = 0;
				cf1 = (cVal/cVolume)*100;
				i = (int)cf1;
				cf1 = i;
				cf1 = cf1/100;
                row.setAttribute("CurContArea", cf1);    
            }

            cVal = cs.getFloat(5);
			cf1 = 0;
			cf1 = (float)cVal * 100;
			i = (int)cf1;
			cf1 = i;
			cf1 = cf1/100;
			row.setAttribute("CurLinear", cf1);
            mVal = Float.valueOf(row.getAttribute("MaxLinear").toString());
			cf1 = 0;
			cf1 = (mVal-cVal)*100;
			i = (int)cf1;
			cf1 = i;
			cf1 = cf1/100;
            row.setAttribute("OpnLinear", cf1);
            // Calc Total Container for Weight UOM
            if (row1.getAttribute("MaxLinear") != null) {
                float cLinear = Float.valueOf(row1.getAttribute("MaxLinear").toString());
				cf1 = 0;
				cf1 = (cVal/cLinear)*100;
				i = (int)cf1;
				cf1 = i;
				cf1 = cf1/100;
                row.setAttribute("CurContLinear", cf1);    
            }

            cVal = cs.getFloat(6);
			cf1 = 0;
			cf1 = (float)cVal * 100;
			i = (int)cf1;
			cf1 = i;
			cf1 = cf1/100;
			row.setAttribute("CurUnits", cf1);
            mVal = Float.valueOf(row.getAttribute("MaxUnits").toString());
			cf1 = 0;
			cf1 = (mVal-cVal)*100;
			i = (int)cf1;
			cf1 = i;
			cf1 = cf1/100;
            row.setAttribute("OpnUnits", cf1);
            // Calc Total Container for Weight UOM
            if (row1.getAttribute("MaxQty") != null) {
                float cQty = Float.valueOf(row1.getAttribute("MaxQty").toString());
				cf1 = 0;
				cf1 = (cVal/cQty)*100;
				i = (int)cf1;
				cf1 = i;
				cf1 = cf1/100;
                row.setAttribute("CurContQty", cf1);    
            }

            cs.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    /**Container's getter for ParmValuesVO1
     */
    public ParmValuesVOImpl getParmValuesVO1() {
        return (ParmValuesVOImpl)findViewObject("ParmValuesVO1");
    }
    
    public void InitColHDR()  {  
        Number val = 1; 
        OAViewObject vo = (OAViewObject)findViewObject("EnableColHdrVO1");

        if (vo != null)  {  
            if (vo.getFetchedRowCount() == 0)  {  
                vo.setMaxFetchSize(0);  
                vo.executeQuery();  
                vo.insertRow(vo.createRow());  
                OARow row = (OARow)vo.first();  
           
                row.setAttribute("rowkey", val); 
                row.setAttribute("EnableCol1", Boolean.FALSE);    
                row.setAttribute("EnableCol2", Boolean.FALSE);    
                row.setAttribute("EnableCol3", Boolean.FALSE);    
                row.setAttribute("EnableCol4", Boolean.FALSE);    
                row.setAttribute("EnableCol5", Boolean.FALSE);    
                row.setAttribute("EnableCol6", Boolean.FALSE);    
                row.setAttribute("EnableCol7", Boolean.FALSE);    
                row.setAttribute("EnableCol8", Boolean.FALSE);    
                row.setAttribute("EnableCol9", Boolean.FALSE);    
                row.setAttribute("EnableCol10", Boolean.FALSE);    
                row.setAttribute("EnableCol11", Boolean.FALSE);    
                row.setAttribute("EnableCol12", Boolean.FALSE);    
                row.setAttribute("EnableCol13", Boolean.FALSE);    
                row.setAttribute("EnableCol14", Boolean.FALSE);    
                row.setAttribute("EnableCol15", Boolean.FALSE);    
                row.setAttribute("EnableCol16", Boolean.FALSE);    
                row.setAttribute("EnableCol17", Boolean.FALSE);    
                row.setAttribute("EnableCol18", Boolean.FALSE);    
                row.setAttribute("EnableCol19", Boolean.FALSE);    
                row.setAttribute("EnableCol20", Boolean.FALSE);    
                row.setAttribute("EnableCol21", Boolean.FALSE);    
                row.setAttribute("EnableCol22", Boolean.FALSE);    
                row.setAttribute("EnableCol23", Boolean.FALSE);    
                row.setAttribute("EnableCol24", Boolean.FALSE);    
                row.setAttribute("EnableCol25", Boolean.FALSE);    
                row.setAttribute("EnableCol26", Boolean.FALSE);    
            } 
        } 
    }


    public void UpdateColHDR(String numColsTxt)  {  

        Number val = 1; 
        
        int numCols = Integer.valueOf(numColsTxt);
        
        OAViewObject vo = (OAViewObject)findViewObject("EnableColHdrVO1");
        OARow row = (OARow)vo.getCurrentRow();

        for (int i=1; i <= numCols; i++) {
            String colName = "EnableCol" + String.valueOf(i);
            row.setAttribute(colName, Boolean.TRUE);    
        }

        for (int i=numCols+1; i <= 26; i++) {
            String colName = "EnableCol" + String.valueOf(i);
            row.setAttribute(colName, Boolean.FALSE);    
        }

    }


    /**Container's getter for EnableColHdrVO1
     */
    public EnableColHdrVOImpl getEnableColHdrVO1() {
        return (EnableColHdrVOImpl)findViewObject("EnableColHdrVO1");
    }

    /**Container's getter for LovPlanPgmVO1
     */
    public LovPlanPgmVOImpl getLovPlanPgmVO1() {
        return (LovPlanPgmVOImpl)findViewObject("LovPlanPgmVO1");
    }

    /**Container's getter for ReqHdrVO1
     */
    public ReqHdrVOImpl getReqHdrVO1() {
        return (ReqHdrVOImpl)findViewObject("ReqHdrVO1");
    }

    /**Container's getter for ReqLinesVO1
     */
    public ReqLinesVOImpl getReqLinesVO1() {
        return (ReqLinesVOImpl)findViewObject("ReqLinesVO1");
    }

    /**Container's getter for ReqHdrLinesVL1
     */
    public ViewLinkImpl getReqHdrLinesVL1() {
        return (ViewLinkImpl)findViewLink("ReqHdrLinesVL1");
    }

    /**Container's getter for ReqLinesVO2
     */
    public ReqLinesVOImpl getReqLinesVO2() {
        return (ReqLinesVOImpl)findViewObject("ReqLinesVO2");
    }
}