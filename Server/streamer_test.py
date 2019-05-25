import socket

if __name__ == "__main__":
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect(("127.0.0.1", 1))
    f = open(
        "G:\\OneDrive - student.put.poznan.pl\\Prace studia\\Projekty\\PUT-VAC_Local\\VAC_Server" +
        "\\wilhelm.wav", "rb"
    )
    data = f.read()
    print(str(len(data)))
    sock.send(len(data).to_bytes(4, 'big'))

    while True:
        sock.send(data)
