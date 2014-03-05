<%-- 
    Document   : index
    Created on : 10 Φεβ 2013, 9:59:49 μμ
    Author     : Giorgos
--%>

<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="control.Search"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Medlars Search</title>
    </head>
    <body>
        
        <%!            String k;
            String path;
            String query;
            String automatic;
            String manual;
        %>
        <%
             path = request.getRealPath("/");
        %>
        <%
            k = "5";
            query = "";
            manual = "";
            automatic = "";
            if (request.getParameter("k") != null && request.getParameter("k") != "") {
                k = request.getParameter("k");
            }
            if (request.getParameter("query") != null) {
                query = request.getParameter("query");
            }
            if (request.getParameter("terms") != null) {
                if (request.getParameter("terms").equals("automatic")) {
                    automatic = "checked";
                } else {
                    manual = "checked";
                }
            } else {
                manual = "checked";
            }

        %>

        <style type="text/css">
            #list{
                visibility: hidden;
                cursor: pointer;
                position: Absolute;
                border-top-color: orange;
                top: 140px;
                right: 200px;
                border: 1px solid orange;
                background-color: #ffffff;
            }
        </style>

        <script type="text/javascript">

            function Clickme()
            {   
                var list = document.getElementById('list');

                if(list.style.visibility=='visible')
                {
                    list.style.visibility='hidden';
                }
                else
                {
                    list.style.visibility='visible'
                }
            }
        </script>
        <script type="text/javascript">

            function put(i)
            {
                var query = document.getElementById('q');
                query.value += ", "+ i.id;
                
            }
        </script>

    <center>
        <form name="search" method="get" action="index.jsp" target="_self"  >
            <table width="70%"  cellspacing="10" cellpadding="0" >
                <tr>
                    <td style="border:2px solid orange;" border="2" bgcolor="ffffcc" width="40%" height="148">
                        <table><tr><td><img src="logo.png"></td><td><font color=orange size="4">Information Retrieval<br>Systems</font></td></tr></table>                            
                    </td>
                    <td style="border:2px solid orange;" border="2" bgcolor="ffffcc" width="40%" height="148">
                        <table><tr><td>
                            <center>
                                <font color=orange size="4"> Automatic query expansion</center></td><td> <input type="radio" name="terms" value="automatic" <%=automatic%> ></td></tr>
                <tr> <td><br><font color=orange size="4">Select k </font></td><td><br><input type="text" size="1" maxlength="2" name="k" value=<%=k%>></td></tr></table>
            </td>
            <td style="border:2px solid orange;" border="2" bgcolor="ffffcc" width="20%" height="148">
                <table><tr><td>
                    <center>
                        <font color=orange size="4">Term Selection</font></center></td> <td><input type="radio" name="terms" value="manual" <%=manual%> ></td></tr>
            <tr><td><br><div id="synonyms" ONCLICK="Clickme();"><a href="#"><font color=orange size="4">Related Terms</font></a></div>
                </td><td></td></tr></table>                    
            </td>
            </tr>                    
            </table>
            <table width="50%"  cellspacing="0" cellpadding="0" >
                <tr>
                    <td><input id="q" type="text" name="query" size="100%" value="<%=query%>" ></td>
                    <td><input type="image" src="search.png" ></td>
                </tr>
            </table>
            <input type="hidden" name="start" value="0">
        </form>        
        <% //scriptlet example 
            if (request.getParameter("query") != null && !request.getParameter("query").equals("")) {
                String query = request.getParameter("query").replaceAll("[:(){}]", "");
                int start = Integer.parseInt(request.getParameter("start"));
                if (request.getParameter("terms") != null) {
                    if (request.getParameter("terms").equals("automatic")) {
                        try {
                            int k = Integer.parseInt(request.getParameter("k"));
                            out.println("results for <font color=\"blue\">");
                            out.println(query + ":<br><br>");
                            out.println("</font>");
                            out.println(Search.search(query, true, k, start,path));
                        } catch (Exception e) {
                            out.println("<br>Insert an integer value for k");
                        }

                    } else {
                        out.println("results for <font color=\"blue\">");
                        out.println(query + ":<br><br>");
                        out.println("</font>");
                        out.println(Search.search(query, false, 10, start,path));
                        ArrayList<String> synonyms = Search.findSynonyms(query, 10 ,path );
                        out.println("<div id=\"list\">");
                        for (int i = 0; i < synonyms.size(); i++) {
                            if (!synonyms.get(i).equals("")) {
                                out.println("<div id=\"" + synonyms.get(i) + "\"" + "ondblclick=\"put(this);\"><font size=\"4\">" + synonyms.get(i) + "</font><br>");
                                out.println("</div>");
                            }
                        }
                        out.println("</div>");
                    }
                } else {
                    out.println("<h1>please choose automatic term selection or manual</h1>");
                }
            } else {
                out.println("<br><br><br><center><img src=\"1.jpg\"><img src=\"2.jpg\"><img src=\"3.jpg\"></center>");
            }
        %>

    </body>
</html>
