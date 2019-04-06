function server_connect() {
    jQuery.get({
        url: 'http://localhost:80/VAC/connect',
    }, function (data, textStatus, jqXHR) {
        alert(data);
    });
}

function server_disconnect() {
    jQuery.get({
        url: 'http://localhost:80/VAC/disconnect',
    }, function (data, textStatus, jqXHR) {
        alert(data);
    });
}

function server_shutdown() {
    jQuery.get({
        url: 'http://localhost:80/VAC/shutdown',
    }, function (data, textStatus, jqXHR) {
        alert(data);
    });
}