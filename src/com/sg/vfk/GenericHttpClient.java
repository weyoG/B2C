package com.sg.vfk;

import com.temenos.tafj.common.jSession;
import com.temenos.tafj.common.jVar;
import com.temenos.tafj.runtime.extension.BasicReplacement;
import com.temenos.tafj.runtime.jRunTime;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class GenericHttpClient extends BasicReplacement {

    private static final String className = GenericHttpClient.class.getName();

    @Override
    public jVar invoke(Object... arg0) {
        String request = String.valueOf(arg0[0]);
        String result = processRequest(request);
        ((jVar) arg0[1]).set(result);
        return null;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        System.out.println("Test Run");
        GenericHttpClient ghc = new GenericHttpClient();
        String src = "C:\\Users\\glen.weyombo\\Documents\\SG\\projects\\java\\VFKGenericHttpClient\\sandboxCert.cer|Safaricom998#";
        String securityCredential = SecureMessage.encrypt(src);
        //String strdata = "endPoint*https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials|connectionTimeout*120|sessionTimeout*120|headers*Content-Type: application/json^Authorization: Basic QkN5VU1WZjF4V0cyRzFzNUc2aGRMc05pWUhCUDhFenI6NE1JT3JWeEtRSDFFNjd4aA==|payload*{}|requestType*getRequest";
        String strdata = "endPoint*https://sandbox.safaricom.co.ke/mpesa/b2c/v1/paymentrequest|connectionTimeout*120|sessionTimeout*180|" +
                "headers*Content-Type: application/json^Authorization: Bearer DSXlGYAXR6jrskT47eVqmkpvJaYg|" +
                "payload*{" +
                "\"InitiatorName\": \"ApiOP98\"," +
                "\"SecurityCredential\":\"" +securityCredential+"\"," +
                "\"CommandID\": \"BusinessPayment\"," +
                "\"Amount\": \"10\"," +
                "\"PartyA\": \"600998\"," +
                "\"PartyB\": \"254708374149\"" +
                ",\"Remarks\": \"Test Remarks\"," +
                "\"QueueTimeOutURL\": " +
                "\"https://d1luqxddbfw3uk.cloudfront.net/VAS/services/b2c/queueto\"," +
                "\"ResultURL\": \"https://d1luqxddbfw3uk.cloudfront.net/VAS/services/b2c/result\"" +
                ",\"Occassion\": \"Additional Info\"" +
                "}" +
                "|requestType*postRequest";
        String strghc = ghc.processRequest(strdata);

        System.out.println(strghc);
    }

    private String processRequest(final String data) {
        String[] arr1 = data.split("\\|");
        Map<String, String> map = new HashMap<>();
        if (arr1.length > 0)
            for (String s : arr1) {
                String[] arr2 = s.split("\\*");
                if (arr2.length == 2)
                    map.put(arr2[0], arr2[1]);
            }

        if (map.size() == 0)
            return "05|Invalid number of parameter";

        String result = "";
        Object obj = map.get("requestType");
        if (obj == null)
            return "05|Invalid request type";

        String requestType = obj.toString();
        switch (requestType) {
            case "getISODate":
                result = getISODate();
                break;
            case "escapeString":
                result = escapeString(map);
                break;
            case "getRequest":
                result = getRequest(map);
                break;
            case "postRequest":
                result = postRequest(map);
                break;
            default:
                return "05|Unsupported request type";
        }

        return result;
    }

    private String getISODate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        return sdf.format(new Date());
    }

    private String escapeString(final Map<String, String> map) {
        String str = map.get("payload");
        return StringEscapeUtils.escapeJava(str);
    }


    private synchronized String postRequest(final Map<String, String> map) {
        String statusCode = "05";
        String statusMsg = "Generic Error 01";
        String obj = null;

        URL url;
        HttpURLConnection con = null;
        InputStream in = null;
        BufferedReader bf = null;
        try {
            allowCerts();

            obj = map.get("endPoint");
            if (obj == null)
                throw new Exception("EndPoint not set");
            String endPoint = obj;

            obj = map.get("connectionTimeout");
            if (obj == null)
                throw new Exception("Connection Timeout not set");
            int connectionTimeout = Integer.parseInt(obj);

           /* obj = map.get("sessionTimeout");
            if (obj == null)
                throw new Exception("Session Timeout not set");
            int sessionTimeout = Integer.parseInt(obj);*/

            obj = map.get("headers");
            if (obj == null)
                throw new Exception("HTTP Headers not set");
            String headers = obj;

            obj = map.get("payload");
            if (obj == null)
                throw new Exception("Request Payload not set");
            String payload = obj;

            System.out.println(String.format("The request is: %s", payload));

            url = new URL(endPoint);
            con = endPoint.startsWith("http") ? (HttpURLConnection) url.openConnection()
                    : (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");

            String[] arr = headers.split("\\^");
            if (arr.length > 0)
                for (String s : arr) {
                    String[] arr2 = s.split("\\:");
                    if (arr2.length > 0)
                        con.setRequestProperty(arr2[0], arr2[1]);
                }

            con.setConnectTimeout(connectionTimeout);
            // con.setReadTimeout(sessionTimeout);
            con.setDoOutput(true);

            DataOutputStream dos = new DataOutputStream(con.getOutputStream());
            dos.write(payload.getBytes());
            dos.flush();
            dos.close();

            in = con.getInputStream();
            if (in == null) {
                in = con.getErrorStream();
            }

            bf = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bf.readLine()) != null) {
                sb.append(line);
            }

            statusCode = "00";
            statusMsg = sb.toString();
        } catch (MalformedURLException ex) {
            statusMsg = String.format("%s:%s", "MalformedURLException: ", ex.getMessage());
        } catch (IOException ex) {
            statusMsg = String.format("%s:%s", "IOException: ", ex.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            statusMsg = String.format("%s:%s", "NoSuchAlgorithmException: ", ex.getMessage());
        } catch (KeyManagementException ex) {
            statusMsg = String.format("%s:%s", "KeyManagementException: ", ex.getMessage());
        } catch (Exception ex) {
            statusMsg = String.format("%s:%s", "Exception: ", ex.getMessage());
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (Exception ex) {
                    ex.toString();
                }
            }

            if (in != null) {
                try {
                    in.close();
                } catch (Exception ex) {
                    ex.toString();
                }
            }

            if (con != null) {
                try {
                    con.disconnect();
                } catch (Exception ex) {
                    ex.toString();
                }
            }
        }

        String result = String.format("%s|%s", statusCode, statusMsg);
        System.out.println(String.format("The result is: %s", statusMsg));

        return result;
    }

    private synchronized String getRequest(final Map<String, String> map) {
        String statusCode = "05";
        String statusMsg = "Generic Error 01";
        String obj = null;

        URL url;
        HttpURLConnection con = null;
        InputStream in = null;
        BufferedReader bf = null;
        try {
            allowCerts();

            obj = map.get("endPoint");
            if (obj == null)
                throw new Exception("EndPoint not set");
            String endPoint = obj;

            obj = map.get("connectionTimeout");
            if (obj == null)
                throw new Exception("Connection Timeout not set");
            int connectionTimeout = Integer.parseInt(obj);

            obj = map.get("sessionTimeout");
            if (obj == null)
                throw new Exception("Session Timeout not set");
            //  int sessionTimeout = Integer.parseInt(obj);

            obj = map.get("headers");
            if (obj == null)
                throw new Exception("HTTP Headers not set");
            String headers = obj;

            obj = map.get("payload");

            url = new URL(endPoint);
            con = endPoint.startsWith("http") ? (HttpURLConnection) url.openConnection()
                    : (HttpsURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("GET");

            String[] arr = headers.split("\\^");
            if (arr.length > 0)
                for (String s : arr) {
                    String[] arr2 = s.split("\\:");
                    if (arr2.length > 0)
                        con.setRequestProperty(arr2[0], arr2[1]);
                }

            con.setConnectTimeout(connectionTimeout);
            //con.setReadTimeout(sessionTimeout);
            con.setDoOutput(true);
            con.connect();

            in = con.getInputStream();
            if (in == null) {
                in = con.getErrorStream();
            }

            bf = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bf.readLine()) != null) {
                sb.append(line);
            }

            statusCode = "00";
            statusMsg = sb.toString();
        } catch (MalformedURLException ex) {
            statusMsg = String.format("%s:%s", "MalformedURLException: ", ex.getMessage());
        } catch (IOException ex) {
            statusMsg = String.format("%s:%s", "IOException: ", ex.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            statusMsg = String.format("%s:%s", "NoSuchAlgorithmException: ", ex.getMessage());
        } catch (KeyManagementException ex) {
            statusMsg = String.format("%s:%s", "KeyManagementException: ", ex.getMessage());
        } catch (Exception ex) {
            statusMsg = String.format("%s:%s", "Exception: ", ex.getMessage());
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (Exception ex) {
                    ex.toString();
                }
            }

            if (in != null) {
                try {
                    in.close();
                } catch (Exception ex) {
                    ex.toString();
                }
            }

            if (con != null) {
                try {
                    con.disconnect();
                } catch (Exception ex) {
                    ex.toString();
                }
            }
        }

        String result = String.format("%s|%s", statusCode, statusMsg);
        System.out.println(String.format("The result is: %s", statusMsg));

        return result;
    }


    private synchronized void allowCerts() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    public static jRunTime INSTANCE(jSession session) {
        jRunTime prg = session.getRuntimeCache(className);
        if (prg == null) {
            prg = new GenericHttpClient();
            prg.init(session);
        }
        return prg;
    }

    public void stack(jRunTime prg) {
        session.setRuntimeCache(className, prg);
    }

}
