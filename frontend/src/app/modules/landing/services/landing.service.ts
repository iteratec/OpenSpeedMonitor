import { Injectable } from '@angular/core';
import {ApplicationList} from "../models/application-list.model";
import {BehaviorSubject} from "rxjs/internal/BehaviorSubject";
import {Application} from "../models/application.model";

@Injectable()
export class LandingService {

  applicationList$ = new BehaviorSubject<ApplicationList>({isLoading: true, applications: []});

  constructor() {
    setTimeout(() => {
      this.applicationList$.next({
        isLoading: false,
        applications: [
          new Application({
            id: 1,
            name: "otto.de Desktop",
            numPages: 8,
            csiDate: "2018-10-01",
            lastResultDate: "2018-10-01",
            csi: 76.0
          }),
          new Application({
            id: 2,
            name: "otto.de Smartphone",
            numPages: 10,
            csiDate: "2018-09-28",
            lastResultDate: "2018-10-01",
            csi: 66.0
          })
        ]
      })
    }, 2000);
  }
}
