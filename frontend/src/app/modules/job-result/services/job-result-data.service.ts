import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {URL} from '../../../enums/url.enum';
import {catchError, tap} from 'rxjs/operators';
import {JobResult} from '../models/job-result.model';

@Injectable({
  providedIn: 'root'
})
export class JobResultDataService {

  constructor(private http: HttpClient) {
  }

  getJobResults(jobId: number): Observable<JobResult[]> {
    const params = {jobId: jobId.toString(10)};
    return this.http.get<JobResult[]>(URL.JOB_RESULTS, {params: params})
      .pipe(
        catchError(this.handleError<JobResult[]>('getJobResults', []))
      );
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(operation, error.message);
      return of(result as T);
    };
  }
}
