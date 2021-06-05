
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Johannes Loibner
 */
public class MainController {
    
    private char [] keyStorePw;
    private UserData user;
    private File qrCode;
    private String server;
    
    public MainController()
    {
        keyStorePw = "Jolk(/587!jk".toCharArray();
        user = new UserData(null, null, -1);
        qrCode = new File(System.getProperty("user.dir") + File.separator + "qrCode.jpeg");
        server = "https://benjomobsec.azurewebsites.net";
    }
    
    public boolean readUserData()
    {
        try
        {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("msKeyStore.jks"), keyStorePw);
            Key secret = ks.getKey(user.getPhoneNumber(), keyStorePw);
            user.setPrivateKey(secret.getEncoded());
            
            secret = ks.getKey(user.getPhoneNumber()+"-userid", keyStorePw);
            byte [] help = {0x00, secret.getEncoded()[0]}; //Should be done better!
            ByteBuffer byteBuffer = ByteBuffer.wrap(help);
            user.setMsgId(byteBuffer.getShort());
        }
        catch(Exception ex)
        {
            System.out.println("ReadUserData: " + ex.getMessage());
            return false;
        }
        
        return true;
    }
    
    public boolean writeUserData()
    {
        try
        {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("msKeyStore.jks"), keyStorePw);
            SecretKey secretKey = new SecretKeySpec(user.getPrivateKey(), 0, user.getPrivateKey().length, "AES");
            KeyStore.SecretKeyEntry secret = new KeyStore.SecretKeyEntry(secretKey);
            KeyStore.ProtectionParameter password = new KeyStore.PasswordProtection(keyStorePw);
            ks.setEntry(user.getPhoneNumber(), secret, password);
            
            BigInteger bigInt = BigInteger.valueOf(user.getMsgId());
            secretKey = new SecretKeySpec(bigInt.toByteArray(), 0, bigInt.toByteArray().length, "AES");
            secret = new KeyStore.SecretKeyEntry(secretKey);
            ks.setEntry(user.getPhoneNumber()+"-userid", secret, password);
            FileOutputStream fos = new FileOutputStream("msKeyStore.jks");
            ks.store(fos, keyStorePw);
        }
        catch(KeyStoreException ex)
        {
            try
            {
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(null, keyStorePw);
                FileOutputStream fos = new FileOutputStream("msKeyStore.jks");
                ks.store(fos, keyStorePw);
            }
            catch(Exception ex1)
            {
                System.out.println("WriteUserData: " + ex1.getMessage());
                return false;
            }
        }
        catch(FileNotFoundException ex)
        {
            System.out.println("WriteUserData: " + ex.getMessage());
            return false;
        }
        catch(IOException ex)
        {
            System.out.println("WriteUserData: " + ex.getMessage());
            return false;
        }
        catch(NoSuchAlgorithmException ex)
        {
            System.out.println("WriteUserData: " + ex.getMessage());
            return false;
        }
        catch(CertificateException ex)
        {
            System.out.println("WriteUserData: " + ex.getMessage());
            return false;
        }
        
        return true;
    }

    public UserData getUser() {
        return user;
    }

    public File getQrCode() {
        return qrCode;
    }

    public String getServer() {
        return server;
    }

    
    
    public void createNewPrivateKey()
    {
        SecureRandom sr = new SecureRandom();
        
        byte key [] = new byte[16];
        sr.nextBytes(key);
        
        user.setPrivateKey(key);
        user.setMsgId(0);
    }
    
    public boolean createQRCode()
    {
        try 
        {
            String str = Base64.getEncoder().encodeToString(user.getPrivateKey());
            BitMatrix matrix = new MultiFormatWriter().encode(new String(str.getBytes(), StandardCharsets.UTF_8), BarcodeFormat.QR_CODE, 400, 400);
            MatrixToImageWriter.writeToFile(matrix, "JPEG", qrCode);
        } 
        catch (WriterException ex) 
        {
            System.out.println("CreateQrCode: " + ex.getMessage());
            return false;
        } 
        catch (IOException ex) 
        {
            System.out.println("CreateQrCode: " + ex.getMessage());
            return false;
        }
        return true;
    }
    
    public String decryptMessage(String message)
    {
        try
        {
            byte [] ciText = Base64.getDecoder().decode(message.split("\"")[1].getBytes());
            byte [] iv = new byte[16];
            byte ciphertext[];
            byte encKey [];
            byte authKey [];
            SecretKeySpec macKey;
            Mac hmac = Mac.getInstance("HmacSHA256");
            byte mac [];
            Cipher cipher;
            byte pt [];

            ByteBuffer buffer = ByteBuffer.wrap(ciText);

            int lengthIV = buffer.get();
            if(lengthIV != 16)
            {
                System.out.println("Invalid IV length");
                return null;
            }

            iv = new byte [lengthIV];
            buffer.get(iv);

            int lengthMAC = buffer.get();
            if(lengthMAC != 32)
            {
                System.out.println("Invalid MAC length");
                return null;
            }

            byte encMAc [] = new byte[lengthMAC];
            buffer.get(encMAc);

            ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            encKey = HKDF.fromHmacSha256().expand(user.getPrivateKey(), "encKey".getBytes(StandardCharsets.UTF_8), 16);
            authKey = HKDF.fromHmacSha256().expand(user.getPrivateKey(), "authKey".getBytes(StandardCharsets.UTF_8), 32);
            macKey = new SecretKeySpec(authKey, "HmacSHA256");
            hmac = Mac.getInstance("HmacSHA256");

            hmac.init(macKey);
            hmac.update(iv);
            hmac.update(ciphertext);

            mac = hmac.doFinal();

            if(!MessageDigest.isEqual(mac, encMAc))
            {
                throw new Exception("Message could net be Authenticated!");
            }

            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encKey, "AES"), new IvParameterSpec(iv));

            pt = cipher.doFinal(ciphertext);
            
            user.setMsgId(user.getMsgId()+1);
            this.updateKey();
            
            return new String(pt);
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
            return null;
        }
    }
    
    public void updateKey()
    {
        //byte [] newKey = HKDF.fromHmacSha256().extractAndExpand(new byte [0], user.getPrivateKey(), null, 16);
        //user.setPrivateKey(newKey);
    }
    
    public static void main(String[] args) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException 
    {
        //Initaially create the KeyStore:
        //KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        //char [] pwdArray = "Jolk(/587!jk".toCharArray();
        //ks.load(null, pwdArray);
        //FileOutputStream fos = new FileOutputStream("msKeyStore.jks");
        //ks.store(fos, pwdArray);
    }
    
}
