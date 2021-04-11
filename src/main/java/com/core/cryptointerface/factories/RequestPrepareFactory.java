/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.core.cryptointerface.factories;


import com.core.cryptointerface.components.Settings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author SAMS
 */
public class RequestPrepareFactory extends JSONObject {

    Settings settings;

    public RequestPrepareFactory(Settings settings) {
        this.settings = settings;
    }

    public JSONObject jsonGet(String url, Optional<String> apiVersion) throws IOException, MalformedURLException, ParseException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; utf-8");

        if (settings.isExist("serverOauthToken")) {

            boolean needOauth = Boolean.parseBoolean((String) settings.get("serverNeedOauth").getValue());
            String token = (String) settings.get("serverOauthToken").getValue();

            if (needOauth) {
                headers.put("Authorization", "Bearer " + token);
            }
        }
        if (apiVersion.isPresent()) {
            headers.put("X-API-VERSION", apiVersion.get());
        }
        headers.put("Accept", "application/json");

        return execute("GET", url,
                Optional.ofNullable(new JSONObject()),
                Optional.of(headers)
        );
    }

    public JSONObject jsonPost(String url, Optional<String> apiVersion, JSONObject params) throws IOException, MalformedURLException, ParseException {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; utf-8");
        if (settings.isExist("serverOauthToken")) {

            boolean needOauth = Boolean.parseBoolean((String) settings.get("serverNeedOauth").getValue());
            String token = (String) settings.get("serverOauthToken").getValue();

            if (needOauth) {
                headers.put("Authorization", "Bearer " + token);
            }
        }
        if (apiVersion.isPresent()) {
            headers.put("X-API-VERSION", apiVersion.get());
        }
        headers.put("Accept", "application/json");

        return execute("POST", url,
                Optional.of(params),
                Optional.of(headers)
        );
    }

    public JSONObject jsonPut(String url, Optional<String> apiVersion, JSONObject params) throws IOException, MalformedURLException, ParseException {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; utf-8");
        if (settings.isExist("serverOauthToken")) {

            boolean needOauth = Boolean.parseBoolean((String) settings.get("serverNeedOauth").getValue());
            String token = (String) settings.get("serverOauthToken").getValue();

            if (needOauth) {
                headers.put("Authorization", "Bearer " + token);
            }
        }
        if (apiVersion.isPresent()) {
            headers.put("X-API-VERSION", apiVersion.get());
        }
        headers.put("Accept", "application/json");

        return execute("PUT", url,
                Optional.of(params),
                Optional.of(headers)
        );
    }

    private JSONObject execute(String method, String urlName, Optional<JSONObject> params,
            Optional<Map<String, String>> headers) throws MalformedURLException, IOException, ParseException {

        URL url = new URL(urlName);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);

        if (headers.isPresent()) {

            for (Map.Entry<String, String> entry : headers.get().entrySet()) {

                con.setRequestProperty(entry.getKey(), entry.getValue());
            }

        }

        con.setDoOutput(true);

        if (params.isPresent()) {

            try ( OutputStream os = con.getOutputStream()) {
                byte[] input = params.get()
                        .toJSONString()
                        .getBytes();
                os.write(input, 0, input.length);
            }

        }

        con.getOutputStream().flush();
        con.getOutputStream().close();

        try ( BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {

                response.append(responseLine.trim());
            }

            con.getInputStream().close();
            con.disconnect();

            return (JSONObject) (new JSONParser())
                    .parse(response.toString());
        }

    }

}
