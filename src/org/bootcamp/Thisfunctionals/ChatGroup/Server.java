package org.bootcamp.Thisfunctionals.ChatGroup;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    //responsible for listening for incoming connection or clients
    // creating a socket object to communicate with them
    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }
    public void startServer(){


            //While socket its not closed we're going to wait for a client to connect
            try {

                while(!serverSocket.isClosed()) {
                    Socket client = serverSocket.accept();
                    System.out.println("New client connect to the server!");

                    //Responsible for communicating with a client this class implements runnable
                    ClientHandler clientHandler = new ClientHandler(client);

                    Thread t = new Thread(clientHandler);
                    t.start();

                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void closeServerSocket(){
        //we create this method because if an error occurs we want to shut down our server socket
            try{
                if (serverSocket != null){
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    public static void main(String[] args) throws IOException {

        /*Our server will be listening for clients that are making a connection to this port number
        //So a client is going to have a socket, they're going to send out saying to the server "hey i want to talk
        //on port 1234" so this number has to match whe we create our client
        we do this because our server object takes a serversocket as a constructor*/

        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}

