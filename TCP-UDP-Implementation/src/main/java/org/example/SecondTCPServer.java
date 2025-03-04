package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.io.ByteArrayOutputStream;

public class SecondTCPServer {
    public static void main(String[] args) throws IOException {
        long sharedKey = 0x01AB44AB229867EFL;
        //byte[] receivedData = new byte[1048576];
        byte[] message = new byte[]{0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF};

        byte[] buffer = new byte[1024];
        int bytes;
        ByteArrayOutputStream receivedData = new ByteArrayOutputStream();

        ServerSocket serv = new ServerSocket(26914);
        Socket serverSocket = serv.accept();
        System.out.println("Connection established yipiee");
        InputStream inputStream = serverSocket.getInputStream();
        OutputStream outputStream = serverSocket.getOutputStream();
        //System.out.println(inputStream.readNBytes(1000000));
//

        byte[][] total = new byte[1024][1024];
        int index = 0;

        while((bytes = inputStream.read(buffer)) != -1){

            if(bytes == 1024){
                total[index] = Arrays.copyOf(buffer, bytes);
                index++;
                outputStream.write(message);
                outputStream.flush();
            }
        }
        for(byte[] t : total){
            System.out.println(getString(encryptionFunction(t, sharedKey)));
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
