function trioOn () {
    $('#assFee').show('fast').removeAttr("disabled");
    $('#appointedAssistant1').show('fast').removeAttr("disabled");
    $('#appointedAssistant2').show('fast').removeAttr("disabled");
}

function trioOff() {
    $('#assFee').hide().attr("disabled", "disabled");
    $('#appointedAssistant1').hide().attr("disabled", "disabled");
    $('#appointedAssistant2').hide().attr("disabled", "disabled");
}

function isTrio(){
    var refType = $('form input[name=refType]:checked').val()
    return  refType == "trio"
}

function editMatchFunctions () {
    $('#refTypeDommer').click(trioOff);
    $('#refTypeTrio').click(trioOn);
    validateMatchForm();
    if(!isTrio()){trioOff();}
}

function validateMatchForm(){

    $('#match-form').validate(
        {
            errorPlacement: function(error, element) {
                error.appendTo( element.nextAll("span") );
            },
            rules: {
                home: {
                    required: true
                },
                away: {
                    required: true
                },
                venue: {
                    required: true
                },
                date: {
                    required: true
                },
                time: {
                    required: true
                },
                level: {
                    required: true
                },
                refType: {
                    required: true
                },
                refFee: {
                    required: true
                },
                assFee: {
                    depends: function(element) {
                        return $("#refTypeTrio:checked")
                    }
                }
            },
            messages: {
                home: "Hjemmelag må fylles ut",
                away: "Bortelag må fylles ut",
                venue: "Bane må fylles ut",
                name: "Navn må fylles ut",
                time: "Tidspunkt for kampen må fylles ut",
                date: "Dato for kampen må fylles ut",
                level: "Nivå for kampen må fylles ut",
                refFee: "Dommerhonorar må fylles ut",
                assFee: "AD-honorar må fylles ut for trio",
                refType: "Velg mellom dommer og trio"
            },
            highlight: function(label) {
                $(label).closest('.control-group').addClass('error');
            },
            success: function(label) {
                label
                    .removeClass('error')
                    .closest('.control-group').removeClass('error');
            }
        });
}