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

    u_id = str(int(user_max_id()) + 1)

    sql_query = 'insert into Users values (' + u_id + ',\'' + login + '\',\'' + passwd + '\');'
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


def user_get_id(login: str) -> int:
    sql_query = 'select * from Users where Login=' + login
    result = select_one_value(sql_query)
    if result == 'None':
        result = '0'
    return int(result)


def user_validate(login: str, passwd: str) -> bool:
    sql_query = 'select * from Users where Login=' + login + ' and Password=' + passwd
    result = select_one_value(sql_query)
    if result == 'None':
        return False
    else:
        return True


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

    e_id = str(entries_max_id() + 1)

    date = '\'' + datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3] + '\''
    sql_query = 'insert into Entries values' + \
                '(' + e_id + ',\'' + entry_type + '\',\'' + event + '\',' + content + ',' + date + ')'
    query_non_return(sql_query)


# Statistics

def statistics_insert(s_id: str, name: str, val: str) -> None:
    date = '\'' + datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3] + '\''
    sql_query = 'insert into [Statistics] values' + \
                '(' + s_id + ',\'' + name + '\',\'' + val + '\',' + date + ')'
    query_non_return(sql_query)


# Sessions

def session_max_id() -> int:
    sql_query = 'select max(SessionID) from Sessions;'
    result = select_one_value(sql_query)
    if result == 'None':
        result = '0'
    return int(result)


def session_insert(s_id: int, u_id: int, ser_id: int) -> None:
    date = '\'' + datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3] + '\''
    sql_query = 'insert into Sessions values' + \
                '(' + str(s_id) + ',' + str(u_id) + ',' + str(ser_id) + ',' + date + ')'
    query_non_return(sql_query)
