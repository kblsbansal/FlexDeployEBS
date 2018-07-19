package mbci.oracle.apps.xxmb.rice183.webui;

import oracle.apps.fnd.framework.webui.OAControllerImpl;

import java.io.Serializable;

import oracle.apps.fnd.common.VersionInfo;
import oracle.apps.fnd.framework.OAApplicationModule;
import oracle.apps.fnd.framework.OAException;
import oracle.apps.fnd.framework.OARow;
import oracle.apps.fnd.framework.OAViewObject;
import oracle.apps.fnd.framework.webui.OAPageContext;
import oracle.apps.fnd.framework.webui.OAWebBeanConstants;
import oracle.apps.fnd.framework.webui.beans.OARawTextBean;
import oracle.apps.fnd.framework.webui.beans.OAWebBean;
import oracle.apps.fnd.framework.webui.beans.form.OAFormValueBean;
import oracle.apps.fnd.framework.webui.beans.message.OAMessageTextInputBean;

import oracle.apps.fnd.framework.webui.beans.OAWebBean;
import oracle.apps.fnd.framework.webui.beans.message.OAMessageStyledTextBean;
import oracle.apps.fnd.framework.webui.beans.table.OAAdvancedTableBean;
import oracle.apps.fnd.framework.webui.beans.table.OAColumnBean;
import oracle.apps.fnd.framework.webui.beans.table.OASortableHeaderBean;

import oracle.cabo.ui.validate.Formatter;
import java.text.DecimalFormat;

        import java.sql.Connection;  
        import java.sql.PreparedStatement;  
        import java.sql.ResultSet;

import oracle.apps.fnd.framework.webui.OADecimalValidater;
import oracle.apps.fnd.framework.webui.beans.form.OADefaultListBean;
import oracle.apps.fnd.framework.webui.beans.form.OADefaultShuttleBean;

import oracle.apps.fnd.framework.webui.beans.layout.OAPageLayoutBean;

/**
 * Controller for ...
 */
public class XXMBRice183CO extends OAControllerImpl {
    public static final String RCS_ID="$Header$";
    public static final boolean RCS_ID_RECORDED =
          VersionInfo.recordClassVersion(RCS_ID, "%packagename%");

    /**
     * Layout and page setup logic for a region.
     * @param pageContext the current OA page context
     * @param webBean the web bean corresponding to the region
     */
    public void processRequest(OAPageContext pageContext, OAWebBean webBean)
    {
      super.processRequest(pageContext, webBean);
           
        OAApplicationModule am = null;
        am = pageContext.getRootApplicationModule();

        OADefaultListBean list = (OADefaultListBean)webBean.findChildRecursive("PlanPgmAvail");
        list.setListCacheEnabled(true);

		int userId = pageContext.getUserId();

        Serializable[] param = {};
        String queryID = am.invokeMethod("loadGTTQuery", param).toString();
        String daysFrmToday = pageContext.getProfile("XXMB_183_DAYS_FILTER");
        if (daysFrmToday == null ) {
            daysFrmToday = "7";  // default days from today to 7 if PO not defined
        }
        Serializable[] param2 = {queryID, daysFrmToday};
        am.invokeMethod("setQueryID", param2);
        
//        /* Add scroll bars to table 
         addScrollBarsToTable(pageContext, webBean,"DivStart","DivEnd",true,"400",true,"400");
        
        Serializable[] param3 = {}; 
        am.invokeMethod("InitColHDR", param3);

		//   Setting the Window Title with the query name
        try
        {
            Connection conn = pageContext.getApplicationModule(webBean).getOADBTransaction().getJdbcConnection();
            String Query = "select query_name query_nam from msc_personal_queries where query_id = (select query_id FROM (SELECT query_id FROM xxmb_wkb_qry_gtt WHERE created_by = "+userId+" ORDER  BY creation_date DESC) WHERE rownum = 1)" ;
            PreparedStatement stmt = conn.prepareStatement(Query);
            for(ResultSet resultset = stmt.executeQuery(); resultset.next();)
            {
               String queryName = resultset.getString("query_nam");
               OAPageLayoutBean pageLayoutBean = new OAPageLayoutBean();
               ((OAPageLayoutBean)webBean).setTitle("Planned Order Contanerization - "+queryName);
            }
        }
            catch(Exception exception)
        {
            throw new OAException("error in setting the Title with Query: "+exception, OAException.ERROR);
        }
        //   End of the change
        
        formatNumCols(pageContext, webBean);
    }

