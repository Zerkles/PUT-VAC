import json

from flask import Flask, Response
from flask_cors import CORS

import atexit

import managing
import database
import logger

# app init
import request_handling

app = Flask(__name__, template_folder='templates')
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'

debug: bool = False

server_id: int
streamer_port: int = 2


def response_json(data: dict) -> Response:
    json_str = json.dumps(data)
    response = Response(200)
    response.data = json_str
    response.content_type = 'text/json'
    return response


@app.route('/VAC/connect', methods=['GET'])
def connect(): request_handling.connect()


@app.route('/VAC/disconnect', methods=['GET'])
def disconnect(): request_handling.disconnect()


@app.route('/VAC/')
def test(): request_handling.test()


@app.route('/VAC/manager', methods=['GET'])
def manager(): request_handling.manager()


@app.route('/VAC/shutdown', methods=['GET'])
def shutdown(): request_handling.shutdown()


# Database routes

@app.route('/VAC/db/test/', methods=['GET'])
def db_test(): request_handling.db_test()


@app.route('/VAC/db/Users', methods=['POST', 'DELETE'])
def db_user(): request_handling.db_user()


@app.route('/VAC/db/Statistics', methods=['GET'])
def db_statistics(): request_handling.db_statistics()


@app.route('/VAC/db/Loggers', methods=['GET'])
def db_logger(): request_handling.db_logger()


def main():
    global server_id
    global streamer_port

    try:
        print('Connecting to database . . ')
        conn = database.connect()
        conn.close()
    except Exception:
        print('Could not connect to database')
        return

    server_id = logger.server()
    managing.server_id = server_id
    logger.server_id = server_id
    if not database.loggers_exists(server_id):
        database.loggers_insert(server_id)

    logger.log_entry('Server', 'Started', '')

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
