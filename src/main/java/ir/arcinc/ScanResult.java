package ir.arcinc;

/**
 * Created by taha on 10/23/2016.
 */
public class ScanResult{
    byte[] data;
    int size;

    ScanResult(byte[] data, int size) {
        this.data = data;
        this.size = size;
    }
}