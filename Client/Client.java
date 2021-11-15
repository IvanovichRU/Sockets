import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;

class Client {
    
    public static Scanner entrada;
    public static void main(String[] args) {
        entrada = new Scanner(System.in);
        mostrarMenu();
        int opcion = entrada.nextInt();
        if (opcion != 3) {
            Socket socket;
            try {
                socket = new Socket("127.0.0.1", 8080);
                DataOutputStream salidaSocket = new DataOutputStream(socket.getOutputStream());
                if (opcion == 2) {
                    ByteBuffer optionBuffer = ByteBuffer.allocate(4);
                    optionBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    optionBuffer.putInt(opcion);
                    salidaSocket.write(optionBuffer.array());
                    salidaSocket.flush();


                    System.out.println("¿De qué tamaño es el vector?");
                    int vectorSize = entrada.nextInt();
                    ByteBuffer buffer = ByteBuffer.allocate(4);
                    buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);
                    buffer.putInt(vectorSize);
                    salidaSocket.write(buffer.array());
                    salidaSocket.flush();
                    System.out.println("Mensaje enviado.");
                    
                    buffer = ByteBuffer.allocate(4 * vectorSize);
                    buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);
                    System.out.println("Ingrese los valores del vector.");
                    for (int i = 0; i < vectorSize; i++) {
                        System.out.println("Valor " + (i + 1) + ": ");
                        buffer.putFloat(entrada.nextFloat());
                    }
                    salidaSocket.write(buffer.array());
                    salidaSocket.flush();
                    System.out.println("Mensaje enviado.");
    
                    DataInputStream entradaDIS = new DataInputStream(socket.getInputStream());
                    byte[] mayor = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        mayor[i] = entradaDIS.readByte();
                    }
                    float mayor_f = ByteBuffer.wrap(mayor).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    System.out.println("El número mayor en el vector es: " + mayor_f);
        
                    entradaDIS.close();
                    salidaSocket.close();
                    socket.close();
                }
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        // int opcionElegida = entrada.nextInt();
        // while (opcionElegida != 3) {
        //     Socket socket = new Socket("127.0.0.1", 8080);
        //     if (opcionElegida == 2) {
        //         float[] vector = leerVector();
        //         System.out.println("Vector leído, enviando a servidor...");
        //         DataOutputStream datosAEnviar = prepararDatosVector(vector);
        //     }
        //     opcionElegida = entrada.nextInt();
        // }
        System.out.println("Terminando programa cliente.");
        entrada.close();
    }

    public static void mostrarMenu() {
        System.out.println("1. Addition");
        System.out.println("2. Greatest");
        System.out.println("3. Exit");
    }

    public static float[] leerVector() {
        System.out.println("De que tamaño es el vector?");
        int tamanoVector = entrada.nextInt();
        float[] vector = new float[tamanoVector];
        System.out.println("Introduzca los valores del vector:");
        for (int i = 0; i < tamanoVector; i++) {
            vector[i] = entrada.nextFloat();
        }
        return leerVector();
    }

    public static DataOutputStream prepararDatosVector(float[] vector) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        DataOutputStream datos = new DataOutputStream(salida);
        return datos;
    }

    public static float[][] readMatrix() {
        Scanner in = new Scanner(System.in);
        System.out.println("What is the width of the array?");
        int matrixWidth = in.nextInt();
        System.out.println("What is the height of the array?");
        int matrixHeight = in.nextInt();
        float[][] returnMatrix = new float[matrixWidth][matrixHeight];
        int counter = 1;
        for (int i = 0; i < matrixHeight; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                returnMatrix[j][i] = (float) counter;
                counter++;
            }
        }
        for (int i = 0; i < matrixHeight; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                System.out.print(returnMatrix[j][i]);
                System.out.print("\t");
            }
            System.out.println();
        }
        in.close();
        return returnMatrix;
    }
}