import json
from pathlib import Path
from typing import Dict

from flask import request
from flask_cors import CORS
from flask import Flask, render_template
import pymssql

import atexit

import managing

# app init
app = Flask(__name__, template_folder='templates')
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'

config_json = json.loads(Path('config.json').read_text())


def db_connect():
    global config_json
    # Connect to Microsoft SQL server
    conn = pymssql.connect(
        server=config_json['server'],
        user=config_json['user'],
        password=config_json['password'],
        database=config_json['database']
    )
    return conn


@app.route('/VAC/connect')
def connect():
    response: str = managing.add_client(request)
    return response, 200


@app.route('/VAC/db/test/')
def db_test():
    conn = db_connect()
    cursor = conn.cursor()
    # Get all students from database
    cursor.execute('select * from Test_Table;')
    row = cursor.fetchone()
    dictionary: dict = {}

    i: int = 0
    while row:
        dictionary[str(i)] = []
        for elem in row:
            dictionary[str(i)].append(elem)
        i += 1
        row = cursor.fetchone()
    json_str = json.dumps(dictionary)
    return json_str


@app.route('/VAC/disconnect')
def disconnect():
    response: str = managing.remove_client(request)
    return response, 200


@app.route('/')
@app.route('/VAC/')
def test():
    return "VAC is working!", 200


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


if __name__ == '__main__':
    atexit.register(managing.cleanup)

    print('Waiting for RTP streamer connection . . .')
    '''while True:
        try:
            managing.rtp_server_socket.connect(("127.0.0.1", 49152))
        except Exception:
            continue
        break'''
    managing.start_manager_thread()

    app.run(
        host='0.0.0.0',
        port=int(80),
        threaded=True,
        debug=False
    )
