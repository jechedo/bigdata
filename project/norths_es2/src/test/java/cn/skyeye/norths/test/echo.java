package cn.skyeye.norths.test;

import org.apache.commons.net.echo.EchoTCPClient;
import org.apache.commons.net.echo.EchoUDPClient;

import java.io.*;
import java.net.InetAddress;
import java.net.SocketException;

/***
 * This is an example program demonstrating how to use the EchoTCPClient
 * and EchoUDPClient classes.  This program connects to the default echo
 * service port of a specified server, then reads lines from standard
 * input, writing them to the echo server, and then printing the echo.
 * The default is to use the TCP port.  Use the -udp flag to use the UDP
 * port.
 * <p>
 * Usage: echo [-udp] <hostname>
 ***/
public final class echo
{

    public static final void echoTCP(String host, int port) throws IOException
    {
        EchoTCPClient client = new EchoTCPClient();
        String line;

        // We want to timeout if a response takes longer than 60 seconds
        client.setDefaultTimeout(6000);
        client.connect(host, port);
        System.out.println("Connected to " + host + ".");
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        PrintWriter echoOutput =
            new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);

        BufferedReader echoInput =
            new BufferedReader(new InputStreamReader(client.getInputStream()));

        boolean start = true;
        while (start)
        {
            echoOutput.println("hello world");
            System.out.println(echoInput.readLine());
        }

        client.disconnect();
    }

    public static final void echoUDP(String host, int port) throws IOException
    {
        int length, count;
        byte[] data;
        String line;
        BufferedReader input;
        InetAddress address;
        EchoUDPClient client;

        input = new BufferedReader(new InputStreamReader(System.in));
        address = InetAddress.getByName(host);
        client = new EchoUDPClient();

        client.open(port);
        // If we don't receive an echo within 5 seconds, assume the packet is lost.
        client.setSoTimeout(5000);
        System.out.println("Ready to echo to " + host + ".");

        // Remember, there are no guarantees about the ordering of returned
        // UDP packets, so there is a chance the output may be jumbled.
        while ((line = input.readLine()) != null)
        {
            data = line.getBytes();
            client.send(data, address);
            count = 0;
            do
            {
                try
                {
                    length = client.receive(data);
                }
                // Here we catch both SocketException and InterruptedIOException,
                // because even though the JDK 1.1 docs claim that
                // InterruptedIOException is thrown on a timeout, it seems
                // SocketException is also thrown.
                catch (SocketException e)
                {
                    // We timed out and assume the packet is lost.
                    System.err.println(
                        "SocketException: Timed out and dropped packet");
                    break;
                }
                catch (InterruptedIOException e)
                {
                    // We timed out and assume the packet is lost.
                    System.err.println(
                        "InterruptedIOException: Timed out and dropped packet");
                    break;
                }
                System.out.print(new String(data, 0, length));
                count += length;
            }
            while (count < data.length);

            System.out.println();
        }

        client.close();
    }


    public static void main(String[] args) throws IOException {

        echoTCP("192.168.66.66", 514);
       // echoUDP("191.168.66.66", 514);

    }

}
