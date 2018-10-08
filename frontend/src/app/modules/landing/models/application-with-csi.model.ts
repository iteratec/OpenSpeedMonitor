import {Application} from "../../../models/application.model";
import {Csi} from "../../../models/csi.model";
import {ApplicationCsi} from "../../../models/csi-list.model";

export class ApplicationWithCsi extends Application {
  recentCsi: Csi;
  csiIsLoading: boolean;

  constructor(application: Application, applicationCsi: ApplicationCsi) {
    super(application);
    this.recentCsi = new Csi(applicationCsi ? applicationCsi.recentCsi() : {});
    this.csiIsLoading = applicationCsi ? applicationCsi.isLoading : false;
  }
}