    /**
     * Procedure to handle form submissions for form elements in
     * a region.
     * @param pageContext the current OA page context
     * @param webBean the web bean corresponding to the region
     */
    public void processFormRequest(OAPageContext pageContext, OAWebBean webBean)
    {
      super.processFormRequest(pageContext, webBean);

      OAApplicationModule am;
      OAViewObject vo;
      OAViewObject vo1;
      OARow row;
      
      am = pageContext.getApplicationModule(webBean);
      vo =(OAViewObject)am.findViewObject("WkbQryVO1");
      
      vo1 = (OAViewObject)am.findViewObject("ParmValuesVO1");       
      row = (OARow)vo1.getCurrentRow(); 

      OAFormValueBean cnfAction = (OAFormValueBean)webBean.findChildRecursive("confirmAction");
      String cnfActTxt = "";
      if (cnfAction.getValue(pageContext) != null ) {
        cnfActTxt = (String)cnfAction.getValue(pageContext);   
        if ( !("".equals(cnfActTxt) ) ) {
            throw new OAException(cnfActTxt, OAException.INFORMATION);
        }
      }
      
      int reload = 0;
      String backup = "1";
/*
      if ("LookupCodeChange".equals(pageContext.getParameter(EVENT_PARAM)) ) {
          String rowref = pageContext.getParameter(OAWebBeanConstants.EVENT_SOURCE_ROW_REFERENCE);
          Serializable[] parms = {rowref};
          am.invokeMethod("LookupCodeChg", parms);
      }

*/

    if ("DaysFrmTodayChg".equals(pageContext.getParameter(EVENT_PARAM))) {
        OAFormValueBean hQueryID = (OAFormValueBean)webBean.findChildRecursive("ParentQryID");
        String queryID = (String)hQueryID.getValue(pageContext);
        String daysFilter= pageContext.getParameter("DaysFilter");
        Serializable[] parms = {queryID, daysFilter};
        am.invokeMethod("ReloadGTTQuery", parms);         
        reload = 1;
    }

          
    if ("AddDaysChg".equals(pageContext.getParameter(EVENT_PARAM))) {

        String retVal = "0";
        OAFormValueBean hQueryID = (OAFormValueBean)webBean.findChildRecursive("ParentQryID");
        String queryID = (String)hQueryID.getValue(pageContext);
        String daysFilter= pageContext.getParameter("DaysFilter");
        String addDaysFilter= pageContext.getParameter("AddDaysFilter");
        Serializable[] parms = {queryID, daysFilter, addDaysFilter};
        retVal = am.invokeMethod("reloadAddDays", parms).toString();         
        if ("1".equals(retVal)) {
            reload = 1;
            backup = "2";
        }
        else {
            throw new OAException(retVal, OAException.INFORMATION);
        }
    }


     if ("SelectAllChg".equals(pageContext.getParameter(EVENT_PARAM))) {
        OAFormValueBean hQueryID = (OAFormValueBean)webBean.findChildRecursive("ParentQryID");
        String queryID = (String)hQueryID.getValue(pageContext);
        String selectAll= pageContext.getParameter("SelectAll");
        Serializable[] parms = {queryID, selectAll};
        am.invokeMethod("SelectAllChg", parms);         
    }
 
     if ("ItemQtyChg".equals(pageContext.getParameter(EVENT_PARAM))) {
         String rowref = pageContext.getParameter(OAWebBeanConstants.EVENT_SOURCE_ROW_REFERENCE);
         Serializable[] parms = {rowref, "B"};
         am.invokeMethod("ItemQtyChg", parms);         
      // Recalculate Totals after Qty Change
         setColTotals(pageContext, webBean);
     }

     if ("AddDaysQtyChg".equals(pageContext.getParameter(EVENT_PARAM))) {
         String rowref = pageContext.getParameter(OAWebBeanConstants.EVENT_SOURCE_ROW_REFERENCE);
         Serializable[] parms = {rowref, "A"};
         am.invokeMethod("ItemQtyChg", parms);         
        // Recalculate Totals after Qty Change
         setColTotals(pageContext, webBean);
     }

     if ("ItemSelChg".equals(pageContext.getParameter(EVENT_PARAM))) {
         String rowref = pageContext.getParameter(OAWebBeanConstants.EVENT_SOURCE_ROW_REFERENCE);
         Serializable[] parms = {rowref};
         am.invokeMethod("ItemSelChg", parms);         
     }

     if ("AdjDataButton".equals(pageContext.getParameter(EVENT_PARAM))) {
         OAFormValueBean hQueryID = (OAFormValueBean)webBean.findChildRecursive("ParentQryID");
         String queryID = (String)hQueryID.getValue(pageContext);
         Serializable[] parms = {queryID, "B"};
         String retVal = am.invokeMethod("AdjDataButton", parms).toString();                  
         
      // Recalculate Totals after Factor Adjustment
         setColTotals(pageContext, webBean);
         
         if (! retVal.equals("1") ) {
             throw new OAException(retVal, OAException.INFORMATION);
         }
     }

     if ("AdjAddDataClick".equals(pageContext.getParameter(EVENT_PARAM))) {
        OAFormValueBean hQueryID = (OAFormValueBean)webBean.findChildRecursive("ParentQryID");
        String queryID = (String)hQueryID.getValue(pageContext);
        Serializable[] parms = {queryID, "A"};
        String retVal = am.invokeMethod("AdjDataButton", parms).toString();                  
              
        // Recalculate Totals after Factor Adjustment
        setColTotals(pageContext, webBean);
              
        if (! retVal.equals("1") ) {
            throw new OAException(retVal, OAException.INFORMATION);
        }
    }


     if ("OneDayButton".equals(pageContext.getParameter(EVENT_PARAM))) {
        OAFormValueBean hQueryID = (OAFormValueBean)webBean.findChildRecursive("ParentQryID");
        String queryID = (String)hQueryID.getValue(pageContext);
        Serializable[] parms = {queryID};
        String msg = am.invokeMethod("OneDayButton", parms).toString();                  
        // Recalculate Totals after Factor Adjustment
        setColTotals(pageContext, webBean);
//         throw new OAException(msg, OAException.INFORMATION);
     }

     
          if ("ProcessButton".equals(pageContext.getParameter(EVENT_PARAM))) {

              OAFormValueBean hQueryID = (OAFormValueBean)webBean.findChildRecursive("ParentQryID");
              String queryID = (String)hQueryID.getValue(pageContext);

              OAFormValueBean hVendorID = (OAFormValueBean)webBean.findChildRecursive("ParentSupplierID");
              
              String vendorID = "";
              
              if (hVendorID.getValue(pageContext) != null ) {
                vendorID = (String)hVendorID.getValue(pageContext);
              }

              String msg;   
              Serializable[] parms = {queryID, vendorID};
              msg = am.invokeMethod("ProcessButton", parms).toString();
              vo.executeQuery();
          // Recalculate Totals after Qty Release
              setColTotals(pageContext, webBean);
//              reload = 1;
              throw new OAException(msg, OAException.INFORMATION);
              
          }


//     if ("LoadDataButton".equals(pageContext.getParameter(EVENT_PARAM)) || reload == 1) {
     if ("ApplyFilterButton".equals(pageContext.getParameter(EVENT_PARAM)) || reload == 1) {
         /* The below code line is used to initialize the application module */
         /* The below code line is used to initialize VO*/
         /* DataDisplayVO1 is the instance name in AM which is the original name of the VO */

              OAFormValueBean hQueryID = (OAFormValueBean)webBean.findChildRecursive("ParentQryID");
              String queryID = (String)hQueryID.getValue(pageContext);
              
              OAFormValueBean hOrgID = (OAFormValueBean)webBean.findChildRecursive("ParentOrgID");
              String orgID = (String)hOrgID.getValue(pageContext);
            
//              OAFormValueBean hPlnrCode = (OAFormValueBean)webBean.findChildRecursive("ParentPlnrCode");
//              String plnrCode = (String)hPlnrCode.getValue(pageContext);

              String planPgmLst = "";
              OADefaultShuttleBean shuttle = (OADefaultShuttleBean)webBean.findChildRecursive("PlanPgmRN");
              String[] selPgms = shuttle.getTrailingListOptionValues(pageContext, shuttle);
              if (selPgms != null) {
                for (int i = 0; i < selPgms.length ; i++) {
                    if (i == 0) {
                        planPgmLst = "'" + selPgms[i] + "'";
                    }
                    else {
                        planPgmLst = planPgmLst + ", '" + selPgms[i] + "'";
                    }
                }    
//                throw new OAException("Items in trailing list are: " + planPgmLst, OAException.INFORMATION);
              } 
//              else {
//                throw new OAException("Please shuttle at least one item from available list.", OAException.ERROR);
//              }
                
              OAFormValueBean hSupplierID = (OAFormValueBean)webBean.findChildRecursive("ParentSupplierID");
              String supplierID = (String)hSupplierID.getValue(pageContext);
              
              String incSecondary = pageContext.getParameter("InclSecondary");
              
              Serializable[] parms = {queryID, orgID, planPgmLst, supplierID, incSecondary, backup};
              am.invokeMethod("loadMTXQuery", parms);
              vo.executeQuery();
 
              setColHeaders(pageContext, webBean);
              setColTotals(pageContext, webBean);
   
//              OAAdvancedTableBean advtableBean = (OAAdvancedTableBean)webBean.findChildRecursive("PlanOrdersRN");
//              OAMessageStyledTextBean SkidQtyTot = (OAMessageStyledTextBean) advtableBean.findChildRecursive("SkidQty");
//              SkidQtyTot.setAttributeValue(TABULAR_FUNCTION_VALUE_ATTR,"10");
              
    }
          
 
     if (pageContext.isLovEvent()) {
        String lovID = pageContext.getLovInputSourceId();
        if ("ContainerLOV".equals(lovID)) {
            OAFormValueBean hQueryID = (OAFormValueBean)webBean.findChildRecursive("ParentQryID");
            String queryID = (String)hQueryID.getValue(pageContext);
            Serializable[] parms = {queryID};
            am.invokeMethod("calcTotals", parms);                  
             // Recalculate Totals after Factor Adjustment
             // setColTotals(pageContext, webBean);
             //         throw new OAException(msg, OAException.INFORMATION);
        }
      }


     if ("chgReqDate".equals(pageContext.getParameter(EVENT_PARAM))) {

         OAFormValueBean hQueryID = (OAFormValueBean)webBean.findChildRecursive("ParentQryID");
         String queryID = (String)hQueryID.getValue(pageContext);
         Serializable[] parms = { queryID };
         String msg = am.invokeMethod("calcDates", parms).toString();     
         
         if ( ! ("Ok".equals(msg)) ) {
             throw new OAException("Error calculating dates: " + msg , OAException.ERROR);              
         }
         
     }   
     
     if ("delRowClk".equals(pageContext.getParameter(EVENT_PARAM))) {

         String reqNum = pageContext.getParameter("tblReqNumber");

         OAFormValueBean hQueryID = (OAFormValueBean)webBean.findChildRecursive("ParentQryID");
         String queryID = (String)hQueryID.getValue(pageContext);

         Serializable[] parms = {queryID, reqNum};
         String msg = am.invokeMethod("delPendingReqs", parms).toString();

         //Clear MTX Grid 
         vo.executeQuery();
         // Recalculate Totals after Qty Release
         setColTotals(pageContext, webBean);

         if ( !("Ok".equals(msg)) ) {
             throw new OAException("Error canceling releases: " + msg + ", Req Number: " + reqNum, OAException.ERROR);      
         }
         
     }
    
     if ("clearAllBtnClk".equals(pageContext.getParameter(EVENT_PARAM))) {
         
         cnfAction.setText(pageContext, "clearReqs");
         
     }
        
//     if ("clearAllBtnClk".equals(pageContext.getParameter(EVENT_PARAM)) || (pageContext.getParameter("yesButton") != null) ) {
//     if ("clearReqs".equals(cnfActTxt) && (pageContext.getParameter("yesButton") != null) ) {
     if ( pageContext.getParameter("yesButton") != null ) {

         OAFormValueBean hQueryID = (OAFormValueBean)webBean.findChildRecursive("ParentQryID");
         String queryID = (String)hQueryID.getValue(pageContext);
         Serializable[] parms = { queryID, "ALL" };
         String msg = am.invokeMethod("delPendingReqs", parms).toString();

         cnfAction.setText(pageContext, "");

         //Clear MTX Grid 
         vo.executeQuery();
         // Recalculate Totals after Qty Release
         setColTotals(pageContext, webBean);

         if ( !("Ok".equals(msg)) ) {
             throw new OAException("Error canceling releases: " + msg, OAException.ERROR);      
         }
         
     }        

     if ("releaseBtnClk".equals(pageContext.getParameter(EVENT_PARAM))) {
              
        cnfAction.setText(pageContext, "releaseReqs");
              
     }


//     if ("releaseBtnClk".equals(pageContext.getParameter(EVENT_PARAM))) {
//     if ("releaseReqs".equals(cnfActTxt) && (pageContext.getParameter("yesButton") != null) ) {
     if ( pageContext.getParameter("relYes") != null ) {

         OAFormValueBean hQueryID = (OAFormValueBean)webBean.findChildRecursive("ParentQryID");
         String queryID = (String)hQueryID.getValue(pageContext);
         Serializable[] parms = { queryID };
         String msg = am.invokeMethod("relPendingReqs", parms).toString();

         cnfAction.setText(pageContext, "");


         //Clear MTX Grid 
         vo.executeQuery();
         // Recalculate Totals after Qty Release
         setColTotals(pageContext, webBean);

         if ( !("Ok".equals(msg)) ) {
             throw new OAException("Error releasing releases: " + msg, OAException.ERROR);      
         }
     
     }



//     if ("releaseBtnClk".equals(pageContext.getParameter(EVENT_PARAM))) {
//     }        


     if ("relTabClk".equals(pageContext.getParameter(EVENT_PARAM))) {

         OAFormValueBean hQueryID = (OAFormValueBean)webBean.findChildRecursive("ParentQryID");
         String queryID = (String)hQueryID.getValue(pageContext);
         Serializable[] parms = { queryID };
         am.invokeMethod("loadPendingReqs", parms);
         
     }
          
     if ("NumContainersChg".equals(pageContext.getParameter(EVENT_PARAM)) ) {
        
        String uomVal = null;
		int i = 0;
		float copen = 0;

        uomVal = calcMaxContainerValues(pageContext, "Weight");
        if (uomVal != null) {
//            OAMessageTextInputBean maxWeight = (OAMessageTextInputBean)webBean.findChildRecursive("maxWeight");
//            maxWeight.setValue(pageContext,uomVal);
            row.setAttribute("MaxWeight", Float.valueOf(uomVal)); 
            float maxV = Float.valueOf(uomVal);
            String curVt = row.getAttribute("CurWeight").toString();
            float curV = Float.valueOf(curVt);
			copen = (maxV - curV)*100;
			i = (int)copen;
			copen = i;
			copen = copen/100;
            row.setAttribute("OpnWeight", copen ); 
        }
        
         uomVal = calcMaxContainerValues(pageContext, "Volume");
         if (uomVal != null) {
//             OAMessageTextInputBean maxVolume = (OAMessageTextInputBean)webBean.findChildRecursive("maxVol");
//             maxVolume.setValue(pageContext,uomVal);
             row.setAttribute("MaxVol", Float.valueOf(uomVal)); 
             float maxV = Float.valueOf(uomVal);
             String curVt = row.getAttribute("CurVol").toString();
             float curV = Float.valueOf(curVt);
 			 copen = (maxV - curV)*100;
			 i = (int)copen;
			 copen = i;
			 copen = copen/100;
             row.setAttribute("OpnVol", copen); 
         }

         uomVal = calcMaxContainerValues(pageContext, "Area");
         if (uomVal != null) {
         //             OAMessageTextInputBean maxVolume = (OAMessageTextInputBean)webBean.findChildRecursive("maxVol");
         //             maxVolume.setValue(pageContext,uomVal);
             row.setAttribute("MaxArea", Float.valueOf(uomVal)); 
             float maxV = Float.valueOf(uomVal);
             String curVt = row.getAttribute("CurArea").toString();
             float curV = Float.valueOf(curVt);
 			 copen = (maxV - curV)*100;
			 i = (int)copen;
			 copen = i;
			 copen = copen/100;
             row.setAttribute("OpnArea", copen ); 
         }

//         System.out.println("MBCI Containerization Test123 - ");
//	     System.out.println("ContLinear:" + Float.valueOf(pageContext.getParameter("ContainerLinear")));
		 uomVal = calcMaxContainerValues(pageContext, "Linear");
         if (uomVal != null) {
//             OAMessageTextInputBean maxLinear = (OAMessageTextInputBean)webBean.findChildRecursive("maxLinear");
//             maxLinear.setValue(pageContext,uomVal);
             row.setAttribute("MaxLinear", Float.valueOf(uomVal)); 
             float maxV = Float.valueOf(uomVal);
             String curVt = row.getAttribute("CurLinear").toString();
             float curV = Float.valueOf(curVt);
 			 copen = (maxV - curV)*100;
			 i = (int)copen;
			 copen = i;
			 copen = copen/100;
             row.setAttribute("OpnLinear", copen ); 
         }
         uomVal = calcMaxContainerValues(pageContext, "Units");
         if (uomVal != null) {
//             OAMessageTextInputBean maxUnits = (OAMessageTextInputBean)webBean.findChildRecursive("maxUnits");
//             maxUnits.setValue(pageContext,uomVal);
             row.setAttribute("MaxUnits", Float.valueOf(uomVal)); 
             float maxV = Float.valueOf(uomVal);
             String curVt = row.getAttribute("CurUnits").toString();
             float curV = Float.valueOf(curVt);
 			 copen = (maxV - curV)*100;
			 i = (int)copen;
			 copen = i;
			 copen = copen/100;
             row.setAttribute("OpnUnits", copen ); 
         }
       
     }
 
      if (pageContext.getParameter("Go") != null) {
        /* The below code line is used to initialize the application module */
        /* The below code line is used to initialize VO*/
        /* DataDisplayVO1 is the instance name in AM which is the original name of the VO */
             OAFormValueBean hQueryID = (OAFormValueBean)webBean.findChildRecursive("ParentQryID");
             String queryID = (String)hQueryID.getValue(pageContext);
             
               OAFormValueBean hOrgID = (OAFormValueBean)webBean.findChildRecursive("ParentOrgID");
               String orgID = (String)hOrgID.getValue(pageContext);
             
               OAFormValueBean hPlnrCode = (OAFormValueBean)webBean.findChildRecursive("ParentPlnrCode");
               String plnrCode = (String)hPlnrCode.getValue(pageContext);
             
               OAFormValueBean hSupplierID = (OAFormValueBean)webBean.findChildRecursive("ParentSupplierID");
               String supplierID = (String)hSupplierID.getValue(pageContext);
             
               String incSecondary = pageContext.getParameter("InclSecondary");
             
             Serializable[] parms = {queryID, orgID, plnrCode, supplierID, incSecondary};
             am.invokeMethod("loadMTXQuery", parms);
             vo.executeQuery();
           } 
      }


