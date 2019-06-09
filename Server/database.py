import json
from pathlib import Path
from datetime import datetime

import pymssql

config_json: dict = json.loads(Path('config.json').read_text())


def query_non_return(sql_query: str) -> None:
    conn = connect()
    cursor = conn.cursor()
    cursor.execute(sql_query)
    conn.commit()
    conn.close()


def select_one_value(sql_query: str) -> str:
    conn = connect()
    cursor = conn.cursor()
    cursor.execute(sql_query)
    row = cursor.fetchone()
    result = str(row[0])
    conn.close()
    return result


def table_to_dict(cursor) -> dict:
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
    cursor.execute('select * from Test_Table;')
    data: dict = table_to_dict(cursor)
    conn.close()
    return data


# Server

def server_max_id() -> int:
    sql_query = 'select max(ServerID) from Servers;'
    result = select_one_value(sql_query)
    if result == 'None':
        result = '0'
    return int(result)


def server_insert(info: dict):
    v1 = str(info['s_id'])
    v3 = info['os']
    v4 = info['os_ver']
    v5 = info['p_ver']
    v6 = info['n_name']
    v7 = info['cpu']
    v8 = str(info['ram_size'])

    sql_query = 'delete from Servers where ServerID=' + v1 + ';' + \
                'insert into Servers values ' \
                '(' + v1 + ',' + v1 + ',\'' + \
                v3 + '\',\'' + v4 + '\',\'' + v5 + '\',\'' + \
                v6 + '\',\'' + v7 + '\',' + v8 + ');'
    query_non_return(sql_query)


# Users

def user_insert(login: str, passwd: str) -> bool:
    if user_exists(login):
        return False

    uid = str(int(user_max_id()) + 1)

    sql_query = 'insert into Users values (' + uid + ',\'' + login + '\',\'' + passwd + '\');'
    query_non_return(sql_query)

    return True


def user_delete(login: str):
    sql_query = 'delete from Users where Login = \'' + login + '\''
    query_non_return(sql_query)


def user_max_id() -> int:
    sql_query = 'select max(UserID) from Users;'
    result = select_one_value(sql_query)
    if result == 'None':
        result = '0'
    return int(result)


def user_exists(login: str) -> bool:
    result: bool = False
    sql_query = 'select \'true\' from Users where Login=\'' + login + '\''
    conn = connect()
    cursor = conn.cursor()
    cursor.execute(sql_query)
    row = cursor.fetchone()
    if row is not None:
        result = True

    conn.close()
    return result


# Entries

def entries_max_id() -> int:
    sql_query = 'select max(EntryID) from Entries;'
    result = select_one_value(sql_query)
    if result == 'None':
        result = '0'
    return int(result)


def entries_insert(entry_type: str, event: str, content: str) -> None:
    if content == '':
        content = 'null'
    else:
        content = '\'' + content + '\''

    eid = str(entries_max_id() + 1)

    date = '\'' + datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3] + '\''
    sql_query = 'insert into Entries values' + \
                '(' + eid + ',\'' + entry_type + '\',\'' + event + '\',' + content + ',' + date + ')'
    query_non_return(sql_query)
