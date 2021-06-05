/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author Johannes Loibner
 */
public class HttpsRequestPoster {
    
    private final static String USER_AGENT = "DesktopApp";
    
    static {
        
        TrustManager[] trustAllCertificates = new TrustManager[]
        {
            new X509TrustManager() 
            {
                @Override
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }
        };
        
        HostnameVerifier trustAllHostnames = new HostnameVerifier() 
        {
            @Override
            public boolean verify(String string, SSLSession ssls) {
                return true;
            }
        };
        
        try
        {
            System.setProperty("jsse.enableSNIExtension", "false");
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCertificates, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(trustAllHostnames);
        }
        catch(GeneralSecurityException e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }
        
        public static String makePostRequest(String url, Map<String, String> parameters) 
        {
            try 
            {
                //ensureAllParametersArePresent(parameters);
                //we need this cookie to submit form
                String initialCookies = getUrlConnection(url, "").getHeaderField("Set-Cookie");
                HttpsURLConnection con = getUrlConnection(url, initialCookies);
                String urlParameters = processRequestParameters(parameters);
                // Send post request
                sendPostParameters(con, urlParameters);
                int code = con.getResponseCode();
                String text = "";
                if(code == 200)
                {
                    BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                    File outputFile = new File("P:\\file.html");
                    if (!outputFile.exists()) {
                        outputFile.createNewFile();
                    }
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        //bw.write(inputLine);
                        text += inputLine + "\n";
                    }
                    in.close();
                    bw.flush();
                    bw.close();
                }
                
                //print result
                text = text + ";" + code;
                
                
                return text;
            } 
            catch (Exception e) 
            {
                throw new RuntimeException(e);
            }
        }
        
        private static void sendPostParameters(URLConnection con, String urlParameters) throws IOException 
        {
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
        }
        
        private static HttpsURLConnection getUrlConnection(String url, String cookies) throws IOException 
        {
            HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            //con.setRequestProperty("Cookie", cookies);
            return con;
        }
        
        private static void ensureAllParametersArePresent(Map<String, String> parameters) 
        {
            if (parameters.get("send") == null) 
            {
                parameters.put("send", "Envoyer votre message");
            }
            if (parameters.get("phone") == null) 
            {
                parameters.put("phone", "");
            }
        }
        
        private static String processRequestParameters(Map<String, String> parameters) 
        {
            StringBuilder sb = new StringBuilder();
            for (String parameterName : parameters.keySet()) 
            {
                sb.append(parameterName).append('=').append(urlEncode(parameters.get(parameterName))).append('&');
            }
            return sb.substring(0, sb.length() - 1);
        }
        
        private static String urlEncode(String s) 
        {
        try 
        {
            return URLEncoder.encode(s, "UTF-8");
        } 
        catch (UnsupportedEncodingException e) 
        {
            // This is impossible, UTF-8 is always supported according to the java standard
            throw new RuntimeException(e);
        }
    }

    
}
