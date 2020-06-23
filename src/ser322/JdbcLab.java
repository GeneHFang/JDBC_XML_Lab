
package ser322;

//Imports
import java.sql.*;
import java.util.ArrayList;
import java.io.File;

import javax.print.attribute.standard.DocumentName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * JDBCLab Activity 1 & Activity 2 pt 1
 * @author Gene H. Li - ghli1
 * 
 * Command line Java program that executes:
 *  (from Activity 1):
 *          - a Select query that lists Employee IDs, Employee Names, together with their Dept names
 *          - a Select query that lists Department name, customer name, and total amount spent in purchase given a department number
 *          - an Insert query that adds a new Customer into the database through product purchase
 *  (from Activity 2):
 *          - an export of current database to an indented .xml file to the project directory 
 */


public class JdbcLab {
    public static void main(String args[]){
        //The data
        ResultSet r = null, r1 = null, r2 = null;
        //The query(s)
        Statement s = null, s1 = null, s2 = null;
        PreparedStatement ps = null;
        //The socket connection
        Connection c = null;
        
        String _url, _user, _pwd, _driver;
        _url = args[0];
        _user = args[1];
        _pwd = args[2];
        _driver = args[3];

        //Table line for console output
        String tableBreak = "\n";
        for (int i = 0 ; i < 45 ; i++){
            tableBreak+="-";
        }
        

        try {
            Class.forName(_driver);
            c = DriverManager.getConnection(_url, _user, _pwd);
            
            s = c.createStatement();
            
            String query=""; 

            switch(args[4]){
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
                    query = "select dname, name, round(price*quantity,2) as amountspent from (customer join product on prodid=pid) join dept on made_by=deptno where deptno=?;";
                    ps = c.prepareStatement(query);
                    ps.setString(1,args[5]);
                    r = ps.executeQuery();
                    System.out.format("%15s%20s%10s", "Dept Name|","Customer Name|"," Amount Spent");
                    System.out.println(tableBreak);
                    while (r.next()){
                        System.out.format("%15s%20s%10s", r.getString(1)+"|",r.getString(2)+"|"," "+r.getFloat(3));
                        System.out.println("");
                    }
                    break;
                
                case "dml1":
                    query = "insert into customer values (?, ?, ? ,?);";
                    ps = c.prepareStatement(query);
                    ps.setInt(1,Integer.parseInt(args[5]));
                    ps.setInt(2,Integer.parseInt(args[6]));
                    ps.setString(3,args[7]);
                    ps.setInt(4, Integer.parseInt(args[8]));
                    try{
                        ps.executeUpdate();
                        System.out.println("SUCCESS");
                    }
                    catch (SQLIntegrityConstraintViolationException integrityex) {
                        System.out.println(integrityex.getLocalizedMessage());
                        System.out.println("Please ensure you choose a UNIQUE ID for this customer or that the product ID corresponds to an existing product");
                    }
                    catch (NumberFormatException numberformat) {
                        System.out.println(numberformat.getLocalizedMessage());
                        System.out.println("Please ensure you follow the schema for customer. Values should be of type, in order, (INT INT STRING INT)");
                    }
                    break;

                case "export":

                    try {
                        DocumentBuilderFactory docf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder docb = docf.newDocumentBuilder();

                        //Root (hard coded as DeptStore)
                        Document doc = docb.newDocument();
                        Element root = doc.createElement("DeptStore");
                        doc.appendChild(root);
                        
                        query = "show tables;";
                        r = s.executeQuery(query);
                        
                        //debugging
                        // System.out.println("here in export");
                        
                        String subQuery = "";
                        String subQuery2 = "";
                        
                        //Iterate through list of table names, creating XML elements for each one
                        while (r.next()){
                            //debugging    
                            // System.out.println("here before making tables");
                        
                            String table = r.getString(1).toUpperCase();
                            String tablePlural = table+"S";
                            Element tableElement = doc.createElement(tablePlural);
                            root.appendChild(tableElement);
                            subQuery = "show columns from ";
                            
                            //Originally tried using a PreparedStatement, but had issues getting Table and Column names without extraneous '' character
                            s1 = c.createStatement();
                            r1 = s1.executeQuery(subQuery+table+";");
                            // ps = c.prepareStatement(subQuery);
                            // ps.setString(1,table);
                            // r1 = ps.executeQuery();
                            
                            //column names for xml export
                            ArrayList<String> columns = new ArrayList<>();
                            
                            //debugging    
                            // System.out.println("here before making columns");
                            
                            //Iterate through list of column names and placing them inside an ArrayList
                            while (r1.next()){
                                columns.add(r1.getString(1));
                            }
                            int columnNum = columns.size();

                            //Same problem as with line 149 with PreparedStatements
                            subQuery2 = "select * from ";
                            s2 = c.createStatement();
                            r2 = s2.executeQuery(subQuery2+table+";");
                            // ps1 = c.prepareStatement(subQuery2);
                            // ps1.setString(1, table);
                            // r2 = ps1.executeQuery();
                            
                            //debugging
                            // System.out.println("here before making elements");  

                            //Iterate through all rows of current table and add elements/attributes to XML table
                            while (r2.next()){
                                Element row = doc.createElement(table);
                                tableElement.appendChild(row);
                                row.setAttribute(columns.get(0), r2.getString(1));
                                for(int i = 1; i < columnNum ; i++){
                                    Element attribute = doc.createElement(columns.get(i));
                                    
                                    //null values changed to String "NULL" to avoid parsing errors when building XML file 
                                    if (r2.getString(i+1) == null) {
                                        attribute.appendChild(doc.createTextNode("NULL"));
                                    }
                                    else {
                                        attribute.appendChild(doc.createTextNode(r2.getString(i+1)));
                                    }

                                    row.appendChild(attribute);
                                }
                            }
                            //debugging
                            // System.out.println("here after one table done");
                        }
                        //debugging    
                        // System.out.println("here after db is done");

                        TransformerFactory hasbro = TransformerFactory.newInstance();
                        Transformer starScream = hasbro.newTransformer();
                        
                        //Indenting for readability
                        starScream.setOutputProperty(OutputKeys.INDENT, "yes");
                        starScream.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

                        DOMSource src = new DOMSource(doc);
                        String filename = args[5]+".xml";
                        StreamResult res = new StreamResult(new File(filename));
                        starScream.transform(src, res);

                        System.out.println("Success");
                    }
                    catch (ParserConfigurationException pce){
                        pce.printStackTrace();
                    }
                    catch (TransformerException tfe){
                        tfe.printStackTrace();
                    }
                    break;
            }
        }
        catch (ArrayIndexOutOfBoundsException aie){
            System.out.println("Incorrect number of arguments!");
            if (args[4].equals("query2")) {System.out.println("Please specify a department!");}
        }
        catch (SQLException sqe){
            System.out.println("DB access error!");
            System.out.println(sqe.getLocalizedMessage());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try {
                if (r!=null) r.close();
                if (r1!=null) r1.close();
                if (r2!=null) r2.close();
                if (s!=null) s.close();
                if (s1!=null) s1.close();
                if (s2!=null) s2.close();
                if (ps!=null) ps.close();
                if (c!=null) c.close();
            }
            catch(SQLException se){
                se.printStackTrace();
            }
        }
    }
}