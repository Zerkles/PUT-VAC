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

    if row is None:
        conn.close()
        return 'None'
    else:
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


def server_exists(s_id: int):
    result: bool = False
    sql_query = 'select \'true\' from Servers where ServerID=' + str(s_id)

    conn = connect()
    cursor = conn.cursor()
    cursor.execute(sql_query)
    row = cursor.fetchone()

    if row is not None:
        result = True
    conn.close()
    return result


def server_insert(info: dict):
    s_id = str(info['s_id'])
    os = "'" + info['os'] + "'"
    os_ver = "'" + info['os_ver'] + "'"
    p_ver = "'" + info['p_ver'] + "'"
    n_name = "'" + info['n_name'] + "'"
    cpu = "'" + info['cpu'] + "'"
    ram_size = str(info['ram_size'])

    sql_query: str = ''

    if server_exists(int(s_id)):
        sql_query = 'update Servers set ' \
                    '[OS Name]=' + os + ',[OS Version]=' + os_ver + ',' + \
                    '[Python Version]=' + p_ver + ',[Node Name]=' + n_name + ',' + \
                    'CPU=' + cpu + ',' + '[Memory Size]=' + ram_size + \
                    'where ServerID=' + s_id
    else:
        sql_query = 'insert into Servers values ' \
                    '(' + s_id + ',' + s_id + ',' + \
                    os + ',' + os_ver + ',' + p_ver + ',' + \
                    n_name + ',' + cpu + ',' + ram_size + ');'

    query_non_return(sql_query)


# Users

def user_insert(login: str, passwd: str) -> None:
    u_id = str(int(user_max_id()) + 1)

    sql_query = 'insert into Users values (' + u_id + ',\'' + login + '\',\'' + passwd + '\');'
    query_non_return(sql_query)


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
    sql_query = 'select * from Users where Login=\'' + login + '\''
    result = select_one_value(sql_query)
    if result == 'None':
        result = '0'
    return int(result)


def user_authenticate(login: str, passwd: str) -> bool:
    sql_query = 'select * from Users where Login=\'' + login + '\' and Password=\'' + passwd + '\''
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
    date = '\'' + datetime.now().strftime('%Y-%m-%d %H:%M:%S') + '\''
    sql_query = 'insert into [Statistics] values' + \
                '(' + s_id + ',\'' + name + '\',\'' + val + '\',' + date + ')'
    query_non_return(sql_query)


def statistics_select_data_amount(s_id: int) -> dict:
    conn = connect()
    cursor = conn.cursor()
    sql_query_1: str = 'select [Value], St.Date from [Statistics] St, [Sessions] Se ' + \
                       'where St.SessionID=Se.SessionID ' + \
                       "and Name = 'Data received' and UserID=" + str(s_id)

    sql_query_2: str = 'select [Value] from [Statistics] St, [Sessions] Se ' + \
                       'where St.SessionID=Se.SessionID ' + \
                       "and Name = 'Data sent' and UserID=" + str(s_id)

    cursor.execute(sql_query_1)
    data_1: dict = table_to_dict(cursor)
    cursor.execute(sql_query_2)
    data_2: dict = table_to_dict(cursor)
    conn.close()
    for key in data_1.keys():
        if key == '0':
            data_1[key][0] = 'Data sent'
            data_1[key][1] = 'Data received'
            data_1[key].append('Date')
        else:
            data_1[key][0] = str(data_1[key][0]) + ' KB'
            data_1[key].append(data_1[key][1].strftime('%Y-%m-%d %H:%M:%S'))
            data_1[key][1] = str(data_2[key][0]) + ' KB'
    return data_1


def statistics_select_session_history(s_id: int) -> dict:
    conn = connect()
    cursor = conn.cursor()
    sql_query: str = 'select Se.Date, St.Date as Duration from [Statistics] St, [Sessions] Se ' + \
                     'where St.SessionID=Se.SessionID and UserID=' + str(s_id) + \
                     ' group by St.SessionID, Se.SessionID, St.Date, Se.Date ' + \
                     'having St.Date=max(St.Date);'

    cursor.execute(sql_query)
    data: dict = table_to_dict(cursor)
    conn.close()
    for key in data.keys():
        if key == '0':
            pass
        else:
            data[key][1] = str((data[key][1] - data[key][0]).seconds) + ' s'
            data[key][0] = data[key][0].strftime('%Y-%m-%d %H:%M:%S')
    return data


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


def session_select_login_dates(s_id: int):
    conn = connect()
    cursor = conn.cursor()
    sql_query: str = 'select [Date] from [Sessions] where UserID=' + str(s_id)

    cursor.execute(sql_query)
    data: dict = table_to_dict(cursor)
    conn.close()
    for key in data.keys():
        if key == '0':
            data[key][0] = 'Login date'
        else:
            data[key][0] = data[key][0].strftime('%Y-%m-%d %H:%M')
    return data


# Loggers

def loggers_exists(l_id: int) -> bool:
    sql_query = 'select * from Loggers ' + \
                'where LoggerID=' + str(l_id)
    conn = connect()
    cursor = conn.cursor()
    cursor.execute(sql_query)
    row = cursor.fetchone()

    result: bool = False

    if row is not None:
        result = True
    conn.close()
    return result


def loggers_insert(s_id: int):
    sql_query = 'insert into Loggers values' + \
                '(' + str(s_id) + ',' + str(s_id) + ')'
    query_non_return(sql_query)


def loggers_select_connection():
    pass


# Clients

def client_difference(info) -> bool:
    u_id = str(user_get_id(info['login']))
    os = "'" + info['os'] + "'"
    os_ver = "'" + info['os_ver'] + "'"
    brand = "'" + info['brand'] + "'"
    model = "'" + info['model'] + "'"
    sql_query = 'select * from Client ' + \
                'where UserID=' + u_id + ' and [OS Name]=' + os + ' and ' + \
                '[OS Version]=' + os_ver + ' and [Device Brand]=' + brand + \
                ' and [Device Model]=' + model
    conn = connect()
    cursor = conn.cursor()
    cursor.execute(sql_query)
    row = cursor.fetchone()

    print(sql_query)

    result: bool = False

    if row is None:
        result = True
    conn.close()
    return result


def client_insert(info):
    if client_difference(info):
        u_id = str(user_get_id(info['login']))
        os = "'" + info['os'] + "'"
        os_ver = "'" + info['os_ver'] + "'"
        brand = "'" + info['brand'] + "'"
        model = "'" + info['model'] + "'"

        sql_query = 'insert into Client values' + \
                    '(' + str(u_id) + ',' + os + ',' + os_ver + ',' + brand + ',' + model + ')'
        print(sql_query)
        query_non_return(sql_query)
