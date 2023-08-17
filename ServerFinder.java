import java.util.*;
import java.io.*;
import org.json.simple.JSONObject;

public class ServerFinder
{
    private static String SearchInput = "";
    private static String FindOutput = "";
    private static int ThreadCount = 0;

    private static List<String> IpList = new ArrayList<String>();
    private static List<SearchThread> SearchThreads = new ArrayList<SearchThread>();

    public static List<String> FoundServerIps = new ArrayList<String>();
    public static List<JSONObject> FoundServerPingData = new ArrayList<JSONObject>();

    private static ListServer FoundServerPage;

    private static boolean ParseInput(String FileSource)
    {
        try
        {
            FileInputStream InputFileStream = new FileInputStream(SearchInput);
            String Line = "";

            int InputCharacter = 0;
            
            try
            {
                while ((InputCharacter = InputFileStream.read()) != -1)
                {
                    if (((char) InputCharacter == '\n') || ((char) InputCharacter == '\r'))
                    {
                        //generate ips from range
                        String StartIp = "";
                        String EndIp = "";

                        boolean OnStartIp = true;
                        for (int i = 0; i < Line.length(); i++)
                        {
                            if (Line.charAt(i) == '-')
                            {
                                OnStartIp = false;
                                continue;
                            }

                            if (OnStartIp)
                            {
                                StartIp += Line.charAt(i);
                            }
                            else
                            {
                                EndIp += Line.charAt(i);
                            }
                        }

                        for (;;)
                        {
                            if (!StartIp.equals("")) {
                                IpList.add(StartIp);
                            }

                            if (StartIp.equals(EndIp))
                            {
                                break;
                            }

                            int IpVal1 = 0;
                            int IpVal2 = 0;
                            int IpVal3 = 0;
                            int IpVal4 = 0;

                            String IpSVal[] = {"", "", "", ""};

                            int IpValIndex = 0;
                            for (int i = 0; i < StartIp.length(); i++)
                            {
                                if (StartIp.charAt(i) == '.')
                                {
                                    IpValIndex++;
                                    continue;
                                }

                                IpSVal[IpValIndex] += StartIp.charAt(i);
                            }

                            try
                            {
                                IpVal1 = Integer.parseInt(IpSVal[0]);
                                IpVal2 = Integer.parseInt(IpSVal[1]);
                                IpVal3 = Integer.parseInt(IpSVal[2]);
                                IpVal4 = Integer.parseInt(IpSVal[3]);
                            }
                            catch (NumberFormatException Exception)
                            {
                                String ErrorString = "Error parsing ip string: ";
                                ErrorString += (IpSVal[0] + ".");
                                ErrorString += (IpSVal[1] + ".");
                                ErrorString += (IpSVal[2] + ".");
                                ErrorString += (IpSVal[3]);

                                System.out.println(ErrorString);
                                break;
                            }

                            if (IpVal4 == 255)
                            {
                                if (IpVal3 == 255)
                                {
                                    if (IpVal2 == 255)
                                    {
                                        if (IpVal1 == 255)
                                        {
                                            System.out.println("Error Ip limit reached while parsing file: " + SearchInput);
                                            return false;
                                        }
                                        else
                                        {
                                            IpVal1++;
                                            IpVal4 = 0;
                                            IpVal3 = 0;
                                            IpVal2 = 0;
                                        }
                                    }
                                    else
                                    {
                                        IpVal2++;
                                        IpVal4 = 0;
                                        IpVal3 = 0;
                                    }
                                }
                                else
                                {
                                    IpVal3++;
                                    IpVal4 = 0;
                                }
                            }
                            else
                            {
                                IpVal4++;
                            }

                            String IncrementedIp = "";
                            IncrementedIp += String.valueOf(IpVal1);
                            IncrementedIp += '.';
                            IncrementedIp += String.valueOf(IpVal2);
                            IncrementedIp += '.';
                            IncrementedIp += String.valueOf(IpVal3);
                            IncrementedIp += '.';
                            IncrementedIp += String.valueOf(IpVal4);

                            StartIp = IncrementedIp;
                        }

                        Line = "";
                        continue;
                    }
                    Line += (char) InputCharacter;
                }

                InputFileStream.close();
            }
            catch (IOException Exception)
            {
                System.out.println("Error io exception thrown when parsing: " + SearchInput);
                return false;
            }
        }
        catch (FileNotFoundException Exception)
        {
            System.out.println("Error could not open input file: " + SearchInput);
            return false;
        }

        return true;
    }

    private static void AllocateThreads(String FileOutput)
    {
        if (ThreadCount == 0)
        {
            return;
        }

        int SearchThreadSegmentCount = IpList.size() / ThreadCount;
        if ((IpList.size() % ThreadCount) != 0)
        {
            //create a seprate thread for the remainder of the division
            List<String> SearchSegment = new ArrayList<String>();
            int IpListCurrentIndex = (SearchThreadSegmentCount * ThreadCount);

            for (int i = 0; i < IpList.size() % ThreadCount; i++)
            {
                SearchSegment.add(IpList.get(IpListCurrentIndex + i));
            }

            SearchThreads.add(new SearchThread(SearchSegment, FindOutput));
        }

        int IpListCurrentIndex = 0;
        for (int i = 0; i < ThreadCount; i++)
        {
            List<String> SearchSegment = new ArrayList<String>();
            for (int a = 0; a < SearchThreadSegmentCount; a++)
            {
                SearchSegment.add(IpList.get(a + IpListCurrentIndex));
            }
            IpListCurrentIndex += SearchThreadSegmentCount;

            SearchThreads.add(new SearchThread(SearchSegment, FindOutput));
        }

        for (int i = 0; i < SearchThreads.size(); i++)
        {
            SearchThreads.get(i).start();
        }
        return;
    }

    private static boolean StartHttpServer()
    {
        FoundServerPage = new ListServer();
        if (!FoundServerPage.Ok)
        {
            return false;
        }
        FoundServerPage.start();
        return true;
    }

    private static void WaitForThreads()
    {
        for (int i = 0; i < SearchThreads.size(); i++)
        {
            try 
            {
                SearchThreads.get(i).join();
            }
            catch (InterruptedException Exception)
            {
                System.out.println("Thread interruption exception encounterd");
            }
        }

        System.out.println("Done Searching!");
        return;
    }

    public static void main(String[] args)
    {
        if (args.length != 3)
        {
            System.out.println("Usage: ./Program SearchInput.txt FindOutput.txt [thread ammount]");
            return;
        }

        SearchInput = args[0];
        FindOutput = args[1];
        ThreadCount = Integer.parseInt(args[2]);

        if (ThreadCount < 1)
        {
            System.out.println("Error Thread count must be greater than zero");
            return;
        }

        if (!ParseInput(SearchInput))
            return;

        AllocateThreads(FindOutput);

        if (!StartHttpServer())
        {
            System.out.println("Error failed to start httpserver");
            return;
        }

        WaitForThreads();
    }
}