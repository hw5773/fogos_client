import FlexID.FlexID;
import FlexID.InterfaceType;
import FlexID.Locator;
import FogOSClient.FogOSClient;
import FogOSContent.Content;
import FogOSMessage.*;
import FogOSResource.Resource;
import FogOSService.Service;
import FogOSService.ServiceContext;
import sun.misc.Request;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class FogClient {
    private static FogOSClient fogos;
    private static final String rootPath = "D:\tmp";

    public static void main(String[] args) throws NoSuchAlgorithmException {
            // 1. Initialize the FogOSClient instance.
            // This will automatically build the contentStore inside the core,
            // a list of services, and a list of resources
            fogos = new FogOSClient(rootPath);

            // 2. Add manually resource, content, or service
            // 2-1. Add resources to be monitored
            Resource testResource = new Resource("Test", "", "hop", false) {
                @Override
                public void monitorResource() {
                    this.setCurr(Integer.toString(Integer.parseInt(this.getCurr()) + 1));
                    System.out.println("[Resource Test] " + this.getCurr() + " " + this.getUnit());
                }
            };

            fogos.addResource(testResource);

            // 2-2. Add content manually if any
            Content testContent = new Content("testContent", "D:\tmp\test.jpg", true);
            fogos.addContent(testContent);

            // 2-3. Add service to run
            KeyPairGenerator testServiceKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
            testServiceKeyPairGenerator.initialize(2048);
            KeyPair testServiceKeyPair = testServiceKeyPairGenerator.genKeyPair();
            Locator testServiceLoc = new Locator(InterfaceType.ETH, "127.0.0.1", 5550);
            ServiceContext testServiceCtx = new ServiceContext("FogClientTestService",
                    testServiceKeyPair, testServiceLoc, false, null);
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

            // 4. do something needed for this application
            QueryMessage query = fogos.makeQueryMessage("test");
            ReplyMessage reply = null;
            ArrayList<ReplyEntry> replyList;
            FlexID chosen, peer;
            RequestMessage request;
            ResponseMessage response;

            fogos.sendQueryMessage(query);
            do {
                reply = fogos.getReplyMessage();
            } while (reply == null);
            replyList = reply.getReplyList();

            for (ReplyEntry e : replyList) {
                id = e.getFlexID();
            }

            request = fogos.makeRequestMessage(chosen);
            do {
                response = fogos.getResponseMessage();
            } while (response == null);

            peer = response.getPeerID();



            // 5. finalize the FogOS interaction
            fogos.exit();
            System.out.println("[FogClient] FogOS Core quits.");
            System.exit(0);
    }
}