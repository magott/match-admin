function trioOn() {
    $('#assFee').show('fast').removeAttr("disabled");
    $('.ass-controls').show('fast');
    $('#appointedAssistant1').show('fast').removeAttr("disabled");
    $('#appointedAssistant2').show('fast').removeAttr("disabled");
}

function trioOff() {
    $('#assFee').hide().attr("disabled", "disabled");
    $('#appointedAssistant1').hide().attr("disabled", "disabled");
    $('.ass-controls').hide();
    $('#appointedAssistant2').hide().attr("disabled", "disabled");
}

function isTrio() {
    var refType = $('form input[name=refType]:checked').val()
    return  refType == "trio"
}

function editMatchFunctions() {
    $('#refTypeDommer').click(trioOff);
    $('#refTypeTrio').click(trioOn);
    $('#appointedRef').change(setUserLink)
    $('#appointedAssistant1').change(setUserLink)
    $('#appointedAssistant2').change(setUserLink)
    $('#appointedRef').trigger('change');
    $('#appointedAssistant1').trigger('change');
    $('#appointedAssistant2').trigger('change');
    validateMatchForm();
    if (!isTrio()) {
        trioOff();
    }
    $('#match-form').change(function(evt) {
        $('#send-mail').attr("disabled","disabled");
    });
    $('#send-mail').click(sendMail);
    $('#delete-match').click(deleteResource);
}

function validateMatchForm() {

    $('#match-form').validate(
        {
            onkeyup: false,
            errorPlacement:function (error, element) {
                error.appendTo(element.nextAll("span"));
            },
            rules:{
                home:{
                    required:true
                },
                away:{
                    required:true
                },
                venue:{
                    required:true
                },
                date:{
                    required:true
                },
                time:{
                    required:true,
                    time: true
                },
                level:{
                    required:true
                },
                refType:{
                    required:true
                },
                refFee:{
                    required:true
                },
                assFee:{
                    depends:function (element) {
                        return $("#refTypeTrio:checked")
                    }
                }
            },
            messages:{
                home:"Hjemmelag må fylles ut",
                away:"Bortelag må fylles ut",
                venue:"Bane må fylles ut",
                name:"Navn må fylles ut",
                time:{
                    required: "Tidspunkt for kampen må fylles ut",
                    time: "Ugyldig tidsformat (gyldig format f.eks 23:59)"
                },
                date:"Dato for kampen må fylles ut",
                level:"Nivå for kampen må fylles ut",
                refFee:"Dommerhonorar må fylles ut",
                assFee:"AD-honorar må fylles ut for trio",
                refType:"Velg mellom dommer og trio"
            },
            highlight:function (label) {
                $(label).closest('.control-group').addClass('error');
            },
            success:function (label) {
                label
                    .removeClass('error')
                    .closest('.control-group').removeClass('error');
            }
        });
}


function validateLogin() {

    $('#login-form').validate(
        {
            onkeyup: false,
            errorPlacement:function (error, element) {
                error.appendTo(element.next("span"));
            },
            rules:{
                email:{
                    required:true,
                    email:true
                },
                password:{
                    required:true
                }

            },
            messages:{
                email:{
                    required:"E-post må fylles ut",
                    email:"E-post-adressen er ikke gyldig"
                },
                password:{
                    required: "Passord må fylles ut",
                }
            },
            highlight:function (label) {
                $(label).closest('.control-group').addClass('error');
            },
            success:function (label) {
                label
                    .removeClass('error')
                    .closest('.control-group').removeClass('error');
            }
        });
}

