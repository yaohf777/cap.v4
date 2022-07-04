package com.sap.rc.main.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JwtGenerator {
    
    private static final String IDENTITY_ZONE_ID = "a09a3440-1da8-4082-a89c-3cce186a9b6c";
    private static final String CLIENT_ID = "testClient!t27";

    // return a value suitable for the HTTP "Authorization" header containing the JWT with the given scopes
    public String getTokenForAuthorizationHeader(String... scopes) {
        return "Bearer " + getToken(scopes);
    }

    // return the JWT for the given scopes
    private String getToken(String... scopes) {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode root = mapper.createObjectNode();
        root.put("client_id", CLIENT_ID);
        root.put("exp", Integer.MAX_VALUE);
        root.set("scope", getScopesJSON(scopes));
        root.put("user_name", "user name");
        root.put("user_id", "user_id");
        root.put("email", "testUser@testOrg");
        root.put("zid", IDENTITY_ZONE_ID);

        return getTokenForClaims(root.toString());
    }

    // sign the claims and return the resulting JWT
    private String getTokenForClaims(String claims) {
        RsaSigner signer = new RsaSigner(readFromFile("/privateKey.txt"));
        return JwtHelper.encode(claims, signer).getEncoded();
    }
    
    public String getPublicKey() {
        String publicKey = readFromFile("/publicKey.txt");
        return removeLinebreaks(publicKey);
    }

    private String removeLinebreaks(String input) {
        return input.replace("\n", "").replace("\r", "");
    }

    private String readFromFile(String path) {
        
        try ( InputStream is = getClass().getResourceAsStream(path); ) {
            return IOUtils.toString(is, Charset.defaultCharset());
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    // convert Java array into JSON array
    private ArrayNode getScopesJSON(String[] scopes) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode scopesArray = mapper.createArrayNode();
        for (String scope : scopes) {
            scopesArray.add(scope);
        }
        return scopesArray;
    }
    
    public String getClientId() {
        return CLIENT_ID;
    }

    public String getIdentityZone() {
        return IDENTITY_ZONE_ID;
    }
}