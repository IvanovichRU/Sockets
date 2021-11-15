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
        Socket socket;
        DataOutputStream salidaSocket;
        DataInputStream entradaSocket;
        int opcion = 0;
        try {
            socket = new Socket("127.0.0.1", 8000);
            salidaSocket = new DataOutputStream(socket.getOutputStream());
            entradaSocket = new DataInputStream(socket.getInputStream());
            mostrarMenu();
            opcion = entrada.nextInt();
            while (opcion != 3) {
                if (opcion == 1) {
                    // Enviar la opción seleccionada al servidor.
                    ByteBuffer optionBuffer = ByteBuffer.allocate(4);
                    optionBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    optionBuffer.putInt(opcion);
                    salidaSocket.write(optionBuffer.array());
                    salidaSocket.flush();

                    System.out.print("Ingrese el número de columnas de la primer matriz: ");
                    int anchoMatrizUno = entrada.nextInt();

                    System.out.print("Ingrese el número de filas de la primer matriz: ");
                    int altoMatrizUno = entrada.nextInt();

                    System.out.print("Ingrese el número de columnas de la segunda matriz: ");
                    int anchoMatrizDos = entrada.nextInt();

                    System.out.print("Ingrese el número de filas de la segunda matriz: ");
                    int altoMatrizDos = entrada.nextInt();

                    if (anchoMatrizUno != anchoMatrizDos || altoMatrizUno != altoMatrizDos) {
                        System.out.println("Las matrices no son del mismo tamaño, la suma no es posible.");
                    }
                    else {
                        ByteBuffer tamanosMatrices = ByteBuffer.allocate(8);
                        tamanosMatrices.order(ByteOrder.LITTLE_ENDIAN);
                        tamanosMatrices.putInt(anchoMatrizUno);
                        tamanosMatrices.putInt(altoMatrizUno);
                        salidaSocket.write(tamanosMatrices.array());
                        salidaSocket.flush();

                        System.out.println("Ingrese valores de la primer matriz:");
                        int tamanoTotal = anchoMatrizUno * altoMatrizUno;
                        float[][] matrizUno = leerValoresMatriz(anchoMatrizUno, altoMatrizDos);
                        System.out.println("Ingrese valores de la segunda matriz:");
                        float[][] matrizDos = leerValoresMatriz(anchoMatrizUno, altoMatrizDos);

                        float[] matriz1DUno = matriz2Da1D(matrizUno, anchoMatrizUno, altoMatrizUno);
                        float[] matriz1DDos = matriz2Da1D(matrizDos, anchoMatrizUno, altoMatrizUno);

                        ByteBuffer matricesBuffer = ByteBuffer.allocate(tamanoTotal * 2 * 4);
                        matricesBuffer.order(ByteOrder.LITTLE_ENDIAN);
                        for (int i = 0; i < tamanoTotal; i++) {
                            matricesBuffer.putFloat(matriz1DUno[i]);
                        }
                        for (int i = 0; i < tamanoTotal; i++) {
                            matricesBuffer.putFloat(matriz1DDos[i]);
                        }
                        salidaSocket.write(matricesBuffer.array());
                        salidaSocket.flush();

                        byte[] matrizResultanteBytes = new byte[4 * tamanoTotal];
                        for (int i = 0; i < tamanoTotal * 4; i++) {
                            matrizResultanteBytes[i] = entradaSocket.readByte();
                        }
                        float[] matrizResultante = new float[tamanoTotal];
                        ByteBuffer bufferResultante = ByteBuffer.wrap(matrizResultanteBytes);
                        bufferResultante.order(ByteOrder.LITTLE_ENDIAN);
                        for (int i = 0; i < tamanoTotal; i++) {
                            matrizResultante[i] = bufferResultante.getFloat();
                        }
                        int contador = 0;
                        System.out.println("La matriz resultante de la suma es:");
                        for (int i = 0; i < altoMatrizUno; i++) {
                            for (int j = 0; j < anchoMatrizUno; j++) {
                                System.out.print(matrizResultante[contador] + "\t");
                                contador++;
                            }
                            System.out.println();
                        }
                        System.out.println();
                    }
                }
                else if (opcion == 2) {
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
                    System.out.println("Tamaño del vector enviado.");
                    
                    buffer = ByteBuffer.allocate(4 * vectorSize);
                    buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);
                    System.out.println("Ingrese los valores del vector.");
                    for (int i = 0; i < vectorSize; i++) {
                        System.out.println("Valor " + (i + 1) + ": ");
                        buffer.putFloat(entrada.nextFloat());
                    }
                    salidaSocket.write(buffer.array());
                    salidaSocket.flush();
                    System.out.println("Vector enviado.");

                    byte[] mayor = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        mayor[i] = entradaSocket.readByte();
                    }
                    float mayor_f = ByteBuffer.wrap(mayor).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    System.out.println("El número mayor en el vector es: " + mayor_f);
                    System.out.println();
                    System.out.println();
                }
                else {
                    System.out.println("Elija una de las 3 opciones por favor:");
                }
                mostrarMenu();
                opcion = entrada.nextInt();
            }
            socket.close();
            entradaSocket.close();
            salidaSocket.close();
            entrada.close();
        }
        catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void mostrarMenu() {
        System.out.println("1. Addition");
        System.out.println("2. Greatest");
        System.out.println("3. Exit");
    }

    public static float[][] leerValoresMatriz(int ancho, int alto) {
        float[][] matriz = new float[ancho][alto];
        int contador = 0;
        for (int i = 0; i < alto; i++) {
            for (int j = 0; j < ancho; j++) {
                System.out.println("Ingrese el siguiente valor de la matriz: ");
                imprimirMatriz(matriz, ancho, alto, contador);
                matriz[j][i] = entrada.nextFloat();
                contador++;
            }
        }
        System.out.println("-------------------------------");
        imprimirMatriz(matriz, ancho, alto, contador);
        System.out.println("-------------------------------");
        return matriz;
    }

    public static void imprimirMatriz(float[][] matriz, int ancho, int alto, int siguiente) {
        int contador = 0;
        for (int i = 0; i < alto; i++) {
            for (int j = 0; j < ancho; j++) {
                if (contador == siguiente) {
                    System.out.print("#.#");
                }
                else {
                    System.out.print(matriz[j][i]);
                }
                System.out.print("\t");
                contador++;
            }
            System.out.println();
        }
    }

    public static float[] matriz2Da1D(float[][] matriz, int ancho, int alto) {
        float[] matriz1D = new float[ancho * alto];
        int contador = 0;
        for (int i = 0; i < alto; i++) {
            for (int j = 0; j < ancho; j++) {
                matriz1D[contador] = matriz[j][i];
                contador++;
            }
        }
        return matriz1D;
    }

}