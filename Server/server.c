#include <stdio.h>
#include <netdb.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>
#include <poll.h>

#define MAX 400
#define PORT 8000
#define SA struct sockaddr

float encontrar_mayor(float *numeros, int tamano)
{
	float mayor_actual = numeros[0];
	for (int i = 0; i < tamano; i++)
	{
		if (numeros[i] > mayor_actual)
		{
			mayor_actual = numeros[i];
		}
	}
	return mayor_actual;
}

void func(int sockfd)
{
	int call_output;
	while (1)
	{
		int selected_option;
		int recv_response = recv(sockfd, &selected_option, sizeof(int), MSG_NOSIGNAL);
		if (recv_response == 0)
		{
			printf("Cliente desconectado.\nCerrando servidor.\n");
			break;
		}
		if (selected_option == 1)
		{
			int anchoMatrices, altoMatrices, tamano_total;
			recv(sockfd, &anchoMatrices, sizeof(int), MSG_NOSIGNAL);
			recv(sockfd, &altoMatrices, sizeof(int), MSG_NOSIGNAL);
			tamano_total = anchoMatrices * altoMatrices;
			printf("Ancho de matrices es: %d\nAlto de matrices es: %d\n", anchoMatrices, altoMatrices);
			float matriz1[tamano_total];
			float matriz2[tamano_total];
			recv(sockfd, &matriz1, sizeof(float) * tamano_total, MSG_NOSIGNAL);
			recv(sockfd, &matriz2, sizeof(float) * tamano_total, MSG_NOSIGNAL);
			float matriz_final[tamano_total];
			for (int i = 0; i < tamano_total; i++)
			{
				matriz_final[i] = matriz1[i] + matriz2[i];
			}
			send(sockfd, matriz_final, 4 * tamano_total, MSG_NOSIGNAL);
		}
		else if (selected_option == 2)
		{
			int vector_size;
			recv(sockfd, &vector_size, sizeof(int), MSG_NOSIGNAL);
			printf("El tamaño del vector es: %d\n", vector_size);
			float floats[vector_size];
			recv(sockfd, floats, sizeof(floats), MSG_NOSIGNAL);
			float greatest = encontrar_mayor(floats, vector_size);
			printf("%f\n", greatest);
			send(sockfd, &greatest, 4, MSG_NOSIGNAL);
		}
	}
}

// Driver function
int main()
{
	int sockfd, connfd, len;
	struct sockaddr_in servaddr, cli;

	// socket create and verification
	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd == -1) {
		printf("socket creation failed...\n");
		exit(0);
	}
	else
		printf("Socket successfully created..\n");
	bzero(&servaddr, sizeof(servaddr));

	// assign IP, PORT
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
	servaddr.sin_port = htons(PORT);

	// Binding newly created socket to given IP and verification
	if ((bind(sockfd, (SA*)&servaddr, sizeof(servaddr))) != 0) {
		printf("socket bind failed...\n");
		exit(0);
	}
	else
		printf("Socket successfully binded..\n");

	// Now server is ready to listen and verification
	if ((listen(sockfd, 5)) != 0) {
		printf("Listen failed...\n");
		exit(0);
	}
	else
		printf("Server listening..\n");
	len = sizeof(cli);

	// Accept the data packet from client and verification
	connfd = accept(sockfd, (SA*)&cli, &len);
	if (connfd < 0) {
		printf("server accept failed...\n");
		exit(0);
	}
	else
		printf("server accept the client...\n");
  
	// Function for chatting between client and server
	func(connfd);
  
	// After chatting close the socket
	close(sockfd);
}

