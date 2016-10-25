package ir.arcinc;

import com.suprema.BioMiniSDK;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ir.arcinc.Commons.*;

/**
 * Created by taha on 10/14/2016.
 */
public class ServerMain {

    private static BioMiniSDK scanner = new BioMiniSDK();

    public static void main(String[] args) throws Exception {
        if (scanner.UFS_Init() != 0) {
            throw new Exception("Cannot init");
        }
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 6985;
        ExecutorService pool = Executors.newCachedThreadPool();
        ServerSocket socket = new ServerSocket(port);
        System.out.println("Starting to listen on port: " + port);
        while (true){
            try {
                Socket client = socket.accept();
                System.out.println("Client connected: " + client.getRemoteSocketAddress());
                pool.submit(new ConnectionHandler(client));
            } catch (SocketException e){

            }
        }
    }

    private static class ConnectionHandler implements Runnable{

        private Socket client;

        public ConnectionHandler(Socket socket) {
            this.client = socket;
        }

        @Override
        public void run() {
            try (DataInputStream dataInputStream = new DataInputStream(client.getInputStream());
                 DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());
            ) {
                int choice = dataInputStream.readInt();
                switch (choice) {
                    case 1: // Enroll
                        try {
                            ScanResult res = Scan();
                            dataOutputStream.writeInt(1); // OK
                            dataOutputStream.writeInt(res.size);
                            dataOutputStream.write(res.data, 0, 1024);
                        } catch (Exception e) {
                            dataOutputStream.writeInt(-1); //Error
                        } finally {
                            dataOutputStream.flush();
                        }
                        break;
                    case 2: //Verify
                        try {
                            ScanResult r1 = GetEnrollDataFromSocket(dataInputStream);
                            ScanResult r2 = GetEnrollDataFromSocket(dataInputStream);
                            if (Verify(r1, r2))
                                dataOutputStream.writeInt(1); //Match
                            else
                                dataOutputStream.writeInt(0); //NotMatch
                        } catch (Exception e) {
                            dataOutputStream.writeInt(-1); //Error
                        } finally {
                            dataOutputStream.flush();

                        }
                        break;
                }
            } catch (IOException e) {

            } finally {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static synchronized ScanResult Scan() throws Exception {
        long[] hScanner = new long[1];

        hScanner=GetCurrentScannerHandle();

        if(hScanner!=null) {

            scanner.UFS_ClearCaptureImageBuffer(hScanner[0]);

            System.out.println("Place finger...");

            if (scanner.UFS_CaptureSingleImage(hScanner[0]) == 0) {

                System.out.println("Remove finger...");

                byte[] bTemplate = new byte[1024];

                int[] refTemplateSize = new int[1];

                int[] refTemplateQuality = new int[1];

                byte[] byteTemplateArray = new byte[1024];

                try {
                    if (scanner.UFS_ExtractEx(hScanner[0], 1024, bTemplate, refTemplateSize, refTemplateQuality) == 0) {

                        System.arraycopy(bTemplate, 0, byteTemplateArray, 0, refTemplateSize[0]);//byte[][]

                        return new ScanResult(byteTemplateArray, refTemplateSize[0]);

                    }
                } catch (Exception ignored) {

                }
            }
        }
        throw new Exception("Could not scan");
    }

    public static long[] GetCurrentScannerHandle()
    {
        long[] hScanner = new long[1];
        int nRes =0;
        int[] nNumber = new int[1];

        nRes = scanner.UFS_GetScannerNumber(nNumber);

        if(nRes==0){

            if(nNumber[0] <=0){

                return null;
            }

        }else{

            return null;
        }

        int index = 0;

        nRes = scanner.UFS_GetScannerHandle(index,hScanner);

        if(nRes ==0 && hScanner!=null){
            return hScanner;
        }
        return null;
    }

    public static int Match(List<ScanResult> scans, ScanResult to){
        for (int i = 0; i < scans.size(); i++)
            if (Verify(to, scans.get(i)))
                return i;
        return -1;
    }

    public static boolean Verify(ScanResult a, ScanResult b){

        long[] hMatcher = new long[1];
        int[] refVerify = new int[1];
        scanner.UFM_Create(hMatcher);

        scanner.UFM_Verify(hMatcher[0], a.data, a.size, b.data, b.size, refVerify);
        return refVerify[0] == 1;
    }
}