function validateUserForm() {

    $('#user-form').validate(
        {
            onkeyup: false,
            errorPlacement:function (error, element) {
                error.appendTo(element.nextAll("span"));
            },
            rules:{
                email:{
                    required:true,
                    email:true
                },
                telephone:{
                    required:true,
                    digits:true,
                    rangelength: [8, 8]
                },
                name:{
                    required:true
                },
                level:{
                    required:true
                },
                refNumber:{
                  required:true,
                    digits:true
                },
                password:{
                    required:true,
                    minlength: 8
                },
                password2: {
                    required:true,
                    equalTo: "#password"
                }
            },
            messages:{
                name: "Navn må fylles ut",
                telephone: {
                    required: "Telefonnumer må fylles ut",
                    rangelength: "Telefonnummer skal bestå av {0} tegn",
                    digits: "Ugyldig telefonnummer"
                },
                level: "Fyll ut hvilket nivå du dømte på siste sesong",
                refNumber: {
                    required: "Dommernummer må fylles ut",
                    digits: "Dommernummer skal kun bestå av tall"
                },
                email:{
                    required:"E-post må fylles ut",
                    email:"E-post-adressen er ikke gyldig"
                },
                password:{
                    required:"Passord må fylles ut",
                    minlength: "Passordet må bestå av minst {0} tegn"
                },
                password2:{
                    required:"Du må gjenta passordet",
                    minlength: "Passordet må bestå av minst {0} tegn",
                    equalTo: "Begge passordene må være like"
                }
            },
            highlight:function (label) {
                $(label).closest('.control-group').addClass('error');
            },
            success:function (label) {
                label
                    .removeClass('error')
                    .closest('.control-group').removeClass('error');
            }
        });

}

function interestButtonFunctions(){
    $("#ref").click(function() {
        refBtn($(this))
    });
    $("#assRef").click(function() {
        refBtn($(this))
    });
}

function refBtn(button){
    var url = window.location.href
    if(button.attr("data-state") == "not-interested"){
        button.attr("disabled", "disabled");
        ajaxinterest("POST", url, button,function(){})
    }else if(button.attr("data-state") == "interested"){
        button.attr("disabled", "disabled");
        ajaxinterest("DELETE", url, button,function(){})
    }
}

function deleteResource(){
    $.ajax({
        type: "DELETE",
        url: window.location.href,
        success: function(data){window.location.href=data.href;}
    });
}

function sendMail(evt){
    var button = $(this)
    button.attr("disabled","disabled");
    $('.mail-processing').show();
    $.ajax({
        type: "POST",
        url: window.location.href.replace(/\/$/, "")+"/sendmail",
        success: function(data){
            $('.mail-processing').hide();
            $('.mail-success').fadeIn();
        },
        error: function(data){
            $('.mail-processing').hide();
            $('.mail-failure').fadeIn();
        },
        complete: function(){
            button.removeAttr("disabled");
            setTimeout(function(){$(".mail-status").fadeOut()}, 7000);
        }
    });
}

function ajaxinterest(method, url, button, onError){
    $.ajax({
        type: method,
        url: url + "?reftype="+button.attr("id"),
        error: function() {onError();},
        success: function(data){button.attr("data-state", data.newState);},
        complete: function(){button.removeAttr("disabled");}
    });
}

function setUserLink(){
    var userId = $(this).attr('value')
    if(userId)
        $(this).next('a').attr('href','/admin/users/'+userId);
    else
        $(this).next('a').removeAttr("href")

}

function validatePasswordResetForm() {

    $('#reset-password-form').validate(
        {
            onkeyup: false,
            errorPlacement:function (error, element) {
                error.appendTo(element.nextAll("span"));
            },
            rules:{

                password:{
                    required:true,
                    minlength: 8
                },
                password2: {
                    required:true,
                    equalTo: "#password"
                }
            },
            messages:{
                password:{
                    required:"Passord må fylles ut",
                    minlength: "Passordet må bestå av minst {0} tegn"
                },
                password2:{
                    required:"Du må gjenta passordet",
                    minlength: "Passordet må bestå av minst {0} tegn",
                    equalTo: "Passordene må være like"
                }
            },
            highlight:function (label) {
                $(label).closest('.control-group').addClass('error');
            },
            success:function (label) {
                label
                    .removeClass('error')
                    .closest('.control-group').removeClass('error');
            }
        });

}


