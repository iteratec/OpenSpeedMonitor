import {Application} from "../../../models/application.model";

export class ApplicationWithCsi extends Application {
  csi: number;

  constructor(application: Application) {
    super(application);
    this.csi = 0;
  }
}

export interface ApplicationList {
  isLoading: boolean;
  applications: ApplicationWithCsi[];
}
