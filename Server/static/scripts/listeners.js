$(document).ready(function () {
    $('#GitHub').on("click", function () {
        window.open('https://github.com/Zerkles/PUT-VAC', '_blank');
    });
    $('#PP').on("click", function () {
        window.open('https://www.put.poznan.pl', '_blank');
    });
    $('#But_Connect').on("click", function () {
        server_connect();
    });
    $('#But_Disconnect').on("click", function () {
        server_disconnect();
    });
    $('#But_Shutdown').on("click", function () {
        server_shutdown();
    });
});