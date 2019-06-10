import json

from flask import request, Response
from flask_cors import CORS
from flask import Flask, render_template

import atexit

import managing
import database
import logger

# app init
app = Flask(__name__, template_folder='templates')
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'

debug: bool = False

server_id: int
streamer_port: int = 2


def response_json(data: dict) -> Response:
    json_str = json.dumps(data, indent=1)
    response = Response(200)
    response.data = json_str
    response.content_type = 'text/json'
    return response


@app.route('/VAC/connect', methods=['GET'])
def connect():
    response: str = managing.add_client(request)
    if response == 'incorrect credentials':
        return '', 401
    else:
        return response, 200


@app.route('/VAC/disconnect', methods=['GET'])
def disconnect():
    response: str = managing.remove_client(request)
    if response == 'disconnect success':
        return '', 200
    else:
        return '', 409


@app.route('/VAC/')
def test():
    return Response(200, "<p>VAC is working!</p>")


@app.route('/VAC/manager', methods=['GET'])
def manager():
    return render_template('Index.html', name='VAC Server Manager')


@app.route('/VAC/shutdown', methods=['GET'])
def shutdown():
    managing.server_shutdown = True
    func = request.environ.get('werkzeug.server.shutdown')
    if func is None:
        raise RuntimeError('Not running with the Werkzeug Server')
    func()

    print("Server shutting down...")
    logger.log_entry('Server', 'Shutdown', '')
    return Response(200, '<p>Server shutting down...</p>')


# Database routes

@app.route('/VAC/db/test/', methods=['GET'])
def db_test():
    table: dict = database.test_get()
    logger.log_entry('Server', 'Database', logger.create_json('Db test request'))
    return response_json(table)


@app.route('/VAC/db/Users/', methods=['POST', 'DELETE'])
def db_user():
    if request.method == 'POST':
        payload = json.loads(request.data)
        if database.user_insert(payload['login'], payload['passwd']):
            return '', 201
        else:
            return '', 400
    elif request.method == 'DELETE':
        payload = json.loads(request.data)
        login: str = payload['login']
        password: str = payload['passwd']

        if database.user_validate(login, password):
            payload = json.loads(request.data)
            database.user_delete(payload('login'))
            return '', 200
        else:
            return '', 401


@app.route('/VAC/db/Statistics/', methods=['GET'])
def db_statistics():
    payload = json.loads(request.data)

    try:
        login: str = payload['login']
        password: str = payload['passwd']
        s_type: str = payload['type']
    except Exception:
        return '', 400

    if database.user_validate(login, password):
        if s_type == 'data_amount':
            pass
        elif s_type == 'session_time':
            pass
        elif s_type == 'login_history':
            pass
        return '{"0": "TODO"}', 200
    else:
        return '', 401


def main():
    global server_id
    global streamer_port

    try:
        conn = database.connect()
        conn.close()
        server_id = logger.server()
        managing.server_id = server_id
        logger.server_id = server_id
        logger.log_entry('Server', 'Started', '')
    except Exception:
        print('Could not connect to database')
        return

    atexit.register(managing.cleanup)

    if not debug:
        print('Waiting for RTP streamer connection . . .')
        while True:
            try:
                managing.rtp_server_socket.connect(("127.0.0.1", streamer_port))
            except Exception:
                continue
            break

    managing.start_manager_thread()
    managing.start_performance_log_thread()

    app.run(
        host='0.0.0.0',
        port=80,
        threaded=True,
        debug=False
    )


if __name__ == '__main__':
    main()
