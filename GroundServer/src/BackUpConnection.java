import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
 
public class BackUpConnection {
       
        ExchangerThread readThread , writeThread;
        boolean FCstatus = false;
        boolean GCSstatus = false;
       
        public BackUpConnection(InputStream input1 , OutputStream output1 , InputStream input2 , OutputStream output2) throws IOException
        {
        		System.out.println("FlightAndroid와 GroundMonitor의 통신이 이루어집니다.");
        		
                readThread = new ExchangerThread(input1 , output2);
                readThread.start();
                writeThread = new ExchangerThread(input2 , output1);
                writeThread.start();
               
        }
       
        class ExchangerThread extends Thread
        {
                BufferedReader reader;
                PrintWriter writer;
                InputStream input1;
                OutputStream output1;
               
                public ExchangerThread(InputStream input , OutputStream output)  throws IOException {
                        // TODO Auto-generated constructor stub
                        reader = new BufferedReader(new InputStreamReader(input));
                        writer = new PrintWriter(output,true);
                        input1 = input;
                        output1 = output;
               
                }
               
                public void run()
                {
                        try
                        {
                        	/*
                        	byte[] data = new byte[4096];
                        	int lng = 0;
                        	*/
                        		writer.println("s'set"); 
                                while(true)
                                {
                                		
                                        for(String str ; (str = reader.readLine())!= null;)
                                       {
                                        		System.out.println("message: " + str);
                                       		 
                                                writer.println("m'" + str);
                                        }
                                	
                                	/*
                                	lng = input1.read(data);
                                	if(lng >0)
                                	{
                                		output1.write(data);
                                		System.out.println("input: " + lng);
                                	}
                                	*/
                                }
                        }
                        catch(Exception e)
                        {
                                System.err.println("메세지 데이터를 읽어오는 중 예외가 발생");
                                e.printStackTrace();
                        }
                }
        }
 
}