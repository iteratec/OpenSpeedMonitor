"use strict";
var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
var JobGroup = /** @class */ (function () {
    function JobGroup() {
    }
    // constructor(id : number, name : string)
    // constructor(jsonObject : any)
    // constructor(idOrJson : number | any, nameOrNull?: string) {
    // }
    JobGroup.prototype.getId = function () {
        return this.id;
    };
    JobGroup.prototype.getName = function () {
        return this.name;
    };
    return JobGroup;
}());
exports.JobGroup = JobGroup;
var JobGroupFromJson = /** @class */ (function (_super) {
    __extends(JobGroupFromJson, _super);
    function JobGroupFromJson(JsonObject) {
        var _this = _super.call(this) || this;
        Object.assign(_this, JsonObject);
        return _this;
    }
    return JobGroupFromJson;
}(JobGroup));
exports.JobGroupFromJson = JobGroupFromJson;
var JobGroupFromParams = /** @class */ (function (_super) {
    __extends(JobGroupFromParams, _super);
    function JobGroupFromParams(id, name) {
        var _this = _super.call(this) || this;
        _this.id = id;
        _this.name = name;
        return _this;
    }
    return JobGroupFromParams;
}(JobGroup));
exports.JobGroupFromParams = JobGroupFromParams;
