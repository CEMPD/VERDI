package anl.verdi.io;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;import gov.epa.emvl.GridCellStatistics;



public class WebClient {
	
    
	
	public static final String CONTENT_URL_ENCODED = "application/x-www-form-urlencoded";
	
	Map<String, String> headers = new HashMap<String, String>();
	Map<String, String> params = new HashMap<String, String>();
	String uri = null;
	String requestMethod = "GET";
	String contentType = null;
	String requestData = null;
	
	int responseCode = 0;
	String responseData = "";
	
	public void init() {
		uri = null;
		requestMethod = "GET";
		contentType = CONTENT_URL_ENCODED;
		headers.clear();
		params.clear();
	}
	
	public void setContentType(String str) {
		contentType = str;
	}
	
	public String getRequestMethod() {
		return requestMethod;
	}
	
	public void setRequestMethod(String str) {
		requestMethod = str;
	}
	
	public String getUri() {
		return uri;
	}
	
	public void setUri(String str) {
		uri = str;
	}
	
	public void setRequestData(String data) {
		requestData = data;
	}
	
	public String getRequestData() {
		return requestData;
	}
	
	public void addHeader(String key, String value) {
		headers.put(key, value);
	}
	
	public void addParam(String key, String value) {
		params.put(key, value);
	}
	
	public void sendRequest() throws IOException {
		String targetUrl = uri;
		String urlParameters = "";
		if (CONTENT_URL_ENCODED.equals(contentType))
			urlParameters = buildUrlParams();
		else if (requestData != null && requestMethod.equals("POST"))
			urlParameters = requestData;
		
	    if (requestMethod.equals("GET") && urlParameters.length() > 0)
	    	targetUrl += "?" + urlParameters;
	    URL url = new URL(targetUrl);
	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	    connection.setRequestMethod(requestMethod);
	    if (contentType != null)
	    	connection.setRequestProperty("Content-Type", 
	    			contentType);
	    
	    for(String header : headers.keySet()) {
	    	connection.setRequestProperty(header, headers.get(header));
	    }

	    if (urlParameters.length() > 0)
	    	connection.setRequestProperty("Content-Length", 
	    			Integer.toString(urlParameters.getBytes().length));
	    //connection.setRequestProperty("Content-Language", "en-US");  
	    

	    connection.setUseCaches(false);

	    if (requestMethod.equals("POST")) {
	    	connection.setDoOutput(true);
		    DataOutputStream wr = new DataOutputStream (
			        connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.close();
	    }


	    //200, 201: success
	    responseCode = connection.getResponseCode();
	    // System.out.println(uri);
	    // System.out.println(responseCode);
	    //Get Response  
	    InputStream is = null;
	    if (responseCode >= 400)
	    	is = connection.getErrorStream();
	    else
	    	is = connection.getInputStream();
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	    StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
	    String line;
	    while ((line = rd.readLine()) != null) {
	      response.append(line);
	      response.append('\r');
	    }
	    rd.close();


	    connection.disconnect();
	    
	    responseData = response.toString();
	    //System.out.println(responseData);
	  }
	
	public int getResponseCode() {
		return responseCode;
	}
	
	
	public static void main(String[] args) {
		WebClient client = new WebClient();
		client.init();
		client.setRequestMethod("GET");
		
		Formatter f = new Formatter();
		double lonMin = -144.35896888056763;
		double lonMax = -45.90539494857017;
		double latMin = 11.529013854774044;
		double latMax = 55.74150445246831;
		

		int resx = 800;
		int resy = 600;
		String key = "pk.eyJ1IjoidGFob3dhcmQ5NiIsImEiOiJjbTBjdGphbWowNjdqMmlxMTEyejNkNndoIn0.gQYDB3yt--NRlehJqPSwfQ";
		//formatter.format("%4$2s %3$2s %2$2s %1$2s", "a", "b", "c", "d")
		String template = "https://api.mapbox.com/styles/v1/mapbox/satellite-v9/static/[%1.5f,%1.5f,%1.5f,%1.5f]/%dx%d?access_token=%s";
		String formatted = f.format(template, lonMin, latMin, lonMax, latMax, resx, resy, key).toString();
		//String template = "Hello %s there";
		//String formatted = f.format(template ,"jim").toString();
		System.out.println(formatted);
		
		client.setUri(formatted);
	}
	
	public String getResponseData() {
		return responseData;
	}
	
	private String buildUrlParams() throws UnsupportedEncodingException {
		String ret = "";
		if (params.isEmpty()) {
			return ret;
		}
		for(String key : params.keySet()) {
			if (ret.length() > 0)
				ret += "&";
			ret += URLEncoder.encode(key, StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8.name());
		}
		return ret;
	}


}
