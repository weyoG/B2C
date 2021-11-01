package com.sg.vfk;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * TODO: Document me!
 *
 * @author kudzanai.masiwa
 *
 */
public class ToBase64 {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }
    
    public static String convertToBase64(String accessCredentials) {
        String errorMsg = null;
        String base64encodedString = null;
        try {
          
           // Encode using basic encoder
           String credentials = accessCredentials;
           base64encodedString = Base64.getEncoder().encodeToString(
                   credentials.getBytes("utf-8"));          
           if (base64encodedString == null){
               errorMsg = "Base64 Null Exception";
             throw new NullPointerException(errorMsg);  
           }
        } catch(UnsupportedEncodingException e) {
            errorMsg = "Unsupported Encoding :" + e.getMessage();
           System.out.println(errorMsg);
           return errorMsg;
        }catch(NullPointerException e) {
            errorMsg = "Base64 NullPointerException :" + e.getMessage();
           System.out.println(errorMsg);
           return errorMsg; 
        }

        return base64encodedString;
     }
    
    public String convertToBase64(byte[] bytarray){
        String errorMsg = null;
        String result = null;
      try{
          result = Base64.getEncoder().encodeToString(bytarray);
     
      } catch (NullPointerException e) {
          errorMsg = "Base64 NullPointerException :" + e.getMessage();
         System.out.println(errorMsg);
         return errorMsg;   
      }
        return result;
    
    }
    

}
