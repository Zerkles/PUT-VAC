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

no_streamer: bool = False
no_database: bool = False

server_id: int
streamer_port: int = 9999

ns_db = api.namespace('VAC/db', description='Operations related to database')
ns_vac = api.namespace('VAC', description='Operations related to base functionality')


@api.representation('text/html')
def output_html(data, code, headers):
    resp = Response(data, status=code)
    resp.headers.extend(headers or {})
    return resp


@ns_vac.route('/login', endpoint='login')
@ns_vac.response(200, 'Success')
@ns_vac.response(401, 'Unauthorized')
class Login(Resource):
    def get(self):
        """
        Authenticates user
        """
        resp = request_handling.login()
        return resp


@ns_vac.route('/connect', endpoint='connect')
@ns_vac.response(200, 'Success')
@ns_vac.response(401, 'Unauthorized')
class Connect(Resource):
    def get(self):
        """
        Executes connect procedure
        """
        resp = request_handling.connect()
        return resp


@ns_vac.route('/disconnect', endpoint='disconnect')
@ns_vac.response(200, 'Success')
@ns_vac.response(409, 'Not connected')
class Disconnect(Resource):
    def get(self):
        """
        Disconnects connected client
        """
        resp = request_handling.disconnect()
        return resp


@ns_vac.route('/', endpoint='VAC')
@ns_vac.response(200, 'Success')
class Test(Resource):

    @ns_vac.doc('vac_test')
    def get(self):
        """
        Returns text indicating that server is working
        """
        return request_handling.test()


@ns_vac.route('/manager', endpoint='manager')
@ns_vac.response(200, 'Success')
class Manager(Resource):
    @ns_vac.doc('get_manager')
    def get(self):
        """
        Returns VAC manager site
        """
        resp = request_handling.manager()
        return resp


@ns_vac.route('/shutdown', endpoint='shutdown')
@ns_vac.response(200, 'Success')
class Shutdown(Resource):
    @ns_vac.doc('Shutdown_server')
    def get(self):
        """
        Starts server shutdown procedure
        """
        resp = request_handling.shutdown()
        return resp


# Database routes

@ns_db.route('/Test_Table', endpoint='Test_Table')
@ns_db.response(200, 'Success', models.db_table)
class DbTest(Resource):
    @ns_db.doc('get_test_table')
    def get(self):
        """
        Returns test table from database
        """
        resp = request_handling.db_test()
        return resp


@ns_db.route('/Users', endpoint='Users')
@ns_db.expect(models.user_login)
class DbUsers(Resource):
    @ns_db.response(201, 'Created')
    @ns_db.response(409, 'User already exists')
    def post(self):
        """
        Adds user to database
        """
        resp = request_handling.db_user_post()
        return resp

    @ns_db.response(200, 'Success')
    @ns_db.response(401, 'Unauthorized')
    def delete(self):
        """
        Removes user from database
        """
        resp = request_handling.db_user_delete()
        return resp
        pass


@ns_db.route('/Statistics', endpoint='Statistics')
@ns_db.response(200, 'Success', models.db_table)
@ns_db.response(400, 'Missing arguments')
@ns_db.response(401, 'Unauthorized')
class DbStatistics(Resource):
    @ns_db.doc('get_statistics', params={
        'login': 'User login',
        'passwd': 'User password',
        'type': 'Type of data to get from table'
    })
    def get(self):
        """
        Returns chosen statistics from database
        """
        resp = request_handling.db_statistics()
        return resp


# TODO
@app.route('/Loggers', methods=['GET'])
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
