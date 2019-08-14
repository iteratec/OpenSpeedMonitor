import {Injectable} from '@angular/core';
import {BehaviorSubject, EMPTY} from "rxjs";
import {PerformanceAspectType} from "../models/perfomance-aspect.model";
import {HttpClient} from "@angular/common/http";
import {catchError} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class PerformanceAspectService {

  aspectTypes$ = new BehaviorSubject<PerformanceAspectType[]>([]);

  constructor(private http: HttpClient) {
    this.loadAspectTypes();
  }

  loadAspectTypes() {
    this.http.get<PerformanceAspectType[]>("/aspectConfiguration/rest/getAspectTypes").pipe(
      catchError((error) => {
        console.error(error);
        return EMPTY;
      })).subscribe((types: PerformanceAspectType[]) => {
        types.map((type) => {
          type.kind = "performance-aspect-type"
        });
        this.aspectTypes$.next(types)
    });
  }
}
