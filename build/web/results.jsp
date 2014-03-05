<!DOCTYPE hmtl>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="control.Search"%>
<html>
    <%!
            String id ;
            String path;
        %>
        <%
            if(request.getParameter("id")!=null)
                    id=request.getParameter("id");
            else
                id="5";
        %>
         <%
             path = request.getRealPath("/");
        %>
    <head>        
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><%=Search.findDocTitle(Integer.parseInt(id),path)%> </title>
    </head>
    <body>
        <center>
        <br>
        <H1><%=Search.findDocTitle(Integer.parseInt(id),path)%></H1>
        <br><hr><br>
        <div><%= Search.findDoc(Integer.parseInt(id),path)%></div>
        <hr>
        </center>
    </body>
</html>

