function trioOn() {
    $('#assFee').show('fast').removeAttr("disabled");
    $('#appointedAssistant1').show('fast').removeAttr("disabled");
    $('#appointedAssistant2').show('fast').removeAttr("disabled");
}

function trioOff() {
    $('#assFee').hide().attr("disabled", "disabled");
    $('#appointedAssistant1').hide().attr("disabled", "disabled");
    $('#appointedAssistant2').hide().attr("disabled", "disabled");
}

function isTrio() {
    var refType = $('form input[name=refType]:checked').val()
    return  refType == "trio"
}

function editMatchFunctions() {
    $('#refTypeDommer').click(trioOff);
    $('#refTypeTrio').click(trioOn);
    validateMatchForm();
    if (!isTrio()) {
        trioOff();
    }
}

function validateMatchForm() {

    $('#match-form').validate(
        {
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
                    required:true
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
                time:"Tidspunkt for kampen må fylles ut",
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