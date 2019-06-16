import json

from flask import Response, request, render_template

import database
import logger
import managing
import server


def response_json(data: dict, status: int) -> Response:
    json_str = json.dumps(data, indent=1)
    response = Response(status)
    response.data = json_str
    response.content_type = 'text/json'
    return response


def response_html(data: str, status: int) -> Response:
    response = Response(status)
    response.data = data
    response.content_type = 'text/html'
    return response


def json_to_html(data: dict) -> str:
    result = '<table>'
    for key in data.keys():
        result += '<tr>'
        if key == '0':
            for elem in data[key]:
                result += '<th>'
                result += elem
                result += '</th>'
        else:
            for elem in data[key]:
                result += '<td>'
                if elem is not None:
                    result += elem
                else:
                    result += 'NULL'
                result += '</td>'
        result += '</tr>'
    result += '</table>'
    return result


def login():
    if database.user_authenticate(request.args['login'], request.args['passwd']):
        database.client_insert(request.args)
        return Response(status=200)
    else:
        return Response(status=401)


def connect():
    response: str = managing.add_client(request)
    if response == 'incorrect credentials':
        return Response(status=401)
    else:
        return Response(response, status=200)


def disconnect():
    response: str = managing.remove_client(request)
    if response == 'disconnect success':
        return Response(status=200)
    else:
        return Response(status=409)


def test():
    return response_html("<p>VAC is working!</p>", 200)


def manager():
    return response_html(render_template('Index.html', name='VAC Server Manager'), 200)


def shutdown():
    managing.server_shutdown = True
    func = request.environ.get('werkzeug.server.shutdown')
    if func is None:
        raise RuntimeError('Not running with the Werkzeug Server')
    func()

    print("Server shutting down . . .")
    if not server.no_database:
        logger.log_entry('Server', 'Shutdown', '')
    return Response('<p>Server shutting down...</p>', status=200)


# Database routes

def db_test():
    table: dict = database.test_get()
    if not server.no_database:
        logger.log_entry('Server', 'Database', json.dumps({
            'description': 'Db test request'
        }))
    return response_json(table, 200)


def db_user_post():
    payload = json.loads(request.data)
    if database.user_exists(payload['login']):
        return Response(status=409)

    database.user_insert(payload['login'], payload['passwd'])
    return Response(status=201)


def db_user_delete():
    payload = json.loads(request.data)
    login: str = payload['login']
    password: str = payload['passwd']

    if database.user_authenticate(login, password):
        database.user_delete(login)
        return Response(status=200)
    else:
        return Response(status=401)


def db_statistics():
    try:
        login: str = request.args['login']
        password: str = request.args['passwd']
        s_type: str = request.args['type']
    except Exception:
        return Response(status=400)

    logger.log_entry('Server', 'Database', json.dumps({
        'login': login,
        'description': 'Db statistics request'
    }))

    if database.user_authenticate(login, password):
        s_id = database.user_get_id(login)
        if s_type == 'data_amount':
            data = database.statistics_select_data_amount(s_id)
            return Response(json.dumps(data), 200)
        elif s_type == 'session_history':
            data = database.statistics_select_session_history(s_id)
            return Response(json.dumps(data), 200)
        elif s_type == 'login_history':
            data = database.session_select_login_dates(s_id)
            return Response(json.dumps(data), 200)
        else:
            return Response(status=404)
    else:
        return Response(status=401)


def db_logs():
    try:
        l_type: str = request.args['type']
        format: str = request.args['format']
    except Exception:
        return Response(status=400)

    logger.log_entry('Server', 'Database', json.dumps({
        'description': 'Db logs request'
    }))

    if l_type == 'performance':
        data = database.logs_select_performance(logger.server_id)
    elif l_type == 'connection':
        data = database.logs_select_client(logger.server_id)
    elif l_type == 'server_activity':
        data = database.logs_select_activity(logger.server_id)
    else:
        return Response(status=404)

    if format.upper() == 'HTML':
        return Response(json_to_html(data), 200)
    else:
        return Response(json.dumps(data, indent=1), 200)


