/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package medlars_collection;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
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
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Giorgos
 */
public class Medlars_collection {
    
    public static void indexing()throws Exception {
        // 0. Specify the analyzer for tokenizing text.
        //    The same analyzer should be used for indexing and searching
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
        String indexLocation = "med";
        // 1. create the index
        Directory index = FSDirectory.open(new File(indexLocation));
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);
        IndexWriter writer = new IndexWriter(index, config);
        
        //Read documents
        BufferedReader med = new BufferedReader(new InputStreamReader(new FileInputStream("med.all")));
        String line="";
        String docNo="";
        String document="";
        String title = "";
        line=med.readLine();
        while(med.ready()){            
            if(line.startsWith(".I")){
                if(!document.equals("")){
                    addDoc(writer, document , docNo,title);
                    document="";
                }
                docNo=line.split(" ")[1];
                line=med.readLine();
                line=med.readLine();
                title=line;
                
            }
            document+=line +"<br>";
            line=med.readLine();
        }
        if(!document.equals(""))
             addDoc(writer, document,docNo,title);
        writer.close();
        med.close();
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ParseException, Exception {
        // 0. create the index
        //indexing();
        //    The same analyzer should be used for indexing and searching
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
        String indexLocation = "med";
        // 1. open the index
        Directory index = FSDirectory.open(new File(indexLocation));
        //Read queries
        BufferedReader queriesFile = new BufferedReader(new InputStreamReader(new FileInputStream("query.text")));
        String line="";
        ArrayList<String> queries = new ArrayList<String>();
        String query="";
        while(queriesFile.ready()){
            line=queriesFile.readLine();
            if(line.startsWith(".I")){
                queriesFile.readLine();
                line=queriesFile.readLine();
                if(!query.equals(""))
                    queries.add(query);
                query="";
            }
            query+=line;
        }
        if(!query.equals(""))
             queries.add(query);
        
        queriesFile.close();
        
        OutputStreamWriter results = new OutputStreamWriter(new FileOutputStream("results.text"));
        OutputStreamWriter resultsexp = new OutputStreamWriter(new FileOutputStream("resultsexpanded.text"));
        OutputStreamWriter resultsexp10 = new OutputStreamWriter(new FileOutputStream("resultsexpanded10.text"));
        
       
        // 2. query
        for(int i=0;i<queries.size();i++){
            Query q = new QueryParser(Version.LUCENE_35, "document", analyzer).parse(queries.get(i));
            
            //expand query
            ArrayList<String> synonyms = KnowledgeBase.findSynonyms(queries.get(i),5);
            String expanded_query = queries.get(i);
            for(String syn :synonyms)
                expanded_query += syn ;  
            
             //expand query
            ArrayList<String> synonyms_10 = KnowledgeBase.findSynonyms(queries.get(i),10);
            String expanded_query_10 = queries.get(i);
            for(String syn :synonyms)
                expanded_query += syn ;     
            
            Query exp_q = new QueryParser(Version.LUCENE_35, "document", analyzer).parse(expanded_query);
            Query exp_q_10 = new QueryParser(Version.LUCENE_35, "document", analyzer).parse(expanded_query_10);
            
            // 3. search 
            int hitsPerPage = 200;
            IndexReader reader = IndexReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            
            // 4. write results
            //System.out.println("Found: "+ hits.length + "hits");
            for(int j=0;j<hits.length;++j) {
                int docId = hits[j].doc;
                Document d = searcher.doc(docId);
                results.write((i+1)+"\tQ0\t"+ d.get("number")+ "\t" + (j+1) +"\t0.0\tprise1\n");
            }
            searcher.close();
            reader.close();
            
            collector = TopScoreDocCollector.create(hitsPerPage, true);
            reader = IndexReader.open(index);
            searcher = new IndexSearcher(reader);            
            searcher.search(exp_q, collector);
            ScoreDoc[] exp_hits = collector.topDocs().scoreDocs;
            
            // 4. write results for expanded query k = 5
            for(int j=0;j<exp_hits.length;++j) {
                int docId = exp_hits[j].doc;
                Document d = searcher.doc(docId);
                resultsexp.write((i+1)+"\tQ0\t"+ d.get("number")+ "\t" + (j+1) +"\t0.0\tprise1\n");
            }
            
            searcher.close();
            reader.close();
            
            collector = TopScoreDocCollector.create(hitsPerPage, true);
            reader = IndexReader.open(index);
            searcher = new IndexSearcher(reader);            
            searcher.search(exp_q_10, collector);
            ScoreDoc[] exp_hits_10 = collector.topDocs().scoreDocs;
            
            // 4. write results for expanded query k = 10
            for(int j=0;j<exp_hits_10.length;++j) {
                int docId = exp_hits_10[j].doc;
                Document d = searcher.doc(docId);
                resultsexp10.write((i+1)+"\tQ0\t"+ d.get("number")+ "\t" + (j+1) +"\t0.0\tprise1\n");
            }
            
            searcher.close();
            reader.close();
        }
        results.close();
        resultsexp.close();
        resultsexp10.close();
        
        BufferedReader qrel = new BufferedReader(new InputStreamReader(new FileInputStream("qrels.text")));
        OutputStreamWriter qrels = new OutputStreamWriter(new FileOutputStream("qrels2.text"));
        
        line="";
        //make qrels in trec's format
        while(qrel.ready()){
            line = qrel.readLine();
            StringTokenizer st = new StringTokenizer(line);
            qrels.write(st.nextToken()+"\tQ0\t" +st.nextToken()+"\t1\n");
        }
        qrel.close();
        qrels.close();
  }

  private static void addDoc(IndexWriter writer, String text,String num,String tit) throws IOException {
        Document doc = new Document();
        Field title = new Field("title", tit, Field.Store.YES, Field.Index.ANALYZED);
        Field document = new Field("document", text, Field.Store.YES, Field.Index.ANALYZED);
        Field docNo = new Field("number", num, Field.Store.YES, Field.Index.ANALYZED);
        doc.add(document);
        doc.add(title);
        doc.add(docNo);
        writer.addDocument(doc);
  }
}

