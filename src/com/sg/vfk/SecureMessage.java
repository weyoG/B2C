package com.sg.vfk;

import java.io.File;
import java.io.FileInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;

/**
 * TODO: Document me!
 *
 * @author kudzanai.masiwa
 *
 */
public class SecureMessage {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
       
       String src = "C:\\Users\\glen.weyombo\\Documents\\SG\\projects\\java\\VFKGenericHttpClient\\SandboxCertificate.cer|Safaricom998#";
       System.out.println(encrypt(src));
    }
    
    public static String encrypt(String src) {
        try {
            
            
            String[] arr1 = src.split("\\|");
            String InitiationPassword = arr1[1];
            String Cert = arr1[0];
            
            byte[] input = InitiationPassword.getBytes();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            FileInputStream fin = new FileInputStream(new File(Cert));
            CertificateFactory f = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate)f.generateCertificate(fin);
            PublicKey pk = certificate.getPublicKey();
            cipher.init(Cipher.ENCRYPT_MODE, pk);
            byte[] cipherText = cipher.doFinal(input);
            ToBase64 b64 = new ToBase64();
            String result = b64.convertToBase64(cipherText);
            return result;
        } catch (Exception e) {
             return e.getMessage();
        }
    }
    
    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
    

}
