package test;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import com.jayway.jsonpath.JsonPath;

public class PlivoTest {
	private final static String auth_id = "MANJCWMZNJZJJKMZKYNJ";
	private final static String auth_token = "ZDRjNmJiMmFkNjVmOTQ4YTAzZDdhOGYwZTU5ZTEz";
	private final static String plivo_url = "https://api.plivo.com";

	public static void main(String[] args) throws Exception {
		
		//Get Cash Credits to check after completion
		String retrieveAccountDetails = getResponse(plivo_url+"/v1/Account/"+auth_id+"/");
		String preCashCredits = getListOfFieldDataInJSON("cashCredit", retrieveAccountDetails);
		System.out.println("Total Cash Credits = " + preCashCredits);

		/**
		 * Scenario - 1
		 * Get all the Numbers
		 */
		String getAllNumbersResponse = getResponse(plivo_url+"/v1/Account/"+auth_id+"/Number/");
		String getallNumbers = getListOfFieldDataInJSON("number", getAllNumbersResponse);
		System.out.println(getallNumbers);

		/**
		 * Scenario - 2
		 * Get Message UUID after sending message
		 */
		String requestBody = "{\r\n" + 
				"  \"src\": \""+getallNumbers+"\",\r\n" + 
				"  \"dest\": \""+getallNumbers+"\",\r\n" + 
				"  \"text\": \"Text Message\",\r\n" + 
				"  \"type\": \"sms\",\r\n" + 
				"  \"url\": \"https://api.plivo.com/v1/Account/\""+auth_id+"\"/Message\",\r\n" + 
				"  \"method\": \"POST\",\r\n" + 
				"  \"log\": false,\r\n" + 
				"  \"trackable\": true\r\n" + 
				"}";

		String sendMessageResponse = getResponse(plivo_url+"/v1/Account/"+auth_id+"/Message/", requestBody);
		String getMessageUUID = getListOfFieldDataInJSON("message_uuid", sendMessageResponse);
		System.out.println(getMessageUUID);

		/**
		 * Scenario - 3
		 * Get the details of message using message UUID
		 */
		String getMDRResponse = getResponse(plivo_url+"/v1/Account/"+auth_id+"/Message/"+getMessageUUID+"/");
		System.out.println(getMDRResponse);

		/**
		 * Scenario - 4
		 * Get the details of pricing api to determine the rate of the message which is outbound rate under
		 */
		String getPricigResponse = getResponse(plivo_url+"/v1/Account/"+auth_id+"/Pricing/");
		String getOutboundRate = getListOfFieldDataInJSON("outboundRate", getPricigResponse);
		System.out.println(getOutboundRate);

		/**
		 * Scenario - 5
		 * Verify the rate and the price deducted for the sending message, should be same
		 */
		String total_rate = getListOfFieldDataInJSON("totalRate", getMDRResponse);
		String.valueOf(total_rate == String.valueOf(getOutboundRate) ? "Rate and Price deducted for sending message is same" : 
			new Exception("Rate and Price deducted for sending message is same"));

		/**
		 * Scenario - 6
		 * Get all the Numbers
		 */
		retrieveAccountDetails = getResponse(plivo_url+"/v1/Account/"+auth_id+"/");
		String cashCredits = getListOfFieldDataInJSON("cashCredit", retrieveAccountDetails);
		boolean remainingAmout = (Integer.parseInt(preCashCredits) - Integer.parseInt(total_rate)) == Integer.parseInt(cashCredits);
		System.out.println("Total Remaining Cash Credits = " + remainingAmout);

	}

	/**
	 * Used to get property from Properties file
	 * @param key
	 * @return
	 */
	public static String getProperty(String key){
		Properties prop = new Properties();
		InputStream input = null;
		String value = null;
		try {
			input = new FileInputStream(System.getProperty("user.dir") + "/resources/path.properties");
			prop.load(input);
			value = prop.getProperty(key);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return value;
	}

	/**
	 * Used to get the response for POST request
	 * @param url
	 * @param requestBody
	 * @return
	 * @throws Exception
	 */
	public static String getResponse(String url, String requestBody) throws Exception{
		String response;
		HttpURLConnection con = null;
		URL uri = null;
		int code = 0;
		try {
			uri = new URL(url);
			System.out.println(uri);
			con = (HttpURLConnection) uri.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Authorization", auth_token);
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("charset","UTF-8");
			con.setDoOutput(true);
			con.setDoInput(true);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		if( requestBody != null) {
			con.setDoInput(true);
			con.setDoOutput(true);
			try {
				DataOutputStream out = new  DataOutputStream(con.getOutputStream());
				out.writeBytes(requestBody);
				out.flush();
				out.close();
			} catch (IOException e) {
				throw new Exception(e.getMessage());
			}
		}
		try {
			code = con.getResponseCode();
			if(code == 200){
				System.out.println("Actual Status Code: "+code);
			}else{
				throw new Exception("Expected Status code: 200. Actual Status code: "+code);
			}
			con.getContent().toString();
			InputStream in = con.getInputStream();
			String encoding = con.getContentEncoding();
			encoding = encoding == null ? "UTF-8" : encoding;
			response = IOUtils.toString(in, encoding);
			System.out.println("Response: "+response);
		}catch(Exception e){
			throw new Exception("Failed to get json response");
		}
		return response;
	}

	/**
	 * Used to get the response for GET request
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String getResponse(String url) throws Exception{
		String response;
		HttpURLConnection con = null;
		URL uri = null;
		int code = 0;
		try {
			uri = new URL(url);
			System.out.println(uri);
			con = (HttpURLConnection) uri.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("charset","UTF-8");
			con.setDoOutput(true);
			con.setDoInput(true);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}

		try {
			code = con.getResponseCode();
			if(code == 200){
				System.out.println("Actual Status Code: "+code);
			}else{
				throw new Exception("Expected Status code: 200. Actual Status code: "+code);
			}
			con.getContent().toString();
			InputStream in = con.getInputStream();
			String encoding = con.getContentEncoding();
			encoding = encoding == null ? "UTF-8" : encoding;
			response = IOUtils.toString(in, encoding);
			System.out.println("Response: "+response);
		}catch(Exception e){
			throw new Exception("Failed to get json response", e);
		}
		return response;
	}

	/**
	 * Used to get the values from JSON response
	 * @param xpath
	 * @param jsonresponse
	 * @return
	 * @throws Exception
	 */
	public static String getListOfFieldDataInJSON(String xpath, String jsonresponse) throws Exception{
		String value = null;
		try {
			value = (JsonPath.read(jsonresponse, xpath)).toString();
		} catch (InvalidPathException e) {
			return null;
		} 
		if(value == null){
			throw new Exception("Not able to get the data with the JSON xpath - <b>" + xpath + "</b>");
		}
		return value;
	}
}
