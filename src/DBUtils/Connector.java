package DBUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author chen
 *
 */
public class Connector {
    
    private final String URL = "jdbc:mysql://192.168.199.244:3306/COMPANY?characterEncoding=utf8&useSSL=true";
    private final String NAME = "hadoop";
    private final String PASSWORD = "zhi";

    private static Connection conn = null;
    
    private Connector(){
        try {
            // 1.������������
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("JDBC���سɹ���");
            // 2.������ݿ������
            double time = System.currentTimeMillis();
            conn = DriverManager.getConnection(URL, NAME, PASSWORD);
            System.out.println("�ѻ������! ��ʱ "+(System.currentTimeMillis()-time)+" ms. \n");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static Connection getConnection() {
        if (conn==null) {
            new Connector();
        }
        return conn;
    }
    

}
