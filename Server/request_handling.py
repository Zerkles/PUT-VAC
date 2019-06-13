import json

from flask import Response, request, render_template

import database
import logger
import managing


def response_json(data: dict) -> Response:
    json_str = json.dumps(data)
    response = Response(200)
    response.data = json_str
    response.content_type = 'text/json'
    return response


def connect():
    response: str = managing.add_client(request)
    if response == 'incorrect credentials':
        return '', 401
    else:
        return response, 200


def disconnect():
    response: str = managing.remove_client(request)
    if response == 'disconnect success':
        return '', 200
    else:
        return '', 409


def test():
    return Response(200, "<p>VAC is working!</p>")


def manager():
    return render_template('Index.html', name='VAC Server Manager')


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

def db_test():
    table: dict = database.test_get()
    logger.log_entry('Server', 'Database', logger.create_json('Db test request'))
    return response_json(table)


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

        if database.user_authenticate(login, password):
            payload = json.loads(request.data)
            database.user_delete(payload('login'))
            return '', 200
        else:
            return '', 401


def db_statistics():
    try:
        login: str = request.args['login']
        password: str = request.args['passwd']
        s_type: str = request.args['type']
    except Exception:
        return '', 400

    if database.user_authenticate(login, password):
        if s_type == 'data_amount':
            pass
        elif s_type == 'session_time':
            pass
        elif s_type == 'login_history':
            pass
        elif s_type == 'test':
            table: dict = database.test_get()
            logger.log_entry('Server', 'Database', logger.create_json('Db test request'))
            return response_json(table), 200
        return '{"0": ["TODO1"],"1": ["TODO2"]}', 200
    else:
        return '', 401


def db_logger():
    resp: str = ''

    return resp, 200
