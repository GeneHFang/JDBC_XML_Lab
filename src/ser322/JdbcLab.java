
package ser322;

import java.sql.*;

/**
 * 
 */


public class JdbcLab {
    public static void main(String args[]){
        //The data
        ResultSet r = null;
        //The query 
        Statement s = null;
        PreparedStatement ps = null;
        //The socket connection
        Connection c = null;

        
        String _url, _user, _pwd, _driver;

        String tableBreak = "\n";
        for (int i = 0 ; i < 45 ; i++){
            tableBreak+="-";
        }

        _url = args[0];
        _user = args[1];
        _pwd = args[2];
        // _driver = args[3];
        

        try {
            // Class.forName(_driver);
            c = DriverManager.getConnection(_url, _user, _pwd);
            
            s = c.createStatement();
            
            String query=""; 

            switch(args[3]){
                case "query1":
                    query = "select empno, ename, dname from emp inner join dept on emp.deptno=dept.deptno;";
                    r = s.executeQuery(query);
             
                    System.out.format("%15s%20s%10s", "Employee ID|","Employee Name|"," Dept Name");

                    System.out.println(tableBreak);
                    while (r.next()){
                        System.out.format("%15s%20s%10s", r.getInt(1)+"|",r.getString(2)+"|"," "+r.getString(3));
                        System.out.println("");
                    }
                    break;
                
                case "query2":
                    query = "select dname, name, round(price*quantity,2) as amountspent from (customer join product on prodid=pid) join dept on made_by=deptno where dname=?;";
                    ps = c.prepareStatement(query);
                    ps.setString(1,args[4]);
                    r = ps.executeQuery();
                    System.out.format("%15s%20s%10s", "Dept Name|","Customer Name|"," Amount Spent");
                    System.out.println(tableBreak);
                    while (r.next()){
                        System.out.format("%15s%20s%10s", r.getString(1)+"|",r.getString(2)+"|"," "+r.getFloat(3));
                        System.out.println("");
                        // System.out.println(r.getString(1)+" | "+r.getString(2)+" | "+r.getFloat(3));
                    }
                    break;
                
                case "dml1":
                    query = "insert into customer values (?, ?, ? ,?);";
                    ps = c.prepareStatement(query);
                    ps.setInt(1,Integer.parseInt(args[4]));
                    ps.setInt(2,Integer.parseInt(args[5]));
                    ps.setString(3,args[6]);
                    ps.setInt(4, Integer.parseInt(args[7]));
                    try{
                        ps.executeUpdate();
                        System.out.println("SUCCESS");
                    }
                    catch (SQLException insertexception) {
                        insertexception.printStackTrace();
                    }
                    
                    
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try {
                if (r!=null) r.close();
                if (s!=null) s.close();
                if (c!=null) c.close();
            }
            catch(SQLException se){
                se.printStackTrace();
            }
        }
    }

    /*Debugging */
    // static void print(String args) {
    //     System.out.println("Connection Established; Command " +args);
    // }
}