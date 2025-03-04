package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SecondTCPClient {
    public static void main(String[] args) throws IOException {
        List<Integer> size = List.of(1024, 512, 256);
        List<Double> time1024 = new ArrayList<>();
        List<Double> time512 = new ArrayList<>();
        List<Double> time256 = new ArrayList<>();
        Socket socket = new Socket("localhost", 26914);
        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();
        long sharedKey = 0x01AB44AB229867EFL;
        byte[] message;
        byte[] response = new byte[8];
        for(int packetSize: size) {
            for (int i = 0; i < 1; i++) {
                if(packetSize == 1024){
                    byte[][] packets = new byte[1024][1024];
                    for(int x=0; x<1024; x++){
                        message = messageGenerator(1024);
                        packets[x] = encryptionFunction(message, sharedKey);
                    }

                    long start1024 = System.nanoTime();
                    for (byte[] packet: packets){
                        //System.out.println(getString(packet));
                        outputStream.write(packet);
                        outputStream.flush();
                        response = inputStream.readNBytes(8);
                        //System.out.println(humanReadable(response));
                    }
                    long end1024 = System.nanoTime();
                    double difference = end1024-start1024;
                    time1024.add(difference);

                }else if(packetSize == 512){
                    byte[][] packets = new byte[2048][512];
                    for(int x=0; x<2048; x++){
                        message = messageGenerator(512);
                        packets[x] = encryptionFunction(message, sharedKey);
                    }

                    long start512 = System.nanoTime();
                    for (byte[] packet: packets){
                        //System.out.println(getString(packet));
                        outputStream.write(packet);
                        outputStream.flush();
                        response = inputStream.readNBytes(8);
                        //System.out.println(humanReadable(response));
                    }
                    long end512 = System.nanoTime();
                    double difference = end512-start512;
                    time512.add(difference);

                }else if(packetSize == 256){
                    byte[][] packets = new byte[4096][256];
                    for(int x=0; x<4096; x++){
                        message = messageGenerator(256);
                        packets[x] = encryptionFunction(message, sharedKey);
                    }

                    long start256 = System.nanoTime();
                    for (byte[] packet: packets){
                        //System.out.println(getString(packet));
                        outputStream.write(packet);
                        outputStream.flush();
                        response = inputStream.readNBytes(8);
                        //System.out.println(humanReadable(response));
                    }
                    long end256 = System.nanoTime();
                    double difference = end256-start256;
                    time256.add(difference);

                }

            }
        }
        //System.out.println(humanReadable(response));
        Double throughput1024 = calculateThroughput(1024, time1024);
        Double throughput512 = calculateThroughput(512, time512);
        Double throughput256 = calculateThroughput(256, time256);
        System.out.println("Throughput for 1024 bytes is " + throughput1024);
        System.out.println("Throughput for 512 bytes is " + throughput512);
        System.out.println("Throughput for 256 bytes is " + throughput256);
        socket.close();
    }

    public static Double calculateThroughput(Integer packetSize, List<Double> time){
        Double sum = 0.0;
        for( Double t : time){
            sum += t;
        }
        Double throughput = (packetSize * 8)/(sum);
        return throughput;
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

    public static byte[] toByte(int num){
        return new byte[] {
                (byte) (num >> 24),
                (byte) (num >> 16),
                (byte) (num >> 28),
                (byte) (num)
        };
    }

    public static String humanReadable(byte[] message){
        StringBuilder hex = new StringBuilder();
        for (byte b : message){
            hex.append(String.format("%02X", b));
        }
        return hex.toString().trim();
    }


    public static String getString(byte[] message) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < message.length; i += 4) {
            int number = ((message[i] & 0xFF) << 24) | ((message[i+1] & 0xFF) << 16) |
                    ((message[i+2] & 0xFF) << 8) | (message[i+3] & 0xFF);
            result.append(number).append(" ");
        }
        return result.toString().trim();
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
