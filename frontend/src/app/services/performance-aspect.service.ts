import {Injectable} from '@angular/core';
import {BehaviorSubject, EMPTY} from "rxjs";
import {PerformanceAspectType} from "../models/perfomance-aspect.model";
import {HttpClient} from "@angular/common/http";
import {catchError} from "rxjs/operators";
import {ResponseWithLoadingState} from "../models/response-with-loading-state.model";

@Injectable({
  providedIn: 'root'
})
export class PerformanceAspectService {

  aspectTypes$ = new BehaviorSubject<ResponseWithLoadingState<PerformanceAspectType[]>>({isLoading: false, data: []});

  constructor(private http: HttpClient) {
    this.loadAspectTypes();
  }

  loadAspectTypes() {
    this.aspectTypes$.next({...this.aspectTypes$.getValue(), isLoading: true});
    this.http.get<PerformanceAspectType[]>("/aspectConfiguration/rest/getAspectTypes").pipe(
      catchError((error) => {
        console.error(error);
        return EMPTY;
      })).subscribe((types: PerformanceAspectType[]) => {
        types.map((type) => {
          type.kind = "performance-aspect-type"
        });
        this.aspectTypes$.next({isLoading: false, data: types})
    });
  }
}
