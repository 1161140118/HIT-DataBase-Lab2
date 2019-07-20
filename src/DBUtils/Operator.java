/**
 * 
 */
package DBUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.Statement;

/**
 * @author chen
 *
 */
public class Operator {
    public static final String q1 = "参加了项目编号为%PNO%的项目的员工号";
    public static final String q2 = "参加了项目名为%PNAME%的员工名字";
    public static final String q3 = "在%DNAME%工作的所有工作人员的名字和地址";
    public static final String q4 = "在%DNAME%工作且工资低于%SALARY%元的员工名字和地址";
    public static final String q5 = "没有参加项目编号为%PNO%的项目的员工姓名";
    public static final String q6 = "由%ENAME%领导的工作人员的姓名和所在部门的名字";
    public static final String q7 = "至少参加了项目编号为%PNO1%和%PNO2%的项目的员工号";
    public static final String q8 = "员工平均工资低于%SALARY%元的部门名称";
    public static final String q9 = "至少参与了%N%个项目且工作总时间不超过%HOURS%小时的员工名字";
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * 按行输出查询结果
     * @param resultSet
     * @throws SQLException
     */
    public static void printResult(ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = (ResultSetMetaData) resultSet.getMetaData();
        int col = resultSetMetaData.getColumnCount();
        System.out.println("--------------------------------------------------");
        for (int i = 0; i < col; i++) {
            System.out.print(resultSetMetaData.getColumnName(i+1) + "\t");
        }
        System.out.println();
        while (resultSet.next()) {
            for (int i = 0; i < col; i++) {
                System.out.print(resultSet.getString(i + 1) + "\t");
            }
            System.out.println();
        }
        System.out.println("--------------------------------------------------");
    }
    
    /**
     * 打印查询菜单并获得选项
     * @return  查询编号
     */
    public static int queryMenu() {
        Class<Operator> classop = Operator.class;
        for (int i=1;i<=9;i++) {
            String q = "q"+i;
            Field field;
            try { // 通过反射机制访问静态查询语句
                field = classop.getField(q);
                System.out.println(i+" : "+field.get(null));
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                System.err.println("Cann't get field "+q+" !");
                continue;
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        System.out.println("输入数字选择查询语句...（输入其他数字退出）");
        String string = scanner.nextLine();
        // 输入检测
        while(!string.matches("\\d+")) {
            System.err.println("输入非法！");
            string = scanner.nextLine();
        }
        int result = Integer.valueOf(string);
        if (result<0 || result >9) {
            result = 0;
        }
        return result;
    }
    
    private static String[] getParameters(int i)  {
        try {
            System.out.println("查询： "+ Operator.class.getField("q"+i).get(null));
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                | SecurityException e) {
            e.printStackTrace();
        }
        String[] strings;
        do {
            System.out.println("输入两个参数，以空格分隔：");
             strings=scanner.nextLine().split(" ");
        } while (strings.length!=2);
        return strings;
    }
    
    private static String getParameter(int i) {
        try {
            System.out.println("查询： "+ Operator.class.getField("q"+i).get(null));
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                | SecurityException e) {
            e.printStackTrace();
        }
        System.out.println("输入唯一参数：");
        String string="";
        while( string.isEmpty()) {
            string = scanner.nextLine();
        }
        return string;
    }
    
    public static String queryer(int i) {
        String result = "";
        String[] para;
        switch (i) {
            case 1:
                result =  "select ESSN from WORKS_ON where PNO=\""+getParameter(i)+"\"";
                break;
                
            case 2:
                result = "select ENAME from EMPLOYEE natural join WORKS_ON "
                        + "where PNO="
                        + "(select PNO from PROJECT where PNAME=\""+getParameter(i)+"\")";
                break;
            
            case 3:
                result = "select ENAME,ADDRESS from EMPLOYEE where "
                        + "DNO=(select DNO from DEPARTMENT where DNAME=\""+getParameter(i)+"\")";
                break;

            case 4:
                para = getParameters(i);
                result = "select ENAME,ADDRESS from EMPLOYEE natural join DEPARTMENT "
                        + "where DNAME=\""+para[0]+"\" and SALARY <"+para[1]+" " ;
                break;

            case 5:
                result = "select ENAME from EMPLOYEE "
                        + "where  ESSN not in "
                        + "(select ESSN from WORKS_ON where PNO=\""+getParameter(i)+"\")";
                break;

            case 6:
                result = "SELECT ENAME,DNAME " + 
                        "FROM EMPLOYEE NATURAL JOIN DEPARTMENT " + 
                        "WHERE SUPERSSN=(SELECT ESSN FROM EMPLOYEE WHERE ENAME=\""+getParameter(i)+"\");";
                break;

            case 7:
                para = getParameters(i);
                result = "SELECT W1.ESSN " + 
                        "FROM WORKS_ON AS W1, WORKS_ON AS W2 " + 
                        "WHERE W1.PNO=\""+para[0]+"\" AND W2.PNO=\""+para[1]+"\" AND W1.ESSN=W2.ESSN;";
                break;

            case 8:
                result = "select DNAME from DEPARTMENT where DNO in"
                        + "(select DNO from EMPLOYEE "
                        + "group by DNO having avg(SALARY)<"+getParameter(i)+")";
                break;

            case 9:
                para = getParameters(i);
                result = "SELECT ENAME " + 
                        "FROM EMPLOYEE NATURAL JOIN WORKS_ON " + 
                        "GROUP BY ENAME " + 
                        "HAVING COUNT(*)>="+para[0]+" AND SUM(HOURS)<="+para[1];
                break;
                
            default:
                System.err.println("Opt number error!");
                break;
        }
        
        System.out.println("查询已发送，请稍等！");
        return result;
    }
    

    public static void executor() {
        try {
            Connection connection = Connector.getConnection();
            Statement stat = (Statement) connection.createStatement();
            // 选项
            int opt=0;
            while( (opt=queryMenu())!=0 ) {
                String sql = queryer(opt);
                double time = System.currentTimeMillis();
                // 执行查询
                ResultSet rst = stat.executeQuery(sql);
                printResult(rst);
                System.out.println("查询完成！ 用时 "+ (System.currentTimeMillis()-time) +" ms.\n");
            }
            System.out.println("Bye!");
            connection.close();
            System.out.println("连接已断开！");
            stat.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        executor();
    }

}
