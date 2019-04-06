from flask import Flask
from flask import request
from flask_cors import CORS, cross_origin
from flask import Flask, render_template

import socket
from multiprocessing import Process
import atexit
import time

app = Flask(__name__, template_folder='templates')
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'

port = 55000
processes = dict()
client_sockets = dict()


def cleanup():
    proc_num = 1
    for proc in processes.values():
        print('Terminating process ' + str(proc_num))
        proc_num += 1
        proc.terminate()
        proc.join()
    print('Cleaned up!')
    time.sleep(10)
    return


def proc_listen(proc_socket):
    conn, address = proc_socket.accept()
    print('Client connected!')

    while True:
        msg = str()
        try:
            recv_size = int(conn.recv(10))
            print(str('Size: ' + str(recv_size)))
            while True:
                try:
                    if recv_size > 1024:
                        msg_recv = conn.recv(1024)
                        recv_size -= 1024
                        msg += str(msg_recv)
                    else:
                        msg_recv = conn.recv(recv_size)
                        msg += msg_recv.decode('utf-8')
                        break
                except TypeError:
                    print('Type error!')
                    break
            print('Received:' + msg)
        except OSError:
            pass
    # return


@app.route('/VAC/connect')
def connect():
    global port

    if not str(request.remote_addr) in client_sockets.keys():
        print('Port: ' + str(port))
        client_sockets[str(request.remote_addr)] = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_sockets[str(request.remote_addr)].bind(('', port))
        client_sockets[str(request.remote_addr)].listen(1)
        port += 1
        proc = Process(target=proc_listen, args=(client_sockets[str(request.remote_addr)],))
        processes[str(request.remote_addr)] = proc
        proc.start()
        print("Client connected: " + request.remote_addr)
        return 'You are using port: ' + str(port - 1), 200
    else:
        client_port = client_sockets[str(request.remote_addr)].getsockname()[1]
        return 'You are using port: ' + str(client_port), 200


@app.route('/VAC/disconnect')
def disconnect():
    global port

    if str(request.remote_addr) in client_sockets.keys():
        client_port = client_sockets[str(request.remote_addr)].getsockname()[1]
        print('Port: ' + str(client_port))
        processes[str(request.remote_addr)].terminate()
        processes[str(request.remote_addr)].join()
        del processes[str(request.remote_addr)]
        del client_sockets[str(request.remote_addr)]

        print("Client disconnected: " + request.remote_addr)
        return 'Disconnect successful', 200
    else:
        return 'You are not connected', 200


@app.route('/VAC/')
def hello_world():
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
    print("Shutting server down!")
    return 'Server shutting down...', 200


if __name__ == '__main__':
    atexit.register(cleanup)

    app.run(
        host='0.0.0.0',
        port=int(80),
        threaded=True,
        debug=False
    )
