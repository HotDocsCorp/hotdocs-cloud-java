package com.hotdocs.cloud;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import com.hotdocs.cloud.embedded.CreateSessionRequest;

public class Test {

    public static void main(String[] args)
            throws KeyManagementException, NoSuchAlgorithmException, InvalidKeyException,
            MalformedURLException, SignatureException, IOException, URISyntaxException {

        Client client = new Client("AutoTest", "SmVMfAceVIAtIrzA");
        CreateSessionRequest request = new CreateSessionRequest(client, "randomname4",
                "C:\\Users\\wilkinsk\\Documents\\HotDocs\\Templates\\EmploymentAgreement.hdpkg");
        String sessionId = request.send();
        
        System.out.println(sessionId);
    
    }

}
