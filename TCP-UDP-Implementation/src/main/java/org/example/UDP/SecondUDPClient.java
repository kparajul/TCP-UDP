package org.example.UDP;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class SecondUDPClient {
    public static void main(String[] args) throws IOException {
        long sharedKey = 0x01AB44AB229867EFL;
        List<Integer> size = List.of(1024, 512, 256);
        List<Double> time1024 = new ArrayList<>();
        List<Double> time512 = new ArrayList<>();
        List<Double> time256 = new ArrayList<>();
        InetAddress address = InetAddress.getByName("moxie.cs.oswego.edu");
        int port = 26916;

        byte[] message;
        byte[] encryptedMessage;
        byte[] response = new byte[8];
        int totalPackets;

        DatagramSocket socket = new DatagramSocket(26916);

        for(int packetSize: size){
            if(packetSize==1024){
                totalPackets = 1024;
            } else if (packetSize==512) {
                totalPackets = 2048;
            }else{
                totalPackets = 4096;
            }

            byte[][] packets = new byte[totalPackets][packetSize];

            for(int i=0; i<totalPackets; i++){
                message=messageGenerator(packetSize);
                packets[i] = encryptionFunction(message,sharedKey);
            }
            for(int i = 0; i<totalPackets; i++) {
                long startTime = System.nanoTime();
                socket.send(new DatagramPacket(packets[i], packets[i].length, address, port));
                DatagramPacket receivedPacket = new DatagramPacket(response, response.length);
                socket.receive(receivedPacket);
                int temp = i+1;
                int received = ByteBuffer.wrap(receivedPacket.getData()).getInt();

                if(received != temp){
                    System.out.println(received);
                    System.out.println(temp);
                    System.out.println("Wrong response");
                }
                long endTime = System.nanoTime();
                double diff = (double) (endTime - startTime);
                if (packetSize == 1024) time1024.add(diff);
                else if (packetSize == 512) time512.add(diff);
                else if (packetSize == 256) time256.add(diff);
            }


        }
        socket.close();

        System.out.println("Throughput for 1024 bytes is " + calculateThroughput(1024 * 1024, time1024) + " bits per second");
        System.out.println("Throughput for 512 bytes is " + calculateThroughput(512 * 2048, time512) + " bits per second");
        System.out.println("Throughput for 256 bytes is " + calculateThroughput(256 * 4096, time256) + " bits per second");


    }

    public static Double calculateThroughput(Integer byteSize, List<Double> time){
        Double sum = 0.0;
        for( Double t : time){
            sum += t;
        }
        return  (byteSize * 8)/(sum/1000000000);
    }

    public static byte[] messageGenerator(int num){
        byte[] message = new byte[num];
        int count = 0;
        for (int a =0; a<num; a+=4){
            byte[] intByte = toByte(count++);
            System.arraycopy(intByte, 0, message, a, 4);
        }
        return message;
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
    public static byte[] toByte(int num){
        return new byte[] {
                (byte) (num >> 24),
                (byte) (num >> 16),
                (byte) (num >> 28),
                (byte) (num)
        };
    }
}
