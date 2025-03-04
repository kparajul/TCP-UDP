package org.example.UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ServerUDP {
    public static void main(String[] args) throws IOException {
        long sharedKey = 0x01AB44AB229867EFL;
        DatagramPacket received;
        DatagramPacket sent;
        byte[] decrypted;
        byte[] encrypted;
        byte[] buffer = new byte[512];
        int bytes;
        InetAddress addressClient;
        int portClient;

        DatagramSocket socket = new DatagramSocket(26915);
        System.out.println("Server started");

        while(true){
            received = new DatagramPacket(buffer, buffer.length);
            decrypted = encryptionFunction(received.getData(), sharedKey);
            encrypted = encryptionFunction(decrypted, sharedKey);
            addressClient = received.getAddress();
            portClient = received.getPort();
            socket.send(new DatagramPacket(encrypted, encrypted.length, addressClient, portClient));
            System.out.println(humanReadable(decrypted));
        }
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

    public static String humanReadable(byte[] message){
        StringBuilder hex = new StringBuilder();
        for (byte b : message){
            hex.append(String.format("%02X", b));
        }
        return hex.toString().trim();
    }

}
