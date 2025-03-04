package org.example.UDP;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ClientUDP {

    public static void main(String[] args) throws IOException {
        long sharedKey = 0x01AB44AB229867EFL;
        List<Integer> size = List.of(8,64,256,512);
        List<Double> time = new ArrayList<>();
        long startTime;
        long endTime;
        double rtt;


        DatagramSocket socket = new DatagramSocket();
        InetAddress addressServer = InetAddress.getByName("localhost");
        int portServer = 26915;

        byte[] message;
        DatagramPacket received;
        byte[] encryptedMessage;

        byte[] decryptedMessage;

        for(int packetSize : size){
            for (int i=0; i<30; i++) {
                byte[] buffer = new byte[packetSize];
                message = messageGenerator(packetSize);
                encryptedMessage = encryptionFunction(message, sharedKey);
                startTime = System.nanoTime();
                socket.send(new DatagramPacket(encryptedMessage, encryptedMessage.length, addressServer, portServer));
                received = new DatagramPacket(buffer, buffer.length);
                socket.receive(received);
                decryptedMessage = encryptionFunction(received.getData(), sharedKey);
                if (Arrays.equals(message, decryptedMessage)) {
                    endTime = System.nanoTime();
                    rtt = (double) (endTime - startTime) / 1000000; //seconds
                    time.add(rtt);

                } else {
                    System.out.println("Messages don't match");
                    socket.close();
                    break;
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
