package control;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import java.io.*;
import java.util.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Giorgos
 */
public class Search {

    public static String encodeURIcomponent(String s) {
        StringBuilder o = new StringBuilder();
        for (char ch : s.toCharArray()) {
            if (isUnsafe(ch)) {
                o.append('%');
                o.append(toHex(ch / 16));
                o.append(toHex(ch % 16));
            } else {
                o.append(ch);
            }
        }
        return o.toString();
    }

    private static char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private static boolean isUnsafe(char ch) {
        if (ch > 128 || ch < 0) {
            return true;
        }
        return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
    }

    public static String findDocTitle(int id ,String path) throws IOException {
        String indexLocation = path + "indexes/med";
        // 1. open the index
        Directory index = FSDirectory.open(new File(indexLocation));
        IndexReader reader = IndexReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        Document d = searcher.doc(id);
        return d.get("title");
    }

    public static String findDoc(int id ,String path) throws IOException {
        String indexLocation = path + "indexes/med";
        // 1. open the index
        Directory index = FSDirectory.open(new File(indexLocation));
        IndexReader reader = IndexReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        Document d = searcher.doc(id);
        String output = d.get("document");
        return output;
    }

    public static ArrayList<String> findSynonyms(String query, int k ,String path) {
        ArrayList<String> output = new ArrayList<String>();
        // 0. Specify the analyzer for tokenizing text.
        //    The same analyzer should be used for indexing and searching
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
        String indexLocation = path + "indexes/knowledge";
        try {
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
            for (int j = 0; j < hits.length; ++j) {
                int docId = hits[j].doc;
                Document d = searcher.doc(docId);
                String syn = d.get("synonym").replace("(", "").replace(")", "").replace(":", "");
                output.add(syn.split("#")[0]);
            }
            searcher.close();
            reader.close();
        } catch (Exception e) {
        }
        return output;
    }

