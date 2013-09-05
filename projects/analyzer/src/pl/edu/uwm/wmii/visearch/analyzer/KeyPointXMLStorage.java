package pl.edu.uwm.wmii.visearch.analyzer;
/**
 * @author ksopyla
 * 
 * 
 */

import java.io.File;
import java.nio.file.Files;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.KeyPoint;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Saves the keypoints and its descriptrors in XML file.
 * File structure
 * 
 * <KeyPoints>
 * 	<SIFT> - Method
 * 		<kp size="" angle="" x="" y="" resolution="">
 * 			<desc>1,2,3,4....CSV vector</desc>
 * 		</kp>
 * 	</SIFT> 
 * </KeyPoints>
 * @author ksopyla
 */
public class KeyPointXMLStorage {

	

	public void save(MatOfKeyPoint keyPoints, Mat descriptors, String fileName, ExtractionMethod method) {
		// TODO Auto-generated method stub

		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();


			Document doc = docBuilder.newDocument();
			Element root = doc.createElement("keypoints");

			doc.appendChild(root);

			String methodName = method.toString(); 

			Element metEl = doc.createElement(methodName);
			
			
			List<KeyPoint> kpList = keyPoints.toList();
			
			int iter =0;
			for (KeyPoint keyPoint : kpList) {

				//create the keypoint node
				Element kpEl = doc.createElement("kp");
				
				//set the attribute
				kpEl.setAttribute("x", Num2Str(keyPoint.pt.x)); 
				kpEl.setAttribute("y", Num2Str(keyPoint.pt.y));
				
				kpEl.setAttribute("angle", Num2Str(keyPoint.angle));
				kpEl.setAttribute("size", Num2Str(keyPoint.size));
				kpEl.setAttribute("response", Num2Str(keyPoint.response));
				kpEl.setAttribute("octave", Num2Str(keyPoint.octave));
				
				//get the descriptor for this keypoint
				Mat row = descriptors.row(iter);
				iter++;
				String desCSV = ConvertToCSV(row);
				
				//create descriptor node, descriptor in CSV format
				Element desEl = doc.createElement("desc");
				desEl.setAttribute("dim", Num2Str(row.cols()));
				desEl.appendChild(doc.createTextNode(desCSV));
				
				//add descriptor to keypoint node
				kpEl.appendChild(desEl);
				
				//add whole keypoint to keypoint method collection
				metEl.appendChild(kpEl);
			}


			root.appendChild(metEl);


			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			
			File xmlFile = new File(fileName);
			
			File parentXmlFile = xmlFile.getParentFile();
			if(!parentXmlFile.exists())
			{
				parentXmlFile.mkdirs();
			}
			
			StreamResult result = new StreamResult(xmlFile);
			
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}

	}

	private String ConvertToCSV(Mat desc) {
		String t = desc.dump();
		t= t.substring(1, t.length()-1);		
		return t;
	}

	

	private String Num2Str(double num)
	{
		DecimalFormat threeDec = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
		return threeDec.format(num);
		
	}

	private String Num2Str(float num)
	{
		DecimalFormat threeDec = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
		return threeDec.format(num);
		
	}

}
