package org.bootcamp.Thisfunctionals.ChatGroup;

import java.io.*;
import java.net.Socket;

import java.util.ArrayList;

/* runnable is implemented on a class whose instances will be executed by a separate thread
this is vital because if we did not spawn a new thread to handle the connection with each new client
our  app would only be handle one client at a time */
public class ClientHandler implements Runnable {

    /* We create the array to keep track of our clients, so that whenever a client sends a message we loop to the array
     * of clients and send the message to each client. Responsible for us to allow communication or send messages or
     * broadcast a message to multiple clients instead of just one or just the server, and we want it to be static
     * we want it to belong to the class, not each object of the class */
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    private Socket socket; //used to establish a connection between the client and the server
    private BufferedReader bufferedReader; //Read data, messages sent from the client
    private BufferedWriter bufferedWriter; /*Sent data, messages to our client. These messages have been sent from other
     * clients that will then be broadcast by using this array list*/
    private String clientName; //name of client

    public ClientHandler(Socket socket) {
        /*means this is the object that is being made from this class, for that object (this) is being made set the
         * socket of it equal to what is passed into the class Server as a constructor inside the instance clientHandler*/
        try {
            /*each socket has OutputStream that sends data(message) to whatever you are connected to and an InputStream
             * that you can use to read data to whatever you are connected to had sent to you */
            this.socket = socket;

          /* We want a character stream because we are sending messages, in java character streams end with the word writer,
          while byte streams end with the word stream this is why we wrapp our OutputStream in a byte stream because,
          we want to send over characters*/
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //This stream reads messages from the bufferWriter(that sends messages)
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            this.clientName = bufferedReader.readLine(); /* we read from a BufferReader, we are reading a line, because
          when a client presses enter a string will send over along with a new line character, we want to read from
          the stream up until a new line character, which means the user has pressed the enter key*/

            clientHandlers.add(this); /* Add the client to the array list, so they can be a part of the group chat and
           receive messages from other users */

            /* Now we want to send a message to any connected client that a new user has joined the chat, along with
             * the use's username */
            broadcastMessage("[SERVER] " + clientName + " has entered the chat!");

            /* close down everything with a method to avoid nested try/catch boxes */
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    @Override
    public void run () {
        /*Everything in this run method is what is running one a separate thread, here we listen from messages, because
        is a blocking operation which means that the program will be stuck until
        * the operation is completed so if we weren't using multiple threads our program would be stuck waiting for
        * a message from the client. So instead we have a separate thread waiting for messages and another one working
        * with the rest of our app because if not our program would be sitting here, waiting for messages to come in
        * what we want to do is to be able to send messages, we don't want to wait for someone to send a message
        * before we can actually send one */
        String messageFromClient;

        while (socket.isConnected()){
            try{
                messageFromClient = bufferedReader.readLine(); /*o the program will hold here until we receive
                a message from a client and this is why we want to run on a separate thread, cause this is a blocking*/
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break; //it means when the client disconnect this will break us out of the while loop
            }
        }
    }
    public void broadcastMessage(String messageToSend){
        //for each client handler in our array list this will represent each client handler each time for each iteration
        for (ClientHandler clientHandler : clientHandlers){
            try{
                if(!clientHandler.clientName.equals(clientName)){ //If it doens't equals we send the message to client
                    clientHandler.bufferedWriter.write(messageToSend); /*we pass the messageFromClient that we invoke
                    on the run method*/
                    clientHandler.bufferedWriter.newLine(); /*It means "im done sending over data, so no need to wait
                    more data from me */
                    clientHandler.bufferedWriter.flush(); /* The messages we send they're not big enough to fill the
                    entire buffer, so we manually flush it*/
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }

        }
    }
    /* method that signal that a user has left the chat/disconnect  */
    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("[SERVER] " + clientName + " has disconnected.");
    }
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        /* we just need to close the wrapper and not the Input/Output Stream Reader/Writer and closing a socket means
        * input/output stream */

        try{
            if (bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
