import {CsiDTO} from "./csi.model";

export interface ApplicationCsiListDTO {
  csiDtoList: CsiDTO[],
  hasCsiConfiguration: boolean,
  hasJobResults: boolean,
  hasInvalidJobResults: boolean
}

