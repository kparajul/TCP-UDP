package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Random;

public class ClientTCP {
    public static void main(String[] args) throws IOException {
        int byteSize = 512;
        //I can't understand why I had to put L in the end to indicate this value was a long even though it's declared as a long
        long sharedKey = 0x01AB44AB229867EFL;
        byte[] message = messageGenerator(byteSize);

        System.out.println("Unencrypted message: " + humanReadable(message));
        message = encryptionFunction(message, sharedKey); //encrypting
        System.out.println("Encrypted message: " + humanReadable(message));

        byte[] messageBack = new byte[byteSize];
        double rtt;

        Socket socket = new Socket("localhost", 8080);
        System.out.println("Connection established");
        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();
        long startTime = System.nanoTime();
        outputStream.write(message);
        outputStream.flush();
        int respSize = inputStream.read(messageBack);
        if(respSize == byteSize){ //validating only using the size
            long finalTime = System.nanoTime();
            socket.close();
            System.out.println("Received encrypted message:" + humanReadable(messageBack));
            messageBack = encryptionFunction(messageBack, sharedKey); //decrypting
            System.out.println("Received decrypted message:" + humanReadable(messageBack));
            rtt = finalTime-startTime;
            System.out.println(rtt);
        }else {
            System.out.println("Size mismatch");
            socket.close();
        }
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
        long key = sharedKey;
        for(int i = 0; i<message.length; i += 8){
            key = XORShift(key);
            //for every i (0-7), there will be j(0-7) so each i+j is 0-7 depending on i
            for(int j = 0; j < 8 && (i+j) < message.length; j++){
                //the j represents the position of the message we are trying to xor
                //shifting needs to be explained in person
                message[i+j] = (byte) (message[i+j] ^ (key >>> (8*(7-j)) & 0xFF));
            }

        }
        return message;
    }

    public static long XORShift(long r){
        r ^= r<<13;
        r ^= r>>7;
        r ^= r<<17;
        return r;
    }

}
