package org.mindroid.implementationFinder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class ImplementationFinder {
    private static String basePath = "..\\androidApp\\app\\src\\main\\java\\org\\mindroid\\android\\app\\programs\\";
    private static final JSONObject json = new JSONObject();

    private static String[] classesSolutions;
    private static String[] classesStubs;
    private static String[] classesDev;
    private static String[] classesStatemachine;

    public static void main(String[] args) throws IOException {
        File[] dev = getDevSetsList();
        File[] workshop = getWorkshopSetList();

        for (File file : dev) {
            getImplementations(file.getPath(),"DEV_"+file.getName());
        }

        for (File file : workshop) {
            getImplementations(file.getPath(),"WORKSHOP_"+file.getName());
        }

        String jsonString = json.toJSONString();
        //System.out.println("JSON String: " + jsonString);

        FileWriter outFile = new FileWriter("programs.json");
        try {
            outFile.write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            outFile.flush();
            outFile.close();
        }
        //testParse();

    }

    private static File[] getDevSetsList(){
        File folder = new File(basePath + "dev");
        return getSubDirsToStringArray(folder);
    }

    private static File[] getWorkshopSetList(){
        File folder = new File(basePath + "workshop");
        return getSubDirsToStringArray(folder);
    }

    private static File[] getSubDirsToStringArray(File folder){
        File[] listOfFiles = folder.listFiles();
        ArrayList<File> listOfDirs = new ArrayList<File>(listOfFiles.length);
        for (File file : listOfFiles) {
            if(file.isDirectory()){
                listOfDirs.add(file);
            }
        }
        return listOfDirs.toArray(new File[listOfDirs.size()]);
    }

    private static void getImplementations(String path, String setName) {
        // build correct Path
        File folder = new File(path);
        // get list of Files
        File[] files = folder.listFiles();
        JSONArray classes = new JSONArray();
        if (files!=null) {
            for (int i = 0; i < files.length; i++) {
                // only use if it is a file
                System.out.println(files[i].getName());
                if (files[i].isFile() && !files[i].getName().contains("~ava")) {
                    // split path by 'java', second part is important, replace backslash with dot
                    String className = String.valueOf(files[i].toPath()).split("java")[1].replace('\\', '.');
                    // remove leading and trailing dots
                    className = className.substring(1, className.length() - 1);
                    // write to array
                    classes.add(className);
                }
            }
            // put array to JSON
            json.put(setName, classes);
            //System.out.println(json.toJSONString());
        }
    }

    @Deprecated
    private static void testParse(){
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) parser.parse(new FileReader("programs.json"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        classesStatemachine = parseStringArray("classesStatemachine", jsonObject);
        classesSolutions = parseStringArray("classesSolutions", jsonObject);
        classesStubs = parseStringArray("classesStubs", jsonObject);
        classesDev = parseStringArray("classesDev", jsonObject);
    }

    private static String[] parseStringArray(String key, JSONObject jsonObject){
        JSONArray classesJson = (JSONArray) jsonObject.get(key);
        String[] classNames = new String[classesJson.size()];
        for (int i = 0; i < classNames.length; i++) {
            classNames[i] = (String) classesJson.get(i);
        }
        //System.out.println("classes:" + Arrays.toString(classNames));
        return classNames;
    }
}
