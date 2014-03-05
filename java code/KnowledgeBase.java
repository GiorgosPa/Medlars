/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package medlars_collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Giorgos
 */
public class KnowledgeBase {

    public static void main(String args[]) throws CorruptIndexException, IOException, ParseException, LockObtainFailedException, Exception {

        try {
            System.out.println(parseTerms("Knowledge_Base.xml"));
        } catch (Exception ex) {
            Logger.getLogger(KnowledgeBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void indexing() throws Exception {
        // 0. Specify the analyzer for tokenizing text.
        //    The same analyzer should be used for indexing and searching
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
        String indexLocation = "knowledge";
        // 1. create the index
        Directory index = FSDirectory.open(new File(indexLocation));
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);
        IndexWriter writer = new IndexWriter(index, config);
        List<Term> terms = parseTerms("Knowledge_Base.xml");
        for (Term term : terms) {
            addDoc(writer, term.getDef(), term.getIs_a(), term.getSynonym());
        }
        writer.close();
    }

    public static ArrayList<String> findSynonyms(String query, int k) throws Exception {
        // 0. create the index
        //indexing();
        //    The same analyzer should be used for indexing and searching
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
        String indexLocation = "knowledge";
        // 1. open the index
        Directory index = FSDirectory.open(new File(indexLocation));

        // 2. query
        Query q = new QueryParser(Version.LUCENE_35, "name", analyzer).parse(query);

        // 3. search             
        IndexReader reader = IndexReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopScoreDocCollector collector = TopScoreDocCollector.create(k, true);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        // 4. display results
        //System.out.println("Found: "+ hits.length + "hits");
        ArrayList<String> synonyms = new ArrayList<String>();
        for (int j = 0; j < hits.length; ++j) {
            int docId = hits[j].doc;
            org.apache.lucene.document.Document d = searcher.doc(docId);
            String syn = d.get("synonym").replace("(", "").replace(")", "").replace(":", "");
            synonyms.add(syn.split("#")[0]);
        }
        
        searcher.close();
        reader.close();

        return synonyms;
    }

    private static void addDoc(IndexWriter writer, String name, String is_a, ArrayList<String> synonyms) throws IOException {
        org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
        Field namefield = new Field("name", name, Field.Store.YES, Field.Index.ANALYZED);
        Field is_afield = new Field("is_a", is_a, Field.Store.YES, Field.Index.ANALYZED);
        String syn = "";
        for (String synonym : synonyms) {
            syn += synonym + "#";
        }
        //remove last #
        if (syn.length() > 0) {
            syn = syn.substring(0, syn.length() - 1);
        }

        Field synonymfield = new Field("synonym", syn, Field.Store.YES, Field.Index.ANALYZED);
        doc.add(namefield);
        doc.add(is_afield);
        doc.add(synonymfield);
        writer.addDocument(doc);
    }

    private static List<Term> parseTerms(String fl) throws Exception {
        List<Term> terms = new ArrayList<Term>();

        //Δημιουργία του DOM XML parser
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        //Parse fil to DOM Tree
        Document doc = dBuilder.parse(fl);

        //Διαβάσμα του root κόμβου
        Element rootElement = doc.getDocumentElement();
        System.out.println("Root element :" + rootElement.getNodeName());
        //Παίρνουμε όλα τα elements  <user>
        NodeList nList = doc.getElementsByTagName("term");

        for (int n = 0; n < nList.getLength(); n++) {
            Node nNode = nList.item(n);
            Element eElement = (Element) nNode;
            //Διαβάζουμε τα στοιχεία που βρίσκονται στα tags κάτω από κάθε user
            //element
            ArrayList<String> namelist = getTagValue("name", eElement);
            ArrayList<String> deflist = getTagValue("def", eElement);
            ArrayList<String> is_alist = getTagValue("is_a", eElement);
            ArrayList<String> synonyms = getTagValue("synonym", eElement);
            String name = listToString(namelist);
            String def = listToString(deflist);
            name += def;
            String is_a = listToString(is_alist);
            //Δημιουργούμε ένα object Temr με τα στοιχεία που διαβάσαμε
            Term term = new Term(name, synonyms, is_a);
            terms.add(term);
        }

        return terms;
    }

    /***
     * Επιστρέφει το κείμενο που βρίσκεται ανάμεσα στo <stag></stag>
     * 
     * @param sTag
     * @param eElement το parent node (tag element) 
     * @return 
     */
    private static ArrayList<String> getTagValue(String sTag, Element eElement) {
        ArrayList<String> output = new ArrayList<String>();
        if (eElement.getElementsByTagName(sTag).item(0) != null) {
            NodeList nlList;
            if (eElement.getElementsByTagName(sTag).getLength() > 1) {
                for (int j = 0; j < eElement.getElementsByTagName(sTag).getLength(); j++) {
                    nlList = eElement.getElementsByTagName(sTag).item(j).getChildNodes();
                    Node nValue = nlList.item(0);
                    if (nValue != null) {
                        output.add(nValue.getNodeValue());
                    }
                }
                return output;
            } else {
                nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
                Node nValue = nlList.item(0);
                output.add(nValue.getNodeValue());
                return output;
            }
        } else {
            return output;
        }
    }

    private static String listToString(List<String> list) {
        String output = "";
        for (String item : list) {
            output = output + " " + item;
        }
        return output;

    }
}
