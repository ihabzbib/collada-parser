import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ColladaParser {


	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.print("Enter filename: ");
		InputStreamReader in = new InputStreamReader(System.in);
		BufferedReader reader;
		reader = new BufferedReader(in);

		try {
			String input = reader.readLine();
			File file = new File(input);

			if (!file.exists()) {
				System.out.println("File doesn't exist.");
				System.exit(0);
			}
			
			ArrayList<Object[]> vertices = new ArrayList<Object[]>(); 
			ArrayList<Object[]> normals = new ArrayList<Object[]>(); 
			ArrayList<Object[]> faces = new ArrayList<Object[]>();
			
			
			parse(file, vertices, normals, faces);
			String[] tokens = file.getName().split("\\.");
			String extension = "dae";
			String filename = file.getName();
			if (tokens.length > 1) {
				extension = tokens[tokens.length - 1];
				filename = tokens[tokens.length - 2];
			}
			
			
			FileWriter fstream = new FileWriter(filename + ".txt");
			BufferedWriter out = new BufferedWriter(fstream);
			print(out, vertices, normals, faces );
			out.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private static void print(BufferedWriter out , ArrayList<Object[]> vertices , ArrayList<Object[]> normals , ArrayList<Object[]> faces ) throws IOException{
		write(out, vertices.size() + " " + normals.size() + " " + faces.size() + "\n");
		
		//Write vertices
		for (Object[] v : vertices) {
			for (int i = 0; i<v.length ; i++) {
				write(out, v[i] + (i == v.length - 1 ? "\n" : " "));
			}
			
		}
		
		//Write normals
		for (Object[] n : normals) {
			for (int i = 0; i<n.length ; i++) {
				write(out, n[i] + (i == n.length - 1 ? "\n" : " "));
			}
		}
		
		//Write faces
		for (Object[] f : faces) {
			for (int i = 0; i<f.length ; i++) {
				write(out, f[i] + (i == f.length - 1 ? "\n" : " "));
			}
		}
		
		
	}
	
	private static void write(BufferedWriter out, Object obj) throws IOException{
		System.out.print(obj.toString());
		out.write(obj.toString());
	}
	
	private static void parse(File file, ArrayList<Object[]> vertices , ArrayList<Object[]> normals , ArrayList<Object[]> faces ){
		Document doc;
		XPath xpath;
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(false); // never forget this!
		
		DocumentBuilder builder;
		try {
			builder = domFactory.newDocumentBuilder();

			doc = builder.parse(file);

			XPathFactory factory = XPathFactory.newInstance();
			xpath = factory.newXPath();
			int ia = 0, ib, ic = 0, id;
			
			NodeList geometry = xpathParse("//geometry/@id", doc, xpath);
			for (int i = 0; i < geometry.getLength(); i++) {
				
				
				String obj = geometry.item(i).getNodeValue();

				
				//Vertices  Cube_001-mesh-positions-array
				Node positions = xpathParseNode("//float_array[@id='" + obj + "-positions-array']/text()", doc, xpath);
				String[] tokens = positions.getNodeValue().trim().split(" ");
				for(int j = 0; j< tokens.length; j += 3 ){
					
						vertices.add(new Object[]{
								tokens[j].trim(), // Read x 
								tokens[j+1].trim(), // Read y
								tokens[j+2].trim()  // Read z
						});
				}
				
				//Normals  Cube_001-mesh-normals-array
				Node norm = xpathParseNode("//float_array[@id='" + obj + "-normals-array']/text()", doc, xpath);
				tokens = norm.getNodeValue().trim().split(" ");
				for(int j = 0; j< tokens.length; j += 3 ){
					
						normals.add(new Object[]{
								tokens[j].trim(), // Read x 
								tokens[j+1].trim(), // Read y
								tokens[j+2].trim()  // Read z
						});
				}
				
				
				Node vcount = xpathParseNode("//geometry[@id='" + obj + "']/mesh/polylist/vcount/text()", doc, xpath);
				String[] vTokens = vcount.getNodeValue().trim().split(" ");
				
				Node p = xpathParseNode("//geometry[@id='" + obj + "']/mesh/polylist/p/text()", doc, xpath);
				String[] pTokens =p.getNodeValue().trim().split(" ");
				
				int count =0;
				for(int k=0; k < vTokens.length; k++){
					int faceSize = new Integer(vTokens[k]).intValue();
					Object[] face = new Object [2*faceSize + 2];
					
					face[0] = faceSize;
					face[faceSize + 1] = "     ";
					for(int l = 0; l<faceSize; l++){
						face[l + 1] = pTokens[count++];
						face[l + 2 + faceSize] = pTokens[count++];
					}
					
					faces.add(face);
				}
			}

		} catch (Exception e){
			e.printStackTrace();
		}
			
	}

	
	private static Node xpathParseNode(String path, Document doc, XPath xpath) throws XPathExpressionException {
		XPathExpression expr = xpath.compile(path);
		Object result = expr.evaluate(doc, XPathConstants.NODE);
		Node node = (Node) result;
		return node;
	}
	
	private static NodeList xpathParse(String path, Document doc, XPath xpath) throws XPathExpressionException {

		XPathExpression expr = xpath.compile(path);

		Object result = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;

		return nodes;
	}
	
	
}
