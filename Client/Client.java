import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;

class Client {

    // Declaramos un escaner para leer entrada del usuario en diferentes
    // puntos del programa.
    public static Scanner input;

    public static void main(String[] args) {

        // Inicializamos el objeto de escaner declarado anteriormente.
        input = new Scanner(System.in);

        // Declaramos un objeto Socket donde escribiremos y leeremos.
        Socket socket;

        // Declaramos objetos DataOutputStream y DataInputStream para
        // leer y escribir al socket.
        DataOutputStream socketOut;
        DataInputStream socketIn;

        // Inicializamos una variable opción que determinará la
        // operación a realizar.
        int option = 0;

        // Debemos encapsular la inicialización de los objetos
        // Socket, DataOutputStream y DataInputStream en un try
        // debido a que pueden lanzar excepciones.
        try {
            
            // Inicializamos el socket con la IP de loopback
            // en el puerto 8000
            socket = new Socket("127.0.0.1", 8000);

            // Inicializamos los objetos Data___Stream obteniendo
            // objetos del socket.
            socketOut = new DataOutputStream(socket.getOutputStream());
            socketIn = new DataInputStream(socket.getInputStream());

            // Mostramos por primera vez el menú que se mostrará al usuario.
            showMenu();

            // Leemos qué opción elige el usuario con el escaner.
            option = input.nextInt();

            // Ahora dependiendo de la opción elegida, hacemos distintos procesos.
            while (option != 3) {

                // Si el usuario elige la opción uno:
                if (option == 1) {

                    // Escribimos al socket la opción seleccionada para que inicie
                    // el proceso correcto de leer los datos necesarios.
                    socketOut.write(intToByteArray(option));
                    socketOut.flush();

                    // Pedimos y leemos los tamaños de ambas matrices a sumar para
                    // verificar que sean de las mismas dimensiones.
                    System.out.print("Input the number of columns for the first matrix: ");
                    int matrixOneWidth = input.nextInt();
                    System.out.print("Input the number of rows for the first matrix: ");
                    int matrixOneHeight = input.nextInt();
                    System.out.print("Input the number of columns for the second matrix: ");
                    int matrixTwoWidth = input.nextInt();
                    System.out.print("Input the number of rows for the second matrix: ");
                    int matrixTwoHeight = input.nextInt();

                    // Revisamos que sean de las mismas dimensiones las matrices.
                    if (matrixOneWidth != matrixTwoWidth || matrixOneHeight != matrixTwoHeight) {
                        // Sino, solo se lo indicamos al usuario y regresamos al menú.
                        System.out.println("The matrices are not the same size, the sum is not possible.");
                    }
                    else {

                        // Escribimos al socket los tamaños de las matrices.
                        socketOut.write(intToByteArray(matrixOneWidth));
                        socketOut.write(intToByteArray(matrixOneHeight));
                        socketOut.flush();

                        // Creamos una variable que contenga el número de elementos
                        // totales de las matrices.
                        int matrixElements = matrixOneWidth * matrixOneHeight;

                        // Pedimos y leemos los valores de ambas matrices.
                        System.out.println("Input the first matrix's values:");
                        float[][] matrixOne = readMatrixValues(matrixOneWidth, matrixTwoHeight);
                        System.out.println("Input the second matrix's values");
                        float[][] matrixTwo = readMatrixValues(matrixOneWidth, matrixTwoHeight);

                        // Para el fácil manejo, el servidor recibe los datos de manera lineal
                        // entonces debemos mover los valores leídos de un vector bidimensional
                        // a uno lineal.
                        float[] matrixOne1D = matrix2DTo1D(matrixOne, matrixOneWidth, matrixOneHeight);
                        float[] matrixTwo1D = matrix2DTo1D(matrixTwo, matrixOneWidth, matrixOneHeight);

                        // Escribimos los valores leídos de las matrices una vez
                        // almacenados unidimensionalmente, al socket.
                        socketOut.write(floatsToByteArray(matrixOne1D));
                        socketOut.write(floatsToByteArray(matrixTwo1D));
                        socketOut.flush();

                        // Creamos un vector en donde almacenar el resultado de
                        // la suma de matrices en preparación a la respuesta
                        // del servidor.
                        byte[] matrixResultBytes = new byte[4 * matrixElements];

                        // Leemos los datos enviados al socket por el servidor
                        // y los insertamos en el vector unidimensional
                        // que creamos anteriormente.
                        for (int i = 0; i < matrixElements * 4; i++) {
                            matrixResultBytes[i] = socketIn.readByte();
                        }

                        // Los datos leídos del socket son bytes en crudo, entonces
                        // deberemos convertirlos a floats en orden big endian
                        // para poder imprimirlos desde el cliente en Java.

                        // Creamos un vector de floats donde guardaremos los flotantes
                        // ya convertidos.
                        float[] matrixResult = new float[matrixElements];

                        // Envolvemos el vector donde almacenamos los bytes en crudo
                        // leídos del socket en un ByteBuffer para indicar a
                        // Java que están en orden little endian.
                        ByteBuffer resultBuffer = ByteBuffer.wrap(matrixResultBytes);
                        resultBuffer.order(ByteOrder.LITTLE_ENDIAN);

                        // Insertamos como flotantes en big endian los bytes
                        // almacenados en el buffer al arreglo de floats
                        // que preparamos anteriormente.
                        for (int i = 0; i < matrixElements; i++) {
                            matrixResult[i] = resultBuffer.getFloat();
                        }

                        // Creamos una variable de apoyo para imprimir los
                        // valores de la matriz resultante en filas y columnas
                        // siendo un vector lineal.
                        int counter = 0;

                        // Imprimimos la matriz resultante recorriendola con un
                        // ciclo for.
                        System.out.println("The resulting matrix from the sum is:");
                        for (int i = 0; i < matrixOneHeight; i++) {
                            for (int j = 0; j < matrixOneWidth; j++) {
                                System.out.print(matrixResult[counter] + "\t");
                                counter++;
                            }
                            System.out.println();
                        }
                        System.out.println();

                        // Finalmente salimos de esta condicional y regresamos al menú.
                    }
                } // Si el usuario elige la opción dos.
                else if (option == 2) {

                    // Escribimos al socket la opción seleccionada para que inicie
                    // el proceso correcto de leer los datos necesarios.
                    socketOut.write(intToByteArray(option));
                    socketOut.flush();
    
                    // Pedimos y leemos la longitud del vector sonbre el que
                    // se trabajará.
                    System.out.println("What is the vector's size?");
                    int vectorSize = input.nextInt();

                    // Escribimos al socket el tamaño del vector leído
                    // para que el servidor comience a prepararse
                    socketOut.write(intToByteArray(vectorSize));
                    socketOut.flush();
                    
                    // Pedimos los valores del vector al usuario.
                    System.out.println("Input the vector's values:");

                    // Creamos un vector para almacenar los valores.
                    float[] vectorValues = new float[vectorSize];

                    // Leemos y almacenamos los valores introducidos por el
                    // usuario.
                    for (int i = 0; i < vectorValues.length; i++) {
                        System.out.println("Value " + (i + 1) + ": ");
                        vectorValues[i] = input.nextFloat();
                    }

                    // Escribimos y enviamos al socket.
                    socketOut.write(floatsToByteArray(vectorValues));
                    socketOut.flush();

                    // Creamos un vector para almacenar los valores recibidos
                    // del servidor.
                    byte[] highest = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        highest[i] = socketIn.readByte();
                    }

                    // Creamos un vector de flotantes que inicializamos
                    // fácilmente con funciones de la clase ByteBuffer
                    float highestFloat = ByteBuffer.wrap(highest).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    System.out.println("El número mayor en el vector es: " + highestFloat);
                    System.out.println();
                }
                else {
                    System.out.println("Elija una de las 3 opciones por favor:");
                }
                showMenu();
                option = input.nextInt();
            }
            // entradaSocket.close();
            // salidaSocket.close();
            // socket.close();
            input.close();
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

    // **************************************************************
    // Java, por defecto, trabajo con datos en orden "Big Endian"
    // donde el byte más significativo es el primero en espacio
    // de memoria, por ello, debemos tomar unos pasos extra para
    // reacomodar los bytes escritos a orden "Little Endian" para
    // el servidor en C, que espera datos en este orden. Para ello
    // se crearon estas funciones de utilidad para convertir
    // ints y floats a little endian.
    public static byte[] intToByteArray(int number) {

        // Creamos un objeto ByteBuffer para almacenar y reordenar los
        // bytes de la opción elegida por el usuario, le damos espacio
        // para 4 bytes, que es el tamaño de un entero de 32 bits.
        ByteBuffer intBuffer = ByteBuffer.allocate(4);

        // Cambiamos el ordenamiento de los bytes a little endian.
        intBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // Insertamos el entero al ByteBuffer
        intBuffer.putInt(number);

        // Regresamos el vector de bytes para ser procesado.
        return intBuffer.array();
    }

    public static byte[] floatsToByteArray(float[] numbers) {

        // Creamos un objeto ByteBuffer para alamcenar y reordenar los
        // bytes que representarán los valores obtenidos del usuario
        // mediante el cliente.
        ByteBuffer numbersBuffer = ByteBuffer.allocate(numbers.length * 4);

        // Cambiamos el ordenamiento de los bytes a little endian.
        numbersBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // Insertamos todos los flotantes leídos al vector de
        // bytes ordenado en little endian.
        for (float number : numbers) {
            numbersBuffer.putFloat(number);
        }

        // Regresamos el vector de bytes para ser procesado.
        return numbersBuffer.array();
    }

    // ***************************************************************

    // Simple función para imprimir el menú al usuario del cliente.
    public static void showMenu() {
        System.out.println("1. Addition");
        System.out.println("2. Greatest");
        System.out.println("3. Exit");
    }

    // Esta función se encarga de leer los valores de una matriz
    // dándole el ancho y alto de la matriz a leer.
    public static float[][] readMatrixValues(int width, int height) {

        // Creamos una matriz para almacenar los datos leídos de
        // lo que el usuario ingrese.
        float[][] matrix = new float[width][height];

        // Creamos una variable de apoyo para almacenar en qué elemento
        // de la matriz nos encontramos leyendo, esta variable se la
        // pasaremos más adelante a la función `imprimirMatriz` para
        // visualizar el elemento actual que el usuario está registrando.
        int counter = 0;

        // Recorremos la matriz a su alto en este ciclo.
        for (int i = 0; i < height; i++) {

            // Recorremos la matriz a su ancho en este ciclo.
            for (int j = 0; j < width; j++) {

                // Pedimos el siguiente valor de la matriz al usuario.
                System.out.println("Input the matrix's next value: ");

                // Llamamos la función `imprimirMatriz` como apoyo visual para
                // el usuario.
                printMatrix(matrix, width, height, counter);

                // Almacenamos el valor leído en el espacio correspondiente
                // de la matriz.
                matrix[j][i] = input.nextFloat();

                // Incrementamos la variable de apoyo para que apunte al
                // siguiente elemento en el siguiente ciclo.
                counter++;
            }
        }

        // Finalmente, fuera del ciclo y denotada por guiones, mostramos
        // la matriz como se ve al finalizar la lectura de sus valores.
        System.out.println("-------------------------------");
        printMatrix(matrix, width, height, counter);
        System.out.println("-------------------------------");

        // Regresamos la matriz bidimensional a quien llamó esta función.
        return matrix;
    }

    // Esta función muestra la matriz dada y denota su elemento con
    // índice `siguiente` con #.#
    public static void printMatrix(float[][] matrix, int width, int height, int next) {
        int counter = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (counter == next) {
                    System.out.print("#.#");
                }
                else {
                    System.out.print(matrix[j][i]);
                }
                System.out.print("\t");
                counter++;
            }
            System.out.println();
        }
    }

    // Esta función cumple la función de convertir una matriz 2D y
    // la convierte en un vector o arreglo unidimensional con sus
    // valores correctos, moviendo cada fila de la matriz y
    // ordenándolas de manera lineal una detrás de la otra.
    public static float[] matrix2DTo1D(float[][] matrix, int width, int height) {

        // Creamos un vector para almacenar los valores de la matriz
        // de manera lineal o unidimensional, le damos espacio para
        // almacenar el número de elementos que tenga la matriz dada.
        float[] matrix1D = new float[width * height];

        // Creamos una variable de apoyo para llevar control del índice
        // en el vector representativo de la matriz 2D.
        int counter = 0;

        // En estos ciclos recorremos cada índice de la matriz.
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                // Almacenamos el valor de la matriz correspondiente
                // al vector unidimensional.
                matrix1D[counter] = matrix[j][i];

                // Incrementamos la variable de apoyo.
                counter++;
            }
        }

        // Regresamos el vector unidimensional que contiene todos los valores
        // de la matriz bidimensional inicial.
        return matrix1D;
    }

}