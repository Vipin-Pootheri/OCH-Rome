package webdriver;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;



public class BaseTest {
	public WebDriver driver;
	public Properties prop;
	public ExtentReports rep = ExtentManager.getInstance();
	//public ExtentReports rep = ExtentManager.getInstance();
	public ExtentTest test;
	public Connection con;
	public Statement st;
	public void connectdatabase(String dataBaseName,String SchemaName, String password) throws SQLException, ClassNotFoundException {
		try {
			
			Class.forName("oracle.jdbc.driver.OracleDriver");
			 con = DriverManager.getConnection("jdbc:oracle:thin:@" + dataBaseName , SchemaName , password);
			 st=con.createStatement(); 
		}catch (Error e) {
			reportFailure("Cannot connect to database " + e.getMessage());
			Assert.fail();
		}
	}

	public void init(){
		//init the prop file
		
		
		if(prop==null){
			prop=new Properties();
			try {
				FileInputStream fs = new FileInputStream(System.getProperty("user.dir")+"//src//test//java//webdriver//projectconfig.properties");
				prop.load(fs);
				 

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void openBrowser(String bType){
	
		if(bType.equals("Mozilla")) {
			System.setProperty("webdriver.gecko.driver", prop.getProperty("firefoxdriver_exe"));
			driver=new FirefoxDriver();
			test.log(LogStatus.INFO, "Opened Mozilla Browser");}
		else if(bType.equals("Chrome")){
			System.setProperty("webdriver.chrome.driver", prop.getProperty("chromedriver_exe"));
			driver=new ChromeDriver();
			test.log(LogStatus.INFO, "Opened Chrome Browser");
		}
		
		driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		
	}

	public void navigate(String urlKey){
		driver.get(prop.getProperty(urlKey));
		test.log(LogStatus.INFO, "Navigated to " + urlKey);
	}
	
	public void click(String locatorKey){
		getElement(locatorKey).click();
		test.log(LogStatus.INFO, "Clicked on  " + locatorKey);
	}
	
	public void type(String locatorKey,String data){
		getElement(locatorKey).sendKeys(data);
		test.log(LogStatus.INFO, "Typed " + data + " on the field " + locatorKey);
		
	}
	// finding element and returning it
	public WebElement getElement(String locatorKey){
		WebElement e=null;
		try{
		if(locatorKey.endsWith("_id"))
			e = driver.findElement(By.id(prop.getProperty(locatorKey)));
		else if(locatorKey.endsWith("_name"))
			e = driver.findElement(By.name(prop.getProperty(locatorKey)));
		else if(locatorKey.endsWith("_xpath"))
			e = driver.findElement(By.xpath(prop.getProperty(locatorKey)));
		else{
			reportFailure("Locator not correct - " + locatorKey);
			Assert.fail("Locator not correct - " + locatorKey);
		}
		
		}catch(Exception ex){
			// fail the test and report the error
			reportFailure(ex.getMessage());
			ex.printStackTrace();
			Assert.fail("Failed the test - "+ex.getMessage());
		}
		return e;
	}
	/***********************Validations***************************/
	public boolean verifyTitle(){
		return false;		
	}
	
	public boolean isElementPresent(String locatorKey){
		List<WebElement> elementList=null;
		if(locatorKey.endsWith("_id"))
			elementList = driver.findElements(By.id(prop.getProperty(locatorKey)));
		else if(locatorKey.endsWith("_name"))
			elementList = driver.findElements(By.name(prop.getProperty(locatorKey)));
		else if(locatorKey.endsWith("_xpath"))
			elementList = driver.findElements(By.xpath(prop.getProperty(locatorKey)));
		else{
			reportFailure("Locator not correct - " + locatorKey);
			Assert.fail("Locator not correct - " + locatorKey);
		}
		
		if(elementList.size()==0)
			return false;	
		else
			return true;
	}
	
	public boolean verifyText(String locatorKey,String expectedTextKey){
		String actualText=getElement(locatorKey).getText().trim();
		String expectedText=prop.getProperty(expectedTextKey);
		if(actualText.equals(expectedText))
			return true;
		else 
			return false;
		
	}
	/*****************************Reporting********************************/
	
	public void reportPass(String msg){
		test.log(LogStatus.PASS, msg);
	}
	
	public void reportFailure(String msg){
		test.log(LogStatus.FAIL, msg);
		takeScreenShot();
		Assert.fail(msg);
	}
	
	public void takeScreenShot(){
		// fileName of the screenshot
		Date d=new Date();
		String screenshotFile=d.toString().replace(":", "_").replace(" ", "_")+".png";
		// store screenshot in that file
		File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
		try {
			FileUtils.copyFile(scrFile, new File(System.getProperty("user.dir")+"//screenshots//"+screenshotFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//put screenshot file in reports
		test.log(LogStatus.INFO,"Screenshot-> "+ test.addScreenCapture(System.getProperty("user.dir")+"//screenshots//"+screenshotFile));
		
	}
	public void closebrowser() {
		rep.endTest(test);
		rep.flush();
		//driver.quit();
	}
	
	public void validate_attributes(String tablename, String fieldname, String prty_key_value, String text) {
		test.log(LogStatus.INFO, "Started the validation of "+ fieldname + " for the value "+ text);
		String query = "select attrib_val from " + tablename + " where PRTY_KEY = '" + prty_key_value +"'" + " and attrib_nm = '" + fieldname + "'" ;
		int tcount=0;
		try {
		ResultSet rs=st.executeQuery(query);
		String actresult=null;
		while (rs.next()) {
			 actresult= rs.getString(1);
					tcount=tcount+1;}
		if(tcount >1) {
			test.log(LogStatus.FAIL, "Attribute "+ fieldname + " has multiple values in " + tablename + " table  : Query used : "+ query);
		}	
		else if (tcount==0) {
			if (text.equals("")) {
				test.log(LogStatus.PASS, "Attribute "+ fieldname + " is not present in " + tablename + " table as data is not enetered: Query used : "+ query);
			}
			else {
			test.log(LogStatus.FAIL, "Attribute "+ fieldname + " has not loaded in " + tablename + " table : Query used : "+ query);
			}
		}
		else if (tcount==1) {
			
			if (actresult.equals(text)){
				test.log(LogStatus.PASS, "Attribute "+ fieldname + " has correctly loaded into "+ tablename + "table : Actual Value = "+ actresult + " Expected Value = "+ text);
			}
			else {
				test.log(LogStatus.FAIL, "Attribute "+ fieldname + " has not loaded properly into "+ tablename + "table : Actual Value = "+ actresult + " Expected Value = "+ text + " : Query used : "+ query);
			}
		}
		}catch (Throwable e ) {
			
			test.log(LogStatus.FAIL,"Error in Executing query : "  + e.getMessage() + "Query Used :"+ query);
			
	}
		

	}
	public boolean validate_customer(String prty_key) throws SQLException {
		test.log(LogStatus.INFO, "Validation of Cutomer " + prty_key + " started ");
		int tcount=0;
		String query="select * from prty_hist where attrib_nm = 'PRTY_KEY' and end_dt = '31DEC2999' and attrib_val = '" + prty_key + "'";
		try {
		ResultSet rs=st.executeQuery(query);
		while (rs.next()) {
			tcount=tcount+1;
		}
		if(tcount ==1) {
			test.log(LogStatus.PASS, "Customer "+ prty_key + " has correctly loaded to PRTY_HIST table  : Query used : "+ query);
			return true;
		}	
		else if(tcount >1){
			test.log(LogStatus.FAIL, "Customer "+ prty_key + " has loaded multiple times to PRTY_HIST table  : Query used : "+ query);
			
			return false;
		}
		else if(tcount ==0){
			test.log(LogStatus.FAIL, "Customer "+ prty_key + " has not loaded to PRTY_HIST table  : Query used : "+ query);
			
			return false;
		}
		}catch (Error e) {
			test.log(LogStatus.FAIL, "Error in executing query "+ e.getMessage() + "Query = "+ query);
			Assert.fail();
			return false;
		}
		return false;
	}
}
