from flask_restplus import fields


class Models:
    """
    Model container class
    """
    db_table = None
    user_login = None

    def __init__(self, api):
        self.db_table = api.model('Database table', {
            '0': fields.List(fields.String(), required=True, readOnly=True, min_items=1),
            '1': fields.List(fields.String(), required=False, readOnly=True, min_items=1),
            '2': fields.List(fields.String(), required=False, readOnly=True, min_items=1),
            '...': fields.List(fields.String(), required=False, readOnly=True, min_items=1),
            'n': fields.List(fields.String(), required=False, readOnly=True, min_items=1)
        })
        self.user_login = api.model('Login data', {
            'login': fields.String(required=True, readOnly=True),
            'passwd': fields.String(required=True, readOnly=True)
        })
