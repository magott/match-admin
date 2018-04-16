window.onload = function() {
    console.debug("Laster..");
    bootstrap();
};
var select = document.getElementById("stats-select");
var dataelement = document.getElementById("stats-data");
var currentSeason = moment().quarter >= 3 ? moment().year() : moment().year()-1;
var Stats = {
    seasons: function() {
        return _.range(currentSeason, 2012).map(function(y){
            return {key: y, value: y + "/" +(y+1)}
        });
    },
    list: [],
    loadList: function(year) {
        m.request({
            method: "GET",
            url: window.location + "?season="+year,
            withCredentials: true,
        }).then(function(data) {
            Stats.list = data;
            m.render(dataelement, Stats.list.map(function(stat){
                return m("p", stat);
            }));
        })
    },
    change: function(data){
        Stats.loadList(data.value);
    },
    init: function(){
        m.render(select, m("select", { onchange : function(){ Stats.change(this); }}, Stats.seasons().map(function(s){
            return m("option",{value: s.key}, s.value);
        })))
        Stats.loadList(currentSeason);
    },
};


var bootstrap = function(){
    Stats.init();
};
