package com.fulan.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;

public class JsonTest {
    public static void main(String[] args) {
        String path = JsonTest.class.getClassLoader ().getResource ("menu.json").getPath ();
        String s = readJsonFile (path);
        JSONObject json = JSON.parseObject (s);
        JSONArray btnList = json.getJSONArray ("btnList");
        System.out.println ("btnList");
        for(int i=0;i<btnList.size ();i++){
            JSONObject btnObject=btnList.getJSONObject (i);
            String name=(String)btnObject.get ("name");
            System.out.println (name);
            JSONArray childrenList = btnObject.getJSONArray ("children");
            System.out.println ("children");
            for(int j=0;j<childrenList.size ();j++){
                JSONObject childrenObject = childrenList.getJSONObject (i);
                String id=(String)childrenObject.get ("id");
                String childreName=(String)childrenObject.get ("name");
                System.out.println ("id:"+id);
                System.out.println ("name:"+childreName);
            }
        }
    }
    /**
     * 读取json文件，返回json串
     */
    public static String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            File jsonFile = new File (fileName);
            Reader reader = new InputStreamReader (new FileInputStream (jsonFile), "utf-8");
//            int ch = 0;
//            StringBuffer sb = new StringBuffer ();
//            while ((ch = reader.read ()) != -1) {
//                sb.append ((char) ch);
//            }
            BufferedReader bf=new BufferedReader (reader);
            String line=null;
            StringBuffer sb=new StringBuffer ();
            while ((line=bf.readLine ())!=null){
                sb.append (line);
            }
            reader.close ();
            jsonStr = sb.toString ();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace ();
            return null;
        }
    }
}
