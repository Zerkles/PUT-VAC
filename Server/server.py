from flask import Flask, Response
from flask_cors import CORS
from flask_restplus import Resource, Api

import json
import atexit

import managing
import database
import logger
from Models import Models

# app init
import request_handling

app = Flask(__name__, template_folder='templates')
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'

api = Api(app, version='1.0', title='VAC API',
          description='An API for VAC project')

models: Models = Models(api)

no_streamer: bool = True
no_database: bool = False

server_id: int
streamer_port: int = 2


@api.representation('text/html')
def output_html(data, code, headers):
    resp = Response(data, status=code)
    resp.headers.extend(headers or {})
    return resp


@api.route('/VAC/connect', endpoint='connect')
@api.expect(models.user_login)
@api.response(200, 'Success')
@api.response(401, 'Unauthorized')
class Connect(Resource):
    def get(self):
        resp = request_handling.connect()
        return resp


@api.route('/VAC/disconnect', endpoint='disconnect')
@api.response(200, 'Success')
@api.response(409, 'Not connected')
class Disconnect(Resource):
    def get(self):
        resp = request_handling.disconnect()
        return resp


@api.route('/VAC/', endpoint='VAC')
@api.response(200, 'Success')
class Test(Resource):
    def get(self):
        return request_handling.test()


@api.route('/VAC/manager', endpoint='manager')
@api.response(200, 'Success')
class Manager(Resource):
    @api.doc('get_manager')
    def get(self):
        resp = request_handling.manager()
        return resp


@api.route('/VAC/shutdown', endpoint='shutdown')
@api.response(200, 'Success')
class Shutdown(Resource):
    @api.doc('Shutdown_server')
    def get(self):
        resp = request_handling.shutdown()
        return resp


# Database routes

@api.route('/VAC/db/Test_Table', endpoint='db/Test_table')
@api.response(200, 'Success', models.db_table)
class DbTest(Resource):
    @api.doc('get_test_table')
    def get(self):
        resp = request_handling.db_test()
        return resp


@api.route('/VAC/db/Users', endpoint='db/Users')
@api.expect(models.user_login)
class DbUsers(Resource):
    @api.response(201, 'Created')
    @api.response(409, 'User already exists')
    def post(self):
        resp = request_handling.db_user_post()
        return resp

    @api.response(200, 'Success')
    @api.response(401, 'Unauthorized')
    def delete(self):
        resp = request_handling.db_user_delete()
        return resp
        pass


@api.route('/VAC/db/Statistics', endpoint='db/Statistics')
@api.response(200, 'Success', models.db_table)
@api.response(400, 'Missing arguments')
@api.response(401, 'Unauthorized')
class DbStatistics(Resource):
    @api.doc('get_statistics', params={
        'login': 'User login',
        'password': 'User password',
        'type': 'Type of data to get from table'
    })
    def get(self):
        resp = request_handling.db_statistics()
        return resp


@app.route('/VAC/db/Loggers', methods=['GET'])
def db_logger():
    return request_handling.db_logger()


def main():
    global server_id
    global streamer_port

    if not no_database:
        try:
            print('Connecting to database . . .')
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
        if not no_database:
            logger.log_entry('Server', 'Started', '')

    atexit.register(managing.cleanup)

    if not no_streamer:
        print('Waiting for RTP streamer connection . . .')
        while True:
            try:
                managing.rtp_server_socket.connect(("127.0.0.1", streamer_port))
            except Exception:
                continue
            break

    managing.start_manager_thread()

    if not no_database:
        managing.start_performance_log_thread()

    app.run(
        host='0.0.0.0',
        port=80,
        threaded=True,
        debug=False
    )


if __name__ == '__main__':
    main()
