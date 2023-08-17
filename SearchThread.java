import java.util.List;
import java.net.*;
import java.io.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SearchThread extends Thread
{
    private List<String> Ips;
    private String OutputFile;

    public SearchThread(List<String> SearchIps, String OutputFile_)
    {
        Ips = SearchIps;
        OutputFile = OutputFile_;
        return;
    }

    private synchronized void Print(String Message)
    {
        System.out.println(Message);
        return;
    }

    private synchronized void OutputFoundServer(String Ip, JSONObject JsonData)
    {
        ServerFinder.FoundServerIps.add(Ip);
        ServerFinder.FoundServerPingData.add(JsonData);

        //write ip to outfile
        try
        {
            FileOutputStream FileStream = new FileOutputStream(OutputFile, true);
            FileStream.write((Ip + '\n').getBytes());
            FileStream.close();
        }
        catch (FileNotFoundException Exception)
        {
            Print("Error could not open output file: " + OutputFile);
            return;
        }
        catch (IOException Exception)
        {
            Print("Error could not output to file: " + OutputFile);
            return;
        }
        return;
    }

    private void PirntFailed(String Ip)
    {
        Print("Failed to connect to: " + Ip);
        return;
    }

    private void PrintSuccess(String Ip)
    {
        Print("Found Server");
        return;
    }

    public int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) throw new RuntimeException("VarInt too big");
            if ((k & 0x80) != 128) break;
        }
        return i;
    }
 
    public void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
              out.writeByte(paramInt);
              return;
            }

            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }

    public void run()
    {
        for (int i = 0; i < Ips.size(); i++)
        {
            Socket NetworkSocket = new Socket();
            OutputStream OutDataStream;
            DataOutputStream dataOutputStream;
            InputStream InputDataStream;
            InputStreamReader inputStreamReader;

            try
            {
                NetworkSocket.setSoTimeout(7000);
            }
            catch (SocketException e)
            {

            }

            InetSocketAddress TryAddress = new InetSocketAddress(Ips.get(i), 25565);
            try 
            {

                NetworkSocket.connect(TryAddress, 7000);
                OutDataStream = NetworkSocket.getOutputStream();
                dataOutputStream = new DataOutputStream(OutDataStream);

                InputDataStream = NetworkSocket.getInputStream();
                inputStreamReader = new InputStreamReader(InputDataStream);

                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream handshake = new DataOutputStream(b);
                handshake.write(0x00);
                writeVarInt(handshake, 4);
                writeVarInt(handshake, Ips.get(i).length());
                handshake.writeBytes(Ips.get(i));
                handshake.writeShort(25565);
                writeVarInt(handshake, 1);

                writeVarInt(dataOutputStream, b.size());
                dataOutputStream.write(b.toByteArray());

                dataOutputStream.writeByte(0x01); //size is only 1
                dataOutputStream.writeByte(0x00); //packet id for ping
                DataInputStream dataInputStream = new DataInputStream(InputDataStream);
                readVarInt(dataInputStream); //size of packet
                int id = readVarInt(dataInputStream); //packet id
                
                if (id == -1) {
                    throw new IOException("Premature end of stream.");
                }
        
                if (id != 0x00) { //we want a status response
                    throw new IOException("Invalid packetID");
                }
                int length = readVarInt(dataInputStream); //length of json string
        
                if (length == -1) {
                    throw new IOException("Premature end of stream.");
                }

                if (length == 0) {
                    throw new IOException("Invalid string length.");
                }
        
                byte[] in = new byte[length];
                dataInputStream.readFully(in);  //read json string
                String json = new String(in);

                try
                {
                    JSONParser jsonParser = new JSONParser();
                    JSONObject ParsedJSON = (JSONObject) jsonParser.parse(json);
                    OutputFoundServer(Ips.get(i), ParsedJSON);
                    PrintSuccess(Ips.get(i));
                }
                catch (ParseException Exception)
                {
                    PirntFailed(Ips.get(i));
                }
                
                dataOutputStream.close();
                OutDataStream.close();
                inputStreamReader.close();
                InputDataStream.close();
                NetworkSocket.close();
            }
            catch (IOException e)
            {
                PirntFailed(Ips.get(i));
                //return;
            }
        }
        return;
    }
}
