import {Injectable} from '@angular/core';
import {EMPTY, ReplaySubject} from "rxjs";
import {PerformanceAspectType} from "../models/perfomance-aspect.model";
import {HttpClient} from "@angular/common/http";
import {catchError} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class PerformanceAspectService {

  aspectTypes$ = new ReplaySubject<PerformanceAspectType[]>(1);

  constructor(private http: HttpClient) {
    this.loadAspectTypes();
  }

  loadAspectTypes() {
    this.http.get<PerformanceAspectType[]>("/aspectConfiguration/rest/getAspectTypes").pipe(
      catchError((error) => {
        console.error(error);
        return EMPTY;
      })).subscribe((types: PerformanceAspectType[]) => this.aspectTypes$.next(types));
  }
}
