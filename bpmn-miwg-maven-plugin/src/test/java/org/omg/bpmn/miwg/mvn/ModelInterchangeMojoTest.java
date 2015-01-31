package org.omg.bpmn.miwg.mvn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.model.Resource;
import org.junit.Before;
import org.junit.Test;
import org.omg.bpmn.miwg.util.HTMLAnalysisOutputWriter;
import org.omg.bpmn.miwg.util.TestUtil;
import org.omg.bpmn.miwg.xmlCompare.XmlCompareAnalysisTool;
import org.omg.bpmn.miwg.xpath.XPathAnalysisTool;
import org.omg.bpmn.miwg.xsd.XsdAnalysisTool;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ModelInterchangeMojoTest {

	private static final String W4_MODELER_ID = "W4 BPMN+ Composer V.9.0";
	private static final String CAMUNDA_MODELER_ID = "camunda Modeler 2.4.0";
	private static final String CAMUNDA_JS_ID = "camunda-bpmn.js c906a7c941b82dbb832ed9be62989c171c724199";
	private static final String ECLIPSE_MODELER_ID = "Eclipse BPMN2 Modeler 0.2.6";
	private static final String IBM_MODELER_ID = "IBM Process Designer 8.0.1";
	private static final String MID_MODELER_ID = "MID Innovator 11.5.2.30413";
	private static final String SIGNAVIO_MODELER_ID = "Signavio Process Editor 7.8.1";
	private static final String YAOQIANG_2_MODELER_ID = "Yaoqiang BPMN Editor 2.2.6";
	private static final String YAOQIANG_3_MODELER_ID = "Yaoqiang BPMN Editor 3.0.1";
	private static ModelInterchangeMojo mojo;
	private static XPathExpression testOverviewExpr;
	private static File overview;
	private static DocumentBuilder docBuilder;
    private XPath xPath;

	@Before
	public void setUp() throws Exception {
		TestUtil.prepareHTMLReportFolder(TestUtil.REPORT_BASE_FOLDER);

		mojo = new ModelInterchangeMojo();
		mojo.outputDirectory = new File(TestUtil.REPORT_BASE_FOLDER);
		mojo.resources = new ArrayList<Resource>();

		overview = HTMLAnalysisOutputWriter
				.getOverviewFile(mojo.outputDirectory);

		docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		XPathFactory xPathFactory = XPathFactory.newInstance();
        xPath = xPathFactory.newXPath();
		testOverviewExpr = xPath.compile("//div[@class=\"test\"]");
	}

	@Test
	public void testScanResourcesForAllBpmnFiles() {
		List<File> bpmnFiles = new ArrayList<File>();
		File dir = new File("src/test/resources");
		mojo.scanForBpmn(dir, bpmnFiles);
		// 15 = the total number of BPMN files in src/test/resources
		assertEquals(21, bpmnFiles.size());
	}

	@Test
	public void testScanResourcesForSpecificVendorBpmnFiles() {
		List<File> bpmnFiles = new ArrayList<File>();
		File dir = new File("src" + File.separator + "test" + File.separator
				+ "resources");
		mojo.scanForBpmn(dir, bpmnFiles, W4_MODELER_ID);
		assertEquals(9, bpmnFiles.size());
	}

	@Test
	public void testMojo() {
		try {
			Resource res = new Resource();
			res.setDirectory("src/test/resources");
			mojo.resources.add(res);
			mojo.execute();

			System.out
					.println("Checking expected output exists with base folder: "
							+ TestUtil.REPORT_BASE_FOLDER);

            // assert structure and content of overview file
			assertTrue(overview.exists());
			Document document = docBuilder.parse(overview);
			NodeList nodes = (NodeList) testOverviewExpr.evaluate(document,
					XPathConstants.NODESET);
			assertEquals(9 /* count of .bpmn in W4 dir */, nodes.getLength());

			// report files for each tool
			assertHtmlReportsExist(new File(TestUtil.REPORT_BASE_FOLDER,
					XmlCompareAnalysisTool.NAME));
			assertHtmlReportsExist(new File(TestUtil.REPORT_BASE_FOLDER,
					XsdAnalysisTool.NAME));
			assertHtmlReportsExist(new File(TestUtil.REPORT_BASE_FOLDER,
					XPathAnalysisTool.NAME));

            // assert structure of individual results file
            File xpathResult = new File(TestUtil.REPORT_BASE_FOLDER
                    + File.separator + XPathAnalysisTool.NAME + File.separator
                    + W4_MODELER_ID + File.separator
                    + W4_MODELER_ID + "-A.1.0-roundtrip.html");
            System.out.println("Checking file: " + xpathResult);
            assertTrue(xpathResult.exists());
            document = docBuilder.parse(xpathResult);
            nodes = (NodeList) xPath.compile(
                    "//body/div[@class=\"testresults\"]")
                    .evaluate(document, XPathConstants.NODESET);
            assertTrue("Did not find result element", nodes.getLength() == 1);
            nodes = (NodeList) xPath.compile(
                    "//body/div[@class=\"testresults\"]/div[@class=\"tool\"]")
                    .evaluate(document, XPathConstants.NODESET);
            assertTrue("Did not find tool element", nodes.getLength() == 1);
            nodes = (NodeList) xPath
                    .compile(
                            "//body/div[@class=\"testresults\"]/div[@class=\"tool\"]/div[@class=\"test\"]")
                    .evaluate(document, XPathConstants.NODESET);
            assertTrue("Did not find test element", nodes.getLength() >= 1);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getClass() + ":" + e.getMessage());
		}
	}

	private void assertHtmlReportsExist(File toolFldr) {
		File w4Fldr = new File(toolFldr, W4_MODELER_ID);
		assertTrue("Tool folder " + toolFldr.getAbsolutePath() + " not found",
				toolFldr.exists());
		assertTrue("Tool report for W4 A.1.0 export not found", new File(
				w4Fldr, W4_MODELER_ID + "-A.1.0-export.html").exists());
		assertTrue("Tool report for W4 A.1.0 roundtrip not found", new File(
				w4Fldr, W4_MODELER_ID + "-A.1.0-roundtrip.html").exists());
		assertTrue("Tool report for W4 A.2.0 export not found", new File(
				w4Fldr, W4_MODELER_ID + "-A.2.0-export.html").exists());
		assertTrue("Tool report for W4 A.2.0 roundtrip not found", new File(
				w4Fldr, W4_MODELER_ID + "-A.2.0-roundtrip.html").exists());
		assertTrue("Tool report for W4 A.3.0 export not found", new File(
				w4Fldr, W4_MODELER_ID + "-A.3.0-export.html").exists());
		assertTrue("Tool report for W4 A.3.0 roundtrip not found", new File(
				w4Fldr, W4_MODELER_ID + "-A.3.0-roundtrip.html").exists());
		assertTrue("Tool report for W4 A.4.0 export not found", new File(
				w4Fldr, W4_MODELER_ID + "-A.4.0-export.html").exists());
		assertTrue("Tool report for W4 A.4.0 roundtrip not found", new File(
				w4Fldr, W4_MODELER_ID + "-A.4.0-roundtrip.html").exists());
		assertTrue("Tool report for W4 A.4.1 roundtrip not found", new File(
				w4Fldr, W4_MODELER_ID + "-A.4.1-roundtrip.html").exists());
	}

	@Test
	public void testMojoHandlingSchemaInvalidBpmn() {
		try {
			Resource res = new Resource();
			res.setDirectory("src/test/invalid-resources");
			mojo.resources.add(res);
			mojo.execute();

			assertTrue(overview.exists());
			Document document = docBuilder.parse(overview);
			NodeList nodes = (NodeList) testOverviewExpr.evaluate(document,
					XPathConstants.NODESET);

			Node invalidNode = nodes.item(0);

			// There should be 2 findings: an invalid element and an invalid
			// attribute
			assertEquals("2",
                    invalidNode.getAttributes()
                            .getNamedItem("data-xsd-finding")
							.getNodeValue());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getClass() + ":" + e.getMessage());
		}
	}

	@Test
	public void testApplicationsParsing() {
		List<String> applications = mojo.getApplications();
		assertEquals(17, applications.size());
		assertTrue(applications.contains(CAMUNDA_MODELER_ID));
		assertTrue(applications.contains(CAMUNDA_JS_ID));
		assertTrue(applications.contains(ECLIPSE_MODELER_ID));
		assertTrue(applications.contains(IBM_MODELER_ID));
		assertTrue(applications.contains(MID_MODELER_ID));
		assertTrue(applications.contains(SIGNAVIO_MODELER_ID));
		assertTrue(applications.contains(YAOQIANG_2_MODELER_ID));
		assertTrue(applications.contains(YAOQIANG_3_MODELER_ID));
		assertTrue(applications.contains(W4_MODELER_ID));
	}
}