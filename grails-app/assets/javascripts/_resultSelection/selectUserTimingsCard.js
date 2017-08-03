"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.selectUserTimings = (function() {
    var resetButtonElement = $(".reset-result-selection");
    var cards = [];

    function Card(cardDomElement){
        var cardElement = $(cardDomElement);
        var userTimingsSelectElement = cardElement.find('.select-usertimings-element-class');

        function registerEvents(){
            if(cardElement && userTimingsSelectElement){
                userTimingsSelectElement.on("change", function () {
                    cardElement.trigger("userTimingSelectionChanged", {userTimings: userTimingsSelectElement.val()});
                });
                resetButtonElement.on("click", function () {
                    OpenSpeedMonitor.domUtils.deselectAllOptions(cardElement, true);
                })
            }
        }

        this.updateOptions = function (options) {
            if(cardElement && userTimingsSelectElement){
                OpenSpeedMonitor.domUtils.updateSelectOptionsNamesOnly(userTimingsSelectElement, options, OpenSpeedMonitor.i18n.noResultsMsg);
                userTimingsSelectElement.trigger("change");
            }
        };

        registerEvents();
    }

    function init() {
        $('.select-usertimings-card-class').each(function (index, cardElement) {
            var card = new Card(cardElement);
            cards.push(card);
        })
    }


    var updateUserTimings = function(userTimings) {
        cards.forEach(function (card) {
            card.updateOptions(userTimings);
        })
    };


    init();
    return {
        updateUserTimings: updateUserTimings
    }
})();
