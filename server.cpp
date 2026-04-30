#include <iostream>
#include <string>
#include <winsock2.h>
#include <ws2tcpip.h>

// Aceasta linie ii spune linker-ului din Visual Studio sa adauge biblioteca Winsock
#pragma comment(lib, "ws2_32.lib") 

using namespace std;

int main() {
    // 1. Initializare Winsock (Specific Windows)
    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        cout << "Eroare la initializarea Winsock." << endl;
        return 1;
    }

    SOCKET server_fd, client_socket;
    struct sockaddr_in address;
    int addrlen = sizeof(address);
    char buffer[1024] = { 0 };

    // 2. Creare socket
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET) {
        cout << "Eroare la creare socket: " << WSAGetLastError() << endl;
        WSACleanup();
        return 1;
    }

    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons(8080); // Acelasi port cu Java

    // 3. Bind si Listen
    if (bind(server_fd, (struct sockaddr*)&address, sizeof(address)) == SOCKET_ERROR) {
        cout << "Eroare la bind: " << WSAGetLastError() << endl;
        closesocket(server_fd);
        WSACleanup();
        return 1;
    }

    listen(server_fd, 3);
    cout << "Server C++ pornit pe Windows! Astept conexiunea ..." << endl;

    // 4. Accept
    if ((client_socket = accept(server_fd, (struct sockaddr*)&address, &addrlen)) == INVALID_SOCKET) {
        cout << "Eroare la accept: " << WSAGetLastError() << endl;
        closesocket(server_fd);
        WSACleanup();
        return 1;
    }
    cout << "Colega s-a conectat! Astept primul mesaj..." << endl;

    // 5. Bucla de Chat
    while (true) {
        memset(buffer, 0, sizeof(buffer));
        int valread = recv(client_socket, buffer, 1024, 0);

        if (valread <= 0 || strncmp(buffer, "exit", 4) == 0) {
            cout << "Colega a inchis conexiunea." << endl;
            break;
        }

        cout << "Java: " << buffer;

        cout << "Tu (C++): ";
        string raspuns;
        getline(cin, raspuns);

        string mesaj_de_trimis = raspuns + "\n";
        send(client_socket, mesaj_de_trimis.c_str(), mesaj_de_trimis.length(), 0);

        if (raspuns == "exit") {
            cout << "Ai inchis chat-ul." << endl;
            break;
        }
    }

    // 6. Curatare resurse (Specific Windows)
    closesocket(client_socket);
    closesocket(server_fd);
    WSACleanup();
    return 0;
}