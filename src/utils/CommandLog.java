package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONException;
import org.json.JSONObject;

import aspectibot.AspectiBot;

public class CommandLog {
    
    public static String get(String command) throws JSONException, IOException {
        String response = readJSON().getString(command);
        return response;
    }
    
    public static void add(String command, String log) throws IOException {    
        JSONObject json = readJSON();
        if(json.has(command))
            return;
        json.put(command, log);
        writeJSON(json);
    }
    
    public static void delete(String command) throws IOException {
        JSONObject json = readJSON();
        Object result = json.remove(command);
        if(result != null)
            writeJSON(json);
    }
    
    public static void edit(String command, String log) throws IOException {
        delete(command);
        add(command, log);
    }
    
    private static JSONObject readJSON() throws IOException {
        String jsonString = new String(Files.readAllBytes(Paths.get(AspectiBot.COMMAND_LOG_PATH)));
        return new JSONObject(jsonString);
    }
    
    private static void writeJSON(JSONObject json) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(AspectiBot.COMMAND_LOG_PATH));
        writer.write(json.toString());
        writer.close();
    }
}