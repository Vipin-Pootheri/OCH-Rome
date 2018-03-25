package webdriver;

import java.io.IOException;
import java.util.Hashtable;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.relevantcodes.extentreports.LogStatus;




public class TestCase extends BaseTest{
	
	Xls_Reader xls = new Xls_Reader(System.getProperty("user.dir")+ "\\TestData.xlsx");
	String dataBaseName = xls.getCellData("Config", 1, 5)+":1521:" + xls.getCellData("Config", 1, 7);
	String SchemaName=xls.getCellData("Config", 1, 3);
	String password =xls.getCellData("Config", 1, 4);

	
	@Test(priority=1,dataProvider="getData")
	public void test(Hashtable<String, String> data) {
		try{
		init();
		int colcount= getcolcount();
		test=rep.startTest("Data Creation for " + data.get("PRTY_KEY"));
		test.log(LogStatus.INFO, "Starting data creation for "+ data.get("PRTY_KEY"));
		/*openBrowser("Mozilla");
		navigate("ebobsurl");
		type("srchfield_xpath",data.get("Name"));
		click("srchbtn_xpath");
		Thread.sleep(200);
		type("minprice_xpath",data.get("Salary"));
		type("maxprice_xpath",data.get("Age"));
		click("Go_xpath");*/
		test.log(LogStatus.PASS, "Data for " + data.get("PRTY_KEY") + " created sucesssfully");
		test.log(LogStatus.INFO, "Verification of data in Rome Started");
		connectdatabase(dataBaseName,SchemaName,password);
		boolean test = validate_customer (data.get("PRTY_KEY"));
		if (test) {
			for (int i=0;i<colcount;i++) {
			int j=1;
			if (xls.getCellData("TestData", i, 1).endsWith("NA")) {}
			else 
			{
				String tablename = xls.getCellData("TestData", i, j);
				String fieldname =xls.getCellData("TestData", i, j+1);
				String prty_key_value =data.get("PRTY_KEY"); 
				String text = data.get(fieldname);
				validate_attributes(tablename, fieldname,prty_key_value,text);
				}
			}
		}
		}
		catch(Exception e ) {
			test.log(LogStatus.FAIL, "Test Failed due to + " + e.getMessage());
			closebrowser();
		}
	}
	@AfterMethod()
	public void quit() {
		closebrowser();
	}
	@DataProvider
	public Object[][] getData() throws IOException{
				
		
		String sheetname="TestData";
		
		int k=2;
		int rows=0;
		while (!(xls.getCellData(sheetname, 0, k)=="")){
			k=k+1;
			rows=rows+1;
		}
		rows =rows-1;
		//System.out.println("No.Of datarows " + rows);

		int cols=0;
		while (!(xls.getCellData(sheetname, cols, 1)=="")){
			cols =cols+1;
		}
		//System.out.println("No.Of datacols " + cols);
		int row=0;
		//read data
		Object[][] data = new Object[rows][1];
		Hashtable<String, String> table =null;
		String key;
		String value;
		for (int i=2;i<=rows+1;i++) {
			table =new Hashtable<String,String>();
			for (int j=0;j<cols;j++) {
				key=xls.getCellData(sheetname, j, 2);
				value=xls.getCellData(sheetname, j, i+1);	
				table.put(key, value);
			}
			data[row][0]=table;
			row =row+1;
		}
		return data;
	
	}

	public int getcolcount() {
		int coldata=0;
		while (!(xls.getCellData("TestData", coldata, 1)=="")){
			coldata =coldata+1;
		}
		return coldata;
	}


}
