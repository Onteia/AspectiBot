package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.management.openmbean.KeyAlreadyExistsException;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {

    public static String get(String key, String jsonPath) throws JSONException, IOException {
        String response = readJSON(jsonPath).getString(key);
        return response;
    }
    
    public static void add(String key, String value, String jsonPath) throws IOException, KeyAlreadyExistsException {    
        JSONObject json = readJSON(jsonPath);
        if(json.has(key)) {
            throw new KeyAlreadyExistsException();
        }
        json.put(key, value);
        writeJSON(json, jsonPath);
    }
    
    public static void delete(String key, String jsonPath) throws IOException {
        JSONObject json = readJSON(jsonPath);
        Object result = json.remove(key);
        if(result != null)
            writeJSON(json, jsonPath);
    }
    
    public static void edit(String key, String value, String jsonPath) throws IOException {
        delete(key, jsonPath);
        add(key, value, jsonPath);
    }
    
    private static JSONObject readJSON(String jsonPath) throws IOException {
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonPath)));
        return new JSONObject(jsonString);
    }
    
    private static void writeJSON(JSONObject json, String jsonPath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(jsonPath));
        writer.write(json.toString());
        writer.close();
    }
    
}
