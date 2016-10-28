$(document).ready(function() {
    $("#login-form").submit(function (event) {
        var name = $("#name").val();
        var pass = $("#pass").val();
        var passConf = $("#confirm-pass").val();
        var email = $("#email").val();
        if (pass !== passConf) {
            $("#status-message").html("<p class=\"status-message\">Passwords must match</p>");
        } else if (pass.length > 64) {
            $("#status-message").html("<p class=\"status-message\">Password length must be lower than 64 symbols</p>");
        } else if (pass.length < 8) {
            $("#status-message").html("<p class=\"status-message\">Password length must be at least 8 symbols</p>");
        } else {
            $.ajax({
                url: "/signup",
                type: "POST",
                data: {"user_name": name, "user_pass": pass, "user_email": email},
                success: function (data) {
                    window.location.replace("/signupconfirm")
                },
                error: function (data) {
                    $("#status-message").html("<p class=\"status-message\">" + data.str + "</p>");
                }
            });
        }
        event.preventDefault();
    });

});