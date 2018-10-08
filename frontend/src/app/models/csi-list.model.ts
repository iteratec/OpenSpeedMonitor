import {Csi, CsiDTO} from "./csi.model";

export interface ApplicationCsiDTO {
  csiValues?: CsiDTO[];
  hasCsiConfiguration?: boolean;
  hasJobResults?: boolean;
  hasInvalidJobResults?: boolean;
  isLoading?: boolean;
}

export class ApplicationCsi implements ApplicationCsiDTO{
  csiValues: Csi[];
  isLoading: boolean;
  hasCsiConfiguration: boolean;
  hasJobResults: boolean;
  hasInvalidJobResults: boolean;

  constructor(dto: ApplicationCsiDTO) {
    this.csiValues = dto.csiValues ? dto.csiValues.map(csiDto => new Csi(csiDto)) : [];
    this.hasCsiConfiguration = !!dto.hasCsiConfiguration;
    this.hasJobResults = !!dto.hasJobResults;
    this.hasInvalidJobResults = !!dto.hasInvalidJobResults;
    this.isLoading = !!dto.isLoading;
  }

  recentCsi(): Csi {
    return this.csiValues.slice(-1)[0];
  }
}

export interface ApplicationCsiDTOById {
  [applicationId: number]: ApplicationCsiDTO;
}

export interface ApplicationCsiById {
  [applicationId: number]: ApplicationCsi;
}
