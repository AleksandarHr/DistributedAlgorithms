package cs451;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        HashMap<InetSocketAddress, Integer> addressesToPids = new HashMap<InetSocketAddress, Integer>();
        HashMap<Integer, InetSocketAddress> pidsToAddresses = new HashMap<Integer, InetSocketAddress>();

        System.out.println("My id is " + parser.myId() + ".");
        System.out.println("List of hosts is:");
        Process p = null;
        for (Host host: parser.hosts()) {
    		System.out.println(host.getId() + ", " + host.getIp() + ", " + host.getPort());
        	InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName(host.getIp()), host.getPort());
        	addresses.add(addr);
        	addressesToPids.put(addr, host.getId());
        	pidsToAddresses.put(host.getId(), addr);
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
        p.setAddressesToPids(addressesToPids);
        p.setPidsToAddresses(pidsToAddresses);
        
    	for (int i = 2; i >= 1; i--) {
        	System.out.println("b " + i);
    		p.getFifo().fifoBroadcast("Hello " + i, i);
    	}
        for (int i = 3; i <= 4; i++) {
        	if (parser.myId() == 1) {
        		Thread.sleep(2 * 1000);
        	}
        	System.out.println("b " + i);
        	p.getFifo().fifoBroadcast("Hello " + i, i);
        }
        for (int i = 8; i >= 5; i--) {
        	if (parser.myId() == 2) {
        		Thread.sleep(2 * 1000);
        	}
        	System.out.println("b " + i);
        	p.getFifo().fifoBroadcast("Hello " + i, i);
        }

        System.out.println("Signaling end of broadcasting messages");
        coordinator.finishedBroadcasting();

        while (true) {
        	// Sleep for 1 hour
        	Thread.sleep(60 * 60 * 1000);
        }
    }
}
