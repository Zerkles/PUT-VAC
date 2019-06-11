from multiprocessing import Process
from threading import Thread, Lock
from socket import socket, AF_INET, SOCK_STREAM
from typing import Dict
import time
from Vac import Vac
import cv2
import numpy as np
import json
import database
import logger

server_id: int

# TCP port assigned to client
# increases with every successful connect request
tcp_port: int = 55000

# RTP port assigned to client
# increases by 2 with every successful connect request
rtp_port: int = 49152

manager_thread: Thread = Thread()
performance_log_thread: Thread = Thread()
server_shutdown: bool = False
global_lock = Lock()

# Dictionaries
processes: Dict[str, Process] = dict()
client_sockets: Dict[str, socket] = dict()

# RTP service socket
rtp_server_socket = socket(AF_INET, SOCK_STREAM)


# To be used on app exit
def cleanup() -> None:
    global manager_thread
    global performance_log_thread
    global client_sockets
    global processes

    print('Cleanup')
    proc_num = 1
    sock_num = 1
    for sock in client_sockets.values():
        print('Closing socket ' + str(sock_num))
        sock_num += 1
        sock.close()
    for proc in processes.values():
        print('Terminating process ' + str(proc_num))
        proc_num += 1
        proc.terminate()
        proc.join()

    manager_thread.join()
    performance_log_thread.join()
    print('Cleaned up!')
    time.sleep(1)
    return


# Receives data in chunks (max 1024 bytes) and merges it
def receive_data(conn, recv_size: int, converter: Vac):
    msg = bytes()
    while True:
        try:
            # Data receiving
            if recv_size >= 4096:
                msg_recv = conn.recv(4096)
            else:
                msg_recv = conn.recv(recv_size)
            # Length 0 informs of an error/connection end
            if len(msg_recv) == 0:
                return 'disconnected'

            msg += bytes(msg_recv)

            recv_size -= len(msg_recv)
            if recv_size <= 0:
                break
        except TypeError:
            return TypeError
        except Exception as e:
            print(str(e))
    array = np.asarray(bytearray(msg), dtype=np.uint8)
    img = cv2.imdecode(buf=array, flags=cv2.IMREAD_COLOR)

    try:
        converter.feed_image(img)
    except Exception as e:
        print('Receive exception 2')
        print(str(e))
    return msg, img


# Listen function for process
def proc_listen(client_socket, streamer_tcp_port, s_id: int) -> None:
    rtp_socket = socket(AF_INET, SOCK_STREAM)
    rtp_socket.connect(("127.0.0.1", streamer_tcp_port))

    converter = Vac(rtp_socket)

    total_size: float = 0

    conn, address = client_socket.accept()
    print('Client connected!')
    not_int_size = 0
    while True:
        try:
            recv_data = conn.recv(4)
            if len(recv_data) == 0:
                print('Client disconnected!')
                break
            try:
                recv_size = int.from_bytes(recv_data, "big")
                total_size += recv_size / 1000000
                msg, img = receive_data(conn, recv_size, converter)

                if msg == TypeError:
                    continue
                elif msg == 'disconnected':
                    print('Client disconnected!')
                    logger.statistic(s_id, 'Data received', str(int(total_size)))
                    return
            except ValueError:
                print("------------------------")
                print("Not int!")
                print("Received: " + str(recv_data))
                not_int_size += len(recv_data)
                print("Size: " + str(not_int_size))
                print("------------------------")
        except OSError:
            pass
    logger.statistic(s_id, 'Data received', str(int(total_size)))


def add_client(request) -> str:
    global tcp_port
    global rtp_port
    global global_lock

    login: str = request.args['login']
    passwd: str = request.args['passwd']

    print(request.args)

    if database.user_authenticate(login, passwd):
        client_tcp_port = tcp_port
        client_rtp_port = rtp_port
        tcp_port += 1
        rtp_port += 4

        sock = socket(AF_INET, SOCK_STREAM)
        client_sockets[str(request.remote_addr)] = sock
        client_sockets[str(request.remote_addr)].bind(('', client_tcp_port))
        client_sockets[str(request.remote_addr)].listen(1)

        # Sending client information to RTP streamer
        client_addr: str = request.remote_addr
        client_json = {'address': client_addr, 'rtp_port': client_rtp_port}
        client_json_str: str = json.dumps(client_json)
        rtp_server_socket.send(len(client_json_str).to_bytes(4, byteorder='big'))
        rtp_server_socket.send(client_json_str.encode())
        print("Client info: " + client_json_str)

        recv_data = rtp_server_socket.recv(4)
        streamer_tcp_port = int.from_bytes(recv_data, "big")

        # Critical section
        global_lock.acquire()

        # Acquire UserID and SessionID
        u_id: int = database.user_get_id(login)
        s_id: int = database.session_max_id() + 1
        database.session_insert(s_id, u_id, server_id)
        database.client_insert(request.args)

        proc = Process(target=proc_listen, args=(sock, streamer_tcp_port, s_id))
        processes[str(request.remote_addr)] = proc
        proc.start()
        global_lock.release()

        logger.log_entry('Client', 'Connected', logger.create_json('login'))
        # End critical section

        # Building response
        response_json = {'tcp_port': client_tcp_port, 'rtp_port': client_rtp_port}
        response_json_str = json.dumps(response_json)
        print("Response: " + response_json_str)
        return response_json_str

    else:
        return 'incorrect credentials'


def remove_client(request) -> str:
    global tcp_port
    global global_lock
    global rtp_server_socket

    if str(request.remote_addr) in client_sockets.keys():
        client_port = client_sockets[str(request.remote_addr)].getsockname()[1]
        print('Port: ' + str(client_port))

        # Critical section
        global_lock.acquire()
        processes[str(request.remote_addr)].kill()
        processes[str(request.remote_addr)].join()
        del processes[str(request.remote_addr)]
        del client_sockets[str(request.remote_addr)]
        global_lock.release()
        # End critical section

        client_addr: str = request.remote_addr
        client_json = {'address': client_addr}
        client_json_str: str = json.dumps(client_json)
        rtp_server_socket.send(len(client_json_str).to_bytes(4, byteorder='big'))
        rtp_server_socket.send(client_json_str.encode())

        logger.log_entry('Client', 'Connected', logger.create_json(request.args['login']))

        print("Client disconnected: " + request.remote_addr)
        return 'disconnect success'
    else:
        return 'not connected'


# Threads

# Removes inactive processes and their sockets
# Processes get inactive when client disconnects
# without http request
def manage_processes() -> None:
    global processes
    global server_shutdown

    while not server_shutdown:
        if len(processes) > 0:
            while True:
                changed = False
                for key in processes.keys():
                    if not processes[key].is_alive():
                        del processes[key]
                        del client_sockets[key]
                        changed = True
                        print('Removed inactive process!')
                        break
                if not changed:
                    if len(processes) <= 0:
                        print('No active processes left!')
                    break
        time.sleep(2.5)
    return


def performance_log() -> None:
    global server_shutdown

    while not server_shutdown:
        logger.performance()
        time.sleep(15)
        pass


def start_manager_thread() -> None:
    global manager_thread

    manager_thread = Thread(target=manage_processes)
    manager_thread.start()


def start_performance_log_thread() -> None:
    global performance_log_thread

    performance_log_thread = Thread(target=performance_log)
    performance_log_thread.start()
