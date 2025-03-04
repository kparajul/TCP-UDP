package org.example.TCP;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.io.ByteArrayOutputStream;

public class SecondTCPServer {
    public static void main(String[] args) throws IOException {
        long sharedKey = 0x01AB44AB229867EFL;
        //byte[] receivedData = new byte[1048576];
        byte[] message = new byte[8];

        byte[] buffer = new byte[1024];
        int bytes;
        ByteArrayOutputStream receivedData = new ByteArrayOutputStream();

        ServerSocket serv = new ServerSocket(26914);
        Socket serverSocket = serv.accept();
        System.out.println("Connection established yipiee");
        InputStream inputStream = serverSocket.getInputStream();
        OutputStream outputStream = serverSocket.getOutputStream();

        byte[][] total1024 = new byte[1024][1024];
        byte[][] total512 = new byte[2048][512];
        byte[][] total256 = new byte[4096][256];


        int index1024 = 0;
        int index512 = 0;
        int index256 = 0;

        while((bytes = inputStream.read(buffer)) != -1){

            if(bytes == 1024){
                total1024[index1024] = Arrays.copyOf(buffer, bytes);
                message = ByteBuffer.allocate(8).putInt(index1024+1).putInt(index1024+1).array();
                index1024++;
            } else if(bytes == 512){
                total512[index512] = Arrays.copyOf(buffer, bytes);
                message = ByteBuffer.allocate(8).putInt(index512+1).putInt(index512+1).array();
                index512++;
            } else if(bytes == 256){
                total256[index256] = Arrays.copyOf(buffer, bytes);
                message = ByteBuffer.allocate(8).putInt(index256+1).putInt(index256+1).array();
                index256++;
            }

            outputStream.write(message);
            outputStream.flush();

        }

        for(byte[] t : total1024){
            System.out.println("1024 " + getString(encryptionFunction(t, sharedKey)));
        }
        for(byte[] t : total512){
            System.out.println("512 " + getString(encryptionFunction(t, sharedKey)));
        }
        for(byte[] t : total256){
            System.out.println("256 " + getString(encryptionFunction(t, sharedKey)));
        }
        System.out.println("Client disconnected");
        serverSocket.close();
        serv.close();
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
