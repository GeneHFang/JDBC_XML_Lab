
package ser322;

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
 * 
 */


public class JdbcLab {
    public static void main(String args[]){
        //The data
        ResultSet r = null, r1 = null, r2 = null;
        //The query 
        Statement s = null, s1 = null, s2 = null;
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
        _driver = args[3];
        

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
                    query = "select dname, name, round(price*quantity,2) as amountspent from (customer join product on prodid=pid) join dept on made_by=deptno where dname=?;";
                    ps = c.prepareStatement(query);
                    ps.setString(1,args[5]);
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

                case "export":

                    try {
                        DocumentBuilderFactory docf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder docb = docf.newDocumentBuilder();

                        //Root
                        Document doc = docb.newDocument();
                        Element root = doc.createElement("DeptStore");
                        doc.appendChild(root);
                        
                        query = "show tables;";
                        r = s.executeQuery(query);
                        
                        
                        System.out.println("here in export");
                        
                        
                        String subQuery = "";
                        String subQuery2 = "";
                        while (r.next()){
                            
                        System.out.println("here before making tables");
                        
                        // tables.add(r.getString(1));
                        String table = r.getString(1).toUpperCase();
                        System.out.println(table);
                            String tablePlural = table+"S";
                            Element tableElement = doc.createElement(tablePlural);
                            root.appendChild(tableElement);
                            subQuery = "show columns from ";
                            s1 = c.createStatement();
                            r1 = s1.executeQuery(subQuery+table+";");
                            // ps = c.prepareStatement(subQuery);
                            // ps.setString(1,table);
                            ArrayList<String> columns = new ArrayList<>();

                            // r1 = ps.executeQuery();
                            
                            
                        System.out.println("here before making columns");
                            while (r1.next()){
                                columns.add(r1.getString(1));
                            }
                            int columnNum = columns.size();

                            subQuery2 = "select * from ";
                            s2 = c.createStatement();
                            r2 = s2.executeQuery(subQuery2+table+";");
                            // ps1 = c.prepareStatement(subQuery2);
                            // ps1.setString(1, table);

                            // r2 = ps1.executeQuery();
                            
                            
                        System.out.println("here before making elements");
                            while (r2.next()){
                                Element row = doc.createElement(table);
                                tableElement.appendChild(row);
                                row.setAttribute(columns.get(0), r2.getString(1));
                                for(int i = 1; i < columnNum ; i++){
                                    Element attribute = doc.createElement(columns.get(i));
                                    if (r2.getString(i+1) == null) {
                                        attribute.appendChild(doc.createTextNode("NULL"));
                                    }
                                    else {
                                        attribute.appendChild(doc.createTextNode(r2.getString(i+1)));
                                    }

                                    row.appendChild(attribute);
                                }
                            }
                        System.out.println("here after one table done");
                        }
                        System.out.println("here after db is done");
                        TransformerFactory hasbro = TransformerFactory.newInstance();
                        Transformer starScream = hasbro.newTransformer();
                        
                        starScream.setOutputProperty(OutputKeys.INDENT, "yes");
                        starScream.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

                        DOMSource src = new DOMSource(doc);
                        String filename = args[5]+".xml";
                        // System.out.println(doc.getFirstChild().getFirstChild().getFirstChild().getFirstChild().getFirstChild().getTextContent());

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

    /*Debugging */
    // static void print(String args) {
    //     System.out.println("Connection Established; Command " +args);
    // }
}