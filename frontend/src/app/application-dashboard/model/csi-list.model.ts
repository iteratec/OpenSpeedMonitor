import {CsiDTO} from "./csi.model";

export interface CsiListDTO {
  jobGroupCsiDtos: CsiDTO[],
  hasCsiConfiguration: boolean
}
