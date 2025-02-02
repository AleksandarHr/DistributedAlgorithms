package cs451;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

    private static void handleSignal(Parser parser, Process p) throws InterruptedException {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
        try {
        	FileWriter writer = new FileWriter(parser.output(), false);
        	writer.write(p.getOutput());
        	writer.close();
        	System.out.println("Written to output");
        } catch (IOException e) {
        	System.out.println("Unable to write to output");
        	e.printStackTrace();
        }
        
//        System.out.println(" ELAPSED TIME in ms = " + p.getElapsed());
//        System.out.println(" COUNT PL DELIVERED = " + p.countDelivered());
//        System.out.println(" COUNT URB DELIVERED = " + p.getUrb().countDelivered());
        p.killProcess();
    }

    private static void initSignalHandlers(Parser parser, Process p) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
					handleSignal(parser, p);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
    }

    public static void main(String[] args) throws InterruptedException, UnknownHostException {
        Parser parser = new Parser(args);
        parser.parse();
        
        String[] dependencies = {};
        int messageCount = 0;
        if (parser.hasConfig()) {
        	try {
        		File config = new File(parser.config());
        		Scanner myScanner = new Scanner(config);
        		messageCount = Integer.parseInt(myScanner.nextLine());
        		int processId = parser.myId();
        		int lineId = 0;
        		while (myScanner.hasNextLine()) {
        			lineId++;
        			String[] temp = myScanner.nextLine().trim().split(" ");        			
        			if (lineId == processId) {
        				dependencies = temp;
        			}
        		}
        		myScanner.close();
        	} catch (FileNotFoundException e) {
        		System.out.println("Unable to read config file.");
        		e.printStackTrace();
        	}
        }
        
//        ArrayList<String> dependencies = new ArrayList<String>();
//        dependencies.add(Integer.toString(parser.myId()));
//        int setForThis1 = 3;
//        int setForThis2 = 4;
//        if (setForThis1 == parser.myId()) {
//        	dependencies.add(Integer.toString(2));
//        	dependencies.add(Integer.toString(4));
//        }
//        if (setForThis2 == parser.myId()) {
//        	dependencies.add(Integer.toString(5));
//        }
//        int messageCount = 5;
        
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
        		p = new Process(InetAddress.getByName(host.getIp()), host.getPort(), host.getId(), messageCount);
        	}
        }
        p.setAllProcesses(addresses);
        p.setDependencies(dependencies);
        
        initSignalHandlers(parser, p);
        
        System.out.println("Barrier: " + parser.barrierIp() + ":" + parser.barrierPort());
        System.out.println("Signal: " + parser.signalIp() + ":" + parser.signalPort());
        System.out.println("Output: " + parser.output());

        Coordinator coordinator = new Coordinator(parser.myId(), parser.barrierIp(), parser.barrierPort(), parser.signalIp(), parser.signalPort());

        System.out.println("Waiting for all processes for finish initialization");
        coordinator.waitOnBarrier();

        System.out.println("Broadcasting " + messageCount + " messages...");
        
//        p.beginFifo();
        if (parser.myId() == 3) {
        	Thread.sleep(5 * 1000);
        }
        p.beginLcb();
        System.out.println(p.getDependencies().toString());
        System.out.println("Signaling end of broadcasting messages");
        coordinator.finishedBroadcasting();

        while (true) {
        	// Sleep for 1 hour
        	Thread.sleep(60 * 60 * 1000);
        }
    }
}
