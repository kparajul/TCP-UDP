package org.example.UDP;

import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SecondUDPServer {

    public static void main(String[] args) throws IOException {
        long sharedKey = 0x01AB44AB229867EFL;
        byte[] message = new byte[8];
        byte[][] total1024 = new byte[1024][1024];
        byte[][] total512 = new byte[2048][512];
        byte[][] total256 = new byte[4096][256];


        int index1024 = 0;
        int index512 = 0;
        int index256 = 0;
        byte[] buffer = new byte[1024];


        DatagramSocket socket = new DatagramSocket(26916);
        System.out.println("UDP Server started");
        DatagramPacket received = new DatagramPacket(buffer, buffer.length);

        while (true){
            socket.receive(received);
            byte[] realData = Arrays.copyOf(received.getData(), received.getLength());

            int realLength = received.getLength();

            if(realLength == 1024){
                total1024[index1024] = realData;
                message = ByteBuffer.allocate(8).putInt(index1024+1).putInt(index1024+1).array();
                index1024++;
            } else if(realLength == 512){
                total512[index512] = realData;
                message = ByteBuffer.allocate(8).putInt(index512+1).putInt(index512+1).array();
                index512++;
            } else if(realLength == 256){
                total256[index256] = realData;
                message = ByteBuffer.allocate(8).putInt(index256+1).putInt(index256+1).array();
                index256++;
            } else{
                System.out.println("There seems to be a problem in packet size");
            }
            InetAddress address = received.getAddress();
            int port = received.getPort();
            socket.send(new DatagramPacket(message, message.length, address, port));
            if ((index256 == 4096 ) && (index512 == 2048) && (index1024  == 1024)){
                System.out.println("socket closing, expected stuff received");
                socket.close();
                break;
            }
        }
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
