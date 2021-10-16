package com.example.springsocial;

import com.core.cryptolib.forms.FirmwareRequestForm;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.CRC32;
import org.bouncycastle.util.encoders.Hex;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class SpringSocialApplicationTests {

    @Test
    public void contextLoads() throws ParseException, FileNotFoundException, IOException {
//        String str = "{\"trustedDevicePrivateId\":\"AAAAAAAGGYA=\",\"firmware\":{\"offset\":23234,\"version\":\"0.1\",\"status\":2}}";
//
//        JSONParser parser = new JSONParser();
//
//        JSONObject obj = (JSONObject) parser.parse(str);
//
//        System.out.println("First=>" + (JSONObject) obj.get("firmware"));
//
//        JSONObject object = (JSONObject) obj.get("firmware");
//        String version = (String) object.get("version");
//        long status = (Long) object.get("status");
//        long offset = (long) object.get("offset");
//        /// FirmwareRequestForm firmwareRequestForm = new FirmwareRequestForm((JSONObject)obj.get("firmware"));
//
//    
//         System.out.println("version=>" + version);
//         System.out.println("status=>" + status);
//         System.out.println("offset=>" + offset);

//        long fileSize = 0l;
//        long checkSum = 0l;
//        long offset = 0l;
//
//        int size = 0;
//
//        String filePart = "";
//
//        File file = new File("D:\\var\\log\\tomcat9\\123.hex");
//
//        fileSize = file.length();
//        checkSum = 0l;
//        offset = 0;
//
//        FileInputStream fis = new FileInputStream(file);
//        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
//
//        long sum = 0;
//
//        br.skip(offset);
//        while (true) {
//
//            int a = br.read();
//
//            if (a == -1) {
//                System.out.println(filePart);
//                break;
//            }
//
//            char c = (char) a;
//            filePart = filePart
//                    .concat("" + c);
//
//            size += 1;
//
//            if (size >= 5120 && a == 10) {
//
//                System.out.println(filePart);
//                filePart = "";
//                offset += size;
//                size = 0;
//
//            }
//
//        }
//
//        fis.close();
//        
//        fis = new FileInputStream(file);
//        byte[] f = fis.readAllBytes();
//        System.out.println("len=>" + f.length);
//
//        CRC32 fileCRC32 = new CRC32();
//        fileCRC32.update(f);
//
//        checkSum = fileCRC32.getValue();
//        
//        fis.close();
//
//        System.out.println("checksumm=" + checkSum);
    }

}
