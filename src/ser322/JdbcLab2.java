package ser322; 


//Imports
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


/**
 * JDBCLab Activity 2 pt 2
 * @author Gene H. Li - ghli1
 * 
 * Command line Java program that executes XPATH query to find Description attribute from PRODUCT table based on a Department number.
 * For successful query, an .xml file must be present within project directory.
 * Can query multiple .xml files, as long as they are all within the same level in the directory. 
 */

public class JdbcLab2 {
    public static void main(String[] args) {
        
        try {
            
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            DocumentBuilder b = f.newDocumentBuilder();
            
            //Flag to check for whether xml file exists
            boolean flag = false; 
            
            //Get project directory folder and files within directory
            File folder = new File("./");
            File[] lis = folder.listFiles();
            //Iterate to find and execute XPATH query on all files that end in the extension .xml
            for(int i = 0 ; i < lis.length ; i++){
                if (lis[i].getName().endsWith(".xml")){
                    flag = true; 
                    File fi = lis[i];
                    Document doc = b.parse(fi);
                    XPathFactory xpf = XPathFactory.newInstance();
                    XPath xp = xpf.newXPath();
                    XPath xp1 = xpf.newXPath();
                    
                    //Queries for description based on dept number
                    XPathExpression ex = xp.compile("//PRODUCT[./MADE_BY/text()='"+args[0]+"']/DESCRIP");

                    //Queries for the dept name for console output
                    XPathExpression ex2 = xp1.compile("//DEPT[@DEPTNO='"+args[0]+"']/DNAME/text()");
                    NodeList n1 = (NodeList) ex.evaluate(doc, XPathConstants.NODESET);

                    String dept = ex2.evaluate(doc);

                    if (dept.equals("")){
                        System.out.println("Please ensure department number was entered correctly and/or is valid");
                    }
                    else{

                        System.out.println("Products found in the "+dept+" Department in "+lis[i].getName());
                        for(int j = 0; j < n1.getLength() ; j++){
                            
                            System.out.println("  - "+n1.item(j).getTextContent());
                            
                        }
                    }
                }
            }

            if (!flag){
                System.out.println("XML file not found in directory! Please execute the export statement from JdbcLab.java first!");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }



    }
}