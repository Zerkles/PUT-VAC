import json

from flask import request
from flask_cors import CORS
from flask import Flask, render_template

import atexit

import managing
import database

# app init
app = Flask(__name__, template_folder='templates')
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'


@app.route('/VAC/connect')
def connect():
    response: str = managing.add_client(request)
    return response, 200


@app.route('/VAC/db/test/')
def db_test():
    table: dict = database.test_get()
    json_str = json.dumps(table)
    return json_str


@app.route('/VAC/disconnect')
def disconnect():
    response: str = managing.remove_client(request)
    return response, 200


@app.route('/')
@app.route('/VAC/')
def test():
    return "<p>VAC is working!</p>", 200


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
