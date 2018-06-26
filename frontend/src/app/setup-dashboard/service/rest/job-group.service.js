"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
var core_1 = require("@angular/core");
var rxjs_1 = require("rxjs");
var JobGroupService = /** @class */ (function () {
    function JobGroupService(http) {
        this.http = http;
        this.jobGroups$ = new rxjs_1.ReplaySubject(1);
        this.updateActiveJobGroups();
    }
    JobGroupService.prototype.updateActiveJobGroups = function () {
        var _this = this;
        this.http.get("jobGroup/getAllActive")
            .subscribe(function (next) { return _this.jobGroups$.next(next); }, function (error) { return _this.handleError(error); });
    };
    JobGroupService.prototype.handleError = function (error) {
        console.log(error);
    };
    JobGroupService.prototype.getJobGroupToPagesMapDto = function (from, to) {
        return this.http.get('/jobGroup/getJobGroupsWithPages', {
            params: {
                from: from,
                to: to
            }
        });
    };
    JobGroupService = __decorate([
        core_1.Injectable({
            providedIn: 'root'
        })
    ], JobGroupService);
    return JobGroupService;
}());
exports.JobGroupService = JobGroupService;
