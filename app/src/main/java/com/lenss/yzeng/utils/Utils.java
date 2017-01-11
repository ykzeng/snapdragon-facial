package com.lenss.yzeng.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by yukun on 1/10/2017.
 */

public class Utils {
    public static byte[] serialize(Object obj) throws IOException {
        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream b = new ByteArrayInputStream(bytes)){
            try(ObjectInputStream o = new ObjectInputStream(b)){
                return o.readObject();
            }
        }
    }

    public static void writeMapToFile(Map map, String filePath){
        File file = new File(filePath);
        if (file.exists())
            file.delete();
        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write("Bytes\t Occurence\n");
            Iterator itr = map.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry entry = (Map.Entry) (itr.next());
                writer.write(entry.getKey() + "\t " + entry.getValue() + "\n");
                writer.flush();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
