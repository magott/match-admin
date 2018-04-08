window.onload = function() {
    console.debug("Laster matchevents");
    bootstrap();
};
var root = document.getElementById("event-table");
var eventLevel = function(level) {
    switch (level){
        case "success": return "label label-success";
        case "ok": return "label label-info";
        case "warn": return "label label-warning";
        case "error": return "label label-important";
    }
};

var Event = {
    list: [],
    loadList: function() {
        m.request({
            method: "GET",
            url: window.location + "/events",
            withCredentials: true,
        })
        .then(function(data) {
            Event.list = data;
            m.render(root,[
                m("thead", m("tr", [m("th","Tid"), m("th","Hendelse"),m("th","Beskrivelse"),m("th","Bruker")])),
                m("tbody", Event.list.map(function(event){
                return m("tr", [
                        m("td", moment(event.timestamp).format("DD.MM.YY HH:mm")),
                        m("td", m("span",{class: eventLevel(event.level)}, event.typ)),
                        m("td", event.description),
                        m("td", event.recipient)
                ]);
            }))]
            )
        })
    },
};

var bootstrap = function(){
    Event.loadList();
};

