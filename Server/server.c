#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <unistd.h>
#include <errno.h>

#define SA struct sockaddr

int socketfd, connectionfd, length;
struct sockaddr_in servaddr, cli;

// Función para encontrar el flotante mayor en un vector.
float find_highest(float *numbers, int size)
{
	// Esta variable contendrá el número mayor
	float current_highest = numbers[0];

	// Recorremos el vector para encontrar el número mayor
	// comenzamos del índice 1, ya que la variable ya la
	// inicializamos con el valor del índice 0.
	for (int i = 1; i < size; i++)
	{
		// Comparamos el índice actual con el número mayor
		// hasta este punto.
		if (numbers[i] > current_highest)
		{
			// Si el número en el índice i es mayor al actual
			// número mayor, asignamos este nuevo.
			current_highest = numbers[i];
		}
	}
	// La función regresa el mayor flotante encontrado en el vector.
	return current_highest;
}

void matrices(int socketfd)
{
	// Creamos un ciclo eterno para seguir escuchando en el servidor.
	while (1)
	{
		// Creamos variables para saber qué operación desea hacer el cliente.
		// y otra por si el socket fue cerrado por el cliente.
		int selected_option, recv_response;

		// Asignamos la respuesta de la función recv para saber si el socket fue
		// cerrado, al mismo tiempo asignamos lo leído del socket a la variable de opción
		// seleccionada.
		recv_response = recv(socketfd, &selected_option, sizeof(int), MSG_NOSIGNAL);

		// Si recv regresó 0 o un número negativo, el socket fue cerrado o se encontró
		// algún error y debemos terminar el ciclo.
		if (recv_response <= 0)
		{
			break;
		}

		// Si recv fue exitoso, revisamos qué valor se leyó del socket, que debería
		// ser la opción de operación que el cliente envió.

		// El cliente envió la opción 1, la suma de dos matrices.
		if (selected_option == 1)
		{
			// Creamos variables que contendrán el número de columnas, filas
			// y elementos totales de las matrices.
			int matrix_columns, matrix_rows, matrix_elements;

			// Leemos del socket para obtener los números anteriormente
			// mencionados y calculamos el total de elementos en las matrices
			// en base a ellos.
			recv(socketfd, &matrix_columns, sizeof(int), MSG_NOSIGNAL);
			recv(socketfd, &matrix_rows, sizeof(int), MSG_NOSIGNAL);
			matrix_elements = matrix_columns * matrix_rows;

			// Una vez que sabemos las dimensiones de las matrices, podemos
			// crear dos vectores que fungirán de matrices.
			float matrix_one[matrix_elements];
			float matrix_two[matrix_elements];

			// Leemos dos veces más del socket para obtener los flotantes que
			// el cliente envió para cada matriz.
			recv(socketfd, &matrix_one, sizeof(float) * matrix_elements, MSG_NOSIGNAL);
			recv(socketfd, &matrix_two, sizeof(float) * matrix_elements, MSG_NOSIGNAL);

			// Creamos un nuevo vector que contendrá el resultado de la suma
			// de las dos matrices.
			float matrix_result[matrix_elements];

			// Rellenamos la matriz resultante con la suma de los dos elementos
			// correspondientes de cada matriz.
			for (int i = 0; i < matrix_elements; i++)
			{
				matrix_result[i] = matrix_one[i] + matrix_two[i];
			}

			// Finalmente, enviamos al cliente la matriz resultante en forma de vector.
			send(socketfd, matrix_result, 4 * matrix_elements, MSG_NOSIGNAL);
		}

		// El cliente envió la opción 2, encontrar el flotante mayor en
		// un vector.
		else if (selected_option == 2)
		{
			// Creamos una variable que contendrá el tamaño del vector.
			int vector_size;
			
			// Leemos un entero del socket, el cuál debe ser el tamaño
			// del vector que el cliente envió y lo guardamos en
			// 'vector_size'
			recv(socketfd, &vector_size, sizeof(int), MSG_NOSIGNAL);

			// creamos un vector con el tamaño indicado por el cliente
			// para almacenar los valores flotantes que recibiremos
			// del cliente.
			float floats[vector_size];

			// Leemos el socket que ahora debería contener los valores
			// del vector que envió el cliente.
			recv(socketfd, floats, sizeof(floats), MSG_NOSIGNAL);

			// Creamos una variable que contendrá el valor mayor de
			// del vector.
			float greatest = find_highest(floats, vector_size);

			// Finalmente, enviamos el valor mayor al cliente.
			send(socketfd, &greatest, 4, MSG_NOSIGNAL);
		}

		// No tenemos una condicional para la opción 3 que es salir
		// debido a que si el cliente sale, cierra el socket al mismo
		// tiempo y si el servidor lee que el socket fue cerrado, sale
		// del ciclo infinito, sale de la función y termina el programa
	}
}

// Driver function
int main()
{
	// Creación y verificación del socket.
	socketfd = socket(AF_INET, SOCK_STREAM, 0);
	// Si existe algún error en la creación del socket.
	if (socketfd == -1)
		exit(0);

	int option = 1;
	setsockopt(socketfd, SOL_SOCKET, SO_REUSEADDR | SO_REUSEPORT, &option, sizeof(option));

	memset(&servaddr, 0, sizeof(servaddr));

	// Asignar IP y puerto
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
	servaddr.sin_port = htons(8000);

	// Enlazar el socket nuevo a la IP y verificarlo.
	if ((bind(socketfd, (SA*)&servaddr, sizeof(servaddr))) != 0)
		exit(0);

	// El servidor comienza a escuchar por conexiones.
	if ((listen(socketfd, 5)) != 0)
		exit(0);
	
	length = sizeof(cli);

	// Aceptar el paquete de datos del cliente conectante.
	connectionfd = accept(socketfd, (SA*)&cli, &length);
	if (connectionfd < 0)
		exit(0);
  
	// Esta función maneja la comunicación y procesamiento de matrices.
	matrices(connectionfd);
  
	// Al terminar la conexión, cerramos el socket.
	close(socketfd);
}

