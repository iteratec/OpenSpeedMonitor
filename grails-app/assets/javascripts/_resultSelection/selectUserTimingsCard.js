"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectUserTimings = (function() {
    var resetButtonElement = $(".reset-result-selection");
    var currentOptionValues = [];
    var cards = [];

    function Card(cardElementId, selectElementId){
        var cardElement = $(cardElementId);
        var userTimingsSelectElement = $(selectElementId);

        this.registerEvents = function(){
            if(cardElement && userTimingsSelectElement){
                userTimingsSelectElement.on("change", function () {
                    cardElement.trigger("userTimingSelectionChanged", {userTimings: userTimingsSelectElement.val()});
                });
                resetButtonElement.on("click", function () {
                    OpenSpeedMonitor.domUtils.deselectAllOptions(cardElement, true);
                })
            }
        };

        this.updateOptions = function (options) {
            if(cardElement && userTimingsSelectElement){
                OpenSpeedMonitor.domUtils.updateSelectOptionsNamesOnly(userTimingsSelectElement, options, options);
                userTimingsSelectElement.trigger("change");
            }
        };
    }

    function registerCard(cardElementId, selectElementId){
        var card = new Card(cardElementId, selectElementId);
        card.registerEvents();
        card.updateOptions(currentOptionValues);
        cards.push(card);
    }

    var init = function() {
        registerCard('#select-usertimings-cardUncached', "#userTimingsSelectHtmlIdUncached");
        registerCard('#select-usertimings-cardCached', "#userTimingsSelectHtmlIdCached");
    };


    var updateUserTimings = function(userTimings) {
        updateOptionsForAllCards(userTimings);

    };

    function updateOptionsForAllCards(userTimings) {
        currentOptionValues = userTimings;
        cards.forEach(function (card) {
            card.updateOptions(currentOptionValues);
        })
    }

    init();
    return {
        updateUserTimings: updateUserTimings,
        registerCard: registerCard
    }
})();
