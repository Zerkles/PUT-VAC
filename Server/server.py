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

debug: bool = True

server_id: int


def response_json(data: dict) -> Response:
    json_str = json.dumps(data, indent=1)
    response = Response(200)
    response.data = json_str
    response.content_type = 'text/json'
    return response


@app.route('/VAC/connect', methods=['GET'])
def connect():
    response: str = managing.add_client(request)
    return response, 200


@app.route('/VAC/disconnect', methods=['GET'])
def disconnect():
    response: str = managing.remove_client(request)
    return response, 200


@app.route('/VAC/')
def test():
    return "<p>VAC is working!</p>", 200


@app.route('/VAC/manager', methods=['GET'])
def manager():
    return render_template('Index.html', name='VAC Server Manager')


@app.route('/VAC/shutdown', methods=['GET'])
def shutdown():
    global stop_all

    stop_all = True
    func = request.environ.get('werkzeug.server.shutdown')
    if func is None:
        raise RuntimeError('Not running with the Werkzeug Server')
    func()

    print("Server shutting down...")
    logger.log(logger.create_json('Server', 'Shutdown'))
    return '<p>Server shutting down...</p>', 200


# Database routes

@app.route('/VAC/db/test/', methods=['GET'])
def db_test():
    table: dict = database.test_get()
    logger.log(logger.create_json('Server', 'Db test request'))
    return response_json(table)


@app.route('/VAC/db/Users/', methods=['POST', 'DELETE'])
def db_user():
    if request.method == 'POST':
        payload = json.loads(request.data)
        if database.user_insert(payload['login'], payload['passwd']):
            return 201
        else:
            return 400
    elif request.method == 'DELETE':
        payload = json.loads(request.data)
        database.user_delete(payload('login'))
        return 200


@app.route('/VAC/db/Statistics/', methods=['GET'])
def db_statistics():
    login: str = request.args['login']
    type: str = request.args['type']
    if type == 'data_amount':
        pass
    elif type == 'session_time':
        pass
    elif type == 'login_history':
        pass
    return '{"0": "TODO"}'


def main():
    global server_id

    try:
        conn = database.connect()
        conn.close()
        server_id = logger.server()
        managing.server_id = server_id
        logger.log(logger.create_json('Server', 'Started'))
    except Exception:
        print('Could not connect to database')
        return

    atexit.register(managing.cleanup)

    if not debug:
        print('Waiting for RTP streamer connection . . .')
        while True:
            try:
                managing.rtp_server_socket.connect(("127.0.0.1", 49152))
            except Exception:
                continue
            break

    managing.start_manager_thread()

    app.run(
        host='0.0.0.0',
        port=int(80),
        threaded=True,
        debug=False
    )


if __name__ == '__main__':
    main()
