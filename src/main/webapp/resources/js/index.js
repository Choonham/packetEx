$(document).ready(function () {

    $("#lookUp").on("click", function() {
        let hostName = $("#hostName").val();

        $.ajax({
            url: "capture/getIP.do" , //컨트롤러 URL
            data: {
                hostName: hostName
            },
            beforeSend: function(xhr) {
                xhr.setRequestHeader("X-CSRF-TOKEN", $('#_csrf').val());
            },
            async: false,
            dataType: 'json',
            type: 'POST',
            success: function (data2) {
                $("#ipAddress").empty();
                $("#ipAddress").append("IP: " + data2.result);
            },
            error: function (jqXHR) {
                console.log(jqXHR);
            }
        });
    });

    $("#start").on("click", function() {

        $.ajax({
            url: "capture/start.do" , //컨트롤러 URL
            data: {
                expression: $("#expression").val()
            },
            beforeSend: function(xhr) {
                xhr.setRequestHeader("X-CSRF-TOKEN", $('#_csrf').val());
            },
            async: false,
            dataType: 'json',
            type: 'POST',
            success: function (data2) {
                if (data2 != 0) {
                    $("#status").text("패킷 캡쳐 중...");
                } else {
                    $("#status").text("패킷를 시작하지 못했습니다.");
                }
            },
            error: function (jqXHR) {
                console.log(jqXHR);
            }
        });
    });

    $("#stop").on("click", function() {

        $.ajax({
            url: "capture/stop.do" , //컨트롤러 URL
            data: {
            },
            beforeSend: function(xhr) {
                xhr.setRequestHeader("X-CSRF-TOKEN", $('#_csrf').val());
            },
            async: false,
            dataType: 'json',
            type: 'POST',
            success: function (data2) {
                if (data2 != 0) {
                    $("#status").text("패킷 캡쳐 중지");
                } else {
                    $("#status").text("패킷 캡쳐를 중지하지 못했습니다.");
                }
            },
            error: function (jqXHR) {
                console.log(jqXHR);
            }
        });
    });


});