"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
var core_1 = require("@angular/core");
var ApplicationSelectComponent = /** @class */ (function () {
    function ApplicationSelectComponent(jobGroupService) {
        this.jobGroupService = jobGroupService;
        this.onSelectedApplicationChanged = new core_1.EventEmitter();
        this.jobGroups$ = jobGroupService.activeOrRecentlyMeasured$;
    }
    ApplicationSelectComponent.prototype.onSelect = function () {
        console.log(this.application);
        this.onSelectedApplicationChanged.emit(this.application);
    };
    __decorate([
        core_1.Output()
    ], ApplicationSelectComponent.prototype, "onSelectedApplicationChanged", void 0);
    ApplicationSelectComponent = __decorate([
        core_1.Component({
            selector: 'osm-application-select',
            templateUrl: './application-select.component.html',
            styleUrls: ['./application-select.component.css']
        })
    ], ApplicationSelectComponent);
    return ApplicationSelectComponent;
}());
exports.ApplicationSelectComponent = ApplicationSelectComponent;
