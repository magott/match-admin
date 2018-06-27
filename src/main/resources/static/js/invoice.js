var regning = document.getElementById("regning");
var createClicked = false;
console.debug("Laster regning..");

var Invoice = {
    status: {},
    matchId: _.head(_.takeRight(_.split(window.location, "/"))),
    loadStatus: function() {
        console.debug("Laster regning for "+Invoice.matchId)
        m.request({
            method: "GET",
            url: window.location.origin + "/admin/invoice/match/"+Invoice.matchId,
            withCredentials: true,
            extract: function(xhr) {return {status: xhr.status, body: xhr.responseText}}
        }).then(function (response) {
            if(response.status == 200){
                return Invoice.loadStatus2(JSON.parse(response.body));
            } else {
                m.render(regning, [
                    m("label", {class: "control-label"}, "Regningstatus"),
                    m("div", {class: "controls"}, [m("button", {id: "createButton", class: "btn", type: "button", onclick:(Invoice.createInvoice)}, "Opprett regning")])
                ])
            }
        })
    },
    //onclick: window.open(Invoice.status.url)
    loadStatus2: function(data) {
        m.request({
            method: "GET",
            url: window.location.origin +"/admin/invoice/" + data.invoiceNumber,
            withCredentials: true,
        }).then(function (response) {
            Invoice.status = response;
            m.render(regning, [
                m("label", {class: "control-label"}, "Regningstatus"),
                m("div",{class: "controls"}, [m("button", {class: "btn", type: "button", onclick: function () {window.open(Invoice.status.url)}}, Invoice.status.status)])
            ])
        }).catch(function() {
            m.render(regning, [
                m("label", {class: "control-label"}, "Regningstatus"),
                m("div", {class: "controls"}, [m("button", {id: "createButton", class: "btn", type: "button", onclick:(Invoice.createInvoice)}, "Opprett regning")])
            ])
        })
    },
    createInvoice: function() {
        document.getElementById("createButton").setAttribute("disabled","disabled")
        m.request({
            method: "POST",
            url: window.location.origin + "/admin/invoice/match/"+Invoice.matchId,
            withCredentials: true,
        }).then(function(response){
            Invoice.loadStatus2(response);
            document.getElementById("createButton").removeAttribute("disabled");
        })
    }
};
var bootstrapInvoice = function() {
  Invoice.loadStatus();
};
bootstrapInvoice();