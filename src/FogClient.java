import FlexID.FlexID;
import FlexID.InterfaceType;
import FlexID.FlexIDFactory;
import FlexID.Locator;
import FogOSClient.FogOSClient;
import FogOSContent.Content;
import FogOSMessage.*;
import FogOSResource.Resource;
import FogOSResource.ResourceType;
import FogOSSecurity.Role;
import FogOSSecurity.SecureFlexIDSession;
import FogOSService.Service;
import FogOSService.ServiceContext;
import FogOSService.ServiceType;

import java.io.IOException;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;

import java.net.URL;
import javax.net.ssl.HttpsURLConnection;


public class FogClient {
    private static FogOSClient fogos;
    //private static final String rootPath = "C:\\Users\\HMLEE\\FogOS";
    private static final String rootPath = "D:\\tmp";


    public static void main(String[] args) throws Exception {
            // 1. Initialize the FogOSClient instance.
            // This will automatically build the contentStore inside the core,
            // a list of services, and a list of resources
            fogos = new FogOSClient(rootPath);

            // 2. Add manually resource, content, or service
            // 2-1. Add resources to be monitored

            Resource testResource = new Resource("Test", ResourceType.NetworkInterface,
                "", "hop", false) {
                @Override
                public void monitorResource() {
                    this.setCurr(Integer.toString(Integer.parseInt(this.getCurr()) + 1));
                    System.out.println("[Resource Test] " + this.getCurr() + " " + this.getUnit());
                }
            };

        // 2-2. Add content manually if any
            //Content testContent = new Content("test.txt", "C:\\Users\\HMLEE\\FogOS\\test.txt", true, "Hash");
            Content testContent = new Content("test.png", "D:\\tmp\\test.png", true, "SHA1-HASH");
            fogos.addContent(testContent);

            // 2-3. Add service to run
            KeyPairGenerator testServiceKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
            testServiceKeyPairGenerator.initialize(2048);
            KeyPair testServiceKeyPair = testServiceKeyPairGenerator.genKeyPair();
            Locator testServiceLoc = new Locator(InterfaceType.ETH, "127.0.0.1", 5550);
            ServiceContext testServiceCtx = new ServiceContext("FogClientTestService",
                    ServiceType.Streaming, testServiceKeyPair, testServiceLoc, false, null);
            Service testService = new Service(testServiceCtx) {
                @Override
                public void processInputFromPeer() {
                    System.out.println("[FogOSService] Process Input from Peer");
                }

                @Override
                public void processOutputToPeer() {
                    System.out.println("[FogOSService] Process Output to Peer");
                }
            };

            fogos.addService(testService);

            // 3. begin the FogOS interaction
            fogos.begin();

            System.out.println("[FogClient] FogOS Core begins.");

            // Explicitly register Content
            HashMap<String, String> attributes = new HashMap<String, String>();
            attributes.put("title", "TestContent");
            attributes.put("desc", "Test");
            fogos.registerContent(testContent, attributes);
           //fogos.registerContent("test.jpg", "D:\\tmp\\test.jpg");

            // Explicitly register service
            //fogos.registerService(testService);

            // 4. do something needed for this application
            QueryMessage query = fogos.makeQueryMessage("Content", "Video", "resolution", true, 10);
            query.setAttribute("keyword", "soccer", "", "");

            ReplyMessage reply = null;
            ArrayList<ReplyEntry> replyList;
            FlexID chosen, peer;
            RequestMessage request;
            ResponseMessage response;
            SecureFlexIDSession session;

            fogos.sendQueryMessage(query);
            do {
                reply = fogos.getReplyMessage();
            } while (reply == null);
            replyList = reply.getReplyList();
            chosen = replyList.get(0).getFlexID();

            request = fogos.makeRequestMessage(chosen);
            fogos.proxyRequestMessage(request);

            do {
                response = fogos.getResponseMessage();
            } while (response == null);

            FlexIDFactory factory = new FlexIDFactory();
            FlexID id = factory.generateDeviceID();
            peer = response.getPeerID();
            System.out.println("IP: " + peer.getLocator().getAddr() + ":" + peer.getLocator().getPort());
            session = new SecureFlexIDSession(Role.INITIATOR, id, peer);
            int ret = session.doHandshake(0);


            String a = "GET  /dash/test_input.mp4  HTTP/1.1 \nConnection: keep-alive\r\nHost: 52.78.23.173\r\n\n\n";
            int rcvd = -1;
            //a = "GET DASH\n";
            session.send(a);

            byte[] buf = new byte[20000];
            while (true) {
                //System.out.println("BBBBBB");
                rcvd = session.recv(buf, 20000);

                if (rcvd > 0) {
                    /*
                    System.out.print("First 5 bytes: " + buf[0] + " " + buf[1] + " " + buf[2] + " " + buf[3] + " " + buf[4]);
                    System.out.println();

                    System.out.print("Last 5 bytes: " + buf[rcvd-5] + " " + buf[rcvd-4] + " " + buf[rcvd-3] + " " + buf[rcvd-2] + " " + buf[rcvd-1]);
                    System.out.println();
                    Thread.sleep(500);
                    */
                    System.out.println(new String(buf).trim());
                }
            }

        // 5. finalize the FogOS interaction
            //fogos.exit();
            //System.out.println("[FogClient] FogOS Core quits.");
            //System.exit(0);
    }
}