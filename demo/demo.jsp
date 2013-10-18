<!DOCTYPE html>

<%@ page import="com.hotdocs.cloud.Client,com.hotdocs.cloud.CreateSessionRequest" %>

<%
    Client client = new Client("SUBSCRIBER_ID", "SIGNING_KEY");
    CreateSessionRequest req = new CreateSessionRequest("Employment Agreement",
        getServletContext().getRealPath("/EmploymentAgreement.hdpkg"));
    String sessionId = client.sendRequest(req);
%>

<html>
<head>
    <title>Employment Agreement Generator</title>
    <script type="text/javascript" src="http://files.hotdocs.ws/download/easyXDM.min.js"></script>
    <script type="text/javascript" src="http://files.hotdocs.ws/download/hotdocs.js"></script>
</head>
<body onload="HD$.CreateInterviewFrame('interview', '<%= sessionId %>');">
    <h1>Employment Agreement Generator</h1>
    <div id="interview" style="width:100%; height:600px; border:1px solid black">
    </div>
</body>
</html>