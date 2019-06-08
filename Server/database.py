import json
from pathlib import Path

import pymssql

config_json = json.loads(Path('config.json').read_text())


def connect():
    global config_json
    # Connect to Microsoft SQL server
    conn = pymssql.connect(
        server=config_json['server'],
        user=config_json['user'],
        password=config_json['password'],
        database=config_json['database']
    )
    return conn


def test_get() -> dict:
    conn = connect()
    cursor = conn.cursor()
    # Get all students from database
    cursor.execute('select * from Test_Table;')
    row = cursor.fetchone()
    data: dict = {'0': []}

    for elem in cursor.description:
        data['0'].append(elem[0])
        pass

    i: int = 1

    while row:
        data[str(i)] = []
        for elem in row:
            data[str(i)].append(elem)
        i += 1
        row = cursor.fetchone()
    return data