    public static String search(String query, boolean expand, int k, int start ,String path) {
        String output = "";
        int hitsPerPage = 1000;
        // 0. Specify the analyzer for tokenizing text.
        //    The same analyzer should be used for indexing and searching
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
        String indexLocation = path + "indexes/med";
        try {
            // 1. open the index
            Directory index = FSDirectory.open(new File(indexLocation));
            // 2. query
            Query q = new QueryParser(Version.LUCENE_35, "document", analyzer).parse(query);

            if (!expand) {
                // 3. search 
                IndexReader reader = IndexReader.open(index);
                IndexSearcher searcher = new IndexSearcher(reader);
                TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
                searcher.search(q, collector);
                TopDocs topdocs = collector.topDocs();
                ScoreDoc[] hits = topdocs.scoreDocs;
                int totalhits = topdocs.totalHits;
                output += "<br>found " + totalhits + " results<br><br>";
                output += "<center><table>";
                // 4. display results
                int end = start + 10;
                if (end > hits.length) {
                    end = hits.length;
                }
                for (int j = start; j < end; ++j) {
                    int docId = hits[j].doc;
                    Document d = searcher.doc(docId);
                    if (d.get("title") != null) {
                        String doc = "..." + d.get("document").substring(d.get("title").length(), d.get("document").length()).replace("<br>", "");
                        if (doc.length() > 100) {
                            doc = doc.substring(0, 100) + "...";
                        }
                        output += "<tr><td><a href=results.jsp?id=" + docId + ">" + d.get("title") + "</a>" + "<br>" + doc + "<br><br></td></tr>";
                    }
                }
                output += "<tr><td><center>";
                if (start != 0) {
                    output += "<br><br><a href=index.jsp?start=" + (start - 10) + "&k=&terms=manual&query=" + encodeURIcomponent(query) + "><img src=\"previous.png\"></a>&nbsp;&nbsp;";
                }
                int numberOfPages = totalhits / 10;
                if (numberOfPages == 0 && totalhits != 0) {
                    numberOfPages = 1;
                }
                if (numberOfPages <= 20) {
                    for (int j = 0; j < numberOfPages; j++) {
                        if (start == j * 10) {
                            output += "&nbsp;&nbsp;<font color = \"red\"><b>" + (j + 1) + "</b>";
                        } else {
                            output += "&nbsp;&nbsp;" + "<a STYLE=\"text-decoration:none\" href=index.jsp?start=" + (j * 10) + "&k=&terms=manual&query=" + encodeURIcomponent(query) + "><font color = \"black\"><b>" + (j + 1) + "</b></a>";
                        }
                    }
                } else {
                    if (start / 10 > 3) {
                        output += "&nbsp;&nbsp;" + "<a STYLE=\"text-decoration:none\" href=index.jsp?start=0&k=&terms=manual&query=" + encodeURIcomponent(query) + "><font color = \"black\"><b>1</b></a>";
                    }
                    if (start / 10 > 4) {
                        output += "&nbsp;&nbsp;" + "<a STYLE=\"text-decoration:none\" href=index.jsp?start=10&k=&terms=manual&query=" + encodeURIcomponent(query) + "><font color = \"black\"><b>2</b></a>";
                    }
                    if (start / 10 > 5) {
                        output += "&nbsp;&nbsp;" + "<a STYLE=\"text-decoration:none\" href=index.jsp?start=20&k=&terms=manual&query=" + encodeURIcomponent(query) + "><font color = \"black\"><b>3</b></a>";
                    }
                    if (start / 10 > 6) {
                        output += "&nbsp;&nbsp;...";
                    }
                    int to=start/10+4;
                    if(to>numberOfPages)
                        to=numberOfPages;
                    for (int j = start / 10 - 3; j < to; j++){
                        if (j < 0) {
                            continue;
                        }
                        if (start == j * 10) {
                            output += "&nbsp;&nbsp;<font color = \"red\"><b>" + (j + 1) + "</b>";
                        } else {
                            output += "&nbsp;&nbsp;" + "<a STYLE=\"text-decoration:none\" href=index.jsp?start=" + (j * 10) + "&k=&terms=manual&query=" + encodeURIcomponent(query) + "><font color = \"black\"><b>" + (j + 1) + "</b></a>";
                        }
                    }
                    if(to!=numberOfPages&&to!=numberOfPages-1&&to!=numberOfPages-2&&to!=numberOfPages-3)
                        output += "&nbsp;&nbsp;...";
                    int from = numberOfPages - 3;
                    if (start / 10 + 4 > from) {
                        from = start / 10 + 4;
                    }
                    to = numberOfPages;
                    for (int j = from; j < to; j++) {
                        if (start == j * 10) {
                            output += "&nbsp;&nbsp;<font color = \"red\"><b>" + (j + 1) + "</b>";
                        } else {
                            output += "&nbsp;&nbsp;" + "<a STYLE=\"text-decoration:none\" href=index.jsp?start=" + (j * 10) + "&k=&terms=manual&query=" + encodeURIcomponent(query) + "><font color = \"black\"><b>" + (j + 1) + "<b></a>";
                        }
                    }
                }
                if (start != (numberOfPages - 1) * 10 && totalhits != 0) {
                    output += "&nbsp;&nbsp;<a href=index.jsp?start=" + (start + 10) + "&k=&terms=manual&query=" + encodeURIcomponent(query) + "><img src=\"next.png\"></a>";
                }
                searcher.close();
                reader.close();
                output += "</td></tr></table></center>";
            } else {
                //expand query
                ArrayList<String> synonyms = findSynonyms(query, k , path);
                String expanded_query = query;
                for (String syn : synonyms) {
                    expanded_query += syn;
                }
                Query exp_q = new QueryParser(Version.LUCENE_35, "document", analyzer).parse(expanded_query);

                IndexReader reader = IndexReader.open(index);
                IndexSearcher searcher = new IndexSearcher(reader);
                TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
                searcher.search(exp_q, collector);
                TopDocs topdocs = collector.topDocs();
                ScoreDoc[] hits = topdocs.scoreDocs;
                int totalhits = topdocs.totalHits;
                output += "<br>found " + totalhits + " results<br><br>";
                output += "<center><table>";

                int end = start + 10;
                if (end > hits.length) {
                    end = hits.length;
                }
                for (int j = start; j < end; ++j) {
                    int docId = hits[j].doc;
                    Document d = searcher.doc(docId);
                    if (d.get("title") != null) {
                        String doc = "..." + d.get("document").substring(d.get("title").length(), d.get("document").length()).replace("<br>", "");
                        if (doc.length() > 100) {
                            doc = doc.substring(0, 100) + "...";
                        }
                        output += "<tr><td><a href=results.jsp?id=" + docId + ">" + d.get("title") + "</a>" + "<br>" + doc + "<br><br></td></tr>";
                    }
                }
                output += "<tr><td><center>";
                if (start != 0) {
                    output += "<br><br><a href=index.jsp?start=" + (start - 10) + "&k=" + k + "&terms=automatic&query=" + encodeURIcomponent(query) + "><img src=\"previous.png\"></a>&nbsp;&nbsp;";
                }
                int numberOfPages = totalhits / 10;
                if (numberOfPages == 0 && totalhits != 0) {
                    numberOfPages = 1;
                }
                if (numberOfPages <= 20) {
                    for (int j = 0; j < numberOfPages; j++) {
                        if (start == j * 10) {
                            output += "&nbsp;&nbsp;<font color = \"red\"><b>" + (j + 1) + "</b>";
                        } else {
                            output += "&nbsp;&nbsp;" + "<a STYLE=\"text-decoration:none\" href=index.jsp?start=" + (j * 10) + "&k=" + k + "&terms=automatic&query=" + encodeURIcomponent(query) + "><font color = \"black\"><b>" + (j + 1) + "</b></a>";
                        }
                    }
                } else {
                    if (start / 10 > 3) {
                        output += "&nbsp;&nbsp;" + "<a STYLE=\"text-decoration:none\" href=index.jsp?start=0&k=" + k + "&terms=automatic&query=" + encodeURIcomponent(query) + "><font color = \"black\"><b>1</b></a>";
                    }
                    if (start / 10 > 4) {
                        output += "&nbsp;&nbsp;" + "<a STYLE=\"text-decoration:none\" href=index.jsp?start=10&k=" + k + "&terms=automatic&query=" + encodeURIcomponent(query) + "><font color = \"black\"><b>2</b></a>";
                    }
                    if (start / 10 > 5) {
                        output += "&nbsp;&nbsp;" + "<a STYLE=\"text-decoration:none\" href=index.jsp?start=20&k=" + k + "&terms=automatic&query=" + encodeURIcomponent(query) + "><font color = \"black\"><b>3</b></a>";
                    }
                    if (start / 10 > 6) {
                        output += "&nbsp;&nbsp;...";
                    }
                    int to=start/10+4;
                    if(to>numberOfPages)
                        to=numberOfPages;
                    for (int j = start / 10 - 3; j <to; j++) {
                        if (j < 0) {
                            continue;
                        }
                        if (start == j * 10) {
                            output += "&nbsp;&nbsp;<font color = \"red\"><b>" + (j + 1) + "</b>";
                        } else {
                            output += "&nbsp;&nbsp;" + "<a STYLE=\"text-decoration:none\" href=index.jsp?start=" + (j * 10) + "&k=" + k + "&terms=automatic&query=" + encodeURIcomponent(query) + "><font color = \"black\"><b>" + (j + 1) + "</b></a>";
                        }
                    }
                    if(to!=numberOfPages&&to!=numberOfPages-1&&to!=numberOfPages-2&&to!=numberOfPages-3)
                        output += "&nbsp;&nbsp;...";
                    int from = numberOfPages - 3;
                    if (start / 10 + 4 > from) {
                        from = start / 10 + 4;
                    }
                    to = numberOfPages;
                    for (int j = from; j < to; j++) {
                        {
                            if (start == j * 10) {
                                output += "&nbsp;&nbsp;<font color = \"red\"><b>" + (j + 1) + "</b>";
                            } else {
                                output += "&nbsp;&nbsp;" + "<a STYLE=\"text-decoration:none\" href=index.jsp?start=" + (j * 10) + "&k=" + k + "&terms=automatic&query=" + encodeURIcomponent(query) + "><font color = \"black\"><b>" + (j + 1) + "</a>";
                            }
                        }
                    }
                    if (start != (numberOfPages - 1) * 10 && totalhits != 0) {
                        output += "&nbsp;&nbsp;<a href=index.jsp?start=" + (start + 10) + "&k=" + k + "&terms=automatic&query=" + encodeURIcomponent(query) + "><img src=\"next.png\"></a>";
                    }

                    searcher.close();
                    reader.close();
                    output += "</td></tr></table></center>";
                }
            }
        } catch (Exception e) {
        }
        return output;
    }
}
