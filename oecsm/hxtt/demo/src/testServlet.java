import java.net.URL;
import java.sql.*;
import java.io.*;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;

/* A demo show how to use JDBC in Servlet. */
public class testServlet extends HttpServlet
{
    private boolean loginFlag=false;
    private String lastSQL=null;

    private HttpServletRequest request=null;
    private HttpServletResponse response=null;

    private ByteArrayOutputStream bytes=null;
    private	PrintStream	outstream=null;

    private Connection connection=null;

    private static final String servletName="testServlet";
    private static final String title="JDBC Driver Test Servlet Version 1.1";
    private static final String[] drivers=
    {
        "com.hxtt.sql.dbf.DBFDriver",
        "com.hxtt.sql.text.TextDriver",
        "com.hxtt.sql.paradox.ParadoxDriver",
        "com.hxtt.sql.access.AccessDriver",
        "com.hxtt.sql.excel.ExcelDriver",
        "com.hxtt.sql.cobol.CoboLDriver",
        "com.hxtt.sql.xml.XMLDriver",
        "com.hxtt.sql.odb.ODBDriver",
        "sun.jdbc.odbc.JdbcOdbcDriver"
    };

    public String getServletInfo()
    {return title+"\n(c) HXTT\nOct 1998";}

    public void init(ServletConfig servletConfig)
    {
        try
        {
            super.init(servletConfig);
            bytes=new ByteArrayOutputStream (4096);
            outstream=new PrintStream (bytes);
        }
        catch (ServletException e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void destroy()
    {logout();}

    private void logout()
    {
        loginFlag=false;
        lastSQL=null;
        try
        {
            if (connection!=null)
            {
                connection.close();
                connection=null;
            }
            bytes.close();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized void service(HttpServletRequest requestp, HttpServletResponse response)throws ServletException, IOException
    {
        Statement statement=null;
        try
        {
            //ServletRequest can't support Unicode char post
            //this.request=requestp;

            this.request=requestp;
            //HxttHttpServletRequest supports Unicode char post
            //this.request=new com.hxtt.net.http.HxttHttpServletRequest(requestp);

            this.response=response;

            setContentType(); //Avoid the browser reset the connection after a long time wait.

            String sql=request.getParameter("sql");
            if(sql==null || sql.trim().length()==0)
                logout();
            else
                lastSQL=sql.trim();

            String user=request.getParameter("user");
            String password=request.getParameter("password");
            if(user==null || !user.equals("test")
                   || password==null || !password.equals("tset")){
                sendLogonHTML();
                return;
            }

            if(!loginFlag)
            {
                if(user==null)
                {
                    sendLogonHTML();
                    return;
                }

                if(connection==null)
                {
                    String url=request.getParameter("url");
                    String driver=request.getParameter("driver");
                    Class.forName(driver).newInstance();
                    //java.sql.DriverManager.setLogStream(System.out);

                    getConnection(url,user,password);
                }
                loginFlag=true;
            }

            statement=connection.createStatement();
            statement.setMaxRows(500);//It's disabled when stmt.setFetchSize() is enabled.
//          statement.setFetchSize(15);//Now it's enabled when the query references only a single table in the database.



            if(lastSQL.equals("RELEASE ALL Access DATABASE")){
                com.hxtt.sql.access.AccessDriver.releaseAll();
                sendLogonHTML();
                return;
            }

            if(lastSQL.length()>5 && "SELECT".equalsIgnoreCase(lastSQL.substring(0, 6))){
                ResultSet resultSet=statement.executeQuery(lastSQL);
                if (resultSet!=null){
                    sendQueryHTML(resultSet);
                    resultSet.close();
                    resultSet=null;
                }
            } else{
                int rowCount=statement.executeUpdate(lastSQL);
                sendUpdateHTML(rowCount);
            }
        }
        catch (SQLException e)
        {
            sendSQLExceptionHTML(e);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }finally{
            if (statement!=null)
            {
                try{
                    statement.close();
                }catch(SQLException sqle){

                }
                statement=null;
            }
        }
    }

    private synchronized void getConnection(String url,String user,String password)throws SQLException
    {
        connection=DriverManager.getConnection(url,user,password);

        SQLWarning warning=connection.getWarnings();
        if(warning!=null)
        {
            System.out.println("\nSQLWarning");
            while (warning!=null)
            {
                System.out.println("  SQLState:  " +warning.getSQLState());
                System.out.println("  Message:   " +warning.getMessage());
                System.out.println("  ErrorCode: " +warning.getErrorCode());
                System.out.println();
                warning=warning.getNextWarning();
            }
        }
        loginFlag=true;
    }

    private void setContentType()
    {
        response.setContentType("TEXT/HTML");
        outstream.print( "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN//\">");
    }

    private void sendQueryHTML(ResultSet resultSet) throws SQLException
    {


        Vector words=new Vector(200);

        words.addElement("<h2 align=center><font color=#993366>The SQL Query Results</font></h2>");
        words.addElement("<HR WIDTH=100%>");
        try
        {
            ResultSetMetaData resultSetMetaData=resultSet.getMetaData();
            int iNumCols=resultSetMetaData.getColumnCount();

            DatabaseMetaData databaseMetaData=connection.getMetaData();;

            words.addElement("<font color=#800040>JDBC Database MetaData:</font><BR>");
            words.addElement("<BR>Connection url: " + databaseMetaData.getURL());
            words.addElement("<BR>Driver Name: " +databaseMetaData.getDriverName());
            words.addElement("<BR>Driver Version: " +databaseMetaData.getDriverVersion());
            words.addElement("<BR>User Name: " +databaseMetaData.getUserName());
            words.addElement("<BR>Database Product Name: " +databaseMetaData.getDatabaseProductName());
            words.addElement("<BR>Database Product Version: " +databaseMetaData.getDatabaseProductVersion());

            words.addElement("<BR><BR><font color=#800040><BR>the last processed SQL command: </font>"+lastSQL+"<BR>");

            words.addElement("<TABLE BORDER=1>");
            words.addElement("<tr bgcolor=#887766>");

            int i;
            for (i=1; i <=iNumCols; i++)
            {
                words.addElement("<TH><font color=#FFFFFF>" + resultSetMetaData.getColumnName(i) + "</font>");
            }
            while (resultSet.next())
            {
                words.addElement("<TR>");
                for (i=1; i <=iNumCols; i++)
                {
                    words.addElement("<TD>" + resultSet.getString(i));
                }
            }
            words.addElement("</TABLE>");
            words.addElement("<HR WIDTH=100%>");
        }
        catch (SQLException e)
        {
            sendSQLExceptionHTML(e);
        }

        getHTML(words);
        sendSQLHTML();
    }

    private void sendUpdateHTML(int rowCount)
    {
        Vector words=new Vector(10);

        words.addElement("<h2 align=center><font color=#993366>The Status of Update, Insert or Delete</font></h2>");
        words.addElement("<font color=#800080><BR>the last processed SQL command: </font>"+lastSQL+"<BR><BR>");
        if(rowCount>0)
            words.addElement("<p><h3 align=left>The Insert, Update or Delete operation succeeded.</h3>");
        else
            words.addElement("<p><h3 align=left>The Insert, Update or Delete operation failed.</h3>");

        getHTML(words);
        sendSQLHTML();
    }

    private void sendSQLExceptionHTML(SQLException e)
    {
        Vector words=new Vector(30);

        words.addElement("<h2 align=center><font color=#993366>A SQL Exception Occurred</h2>");
        words.addElement("<p>Please hit the browser's Back button.</font>");

        while (e!=null)
        {
            words.addElement("<P>SQLState:  "+e.getSQLState()+"</P>");
            words.addElement("<P>Message:   "+e.getMessage()+"</P>");
            words.addElement("<P>ErrorCode: "+e.getErrorCode()+"</P>");
            e=e.getNextException();
        }

        getHTML(words);
    }

    private void sendLogonHTML()
    {
        Vector words=new Vector(30);
        words.addElement("<h2 align=center><font color=#223344>" + title + "</h2></font>");
        words.addElement("<form action=http://"+request.getServerName()
            +":"+request.getServerPort()+"/servlet/"+servletName+" method=POST>");
        words.addElement("<blockquote><div align=center><center><p>JDBC Driver: <select name=driver size=1 tabindex=1>");
        words.addElement("<option selected value=" + drivers[0] + ">" + drivers[0] + "</option>");
        for (int i=1; i < drivers.length; i++)
            words.addElement("<option value=" + drivers[i] + ">" + drivers[i] + "</option>");
        words.addElement("</select>&nbsp;&nbsp;</center></div></blockquote>");
        words.addElement("<blockquote><div align=center><center><p>User: <input type=text name=user size=10 tabindex=2> &nbsp;Password: <input type=password name=password size=10 tabindex=3></p>");
        words.addElement("</center></div></blockquote>");
        words.addElement("<blockquote><div align=center><center><p>URL: <input type=text name=url size=30 tabindex=4>(e.g., jdbc:Access:/datafiles)&nbsp;");
        words.addElement("</center></div></blockquote>");
        words.addElement("<blockquote><p align=center><font color=#800080>Enter a SQL command here:(e.g., SELECT * FROM TEST WHERE CHAR1>'a')</font></p>");
        words.addElement("<div align=center><center><p><textarea rows=2 name=sql cols=49 tabindex=5></textarea></p></center></div>");
        words.addElement("<blockquote><div align=center><center><p><input type=submit value=Submit SQL Query or Command tabindex=6><input type=reset value=\"Clear Entries\" name=\"clearEntries\" tabindex=7></p>");

        getHTML(words);
    }

    private void sendSQLHTML()
    {
        Vector words=new Vector(10);
        words.addElement("<form action=http://"+request.getServerName()
            +":"+request.getServerPort()+"/servlet/"+servletName+" method=POST>");

        words.addElement("<blockquote><p align=left><font color=#800080>Please input a SQL command here:</font></p>");
        words.addElement("<div align=left><left><p><textarea rows=3 name=sql cols=60 tabindex=1>"
            + this.lastSQL + "</textarea></p></left></div>");
        words.addElement("<blockquote><div align=left><left><p><input type=submit value=Submit SQL Query or Command tabindex=2><input type=reset value=\"Previous SQL Command\" name=\"clearSQL\" tabindex=3></p>");

        getHTML(words);
    }

    private void getHTML(Vector words)
    {
        try
        {
            outstream.print("<HTML><HEAD><TITLE>"+title+"</TITLE></HEAD>");
            outstream.print("<BODY bgcolor=#C0C0C0 text=#FFFFFFF>");

            for (java.util.Enumeration e = words.elements() ; e.hasMoreElements() ;){
                outstream.print((String)e.nextElement());
            }


            outstream.print("</BODY></HTML>");
            outstream.flush();
            bytes.writeTo(response.getOutputStream ());
            bytes.reset();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
