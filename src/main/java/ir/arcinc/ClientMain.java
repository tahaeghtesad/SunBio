package ir.arcinc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by taha on 10/23/2016.
 */
public class ClientMain {
    private static String address;
    private static int port;
    public static void main(String[] args) throws IOException {
        port = args.length > 1 ? Integer.parseInt(args[1]) : 6985;
        address = args.length > 1 ? args[0] : "localhost";
        Scanner console = new Scanner(System.in);
        ArrayList<ScanResult> scans = new ArrayList<>();

        while (true){
            try {
                System.out.println("1. Enroll");
                System.out.println("2. Verify");
                System.out.println("3. Match");
                int choice = console.nextInt();
                switch (choice) {
                    case 1:
                        ScanResult res = Enroll();
                        scans.add(res);
                        System.out.println("Scan added.");
                        break;
                    case 2:
                        System.out.println("Total scans: " + scans.size() + "\n Enter two fingerprint enrollIds to match: ");
                        int enrollId1 = console.nextInt();
                        int enrollId2 = console.nextInt();

                        if (Match(scans.get(enrollId1),scans.get(enrollId2)))
                            System.out.println("Match");
                        else
                            System.out.println("Not match");

                        break;
                    case 3:
                        ScanResult enrollee = Enroll();
                        for (int i = 0; i < scans.size(); i++){
                            try {
                                if (Match(scans.get(i), enrollee))
                                    System.out.println(i + " ~> Match");
                                else System.out.println(i + " ~> Not Match");
                            } catch (Exception e1) {
                                System.out.println(i + " ~> Failure");
                            }
                        }
                        break;
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private static ScanResult Enroll() throws Exception {
        try (Socket socket = new Socket(address, port);
             DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
             DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        ) {
            dataOutputStream.writeInt(1);
            dataOutputStream.flush();
            int status = dataInputStream.readInt();
            if (status == -1)
                throw new Exception("Scan failed");
            return Commons.GetEnrollDataFromSocket(dataInputStream);
        }
    }
    private static boolean Match(ScanResult r1, ScanResult r2) throws IOException, Exception {
        try (Socket socket = new Socket(address, port);
             DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
             DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        ) {
            dataOutputStream.writeInt(2);
            dataOutputStream.writeInt(r1.size);
            dataOutputStream.write(r1.data, 0, 1024);
            dataOutputStream.writeInt(r2.size);
            dataOutputStream.write(r2.data, 0, 1024);
            dataOutputStream.flush();
            int status = dataInputStream.readInt();
            if (status == -1)
                throw new Exception("Matching failed");
            else if (status == 0)
                return false;
            else if (status == 1)
                return true;
            return false;
        }
    }
}
