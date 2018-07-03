package mpi.eudico.server.corpora.clomimpl.graf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.AnnotationDocEncoder;
import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.IoUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GrAFEncoder implements AnnotationDocEncoder {
	protected Document doc;
	private Element graphElement;
	
	public GrAFEncoder() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder        db  = dbf.newDocumentBuilder();
		doc = db.newDocument();
		graphElement = doc.createElement("graph");
	}

	@Override
	public void encodeAndSave(Transcription theTranscription,
			EncoderInfo theEncoderInfo, List<TierImpl> tierOrder, String path)
			throws IOException {
		// ignore the tier order? build trees
		TranscriptionImpl transImpl = (TranscriptionImpl) theTranscription;
		graphElement.setAttribute("id", transImpl.getName());
		
		int nodeId = 1;
		//int nodeSetId = 1;
		int regionId = 1;
		int edgeId = 1;
		//int edgeSetId = 1;
		//HashMap<String, int[]> regionMap = new LinkedHashMap<String, int[]>();
		Map<Annotation, String> annNodeIdMap = new HashMap<Annotation, String>();
		List<Element> nodeSets = new ArrayList<Element>();
		List<Element> edges = new ArrayList<Element>();
		List<Element> regions = new ArrayList<Element>();
		
		// build tree
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		List<DefaultMutableTreeNode> tierNodes = new ArrayList<DefaultMutableTreeNode>();
		Map<TierImpl, DefaultMutableTreeNode> nodeMap = new HashMap<TierImpl, DefaultMutableTreeNode>();
		TierImpl t1;
		for (int i = 0; i < transImpl.getTiers().size(); i++) {
			t1 = transImpl.getTiers().get(i);
			DefaultMutableTreeNode nextnode = new DefaultMutableTreeNode(t1);
			tierNodes.add(nextnode);
			nodeMap.put(t1, nextnode);
		}
		for (int i = 0; i < transImpl.getTiers().size(); i++) {
			t1 = transImpl.getTiers().get(i);
			if (t1.getParentTier() == null) {
				root.add(nodeMap.get(t1));
			} else {
				nodeMap.get(t1.getParentTier()).add(nodeMap.get(t1));
			}
		}
		
		tierNodes.clear();
		nodeMap.clear();
		
		Enumeration<DefaultMutableTreeNode> treeEnum = root.breadthFirstEnumeration();// breadthFirst? depthFirst
		treeEnum.nextElement();
		DefaultMutableTreeNode node;
		while (treeEnum.hasMoreElements()) {
			node = treeEnum.nextElement();
			t1 = (TierImpl) node.getUserObject();
			Element nodeSet = createNodeSet(t1.getName());
			nodeSets.add(nodeSet);
			
			AbstractAnnotation nextAnn = null;
			AbstractAnnotation prevAnn = null;
			List anns = t1.getAnnotations();
			
			for (int i = 0; i < anns.size(); i++) {
				nextAnn = (AbstractAnnotation) anns.get(i);
				String nid = "n" + nodeId++;
				// create a graf node and check parent
				String link = null;
				if (nextAnn instanceof AlignableAnnotation) {
					StringBuilder anchors = new StringBuilder();
					AlignableAnnotation nextAnnAl = (AlignableAnnotation) nextAnn;
					if (nextAnnAl.getBegin().isTimeAligned()) {
						anchors.append(String.valueOf(nextAnnAl.getBegin().getTime()));
					} else {
						anchors.append(String.valueOf(-1));
						// add an edge to previous annotation
						if (prevAnn != null && prevAnn.getParentAnnotation() == nextAnn.getParentAnnotation()) {
							Element edge = createEdge("r" + edgeId++, annNodeIdMap.get(prevAnn), nid);//from - to
							edges.add(edge);
						}
					}
					anchors.append(" ");
					if (nextAnnAl.getEnd().isTimeAligned()) {
						anchors.append(String.valueOf(nextAnnAl.getEnd().getTime()));
					} else {
						anchors.append(String.valueOf(-1));// this one will "edge" to next ann  
					}
					// create a region
					link = "r" + regionId++;
					Element region = createRegion(link, anchors.toString());
					regions.add(region);
				} else {//ref annotation
					RefAnnotation refAnno = (RefAnnotation) nextAnn;
					if (refAnno.getPrevious() != null) {
						Element edge = createEdge("r" + edgeId++, annNodeIdMap.get(prevAnn), nid);//from prev - to this
						edges.add(edge);
					}
					if (refAnno.getParentAnnotation() != null) {// should never be null for a reference annotation
						Element edge = createEdge("r" + edgeId++, nid, annNodeIdMap.get(refAnno.getParentAnnotation()));//from this - to parent
						edges.add(edge);
					}
				}
				
				Element annNode = createNode(nid, nextAnn.getValue(), link);
				nodeSet.appendChild(annNode);
				annNodeIdMap.put(nextAnn, nid);
				
				prevAnn = nextAnn;
			}
		}
		
		// add all regions, all nodesets and alla edges to the document element (graph)
		for (int i = 0; i < regions.size(); i++) {
			graphElement.appendChild(regions.get(i));
		}
		for (int i = 0; i < nodeSets.size(); i++) {
			graphElement.appendChild(nodeSets.get(i));
		}
		for (int i = 0; i < edges.size(); i++) {
			graphElement.appendChild(edges.get(i));
		}
		try {
			IoUtil.writeEncodedFile("UTF-8", path, graphElement);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private Element createNodeSet(String id) {
		Element elem = doc.createElement("nodeSet");
		elem.setAttribute("id", id);
		return elem;
	}

	private Element createNode(String id, String value, String link) {
		Element nn = doc.createElement("node");
		if (link != null) {
			Element ln = doc.createElement("link");
			ln.setAttribute("to", link);// or #link ?
			nn.appendChild(ln);
		}
		Element as = doc.createElement("as");
		as.setAttribute("type", "default");
		nn.appendChild(as);
		// here a <fs> element should be inserted, instead of directly an <f> element
		Element feat = doc.createElement("f");
		feat.setAttribute("n", "label");
		feat.setAttribute("v", value);
		as.appendChild(feat);
		
		return nn;
	}
	
	private Element createEdge(String id, String from, String to) {
		Element nedge = doc.createElement("edge");
		nedge.setAttribute("from", from);
		nedge.setAttribute("to", to);
		
		return nedge;
	}
	
	private Element createRegion(String link, String anchors) {
		Element re = doc.createElement("region");
		re.setAttribute("id", link);
		re.setAttribute("anchors", anchors);
		
		return re;
	}
	
	public static void main(String[] args) {
		try {
			GrAFEncoder enc = new GrAFEncoder();
			TranscriptionImpl trans = new TranscriptionImpl("/Users/Shared/MPI/Demo material/pear/pear story.eaf");
			//ACMTranscriptionStore.getCurrentTranscriptionStore().loadTranscription(trans);
			enc.encodeAndSave(trans, null, null, "/Users/Shared/MPI/Demo material/pear/pear story_graf.xml");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		/*
		try {
			Process p = Runtime.getRuntime().exec("http://lat-mpi.eu/tools/elan");
			int i = p.exitValue();
			System.out.println("exit: " + i);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		*/
	}
}
