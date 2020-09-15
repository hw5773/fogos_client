import FlexID.FlexID;
import FlexID.InterfaceType;
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
import sun.misc.Request;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;


public class FogClient {
    private static FogOSClient fogos;
    //private static final String rootPath = "C:\\Users\\HMLEE\\FogOS";
    private static final String rootPath = "D:\\tmp";


    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException, InvalidKeySpecException, SignatureException, InvalidKeyException {
            // 1. Initialize the FogOSClient instance.
            // This will automatically build the contentStore inside the core,
            // a list of services, and a list of resources
            fogos = new FogOSClient(rootPath);

            // 2. Add manually resource, content, or service
            // 2-1. Add resources to be monitored
        /*
            Resource testResource = new Resource("Test", ResourceType.NetworkInterface,
                "", "hop", false) {
                @Override
                public void monitorResource() {
                    this.setCurr(Integer.toString(Integer.parseInt(this.getCurr()) + 1));
                    System.out.println("[Resource Test] " + this.getCurr() + " " + this.getUnit());
                }
            };
         */

        /*
            Resource ifaceResource = new Resource("ifaceType", ResourceType.NetworkInterface,
                    "", "Ethernet", false) {
                @Override
                public void monitorResource() {

                }
            };
            Resource hwAddrResource = new Resource("hwAddress", ResourceType.NetworkInterface,
                "", "20-12-A3-B2-8J-KU", false) {
                @Override
                public void monitorResource() {

                }
            };
            Resource ipResource = new Resource("ipv4", ResourceType.NetworkInterface,
                "", "1.2.3.4", false) {
                @Override
                public void monitorResource() {

                }
            };

            fogos.addResource(ifaceResource);
            fogos.addResource(hwAddrResource);
            fogos.addResource(ipResource);

         */

        // 2-2. Add content manually if any
            //Content testContent = new Content("testContent", "C:\\Users\\HMLEE\\FogOS\\test.txt", true, "Hash");
            Content testContent = new Content("test.jpg", "D:\\tmp\\test.jpg", true, "SHA1-HASH");
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

            // Explicitly register Content
           //fogos.registerContent("test.txt", "C:\\Users\\HMLEE\\FogOS\\test.txt");
           fogos.registerContent("test.jpg", "D:\\tmp\\test.jpg");

            System.out.println("[FogClient] FogOS Core begins.");
            //System.exit(0);

            // 4. do something needed for this application
            QueryMessage query = fogos.makeQueryMessage("test");
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
            fogos.sendRequestMessage(request);

            do {
                response = fogos.getResponseMessage();
            } while (response == null);

            peer = response.getPeerID();
            session = new SecureFlexIDSession(Role.INITIATOR, peer);

            session.send("Test Message!");

            // 5. finalize the FogOS interaction
            fogos.exit();
            System.out.println("[FogClient] FogOS Core quits.");
            System.exit(0);
    }
}