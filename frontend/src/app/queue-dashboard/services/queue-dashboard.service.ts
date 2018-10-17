import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {EMPTY, ReplaySubject} from "rxjs/index";
import {WptServerDTO} from "../models/WptServerDTO";
import {catchError, map} from "rxjs/internal/operators";
import {EmptyExpr} from "@angular/compiler";


@Injectable()
export class QueueDashboardService {

  private http: HttpClient;

  activeServer$ = new ReplaySubject<WptServerDTO[]>(1)

  constructor (httpClient: HttpClient){
    this.http = httpClient

    this.getActiveWptServer().subscribe(
      value => value.forEach( (val) => this.getLocationsForWptServer(val.id))
    )
  }

  private getActiveWptServer(){
    return this.http.get<WptServerDTO[]>("/queueDashboard/rest/getActiveWptServer").pipe(
      catchError( error => {this.handleError(error); return EMPTY})
    )
  }

  private getLocationsForWptServer(serverId: number){

    const params = new HttpParams().set("id", serverId.toString())
    this.http.post<WptServerDTO[]>("/queueDashboard/rest/getLocationListForWptServer", {params}).subscribe(
      val => console.log(val),
      error => this.handleError(error)
    )
  }

  private handleError(error: any){
    console.log(error)
  }
}

