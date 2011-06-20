package com.yourhairsalon.booking.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.servlet.view.document.AbstractExcelView;

import com.yourhairsalon.booking.domain.*;
import com.yourhairsalon.booking.form.ClientPlusNote;

public class ReportListExcelView extends AbstractExcelView{
	
	private static final Log log = LogFactory.getLog(ReportListExcelView.class);
	
	@Override
	protected void buildExcelDocument(Map model, HSSFWorkbook workbook,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		log.debug("Entered buildExcelDocument");
		
		
		// TODO: Based on what the model.get returns determines the report to display
		// which means The createSheet and setExcelHeader is dependent on it
		
		
		
		
		ArrayList reportList = (ArrayList) model.get("reportList");
		log.debug("reportList: " + reportList);
		
		ArrayList clientReportList = (ArrayList) model.get("clientReportList");
		
		
		List<Payments> reportTodaysPayementsList = (List<Payments>) model.get("reportTodaysPaymentsList");
		log.debug("reportTodaysPayementsList: " + reportTodaysPayementsList);
		
		if(clientReportList != null){
			log.debug("**** exporting the client list and it has something in it !!!!!");
			HSSFSheet excelSheet = workbook.createSheet("Client List");
			HSSFRow excelHeader = excelSheet.createRow(0);
			excelHeader.createCell(0).setCellValue("First name");
			excelHeader.createCell(1).setCellValue("Last name");
			excelHeader.createCell(2).setCellValue("Email");
			excelHeader.createCell(3).setCellValue("Cell phone");
			excelHeader.createCell(4).setCellValue("Work phone");
			excelHeader.createCell(5).setCellValue("Home phone");
			excelHeader.createCell(6).setCellValue("Address 1");
			excelHeader.createCell(7).setCellValue("Address 2");
			excelHeader.createCell(8).setCellValue("City");
			excelHeader.createCell(9).setCellValue("State");
			excelHeader.createCell(10).setCellValue("Zip");
			excelHeader.createCell(11).setCellValue("Notes");
			excelHeader.createCell(12).setCellValue("DOB");
			
			int record = 1;
			for (int x=0;x<clientReportList.size();x++ ) {
				ClientPlusNote clientnote = (ClientPlusNote)clientReportList.get(x);
				log.debug("client note: "+clientnote.getNotes());
				log.debug("First name: "+clientnote.getFirstName());
				log.debug("Last name: "+clientnote.getLastName());
				HSSFRow excelRow = excelSheet.createRow(record++);
				excelRow.createCell(0).setCellValue(clientnote.getFirstName());
				excelRow.createCell(1).setCellValue(clientnote.getLastName());
				excelRow.createCell(2).setCellValue(clientnote.getEmail());
				excelRow.createCell(3).setCellValue(clientnote.getCell_phone());
				excelRow.createCell(4).setCellValue(clientnote.getWork_phone());
				excelRow.createCell(5).setCellValue(clientnote.getHome_phone());
				excelRow.createCell(6).setCellValue(clientnote.getAddress1());
				excelRow.createCell(7).setCellValue(clientnote.getAddress2());
				excelRow.createCell(8).setCellValue(clientnote.getCitycode());
				excelRow.createCell(9).setCellValue(clientnote.getStatecode());
				excelRow.createCell(10).setCellValue(clientnote.getZipcode());
				excelRow.createCell(11).setCellValue(clientnote.getNotes());
				excelRow.createCell(12).setCellValue(clientnote.getBirthDay());
				
			}
		}
		if(reportTodaysPayementsList != null){
			log.debug("reportTodaysPayementsList not null");
		}
		
		if(reportList != null){
			log.debug("reportList not null");
			HSSFSheet excelSheet = workbook.createSheet("Report List");
			setExcelHeader(excelSheet);
			
			setExcelRows(excelSheet,reportList);
		}
		
		log.debug("Exiting buildExcelDocument");
	}

	public void setExcelHeader(HSSFSheet excelSheet) {
		HSSFRow excelHeader = excelSheet.createRow(0);
		excelHeader.createCell(0).setCellValue("Id");
		excelHeader.createCell(1).setCellValue("Name");
		excelHeader.createCell(2).setCellValue("Type");
		excelHeader.createCell(3).setCellValue("Aggressive");
		excelHeader.createCell(4).setCellValue("Weight");
	}
	
	public void setExcelRows(HSSFSheet excelSheet, ArrayList<Clients> clientList){
		int record = 1;
		for (Clients client : clientList) {
			HSSFRow excelRow = excelSheet.createRow(record++);
			excelRow.createCell(0).setCellValue(client.getId());
			excelRow.createCell(1).setCellValue(client.getFirstName());
			excelRow.createCell(2).setCellValue(client.getLastName());
			excelRow.createCell(3).setCellValue(client.getUsername());
			excelRow.createCell(4).setCellValue(client.getFirstName());
		}
	}	

}
