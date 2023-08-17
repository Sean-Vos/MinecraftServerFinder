//import java.io.BufferedReader;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class ListServer extends Thread
{
    private static String BaseDocument = "";
    public boolean Ok = false;

    private static synchronized void LogServerError(String ErrorString)
    {
        FileOutputStream FileOut = null;

        try
        {
            FileOut = new FileOutputStream("LogFile.txt", true);
            FileOut.write((ErrorString + '\n').getBytes());
            FileOut.close();
        }
        catch (FileNotFoundException exception)
        {
            return;
        }
        catch (IOException exception)
        {
            return;
        }

        return;
    }

    public static String insertString(
        String originalString,
        String stringToBeInserted,
        int index)
    {
  
        // Create a new string
        String newString = new String();
  
        for (int i = 0; i < originalString.length(); i++) {
  
            // Insert the original string character
            // into the new string
            newString += originalString.charAt(i);
  
            if (i == index) {
  
                // Insert the string to be inserted
                // into the new string
                newString += stringToBeInserted;
            }
        }
  
        // return the modified String
        return newString;
    }

    public ListServer()
    {
        try
        {
            FileInputStream Input = new FileInputStream("List.html");
            int InputChar = 0;
            while((InputChar = Input.read()) != -1)
            {
                BaseDocument += (char) InputChar;
            }
            Input.close();
            Ok = true;
        }
        catch (NullPointerException Exception)
        {
            return;
        }
        catch (IOException Exception)
        {
            return;
        }
        return;
    }

    private static void HandleRequest(HttpExchange exchange) throws IOException
    {
        //update list
        List<String> Ip = new ArrayList<String>();
        List<String> VersionNames = new ArrayList<String>();
        List<String> Descriptions = new ArrayList<String>();
        List<Long> PlayerMaximums = new ArrayList<Long>();
        List<Long> PlayersOnline = new ArrayList<Long>();
                
        for (int i = 0; i < ServerFinder.FoundServerIps.size(); i++)
        {
            Ip.add(ServerFinder.FoundServerIps.get(i));
            
            try
            {
                JSONObject JsonData = ServerFinder.FoundServerPingData.get(i);
                if (JsonData != null) 
                {
                    JSONObject Version;
                    try
                    {
                        Version = (JSONObject) JsonData.get("version");
                    }
                    catch (ClassCastException exception)
                    {
                        Version = null;
                    }

                    if (Version == null)
                    {
                        VersionNames.add("Error");
                    }
                    else
                    {
                        Object VersionName = Version.get("name");

                        if (VersionName == null) 
                        {
                            VersionNames.add("Error");
                        }
                        else
                        {
                            VersionNames.add((String) Version.get("name"));
                        }
                    }


                    JSONObject Description;
                    try
                    {
                        Description = (JSONObject) JsonData.get("description");
                    }
                    catch (ClassCastException exception)
                    {
                        Description = null;
                    }

                    if (Description == null)
                    {
                        Descriptions.add("Error");
                    }
                    else
                    {
                        Object text = Description.get("text");
                        if (text == null)
                        {
                            Descriptions.add("Error");
                        }
                        else
                        {
                            Descriptions.add((String) Description.get("text"));
                        }
                    }

                    JSONObject PlayerCount;
                    try
                    {
                        PlayerCount = (JSONObject) JsonData.get("players");
                    }
                    catch (ClassCastException exception)
                    {
                        PlayerCount = null;
                    }

                    if (PlayerCount == null)
                    {
                        PlayerMaximums.add(Long.parseLong("-999"));
                        PlayersOnline.add(Long.parseLong("-999"));
                    }
                    else
                    {
                        Object Max = PlayerCount.get("max");

                        if (Max == null)
                        {
                            PlayerMaximums.add(Long.parseLong("-999"));
                        }
                        else
                        {
                            PlayerMaximums.add((Long) Max);
                        }

                        Object Online = PlayerCount.get("online");

                        if (Online == null)
                        {
                            PlayersOnline.add(Long.parseLong("-999"));
                        }
                        else
                        {
                            PlayersOnline.add((Long) Online);
                        }
                    }

                }
                else
                {
                    VersionNames.add("Error");
                    Descriptions.add("Error");
                    PlayerMaximums.add(Long.parseLong("-999"));
                    PlayersOnline.add(Long.parseLong("-999"));
                }
            }
            catch (NumberFormatException exception)
            {
                LogServerError(exception.getMessage());
            }
            catch (IndexOutOfBoundsException exception)
            {
                LogServerError(exception.getMessage());
            }
            catch (UnsupportedOperationException exception)
            {
                LogServerError(exception.getMessage());
            }
            catch (ClassCastException exception)
            {
                LogServerError(exception.getMessage());
            }
            catch (NullPointerException exception)
            {
                LogServerError(exception.getMessage());
            }
            catch (IllegalArgumentException exception)
            {
                LogServerError(exception.getMessage());
            }
        }

        String NewDocument = BaseDocument;
        int DataTableStart = BaseDocument.indexOf("</tr>");
        String InsertData = "";

        for (int i = 0; i < Ip.size(); i++)
        {
            InsertData += "<tr>\n";
            InsertData += "<th>" + Ip.get(i) + "</th>\n";
            InsertData += "<th>" + VersionNames.get(i) + "</th>\n";
            InsertData += "<th>" + Descriptions.get(i) + "</th>\n";
            InsertData += "<th>" + Long.toString(PlayersOnline.get(i)) + " / " + Long.toString(PlayerMaximums.get(i)) + "</th>\n";
            InsertData += "</tr>\n";
        }
        NewDocument = insertString(NewDocument, InsertData, DataTableStart + 6);

        exchange.sendResponseHeaders(200, NewDocument.getBytes().length);
        OutputStream OutStream = exchange.getResponseBody();
        OutStream.write(NewDocument.getBytes());
        OutStream.close();
        return;
    }

    public void run()
    {
        try
        {
            HttpServer Server = HttpServer.create(new InetSocketAddress(80), 5);
            HttpContext Context = Server.createContext("/");
            Context.setHandler(ListServer::HandleRequest);
            Server.start();
        }
        catch (IOException exception)
        {
            return;
        }
    }
}
