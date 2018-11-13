import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {BehaviorSubject, ReplaySubject} from "rxjs/index";
import {WptServerDTO} from "../models/WptServerDTO";
import {LocationInfoDTO} from "../models/LocationInfoDTO";

export interface ServerInfo {
  [serverId: number]: LocationInfoDTO[],
}

@Injectable()
export class QueueDashboardService {
  activeServers$ = new ReplaySubject<WptServerDTO[]>(1);
  serverInfo$ = new BehaviorSubject<ServerInfo>({});

  constructor (private http: HttpClient){}

  getActiveWptServer(){
    this.http.get<WptServerDTO[]>("/queueDashboard/rest/getActiveWptServer").subscribe(
      value => this.activeServers$.next(value),
      error1 => this.handleError( error1 )
    )
  }

  getInfoTableForWptServer(serverId: number){
    let params = new HttpParams().set("id", serverId.toString());
    this.http.post<LocationInfoDTO[]>("/queueDashboard/rest/getWptServerInformation", params).subscribe(
      value => {
        this.serverInfo$.next( {
          ...this.serverInfo$.getValue(),
          [serverId]: value
        })},
      error => this.handleError(error)
    )
  }

  private handleError(error: any){
    console.log(error)
  }
}
