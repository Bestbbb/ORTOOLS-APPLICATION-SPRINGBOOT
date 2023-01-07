package demo.jsonUtils;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import demo.domain.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class LoadFile {


    private static final Logger LOGGER = LoggerFactory.getLogger(LoadFile.class);
    public static void writeJsonFile(Object output, String path) {
        String jsonString = convertObjectToJSON(output); // job转string
        write(jsonString, path);
    }

    public static String convertObjectToJSON(Object object) {
        return JSON.toJSONString(object);
    }

    public static void write(String str, String path) {
        try {
            BufferedWriter writer = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (path),"UTF-8"));
            writer.write("");
            writer.write(str);
            writer.flush();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try (FileWriter writer = new FileWriter(path)) {
//            writer.write("");//清空原文件内容
//            writer.write(str);
//            writer.flush();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }
    public static void saveJson(String filePath) {
        String writeString = JSON.toJSONString(SerializerFeature.PrettyFormat);
        LOGGER.info(writeString);
        BufferedWriter writer = null;
        File file = new File(filePath);
        //如果文件不存在则新建
        if (!file.exists()) {
            try {
                boolean newFile = file.createNewFile();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
        //如果多次执行同一个流程，会导致json文件内容不断追加，在写入之前清空文件
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8));
            writer.write("");
            writer.write(writeString);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    // 用于读取JSON文件
    public static Input readJsonFile(String filePath) {
        System.out.println(filePath);
        StringBuilder readJson = new StringBuilder();
        try (InputStreamReader inputStreamReader = new InputStreamReader(
                Objects.requireNonNull(new FileInputStream(filePath)), StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {

            String tempString;
            while ((tempString = reader.readLine()) != null) {
                readJson.append(tempString);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        // 获取json
        Input jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(readJson.toString(), Input.class);
//            System.out.println(jsonObject.getResourcePool());
//            JSONArray objects = JSONArray.parseArray(jsonObject.getResourcePool());
//            List<JSONObject> jsonObjects = objects.toJavaList(JSONObject.class);
//            jsonObjects.forEach(i->System.out.println(i.get("id")));


//            LOGGER.info(JSON.toJSONString(jsonObject));
        } catch (JSONException e) {
            LOGGER.error(e.getMessage());
        }
        return jsonObject;
    }

    public static void main(String[] args) {
        saveJson("/22.json");
//        System.out.println(LoadFile.readJsonFile("json/input_1.json"));
    }


}


