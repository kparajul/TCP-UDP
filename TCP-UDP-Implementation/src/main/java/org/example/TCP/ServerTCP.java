package org.example.TCP;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class ServerTCP {
    public static void main(String[] args) throws IOException {
        long sharedKey = 0x01AB44AB229867EFL;
        byte[] received;
        byte[] decrypted;
        byte[] encrypted;
        byte[] buffer = new byte[512];
        int bytes;

        ServerSocket serv = new ServerSocket(26914);
        Socket serverSocket = serv.accept();
        System.out.println("Connection established yipiee");

        InputStream inputStream = serverSocket.getInputStream();
        OutputStream outputStream = serverSocket.getOutputStream();

        while ((bytes = inputStream.read(buffer)) != -1) {
            received = Arrays.copyOf(buffer, bytes);
            decrypted = encryptionFunction(received, sharedKey);
            encrypted = encryptionFunction(decrypted, sharedKey);
            outputStream.write(encrypted);
            outputStream.flush();
        }
        System.out.println("Client disconnected");
        serverSocket.close();
        serv.close();
    }

    public static String humanReadable(byte[] message){
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
