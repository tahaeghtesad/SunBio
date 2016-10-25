package ir.arcinc;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by taha on 10/23/2016.
 */
public class Commons {
    public static ScanResult GetEnrollDataFromSocket(DataInputStream dataInputStream) throws IOException {
        int size = dataInputStream.readInt();
        byte[] data = new byte[1024];
        int readCount = 0;
        while (readCount<size) {
            readCount += dataInputStream.read(data, readCount, 1024);
        }
        return new ScanResult(data,size);
    }
}