    public void formatNumCols(OAPageContext pageContext, OAWebBean webBean) {

        Formatter formatter = new OADecimalValidater("#,###,##0;#,###,##0","#,##0;#,##0");

        OAMessageStyledTextBean numColx = (OAMessageStyledTextBean)webBean.findIndexedChildRecursive("SingleDay");
        numColx.setAttributeValue(ON_SUBMIT_VALIDATER_ATTR, formatter);

        numColx = (OAMessageStyledTextBean)webBean.findIndexedChildRecursive("SkidQty");
        numColx.setAttributeValue(ON_SUBMIT_VALIDATER_ATTR, formatter);

        numColx = (OAMessageStyledTextBean)webBean.findIndexedChildRecursive("TotalQty");
        numColx.setAttributeValue(ON_SUBMIT_VALIDATER_ATTR, formatter);
    
        for (int i = 1; i <= 26; i++) {
            String colName = "QtyBuck" + String.valueOf(i);
            OAMessageStyledTextBean numCol = (OAMessageStyledTextBean)webBean.findIndexedChildRecursive(colName);
            numCol.setAttributeValue(ON_SUBMIT_VALIDATER_ATTR, formatter);
//            OAColumnBean columnBean = (OAColumnBean)webBean.findIndexedChildRecursive(colName + "Col");
//            columnBean.setAttributeValue(ON_SUBMIT_VALIDATER_ATTR, formatter);
        }
    }


