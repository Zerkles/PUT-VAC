from typing import Dict

from flask import request
from flask_cors import CORS
from flask import Flask, render_template

import socket
from multiprocessing import Process
from threading import Thread, Lock
import atexit
import time

# app init
app = Flask(__name__, template_folder='templates')
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'

# port assigned to client
# increases with every connect request
port: int = 55000

# Dictionaries
manager_thread = None
terminate_proc_manager = False
global_lock = Lock()
processes: Dict[str, Process] = dict()
client_sockets: Dict[str, socket.socket] = dict()


# Terminates processes on exit
def cleanup():
    global terminate_proc_manager
    print('Cleanup')
    terminate_proc_manager = True
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
    print('Cleaned up!')
    time.sleep(1)
    return


def receive_data(conn, recv_size: int):
    msg = str()
    while True:
        try:
            # Data receiving
            if recv_size > 1024:
                msg_recv = conn.recv(1024)
            else:
                msg_recv = conn.recv(recv_size)

            # Length 0 informs of an error/connection end
            if len(msg_recv) == 0:
                return 'disconnected'

            msg += str(msg_recv)
            print(str(msg_recv))

            recv_size -= 1024
            if recv_size <= 0:
                break
        except TypeError:
            print('Type error!')
            return TypeError
    return msg


# Listen function for process
def proc_listen(proc_socket):
    conn, address = proc_socket.accept()
    print('Client connected!')

    while True:
        try:
            recv_data = conn.recv(10)
            if len(recv_data) == 0:
                print('Client disconnected!')
                break
            try:
                recv_size = int(recv_data)
                print(str('Size: ' + str(recv_size)))
                msg = receive_data(conn, recv_size)
                if msg == TypeError:
                    continue
                elif msg == 'disconnected':
                    print('Client disconnected!')
                    return
                print('Received: ' + msg)
            except ValueError:
                print("Not int!")
        except OSError:
            pass
    # return


@app.route('/VAC/connect')
def connect():
    global port
    global global_lock

    if not str(request.remote_addr) in client_sockets.keys():
        print('Port: ' + str(port))
        client_sockets[str(request.remote_addr)] = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_sockets[str(request.remote_addr)].bind(('', port))
        client_sockets[str(request.remote_addr)].listen(1)
        port += 1

        # Critical section
        global_lock.acquire()
        proc = Process(target=proc_listen, args=(client_sockets[str(request.remote_addr)],))
        processes[str(request.remote_addr)] = proc
        proc.start()
        global_lock.release()
        # End critical section

        print("Client connected: " + request.remote_addr)
        return str(port - 1), 200
    else:
        client_port = client_sockets[str(request.remote_addr)].getsockname()[1]
        return 'You are using port: ' + str(client_port), 200


@app.route('/VAC/disconnect')
def disconnect():
    global port
    global global_lock

    if str(request.remote_addr) in client_sockets.keys():
        client_port = client_sockets[str(request.remote_addr)].getsockname()[1]
        print('Port: ' + str(client_port))

        # Critical section
        global_lock.acquire()
        processes[str(request.remote_addr)].terminate()
        del processes[str(request.remote_addr)]
        del client_sockets[str(request.remote_addr)]
        global_lock.release()
        # End critical section

        print("Client disconnected: " + request.remote_addr)
        return 'Disconnect successful', 200
    else:
        return 'You are not connected', 200


@app.route('/')
@app.route('/VAC/')
def test():
    print("It's working!")
    return "I'm am working!", 200


@app.route('/VAC/manager')
def manager():
    return render_template('Index.html', name='VAC Server Manager')


@app.route('/VAC/shutdown')
def server_shutdown():
    global stop_all

    stop_all = True
    func = request.environ.get('werkzeug.server.shutdown')
    if func is None:
        raise RuntimeError('Not running with the Werkzeug Server')
    func()
    print("Server shutting down...")
    return 'Server shutting down...', 200


def manage_processes():
    global processes
    global terminate_proc_manager

    while not terminate_proc_manager:
        if len(processes) > 0:
            print('Checking inactive processes')
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


if __name__ == '__main__':
    atexit.register(cleanup)

    print('Before proc create')
    manager_thread = Thread(target=manage_processes)
    print('Before proc start')
    manager_thread.start()

    app.run(
        host='0.0.0.0',
        port=int(80),
        threaded=True,
        debug=False
    )
