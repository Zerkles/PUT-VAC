import database
from pathlib import Path
import json
import platform
import psutil

server_id: int


def create_json_client(login: str, content: str) -> str:
    result = {
        'login': login,
        'content': content
    }
    return json.dumps(result)


def create_json(content: str) -> str:
    result = {'content': content}
    return json.dumps(result)


def server() -> int:
    sid = database.server_max_id() + 1

    if 'serverId' not in database.config_json:
        database.config_json['serverId'] = sid
        data_str = json.dumps(database.config_json, indent=1)
        Path('config.json').write_text(data_str)

    uname: platform.uname_result = platform.uname()
    info = {
        's_id': sid,
        'os': str(uname.system),
        'os_ver': str(uname.version),
        'p_ver': str(platform.python_version()),
        'n_name': str(uname.node),
        'cpu': str(uname.processor),
        'ram_size': int(psutil.virtual_memory().total / 1000000)
    }
    database.server_insert(info)
    return sid


def log(entry_type: str, event: str, content: str) -> None:
    database.entries_insert(entry_type, event, content)


def performance():
    global server_id

    ram_usage = int(psutil.virtual_memory().used / 1000000)
    ram_percent = int(psutil.virtual_memory().percent)
    cpu_percent = 0.0
    while cpu_percent == 0.0:
        cpu_percent = psutil.cpu_percent(interval=0.05)

    perf_json: dict = {
        'ram_MB': ram_usage,
        'ram_%': ram_percent,
        'cpu_%': cpu_percent
    }

    log('Server', 'Performance', json.dumps(perf_json))
