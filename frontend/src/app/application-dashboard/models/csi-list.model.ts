import {CsiDTO} from "./csi.model";

export interface ApplicationCsiListDTO {
  csiDtoList: CsiDTO[],
  hasCsiConfiguration: boolean,
  isLoading: boolean
}

