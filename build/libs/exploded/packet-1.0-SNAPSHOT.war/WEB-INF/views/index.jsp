<%--
  Created by IntelliJ IDEA.
  User: choon
  Date: 2022-03-29
  Time: 오후 9:16
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script type="text/javascript" src="/resources/js/index.js"></script>
    <title>Title</title>
</head>
<body>
    <div>
        <h3>Host Name: </h3>
        <input type="text" id="hostName"/>
        <button type="button" value="lookUp" id="lookUp">lookUp!</button>
        <h3 id="ipAddress"> IP: </h3>
    </div>
    <div>
        <input type="text" id="expression" placeholder="filter" style="width: 90%"/>
        <input type="text" id="netmask" placeholder="netmask"/>
        <button type="button" value="start" id="start">start</button>
        <h3 id="status"></h3>
        <button type="button" value="start" id="stop">stop</button>
    </div>
</body>
</html>