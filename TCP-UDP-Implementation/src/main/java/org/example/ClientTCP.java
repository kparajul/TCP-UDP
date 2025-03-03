package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ClientTCP {
     public static void main(String[] args) throws IOException {
        List<Integer> size = List.of(8,64,256,512);
        List<Double> time = new ArrayList<>();
        Socket socket = new Socket("pi.cs.oswego.edu", 26912);
        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();
        double rtt;
        byte[] message;
        byte[] encryptedMessage;
         byte[] decryptedMessage;
        for(int packetSize: size) {
            for (int i = 0; i < 30; i++) {
                //I can't understand why I had to put L in the end to indicate this value was a long even though it's declared as a long
                long sharedKey = 0x01AB44AB229867EFL;
                message = messageGenerator(packetSize);
                byte[] messageBack = new byte[packetSize];
                //System.out.println("Initial message: " + humanReadable(message));
                encryptedMessage = encryptionFunction(message, sharedKey); //encrypted
                //System.out.println("Encrypted message: " + humanReadable(encryptedMessage));

                //start time
                long startTime = System.nanoTime();
                //send data
                outputStream.write(encryptedMessage);
                outputStream.flush();
                //response
                int respSize = inputStream.read(messageBack);
                //System.out.println("received message: " + humanReadable(messageBack));
                if (respSize == packetSize) { //validating only using the size
                    long finalTime = System.nanoTime();
                    decryptedMessage = encryptionFunction(messageBack, sharedKey); //decrypted
                    //System.out.println("Decrypted received message: " + humanReadable(decryptedMessage));

                    if (Arrays.equals(message, decryptedMessage)) {
                        rtt = finalTime - startTime;
                        Double rttSecond = rtt / 1000000000;
                        time.add(rttSecond);

                    } else {
                        System.out.println("Messages don't match");
                    }
                    //System.out.println("RTT for " + byteSize + " bytes transfer is " + rttSecond + "seconds");
                } else {
                    System.out.println("Size mismatch");
                    socket.close();
                }

            }
            finalRTT(time, packetSize);
        }
        socket.close();
    }

    public static void finalRTT(List<Double> time, int byteSize){
         Double sum = 0.0;
        for( Double t : time){
            sum += t;
        }
        Double finalRTT = sum/(time.size());
        System.out.println("Average rtt for size " + byteSize + " is " + finalRTT + " seconds");
    }

    public static byte[] messageGenerator(int num){
        byte[] randomMessage = new byte[num];
        Random random = new Random();
        random.nextBytes(randomMessage);
        return randomMessage;
    }

    public static String humanReadable(byte[] message) {
        StringBuilder hex = new StringBuilder();
        for (byte b : message){
            hex.append(String.format("%02X", b));
        }
        return hex.toString().trim();
    }

    public static byte[] encryptionFunction(byte[] message, long sharedKey){
         byte[] returnMessage = message.clone();
        long key = sharedKey;
        for(int i = 0; i<returnMessage.length; i += 8){
            key = XORShift(key);
            //for every i (0-7), there will be j(0-7) so each i+j is 0-7 depending on i
            for(int j = 0; j < 8 && (i+j) < returnMessage.length; j++){
                //the j represents the position of the message we are trying to xor
                //shifting needs to be explained in person
                returnMessage[i+j] = (byte) (returnMessage[i+j] ^ (key >>> (8*(7-j)) & 0xFF));
            }

        }
        return returnMessage;
    }

    public static long XORShift(long r){
        r ^= r<<13;
        r ^= r>>7;
        r ^= r<<17;
        return r;
    }

}
