import {Csi, CsiDTO} from "./csi.model";

export interface ApplicationCsiDTO {
  csiDtoList?: CsiDTO[];
  hasCsiConfiguration?: boolean;
  hasJobResults?: boolean;
  hasInvalidJobResults?: boolean;
  isLoading?: boolean;
}

export class ApplicationCsi {
  csiValues: Csi[];
  isLoading: boolean;
  hasCsiConfiguration: boolean;
  hasJobResults: boolean;
  hasInvalidJobResults: boolean;

  constructor(dto: ApplicationCsiDTO) {
    this.csiValues = dto.csiDtoList ? dto.csiDtoList.map(csiDto => new Csi(csiDto)) : [];
    this.hasCsiConfiguration = !!dto.hasCsiConfiguration;
    this.hasJobResults = !!dto.hasJobResults;
    this.hasInvalidJobResults = !!dto.hasInvalidJobResults;
    this.isLoading = !!dto.isLoading;
  }

  recentCsi(): Csi {
    return this.csiValues.slice(-1)[0];
  }
}

export interface ApplicationCsiById {
  [applicationId: number]: ApplicationCsi;
}