    public void setColTotals(OAPageContext pageContext, OAWebBean webBean) {
        
        String step = "0";
        try 
        //              OAAdvancedTableBean advtableBean = (OAAdvancedTableBean)webBean.findChildRecursive("PlanOrdersRN");
        //              OAMessageStyledTextBean SkidQtyTot = (OAMessageStyledTextBean) advtableBean.findChildRecursive("SkidQty");
        //              SkidQtyTot.setAttributeValue(TABULAR_FUNCTION_VALUE_ATTR,"10");
        {  
            step = "1";
            int numCols = 0;
            OAAdvancedTableBean advtableBean = (OAAdvancedTableBean)webBean.findIndexedChildRecursive("PlanOrdersRN");

            Connection conn = pageContext.getApplicationModule(webBean).getOADBTransaction().getJdbcConnection();            
            
            String Query = "select to_char(nvl(sum(sum_qty), 0), '999,999,990') sum_qty, ";
            Query = Query + "to_char(nvl(sum(sum_skids), 0), '999,999,990') sum_skids, ";
            Query = Query + "to_char(nvl(sum(total_qty), 0), '999,999,990') total_qty, ";
            for (int i = 1; i <= 25; i++) {
                Query = Query + "to_char(nvl(sum(qty_bkt" + String.valueOf(i) + "), 0), '999,999,990') sum_bkt" + String.valueOf(i) + ", ";
            }
            Query = Query + "nvl(sum(qty_bkt26), 0) sum_bkt26 ";
            Query = Query + "from xxmb_wkbqry_mtx";  
            
            
            PreparedStatement stmt = conn.prepareStatement(Query);  
            step = "2";    
            for(ResultSet resultset = stmt.executeQuery(); resultset.next();)  
            {  
                step = "3";
//                String colValue = String.valueOf(resultset.getInt("sum_qty"));
                String colValue = resultset.getString("sum_qty");
                OAMessageTextInputBean ItemQtyTot = (OAMessageTextInputBean) advtableBean.findChildRecursive("ItemQty");
                ItemQtyTot.setAttributeValue(TABULAR_FUNCTION_VALUE_ATTR, colValue);

//                colValue = String.valueOf(resultset.getInt("sum_skids"));
                colValue = resultset.getString("sum_skids");
                OAMessageStyledTextBean SkidQtyTot = (OAMessageStyledTextBean) advtableBean.findChildRecursive("SkidQty");
                SkidQtyTot.setAttributeValue(TABULAR_FUNCTION_VALUE_ATTR, colValue);

//                colValue = String.valueOf(resultset.getInt("total_qty"));
                colValue = resultset.getString("total_qty");
                OAMessageStyledTextBean TotalQtyTot = (OAMessageStyledTextBean) advtableBean.findChildRecursive("TotalQty");
                TotalQtyTot.setAttributeValue(TABULAR_FUNCTION_VALUE_ATTR, colValue);

                for (int i=1; i <= 26; i++) {
//                    colValue = String.valueOf(resultset.getInt("sum_bkt" + String.valueOf(i)));
                    colValue = resultset.getString("sum_bkt" + String.valueOf(i));
                    OAMessageStyledTextBean ColQtyTot = (OAMessageStyledTextBean) advtableBean.findChildRecursive("QtyBuck" + String.valueOf(i));
                    ColQtyTot.setAttributeValue(TABULAR_FUNCTION_VALUE_ATTR, colValue);                    
                }
                                
            }  
          
 
        }  
            catch(Exception exception)    
        {   
            throw new OAException(step + ": error in Processig Col Headers SQL: "+exception, OAException.ERROR);  
        }  
        
          
    }

