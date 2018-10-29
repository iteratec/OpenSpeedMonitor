import {Application} from "../../../models/application.model";
import {Csi} from "../../../models/csi.model";
import {ApplicationCsi} from "../../../models/csi-list.model";

export class ApplicationWithCsi extends Application {
  recentCsi: Csi;
  csiIsLoading: boolean;

  constructor(application: Application, applicationCsi: ApplicationCsi, isLoading: boolean) {
    super(application);
    const recentCsi = applicationCsi ? applicationCsi.recentCsi() : null;
    this.recentCsi = new Csi(recentCsi || {});
    this.csiIsLoading = isLoading;
  }
}
