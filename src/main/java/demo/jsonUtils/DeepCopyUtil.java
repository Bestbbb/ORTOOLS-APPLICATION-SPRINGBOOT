package demo.jsonUtils;

import java.io.*;
import java.util.List;

public class DeepCopyUtil {

    /**
     * List深拷贝 条件:需要model实现Serializable
     */
    public static <T> List<T> deepCopy(List<T> srcList)  {
        List<T> desList = null;
        ByteArrayOutputStream baos=null;
        ObjectOutputStream oos=null;
        ByteArrayInputStream bais=null;
        ObjectInputStream ois=null;
        try {
            //序列化
            baos= new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(srcList);

            //反序列化
            bais = new ByteArrayInputStream(baos.toByteArray());
            ois = new ObjectInputStream(bais);
            desList = (List<T>) ois.readObject();
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bais.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return desList;
    }
}