    public void setColHeaders(OAPageContext pageContext, OAWebBean webBean) {
        
        String step = "0";
        try 
          
        {  
            step = "1";
            int numCols = 0;
            OAAdvancedTableBean advtableBean = (OAAdvancedTableBean)webBean.findIndexedChildRecursive("PlanOrdersRN");

            Connection conn = pageContext.getApplicationModule(webBean).getOADBTransaction().getJdbcConnection();            
            String Query = "select * from xxmb_wkbqry_colhdrs order by col_number";  
            PreparedStatement stmt = conn.prepareStatement(Query);  
            step = "2";    
            for(ResultSet resultset = stmt.executeQuery(); resultset.next();)  
            {  
                step = "3";
                String colName = resultset.getString("col_heading");                  
                String colNum = String.valueOf(resultset.getInt("col_number"));
                String colID = "QtyBuck" + colNum + "Col";
                step = "4";
                OAColumnBean columnBean = (OAColumnBean)advtableBean.findIndexedChildRecursive(colID);
                OASortableHeaderBean colHeaderBean = (OASortableHeaderBean)columnBean.getColumnHeader();
                colHeaderBean.setText(colName);
                numCols++;
            }  

            OAApplicationModule am = null;
            am = pageContext.getRootApplicationModule();
            Serializable[] params = {String.valueOf(numCols)}; 
            am.invokeMethod("UpdateColHDR", params);
            
    
        }  
            catch(Exception exception)    
        {   
            throw new OAException(step + ": error in Processig Col Headers SQL: "+exception, OAException.ERROR);  
        }  
        
          
    }


      
    public String calcMaxContainerValues(OAPageContext pageContext, String valOf) {
        float NumContainers = 0;
        float ContWeight = 0; float ContVolume = 0; float ContArea = 0; float ContLinear = 0; float ContUnits = 0;
        float MaxContWeight = 0; float MaxContVolume = 0; float MaxContArea = 0; float MaxContLinear = 0; float MaxContUnits = 0;
                
        String retValue = null;
        try {
        if (pageContext.getParameter("NumContainers") != null)  {
            NumContainers = Float.valueOf(pageContext.getParameter("NumContainers"));
            
            if (pageContext.getParameter("ContainerWeight") != null && valOf == "Weight") {
                ContWeight = Float.valueOf(pageContext.getParameter("ContainerWeight"));
                MaxContWeight = (NumContainers * ContWeight)*100;
				int i = (int)MaxContWeight;
				MaxContWeight = i;
                MaxContWeight = MaxContWeight / 100;
                retValue = Float.toString(MaxContWeight);
            }
            if (pageContext.getParameter("ContainerVolume") != null && valOf == "Volume") {
                ContVolume = Float.valueOf(pageContext.getParameter("ContainerVolume"));
                MaxContVolume = (NumContainers * ContVolume)*100;
				int i = (int)MaxContVolume;
				MaxContVolume = i;
                MaxContVolume = MaxContVolume / 100;
                retValue = Float.toString(MaxContVolume);
            }

            if (pageContext.getParameter("ContainerArea") != null && valOf == "Area") {
                ContArea = Float.valueOf(pageContext.getParameter("ContainerArea"));
                MaxContArea = (NumContainers * ContArea)*100;
				int i = (int)MaxContArea;
				MaxContArea = i;
                MaxContArea = MaxContArea / 100;
                retValue = Float.toString(MaxContArea);
            }


            if (pageContext.getParameter("ContainerLinear") != null && valOf == "Linear") {
				
                ContLinear = Float.valueOf(pageContext.getParameter("ContainerLinear"));
                MaxContLinear = (NumContainers * ContLinear)*100;
				int i = (int)MaxContLinear;
				MaxContLinear = i;
                MaxContLinear = MaxContLinear / 100;
                retValue = Float.toString(MaxContLinear);
            }
            if (pageContext.getParameter("ContainerQty") != null && valOf == "Units") {
                ContUnits = Float.valueOf(pageContext.getParameter("ContainerQty"));
                MaxContUnits = (NumContainers * ContUnits)*100;
				int i = (int)MaxContUnits;
				MaxContUnits = i;
                MaxContUnits = MaxContUnits / 100;
                retValue = Float.toString(MaxContUnits);
            }
        }
        }catch(Exception e) {
            e.printStackTrace();
            retValue = null;
        }
        return retValue;
    }
      
      
    public void addScrollBarsToTable(OAPageContext pageContext, 
                                     OAWebBean webBean, 
                                     String preRawTextBean, 
                                     String postRawTextBean, 
                                     boolean horizontal_scroll, String width, 
                                     boolean vertical_scroll, String height)
    {
        String l_height = "400";
//        String l_width = "400";
         String l_width = "1360";
        pageContext.putMetaTag("toHeight", "<style type=\"text/css\">.toHeight {height:24px; color:black;}</style>");

        OARawTextBean startDIVTagRawBean = (OARawTextBean) webBean.findChildRecursive(preRawTextBean);
        if (startDIVTagRawBean == null)
        {
            throw new OAException("Not able to retrieve raw text bean just above the table bean. Please verify the id of pre raw text bean.");
        }

        OARawTextBean endDIVTagRawBean = (OARawTextBean) webBean.findChildRecursive(postRawTextBean);
        if (endDIVTagRawBean == null)
        {
            throw new OAException("Not able to retrieve raw text bean just below the table bean. Please verify the id of post raw text bean.");
        }

        if (!((height == null) || ("".equals(height))))
        {
            try
            {
                Integer.parseInt(height);
                l_height = height;
            }
            catch (Exception e)
            {
                throw new OAException("Height should be an integer value.");
            }
        }


        if (!((width == null) || ("".equals(width))))
        {
            try
            {
                Integer.parseInt(width);
//                l_width = width;
            }
            catch (Exception e)
            {
                throw new OAException("Width should be an integer value.");
            }
        }

        String divtext = "";
        if ((horizontal_scroll) && (vertical_scroll))
        {
            divtext = "<DIV style='width:" + l_width + ";height:" + l_height + ";overflow:auto;padding-bottom:20px;border:0'>";
        }
        else if (horizontal_scroll)
        {
            divtext = "<DIV style='width:" + l_width + ";overflow-x:auto;padding-bottom:20px;border:0'>";
        }
        else if (vertical_scroll)
        {
            divtext = "<DIV style='height:" + l_height + ";overflow-y:auto;padding-bottom:20px;border:0'>";
        }
        else
        {
            throw new OAException("Both vertical and horizintal scrollbars are passed as false,hence, no scrollbars will be rendered.");
        }
        startDIVTagRawBean.setText(divtext);
        endDIVTagRawBean.setText("</DIV>");
    }      
}
