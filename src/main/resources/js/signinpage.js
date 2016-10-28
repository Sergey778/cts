$(document).ready(function() {
    $("#login-form").submit(function (event) {
        var name = $("#login-name").val();
        var pass = $("#login-pass").val();
        $.ajax({
            url: "/signin",
            type: "POST",
            data: {"user_name": name, "user_password": pass},
            success: function (data) {
                window.location.replace("/index")
            },
            error: function (data) {
                $("#status-message").html("<p class=\"status-message\">Incorrect Password</p>");
            }
        });
        event.preventDefault();
    });

});