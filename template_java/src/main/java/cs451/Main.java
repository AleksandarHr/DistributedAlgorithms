package cs451;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Main {

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException, UnknownHostException {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID is " + pid + ".");
        System.out.println("Use 'kill -SIGINT " + pid + " ' or 'kill -SIGTERM " + pid + " ' to stop processing packets.");
        ArrayList<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();
        System.out.println("My id is " + parser.myId() + ".");
        System.out.println("List of hosts is:");
        Process p = null;
        for (Host host: parser.hosts()) {
    		System.out.println(host.getId() + ", " + host.getIp() + ", " + host.getPort());
        	InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName(host.getIp()), host.getPort());
        	addresses.add(addr);
    		if (host.getId() == parser.myId()) {
    			System.out.println("It's me!!");
        		p = new Process(InetAddress.getByName(host.getIp()), host.getPort(), host.getId());
        	}
        }

        System.out.println("Barrier: " + parser.barrierIp() + ":" + parser.barrierPort());
        System.out.println("Signal: " + parser.signalIp() + ":" + parser.signalPort());
        System.out.println("Output: " + parser.output());
        // if config is defined; always check before parser.config()
        if (parser.hasConfig()) {
            System.out.println("Config: " + parser.config());
        }


        Coordinator coordinator = new Coordinator(parser.myId(), parser.barrierIp(), parser.barrierPort(), parser.signalIp(), parser.signalPort());

        System.out.println("Waiting for all processes for finish initialization");
        coordinator.waitOnBarrier();

        System.out.println("Broadcasting messages...");
        p.setAllProcesses(addresses);

        if (parser.myId() == 1) {
        	System.out.println("Process " + parser.myId() + " broadcasting message \"Hello\"");
        	p.getBeb().bebBroadcast("Hello", 1);
        }
        System.out.println("Signaling end of broadcasting messages");
        coordinator.finishedBroadcasting();

        while (true) {
        	// Sleep for 1 hour
        	Thread.sleep(60 * 60 * 1000);
        }
    }
}
